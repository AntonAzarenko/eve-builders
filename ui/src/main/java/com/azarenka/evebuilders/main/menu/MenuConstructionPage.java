package com.azarenka.evebuilders.main.menu;

import com.azarenka.evebuilders.component.NavigationParentViewWithTabs;
import com.azarenka.evebuilders.component.NavigationTab;
import com.azarenka.evebuilders.main.MainWidget;
import com.azarenka.evebuilders.main.constructions.assembly.BuilderConstructionView;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.ParentLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RoutePrefix;

import org.springframework.security.access.prepost.PreAuthorize;

@RoutePrefix("construction")
@Route("")
@PreAuthorize("@accessControlSecurity.canAny('DASHBOARD_VIEW','CONTRACTS_ACCEPT','CONTRACTS_DISCARD')")
@ParentLayout(MainWidget.class)
public class MenuConstructionPage extends NavigationParentViewWithTabs implements LocaleChangeObserver {

    public MenuConstructionPage() {


        //todo will uncommited later
        //addView(PersonalConstructionView.class, getTranslation("tab.construction.personal_orders"));
        addView(BuilderConstructionView.class, getTranslation("tab.construction.build_orders"),
            VaadinIcon.FACTORY.create(), "tab-assembly");
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        Tabs tabs = getTabs();
        //todo will uncommited later
        //tabs.getTabAt(1).setLabel(getTranslation("tab.construction.personal_orders"));
        ((NavigationTab) tabs.getTabAt(0)).updateLabel(getTranslation("tab.construction.build_orders"),
            VaadinIcon.FACTORY.create());
    }
}
