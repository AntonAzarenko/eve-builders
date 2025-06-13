package com.azarenka.evebuilders.main.constructions;

import com.azarenka.evebuilders.common.util.VaadinUtils;
import com.azarenka.evebuilders.component.PopupMenuComponent;
import com.azarenka.evebuilders.component.View;
import com.azarenka.evebuilders.domain.ModuleSlot;
import com.azarenka.evebuilders.domain.db.DistributedOrder;
import com.azarenka.evebuilders.domain.db.Fit;
import com.azarenka.evebuilders.domain.db.Module;
import com.azarenka.evebuilders.domain.dto.ProductionNode;
import com.azarenka.evebuilders.domain.dto.ViewMode;
import com.azarenka.evebuilders.domain.sqllite.InvType;
import com.azarenka.evebuilders.main.menu.MenuConstructionPageView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dnd.DragSource;
import com.vaadin.flow.component.dnd.DropTarget;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.shared.SelectionPreservationMode;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import jakarta.annotation.security.RolesAllowed;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Route(value = "build_order", layout = MenuConstructionPageView.class)
@RolesAllowed({"ROLE_ADMIN", "ROLE_SUPER_ADMIN", "ROLE_USER"})
@PageTitle("Build Order")
public class BuilderConstructionView extends View implements LocaleChangeObserver {

    private static final Pattern PATTERN = Pattern.compile("x(\\d+)$");

    private final BuilderConstructionController controller;
    private final Map<ProductionNode, Double> addedModulesToEfficiencyMap = new HashMap<>();
    private final Map<ProductionNode, Integer> addedModulesToCountMap = new HashMap<>();
    private double baseSotiyoBenefitPercentage = 4.2;
    private final TreeGrid<ProductionNode> treeGrid = createTreeGrid();
    private final Grid<ProductionNode> listGrid = createGrid();
    private final List<ProductionNode> rootNodes = new ArrayList<>();
    private final Set<String> rendererModules = new HashSet<>();
    private final HorizontalLayout mainLayout = new HorizontalLayout();
    private final RadioButtonGroup<ViewMode> viewModeSelector = new RadioButtonGroup<>();

    private Fit fit;
    private DistributedOrder order;

    private VerticalLayout leftSidePanel;
    private HorizontalLayout leftSideToolbar;
    private VerticalLayout middleSidePanel;
    private VerticalLayout rightSidePanel;


    public BuilderConstructionView(@Autowired BuilderConstructionController controller) {
        this.controller = controller;
        this.order = (DistributedOrder) VaadinSession.getCurrent().getAttribute("currentOrder");
        this.fit = (Fit) VaadinSession.getCurrent().getAttribute("currentFit");
        initView();
    }

    private void initView() {
        initLeftSidePanel();
        initMiddleSidePanel();
        initRightSidePanel();
        Div divider1 = new Div();
        divider1.addClassName("vertical-divider");
        Div divider2 = new Div();
        divider2.addClassName("vertical-divider");
        mainLayout.add(leftSidePanel, divider1, middleSidePanel, divider2, rightSidePanel);
        mainLayout.setFlexGrow(1, leftSidePanel, middleSidePanel, rightSidePanel);
        mainLayout.setSizeFull();
        add(mainLayout);
    }

    private void initLeftSidePanel() {
        initRadioButtonView();
        leftSidePanel = VaadinUtils.initCommonVerticalLayout();
        leftSidePanel.setWidthFull();
        leftSidePanel.addClassName("scrollable-column");
        leftSideToolbar = new HorizontalLayout(viewModeSelector);
        leftSideToolbar.setAlignItems(FlexComponent.Alignment.CENTER);
        leftSideToolbar.setWidthFull();
        leftSidePanel.add(leftSideToolbar);
    }

    private void initRadioButtonView() {
        viewModeSelector.setItems(ViewMode.LIST, ViewMode.TREE);
        viewModeSelector.setRenderer(new ComponentRenderer<>(mode -> {
            Icon icon = switch (mode) {
                case LIST -> new Icon(VaadinIcon.LIST);
                case TREE -> new Icon(VaadinIcon.ARCHIVES);
            };
            icon.setSize("20px");
            return icon;
        }));
        viewModeSelector.setValue(ViewMode.TREE);
        viewModeSelector.addValueChangeListener(event -> {
            leftSidePanel.removeAll();
            leftSidePanel.add(leftSideToolbar);
            if (Objects.requireNonNull(event.getValue()) == ViewMode.LIST) {
                leftSidePanel.add(buildListView());
            } else {
                leftSidePanel.add(buildTreeGrid());
            }
        });
    }

