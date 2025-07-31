package com.azarenka.evebuilders.component.exception;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.server.ErrorEvent;

public class IndustryErrorHandler extends CommonErrorHandler {

    @Override
    protected Dialog initErrorWindow(ErrorEvent event) {
        return super.initErrorWindow(event);
    }
}
