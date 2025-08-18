package com.azarenka.evebuilders.main.trade;

import com.azarenka.evebuilders.component.View;
import com.azarenka.evebuilders.main.menu.MenuTradePage;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.RolesAllowed;

@Route(value = "market", layout = MenuTradePage.class)
@RolesAllowed({"ROLE_SUPER_ADMIN", "ROLE_BUILDER", "ROLE_MINER", "ROLE_ADMIN"})
@PageTitle("Market-1")
public class MarketView extends View {

    public MarketView() {
        add(new Div("Market"));
    }
}
