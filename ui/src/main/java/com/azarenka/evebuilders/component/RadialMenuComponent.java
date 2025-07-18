package com.azarenka.evebuilders.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.notification.Notification;

@Tag("radial-menu")
@JsModule("./radial-menu.js")
public class RadialMenuComponent extends Component {

    public RadialMenuComponent() {
        getElement().addEventListener("item-click", e -> {
            int index = (int) e.getEventData().getNumber("event.detail");
            Notification.show("Клик по элементу: " + (index + 1));
        }).addEventData("event.detail");
    }
}
