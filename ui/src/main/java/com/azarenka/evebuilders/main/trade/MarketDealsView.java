package com.azarenka.evebuilders.main.trade;

import com.azarenka.evebuilders.common.util.VaadinUtils;
import com.azarenka.evebuilders.component.View;
import com.azarenka.evebuilders.domain.dto.market.DealRowDTO;
import com.azarenka.evebuilders.domain.dto.market.MarketFilter;
import com.azarenka.evebuilders.main.menu.MenuTradePage;
import com.azarenka.evebuilders.service.impl.auth.eve.SecurityUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LocalDateRenderer;
import com.vaadin.flow.data.renderer.NumberRenderer;
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

@Route(value = "orders", layout = MenuTradePage.class)
@RolesAllowed({"ROLE_SUPER_ADMIN", "ROLE_MINER", "ROLE_ADMIN", "ROLE_BUILDER"})
@PageTitle("MARKET Orders")
public class MarketDealsView extends View {

    private final Grid<DealRowDTO> grid = new Grid<>(DealRowDTO.class, false);
    private final FlexLayout cards = new FlexLayout();

    private final MultiSelectComboBox<String> resource = new MultiSelectComboBox<>("Ресурс");

    private final Button filterButton = new Button("Фильтр", new Icon(VaadinIcon.FILTER));
    private final Button reset = new Button("Сброс");
    private final Button tableViewButton = new Button(LineAwesomeIcon.TABLE_SOLID.create());
    private final Button cardViewButton = new Button(LineAwesomeIcon.ADDRESS_CARD_SOLID.create());

    private final MarketFilter current = new MarketFilter();


    public MarketDealsView() {
        getStyle().set("padding", "0 10px 0 10px");
        initContent();

        grid.setItems(new CallbackDataProvider<>(this::fetch, this::count));

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

    private void configureGrid() {
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COMPACT);
        grid.setHeight("100%");

        grid.addColumn(new ComponentRenderer<>(item -> {
                Span cell = new Span(item.orderId());
                cell.getElement().getStyle().set("cursor", "copy");
                cell.getElement().addEventListener("click", ev ->
                    VaadinUtils.copyToClipboard(this, cell.getText(), "Скопировано " + cell.getText())
                );
                return cell;
            }))
            .setHeader("Order ID")
            .setAutoWidth(true)
            .setFlexGrow(0);

        // Resource
        grid.addColumn(DealRowDTO::resourceName)
            .setHeader("Resource")
            .setAutoWidth(true);

        // Seller / Buyer
        grid.addColumn(DealRowDTO::sellerUsername)
            .setHeader("Seller")
            .setAutoWidth(true)
            .setFlexGrow(0);
        grid.addColumn(DealRowDTO::buyerUsername)
            .setHeader("Buyer")
            .setAutoWidth(true)
            .setFlexGrow(0);

        // Qty (total)
        grid.addColumn(new NumberRenderer<>(
                DealRowDTO::qty, NumberFormat.getIntegerInstance()))
            .setHeader("Qty")
            .setAutoWidth(true)
            .setFlexGrow(0);

        // Price (ISK/u)
        grid.addColumn(new NumberRenderer<>(
                DealRowDTO::pricePerUnit, NumberFormat.getNumberInstance(Locale.US)))
            .setHeader("Price (ISK/u)")
            .setAutoWidth(true)
            .setFlexGrow(0);

        // Total Price
        grid.addColumn(new NumberRenderer<>(
                DealRowDTO::totalPrice, NumberFormat.getNumberInstance(Locale.US)))
            .setHeader("Total")
            .setAutoWidth(true)
            .setFlexGrow(0);

        // Location
        grid.addColumn(DealRowDTO::locationName)
            .setHeader("Location")
            .setAutoWidth(true);

        // Created On
        grid.addColumn(new LocalDateRenderer<>(DealRowDTO::createdOn, "yyyy-MM-dd"))
            .setHeader("Created")
            .setAutoWidth(true)
            .setFlexGrow(0);

        // Status (badge)
        grid.addColumn(new ComponentRenderer<>(row -> badge(row.status())))
            .setHeader("Status")
            .setAutoWidth(true)
            .setFlexGrow(0);

        // Actions (per role)
        grid.addColumn(new ComponentRenderer<>(row -> {
            HorizontalLayout actions = new HorizontalLayout();
            actions.setSpacing(true);
            actions.setPadding(false);

            // для продавца (майнёра)
            if (isSeller(row)) {
                Button more = new Button(new Icon(VaadinIcon.ELLIPSIS_DOTS_V));
                more.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
                more.getElement().setProperty("title", "Actions");

                ContextMenu menu = new ContextMenu(more);
                menu.setOpenOnClick(true);

                boolean active = "ACTIVE".equals(row.status());

                MenuItem edit = menu.addItem("Edit", e -> openEditDialog(row));
                edit.setEnabled(active);

                MenuItem cancel = menu.addItem("Cancel", e -> openCancelDialog(row));
                cancel.setEnabled(active);
                actions.add(more);
            }

            // для покупателя (строителя)
            if (isBuyer(row)) {
                Button more = new Button(new Icon(VaadinIcon.ELLIPSIS_DOTS_V));
                more.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
                more.getElement().setProperty("title", "Actions");

                ContextMenu menu = new ContextMenu(more);
                menu.setOpenOnClick(true);
                boolean active = "ACTIVE".equals(row.status());
                MenuItem edit = menu.addItem("Confirm Delivery", e -> openEditDialog(row));
                edit.setEnabled(active);
                actions.add(more);
            }

            return actions;
        })).setHeader("Actions").setAutoWidth(true).setFlexGrow(0).setTextAlign(ColumnTextAlign.END);
    }

