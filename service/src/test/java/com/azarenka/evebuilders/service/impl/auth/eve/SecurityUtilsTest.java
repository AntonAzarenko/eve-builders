package com.azarenka.evebuilders.service.impl.auth.eve;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityUtilsTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void returnsUsernameFromUserDetailsAuthentication() {
        var principal = User.withUsername("pilot")
            .password("secret")
            .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
            .build();
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );

        assertEquals("pilot", SecurityUtils.getUserName());
        assertTrue(SecurityUtils.getCurrentUserName().isPresent());
    }

    @Test
    void returnsUsernameFromOAuth2UserAuthentication() {
        var principal = new DefaultOAuth2User(
            List.of(new SimpleGrantedAuthority("ROLE_USER")),
            Map.of("name", "eve-pilot"),
            "name"
        );
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );

        assertEquals("eve-pilot", SecurityUtils.getUserName());
    }

    @Test
    void returnsNullWhenAuthenticationIsAnonymous() {
        SecurityContextHolder.getContext().setAuthentication(
            new AnonymousAuthenticationToken(
                "key",
                "anonymousUser",
                List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
            )
        );

        assertNull(SecurityUtils.getUserName());
        assertFalse(SecurityUtils.getCurrentAuthentication().isPresent());
    }
}
