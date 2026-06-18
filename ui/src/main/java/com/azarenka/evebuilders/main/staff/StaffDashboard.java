package com.azarenka.evebuilders.main.staff;

import com.azarenka.evebuilders.component.StatCard;
import com.azarenka.evebuilders.component.View;
import com.azarenka.evebuilders.domain.db.Role;
import com.azarenka.evebuilders.domain.dto.UserDto;
import com.azarenka.evebuilders.main.managment.api.IStaffController;
import com.azarenka.evebuilders.main.menu.MenuStaffPage;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.access.prepost.PreAuthorize;

@Route(value = "dashbord", layout = MenuStaffPage.class)
@PreAuthorize("@accessControlSecurity.canAny('CORPORATION_CONTRACT_EDIT','CORPORATION_VIEW')")
public class StaffDashboard extends View {

    private final FlexLayout cards = new FlexLayout();
    private final IStaffController controller;
    private final List<UserDto> allUsers;

    public StaffDashboard(IStaffController controller) {
        this.controller = controller;
        allUsers = controller.getAllUsers();
        initContent();
        add(cards);
    }

    private void initContent() {
        cards.setWidthFull();
        cards.getStyle().set("gap", "12px");
        cards.getStyle().set("flex-wrap", "wrap");
        cards.add(initTotalStaff(), initBuilders(), initMiners(), initMinersAndBuilders(), initActiveUsers(),
            initInActiveUsers());
    }

    private Component initTotalStaff() {
        List<UserDto> userWhoHasOneOrder = controller.getUserWhoHasOneOrder();
        StatCard totalUsers = new StatCard("Всего пользователей", String.valueOf(allUsers.size()),
            "Активных - " + userWhoHasOneOrder.size());
        totalUsers.addClickListener(event -> {
            VaadinSession.getCurrent().setAttribute("usersDto", allUsers);
            UI.getCurrent().navigate(StaffByStateView.class);
        });
        return totalUsers;
    }

    private Component initMiners() {
        List<UserDto> miners =
            allUsers.stream().filter(userDto -> userDto.getRoles().contains(Role.ROLE_MINER)).toList();
        StatCard minerUsersCard = new StatCard("Всего Майнеров", String.valueOf(miners.size()),
            "Активных - 0");
        minerUsersCard.addClickListener(event -> {
            VaadinSession.getCurrent().setAttribute("usersDto", miners);
            UI.getCurrent().navigate(StaffByStateView.class);
        });
        return minerUsersCard;
    }

    private Component initBuilders() {
        List<UserDto> miners =
            allUsers.stream().filter(userDto -> userDto.getRoles().contains(Role.ROLE_BUILDER)).toList();
        StatCard buildersUsersCard = new StatCard("Всего строителей", String.valueOf(miners.size()),
            "Активных - 0");
        buildersUsersCard.addClickListener(event -> {
            VaadinSession.getCurrent().setAttribute("usersDto", miners);
            UI.getCurrent().navigate(StaffByStateView.class);
        });
        return buildersUsersCard;
    }

    private Component initMinersAndBuilders() {
        List<UserDto> combinedUsers = new ArrayList<>();
        allUsers.forEach(userDto -> {
            if (userDto.getRoles().contains(Role.ROLE_BUILDER) && userDto.getRoles().contains(Role.ROLE_MINER)) {
                combinedUsers.add(userDto);
            }
        });
        StatCard minerAndBuildersCard =
            new StatCard("Пользователи с ролями майнер и строитель", String.valueOf(combinedUsers.size()),
                "");
        minerAndBuildersCard.addClickListener(event -> {
            VaadinSession.getCurrent().setAttribute("usersDto", combinedUsers);
            UI.getCurrent().navigate(StaffByStateView.class);
        });
        return minerAndBuildersCard;
    }

    private Component initActiveUsers() {
        List<UserDto> userWhoHasOneOrder = controller.getUserWhoHasOneOrder();
        StatCard activeUsersCard = new StatCard("Активные пользователи", String.valueOf(userWhoHasOneOrder.size()),
            "");

        activeUsersCard.addClickListener(event -> {
            VaadinSession.getCurrent().setAttribute("usersDto", userWhoHasOneOrder);
            UI.getCurrent().navigate(StaffByStateView.class);
        });
        return activeUsersCard;
    }

    private Component initInActiveUsers() {
        List<UserDto> userWhoHasOneOrder = controller.getUserWhoHasOneOrder();
        List<UserDto> inActiveUsers = allUsers.stream()
            .filter(u -> u.getRoles().contains(Role.ROLE_BUILDER))
            .filter(u -> !userWhoHasOneOrder.contains(u))
            .collect(Collectors.toList());
        StatCard inActiveUsersCard = new StatCard("Неактивные пользователи", String.valueOf(inActiveUsers.size()), "");

        inActiveUsersCard.addClickListener(event -> {
            VaadinSession.getCurrent().setAttribute("usersDto", inActiveUsers);
            UI.getCurrent().navigate(StaffByStateView.class);
        });
        return inActiveUsersCard;
    }
}
