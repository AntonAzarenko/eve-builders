package com.azarenka.evebuilders.main.trade;

import static org.apache.catalina.manager.JspHelper.formatNumber;

import com.azarenka.evebuilders.common.util.VaadinUtils;
import com.azarenka.evebuilders.component.View;
import com.azarenka.evebuilders.domain.dto.market.MarketFilter;
import com.azarenka.evebuilders.domain.dto.market.RequestRowDTO;
import com.azarenka.evebuilders.main.menu.MenuTradePage;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LocalDateRenderer;
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import org.vaadin.lineawesome.LineAwesomeIcon;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Stream;

import jakarta.annotation.security.RolesAllowed;

@Route(value = "requests", layout = MenuTradePage.class)
@RolesAllowed({"ROLE_SUPER_ADMIN", "ROLE_MINER", "ROLE_ADMIN"})
@PageTitle("Market Requests")
public class MarketRequestView extends View implements LocaleChangeObserver {

    private final Grid<RequestRowDTO> grid = new Grid<>(RequestRowDTO.class, false);
    private final FlexLayout cards = new FlexLayout();

    private final MultiSelectComboBox<String> resource = new MultiSelectComboBox<>("Ресурс");
    private final ComboBox<String> status = new ComboBox<>("Статус");
    private final IntegerField minQty = new IntegerField("Мин. кол-во");
    private final IntegerField maxQty = new IntegerField("Макс. кол-во");
    private final NumberField minPrice = new NumberField("Мин. цена (ISK/ед.)");
    private final NumberField maxPrice = new NumberField("Макс. цена (ISK/ед.)");
    private final DatePicker minDeadline = new DatePicker("Дедлайн с");
    private final DatePicker maxDeadline = new DatePicker("Дедлайн по");

    private final Button filterButton = new Button("Фильтр", new Icon(VaadinIcon.FILTER));
    private final Button reset = new Button("Сброс");
    private final Button tableViewButton = new Button(LineAwesomeIcon.TABLE_SOLID.create());
    private final Button cardViewButton = new Button(LineAwesomeIcon.ADDRESS_CARD_SOLID.create());

    private final MarketFilter current = new MarketFilter();

    public MarketRequestView() {
        getStyle().set("padding", "0 10px 0 10px");
        initContent();

        grid.setItems(new CallbackDataProvider<>(
            this::fetch, this::count
        ));

        filterButton.addClickListener(e -> grid.getDataProvider().refreshAll());
        reset.addClickListener(e -> {
            clearFilters();
            grid.getDataProvider().refreshAll();
        });

        refreshCards();
    }

    private void initContent() {
        add(buildFilters());
        configureGrid();
        cards.setWidthFull();
        cards.getStyle().set("gap", "12px");
        cards.getStyle().set("flex-wrap", "wrap");
        cards.setVisible(false);
        add(grid, cards);
        expand(grid);
    }

    private Stream<RequestRowDTO> fetch(Query<RequestRowDTO, Void> q) {

        var data = List.of(
            new RequestRowDTO("REQ-101", "builder.alfa", "Tritanium", "34",
                100_000, 45_000, new BigDecimal("4.22")
                , "Jita IV - Moon 4",
                LocalDate.now().plusDays(20), "PARTIALLY_FILLED"),
            new RequestRowDTO("REQ-119", "builder.bravo", "Pyerite", "35",
                50_000, 50_000, new BigDecimal("8.33")
                , "Perimeter - TTT",
                LocalDate.now().plusDays(8), "ACTIVE"),
            new RequestRowDTO("REQ-200", "builder.charlie", "Scordite", "1228",
                12_000, 0, new BigDecimal("40.1")
                , "Amarr VIII - Emperor Family Academy",
                LocalDate.now().plusDays(3), "COMPLETED")
        );

        return data.stream()
            .filter(r -> filter(r, current))
            .skip(q.getOffset())
            .limit(q.getLimit());
    }

