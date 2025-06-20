package com.azarenka.evebuilders.service.impl.auth;

import com.azarenka.evebuilders.domain.db.Role;
import com.azarenka.evebuilders.domain.db.User;
import com.azarenka.evebuilders.domain.dto.EveUserPrincipal;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;
import java.util.Collections;

public class SecurityUtils {

    public static String getUserName() {
        var principal = getCurrentPrincipal();
        return principal != null ? principal.getUsername() : null;
    }

    public static Collection<Role> getUserRoles() {
        var principal = getCurrentPrincipal();
        if (principal != null && principal.getUser() != null) {
            return principal.getUser().getRoles();
        }
        return Collections.emptyList();
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
