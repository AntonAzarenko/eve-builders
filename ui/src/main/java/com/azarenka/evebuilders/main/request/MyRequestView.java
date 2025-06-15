package com.azarenka.evebuilders.main.request;

import com.azarenka.evebuilders.component.View;
import com.azarenka.evebuilders.main.menu.MenuRequestCenterPage;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "my-requests", layout = MenuRequestCenterPage.class)
@RolesAllowed({"ROLE_COORDINATOR"})
public class MyRequestView  extends View {
    public MyRequestView() {
        add(new Span("My Requests"));
    }
}
