package com.azarenka.evebuilders.main.constructions;

import com.azarenka.evebuilders.common.util.VaadinUtils;
import com.azarenka.evebuilders.component.OrderFilterPopupComponent;
import com.azarenka.evebuilders.component.SearchComponent;
import com.azarenka.evebuilders.component.View;
import com.azarenka.evebuilders.domain.OrderStatusEnum;
import com.azarenka.evebuilders.domain.db.DistributedOrder;
import com.azarenka.evebuilders.domain.db.Fit;
import com.azarenka.evebuilders.domain.db.OrderFilter;
import com.azarenka.evebuilders.main.commonview.FitView;
import com.azarenka.evebuilders.main.commonview.NotificationWindow;
import com.azarenka.evebuilders.main.constructions.api.ICorporationConstructionController;
import com.azarenka.evebuilders.main.menu.MenuConstructionPage;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSelectionModel;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Route(value = "corporation", layout = MenuConstructionPage.class)
@PageTitle("Constructions")
@RolesAllowed({"ROLE_ADMIN", "ROLE_SUPER_ADMIN", "ROLE_USER"})
public class CorporationConstructionsView extends View implements LocaleChangeObserver {

    private ListDataProvider<DistributedOrder> dataProvider;
    private Grid<DistributedOrder> grid;
    private ICorporationConstructionController controller;
    private SearchComponent searchField;
    private Button updateStatusOrderButton;
    private Button buildButton;
    private OrderFilterPopupComponent orderFilterPopupComponent;
    private Button fitButton;
    private Button showFullOrder;
    private Button discardOrderButton;
    private Button filterButton;
    private OrderFilter appliedFilter = new OrderFilter();

    public CorporationConstructionsView(ICorporationConstructionController controller) {
        this.controller = controller;
        initMainLayout();
        updateButtonsStatus();
    }

    private void initMainLayout() {
        super.getStyle().set("padding", "0px 5px 0px 5px");
        add(initToolBarLayout(), initFilterLayout(), initGrid());
    }

