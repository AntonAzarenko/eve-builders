package com.azarenka.evebuilders.main.request.create;

import com.azarenka.evebuilders.main.commonview.CommonDialogComponent;
import com.azarenka.evebuilders.main.request.api.IRequestController;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.server.VaadinSession;

public class CreateRequestView extends CommonDialogComponent {

    public CreateRequestView(IRequestController controller) {
        super("create-request-view",false);
        setHeaderTitle("Create Request");
        setHeight("750px");
        setWidth("800px");
        setDraggable(true);
        initContent(controller);
    }

    private void initContent(IRequestController controller) {
        var parametersRequestView = new ParametersRequestView(controller);
        add(parametersRequestView);
        var applyButton = parametersRequestView.getApplyButton();
        applyButton.addClickListener(event -> {
            if (parametersRequestView.isValid()) {
                this.close();
            }
        });
        Button closeButton = (Button) createCloseButton();
        closeButton.addClickListener(event -> VaadinSession.getCurrent().setAttribute("requestOrder", null));
        getFooter().add(applyButton, parametersRequestView.getClearButton(), closeButton);
    }
}
