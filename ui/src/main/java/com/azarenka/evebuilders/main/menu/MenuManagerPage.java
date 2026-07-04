package com.azarenka.evebuilders.main.menu;

import com.azarenka.evebuilders.component.NavigationParentViewWithTabs;
import com.azarenka.evebuilders.component.NavigationTab;
import com.azarenka.evebuilders.main.MainWidget;
import com.azarenka.evebuilders.main.managment.corporation.CorporationRegistryView;
import com.azarenka.evebuilders.main.managment.dashboard.DashboardView;
import com.azarenka.evebuilders.main.managment.orders.OrdersManagmentView;
import com.azarenka.evebuilders.main.managment.page.AddOrderPage;
import com.azarenka.evebuilders.main.managment.properties.PropertiesView;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.ParentLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RoutePrefix;
import org.springframework.security.access.prepost.PreAuthorize;

@RoutePrefix("manager")
@Route("")
@PreAuthorize("@accessControlSecurity.canAny('CONTRACTS_VIEW','CONTRACTS_CREATE','CONTRACTS_EDIT','CONTRACTS_CANCEL','CORPORATION_VIEW','CORPORATION_CONTRACT_VIEW','CORPORATION_CONTRACT_EDIT')")
@ParentLayout(MainWidget.class)
public class MenuManagerPage extends NavigationParentViewWithTabs implements LocaleChangeObserver {

    public MenuManagerPage() {
        addView(DashboardView.class, getTranslation("tab.manager.dashboard"), VaadinIcon.CROSSHAIRS.create(), "tab-dashboard");
        addView(AddOrderPage.class, getTranslation("tab.manager.add_order"), VaadinIcon.DOCTOR_BRIEFCASE.create(), "tab-orders");
        addView(PropertiesView.class, getTranslation("tab.manager.properties"), VaadinIcon.FOLDER.create(), "tab-properties");
        addView(OrdersManagmentView.class, getTranslation("tab.manager.management"), VaadinIcon.TOOLS.create(), "tab-managment");
        //addView(CorporationRegistryView.class, "Corporations Registry", VaadinIcon.BUILDING.create(), "tab-corporation-registry");
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        Tabs tabs = getTabs();
        ((NavigationTab) tabs.getTabAt(0)).updateLabel(getTranslation("tab.manager.dashboard"),
                VaadinIcon.CROSSHAIRS.create());
        ((NavigationTab) tabs.getTabAt(1)).updateLabel(getTranslation("tab.manager.add_order"),
                VaadinIcon.DOCTOR_BRIEFCASE.create());
        ((NavigationTab) tabs.getTabAt(2)).updateLabel(getTranslation("tab.manager.properties"),
                VaadinIcon.FOLDER.create());
        ((NavigationTab) tabs.getTabAt(3)).updateLabel(getTranslation("tab.manager.management"),
                VaadinIcon.TOOLS.create());
        //((NavigationTab) tabs.getTabAt(4)).updateLabel("Corporations Registry", VaadinIcon.BUILDING.create());
    }
}
