package com.azarenka.evebuilders.main.menu;

import com.azarenka.evebuilders.component.NavigationParentViewWithTabs;
import com.azarenka.evebuilders.component.NavigationTab;
import com.azarenka.evebuilders.main.MainWidget;
import com.azarenka.evebuilders.main.staff.StaffDashboard;
import com.azarenka.evebuilders.main.staff.StaffProperties;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.ParentLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RoutePrefix;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;

@RoutePrefix("staff")
@Route("")
@RolesAllowed({"ROLE_ADMIN", "ROLE_SUPER_ADMIN"})
@PageTitle("Staff")
@ParentLayout(MainWidget.class)
public class MenuStaffPage extends NavigationParentViewWithTabs implements LocaleChangeObserver {

    public MenuStaffPage() {
        addView(StaffDashboard.class, getTranslation("tab.manager.dashboard"), VaadinIcon.CROSSHAIRS.create());
        addView(StaffProperties.class, getTranslation("tab.manager.properties"), VaadinIcon.FOLDER.create());
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        Tabs tabs = getTabs();
        ((NavigationTab) tabs.getTabAt(0)).updateLabel(getTranslation("tab.manager.dashboard"),
                VaadinIcon.CROSSHAIRS.create());
        ((NavigationTab) tabs.getTabAt(1)).updateLabel(getTranslation("tab.manager.properties"),
                VaadinIcon.FOLDER.create());
    }
}
