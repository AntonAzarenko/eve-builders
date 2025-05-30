package com.azarenka.evebuilders.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.RouterLayout;

public abstract class NavigableParentView extends View implements RouterLayout {

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if(event.getNavigationTarget() == getClass()) {
            if(getDefaultChildView() != null) {
                event.forwardTo(getDefaultChildView());
            }
        }
    }

    protected abstract Class<? extends Component> getDefaultChildView();
}
