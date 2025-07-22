package com.azarenka.evebuilders.component.exception;

import com.azarenka.evebuilders.common.util.VaadinUtils;
import com.azarenka.evebuilders.main.commonview.CommonDialogComponent;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Pre;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import org.apache.commons.lang3.StringUtils;

public class ErrorWindow extends CommonDialogComponent {

    private final String message;
    private final String stackTrace;

    public ErrorWindow(String message, String stackTrace) {
        this.message = message;
        this.stackTrace = stackTrace;
        super.setHeaderTitle("Error");
        super.getHeader().add(createCloseButton());
        super.add(initRootLayout());
        super.setVisible(true);
        super.setResizable(true);
        super.setDraggable(true);
        super.setWidth("500px");
        super.setHeight("200px");
        super.getFooter().add(buildControlsLayout());
    }

    public String getMessage() {
        return message;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    private VerticalLayout initRootLayout() {
        var rootLayout = VaadinUtils.initCommonVerticalLayout();
        var errorMessage = buildErrorMessageLayout();
        rootLayout.add(errorMessage);
        if (StringUtils.isNotBlank(stackTrace)) {
            VerticalLayout errorStackTracePanel = buildErrorStackTracePanel(stackTrace);
            var details = new Button("Show more");
            details.addClickListener(new DetailsButtonClickListener(details, errorStackTracePanel));
            getFooter().add(details);
            rootLayout.setMargin(true);
            rootLayout.add(errorStackTracePanel);
        }
        return rootLayout;
    }

    private HorizontalLayout buildErrorMessageLayout() {
        var errorMessage = new Div(StringUtils.defaultIfBlank(message, "Exception occurred"));
        var horizontalLayout = new HorizontalLayout(errorMessage);
        horizontalLayout.setSizeFull();
        return horizontalLayout;
    }

    private HorizontalLayout buildControlsLayout() {
        var okButton = new com.vaadin.flow.component.button.Button();
        okButton.addClickListener(event -> close());
        var controlPanel = new HorizontalLayout();
        controlPanel.add(okButton);
        return controlPanel;
    }

    private VerticalLayout buildErrorStackTracePanel(String stacktraceValue) {
        var errorStackTrace = new Pre();
        errorStackTrace.add(stacktraceValue);
        var layout = new VerticalLayout(errorStackTrace);
        layout.setVisible(false);
        layout.setSizeFull();
        return layout;
    }

    private class DetailsButtonClickListener implements ComponentEventListener<ClickEvent<Button>> {

        private final Button details;
        private final VerticalLayout stackTracePanel;

        DetailsButtonClickListener(Button detailsButton, VerticalLayout errorStackTracePanel) {
            details = detailsButton;
            stackTracePanel = errorStackTracePanel;
        }

        @Override
        public void onComponentEvent(ClickEvent<Button> event) {
            boolean visible = stackTracePanel.isVisible();
            if (visible) {
                details.setText("Show more");
                setHeight("200px");
                setWidth("500px");
            } else {
                details.setText("Show less");
                setHeight("70%");
                setWidth("80%");
            }
            stackTracePanel.setVisible(!visible);
        }
    }
}
