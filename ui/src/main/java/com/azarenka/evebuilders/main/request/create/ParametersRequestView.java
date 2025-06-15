package com.azarenka.evebuilders.main.request.create;

import com.azarenka.evebuilders.common.util.VaadinUtils;
import com.azarenka.evebuilders.component.SearchComponent;
import com.azarenka.evebuilders.component.View;
import com.azarenka.evebuilders.domain.GroupTypeEnum;
import com.azarenka.evebuilders.domain.db.*;
import com.azarenka.evebuilders.domain.sqllite.InvGroup;
import com.azarenka.evebuilders.domain.sqllite.InvType;
import com.azarenka.evebuilders.main.commonview.FitView;
import com.azarenka.evebuilders.main.request.api.ICreateRequestController;
import com.azarenka.evebuilders.validators.RequiredValidator;
import com.azarenka.evebuilders.validators.StubValidator;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.ComboBoxVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.server.VaadinSession;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;

public class ParametersRequestView extends View implements LocaleChangeObserver {

    private static final String REQUIRED_FIELD_VALUE = "error.message.required_value";

    private final IntegerField countShipsField = new IntegerField(getTranslation("management.label.count"));
    private final BigDecimalField costField = new BigDecimalField(getTranslation("management.label.cost"));
    private final ComboBox<OrderType> orderScopeField = new ComboBox(getTranslation("management.label.type"));
    private final ComboBox<PriorityOption> priorityField = new ComboBox(getTranslation("management.label.priority"));
    private final ComboBox<Fit> fitField = new ComboBox(getTranslation("management.label.fit"));
    private final Binder<RequestOrder> binder = new Binder<>();
    private final ICreateRequestController controller;
    private final DatePicker datePickerField = new DatePicker();
    private RequiredValidator requiredValidator = new RequiredValidator(getTranslation(REQUIRED_FIELD_VALUE));
    private final Div imageContainer = new Div();

    private RequestOrder request = new RequestOrder();
    private ComboBox<InvGroup> groupsComboBox;
    private ComboBox<String> categoryComboBox;
    private ComboBox<InvType> itemsComboBox;
    private ListDataProvider<Fit> fitDataProvider;
    private Button loadButton;
    private Button showFitButton;
    private Span paramentersSpan;
    private Image image = new Image();
    private List<InvGroup> invGroupById = new ArrayList<>();
    private SearchComponent searchField;

    public ParametersRequestView(ICreateRequestController controller) {
        this.controller = controller;
        initContent();
        RequestOrder originalOrder = (RequestOrder) VaadinSession.getCurrent().getAttribute("requestOrder");
        if (Objects.nonNull(originalOrder)) {
            request = originalOrder;
            binder.readBean(originalOrder);
        }
    }

    @ClientCallable
    public void receiveClipboardTextWithProgress(String text) {
        if (text != null && !text.isEmpty()) {
            boolean upload = controller.uploadFit(text);
            if (!upload) {
                Notification.show("Не получилось загрузить фит, проверте данные и попробуйте еще раз!",
                        5000, Notification.Position.MIDDLE);
            } else {
                Notification.show("Фит загружен",
                        5000, Notification.Position.MIDDLE);
            }
        } else {
            Notification.show("Буфер обмена пуст", 5000, Notification.Position.MIDDLE);
        }
        refresh();
    }

    public void refresh() {
        fitDataProvider = DataProvider.ofCollection(controller.gitAllFits());
        fitField.setItems(fitDataProvider);
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        itemsComboBox.setLabel(getTranslation("management.add_ships"));
        groupsComboBox.setLabel(getTranslation("management.label_groups"));
        paramentersSpan.setText(getTranslation("management.label.parameters"));
        countShipsField.setLabel(getTranslation("management.label.count"));
        costField.setLabel(getTranslation("management.label.cost"));
        orderScopeField.setLabel(getTranslation("management.label.type"));
        priorityField.setLabel(getTranslation("management.label.priority"));
        fitField.setLabel(getTranslation("management.label.fit"));
        var selectedType = orderScopeField.getValue();
        orderScopeField.setItems(OrderType.values());
        orderScopeField.setValue(selectedType);
        var selectedPriority = priorityField.getValue();
        priorityField.setItems(PriorityOption.values());
        priorityField.setValue(selectedPriority);
        requiredValidator = new RequiredValidator(getTranslation(REQUIRED_FIELD_VALUE));
    }