    private void openConfirmDeliveryDialog(DealRowDTO row) {
    }

    private void openCancelDialog(DealRowDTO row) {

    }

    private void openEditDialog(DealRowDTO row) {

    }

    private Stream<DealRowDTO> fetch(Query<DealRowDTO, Void> q) {

        var data = List.of(
            new DealRowDTO(
                "ORD-9001",
                "builder.alfa",
                "AntonFromEpam",
                "Tritanium",
                "34",
                9_000L,                  // qty
                new BigDecimal("4.22"),  // pricePerUnit
                new BigDecimal("4.22").multiply(new BigDecimal("9000")), // totalPrice
                "Jita IV - Moon 4",
                "PARTIALLY_FULFILLED",   // статус сделки
                LocalDate.now().minusDays(2) // createdOn
            ),
            new DealRowDTO(
                "ORD-9002",
                "AntonFromEpam",
                "miner.echo",
                "Pyerite",
                "35",
                5_000L,
                new BigDecimal("8.33"),
                new BigDecimal("8.33").multiply(new BigDecimal("5000")),
                "Perimeter - TTT",
                "ACTIVE",
                LocalDate.now().minusDays(1)
            ),
            new DealRowDTO(
                "ORD-9003",
                "builder.charlie",
                "AntonFromEpam",
                "Scordite",
                "1228",
                12_000L,
                new BigDecimal("40.10"),
                new BigDecimal("40.10").multiply(new BigDecimal("12000")),
                "Amarr VIII - Emperor Family Academy",
                "COMPLETED",
                LocalDate.now().minusDays(5)
            )
        );

        return data.stream()
            .filter(r -> filter(r, current))
            .skip(q.getOffset())
            .limit(q.getLimit());
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
        DataProvider<DealRowDTO, Void> provider =
            (DataProvider<DealRowDTO, Void>) grid.getDataProvider();
        var query = new Query<DealRowDTO, Void>(0, 50, Collections.emptyList(), null, null);

        provider.fetch(query).forEach(row -> cards.add(card(row)));
    }

