package com.azarenka.evebuilders.main.managment.properties;

import com.azarenka.evebuilders.common.util.VaadinUtils;
import com.azarenka.evebuilders.component.View;
import com.azarenka.evebuilders.domain.db.Role;
import com.azarenka.evebuilders.domain.dto.UserDto;
import com.azarenka.evebuilders.main.managment.api.IPropertiesController;
import com.azarenka.evebuilders.main.menu.MenuManagerPageView;
import com.azarenka.evebuilders.service.impl.auth.SecurityUtils;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
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
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Route(value = "properties", layout = MenuManagerPageView.class)
@RolesAllowed({"ROLE_ADMIN", "ROLE_SUPER_ADMIN"})
public class PropertiesView extends View {

    private final IPropertiesController controller;
    private final H3 permissionsTitle = new H3("Пользователи и роли");
    private final TextField searchField = new TextField("Поиск пользователя");
    private final VerticalLayout userListLayout = VaadinUtils.initCommonVerticalLayout();
    private final Scroller scroller = new Scroller();

    public PropertiesView(@Autowired IPropertiesController controller) {
        this.controller = controller;
        super.setPadding(true);
        initContent();
    }

    private void initContent() {
        add(initPermissionsLayout(), new Hr());
    }

    private VerticalLayout initPermissionsLayout() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setMaxHeight("50%");
        layout.setWidthFull();
        layout.setSpacing(false);
        searchField.setPlaceholder("Введите имя");
        searchField.setClearButtonVisible(true);
        searchField.setWidth("400px");
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
        boolean isEditPermitted = SecurityUtils.getUserRoles().contains(Role.ROLE_SUPER_ADMIN);
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
        span.getStyle().setFontWeight("900");
        userInfo.add(span, new Span(user.getCharacterId()));

        HorizontalLayout rolesLayout = new HorizontalLayout();
        rolesLayout.setSpacing(true);
        for (Role role : user.getRoles()) {
            Span badge = new Span(role.name());
            badge.getElement().getThemeList().add("badge");
            rolesLayout.add(badge);
        }

        Button editRolesButton = new Button(VaadinIcon.EDIT.create());
        editRolesButton.setEnabled(isEditPermitted);
        editRolesButton.addClickListener(e -> openEditRolesDialog(user));

        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setAlignItems(FlexComponent.Alignment.CENTER);
        row.add(userInfo, rolesLayout, editRolesButton);
        row.expand(userInfo);
        userListLayout.add(row, new Hr());
    }

    public void openEditRolesDialog(UserDto user) {
        EditRolesDialog dialog = new EditRolesDialog(user,  selectedRoles -> {
            controller.updateUserRoles(user, selectedRoles);
            Notification.show("Роли обновлены", 3000, Notification.Position.MIDDLE);
            UI.getCurrent().refreshCurrentRoute(true);
        });
        dialog.open();
    }
}
