package com.azarenka.evebuilders.component;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.RouterLayout;

public class View extends VerticalLayout implements BeforeEnterObserver {

    public View() {
        setSpacing(false);
        setPadding(false);
        setMargin(false);
        setSizeFull();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {

    }
}
