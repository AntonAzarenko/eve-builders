package com.azarenka.evebuilders.service.impl.auth.eve;

import com.azarenka.evebuilders.domain.dto.EveUserPrincipal;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    public static String getUserName() {
        var principal = getCurrentPrincipal();
        return principal != null ? principal.getUsername() : null;
    }

    private static EveUserPrincipal getCurrentPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
            && authentication.getPrincipal() instanceof EveUserPrincipal principal) {
            return principal;
        }
        return null;
    }
}
