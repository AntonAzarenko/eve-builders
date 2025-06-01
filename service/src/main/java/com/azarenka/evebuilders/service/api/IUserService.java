package com.azarenka.evebuilders.service.api;

import com.azarenka.evebuilders.domain.db.User;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Optional;

public interface IUserService {

    Optional<User> getByUsername(String username);

    User saveUser(User tokenResponse);

    void authenticateUser(User user, HttpServletRequest request);

    String getCharacterId();

    String getUserToken();

    void updateLanguage(String language);
}
