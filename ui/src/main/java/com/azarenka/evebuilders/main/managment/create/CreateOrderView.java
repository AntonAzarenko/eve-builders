package com.azarenka.evebuilders.main.managment.create;

import com.azarenka.evebuilders.component.View;
import com.azarenka.evebuilders.main.managment.api.ICreateOrderController;
import com.azarenka.evebuilders.main.managment.page.AddOrderPage;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "default", layout = AddOrderPage.class)
@RolesAllowed({"ROLE_ADMIN", "ROLE_SUPER_ADMIN"})
@PermitAll
public class CreateOrderView extends View {

    private final ICreateOrderController controller;

    public CreateOrderView(ICreateOrderController controller) {
        this.controller = controller;
        super.setPadding(true);
        initSplitLayout();
        add(initSplitLayout());
    }

    private SplitLayout initSplitLayout() {
        SplitLayout splitLayout = new SplitLayout();
        splitLayout.setSizeFull();
        splitLayout.addToPrimary(new ParametersOrderView(controller));
        splitLayout.setSplitterPosition(50);
        splitLayout.addToSecondary(new ExistingOrdersView(controller));
        return splitLayout;
    }
}
