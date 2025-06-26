package com.azarenka.evebuilders.service.impl.auth;

import com.azarenka.evebuilders.domain.dto.EveUserPrincipal;
import com.azarenka.evebuilders.service.impl.intergarion.TokenRefresherService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class EveAuthenticationSuccessHandler  implements AuthenticationSuccessHandler {

    private final TokenRefresherService tokenRefresherService;

    public EveAuthenticationSuccessHandler(TokenRefresherService tokenRefresherService) {
        this.tokenRefresherService = tokenRefresherService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        if (authentication.getPrincipal() instanceof EveUserPrincipal principal) {
            tokenRefresherService.setUidCookie(principal.getUser().getUid(), response);
        }
        // редирект или просто продолжение
        response.sendRedirect("/landing");
    }
}
