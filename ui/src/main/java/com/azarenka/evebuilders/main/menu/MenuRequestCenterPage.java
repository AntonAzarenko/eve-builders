package com.azarenka.evebuilders.main.menu;

import com.azarenka.evebuilders.common.util.BuilderPermission;
import com.azarenka.evebuilders.component.NavigationParentViewWithTabs;
import com.azarenka.evebuilders.component.NavigationTab;
import com.azarenka.evebuilders.domain.db.Role;
import com.azarenka.evebuilders.main.MainWidget;
import com.azarenka.evebuilders.main.request.create.CreateRequestView;
import com.azarenka.evebuilders.main.request.coordinator.requests.CoordinatorRequestsView;
import com.azarenka.evebuilders.main.request.admin.RequestsView;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.ParentLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RoutePrefix;
import jakarta.annotation.security.RolesAllowed;

import java.util.Map;

@RoutePrefix("request-center")
@Route("")
@RolesAllowed({"ROLE_COORDINATOR", "ROLE_ADMIN", "ROLE_SUPER_ADMIN"})
@PageTitle("Request Center")
@ParentLayout(MainWidget.class)
public class MenuRequestCenterPage extends NavigationParentViewWithTabs implements LocaleChangeObserver {

    public MenuRequestCenterPage() {
        addTabIfAllowed(getTranslation("tab.request.create_request"), CreateRequestView.class,
                new Role[]{Role.ROLE_COORDINATOR}, VaadinIcon.RECORDS.create());
        addTabIfAllowed(getTranslation("tab.request.my_request"), CoordinatorRequestsView.class,
                new Role[]{Role.ROLE_COORDINATOR}, VaadinIcon.HOME_O.create());
        addTabIfAllowed(getTranslation("tab.request.group_request"), RequestsView.class,
                new Role[]{Role.ROLE_SUPER_ADMIN, Role.ROLE_ADMIN}, VaadinIcon.LINES_LIST.create());
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        Map<Class<?>, NavigationTab> tabMap = getTabMap();
        if (BuilderPermission.hasCoordinatorPermission()) {
            tabMap.get(CreateRequestView.class).updateLabel(getTranslation("tab.request.create_request"),
                    VaadinIcon.RECORDS.create());
            tabMap.get(CoordinatorRequestsView.class).updateLabel(getTranslation("tab.request.my_request"),
                    VaadinIcon.HOME_O.create());
        }
        if (BuilderPermission.hasAdminPermission()) {
            tabMap.get(RequestsView.class).updateLabel(getTranslation("tab.request.group_request"),
                    VaadinIcon.LINES_LIST.create());
        }
    }
}
