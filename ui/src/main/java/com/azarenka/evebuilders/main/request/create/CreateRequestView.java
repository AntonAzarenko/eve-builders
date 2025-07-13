package com.azarenka.evebuilders.main.request.create;

import com.azarenka.evebuilders.main.commonview.CommonDialogComponent;
import com.azarenka.evebuilders.main.request.api.ICreateRequestController;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.server.VaadinSession;


public class CreateRequestView extends CommonDialogComponent {

    public CreateRequestView(ICreateRequestController controller) {
        setHeaderTitle("Create Request");
        setHeight("750px");
        setWidth("800px");
        initContent(controller);
    }

    private void initContent(ICreateRequestController controller) {
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
