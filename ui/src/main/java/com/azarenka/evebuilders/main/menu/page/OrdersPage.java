package com.azarenka.evebuilders.main.menu.page;

import com.azarenka.evebuilders.component.NavigableParentView;
import com.azarenka.evebuilders.main.MainWidget;
import com.azarenka.evebuilders.main.menu.MenuOrdersView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.router.ParentLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RoutePrefix;
import jakarta.annotation.security.PermitAll;

@RoutePrefix("orders")
@Route("")
@PermitAll
@ParentLayout(MainWidget.class)
public class OrdersPage extends NavigableParentView {

    @Override
    protected Class<? extends Component> getDefaultChildView() {
        return MenuOrdersView.class;
    }
}