    private void initContent() {
        initOrderFieldsLayout();
        Div parameterCard = new Div();
        parameterCard.addClassName("add-ship-parameter-card");
        paramentersSpan = new Span(getTranslation("management.label.parameters"));
        VerticalLayout mainLayout = VaadinUtils.initCommonVerticalLayout();
        mainLayout.setPadding(true);
        mainLayout.setSpacing(false);
        HorizontalLayout layout = new HorizontalLayout(categoryComboBox, groupsComboBox, itemsComboBox, imageContainer);
        layout.setWidthFull();
        layout.getStyle().set("flex-wrap", "wrap");
        mainLayout.add(
                layout,
                initFitLoadFitButtonLayout(),
                countShipsField,
                costField,
                orderScopeField,
                priorityField,
                datePickerField,
                initButtonsLayout()
        );
        parameterCard.add(paramentersSpan, new Hr(), mainLayout);
        add(initHeaderLayout(), parameterCard);
    }

    private HorizontalLayout initHeaderLayout() {
        searchField = new SearchComponent(getTranslation("management.search.placeholder"),
                event -> searchByText(searchField.getValue()),
                event -> clearSearch()
        );
        searchField.setWidth("70%");
        var headerTitle = new HorizontalLayout(searchField, new Button(VaadinIcon.REFRESH.create(), event -> refresh()));
        headerTitle.setWidthFull();
        headerTitle.setVerticalComponentAlignment(FlexComponent.Alignment.STRETCH);
        headerTitle.setAlignItems(FlexComponent.Alignment.CENTER);
        add(headerTitle);
        return headerTitle;
    }

    private void initOrderFieldsLayout() {
        initCategoryCombobox();
        initGroupCombobox();
        initItemsCombobox();
        initCountShipsField();
        initCostField();
        initPriorityField();
        initTypeField();
        initFitField();
        initDataPickerLayout();
        VaadinUtils.setPadding("5px", categoryComboBox, groupsComboBox, itemsComboBox, countShipsField, costField,
                orderScopeField, priorityField, fitField, datePickerField);
        updateFieldsStatus();
    }

    private void initCategoryCombobox() {
        categoryComboBox = new ComboBox<>();
        categoryComboBox.setLabel("Категория");
        categoryComboBox.addThemeVariants(ComboBoxVariant.LUMO_SMALL);
        categoryComboBox.setItems(GroupTypeEnum.MODULES.getValues());
        categoryComboBox.setClearButtonVisible(true);
        categoryComboBox.addValueChangeListener(event -> {
            String value = event.getValue();
            if (Objects.nonNull(value)) {
                GroupTypeEnum groupTypeEnum = GroupTypeEnum.valueOf(value);
                invGroupById = controller.getInvGroupsById(groupTypeEnum.getGroupId());
                groupsComboBox.setItems(invGroupById);
                InvGroup allGroup = new InvGroup();
                allGroup.setGroupName("ALL");
                invGroupById.add(0, allGroup);
                itemsComboBox.setLabel(getTranslation(groupTypeEnum.toString()));
            }
            updateFieldsStatus();
        });
        binder.forField(categoryComboBox)
                .withValidator(requiredValidator)
                .bind(RequestOrder::getCategory, RequestOrder::setCategory);
    }

