package com.azarenka.evebuilders.component.exception;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.server.DefaultErrorHandler;
import com.vaadin.flow.server.ErrorEvent;

import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.Objects;

public class CommonErrorHandler extends DefaultErrorHandler {

    @Override
    public void error(ErrorEvent event) {
        Dialog errorWindow = Objects.requireNonNull(initErrorWindow(event), "Error window shouldn't be null");
        errorWindow.open();
    }

    protected Dialog initErrorWindow(ErrorEvent event) {
        return new ErrorWindow("Exception occurred", ExceptionUtils.getStackTrace(event.getThrowable()));
    }
}
