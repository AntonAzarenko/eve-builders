package com.azarenka.evebuilders.main.staff;

import com.azarenka.evebuilders.common.util.IGridColumnAdder;
import com.azarenka.evebuilders.common.util.VaadinUtils;
import com.azarenka.evebuilders.component.View;
import com.azarenka.evebuilders.domain.db.DistributedOrder;
import com.azarenka.evebuilders.domain.dto.UserDto;
import com.azarenka.evebuilders.main.menu.MenuStaffPage;
import com.azarenka.evebuilders.main.staff.StaffByStateView.StaffUserInfo;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import jakarta.annotation.security.RolesAllowed;

@Route(value = "personal", layout = MenuStaffPage.class)
@RolesAllowed({"ROLE_ADMIN", "ROLE_SUPER_ADMIN"})
public class StaffByStateView extends View implements IGridColumnAdder<StaffUserInfo>,
    LocaleChangeObserver {

    private final StaffController controller;
    private final Button backButton = VaadinUtils.createLumoButton(VaadinIcon.BACKSPACE);
    private final List<DistributedOrder> distributedOrders;
    private final List<UserDto> users;

    private ListDataProvider<StaffUserInfo> dataProvider;
    private Grid<StaffUserInfo> grid;

    public StaffByStateView(@Autowired StaffController controller) {
        this.controller = controller;
        this.distributedOrders = controller.getDistributedOrders();
        this.users = (List<UserDto>) VaadinSession.getCurrent().getAttribute("usersDto");
        initContent();
    }

    private void initContent() {
        add(initToolbar(), initGrid());
    }

    private HorizontalLayout initToolbar() {
        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.setWidthFull();
        initButtons();
        toolbar.add(backButton);
        return toolbar;
    }

    private void initButtons() {
        backButton.addClickListener(event -> UI.getCurrent().navigate(StaffDashboard.class));
    }

    private Grid<StaffUserInfo> initGrid() {
        dataProvider = DataProvider.ofCollection(getData(distributedOrders, users));
        grid = VaadinUtils.initGrid(dataProvider, "distributed-orders-grid");
        addColumns();
        grid.getColumns().forEach(shipOrderDtoColumn -> {
            shipOrderDtoColumn.setSortable(true);
            shipOrderDtoColumn.setResizable(true);
        });
        return grid;
    }

    private void addColumns() {
        addColumn(StaffUserInfo::getCharacterId, "200px", grid);
        addColumn(StaffUserInfo::getUsername, "200px", grid);
       /* addComponentColumn(value -> new HorizontalLayout(new Span(value.getRoles().toString())),
            "200px", grid);*/
        addColumn(value -> value.getLastOrderDate().toString(), "200px", grid);
    }

    private List<StaffUserInfo> getData(List<DistributedOrder> distributedOrders, List<UserDto> users) {
        List<StaffUserInfo> userInfos = new ArrayList<>();
        if (users != null) {
            users.forEach(userDto -> {
                int count =
                    (int) distributedOrders.stream()
                        .filter(order -> order.getUserName().equals(userDto.getUsername()))
                        .count();
                String lastOrderDate = "";
                if (count > 0) {
                    Optional<DistributedOrder> max =
                        distributedOrders.stream()
                            .filter(order -> order.getUserName().equals(userDto.getUsername()))
                            .max(Comparator.comparing(DistributedOrder::getCreatedDate));
                    if (max.isPresent()) {
                        lastOrderDate = max.get().getCreatedDate().toString();
                    }
                }
                var staffUserInfo = new StaffUserInfo(count, lastOrderDate);
                staffUserInfo.setCharacterId(userDto.getCharacterId());
                staffUserInfo.setUsername(userDto.getUsername());
                userInfos.add(staffUserInfo);
            });
        }
        return userInfos;
    }

    public static class StaffUserInfo extends UserDto {

        private int countOrders;
        private String lastOrderDate;

        public StaffUserInfo(int countOrders, String lastOrderDate) {
            super();
            this.countOrders = countOrders;
            this.lastOrderDate = lastOrderDate;
        }

        public String getLastOrderDate() {
            return lastOrderDate;
        }

        public void setLastOrderDate(String lastOrderDate) {
            this.lastOrderDate = lastOrderDate;
        }

        public int getCountOrders() {
            return countOrders;
        }

        public void setCountOrders(int countOrders) {
            this.countOrders = countOrders;
        }
    }
}
