package com.azarenka.evebuilders.service.impl.auth.eve;

import com.azarenka.evebuilders.domain.db.User;
import com.azarenka.evebuilders.repository.database.IUserRepository;
import com.azarenka.evebuilders.service.api.IAccessControlService;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

@Component("accessControlSecurity")
public class AccessControlSecurity {

    private final IAccessControlService accessControlService;
    private final IUserRepository userRepository;

    public AccessControlSecurity(IAccessControlService accessControlService,
                                 IUserRepository userRepository) {
        this.accessControlService = accessControlService;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public boolean can(String permissionCode) {
        return hasCurrentUserPermission(permissionCode);
    }

    @Transactional(readOnly = true)
    public boolean hasCurrentUserPermission(String permissionCode) {
        String userId = currentUserId();
        if (userId == null) {
            return false;
        }
        return isSuperAdmin(userId) || accessControlService.hasPermission(userId, permissionCode);
    }

    @Transactional(readOnly = true)
    public boolean canAny(String... permissionCodes) {
        String userId = currentUserId();
        if (userId == null) {
            return false;
        }
        if (isSuperAdmin(userId)) {
            return true;
        }
        if (permissionCodes == null || permissionCodes.length == 0) {
            return false;
        }
        return accessControlService.hasAnyPermission(userId, normalize(permissionCodes));
    }

    @Transactional(readOnly = true)
    public boolean canAll(String... permissionCodes) {
        String userId = currentUserId();
        if (userId == null) {
            return false;
        }
        if (isSuperAdmin(userId)) {
            return true;
        }
        if (permissionCodes == null || permissionCodes.length == 0) {
            return true;
        }
        return accessControlService.hasAllPermissions(userId, normalize(permissionCodes));
    }

    @Transactional(readOnly = true)
    public boolean hasPermission(String userId, String permissionCode) {
        if (userId == null || userId.isBlank()) {
            return false;
        }
        return isSuperAdmin(userId) || accessControlService.hasPermission(userId, permissionCode);
    }

    @Transactional(readOnly = true)
    public boolean isSuperAdmin() {
        String userId = currentUserId();
        return userId != null && accessControlService.isSuperAdmin(userId);
    }

    private boolean isSuperAdmin(String userId) {
        return accessControlService.isSuperAdmin(userId);
    }

    private Set<String> normalize(String... permissionCodes) {
        return Arrays.stream(permissionCodes)
            .filter(Objects::nonNull)
            .map(String::trim)
            .map(String::toUpperCase)
            .filter(code -> !code.isBlank())
            .collect(java.util.stream.Collectors.toSet());
    }

    private String currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }
        String username = authentication.getName();
        if (username == null || username.isBlank() || Objects.equals(username, "anonymousUser")) {
            return null;
        }
        return userRepository.findByUsername(username)
            .map(User::getUid)
            .orElse(null);
    }
}
