package com.azarenka.evebuilders.main.constructions.assembly;

import com.azarenka.evebuilders.common.util.VaadinUtils;
import com.azarenka.evebuilders.component.View;
import com.azarenka.evebuilders.domain.dto.ItemDto;
import com.azarenka.evebuilders.domain.dto.ProductionNode;
import com.azarenka.evebuilders.domain.dto.ViewMode;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.shared.SelectionPreservationMode;
import com.vaadin.flow.component.treegrid.TreeGrid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class LeftSidePanel extends View {

    private final AssemblyState assemblyState;
    private final BuilderConstructionController controller;
    private final TreeGrid<ProductionNode> treeGrid = initTreeGrid();
    private final Grid<Map.Entry<String, Integer>> summaryGrid = initSummaryGrid();

    private Button listViewButton;
    private Button treeViewButton;
    private Button summorizeViewButton;
    private Button showMineralsButton;
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
        showMineralsButton = new Button("Show Minerals");
        listViewButton = new Button(VaadinIcon.LIST.create());
        treeViewButton = new Button(VaadinIcon.ARCHIVES.create());
        summorizeViewButton = new Button(VaadinIcon.ABACUS.create());
        listViewButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ICON);
        treeViewButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ICON);
        summorizeViewButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ICON);
        listViewButton.addClickListener(event -> changeViewMode(ViewMode.LIST));
        treeViewButton.addClickListener(event -> changeViewMode(ViewMode.TREE));
        summorizeViewButton.addClickListener(event -> changeViewMode(ViewMode.SUMMARY));
        showMineralsButton.addClickListener(event -> {
            Dialog dialog = new Dialog();
            dialog.setWidth("500px");
            dialog.setHeight("500px");
            List<ItemDto> minerals = controller.getMinerals(assemblyState.getRootNodes().stream()    // List<ProductionNode> корней
                .flatMap(LeftSidePanel::deepStream)  // разворачиваем всё дерево
                .map(ProductionNode::getTypeName)
                .toList());
            minerals.forEach(dto -> {
                dialog.add(new HorizontalLayout(new Span(String.valueOf(dto.getInvType().getTypeName())), new Span(
                    String.valueOf(dto.getAsset().getQuantity()))), new Span(dto.getUserName()));
            });
            dialog.open();
        });
        leftSideToolbar.add(treeViewButton, listViewButton, summorizeViewButton, showMineralsButton);
    }

    private static Stream<ProductionNode> deepStream(ProductionNode node) {
        return Stream.concat(
            Stream.of(node),
            node.getChildren().stream()
                .flatMap(LeftSidePanel::deepStream)
        );
    }

    private void changeViewMode(ViewMode viewMode) {
        stateViewMode = viewMode;
        refresh();
    }

    public void refresh() {
        removeAll();
        add(leftSideToolbar);
        switch (stateViewMode) {
            case LIST -> add(new ListItemView(assemblyState, controller));
            case TREE -> add(buildTreeGrid());
            case SUMMARY -> add(buildSummaryView());
        }
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
        Button copyButton = new Button("Скопировать", e -> {
            StringBuilder sb = new StringBuilder();
            aggregated.forEach((name, qty) -> sb.append(name).append(" ").append(qty).append("\n"));
            VaadinUtils.copyToClipboard(summaryGrid, sb.toString(),
                    String.format("Скопировано айтемов %s", aggregated.entrySet().stream().count()));
        });
        var layout = VaadinUtils.initCommonVerticalLayout();
        layout.add(copyButton, summaryGrid);
        layout.setSizeFull();
        return layout;
    }

    private void collectAll(ProductionNode node, Map<String, Integer> map) {
        if (assemblyState.getExcludedNodes().contains(node)) {
            return;
        }
        if (node.getChildren().isEmpty()) {
            int adjusted = node.getFinalQuantity();
            map.merge(node.getTypeName(), adjusted, Integer::sum);
        }
        node.getChildren().forEach(child -> collectAll(child, map));
    }

    private Map<String, Integer> aggregateAllMaterials() {
        Map<String, Integer> aggregated = new HashMap<>();
        for (ProductionNode root : assemblyState.getRootNodes()) {
            collectAll(root, aggregated);
        }
        return aggregated;
    }

    private Image createIcon(String moduleName) {
        var icon = controller.getImageByInvTypeName(moduleName);
        icon.setWidth("25px");
        icon.setHeight("25px");
        return icon;
    }
}
