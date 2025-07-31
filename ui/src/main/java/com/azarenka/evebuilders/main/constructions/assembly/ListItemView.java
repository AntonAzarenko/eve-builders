package com.azarenka.evebuilders.main.constructions.assembly;

import com.azarenka.evebuilders.common.util.INumberFormater;
import com.azarenka.evebuilders.common.util.VaadinUtils;
import com.azarenka.evebuilders.component.PopupMenuBuilder;
import com.azarenka.evebuilders.component.PopupMenuComponent;
import com.azarenka.evebuilders.component.View;
import com.azarenka.evebuilders.domain.dto.ProductionNode;
import com.azarenka.evebuilders.main.constructions.api.IBuildConstructionController;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;

import org.vaadin.lineawesome.LineAwesomeIcon;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ListItemView extends View implements INumberFormater {

    private static final String ROOT_NODE_FORMATTER = "%s x %s";
    private static final String STAGE_HEADER_FORMATTER = "%s: %s";

    private final AssemblyState assemblyState;
    private final IBuildConstructionController controller;

    public ListItemView(AssemblyState assemblyState, IBuildConstructionController controller) {
        super();
        this.removeAll();
        this.assemblyState = assemblyState;
        this.controller = controller;
        addClassName("scrollable-column");
        add(initContent());
    }

    public void refresh() {
        this.removeAll();
        this.add(initContent());
    }

    private VerticalLayout initContent() {
        var listViewLayout = VaadinUtils.initCommonVerticalLayout();
        listViewLayout.setWidth("96%");
        assemblyState.getRootNodes().forEach(rootNode -> {
            var rootHeader = new HorizontalLayout();
            rootHeader.setAlignItems(FlexComponent.Alignment.CENTER);
            rootHeader.setJustifyContentMode(JustifyContentMode.BETWEEN);
            rootHeader.addClassName("root-header");
            var typeName = rootNode.getTypeName();
            var name = new Span(
                String.format(ROOT_NODE_FORMATTER, typeName, assemblyState.getCount(rootNode)));
            rootHeader.add(controller.createIcon(typeName), name);
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
        Map<Integer, Map<String, Integer>> grouped = assemblyState.getStagesMap().get(rootNode);
        final int[] stage = {grouped.size()};
        grouped.forEach((key, materials) -> {
            stage[0] = stage[0] - 1;
            if (stage[0] != grouped.size() - 1) {
                var materialLayout = VaadinUtils.initCommonVerticalLayout();
                materialLayout.getStyle().set("border-top", "1px solid #e0e0e0");
                materials.forEach((materialName, materialCount) -> {
                    var matRow = createMatRowLayout();
                    var componentLayout = new HorizontalLayout(createIcon(materialName), new Span(materialName),
                        new Span("x " + formatNumber(materialCount)));
                    componentLayout.setWidthFull();
                    componentLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
                    matRow.add(componentLayout, initRowButtonsLayout(rootNode, materialName, key, materialCount));
                    materialLayout.add(matRow);
                });
                var stageLabel = new Span(String.format(STAGE_HEADER_FORMATTER, "Stage", stage[0]));
                var stageDetails = new Details(stageLabel, materialLayout);
                stageDetails.setOpened(true);
                stageDetails.setSummary(stageLabel);
                stageDetails.addClassName("stage-details");
                stageDetails.setWidthFull();
                var copyStageMatButton = VaadinUtils.createLumoButton(VaadinIcon.COPY);
                var infoStageButton = VaadinUtils.createLumoButton(VaadinIcon.INFO_CIRCLE);
                var showMineralsButton = VaadinUtils.createLumoButton(LineAwesomeIcon.CALCULATOR_SOLID);
                copyStageMatButton.addClickListener(e -> {
                    StringBuilder sb = new StringBuilder();
                    materials.forEach((name, qty) -> sb.append(name).append(" ").append(qty).append("\n"));
                    VaadinUtils.copyToClipboard(this, sb.toString(),
                        String.format("Скопировано айтемов %s", materials.entrySet().stream().count()));
                });
                int s = stage[0];
                List<ProductionNode> productionNodesForStage = collectProductionNodesByStage(rootNode, key);
                infoStageButton.addClickListener(e -> {
                    new StageInfoWindow(productionNodesForStage, assemblyState, s, controller).open();
                });
                showMineralsButton.addClickListener(e -> {
                    new CalculationItemsWindow(controller, productionNodesForStage,
                        assemblyState.getStagesMap().get(rootNode).get(key), "Stage " + s).open();
                });
                var innerDetailsLayout = createInnerDetailsLayout();
                innerDetailsLayout.add(stageDetails, showMineralsButton, infoStageButton, copyStageMatButton);
                innerDetailsLayout.addClassName("btn-col");
                stagesLayout.add(innerDetailsLayout);
            }
        });
        return stagesLayout;
    }

    private HorizontalLayout initRowButtonsLayout(ProductionNode rootNode, String materialName, Integer stage,
                                                  Integer materialCount) {
        var copyMaterialButton = createCopyMaterialButton(materialName, materialCount);
        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.add(copyMaterialButton, createPropertiesMaterialButton(rootNode, materialName, stage));
        return buttonsLayout;
    }

    private Button createCopyMaterialButton(String materialName, Integer materialCount) {
        var copyMaterialButton = VaadinUtils.createLumoTertiaryButton(VaadinIcon.CLIPBOARD);
        copyMaterialButton.addClickListener(event ->
            VaadinUtils.copyToClipboard(copyMaterialButton, String.format("%s %s", materialName, materialCount),
                "Скопировано"));
        return copyMaterialButton;
    }

    private Button createPropertiesMaterialButton(ProductionNode rootNode, String materialName, int stage) {
        boolean isNotFinalItem = doesItemNotFinal(rootNode, materialName, stage);
        var productionNodes = assemblyState.findAllNodesByName(rootNode, materialName);
        var menuWindow = isCompositeItem(productionNodes)
            ? createPopupMenuComponentWithoutEfficiency(productionNodes)
            : createPopupMenuComponentWithEfficiency(productionNodes);
        UI.getCurrent().add(menuWindow);
        var propertiesMaterialButton = menuWindow.getOpenMenuButton();
        if (assemblyState.getManuallyExcludedNodes().containsAll(productionNodes) && isNotFinalItem) {
            propertiesMaterialButton.getStyle().set("color", "red");
        } else if (assemblyState.getEfficiencyMap().containsKey(productionNodes.get(0)) && isNotFinalItem) {
            propertiesMaterialButton.getStyle().set("color", "green");
        }
        propertiesMaterialButton.setEnabled(isNotFinalItem);
        if (assemblyState.isEveryBlueprintHasBenefits() && isNotFinalItem
            && (Objects.nonNull(productionNodes.get(0).getMaterialType()) &&
            !assemblyState.getCompositeTypes().contains(productionNodes.get(0).getMaterialType()))) {
            productionNodes.forEach(productionNode ->
                assemblyState.setEfficiency(productionNode, assemblyState.getEveryBlueprintBenefitsCount()));
        }
        return propertiesMaterialButton;
    }

    private boolean isCompositeItem(List<ProductionNode> productionNodes) {
        return !(Objects.nonNull(productionNodes.get(0).getMaterialType()) &&
            !assemblyState.getCompositeTypes().contains(productionNodes.get(0).getMaterialType()));
    }

    private Checkbox initExcludeCheckbox(List<ProductionNode> productionNodes) {
        var excludedCheckbox = new Checkbox("Исключить из просчета");
        excludedCheckbox.addValueChangeListener(event -> {
            if (event.getValue()) {
                assemblyState.getManuallyExcludedNodes().addAll(productionNodes);
                for (ProductionNode root : productionNodes) {
                    excludeRecursively(root, assemblyState.getExcludedNodes());
                }
            } else {
                productionNodes.forEach(assemblyState.getManuallyExcludedNodes()::remove);
                for (ProductionNode root : productionNodes) {
                    includeRecursively(root, assemblyState.getExcludedNodes());
                }
            }
        });
        excludedCheckbox.setValue(assemblyState.getManuallyExcludedNodes().containsAll(productionNodes));
        return excludedCheckbox;
    }

    private IntegerField initEfficiencyField(List<ProductionNode> productionNodes) {
        var efficiencyField = new IntegerField("Экономия материалов %");
        efficiencyField.setValue(Objects.isNull(assemblyState.getEfficiencyMap().get(productionNodes.get(0)))
            ? 0 : assemblyState.getEfficiencyMap().get(productionNodes.get(0)).intValue());
        return efficiencyField;
    }

    private PopupMenuComponent createPopupMenuComponentWithEfficiency(List<ProductionNode> productionNodes) {
        var tooltip = "Установите улучшение чертежа для правильного отображения количества материалов";
        var efficiencyField = initEfficiencyField(productionNodes);
        return new PopupMenuBuilder().withComponent(efficiencyField)
            .withTitle("Настройка узла")
            .withComponent(initExcludeCheckbox(productionNodes))
            .withTooltip(tooltip)
            .withIcon(VaadinIcon.COG_O)
            .onApply(keyPressEvent -> {
                var value = efficiencyField.getValue();
                productionNodes.forEach(productionNode -> {
                    assemblyState.getEfficiencyMap()
                        .put(productionNode, Objects.isNull(value) ? 0 : Double.valueOf(value));
                });
                assemblyState.recalculateStages();
                this.refresh();
            }).build();
    }

    private PopupMenuComponent createPopupMenuComponentWithoutEfficiency(List<ProductionNode> productionNodes) {
        var tooltip = "Исключите компонент из просчета";
        return new PopupMenuBuilder()
            .withTitle("Настройка узла")
            .withComponent(initExcludeCheckbox(productionNodes))
            .withTooltip(tooltip)
            .withIcon(VaadinIcon.COG)
            .onApply(keyPressEvent -> {
                assemblyState.recalculateStages();
                this.refresh();
            }).build();
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

    private void excludeRecursively(ProductionNode node, Set<ProductionNode> excluded) {
        excluded.add(node);
        if (node.getChildren() != null) {
            for (ProductionNode child : node.getChildren()) {
                excludeRecursively(child, excluded);
            }
        }
    }

    private void includeRecursively(ProductionNode node, Set<ProductionNode> excluded) {
        excluded.remove(node);
        if (node.getChildren() != null) {
            for (ProductionNode child : node.getChildren()) {
                includeRecursively(child, excluded);
            }
        }
    }
}
