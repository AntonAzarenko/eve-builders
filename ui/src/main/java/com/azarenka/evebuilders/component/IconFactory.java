package com.azarenka.evebuilders.component;

import com.vaadin.flow.component.icon.AbstractIcon;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.icon.VaadinIcon;

import org.vaadin.lineawesome.LineAwesomeIcon;

public class IconFactory {

    public static SvgIcon lineAwesome(LineAwesomeIcon lineAwesomeIcon) {
        return lineAwesome(lineAwesomeIcon, "var(--lumo-icon-size-m)", "var(--lumo-body-text-color)");
    }

    public static SvgIcon lineAwesome(LineAwesomeIcon lineAwesomeIcon, String fontSize, String color) {
        SvgIcon svgIcon = lineAwesomeIcon.create();
        svgIcon.setSize("24px");
        svgIcon.setColor(color);
        return svgIcon;
    }

    public static Icon font(VaadinIcon font) {
        return font(font, "var(--lumo-icon-size-m)", "var(--lumo-body-text-color)");
    }

    public static Icon font(VaadinIcon font, String fontSize, String color) {
        Icon svgIcon = font.create();
        svgIcon.getElement().removeAttribute("icon-size");

        svgIcon.setSize("24px");
        svgIcon.setColor(color);
        svgIcon.getStyle().set("width", "1.5rem !important");
        svgIcon.getStyle().set("height", "1.5rem !important");
        return svgIcon;
    }
}
