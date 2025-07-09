package com.azarenka.evebuilders.main.request.coordinator.requests;

import com.azarenka.evebuilders.common.util.VaadinUtils;
import com.azarenka.evebuilders.component.SearchComponent;
import com.azarenka.evebuilders.component.View;
import com.azarenka.evebuilders.domain.db.RequestOrder;
import com.azarenka.evebuilders.domain.db.RequestOrderStatusEnum;
import com.azarenka.evebuilders.main.menu.MenuRequestCenterPage;
import com.azarenka.evebuilders.main.request.api.IRequestsController;
import com.azarenka.evebuilders.service.util.IOrderStatusToStringConverter;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.Route;

import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import jakarta.annotation.security.RolesAllowed;

@Route(value = "my-requests", layout = MenuRequestCenterPage.class)
@RolesAllowed({"ROLE_COORDINATOR"})
public class CoordinatorRequestsView extends View implements LocaleChangeObserver, IOrderStatusToStringConverter {

    private SearchComponent searchField;
    private ListDataProvider<RequestOrder> dataProvider;
    private Grid<RequestOrder> grid;
    private final IRequestsController controller;
    private Button submitButton;
    private Button suspendedRequestButton;
    private Button continueRequestButton;

    public CoordinatorRequestsView(@Autowired IRequestsController controller) {
        this.controller = controller;
        initMainLayout();
    }

    private void initMainLayout() {
        super.getStyle().set("padding", "0px 5px 0px 5px");
        add(initToolBarLayout(), initGrid());
        updateStatusButton();
    }

    private HorizontalLayout initToolBarLayout() {
        suspendedRequestButton = new Button(getTranslation("button.request_suspended"));
        suspendedRequestButton.addClickListener(event -> {
            var requestOrder = grid.getSelectedItems().stream().findFirst().get();
            requestOrder.setRequestStatus(RequestOrderStatusEnum.SUSPENDED);
            controller.updateRequest(requestOrder);
            UI.getCurrent().refreshCurrentRoute(true);
        });
        continueRequestButton = new Button("button.request_continue");
        continueRequestButton.addClickListener(event -> {
            var requestOrder = grid.getSelectedItems().stream().findFirst().get();
            requestOrder.setRequestStatus(
                Objects.nonNull(requestOrder.getPrice()) && !requestOrder.getPrice().equals(BigDecimal.ZERO) ?
                    RequestOrderStatusEnum.SUBMITTED : RequestOrderStatusEnum.CREATED);
            controller.updateRequest(requestOrder);
            UI.getCurrent().refreshCurrentRoute(true);
        });
        submitButton = new Button(getTranslation("button.submit"));
        submitButton.addClickListener(event -> {
            var requestOrder = grid.getSelectedItems().stream().findFirst().get();
            requestOrder.setRequestStatus(RequestOrderStatusEnum.APPROVED);
            controller.updateRequest(requestOrder);
            UI.getCurrent().refreshCurrentRoute(true);
        });
        submitButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_SMALL);
        continueRequestButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_SMALL);
        suspendedRequestButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_SMALL);
        searchField = new SearchComponent(getTranslation("order.search.placeholder"),
            event -> searchByText(searchField.getValue()),
            event -> clearSearch()
        );
        var layout = new HorizontalLayout(submitButton, suspendedRequestButton, continueRequestButton, searchField);
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
        grid.addSelectionListener(event -> {
            updateStatusButton();
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
            var list = items.stream()
                .filter(item -> (
                    (item.getItemName() != null && item.getItemName().toLowerCase().contains(lowerCaseValue)) ||
                        (item.getRequestStatus() != null && item.getRequestStatus().name().toLowerCase()
                            .contains(lowerCaseValue)) ||
                        (item.getId() != null && item.getId().toLowerCase().contains(lowerCaseValue))))
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
        addColumn(value -> convertRequestStatus(value.getRequestStatus()))
            .setWidth("200px");
        addColumn(RequestOrder::getItemName).setWidth("200px");
        addColumn(RequestOrder::getPriority).setWidth("100px");
        addNumberColumn(RequestOrder::getCount).setWidth("100px");
        addAmountColumn(RequestOrder::getPrice);
        addColumn(RequestOrder::getCreatedBy);
        addColumn(order -> order.getCreatedDate().toString()).setWidth("200px");
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
        grid.getColumns().get(7).setHeader(getTranslation("table.column.create_date_request"));
        grid.getColumns().get(8).setHeader(getTranslation("table.column.deadline"));
        searchField.setPlaceholder(getTranslation("request.search.placeholder"));
        submitButton.setText(getTranslation("button.submit"));
        suspendedRequestButton.setText(getTranslation("button.request_suspended"));
        continueRequestButton.setText(getTranslation("button.request_continue"));
    }

    private void updateStatusButton() {
        boolean selected = !grid.getSelectedItems().isEmpty();
        var request = getRequest();
        if (Objects.nonNull(request)) {
            var requestStatus = request.getRequestStatus();
            submitButton.setEnabled(selected && requestStatus == RequestOrderStatusEnum.SUBMITTED);
            suspendedRequestButton.setEnabled(selected && (requestStatus == RequestOrderStatusEnum.SUBMITTED ||
                requestStatus == RequestOrderStatusEnum.CREATED));
            continueRequestButton.setEnabled(selected && requestStatus == RequestOrderStatusEnum.SUSPENDED);
        } else {
            submitButton.setEnabled(false);
            suspendedRequestButton.setEnabled(false);
            continueRequestButton.setEnabled(false);
        }
    }

    private RequestOrder getRequest() {
        return grid.getSelectedItems()
            .stream()
            .findFirst()
            .orElse(null);
    }
}
