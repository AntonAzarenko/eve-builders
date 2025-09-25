package com.azarenka.evebuilders.main.menu;

import com.azarenka.evebuilders.common.util.BuilderPermission;
import com.azarenka.evebuilders.component.NavigationParentViewWithTabs;
import com.azarenka.evebuilders.component.NavigationTab;
import com.azarenka.evebuilders.domain.db.Role;
import com.azarenka.evebuilders.main.MainWidget;
import com.azarenka.evebuilders.main.staff.StaffDashboard;
import com.azarenka.evebuilders.main.staff.StaffProperties;
import com.azarenka.evebuilders.main.staff.fleet.StaffFleetActivityDashboard;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.ParentLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RoutePrefix;

import jakarta.annotation.security.RolesAllowed;

@RoutePrefix("staff")
@Route("")
@RolesAllowed({"ROLE_ADMIN", "ROLE_SUPER_ADMIN", "ROLE_CEO"})
@PageTitle("Staff")
@ParentLayout(MainWidget.class)
public class MenuStaffPage extends NavigationParentViewWithTabs implements LocaleChangeObserver {

    public MenuStaffPage() {
        addTabIfAllowed(getTranslation("tab.manager.dashboard"), StaffDashboard.class,
            new Role[]{Role.ROLE_SUPER_ADMIN, Role.ROLE_ADMIN}, VaadinIcon.CROSSHAIRS.create(),
            "tab-manager.dashboard");
        addTabIfAllowed(getTranslation("tab.manager.properties"), StaffProperties.class,
            new Role[]{Role.ROLE_SUPER_ADMIN, Role.ROLE_ADMIN}, VaadinIcon.FOLDER.create(), "tab-manager.properties");
        addTabIfAllowed(getTranslation("tab.staff.fleet_avtivity"), StaffFleetActivityDashboard.class,
            new Role[]{Role.ROLE_SUPER_ADMIN, Role.ROLE_ADMIN, Role.ROLE_CEO}, VaadinIcon.FEMALE.create(),
            "tab-staff-fleet_avtivity");
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        Tabs tabs = getTabs();
        if (BuilderPermission.hasAdminPermission()) {
            ((NavigationTab) tabs.getTabAt(0)).updateLabel(getTranslation("tab.manager.dashboard"),
                VaadinIcon.CROSSHAIRS.create());
            ((NavigationTab) tabs.getTabAt(1)).updateLabel(getTranslation("tab.manager.properties"),
                VaadinIcon.FOLDER.create());
        }
        if (BuilderPermission.hasAdminPermission() || BuilderPermission.hasCeoPermission()) {
            ((NavigationTab) tabs.getTabAt(2)).updateLabel(getTranslation("tab.staff.fleet_avtivity"),
                VaadinIcon.FEMALE.create());
        }
    }
}
