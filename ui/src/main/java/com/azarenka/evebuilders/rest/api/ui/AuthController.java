package com.azarenka.evebuilders.rest.api.ui;

import com.azarenka.evebuilders.domain.auth.auth.ui.AccessTokenResponse;
import com.azarenka.evebuilders.domain.auth.auth.ui.AuthProfile;
import com.azarenka.evebuilders.domain.auth.auth.ui.EveExchangeRequest;
import com.azarenka.evebuilders.domain.auth.auth.ui.JwtProperties;
import com.azarenka.evebuilders.domain.auth.auth.ui.LoginRequest;
import com.azarenka.evebuilders.domain.auth.auth.ui.MeResponse;
import com.azarenka.evebuilders.domain.db.User;
import com.azarenka.evebuilders.domain.dto.EveUserPrincipal;
import com.azarenka.evebuilders.domain.db.TokenResponse;
import com.azarenka.evebuilders.repository.database.IUserRepository;
import com.azarenka.evebuilders.service.api.IAccessControlService;
import com.azarenka.evebuilders.service.impl.auth.eve.EveOAuth2UserService;
import com.azarenka.evebuilders.service.impl.auth.eve.EveAuthService;
import com.azarenka.evebuilders.service.impl.auth.eve.ui.JwtService;
import com.azarenka.evebuilders.service.impl.intergarion.EveCharacterService;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final JwtProperties props;
    private final RefreshCookie refreshCookie;
    private final EveAuthService eveAuthService;
    private final EveOAuth2UserService eveOAuth2UserService;
    private final IUserRepository userRepository;
    private final IAccessControlService accessControlService;
    private final EveCharacterService eveCharacterService;

    public AuthController(AuthenticationManager authenticationManager,
                          UserDetailsService userDetailsService,
                          JwtService jwtService,
                          JwtProperties props,
                          RefreshCookie refreshCookie,
                          EveAuthService eveAuthService,
                          EveOAuth2UserService eveOAuth2UserService,
                          IUserRepository userRepository,
                          IAccessControlService accessControlService,
                          EveCharacterService eveCharacterService) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
        this.props = props;
        this.refreshCookie = refreshCookie;
        this.eveAuthService = eveAuthService;
        this.eveOAuth2UserService = eveOAuth2UserService;
        this.userRepository = userRepository;
        this.accessControlService = accessControlService;
        this.eveCharacterService = eveCharacterService;
    }

    @PostMapping("/auth/login")
    public AccessTokenResponse login(@RequestBody LoginRequest req, HttpServletResponse response) {
        Authentication auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(req.username(), req.password())
        );
        UserDetails user = (UserDetails) auth.getPrincipal();
        String access = jwtService.generateAccessToken(user);
        String refresh = jwtService.generateRefreshToken(user);
        refreshCookie.setRefreshCookie(response, refresh);
        return new AccessTokenResponse(access);
    }

    @PostMapping("/auth/eve/exchange")
    public AccessTokenResponse eveExchange(@RequestBody EveExchangeRequest req, HttpServletResponse response) {
        if (req == null || req.code() == null || req.code().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "code is required");
        }

        try {
            TokenResponse tokenResponse = eveAuthService.exchangeCodeForToken(req.code());
            User user = eveOAuth2UserService.authenticateByAccessToken(tokenResponse.getAccessToken());
            UserDetails principal = new EveUserPrincipal(user, Map.of());

            String access = jwtService.generateAccessToken(principal);
            String refresh = jwtService.generateRefreshToken(principal);
            refreshCookie.setRefreshCookie(response, refresh);
            return new AccessTokenResponse(access);
        } catch (OAuth2AuthenticationException ex) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, ex.getMessage(), ex);
        } catch (RuntimeException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "EVE exchange failed", ex);
        }
    }

    @PostMapping("/auth/refresh")
    public AccessTokenResponse refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshJwt = readCookie(request, props.refreshCookieName())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No refresh cookie"));
        if (!jwtService.isRefreshToken(refreshJwt)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }
        String username = jwtService.extractUsername(refreshJwt);
        UserDetails user = userDetailsService.loadUserByUsername(username);
        String newRefresh = jwtService.generateRefreshToken(user);
        refreshCookie.setRefreshCookie(response, newRefresh);

        String newAccess = jwtService.generateAccessToken(user);
        return new AccessTokenResponse(newAccess);
    }

    @PostMapping("/auth/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletResponse response) {
        refreshCookie.clearRefreshCookie(response);
    }

    @GetMapping({"/auth/me", "/auth/profile"})
    public MeResponse me(Authentication authentication, HttpServletRequest request) {
        User user = resolveCurrentUser(authentication, request);
        AuthProfile authProfile = accessControlService.getAuthProfile(user.getUid());
        String corporationId = eveCharacterService.getParameter(user.getCharacterInfo(), "corporation_id", String.class);
        return new MeResponse(
            user.getUid(),
            user.getCharacterId(),
            user.getUsername(),
            corporationId,
            user.getCorporationName(),
            authProfile.roles(),
            authProfile.permissions(),
            authProfile.superAdmin()
        );
    }

    @GetMapping("/ping")
    public Map<String, Object> ping(Authentication authentication) {
        return Map.of(
            "ok", true,
            "ts", Instant.now().toString(),
            "user", authentication != null ? authentication.getName() : null
        );
    }

    private Optional<String> readCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        return Arrays.stream(cookies)
            .filter(c -> name.equals(c.getName()))
            .map(Cookie::getValue)
            .findFirst();
    }

    private User resolveCurrentUser(Authentication authentication, HttpServletRequest request) {
        Authentication currentAuthentication = authentication;
        if (currentAuthentication == null) {
            currentAuthentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        }
        if (currentAuthentication != null && currentAuthentication.isAuthenticated()
            && currentAuthentication.getName() != null && !"anonymousUser".equals(currentAuthentication.getName())) {
            return userRepository.findByUsername(currentAuthentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated"));
        }

        String refreshJwt = readCookie(request, props.refreshCookieName())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated"));
        if (!jwtService.isRefreshToken(refreshJwt)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        String username = jwtService.extractUsername(refreshJwt);
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated"));
    }
}
