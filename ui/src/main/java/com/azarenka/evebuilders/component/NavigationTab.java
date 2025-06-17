package com.azarenka.evebuilders.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.router.RouteConfiguration;

public class NavigationTab extends Tab {

    private final Class<? extends Component> navigationTarget;

    NavigationTab(Class<? extends Component> navigationTarget, String label, Icon tabIcon) {
        super(tabIcon, new Span(label));
        this.navigationTarget = navigationTarget;
    }

    public NavigationTab(Class<? extends Component> navigationTarget, String label, Icon icon, int badgeCount) {
        super();
        this.navigationTarget = navigationTarget;
        updateLabel(label, icon, badgeCount);
    }

    public void updateLabel(String label, Icon icon) {
        updateLabel(label, icon, 0);
    }

    public void updateLabel(String label, Icon icon, int badgeCount) {
        removeAll();
        HorizontalLayout layout = new HorizontalLayout();
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        icon.setSize("16px");
        Span labelSpan = new Span(label);
        layout.add(icon, labelSpan);
        if (badgeCount > 0) {
            Span badge = new Span(String.valueOf(badgeCount));
            badge.getElement().getThemeList().add("badge primary small");
            badge.getStyle().set("margin-left", "-10px");
            layout.add(badge);
        }
        add(layout);
    }

    String getHref() {
        return RouteConfiguration.forApplicationScope().getUrl(navigationTarget);
    }

    Class<? extends Component> getNavigationTarget() {
        return navigationTarget;
    }
}
