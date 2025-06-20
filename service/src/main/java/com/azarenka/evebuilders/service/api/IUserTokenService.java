package com.azarenka.evebuilders.service.api;

import com.azarenka.evebuilders.domain.db.TokenResponse;
import com.azarenka.evebuilders.domain.db.UserToken;

import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;

public interface IUserTokenService {

    UserToken save(UserToken userToken);

    String getUserToken(String userId);

}
