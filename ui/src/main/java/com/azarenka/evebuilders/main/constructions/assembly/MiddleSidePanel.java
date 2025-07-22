package com.azarenka.evebuilders.main.constructions.assembly;

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

    public MiddleSidePanel(BuilderConstructionController controller, AssemblyState assemblyState,
                           LeftSidePanel leftSidePanel) {
        this.controller = controller;
        this.assemblyState = assemblyState;
        this.leftSidePanel = leftSidePanel;
        setClassName("assembly-area");
        //setClassName("scrollable-column");
        setSizeFull();
        setWidth("70%");
        initPanel();
    }

    private void initPanel() {
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
            add(renderDroppedModule(root, pureName));
            leftSidePanel.refresh();
        } else {
            Map<ProductionNode, Integer> countMap = assemblyState.getCountMap();
            Optional<ProductionNode> first = countMap.keySet().stream()
                .filter(e -> e.getTypeName().equals(moduleName))
                .findFirst();
            first.ifPresent(e -> countMap.put(e, countMap.get(e) + 1));
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
        var popupCountMenu = initPopupCountMenu(root);
        getUI().ifPresent(ui -> ui.add(popupEfficiencyMenu));
        getUI().ifPresent(ui -> ui.add(popupCountMenu));
        var countMenuButton = popupCountMenu.getOpenMenuButton();
        var bluePrintPropertiesButton = popupEfficiencyMenu.getOpenMenuButton();
        var deleteButton = new Button(VaadinIcon.CLOSE.create());
        deleteButton.addClassName("delete-button");
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY_INLINE);
        deleteButton.addClickListener(e ->
            layout.getParent().ifPresent(parent -> {
                if (parent instanceof HasComponents) {
                    ((HasComponents) parent).remove(layout);
                    assemblyState.removeModule(root);
                    leftSidePanel.refresh();
                }
            }));
        return new HorizontalLayout(countMenuButton, bluePrintPropertiesButton, deleteButton);
    }

    private PopupMenuComponent initPopupEfficiencyMenu(ProductionNode root) {
        var tooltip = "Установите улучшение чертежа для правильного отображения количества материалов";
        var efficiencyField = new IntegerField("Экономия материалов %");
        return new PopupMenuBuilder().withComponent(efficiencyField)
            .withTooltip(tooltip)
            .withIcon(VaadinIcon.COG)
            .onApply(keyPressEvent -> {
                var value = efficiencyField.getValue();
                assemblyState.getEfficiencyMap().put(root, Objects.isNull(value) ? 0 : Double.valueOf(value));
                leftSidePanel.refresh();
            }).build();
    }

    private PopupMenuComponent initPopupCountMenu(ProductionNode root) {
        var countIntegerField = new IntegerField("Количество");
        return new PopupMenuBuilder().withComponent(countIntegerField)
            .withTooltip("Установить количество компонентов")
            .withIcon(VaadinIcon.DROP)
            .onApply(event -> {
                var value = countIntegerField.getValue();
                assemblyState.getCountMap().compute(root, (k, integer) -> Objects.isNull(value) ? integer : value);
                leftSidePanel.refresh();
            })
            .build();
    }

    private Image createIcon(String moduleName) {
        moduleName = parseModuleName(moduleName);
        Image icon = controller.getImageByInvTypeName(moduleName);
        icon.setWidth("24px");
        icon.setHeight("24px");
        return icon;
    }

    private int parseModuleCount(String moduleName) {
        Matcher matcher = pattern.matcher(moduleName);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return 1;
    }

    private String parseModuleName(String moduleName) {
        Matcher matcher = Pattern.compile("^(.*)\\s+x\\d+$").matcher(moduleName.trim());
        if (matcher.matches()) {
            return matcher.group(1).trim();
        }
        return moduleName.trim();
    }
}
