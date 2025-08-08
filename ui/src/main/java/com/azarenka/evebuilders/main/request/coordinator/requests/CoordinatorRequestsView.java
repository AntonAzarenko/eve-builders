package com.azarenka.evebuilders.main.request.coordinator.requests;

import com.azarenka.evebuilders.common.util.VaadinUtils;
import com.azarenka.evebuilders.component.SearchComponent;
import com.azarenka.evebuilders.component.View;
import com.azarenka.evebuilders.domain.db.Fit;
import com.azarenka.evebuilders.domain.db.RequestOrder;
import com.azarenka.evebuilders.domain.db.RequestOrderStatusEnum;
import com.azarenka.evebuilders.main.commonview.FitView;
import com.azarenka.evebuilders.main.menu.MenuRequestCenterPage;
import com.azarenka.evebuilders.main.request.api.ICreateRequestController;
import com.azarenka.evebuilders.main.request.create.CreateRequestView;
import com.azarenka.evebuilders.service.util.IOrderStatusToStringConverter;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.grid.contextmenu.GridMenuItem;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Collection;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import jakarta.annotation.security.RolesAllowed;

@Route(value = "my-requests", layout = MenuRequestCenterPage.class)
@RolesAllowed({"ROLE_COORDINATOR"})
public class CoordinatorRequestsView extends View implements LocaleChangeObserver, IOrderStatusToStringConverter {

    private SearchComponent searchField;
    private ListDataProvider<RequestOrder> dataProvider;
    private Grid<RequestOrder> grid;
    private final ICreateRequestController createRequestController;
    private Button submitButton;
    private Button suspendedRequestButton;
    private Button continueRequestButton;
    private Button createRequestButton;
    private Button repeatRequestButton;
    private Button removeRequestButton;
    private Button editButton;
    private Button fitManagerButton;

    private GridMenuItem<RequestOrder> createItem;
    private GridMenuItem<RequestOrder> editItem;
    private GridMenuItem<RequestOrder> repeatItem;
    private GridMenuItem<RequestOrder> removeItem;
    private GridMenuItem<RequestOrder> submitItem;
    private GridMenuItem<RequestOrder> suspendedItem;
    private GridMenuItem<RequestOrder> continueItem;
    private GridMenuItem<RequestOrder> fitItem;

    public CoordinatorRequestsView(@Autowired ICreateRequestController createRequestController) {
        this.createRequestController = createRequestController;
        initMainLayout();
    }

    private void initMainLayout() {
        super.getStyle().set("padding", "0px 5px 0px 5px");
        add(initSearchFieldLayout(), initToolBarLayout(), initGrid());
        updateStatusButton();
    }

