package com.azarenka.evebuilders.rest.api.ui;

import com.azarenka.evebuilders.domain.acl.Role;
import com.azarenka.evebuilders.domain.acl.RolePermission;
import com.azarenka.evebuilders.domain.acl.UserPermission;
import com.azarenka.evebuilders.domain.acl.UserRole;
import com.azarenka.evebuilders.domain.dto.acl.CreateRoleRequest;
import com.azarenka.evebuilders.domain.dto.acl.AdminUserSummaryDto;
import com.azarenka.evebuilders.domain.dto.acl.PermissionDto;
import com.azarenka.evebuilders.domain.dto.acl.RoleDto;
import com.azarenka.evebuilders.domain.dto.acl.UpdateRolePermissionsRequest;
import com.azarenka.evebuilders.domain.dto.acl.UpdateRoleRequest;
import com.azarenka.evebuilders.domain.dto.acl.UpdateUserDirectPermissionsRequest;
import com.azarenka.evebuilders.domain.dto.acl.UpdateUserRolesRequest;
import com.azarenka.evebuilders.domain.dto.acl.UserAccessDto;
import com.azarenka.evebuilders.domain.db.Permission;
import com.azarenka.evebuilders.domain.db.User;
import com.azarenka.evebuilders.repository.database.IUserRepository;
import com.azarenka.evebuilders.repository.database.acl.IPermissionRepository;
import com.azarenka.evebuilders.repository.database.acl.IRolePermissionRepository;
import com.azarenka.evebuilders.repository.database.acl.IRoleRepository;
import com.azarenka.evebuilders.repository.database.acl.IUserPermissionRepository;
import com.azarenka.evebuilders.repository.database.acl.IUserRoleRepository;
import com.azarenka.evebuilders.service.api.IAccessControlService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin")
public class AccessControlAdminController {

    private final IAccessControlService accessControlService;
    private final IUserRepository userRepository;
    private final IRoleRepository roleRepository;
    private final IPermissionRepository permissionRepository;
    private final IRolePermissionRepository rolePermissionRepository;
    private final IUserRoleRepository userRoleRepository;
    private final IUserPermissionRepository userPermissionRepository;

    public AccessControlAdminController(IAccessControlService accessControlService,
                                        IUserRepository userRepository,
                                        IRoleRepository roleRepository,
                                        IPermissionRepository permissionRepository,
                                        IRolePermissionRepository rolePermissionRepository,
                                        IUserRoleRepository userRoleRepository,
                                        IUserPermissionRepository userPermissionRepository) {
        this.accessControlService = accessControlService;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.userRoleRepository = userRoleRepository;
        this.userPermissionRepository = userPermissionRepository;
    }

    @GetMapping("/roles")
    @PreAuthorize("@accessControlSecurity.can('CORPORATION_CONTRACT_VIEW')")
    public List<RoleDto> getRoles() {
        authorize("CORPORATION_CONTRACT_VIEW");
        return roleRepository.findAllByOrderByCodeAsc().stream().map(this::toRoleDto).toList();
    }

    @GetMapping("/roles/{code}")
    @PreAuthorize("@accessControlSecurity.can('CORPORATION_CONTRACT_VIEW')")
    public RoleDto getRole(@PathVariable String code) {
        authorize("CORPORATION_CONTRACT_VIEW");
        return toRoleDto(findRoleByCodeOrThrow(code));
    }

    @PostMapping("/roles")
    @Transactional
    @PreAuthorize("@accessControlSecurity.can('CORPORATION_CONTRACT_EDIT')")
    public RoleDto createRole(@Valid @RequestBody CreateRoleRequest request) {
        authorize("CORPORATION_CONTRACT_EDIT");
        Role role = new Role();
        role.setCode(normalizeCode(request.code()));
        role.setName(request.name());
        role.setDescription(request.description());
        role.setSystemRole(false);
        return toRoleDto(accessControlService.createRole(role));
    }

    @PutMapping("/roles/{code}")
    @Transactional
    @PreAuthorize("@accessControlSecurity.can('CORPORATION_CONTRACT_EDIT')")
    public RoleDto updateRole(@PathVariable String code, @Valid @RequestBody UpdateRoleRequest request) {
        authorize("CORPORATION_CONTRACT_EDIT");
        Role role = findRoleByCodeOrThrow(code);
        role.setName(request.name());
        role.setDescription(request.description());
        return toRoleDto(accessControlService.updateRole(role));
    }

