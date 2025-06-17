package com.azarenka.evebuilders.component;

import com.azarenka.evebuilders.common.util.VaadinUtils;
import com.azarenka.evebuilders.domain.db.Role;
import com.azarenka.evebuilders.service.impl.auth.SecurityUtils;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;

import java.util.*;

public class NavigationParentViewWithTabs extends NavigableParentView implements AfterNavigationObserver {

    private final Tabs tabs = new Tabs();
    private final Div viewDisplay = new Div();
    private boolean noNavigation = false;
    private final Map<Class<?>, NavigationTab> tabMap = new LinkedHashMap<>();


    public NavigationParentViewWithTabs() {
        setSizeFull();
        add(tabs);
        viewDisplay.setSizeFull();
        addAndExpand(viewDisplay);
        VaadinUtils.addComponentId(tabs, "vertical-tabs");
    }

    public void addView(Class<? extends Component> viewClass, String label, Icon tabIcon) {
        addView(viewClass, label, null, tabIcon, 0);
    }

    public void addView(Class<? extends Component> viewClass, String label, String tabId, Icon tabIcon, Integer badgeCount) {
        NavigationTab tab = new NavigationTab(viewClass, label, tabIcon, badgeCount);
        if (tabId != null) {
            tab.setId(tabId);
        }
        tabs.add(tab);
        tabMap.put(viewClass, tab);
    }

    public void addTabIfAllowed(String caption, Class<? extends Component> viewClass, Role[] viewPermission, Icon tabIcon) {
        boolean hasPermission = Arrays.stream(viewPermission)
                .anyMatch(Objects.requireNonNull(SecurityUtils.getUserRoles())::contains);
        if (hasPermission) {
            addView(viewClass, caption, tabIcon);
        }
    }

    public void addTabIfAllowedWithBadge(String caption, Class<? extends Component> viewClass, Role[] viewPermission,
                                         Icon tabIcon, Integer badgeCount) {
        boolean hasPermission = Arrays.stream(viewPermission)
                .anyMatch(Objects.requireNonNull(SecurityUtils.getUserRoles())::contains);
        if (hasPermission) {
            addView(viewClass, caption, null, tabIcon, badgeCount);
        }
    }

    @Override
    public void showRouterLayoutContent(HasElement view) {
        this.viewDisplay.removeAll();
        this.viewDisplay.getElement().appendChild(view.getElement());
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        tabs.addSelectedChangeListener(selectionEvent -> {
            if (!noNavigation) {
                UI.getCurrent().navigate(((NavigationTab) selectionEvent.getSelectedTab()).getHref());
            }
            noNavigation = false;
        });
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        final String targetUrl = event.getLocation().getPath();

        Optional<Integer> toBeSelectedTabIndex = Optional.empty();
        int index = 0;
        for (Component tab : tabs.getChildren().toList()) {
            String tabUrl = ((NavigationTab) tab).getHref();
            if (targetUrl.equals(tabUrl) || targetUrl.startsWith(tabUrl)) {
                toBeSelectedTabIndex = Optional.of(index);
                break;
            }
            index++;
        }

        toBeSelectedTabIndex.ifPresent(idx -> {
            if (idx != tabs.getSelectedIndex()) {
                noNavigation = true;
                tabs.setSelectedIndex(idx);
            }
        });
    }

    @Override
    protected Class<? extends Component> getDefaultChildView() {
        if (tabs.getComponentCount() == 0) {
            return null;
        }
        return ((NavigationTab) tabs.getTabAt(0)).getNavigationTarget();
    }

    public Tabs getTabs() {
        return tabs;
    }

    public Map<Class<?>, NavigationTab> getTabMap() {
        return tabMap;
    }
}
