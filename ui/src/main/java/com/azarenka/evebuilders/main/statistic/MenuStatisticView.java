package com.azarenka.evebuilders.main.statistic;

import static com.azarenka.evebuilders.domain.enums.Metric.ORDERS_ALL;
import static com.azarenka.evebuilders.domain.enums.Metric.ORDERS_MONTH;
import static com.azarenka.evebuilders.domain.enums.Metric.SHIPS_MADE;

import com.azarenka.evebuilders.component.View;
import com.azarenka.evebuilders.domain.dto.UserStat;
import com.azarenka.evebuilders.domain.enums.Metric;
import com.azarenka.evebuilders.main.menu.MenuStatisticPage;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;

import jakarta.annotation.security.RolesAllowed;

@Route(value = "default", layout = MenuStatisticPage.class)
@RolesAllowed({"ROLE_SUPER_ADMIN", "ROLE_BUILDER", "ROLE_MINER", "ROLE_ADMIN", "ROLE_COORDINATOR"})
@PageTitle("Statistic")
public class MenuStatisticView extends View implements LocaleChangeObserver {

    private final IStatisticController controller;

    private Metric currentMetric = ORDERS_ALL;
    private boolean includeInactive = false;
    private LocalDate periodFrom;
    private LocalDate periodTo;

    private final Button ordersAllButton = new Button(getTranslation("button.by_orders"));
    private final Button ordersMonthButton = new Button(getTranslation("button.monthly"));
    private final Button shipsMadeButton = new Button(getTranslation("button.ships_count"));
    private final Checkbox includeInactiveButton = new Checkbox(getTranslation("label.statistic.include_inactive"));

    private final Div podiumContainer = new Div();
    private final Grid<UserStat> grid = new Grid<>(UserStat.class, false);

    private H1 title;
    private Button help = new Button(new Icon(VaadinIcon.QUESTION_CIRCLE));

    public MenuStatisticView(IStatisticController controller) {
        this.controller = controller;
        setSizeFull();
        super.getStyle().set("padding", "5px 5px 30px 5px");
        var horizontalLayout = new HorizontalLayout(buildPodium());
        horizontalLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        horizontalLayout.setWidthFull();
        add(buildHeader(), buildToolbar(), horizontalLayout, buildGrid());
        initState();
        reload();
    }

    private HorizontalLayout buildHeader() {
        title = new H1(getTranslation("label.statistic.header"));
        var help = new Button(new Icon(VaadinIcon.QUESTION_CIRCLE));
        help.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        help.setTooltipText(getTranslation("message.button_tooltip.statistic_calculation_info"));
        var spacer = new Span(" ");
        var header = new HorizontalLayout(spacer, title, help);
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.setAlignItems(Alignment.CENTER);
        return header;
    }

    private HorizontalLayout buildToolbar() {
        styleAsToggle(ordersAllButton, true);
        styleAsToggle(ordersMonthButton, false);
        styleAsToggle(shipsMadeButton, false);
        ordersAllButton.addClickListener(e -> selectMetric(Metric.ORDERS_ALL));
        ordersMonthButton.addClickListener(e -> selectMetric(Metric.ORDERS_MONTH));
        shipsMadeButton.addClickListener(e -> selectMetric(Metric.SHIPS_MADE));
        includeInactiveButton.addValueChangeListener(e -> {
            includeInactive = Boolean.TRUE.equals(e.getValue());
            reload();
        });
        var left = new HorizontalLayout(ordersAllButton, ordersMonthButton, shipsMadeButton);
        var right = new HorizontalLayout(includeInactiveButton);
        var spacer = new Div();
        HorizontalLayout bar = new HorizontalLayout(left, spacer, right);
        bar.setWidthFull();
        bar.setAlignItems(Alignment.CENTER);
        bar.expand(spacer);
        bar.getStyle().set("position", "sticky");
        bar.getStyle().set("top", "0");
        bar.getStyle().set("z-index", "1");
        bar.getStyle().set("background", "var(--lumo-base-color)");
        return bar;
    }

    private Div buildPodium() {
        podiumContainer.getStyle().set("margin", "var(--lumo-space-m) 0");
        return podiumContainer;
    }

    private Grid<UserStat> buildGrid() {
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.addComponentColumn(us -> {
            var row = new HorizontalLayout();
            row.setSpacing(true);
            var avatar = controller.getCharacterPortrait(us.displayName());
            var name = new Span(us.displayName());
            if (!us.active()) {
                name.getStyle().set("opacity", "0.65");
            }
            row.add(avatar, name);
            return row;
        }).setAutoWidth(true).setFlexGrow(1);
        grid.addColumn(UserStat::rank).setAutoWidth(true);
        grid.addColumn(UserStat::value).setAutoWidth(true);
        grid.addColumn(value -> getTranslation(metricLabel(value.metricLabel()))).setAutoWidth(true);
        return grid;
    }

