package com.azarenka.evebuilders.main.menu;

import com.azarenka.evebuilders.component.View;
import com.azarenka.evebuilders.main.menu.page.PersonalPage;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.util.Arrays;

@Route(value = "default", layout = PersonalPage.class)
@PermitAll
public class MenuPersonalView extends View {

    public MenuPersonalView() {
        VaadinIcon[] values = VaadinIcon.values();
        Arrays.stream(values).forEach(e -> {
            Button button = new Button(e.name(), e.create());
            button.addThemeVariants(ButtonVariant.LUMO_SMALL);
            addAndExpand(button);
        });
    }
}
