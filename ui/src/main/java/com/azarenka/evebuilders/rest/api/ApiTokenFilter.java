package com.azarenka.evebuilders.rest.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class ApiTokenFilter extends OncePerRequestFilter {

    @Value("${casino.api-token}")
    private String apiToken;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String uri = request.getRequestURI();
        if (uri.startsWith("/api/v1/casino/")) {
            String token = request.getHeader("X-API-TOKEN");

            if (token == null || !token.equals(apiToken)) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or missing API token");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
