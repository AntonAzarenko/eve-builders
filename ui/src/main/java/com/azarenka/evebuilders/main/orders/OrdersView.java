package com.azarenka.evebuilders.main.orders;

import com.azarenka.evebuilders.common.util.VaadinUtils;
import com.azarenka.evebuilders.component.OrderFilterPopupComponent;
import com.azarenka.evebuilders.component.SearchComponent;
import com.azarenka.evebuilders.component.View;
import com.azarenka.evebuilders.domain.OrderStatusEnum;
import com.azarenka.evebuilders.domain.db.Order;
import com.azarenka.evebuilders.domain.db.OrderFilter;
import com.azarenka.evebuilders.domain.db.Role;
import com.azarenka.evebuilders.domain.dto.ShipOrderDto;
import com.azarenka.evebuilders.main.commonview.NotificationWindow;
import com.azarenka.evebuilders.main.managment.create.CreateOrderView;
import com.azarenka.evebuilders.main.orders.api.IOrderViewController;
import com.azarenka.evebuilders.service.impl.auth.SecurityUtils;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSelectionModel;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.splitlayout.SplitLayoutVariant;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.server.VaadinSession;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;

public class OrdersView extends View implements LocaleChangeObserver {

    private final IOrderViewController controller;
    private ListDataProvider<ShipOrderDto> dataProvider;
    private Grid<ShipOrderDto> grid;
    private VerticalLayout metadataLayout;
    private Button takeAnOrderButton;
    private Button filterButton;
    private Button editOrderButton;
    private Button orderInfoButton;
    private SearchComponent searchField;
    private OrderFilterPopupComponent orderFilterPopupComponent;
    private OrderFilter appliedFilter = new OrderFilter();

    public OrdersView(IOrderViewController controller) {
        this.controller = controller;
        add(initMainLayout());
        getUI().ifPresent(ui -> ui.add(orderFilterPopupComponent));
    }

    private VerticalLayout initMainLayout() {
        VerticalLayout mainLayout = VaadinUtils.initCommonVerticalLayout();
        mainLayout.add(initToolBarLayout(), initSplitLayout());
        return mainLayout;
    }

