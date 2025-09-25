package com.azarenka.evebuilders.main.staff.fleet;

import com.azarenka.evebuilders.common.util.VaadinUtils;
import com.azarenka.evebuilders.component.View;
import com.azarenka.evebuilders.domain.dto.UserFleetStat;
import com.azarenka.evebuilders.domain.enums.FleetMetric;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
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
import com.vaadin.flow.router.Route;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;

import jakarta.annotation.security.RolesAllowed;

@Route(value = "fleet-statistic", layout = StaffFleetActivityDashboard.class)
@RolesAllowed({"ROLE_ADMIN", "ROLE_SUPER_ADMIN", "ROLE_CEO"})
public class FleetStatistic extends View implements LocaleChangeObserver {

    private final IFleetStatisticController controller;

    private FleetMetric currentMetric = FleetMetric.CTA_ALL;
    private LocalDate periodFrom;
    private LocalDate periodTo;

    private final Button ktaAllButton   = new Button(getTranslation("button.cta_all"));
    private final Button ktaMonthButton = new Button(getTranslation("button.cta_month"));

    private final Div podiumContainer = new Div();
    private final Grid<UserFleetStat> grid = new Grid<>(UserFleetStat.class, false);

    private H1 title;

    public FleetStatistic(IFleetStatisticController controller) {
        this.controller = controller;
        setSizeFull();
        getStyle().set("padding", "5px 5px 30px 5px");

        var horizontalLayout = new HorizontalLayout(buildPodium());
        horizontalLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        horizontalLayout.setWidthFull();

        add(buildHeader(), buildToolbar(), horizontalLayout, buildGrid());
        initState();
        reload();
    }

