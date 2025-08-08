package com.azarenka.evebuilders.main.request.coordinator.requests;

import com.azarenka.evebuilders.common.util.IGridColumnAdder;
import com.azarenka.evebuilders.common.util.VaadinUtils;
import com.azarenka.evebuilders.domain.db.Fit;
import com.azarenka.evebuilders.main.commonview.CommonDialogComponent;
import com.azarenka.evebuilders.main.commonview.FitView;
import com.azarenka.evebuilders.main.request.api.ICreateRequestController;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;

import org.vaadin.lineawesome.LineAwesomeIcon;

import java.util.Optional;

public class FitManageWindow extends CommonDialogComponent implements IGridColumnAdder<Fit>, LocaleChangeObserver {

    private final ICreateRequestController controller;
    private Grid<Fit> grid;
    private ListDataProvider<Fit> dataProvider;

    private Button removeButton;
    private Button uploadFitButton;
    private Button showFitButton;

    public FitManageWindow(ICreateRequestController controller) {
        super("fit-management-view", true);
        this.controller = controller;
        super.setHeaderTitle("Управление фитами");
        super.setWidth("900px");
        super.setHeight("500px");
        super.getFooter().add(initButtonsLayout());
        initContent();
        updateButtonStatus();
    }

    private HorizontalLayout initButtonsLayout() {
        var horizontalLayout = new HorizontalLayout();
        horizontalLayout.setWidthFull();
        removeButton = VaadinUtils.createLumoButton(LineAwesomeIcon.TRASH_ALT);
        removeButton.addClickListener(e -> {
            Fit fit = grid.getSelectionModel().getFirstSelectedItem().get();
            new ConfirmDialog("Remove item", "Вы Уверены что ходите удалить фит?", "Удалить",
                s -> controller.deleteFit(fit),
                "Отмена", ew -> {
            }).open();
        });
        showFitButton = VaadinUtils.createLumoButton(VaadinIcon.PRESENTATION);
        showFitButton.addClickListener(e -> {
            Fit fit = grid.getSelectionModel().getFirstSelectedItem().get();
            clickFitButton(fit);
        });
        uploadFitButton = VaadinUtils.createLumoButton(VaadinIcon.PLUS);
        uploadFitButton.addClickListener(e -> {
            getUI().ifPresent(ui -> ui.getPage().executeJs(
                "navigator.clipboard.readText().then(text => {" +
                    "   $0.$server.receiveClipboardTextWithProgress(text);" +
                    "}).catch(err => {" +
                    "   console.error('Ошибка чтения из буфера обмена:', err);" +
                    "   $0.$server.notifyError();" +
                    "});",
                getElement()
            ));
        });
        horizontalLayout.add(removeButton, showFitButton, uploadFitButton);
        horizontalLayout.setJustifyContentMode(JustifyContentMode.END);
        return horizontalLayout;
    }

    private void initContent() {
        initGrid();
        add(grid);
    }

    private void initGrid() {
        dataProvider = DataProvider.ofCollection(controller.gitAllFitsByUser());
        grid = VaadinUtils.initGrid(dataProvider, "fit-management-grid");
        addColumns();
        grid.getColumns().forEach(shipOrderDtoColumn -> {
            shipOrderDtoColumn.setSortable(true);
            shipOrderDtoColumn.setResizable(true);
        });
        grid.addItemClickListener(event -> updateButtonStatus());
    }

    private void updateButtonStatus() {
        Optional<Fit> firstSelectedItem = grid.getSelectionModel().getFirstSelectedItem();
        boolean isEnabled = firstSelectedItem.isPresent();
        removeButton.setEnabled(isEnabled);
        showFitButton.setEnabled(isEnabled);
    }

    private void addColumns() {
        addColumn(Fit::getName, "250px", grid);
        addColumn(Fit::getCreatedBy, "150px", grid);
        addColumn(fit -> fit.getCreatedDate().toString(), "150px", grid);
        addColumn(Fit::getUpdatedBy, "150px", grid);
        addColumn(fit -> fit.getUpdatedDate().toString(), "150px", grid);
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        grid.getColumns().get(0).setHeader("Название");
        grid.getColumns().get(1).setHeader("Загружен кем");
        grid.getColumns().get(2).setHeader("Дата загрузки");
        grid.getColumns().get(3).setHeader("Обновлено");
        grid.getColumns().get(4).setHeader("Дата Обновления");
    }

    private void clickFitButton(Fit fit) {
        new FitView(fit, controller.getFitLoaderService()).open();
    }

    @ClientCallable
    public void receiveClipboardTextWithProgress(String text) {
        if (text != null && !text.isEmpty()) {
            boolean upload = controller.uploadFit(text);
            if (!upload) {
                Notification.show(getTranslation("message.notification.fit_can_not_load"),
                    5000, Notification.Position.MIDDLE);
            } else {
                Notification.show(getTranslation("message.notification.fit_loaded"),
                    5000, Notification.Position.MIDDLE);
            }
        } else {
            Notification.show(getTranslation("message.notification.clipboard_empty"), 5000,
                Notification.Position.MIDDLE);
        }
        refresh();
    }

    private void refresh() {
        dataProvider.refreshAll();
        grid.setItems(controller.gitAllFitsByUser());
    }
}
