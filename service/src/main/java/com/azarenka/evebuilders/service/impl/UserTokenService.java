package com.azarenka.evebuilders.service.impl;

import com.azarenka.evebuilders.domain.db.UserToken;
import com.azarenka.evebuilders.repository.database.IUserTokenRepository;
import com.azarenka.evebuilders.service.api.IUserTokenService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserTokenService implements IUserTokenService {

    @Autowired
    private IUserTokenRepository repository;

    @Override
    @Transactional
    public UserToken save(UserToken token) {
        return repository.save(token);
    }

    @Override
    public String getUserToken(String userId) {
        return repository.findById(userId).orElseThrow().getAccessToken();
    }
}
