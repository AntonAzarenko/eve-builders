package com.azarenka.evebuilders.security;

import com.azarenka.evebuilders.component.View;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("/unauthorized")
@PageTitle("Unauthorized Page")
@AnonymousAllowed
public class UnauthorizedView extends View {

    public UnauthorizedView() {
        add(new Button(VaadinIcon.BACKSPACE.create(), event -> UI.getCurrent().navigate("/login")));
        add(new H1(getTranslation("errors.message.access_denied")));
        add(new Paragraph(getTranslation("errors.message.access_denied.unauthorized.message")));
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
    }
}
