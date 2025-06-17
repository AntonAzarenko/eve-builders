package com.azarenka.evebuilders.main.managment.create;

import com.azarenka.evebuilders.common.util.VaadinUtils;
import com.azarenka.evebuilders.component.View;
import com.azarenka.evebuilders.domain.OrderStatusEnum;
import com.azarenka.evebuilders.domain.db.Order;
import com.azarenka.evebuilders.domain.db.RequestOrderStatusEnum;
import com.azarenka.evebuilders.main.managment.api.ICreateOrderController;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.server.VaadinSession;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Optional;

public class ExistingOrdersView extends View implements LocaleChangeObserver {

    private final ICreateOrderController controller;
    private Grid<Order> grid;
    private ListDataProvider<Order> dataProvider;
    private HorizontalLayout toolbarLayout = new HorizontalLayout();
    private Button recycleButton;
    private Button repeatOrderButton;
    private Button editButton;

    public ExistingOrdersView(ICreateOrderController controller) {
        this.controller = controller;
        super.getStyle().set("padding-left", "10px");
        initContent();
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        grid.getColumns().get(0).setHeader(getTranslation("table.column.order_number"));
        grid.getColumns().get(1).setHeader(getTranslation("table.column.status"));
        grid.getColumns().get(2).setHeader(getTranslation("table.column.nomination"));
        grid.getColumns().get(3).setHeader(getTranslation("table.column.count"));
        grid.getColumns().get(4).setHeader(getTranslation("table.column.price"));
    }

    private void initContent() {
        initGrid();
        initToolbarLayout();
        add(toolbarLayout, grid);
        updateStatusButtons();
    }

    private void initGrid() {
        dataProvider = DataProvider.ofCollection(controller.getOriginalOrders());
        grid = VaadinUtils.initGrid(dataProvider, "ship-orders-grid");
        addColumns();
        grid.getColumns().forEach(shipOrderDtoColumn -> {
            shipOrderDtoColumn.setSortable(true);
            shipOrderDtoColumn.setResizable(true);
        });
        grid.addSelectionListener(selectionEvent -> updateStatusButtons());
    }

    private void initToolbarLayout() {
        recycleButton = new Button(VaadinIcon.RECYCLE.create());
        recycleButton.setTooltipText("Удалить заказ");
        recycleButton.addClickListener(event -> {
            grid.getSelectionModel().getFirstSelectedItem().ifPresent(order -> {
                if (order.getOrderStatus() == OrderStatusEnum.NEW) {
                    controller.removeOrder(order.getOrderNumber());
                    controller.updateRequestStatusOrder(
                            controller.getRequestOrderById(order.getRequestId()), RequestOrderStatusEnum.SUBMITTED);
                    String message = String.format("Заказ %s был удален", order.getOrderNumber());
                    Notification.show(message);
                    UI.getCurrent().refreshCurrentRoute(true);
                } else {
                    Notification.show(String.format("Заказ %s не может быть удален так как уже находится в работе",
                            order.getOrderNumber()), 3000, Notification.Position.MIDDLE);
                }
            });
        });
        repeatOrderButton = new Button(VaadinIcon.REPLY_ALL.create());
        repeatOrderButton.setTooltipText("Повторить заказ");
        repeatOrderButton.addClickListener(event -> {
            Optional<Order> firstSelectedItem = grid.getSelectionModel().getFirstSelectedItem();
            if (firstSelectedItem.isPresent()) {
                Order order = firstSelectedItem.get();
                order.setId(null);
                order.setOrderNumber(null);
                moveOrderToParameters(order);
            }
        });
        editButton = new Button(VaadinIcon.EDIT.create());
        editButton.setTooltipText("Редактировать заказ");
        editButton.addClickListener(event -> {
            Optional<Order> firstSelectedItem = grid.getSelectionModel().getFirstSelectedItem();
            if (firstSelectedItem.isPresent()) {
                moveOrderToParameters(firstSelectedItem.get());
            }
        });
        repeatOrderButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        recycleButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        editButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        toolbarLayout.add(recycleButton, repeatOrderButton, editButton);
    }

    private void moveOrderToParameters(Order order) {
        VaadinSession.getCurrent().setAttribute("originalOrder", order);
        UI.getCurrent().navigate(CreateOrderView.class);
        UI.getCurrent().refreshCurrentRoute(true);
    }

    private void addColumns() {
        addColumn(Order::getOrderNumber);
        addColumn(value -> value.getOrderStatus().name());
        addColumn(Order::getShipName);
        addNumberColumn(Order::getCount);
        addAmountColumn(order -> formatIsk(order.getPrice()));
    }

    private Grid.Column<Order> addAmountColumn(ValueProvider<Order, ?> provider) {
        Grid.Column<Order> column = grid.addColumn(provider);
        column.setTextAlign(ColumnTextAlign.END);
        return column;
    }

    private Grid.Column<Order> addNumberColumn(ValueProvider<Order, Integer> provider) {
        Grid.Column<Order> column = grid.addColumn(provider);
        column.setTextAlign(ColumnTextAlign.END);
        return column;
    }

    private Grid.Column<Order> addColumn(ValueProvider<Order, String> provider) {
        Grid.Column<Order> column = grid.addColumn(provider);
        return column;
    }

    private String formatIsk(BigDecimal value) {
        if (value == null) return "";
        DecimalFormat df = new DecimalFormat("#,##0.00");
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("ru", "RU"));
        symbols.setGroupingSeparator(' ');
        df.setDecimalFormatSymbols(symbols);
        return df.format(value) + " ISK";
    }

    private void updateStatusButtons() {
        Optional<Order> firstSelectedItem = grid.getSelectionModel().getFirstSelectedItem();
        boolean isOrderSelected = firstSelectedItem.isPresent();
        boolean isEditButtonEnabled =
                isOrderSelected && firstSelectedItem.get().getOrderStatus() != OrderStatusEnum.COMPLETED;
        recycleButton.setEnabled(isOrderSelected);
        repeatOrderButton.setEnabled(isOrderSelected);
        editButton.setEnabled(isEditButtonEnabled);
    }
}