    private void initMiddleSidePanel() {
        middleSidePanel = VaadinUtils.initCommonVerticalLayout();
        middleSidePanel.addClassName("assembly-area");
        middleSidePanel.addClassName("scrollable-column");
        DropTarget<VerticalLayout> dropTarget = DropTarget.create(middleSidePanel);
        dropTarget.addDropListener(event -> {
            String moduleName = (String) event.getDragData().orElse(null);
            if (moduleName != null) {
                if (!rendererModules.contains(moduleName)) {
                    rendererModules.add(moduleName);
                    Component droppedModule = renderDroppedModule(moduleName);
                    middleSidePanel.add(droppedModule);
                } else {
                    Optional<ProductionNode> first = addedModulesToCountMap.keySet().stream()
                            .filter(e -> e.getTypeName().equals(moduleName))
                            .findFirst();
                    first.ifPresent(e -> addedModulesToCountMap.put(e, addedModulesToCountMap.get(e) + 1));
                }
                updateMaterials();
            }
        });
    }

    private void updateMaterials() {
        ViewMode value = viewModeSelector.getValue();
        leftSidePanel.removeAll();
        leftSidePanel.add(leftSideToolbar);
        if (Objects.requireNonNull(value) == ViewMode.LIST) {
            leftSidePanel.add(buildListView());
        } else if (value == ViewMode.TREE) {
            leftSidePanel.add(buildTreeGrid());
        }
    }

    private Component buildListView() {

        rootNodes.clear();
        rootNodes.addAll(addedModulesToCountMap.entrySet().stream().map(Map.Entry::getKey).toList());
        if (rootNodes.size() > 0) {
            listGrid.setItems(flattenWithStagesGrouped(rootNodes.get(0)));
        }
        return listGrid;
        /*VerticalLayout list = VaadinUtils.initCommonVerticalLayout();
        list.setPadding(false);
        list.setSpacing(false);
        rootNodes.clear();
        rootNodes.addAll(addedModulesToCountMap.entrySet().stream().map(Map.Entry::getKey).toList());
        for (ProductionNode root : rootNodes) {
            list.add(buildHeader("Финальная сборка: " + root.getTypeName(), "final-assembly"));
            Map<Integer, Map<String, Integer>> materialsByStage = new TreeMap<>();
            collectAndGroupByStage(root, 1, materialsByStage);
            for (Map.Entry<Integer, Map<String, Integer>> entry : materialsByStage.entrySet()) {
                int stage = entry.getKey();
                list.add(buildHeader("Этап " + stage, "stage-header"));
                entry.getValue().entrySet().stream()
                        .sorted(Map.Entry.comparingByKey())
                        .forEach(material -> {
                            String name = material.getKey();
                            int qty = material.getValue();
                            list.add(buildMaterialLine(name, qty));
                        });
            }
        }
        return list;*/
    }

    private void collectAndGroupByStage(ProductionNode node, int depth, Map<Integer, Map<String, Integer>> map) {
        for (ProductionNode child : node.getChildren()) {
            Map<String, Integer> stageMap = map.computeIfAbsent(depth, d -> new HashMap<>());
            int adjustedQty = recalculateBaseValue(child, child.getQuantity());
            stageMap.merge(child.getTypeName(), adjustedQty, Integer::sum);
            collectAndGroupByStage(child, depth + 1, map);
        }
    }

    private HorizontalLayout buildHeader(String text, String className) {
        Span label = new Span(text);
        label.getStyle().set("font-weight", "bold");
        HorizontalLayout layout = new HorizontalLayout(label);
        layout.addClassName(className);
        layout.setWidthFull();
        layout.getStyle().set("background-color", "#f7f7f7");
        return layout;
    }

    private HorizontalLayout buildMaterialLine(String name, int quantity) {
        Image icon = createIcon(name);
        Span nameSpan = new Span(name);
        Span qtySpan = new Span(String.valueOf(quantity));
        HorizontalLayout materialLayout = new HorizontalLayout(icon, nameSpan);
        HorizontalLayout countLayout = new HorizontalLayout(qtySpan);
        HorizontalLayout line = new HorizontalLayout(materialLayout, countLayout);
        line.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        line.setWidthFull();
        line.addClassName("draggable-item");
        return line;
    }