    private HorizontalLayout initFilterLayout() {
        orderFilterPopupComponent = new OrderFilterPopupComponent().builder(event -> applyFilter())
                .withStatusFilter()
                .withTypeOrderFilter()
                .build();
        filterButton = orderFilterPopupComponent.getOpenFilterButton();
        fitButton = new Button(VaadinIcon.FILE_START.create(), event -> {
            Optional<DistributedOrder> first = grid.getSelectedItems().stream().findFirst();
            first.ifPresent(order -> {
                if (Objects.nonNull(order.getFitId())) {
                    Fit fit = controller.getFitById(order.getFitId());
                    if (Objects.nonNull(fit)) {
                        new FitView(fit, controller.getFitLoaderService()).open();
                    } else {
                        Notification notification = new Notification("Фит для этого заказа не загружен",
                                3000, Notification.Position.MIDDLE);
                        notification.open();
                    }
                }
            });
        });
        showFullOrder = new Button(VaadinIcon.PRESENTATION.create());
        showFullOrder.addClickListener(event ->
                new DistributedOrderDetailsWindow(grid.getSelectionModel().getSelectedItems().stream().findFirst().get()).open());
        filterButton.setTooltipText(getTranslation("message.button.tooltip.filter_window"));
        fitButton.setTooltipText(getTranslation("message.button.tooltip.show_fit"));
        showFullOrder.setTooltipText(getTranslation("message.button.tooltip.show_full_order"));
        discardOrderButton = new Button(VaadinIcon.TRASH.create(), event -> onDiscardOrderClicked());
        fitButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ICON);
        filterButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ICON);
        showFullOrder.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ICON);
        discardOrderButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ICON);
        return new HorizontalLayout(filterButton, fitButton, showFullOrder, discardOrderButton);
    }

    private HorizontalLayout initToolBarLayout() {
        updateStatusOrderButton = new Button(getTranslation("button.finish_order"), VaadinIcon.CASH.create(),
                event -> {
                    List<DistributedOrder> selectedItems = grid.getSelectionModel().getSelectedItems().stream().toList();
                    if (selectedItems.size() == 1) {
                        FinishOrderWindow finishOrderWindow = new FinishOrderWindow(selectedItems.get(0), controller,
                                save -> UI.getCurrent().refreshCurrentRoute(true));
                        finishOrderWindow.open();
                    }
                }
        );
        updateStatusOrderButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ICON);
        buildButton = new Button(getTranslation("button.build_order"), VaadinIcon.CALC_BOOK.create(), event -> openBuildTab());
        buildButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ICON);
        searchField = new SearchComponent(getTranslation("order.search.placeholder"),
                event -> searchByText(searchField.getValue()),
                event -> clearSearch()
        );
        HorizontalLayout layout = new HorizontalLayout(updateStatusOrderButton, buildButton, searchField);
        layout.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        layout.setWidthFull();
        return layout;
    }

    private void onDiscardOrderClicked() {
        Optional<DistributedOrder> firstSelectedItem = grid.getSelectionModel().getFirstSelectedItem();
        firstSelectedItem.ifPresent(order -> {
            if (order.getOrderStatus() != OrderStatusEnum.COMPLETED) {
                LocalDate createdOrderDate = order.getCreatedDate();
                LocalDate deadLineDate = order.getFinishedDate();
                LocalDate now = LocalDate.now();

                long totalDays = ChronoUnit.DAYS.between(createdOrderDate, deadLineDate);
                long halfDays = totalDays / 2;

                long daysLeft = ChronoUnit.DAYS.between(now, deadLineDate);
                if (daysLeft < 0) {
                    new NotificationWindow("Warning", "До даты завершения осталось " + daysLeft + ".\n " +
                            "Вы не можете отменить заказ так как заказ уже просрочен")
                            .open();
                } else {
                    if (daysLeft < halfDays) {
                        new NotificationWindow("Warning", "До даты завершения осталось " + daysLeft + ".\n " +
                                "Вы не можете отменить заказ так как прошло более половины времени с момента создания заказа")
                                .open();
                    } else {
                        controller.discardOrder(order);
                        UI.getCurrent().refreshCurrentRoute(true);
                    }
                }
            } else {
                new NotificationWindow("Error", "Вы не можете отменить заказ так как заказ уже завершен").open();
            }
        });
    }

    private void openBuildTab() {
        DistributedOrder distributedOrder = grid.getSelectionModel().getFirstSelectedItem().get();
        Fit fit = controller.getFitById(distributedOrder.getFitId());
        VaadinSession.getCurrent().setAttribute("currentOrder", distributedOrder);
        VaadinSession.getCurrent().setAttribute("currentFit", fit);
        UI.getCurrent().navigate(BuilderConstructionView.class);
    }

    private Grid<DistributedOrder> initGrid() {
        dataProvider = DataProvider.ofCollection(controller.getOrderList(appliedFilter));
        grid = VaadinUtils.initGrid(dataProvider, "distributed-orders-grid");
        addColumns();
        grid.getColumns().forEach(shipOrderDtoColumn -> {
            shipOrderDtoColumn.setSortable(true);
            shipOrderDtoColumn.setResizable(true);
        });
        GridSelectionModel<DistributedOrder> selectionModel = grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        selectionModel.addSelectionListener(selectionEvent -> updateButtonsStatus());
        return grid;
    }

    private void searchByText(String value) {
        if (!value.isEmpty()) {
            Collection<DistributedOrder> items = dataProvider.getItems();
            String lowerCaseValue = value.trim().toLowerCase();
            List<DistributedOrder> list = items.stream()
                    .filter(item -> (
                            item.getOrderNumber() != null && item.getOrderNumber().toLowerCase().contains(lowerCaseValue)) ||
                            (item.getShipName() != null && item.getShipName().toLowerCase().contains(lowerCaseValue)) ||
                            (item.getOrderStatus() != null && item.getOrderStatus().name().toLowerCase().contains(lowerCaseValue)))
                    .toList();
            dataProvider = DataProvider.ofCollection(list);
            grid.setDataProvider(dataProvider);
            dataProvider.refreshAll();
        } else {
            dataProvider = DataProvider.ofCollection(controller.getOrderList(appliedFilter));
            grid.setDataProvider(dataProvider);
            dataProvider.refreshAll();
        }
    }

    private void addColumns() {
        addColumn(DistributedOrder::getOrderNumber, "130px");
        addColumn(value -> value.getOrderStatus().name(), "130px");
        addColumn(DistributedOrder::getShipName, "150px");
        addNumberColumn(DistributedOrder::getCount, "130px");
        addNumberColumn(DistributedOrder::getCountReady, "130px");
        addColumn(order -> order.getFinishedDate().toString(), "170px");
    }

    private Grid.Column<DistributedOrder> addComponentColumn(ValueProvider<DistributedOrder, Button> provider,
                                                             String width) {
        Grid.Column<DistributedOrder> column = grid.addComponentColumn(provider);
        column.setWidth(width);
        return column;
    }

    private Grid.Column<DistributedOrder> addAmountColumn(ValueProvider<DistributedOrder, BigDecimal> provider,
                                                          String width) {
        Grid.Column<DistributedOrder> column = grid.addColumn(provider);
        column.setWidth(width);
        return column;
    }

    private Grid.Column<DistributedOrder> addNumberColumn(ValueProvider<DistributedOrder, Integer> provider,
                                                          String width) {
        Grid.Column<DistributedOrder> column = grid.addColumn(provider);
        column.setWidth(width);
        column.setTextAlign(ColumnTextAlign.END);
        return column;
    }

    private Grid.Column<DistributedOrder> addColumn(ValueProvider<DistributedOrder, String> provider,
                                                    String width) {
        Grid.Column<DistributedOrder> column = grid.addColumn(provider);
        column.setWidth(width);
        return column;
    }

    private void updateButtonsStatus() {
        List<DistributedOrder> selectedItems = grid.getSelectionModel().getSelectedItems().stream().toList();
        boolean isUpdateButtonEnabled = !selectedItems.isEmpty()
                && !selectedItems.get(0).getCount().equals(selectedItems.get(0).getCountReady());
        updateStatusOrderButton.setEnabled(isUpdateButtonEnabled);
        buildButton.setEnabled(isUpdateButtonEnabled);
        fitButton.setEnabled(!selectedItems.isEmpty());
        showFullOrder.setEnabled(!selectedItems.isEmpty());
        discardOrderButton.setEnabled(!selectedItems.isEmpty());
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        grid.getColumns().get(0).setHeader(getTranslation("table.column.order_number"));
        grid.getColumns().get(1).setHeader(getTranslation("table.column.status"));
        grid.getColumns().get(2).setHeader(getTranslation("table.column.nomination"));
        grid.getColumns().get(3).setHeader(getTranslation("table.column.count"));
        grid.getColumns().get(4).setHeader(getTranslation("table.column.ready_count"));
        grid.getColumns().get(5).setHeader(getTranslation("table.column.deadline"));
        updateStatusOrderButton.setText(getTranslation("button.finish_order"));
        searchField.setPlaceholder(getTranslation("order.search.placeholder"));
        buildButton.setText(getTranslation("button.build_order"));
        fitButton.setTooltipText(getTranslation("message.button.tooltip.show_fit"));
        showFullOrder.setTooltipText(getTranslation("message.button.tooltip.show_full_order"));
        filterButton.setTooltipText(getTranslation("message.button.tooltip.filter_window"));
    }

    private void applyFilter() {
        appliedFilter = new OrderFilter(orderFilterPopupComponent.getAppliedFilter());
        dataProvider = DataProvider.ofCollection(controller.getOrderList(appliedFilter));
        grid.setDataProvider(dataProvider);
        dataProvider.refreshAll();
    }

    private void clearSearch() {
        searchField.clearText();
        searchByText("");
    }
}
