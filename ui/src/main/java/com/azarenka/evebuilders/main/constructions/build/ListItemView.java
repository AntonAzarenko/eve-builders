package com.azarenka.evebuilders.main.constructions.build;

import com.azarenka.evebuilders.common.util.VaadinUtils;
import com.azarenka.evebuilders.component.View;
import com.azarenka.evebuilders.domain.dto.ProductionNode;
import com.azarenka.evebuilders.main.constructions.api.IBuildConstructionController;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.*;
import java.util.stream.Collectors;

public class ListItemView extends View {

    private static final String ROOT_NODE_FORMATTER = "%s x %s";
    private static final String STAGE_HEADER_FORMATTER = "%s: %s";

    private final AssemblyState assemblyState;
    private final IBuildConstructionController controller;

    public ListItemView(AssemblyState assemblyState, IBuildConstructionController controller) {
        super();
        this.assemblyState = assemblyState;
        this.controller = controller;
        add(initContent());
    }

    private VerticalLayout initContent() {
        var listViewLayout = VaadinUtils.initCommonVerticalLayout();
        listViewLayout.setWidth("96%");
        assemblyState.getRootNodes().forEach(rootNode -> {
            var rootHeader = new HorizontalLayout();
            rootHeader.setAlignItems(FlexComponent.Alignment.CENTER);
            rootHeader.addClassName("root-header");
            var typeName = rootNode.getTypeName();
            var name = new Span(
                    String.format(ROOT_NODE_FORMATTER, typeName, assemblyState.getCount(rootNode)));
            rootHeader.add(createIcon(typeName), name);
            var rootDetails = new Details(rootHeader, buildStages(rootNode));
            rootDetails.addClassName("root-block");
            rootDetails.setOpened(true);
            rootDetails.setWidthFull();
            listViewLayout.add(rootDetails);
        });
        return listViewLayout;
    }

    private Component buildStages(ProductionNode rootNode) {
        var stagesLayout = VaadinUtils.initCommonVerticalLayout();
        Map<Integer, Map<String, Integer>> grouped = calculateStages(rootNode);
        grouped.forEach((key, materials) -> {
            int stage = key;
            var materialLayout = VaadinUtils.initCommonVerticalLayout();
            materialLayout.getStyle().set("border-top", "1px solid #e0e0e0");
            materials.forEach((materialName, materialCount) -> {
                var matRow = createMatRowLayout();
                var componentLayout = new HorizontalLayout(createIcon(materialName), new Span(materialName),
                        new Span("x " + materialCount));
                componentLayout.setWidthFull();
                componentLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
                boolean isNotFinalItem = doesItemNotFinal(rootNode, materialName, stage);
                var propertiesMaterialButton = VaadinUtils.createLumoTertiaryButton(VaadinIcon.COG_O);
                var copyMaterialButton = VaadinUtils.createLumoTertiaryButton(VaadinIcon.CLIPBOARD);
                propertiesMaterialButton.setEnabled(isNotFinalItem);
                HorizontalLayout buttonsLayout = new HorizontalLayout();
                buttonsLayout.add(copyMaterialButton, propertiesMaterialButton);
                matRow.add(componentLayout, buttonsLayout);
                materialLayout.add(matRow);
            });
            var stageLabel = new Span(String.format(STAGE_HEADER_FORMATTER, "Stage", stage));
            var stageDetails = new Details(stageLabel, materialLayout);
            stageDetails.setSummary(stageLabel);
            stageDetails.addClassName("stage-details");
            stageDetails.setWidthFull();
            var copyStageMatButton = VaadinUtils.createLumoTertiaryButton(VaadinIcon.COPY);
            var infoStageButton = VaadinUtils.createLumoTertiaryButton(VaadinIcon.INFO_CIRCLE);
            copyStageMatButton.addClickListener(e -> {
                StringBuilder sb = new StringBuilder();
                materials.forEach((name, qty) -> sb.append(name).append(" ").append(qty).append("\n"));
                VaadinUtils.copyToClipboard(this, sb.toString(),
                        String.format("Скопировано айтемов %s", materials.entrySet().stream().count()));
            });
            infoStageButton.addClickListener(e -> {
                var distinctNodes = collectProductionNodesByStage(rootNode, stage).stream()
                        .collect(Collectors.toMap(
                                ProductionNode::getTypeName,
                                node -> node,
                                (existing, duplicate) -> existing))
                        .values()
                        .stream()
                        .toList();
                new StageInfoWindow(distinctNodes, stage).open();
            });
            var innerDetailsLayout = createInnerDetailsLayout();
            innerDetailsLayout.add(stageDetails, infoStageButton, copyStageMatButton);
            innerDetailsLayout.addClassName("btn-col");
            stagesLayout.add(innerDetailsLayout);

        });
        return stagesLayout;
    }

