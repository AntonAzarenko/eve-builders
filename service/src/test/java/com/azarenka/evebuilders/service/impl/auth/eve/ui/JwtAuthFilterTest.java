package com.azarenka.evebuilders.service.impl.auth.eve.ui;

import com.azarenka.evebuilders.domain.auth.auth.ui.JwtProperties;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.concurrent.atomic.AtomicBoolean;

import jakarta.servlet.FilterChain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private JwtProperties props;

    private JwtAuthFilter filter;

    @BeforeEach
    void setUp() {
        lenient().when(props.refreshCookieName()).thenReturn("refresh");
        filter = new JwtAuthFilter(jwtService, userDetailsService, props);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void authenticatesFromBearerToken() throws Exception {
        UserDetails user = User.withUsername("pilot").password("x").authorities("ADMIN").build();
        when(jwtService.extractUsername("bearer-jwt")).thenReturn("pilot");
        when(userDetailsService.loadUserByUsername("pilot")).thenReturn(user);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/admin/roles");
        request.addHeader("Authorization", "Bearer bearer-jwt");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean chainCalled = new AtomicBoolean(false);
        FilterChain chain = (req, res) -> chainCalled.set(true);

        filter.doFilter(request, response, chain);

        assertEquals(true, chainCalled.get());
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("pilot", SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @Test
    void fallsBackToRefreshCookieWhenBearerTokenCannotAuthenticate() throws Exception {
        UserDetails user = User.withUsername("cookie-pilot").password("x").authorities("SUPER_ADMIN").build();
        when(jwtService.extractUsername("bad-jwt")).thenThrow(new RuntimeException("expired"));
        when(jwtService.isRefreshToken("refresh-jwt")).thenReturn(true);
        when(jwtService.extractUsername("refresh-jwt")).thenReturn("cookie-pilot");
        when(userDetailsService.loadUserByUsername("cookie-pilot")).thenReturn(user);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/admin/users");
        request.addHeader("Authorization", "Bearer bad-jwt");
        request.setCookies(new jakarta.servlet.http.Cookie("refresh", "refresh-jwt"));
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean chainCalled = new AtomicBoolean(false);
        FilterChain chain = (req, res) -> chainCalled.set(true);

        filter.doFilter(request, response, chain);

        assertEquals(true, chainCalled.get());
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("cookie-pilot", SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @Test
    void leavesContextEmptyWhenNoAuthenticationAvailable() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/admin/users");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean chainCalled = new AtomicBoolean(false);
        FilterChain chain = (req, res) -> chainCalled.set(true);

        filter.doFilter(request, response, chain);

        assertEquals(true, chainCalled.get());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