    @DeleteMapping("/roles/{code}")
    @Transactional
    @PreAuthorize("@accessControlSecurity.can('CORPORATION_CONTRACT_EDIT')")
    public void deleteRole(@PathVariable String code) {
        authorize("CORPORATION_CONTRACT_EDIT");
        accessControlService.deleteRole(findRoleByCodeOrThrow(code).getId());
    }

    @GetMapping("/permissions")
    @PreAuthorize("@accessControlSecurity.can('CORPORATION_CONTRACT_VIEW')")
    public List<PermissionDto> getPermissions() {
        authorize("CORPORATION_CONTRACT_VIEW");
        return permissionRepository.findAllByOrderByGroupNameAscCodeAsc().stream().map(this::toPermissionDto).toList();
    }

    @GetMapping("/users")
    @PreAuthorize("@accessControlSecurity.can('CORPORATION_CONTRACT_VIEW')")
    public List<AdminUserSummaryDto> getUsers() {
        authorize("CORPORATION_CONTRACT_VIEW");
        return userRepository.findAllByOrderByUsernameAscUidAsc().stream()
            .map(this::toUserSummaryDto)
            .toList();
    }

    @GetMapping("/roles/{code}/permissions")
    @PreAuthorize("@accessControlSecurity.can('CORPORATION_CONTRACT_VIEW')")
    public Set<PermissionDto> getRolePermissions(@PathVariable String code) {
        authorize("CORPORATION_CONTRACT_VIEW");
        Role role = findRoleByCodeOrThrow(code);
        return rolePermissionRepository.findByIdRoleId(role.getId()).stream()
            .map(RolePermission::getPermission)
            .map(this::toPermissionDto)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @PutMapping("/roles/{code}/permissions")
    @Transactional
    @PreAuthorize("@accessControlSecurity.can('CORPORATION_CONTRACT_EDIT')")
    public Set<PermissionDto> updateRolePermissions(@PathVariable String code,
                                                    @Valid @RequestBody UpdateRolePermissionsRequest request) {
        authorize("CORPORATION_CONTRACT_EDIT");
        Role role = findRoleByCodeOrThrow(code);
        rolePermissionRepository.deleteByIdRoleId(role.getId());
        for (String permissionCode : request.permissionCodes()) {
            Permission permission = findPermissionByCodeOrThrow(permissionCode);
            accessControlService.assignPermissionToRole(role.getId(), permission.getId());
        }
        return getRolePermissions(code);
    }

    @GetMapping("/users/{userId}/access")
    @PreAuthorize("@accessControlSecurity.can('CORPORATION_CONTRACT_VIEW')")
    public UserAccessDto getUserAccess(@PathVariable String userId) {
        authorize("CORPORATION_CONTRACT_VIEW");
        return buildUserAccessDto(findUserByIdOrThrow(userId));
    }

    @PutMapping("/users/{userId}/roles")
    @Transactional
    @PreAuthorize("@accessControlSecurity.can('CORPORATION_CONTRACT_EDIT')")
    public UserAccessDto updateUserRoles(@PathVariable String userId,
                                         @Valid @RequestBody UpdateUserRolesRequest request) {
        authorize("CORPORATION_CONTRACT_EDIT");
        findUserByIdOrThrow(userId);
        userRoleRepository.deleteByIdUserId(userId);
        for (String roleCode : request.roleCodes()) {
            Role role = findRoleByCodeOrThrow(roleCode);
            accessControlService.assignRoleToUser(userId, role.getId());
        }
        return buildUserAccessDto(findUserByIdOrThrow(userId));
    }

    @PutMapping("/users/{userId}/direct-permissions")
    @Transactional
    @PreAuthorize("@accessControlSecurity.can('CORPORATION_CONTRACT_EDIT')")
    public UserAccessDto updateUserDirectPermissions(@PathVariable String userId,
                                                     @Valid @RequestBody UpdateUserDirectPermissionsRequest request) {
        authorize("CORPORATION_CONTRACT_EDIT");
        findUserByIdOrThrow(userId);
        userPermissionRepository.deleteByIdUserId(userId);
        for (String permissionCode : request.permissionCodes()) {
            Permission permission = findPermissionByCodeOrThrow(permissionCode);
            accessControlService.assignDirectPermissionToUser(userId, permission.getId());
        }
        return buildUserAccessDto(findUserByIdOrThrow(userId));
    }

    private void authorize(String permissionCode) {
        String currentUserId = currentUserId();
        if (!accessControlService.isSuperAdmin(currentUserId) && !accessControlService.hasPermission(currentUserId, permissionCode)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Missing permission: " + permissionCode);
        }
    }

    private String currentUserId() {
        var authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        return userRepository.findByUsername(authentication.getName())
            .map(User::getUid)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated"));
    }

    private Role findRoleByCodeOrThrow(String code) {
        return roleRepository.findByCode(normalizeCode(code))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found: " + code));
    }

    private Permission findPermissionByCodeOrThrow(String code) {
        return permissionRepository.findByCode(normalizeCode(code))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Permission not found: " + code));
    }

    private User findUserByIdOrThrow(String userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + userId));
    }

    private RoleDto toRoleDto(Role role) {
        Set<PermissionDto> permissions = rolePermissionRepository.findByIdRoleId(role.getId()).stream()
            .map(RolePermission::getPermission)
            .map(this::toPermissionDto)
            .collect(Collectors.toCollection(LinkedHashSet::new));
        return new RoleDto(role.getId(), role.getCode(), role.getName(), role.getDescription(), role.isSystemRole(), permissions);
    }

    private PermissionDto toPermissionDto(Permission permission) {
        return new PermissionDto(
            permission.getId(),
            permission.getCode(),
            permission.getName(),
            permission.getDescription(),
            permission.getGroupName()
        );
    }

    private AdminUserSummaryDto toUserSummaryDto(User user) {
        Set<String> roles = user.getUserRoles().stream()
            .map(UserRole::getRole)
            .filter(java.util.Objects::nonNull)
            .map(Role::getCode)
            .filter(java.util.Objects::nonNull)
            .sorted()
            .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> directPermissions = user.getDirectPermissions().stream()
            .map(UserPermission::getPermission)
            .filter(java.util.Objects::nonNull)
            .map(Permission::getCode)
            .filter(java.util.Objects::nonNull)
            .sorted()
            .collect(Collectors.toCollection(LinkedHashSet::new));
        boolean superAdmin = roles.contains("SUPER_ADMIN");

        return new AdminUserSummaryDto(
            user.getUid(),
            user.getUsername(),
            user.getUsername(),
            user.getCorporationName(),
            roles,
            directPermissions,
            superAdmin
        );
    }

    private UserAccessDto buildUserAccessDto(User user) {
        Set<String> roleCodes = accessControlService.getUserRoles(user.getUid()).stream()
            .map(Role::getCode)
            .collect(Collectors.toCollection(LinkedHashSet::new));
        boolean superAdmin = accessControlService.isSuperAdmin(user.getUid());
        Set<String> directPermissionCodes = accessControlService.getDirectPermissions(user.getUid()).stream()
            .map(Permission::getCode)
            .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> finalPermissionCodes = superAdmin
            ? Set.of()
            : accessControlService.getFinalPermissionCodes(user.getUid());

        Set<com.azarenka.evebuilders.domain.dto.acl.RoleDto> roles = accessControlService.getUserRoles(user.getUid()).stream()
            .map(this::toRoleDto)
            .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<PermissionDto> directPermissions = accessControlService.getDirectPermissions(user.getUid()).stream()
            .map(this::toPermissionDto)
            .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<PermissionDto> finalPermissions = permissionRepository.findAllByOrderByGroupNameAscCodeAsc().stream()
            .filter(permission -> finalPermissionCodes.contains(permission.getCode()))
            .map(this::toPermissionDto)
            .collect(Collectors.toCollection(LinkedHashSet::new));

        return new UserAccessDto(user.getUid(), user.getUsername(), roles, directPermissions, finalPermissions, superAdmin);
    }

    private String normalizeCode(String code) {
        return code == null ? null : code.trim().toUpperCase();
    }
}
