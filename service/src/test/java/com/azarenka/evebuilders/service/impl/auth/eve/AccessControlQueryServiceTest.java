package com.azarenka.evebuilders.service.impl.auth.eve;

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
import com.azarenka.evebuilders.repository.database.acl.IUserPermissionRepository;
import com.azarenka.evebuilders.repository.database.acl.IUserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccessControlQueryServiceTest {

    @Mock
    private IUserRepository userRepository;
    @Mock
    private IUserRoleRepository userRoleRepository;
    @Mock
    private IRolePermissionRepository rolePermissionRepository;
    @Mock
    private IUserPermissionRepository userPermissionRepository;
    @Mock
    private IPermissionRepository permissionRepository;

    private AccessControlQueryService queryService;

    private User user;
    private Role ceoRole;
    private Role managerRole;
    private Role minerRole;
    private Role builderRole;
    private Role superAdminRole;
    private Permission dashboardView;
    private Permission contractsView;
    private Permission contractsCreate;
    private Permission contractsEdit;
    private Permission contractsAccept;
    private Permission contractsCancel;
    private Permission contractsDiscard;
    private Permission corporationView;
    private Permission corporationContractView;
    private Permission corporationContractEdit;

    @BeforeEach
    void setUp() {
        queryService = new AccessControlQueryService(
            userRepository,
            userRoleRepository,
            rolePermissionRepository,
            userPermissionRepository,
            permissionRepository
        );

        user = new User();
        user.setUid("user-1");
        user.setUsername("pilot");

        ceoRole = role(1L, "CEO", true);
        managerRole = role(2L, "MANAGER", true);
        minerRole = role(3L, "MINER", true);
        builderRole = role(4L, "BUILDER", true);
        superAdminRole = role(5L, "SUPER_ADMIN", true);

        dashboardView = permission(10L, "DASHBOARD_VIEW");
        contractsView = permission(11L, "CONTRACTS_VIEW");
        contractsCreate = permission(12L, "CONTRACTS_CREATE");
        contractsEdit = permission(13L, "CONTRACTS_EDIT");
        contractsAccept = permission(14L, "CONTRACTS_ACCEPT");
        contractsCancel = permission(15L, "CONTRACTS_CANCEL");
        contractsDiscard = permission(16L, "CONTRACTS_DISCARD");
        corporationView = permission(17L, "CORPORATION_VIEW");
        corporationContractView = permission(18L, "CORPORATION_CONTRACT_VIEW");
        corporationContractEdit = permission(19L, "CORPORATION_CONTRACT_EDIT");
    }

    @Test
    void userWithoutRolesOrPermissionsGetsEmptySets() {
        when(userRoleRepository.findByIdUserIdOrderByIdRoleIdAsc(user.getUid())).thenReturn(List.of());
        when(userPermissionRepository.findByIdUserIdOrderByIdPermissionIdAsc(user.getUid())).thenReturn(List.of());

        assertEquals(Set.of(), queryService.getUserRoles(user.getUid()));
        assertEquals(Set.of(), queryService.getFinalPermissions(user.getUid()));
        assertEquals(Set.of(), queryService.getFinalPermissionCodes(user.getUid()));
        assertFalse(queryService.isSuperAdmin(user.getUid()));
    }

    @Test
    void singleRoleProvidesRolePermissions() {
        UserRole userRole = userRole(user.getUid(), managerRole);
        RolePermission contractsGrant = rolePermission(managerRole, contractsView);
        RolePermission contractsCreateGrant = rolePermission(managerRole, contractsCreate);

        when(userRoleRepository.findByIdUserIdOrderByIdRoleIdAsc(user.getUid())).thenReturn(List.of(userRole));
        when(rolePermissionRepository.findByIdRoleIdIn(List.of(managerRole.getId())))
            .thenReturn(List.of(contractsGrant, contractsCreateGrant));
        when(userPermissionRepository.findByIdUserIdOrderByIdPermissionIdAsc(user.getUid())).thenReturn(List.of());

        assertEquals(Set.of(contractsView, contractsCreate), queryService.getFinalPermissions(user.getUid()));
        assertEquals(Set.of("CONTRACTS_VIEW", "CONTRACTS_CREATE"), queryService.getFinalPermissionCodes(user.getUid()));
    }

    @Test
    void multipleRolesAreMergedWithoutDuplicates() {
        UserRole ceoUserRole = userRole(user.getUid(), ceoRole);
        UserRole builderUserRole = userRole(user.getUid(), builderRole);
        RolePermission ceoCorporation = rolePermission(ceoRole, corporationView);
        RolePermission ceoContractsView = rolePermission(ceoRole, corporationContractView);
        RolePermission ceoContractsEdit = rolePermission(ceoRole, corporationContractEdit);
        RolePermission builderDashboard = rolePermission(builderRole, dashboardView);
        RolePermission builderAccept = rolePermission(builderRole, contractsAccept);
        RolePermission builderDiscard = rolePermission(builderRole, contractsDiscard);
        UserPermission directContracts = userPermission(user.getUid(), contractsView);

        when(userRoleRepository.findByIdUserIdOrderByIdRoleIdAsc(user.getUid())).thenReturn(List.of(ceoUserRole, builderUserRole));
        when(rolePermissionRepository.findByIdRoleIdIn(List.of(ceoRole.getId(), builderRole.getId())))
            .thenReturn(List.of(ceoCorporation, ceoContractsView, ceoContractsEdit, builderDashboard, builderAccept, builderDiscard, ceoCorporation));
        when(userPermissionRepository.findByIdUserIdOrderByIdPermissionIdAsc(user.getUid())).thenReturn(List.of(directContracts));

        assertEquals(Set.of(corporationView, corporationContractView, corporationContractEdit, dashboardView, contractsAccept, contractsDiscard, contractsView),
            queryService.getFinalPermissions(user.getUid()));
        assertEquals(Set.of("CORPORATION_VIEW", "CORPORATION_CONTRACT_VIEW", "CORPORATION_CONTRACT_EDIT",
                "DASHBOARD_VIEW", "CONTRACTS_ACCEPT", "CONTRACTS_DISCARD", "CONTRACTS_VIEW"),
            queryService.getFinalPermissionCodes(user.getUid()));
    }

    @Test
    void directPermissionIsIncludedInFinalPermissions() {
        UserPermission directContractsEdit = userPermission(user.getUid(), contractsEdit);

        when(userRoleRepository.findByIdUserIdOrderByIdRoleIdAsc(user.getUid())).thenReturn(List.of());
        when(userPermissionRepository.findByIdUserIdOrderByIdPermissionIdAsc(user.getUid())).thenReturn(List.of(directContractsEdit));

        assertEquals(Set.of(contractsEdit), queryService.getDirectPermissions(user.getUid()));
        assertEquals(Set.of(contractsEdit), queryService.getFinalPermissions(user.getUid()));
    }

    @Test
    void superAdminGetsAllPermissionsInCodeSetButProfileHidesThem() {
        UserRole superRole = userRole(user.getUid(), superAdminRole);

        when(userRepository.findById(user.getUid())).thenReturn(Optional.of(user));
        when(userRoleRepository.findByIdUserIdOrderByIdRoleIdAsc(user.getUid())).thenReturn(List.of(superRole));
        when(permissionRepository.findAllByOrderByGroupNameAscCodeAsc())
            .thenReturn(List.of(
                dashboardView,
                contractsView,
                contractsCreate,
                contractsEdit,
                contractsAccept,
                contractsCancel,
                contractsDiscard,
                corporationView,
                corporationContractView,
                corporationContractEdit
            ));

        assertTrue(queryService.isSuperAdmin(user.getUid()));
        assertEquals(Set.of("DASHBOARD_VIEW", "CONTRACTS_VIEW", "CONTRACTS_CREATE", "CONTRACTS_EDIT", "CONTRACTS_ACCEPT",
                "CONTRACTS_CANCEL", "CONTRACTS_DISCARD", "CORPORATION_VIEW", "CORPORATION_CONTRACT_VIEW", "CORPORATION_CONTRACT_EDIT"),
            queryService.getFinalPermissionCodes(user.getUid()));

        AuthProfile profile = queryService.getAuthProfile(user.getUid());
        assertTrue(profile.superAdmin());
        assertEquals(Set.of("SUPER_ADMIN"), profile.roles());
        assertEquals(Set.of(), profile.permissions());
    }

    private Role role(Long id, String code, boolean systemRole) {
        Role role = new Role();
        role.setId(id);
        role.setCode(code);
        role.setName(code);
        role.setDescription(code);
        role.setSystemRole(systemRole);
        return role;
    }

    private Permission permission(Long id, String code) {
        Permission permission = new Permission();
        permission.setId(id);
        permission.setCode(code);
        permission.setName(code);
        permission.setDescription(code);
        permission.setGroupName("TEST");
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
