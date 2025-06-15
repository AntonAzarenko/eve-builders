package com.azarenka.evebuilders.main.constructions;

import com.azarenka.evebuilders.component.View;
import com.azarenka.evebuilders.main.menu.MenuConstructionPage;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "personal", layout = MenuConstructionPage.class)
@RolesAllowed({"ROLE_ADMIN", "ROLE_SUPER_ADMIN", "ROLE_USER"})
public class PersonalConstructionView extends View {

    public PersonalConstructionView() {
        add(new H2("Personal Orders"));
    }
}
