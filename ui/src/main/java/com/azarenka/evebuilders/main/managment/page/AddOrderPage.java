package com.azarenka.evebuilders.main.managment.page;

import com.azarenka.evebuilders.component.NavigableParentView;
import com.azarenka.evebuilders.main.managment.create.CreateOrderView;
import com.azarenka.evebuilders.main.menu.MenuManagerPage;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.ParentLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RoutePrefix;
import jakarta.annotation.security.RolesAllowed;

@RolesAllowed({"ROLE_ADMIN", "ROLE_SUPER_ADMIN"})
@RoutePrefix("create")
@PageTitle("Create")
@Route("")
@ParentLayout(MenuManagerPage.class)
public class AddOrderPage extends NavigableParentView {

    @Override
    protected Class<? extends Component> getDefaultChildView() {
        return CreateOrderView.class;
    }
}
