package com.azarenka.evebuilders.component;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;

import java.util.ArrayList;
import java.util.List;

public class PopupMenuBuilder {

    private final List<Component> components = new ArrayList<>();
    private String tooltip = "";
    private VaadinIcon icon = VaadinIcon.ELLIPSIS_DOTS_V;
    private ComponentEventListener<ClickEvent<Button>> listener = event -> {};
    private String title = "";

    public PopupMenuBuilder withComponent(Component component) {
        components.add(component);
        return this;
    }

    public PopupMenuBuilder withTitle(String title) {
        this.title = title;
        return this;
    }

    public PopupMenuBuilder withTooltip(String tooltip) {
        this.tooltip = tooltip;
        return this;
    }

    public PopupMenuBuilder withIcon(VaadinIcon icon) {
        this.icon = icon;
        return this;
    }

    public PopupMenuBuilder onApply(ComponentEventListener<ClickEvent<Button>> listener) {
        this.listener = listener;
        return this;
    }

    public PopupMenuComponent build() {
        return new PopupMenuComponent(title, tooltip, listener, components, icon);
    }
}
