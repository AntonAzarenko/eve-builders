package com.azarenka.evebuilders.main.managment.create;

import com.azarenka.evebuilders.common.util.VaadinUtils;
import com.azarenka.evebuilders.component.View;
import com.azarenka.evebuilders.domain.mysql.Order;
import com.azarenka.evebuilders.main.managment.api.ICreateOrderController;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.function.ValueProvider;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class ExistingOrdersView extends View {

    private final ICreateOrderController controller;
    private Grid<Order> grid;
    private ListDataProvider<Order> dataProvider;

    public ExistingOrdersView(ICreateOrderController controller) {
        this.controller = controller;
        initContent();
    }

    private void initContent() {
        initGrid();
        add(grid);
    }

    private void initGrid() {
        dataProvider = DataProvider.ofCollection(controller.getOriginalOrders());
        grid = VaadinUtils.initGrid(dataProvider, "ship-orders-grid");
        addColumns();
        grid.getColumns().forEach(shipOrderDtoColumn -> {
            shipOrderDtoColumn.setSortable(true);
            shipOrderDtoColumn.setResizable(true);
        });
    }

    private void initButtonsLayout() {

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
}