    private HorizontalLayout initToolBarLayout() {
        suspendedRequestButton = new Button(getTranslation("button.request_suspended"));
        createRequestButton = new Button(getTranslation("button.request_create"));
        createRequestButton.addClickListener(event -> clickCreateButton());
        repeatRequestButton = new Button(getTranslation("button.request_repeat"));
        repeatRequestButton.setTooltipText(getTranslation("message.button_tooltip.repeat"));
        repeatRequestButton.addClickListener(event -> clickRepeatButton());
        removeRequestButton = new Button(getTranslation("button.request_remove"));
        removeRequestButton.setTooltipText(getTranslation("message.button_tooltip.remove"));
        removeRequestButton.addClickListener(event ->
            grid.getSelectionModel().getFirstSelectedItem().ifPresent(this::clickRemoveButton));
        suspendedRequestButton.addClickListener(event ->
            grid.getSelectionModel().getFirstSelectedItem().ifPresent(this::clickSuspendOrderButton));
        continueRequestButton = new Button("button.request_continue");
        continueRequestButton.addClickListener(event ->
            grid.getSelectionModel().getFirstSelectedItem().ifPresent(this::clickContinueButton));
        submitButton = new Button(getTranslation("button.submit"));
        submitButton.addClickListener(event ->
            grid.getSelectionModel().getFirstSelectedItem().ifPresent(this::clickSubmitButton));
        submitButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_SMALL);
        editButton = new Button(getTranslation("button.request_edit"));
        editButton.setTooltipText(getTranslation("message.button_tooltip.edit"));
        editButton.addClickListener(event ->
            grid.getSelectionModel().getFirstSelectedItem().ifPresent(this::clickEditButton));
        fitManagerButton = VaadinUtils.createLumoButton(VaadinIcon.TOOLS);
        fitManagerButton.addClickListener(event -> new FitManageWindow(createRequestController).open());
        var layout = new HorizontalLayout(createRequestButton, editButton, repeatRequestButton, submitButton,
            suspendedRequestButton, continueRequestButton, removeRequestButton, fitManagerButton);
        continueRequestButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_SMALL);
        suspendedRequestButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_SMALL);
        createRequestButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_SMALL);
        repeatRequestButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_SMALL);
        removeRequestButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_SMALL);
        fitManagerButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_SMALL);
        editButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_SMALL);
        layout.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        layout.setWidthFull();
        return layout;
    }

    private HorizontalLayout initSearchFieldLayout() {
        searchField = new SearchComponent(getTranslation("order.search.placeholder"),
            event -> searchByText(searchField.getValue()),
            event -> clearSearch()
        );
        var horizontalLayout = new HorizontalLayout(searchField);
        horizontalLayout.setWidthFull();
        horizontalLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        return horizontalLayout;
    }

    private Grid<RequestOrder> initGrid() {
        dataProvider = DataProvider.ofCollection(createRequestController.getRequestOrders());
        grid = VaadinUtils.initGrid(dataProvider, "distributed-orders-grid");
        addColumns();
        grid.getColumns().forEach(shipOrderDtoColumn -> {
            shipOrderDtoColumn.setSortable(true);
            shipOrderDtoColumn.setResizable(true);
        });
        grid.addSelectionListener(event -> {
            updateStatusButton();
        });
        createContextMenu(grid);
        return grid;
    }

    private void createContextMenu(Grid<RequestOrder> grid) {
        var gridContextMenu = new GridContextMenu<>(grid);
        submitItem = gridContextMenu.addItem(getTranslation("button.submit"), e ->
            grid.getSelectionModel().getFirstSelectedItem().ifPresent(this::clickSubmitButton));
        suspendedItem = gridContextMenu.addItem(getTranslation("button.request_suspended"),
            e -> e.getItem().ifPresent(this::clickSuspendOrderButton));
        continueItem = gridContextMenu.addItem(getTranslation("button.request_continue"),
            e -> e.getItem().ifPresent(this::clickContinueButton));
        gridContextMenu.add(new Hr());
        createItem = gridContextMenu.addItem(getTranslation("button.request_create"), e -> clickCreateButton());
        fitItem = gridContextMenu.addItem(getTranslation("button.request_fit"),
            e -> e.getItem().ifPresent(this::clickFitButton));
        gridContextMenu.add(new Hr());
        editItem = gridContextMenu.addItem(getTranslation("button.request_edit"),
            e -> e.getItem().ifPresent(this::clickEditButton));
        repeatItem = gridContextMenu.addItem(getTranslation("button.request_repeat"), e -> clickRepeatButton());
        removeItem = gridContextMenu.addItem(getTranslation("button.request_remove"),
            e -> e.getItem().ifPresent(this::clickRemoveButton));
        gridContextMenu.addGridContextMenuOpenedListener(event -> {
            gridContextMenu.setVisible(true);
            event.getItem().ifPresent(grid::select);
            updateMenuItems(event.getItem());
        });
    }

    private void clickCreateButton() {
        var createRequestView = new CreateRequestView(createRequestController);
        createRequestView.open();
    }


    private void clearSearch() {
        searchField.clearText();
        searchByText("");
    }

    private void clickRemoveButton(RequestOrder order) {
        if (order.getRequestStatus() == RequestOrderStatusEnum.SUBMITTED ||
            order.getRequestStatus() == RequestOrderStatusEnum.CREATED) {
            createRequestController.removeRequest(order.getId());
            var message = String.format(getTranslation("message.notification.request_removed"),
                order.getItemName());
            Notification.show(message);
            UI.getCurrent().refreshCurrentRoute(true);
        } else {
            Notification.show(String.format(getTranslation("message.notification.request_can_not_removed"),
                order.getItemName()), 3000, Notification.Position.MIDDLE);
        }
    }

    private void clickRepeatButton() {
        Optional<RequestOrder> firstSelectedItem = grid.getSelectionModel().getFirstSelectedItem();
        if (firstSelectedItem.isPresent()) {
            RequestOrder order = firstSelectedItem.get();
            order.setId(null);
            order.setRequestStatus(RequestOrderStatusEnum.CREATED);
            moveOrderToParameters(order);
            var createRequestView = new CreateRequestView(createRequestController);
            createRequestView.open();
        }
    }

    private void clickSuspendOrderButton(RequestOrder order) {
        order.setRequestStatus(RequestOrderStatusEnum.SUSPENDED);
        createRequestController.updateRequest(order);
        UI.getCurrent().refreshCurrentRoute(true);
    }

    private void clickEditButton(RequestOrder order) {
        moveOrderToParameters(order);
        var createRequestView = new CreateRequestView(createRequestController);
        createRequestView.open();
    }

    private void clickContinueButton(RequestOrder order) {
        order.setRequestStatus(
            Objects.nonNull(order.getPrice()) && !order.getPrice().equals(BigDecimal.ZERO) ?
                RequestOrderStatusEnum.SUBMITTED : RequestOrderStatusEnum.CREATED);
        createRequestController.updateRequest(order);
        UI.getCurrent().refreshCurrentRoute(true);
    }

    private void clickSubmitButton(RequestOrder order) {
        order.setRequestStatus(RequestOrderStatusEnum.APPROVED);
        createRequestController.updateRequest(order);
        UI.getCurrent().refreshCurrentRoute(true);
    }

    private void clickFitButton(RequestOrder order) {
        String fitId = order.getFitId();
        if (fitId != null && !fitId.isEmpty()) {
            Fit fit = createRequestController.getFitById(fitId);
            new FitView(fit, createRequestController.getFitLoaderService()).open();
        }
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
            dataProvider = DataProvider.ofCollection(createRequestController.getRequestOrders());
            grid.setDataProvider(dataProvider);
            dataProvider.refreshAll();
        }
    }

    private void addColumns() {
        addColumn(value -> convertRequestStatus(value.getRequestStatus()))
            .setWidth("135px");
        addColumn(RequestOrder::getItemName).setWidth("145px");
        addColumn(RequestOrder::getPriority).setWidth("115px");
        addNumberColumn(RequestOrder::getCount).setWidth("90px");
        addAmountColumn(order -> formatIsk(order.getPrice())).setWidth("175px");
        addColumn(RequestOrder::getCreatedBy).setWidth("141px");
        addColumn(order -> order.getCreatedDate().toString()).setWidth("190px");
        addColumn(order -> order.getFinishDate().toString());
        addColumn(RequestOrder::getId).setWidth("320px");
    }

    private Grid.Column<RequestOrder> addAmountColumn(ValueProvider<RequestOrder, ?> provider) {
        Grid.Column<RequestOrder> column = grid.addColumn(provider);
        column.setTextAlign(ColumnTextAlign.END);
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
        grid.getColumns().get(0).setHeader(getTranslation("table.column.status"));
        grid.getColumns().get(1).setHeader(getTranslation("table.column.nomination"));
        grid.getColumns().get(2).setHeader(getTranslation("table.column.priority"));
        grid.getColumns().get(3).setHeader(getTranslation("table.column.count"));
        grid.getColumns().get(4).setHeader(getTranslation("table.column.price"));
        grid.getColumns().get(5).setHeader(getTranslation("table.column.created_by"));
        grid.getColumns().get(6).setHeader(getTranslation("table.column.create_date_request"));
        grid.getColumns().get(7).setHeader(getTranslation("table.column.deadline"));
        grid.getColumns().get(8).setHeader(getTranslation("table.column.request_id"));
        searchField.setPlaceholder(getTranslation("request.search.placeholder"));
        submitButton.setText(getTranslation("button.submit"));
        suspendedRequestButton.setText(getTranslation("button.request_suspended"));
        continueRequestButton.setText(getTranslation("button.request_continue"));
        createItem.setText(getTranslation("button.request_create"));
        editItem.setText(getTranslation("button.request_edit"));
        repeatItem.setText(getTranslation("button.request_repeat"));
        removeItem.setText(getTranslation("button.request_remove"));
        submitItem.setText(getTranslation("button.submit"));
        suspendedItem.setText(getTranslation("button.request_suspended"));
        continueItem.setText(getTranslation("button.request_continue"));
        fitItem.setText(getTranslation("button.request_fit"));
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
            repeatRequestButton.setEnabled(selected);
            removeRequestButton.setEnabled(selected && requestStatus == RequestOrderStatusEnum.CREATED);
            editButton.setEnabled(selected && requestStatus == RequestOrderStatusEnum.CREATED);
        } else {
            submitButton.setEnabled(false);
            suspendedRequestButton.setEnabled(false);
            continueRequestButton.setEnabled(false);
            repeatRequestButton.setEnabled(false);
            removeRequestButton.setEnabled(false);
            editButton.setEnabled(false);
        }
    }

    private void updateMenuItems(Optional<RequestOrder> orderOptional) {
        Set<RequestOrder> selected = grid.getSelectedItems();
        if (orderOptional.isPresent() && selected.contains(orderOptional.get())) {
            var request = orderOptional.get();
            var requestStatus = request.getRequestStatus();
            createItem.setEnabled(true);
            repeatItem.setEnabled(true);
            removeItem.setEnabled(requestStatus == RequestOrderStatusEnum.CREATED);
            suspendedItem.setEnabled(requestStatus == RequestOrderStatusEnum.SUBMITTED ||
                requestStatus == RequestOrderStatusEnum.CREATED);
            editItem.setEnabled(requestStatus == RequestOrderStatusEnum.CREATED);
            submitItem.setEnabled(requestStatus == RequestOrderStatusEnum.SUBMITTED);
            continueItem.setEnabled(requestStatus == RequestOrderStatusEnum.SUSPENDED);
            fitItem.setEnabled(Objects.nonNull(request.getFitId()));
        } else {
            createItem.setVisible(true);
            repeatItem.setEnabled(false);
            removeItem.setEnabled(false);
            suspendedItem.setEnabled(false);
            editItem.setEnabled(false);
            submitItem.setEnabled(false);
            continueItem.setEnabled(false);
            fitItem.setEnabled(false);
        }
    }

    private RequestOrder getRequest() {
        return grid.getSelectedItems()
            .stream()
            .findFirst()
            .orElse(null);
    }

    private void moveOrderToParameters(RequestOrder order) {
        VaadinSession.getCurrent().setAttribute("requestOrder", order);
        UI.getCurrent().refreshCurrentRoute(true);
    }

    private String formatIsk(BigDecimal value) {
        if (Objects.isNull(value)) {
            return "";
        }
        DecimalFormat df = new DecimalFormat("#,##0.00");
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("ru", "RU"));
        symbols.setGroupingSeparator(' ');
        df.setDecimalFormatSymbols(symbols);
        return df.format(value) + " ISK";
    }
}
