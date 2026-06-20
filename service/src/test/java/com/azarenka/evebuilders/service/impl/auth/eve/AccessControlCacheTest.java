package com.azarenka.evebuilders.service.impl.auth.eve;

import com.azarenka.evebuilders.domain.acl.Role;
import com.azarenka.evebuilders.domain.acl.RolePermission;
import com.azarenka.evebuilders.domain.acl.RolePermissionId;
import com.azarenka.evebuilders.domain.acl.UserRole;
import com.azarenka.evebuilders.domain.acl.UserRoleId;
import com.azarenka.evebuilders.domain.db.Permission;
import com.azarenka.evebuilders.domain.db.User;
import com.azarenka.evebuilders.repository.database.IUserRepository;
import com.azarenka.evebuilders.repository.database.acl.IPermissionRepository;
import com.azarenka.evebuilders.repository.database.acl.IRolePermissionRepository;
import com.azarenka.evebuilders.repository.database.acl.IRoleRepository;
import com.azarenka.evebuilders.repository.database.acl.IUserPermissionRepository;
import com.azarenka.evebuilders.repository.database.acl.IUserRoleRepository;
import com.azarenka.evebuilders.repository.database.acl.UserRoleSyncResult;
import com.azarenka.evebuilders.service.api.IAccessControlService;
import com.azarenka.evebuilders.service.config.AccessControlCacheConfig;
import com.azarenka.evebuilders.service.impl.AccessControlService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

class AccessControlCacheTest {

    private AnnotationConfigApplicationContext context;
    private IAccessControlService accessControlService;
    private IUserRepository userRepository;
    private IUserRoleRepository userRoleRepository;
    private IRolePermissionRepository rolePermissionRepository;
    private IPermissionRepository permissionRepository;
    private IRoleRepository roleRepository;
    private IUserPermissionRepository userPermissionRepository;

    @BeforeEach
    void setUp() {
        context = new AnnotationConfigApplicationContext();
        context.register(AccessControlCacheConfig.class);
        context.registerBean(IUserRepository.class, () -> mock(IUserRepository.class));
        context.registerBean(IUserRoleRepository.class, () -> mock(IUserRoleRepository.class));
        context.registerBean(IRolePermissionRepository.class, () -> mock(IRolePermissionRepository.class));
        context.registerBean(IPermissionRepository.class, () -> mock(IPermissionRepository.class));
        context.registerBean(IRoleRepository.class, () -> mock(IRoleRepository.class));
        context.registerBean(IUserPermissionRepository.class, () -> mock(IUserPermissionRepository.class));
        context.registerBean(AccessControlQueryService.class, () -> new AccessControlQueryService(
            context.getBean(IUserRepository.class),
            context.getBean(IUserRoleRepository.class),
            context.getBean(IRolePermissionRepository.class),
            context.getBean(IUserPermissionRepository.class),
            context.getBean(IPermissionRepository.class)
        ));
        context.registerBean(AccessControlService.class, () -> new AccessControlService(
            context.getBean(IRoleRepository.class),
            context.getBean(IPermissionRepository.class),
            context.getBean(IUserRepository.class),
            context.getBean(IUserRoleRepository.class),
            context.getBean(IRolePermissionRepository.class),
            context.getBean(IUserPermissionRepository.class),
            context.getBean(AccessControlQueryService.class)
        ));
        context.refresh();

        accessControlService = context.getBean(IAccessControlService.class);
        userRepository = context.getBean(IUserRepository.class);
        userRoleRepository = context.getBean(IUserRoleRepository.class);
        rolePermissionRepository = context.getBean(IRolePermissionRepository.class);
        permissionRepository = context.getBean(IPermissionRepository.class);
        roleRepository = context.getBean(IRoleRepository.class);
        userPermissionRepository = context.getBean(IUserPermissionRepository.class);
    }

    @AfterEach
    void tearDown() {
        if (context != null) {
            context.close();
        }
    }

