package com.azarenka.evebuilders.main.staff;

import com.azarenka.evebuilders.domain.db.Role;
import com.azarenka.evebuilders.domain.dto.UserDto;
import com.azarenka.evebuilders.main.commonview.CommonDialogComponent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.checkbox.CheckboxGroupVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.Set;
import java.util.function.Consumer;

public class EditRolesDialog extends CommonDialogComponent {

    private final CheckboxGroup<Role> rolesCheckboxGroup = new CheckboxGroup<>();
    private final Button saveButton = new Button(VaadinIcon.CHECK.create());
    private final Consumer<Set<Role>> onSave;

    public EditRolesDialog(UserDto user, Consumer<Set<Role>> onSave) {
        this.onSave = onSave;
        applyCommonProperties("role-change-window", false);
        super.setHeaderTitle(user.getUsername());
        super.setWidth("400px");
        add(initContent(user));
        getFooter().add(initButtonsLayout());
    }

    private VerticalLayout initContent(UserDto user) {
        Role[] values = Role.values();
        rolesCheckboxGroup.setLabel("Доступные роли");
        rolesCheckboxGroup.setItems(values);
        rolesCheckboxGroup.setItemLabelGenerator(Role::name);
        rolesCheckboxGroup.select(user.getRoles());
        rolesCheckboxGroup.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);
        VerticalLayout content = new VerticalLayout(rolesCheckboxGroup);
        content.setSpacing(true);
        content.setPadding(true);
        return content;
    }

    private HorizontalLayout initButtonsLayout() {
        saveButton.setIcon(VaadinIcon.CHECK.create());
        saveButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        saveButton.addClickListener(e -> {
            onSave.accept(rolesCheckboxGroup.getSelectedItems());
            close();
        });
        HorizontalLayout footer = new HorizontalLayout(createCloseButton(), saveButton);
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        footer.setWidthFull();
        return footer;
    }
}
