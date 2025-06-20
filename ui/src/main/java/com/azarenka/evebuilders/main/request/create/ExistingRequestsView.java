package com.azarenka.evebuilders.main.request.create;

import com.azarenka.evebuilders.common.util.VaadinUtils;
import com.azarenka.evebuilders.component.View;
import com.azarenka.evebuilders.domain.db.RequestOrder;
import com.azarenka.evebuilders.domain.db.RequestOrderStatusEnum;
import com.azarenka.evebuilders.main.request.api.ICreateRequestController;
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
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Optional;

public class ExistingRequestsView extends View implements LocaleChangeObserver {

    private final ICreateRequestController controller;
    private Grid<RequestOrder> grid;
    private ListDataProvider<RequestOrder> dataProvider;
    private HorizontalLayout toolbarLayout = new HorizontalLayout();
    private Button recycleButton;
    private Button repeatOrderButton;
    private Button editButton;

    public ExistingRequestsView(ICreateRequestController controller) {
        this.controller = controller;
        super.getStyle().set("padding-left", "10px");
        initContent();
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        grid.getColumns().get(0).setHeader(getTranslation("table.column.nomination"));
        grid.getColumns().get(1).setHeader(getTranslation("table.column.status"));
        grid.getColumns().get(2).setHeader(getTranslation("table.column.count"));
        grid.getColumns().get(3).setHeader(getTranslation("table.column.price"));
    }

    private void initContent() {
        initGrid();
        initToolbarLayout();
        add(toolbarLayout, grid);
        updateStatusButtons();
    }

    private void initGrid() {
        dataProvider = DataProvider.ofCollection(controller.getRequestOrders());
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
                if (order.getRequestStatus() == RequestOrderStatusEnum.SUBMITTED) {
                    controller.removeRequest(order.getId());
                    String message = String.format("Заявка на %s была удалена", order.getItemName());
                    Notification.show(message);
                    UI.getCurrent().refreshCurrentRoute(true);
                } else {
                    Notification.show(String.format("Заявка на %s не может быть удалена так как заказ уже находится в работе",
                            order.getItemName()), 3000, Notification.Position.MIDDLE);
                }
            });
        });
        repeatOrderButton = new Button(VaadinIcon.REPLY_ALL.create());
        repeatOrderButton.setTooltipText("Повторить заказ");
        repeatOrderButton.addClickListener(event -> {
            Optional<RequestOrder> firstSelectedItem = grid.getSelectionModel().getFirstSelectedItem();
            if (firstSelectedItem.isPresent()) {
                RequestOrder order = firstSelectedItem.get();
                order.setId(StringUtils.EMPTY);
                order.setRequestStatus(RequestOrderStatusEnum.SUBMITTED);
                moveOrderToParameters(order);
            }
        });
        editButton = new Button(VaadinIcon.EDIT.create());
        editButton.setTooltipText("Редактировать заказ");
        editButton.addClickListener(event -> {
            Optional<RequestOrder> firstSelectedItem = grid.getSelectionModel().getFirstSelectedItem();
            firstSelectedItem.ifPresent(this::moveOrderToParameters);
        });
        repeatOrderButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        recycleButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        editButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        toolbarLayout.add(recycleButton, repeatOrderButton, editButton);
    }

    private void moveOrderToParameters(RequestOrder order) {
        VaadinSession.getCurrent().setAttribute("requestOrder", order);
        UI.getCurrent().navigate(CreateRequestView.class);
        UI.getCurrent().refreshCurrentRoute(true);
    }

    private void addColumns() {
        addColumn(RequestOrder::getItemName);
        addColumn(value -> value.getRequestStatus().name());
        addNumberColumn(RequestOrder::getCount);
        addAmountColumn(order -> formatIsk(order.getPrice()));
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

    private String formatIsk(BigDecimal value) {
        if (value == null) return "";
        DecimalFormat df = new DecimalFormat("#,##0.00");
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("ru", "RU"));
        symbols.setGroupingSeparator(' ');
        df.setDecimalFormatSymbols(symbols);
        return df.format(value) + " ISK";
    }

    private void updateStatusButtons() {
        Optional<RequestOrder> firstSelectedItem = grid.getSelectionModel().getFirstSelectedItem();
        boolean isOrderSelected = firstSelectedItem.isPresent();
        boolean isEditButtonEnabled =
                isOrderSelected && firstSelectedItem.get().getRequestStatus() == RequestOrderStatusEnum.SUBMITTED;
        recycleButton.setEnabled(isOrderSelected);
        repeatOrderButton.setEnabled(isOrderSelected);
        editButton.setEnabled(isEditButtonEnabled);
    }
}