    @Test
    void finalPermissionsAreCachedAndInvalidatedAfterRolePermissionChange() {
        User user = user("user-1", "pilot");
        Role adminRole = role(1L, "ADMIN");
        Permission dashboard = permission(10L, "DASHBOARD_VIEW");
        Permission rolesView = permission(11L, "CORPORATION_CONTRACT_VIEW");

        UserRole userRole = userRole(user, adminRole);
        RolePermission dashboardGrant = rolePermission(adminRole, dashboard);
        RolePermission rolesViewGrant = rolePermission(adminRole, rolesView);
        AtomicReference<List<RolePermission>> rolePermissions = new AtomicReference<>(List.of(dashboardGrant));

        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(userRoleRepository.findByIdUserIdOrderByIdRoleIdAsc("user-1")).thenReturn(List.of(userRole));
        when(rolePermissionRepository.findByIdRoleIdIn(List.of(1L))).thenAnswer(invocation -> rolePermissions.get());
        when(permissionRepository.findAllByOrderByGroupNameAscCodeAsc()).thenReturn(List.of(dashboard, rolesView));
        when(roleRepository.findById(1L)).thenReturn(Optional.of(adminRole));
        when(permissionRepository.findById(11L)).thenReturn(Optional.of(rolesView));
        when(rolePermissionRepository.findByIdRoleIdAndIdPermissionId(1L, 11L)).thenReturn(Optional.empty());
        when(rolePermissionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Set<String> first = accessControlService.getFinalPermissionCodes("user-1");
        Set<String> second = accessControlService.getFinalPermissionCodes("user-1");

        assertEquals(Set.of("DASHBOARD_VIEW"), first);
        assertEquals(first, second);
        Mockito.verify(userRoleRepository, times(2)).findByIdUserIdOrderByIdRoleIdAsc("user-1");
        Mockito.verify(rolePermissionRepository, times(1)).findByIdRoleIdIn(List.of(1L));

        accessControlService.assignPermissionToRole(1L, 11L);
        rolePermissions.set(List.of(dashboardGrant, rolesViewGrant));

        Set<String> afterMutation = accessControlService.getFinalPermissionCodes("user-1");

        assertTrue(afterMutation.contains("DASHBOARD_VIEW"));
        assertTrue(afterMutation.contains("CORPORATION_CONTRACT_VIEW"));
        Mockito.verify(userRoleRepository, times(4)).findByIdUserIdOrderByIdRoleIdAsc("user-1");
        Mockito.verify(rolePermissionRepository, times(2)).findByIdRoleIdIn(List.of(1L));
    }

    @Test
    void finalPermissionsAreCachedAndInvalidatedAfterUserRoleChange() {
        User user = user("user-2", "pilot-2");
        Role adminRole = role(1L, "ADMIN");
        Permission dashboard = permission(10L, "DASHBOARD_VIEW");

        UserRole userRole = userRole(user, adminRole);
        RolePermission dashboardGrant = rolePermission(adminRole, dashboard);
        AtomicReference<List<UserRole>> userRoles = new AtomicReference<>(List.of());
        AtomicReference<List<RolePermission>> rolePermissions = new AtomicReference<>(List.of(dashboardGrant));

        when(userRepository.findById("user-2")).thenReturn(Optional.of(user));
        when(userRoleRepository.findByIdUserIdOrderByIdRoleIdAsc("user-2")).thenAnswer(invocation -> userRoles.get());
        when(rolePermissionRepository.findByIdRoleIdIn(List.of(1L))).thenAnswer(invocation -> rolePermissions.get());
        when(roleRepository.findById(1L)).thenReturn(Optional.of(adminRole));
        when(userRoleRepository.findByIdUserIdAndIdRoleId("user-2", 1L)).thenReturn(Optional.empty());
        when(userRoleRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        assertEquals(Set.of(), accessControlService.getFinalPermissionCodes("user-2"));

        accessControlService.assignRoleToUser("user-2", 1L);
        userRoles.set(List.of(userRole));

        Set<String> afterMutation = accessControlService.getFinalPermissionCodes("user-2");

        assertEquals(Set.of("DASHBOARD_VIEW"), afterMutation);
    }

    @Test
    void finalPermissionsAreCachedAndInvalidatedAfterReplaceUserRoles() {
        User user = user("user-3", "pilot-3");
        Role ceoRole = role(2L, "CEO");
        Permission dashboard = permission(10L, "DASHBOARD_VIEW");

        UserRole userRole = userRole(user, ceoRole);
        RolePermission dashboardGrant = rolePermission(ceoRole, dashboard);
        AtomicReference<List<UserRole>> userRoles = new AtomicReference<>(List.of());
        AtomicReference<List<RolePermission>> rolePermissions = new AtomicReference<>(List.of(dashboardGrant));

        when(userRepository.findById("user-3")).thenReturn(Optional.of(user));
        when(userRepository.existsById("user-3")).thenReturn(true);
        when(userRoleRepository.findByIdUserIdOrderByIdRoleIdAsc("user-3")).thenAnswer(invocation -> userRoles.get());
        when(rolePermissionRepository.findByIdRoleIdIn(List.of(2L))).thenAnswer(invocation -> rolePermissions.get());
        when(roleRepository.findByCode("CEO")).thenReturn(Optional.of(ceoRole));
        when(userRoleRepository.syncUserRoles("user-3", Set.of("CEO")))
            .thenReturn(new UserRoleSyncResult(Set.of(), 1L, 0L));

        assertEquals(Set.of(), accessControlService.getFinalPermissionCodes("user-3"));

        accessControlService.replaceUserRoles("user-3", Set.of("CEO"));
        userRoles.set(List.of(userRole));

        Set<String> afterMutation = accessControlService.getFinalPermissionCodes("user-3");

        assertEquals(Set.of("DASHBOARD_VIEW"), afterMutation);
    }

    private User user(String uid, String username) {
        User user = new User();
        user.setUid(uid);
        user.setUsername(username);
        user.setEnabled(true);
        return user;
    }

    private Role role(Long id, String code) {
        Role role = new Role();
        role.setId(id);
        role.setCode(code);
        role.setName(code);
        role.setDescription(code);
        role.setSystemRole(false);
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

    private UserRole userRole(User user, Role role) {
        UserRole userRole = new UserRole();
        UserRoleId id = new UserRoleId();
        id.setUserId(user.getUid());
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
}
