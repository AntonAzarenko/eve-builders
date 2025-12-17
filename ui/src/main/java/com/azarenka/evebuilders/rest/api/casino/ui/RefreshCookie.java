package com.azarenka.evebuilders.rest.api.casino.ui;

import com.azarenka.evebuilders.domain.auth.auth.ui.JwtProperties;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletResponse;

@Component
public class RefreshCookie {

    private final JwtProperties props;

    public RefreshCookie(JwtProperties props) {
        this.props = props;
    }

    public void setRefreshCookie(HttpServletResponse response, String refreshJwt) {
        ResponseCookie cookie = ResponseCookie.from(props.refreshCookieName(), refreshJwt)
            .httpOnly(true)
            .secure(false)
            .sameSite("Lax")
            .path("/api/auth")
            .maxAge(props.refreshTtlSeconds())
            .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }

    public void clearRefreshCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(props.refreshCookieName(), "")
            .httpOnly(true)
            .secure(false)
            .sameSite("Lax")
            .path("/api/auth")
            .maxAge(0)
            .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }
}
