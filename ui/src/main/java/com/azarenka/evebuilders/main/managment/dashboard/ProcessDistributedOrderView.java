package com.azarenka.evebuilders.main.managment.dashboard;

import com.azarenka.evebuilders.common.util.IGridColumnAdder;
import com.azarenka.evebuilders.common.util.VaadinUtils;
import com.azarenka.evebuilders.component.View;
import com.azarenka.evebuilders.domain.OrderStatusEnum;
import com.azarenka.evebuilders.domain.db.DistributedOrder;
import com.azarenka.evebuilders.main.managment.api.IDashBoardController;
import com.azarenka.evebuilders.main.menu.MenuManagerPage;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.Route;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import jakarta.annotation.security.RolesAllowed;

@Route(value = "process-order", layout = MenuManagerPage.class)
@RolesAllowed({"ROLE_ADMIN", "ROLE_SUPER_ADMIN"})
public class ProcessDistributedOrderView extends View implements IGridColumnAdder<DistributedOrder>,
    LocaleChangeObserver {

    private final IDashBoardController controller;

    private final List<DistributedOrder> distributedOrders;

    private final Button backButton = VaadinUtils.createLumoButton(VaadinIcon.BACKSPACE);
    private final Button checkOrderButton = VaadinUtils.createLumoButton(VaadinIcon.CHECK_SQUARE);

    private ListDataProvider<DistributedOrder> dataProvider;
    private Grid<DistributedOrder> grid;

    public ProcessDistributedOrderView(@Autowired IDashBoardController controller) {
        this.controller = controller;
        this.distributedOrders = controller.getDistributedOrders();
        initContent();
        updateButtonStatus();
    }

    private void initContent() {
        add(initToolbar(), initGrid());
    }

    private HorizontalLayout initToolbar() {
        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.setWidthFull();
        initButtons();
        toolbar.add(backButton, checkOrderButton);
        return toolbar;
    }

    private Grid<DistributedOrder> initGrid() {
        dataProvider = DataProvider.ofCollection(distributedOrders.stream()
            .filter(distributedOrder -> distributedOrder.getOrderStatus() == OrderStatusEnum.WAITING_FOR_APPROVAL)
            .toList());
        grid = VaadinUtils.initGrid(dataProvider, "distributed-orders-grid");
        addColumns();
        grid.getColumns().forEach(shipOrderDtoColumn -> {
            shipOrderDtoColumn.setSortable(true);
            shipOrderDtoColumn.setResizable(true);
        });
        grid.addSelectionListener(event -> updateButtonStatus());
        return grid;
    }

    private void updateButtonStatus() {
        Optional<DistributedOrder> selectedItem = grid.getSelectionModel().getFirstSelectedItem();
        var isSelected = selectedItem.isPresent();
        checkOrderButton.setEnabled(isSelected && selectedItem.get().getOrderStatus() != OrderStatusEnum.COMPLETED);
    }

    private void addColumns() {
        addColumn(DistributedOrder::getOrderNumber, "200px", grid);
        addColumn(value -> value.getOrderStatus().name(), "200px", grid);
        addColumn(DistributedOrder::getShipName, "200px", grid);
        addIntegerColumn(DistributedOrder::getCount, "200px", grid);
        addIntegerColumn(DistributedOrder::getCountReady, "200px", grid);
        addColumn(DistributedOrder::getUserName, "200px", grid);
    }

    private void initButtons() {
        backButton.addClickListener(event -> UI.getCurrent().navigate(DashboardView.class));
        checkOrderButton.addClickListener(event -> {
            Optional<DistributedOrder> distributedOrderOptional = grid.getSelectionModel().getFirstSelectedItem();
            distributedOrderOptional.ifPresent(distributedOrder -> {
                var contractReports = controller.getReporOtrder(distributedOrderOptional.get());
                var orderContractReportWindow =
                    new OrderContractReportWindow(contractReports, distributedOrder, controller);
                orderContractReportWindow.open();
            });
        });
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        grid.getColumns().get(0).setHeader(getTranslation("table.column.order_number"));
        grid.getColumns().get(1).setHeader(getTranslation("table.column.status"));
        grid.getColumns().get(2).setHeader(getTranslation("table.column.nomination"));
        grid.getColumns().get(3).setHeader(getTranslation("table.column.count"));
        grid.getColumns().get(4).setHeader(getTranslation("table.column.ready_count"));
        grid.getColumns().get(5).setHeader(getTranslation("table.column.user_name"));
    }
}
