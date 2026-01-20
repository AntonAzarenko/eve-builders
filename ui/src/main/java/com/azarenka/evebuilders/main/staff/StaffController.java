package com.azarenka.evebuilders.main.staff;

import com.azarenka.evebuilders.domain.db.DistributedOrder;
import com.azarenka.evebuilders.domain.db.Role;
import com.azarenka.evebuilders.domain.dto.UserDto;
import com.azarenka.evebuilders.main.managment.api.IStaffController;
import com.azarenka.evebuilders.service.api.IDistributedOrderService;
import com.azarenka.evebuilders.service.api.IStaffService;
import com.azarenka.evebuilders.service.api.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Set;

@Controller
public class StaffController implements IStaffController {

    @Autowired
    private IStaffService staffService;
    @Autowired
    private IDistributedOrderService distributedOrderService;

    @Override
    public List<UserDto> getAllUsers() {
        return staffService.getAllUsers();
    }

    @Override
    public List<UserDto> getUserWhoHasOneOrder() {
        return staffService.getUserWhoHasOneOrder();
    }

    @Override
    public void updateUserRoles(UserDto user, Set<Role> selectedRoles) {
        staffService.updateUserRoles(user, selectedRoles);
    }

    public List<DistributedOrder> getDistributedOrders() {
        return distributedOrderService.getAllOrders();
    }
}
