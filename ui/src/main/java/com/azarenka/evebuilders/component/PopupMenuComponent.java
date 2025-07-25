package com.azarenka.evebuilders.component;

import static com.vaadin.flow.component.Shortcuts.addShortcutListener;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.List;

public class PopupMenuComponent extends VerticalLayout {

    private final Button openMenuButton;
    private Button applyButton;
    private final ComponentEventListener<ClickEvent<Button>> listener;
    private final List<Component> contentComponents;
    private Icon closeIcon;
    private final String tooltip;
    private final String title;

    public PopupMenuComponent(String title, String tooltip,
                              ComponentEventListener<ClickEvent<Button>> clickListener,
                              List<Component> contentComponents,
                              VaadinIcon openIcon) {
        this.listener = clickListener;
        this.tooltip = tooltip;
        this.contentComponents = contentComponents;
        this.title = title;

        setWidth("350px");
        setSpacing(false);
        setMargin(false);
        addClassName("material-popup");
        getStyle().set("position", "absolute"); // чтобы можно было задавать top/left с клиента

        openMenuButton = new Button(openIcon.create());

        super.setVisible(false);
        initContent();
    }

    private void initContent() {
        initApplyButton();
        initHeader();
        initBodyAndFooter();
        initOpenButton();
        addEscClose();
    }

    private void initHeader() {
        Span titleSpan = new Span(title);
        titleSpan.addClassName("material-popup__title");

        closeIcon = new Icon(VaadinIcon.CLOSE_SMALL);
        closeIcon.addClassName("popup-close-button");
        closeIcon.addClickListener(e -> close());

        HorizontalLayout header = new HorizontalLayout(titleSpan, closeIcon);
        header.addClassName("material-popup__header");
        header.setWidthFull();
        header.setAlignItems(Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        add(header);
    }

    private void initApplyButton() {
        applyButton = new Button("Применить", VaadinIcon.CHECK.create());
        applyButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        applyButton.addClickListener(listener);
        applyButton.addClickListener(event -> super.setVisible(false));
    }

    private void initOpenButton() {
        openMenuButton.setTooltipText(tooltip);
        openMenuButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY_INLINE);

        openMenuButton.addClickListener(e -> {
            if (!super.isVisible()) {
                super.setVisible(true);
                getUI().ifPresent(ui -> {
                    if (!super.getParent().isPresent()) {
                        ui.add(this);
                    }
                });
                openMenuButton.getElement().executeJs("""
                    const btn = this;
                    const popup = $0;
                    const rect = btn.getBoundingClientRect();
                    
                    popup.style.position = 'absolute';
                    popup.style.top = `${rect.bottom + window.scrollY}px`;
                    popup.style.left = `${rect.left + window.scrollX}px`;
                    
                    const outsideClickListener = (event) => {
                        if (!popup.contains(event.target) && !btn.contains(event.target)) {
                            popup.$server.closePopupFromClient();
                            document.removeEventListener('click', outsideClickListener);
                        }
                    };
                    
                    document.addEventListener('click', outsideClickListener);
                    """, super.getElement());
            } else {
                super.setVisible(false);
                super.getStyle().remove("top");
                super.getStyle().remove("left");
            }
        });
    }

    private void initBodyAndFooter() {
        VerticalLayout body = new VerticalLayout();
        body.setPadding(false);
        body.setSpacing(true);
        body.setWidthFull();

        contentComponents.forEach(c -> {
            c.getElement().getStyle().set("width", "100%");
            body.add(c);
        });

        add(body);

        Hr divider = new Hr();
        divider.addClassName("material-popup__divider");
        add(divider);

        HorizontalLayout footer = new HorizontalLayout(applyButton);
        footer.setWidthFull();
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        footer.setPadding(false);
add(footer);
        //super.add(body, footer);
    }

    private void addEscClose() {
        getElement().addEventListener("keydown", e -> close())
            .setFilter("event.key === 'Escape'");
        //addShortcutListener(this::close, Key.ESCAPE);
    }

    public Button getOpenMenuButton() {
        return openMenuButton;
    }

    @ClientCallable
    public void closePopupFromClient() {
        close();
    }

    private void close() {
        setVisible(false);
        getStyle().remove("top");
        getStyle().remove("left");
    }
}
