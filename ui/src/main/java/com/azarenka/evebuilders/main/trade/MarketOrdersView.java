package com.azarenka.evebuilders.main.trade;

import com.azarenka.evebuilders.component.View;
import com.azarenka.evebuilders.main.menu.MenuTradePage;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.RolesAllowed;

@Route(value = "orders", layout = MenuTradePage.class)
@RolesAllowed({"ROLE_SUPER_ADMIN", "ROLE_MINER", "ROLE_ADMIN", "ROLE_BUILDER"})
@PageTitle("MARKET Orders")
public class MarketOrdersView extends View {

    public MarketOrdersView() {
        add(new Div("Market Orders"));
    }
}
