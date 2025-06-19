package com.azarenka.evebuilders.main.commonview;

import com.azarenka.evebuilders.common.util.VaadinUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.VaadinIcon;

public class CommonDialogComponent extends Dialog {

    public void applyCommonProperties(String componentId, boolean resizable) {
        VaadinUtils.addComponentId(this, componentId);
        this.setResizable(resizable);
        this.setDraggable(true);
        this.setCloseOnEsc(true);
        this.setCloseOnOutsideClick(true);
    }

    public Component createCloseButton() {
        Button button = new Button(VaadinIcon.CLOSE.create(), event -> this.close());
        button.setTooltipText(getTranslation("message.button.tooltip.close"));
        button.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_SMALL);
        return button;
    }
}
