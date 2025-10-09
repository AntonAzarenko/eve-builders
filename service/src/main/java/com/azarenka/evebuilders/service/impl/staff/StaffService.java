package com.azarenka.evebuilders.service.impl.staff;

import com.azarenka.evebuilders.domain.db.DistributedOrder;
import com.azarenka.evebuilders.domain.db.Role;
import com.azarenka.evebuilders.domain.dto.UserDto;
import com.azarenka.evebuilders.service.api.IDistributedOrderService;
import com.azarenka.evebuilders.service.api.IStaffService;
import com.azarenka.evebuilders.service.api.IUserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class StaffService implements IStaffService {

    @Autowired
    private IUserService userService;
    @Autowired
    private IDistributedOrderService distributedOrderService;

    @Override
    public List<UserDto> getAllUsers() {
        return userService.getUsersDto();
    }

    @Override
    public List<UserDto> getUserWhoHasOneOrder() {
        List<UserDto> allUsers = getAllUsers();
        List<UserDto> activeUsers = new ArrayList<>();
        List<DistributedOrder> allOrders = distributedOrderService.getAllOrders();
        allUsers.forEach(user -> {
            if (allOrders.stream().filter(order -> order.getUserName().equals(user.getUsername())).count() > 0) {
                activeUsers.add(user);
            }
        });
        return activeUsers;
    }

    @Override
    public void updateUserRoles(UserDto user, Set<Role> selectedRoles) {
        userService.updateUserRoles(user, selectedRoles);
    }
}
