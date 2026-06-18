package com.azarenka.eve.evebuilders.controller.auth;

import com.azarenka.eve.evebuilders.controller.config.ui.RefreshCookie;
import com.azarenka.evebuilders.domain.auth.auth.ui.AccessTokenResponse;
import com.azarenka.evebuilders.domain.auth.auth.ui.JwtProperties;
import com.azarenka.evebuilders.domain.auth.auth.ui.LoginRequest;
import com.azarenka.evebuilders.service.impl.auth.eve.ui.JwtService;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.time.Instant;
import java.util.Optional;
import java.util.Map;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
//@RequestMapping("/api/auth")
@RequestMapping("/api")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final JwtProperties props;
    private final RefreshCookie refreshCookie;

    public AuthController(AuthenticationManager authenticationManager,
                          UserDetailsService userDetailsService,
                          JwtService jwtService,
                          JwtProperties props,
                          RefreshCookie refreshCookie) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
        this.props = props;
        this.refreshCookie = refreshCookie;
    }

    @PostMapping("/login")
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

    @PostMapping("/refresh")
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

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletResponse response) {
        refreshCookie.clearRefreshCookie(response);
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
}
