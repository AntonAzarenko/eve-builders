package com.azarenka.evebuilders.main;

import com.azarenka.evebuilders.service.api.IEveAuthService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("login")
@PageTitle("Login")
@AnonymousAllowed
public class LoginView extends AppLayout {

    private final Button authButton = new Button("Login with EVE Online");
    private IEveAuthService eveAuthService;

    public LoginView(IEveAuthService eveAuthService) {
        this.eveAuthService = eveAuthService;
        var layout = new VerticalLayout();
        layout.addClassName("fullscreen-image");
        layout.setSizeFull();
        layout.setHeight("100%");
        layout.add(initLoginLayout());
        layout.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.setPadding(false);
        layout.setSpacing(false);
        setContent(layout);
    }

    private VerticalLayout initLoginLayout() {
        authButton.addClickListener(event -> UI.getCurrent().getPage().setLocation(eveAuthService.generateAuthUrl()));
        authButton.setWidthFull();
        Span description = new Span("""
                Application for managing construction.
                """);
        Image image = new Image("/themes/builders/img/holdmyprobs_logo.jpg", "asd");
        image.setWidth("64px");
        VerticalLayout div = new VerticalLayout();
        div.setWidth("400px");
        div.setHeight("300px");
        Span header = new Span("Hold My Probs");
        header.getStyle().setFontWeight(900);
        VerticalLayout layout = new VerticalLayout();
        layout.add(header, image, description, authButton);
        layout.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.getStyle().set("background", "white");
        div.add(layout);
        return div;
    }
}
