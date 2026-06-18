package com.azarenka.evebuilders.main.staff;

import com.azarenka.evebuilders.common.util.VaadinUtils;
import com.azarenka.evebuilders.component.View;
import com.azarenka.evebuilders.domain.dto.UserDto;
import com.azarenka.evebuilders.main.managment.api.IStaffController;
import com.azarenka.evebuilders.main.menu.MenuStaffPage;
import com.azarenka.evebuilders.common.util.SpringContextHolder;
import com.azarenka.evebuilders.service.impl.auth.eve.AccessControlSecurity;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.router.Route;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Route(value = "properties", layout = MenuStaffPage.class)
@PreAuthorize("@accessControlSecurity.can('CORPORATION_CONTRACT_EDIT')")
public class StaffProperties extends View {

    private final IStaffController controller;
    private final H3 permissionsTitle = new H3("Пользователи и роли");
    private final TextField searchField = new TextField("Поиск пользователя");
    private final VerticalLayout userListLayout = VaadinUtils.initCommonVerticalLayout();
    private final Scroller scroller = new Scroller();

    public StaffProperties(@Autowired IStaffController controller) {
        this.controller = controller;
        super.setPadding(true);
        super.add(initPermissionsLayout());
    }

    private VerticalLayout initPermissionsLayout() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setWidthFull();
        layout.setSpacing(false);
        searchField.setPlaceholder("Введите имя");
        searchField.setClearButtonVisible(true);
        searchField.setWidth("400px");
        searchField.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        searchField.addValueChangeListener(e -> updateUserList());
        scroller.setContent(userListLayout);
        scroller.setWidthFull();
        layout.add(permissionsTitle, searchField, scroller);
        updateUserList();
        return layout;
    }

    private void updateUserList() {
        userListLayout.removeAll();
        List<UserDto> users = controller.getAllUsers();
        String filter = searchField.getValue() != null ? searchField.getValue().toLowerCase() : "";
        boolean isEditPermitted = SpringContextHolder.getBean(AccessControlSecurity.class)
            .can("CORPORATION_CONTRACT_EDIT");
        users.stream()
                .filter(u -> u.getUsername().toLowerCase().contains(filter) ||
                        u.getCharacterId().toLowerCase().contains(filter))
                .forEach(userDto -> addUserRow(userDto, isEditPermitted));
    }

    private void addUserRow(UserDto user, boolean isEditPermitted) {
        VerticalLayout userInfo = VaadinUtils.initCommonVerticalLayout();
        userInfo.setPadding(false);
        userInfo.setSpacing(false);
        Span span = new Span(user.getUsername());
        span.getStyle().setFontWeight("700");
        userInfo.add(span, new Span(user.getCharacterId()));

        HorizontalLayout rolesLayout = new HorizontalLayout();
        rolesLayout.setSpacing(true);
        for (com.azarenka.evebuilders.domain.db.Role role : user.getRoles()) {
            Span badge = new Span(role.name());
            badge.getElement().getThemeList().add("badge");
            rolesLayout.add(badge);
        }

        Button editRolesButton = new Button(VaadinIcon.EDIT.create());
        editRolesButton.setEnabled(isEditPermitted);
        editRolesButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        editRolesButton.addClickListener(e -> openEditRolesDialog(user));

        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setAlignItems(FlexComponent.Alignment.CENTER);
        row.add(userInfo, rolesLayout, editRolesButton);
        row.expand(userInfo);
        userListLayout.add(row, new Hr());
    }

    public void openEditRolesDialog(UserDto user) {
        EditRolesDialog dialog = new EditRolesDialog(user, selectedRoles -> {
            controller.updateUserRoles(user, selectedRoles);
            Notification.show("Роли обновлены", 3000, Notification.Position.MIDDLE);
            UI.getCurrent().refreshCurrentRoute(true);
        });
        dialog.open();
    }
}
