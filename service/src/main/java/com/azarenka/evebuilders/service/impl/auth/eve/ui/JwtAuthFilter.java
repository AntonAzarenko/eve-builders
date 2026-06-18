package com.azarenka.evebuilders.service.impl.auth.eve.ui;

import com.azarenka.evebuilders.domain.auth.auth.ui.JwtProperties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final JwtProperties props;

    public JwtAuthFilter(JwtService jwtService, UserDetailsService userDetailsService, JwtProperties props) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.props = props;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring("Bearer ".length()).trim();
            try {
                String username = jwtService.extractUsername(token);
                if (username != null && !hasAuthenticatedPrincipal()) {
                    authenticate(username, request);
                }
            } catch (Exception ex) {
                LOGGER.debug("JWT authentication failed for {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
            }
        }

        if (!hasAuthenticatedPrincipal()) {
            authenticateFromRefreshCookie(request);
        }

        filterChain.doFilter(request, response);
    }

    private void authenticateFromRefreshCookie(HttpServletRequest request) {
        Optional<String> refreshJwt = readCookie(request, props.refreshCookieName());
        if (refreshJwt.isEmpty()) {
            return;
        }
        try {
            if (!jwtService.isRefreshToken(refreshJwt.get())) {
                return;
            }
            String username = jwtService.extractUsername(refreshJwt.get());
            if (username != null && !hasAuthenticatedPrincipal()) {
                authenticate(username, request);
                LOGGER.info(
                    "Refresh-cookie request. User={}, Time={}, Method={}, URI={}",
                    username,
                    LocalDateTime.now(),
                    request.getMethod(),
                    request.getRequestURI()
                );
            }
        } catch (Exception ex) {
            LOGGER.debug("Refresh-cookie authentication failed for {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        }
    }

    private void authenticate(String username, HttpServletRequest request) {
        UserDetails user = userDetailsService.loadUserByUsername(username);
        UsernamePasswordAuthenticationToken auth =
            new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(auth);
        LOGGER.info(
            "JWT request. User={}, Time={}, Method={}, URI={}",
            username,
            LocalDateTime.now(),
            request.getMethod(),
            request.getRequestURI()
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

    private boolean hasAuthenticatedPrincipal() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        if (authentication instanceof AnonymousAuthenticationToken) {
            return false;
        }
        return authentication.isAuthenticated() && authentication.getPrincipal() != null;
    }
}