    private void initGroupCombobox() {
        groupsComboBox = new ComboBox<>();
        groupsComboBox.addThemeVariants(ComboBoxVariant.LUMO_SMALL);
        groupsComboBox.setLabel(getTranslation("management.label_groups"));
        groupsComboBox.setItemLabelGenerator(InvGroup::getGroupName);
        groupsComboBox.setEnabled(false);
        groupsComboBox.setClearButtonVisible(true);
        groupsComboBox.addValueChangeListener(event -> {
            if (Objects.nonNull(event.getValue())) {
                Integer groupId = event.getValue().getGroupID();
                if (Objects.isNull(groupId)) {
                    itemsComboBox.setItems(controller.getTypesByGroupIds(
                            invGroupById.stream().map(InvGroup::getGroupID).toList()));
                } else {
                    itemsComboBox.setItems(controller.getTypesByGroupId(event.getValue().getGroupID()));
                }
            }
            updateFieldsStatus();
        });
        binder.forField(groupsComboBox)
                .withValidator(requiredValidator)
                .bind(this::buildGroup, (obj, value) -> obj.setGroupName(value.getGroupName()));
    }

    private void initItemsCombobox() {
        itemsComboBox = new ComboBox<>();
        itemsComboBox.addThemeVariants(ComboBoxVariant.LUMO_SMALL);
        itemsComboBox.setItemLabelGenerator(InvType::getTypeName);
        itemsComboBox.setLabel(getTranslation("management.add_ships"));
        itemsComboBox.setEnabled(false);
        itemsComboBox.setClearButtonVisible(true);
        itemsComboBox.addValueChangeListener(event -> {
            InvType value = event.getValue();
            if (Objects.nonNull(value)) {
                image = createImage(event.getValue());
                imageContainer.removeAll();
                imageContainer.add(image);
            }
        });
        binder.forField(itemsComboBox)
                .withValidator(requiredValidator)
                .bind((this::buildInvType),
                        (shipOrder, invType) -> shipOrder.setItemName(invType.getTypeName()));
    }

    //init order creation fields
    private void initCountShipsField() {
        countShipsField.setWidthFull();
        countShipsField.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        binder.forField(countShipsField)
                .withValidator(requiredValidator)
                .withValidator(value -> value > 0, getTranslation("errors.message.zero_value"))
                .bind(RequestOrder::getCount, RequestOrder::setCount);
    }

    private void initCostField() {
        costField.setWidthFull();
        costField.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        binder.forField(costField)
                .withValidator(requiredValidator)
                .withValidator(value -> value.compareTo(BigDecimal.ZERO) > 0, getTranslation("errors.message.zero_value"))
                .bind(RequestOrder::getPrice, RequestOrder::setPrice);
    }

    private void initPriorityField() {
        priorityField.setWidthFull();
        priorityField.setItems(PriorityOption.values());
        priorityField.setItemLabelGenerator(option -> switch (option) {
            case LOW -> getTranslation("management.label.priority.low");
            case MEDIUM -> getTranslation("management.label.priority.medium");
            case HIGH -> getTranslation("management.label.priority.high");
        });
        binder.forField(priorityField)
                .withValidator(requiredValidator)
                .bind(this::getOption, (order, value) -> order.setPriority(value.name()));
    }

    private PriorityOption getOption(RequestOrder request) {
        if (Objects.nonNull(request.getPriority())) {
            return PriorityOption.valueOf(request.getPriority().toUpperCase());
        }
        return PriorityOption.LOW;
    }

    private void initTypeField() {
        orderScopeField.setWidthFull();
        orderScopeField.addThemeVariants(ComboBoxVariant.LUMO_SMALL);
        orderScopeField.setItems(OrderType.values());
        orderScopeField.setItemLabelGenerator(type -> switch (type) {
            case REDEMPTION -> getTranslation("management.label.type.redemption");
            case MARKET -> getTranslation("management.label.type.market");
        });
        binder.forField(orderScopeField)
                .withValidator(requiredValidator)
                .bind(this::getType, (order, value) -> order.setOrderType(value.name()));
    }

    private OrderType getType(RequestOrder order) {
        if (Objects.nonNull(order.getOrderType())) {
            return OrderType.valueOf(order.getOrderType().toUpperCase());
        }
        return OrderType.REDEMPTION;
    }

