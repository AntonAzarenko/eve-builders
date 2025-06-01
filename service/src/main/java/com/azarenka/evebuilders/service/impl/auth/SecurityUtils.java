package com.azarenka.evebuilders.service.impl.auth;

import com.azarenka.evebuilders.domain.mysql.Role;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class SecurityUtils {

    public static String getUserName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(Objects.nonNull(authentication.getPrincipal())
                && authentication.getPrincipal() instanceof UserDetails principal) {
            return principal.getUsername();
        }
        return null;
    }

    public static Collection<Role> getUserRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(Objects.nonNull(authentication.getPrincipal())
                && authentication.getPrincipal() instanceof UserDetails principal) {
            return (Collection<Role>) principal.getAuthorities();
        }
        return null;
    }
}
