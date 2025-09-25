package com.azarenka.evebuilders.main.staff.fleet;

import com.azarenka.evebuilders.component.NavigationParentViewWithTabs;
import com.azarenka.evebuilders.component.NavigationTab;
import com.azarenka.evebuilders.main.menu.MenuStaffPage;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.ParentLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RoutePrefix;

import org.vaadin.lineawesome.LineAwesomeIcon;

import jakarta.annotation.security.RolesAllowed;

@RoutePrefix("fleet")
@Route("")
@RolesAllowed({"ROLE_ADMIN", "ROLE_SUPER_ADMIN", "ROLE_CEO"})
@ParentLayout(MenuStaffPage.class)
public class StaffFleetActivityDashboard extends NavigationParentViewWithTabs implements LocaleChangeObserver {

    public StaffFleetActivityDashboard() {
        addView(FleetStatistic.class, getTranslation("menu.tab.statistic"), LineAwesomeIcon.CHART_BAR.create(),
            "tab-dashboard");
    }

    @Override
    public void localeChange(LocaleChangeEvent localeChangeEvent) {
        Tabs tabs = getTabs();
        ((NavigationTab) tabs.getTabAt(0)).updateLabel(getTranslation("menu.tab.statistic"),
            LineAwesomeIcon.CHART_BAR.create());

    }
}
