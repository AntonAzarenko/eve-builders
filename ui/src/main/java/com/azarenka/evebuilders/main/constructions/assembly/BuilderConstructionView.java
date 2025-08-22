package com.azarenka.evebuilders.main.constructions.assembly;

import com.azarenka.evebuilders.component.View;
import com.azarenka.evebuilders.domain.db.DistributedOrder;
import com.azarenka.evebuilders.domain.db.Fit;
import com.azarenka.evebuilders.main.menu.MenuConstructionPage;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "build-order", layout = MenuConstructionPage.class)
@RolesAllowed({"ROLE_ADMIN", "ROLE_SUPER_ADMIN", "ROLE_BUILDER"})
@PageTitle("Build Order")
public class BuilderConstructionView extends View {

    private final BuilderConstructionController controller;
    private final HorizontalLayout mainLayout = new HorizontalLayout();
    private final AssemblyState assemblyState = new AssemblyState();

    private final Fit fit;
    private final DistributedOrder order;

    public BuilderConstructionView(@Autowired BuilderConstructionController controller) {
        this.controller = controller;
        this.order = (DistributedOrder) VaadinSession.getCurrent().getAttribute("currentOrder");
        this.fit = (Fit) VaadinSession.getCurrent().getAttribute("currentFit");
        addClassName("corporation-constructions-view");
        initView();
    }

    private void initView() {
        var leftSidePanel = new LeftSidePanel(assemblyState, controller);
        var middleSidePanel = new MiddleSidePanel(controller, assemblyState, leftSidePanel);
        var rightSidePanel = new RightSidePanel(controller, middleSidePanel, order, fit);
        var leftDivider = new Div();
        var rightDivider = new Div();
        leftDivider.addClassName("vertical-divider");
        rightDivider.addClassName("vertical-divider");
        mainLayout.add(leftSidePanel, leftDivider, middleSidePanel, rightDivider, rightSidePanel);
        mainLayout.setFlexGrow(1, leftSidePanel, middleSidePanel, rightSidePanel);
        mainLayout.setSizeFull();
        mainLayout.setPadding(false);
        mainLayout.setSpacing(false);
        mainLayout.getStyle().set("padding", "0px 5px");
        add(mainLayout);
    }
}
