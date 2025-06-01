package com.azarenka.evebuilders.service.impl;

import com.azarenka.evebuilders.domain.mysql.TokenResponse;
import com.azarenka.evebuilders.domain.mysql.UserToken;
import com.azarenka.evebuilders.repository.mysql.IUserTokenRepository;
import com.azarenka.evebuilders.service.api.IUserTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UserTokenService implements IUserTokenService {

    @Autowired
    private IUserTokenRepository repository;

    @Override
    public UserToken createUserToken(String userId, TokenResponse tokenResponse) {
        UserToken userToken = new UserToken();
        userToken.setUserId(userId);
        userToken.setAccessToken(tokenResponse.getAccessToken());
        userToken.setRefreshToken(tokenResponse.getRefreshToken());
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(tokenResponse.getExpiresIn());
        userToken.setExpiresAt(expiresAt);
        return userToken;
    }

    @Override
    @Transactional
    public UserToken save(UserToken userToken) {
        return repository.save(userToken);
    }

    @Override
    public String getUserToken(String userId) {
        return repository.findById(userId).orElseThrow().getAccessToken();
    }
}
