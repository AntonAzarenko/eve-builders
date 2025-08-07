package com.azarenka.evebuilders.main.constructions.assembly;

import com.azarenka.evebuilders.common.util.VaadinUtils;
import com.azarenka.evebuilders.component.PopupMenuBuilder;
import com.azarenka.evebuilders.component.PopupMenuComponent;
import com.azarenka.evebuilders.component.View;
import com.azarenka.evebuilders.domain.dto.ProductionNode;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dnd.DropTarget;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MiddleSidePanel extends View {

    private final BuilderConstructionController controller;
    private final AssemblyState assemblyState;
    private final LeftSidePanel leftSidePanel;
    private final Pattern pattern = Pattern.compile(".*\\s+x(\\d+)$");
    private final VerticalLayout mainlayout = VaadinUtils.initCommonVerticalLayout();

    public MiddleSidePanel(BuilderConstructionController controller, AssemblyState assemblyState,
                           LeftSidePanel leftSidePanel) {
        this.controller = controller;
        this.assemblyState = assemblyState;
        this.leftSidePanel = leftSidePanel;
        mainlayout.addClassName("assembly-area");
        mainlayout.addClassName("scrollable-column");
        mainlayout.getStyle().set("padding", "2px 5px");
        setSizeFull();
        getStyle().set("padding", "0px 5px");
        setWidth("70%");
        initToolbar();
        initPanel();
    }

    void initToolbar() {
        HorizontalLayout middleSideToolbar = new HorizontalLayout();
        middleSideToolbar.setAlignItems(FlexComponent.Alignment.CENTER);
        middleSideToolbar.setWidthFull();
        var clearButton = VaadinUtils.createLumoButton(VaadinIcon.TRASH);
        clearButton.addClickListener(event -> {
            mainlayout.removeAll();
            assemblyState.clearRoots();
            leftSidePanel.refresh();
        });
        clearButton.setTooltipText("Очистить все");
        middleSideToolbar.add(clearButton);
        add(middleSideToolbar);
    }

    private void initPanel() {
        add(mainlayout);
        DropTarget<VerticalLayout> dropTarget = DropTarget.create(this);
        dropTarget.addDropListener(event -> {
            String moduleName = (String) event.getDragData().orElse(null);
            if (moduleName != null) {
                onModuleDragged(moduleName);
            }
        });
    }

    public void onModuleDragged(String moduleName) {
        if (!assemblyState.isAlreadyRendered(moduleName)) {
            int count = parseModuleCount(moduleName);
            String pureName = parseModuleName(moduleName);
            ProductionNode root = controller.getProductionNode(pureName, 1);
            assemblyState.addModule(root, count);
            mainlayout.add(renderDroppedModule(root, pureName));
            leftSidePanel.refresh();
        } else {
            Map<ProductionNode, Integer> countMap = assemblyState.getCountMap();
            Optional<ProductionNode> first = countMap.keySet().stream()
                .filter(e -> e.getTypeName().equals(moduleName))
                .findFirst();
            first.ifPresent(productionNode -> countMap.compute(productionNode,  (k, integer) -> integer + 1));
            leftSidePanel.refresh();
        }
        assemblyState.recalculateRoots();
    }

    private HorizontalLayout renderDroppedModule(ProductionNode root, String moduleName) {
        var icon = createIcon(moduleName);
        var layout = new HorizontalLayout();
        var buttonsLayout = buildRenderDroppedModuleButtonsLayout(root, layout);
        var content = new HorizontalLayout(icon, new Span(moduleName));
        layout.add(content, buttonsLayout);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        layout.addClassName("assembly-module");
        layout.setWidthFull();
        layout.setPadding(false);
        return layout;
    }

    private HorizontalLayout buildRenderDroppedModuleButtonsLayout(ProductionNode root, HorizontalLayout layout) {
        var popupEfficiencyMenu = initPopupEfficiencyMenu(root);
        getUI().ifPresent(ui -> ui.add(popupEfficiencyMenu));
        var bluePrintPropertiesButton = popupEfficiencyMenu.getOpenMenuButton();
        var deleteButton = VaadinUtils.createLumoButton(VaadinIcon.CLOSE);
        deleteButton.addClassName("delete-button");
        deleteButton.addClickListener(e ->
            layout.getParent().ifPresent(parent -> {
                if (parent instanceof HasComponents) {
                    ((HasComponents) parent).remove(layout);
                    assemblyState.removeModule(root);
                    leftSidePanel.refresh();
                }
            }));
        return new HorizontalLayout(bluePrintPropertiesButton, deleteButton);
    }

    private PopupMenuComponent initPopupEfficiencyMenu(ProductionNode root) {
        var tooltip = "Установите улучшение чертежа для правильного отображения количества материалов";
        var efficiencyField = new IntegerField("Экономия материалов %");
        var countIntegerField = new IntegerField("Количество");
        return new PopupMenuBuilder()
            .withTitle("Настройка чертежа")
            .withComponent(countIntegerField)
            .withComponent(efficiencyField)
            .withTooltip(tooltip)
            .withIcon(VaadinIcon.COG)
            .onApply(keyPressEvent -> {
                var efficiencyFieldValue = efficiencyField.getValue();
                var countIntegerFieldValue = countIntegerField.getValue();
                assemblyState.getCountMap().compute(root, (k, integer) -> Objects.isNull(countIntegerFieldValue) ? integer : countIntegerFieldValue);
                assemblyState.getEfficiencyMap().put(root, Objects.isNull(efficiencyFieldValue) ? 0 : Double.valueOf(efficiencyFieldValue));
                leftSidePanel.refresh();
            }).build();
    }

    private Image createIcon(String moduleName) {
        moduleName = parseModuleName(moduleName);
        Image icon = controller.getImageByInvTypeName(moduleName);
        icon.setWidth("24px");
        icon.setHeight("24px");
        return icon;
    }

    private int parseModuleCount(String moduleName) {
        var matcher = pattern.matcher(moduleName);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return 1;
    }

    private String parseModuleName(String moduleName) {
        var matcher = Pattern.compile("^(.*)\\s+x\\d+$").matcher(moduleName.trim());
        if (matcher.matches()) {
            return matcher.group(1).trim();
        }
        return moduleName.trim();
    }
}
