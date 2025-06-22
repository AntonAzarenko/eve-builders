package com.azarenka.evebuilders.main.constructions;

import com.azarenka.evebuilders.common.util.VaadinUtils;
import com.azarenka.evebuilders.component.View;
import com.azarenka.evebuilders.domain.dto.ProductionNode;
import com.azarenka.evebuilders.domain.dto.ViewMode;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.shared.SelectionPreservationMode;
import com.vaadin.flow.component.treegrid.TreeGrid;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class LeftSidePanel extends View {

    private final AssemblyState assemblyState;
    private final BuilderConstructionController controller;
    private final RadioButtonGroup<ViewMode> viewModeSelector = new RadioButtonGroup<>();

    private final Grid<ProductionNode> listGrid = initListGrid();
    private final TreeGrid<ProductionNode> treeGrid = initTreeGrid();
    private final Grid<Map.Entry<String, Integer>> summaryGrid = initSummaryGrid();

    private Button listViewButton;
    private Button treeViewButton;
    private Button summorizeViewButton;
    private ViewMode stateViewMode;
    private HorizontalLayout leftSideToolbar;

    public LeftSidePanel(AssemblyState assemblyState, BuilderConstructionController controller) {
        this.assemblyState = assemblyState;
        this.controller = controller;
        setWidthFull();
        addClassName("scrollable-column");
        init();
    }

    private void init() {
        initToolBar();
        add(leftSideToolbar);
        refresh();
    }

    private void initToolBar() {
        leftSideToolbar = new HorizontalLayout();
        leftSideToolbar.setAlignItems(FlexComponent.Alignment.CENTER);
        leftSideToolbar.setWidthFull();
        stateViewMode = ViewMode.TREE;
        listViewButton = new Button(VaadinIcon.LIST.create());
        treeViewButton = new Button(VaadinIcon.ARCHIVES.create());
        summorizeViewButton = new Button(VaadinIcon.ABACUS.create());
        listViewButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ICON);
        treeViewButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ICON);
        summorizeViewButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ICON);
        listViewButton.addClickListener(event -> {
            stateViewMode = ViewMode.LIST;
            refresh();
        });
        treeViewButton.addClickListener(event -> {
            stateViewMode = ViewMode.TREE;
            refresh();
        });
        summorizeViewButton.addClickListener(event -> {
            stateViewMode = ViewMode.SUMMARY;
            refresh();
        });
        leftSideToolbar.add(treeViewButton, listViewButton, summorizeViewButton);
    }

    public void refresh() {
        removeAll();
        add(leftSideToolbar);

        switch (stateViewMode) {
            case LIST -> add(buildListView());
            case TREE -> add(buildTreeGrid());
            case SUMMARY -> add(buildSummaryView());
        }
    }

    private Component buildListView() {
        var rootNodes = assemblyState.getRootNodes();
        rootNodes.clear();
        rootNodes.addAll(assemblyState.getCountMap().entrySet().stream().map(Map.Entry::getKey).toList());
        if (rootNodes.size() > 0) {
            listGrid.setItems(flattenWithStagesGrouped(rootNodes.get(0)));
        }
        return listGrid;
    }

    private Grid<ProductionNode> initListGrid() {
        Grid<ProductionNode> grid = VaadinUtils.initGrid("list-grid");
        grid.addComponentColumn(node -> {
            if (node.isStageHeader()) {
                Button copyButton = new Button(VaadinIcon.COPY.create(), e -> copyStage(node));
                copyButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_SMALL);
                List<String> components = collectComponentsByStage(assemblyState.getRootNodes().get(0), node.getStage());
                String componentList = String.join(", ", components);
                Button showComponentsButton = new Button(VaadinIcon.INFO_CIRCLE.create(), e -> {
                    Notification.show("Компоненты этапа " + node.getStage() + ": " + componentList, 5000, Notification.Position.MIDDLE);
                });
                showComponentsButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_SMALL);

                Span span = new Span("Этап " + node.getStage());
                HorizontalLayout layout = new HorizontalLayout(new HorizontalLayout(span, showComponentsButton), copyButton);
                layout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
                layout.setWidthFull();
                layout.addClassName("stage-header");
                return layout;
            } else {
                HorizontalLayout layout = new HorizontalLayout(
                        createIcon(node.getTypeName()),
                        new Span(node.getTypeName()),
                        new Span(String.valueOf(node.getQuantity()))
                );
                layout.setWidthFull();
                return layout;
            }
        });
        return grid;
    }

    private Component buildTreeGrid() {
        var rootNodes = assemblyState.getRootNodes();
        rootNodes.clear();
        rootNodes.addAll(assemblyState.getCountMap().entrySet().stream().map(Map.Entry::getKey).toList());
        treeGrid.setItems(rootNodes, ProductionNode::getChildren);
        treeGrid.setSelectionPreservationMode(SelectionPreservationMode.PRESERVE_ALL);
        return treeGrid;
    }

    private TreeGrid<ProductionNode> initTreeGrid() {
        TreeGrid<ProductionNode> grid = new TreeGrid<>();
        grid.setWidthFull();
        grid.addComponentHierarchyColumn(node -> {
            Image icon = createIcon(node.getTypeName());
            HorizontalLayout layout = new HorizontalLayout(icon, new Span(node.getTypeName()));
            layout.setAlignItems(FlexComponent.Alignment.CENTER);
            return layout;
        }).setHeader("Компонент").setAutoWidth(true).setResizable(true);
        grid.addColumn(value -> assemblyState.recalculateBaseValue(value, value.getQuantity()))
                .setHeader("Кол-во")
                .setAutoWidth(true);
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
        Button copyButton = new Button("Скопировать", e -> {
            StringBuilder sb = new StringBuilder();
            aggregated.forEach((name, qty) -> sb.append(name).append(" x ").append(qty).append("\n"));
            //VaadinUtils.copyToClipboard(sb.toString());
            Notification.show("Скопировано");
        });
        VerticalLayout layout = VaadinUtils.initCommonVerticalLayout();
        layout.add(copyButton, summaryGrid);
        layout.setSizeFull();
        return layout;
    }

    private List<ProductionNode> flattenWithStagesGrouped(ProductionNode root) {
        var grouped = new TreeMap<Integer, Map<String, Integer>>();
        collectAndGroupByStage(root, 1, grouped);
        List<ProductionNode> result = new ArrayList<>();
        for (Map.Entry<Integer, Map<String, Integer>> stageEntry : grouped.entrySet()) {
            int stage = stageEntry.getKey();
            ProductionNode header = new ProductionNode();
            header.setStageHeader(true);
            header.setStage(stage);
            result.add(header);
            for (Map.Entry<String, Integer> mat : stageEntry.getValue().entrySet()) {
                ProductionNode item = new ProductionNode();
                item.setTypeName(mat.getKey());
                item.setQuantity(mat.getValue());
                item.setStage(stage);
                item.setStageHeader(false);
                result.add(item);
            }
        }
        return result;
    }

    private void collectAndGroupByStage(ProductionNode node, int depth, Map<Integer, Map<String, Integer>> map) {
        for (ProductionNode child : node.getChildren()) {
            Map<String, Integer> stageMap = map.computeIfAbsent(depth, d -> new HashMap<>());
            int adjustedQty = assemblyState.recalculateBaseValue(child, child.getQuantity());
            stageMap.merge(child.getTypeName(), adjustedQty, Integer::sum);
            collectAndGroupByStage(child, depth + 1, map);
        }
    }

    private void collectAll(ProductionNode node, Map<String, Integer> map) {
        if (node.getChildren().isEmpty()) {
            int adjusted = assemblyState.recalculateBaseValue(node, node.getQuantity());
            map.merge(node.getTypeName(), adjusted, Integer::sum);
        }
        for (ProductionNode child : node.getChildren()) {
            collectAll(child, map);
        }
    }

    private Map<String, Integer> aggregateAllMaterials() {
        Map<String, Integer> aggregated = new HashMap<>();
        for (ProductionNode root : assemblyState.getRootNodes()) {
            collectAll(root, aggregated);
        }
        return aggregated;
    }

    private List<String> collectComponentsByStage(ProductionNode node, int targetStage) {
        List<String> components = new ArrayList<>();
        collectByStageRecursive(node, 1, targetStage, components);
        return components.stream().distinct().sorted().toList(); // удаляем дубли, сортируем
    }

    private void collectByStageRecursive(ProductionNode node, int currentStage, int targetStage, List<String> components) {
        if (currentStage == targetStage && node.getChildren() != null && !node.getChildren().isEmpty()) {
            components.add(node.getTypeName());
        }
        for (ProductionNode child : node.getChildren()) {
            collectByStageRecursive(child, currentStage + 1, targetStage, components);
        }
    }

    private void copyStage(ProductionNode stage) {
        List<ProductionNode> stageMaterials = flattenWithStagesGrouped(stage).stream()
                .filter(n -> !n.isStageHeader() && n.getStage() == stage.getStage())
                .collect(Collectors.toList());

        StringBuilder sb = new StringBuilder();
        for (ProductionNode n : stageMaterials) {
            sb.append(n.getTypeName()).append(" x ").append(n.getQuantity()).append("\n");
        }

        // скопировать в буфер или показать диалог
        Notification.show("Скопировано " + stageMaterials.size() + " компонентов");
    }

    private Image createIcon(String moduleName) {
        moduleName = parseModuleName(moduleName);
        Image icon = controller.getImageByInvTypeName(moduleName);
        icon.setWidth("24px");
        icon.setHeight("24px");
        return icon;
    }

    private String parseModuleName(String moduleName) {
        Matcher matcher = Pattern.compile("^(.*)\\s+x\\d+$").matcher(moduleName.trim());
        if (matcher.matches()) return matcher.group(1).trim();
        return moduleName.trim();
    }
}
