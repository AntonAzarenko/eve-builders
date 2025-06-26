package com.azarenka.evebuilders.service.impl;

import com.azarenka.evebuilders.domain.db.UserToken;
import com.azarenka.evebuilders.repository.database.IUserTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserTokenServiceTest {
    @Mock
    private IUserTokenRepository repository;

    @InjectMocks
    private UserTokenService userTokenService;

    private final String userId = "user-123";
    private final String accessToken = "token-xyz";

    private UserToken token;

    @BeforeEach
    void setUp() {
        token = new UserToken();
        token.setUserId(userId);
        token.setAccessToken(accessToken);
    }

    @Test
    void testSaveReturnsSavedToken() {
        when(repository.save(token)).thenReturn(token);
        UserToken result = userTokenService.save(token);
        assertEquals(token, result);
        verify(repository).save(token);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void testGetUserTokenWhenUserExistsReturnsToken() {
        when(repository.findById(userId)).thenReturn(Optional.of(token));
        String result = userTokenService.getUserToken(userId);
        assertEquals(accessToken, result);
        verify(repository).findById(userId);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void testGetUserTokenWhenUserNotFoundThrowsException() {
        when(repository.findById(userId)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> userTokenService.getUserToken(userId));
        verify(repository).findById(userId);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void testFindByUserIdWhenUserExistsReturnsToken() {
        when(repository.findById(userId)).thenReturn(Optional.of(token));
        UserToken result = userTokenService.findByUserId(userId);
        assertEquals(token, result);
        verify(repository).findById(userId);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void testFindByUserIdWhenUserNotFoundThrowsException() {
        when(repository.findById(userId)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> userTokenService.findByUserId(userId));
        verify(repository).findById(userId);
        verifyNoMoreInteractions(repository);
    }
}