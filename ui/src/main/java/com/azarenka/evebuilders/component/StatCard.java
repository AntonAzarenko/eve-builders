package com.azarenka.evebuilders.component;

import com.azarenka.evebuilders.component.View;
import com.vaadin.flow.component.html.Span;

public class StatCard extends View {

    public StatCard(String title, String value, String footer) {
        addClassName("stat-card");
        setWidth("200px");
        setPadding(true);
        setSpacing(false);
        Span titleSpan = new Span(title);
        Span valueSpan = new Span(value);
        valueSpan.getStyle().set("font-size", "20px");
        valueSpan.getStyle().set("font-weight", "bold");
        Span footerSpan = new Span(footer);
        footerSpan.getStyle()
                .set("font-size", "14px")
                .set("margin-top", "1.5rem")
                .set("color", "gray");
        add(titleSpan, valueSpan, footerSpan);
    }
}
