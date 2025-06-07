package com.azarenka.evebuilders.main.orders;

import com.azarenka.evebuilders.common.util.VaadinUtils;
import com.azarenka.evebuilders.domain.db.DistributedOrder;
import com.azarenka.evebuilders.main.commonview.CommonDialogComponent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;

import java.math.BigDecimal;
import java.util.List;

public class OrderDetailsWindow extends CommonDialogComponent implements LocaleChangeObserver {

    private Grid<DistributedOrder> grid;
    private ListDataProvider<DistributedOrder> dataProvider;

    public OrderDetailsWindow(List<DistributedOrder> orders) {
        super.applyCommonProperties("order_info", true);
        dataProvider = DataProvider.ofCollection(orders);
        super.setWidth("1000px");
        super.setHeight("400px");
        setHeaderTitle(getTranslation("table.column.order_number") + " " + orders.get(0).getOrderNumber());
        VerticalLayout layout = VaadinUtils.initCommonVerticalLayout();
        layout.add(initGrid());
        add(layout);
        getFooter().add(createCloseButton());
    }

    private Grid<DistributedOrder> initGrid() {
        grid = VaadinUtils.initGrid(dataProvider, "distributed-orders-grid");
        addColumns();
        grid.getColumns().forEach(shipOrderDtoColumn -> {
            shipOrderDtoColumn.setSortable(true);
            shipOrderDtoColumn.setResizable(true);
        });
        return grid;
    }

    private void addColumns() {
        addColumn(DistributedOrder::getOrderNumber);
        addColumn(value -> value.getOrderStatus().name());
        addColumn(DistributedOrder::getShipName);
        addNumberColumn(DistributedOrder::getCount);
        addNumberColumn(DistributedOrder::getCountReady);
        addColumn(DistributedOrder::getUserName);
    }

    private Grid.Column<DistributedOrder> addComponentColumn(ValueProvider<DistributedOrder, Button> provider) {
        Grid.Column<DistributedOrder> column = grid.addComponentColumn(provider);
        return column;
    }

    private Grid.Column<DistributedOrder> addAmountColumn(ValueProvider<DistributedOrder, BigDecimal> provider) {
        Grid.Column<DistributedOrder> column = grid.addColumn(provider);
        return column;
    }

    private Grid.Column<DistributedOrder> addNumberColumn(ValueProvider<DistributedOrder, Integer> provider) {
        Grid.Column<DistributedOrder> column = grid.addColumn(provider);
        column.setTextAlign(ColumnTextAlign.END);
        return column;
    }

    private Grid.Column<DistributedOrder> addColumn(ValueProvider<DistributedOrder, String> provider) {
        Grid.Column<DistributedOrder> column = grid.addColumn(provider);
        return column;
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
