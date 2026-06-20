package com.azarenka.evebuilders.main.menu;

import com.azarenka.evebuilders.component.NavigableParentView;
import com.azarenka.evebuilders.main.MainWidget;
import com.azarenka.evebuilders.main.profile.ProfileView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.router.ParentLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RoutePrefix;

import org.springframework.security.access.prepost.PreAuthorize;

@RoutePrefix("profile")
@Route("")
@PreAuthorize("isAuthenticated()")
@ParentLayout(MainWidget.class)
public class MenuProfilePage extends NavigableParentView {

    @Override
    protected Class<? extends Component> getDefaultChildView() {
        return ProfileView.class;
    }
}
