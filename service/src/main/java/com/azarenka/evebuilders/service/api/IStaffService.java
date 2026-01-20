package com.azarenka.evebuilders.service.api;

import com.azarenka.evebuilders.domain.db.Role;
import com.azarenka.evebuilders.domain.dto.UserDto;

import java.util.List;
import java.util.Set;

public interface IStaffService {

    List<UserDto> getAllUsers();

    List<UserDto> getUserWhoHasOneOrder();

    void updateUserRoles(UserDto user, Set<Role> selectedRoles);
}
