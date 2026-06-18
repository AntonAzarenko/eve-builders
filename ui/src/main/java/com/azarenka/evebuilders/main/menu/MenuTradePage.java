package com.azarenka.evebuilders.main.menu;

import com.azarenka.evebuilders.common.util.BuilderPermission;
import com.azarenka.evebuilders.component.NavigationParentViewWithTabs;
import com.azarenka.evebuilders.component.NavigationTab;
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

import org.springframework.security.access.prepost.PreAuthorize;

@RoutePrefix("trade")
@Route("")
@PreAuthorize("@accessControlSecurity.canAny('DASHBOARD_VIEW','CONTRACTS_VIEW','CONTRACTS_CREATE','CONTRACTS_EDIT','CONTRACTS_ACCEPT','CONTRACTS_CANCEL','CONTRACTS_DISCARD')")
@PageTitle("Trade")
@ParentLayout(MainWidget.class)
public class MenuTradePage extends NavigationParentViewWithTabs implements LocaleChangeObserver {

    public MenuTradePage() {
        addTabIfAllowed(getTranslation("tab.trade.market"), MarketView.class,
            LineAwesomeIcon.TRADE_FEDERATION.create(), "tab-market",
            "DASHBOARD_VIEW");
        addTabIfAllowed(getTranslation("tab.trade.orders"), MarketDealsView.class,
            LineAwesomeIcon.LIST_UL_SOLID.create(), "tab-market-orders",
            "DASHBOARD_VIEW");
        addTabIfAllowed(getTranslation("tab.trade.requests"), MarketDemandView.class,
            LineAwesomeIcon.MAGIC_SOLID.create(), "tab-market-requests",
            "DASHBOARD_VIEW");
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
