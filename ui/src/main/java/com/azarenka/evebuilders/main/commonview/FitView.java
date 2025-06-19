package com.azarenka.evebuilders.main.commonview;

import com.azarenka.evebuilders.common.util.BuilderPermission;
import com.azarenka.evebuilders.domain.db.Fit;
import com.azarenka.evebuilders.service.api.IFitLoaderService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.value.ValueChangeMode;

public class FitView extends CommonDialogComponent {

    private final FitComponent fitComponent;
    private final Fit fit;
    private Button editButton;
    private Button copyButton;
    private Button revertButton;
    private IFitLoaderService fitLoaderService;

    public FitView(Fit fit, IFitLoaderService fitLoaderService) {
        this.fitLoaderService = fitLoaderService;
        super.setHeaderTitle(fit.getId());
        super.applyCommonProperties("fit-window", true);
        this.fit = fit;
        this.fitComponent = new FitComponent(fit);
        init();
    }

    private void init() {
        initEditButton();
        initCopyButton();
        initRevertButton();
        add(fitComponent);
        getFooter().add(revertButton, editButton, copyButton, createCloseButton());
        updateButtonsStatus();
    }

    private void initEditButton() {
        editButton = new Button(VaadinIcon.EDIT.create());
        editButton.setVisible(BuilderPermission.hasEditFitPermission());
        editButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_SMALL);
        editButton.setTooltipText(getTranslation("message.button.tooltip.save_edit_fit"));
        editButton.addClickListener(event -> {
            fit.setTextFit(fitComponent.getFitTextArea().getValue());
            updateButtonsStatus();
            fitLoaderService.updateFit(fit);
        });
        TextArea fitTextArea = fitComponent.getFitTextArea();
        fitTextArea.addValueChangeListener(e -> {
            fitComponent.setEditedText(fitComponent.getFitTextArea().getValue());
            updateButtonsStatus();
        });
        fitTextArea.setValueChangeMode(ValueChangeMode.LAZY);
    }

    private void initRevertButton() {
        revertButton = new Button(VaadinIcon.BACKWARDS.create());
        revertButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_SMALL);
        revertButton.setTooltipText(getTranslation("message.button.tooltip.revert_fit"));
        revertButton.addClickListener(e -> {
            fitComponent.getFitTextArea().setValue(fit.getTextFit());
            fitComponent.setEditedText(fit.getTextFit());
        });
    }

    private void initCopyButton() {
        copyButton = new Button(VaadinIcon.COPY.create());
        copyButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_SMALL);
        copyButton.setTooltipText(getTranslation("message.button.tooltip.copy"));
        copyButton.addClickListener(e -> {
            copyButton.getElement().executeJs("navigator.clipboard.writeText($0);", fit.getTextFit());
            Notification.show("Текст скопирован в буфер обмена", 2000, Notification.Position.MIDDLE);
        });
    }

    private void updateButtonsStatus() {
        editButton.setEnabled(!fit.getTextFit().equals(fitComponent.getEditedText()));
        revertButton.setEnabled(!fit.getTextFit().equals(fitComponent.getEditedText()));
    }
}
