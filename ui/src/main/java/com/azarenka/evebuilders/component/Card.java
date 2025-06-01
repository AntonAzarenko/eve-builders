package com.azarenka.evebuilders.component;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;

import java.util.Objects;

public class Card extends Div {

    private final Image icon;
    private Span cardTitle;
    private Span description;

    public Card(String pathIcon) {
        this(pathIcon, null);
    }

    public Card(String pathIcon, String name) {
        this(name, pathIcon, null);
    }

    public Card(String name, String pathIcon, String description) {
        icon = new Image(pathIcon, "Icon");
        initContent(name, description);
    }

    private void initContent(String name, String description) {
        addClassName("card");
        icon.addClassName("card-icon");
        add(icon);
        if (Objects.nonNull(name)) {
            cardTitle = new Span(name);
            cardTitle.addClassName("card-title");
            add(cardTitle);
        }

        if (Objects.nonNull(description)) {
            this.description = new Span(description);
            this.description.addClassName("card-description");
            add(description);
        }
    }

    public Span getCardTitle() {
        return cardTitle;
    }

    public Span getDescription() {
        return description;
    }
}
