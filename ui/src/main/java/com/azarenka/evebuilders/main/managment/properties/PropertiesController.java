package com.azarenka.evebuilders.main.managment.properties;

import com.azarenka.evebuilders.domain.db.Role;
import com.azarenka.evebuilders.domain.dto.UserDto;
import com.azarenka.evebuilders.main.managment.api.IPropertiesController;
import com.azarenka.evebuilders.service.api.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Set;

@Controller
public class PropertiesController implements IPropertiesController {

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
