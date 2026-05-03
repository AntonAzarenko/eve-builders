package com.azarenka.evebuilders.main.orders.corporation;

import com.azarenka.evebuilders.common.util.VaadinUtils;
import com.azarenka.evebuilders.component.View;
import com.azarenka.evebuilders.domain.db.ManagedCorporation;
import com.azarenka.evebuilders.domain.exeptions.ValidationException;
import com.azarenka.evebuilders.main.menu.MenuOrdersPage;
import com.azarenka.evebuilders.main.orders.corporation.api.IManagedCorporationsController;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "my-corporations", layout = MenuOrdersPage.class)
@PageTitle("My Corporations")
@RolesAllowed({"ROLE_ADMIN", "ROLE_SUPER_ADMIN", "ROLE_BUILDER"})
public class ManagedCorporationsView extends View implements LocaleChangeObserver {

    private final IManagedCorporationsController controller;
    private ListDataProvider<ManagedCorporation> dataProvider;
    private Grid<ManagedCorporation> grid;
    private TextField corporationNameField;
    private Button addButton;
    private Button refreshButton;

    public ManagedCorporationsView(IManagedCorporationsController controller) {
        this.controller = controller;
        setPadding(true);
        setSpacing(true);
        add(initForm(), initGrid());
    }

    private HorizontalLayout initForm() {
        corporationNameField = new TextField(getTranslation("corporation.form.name"));
        corporationNameField.setWidth("320px");

        addButton = new Button(getTranslation("corporation.button.add"), event -> saveCorporation());
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);

        refreshButton = new Button(getTranslation("button.app.refresh"), event -> refreshGrid());
        refreshButton.addThemeVariants(ButtonVariant.LUMO_SMALL);

        HorizontalLayout layout = new HorizontalLayout(corporationNameField, addButton, refreshButton);
        layout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.END);
        return layout;
    }

    private Grid<ManagedCorporation> initGrid() {
        dataProvider = new ListDataProvider<>(controller.getMyCorporations());
        grid = VaadinUtils.initGrid(dataProvider, "managed-corporations-grid");
        grid.addColumn(ManagedCorporation::getEveCorporationId).setHeader(getTranslation("table.column.eve_corporation_id")).setAutoWidth(true);
        grid.addColumn(ManagedCorporation::getCorporationName).setHeader(getTranslation("table.column.nomination")).setAutoWidth(true);
        grid.addColumn(ManagedCorporation::getCorporationTicker).setHeader(getTranslation("table.column.ticker")).setAutoWidth(true);
        grid.addColumn(ManagedCorporation::getCreatedBy).setHeader(getTranslation("table.column.created_by")).setAutoWidth(true);
        grid.addColumn(value -> value.getCreatedDate() == null ? "" : value.getCreatedDate().toString())
            .setHeader(getTranslation("table.column.created_date"))
            .setAutoWidth(true);
        return grid;
    }

    private void saveCorporation() {
        try {
            controller.addCorporation(corporationNameField.getValue());
            clearForm();
            refreshGrid();
            VaadinUtils.showNotification(getTranslation("message.notification.corporation_added"));
        } catch (ValidationException e) {
            VaadinUtils.showNotification(getTranslation(e.getMessage()));
        } catch (Exception e) {
            VaadinUtils.showNotification(getTranslation("message.notification.corporation_add_failed"));
        }
    }

    private void clearForm() {
        corporationNameField.clear();
    }

    private void refreshGrid() {
        dataProvider = new ListDataProvider<>(controller.getMyCorporations());
        grid.setItems(dataProvider);
        dataProvider.refreshAll();
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        corporationNameField.setLabel(getTranslation("corporation.form.name"));
        addButton.setText(getTranslation("corporation.button.add"));
        refreshButton.setText(getTranslation("button.app.refresh"));
        grid.getColumns().get(0).setHeader(getTranslation("table.column.eve_corporation_id"));
        grid.getColumns().get(1).setHeader(getTranslation("table.column.nomination"));
        grid.getColumns().get(2).setHeader(getTranslation("table.column.ticker"));
        grid.getColumns().get(3).setHeader(getTranslation("table.column.created_by"));
        grid.getColumns().get(4).setHeader(getTranslation("table.column.created_date"));
    }
}
