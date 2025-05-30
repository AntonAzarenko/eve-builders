package com.azarenka.evebuilders.main.commonview;

import com.vaadin.flow.component.html.Span;

public class NotificationWindow extends CommonDialogComponent {

    public NotificationWindow(String title, String message) {
        setHeaderTitle(title);
        add(new Span(message));
        getFooter().add(createCloseButton());
    }
}
