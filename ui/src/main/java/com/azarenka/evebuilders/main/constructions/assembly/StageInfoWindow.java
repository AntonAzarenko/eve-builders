package com.azarenka.evebuilders.main.constructions.assembly;

import com.azarenka.evebuilders.common.util.INumberFormater;
import com.azarenka.evebuilders.domain.dto.ProductionNode;
import com.azarenka.evebuilders.main.commonview.CommonDialogComponent;
import com.azarenka.evebuilders.main.constructions.api.IBuildConstructionController;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.text.DecimalFormat;
import java.util.List;

public class StageInfoWindow extends CommonDialogComponent implements INumberFormater {

    private final IBuildConstructionController controller;

    public StageInfoWindow(List<ProductionNode> nodeList, AssemblyState assemblyState, int stage,
                           IBuildConstructionController controller) {
        super("stage-info-window", true);
        this.controller = controller;
        setHeaderTitle("Stage " + stage);
        super.setWidth("500px");
        super.setDraggable(true);

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(false);
        layout.setPadding(false);

        for (ProductionNode root : nodeList) {
            if (assemblyState.getExcludedNodes().contains(root)) {
                continue;
            }
            VerticalLayout rootBlock = new VerticalLayout();
            rootBlock.getStyle().set("border-bottom", "1px solid var(--lumo-contrast-10pct)");
            rootBlock.setPadding(false);
            rootBlock.setSpacing(false);

            int rootQty = root.getFinalQuantity();
            Span header = new Span(root.getTypeName() + " x " + formatNumber(rootQty));
            header.getStyle().set("font-weight", "bold");
            rootBlock.add(new HorizontalLayout(createIcon(root.getTypeName()), header));

            for (ProductionNode material : root.getChildren()) {
                int finalQty = material.getFinalQuantity();

                Span line = new Span(material.getTypeName() + ": " + formatNumber(finalQty));
                //line.getStyle().set("margin-left", "5px");
                HorizontalLayout horizontalLayout = new HorizontalLayout(new Span("↳ "), createIcon(material.getTypeName()), line);
                horizontalLayout.getStyle().set("margin-left", "10px");
                rootBlock.add(horizontalLayout);
            }

            layout.add(rootBlock);
        }

        Button closeButton = new Button("Закрыть", e -> close());
        HorizontalLayout buttons = new HorizontalLayout(closeButton);
        buttons.setWidthFull();
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        add(layout);
        getFooter().add(buttons);
    }

    private Image createIcon(String moduleName) {
        var icon = controller.getImageByInvTypeName(moduleName);
        icon.setWidth("25px");
        icon.setHeight("25px");
        return icon;
    }
}
