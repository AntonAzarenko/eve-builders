package com.azarenka.evebuilders.rest.api.casino.ui;

import com.azarenka.evebuilders.domain.auth.auth.ui.JwtProperties;
import com.azarenka.evebuilders.domain.auth.auth.ui.AuthProfile;
import com.azarenka.evebuilders.domain.db.User;
import com.azarenka.evebuilders.repository.database.IUserRepository;
import com.azarenka.evebuilders.rest.api.ui.AuthController;
import com.azarenka.evebuilders.rest.api.ui.RefreshCookie;
import com.azarenka.evebuilders.service.api.IAccessControlService;
import com.azarenka.evebuilders.service.impl.auth.eve.EveAuthService;
import com.azarenka.evebuilders.service.impl.auth.eve.EveOAuth2UserService;
import com.azarenka.evebuilders.service.impl.auth.eve.ui.JwtService;
import com.azarenka.evebuilders.service.impl.auth.eve.TokenRefreshService;
import com.azarenka.evebuilders.service.impl.intergarion.EveCharacterService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.servlet.http.Cookie;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest {

    private AuthenticationManager authenticationManager;
    private UserDetailsService userDetailsService;
    private JwtService jwtService;
    private JwtProperties props;
    private RefreshCookie refreshCookie;
    private EveAuthService eveAuthService;
    private EveOAuth2UserService eveOAuth2UserService;
    private IUserRepository userRepository;
    private IAccessControlService accessControlService;
    private EveCharacterService eveCharacterService;
    private TokenRefreshService tokenRefreshService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        authenticationManager = mock(AuthenticationManager.class);
        userDetailsService = mock(UserDetailsService.class);
        jwtService = mock(JwtService.class);
        props = mock(JwtProperties.class);
        refreshCookie = mock(RefreshCookie.class);
        eveAuthService = mock(EveAuthService.class);
        eveOAuth2UserService = mock(EveOAuth2UserService.class);
        userRepository = mock(IUserRepository.class);
        accessControlService = mock(IAccessControlService.class);
        eveCharacterService = mock(EveCharacterService.class);
        tokenRefreshService = mock(TokenRefreshService.class);

        when(props.refreshCookieName()).thenReturn("refresh");
        when(jwtService.generateAccessToken(any())).thenReturn("local-access-token");
        when(jwtService.generateRefreshToken(any())).thenReturn("local-refresh-token");

        mockMvc = MockMvcBuilders
            .standaloneSetup(new AuthController(
                authenticationManager,
                userDetailsService,
                jwtService,
                props,
                refreshCookie,
                eveAuthService,
                eveOAuth2UserService,
                userRepository,
                accessControlService,
                eveCharacterService,
                mock(com.azarenka.evebuilders.service.api.IUserTokenService.class),
                tokenRefreshService
            ))
            .build();
    }

    @AfterEach
    void tearDown() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    @Test
    void eveExchangeReturnsAccessTokenAndSetsRefreshCookie() throws Exception {
        User user = new User();
        user.setUid("uid-1");
        user.setUsername("pilot");
        user.setCharacterId("123456");
        user.setPassword("");
        user.setRoles(Set.of(com.azarenka.evebuilders.domain.db.Role.ROLE_VIEWER));
        user.setEnabled(true);

        when(eveAuthService.exchangeCodeForToken("auth-code"))
            .thenReturn(tokenResponse("eve-access-token"));
        when(eveOAuth2UserService.authenticateByAccessToken("eve-access-token"))
            .thenReturn(user);

        mockMvc.perform(post("/api/auth/eve/exchange")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"auth-code\"}"))
            .andExpect(status().isOk())
            .andExpect(content().json("{\"accessToken\":\"local-access-token\"}"));

        verify(eveAuthService).exchangeCodeForToken("auth-code");
        verify(eveOAuth2UserService).authenticateByAccessToken("eve-access-token");
        verify(refreshCookie).setRefreshCookie(any(), anyString());
    }

    @Test
    void meReturnsCurrentProfile() throws Exception {
        User user = new User();
        user.setUid("uid-1");
        user.setUsername("pilot");
        user.setCharacterId("123456789");
        user.setCorporationName("Corp Name");
        user.setCharacterInfo("{\"corporation_id\":\"555\"}");

        when(userRepository.findByUsername("pilot")).thenReturn(java.util.Optional.of(user));
        when(accessControlService.getAuthProfile("uid-1"))
            .thenReturn(new AuthProfile("uid-1", "pilot", Set.of("MANAGER"), new LinkedHashSet<>(Set.of("CONTRACTS_VIEW", "DASHBOARD_VIEW")), false));
        when(eveCharacterService.getParameter(user.getCharacterInfo(), "corporation_id", String.class)).thenReturn("555");

        org.springframework.security.core.context.SecurityContextHolder.getContext()
            .setAuthentication(new TestingAuthenticationToken("pilot", "n/a", "AUTHENTICATED"));

        mockMvc.perform(get("/api/auth/me"))
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                  "userId":"uid-1",
                  "eveCharacterId":"123456789",
                  "characterName":"pilot",
                  "corporationId":"555",
                  "corporationName":"Corp Name",
                  "roles":["MANAGER"],
                  "permissions":["CONTRACTS_VIEW","DASHBOARD_VIEW"],
                  "superAdmin":false
                }
                """));
    }

    @Test
    void profileAliasReturnsSuperAdminCurrentProfile() throws Exception {
        User user = new User();
        user.setUid("uid-2");
        user.setUsername("admin");
        user.setCharacterId("999999");
        user.setCorporationName("Admin Corp");
        user.setCharacterInfo("{\"corporation_id\":\"777\"}");

        when(userRepository.findByUsername("admin")).thenReturn(java.util.Optional.of(user));
        when(accessControlService.getAuthProfile("uid-2"))
            .thenReturn(new AuthProfile("uid-2", "admin", Set.of("SUPER_ADMIN"), Set.of(), true));
        when(eveCharacterService.getParameter(user.getCharacterInfo(), "corporation_id", String.class)).thenReturn("777");

        org.springframework.security.core.context.SecurityContextHolder.getContext()
            .setAuthentication(new TestingAuthenticationToken("admin", "n/a", "AUTHENTICATED"));

        mockMvc.perform(get("/api/auth/profile"))
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                  "userId":"uid-2",
                  "eveCharacterId":"999999",
                  "characterName":"admin",
                  "corporationId":"777",
                  "corporationName":"Admin Corp",
                  "roles":["SUPER_ADMIN"],
                  "permissions":[],
                  "superAdmin":true
                }
                """));
    }

    @Test
    void meFallsBackToRefreshCookieWhenAuthenticationMissing() throws Exception {
        User user = new User();
        user.setUid("uid-3");
        user.setUsername("cookie-pilot");
        user.setCharacterId("555555");
        user.setCorporationName("Cookie Corp");
        user.setCharacterInfo("{\"corporation_id\":\"888\"}");

        when(jwtService.isRefreshToken("refresh-jwt")).thenReturn(true);
        when(jwtService.extractUsername("refresh-jwt")).thenReturn("cookie-pilot");
        when(userRepository.findByUsername("cookie-pilot")).thenReturn(java.util.Optional.of(user));
        when(accessControlService.getAuthProfile("uid-3"))
            .thenReturn(new AuthProfile("uid-3", "cookie-pilot", Set.of("CEO"), Set.of("CORPORATION_VIEW"), false));
        when(eveCharacterService.getParameter(user.getCharacterInfo(), "corporation_id", String.class)).thenReturn("888");

        org.springframework.security.core.context.SecurityContextHolder.clearContext();

        mockMvc.perform(get("/api/auth/me")
                .cookie(new Cookie("refresh", "refresh-jwt")))
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                  "userId":"uid-3",
                  "eveCharacterId":"555555",
                  "characterName":"cookie-pilot",
                  "corporationId":"888",
                  "corporationName":"Cookie Corp",
                  "roles":["CEO"],
                  "permissions":["CORPORATION_VIEW"],
                  "superAdmin":false
                }
                """));
    }

    private com.azarenka.evebuilders.domain.db.TokenResponse tokenResponse(String accessToken) {
        var response = new com.azarenka.evebuilders.domain.db.TokenResponse();
        response.setAccessToken(accessToken);
        response.setExpiresIn(3600);
        response.setTokenType("Bearer");
        response.setRefreshToken("eve-refresh-token");
        return response;
    }
}
