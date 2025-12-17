package com.azarenka.evebuilders.service.impl;

import com.azarenka.evebuilders.domain.db.UserToken;
import com.azarenka.evebuilders.repository.database.IUserTokenRepository;
import com.azarenka.evebuilders.service.api.IUserTokenService;
import com.azarenka.evebuilders.service.impl.auth.eve.TokenRefreshService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserTokenService implements IUserTokenService {

    @Autowired
    private IUserTokenRepository repository;
    @Autowired
    private TokenRefreshService tokenRefreshService;

    @Override
    @Transactional
    public UserToken save(UserToken token) {
        return repository.save(token);
    }

    @Override
    public String getUserToken(String userId) {
        return tokenRefreshService
            .refreshTokenIfNeeded(userId)
            .defaultIfEmpty(repository.findById(userId).orElseThrow().getAccessToken())
            .block();
    }

    @Override
    public UserToken findByUserId(String uid) {
        return repository.findById(uid).orElse(null);
    }

    @Override
    public Optional<UserToken> getByUserId(String uid) {
        return repository.findById(uid);
    }

    @Override
    public void delete(String userId) {
        repository.deleteById(userId);
    }
}
