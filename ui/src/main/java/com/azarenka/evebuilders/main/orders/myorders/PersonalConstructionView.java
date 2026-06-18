package com.azarenka.evebuilders.main.orders.myorders;

import com.azarenka.evebuilders.component.View;
import com.azarenka.evebuilders.main.menu.MenuOrdersPage;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.router.Route;

import org.springframework.security.access.prepost.PreAuthorize;

@Route(value = "personal", layout = MenuOrdersPage.class)
@PreAuthorize("@accessControlSecurity.canAny('DASHBOARD_VIEW','CONTRACTS_ACCEPT','CONTRACTS_DISCARD')")
public class PersonalConstructionView extends View {

    public PersonalConstructionView() {
        add(new H2("Personal Orders"));
    }
}
