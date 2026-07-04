package com.azarenka.evebuilders.main.managment.orders;

import com.azarenka.evebuilders.common.util.IGridColumnAdder;
import com.azarenka.evebuilders.common.util.VaadinUtils;
import com.azarenka.evebuilders.component.View;
import com.azarenka.evebuilders.domain.db.DistributedOrder;
import com.azarenka.evebuilders.domain.db.Order;
import com.azarenka.evebuilders.domain.dto.ShipOrderDto;
import com.azarenka.evebuilders.domain.enums.OrderStatusEnum;
import com.azarenka.evebuilders.main.commonview.NotificationWindow;
import com.azarenka.evebuilders.main.managment.api.IDashBoardController;
import com.azarenka.evebuilders.main.managment.api.IOrdersManagmentController;
import com.azarenka.evebuilders.main.managment.create.CreateOrderView;
import com.azarenka.evebuilders.main.managment.dashboard.OrderContractReportWindow;
import com.azarenka.evebuilders.main.menu.MenuManagerPage;
import com.azarenka.evebuilders.service.impl.contract.ContractValidationReport;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.splitlayout.SplitLayoutVariant;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import jakarta.annotation.security.RolesAllowed;

@Route(value = "orders-managment", layout = MenuManagerPage.class)
@RolesAllowed({"ROLE_ADMIN", "ROLE_SUPER_ADMIN"})
@PageTitle("Managment")
public class OrdersManagmentView extends View implements LocaleChangeObserver, IGridColumnAdder<ShipOrderDto> {

    private final IOrdersManagmentController controller;
    private ListDataProvider<ShipOrderDto> dataProvider;
    private Grid<ShipOrderDto> grid;
    private Map<String, Long> usersCountByOrder;

    private ListDataProvider<DistributedOrder> distributedOrderDataProvider;
    private Grid<DistributedOrder> distributedOrderGrid;

    private final Button leftChangeStatusButton = new Button(VaadinIcon.EXCHANGE.create());
    private final Button leftEditButton = new Button(VaadinIcon.EDIT.create());
    private final Button leftAuditButton = new Button(VaadinIcon.CLIPBOARD_TEXT.create());

    private final Button rightValidateButton = new Button(VaadinIcon.CHECK.create());
    private final Button rightChangeStatusButton = new Button(VaadinIcon.EXCHANGE.create());
    private final Button rightDiscardButton = new Button(VaadinIcon.TRASH.create());
    private final Button rightChangeUserButton = new Button(VaadinIcon.USER.create());

    public OrdersManagmentView(@Autowired IOrdersManagmentController controller) {
        this.controller = controller;
        setPadding(true);
        initContent();
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        grid.getColumns().get(0).setHeader(getTranslation("table.column.order_number"));
        grid.getColumns().get(1).setHeader(getTranslation("table.column.status"));
        grid.getColumns().get(2).setHeader(getTranslation("table.column.nomination"));
        grid.getColumns().get(3).setHeader(getTranslation("table.column.count"));
        grid.getColumns().get(4).setHeader(getTranslation("table.column.count_free"));
        grid.getColumns().get(5).setHeader(getTranslation("table.column.price"));
        grid.getColumns().get(6).setHeader(getTranslation("table.column.count_users"));

        distributedOrderGrid.getColumns().get(0).setHeader(getTranslation("table.column.user_name"));
        distributedOrderGrid.getColumns().get(1).setHeader(getTranslation("table.column.status"));
        distributedOrderGrid.getColumns().get(2).setHeader(getTranslation("table.column.count"));
        distributedOrderGrid.getColumns().get(3).setHeader(getTranslation("table.column.ready_count"));

        leftChangeStatusButton.setTooltipText(getTranslation("button.management.change_status"));
        leftEditButton.setTooltipText(getTranslation("button.management.edit"));
        leftAuditButton.setTooltipText(getTranslation("button.management.audit"));

        rightValidateButton.setTooltipText(getTranslation("button.management.validate"));
        rightChangeStatusButton.setTooltipText(getTranslation("button.management.change_status"));
        rightDiscardButton.setTooltipText(getTranslation("button.management.discard"));
        rightChangeUserButton.setTooltipText(getTranslation("button.management.change_user"));
    }

