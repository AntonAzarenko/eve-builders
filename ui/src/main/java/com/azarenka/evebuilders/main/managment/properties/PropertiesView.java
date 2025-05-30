package com.azarenka.evebuilders.main.managment.properties;

import com.azarenka.evebuilders.component.View;
import com.azarenka.evebuilders.main.menu.MenuManagerPageView;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "properties", layout = MenuManagerPageView.class)
@RolesAllowed({"ROLE_ADMIN", "ROLE_SUPER_ADMIN"})
public class PropertiesView extends View {
}
