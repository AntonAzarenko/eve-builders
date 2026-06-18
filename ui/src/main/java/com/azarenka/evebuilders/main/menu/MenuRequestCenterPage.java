package com.azarenka.evebuilders.main.menu;

import com.azarenka.evebuilders.common.util.BuilderPermission;
import com.azarenka.evebuilders.component.NavigationParentViewWithTabs;
import com.azarenka.evebuilders.component.NavigationTab;
import com.azarenka.evebuilders.main.MainWidget;
import com.azarenka.evebuilders.main.request.create.CreateRequestView;
import com.azarenka.evebuilders.main.request.coordinator.requests.CoordinatorRequestsView;
import com.azarenka.evebuilders.main.request.admin.RequestsView;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.ParentLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RoutePrefix;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Map;

@RoutePrefix("request-center")
@Route("")
@PreAuthorize("@accessControlSecurity.canAny('CORPORATION_VIEW','CORPORATION_CONTRACT_VIEW','CORPORATION_CONTRACT_EDIT')")
@PageTitle("Request Center")
@ParentLayout(MainWidget.class)
public class MenuRequestCenterPage extends NavigationParentViewWithTabs implements LocaleChangeObserver {

    public MenuRequestCenterPage() {
        addTabIfAllowed(getTranslation("tab.request.my_request"), CoordinatorRequestsView.class,
                VaadinIcon.HOME_O.create(), "tab-my-requests",
                "CORPORATION_VIEW", "CORPORATION_CONTRACT_VIEW", "CORPORATION_CONTRACT_EDIT");
        addTabIfAllowed(getTranslation("tab.request.group_request"), RequestsView.class,
                VaadinIcon.LINES_LIST.create(), "tab-requests",
                "CORPORATION_CONTRACT_VIEW", "CORPORATION_CONTRACT_EDIT");
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        Map<Class<?>, NavigationTab> tabMap = getTabMap();
        if (BuilderPermission.hasCoordinatorPermission()) {
            tabMap.get(CoordinatorRequestsView.class).updateLabel(getTranslation("tab.request.my_request"),
                    VaadinIcon.HOME_O.create());
        }
        if (BuilderPermission.hasAdminPermission()) {
            tabMap.get(RequestsView.class).updateLabel(getTranslation("tab.request.group_request"),
                    VaadinIcon.LINES_LIST.create());
        }
    }
}
