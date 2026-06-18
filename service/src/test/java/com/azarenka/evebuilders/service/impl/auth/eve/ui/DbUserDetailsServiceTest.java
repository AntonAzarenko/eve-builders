package com.azarenka.evebuilders.service.impl.auth.eve.ui;

import com.azarenka.evebuilders.domain.acl.UserRole;
import com.azarenka.evebuilders.domain.acl.UserRoleId;
import com.azarenka.evebuilders.domain.db.User;
import com.azarenka.evebuilders.repository.database.IUserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DbUserDetailsServiceTest {

    @Mock
    private IUserRepository userRepository;

    @InjectMocks
    private DbUserDetailsService dbUserDetailsService;

    @Test
    void loadUserByUsernameReturnsUserDetailsWithAclRoles() {
        User user = new User();
        user.setUsername("pilot");
        user.setPassword("secret");
        user.setEnabled(true);
        com.azarenka.evebuilders.domain.acl.Role builder = new com.azarenka.evebuilders.domain.acl.Role();
        builder.setId(1L);
        builder.setCode("BUILDER");

        UserRole userRole = new UserRole();
        UserRoleId id = new UserRoleId();
        id.setUserId("uid-1");
        id.setRoleId(1L);
        userRole.setId(id);
        userRole.setRole(builder);
        user.setUserRoles(Set.of(userRole));
        when(userRepository.findByUsername("pilot")).thenReturn(java.util.Optional.of(user));

        UserDetails result = dbUserDetailsService.loadUserByUsername("pilot");

        assertEquals("pilot", result.getUsername());
        assertEquals(Set.of(new SimpleGrantedAuthority("BUILDER")), Set.copyOf(result.getAuthorities()));
    }

    @Test
    void loadUserByUsernameThrowsWhenUserMissing() {
        when(userRepository.findByUsername("missing")).thenReturn(java.util.Optional.empty());

        assertThrows(UsernameNotFoundException.class,
            () -> dbUserDetailsService.loadUserByUsername("missing"));
    }
}
