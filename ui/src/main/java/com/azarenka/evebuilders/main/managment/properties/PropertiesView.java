package com.azarenka.evebuilders.main.managment.properties;

import com.azarenka.evebuilders.common.util.VaadinUtils;
import com.azarenka.evebuilders.component.View;
import com.azarenka.evebuilders.domain.db.ApplicationProperties;
import com.azarenka.evebuilders.domain.db.BlueprintOption;
import com.azarenka.evebuilders.domain.db.Destination;
import com.azarenka.evebuilders.domain.db.OrderType;
import com.azarenka.evebuilders.domain.db.PriorityOption;
import com.azarenka.evebuilders.domain.dto.OrderPresetDefaultsDto;
import com.azarenka.evebuilders.domain.enums.ReceiverTargetType;
import com.azarenka.evebuilders.domain.sqllite.OrderRights;
import com.azarenka.evebuilders.main.managment.api.IPropertiesController;
import com.azarenka.evebuilders.main.menu.MenuManagerPage;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.ComboBoxVariant;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Route(value = "properties", layout = MenuManagerPage.class)
@RolesAllowed({"ROLE_ADMIN", "ROLE_SUPER_ADMIN"})
@PageTitle("Properties")
public class PropertiesView extends View implements LocaleChangeObserver {

    private static final String DESTINATION_KEY = "properties.variables.destination";

    private final IPropertiesController controller;
    private final H3 variablesTitle = new H3(getTranslation("properties.variables.title"));
    private final VerticalLayout variableList = VaadinUtils.initCommonVerticalLayout();
    private final H3 presetTitle = new H3(getTranslation("properties.preset.title"));
    private final VerticalLayout presetCard = VaadinUtils.initCommonVerticalLayout();

    private final ComboBox<OrderType> orderTypeField = new ComboBox<>();
    private final ComboBox<ReceiverTargetType> receiverTypeField = new ComboBox<>();
    private final ComboBox<ReceiverOption> receiverValueField = new ComboBox<>();
    private final ComboBox<PriorityOption> priorityField = new ComboBox<>();
    private final ComboBox<BlueprintOption> blueprintField = new ComboBox<>();
    private final ComboBox<OrderRights> orderRightsField = new ComboBox<>();

    private Map<String, List<? extends ApplicationProperties>> variablesMap;
    private OrderPresetDefaultsDto loadedDefaults;

    public PropertiesView(@Autowired IPropertiesController controller) {
        this.controller = controller;
        super.setPadding(true);
        initContent();
    }

    private void initContent() {
        variablesMap = new LinkedHashMap<>();
        variablesMap.put(DESTINATION_KEY, controller.getDestinations());

        initPresetFields();
        reloadPresetDefaults();

        add(initVariablesLayout(), initPresetLayout());
    }

