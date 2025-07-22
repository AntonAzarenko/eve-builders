package com.azarenka.evebuilders.component;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.List;

public class PopupMenuComponent extends VerticalLayout {

    private final Button openMenuButton;
    private Button applyButton;
    private final ComponentEventListener<ClickEvent<Button>> listener;
    private List<Component> contentComponents;
    private Icon closeIcon;
    private String tooltip;

    public PopupMenuComponent(String tooltip, ComponentEventListener<ClickEvent<Button>> clickListener,
                              List<Component> contentComponents,
                              VaadinIcon openIcon) {
        this.listener = clickListener;
        this.tooltip = tooltip;
        this.contentComponents = contentComponents;
        setWidth("400px");
        openMenuButton = new Button(openIcon.create());
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
                    
                    const outsideClickListener = (event) => {
                        if (!popup.contains(event.target) && !btn.contains(event.target)) {
                            popup.$server.closePopupFromClient();
                            document.removeEventListener('click', outsideClickListener);
                        }
                    };
                    
                    document.addEventListener('click', outsideClickListener);
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
        HorizontalLayout contentLayout = new HorizontalLayout();
        contentLayout.setAlignItems(FlexComponent.Alignment.END);
        contentComponents.forEach(contentLayout::add);
        contentLayout.add(applyButton);
        super.add(contentLayout);
    }

    public Button getOpenMenuButton() {
        return openMenuButton;
    }

    @ClientCallable
    public void closePopupFromClient() {
        setVisible(false);
        getStyle().remove("top");
        getStyle().remove("left");
    }
}
