package com.azarenka.evebuilders.main.menu;

import com.azarenka.evebuilders.component.IconFactory;
import com.azarenka.evebuilders.component.NavigationParentViewWithTabs;
import com.azarenka.evebuilders.component.NavigationTab;
import com.azarenka.evebuilders.main.MainWidget;
import com.azarenka.evebuilders.main.orders.api.IOrderViewController;
import com.azarenka.evebuilders.main.orders.corporation.OrdersView;
import com.azarenka.evebuilders.main.orders.myorders.CorporationConstructionsView;
import com.azarenka.evebuilders.main.orders.myorders.PersonalConstructionView;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.ParentLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RoutePrefix;

import org.vaadin.lineawesome.LineAwesomeIcon;

import jakarta.annotation.security.PermitAll;

@RoutePrefix("orders")
@Route("")
@PermitAll
@ParentLayout(MainWidget.class)
public class MenuOrdersPage extends NavigationParentViewWithTabs implements LocaleChangeObserver {

    public MenuOrdersPage(IOrderViewController controller) {
        addView(OrdersView.class, getTranslation("tab.construction.corporation_orders"),
            IconFactory.font(VaadinIcon.BRIEFCASE, "48px", "#00eaff"), "tab-corporation-orders");
        addView(CorporationConstructionsView.class, getTranslation("tab.construction.my_orders"),
            IconFactory.lineAwesome(LineAwesomeIcon.BRIEFCASE_SOLID, "48px", "#00eaff"), "tab-my-orders");
        addView(PersonalConstructionView.class, getTranslation("tab.construction.personal_orders"),
            IconFactory.lineAwesome(LineAwesomeIcon.PERSON_BOOTH_SOLID, "48px", "#00eaff"), "tab-my-orders");
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        Tabs tabs = getTabs();
        ((NavigationTab) tabs.getTabAt(0)).updateLabel(getTranslation("tab.construction.corporation_orders"),
            VaadinIcon.BRIEFCASE.create());
        ((NavigationTab) tabs.getTabAt(1)).updateLabel(getTranslation("tab.construction.my_orders"),
            LineAwesomeIcon.BRIEFCASE_SOLID.create());
        ((NavigationTab) tabs.getTabAt(2)).updateLabel(getTranslation("tab.construction.personal_orders"),
            LineAwesomeIcon.PERSON_BOOTH_SOLID.create());
    }
}