    private VerticalLayout initVariablesLayout() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setMaxHeight("50%");
        layout.setWidthFull();
        updateVariableList();
        layout.add(variablesTitle, variableList);
        return layout;
    }

    private VerticalLayout initPresetLayout() {
        presetCard.setPadding(false);
        presetCard.setSpacing(false);
        presetCard.setWidthFull();

        HorizontalLayout row1 = new HorizontalLayout(orderTypeField, priorityField);
        row1.setWidthFull();
        HorizontalLayout row2 = new HorizontalLayout(receiverTypeField, receiverValueField);
        row2.setWidthFull();
        HorizontalLayout row3 = new HorizontalLayout(blueprintField, orderRightsField);
        row3.setWidthFull();

        Button saveButton = new Button(VaadinIcon.CHECK.create(), e -> savePresetDefaults());
        saveButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        saveButton.setText(getTranslation("properties.preset.save"));

        Button resetButton = new Button(VaadinIcon.REFRESH.create(), e -> reloadPresetDefaults());
        resetButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        resetButton.setText(getTranslation("properties.preset.reset"));

        Button historyButton = new Button(VaadinIcon.CLOCK.create(), e -> openHistoryDialog());
        historyButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        historyButton.setText(getTranslation("properties.preset.history.button"));

        HorizontalLayout actions = new HorizontalLayout(saveButton, resetButton, historyButton);
        actions.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        actions.setWidthFull();

        presetCard.removeAll();
        presetCard.add(presetTitle, row1, row2, row3, actions);
        return presetCard;
    }

    private void initPresetFields() {
        orderTypeField.setItems(OrderType.values());
        orderTypeField.addThemeVariants(ComboBoxVariant.LUMO_SMALL);
        orderTypeField.setWidthFull();
        orderTypeField.setItemLabelGenerator(value -> switch (value) {
            case REDEMPTION -> getTranslation("management.label.type.redemption");
            case MARKET -> getTranslation("management.label.type.market");
        });

        receiverTypeField.setItems(ReceiverTargetType.values());
        receiverTypeField.addThemeVariants(ComboBoxVariant.LUMO_SMALL);
        receiverTypeField.setWidthFull();
        receiverTypeField.setItemLabelGenerator(value -> switch (value) {
            case CORPORATION -> getTranslation("management.label.receiver_type.corporation");
            case USER -> getTranslation("management.label.receiver_type.user");
        });
        receiverTypeField.addValueChangeListener(event -> {
            loadReceiverOptions(event.getValue());
            receiverValueField.clear();
        });

        receiverValueField.addThemeVariants(ComboBoxVariant.LUMO_SMALL);
        receiverValueField.setWidthFull();
        receiverValueField.setItemLabelGenerator(ReceiverOption::label);

        priorityField.setItems(PriorityOption.values());
        priorityField.addThemeVariants(ComboBoxVariant.LUMO_SMALL);
        priorityField.setWidthFull();
        priorityField.setItemLabelGenerator(value -> switch (value) {
            case LOW -> getTranslation("management.label.priority.low");
            case MEDIUM -> getTranslation("management.label.priority.medium");
            case HIGH -> getTranslation("management.label.priority.high");
        });

        blueprintField.setItems(BlueprintOption.values());
        blueprintField.addThemeVariants(ComboBoxVariant.LUMO_SMALL);
        blueprintField.setWidthFull();
        blueprintField.setItemLabelGenerator(value -> switch (value) {
            case YES -> getTranslation("management.label.blue_print.yes");
            case NO -> getTranslation("management.label.blue_print.no");
        });

        orderRightsField.setItems(OrderRights.values());
        orderRightsField.addThemeVariants(ComboBoxVariant.LUMO_SMALL);
        orderRightsField.setWidthFull();

        updatePresetLabels();
    }

    private void updatePresetLabels() {
        orderTypeField.setLabel(getTranslation("properties.preset.order_type"));
        receiverTypeField.setLabel(getTranslation("properties.preset.receiver_type"));
        receiverValueField.setLabel(getTranslation("properties.preset.receiver_value"));
        priorityField.setLabel(getTranslation("properties.preset.priority"));
        blueprintField.setLabel(getTranslation("properties.preset.blueprint"));
        orderRightsField.setLabel(getTranslation("properties.preset.rights_holder_type"));
    }

    private void loadReceiverOptions(ReceiverTargetType type) {
        if (type == null) {
            receiverValueField.setItems(List.of());
            return;
        }
        if (type == ReceiverTargetType.CORPORATION) {
            var options = controller.getAllManagedCorporations().stream()
                .filter(value -> value.getEveCorporationId() != null && value.getCorporationName() != null)
                .map(value -> new ReceiverOption(String.valueOf(value.getEveCorporationId()), value.getCorporationName()))
                .sorted((a, b) -> a.label().compareToIgnoreCase(b.label()))
                .toList();
            receiverValueField.setItems(options);
            return;
        }
        var options = controller.getAllReceiverUsers().stream()
            .filter(value -> value.getCharacterId() != null && value.getUsername() != null)
            .map(value -> new ReceiverOption(value.getCharacterId(), value.getUsername()))
            .sorted((a, b) -> a.label().compareToIgnoreCase(b.label()))
            .toList();
        receiverValueField.setItems(options);
    }

    private void reloadPresetDefaults() {
        loadedDefaults = controller.getOrderPresetDefaultsForCurrentUser();
        applyLoadedDefaults();
    }

    private void applyLoadedDefaults() {
        if (loadedDefaults == null) {
            return;
        }
        orderTypeField.setValue(loadedDefaults.getOrderType());
        priorityField.setValue(loadedDefaults.getPriority());
        blueprintField.setValue(loadedDefaults.getBlueprint());
        orderRightsField.setValue(loadedDefaults.getOrderRights());

        receiverTypeField.setValue(loadedDefaults.getReceiverType());
        receiverValueField.clear();
        if (StringUtils.isNotBlank(loadedDefaults.getReceiverRefId())
            && StringUtils.isNotBlank(loadedDefaults.getReceiverName())) {
            ReceiverOption option = new ReceiverOption(loadedDefaults.getReceiverRefId(), loadedDefaults.getReceiverName());
            boolean exists = receiverValueField.getListDataView().getItems()
                .anyMatch(value -> value.id().equals(option.id()) && value.label().equals(option.label()));
            if (exists) {
                receiverValueField.setValue(option);
            }
        }

        if (loadedDefaults.isReceiverMissing()) {
            Notification.show(getTranslation("properties.preset.missing_value"), 5000,
                Notification.Position.MIDDLE);
        }
    }

    private void savePresetDefaults() {
        try {
            OrderPresetDefaultsDto dto = new OrderPresetDefaultsDto();
            dto.setOrderType(orderTypeField.getValue());
            dto.setReceiverType(receiverTypeField.getValue());
            ReceiverOption option = receiverValueField.getValue();
            dto.setReceiverRefId(option != null ? option.id() : null);
            dto.setReceiverName(option != null ? option.label() : null);
            dto.setPriority(priorityField.getValue());
            dto.setBlueprint(blueprintField.getValue());
            dto.setOrderRights(orderRightsField.getValue());
            dto.setRightsholder(orderRightsField.getValue() != null ? orderRightsField.getValue().name() : null);
            loadedDefaults = controller.saveOrderPresetDefaultsForCurrentUser(dto);
            Notification.show(getTranslation("properties.preset.saved"), 3000, Notification.Position.MIDDLE);
        } catch (Exception ex) {
            Notification.show(StringUtils.defaultIfBlank(ex.getMessage(), getTranslation("properties.preset.save_error")),
                5000, Notification.Position.MIDDLE);
        }
    }

    private void openHistoryDialog() {
        new PresetHistoryWindow(controller.getOrderPresetDefaultsHistoryForCurrentUser()).open();
    }

    private void updateVariableList() {
        variableList.removeAll();
        variablesMap.keySet().forEach(this::addVariableRow);
    }

    private void addVariableRow(String variable) {
        Span span = new Span(getTranslation(variable));
        span.getStyle().setFontWeight("600");
        VerticalLayout valuesLayout = VaadinUtils.initCommonVerticalLayout();
        valuesLayout.setPadding(false);
        valuesLayout.setSpacing(false);
        valuesLayout.setWidthFull();

        ApplicationProperties baseProperty = variablesMap.get(variable).stream().findFirst().orElse(null);
        if (baseProperty == null) {
            baseProperty = new Destination();
        }
        final ApplicationProperties propertyForActions = baseProperty;
        if (baseProperty instanceof Destination) {
            variablesMap.get(variable).forEach(property -> valuesLayout.add(buildDestinationItem((Destination) property)));
        } else {
            variablesMap.get(variable).forEach(property -> {
                Span badge = new Span(property.getProperty());
                badge.getElement().getThemeList().add("badge");
                valuesLayout.add(badge);
            });
        }

        Button addVariablesButton = new Button(VaadinIcon.PLUS.create(), event -> {
            CreatePropertyWindow createPropertyWindow = new CreatePropertyWindow(getTranslation(getHeader(propertyForActions)),
                getTranslation(getHeader(propertyForActions)), closeEvent -> refreshPropertiesSection());
            createPropertyWindow.setClickListener(value -> controller.addNewProperty(value, propertyForActions));
            createPropertyWindow.open();
        });
        addVariablesButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ICON);

        HorizontalLayout headerLine = new HorizontalLayout(span, addVariablesButton);
        headerLine.setWidthFull();
        headerLine.setAlignItems(FlexComponent.Alignment.CENTER);
        headerLine.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        VerticalLayout rowContent = VaadinUtils.initCommonVerticalLayout();
        rowContent.setPadding(false);
        rowContent.setSpacing(true);
        rowContent.setWidthFull();
        rowContent.add(headerLine, valuesLayout);
        variableList.add(rowContent, new Hr());
    }

    private HorizontalLayout buildDestinationItem(Destination destination) {
        Span value = new Span(destination.getDestination());
        value.getElement().getThemeList().add("badge");

        Button editButton = new Button(VaadinIcon.EDIT.create(), event -> {
            CreatePropertyWindow editWindow = new CreatePropertyWindow(
                getTranslation("window.header.edit_destination"),
                getTranslation("window.header.edit_destination"),
                closeEvent -> refreshPropertiesSection());
            editWindow.getTextField().setValue(destination.getDestination());
            editWindow.setClickListener(newValue -> controller.updateDestination(destination.getDestId(), newValue));
            editWindow.open();
        });
        editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ICON);

        Button deleteButton = new Button(VaadinIcon.TRASH.create(), event -> {
            controller.removeDestination(destination.getDestId());
            refreshPropertiesSection();
        });
        deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_ICON);

        HorizontalLayout actions = new HorizontalLayout(editButton, deleteButton);
        actions.setSpacing(true);

        HorizontalLayout row = new HorizontalLayout(value, actions);
        row.setWidthFull();
        row.setAlignItems(FlexComponent.Alignment.CENTER);
        row.expand(value);
        return row;
    }

    private void refreshPropertiesSection() {
        variablesMap.put(DESTINATION_KEY, controller.getDestinations());
        updateVariableList();
    }

    private String getHeader(ApplicationProperties property) {
        if (property instanceof Destination) {
            return "window.header.add_destination";
        }
        return StringUtils.EMPTY;
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        variablesTitle.setText(getTranslation("properties.variables.title"));
        presetTitle.setText(getTranslation("properties.preset.title"));
        updatePresetLabels();
        updateVariableList();
    }

    private record ReceiverOption(String id, String label) {
    }
}