    private Component buildTreeGrid() {
        rootNodes.clear();
        rootNodes.addAll(addedModulesToCountMap.entrySet().stream().map(Map.Entry::getKey).toList());
        treeGrid.setItems(rootNodes, ProductionNode::getChildren);
        treeGrid.setSelectionPreservationMode(SelectionPreservationMode.PRESERVE_ALL);
        return treeGrid;
    }

    private int recalculateBaseValue(ProductionNode node, Integer value) {
        ProductionNode root = findRoot(node);
        Double blueprintBonus = addedModulesToEfficiencyMap.get(node.getParent());
        int count = value;
        if (!root.equals(node)) {
            count = value - (int) Math.round((double) value / 100 * baseSotiyoBenefitPercentage);
            if (blueprintBonus != null && blueprintBonus > 0) {
                count = value - (int) Math.round((double) value / 100 * (blueprintBonus + baseSotiyoBenefitPercentage));
            }
        }
        Integer rootCount = addedModulesToCountMap.get(root);
        return count * (rootCount != null ? rootCount : 1);
    }

    private void initRightSidePanel() {
        rightSidePanel = VaadinUtils.initCommonVerticalLayout();
        if (Objects.nonNull(order)) {
            rightSidePanel.add(createDraggableModule(order.getShipName()));
        }
        if (Objects.nonNull(fit)) {
            List<Module> modules = getModules(fit);
            rightSidePanel.addClassName("scrollable-column");
            rightSidePanel.add(modules.stream()
                    .sorted(Comparator.comparing(Module::getModuleName))
                    .map(module -> createDraggableModule(module.getModuleName())).toList());
        }
    }

    private List<Module> getModules(Fit fit) {
        String textFit = fit.getTextFit();
        String[] lines = textFit.split("\n");
        final List<Module> modules = new ArrayList<>();
        if (lines.length > 0) {
            IntStream.range(0, lines.length).forEach(i -> {
                String line = lines[i];
                if (StringUtils.isNotBlank(line)) {
                    modules.add(createModule(lines[i]));
                }
            });
        }
        return modules;
    }

    private Module createModule(String line) {
        Module module = new Module();
        module.setId(UUID.randomUUID().toString());
        module.setModuleName(line);
        module.setModuleSlot(ModuleSlot.HIGH_SLOT);
        return module;
    }

