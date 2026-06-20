package com.azarenka.evebuilders.rest.api.casino.ui;

import com.azarenka.evebuilders.domain.acl.Role;
import com.azarenka.evebuilders.domain.acl.RolePermission;
import com.azarenka.evebuilders.domain.acl.RolePermissionId;
import com.azarenka.evebuilders.domain.acl.UserPermission;
import com.azarenka.evebuilders.domain.acl.UserPermissionId;
import com.azarenka.evebuilders.domain.acl.UserRole;
import com.azarenka.evebuilders.domain.acl.UserRoleId;
import com.azarenka.evebuilders.domain.dto.acl.AdminUserSummaryDto;
import com.azarenka.evebuilders.domain.db.Permission;
import com.azarenka.evebuilders.domain.db.PermissionCode;
import com.azarenka.evebuilders.domain.db.User;
import com.azarenka.evebuilders.repository.database.IUserRepository;
import com.azarenka.evebuilders.repository.database.acl.IPermissionRepository;
import com.azarenka.evebuilders.repository.database.acl.IRolePermissionRepository;
import com.azarenka.evebuilders.repository.database.acl.IRoleRepository;
import com.azarenka.evebuilders.repository.database.acl.IUserPermissionRepository;
import com.azarenka.evebuilders.rest.api.ui.AccessControlAdminController;
import com.azarenka.evebuilders.service.api.IAccessControlService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AccessControlAdminControllerTest {

    private IAccessControlService accessControlService;
    private IUserRepository userRepository;
    private IRoleRepository roleRepository;
    private IPermissionRepository permissionRepository;
    private IRolePermissionRepository rolePermissionRepository;
    private IUserPermissionRepository userPermissionRepository;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        accessControlService = mock(IAccessControlService.class);
        userRepository = mock(IUserRepository.class);
        roleRepository = mock(IRoleRepository.class);
        permissionRepository = mock(IPermissionRepository.class);
        rolePermissionRepository = mock(IRolePermissionRepository.class);
        userPermissionRepository = mock(IUserPermissionRepository.class);

        mockMvc = MockMvcBuilders.standaloneSetup(new AccessControlAdminController(
            accessControlService,
            userRepository,
            roleRepository,
            permissionRepository,
            rolePermissionRepository,
            userPermissionRepository
        )).build();
    }

    @AfterEach
    void tearDown() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    @Test
    void getRolesForbiddenWithoutPermission() throws Exception {
        authenticate("pilot", "user-1", false);

        when(accessControlService.isSuperAdmin("user-1")).thenReturn(false);
        when(accessControlService.hasPermission("user-1", PermissionCode.ROLES_VIEW)).thenReturn(false);
        when(userRepository.findByUsername("pilot")).thenReturn(Optional.of(user("user-1", "pilot")));

        mockMvc.perform(get("/api/admin/roles"))
            .andExpect(status().isForbidden());
    }

    @Test
    void getRolesReturnsRolesWhenPermissionPresent() throws Exception {
        authenticate("pilot", "user-1", false);

        Role role = role(10L, "CEO", "CEO", "Executive role", true);
        Permission corporation = permission(1L, "CORPORATION_VIEW", "Corporation", "Corporation");

        when(accessControlService.isSuperAdmin("user-1")).thenReturn(false);
        when(accessControlService.hasPermission("user-1", PermissionCode.ROLES_VIEW)).thenReturn(true);
        when(userRepository.findByUsername("pilot")).thenReturn(Optional.of(user("user-1", "pilot")));
        when(roleRepository.findAllByOrderByCodeAsc()).thenReturn(List.of(role));
        when(rolePermissionRepository.findByIdRoleId(10L)).thenReturn(List.of(rolePermission(role, corporation)));

        mockMvc.perform(get("/api/admin/roles"))
            .andExpect(status().isOk())
            .andExpect(content().json("""
                [
                  {
                    "id":10,
                    "code":"CEO",
                    "name":"CEO",
                    "description":"Executive role",
                    "systemRole":true,
                    "permissions":[
                      {"id":1,"code":"CORPORATION_VIEW","name":"Corporation","description":"Corporation","groupName":"Corporation"}
                    ]
                  }
                ]
                """));
    }

    @Test
    void createRoleReturnsCreatedRole() throws Exception {
        authenticate("admin", "admin-1", true);

        Role created = role(10L, "NEW_ROLE", "New role", "Desc", false);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user("admin-1", "admin")));
        when(accessControlService.isSuperAdmin("admin-1")).thenReturn(true);
        when(accessControlService.createRole(any())).thenReturn(created);
        when(rolePermissionRepository.findByIdRoleId(10L)).thenReturn(List.of());

        mockMvc.perform(post("/api/admin/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"code":"new_role","name":"New role","description":"Desc"}
                    """))
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                  "id":10,
                  "code":"NEW_ROLE",
                  "name":"New role",
                  "description":"Desc",
                  "systemRole":false,
                  "permissions":[]
                }
                """));
    }

    @Test
    void superAdminCanViewPermissions() throws Exception {
        authenticate("admin", "admin-1", true);

        Permission dash = permission(1L, "DASHBOARD_VIEW", "Dash", "Dashboard");
        Permission contracts = permission(2L, "CONTRACTS_VIEW", "Contracts", "Contracts");

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user("admin-1", "admin")));
        when(accessControlService.isSuperAdmin("admin-1")).thenReturn(true);
        when(permissionRepository.findAllByOrderByGroupNameAscCodeAsc()).thenReturn(List.of(dash, contracts));

        mockMvc.perform(get("/api/admin/permissions"))
            .andExpect(status().isOk())
            .andExpect(content().json("""
                [
                  {"id":1,"code":"DASHBOARD_VIEW","name":"Dash","description":"Dash","groupName":"Dashboard"},
                  {"id":2,"code":"CONTRACTS_VIEW","name":"Contracts","description":"Contracts","groupName":"Contracts"}
                ]
                """));
    }

    @Test
    void getUsersReturnsSortedDirectory() throws Exception {
        authenticate("admin", "admin-1", true);

        User alice = user("user-1", "alice");
        alice.setCorporationName("Alpha Corp");
        User bob = user("user-2", "bob");
        bob.setCorporationName("Beta Corp");

        Role managerRole = role(10L, "MANAGER", "Manager", "Management role", true);
        Permission contracts = permission(2L, "CONTRACTS_VIEW", "Contracts", "Contracts");
        alice.setUserRoles(new LinkedHashSet<>(List.of(userRole(alice, managerRole))));
        alice.setDirectPermissions(new LinkedHashSet<>(List.of(userPermission(alice, contracts))));
        bob.setUserRoles(new LinkedHashSet<>());
        bob.setDirectPermissions(new LinkedHashSet<>());

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user("admin-1", "admin")));
        when(accessControlService.isSuperAdmin("admin-1")).thenReturn(true);
        when(userRepository.findAllByOrderByUsernameAscUidAsc()).thenReturn(List.of(alice, bob));

        mockMvc.perform(get("/api/admin/users"))
            .andExpect(status().isOk())
            .andExpect(content().json("""
                [
                  {
                    "userId":"user-1",
                    "username":"alice",
                    "characterName":"alice",
                    "corporationName":"Alpha Corp",
                    "roles":["MANAGER"],
                    "directPermissions":["CONTRACTS_VIEW"],
                    "superAdmin":false
                  },
                  {
                    "userId":"user-2",
                    "username":"bob",
                    "characterName":"bob",
                    "corporationName":"Beta Corp",
                    "roles":[],
                    "directPermissions":[],
                    "superAdmin":false
                  }
                ]
                """));
    }

    @Test
    void updateRolePermissionsReplacesPermissions() throws Exception {
        authenticate("admin", "admin-1", true);

        Role role = role(10L, "MANAGER", "Manager", "Management role", true);
        Permission dash = permission(1L, "DASHBOARD_VIEW", "Dash", "Dashboard");
        Permission contracts = permission(2L, "CONTRACTS_VIEW", "Contracts", "Contracts");
        RolePermission rp1 = rolePermission(role, dash);
        RolePermission rp2 = rolePermission(role, contracts);

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user("admin-1", "admin")));
        when(accessControlService.isSuperAdmin("admin-1")).thenReturn(true);
        when(roleRepository.findByCode("MANAGER")).thenReturn(Optional.of(role));
        when(permissionRepository.findByCode("DASHBOARD_VIEW")).thenReturn(Optional.of(dash));
        when(permissionRepository.findByCode("CONTRACTS_VIEW")).thenReturn(Optional.of(contracts));
        when(rolePermissionRepository.findByIdRoleId(10L)).thenReturn(List.of());
        when(accessControlService.assignPermissionToRole(10L, 1L)).thenReturn(rp1);
        when(accessControlService.assignPermissionToRole(10L, 2L)).thenReturn(rp2);
        when(rolePermissionRepository.findByIdRoleId(10L)).thenReturn(List.of(rp1, rp2));

        mockMvc.perform(put("/api/admin/roles/MANAGER/permissions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"permissionCodes":["DASHBOARD_VIEW","CONTRACTS_VIEW"]}
                    """))
            .andExpect(status().isOk())
            .andExpect(content().json("""
                [
                  {"id":1,"code":"DASHBOARD_VIEW","name":"Dash","description":"Dash","groupName":"Dashboard"},
                  {"id":2,"code":"CONTRACTS_VIEW","name":"Contracts","description":"Contracts","groupName":"Contracts"}
                ]
                """));

        verify(rolePermissionRepository).deleteAllByRoleId(10L);
        verify(accessControlService).assignPermissionToRole(10L, 1L);
        verify(accessControlService).assignPermissionToRole(10L, 2L);
    }

    @Test
    void getUserAccessReturnsMergedAccess() throws Exception {
        authenticate("admin", "admin-1", true);

        User user = user("user-2", "pilot");
        Role userRole = role(10L, "CEO", "CEO", "Executive role", true);
        Permission dash = permission(1L, "DASHBOARD_VIEW", "Dash", "Dashboard");
        Permission contracts = permission(2L, "CONTRACTS_VIEW", "Contracts", "Contracts");

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user("admin-1", "admin")));
        when(accessControlService.isSuperAdmin("admin-1")).thenReturn(true);
        when(userRepository.findById("user-2")).thenReturn(Optional.of(user));
        when(accessControlService.getUserRoles("user-2")).thenReturn(Set.of(userRole));
        when(accessControlService.getDirectPermissions("user-2")).thenReturn(Set.of(contracts));
        when(accessControlService.getFinalPermissionCodes("user-2")).thenReturn(new LinkedHashSet<>(Set.of("DASHBOARD_VIEW", "CONTRACTS_VIEW")));
        when(permissionRepository.findAllByOrderByGroupNameAscCodeAsc()).thenReturn(List.of(dash, contracts));
        when(rolePermissionRepository.findByIdRoleId(10L)).thenReturn(List.of(rolePermission(userRole, dash)));

        mockMvc.perform(get("/api/admin/users/user-2/access"))
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                  "userId":"user-2",
                  "username":"pilot",
                  "roles":[{"id":10,"code":"CEO","name":"CEO","description":"Executive role","systemRole":true,"permissions":[{"id":1,"code":"DASHBOARD_VIEW","name":"Dash","description":"Dash","groupName":"Dashboard"}]}],
                  "directPermissions":[{"id":2,"code":"CONTRACTS_VIEW","name":"Contracts","description":"Contracts","groupName":"Contracts"}],
                  "finalPermissions":[{"id":1,"code":"DASHBOARD_VIEW","name":"Dash","description":"Dash","groupName":"Dashboard"},{"id":2,"code":"CONTRACTS_VIEW","name":"Contracts","description":"Contracts","groupName":"Contracts"}],
                  "superAdmin":false
                }
                """));
    }

    @Test
    void updateUserRolesReplacesRoles() throws Exception {
        authenticate("admin", "admin-1", true);

        Role manager = role(10L, "MANAGER", "Manager", "Management role", true);
        Role ceo = role(11L, "CEO", "CEO", "Executive role", true);
        Role coordinator = role(12L, "COORDINATOR", "Coordinator", "Coordination role", true);
        User user = user("user-2", "pilot");

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user("admin-1", "admin")));
        when(accessControlService.isSuperAdmin("admin-1")).thenReturn(true);
        when(userRepository.findById("user-2")).thenReturn(Optional.of(user));
        when(accessControlService.replaceUserRoles("user-2", Set.of("COORDINATOR", "MANAGER", "CEO")))
            .thenReturn(new LinkedHashSet<>(List.of(coordinator, manager, ceo)));
        when(accessControlService.getUserRoles("user-2")).thenReturn(new LinkedHashSet<>(List.of(coordinator, manager, ceo)));
        when(accessControlService.getDirectPermissions("user-2")).thenReturn(Set.of());
        when(accessControlService.getFinalPermissionCodes("user-2")).thenReturn(Set.of());
        when(permissionRepository.findAllByOrderByGroupNameAscCodeAsc()).thenReturn(List.of());
        when(rolePermissionRepository.findByIdRoleId(10L)).thenReturn(List.of());
        when(rolePermissionRepository.findByIdRoleId(11L)).thenReturn(List.of());
        when(rolePermissionRepository.findByIdRoleId(12L)).thenReturn(List.of());

        mockMvc.perform(put("/api/admin/users/user-2/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"roleCodes":["COORDINATOR","MANAGER","CEO"]}
                    """))
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                  "userId":"user-2",
                  "username":"pilot",
                  "roles":[
                    {"id":12,"code":"COORDINATOR","name":"Coordinator","description":"Coordination role","systemRole":true,"permissions":[]},
                    {"id":10,"code":"MANAGER","name":"Manager","description":"Management role","systemRole":true,"permissions":[]},
                    {"id":11,"code":"CEO","name":"CEO","description":"Executive role","systemRole":true,"permissions":[]}
                  ],
                  "directPermissions":[],
                  "finalPermissions":[],
                  "superAdmin":false
                }
                """));

        verify(accessControlService).replaceUserRoles("user-2", Set.of("COORDINATOR", "MANAGER", "CEO"));
    }

    @Test
    void updateUserDirectPermissionsReplacesPermissions() throws Exception {
        authenticate("admin", "admin-1", true);

        Role role = role(10L, "BUILDER", "Builder", "Construction and production role", true);
        User user = user("user-2", "pilot");
        Permission contracts = permission(2L, "CONTRACTS_VIEW", "Contracts", "Contracts");

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user("admin-1", "admin")));
        when(accessControlService.isSuperAdmin("admin-1")).thenReturn(true);
        when(userRepository.findById("user-2")).thenReturn(Optional.of(user));
        when(permissionRepository.findByCode("CONTRACTS_VIEW")).thenReturn(Optional.of(contracts));
        when(rolePermissionRepository.findByIdRoleId(10L)).thenReturn(List.of());
        when(accessControlService.getUserRoles("user-2")).thenReturn(Set.of(role));
        when(accessControlService.getDirectPermissions("user-2")).thenReturn(Set.of(contracts));
        when(accessControlService.getFinalPermissionCodes("user-2")).thenReturn(Set.of("CONTRACTS_VIEW"));
        when(permissionRepository.findAllByOrderByGroupNameAscCodeAsc()).thenReturn(List.of(contracts));
        when(accessControlService.assignDirectPermissionToUser("user-2", 2L)).thenReturn(userPermission(user, contracts));

        mockMvc.perform(put("/api/admin/users/user-2/direct-permissions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"permissionCodes":["CONTRACTS_VIEW"]}
                    """))
            .andExpect(status().isOk());

        verify(userPermissionRepository).deleteAllByUserId("user-2");
        verify(accessControlService).assignDirectPermissionToUser("user-2", 2L);
    }

    private void authenticate(String username, String userId, boolean superAdmin) {
        org.springframework.security.core.context.SecurityContextHolder.getContext()
            .setAuthentication(new TestingAuthenticationToken(username, "n/a", "AUTHENTICATED"));
        User user = user(userId, username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(accessControlService.isSuperAdmin(userId)).thenReturn(superAdmin);
    }

    private User user(String uid, String username) {
        User user = new User();
        user.setUid(uid);
        user.setUsername(username);
        user.setCharacterId("123");
        user.setEnabled(true);
        return user;
    }

    private Role role(Long id, String code, String name, String description, boolean systemRole) {
        Role role = new Role();
        role.setId(id);
        role.setCode(code);
        role.setName(name);
        role.setDescription(description);
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

    private UserPermission userPermission(User user, Permission permission) {
        UserPermission userPermission = new UserPermission();
        UserPermissionId id = new UserPermissionId();
        id.setUserId(user.getUid());
        id.setPermissionId(permission.getId());
        userPermission.setId(id);
        userPermission.setUser(user);
        userPermission.setPermission(permission);
        return userPermission;
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
}