    private void configureGrid() {
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COMPACT);
        grid.setHeight("100%");

        grid.addColumn(RequestRowDTO::requestId)
            .setHeader("Request ID").setAutoWidth(true).setFlexGrow(0);

        grid.addColumn(RequestRowDTO::requesterUsername)
            .setHeader("Requester").setAutoWidth(true).setFlexGrow(0);

        grid.addColumn(RequestRowDTO::resourceName)
            .setHeader("Resource").setAutoWidth(true);

        grid.addColumn(new ComponentRenderer<>(item -> {
                Span cell = new Span(formatNumber(item.qtyNeeded())); // любое значение/формат
                cell.getElement().getStyle().set("cursor", "copy");
                cell.getElement().addEventListener("click", ev ->
                    VaadinUtils.copyToClipboard(this, cell.getText(), "Скопировано " + cell.getText())
                );
                return cell;
            }))
            .setHeader("Qty Needed").setAutoWidth(true).setFlexGrow(0);

        grid.addColumn(new ComponentRenderer<>(row -> {
            var wrap = new HorizontalLayout();
            wrap.setAlignItems(Alignment.CENTER);
            var n = new Span(NumberFormat.getIntegerInstance().format(row.qtyRemaining()));
            var bar = new ProgressBar(0, Math.max(1, row.qtyNeeded()), row.qtyRemaining());
            bar.setWidth("140px");
            wrap.add(n, bar);
            return wrap;
        })).setHeader("Qty Remaining").setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(new ComponentRenderer<>(order -> {
            Button btn = new Button(LineAwesomeIcon.CHECK_SQUARE.create(), e -> openDeliveredDialog(order));
            boolean enabled = order.status().equals("ACTIVE") && order.qtyRemaining() > 0 /*&& !order.isExpired()*/;
            btn.setEnabled(enabled);
            return btn;
        })).setHeader("Поставка").setAutoWidth(true);
        grid.addColumn(new NumberRenderer<>(
                r -> r.pricePerUnit(), NumberFormat.getNumberInstance(Locale.US)))
            .setHeader("Price (ISK/u)").setAutoWidth(true).setFlexGrow(0);

        grid.addColumn(RequestRowDTO::locationName)
            .setHeader("Location").setAutoWidth(true);

        grid.addColumn(new LocalDateRenderer<>(RequestRowDTO::deadline, "yyyy-MM-dd"))
            .setHeader("Deadline").setAutoWidth(true).setFlexGrow(0);

        grid.addColumn(new ComponentRenderer<>(row -> badge(row.status())))
            .setHeader("Status").setAutoWidth(true).setFlexGrow(0);

