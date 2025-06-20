package com.azarenka.evebuilders.service.api;

import com.azarenka.evebuilders.domain.db.Role;
import com.azarenka.evebuilders.domain.db.User;
import com.azarenka.evebuilders.domain.dto.UserDto;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface IUserService {

    Optional<User> getByUsername(String username);

    User saveUser(User tokenResponse);

    String getCharacterId();

    String getUserToken();

    void updateLanguage(String language);

    List<UserDto> getUsersDto();

    void updateUserRoles(UserDto user, Set<Role> selectedRoles);
}
