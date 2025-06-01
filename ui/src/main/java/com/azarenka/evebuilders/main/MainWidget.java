package com.azarenka.evebuilders.main;

import com.azarenka.evebuilders.component.NavigationParentViewWithTabs;
import com.azarenka.evebuilders.component.NavigationTab;
import com.azarenka.evebuilders.domain.db.Role;
import com.azarenka.evebuilders.main.menu.MenuConstructionPageView;
import com.azarenka.evebuilders.main.menu.MenuManagerPageView;
import com.azarenka.evebuilders.main.menu.MenuPersonalView;
import com.azarenka.evebuilders.main.menu.page.OrdersPage;
import com.azarenka.evebuilders.service.impl.auth.SecurityUtils;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.ParentLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.util.Objects;

@Route(value = "/landing", layout = BuildersApplicationUi.class)
@PermitAll
@ParentLayout(BuildersApplicationUi.class)
public class MainWidget extends NavigationParentViewWithTabs implements LocaleChangeObserver {

    private DrawerToggle drawerToggle = new DrawerToggle();

    public MainWidget() {
        addView(OrdersPage.class, getTranslation("menu.tab.orders"), VaadinIcon.HOME.create());
        addView(MenuConstructionPageView.class, getTranslation("menu.tab.construction"), VaadinIcon.FACTORY.create());
        addTabIfAllowed(getTranslation("menu.tab.manger.orders"), MenuManagerPageView.class, Role.ROLE_ADMIN,
                VaadinIcon.COG.create());
        addTabIfAllowed(getTranslation("menu.tab.personal"), MenuPersonalView.class, Role.ROLE_ADMIN,
                VaadinIcon.SPECIALIST.create());
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
        Tabs tabs = getTabs();
        ((NavigationTab)tabs.getTabAt(0)).updateLabel(getTranslation("menu.tab.orders"), VaadinIcon.HOME.create());
        ((NavigationTab)tabs.getTabAt(1)).updateLabel(getTranslation("menu.tab.construction"), VaadinIcon.FACTORY.create());
        if (Objects.requireNonNull(SecurityUtils.getUserRoles()).contains(Role.ROLE_ADMIN)) {
            ((NavigationTab)tabs.getTabAt(2)).updateLabel(getTranslation("menu.tab.manger.orders"), VaadinIcon.COG.create());
            ((NavigationTab)tabs.getTabAt(3)).updateLabel(getTranslation("menu.tab.personal"), VaadinIcon.SPECIALIST.create());
        }
    }
}
