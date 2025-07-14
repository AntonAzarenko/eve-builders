package com.azarenka.evebuilders.main.commonview;

import com.azarenka.evebuilders.common.util.BuilderPermission;
import com.azarenka.evebuilders.common.util.VaadinUtils;
import com.azarenka.evebuilders.domain.db.Fit;
import com.azarenka.evebuilders.service.api.IFitLoaderService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
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

        VerticalLayout verticalLayout = new VerticalLayout();
        Span created = new Span(String.format("Загружено: %s", fit.getCreatedBy()));
        created.getStyle().set("font-size", "10px");
        super.setHeaderTitle(String.format("Fit: %s", fit.getId()));
        add(created);
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
        copyButton.addClickListener(e ->
                VaadinUtils.copyToClipboard(copyButton, fit.getTextFit(), "Фит скопирован в буфер обмена"));
    }

    private void updateButtonsStatus() {
        editButton.setEnabled(!fit.getTextFit().equals(fitComponent.getEditedText()));
        revertButton.setEnabled(!fit.getTextFit().equals(fitComponent.getEditedText()));
    }
}