    private HorizontalLayout initToolBarLayout() {
        takeAnOrderButton = new Button(getTranslation("button.take_an_order"), VaadinIcon.CART.create(),
                event -> {
                    Optional<ShipOrderDto> firstSelectedItem = grid.getSelectionModel().getFirstSelectedItem();
                    if (firstSelectedItem.isPresent()) {
                        ShipOrderDto shipOrderDto = firstSelectedItem.get();
                        TakeOrderWindow takeOrderWindow = new TakeOrderWindow(shipOrderDto, controller,
                                closeActionEvent -> {
                                    grid.getDataProvider().refreshAll();
                                    updateMetaDataPanel(shipOrderDto);
                                    UI.getCurrent().refreshCurrentRoute(true);
                                });
                        boolean hasFreeShipsInOrder = shipOrderDto.getCount() - shipOrderDto.getInProgressCount() > 0;
                        if (hasFreeShipsInOrder) {
                            takeOrderWindow.open();
                        } else {
                            new NotificationWindow("Warning", getTranslation("errors.message.order_is_distributed"))
                                    .open();
                        }
                    }
                }
        );
        takeAnOrderButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ICON);
        searchField = new SearchComponent(getTranslation("order.search.placeholder"),
                event -> searchByText(searchField.getValue()),
                event -> clearSearch()
        );
        HorizontalLayout layout = new HorizontalLayout(takeAnOrderButton, searchField);
        layout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        layout.setWidthFull();
        layout.getStyle().set("padding", "0px 5px 0px 5px");
        return layout;
    }

    private void searchByText(String value) {
        if (!value.isEmpty()) {
            Collection<ShipOrderDto> items = dataProvider.getItems();
            String lowerCaseValue = value.trim().toLowerCase();
            List<ShipOrderDto> list = items.stream()
                    .filter(item ->
                            (item.getOrderNumber() != null && item.getOrderNumber().toLowerCase().contains(lowerCaseValue)) ||
                                    (item.getItemName() != null && item.getItemName().toLowerCase().contains(lowerCaseValue)) ||
                                    (item.getOrderStatus() != null && item.getOrderStatus().name().toLowerCase().contains(lowerCaseValue))
                    )
                    .toList();
            dataProvider = DataProvider.ofCollection(list);
            grid.setDataProvider(dataProvider);
            dataProvider.refreshAll();
        } else {
            dataProvider = DataProvider.ofCollection(controller.getOrderList(appliedFilter));
            grid.setDataProvider(dataProvider);
            dataProvider.refreshAll();
        }
        selectOrder();
    }

    private void clearSearch() {
        searchField.clearText();
        searchByText("");
    }

    private SplitLayout initSplitLayout() {
        metadataLayout = VaadinUtils.initCommonVerticalLayout();
        SplitLayout splitLayout = new SplitLayout();
        VerticalLayout layout = VaadinUtils.initCommonVerticalLayout();
        layout.add(initFilterLayout(), initGrid());
        layout.getStyle().set("padding", "0px 5px 0px 5px");
        layout.setWidthFull();
        splitLayout.addToPrimary(layout);
        splitLayout.addToSecondary(metadataLayout);
        splitLayout.setSplitterPosition(70);
        splitLayout.addThemeVariants(SplitLayoutVariant.LUMO_SMALL);
        splitLayout.setSizeFull();
        return splitLayout;
    }

    private HorizontalLayout initFilterLayout() {
        orderFilterPopupComponent = new OrderFilterPopupComponent().builder(event -> applyFilter())
                .withStatusFilter()
                .withTypeOrderFilter()
                .withCountFreeFilter()
                .withDistributedFilter()
                .build();
        getUI().ifPresent(ui -> ui.add(orderFilterPopupComponent));
        filterButton = orderFilterPopupComponent.getOpenFilterButton();
        editOrderButton = new Button(VaadinIcon.EDIT.create(), event -> editOrder());
        boolean editPermit = isEditPermitted();
        editOrderButton.setVisible(editPermit);
        editOrderButton.setTooltipText(getTranslation("message.button.tooltip.edit_order"));
        orderInfoButton = new Button(VaadinIcon.INFO.create(), event -> openOrderInfo());
        orderInfoButton.setVisible(editPermit);
        orderInfoButton.setTooltipText(getTranslation("message.button.tooltip.order_info"));
        return new HorizontalLayout(filterButton, editOrderButton, orderInfoButton);
    }

    private void openOrderInfo() {
        String orderNumber = grid.getSelectionModel().getFirstSelectedItem().get().getOrderNumber();
        var orders = controller.getDistributedOrdersByOrderNumber(orderNumber);
        OrderDetailsWindow orderDetailsWindow = new OrderDetailsWindow(orders);
        orderDetailsWindow.open();
    }

    private void editOrder() {
        ShipOrderDto distributedOrder = grid.getSelectionModel().getFirstSelectedItem().get();
        Order order = controller.getOriginalOrderByOrderNumber(distributedOrder.getOrderNumber());
        VaadinSession.getCurrent().setAttribute("originalOrder", order);
        UI.getCurrent().navigate(CreateOrderView.class);
    }

    private void applyFilter() {
        appliedFilter = new OrderFilter(orderFilterPopupComponent.getAppliedFilter());
        dataProvider = DataProvider.ofCollection(controller.getOrderList(appliedFilter));
        grid.setDataProvider(dataProvider);
        dataProvider.refreshAll();
        selectOrder();
    }

    private Grid<ShipOrderDto> initGrid() {
        dataProvider = DataProvider.ofCollection(controller.getOrderList(appliedFilter));
        grid = VaadinUtils.initGrid(dataProvider, "ship-orders-grid");
        addColumns();
        grid.getColumns().forEach(shipOrderDtoColumn -> {
            shipOrderDtoColumn.setSortable(true);
            shipOrderDtoColumn.setResizable(true);
        });
        GridSelectionModel<ShipOrderDto> selectionModel = grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        selectOrder();
        updateButtonStatus();
        selectionModel.addSelectionListener(selectionEvent -> {
            Set<ShipOrderDto> allSelectedItems = selectionEvent.getAllSelectedItems();
            if (allSelectedItems.isEmpty()) {
                metadataLayout.removeAll();
            } else {
                updateMetaDataPanel(allSelectedItems.stream().findFirst().get());
            }
            updateButtonStatus();
        });
        return grid;
    }

    private void selectOrder() {
        Collection<ShipOrderDto> items = dataProvider.getItems();
        if (items.size() > 0) {
            ShipOrderDto shipOrderDto = new ArrayList<>(items).get(0);
            grid.select(shipOrderDto);
            updateMetaDataPanel(shipOrderDto);
        } else {
            metadataLayout.removeAll();
        }
    }

    private void updateMetaDataPanel(ShipOrderDto shipOrderDto) {
        orderInfoButton.setEnabled(isInfoPermitted());
        metadataLayout.removeAll();
        metadataLayout.add(new OrderMetadataView(shipOrderDto, controller));
    }

    private void addColumns() {
        addColumn(ShipOrderDto::getOrderNumber);
        addColumn(value -> value.getOrderStatus().name());
        addColumn(ShipOrderDto::getItemName);
        addNumberColumn(ShipOrderDto::getCount);
        addAmountColumn(order -> formatIsk(order.getPrice()));
    }

    private Grid.Column<ShipOrderDto> addAmountColumn(ValueProvider<ShipOrderDto, ?> provider) {
        Grid.Column<ShipOrderDto> column = grid.addColumn(provider);
        column.setTextAlign(ColumnTextAlign.END);
        return column;
    }

    private Grid.Column<ShipOrderDto> addNumberColumn(ValueProvider<ShipOrderDto, Integer> provider) {
        Grid.Column<ShipOrderDto> column = grid.addColumn(provider);
        column.setTextAlign(ColumnTextAlign.END);
        return column;
    }

    private Grid.Column<ShipOrderDto> addColumn(ValueProvider<ShipOrderDto, String> provider) {
        Grid.Column<ShipOrderDto> column = grid.addColumn(provider);
        return column;
    }

    private void updateButtonStatus() {
        List<ShipOrderDto> selectedItems = grid.getSelectionModel().getSelectedItems().stream().toList();
        boolean isOrderSelected = !selectedItems.isEmpty()
                && !(selectedItems.get(0).getOrderStatus() == OrderStatusEnum.COMPLETED);
        takeAnOrderButton.setEnabled(isOrderSelected);
        editOrderButton.setEnabled(isOrderSelected);
        orderInfoButton.setEnabled(isOrderSelected);
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        grid.getColumns().get(0).setHeader(getTranslation("table.column.order_number"));
        grid.getColumns().get(1).setHeader(getTranslation("table.column.status"));
        grid.getColumns().get(2).setHeader(getTranslation("table.column.nomination"));
        grid.getColumns().get(3).setHeader(getTranslation("table.column.count"));
        grid.getColumns().get(4).setHeader(getTranslation("table.column.price"));
        takeAnOrderButton.setText(getTranslation("button.take_an_order"));
        searchField.setPlaceholder(getTranslation("order.search.placeholder"));
        filterButton.setTooltipText(getTranslation("message.button.tooltip.filter_window"));
        editOrderButton.setTooltipText(getTranslation("message.button.tooltip.edit_order"));
    }

    private String formatIsk(BigDecimal value) {
        if (value == null) return "";
        DecimalFormat df = new DecimalFormat("#,##0.00");
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("ru", "RU"));
        symbols.setGroupingSeparator(' ');
        df.setDecimalFormatSymbols(symbols);
        return df.format(value) + " ISK";
    }

    private boolean isEditPermitted() {
        return Objects.requireNonNull(SecurityUtils.getUserRoles())
                .stream().anyMatch(role -> (role == Role.ROLE_ADMIN) || role == Role.ROLE_SUPER_ADMIN);
    }

    private boolean isInfoPermitted() {
        Optional<ShipOrderDto> firstSelectedItem = grid.getSelectionModel().getFirstSelectedItem();
        return firstSelectedItem.isPresent() && !(firstSelectedItem.get().getOrderStatus() == OrderStatusEnum.NEW);
    }
}
