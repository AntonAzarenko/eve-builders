package com.azarenka.evebuilders.service.impl.auth.eve;

import com.azarenka.evebuilders.domain.db.User;
import com.azarenka.evebuilders.repository.database.IUserRepository;
import com.azarenka.evebuilders.service.api.IAccessControlService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AccessControlSecurityTest {

    private IAccessControlService accessControlService;
    private IUserRepository userRepository;
    private AccessControlSecurity accessControlSecurity;

    @BeforeEach
    void setUp() {
        accessControlService = mock(IAccessControlService.class);
        userRepository = mock(IUserRepository.class);
        accessControlSecurity = new AccessControlSecurity(accessControlService, userRepository);
    }

    @AfterEach
    void tearDown() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    @Test
    void ordinaryUserWithPermissionCanAccess() {
        authenticate("pilot", "user-1");
        when(userRepository.findByUsername("pilot")).thenReturn(Optional.of(user("user-1", "pilot")));
        when(accessControlService.isSuperAdmin("user-1")).thenReturn(false);
        when(accessControlService.hasPermission("user-1", "CORPORATION_CONTRACT_VIEW")).thenReturn(true);

        assertTrue(accessControlSecurity.can("CORPORATION_CONTRACT_VIEW"));
    }

    @Test
    void directPermissionUserCanAccess() {
        authenticate("pilot", "user-1");
        when(userRepository.findByUsername("pilot")).thenReturn(Optional.of(user("user-1", "pilot")));
        when(accessControlService.isSuperAdmin("user-1")).thenReturn(false);
        when(accessControlService.hasPermission("user-1", "CORPORATION_CONTRACT_EDIT")).thenReturn(true);

        assertTrue(accessControlSecurity.hasCurrentUserPermission("CORPORATION_CONTRACT_EDIT"));
    }

    @Test
    void superAdminBypassesPermissionChecks() {
        authenticate("admin", "user-1");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user("user-1", "admin")));
        when(accessControlService.isSuperAdmin("user-1")).thenReturn(true);

        assertTrue(accessControlSecurity.can("CORPORATION_CONTRACT_EDIT"));
        assertTrue(accessControlSecurity.hasPermission("user-1", "CONTRACTS_EDIT"));
        verify(accessControlService, never()).hasPermission("user-1", "CORPORATION_CONTRACT_EDIT");
    }

    @Test
    void userWithoutPermissionsIsDenied() {
        authenticate("pilot", "user-1");
        when(userRepository.findByUsername("pilot")).thenReturn(Optional.of(user("user-1", "pilot")));
        when(accessControlService.isSuperAdmin("user-1")).thenReturn(false);
        when(accessControlService.hasPermission("user-1", "CORPORATION_CONTRACT_VIEW")).thenReturn(false);

        assertFalse(accessControlSecurity.can("CORPORATION_CONTRACT_VIEW"));
    }

    @Test
    void unauthenticatedUserIsDenied() {
        assertFalse(accessControlSecurity.can("CORPORATION_CONTRACT_VIEW"));
    }

    private void authenticate(String username, String userId) {
        org.springframework.security.core.context.SecurityContextHolder.getContext()
            .setAuthentication(new TestingAuthenticationToken(username, "n/a", "AUTHENTICATED"));
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user(userId, username)));
    }

    private User user(String uid, String username) {
        User user = new User();
        user.setUid(uid);
        user.setUsername(username);
        user.setEnabled(true);
        return user;
    }
}
