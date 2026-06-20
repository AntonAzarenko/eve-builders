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
import com.azarenka.evebuilders.domain.exeptions.ValidationException;
import com.azarenka.evebuilders.repository.database.IUserRepository;
import com.azarenka.evebuilders.repository.database.acl.IPermissionRepository;
import com.azarenka.evebuilders.repository.database.acl.IRolePermissionRepository;
import com.azarenka.evebuilders.repository.database.acl.IRoleRepository;
import com.azarenka.evebuilders.repository.database.acl.IUserPermissionRepository;
import com.azarenka.evebuilders.repository.database.acl.IUserRoleRepository;
import com.azarenka.evebuilders.repository.database.acl.UserRoleSyncResult;
import com.azarenka.evebuilders.service.impl.auth.eve.AccessControlQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccessControlServiceTest {

    @Mock
    private IRoleRepository roleRepository;
    @Mock
    private IPermissionRepository permissionRepository;
    @Mock
    private IUserRepository userRepository;
    @Mock
    private IUserRoleRepository userRoleRepository;
    @Mock
    private IRolePermissionRepository rolePermissionRepository;
    @Mock
    private IUserPermissionRepository userPermissionRepository;
    @Mock
    private AccessControlQueryService queryService;

    @InjectMocks
    private AccessControlService accessControlService;

    private User user;
    private Role managerRole;
    private Role superAdminRole;
    private Permission dashboardView;
    private Permission contractsView;
    private Permission contractsEdit;
    private Permission corporationView;
    private Permission corporationContractEdit;
    private Permission adminView;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUid("user-1");
        user.setUsername("test");

        managerRole = role("MANAGER", true, 10L);
        superAdminRole = role("SUPER_ADMIN", true, 11L);

        dashboardView = permission(1L, "DASHBOARD_VIEW", "Dashboard view", "Dashboard");
        contractsView = permission(2L, "CONTRACTS_VIEW", "Contracts view", "Contracts");
        contractsEdit = permission(3L, "CONTRACTS_EDIT", "Contracts edit", "Contracts");
        corporationView = permission(4L, "CORPORATION_VIEW", "Corporation view", "Corporation");
        corporationContractEdit = permission(5L, "CORPORATION_CONTRACT_EDIT", "Corporation contract edit", "Corporation contracts");
        adminView = permission(6L, "ADMIN_VIEW", "Admin view", "Admin");
    }

    @Test
    void getFinalPermissionsMergesRoleAndDirectPermissions() {
        when(queryService.getFinalPermissions(user.getUid())).thenReturn(Set.of(dashboardView, contractsView));

        Set<Permission> permissions = accessControlService.getFinalPermissions(user.getUid());

        assertEquals(Set.of(dashboardView, contractsView), permissions);
    }

    @Test
    void hasPermissionReturnsTrueForRolePermission() {
        when(queryService.isSuperAdmin(user.getUid())).thenReturn(false);
        when(queryService.getFinalPermissionCodes(user.getUid())).thenReturn(Set.of("DASHBOARD_VIEW"));

        assertTrue(accessControlService.hasPermission(user.getUid(), "DASHBOARD_VIEW"));
    }

    @Test
    void hasPermissionReturnsTrueForDirectPermission() {
        when(queryService.isSuperAdmin(user.getUid())).thenReturn(false);
        when(queryService.getFinalPermissionCodes(user.getUid())).thenReturn(Set.of("CONTRACTS_VIEW"));

        assertTrue(accessControlService.hasPermission(user.getUid(), "CONTRACTS_VIEW"));
    }

    @Test
    void hasAnyPermissionReturnsTrueWhenOnePermissionExists() {
        when(queryService.isSuperAdmin(user.getUid())).thenReturn(false);
        when(queryService.getFinalPermissionCodes(user.getUid())).thenReturn(Set.of("DASHBOARD_VIEW"));

        assertTrue(accessControlService.hasAnyPermission(user.getUid(), Set.of("CORPORATION_VIEW", "DASHBOARD_VIEW")));
    }

    @Test
    void hasAllPermissionsReturnsTrueWhenAllPermissionsExist() {
        when(queryService.isSuperAdmin(user.getUid())).thenReturn(false);
        when(queryService.getFinalPermissionCodes(user.getUid())).thenReturn(Set.of("DASHBOARD_VIEW", "CONTRACTS_VIEW"));

        assertTrue(accessControlService.hasAllPermissions(user.getUid(), Set.of("DASHBOARD_VIEW", "CONTRACTS_VIEW")));
    }

    @Test
    void getAuthProfileReturnsMergedPermissionsAndRoles() {
        when(queryService.getAuthProfile(user.getUid()))
            .thenReturn(new AuthProfile(user.getUid(), user.getUsername(), Set.of("MANAGER"), Set.of("DASHBOARD_VIEW", "CONTRACTS_VIEW"), false));

        AuthProfile profile = accessControlService.getAuthProfile(user.getUid());

        assertEquals(user.getUid(), profile.userId());
        assertEquals(user.getUsername(), profile.username());
        assertEquals(Set.of("MANAGER"), profile.roles());
        assertEquals(Set.of("DASHBOARD_VIEW", "CONTRACTS_VIEW"), profile.permissions());
        assertFalse(profile.superAdmin());
    }

    @Test
    void superAdminBypassesAllPermissionChecksAndGetsAllPermissions() {
        when(queryService.isSuperAdmin(user.getUid())).thenReturn(true);
        when(queryService.getAuthProfile(user.getUid()))
            .thenReturn(new AuthProfile(user.getUid(), user.getUsername(), Set.of("SUPER_ADMIN"), Set.of(), true));

        assertTrue(accessControlService.isSuperAdmin(user.getUid()));
        assertTrue(accessControlService.hasPermission(user.getUid(), "ANY_PERMISSION"));
        assertTrue(accessControlService.hasAnyPermission(user.getUid(), Set.of("ANY_PERMISSION", "ANOTHER")));
        assertTrue(accessControlService.hasAllPermissions(user.getUid(), Set.of("ANY_PERMISSION", "ANOTHER")));

        AuthProfile profile = accessControlService.getAuthProfile(user.getUid());
        assertTrue(profile.superAdmin());
        assertEquals(Set.of("SUPER_ADMIN"), profile.roles());
        assertEquals(Set.of(), profile.permissions());
    }

    @Test
    void userWithoutPermissionsGetsEmptyPermissionSet() {
        when(queryService.isSuperAdmin(user.getUid())).thenReturn(false);
        when(queryService.getFinalPermissionCodes(user.getUid())).thenReturn(Set.of());

        assertEquals(Set.of(), accessControlService.getFinalPermissionCodes(user.getUid()));
        assertFalse(accessControlService.hasPermission(user.getUid(), "DASHBOARD_VIEW"));
        assertFalse(accessControlService.hasAnyPermission(user.getUid(), Set.of("DASHBOARD_VIEW")));
        assertFalse(accessControlService.hasAllPermissions(user.getUid(), Set.of("DASHBOARD_VIEW")));
    }

    @Test
    void isSuperAdminReturnsTrueWhenSuperAdminRolePresent() {
        when(queryService.isSuperAdmin(user.getUid())).thenReturn(true);

        assertTrue(accessControlService.isSuperAdmin(user.getUid()));
    }

    @Test
    void isSuperAdminReturnsFalseWhenRoleMissing() {
        when(queryService.isSuperAdmin(user.getUid())).thenReturn(false);

        assertFalse(accessControlService.isSuperAdmin(user.getUid()));
    }

    @Test
    void deleteRoleRejectsSystemRole() {
        when(roleRepository.findById(superAdminRole.getId())).thenReturn(Optional.of(superAdminRole));

        assertThrows(IllegalStateException.class, () -> accessControlService.deleteRole(superAdminRole.getId()));
        verify(rolePermissionRepository, never()).deleteAllByRoleId(any());
        verify(userRoleRepository, never()).deleteAllByRoleId(any());
        verify(roleRepository, never()).delete(any());
    }

    @Test
    void deleteRoleRejectsSystemAdminRole() {
        Role systemAdminRole = role("CEO", true, 12L);
        when(roleRepository.findById(systemAdminRole.getId())).thenReturn(Optional.of(systemAdminRole));

        assertThrows(IllegalStateException.class, () -> accessControlService.deleteRole(systemAdminRole.getId()));
        verify(rolePermissionRepository, never()).deleteAllByRoleId(any());
        verify(userRoleRepository, never()).deleteAllByRoleId(any());
        verify(roleRepository, never()).delete(any());
    }

    @Test
    void assignPermissionToRoleCreatesRelationWhenMissing() {
        when(roleRepository.findById(managerRole.getId())).thenReturn(Optional.of(managerRole));
        when(permissionRepository.findById(dashboardView.getId())).thenReturn(Optional.of(dashboardView));
        when(rolePermissionRepository.findByIdRoleIdAndIdPermissionId(managerRole.getId(), dashboardView.getId()))
            .thenReturn(Optional.empty());
        when(rolePermissionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        RolePermission result = accessControlService.assignPermissionToRole(managerRole.getId(), dashboardView.getId());

        RolePermissionId expectedId = new RolePermissionId();
        expectedId.setRoleId(managerRole.getId());
        expectedId.setPermissionId(dashboardView.getId());
        assertEquals(expectedId, result.getId());
        assertEquals(managerRole, result.getRole());
        assertEquals(dashboardView, result.getPermission());
    }

    @Test
    void assignRoleToUserCreatesRelationWhenMissing() {
        when(userRepository.findById(user.getUid())).thenReturn(Optional.of(user));
        when(roleRepository.findById(managerRole.getId())).thenReturn(Optional.of(managerRole));
        when(userRoleRepository.findByIdUserIdAndIdRoleId(user.getUid(), managerRole.getId()))
            .thenReturn(Optional.empty());
        when(userRoleRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        UserRole result = accessControlService.assignRoleToUser(user.getUid(), managerRole.getId());

        UserRoleId expectedId = new UserRoleId();
        expectedId.setUserId(user.getUid());
        expectedId.setRoleId(managerRole.getId());
        assertEquals(expectedId, result.getId());
        assertEquals(user, result.getUser());
        assertEquals(managerRole, result.getRole());
    }

    @Test
    void assignRoleToUserByRoleObjectCreatesRelationWhenMissing() {
        when(userRepository.findById(user.getUid())).thenReturn(Optional.of(user));
        when(userRoleRepository.findByIdUserIdAndIdRoleId(user.getUid(), managerRole.getId()))
            .thenReturn(Optional.empty());
        when(userRoleRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        UserRole result = accessControlService.assignRoleToUser(user.getUid(), managerRole);

        UserRoleId expectedId = new UserRoleId();
        expectedId.setUserId(user.getUid());
        expectedId.setRoleId(managerRole.getId());
        assertEquals(expectedId, result.getId());
        assertEquals(user, result.getUser());
        assertEquals(managerRole, result.getRole());
    }

    @Test
    void assignRoleToUserByCodeCreatesRelationWhenMissing() {
        when(userRepository.findById(user.getUid())).thenReturn(Optional.of(user));
        when(roleRepository.findByCode("MANAGER")).thenReturn(Optional.of(managerRole));
        when(userRoleRepository.findByIdUserIdAndIdRoleId(user.getUid(), managerRole.getId()))
            .thenReturn(Optional.empty());
        when(userRoleRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        UserRole result = accessControlService.assignRoleToUser(user.getUid(), "manager");

        UserRoleId expectedId = new UserRoleId();
        expectedId.setUserId(user.getUid());
        expectedId.setRoleId(managerRole.getId());
        assertEquals(expectedId, result.getId());
        assertEquals(user, result.getUser());
        assertEquals(managerRole, result.getRole());
    }

    @Test
    void replaceUserRolesSynchronizesAddsAndRemovesWithoutTouchingExistingMappings() {
        Role ceoRole = role("CEO", true, 12L);
        Role builderRole = role("BUILDER", true, 13L);
        LinkedHashSet<String> request = new LinkedHashSet<>();
        request.add("  ceo  ");
        request.add("BUILDER");
        request.add(null);
        request.add(" ");
        request.add("BUILDER");

        when(userRepository.existsById(user.getUid())).thenReturn(true);
        when(userRoleRepository.syncUserRoles(user.getUid(), Set.of("CEO", "BUILDER")))
            .thenReturn(new UserRoleSyncResult(Set.of(), 1L, 1L));
        when(roleRepository.findByCode("CEO")).thenReturn(Optional.of(ceoRole));
        when(roleRepository.findByCode("BUILDER")).thenReturn(Optional.of(builderRole));

        Set<Role> result = accessControlService.replaceUserRoles(user.getUid(), request);

        assertEquals(Set.of(ceoRole, builderRole), result);
    }

    @Test
    void replaceUserRolesAddsRolesWhenUserHasNoRoles() {
        Role minerRole = role("MINER", true, 14L);

        when(userRepository.existsById(user.getUid())).thenReturn(true);
        when(userRoleRepository.syncUserRoles(user.getUid(), Set.of("MINER")))
            .thenReturn(new UserRoleSyncResult(Set.of(), 1L, 0L));
        when(roleRepository.findByCode("MINER")).thenReturn(Optional.of(minerRole));

        Set<Role> result = accessControlService.replaceUserRoles(user.getUid(), Set.of("MINER"));

        assertEquals(Set.of(minerRole), result);
    }

    @Test
    void replaceUserRolesRemovesAllRolesWhenRequestIsEmpty() {
        when(userRepository.existsById(user.getUid())).thenReturn(true);
        when(userRoleRepository.syncUserRoles(user.getUid(), Set.of()))
            .thenReturn(new UserRoleSyncResult(Set.of(), 0L, 1L));

        Set<Role> result = accessControlService.replaceUserRoles(user.getUid(), Set.of());

        assertEquals(Set.of(), result);
        verify(roleRepository, never()).findByCode(any());
    }

    @Test
    void replaceUserRolesRejectsUnknownRoleCodesWithoutMutatingMappings() {
        when(userRepository.existsById(user.getUid())).thenReturn(true);
        when(userRoleRepository.syncUserRoles(user.getUid(), Set.of("UNKNOWN")))
            .thenReturn(new UserRoleSyncResult(Set.of("UNKNOWN"), 0L, 0L));

        ValidationException exception = assertThrows(ValidationException.class,
            () -> accessControlService.replaceUserRoles(user.getUid(), Set.of("UNKNOWN")));

        assertEquals("Unknown role codes: UNKNOWN", exception.getMessage());
        verify(roleRepository, never()).findByCode(any());
    }

    @Test
    void replaceUserRolesIgnoresBlankAndDuplicateValuesDuringNormalization() {
        Role minerRole = role("MINER", true, 14L);
        LinkedHashSet<String> request = new LinkedHashSet<>();
        request.add(" ");
        request.add(null);
        request.add("miner");
        request.add(" MINER ");
        request.add("MINER");

        when(userRepository.existsById(user.getUid())).thenReturn(true);
        when(userRoleRepository.syncUserRoles(user.getUid(), Set.of("MINER")))
            .thenReturn(new UserRoleSyncResult(Set.of(), 1L, 0L));
        when(roleRepository.findByCode("MINER")).thenReturn(Optional.of(minerRole));

        Set<Role> result = accessControlService.replaceUserRoles(user.getUid(), request);

        assertEquals(Set.of(minerRole), result);
    }

    @Test
    void deletePermissionRejectsProtectedPermission() {
        when(permissionRepository.findById(dashboardView.getId())).thenReturn(Optional.of(dashboardView));

        assertThrows(IllegalStateException.class, () -> accessControlService.deletePermission(dashboardView.getId()));
        verify(permissionRepository, never()).delete(any());
    }

    @Test
    void deletePermissionRejectsAdminPermission() {
        when(permissionRepository.findById(adminView.getId())).thenReturn(Optional.of(adminView));

        assertThrows(IllegalStateException.class, () -> accessControlService.deletePermission(adminView.getId()));
        verify(permissionRepository, never()).delete(any());
    }

    private Role role(String code, boolean systemRole, Long id) {
        Role role = new Role();
        role.setId(id);
        role.setCode(code);
        role.setName(code);
        role.setDescription(code);
        role.setSystemRole(systemRole);
        return role;
    }

    private Permission permission(Long id, String code, String name, String groupName) {
        Permission permission = new Permission();
        permission.setId(id);
        permission.setCode(code);
        permission.setName(name);
        permission.setDescription(name);
        permission.setGroupName(groupName);
        return permission;
    }

    private UserRole userRole(String userId, Role role) {
        UserRole userRole = new UserRole();
        UserRoleId id = new UserRoleId();
        id.setUserId(userId);
        id.setRoleId(role.getId());
        userRole.setId(id);
        userRole.setUser(user);
        userRole.setRole(role);
        return userRole;
    }

    private RolePermission rolePermission(Role role, Permission permission) {
        RolePermission rolePermission = new RolePermission();
        RolePermissionId id = new RolePermissionId();
        id.setRoleId(role.getId());
        id.setPermissionId(permission.getId());
        rolePermission.setId(id);
        rolePermission.setRole(role);
        rolePermission.setPermission(permission);
        return rolePermission;
    }

    private UserPermission userPermission(String userId, Permission permission) {
        UserPermission userPermission = new UserPermission();
        UserPermissionId id = new UserPermissionId();
        id.setUserId(userId);
        id.setPermissionId(permission.getId());
        userPermission.setId(id);
        userPermission.setUser(user);
        userPermission.setPermission(permission);
        return userPermission;
    }
}
