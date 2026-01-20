package com.azarenka.evebuilders.service.impl;

import com.azarenka.evebuilders.domain.db.Role;
import com.azarenka.evebuilders.domain.db.User;
import com.azarenka.evebuilders.domain.dto.UserDto;
import com.azarenka.evebuilders.repository.database.IUserRepository;
import com.azarenka.evebuilders.service.api.IUserTokenService;
import com.azarenka.evebuilders.service.impl.auth.eve.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private IUserRepository userRepository;

    @Mock
    private IUserTokenService userTokenService;

    @InjectMocks
    private UserService userService;

    private final String username = "testuser";
    private final String userId = "user-123";
    private final String characterId = "char-999";
    private final String language = "en";

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setUid(userId);
        sampleUser.setUsername(username);
        sampleUser.setCharacterId(characterId);
        sampleUser.setLanguage(language);
        sampleUser.setRoles(Set.of(Role.ROLE_BUILDER));
    }

    @Test
    void getByUsernameUserExistsReturnsUser() {
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(sampleUser));
        Optional<User> result = userService.getByUsername(username);
        assertTrue(result.isPresent());
        assertEquals(sampleUser, result.get());
    }

    @Test
    void getByUsernameUserNotFoundReturnsEmpty() {
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        Optional<User> result = userService.getByUsername(username);
        assertTrue(result.isEmpty());
    }

    @Test
    void getByUserIdUserExistsReturnsUser() {
        when(userRepository.findByUid(userId)).thenReturn(Optional.of(sampleUser));
        Optional<User> result = userService.getByUserId(userId);
        assertTrue(result.isPresent());
        assertEquals(sampleUser, result.get());
    }

    @Test
    void getByUserIdUserNotFoundReturnsEmpty() {
        when(userRepository.findByUid(userId)).thenReturn(Optional.empty());
        Optional<User> result = userService.getByUserId(userId);
        assertTrue(result.isEmpty());
    }

    @Test
    void saveUserReturnsSavedUser() {
        when(userRepository.save(sampleUser)).thenReturn(sampleUser);
        User result = userService.saveUser(sampleUser);
        assertEquals(sampleUser, result);
    }

    @Test
    void getCharacterIdUserExistsReturnsCharacterId() {
        try (MockedStatic<SecurityUtils> utilities = mockStatic(SecurityUtils.class)) {
            utilities.when(SecurityUtils::getUserName).thenReturn(username);
            when(userRepository.findByUsername(username)).thenReturn(Optional.of(sampleUser));
            String result = userService.getCharacterId();
            assertEquals(characterId, result);
        }
    }

    @Test
    void getCharacterIdUserNotFoundReturnsNull() {
        try (MockedStatic<SecurityUtils> utilities = mockStatic(SecurityUtils.class)) {
            utilities.when(SecurityUtils::getUserName).thenReturn(username);
            when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
            String result = userService.getCharacterId();
            assertNull(result);
        }
    }

    @Test
    void getUserTokenUserExistsReturnsToken() {
        String token = "mock-token";
        try (MockedStatic<SecurityUtils> utilities = mockStatic(SecurityUtils.class)) {
            utilities.when(SecurityUtils::getUserName).thenReturn(username);
            when(userRepository.findByUsername(username)).thenReturn(Optional.of(sampleUser));
            when(userTokenService.getUserToken(userId)).thenReturn(token);
            String result = userService.getUserToken();
            assertEquals(token, result);
        }
    }

    @Test
    void updateLanguageUserExistsUpdatesLanguage() {
        try (MockedStatic<SecurityUtils> utilities = mockStatic(SecurityUtils.class)) {
            utilities.when(SecurityUtils::getUserName).thenReturn(username);
            when(userRepository.findByUsername(username)).thenReturn(Optional.of(sampleUser));
            userService.updateLanguage("ru");
            assertEquals("ru", sampleUser.getLanguage());
            verify(userRepository).save(sampleUser);
        }
    }

    @Test
    void updateLanguageUserNotFoundDoesNothing() {
        try (MockedStatic<SecurityUtils> utilities = mockStatic(SecurityUtils.class)) {
            utilities.when(SecurityUtils::getUserName).thenReturn(username);
            when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
            userService.updateLanguage("fr");
            verify(userRepository, never()).save(any());
        }
    }

    @Test
    void getUsersDtoReturnsDtoList() {
        List<User> users = List.of(sampleUser);
        when(userRepository.findAll()).thenReturn(users);
        List<UserDto> result = userService.getUsersDto();
        assertEquals(1, result.size());
        assertEquals(username, result.get(0).getUsername());
        assertEquals(characterId, result.get(0).getCharacterId());
        assertEquals(sampleUser.getRoles(), result.get(0).getRoles());
    }

    @Test
    void updateUserRolesUserExistsUpdatesRoles() {
        Set<Role> newRoles = Set.of(Role.ROLE_BUILDER);
        UserDto userDto = new UserDto(username, characterId, newRoles);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(sampleUser));
        userService.updateUserRoles(userDto, newRoles);
        assertEquals(newRoles, sampleUser.getRoles());
        verify(userRepository).save(sampleUser);
    }

    @Test
    void updateUserRolesUserNotFoundDoesNothing() {
        Set<Role> newRoles = Set.of(Role.ROLE_ADMIN);
        UserDto userDto = new UserDto(username, characterId, newRoles);
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        userService.updateUserRoles(userDto, newRoles);
        verify(userRepository, never()).save(any());
    }
}