    private void initContent() {
        usersCountByOrder = controller.getActiveUsersByOrderNumber();
        dataProvider = DataProvider.ofCollection(controller.getOrders());
        distributedOrderDataProvider = DataProvider.ofCollection(List.of());

        grid = initOrdersGrid();
        distributedOrderGrid = initDistributedOrdersGrid();

        selectFirstOrder();
        updateButtonStatus();
        add(initSplitLayout());
    }

    private SplitLayout initSplitLayout() {
        SplitLayout splitLayout = new SplitLayout();

        VerticalLayout primary = VaadinUtils.initCommonVerticalLayout(initLeftToolbar(), grid);
        VerticalLayout secondary = VaadinUtils.initCommonVerticalLayout(initRightToolbar(), distributedOrderGrid);

        primary.getStyle().set("padding", "0px 5px 30px 5px");
        secondary.getStyle().set("padding", "0px 5px 30px 5px");

        splitLayout.addToPrimary(primary);
        splitLayout.addToSecondary(secondary);
        splitLayout.setSplitterPosition(70);
        splitLayout.addThemeVariants(SplitLayoutVariant.LUMO_SMALL);
        splitLayout.setSizeFull();
        return splitLayout;
    }

    private HorizontalLayout initLeftToolbar() {
        leftChangeStatusButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ICON);
        leftEditButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ICON);
        leftAuditButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ICON);
        leftChangeStatusButton.setTooltipText(getTranslation("button.management.change_status"));
        leftEditButton.setTooltipText(getTranslation("button.management.edit"));
        leftAuditButton.setTooltipText(getTranslation("button.management.audit"));

        leftChangeStatusButton.addClickListener(event -> openChangeStatusWindow());
        leftEditButton.addClickListener(event -> editOrder());
        leftAuditButton.addClickListener(event -> openAuditWindow());

        HorizontalLayout toolbar = new HorizontalLayout(leftChangeStatusButton, leftEditButton, leftAuditButton);
        toolbar.setWidthFull();
        return toolbar;
    }

    private void openChangeStatusWindow() {
        Optional<ShipOrderDto> selectedOrder = grid.getSelectionModel().getFirstSelectedItem();
        selectedOrder.ifPresent(order -> {
            ChangeOrderStatusWindow changeOrderStatusWindow = new ChangeOrderStatusWindow(
                controller,
                order,
                () -> UI.getCurrent().refreshCurrentRoute(true)
            );
            changeOrderStatusWindow.open();
        });
    }

    private void editOrder() {
        Optional<ShipOrderDto> selectedOrder = grid.getSelectionModel().getFirstSelectedItem();
        selectedOrder.ifPresent(orderDto -> {
            Order order = controller.getOriginalOrderByOrderNumber(orderDto.getOrderNumber());
            VaadinSession.getCurrent().setAttribute("originalOrder", order);
            UI.getCurrent().navigate(CreateOrderView.class);
        });
    }

    private void openAuditWindow() {
        Optional<ShipOrderDto> selectedOrder = grid.getSelectionModel().getFirstSelectedItem();
        selectedOrder.ifPresent(order -> {
            var orderAuditRecords = controller.getOrderAuditRecordsByOrderNumber(order.getOrderNumber());
            OrderAuditWindow orderAuditWindow = new OrderAuditWindow(orderAuditRecords);
            orderAuditWindow.open();
        });
    }

    private HorizontalLayout initRightToolbar() {
        rightValidateButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ICON);
        rightChangeStatusButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ICON);
        rightDiscardButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ICON);
        rightChangeUserButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ICON);
        rightValidateButton.setTooltipText(getTranslation("button.management.validate"));
        rightChangeStatusButton.setTooltipText(getTranslation("button.management.change_status"));
        rightDiscardButton.setTooltipText(getTranslation("button.management.discard"));
        rightChangeUserButton.setTooltipText(getTranslation("button.management.change_user"));

        rightValidateButton.addClickListener(event -> openValidateWindow());
        rightChangeStatusButton.addClickListener(event -> openChangeDistributedStatusWindow());
        rightDiscardButton.addClickListener(event -> onDiscardOrderClicked());
        rightChangeUserButton.addClickListener(event -> VaadinUtils.showNotification("Not implemented yet"));

        HorizontalLayout toolbar = new HorizontalLayout(
            rightValidateButton,
            rightChangeStatusButton,
            rightDiscardButton,
            rightChangeUserButton
        );
        toolbar.setWidthFull();
        return toolbar;
    }

    private void onDiscardOrderClicked() {
        Optional<DistributedOrder> selectedOrder = distributedOrderGrid.getSelectionModel().getFirstSelectedItem();
        selectedOrder.ifPresent(order -> {
            if (order.getOrderStatus() != OrderStatusEnum.COMPLETED) {
                LocalDate createdOrderDate = order.getCreatedDate();
                LocalDate deadlineDate = order.getFinishedDate();
                LocalDate now = LocalDate.now();
                long totalDays = ChronoUnit.DAYS.between(createdOrderDate, deadlineDate);
                long halfDays = totalDays / 2;
                long daysLeft = ChronoUnit.DAYS.between(now, deadlineDate);

                if (daysLeft < 0) {
                    new NotificationWindow("Warning",
                        "You cannot discard this order because the deadline has already passed.").open();
                } else if (daysLeft < halfDays) {
                    new NotificationWindow("Warning",
                        "You cannot discard this order because more than half of the order period has passed.").open();
                } else {
                    controller.discardDistributedOrder(order);
                    UI.getCurrent().refreshCurrentRoute(true);
                }
            } else {
                new NotificationWindow("Error",
                    "You cannot discard this order because it is already completed.").open();
            }
        });
    }

    private void openValidateWindow() {
        Optional<DistributedOrder> selectedOrder = distributedOrderGrid.getSelectionModel().getFirstSelectedItem();
        selectedOrder.ifPresent(distributedOrder -> {
            List<ContractValidationReport> contractReports = controller.getReportOrder(distributedOrder);
            IDashBoardController windowController = new IDashBoardController() {
                @Override
                public List<Order> getOrders() {
                    return List.of();
                }

                @Override
                public List<DistributedOrder> getDistributedOrders() {
                    return controller.getDistributedOrdersByOrderNumber(distributedOrder.getOrderNumber());
                }

                @Override
                public List<ContractValidationReport> getReportOtrder(DistributedOrder selectedDistributedOrder) {
                    return controller.getReportOrder(selectedDistributedOrder);
                }

                @Override
                public void update(DistributedOrder selectedDistributedOrder, Integer readyCount) {
                    controller.updateDistributedOrder(selectedDistributedOrder, readyCount);
                }
            };

            OrderContractReportWindow orderContractReportWindow =
                new OrderContractReportWindow(contractReports, distributedOrder, windowController);
            orderContractReportWindow.open();
        });
    }

    private void openChangeDistributedStatusWindow() {
        Optional<DistributedOrder> selectedOrder = distributedOrderGrid.getSelectionModel().getFirstSelectedItem();
        selectedOrder.ifPresent(order -> {
            ChangeDistributedOrderStatusWindow window = new ChangeDistributedOrderStatusWindow(
                controller,
                order,
                () -> UI.getCurrent().refreshCurrentRoute(true)
            );
            window.open();
        });
    }

    private Grid<ShipOrderDto> initOrdersGrid() {
        Grid<ShipOrderDto> ordersGrid = VaadinUtils.initGrid(dataProvider, "orders-managment-grid");
        addOrdersColumns(ordersGrid);
        ordersGrid.getColumns().forEach(column -> {
            column.setSortable(true);
            column.setResizable(true);
        });
        ordersGrid.addSelectionListener(selectionEvent -> {
            Optional<ShipOrderDto> selectedOrder = selectionEvent.getFirstSelectedItem();
            selectedOrder.ifPresentOrElse(this::updateDistributedOrders, this::clearDistributedOrders);
            updateButtonStatus();
        });
        return ordersGrid;
    }

    private Grid<DistributedOrder> initDistributedOrdersGrid() {
        Grid<DistributedOrder> detailsGrid = VaadinUtils.initGrid(distributedOrderDataProvider,
            "orders-managment-users-grid");
        addDistributedOrdersColumns(detailsGrid);
        detailsGrid.getColumns().forEach(column -> {
            column.setSortable(true);
            column.setResizable(true);
        });
        detailsGrid.addSelectionListener(event -> updateButtonStatus());
        return detailsGrid;
    }

    private void addOrdersColumns(Grid<ShipOrderDto> ordersGrid) {
        Function<ShipOrderDto, String> statusText =
            o -> o.getOrderStatus() == null ? "" : o.getOrderStatus().name();

        addColumn(ShipOrderDto::getOrderNumber, "130px", ordersGrid);
        addBadgeColumn(value -> badge(value.getOrderStatus()), "200px", ordersGrid, statusText);
        addColumn(ShipOrderDto::getItemName, "150px", ordersGrid);
        addIntegerColumn(ShipOrderDto::getCount, "100px", ordersGrid);
        addIntegerColumn(order -> order.getCount() - order.getInProgressCount(), "90px", ordersGrid);
        addAmountColumn(order -> formatIsk(order.getPrice()), "160px", ordersGrid);
        addIntegerColumn(order -> getUsersCount(order).intValue(), "120px", ordersGrid);
    }

    private void addDistributedOrdersColumns(Grid<DistributedOrder> detailsGrid) {
        Grid.Column<DistributedOrder> userColumn = detailsGrid.addColumn(DistributedOrder::getUserName);
        userColumn.setWidth("170px");

        Grid.Column<DistributedOrder> statusColumn =
            detailsGrid.addColumn(order -> order.getOrderStatus() == null ? "" : order.getOrderStatus().name());
        statusColumn.setWidth("170px");

        Grid.Column<DistributedOrder> countColumn = detailsGrid.addColumn(DistributedOrder::getCount);
        countColumn.setWidth("100px");
        countColumn.setTextAlign(ColumnTextAlign.END);

        Grid.Column<DistributedOrder> readyColumn = detailsGrid.addColumn(DistributedOrder::getCountReady);
        readyColumn.setWidth("100px");
        readyColumn.setTextAlign(ColumnTextAlign.END);
    }

    private void selectFirstOrder() {
        Optional<ShipOrderDto> firstOrder = dataProvider.getItems().stream().findFirst();
        firstOrder.ifPresentOrElse(order -> {
            grid.select(order);
            updateDistributedOrders(order);
        }, this::clearDistributedOrders);
    }

    private void updateDistributedOrders(ShipOrderDto order) {
        List<DistributedOrder> distributedOrders = controller.getDistributedOrdersByOrderNumber(order.getOrderNumber());
        distributedOrderDataProvider = DataProvider.ofCollection(distributedOrders);
        distributedOrderGrid.setDataProvider(distributedOrderDataProvider);
        distributedOrderDataProvider.refreshAll();
    }

    private void clearDistributedOrders() {
        distributedOrderDataProvider = DataProvider.ofCollection(List.of());
        distributedOrderGrid.setDataProvider(distributedOrderDataProvider);
        distributedOrderDataProvider.refreshAll();
    }

    private void updateButtonStatus() {
        boolean isOrderSelected = grid != null && grid.getSelectionModel().getFirstSelectedItem().isPresent();
        leftChangeStatusButton.setEnabled(isOrderSelected);
        leftEditButton.setEnabled(isOrderSelected);
        leftAuditButton.setEnabled(isOrderSelected);

        Optional<DistributedOrder> distributedSelected = distributedOrderGrid == null
            ? Optional.empty()
            : distributedOrderGrid.getSelectionModel().getFirstSelectedItem();
        boolean isDistributedSelected = distributedSelected.isPresent();
        rightValidateButton.setEnabled(isDistributedSelected
            && distributedSelected.get().getOrderStatus() != OrderStatusEnum.COMPLETED);
        rightChangeStatusButton.setEnabled(isDistributedSelected);
        rightDiscardButton.setEnabled(isDistributedSelected);
        rightChangeUserButton.setEnabled(isDistributedSelected);
    }

    private Long getUsersCount(ShipOrderDto order) {
        return usersCountByOrder.getOrDefault(order.getOrderNumber(), 0L);
    }

    private String formatIsk(BigDecimal value) {
        if (Objects.nonNull(value)) {
            DecimalFormat df = new DecimalFormat("#,##0.00");
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("ru", "RU"));
            symbols.setGroupingSeparator(' ');
            df.setDecimalFormatSymbols(symbols);
            return df.format(value) + " ISK";
        }
        return "";
    }
}