        grid.addColumn(new ComponentRenderer<>(row -> {
            var fulfill = new Button("Fulfill", VaadinIcon.CHECK.create(), e -> openFulfillDialog(row));
            fulfill.addClassName("primary");
            fulfill.setEnabled(!"COMPLETED".equals(row.status()) && row.qtyRemaining() > 0);
            return fulfill;
        })).setHeader("Действия").setAutoWidth(true).setFlexGrow(0);
    }

    private boolean filter(RequestRowDTO r, MarketFilter f) {
        if (f.getResource() != null && !f.getResource().isBlank()
            && !r.resourceName().toLowerCase(Locale.ROOT).contains(f.getResource().toLowerCase(Locale.ROOT))) {
            return false;
        }
        if (f.getLocation() != null && !f.getLocation().isBlank()
            && !r.locationName().toLowerCase(Locale.ROOT).contains(f.getLocation().toLowerCase(Locale.ROOT))) {
            return false;
        }
        if (f.getMinQty() != null && r.qtyRemaining() < f.getMinQty()) {
            return false;
        }
        if (f.getMaxQty() != null && r.qtyRemaining() > f.getMaxQty()) {
            return false;
        }
        if (f.getMinPrice() != null && r.pricePerUnit().compareTo(f.getMinPrice()) < 0) {
            return false;
        }
        if (f.getMaxPrice() != null && r.pricePerUnit().compareTo(f.getMaxPrice()) > 0) {
            return false;
        }
        if (f.getMinDeadline() != null && (r.deadline() == null || r.deadline().isBefore(f.getMinDeadline()))) {
            return false;
        }
        if (f.getMaxDeadline() != null && (r.deadline() == null || r.deadline().isAfter(f.getMaxDeadline()))) {
            return false;
        }
        if (f.getStatus() != null && !"ANY".equals(f.getStatus()) && !Objects.equals(f.getStatus(), r.status())) {
            return false;
        }
        return true;
    }

    private int count(Query<RequestRowDTO, Void> q) {
        return (int) fetch(new Query<>(0, Integer.MAX_VALUE, q.getSortOrders(), q.getInMemorySorting(), null)).count();
    }

    private HorizontalLayout buildFilters() {
        tableViewButton.addClickListener(event -> toggleView(true));
        cardViewButton.addClickListener(event -> toggleView(false));

        reset.addClickListener(e -> {
            clearFilters();
            grid.getDataProvider().refreshAll();
            refreshCards();
        });
        var filter = new HorizontalLayout(resource, filterButton, reset);
        filter.setAlignItems(Alignment.END);
        var row = new HorizontalLayout(filter, new HorizontalLayout(tableViewButton, cardViewButton));
        row.setJustifyContentMode(JustifyContentMode.BETWEEN);
        row.setDefaultVerticalComponentAlignment(Alignment.END);
        row.setAlignItems(Alignment.END);
        row.setWidthFull();
        return row;
    }

    private void clearFilters() {
        resource.clear();
        minQty.clear();
        maxQty.clear();
        minPrice.clear();
        maxPrice.clear();
        minDeadline.clear();
        maxDeadline.clear();
        status.setValue("ANY");
        current.setResource(null);
        current.setLocation(null);
        current.setMinQty(null);
        current.setMaxQty(null);
        current.setMinPrice(null);
        current.setMaxPrice(null);
        current.setMinDeadline(null);
        current.setMaxDeadline(null);
        current.setStatus("ANY");
    }

    private Span badge(String status) {
        var s = new Span(status);
        s.getElement().getThemeList().add("badge");
        switch (status) {
            case "ACTIVE" -> s.getElement().getThemeList().add("badge contrast");
            case "PARTIALLY_FILLED" -> s.getElement().getThemeList().add("badge success");
            case "COMPLETED" -> s.getElement().getThemeList().add("badge primary");
            case "CANCELLED" -> s.getElement().getThemeList().add("badge");
            case "EXPIRED" -> s.getElement().getThemeList().add("badge error");
        }
        return s;
    }

    private void openFulfillDialog(RequestRowDTO row) {
        var dlg = new Dialog();
        dlg.setHeaderTitle("Fulfill Request");
        dlg.setModal(true);
        dlg.setDraggable(true);
        dlg.setResizable(true);

        var available = new IntegerField("Available inventory");
        available.setValue(10);
        available.setReadOnly(true);

        var remaining = new IntegerField("Remaining units");
        remaining.setValue((int) row.qtyRemaining());
        remaining.setReadOnly(true);

        var qty = new IntegerField("Quantity to fulfill");
        qty.setMin(1);
        qty.setMax((int) Math.min(available.getValue(), row.qtyRemaining()));
        qty.setValue(qty.getMax());

        var drop = new ComboBox<String>("Пункт сдачи");
        drop.setItems("Jita IV - Moon 4", row.locationName());
        drop.setValue(row.locationName());

        var accept = new Button("Confirm", e -> {
            if (qty.getValue() == null || qty.getValue() <= 0) {
                Notification.show("Введите валидное количество");
                return;
            }
            Notification.show("Заявка зарезервирована: " + qty.getValue() + " ед.");
            dlg.close();
            grid.getDataProvider().refreshAll();
            refreshCards();
        });
        var cancel = new Button("Cancel", e -> dlg.close());

        var form = VaadinUtils.initCommonVerticalLayout(available, remaining, qty, drop);
        dlg.add(form);
        dlg.getFooter().add(cancel, accept);
        dlg.setCloseOnOutsideClick(false);
        dlg.open();
    }

    private void toggleView(boolean table) {
        grid.setVisible(table);
        cards.setVisible(!table);
        if (!table) {
            refreshCards();
        }
    }

    private void refreshCards() {
        if (!cards.isVisible()) {
            return;
        }
        cards.removeAll();
        // перерисовываем карточки из текущих данных грida
        @SuppressWarnings("unchecked")
        DataProvider<RequestRowDTO, Void> provider =
            (DataProvider<RequestRowDTO, Void>) grid.getDataProvider();
        var query = new Query<RequestRowDTO, Void>(0, 50, Collections.emptyList(), null, null);

        provider.fetch(query).forEach(row -> cards.add(card(row)));
    }

    private Div card(RequestRowDTO r) {
        var card = new Div();
        card.getStyle().set("border", "1px solid var(--lumo-contrast-20pct)");
        card.getStyle().set("border-radius", "12px");
        card.getStyle().set("padding", "12px");
        card.getStyle().set("width", "320px");
        card.getStyle().set("box-shadow", "5px 8px 8px 5px rgb(0 0 0 / 39%)");


        var title = new H2(r.resourceName() + " • " + r.pricePerUnit() + " ISK");
        title.getStyle().set("font-size", "var(--lumo-font-size-m)");
        title.getStyle().set("margin", "0 0 8px 0");

        var meta = new Div(new Span("Request ID: " + r.requestId()),
            new Div(new Span("Requester: " + r.requesterUsername())),
            new Div(new Span("Remaining: " + r.qtyRemaining())),
            new Div(new Span("Location: " + r.locationName())),
            new Div(new Span("Deadline: " + (r.deadline() != null ? r.deadline() : "-"))));
        meta.getStyle().set("display", "grid");
        meta.getStyle().set("gap", "4px");

        var act = new Button("Fulfill", e -> openFulfillDialog(r));
        var finish = new Button("Finish", e -> openFulfillDialog(r));
        act.setEnabled(!"COMPLETED".equals(r.status()) && r.qtyRemaining() > 0);
        finish.setEnabled(!"COMPLETED".equals(r.status()) && r.qtyRemaining() > 0);
        HorizontalLayout horizontalLayout = new HorizontalLayout(act, finish);
        horizontalLayout.setSizeFull();
        card.add(title, badge(r.status()), meta, act, horizontalLayout);
        return card;
    }

    private void openDeliveredDialog(RequestRowDTO order) {
        Dialog dlg = new Dialog();
        dlg.setHeaderTitle("Подтвердить поставку");

        TextField contractId = new TextField("ID контракта");
        IntegerField qty = new IntegerField("Количество (факт)");
        qty.setMin(1);
        qty.setPlaceholder("Оставьте пустым — возьмём из контракта");
        qty.setWidth("200px");

        Button confirm = new Button("Подтвердить", ev -> {
            Integer q = qty.getValue();
            try {
                Notification.show("Заявка на зачёт поставки отправлена", 3000, Notification.Position.MIDDLE);
                dlg.close();
                grid.getDataProvider().refreshAll();
            } catch (Exception ex) {
                Notification.show("Ошибка: " + ex.getMessage(), 5000, Notification.Position.MIDDLE);
            }
        });
        Button cancel = new Button("Отмена", e -> dlg.close());

        VerticalLayout form = new VerticalLayout(contractId, qty);
        form.setPadding(false);
        form.setSpacing(true);
        dlg.add(form);
        dlg.getFooter().add(cancel, confirm);
        dlg.open();
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {

    }
}
