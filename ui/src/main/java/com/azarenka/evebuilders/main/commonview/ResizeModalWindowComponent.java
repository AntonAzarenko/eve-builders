package com.azarenka.evebuilders.main.commonview;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.VaadinIcon;

public class ResizeModalWindowComponent {

    private Button resizeButton;
    private boolean isFullscreen = false;
    private String defaultHeight;
    private String defaultWidth;
    private Dialog dialog;

    public ResizeModalWindowComponent(Dialog dialog) {
        this.dialog = dialog;
        createResizeButton();
    }

    public void setFullscreen(boolean fullscreen) {
        isFullscreen = fullscreen;
    }

    public Button getResizeButton() {
        return resizeButton;
    }

    public void resizeComponent() {
        if (isFullscreen) {
            dialog.setWidth(defaultWidth);
            dialog.setHeight(defaultHeight);
        } else {
            defaultHeight = dialog.getHeight();
            defaultWidth = dialog.getWidth();
            dialog.setSizeFull();
        }
        isFullscreen = !isFullscreen;
    }

    private Button createResizeButton() {
        resizeButton = new Button(VaadinIcon.SQUARE_SHADOW.create(), event -> this.resizeComponent());
        resizeButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ICON);
        return resizeButton;
    }
}
