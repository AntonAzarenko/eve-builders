package com.azarenka.evebuilders.component;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;

public class SearchComponent extends TextField {

    private final ComponentEventListener<ClickEvent<Button>> listener;
    private final ComponentEventListener<ClickEvent<Button>> eraseListener;

    public SearchComponent(String placeholder, ComponentEventListener<ClickEvent<Button>> listener,
                           ComponentEventListener<ClickEvent<Button>> eraseListener) {
        super();
        this.setPlaceholder(placeholder);
        this.listener = listener;
        this.eraseListener = eraseListener;
        initComponent();
    }

    public String getValue() {
        return super.getValue();
    }

    public void clearText() {
        clear();
    }

    public void setPlaceholder(String placeHolder) {
        super.setPlaceholder(placeHolder);
    }

    private void initComponent() {
        setWidth("50%");
        Button searchButton = new Button(VaadinIcon.SEARCH.create());
        searchButton.setMaxWidth("23px");
        searchButton.setMaxHeight("23px");
        searchButton.addClickListener(listener);

        Button eraseButton = new Button(VaadinIcon.CLOSE.create());
        eraseButton.setMaxWidth("23px");
        eraseButton.setMaxHeight("23px");
        eraseButton.addClickListener(eraseListener);
        addToSuffix(searchButton, eraseButton);
        addThemeVariants(TextFieldVariant.LUMO_SMALL);
        addKeyPressListener(Key.ENTER, event -> searchButton.click());
    }
}