    public Map<Integer, Map<String, Integer>> calculateStages(ProductionNode root) {
        var stageMap = new TreeMap<Integer, Map<String, Integer>>();
        collectStagesRecursive(root, 1, stageMap);
        return stageMap;
    }

    private void collectStagesRecursive(ProductionNode node, int stage, Map<Integer, Map<String, Integer>> stageMap) {
        node.getChildren().stream()
                .peek(child -> {
                    int adjustedQty = assemblyState.recalculateBaseValue(child, child.getQuantity());
                    stageMap
                            .computeIfAbsent(stage, s -> new HashMap<>())
                            .merge(child.getTypeName(), adjustedQty, Integer::sum);
                })
                .forEach(child -> collectStagesRecursive(child, stage + 1, stageMap));
    }

    public List<ProductionNode> collectProductionNodesByStage(ProductionNode root, int targetStage) {
        var result = new ArrayList<ProductionNode>();
        collectRecursive(root, 1, targetStage, result);
        return result;
    }

    private void collectRecursive(ProductionNode node, int currentStage, int targetStage, List<ProductionNode> result) {
        if (currentStage == targetStage && node.getChildren() != null && !node.getChildren().isEmpty()) {
            result.add(node);
        }
        node.getChildren().forEach(child ->
                collectRecursive(child, currentStage + 1, targetStage, result));
    }

    private List<String> collectComponentsByStage(ProductionNode node, int targetStage) {
        return collectProductionNodesByStage(node, targetStage).stream()
                .map(ProductionNode::getTypeName)
                .distinct()
                .toList();
    }

    private boolean doesItemNotFinal(ProductionNode rootNode, String moduleName, int stage) {
        var productionNodes = collectProductionNodesByStage(rootNode, stage + 1);
        return productionNodes.stream()
                .filter(pn -> pn.getTypeName().equals(moduleName))
                .anyMatch(productionNode -> !productionNode.getChildren().isEmpty());
    }

    private Image createIcon(String moduleName) {
        var icon = controller.getImageByInvTypeName(moduleName);
        icon.setWidth("25px");
        icon.setHeight("25px");
        return icon;
    }

    private HorizontalLayout createMatRowLayout() {
        var matRow = new HorizontalLayout();
        matRow.setAlignItems(FlexComponent.Alignment.CENTER);
        matRow.setWidthFull();
        matRow.setJustifyContentMode(JustifyContentMode.BETWEEN);
        matRow.getStyle().set("border-bottom", "1px solid #e0e0e0");
        return matRow;
    }

    private HorizontalLayout createInnerDetailsLayout() {
        var innerLayout = new HorizontalLayout();
        innerLayout.setWidthFull();
        innerLayout.getStyle().set("margin", "0");
        innerLayout.getStyle().set("border", "1px solid #ccc");
        innerLayout.getStyle().set("border-radius", "4px");
        innerLayout.getStyle().set("padding-top", "2px");
        innerLayout.setJustifyContentMode(JustifyContentMode.START);
        innerLayout.setDefaultVerticalComponentAlignment(Alignment.START);
        return innerLayout;
    }
}
