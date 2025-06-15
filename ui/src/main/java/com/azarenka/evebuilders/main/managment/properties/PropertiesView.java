package com.azarenka.evebuilders.main.managment.properties;

import com.azarenka.evebuilders.common.util.VaadinUtils;
import com.azarenka.evebuilders.component.View;
import com.azarenka.evebuilders.domain.db.ApplicationProperties;
import com.azarenka.evebuilders.domain.db.Destination;
import com.azarenka.evebuilders.domain.db.Receiver;
import com.azarenka.evebuilders.main.managment.api.IPropertiesController;
import com.azarenka.evebuilders.main.menu.MenuManagerPage;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@Route(value = "properties", layout = MenuManagerPage.class)
@RolesAllowed({"ROLE_ADMIN", "ROLE_SUPER_ADMIN"})
@PageTitle("Properties")
public class PropertiesView extends View {

    private final IPropertiesController controller;
    private final H3 variablesTitle = new H3("Переменные для создания заказа");
    private final VerticalLayout variableList = VaadinUtils.initCommonVerticalLayout();
    private Map<String, List<? extends ApplicationProperties>> variablesMap;

    public PropertiesView(@Autowired IPropertiesController controller) {
        this.controller = controller;
        super.setPadding(true);
        initContent();
    }

    private void initContent() {
        variablesMap = Map.of(
                "Приемщик", controller.getReceivers(),
                "Доставка", controller.getDestinations()
        );
        add(initVariablesLayout());
    }

    private VerticalLayout initVariablesLayout() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setMaxHeight("50%");
        layout.setWidthFull();
        //layout.setSpacing(false);
        updateVariableList();
        layout.add(variablesTitle, variableList);
        return layout;
    }

    private void updateVariableList() {
        variableList.removeAll();
        List<String> variables = variablesMap.keySet().stream().toList();
        variables.stream()
                .forEach(variable -> addVariableRow(variable));
    }

    private void addVariableRow(String variable) {
        VerticalLayout variableInfo = VaadinUtils.initCommonVerticalLayout();
        variableInfo.setPadding(false);
        variableInfo.setSpacing(false);
        Span span = new Span(variable);
        span.getStyle().setFontWeight("600");
        variableInfo.add(span);
        HorizontalLayout rolesLayout = new HorizontalLayout();
        rolesLayout.setSpacing(true);
        ApplicationProperties ap = null;
        for (ApplicationProperties property : variablesMap.get(variable)) {
            Span badge = new Span(property.getProperty());
            badge.getElement().getThemeList().add("badge");
            rolesLayout.add(badge);
            ap = property;
        }

        Button editVariablesButton = new Button(VaadinIcon.EDIT.create());
        editVariablesButton.addClickListener(e -> {
        });
        ApplicationProperties finalAp = ap;
        Button addVariablesButton = new Button(VaadinIcon.PLUS.create(), event -> {
            CreatePropertyWindow createPropertyWindow = new CreatePropertyWindow(getTranslation(getHeader(finalAp)),
                    getTranslation(getHeader(finalAp)), closeEvent ->
                    UI.getCurrent().refreshCurrentRoute(true));
            createPropertyWindow.setClickListener(value -> controller.addNewProperty(value, finalAp));
            createPropertyWindow.open();
        });
        editVariablesButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        addVariablesButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setAlignItems(FlexComponent.Alignment.CENTER);
        row.add(variableInfo, rolesLayout, addVariablesButton, editVariablesButton);
        row.expand(variableInfo);
        variableList.add(row, new Hr());
    }

    private String getHeader(ApplicationProperties property) {
        if (property instanceof Destination) {
            return "window.header.add_destination";
        }
        if (property instanceof Receiver) {
            return "window.header.add_receiver";
        }
        return StringUtils.EMPTY;
    }
}
