package com.azarenka.evebuilders.main.staff;

import com.azarenka.evebuilders.domain.db.Role;
import com.azarenka.evebuilders.domain.dto.UserDto;
import com.azarenka.evebuilders.main.managment.api.IStaffController;
import com.azarenka.evebuilders.service.api.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Set;

@Controller
public class StaffController implements IStaffController {

    @Autowired
    private IUserService userService;

    @Override
    public List<UserDto> getAllUsers() {
        return userService.getUsersDto();
    }

    @Override
    public void updateUserRoles(UserDto user, Set<Role> selectedRoles) {
        userService.updateUserRoles(user, selectedRoles);
    }
}