    private void initState() {
        var now = LocalDate.now();
        if (currentMetric == ORDERS_MONTH) {
            YearMonth ym = YearMonth.from(now);
            periodFrom = ym.atDay(1);
            periodTo = ym.atEndOfMonth();
        } else {
            periodFrom = LocalDate.MIN.plusYears(100);
            periodTo = LocalDate.MAX.minusYears(100);
        }
        includeInactive = false;
        includeInactiveButton.setValue(false);
    }

    private void selectMetric(Metric metric) {
        if (Objects.equals(metric, currentMetric)) {
            return;
        }
        currentMetric = metric;
        if (metric == ORDERS_MONTH) {
            YearMonth ym = YearMonth.from(LocalDate.now());
            periodFrom = ym.atDay(1);
            periodTo = ym.atEndOfMonth();
        } else {
            periodFrom = LocalDate.MIN.plusYears(100);
            periodTo = LocalDate.MAX.minusYears(100);
        }
        styleAsToggle(ordersAllButton, metric == ORDERS_ALL);
        styleAsToggle(ordersMonthButton, metric == ORDERS_MONTH);
        styleAsToggle(shipsMadeButton, metric == SHIPS_MADE);

        reload();
    }

    private void styleAsToggle(Button b, boolean active) {
        b.removeThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_TERTIARY);
        if (active) {
            b.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        } else {
            b.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        }
    }

    private void reload() {
        List<UserStat> stats = controller.fetchLeaderboard(currentMetric, periodFrom, periodTo, includeInactive);
        renderPodium(stats);
        grid.setItems(stats);
    }

    private void renderPodium(List<UserStat> stats) {
        podiumContainer.removeAll();

        var row = new HorizontalLayout();
        row.setWidthFull();
        row.setJustifyContentMode(JustifyContentMode.CENTER);
        row.setAlignItems(Alignment.END);
        row.setSpacing(true);

        var u1 = stats.size() > 0 ? stats.get(0) : null; // 1-е
        var u2 = stats.size() > 1 ? stats.get(1) : null; // 2-е
        var u3 = stats.size() > 2 ? stats.get(2) : null; // 3-е

        var card2 = createPodiumCard(u2, 2, 180);
        var card1 = createPodiumCard(u1, 1, 200);
        var card3 = createPodiumCard(u3, 3, 160);

        row.add(card2, card1, card3);
        podiumContainer.add(row);
    }

    private VerticalLayout createPodiumCard(UserStat userStat, int place, int heightPx) {
        var card = new VerticalLayout();
        card.setPadding(true);
        card.setSpacing(false);
        card.setAlignItems(Alignment.CENTER);
        card.getStyle().set("background", "var(--lumo-contrast-5pct)");
        card.getStyle().set("border-radius", "var(--lumo-border-radius-l)");
        card.getStyle().set("box-shadow", "var(--lumo-box-shadow-s)");
        card.setWidth("220px");
        card.setHeight(heightPx, Unit.PIXELS);
        var icon = createIcon(place);
        var userName = userStat.displayName();
        var image = controller.getCharacterPortrait(userName);
        var avatar = new Avatar(userName, image.getSrc());
        var name = new Span(userStat != null ? userStat.displayName() : "—");
        var value = new Span(userStat != null ? ("«" + userStat.value() + "»") : "—");
        var rank = new Span("#" + place);

        name.getStyle().set("font-weight", "600");
        value.getStyle().set("font-size", "var(--lumo-font-size-l)");
        rank.getStyle().set("color", "var(--lumo-secondary-text-color)");
        if (Objects.isNull(image)) {
            card.add(icon, avatar, name, value, rank);
        } else {
            card.add(icon, image, name, value, rank);
        }
        return card;
    }

    private Icon createIcon(int place) {
        switch (place) {
            case 1: {
                Icon icon = VaadinIcon.TROPHY.create();
                icon.setColor("gold");
                return icon;
            }
            case 2: {
                Icon icon = VaadinIcon.MEDAL.create();
                icon.setColor("silver");
                return icon;
            }
            default: {
                Icon icon = VaadinIcon.STAR.create();
                icon.setColor("#CD7F32");
                return icon;
            }
        }
    }

    @Override
    public void localeChange(LocaleChangeEvent localeChangeEvent) {
        title.setText(getTranslation("label.statistic.header"));

        help.setTooltipText(getTranslation("message.button_tooltip.statistic_calculation_info"));
        grid.getColumns().get(0).setHeader(getTranslation("table.column.participant"));
        grid.getColumns().get(1).setHeader(getTranslation("table.column.place"));
        grid.getColumns().get(2).setHeader(getTranslation("table.column.statistic_value"));
        grid.getColumns().get(3).setHeader(getTranslation("table.column.metric"));

        includeInactiveButton.setLabel(getTranslation("label.statistic.include_inactive"));

        ordersAllButton.setText(getTranslation("button.by_orders"));
        ordersMonthButton.setText(getTranslation("button.monthly"));
        shipsMadeButton.setText(getTranslation("button.ships_count"));
    }

    private static String metricLabel(Metric m) {
        return switch (m) {
            case ORDERS_ALL -> "label.statistic.metric.orders";
            case ORDERS_MONTH -> "label.statistic.metric.month";
            case SHIPS_MADE -> "label.statistic.metric.ships";
        };
    }
}
