package com.azarenka.evebuilders.main.constructions.assembly;

import com.azarenka.evebuilders.common.util.VaadinUtils;
import com.azarenka.evebuilders.component.PopupMenuBuilder;
import com.azarenka.evebuilders.component.PopupMenuComponent;
import com.azarenka.evebuilders.component.View;
import com.azarenka.evebuilders.domain.dto.ProductionNode;
import com.azarenka.evebuilders.domain.dto.ViewMode;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.shared.SelectionPreservationMode;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.treegrid.TreeGrid;

import org.vaadin.lineawesome.LineAwesomeIcon;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class LeftSidePanel extends View {

    private final AssemblyState assemblyState;
    private final BuilderConstructionController controller;
    private final TreeGrid<ProductionNode> treeGrid = initTreeGrid();
    private final Grid<Map.Entry<String, Integer>> summaryGrid = initSummaryGrid();

    private Button listViewButton;
    private Button treeViewButton;
    private Button summorizeViewButton;
    private Button showMineralsButton;
    private Button savePropertiesButton;
    private Button loadPropertiesButton;
    private ViewMode stateViewMode;
    private HorizontalLayout leftSideToolbar;

    public LeftSidePanel(AssemblyState assemblyState, BuilderConstructionController controller) {
        this.assemblyState = assemblyState;
        this.controller = controller;
        setWidthFull();
        init();
    }

    private void init() {
        initToolBar();
        add(leftSideToolbar);
        refresh();
        refreshButtonsView();
    }

    private void initToolBar() {
        leftSideToolbar = new HorizontalLayout();
        leftSideToolbar.setAlignItems(FlexComponent.Alignment.CENTER);
        leftSideToolbar.setWidthFull();
        stateViewMode = ViewMode.TREE;
        showMineralsButton = new Button(LineAwesomeIcon.CALCULATOR_SOLID.create());
        listViewButton = new Button(LineAwesomeIcon.TOOLS_SOLID.create());
        treeViewButton = new Button(LineAwesomeIcon.LIST_ALT.create());
        summorizeViewButton = new Button(VaadinIcon.CART.create());
        savePropertiesButton = new Button(LineAwesomeIcon.SAVE.create());
        loadPropertiesButton = new Button(LineAwesomeIcon.DOWNLOAD_SOLID.create());
        listViewButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ICON);
        treeViewButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ICON);
        showMineralsButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ICON);
        summorizeViewButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ICON);
        loadPropertiesButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ICON);
        savePropertiesButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ICON);
        listViewButton.addClickListener(event -> changeViewMode(ViewMode.LIST));
        treeViewButton.addClickListener(event -> changeViewMode(ViewMode.TREE));
        summorizeViewButton.addClickListener(event -> changeViewMode(ViewMode.SUMMARY));
        var popupMenuComponent = createPopupMenuComponentForHeaderButton();
        UI.getCurrent().add(popupMenuComponent);
        showMineralsButton.addClickListener(event -> {
            CalculationItemsWindow window = new CalculationItemsWindow(controller, getProductionNodes(), getStageMap(),
                assemblyState.getRootNodes().stream()
                    .map(ProductionNode::getTypeName)
                    .collect(Collectors.joining(", ")));
            window.open();
        });
        leftSideToolbar.add(treeViewButton, listViewButton, summorizeViewButton, showMineralsButton,
            popupMenuComponent.getOpenMenuButton());
    }

    private List<ProductionNode> getProductionNodes() {
        return assemblyState.getRootNodes().stream()
            .flatMap(assemblyState::deepStream)
            .toList();
    }

    private Map<String, Integer> getStageMap() {
        return assemblyState.getStagesMap().values().stream()
            .flatMap(innerMap -> innerMap.values().stream())
            .flatMap(map -> map.entrySet().stream())
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                Integer::sum
            ));
    }

    private void changeViewMode(ViewMode viewMode) {
        stateViewMode = viewMode;
        refreshButtonsView();
        refresh();
    }

    public void refresh() {
        removeAll();
        assemblyState.recalculateStages();
        add(leftSideToolbar);
        switch (stateViewMode) {
            case LIST -> add(new ListItemView(assemblyState, controller));
            case TREE -> add(buildTreeGrid());
            case SUMMARY -> add(buildSummaryView());
        }
    }

    private void refreshButtonsView() {
        switch (stateViewMode) {
            case LIST -> {
                treeViewButton.getStyle().setColor("#005fdb");
                summorizeViewButton.getStyle().setColor("#005fdb");
                listViewButton.getStyle().setColor("#68c97e");
            }
            case TREE -> {
                treeViewButton.getStyle().setColor("#68c97e");
                summorizeViewButton.getStyle().setColor("#005fdb");
                listViewButton.getStyle().setColor("#005fdb");
            }
            case SUMMARY -> {
                treeViewButton.getStyle().setColor("#005fdb");
                summorizeViewButton.getStyle().setColor("#68c97e");
                listViewButton.getStyle().setColor("#005fdb");
            }
        }
    }

    private Component buildTreeGrid() {
        var rootNodes = assemblyState.getRootNodes();
        rootNodes.clear();
        rootNodes.addAll(assemblyState.getCountMap().entrySet().stream().map(Map.Entry::getKey).toList());
        treeGrid.setItems(rootNodes, ProductionNode::getChildren);
        treeGrid.expand(rootNodes);
        treeGrid.setSelectionPreservationMode(SelectionPreservationMode.PRESERVE_ALL);
        return treeGrid;
    }

    private TreeGrid<ProductionNode> initTreeGrid() {
        var grid = new TreeGrid<ProductionNode>();
        grid.setWidthFull();
        grid.addComponentHierarchyColumn(node -> {
                Image icon = createIcon(node.getTypeName());
                HorizontalLayout layout = new HorizontalLayout(icon, new Span(node.getTypeName()));
                layout.setAlignItems(FlexComponent.Alignment.CENTER);
                return layout;
            })
            .setHeader("Компонент")
            .setAutoWidth(true)
            .setResizable(true);
        grid.addColumn(value -> value.getFinalQuantity())
            .setHeader("Кол-во")
            .setWidth("100px");
        grid.addThemeVariants(GridVariant.LUMO_COMPACT);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);
        return grid;
    }

    private Grid<Map.Entry<String, Integer>> initSummaryGrid() {
        Grid<Map.Entry<String, Integer>> summaryGrid = VaadinUtils.initGrid("summary-grid");
        summaryGrid.addColumn(Map.Entry::getKey).setHeader("Компонент");
        summaryGrid.addColumn(entry -> String.valueOf(entry.getValue())).setHeader("Кол-во");
        summaryGrid.setWidthFull();
        return summaryGrid;
    }

    private Component buildSummaryView() {
        summaryGrid.removeAllColumns();
        summaryGrid.addColumn(Map.Entry::getKey).setHeader("Component");
        summaryGrid.addColumn(entry -> String.valueOf(entry.getValue())).setHeader("Quantity");
        Map<String, Integer> aggregated = aggregateAllMaterials();
        summaryGrid.setItems(aggregated.entrySet());
        Button copyButton = VaadinUtils.createLumoButton(VaadinIcon.COPY);
        copyButton.addClickListener( e -> {
            StringBuilder sb = new StringBuilder();
            aggregated.forEach((name, qty) -> sb.append(name).append(" ").append(qty).append("\n"));
            VaadinUtils.copyToClipboard(summaryGrid, sb.toString(),
                String.format("Скопировано айтемов %s", aggregated.entrySet().stream().count()));
        });
        var calcButton = VaadinUtils.createLumoButton(LineAwesomeIcon.CALCULATOR_SOLID);
        calcButton.addClickListener( e -> {
            CalculationItemsWindow window = new CalculationItemsWindow(controller, getProductionNodes(), aggregated,
                "Base materials");
            window.open();
        });
        var layout = VaadinUtils.initCommonVerticalLayout();
        layout.add(new HorizontalLayout(copyButton, calcButton), summaryGrid);
        layout.setSizeFull();
        return layout;
    }

    private Map<String, Integer> aggregateAllMaterials() {
        Map<String, Integer> buy = new LinkedHashMap<>();
        assemblyState.getRootNodes().forEach(rootNode -> {
            Map<Integer, List<ProductionNode>> stageMap = assemblyState.buildStageMap(rootNode);
            TreeMap<Integer, Map<String, Integer>> real = assemblyState.calculateRealQuantities(stageMap);
            Map<String, Boolean> isLeaf = computeLeafFlags(stageMap);
            Set<String> rootExcludedTypes = collectRootExcludedTypes(stageMap);
            for (Map<String, Integer> stageQuantities : real.values()) {
                for (Map.Entry<String, Integer> e : stageQuantities.entrySet()) {
                    String type = e.getKey();
                    int qty = e.getValue();
                    boolean shouldBuy = isLeaf.getOrDefault(type, false) || rootExcludedTypes.contains(type);
                    if (shouldBuy && qty > 0) {
                        buy.merge(type, qty, Integer::sum);
                    }
                }
            }
        });
        return buy;
    }

    private Map<String, Boolean> computeLeafFlags(Map<Integer, List<ProductionNode>> stageMap) {
        Map<String, Boolean> isLeaf = new HashMap<>();
        stageMap.values().forEach(list -> {
            for (ProductionNode n : list) {
                isLeaf.merge(
                    n.getTypeName(),
                    n.getChildren().isEmpty(),
                    (oldVal, newVal) -> oldVal && newVal
                );
            }
        });
        return isLeaf;
    }

    private Set<String> collectRootExcludedTypes(Map<Integer, List<ProductionNode>> stageMap) {
        return stageMap.values().stream()
            .flatMap(List::stream)
            .filter(assemblyState::isRootExcluded)
            .map(ProductionNode::getTypeName)
            .collect(Collectors.toSet());
    }

    private Image createIcon(String moduleName) {
        var icon = controller.getImageByInvTypeName(moduleName);
        icon.setWidth("25px");
        icon.setHeight("25px");
        return icon;
    }

    private PopupMenuComponent createPopupMenuComponentForHeaderButton() {
        var tooltip = "Установите улучшение для всех чертежей в проекте";
        var efficiencyField = new IntegerField("Экономия материалов %");
        efficiencyField.setValue(assemblyState.getEveryBlueprintBenefitsCount());
        return new PopupMenuBuilder()
            .withTitle("Настройка всех чертежей")
            .withComponent(efficiencyField)
            .withTooltip(tooltip)
            .withIcon(VaadinIcon.COG_O)
            .onApply(keyPressEvent -> {
                var value = efficiencyField.getValue();
                assemblyState.setEveryBlueprintHasBenefits(assemblyState.getEveryBlueprintBenefitsCount() != value);
                assemblyState.setEveryBlueprintBenefitsCount(value);
                assemblyState.recalculateStages();
                this.refresh();
            }).build();
    }
}
