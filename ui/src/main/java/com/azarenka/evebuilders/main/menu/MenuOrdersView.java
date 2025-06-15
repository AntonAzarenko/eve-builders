package com.azarenka.evebuilders.main.menu;

import com.azarenka.evebuilders.component.View;
import com.azarenka.evebuilders.main.menu.page.OrdersPage;
import com.azarenka.evebuilders.main.orders.OrdersView;
import com.azarenka.evebuilders.main.orders.api.IOrderViewController;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "main", layout = OrdersPage.class)
@PageTitle("Orders")
@RolesAllowed({"ROLE_USER", "ROLE_ADMIN", "ROLE_SUPER_ADMIN"})
public class MenuOrdersView extends View {

    public MenuOrdersView(IOrderViewController controller) {
        add(new OrdersView(controller));
    }
}
