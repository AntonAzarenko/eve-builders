package com.azarenka.evebuilders.main.menu;

import com.azarenka.evebuilders.component.NavigableParentView;
import com.azarenka.evebuilders.main.MainWidget;
import com.azarenka.evebuilders.main.statistic.MenuStatisticView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.ParentLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RoutePrefix;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;

@RoutePrefix("statistic")
@Route("")
@PermitAll
@ParentLayout(MainWidget.class)
public class MenuStatisticPage extends NavigableParentView {

    @Override
    protected Class<? extends Component> getDefaultChildView() {
        return MenuStatisticView.class;
    }
}
