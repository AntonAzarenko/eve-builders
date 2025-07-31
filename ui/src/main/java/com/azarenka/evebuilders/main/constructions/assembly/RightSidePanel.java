package com.azarenka.evebuilders.main.constructions.assembly;

import com.azarenka.evebuilders.common.util.VaadinUtils;
import com.azarenka.evebuilders.component.SearchComponent;
import com.azarenka.evebuilders.component.View;
import com.azarenka.evebuilders.domain.GroupTypeEnum;
import com.azarenka.evebuilders.domain.db.DistributedOrder;
import com.azarenka.evebuilders.domain.db.Fit;
import com.azarenka.evebuilders.domain.sqllite.InvGroup;
import com.azarenka.evebuilders.domain.sqllite.InvType;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dnd.DragSource;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import org.apache.commons.lang3.StringUtils;
import org.vaadin.lineawesome.LineAwesomeIcon;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class RightSidePanel extends View {

    private final BuilderConstructionController controller;
    private final MiddleSidePanel middleSidePanel;
    private HorizontalLayout rightSideToolbar;
    private SearchComponent searchField;
    private VerticalLayout elementsLayout;

    public RightSidePanel(BuilderConstructionController controller, MiddleSidePanel middleSidePanel, DistributedOrder order, Fit fit) {
        this.middleSidePanel = middleSidePanel;
        this.controller = controller;
        initToolbar();
        getStyle().set("padding", "0px 5px");
        initPanel(order, fit);
    }

    void initToolbar() {
        rightSideToolbar = new HorizontalLayout();
        rightSideToolbar.setAlignItems(FlexComponent.Alignment.CENTER);
        rightSideToolbar.setWidthFull();
        searchField = new SearchComponent(getTranslation("management.search.placeholder"),
                event -> searchByText(searchField.getValue()),
                event -> clearSearch()
        );
        searchField.setWidth("70%");
        var addAllButton = VaadinUtils.createLumoButton(LineAwesomeIcon.ANGLE_DOUBLE_LEFT_SOLID);
        var clearButton = VaadinUtils.createLumoButton(VaadinIcon.TRASH);
        clearButton.addClickListener(event -> clearAllModules());
        addAllButton.addClickListener(event -> addAllModules());
        rightSideToolbar.add(addAllButton, clearButton, searchField);
        add(rightSideToolbar);
    }

    private void initPanel(DistributedOrder order, Fit fit) {
        elementsLayout = VaadinUtils.initCommonVerticalLayout();
        elementsLayout.addClassName("scrollable-column");
        if (Objects.nonNull(order)) {
            elementsLayout.add(createDraggableModule(order.getShipName()));
        }
        if (Objects.nonNull(fit)) {
            getModules(fit).stream()
                    .sorted(Comparator.naturalOrder())
                    .map(this::createDraggableModule)
                    .forEach(elementsLayout::add);
        }
        add(elementsLayout);
    }

    private List<String> getModules(Fit fit) {
        var lines = fit.getTextFit().split("\n");
        var modules = new ArrayList<String>();
        IntStream.range(1, lines.length).forEach(i -> {
            String line = lines[i];
            if (StringUtils.isNotBlank(line)) {
                modules.add(line);
            }
        });
        return modules;
    }

    private Component createDraggableModule(String moduleName) {
        var icon = createIcon(moduleName);
        var deleteButton = new Button(VaadinIcon.CLOSE.create());
        deleteButton.addClassName("delete-button");
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_SMALL);
        HorizontalLayout layout = new HorizontalLayout(new HorizontalLayout(icon, new Span(moduleName)), deleteButton);
        deleteButton.addClickListener(e ->
                layout.getParent().ifPresent(parent -> {
                    if (parent instanceof HasComponents) {
                        ((HasComponents) parent).remove(layout);
                    }
                }));
        layout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        layout.addClassName("draggable-item");
        layout.setWidth("95%");
        layout.setPadding(false);
        DragSource<HorizontalLayout> dragSource = DragSource.create(layout);
        dragSource.setDragData(moduleName);
        return layout;
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

    private void searchByText(String value) {
        GroupTypeEnum[] typeEnum = GroupTypeEnum.values();
        IntStream.range(0, typeEnum.length).forEach(i -> {
            var invGroupsById = controller.getInvGroupsById(typeEnum[i].getGroupId());
            var typesByGroupIds = controller.getTypesByGroupIds(invGroupsById.stream().map(InvGroup::getGroupID).toList());
            Optional<InvType> optionalInvType = typesByGroupIds.stream().filter(e -> e.getTypeName().equalsIgnoreCase(value)).findFirst();
            optionalInvType.ifPresent(invType -> elementsLayout.add(createDraggableModule(invType.getTypeName())));
        });
    }

    private void clearSearch() {
        searchField.clearText();
        searchByText("");
    }

    private void addAllModules() {
        elementsLayout.getChildren()
                .filter(component -> component instanceof HorizontalLayout)
                .map(component -> (HorizontalLayout) component)
                .filter(layout -> layout.getClassNames().contains("draggable-item"))
                .map(layout -> {
                    Span span = (Span) ((HorizontalLayout) layout.getComponentAt(0)).getComponentAt(1);
                    return span.getText();
                })
                .forEach(middleSidePanel::onModuleDragged);
    }

    private void clearAllModules() {
        elementsLayout.getChildren()
                .filter(component -> component instanceof HorizontalLayout)
                .map(component -> (HorizontalLayout) component)
                .filter(layout -> layout.getClassNames().contains("draggable-item"))
                .forEach(elementsLayout::remove);
    }

}
