package com.azarenka.evebuilders.service.impl.auth;

import com.azarenka.evebuilders.domain.db.User;
import com.azarenka.evebuilders.domain.db.UserToken;
import com.azarenka.evebuilders.domain.dto.EveUserPrincipal;
import com.azarenka.evebuilders.service.impl.UserService;
import com.azarenka.evebuilders.service.impl.UserTokenService;
import com.azarenka.evebuilders.service.impl.intergarion.TokenRefresherService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

@Component
public class CookieAuthFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(CookieAuthFilter.class);

    private final UserTokenService userTokenService;
    private final UserService userService;
    private final TokenRefresherService tokenRefresherService;

    public CookieAuthFilter(UserTokenService userTokenService,
                            UserService userService,
                            TokenRefresherService tokenRefresherService) {
        this.userTokenService = userTokenService;
        this.userService = userService;
        this.tokenRefresherService = tokenRefresherService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            String uid = extractUidFromCookie(request);
            if (uid != null) {
                UserToken token = userTokenService.findByUserId(uid);
                if (token != null) {
                    User user = userService.getByUserId(uid).orElseThrow();
                    if (token.getExpiresAt().isBefore(LocalDateTime.now().plusSeconds(60))) {
                        tokenRefresherService.refresh(token);
                        LOGGER.info("User {} token refreshed.", user.getUsername());
                    }
                    EveUserPrincipal principal = new EveUserPrincipal(user, Map.of());
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    private String extractUidFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if ("UID".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
