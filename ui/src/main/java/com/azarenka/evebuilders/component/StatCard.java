package com.azarenka.evebuilders.component;

import com.azarenka.evebuilders.component.View;
import com.vaadin.flow.component.html.Span;

public class StatCard extends View {

    public StatCard(String title, String value, String footer) {
        //addClassName("stat-card");
        getStyle().set("border", "1px solid var(--lumo-contrast-20pct)");
        getStyle().set("border-radius", "12px");
        getStyle().set("padding", "12px");
        getStyle().set("width", "320px");
        getStyle().set("height", "150px");
        getStyle().set("box-shadow", "5px 8px 8px 5px rgb(0 0 0 / 39%)");
        setPadding(true);
        setSpacing(false);
        Span titleSpan = new Span(title);
        Span valueSpan = new Span(value);
        valueSpan.getStyle().set("font-size", "20px");
        valueSpan.getStyle().set("font-weight", "bold");
        Span footerSpan = new Span(footer);
        footerSpan.getStyle()
                .set("font-size", "14px")
                .set("margin-top", "2.5rem")
                .set("color", "gray");
        add(titleSpan, valueSpan, footerSpan);
    }
}
