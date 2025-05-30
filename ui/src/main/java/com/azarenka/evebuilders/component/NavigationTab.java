package com.azarenka.evebuilders.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.router.RouteConfiguration;

public class NavigationTab extends Tab {

    private final Class<? extends Component> navigationTarget;

    NavigationTab(Class<? extends Component> navigationTarget, String label, Icon tabIcon) {
        super(tabIcon, new Span(label));
        this.navigationTarget = navigationTarget;
    }

    public void updateLabel(String label, Icon tabIcon) {
        removeAll();
        this.add(tabIcon, new Span(label));
    }

    String getHref() {
        return RouteConfiguration.forApplicationScope().getUrl(navigationTarget);
    }

    Class<? extends Component> getNavigationTarget() {
        return navigationTarget;
    }
}
