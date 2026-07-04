package com.azarenka.evebuilders.main.managment.corporation;

import com.azarenka.evebuilders.common.util.VaadinUtils;
import com.azarenka.evebuilders.component.View;
import com.azarenka.evebuilders.domain.db.ManagedCorporation;
import com.azarenka.evebuilders.main.managment.api.ICorporationRegistryController;
import com.azarenka.evebuilders.main.menu.MenuManagerPage;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "corporation-registry", layout = MenuManagerPage.class)
@PageTitle("Corporation Registry")
@RolesAllowed({"ROLE_ADMIN", "ROLE_SUPER_ADMIN"})
public class CorporationRegistryView extends View implements LocaleChangeObserver {

    private final ICorporationRegistryController controller;
    private ListDataProvider<ManagedCorporation> dataProvider;
    private Grid<ManagedCorporation> grid;
    private Button refreshButton;

    public CorporationRegistryView(ICorporationRegistryController controller) {
        this.controller = controller;
        setPadding(true);
        setSpacing(true);
        add(initToolbar(), initGrid());
    }

    private HorizontalLayout initToolbar() {
        refreshButton = new Button(getTranslation("button.app.refresh"), event -> refreshGrid());
        refreshButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        return new HorizontalLayout(refreshButton);
    }

    private Grid<ManagedCorporation> initGrid() {
        dataProvider = new ListDataProvider<>(controller.getAllCorporations());
        grid = VaadinUtils.initGrid(dataProvider, "corporation-registry-grid");
        grid.addColumn(ManagedCorporation::getEveCorporationId).setHeader(getTranslation("table.column.eve_corporation_id")).setAutoWidth(true);
        grid.addColumn(ManagedCorporation::getCorporationName).setHeader(getTranslation("table.column.nomination")).setAutoWidth(true);
        grid.addColumn(ManagedCorporation::getCorporationTicker).setHeader(getTranslation("table.column.ticker")).setAutoWidth(true);
        grid.addColumn(ManagedCorporation::getOwnerUsername).setHeader(getTranslation("table.column.owner")).setAutoWidth(true);
        grid.addColumn(ManagedCorporation::getCreatedBy).setHeader(getTranslation("table.column.created_by")).setAutoWidth(true);
        grid.addColumn(value -> value.getCreatedDate() == null ? "" : value.getCreatedDate().toString())
            .setHeader(getTranslation("table.column.created_date"))
            .setAutoWidth(true);
        return grid;
    }

    private void refreshGrid() {
        dataProvider = new ListDataProvider<>(controller.getAllCorporations());
        grid.setItems(dataProvider);
        dataProvider.refreshAll();
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        refreshButton.setText(getTranslation("button.app.refresh"));
        grid.getColumns().get(0).setHeader(getTranslation("table.column.eve_corporation_id"));
        grid.getColumns().get(1).setHeader(getTranslation("table.column.nomination"));
        grid.getColumns().get(2).setHeader(getTranslation("table.column.ticker"));
        grid.getColumns().get(3).setHeader(getTranslation("table.column.owner"));
        grid.getColumns().get(4).setHeader(getTranslation("table.column.created_by"));
        grid.getColumns().get(5).setHeader(getTranslation("table.column.created_date"));
    }
}
