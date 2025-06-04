package com.azarenka.evebuilders.main.managment.api;

import com.azarenka.evebuilders.domain.db.Role;
import com.azarenka.evebuilders.domain.dto.UserDto;

import java.util.List;
import java.util.Set;

public interface IStaffController {


    List<UserDto> getAllUsers();

    void updateUserRoles(UserDto user, Set<Role> selectedRoles);
}
