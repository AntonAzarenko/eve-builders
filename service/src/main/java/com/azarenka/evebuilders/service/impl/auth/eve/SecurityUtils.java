package com.azarenka.evebuilders.service.impl.auth.eve;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Optional;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public static Optional<Authentication> getCurrentAuthentication() {
        Authentication authentication = getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        return Optional.of(authentication);
    }

    public static Optional<Object> getPrincipal() {
        return getCurrentAuthentication()
            .map(Authentication::getPrincipal)
            .filter(principal -> principal != null && !(principal instanceof String s && "anonymousUser".equals(s)));
    }

    public static Optional<String> getCurrentUserName() {
        return getCurrentAuthentication()
            .map(SecurityUtils::resolveUserName)
            .filter(name -> !name.isBlank());
    }

    public static String getUserName() {
        return getCurrentUserName().orElse(null);
    }

    private static String resolveUserName(Authentication authentication) {
        if (authentication.getName() != null && !authentication.getName().isBlank() && !"anonymousUser".equals(authentication.getName())) {
            return authentication.getName();
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }
        if (principal instanceof OAuth2User oauth2User) {
            return oauth2User.getName();
        }
        if (principal instanceof String value) {
            return value;
        }
        return null;
    }
}
