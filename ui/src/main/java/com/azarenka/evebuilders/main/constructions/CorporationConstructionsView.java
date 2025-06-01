package com.azarenka.evebuilders.main.constructions;

import com.azarenka.evebuilders.common.util.VaadinUtils;
import com.azarenka.evebuilders.component.SearchComponent;
import com.azarenka.evebuilders.component.View;
import com.azarenka.evebuilders.domain.OrderStatusEnum;
import com.azarenka.evebuilders.domain.db.DistributedOrder;
import com.azarenka.evebuilders.domain.db.Fit;
import com.azarenka.evebuilders.main.commonview.FitView;
import com.azarenka.evebuilders.main.constructions.api.ICorporationConstructionController;
import com.azarenka.evebuilders.main.menu.MenuConstructionPageView;
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
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Route(value = "corporation", layout = MenuConstructionPageView.class)
@PageTitle("Constructions")
@RolesAllowed({"ROLE_ADMIN", "ROLE_SUPER_ADMIN", "ROLE_USER"})
public class CorporationConstructionsView extends View implements LocaleChangeObserver {

    private ListDataProvider<DistributedOrder> dataProvider;
    private Grid<DistributedOrder> grid;
    private ICorporationConstructionController controller;
    private SearchComponent searchField;
    private Button updateStatusOrderButton;
    private Button buildButton;
    private Button banButton;
    private Button fitButton;
    private Button showFullOrder;
    private boolean isStatusFilterOn = false;

    public CorporationConstructionsView(ICorporationConstructionController controller) {
        this.controller = controller;
        initMainLayout();
        updateButtonsStatus();
    }

    private void initMainLayout() {
        add(initToolBarLayout(), initFilterLayout(), initGrid());
    }

    private HorizontalLayout initFilterLayout() {
        banButton = new Button(VaadinIcon.FILTER.create(), event -> excludeCompletedStatus());
        fitButton = new Button(VaadinIcon.FILE_START.create(), event -> {
            Fit fitById = controller.getFitById(grid.getSelectedItems().stream().findFirst().get().getFitId());
            if (Objects.nonNull(fitById)) {
                new FitView(fitById).open();
            } else {
                Notification notification = new Notification();
                notification.setText("Фит для этого заказа не загружен");
                notification.setDuration(1000);
                notification.open();
            }
        });
        showFullOrder = new Button(VaadinIcon.PRESENTATION.create());
        banButton.setTooltipText(getTranslation("message.button.tooltip.completed_filter"));
        fitButton.setTooltipText(getTranslation("message.button.tooltip.show_fit"));
        showFullOrder.setTooltipText(getTranslation("message.button.tooltip.show_full_order"));
        fitButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ICON);
        banButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ICON);
        showFullOrder.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ICON);
        return new HorizontalLayout(banButton, fitButton, showFullOrder);
    }

    private HorizontalLayout initToolBarLayout() {
        updateStatusOrderButton = new Button(getTranslation("button.finish_order"), VaadinIcon.CASH.create(),
                event -> {
                    List<DistributedOrder> selectedItems = grid.getSelectionModel().getSelectedItems().stream().toList();
                    if (selectedItems.size() == 1) {
                        FinishOrderWindow finishOrderWindow = new FinishOrderWindow(selectedItems.get(0), controller);
                        finishOrderWindow.addDialogCloseActionListener(actionEvent -> {
                            grid.getDataProvider().refreshItem(selectedItems.get(0));
                        });
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

    private void openBuildTab() {
        DistributedOrder distributedOrder = grid.getSelectionModel().getFirstSelectedItem().get();
        Fit fitById = controller.getFitById(distributedOrder.getFitId());
        VaadinSession.getCurrent().setAttribute("currentOrder", distributedOrder);
        VaadinSession.getCurrent().setAttribute("currentFit", fitById);
        UI.getCurrent().navigate(BuilderConstructionView.class);
    }

    private Grid<DistributedOrder> initGrid() {
        dataProvider = DataProvider.ofCollection(controller.getOrderList());
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
            dataProvider = DataProvider.ofCollection(controller.getOrderList());
            grid.setDataProvider(dataProvider);
            dataProvider.refreshAll();
        }
    }

    private void addColumns() {
        addColumn(DistributedOrder::getOrderNumber);
        addColumn(value -> value.getOrderStatus().name());
        addColumn(DistributedOrder::getShipName);
        addNumberColumn(DistributedOrder::getCount);
        addNumberColumn(DistributedOrder::getCountReady);
        addColumn(order -> order.getFinishedDate().toString());
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

    private void updateButtonsStatus() {
        List<DistributedOrder> selectedItems = grid.getSelectionModel().getSelectedItems().stream().toList();
        boolean isUpdateButtonEnabled = !selectedItems.isEmpty()
                && !selectedItems.get(0).getCount().equals(selectedItems.get(0).getCountReady());
        updateStatusOrderButton.setEnabled(isUpdateButtonEnabled);
        buildButton.setEnabled(isUpdateButtonEnabled);
        fitButton.setEnabled(!selectedItems.isEmpty());
        showFullOrder.setEnabled(!selectedItems.isEmpty());
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
    }

    private void excludeCompletedStatus() {
        if (isStatusFilterOn) {
            banButton.getStyle().set("color", "blue");
            dataProvider = DataProvider.ofCollection(controller.getOrderList());
            grid.setDataProvider(dataProvider);
            dataProvider.refreshAll();
            isStatusFilterOn = false;
        } else {
            banButton.getStyle().set("color", "red");
            var orderList = controller.getOrderList();
            dataProvider = DataProvider.ofCollection(orderList.stream()
                    .filter(order -> !(order.getOrderStatus() == OrderStatusEnum.COMPLETED))
                    .toList()
            );
            grid.setDataProvider(dataProvider);
            dataProvider.refreshAll();
            isStatusFilterOn = true;
        }
    }

    private void clearSearch() {
        searchField.clearText();
        searchByText("");
    }
}
