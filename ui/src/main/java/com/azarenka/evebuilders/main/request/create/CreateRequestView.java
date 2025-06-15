package com.azarenka.evebuilders.main.request.create;

import com.azarenka.evebuilders.component.View;
import com.azarenka.evebuilders.main.menu.MenuRequestCenterPage;
import com.azarenka.evebuilders.main.request.api.ICreateRequestController;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "create", layout = MenuRequestCenterPage.class)
@RolesAllowed({"ROLE_COORDINATOR"})
@PageTitle("Create Request")
public class CreateRequestView extends View {

    private final ICreateRequestController controller;

    public CreateRequestView(ICreateRequestController controller) {
        this.controller = controller;
        super.setPadding(true);
        initSplitLayout();
        add(initSplitLayout());
    }

    private SplitLayout initSplitLayout() {
        SplitLayout splitLayout = new SplitLayout();
        splitLayout.setSizeFull();
        splitLayout.addToPrimary(new ParametersRequestView(controller));
        splitLayout.setSplitterPosition(50);
        splitLayout.addToSecondary(new ExistingRequestsView(controller));
        return splitLayout;
    }
}
