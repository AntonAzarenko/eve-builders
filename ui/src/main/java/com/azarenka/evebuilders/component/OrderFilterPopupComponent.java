package com.azarenka.evebuilders.component;

import com.azarenka.evebuilders.common.util.VaadinUtils;
import com.azarenka.evebuilders.domain.GroupTypeEnum;
import com.azarenka.evebuilders.domain.OrderStatusEnum;
import com.azarenka.evebuilders.domain.db.OrderFilter;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.shared.SelectionPreservationMode;
import com.vaadin.flow.component.textfield.IntegerField;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class OrderFilterPopupComponent extends Div {

    private final Button openFilterButton = new Button(VaadinIcon.FILTER.create());
    private ComponentEventListener<ClickEvent<Button>> listener;
    private Consumer<OrderFilter> saveConsumer;
    private Button applyButton;
    private Button saveButton;
    private Icon closeIcon;
    private final CheckboxGroup<OrderStatusEnum> statusesCheckboxGroup = new CheckboxGroup<>();
    private final CheckboxGroup<String> typeOrderCombobox = new CheckboxGroup<>();
    private final IntegerField countFreeField = new IntegerField();
    private final RadioButtonGroup<String> radioButtonGroup = new RadioButtonGroup<>();
    private final Checkbox persistCheckbox = new Checkbox("Сохранить настройки");
    private OrderFilter filter = new OrderFilter();

    public OrderFilterPopupComponent() {
        this.addClassName("material-popup");
        super.setVisible(false);
    }

    public OrderFilterBuilder builder(ComponentEventListener<ClickEvent<Button>> clickListener,
                                      Consumer<OrderFilter> saveConsumer) {
        return new OrderFilterBuilder(clickListener, saveConsumer);
    }

    public Button getOpenFilterButton() {
        return openFilterButton;
    }

    public OrderFilter getAppliedFilter() {
        return filter;
    }

    private void initContent(ComponentEventListener<ClickEvent<Button>> clickListener,
                             Consumer<OrderFilter> saveConsumer) {
        this.listener = clickListener;
        this.saveConsumer = saveConsumer;
        initApplyButton();
        initSaveButton();
        initCloseButton();
        initOpenButton();
        super.add(applyButton, saveButton);
    }

    private void initApplyButton() {
        applyButton = new Button(VaadinIcon.CHECK.create());
        applyButton.addClickListener(event -> {
            super.setVisible(false);
            filter.setStatuses(statusesCheckboxGroup.getSelectedItems().stream().toList());
            filter.setOrderTypes(typeOrderCombobox.getSelectedItems().stream().toList());
            filter.setMinFreeCount(countFreeField.getValue());
            filter.setDistributed(
                Objects.isNull(radioButtonGroup.getValue()) ? null : radioButtonGroup.getValue().equals("Полностью"));
        });
        applyButton.addClickListener(listener);
    }

    private void initSaveButton() {
        saveButton = new Button( VaadinIcon.SAFE.create());
        saveButton.getStyle().set("padding", "5px");
        saveButton.addClickListener(event -> {
            super.setVisible(false);
            filter.setStatuses(statusesCheckboxGroup.getSelectedItems().stream().toList());
            filter.setOrderTypes(typeOrderCombobox.getSelectedItems().stream().toList());
            filter.setMinFreeCount(countFreeField.getValue());
            filter.setDistributed(
                Objects.isNull(radioButtonGroup.getValue()) ? null : radioButtonGroup.getValue().equals("Полностью"));
        });
        saveButton.addClickListener(event -> saveConsumer.accept(filter));
        saveButton.addClickListener(listener);
    }

    private void initOpenButton() {
        openFilterButton.setTooltipText(getTranslation("message.button.tooltip.filter_window"));
        openFilterButton.addThemeVariants(ButtonVariant.LUMO_ICON);
        openFilterButton.addClickListener(e -> {
            if (!super.isVisible()) {
                super.setVisible(true);
                openFilterButton.getUI().ifPresent(ui -> {
                    if (this.getParent().isEmpty()) {
                        ui.add(this);
                    }
                });
                openFilterButton.getElement().executeJs("""
                        const btn = this;
                        const popup = $0;
                        const rect = btn.getBoundingClientRect();
                    
                        popup.style.position = 'absolute';
                        popup.style.top = `${rect.bottom + window.scrollY}px`;
                        popup.style.left = `${rect.left + window.scrollX}px`;
                    """, super.getElement());
            } else {
                super.setVisible(false);
                super.getStyle().remove("top");
                super.getStyle().remove("left");
            }
        });
    }

    private void initCloseButton() {
        closeIcon = new Icon(VaadinIcon.CLOSE_SMALL);
        closeIcon.addClassName("popup-close-button");
        closeIcon.addClickListener(e -> super.setVisible(false));
        HorizontalLayout layout = new HorizontalLayout(new Span("Фильтр"), closeIcon);
        layout.setWidthFull();
        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        super.add(layout);
    }

    private void initRadioButtonGroupLayout() {
        VerticalLayout radioButtonLayoout = VaadinUtils.initCommonVerticalLayout();
        radioButtonGroup.setLabel("Распределение");
        radioButtonGroup.setValue("Все");
        radioButtonGroup.setItems("Все", "Полностью", "Не распределен");
        radioButtonGroup.setSelectionPreservationMode(SelectionPreservationMode.DISCARD);
        radioButtonLayoout.add(radioButtonGroup);
        super.add(radioButtonLayoout);
    }

    private void initCountFreeLayout() {
        VerticalLayout countFreeLayout = VaadinUtils.initCommonVerticalLayout();
        countFreeField.setLabel("Свободно");
        countFreeLayout.add(countFreeField);
        super.add(countFreeLayout);
    }

    private void initTypeOrderLayout() {
        VerticalLayout typesOrderLayout = VaadinUtils.initCommonVerticalLayout();
        String[] values = GroupTypeEnum.SHIPS.getValues();
        typeOrderCombobox.setWidthFull();
        typeOrderCombobox.setLabel("Тип заказа");
        typeOrderCombobox.setItems(values);
        typeOrderCombobox.select(Objects.isNull(filter.getOrderTypes()) ||
            filter.getOrderTypes().isEmpty() ? values : filter.getOrderTypes().toArray(new String[0]));

        typesOrderLayout.add(typeOrderCombobox);
        super.add(typesOrderLayout);
    }

    private void initStatusesLayout(OrderStatusEnum... statuses) {
        List<OrderStatusEnum> list = Arrays.asList(statuses);
        statusesCheckboxGroup.setLabel("Статусы");
        statusesCheckboxGroup.setItems(list);
        statusesCheckboxGroup.setItemLabelGenerator(OrderStatusEnum::name);
        statusesCheckboxGroup.select(Objects.isNull(filter.getStatuses()) || filter.getStatuses().isEmpty() ? list : filter.getStatuses());
        VerticalLayout statusesLayout = VaadinUtils.initCommonVerticalLayout();
        statusesLayout.add(statusesCheckboxGroup);
        super.add(statusesLayout);
    }

    private void setFilter(OrderFilter filter) {
        this.filter = filter;
    }

    private OrderFilterPopupComponent getFilter() {
        return this;
    }

    public class OrderFilterBuilder {

        private ComponentEventListener<ClickEvent<Button>> clickListener;
        private Consumer<OrderFilter> saveConsumer;

        OrderFilterBuilder(ComponentEventListener<ClickEvent<Button>> clickListener,
                           Consumer<OrderFilter> saveConsumer) {
            this.clickListener = clickListener;
            this.saveConsumer = saveConsumer;
        }

        public OrderFilterBuilder withStatusFilter(OrderStatusEnum... statuses) {
            initStatusesLayout(statuses);
            return this;
        }

        public OrderFilterBuilder withTypeOrderFilter() {
            initTypeOrderLayout();
            return this;
        }

        public OrderFilterBuilder withCountFreeFilter() {
            initCountFreeLayout();
            return this;
        }

        public OrderFilterBuilder withDistributedFilter() {
            initRadioButtonGroupLayout();
            return this;
        }

        public OrderFilterBuilder withLoadedFilter(OrderFilter filter) {
            setFilter(filter);
            return this;
        }

        public OrderFilterPopupComponent build() {
            initContent(clickListener, saveConsumer);
            return getFilter();
        }
    }
}