    private InvType getInvTypeForModule(String name) {
        return controller.getInvTypeByTypeName(name);
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {

    }

    public void setFit(Fit fit) {
        this.fit = fit;
    }

    public void setOrder(DistributedOrder order) {
        this.order = order;
    }

    private Component createDraggableModule(String moduleName) {
        Image icon = createIcon(moduleName);
        HorizontalLayout moduleLayout = new HorizontalLayout(icon, new Span(moduleName));
        moduleLayout.addClassName("draggable-item");
        moduleLayout.setWidth("95%");
        moduleLayout.setPadding(false);
        DragSource<HorizontalLayout> dragSource = DragSource.create(moduleLayout);
        dragSource.setDragData(moduleName);
        return moduleLayout;
    }

    private Component renderDroppedModule(String moduleName) {
        Image icon = createIcon(moduleName);
        boolean b = checkModuleIsMultipleCount(moduleName);
        int count = b ? Integer.parseInt(moduleName.substring(moduleName.indexOf("x") + 1, moduleName.length() - 1)) : 1;
        moduleName = b ? moduleName.substring(0, moduleName.indexOf("x")) : moduleName;
        ProductionNode root = controller.getProductionNode(moduleName, 1);
        addedModulesToEfficiencyMap.put(root, 0d);
        addedModulesToCountMap.put(root, count);
        HorizontalLayout layout = new HorizontalLayout();
        HorizontalLayout buttonsLayout = buildRenderDroppedModuleButtonsLayout(root, layout);
        HorizontalLayout content = new HorizontalLayout(icon, new Span(moduleName));
        layout.add(content, buttonsLayout);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        layout.addClassName("assembly-module");
        layout.setWidthFull();
        layout.setPadding(false);
        return layout;
    }

    boolean checkModuleIsMultipleCount(String moduleName) {
        Matcher matcher = PATTERN.matcher(moduleName);
        return matcher.find();
    }

    private Image createIcon(String moduleName) {
        Image icon = controller.getImageByInvTypeName(moduleName);
        icon.setWidth("24px");
        icon.setHeight("24px");
        return icon;
    }

    private HorizontalLayout buildRenderDroppedModuleButtonsLayout(ProductionNode root, HorizontalLayout layout) {
        String tooltip = "Установите улучшение материала на блюпринте для правильного отображения количества материалов";
        IntegerField efficiencyField = new IntegerField("Экономия материалов %");
        PopupMenuComponent popupEfficiencyMenu = new PopupMenuComponent(root.getTypeName(), efficiencyField, VaadinIcon.COG, tooltip,
                keyPressEvent -> {
                    Integer value = efficiencyField.getValue();
                    addedModulesToEfficiencyMap.put(root, Objects.isNull(value) ? 0 : Double.valueOf(value));
                    updateMaterials();
                });
        Button bluePrintProperties = popupEfficiencyMenu.getOpenMenuButton();
        IntegerField countIntegerField = new IntegerField("Количество");
        PopupMenuComponent popupCountMenu = new PopupMenuComponent(root.getTypeName(), countIntegerField, VaadinIcon.DROP,
                "", event -> {
            Integer value = countIntegerField.getValue();
            Integer integer = addedModulesToCountMap.get(root);
            addedModulesToCountMap.put(root, Objects.isNull(value) ? integer : value);
            updateMaterials();
        });
        getUI().ifPresent(ui -> ui.add(popupEfficiencyMenu));
        getUI().ifPresent(ui -> ui.add(popupCountMenu));
        Button countMenuButton = popupCountMenu.getOpenMenuButton();
        Button deleteButton = new Button(VaadinIcon.CLOSE.create());
        deleteButton.addClassName("delete-button");
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY_INLINE);
        deleteButton.addClickListener(e -> {
            layout.getParent().ifPresent(parent -> {
                if (parent instanceof HasComponents) {
                    ((HasComponents) parent).remove(layout);
                    addedModulesToEfficiencyMap.remove(root);
                    addedModulesToCountMap.remove(root);
                    rendererModules.remove(root.getTypeName());
                    rootNodes.remove(root);
                    updateMaterials();
                }
            });
        });
        HorizontalLayout buttonsLayout = new HorizontalLayout(countMenuButton, bluePrintProperties, deleteButton);
        return buttonsLayout;
    }

    private TreeGrid<ProductionNode> createTreeGrid() {
        TreeGrid<ProductionNode> grid = new TreeGrid<>();
        grid.setWidthFull();
        grid.addComponentHierarchyColumn(node -> {
            Image icon = createIcon(node.getTypeName());
            HorizontalLayout layout = new HorizontalLayout(icon, new Span(node.getTypeName()));
            layout.setAlignItems(FlexComponent.Alignment.CENTER);
            return layout;
        }).setHeader("Компонент").setAutoWidth(true).setResizable(true);
        grid.addColumn(value -> recalculateBaseValue(value, value.getQuantity()))
                .setHeader("Кол-во")
                .setAutoWidth(true);
        grid.addThemeVariants(GridVariant.LUMO_COMPACT);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);
        return grid;
    }

    private Grid<ProductionNode> createGrid() {
        Grid<ProductionNode> grid = VaadinUtils.initGrid("list-grid");
        grid.addComponentColumn(node -> {
            if (node.isStageHeader()) {
                Button button = new Button(VaadinIcon.COPY.create(), e -> copyStage(node));
                button.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_SMALL);
                Span span = new Span("Этап " + node.getStage() + ". " + node.getTypeName() + " " + node.getQuantity());
                HorizontalLayout layout = new HorizontalLayout(span, button);
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

    public List<ProductionNode> flattenWithStagesGrouped(ProductionNode root) {
        Map<Integer, Map<String, Integer>> grouped = new TreeMap<>();
        collectAndGroupByStage(root, 1, grouped);

        List<ProductionNode> result = new ArrayList<>();

        for (Map.Entry<Integer, Map<String, Integer>> stageEntry : grouped.entrySet()) {
            int stage = stageEntry.getKey();

            // Добавляем заголовок этапа
            ProductionNode header = new ProductionNode();
            header.setStageHeader(true);
            header.setStage(stage);
            result.add(header);

            // Добавляем строки материалов
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


    public ProductionNode findRoot(ProductionNode node) {
        while (node.getParent() != null) {
            node = node.getParent();
        }
        return node;
    }
}