    private boolean filter(DealRowDTO r, MarketFilter f) {
        if (f.getResource() != null && !f.getResource().isBlank()
            && !r.resourceName().toLowerCase(Locale.ROOT).contains(f.getResource().toLowerCase(Locale.ROOT))) {
            return false;
        }
        if (f.getLocation() != null && !f.getLocation().isBlank()
            && !r.locationName().toLowerCase(Locale.ROOT).contains(f.getLocation().toLowerCase(Locale.ROOT))) {
            return false;
        }
        /*if (f.getMinQty() != null && r.qtyRemaining() < f.getMinQty()) {
            return false;
        }
        if (f.getMaxQty() != null && r.qtyRemaining() > f.getMaxQty()) {
            return false;
        }*/
        if (f.getMinPrice() != null && r.pricePerUnit().compareTo(f.getMinPrice()) < 0) {
            return false;
        }
        if (f.getMaxPrice() != null && r.pricePerUnit().compareTo(f.getMaxPrice()) > 0) {
            return false;
        }
       /* if (f.getMinDeadline() != null && (r.deadline() == null || r.deadline().isBefore(f.getMinDeadline()))) {
            return false;
        }
        if (f.getMaxDeadline() != null && (r.deadline() == null || r.deadline().isAfter(f.getMaxDeadline()))) {
            return false;
        }*/
        if (f.getStatus() != null && !"ANY".equals(f.getStatus()) && !Objects.equals(f.getStatus(), r.status())) {
            return false;
        }
        return true;
    }

    private int count(Query<DealRowDTO, Void> q) {
        return (int) fetch(new Query<>(0, Integer.MAX_VALUE, q.getSortOrders(), q.getInMemorySorting(), null)).count();
    }

    private boolean isSeller(DealRowDTO row) {
        return Objects.equals(row.sellerUsername(), SecurityUtils.getUserName());
    }

    private boolean isBuyer(DealRowDTO row) {
        return Objects.equals(row.buyerUsername(), SecurityUtils.getUserName());

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

    private Div card(DealRowDTO d) {
        var card = new Div();
        card.getStyle().set("border", "1px solid var(--lumo-contrast-20pct)");
        card.getStyle().set("border-radius", "12px");
        card.getStyle().set("padding", "12px");
        card.getStyle().set("width", "320px");
        card.getStyle().set("box-shadow", "5px 8px 8px 5px rgb(0 0 0 / 39%)");

        var title = new H2(d.resourceName() + " • "
            + NumberFormat.getNumberInstance(Locale.US).format(d.pricePerUnit()) + " ISK/u");
        title.getStyle().set("font-size", "var(--lumo-font-size-m)");
        title.getStyle().set("margin", "0 0 8px 0");

        // Метаданные
        Span orderId = new Span("Order ID: " + d.orderId());
        orderId.getElement().getStyle().set("cursor", "copy");
        orderId.getElement().addEventListener("click", ev ->
            VaadinUtils.copyToClipboard(this, d.orderId(), "Скопировано " + d.orderId())
        );

        var meta = new Div(
            orderId,
            new Div(new Span("Seller: " + d.sellerUsername())),
            new Div(new Span("Buyer: " + d.buyerUsername())),
            new Div(new Span("Qty: " + NumberFormat.getIntegerInstance().format(d.qty()))),
            new Div(new Span("Total: " + NumberFormat.getNumberInstance(Locale.US).format(d.totalPrice()) + " ISK")),
            new Div(new Span("Location: " + d.locationName())),
            new Div(new Span("Created: " + (d.createdOn() != null ? d.createdOn() : "-")))
        );
        meta.getStyle().set("display", "grid");
        meta.getStyle().set("gap", "4px");

        // Кнопки действий
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);
        actions.setPadding(false);

        if (isSeller(d)) {
            var edit = new Button("Edit", e -> openEditDialog(d));
            var cancel = new Button("Cancel", e -> openCancelDialog(d));
            boolean active = "ACTIVE".equals(d.status());
            edit.setEnabled(active);
            cancel.setEnabled(active);
            actions.add(edit, cancel);
        }
        if (isBuyer(d)) {
            var confirm = new Button("Confirm Delivery", e -> openConfirmDeliveryDialog(d));
            confirm.setEnabled("ACTIVE".equals(d.status()));
            actions.add(confirm);
        }

        card.add(title, badge(d.status()), meta, actions);
        return card;
    }
}
