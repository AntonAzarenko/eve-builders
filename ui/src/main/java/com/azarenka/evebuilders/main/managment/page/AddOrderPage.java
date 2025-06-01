package com.azarenka.evebuilders.main.managment.page;

import com.azarenka.evebuilders.component.NavigableParentView;
import com.azarenka.evebuilders.main.managment.create.CreateOrderView;
import com.azarenka.evebuilders.main.menu.MenuManagerPageView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.router.ParentLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RoutePrefix;
import jakarta.annotation.security.RolesAllowed;

@RolesAllowed("ROLE_ADMIN")
@RoutePrefix("add")
@Route("")
@ParentLayout(MenuManagerPageView.class)
public class AddOrderPage extends NavigableParentView {

    @Override
    protected Class<? extends Component> getDefaultChildView() {
        return CreateOrderView.class;
    }
}
