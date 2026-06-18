package com.azarenka.evebuilders.service.impl;

import com.azarenka.evebuilders.domain.acl.Role;
import com.azarenka.evebuilders.domain.acl.RolePermission;
import com.azarenka.evebuilders.domain.acl.RolePermissionId;
import com.azarenka.evebuilders.domain.acl.UserPermission;
import com.azarenka.evebuilders.domain.acl.UserPermissionId;
import com.azarenka.evebuilders.domain.acl.UserRole;
import com.azarenka.evebuilders.domain.acl.UserRoleId;
import com.azarenka.evebuilders.domain.auth.auth.ui.AuthProfile;
import com.azarenka.evebuilders.domain.db.Permission;
import com.azarenka.evebuilders.domain.db.User;
import com.azarenka.evebuilders.repository.database.IUserRepository;
import com.azarenka.evebuilders.repository.database.acl.IPermissionRepository;
import com.azarenka.evebuilders.repository.database.acl.IRolePermissionRepository;
import com.azarenka.evebuilders.repository.database.acl.IRoleRepository;
import com.azarenka.evebuilders.repository.database.acl.IUserPermissionRepository;
import com.azarenka.evebuilders.repository.database.acl.IUserRoleRepository;
import com.azarenka.evebuilders.service.api.IAccessControlService;
import com.azarenka.evebuilders.service.config.AccessControlCacheConfig;
import com.azarenka.evebuilders.service.impl.auth.eve.AccessControlQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.CacheEvict;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AccessControlService implements IAccessControlService {

    private static final Set<String> PROTECTED_PERMISSION_CODES = Set.of(
        "DASHBOARD_VIEW",
        "CONTRACTS_VIEW",
        "CONTRACTS_CREATE",
        "CONTRACTS_EDIT",
        "CONTRACTS_ACCEPT",
        "CONTRACTS_CANCEL",
        "CONTRACTS_DISCARD",
        "CORPORATION_VIEW",
        "CORPORATION_CONTRACT_VIEW",
        "CORPORATION_CONTRACT_EDIT"
    );

    private final IRoleRepository roleRepository;
    private final IPermissionRepository permissionRepository;
    private final IUserRepository userRepository;
    private final IUserRoleRepository userRoleRepository;
    private final IRolePermissionRepository rolePermissionRepository;
    private final IUserPermissionRepository userPermissionRepository;
    private final AccessControlQueryService queryService;

    public AccessControlService(IRoleRepository roleRepository,
                                IPermissionRepository permissionRepository,
                                IUserRepository userRepository,
                                IUserRoleRepository userRoleRepository,
                                IRolePermissionRepository rolePermissionRepository,
                                IUserPermissionRepository userPermissionRepository,
                                AccessControlQueryService queryService) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.userPermissionRepository = userPermissionRepository;
        this.queryService = queryService;
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Role> getUserRoles(String userId) {
        return queryService.getUserRoles(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Permission> getRolePermissionsForUser(String userId) {
        return queryService.getRolePermissionsForUser(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Permission> getDirectPermissions(String userId) {
        return queryService.getDirectPermissions(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Permission> getFinalPermissions(String userId) {
        return queryService.getFinalPermissions(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<String> getFinalPermissionCodes(String userId) {
        return queryService.getFinalPermissionCodes(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasPermission(String userId, String permissionCode) {
        if (queryService.isSuperAdmin(userId)) {
            return true;
        }
        return getFinalPermissionCodes(userId).contains(normalizeCode(permissionCode));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasAnyPermission(String userId, Set<String> permissionCodes) {
        if (queryService.isSuperAdmin(userId)) {
            return true;
        }
        if (permissionCodes == null || permissionCodes.isEmpty()) {
            return false;
        }
        Set<String> finalCodes = getFinalPermissionCodes(userId);
        Set<String> normalized = permissionCodes.stream()
            .map(this::normalizeCode)
            .filter(code -> code != null && !code.isBlank())
            .collect(Collectors.toSet());
        return normalized.stream().anyMatch(finalCodes::contains);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasAllPermissions(String userId, Set<String> permissionCodes) {
        if (queryService.isSuperAdmin(userId)) {
            return true;
        }
        if (permissionCodes == null || permissionCodes.isEmpty()) {
            return true;
        }
        Set<String> normalized = permissionCodes.stream()
            .map(this::normalizeCode)
            .filter(code -> code != null && !code.isBlank())
            .collect(Collectors.toSet());
        return getFinalPermissionCodes(userId).containsAll(normalized);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isSuperAdmin(String userId) {
        return queryService.isSuperAdmin(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthProfile getAuthProfile(String userId) {
        return queryService.getAuthProfile(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Role> getAllRoles() {
        return roleRepository.findAllByOrderByCodeAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Permission> getAllPermissions() {
        return permissionRepository.findAllByOrderByGroupNameAscCodeAsc();
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = {
        AccessControlCacheConfig.USER_ROLES_CACHE,
        AccessControlCacheConfig.ROLE_PERMISSIONS_CACHE,
        AccessControlCacheConfig.DIRECT_PERMISSIONS_CACHE,
        AccessControlCacheConfig.FINAL_PERMISSIONS_CACHE,
        AccessControlCacheConfig.FINAL_PERMISSION_CODES_CACHE,
        AccessControlCacheConfig.AUTH_PROFILE_CACHE,
        AccessControlCacheConfig.SUPER_ADMIN_CACHE
    }, allEntries = true)
    public Role createRole(Role role) {
        normalizeRole(role);
        roleRepository.findByCode(role.getCode()).ifPresent(existing -> {
            throw new IllegalArgumentException("Role code already exists: " + role.getCode());
        });
        return roleRepository.save(role);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = {
        AccessControlCacheConfig.USER_ROLES_CACHE,
        AccessControlCacheConfig.ROLE_PERMISSIONS_CACHE,
        AccessControlCacheConfig.DIRECT_PERMISSIONS_CACHE,
        AccessControlCacheConfig.FINAL_PERMISSIONS_CACHE,
        AccessControlCacheConfig.FINAL_PERMISSION_CODES_CACHE,
        AccessControlCacheConfig.AUTH_PROFILE_CACHE,
        AccessControlCacheConfig.SUPER_ADMIN_CACHE
    }, allEntries = true)
    public Role updateRole(Role role) {
        normalizeRole(role);
        if (role.getId() == null) {
            throw new IllegalArgumentException("Role id is required for update");
        }
        Role existing = roleRepository.findById(role.getId())
            .orElseThrow(() -> new IllegalArgumentException("Role not found: " + role.getId()));
        if (!existing.getCode().equals(role.getCode())) {
            roleRepository.findByCode(role.getCode()).ifPresent(found -> {
                if (!found.getId().equals(role.getId())) {
                    throw new IllegalArgumentException("Role code already exists: " + role.getCode());
                }
            });
        }
        return roleRepository.save(role);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = {
        AccessControlCacheConfig.USER_ROLES_CACHE,
        AccessControlCacheConfig.ROLE_PERMISSIONS_CACHE,
        AccessControlCacheConfig.DIRECT_PERMISSIONS_CACHE,
        AccessControlCacheConfig.FINAL_PERMISSIONS_CACHE,
        AccessControlCacheConfig.FINAL_PERMISSION_CODES_CACHE,
        AccessControlCacheConfig.AUTH_PROFILE_CACHE,
        AccessControlCacheConfig.SUPER_ADMIN_CACHE
    }, allEntries = true)
    public void deleteRole(Long roleId) {
        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));
        if (role.isSystemRole()) {
            throw new IllegalStateException("System role cannot be deleted: " + role.getCode());
        }
        rolePermissionRepository.deleteByIdRoleId(roleId);
        userRoleRepository.deleteByIdRoleId(roleId);
        roleRepository.delete(role);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = {
        AccessControlCacheConfig.USER_ROLES_CACHE,
        AccessControlCacheConfig.ROLE_PERMISSIONS_CACHE,
        AccessControlCacheConfig.DIRECT_PERMISSIONS_CACHE,
        AccessControlCacheConfig.FINAL_PERMISSIONS_CACHE,
        AccessControlCacheConfig.FINAL_PERMISSION_CODES_CACHE,
        AccessControlCacheConfig.AUTH_PROFILE_CACHE,
        AccessControlCacheConfig.SUPER_ADMIN_CACHE
    }, allEntries = true)
    public RolePermission assignPermissionToRole(Long roleId, Long permissionId) {
        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));
        Permission permission = permissionRepository.findById(permissionId)
            .orElseThrow(() -> new IllegalArgumentException("Permission not found: " + permissionId));

        return rolePermissionRepository.findByIdRoleIdAndIdPermissionId(roleId, permissionId)
            .orElseGet(() -> rolePermissionRepository.save(buildRolePermission(role, permission)));
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = {
        AccessControlCacheConfig.USER_ROLES_CACHE,
        AccessControlCacheConfig.ROLE_PERMISSIONS_CACHE,
        AccessControlCacheConfig.DIRECT_PERMISSIONS_CACHE,
        AccessControlCacheConfig.FINAL_PERMISSIONS_CACHE,
        AccessControlCacheConfig.FINAL_PERMISSION_CODES_CACHE,
        AccessControlCacheConfig.AUTH_PROFILE_CACHE,
        AccessControlCacheConfig.SUPER_ADMIN_CACHE
    }, allEntries = true)
    public void removePermissionFromRole(Long roleId, Long permissionId) {
        rolePermissionRepository.deleteByIdRoleIdAndIdPermissionId(roleId, permissionId);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = {
        AccessControlCacheConfig.USER_ROLES_CACHE,
        AccessControlCacheConfig.ROLE_PERMISSIONS_CACHE,
        AccessControlCacheConfig.DIRECT_PERMISSIONS_CACHE,
        AccessControlCacheConfig.FINAL_PERMISSIONS_CACHE,
        AccessControlCacheConfig.FINAL_PERMISSION_CODES_CACHE,
        AccessControlCacheConfig.AUTH_PROFILE_CACHE,
        AccessControlCacheConfig.SUPER_ADMIN_CACHE
    }, allEntries = true)
    public UserRole assignRoleToUser(String userId, Long roleId) {
        User user = getUserOrThrow(userId);
        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));

        return userRoleRepository.findByIdUserIdAndIdRoleId(userId, roleId)
            .orElseGet(() -> userRoleRepository.save(buildUserRole(user, role)));
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = {
        AccessControlCacheConfig.USER_ROLES_CACHE,
        AccessControlCacheConfig.ROLE_PERMISSIONS_CACHE,
        AccessControlCacheConfig.DIRECT_PERMISSIONS_CACHE,
        AccessControlCacheConfig.FINAL_PERMISSIONS_CACHE,
        AccessControlCacheConfig.FINAL_PERMISSION_CODES_CACHE,
        AccessControlCacheConfig.AUTH_PROFILE_CACHE,
        AccessControlCacheConfig.SUPER_ADMIN_CACHE
    }, allEntries = true)
    public void removeRoleFromUser(String userId, Long roleId) {
        userRoleRepository.deleteByIdUserIdAndIdRoleId(userId, roleId);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = {
        AccessControlCacheConfig.USER_ROLES_CACHE,
        AccessControlCacheConfig.ROLE_PERMISSIONS_CACHE,
        AccessControlCacheConfig.DIRECT_PERMISSIONS_CACHE,
        AccessControlCacheConfig.FINAL_PERMISSIONS_CACHE,
        AccessControlCacheConfig.FINAL_PERMISSION_CODES_CACHE,
        AccessControlCacheConfig.AUTH_PROFILE_CACHE,
        AccessControlCacheConfig.SUPER_ADMIN_CACHE
    }, allEntries = true)
    public UserPermission assignDirectPermissionToUser(String userId, Long permissionId) {
        User user = getUserOrThrow(userId);
        Permission permission = permissionRepository.findById(permissionId)
            .orElseThrow(() -> new IllegalArgumentException("Permission not found: " + permissionId));

        return userPermissionRepository.findByIdUserIdAndIdPermissionId(userId, permissionId)
            .orElseGet(() -> userPermissionRepository.save(buildUserPermission(user, permission)));
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = {
        AccessControlCacheConfig.USER_ROLES_CACHE,
        AccessControlCacheConfig.ROLE_PERMISSIONS_CACHE,
        AccessControlCacheConfig.DIRECT_PERMISSIONS_CACHE,
        AccessControlCacheConfig.FINAL_PERMISSIONS_CACHE,
        AccessControlCacheConfig.FINAL_PERMISSION_CODES_CACHE,
        AccessControlCacheConfig.AUTH_PROFILE_CACHE,
        AccessControlCacheConfig.SUPER_ADMIN_CACHE
    }, allEntries = true)
    public void removeDirectPermissionFromUser(String userId, Long permissionId) {
        userPermissionRepository.deleteByIdUserIdAndIdPermissionId(userId, permissionId);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = {
        AccessControlCacheConfig.USER_ROLES_CACHE,
        AccessControlCacheConfig.ROLE_PERMISSIONS_CACHE,
        AccessControlCacheConfig.DIRECT_PERMISSIONS_CACHE,
        AccessControlCacheConfig.FINAL_PERMISSIONS_CACHE,
        AccessControlCacheConfig.FINAL_PERMISSION_CODES_CACHE,
        AccessControlCacheConfig.AUTH_PROFILE_CACHE,
        AccessControlCacheConfig.SUPER_ADMIN_CACHE
    }, allEntries = true)
    public void deletePermission(Long permissionId) {
        Permission permission = permissionRepository.findById(permissionId)
            .orElseThrow(() -> new IllegalArgumentException("Permission not found: " + permissionId));
        if (PROTECTED_PERMISSION_CODES.contains(permission.getCode())) {
            throw new IllegalStateException("Protected permission cannot be deleted: " + permission.getCode());
        }
        if (rolePermissionRepository.existsByIdPermissionId(permissionId)
            || userPermissionRepository.existsByIdPermissionId(permissionId)) {
            throw new IllegalStateException("Permission is still assigned and cannot be deleted: " + permission.getCode());
        }
        permissionRepository.delete(permission);
    }

    private void normalizeRole(Role role) {
        if (role.getCode() != null) {
            role.setCode(role.getCode().trim().toUpperCase());
        }
        if ("SUPER_ADMIN".equals(role.getCode())) {
            role.setSystemRole(true);
        }
    }

    private RolePermission buildRolePermission(Role role, Permission permission) {
        RolePermission rolePermission = new RolePermission();
        RolePermissionId id = new RolePermissionId();
        id.setRoleId(role.getId());
        id.setPermissionId(permission.getId());
        rolePermission.setId(id);
        rolePermission.setRole(role);
        rolePermission.setPermission(permission);
        return rolePermission;
    }

    private UserRole buildUserRole(User user, Role role) {
        UserRole userRole = new UserRole();
        UserRoleId id = new UserRoleId();
        id.setUserId(user.getUid());
        id.setRoleId(role.getId());
        userRole.setId(id);
        userRole.setUser(user);
        userRole.setRole(role);
        return userRole;
    }

    private UserPermission buildUserPermission(User user, Permission permission) {
        UserPermission userPermission = new UserPermission();
        UserPermissionId id = new UserPermissionId();
        id.setUserId(user.getUid());
        id.setPermissionId(permission.getId());
        userPermission.setId(id);
        userPermission.setUser(user);
        userPermission.setPermission(permission);
        return userPermission;
    }

    private User getUserOrThrow(String userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
    }

    private String normalizeCode(String permissionCode) {
        return permissionCode == null ? null : permissionCode.trim().toUpperCase();
    }
}
