package com.azarenka.evebuilders.main.menu;

import com.azarenka.evebuilders.common.util.BuilderPermission;
import com.azarenka.evebuilders.component.NavigationParentViewWithTabs;
import com.azarenka.evebuilders.component.NavigationTab;
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

import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;

@RoutePrefix("staff")
@Route("")
@PreAuthorize("@accessControlSecurity.canAny('CORPORATION_VIEW','CORPORATION_CONTRACT_VIEW','CORPORATION_CONTRACT_EDIT')")
@PageTitle("Staff")
@ParentLayout(MainWidget.class)
public class MenuStaffPage extends NavigationParentViewWithTabs implements LocaleChangeObserver {

    public MenuStaffPage() {
        addTabIfAllowed(getTranslation("tab.manager.dashboard"), StaffDashboard.class,
            VaadinIcon.CROSSHAIRS.create(), "tab-manager.dashboard",
            "DASHBOARD_VIEW");
        addTabIfAllowed(getTranslation("tab.manager.properties"), StaffProperties.class,
            VaadinIcon.FOLDER.create(), "tab-manager.properties",
            "CORPORATION_CONTRACT_EDIT");
        addTabIfAllowed(getTranslation("tab.staff.fleet_avtivity"), StaffFleetActivityDashboard.class,
            VaadinIcon.FEMALE.create(), "tab-staff-fleet_activity",
            "DASHBOARD_VIEW");
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        Map<Class<?>, NavigationTab> tabMap = getTabMap();
        if (BuilderPermission.hasAdminPermission()) {
            tabMap.get(StaffDashboard.class).updateLabel(getTranslation("tab.manager.dashboard"),
                VaadinIcon.CROSSHAIRS.create());
            tabMap.get(StaffProperties.class).updateLabel(getTranslation("tab.manager.properties"),
                VaadinIcon.FOLDER.create());
        }
        if (BuilderPermission.hasAdminPermission() || BuilderPermission.hasCeoPermission()) {
            tabMap.get(StaffFleetActivityDashboard.class).updateLabel(getTranslation("tab.staff.fleet_avtivity"),
                VaadinIcon.FEMALE.create());
        }
    }
}
