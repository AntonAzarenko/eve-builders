package com.azarenka.evebuilders.main.request.coordinator.requests;

import com.azarenka.evebuilders.common.util.VaadinUtils;
import com.azarenka.evebuilders.component.SearchComponent;
import com.azarenka.evebuilders.component.View;
import com.azarenka.evebuilders.domain.db.RequestOrder;
import com.azarenka.evebuilders.main.menu.MenuRequestCenterPage;
import com.azarenka.evebuilders.main.request.api.IRequestsController;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

@Route(value = "my-requests", layout = MenuRequestCenterPage.class)
@RolesAllowed({"ROLE_COORDINATOR"})
public class CoordinatorRequestsView extends View implements LocaleChangeObserver {

    private SearchComponent searchField;
    private ListDataProvider<RequestOrder> dataProvider;
    private Grid<RequestOrder> grid;
    private final IRequestsController controller;

    public CoordinatorRequestsView(@Autowired IRequestsController controller) {
        this.controller = controller;
        initMainLayout();
    }

    private void initMainLayout() {
        super.getStyle().set("padding", "0px 5px 0px 5px");
        add(initToolBarLayout(), initGrid());
    }

    private HorizontalLayout initToolBarLayout() {
        searchField = new SearchComponent(getTranslation("order.search.placeholder"),
                event -> searchByText(searchField.getValue()),
                event -> clearSearch()
        );
        HorizontalLayout layout = new HorizontalLayout(searchField);
        layout.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        layout.setWidthFull();
        return layout;
    }

    private Grid<RequestOrder> initGrid() {
        dataProvider = DataProvider.ofCollection(controller.getRequestOrders());
        grid = VaadinUtils.initGrid(dataProvider, "distributed-orders-grid");
        addColumns();
        grid.getColumns().forEach(shipOrderDtoColumn -> {
            shipOrderDtoColumn.setSortable(true);
            shipOrderDtoColumn.setResizable(true);
        });
        return grid;
    }

    private void clearSearch() {
        searchField.clearText();
        searchByText("");
    }

    private void searchByText(String value) {
        if (!value.isEmpty()) {
            Collection<RequestOrder> items = dataProvider.getItems();
            String lowerCaseValue = value.trim().toLowerCase();
            List<RequestOrder> list = items.stream()
                    .filter(item -> (
                            (item.getItemName() != null && item.getItemName().toLowerCase().contains(lowerCaseValue)) ||
                                    (item.getRequestStatus() != null && item.getRequestStatus().name().toLowerCase()
                                            .contains(lowerCaseValue))))
                    .toList();
            dataProvider = DataProvider.ofCollection(list);
            grid.setDataProvider(dataProvider);
            dataProvider.refreshAll();
        } else {
            dataProvider = DataProvider.ofCollection(controller.getRequestOrders());
            grid.setDataProvider(dataProvider);
            dataProvider.refreshAll();
        }
    }

    private void addColumns() {
        addColumn(RequestOrder::getId);
        addColumn(value -> value.getRequestStatus().name());
        addColumn(RequestOrder::getItemName);
        addColumn(RequestOrder::getPriority);
        addNumberColumn(RequestOrder::getCount);
        addAmountColumn(RequestOrder::getPrice);
        addColumn(RequestOrder::getCreatedBy);
        addColumn(order -> order.getFinishDate().toString());
    }

    private Grid.Column<RequestOrder> addAmountColumn(ValueProvider<RequestOrder, BigDecimal> provider) {
        Grid.Column<RequestOrder> column = grid.addColumn(provider);
        return column;
    }

    private Grid.Column<RequestOrder> addNumberColumn(ValueProvider<RequestOrder, Integer> provider) {
        Grid.Column<RequestOrder> column = grid.addColumn(provider);
        column.setTextAlign(ColumnTextAlign.END);
        return column;
    }

    private Grid.Column<RequestOrder> addColumn(ValueProvider<RequestOrder, String> provider) {
        Grid.Column<RequestOrder> column = grid.addColumn(provider);
        return column;
    }


    @Override
    public void localeChange(LocaleChangeEvent event) {
        grid.getColumns().get(0).setHeader(getTranslation("table.column.request_id"));
        grid.getColumns().get(1).setHeader(getTranslation("table.column.status"));
        grid.getColumns().get(2).setHeader(getTranslation("table.column.nomination"));
        grid.getColumns().get(3).setHeader(getTranslation("table.column.priority"));
        grid.getColumns().get(4).setHeader(getTranslation("table.column.count"));
        grid.getColumns().get(5).setHeader(getTranslation("table.column.price"));
        grid.getColumns().get(6).setHeader(getTranslation("table.column.created_by"));
        grid.getColumns().get(7).setHeader(getTranslation("table.column.deadline"));
        searchField.setPlaceholder(getTranslation("request.search.placeholder"));
    }
}
