package com.azarenka.evebuilders.service.impl.auth.eve;

import com.azarenka.evebuilders.domain.acl.Role;
import com.azarenka.evebuilders.domain.acl.RolePermission;
import com.azarenka.evebuilders.domain.acl.UserPermission;
import com.azarenka.evebuilders.domain.auth.auth.ui.AuthProfile;
import com.azarenka.evebuilders.domain.db.Permission;
import com.azarenka.evebuilders.domain.db.User;
import com.azarenka.evebuilders.repository.database.IUserRepository;
import com.azarenka.evebuilders.repository.database.acl.IPermissionRepository;
import com.azarenka.evebuilders.repository.database.acl.IRolePermissionRepository;
import com.azarenka.evebuilders.repository.database.acl.IUserPermissionRepository;
import com.azarenka.evebuilders.repository.database.acl.IUserRoleRepository;
import com.azarenka.evebuilders.service.config.AccessControlCacheConfig;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AccessControlQueryService {

    private final IUserRepository userRepository;
    private final IUserRoleRepository userRoleRepository;
    private final IRolePermissionRepository rolePermissionRepository;
    private final IUserPermissionRepository userPermissionRepository;
    private final IPermissionRepository permissionRepository;

    public AccessControlQueryService(IUserRepository userRepository,
                                     IUserRoleRepository userRoleRepository,
                                     IRolePermissionRepository rolePermissionRepository,
                                     IUserPermissionRepository userPermissionRepository,
                                     IPermissionRepository permissionRepository) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.userPermissionRepository = userPermissionRepository;
        this.permissionRepository = permissionRepository;
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = AccessControlCacheConfig.USER_ROLES_CACHE, key = "#userId")
    public Set<Role> getUserRoles(String userId) {
        return userRoleRepository.findByIdUserIdOrderByIdRoleIdAsc(userId).stream()
            .map(userRole -> userRole.getRole())
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = AccessControlCacheConfig.ROLE_PERMISSIONS_CACHE, key = "#userId")
    public Set<Permission> getRolePermissionsForUser(String userId) {
        List<Long> roleIds = userRoleRepository.findByIdUserIdOrderByIdRoleIdAsc(userId).stream()
            .map(userRole -> userRole.getId().getRoleId())
            .toList();
        if (roleIds.isEmpty()) {
            return Set.of();
        }
        return rolePermissionRepository.findByIdRoleIdIn(roleIds).stream()
            .map(RolePermission::getPermission)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = AccessControlCacheConfig.DIRECT_PERMISSIONS_CACHE, key = "#userId")
    public Set<Permission> getDirectPermissions(String userId) {
        return userPermissionRepository.findByIdUserIdOrderByIdPermissionIdAsc(userId).stream()
            .map(UserPermission::getPermission)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = AccessControlCacheConfig.FINAL_PERMISSIONS_CACHE, key = "#userId")
    public Set<Permission> getFinalPermissions(String userId) {
        LinkedHashSet<Permission> permissions = new LinkedHashSet<>(getRolePermissionsForUser(userId));
        permissions.addAll(getDirectPermissions(userId));
        return permissions;
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = AccessControlCacheConfig.FINAL_PERMISSION_CODES_CACHE, key = "#userId")
    public Set<String> getFinalPermissionCodes(String userId) {
        if (isSuperAdmin(userId)) {
            return getAllPermissions().stream()
                .map(Permission::getCode)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        }
        LinkedHashSet<String> codes = new LinkedHashSet<>();
        codes.addAll(getRolePermissionsForUser(userId).stream().map(Permission::getCode).toList());
        codes.addAll(getDirectPermissions(userId).stream().map(Permission::getCode).toList());
        return codes;
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = AccessControlCacheConfig.SUPER_ADMIN_CACHE, key = "#userId")
    public boolean isSuperAdmin(String userId) {
        return getUserRoles(userId).stream()
            .anyMatch(role -> "SUPER_ADMIN".equals(role.getCode()));
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = AccessControlCacheConfig.AUTH_PROFILE_CACHE, key = "#userId")
    public AuthProfile getAuthProfile(String userId) {
        User user = getUserOrThrow(userId);
        Set<String> roles = getUserRoles(userId).stream()
            .map(Role::getCode)
            .collect(Collectors.toCollection(LinkedHashSet::new));
        boolean superAdmin = roles.contains("SUPER_ADMIN");
        return new AuthProfile(user.getUid(), user.getUsername(), roles, superAdmin ? Set.of() : getFinalPermissionCodes(userId), superAdmin);
    }

    @Transactional(readOnly = true)
    public List<Permission> getAllPermissions() {
        return permissionRepository.findAllByOrderByGroupNameAscCodeAsc();
    }

    private User getUserOrThrow(String userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
    }
}
