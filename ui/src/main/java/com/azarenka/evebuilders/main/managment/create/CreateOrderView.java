package com.azarenka.evebuilders.main.managment.create;

import com.azarenka.evebuilders.component.View;
import com.azarenka.evebuilders.main.managment.api.ICreateOrderController;
import com.azarenka.evebuilders.main.managment.page.AddOrderPage;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "default", layout = AddOrderPage.class)
@RolesAllowed({"ROLE_ADMIN", "ROLE_SUPER_ADMIN"})
@PermitAll
public class CreateOrderView extends View {

    private static final String OPEN_CREATE_ORDER_DIALOG = "openCreateOrderDialog";
    private final ICreateOrderController controller;
    private CreateOrderWindow createOrderDialog;

    public CreateOrderView(ICreateOrderController controller) {
        this.controller = controller;
        super.setPadding(false);
        getStyle().set("padding", "0px 5px 30px 5px");
        initContent();
        openDialogIfEditFlow();
    }

    private void initContent() {
        Button createButton = new Button(getTranslation("management.button.create"), VaadinIcon.PLUS.create(),
            event -> {
                VaadinSession.getCurrent().setAttribute("requestOrder", null);
                VaadinSession.getCurrent().setAttribute("originalOrder", null);
                openCreateDialog();
            });
        createButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        HorizontalLayout toolbar = new HorizontalLayout(createButton);
        toolbar.setWidthFull();
        toolbar.setJustifyContentMode(JustifyContentMode.START);
        ExistingOrdersView existingOrdersView = new ExistingOrdersView(controller);
        existingOrdersView.setSizeFull();
        add(toolbar, existingOrdersView);
        setSizeFull();
        expand(existingOrdersView);
    }

    private void openCreateDialog() {
        createOrderDialog = new CreateOrderWindow(controller);
        createOrderDialog.open();
    }

    private void openDialogIfEditFlow() {
        Object openDialogFlag = VaadinSession.getCurrent().getAttribute(OPEN_CREATE_ORDER_DIALOG);
        if (Boolean.TRUE.equals(openDialogFlag)) {
            VaadinSession.getCurrent().setAttribute(OPEN_CREATE_ORDER_DIALOG, false);
            openCreateDialog();
        }
    }
}
