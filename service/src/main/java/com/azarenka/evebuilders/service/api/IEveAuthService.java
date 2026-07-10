package com.azarenka.evebuilders.service.api;

import com.azarenka.evebuilders.domain.db.TokenResponse;

public interface IEveAuthService {

    String generateAuthUrl();

    TokenResponse exchangeCodeForToken(String authorizationCode);


    String getAppVersion();
}
