package com.azarenka.evebuilders.main;

import com.azarenka.evebuilders.common.util.BuilderPermission;
import com.azarenka.evebuilders.component.IconFactory;
import com.azarenka.evebuilders.component.NavigationParentViewWithTabs;
import com.azarenka.evebuilders.component.NavigationTab;
import com.azarenka.evebuilders.domain.db.Role;
import com.azarenka.evebuilders.main.menu.MenuConstructionPage;
import com.azarenka.evebuilders.main.menu.MenuManagerPage;
import com.azarenka.evebuilders.main.menu.MenuRequestCenterPage;
import com.azarenka.evebuilders.main.menu.MenuStaffPage;
import com.azarenka.evebuilders.main.menu.MenuTradePage;
import com.azarenka.evebuilders.main.menu.page.OrdersPage;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.ParentLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.UIScope;

import org.vaadin.lineawesome.LineAwesomeIcon;

import java.util.Map;

import jakarta.annotation.security.PermitAll;

@Route(value = "/landing", layout = BuildersApplicationUi.class)
@PermitAll
@ParentLayout(BuildersApplicationUi.class)
@PageTitle("Landing Page")
@UIScope
public class MainWidget extends NavigationParentViewWithTabs implements LocaleChangeObserver {

    private final DrawerToggle drawerToggle = new DrawerToggle();
    private final MainWidgetController controller;
    private int countSubmittedRequests;
    private final int countNewOrders;

    public MainWidget(MainWidgetController controller) {
        this.controller = controller;
        countSubmittedRequests = controller.countRequests();
        countNewOrders = controller.countNewOrders();
        addTabIfAllowedWithBadge(getTranslation("menu.tab.orders"), OrdersPage.class,
            new Role[]{Role.ROLE_ADMIN, Role.ROLE_SUPER_ADMIN, Role.ROLE_BUILDER}, VaadinIcon.HOME.create(),
            countNewOrders, "tab-order-menu");
        addTabIfAllowed(getTranslation("menu.tab.construction"), MenuConstructionPage.class,
            new Role[]{Role.ROLE_ADMIN, Role.ROLE_SUPER_ADMIN, Role.ROLE_BUILDER}, IconFactory.lineAwesome(
                LineAwesomeIcon.INDUSTRY_SOLID), "tab-construction-menu");
        addTabIfAllowed(getTranslation("menu.tab.manger.orders"), MenuManagerPage.class,
            new Role[]{Role.ROLE_ADMIN, Role.ROLE_SUPER_ADMIN}, VaadinIcon.COG.create(), "tab-manager-menu");
        addTabIfAllowedWithBadge(getTranslation("menu.tab.trade"), MenuTradePage.class,
            new Role[]{Role.ROLE_SUPER_ADMIN, Role.ROLE_BUILDER, Role.ROLE_MINER, Role.ROLE_ADMIN},
            LineAwesomeIcon.TRADE_FEDERATION.create(), countSubmittedRequests,
            "tab-trade-menu");
        addTabIfAllowed(getTranslation("menu.tab.personal"), MenuStaffPage.class,
            new Role[]{Role.ROLE_ADMIN, Role.ROLE_SUPER_ADMIN}, VaadinIcon.SPECIALIST.create(), "tab-stuff-menu");
        addTabIfAllowedWithBadge(getTranslation("menu.tab.request"), MenuRequestCenterPage.class,
            new Role[]{Role.ROLE_COORDINATOR, Role.ROLE_ADMIN, Role.ROLE_SUPER_ADMIN}, VaadinIcon.DASHBOARD.create(),
            countSubmittedRequests, "tab-request-menu");
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        BuildersApplicationUi parent = (BuildersApplicationUi) getParent().get();
        Tabs tabs = getTabs();
        tabs.setOrientation(Tabs.Orientation.VERTICAL);
        remove(tabs);
        parent.addToDrawer(tabs);
        drawerToggle.getElement().setAttribute("slot", "navbar");
        parent.getElement().insertChild(0, drawerToggle.getElement());
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        countSubmittedRequests = controller.countRequests();
        Map<Class<?>, NavigationTab> tabMap = getTabMap();
        if (BuilderPermission.hasBuilderPermission() || BuilderPermission.hasAdminPermission()) {
            tabMap.get(OrdersPage.class)
                .updateLabel(getTranslation("menu.tab.orders"), VaadinIcon.HOME.create(), countNewOrders);
            tabMap.get(MenuConstructionPage.class).updateLabel(getTranslation("menu.tab.construction"),
                LineAwesomeIcon.INDUSTRY_SOLID.create());
        }
        if (BuilderPermission.hasBuilderPermission() || BuilderPermission.hasAdminPermission()
            || BuilderPermission.hasMinerPermission()) {
            tabMap.get(MenuTradePage.class).updateLabel(getTranslation("menu.tab.trade"),
                LineAwesomeIcon.TRADE_FEDERATION.create(), 0);
        }
        if (BuilderPermission.hasAdminPermission()) {
            tabMap.get(MenuManagerPage.class)
                .updateLabel(getTranslation("menu.tab.manger.orders"), VaadinIcon.COG.create());
            tabMap.get(MenuStaffPage.class)
                .updateLabel(getTranslation("menu.tab.personal"), VaadinIcon.SPECIALIST.create());
        }
        if (BuilderPermission.hasCoordinatorPermission() || BuilderPermission.hasAdminPermission()) {
            tabMap.get(MenuRequestCenterPage.class).updateLabel(getTranslation("menu.tab.request"),
                VaadinIcon.DASHBOARD.create(), countSubmittedRequests);
        }
    }
}
