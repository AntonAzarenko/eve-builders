package com.azarenka.evebuilders.component;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.IntegerField;

public class PopupMenuComponent extends Div {

    private final Button openMenuButton;
    private Button applyButton;

    private Icon closeIcon;
    private String moduleName;
    private final IntegerField integerField;
    private String tooltip;
    private final ComponentEventListener<ClickEvent<Button>> listener;

    public PopupMenuComponent(String moduleName, IntegerField integerField, VaadinIcon openIcon, String tooltip,
                              ComponentEventListener<ClickEvent<Button>> clickListener) {
        this.listener = clickListener;
        openMenuButton = new Button(openIcon.create());
        this.moduleName = moduleName;
        this.integerField = integerField;
        this.addClassName("material-popup");
        super.setVisible(false);
        initContent();
    }

    private void initContent() {
        initApplyButton();
        initCloseButton();
        initComponent();
        initOpenButton();
    }

    private void initApplyButton() {
        applyButton = new Button(VaadinIcon.CHECK.create());
        applyButton.addClickListener(listener);
        applyButton.addClickListener(event -> super.setVisible(false));
    }

    private void initOpenButton() {
        openMenuButton.setTooltipText(tooltip);
        openMenuButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY_INLINE);
        openMenuButton.addClickListener(e -> {
            if (!super.isVisible()) {
                super.setVisible(true);
                getUI().ifPresent(ui -> {
                    if (!super.getParent().isPresent()) {
                        ui.add(this);
                    }
                });
                openMenuButton.getElement().executeJs("""
                            const btn = this;
                            const popup = $0;
                            const rect = btn.getBoundingClientRect();

                            popup.style.position = 'absolute';
                            popup.style.top = `${rect.bottom + window.scrollY}px`;
                            popup.style.left = `${rect.left + window.scrollX}px`;
                        """, super.getElement());
            } else {
                super.setVisible(false);
                super.getStyle().remove("top");
                super.getStyle().remove("left");
            }
        });
    }

    private void initCloseButton() {
        closeIcon = new Icon(VaadinIcon.CLOSE_SMALL);
        closeIcon.addClassName("popup-close-button");
        closeIcon.addClickListener(e -> super.setVisible(false));
        super.add(closeIcon);
    }

    private void initComponent() {
        integerField.addKeyPressListener(Key.ENTER, keyPressEvent -> {
            super.setVisible(false);
        });
        HorizontalLayout inputRow = new HorizontalLayout(integerField, applyButton);
        inputRow.setAlignItems(FlexComponent.Alignment.END);
        super.add(inputRow);
    }

    public Button getOpenMenuButton() {
        return openMenuButton;
    }
}
