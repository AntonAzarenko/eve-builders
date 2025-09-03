package com.azarenka.evebuilders.main.menu;

import com.azarenka.evebuilders.common.util.BuilderPermission;
import com.azarenka.evebuilders.component.NavigationParentViewWithTabs;
import com.azarenka.evebuilders.component.NavigationTab;
import com.azarenka.evebuilders.domain.db.Role;
import com.azarenka.evebuilders.main.MainWidget;
import com.azarenka.evebuilders.main.trade.MarketDealsView;
import com.azarenka.evebuilders.main.trade.MarketDemandView;
import com.azarenka.evebuilders.main.trade.MarketView;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.ParentLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RoutePrefix;

import org.vaadin.lineawesome.LineAwesomeIcon;

import java.util.Map;

import jakarta.annotation.security.RolesAllowed;

@RoutePrefix("trade")
@Route("")
@RolesAllowed({"ROLE_SUPER_ADMIN", "ROLE_BUILDER", "ROLE_MINER", "ROLE_ADMIN"})
@PageTitle("Trade")
@ParentLayout(MainWidget.class)
public class MenuTradePage extends NavigationParentViewWithTabs implements LocaleChangeObserver {

    public MenuTradePage() {
        addTabIfAllowed(getTranslation("tab.trade.market"), MarketView.class, new Role[]{
            Role.ROLE_SUPER_ADMIN,
            Role.ROLE_ADMIN,
            Role.ROLE_MINER,
            Role.ROLE_BUILDER}, LineAwesomeIcon.TRADE_FEDERATION.create(), "tab-market");
        addTabIfAllowed(getTranslation("tab.trade.orders"), MarketDealsView.class, new Role[]{
            Role.ROLE_SUPER_ADMIN,
            Role.ROLE_ADMIN,
            Role.ROLE_MINER,
            Role.ROLE_BUILDER
        }, LineAwesomeIcon.LIST_UL_SOLID.create(), "tab-market-orders");
        addTabIfAllowed(getTranslation("tab.trade.requests"), MarketDemandView.class, new Role[]{
            Role.ROLE_SUPER_ADMIN,
            Role.ROLE_ADMIN,
            Role.ROLE_MINER,}, LineAwesomeIcon.MAGIC_SOLID.create(), "tab-market-requests");
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        Map<Class<?>, NavigationTab> tabMap = getTabMap();
        if (BuilderPermission.hasMinerPermission() || BuilderPermission.hasBuilderPermission() ||
            BuilderPermission.hasAdminPermission()) {
            tabMap.get(MarketView.class).updateLabel(getTranslation("tab.trade.market"),
                LineAwesomeIcon.TRADE_FEDERATION.create());
            tabMap.get(MarketDealsView.class).updateLabel(getTranslation("tab.trade.orders"),
                LineAwesomeIcon.LIST_UL_SOLID.create());
        }
        if (BuilderPermission.hasMinerPermission() || BuilderPermission.hasAdminPermission()) {
            tabMap.get(MarketDemandView.class).updateLabel(getTranslation("tab.trade.requests"),
                LineAwesomeIcon.MAGIC_SOLID.create());
        }
    }
}
