package com.azarenka.evebuilders.main.request.admin;

import com.azarenka.evebuilders.common.util.VaadinUtils;
import com.azarenka.evebuilders.component.SearchComponent;
import com.azarenka.evebuilders.component.View;
import com.azarenka.evebuilders.domain.db.RequestOrder;
import com.azarenka.evebuilders.domain.db.RequestOrderStatusEnum;
import com.azarenka.evebuilders.main.managment.create.CreateOrderView;
import com.azarenka.evebuilders.main.menu.MenuRequestCenterPage;
import com.azarenka.evebuilders.main.request.api.IRequestsController;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Route(value = "requests", layout = MenuRequestCenterPage.class)
@RolesAllowed({"ROLE_ADMIN", "ROLE_SUPER_ADMIN"})
@PageTitle("Requests")
public class RequestsView extends View implements LocaleChangeObserver {

    private SearchComponent searchField;
    private ListDataProvider<RequestOrder> dataProvider;
    private Grid<RequestOrder> grid;
    private final IRequestsController controller;
    private Button applyButton;

    public RequestsView(@Autowired IRequestsController controller) {
        this.controller = controller;
        initMainLayout();
    }

    private void initMainLayout() {
        super.getStyle().set("padding", "0px 5px 0px 5px");
        add(initToolBarLayout(), initGrid());
        updateButtonStatus();
    }

    private HorizontalLayout initToolBarLayout() {
        searchField = new SearchComponent(getTranslation("order.search.placeholder"),
                event -> searchByText(searchField.getValue()),
                event -> clearSearch()
        );
        applyButton = new Button(VaadinIcon.LAYOUT.create());
        applyButton.setText("Обработать");
        applyButton.addClickListener(event -> onApplyButtonClicked());
        applyButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ICON);
        HorizontalLayout layout = new HorizontalLayout(applyButton, searchField);
        layout.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        layout.setWidthFull();
        return layout;
    }

    private void onApplyButtonClicked() {
        Optional<RequestOrder> firstSelectedItem = grid.getSelectionModel().getFirstSelectedItem();
        firstSelectedItem.ifPresent(requestOrder -> {
            VaadinSession.getCurrent().setAttribute("requestOrder", requestOrder);
            UI.getCurrent().navigate(CreateOrderView.class);
        });
    }

    private Grid<RequestOrder> initGrid() {
        dataProvider = DataProvider.ofCollection(controller.getRequestOrders());
        grid = VaadinUtils.initGrid(dataProvider, "distributed-orders-grid");
        addColumns();
        grid.getColumns().forEach(shipOrderDtoColumn -> {
            shipOrderDtoColumn.setSortable(true);
            shipOrderDtoColumn.setResizable(true);
        });
        grid.addItemClickListener(event -> updateButtonStatus());
        return grid;
    }

    private void updateButtonStatus() {
        Optional<RequestOrder> firstSelectedItem = grid.getSelectionModel().getFirstSelectedItem();
        boolean isSelected = firstSelectedItem.isPresent();
        applyButton.setEnabled(isSelected
                && firstSelectedItem.get().getRequestStatus() == RequestOrderStatusEnum.SUBMITTED);
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