    private HorizontalLayout initFitLoadFitButtonLayout() {
        loadButton.setTooltipText("Insert from clipboard");
        HorizontalLayout layout = new HorizontalLayout(fitField, loadButton, showFitButton);
        layout.setVerticalComponentAlignment(FlexComponent.Alignment.STRETCH);
        layout.setAlignItems(FlexComponent.Alignment.END);
        layout.setWidthFull();
        return layout;
    }

    private void initFitField() {
        fitDataProvider = DataProvider.ofCollection(controller.gitAllFits());
        fitField.setWidthFull();
        fitField.setItemLabelGenerator(Fit::getName);
        fitField.setItems(fitDataProvider);
        binder.forField(fitField)
                .withValidator(StringUtils.isNotBlank(categoryComboBox.getValue())
                        && GroupTypeEnum.valueOf(categoryComboBox.getValue()) == GroupTypeEnum.valueOf("SHIPS")
                        ? requiredValidator : new StubValidator())
                .bind(this::getFit, (order, value) -> order.setFitId(Objects.nonNull(value) ? value.getId() : StringUtils.EMPTY));
        loadButton = new Button(VaadinIcon.FILE_ADD.create(), event -> {
            getUI().ifPresent(ui -> ui.getPage().executeJs(
                    "navigator.clipboard.readText().then(text => {" +
                            "   $0.$server.receiveClipboardTextWithProgress(text);" +
                            "}).catch(err => {" +
                            "   console.error('Ошибка чтения из буфера обмена:', err);" +
                            "   $0.$server.notifyError();" +
                            "});",
                    getElement()
            ));
        });
        showFitButton = new Button(VaadinIcon.FILE_START.create(), event -> {
            Fit fitById = fitField.getValue();
            if (Objects.nonNull(fitById)) {
                new FitView(fitById, controller.getFitLoaderService()).open();
            }
        });
        fitField.addValueChangeListener(event -> {
            Fit fit = event.getValue();
            if (Objects.nonNull(fit)) {
                categoryComboBox.setValue(GroupTypeEnum.SHIPS.name());
                List<InvGroup> invGroupsById = controller.getInvGroupsById(GroupTypeEnum.SHIPS.getGroupId());
                List<InvType> typesByGroupIds = controller.getTypesByGroupIds(invGroupsById.stream()
                        .map(InvGroup::getGroupID)
                        .toList());
                groupsComboBox.setValue(invGroupsById.stream()
                        .filter(e -> e.getGroupID().equals(fit.getGroupId()))
                        .findFirst()
                        .get());
                itemsComboBox.setValue(typesByGroupIds.stream()
                        .filter(e -> e.getTypeID().equals(fit.getTypeId()))
                        .findFirst()
                        .get());
            }
        });
    }

    private Fit getFit(RequestOrder order) {
        Fit fit = new Fit();
        fit.setName("");
        if (StringUtils.isNotBlank(order.getFitId())) {
            Optional<Fit> first = controller.gitAllFits().stream().filter(e -> e.getId().equals(order.getFitId())).findFirst();
            if (first.isPresent()) {
                fit = first.get();
            }
        }
        return fit;
    }

    private void initDataPickerLayout() {
        datePickerField.setWidthFull();
        datePickerField.setLabel(getTranslation("management.label.finish_date_order"));
        binder.forField(datePickerField)
                .withValidator(requiredValidator)
                .withValidator(value -> value.isAfter(LocalDate.now()), getTranslation("errors.message.data_less_then_day"))
                .bind(RequestOrder::getFinishDate, RequestOrder::setFinishDate);
    }

    private HorizontalLayout initButtonsLayout() {
        Button applyButton = new Button(VaadinIcon.CHECK.create());
        applyButton.addClickListener(event -> {
            clickApplyButton();
        });
        Button clearButton = new Button(VaadinIcon.ERASER.create());
        clearButton.addClickListener(event -> clearFields());
        HorizontalLayout layout = new HorizontalLayout(applyButton, clearButton);
        layout.setWidthFull();
        layout.setMargin(false);
        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        return layout;
    }

