package com.azarenka.evebuilders.main.constructions;

import com.azarenka.evebuilders.component.SearchComponent;
import com.azarenka.evebuilders.component.View;
import com.azarenka.evebuilders.domain.GroupTypeEnum;
import com.azarenka.evebuilders.domain.ModuleSlot;
import com.azarenka.evebuilders.domain.db.DistributedOrder;
import com.azarenka.evebuilders.domain.db.Fit;
import com.azarenka.evebuilders.domain.db.Module;
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
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class RightSidePanel extends View {

    private final BuilderConstructionController controller;
    private HorizontalLayout rightSideToolbar;
    private SearchComponent searchField;

    public RightSidePanel(BuilderConstructionController controller, MiddleSidePanel middleSidePanel, DistributedOrder order, Fit fit) {
        this.controller = controller;
        setSizeFull();
        setClassName("scrollable-column");
        initToolbar();
        initPanel(order, fit, middleSidePanel);
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
        rightSideToolbar.add(searchField);
        add(rightSideToolbar);
    }

    private void initPanel(DistributedOrder order, Fit fit, MiddleSidePanel middleSidePanel) {
        if (Objects.nonNull(order)) {
            add(createDraggableModule(order.getShipName()));
        }
        if (Objects.nonNull(fit)) {
            getModules(fit).stream()
                    .sorted(Comparator.comparing(Module::getModuleName))
                    .map(module -> createDraggableModule(module.getModuleName()))
                    .forEach(this::add);
        }
    }

    private List<Module> getModules(Fit fit) {
        String[] lines = fit.getTextFit().split("\n");
        List<Module> modules = new ArrayList<>();
        IntStream.range(1, lines.length).forEach(i -> {
            String line = lines[i];
            if (StringUtils.isNotBlank(line)) {
                Module module = new Module();
                module.setId(UUID.randomUUID().toString());
                module.setModuleName(line);
                module.setModuleSlot(ModuleSlot.HIGH_SLOT);
                modules.add(module);
            }
        });
        return modules;
    }

    private Component createDraggableModule(String moduleName) {
        var icon = createIcon(moduleName);
        var deleteButton = new Button(VaadinIcon.CLOSE.create());
        deleteButton.addClassName("delete-button");
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY_INLINE);
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
            optionalInvType.ifPresent(invType -> add(createDraggableModule(invType.getTypeName())));
        });
    }

    private void clearSearch() {
        searchField.clearText();
        searchByText("");
    }
}
