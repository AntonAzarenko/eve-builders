package com.azarenka.evebuilders.main.request;

import com.azarenka.evebuilders.component.View;
import com.azarenka.evebuilders.main.menu.MenuRequestCenterPage;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "requests", layout = MenuRequestCenterPage.class)
@RolesAllowed({"ROLE_ADMIN", "ROLE_SUPER_ADMIN"})
public class RequestsView extends View {

    public RequestsView() {
        add(new Span("Requests"));
    }
}
