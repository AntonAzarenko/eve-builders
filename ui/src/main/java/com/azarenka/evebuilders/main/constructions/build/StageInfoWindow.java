package com.azarenka.evebuilders.main.constructions.build;

import com.azarenka.evebuilders.domain.dto.ProductionNode;
import com.azarenka.evebuilders.main.commonview.CommonDialogComponent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.List;

public class StageInfoWindow extends CommonDialogComponent {

    public StageInfoWindow(List<ProductionNode> nodeList, int stage) {
        setHeaderTitle("Stage " + stage);
        super.setWidth("800px");
        super.setDraggable(true);
        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(false);
        layout.setPadding(false);

        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 2)
        );
        nodeList.forEach(productionNode -> {
            addRow(formLayout, productionNode.getTypeName(), String.valueOf(productionNode.getQuantity()));
        });
        layout.add(formLayout);
        Button closeButton = new Button("Закрыть", e -> close());
        HorizontalLayout buttons = new HorizontalLayout(closeButton);
        buttons.setWidthFull();
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        add(layout);
        getFooter().add(buttons);
    }

    private void addRow(FormLayout form, String label, String value) {
        Span labelSpan = new Span(label);
        labelSpan.getStyle()
                .set("font-weight", "900")
                .set("align-self", "center")
                .set("margin", "0");

        Span valueSpan = new Span(value);
        valueSpan.getStyle()
                .set("align-self", "center")
                .set("margin", "0");
        form.addFormItem(valueSpan, labelSpan);
    }
}