    private HorizontalLayout buildHeader() {
        title = new H1(getTranslation("label.fleet.header"));
        var header = new HorizontalLayout(title);
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.CENTER);
        header.setAlignItems(Alignment.CENTER);
        return header;
    }

    private HorizontalLayout buildToolbar() {
        styleAsToggle(ktaAllButton, true);
        styleAsToggle(ktaMonthButton, false);

        ktaAllButton.addClickListener(e -> selectMetric(FleetMetric.CTA_ALL));
        ktaMonthButton.addClickListener(e -> selectMetric(FleetMetric.CTA_MONTH));

        var left = new HorizontalLayout(ktaAllButton, ktaMonthButton);
        var spacer = new Div();
        var bar = new HorizontalLayout(left, spacer);
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

    private Grid<UserFleetStat> buildGrid() {
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        grid.addComponentColumn(us -> {
                var row = new HorizontalLayout();
                row.setSpacing(true);
                var avatarImg = controller.getCharacterPortrait(us.characterId());
                var name = new Span(us.displayName());
                if (!us.active()) {
                    name.getStyle().set("opacity", "0.65");
                }
                row.add(avatarImg == null ? new Avatar(us.displayName()) : avatarImg, name);
                name.getElement().getStyle().set("cursor", "copy");
                name.getElement().addEventListener("click", ev ->
                    VaadinUtils.copyToClipboard(this, name.getText(), "Скопировано " + name.getText())
                );
                return row;
            }).setHeader(getTranslation("table.column.participant" ))
            .setAutoWidth(true).setFlexGrow(1);

        grid.addColumn(UserFleetStat::rank)
            .setHeader(getTranslation("table.column.place" ))
            .setAutoWidth(true);

        grid.addColumn(UserFleetStat::value)
            .setHeader(getTranslation("table.column.statistic_value" ))
            .setAutoWidth(true);

        grid.addColumn(v -> getTranslation(metricLabel(v.metricLabel())))
            .setHeader(getTranslation("table.column.metric" ))
            .setAutoWidth(true);
        grid.addColumn(v -> isTSEnabled(v.active()))
            .setHeader(getTranslation("table.column.ts_enabled" ))
            .setAutoWidth(true);

        return grid;
    }

    public String isTSEnabled(boolean isTSEnabled) {
        return isTSEnabled ? "Y" : "N";
    }

    private void initState() {
        var now = LocalDate.now();
        if (currentMetric == FleetMetric.CTA_MONTH) {
            YearMonth ym = YearMonth.from(now);
            periodFrom = ym.atDay(1);
            periodTo = ym.atEndOfMonth();
        } else {
            periodFrom = LocalDate.of(2017, 1, 1);
            periodTo = LocalDate.of(2047, 12, 31);
        }
    }

    private void selectMetric(FleetMetric metric) {
        if (metric == currentMetric) return;
        currentMetric = metric;
        if (metric == FleetMetric.CTA_MONTH) {
            YearMonth ym = YearMonth.from(LocalDate.now());
            periodFrom = ym.atDay(1);
            periodTo = ym.atEndOfMonth();
        } else {
            periodFrom = LocalDate.of(2017, 1, 1);
            periodTo = LocalDate.of(2047, 12, 31);
        }
        styleAsToggle(ktaAllButton, metric == FleetMetric.CTA_ALL);
        styleAsToggle(ktaMonthButton, metric == FleetMetric.CTA_MONTH);
        reload();
    }

    private void styleAsToggle(Button b, boolean active) {
        b.removeThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_TERTIARY);
        b.addThemeVariants(active ? ButtonVariant.LUMO_PRIMARY : ButtonVariant.LUMO_TERTIARY);
    }

    private void reload() {
        List<UserFleetStat> stats = controller.fetchLeaderboard(currentMetric, periodFrom, periodTo);
        renderPodium(stats);
        grid.setItems(stats);
    }

    private void renderPodium(List<UserFleetStat> stats) {
        podiumContainer.removeAll();

        var row = new HorizontalLayout();
        row.setWidthFull();
        row.setJustifyContentMode(JustifyContentMode.CENTER);
        row.setAlignItems(Alignment.END);
        row.setSpacing(true);

        var u1 = stats.size() > 0 ? stats.get(0) : null;
        var u2 = stats.size() > 1 ? stats.get(1) : null;
        var u3 = stats.size() > 2 ? stats.get(2) : null;

        var card2 = createPodiumCard(u2, 2, 180);
        var card1 = createPodiumCard(u1, 1, 200);
        var card3 = createPodiumCard(u3, 3, 160);

        row.add(card2, card1, card3);
        podiumContainer.add(row);
    }

    private VerticalLayout createPodiumCard(UserFleetStat userStat, int place, int heightPx) {
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
        final String userName = (userStat != null) ? userStat.displayName() : "—";
        var image = (userStat != null) ? controller.getCharacterPortrait(userStat.characterId()) : null;
        var avatar = new Avatar(userName);
        var name = new Span(userName);
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
            case 1 -> {
                Icon icon = VaadinIcon.TROPHY.create(); icon.setColor("gold"); return icon;
            }
            case 2 -> {
                Icon icon = VaadinIcon.MEDAL.create(); icon.setColor("silver"); return icon;
            }
            default -> {
                Icon icon = VaadinIcon.STAR.create(); icon.setColor("#CD7F32"); return icon;
            }
        }
    }

    @Override
    public void localeChange(LocaleChangeEvent e) {
        title.setText(getTranslation("label.fleet.header", "Доска почёта"));

        grid.getColumns().get(0).setHeader(getTranslation("table.column.participant", "Участник"));
        grid.getColumns().get(1).setHeader(getTranslation("table.column.place", "Место"));
        grid.getColumns().get(2).setHeader(getTranslation("table.column.statistic_value", "Значение"));
        grid.getColumns().get(3).setHeader(getTranslation("table.column.metric", "Метрика"));
        grid.getColumns().get(4).setHeader(getTranslation("table.column.ts_enabled", "Ts"));

        ktaAllButton.setText(getTranslation("button.cta_all", "По КТА"));
        ktaMonthButton.setText(getTranslation("button.cta_month", "За месяц (КТА)"));
    }

    private static String metricLabel(FleetMetric m) {
        return switch (m) {
            case CTA_ALL   -> "label.fleet.metric.kta_all";
            case CTA_MONTH -> "label.fleet.metric.kta_month";
        };
    }
}