    private void searchByText(String value) {
        GroupTypeEnum[] typeEnum = GroupTypeEnum.values();
        IntStream.range(0, typeEnum.length).forEach(i -> {
            List<InvGroup> invGroupsById = controller.getInvGroupsById(typeEnum[i].getGroupId());
            List<InvType> typesByGroupIds = controller.getTypesByGroupIds(invGroupsById.stream()
                    .map(InvGroup::getGroupID)
                    .toList());
            Optional<InvType> optionalInvType = typesByGroupIds.stream()
                    .filter(e -> e.getTypeName().equalsIgnoreCase(value))
                    .findFirst();
            if (optionalInvType.isPresent()) {
                InvType invType = optionalInvType.get();
                categoryComboBox.setValue(typeEnum[i].name());
                groupsComboBox.setValue(invGroupsById.stream()
                        .filter(e -> e.getGroupID().equals(invType.getGroupId()))
                        .findFirst()
                        .get());
                itemsComboBox.setValue(invType);
            }
        });
    }

    private void clearSearch() {
        searchField.clearText();
        categoryComboBox.clear();
        groupsComboBox.clear();
        itemsComboBox.clear();
        imageContainer.removeAll();
        searchByText("");
    }

    private void clickApplyButton() {
        if (binder.validate().isOk()) {
            try {
                binder.writeBean(request);
                controller.createRequest(request);
                RequestOrder originalOrder = (RequestOrder) VaadinSession.getCurrent().getAttribute("requestOrder");
                if (Objects.nonNull(originalOrder)) {
                    VaadinSession.getCurrent().setAttribute("requestOrder", null);
                }
                getUI().ifPresent(ui -> ui.refreshCurrentRoute(true));
            } catch (ValidationException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void clearFields() {
        countShipsField.clear();
        costField.clear();
        orderScopeField.clear();
        priorityField.clear();
        fitField.clear();
        datePickerField.clear();
        categoryComboBox.clear();
        groupsComboBox.clear();
        itemsComboBox.clear();
        imageContainer.removeAll();
        VaadinSession.getCurrent().setAttribute("originalOrder", null);
    }

    private Image createImage(InvType invType) {
        return controller.getBigImageByParameters(invType, "64");
    }

    private InvType buildInvType(RequestOrder order) {
        InvType invType = new InvType();
        if (Objects.nonNull(order.getCategory()) && Objects.nonNull(order.getGroupName())) {
            Optional<InvGroup> optionalInvGroup =
                    controller.getInvGroupsById(GroupTypeEnum.valueOf(order.getCategory()).getGroupId()).stream()
                            .filter(e -> e.getGroupName().equals(order.getGroupName()))
                            .findFirst();
            if (optionalInvGroup.isPresent()) {
                InvGroup invGroup = optionalInvGroup.get();
                Optional<InvType> optionalInvType = controller.getTypesByGroupId(invGroup.getGroupID())
                        .stream().filter(e -> e.getTypeName().equals(order.getItemName())).findFirst();
                if (optionalInvType.isPresent()) {
                    invType = optionalInvType.get();
                }
            }
        }
        return invType;
    }

    private InvGroup buildGroup(RequestOrder request) {
        InvGroup invGroup = new InvGroup();
        if (Objects.nonNull(request.getCategory()) && Objects.nonNull(request.getGroupName())) {
            Optional<InvGroup> optionalInvGroup = controller.getInvGroupsById(
                            GroupTypeEnum.valueOf(request.getCategory()).getGroupId()).stream()
                    .filter(e -> e.getGroupName().equals(request.getGroupName())).findFirst();
            if (optionalInvGroup.isPresent()) {
                invGroup = optionalInvGroup.get();
            }
        }
        return invGroup;
    }

    private void updateFieldsStatus() {
        String value = categoryComboBox.getValue();
        InvGroup invGroup = groupsComboBox.getValue();
        groupsComboBox.setEnabled(StringUtils.isNotBlank(value));
        itemsComboBox.setEnabled(Objects.nonNull(invGroup));
    }
}
