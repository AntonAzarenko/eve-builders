package com.azarenka.evebuilders.service.api;

import com.azarenka.evebuilders.domain.db.TokenResponse;
import com.azarenka.evebuilders.domain.db.UserToken;

public interface IUserTokenService {

    /**
     *
     * @param userId
     * @param tokenResponse
     * @return
     */
    UserToken createUserToken(String userId, TokenResponse tokenResponse);

    /**
     *
     * @param userToken
     * @return
     */
    UserToken save(UserToken userToken);

    String getUserToken(String userId);

}
