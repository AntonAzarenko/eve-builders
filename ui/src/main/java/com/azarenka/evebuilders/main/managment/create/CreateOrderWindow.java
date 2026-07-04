package com.azarenka.evebuilders.main.managment.create;

import com.azarenka.evebuilders.main.commonview.CommonDialogComponent;
import com.azarenka.evebuilders.main.managment.api.ICreateOrderController;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;

public class CreateOrderWindow extends CommonDialogComponent {

    public CreateOrderWindow(ICreateOrderController controller) {
        super("create-order-window", true);
        setHeaderTitle(getTranslation("management.button.create"));
        setWidth("1100px");
        setHeight("85vh");
        Button closeButton = new Button(getTranslation("button.app.close"), event -> close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        getFooter().add(closeButton);
        add(new ParametersOrderView(controller));
    }
}
