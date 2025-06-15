package com.azarenka.evebuilders.main.menu;

import com.azarenka.evebuilders.component.NavigationParentViewWithTabs;
import com.azarenka.evebuilders.component.NavigationTab;
import com.azarenka.evebuilders.main.MainWidget;
import com.azarenka.evebuilders.main.constructions.BuilderConstructionView;
import com.azarenka.evebuilders.main.constructions.CorporationConstructionsView;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.ParentLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RoutePrefix;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;

@RoutePrefix("construction")
@Route("")
@RolesAllowed({"ROLE_USER", "ROLE_ADMIN", "ROLE_SUPER_ADMIN"})
@ParentLayout(MainWidget.class)
public class MenuConstructionPage extends NavigationParentViewWithTabs implements LocaleChangeObserver {

    public MenuConstructionPage() {
        addView(CorporationConstructionsView.class, getTranslation("tab.construction.corporation_orders"),
                VaadinIcon.GLOBE_WIRE.create());

        //todo will uncommited later
        //addView(PersonalConstructionView.class, getTranslation("tab.construction.personal_orders"));
        addView(BuilderConstructionView.class, getTranslation("tab.construction.build_orders"), VaadinIcon.GLOBE_WIRE.create());
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        Tabs tabs = getTabs();
        ((NavigationTab)tabs.getTabAt(0)).updateLabel(getTranslation("tab.construction.corporation_orders"), VaadinIcon.GLOBE_WIRE.create());
        //todo will uncommited later
        //tabs.getTabAt(1).setLabel(getTranslation("tab.construction.personal_orders"));
        ((NavigationTab)tabs.getTabAt(1)).updateLabel(getTranslation("tab.construction.build_orders"), VaadinIcon.FILE_TREE.create());
    }
}
