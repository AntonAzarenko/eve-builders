package com.azarenka.evebuilders.main.staff;

import com.azarenka.evebuilders.component.StatCard;
import com.azarenka.evebuilders.component.View;
import com.azarenka.evebuilders.domain.dto.UserDto;
import com.azarenka.evebuilders.main.managment.api.IStaffController;
import com.azarenka.evebuilders.main.menu.MenuStaffPage;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.util.List;

@Route(value = "dashbord", layout = MenuStaffPage.class)
@RolesAllowed({"ROLE_ADMIN", "ROLE_SUPER_ADMIN"})
public class StaffDashboard extends View {

    public StaffDashboard(IStaffController controller) {
        HorizontalLayout statsLayout = new HorizontalLayout();
        statsLayout.addClassName("dashboard-stats");
        statsLayout.setSpacing(true);
        statsLayout.setPadding(true);


        List<UserDto> allUsers = controller.getAllUsers();
        StatCard totalUsers = new StatCard("Всего пользователей", String.valueOf(allUsers.size()),
                "Активных - 1");
        statsLayout.add(totalUsers);
        add(statsLayout);
    }
}
