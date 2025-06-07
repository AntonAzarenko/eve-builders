package com.azarenka.evebuilders.component;

import com.azarenka.evebuilders.common.util.VaadinUtils;
import com.azarenka.evebuilders.domain.GroupTypeEnum;
import com.azarenka.evebuilders.domain.OrderStatusEnum;
import com.azarenka.evebuilders.domain.db.OrderFilter;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.IntegerField;

import java.util.Objects;

public class OrderFilterPopupComponent extends Div {

    private final Button openFilterButton = new Button(VaadinIcon.FILTER.create());
    private ComponentEventListener<ClickEvent<Button>> listener;
    private Button applyButton;
    private Icon closeIcon;
    private final CheckboxGroup<OrderStatusEnum> statusesCheckboxGroup = new CheckboxGroup<>();
    private final CheckboxGroup<String> typeOrderCombobox = new CheckboxGroup<>();
    private final IntegerField countFreeField = new IntegerField();
    private final RadioButtonGroup<String> radioButtonGroup = new RadioButtonGroup<>();
    private final OrderFilter filter = new OrderFilter();

    public OrderFilterPopupComponent() {
        this.addClassName("material-popup");
        super.setVisible(false);
    }

    public OrderFilterBuilder builder(ComponentEventListener<ClickEvent<Button>> clickListener) {
        return new OrderFilterBuilder(clickListener);
    }

    public Button getOpenFilterButton() {
        return openFilterButton;
    }

    public OrderFilter getAppliedFilter() {
        return filter;
    }

    private void initContent(ComponentEventListener<ClickEvent<Button>> clickListener) {
        this.listener = clickListener;
        initApplyButton();
        initCloseButton();
        initOpenButton();
        super.add(applyButton);
    }

    private void initApplyButton() {
        applyButton = new Button(VaadinIcon.CHECK.create());
        applyButton.addClickListener(event -> {
            super.setVisible(false);
            filter.setStatuses(statusesCheckboxGroup.getSelectedItems().stream().toList());
            filter.setShipType(typeOrderCombobox.getSelectedItems().stream().toList());
            filter.setMinFreeCount(countFreeField.getValue());
            filter.setDistributed(Objects.nonNull(radioButtonGroup.getValue()) && radioButtonGroup.getValue().equals("Полностью"));
        });
        applyButton.addClickListener(listener);
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
        radioButtonGroup.setValue("Полностью");
        radioButtonGroup.setValue("Не распределен");
        radioButtonGroup.setItems("Полностью", "Не распределен");
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
        typeOrderCombobox.select(values);
        typesOrderLayout.add(typeOrderCombobox);
        super.add(typesOrderLayout);
    }

    private void initStatusesLayout() {
        OrderStatusEnum[] statuses = OrderStatusEnum.values();
        statusesCheckboxGroup.setLabel("Статусы");
        statusesCheckboxGroup.setItems(statuses);
        statusesCheckboxGroup.setItemLabelGenerator(OrderStatusEnum::name);
        statusesCheckboxGroup.select(statuses);
        VerticalLayout statusesLayout = VaadinUtils.initCommonVerticalLayout();
        statusesLayout.add(statusesCheckboxGroup);
        super.add(statusesLayout);
    }

    private OrderFilterPopupComponent getFilter() {
        return this;
    }

    public class OrderFilterBuilder {

        private ComponentEventListener<ClickEvent<Button>> clickListener;

        OrderFilterBuilder(ComponentEventListener<ClickEvent<Button>> clickListener) {
            this.clickListener = clickListener;
        }

        public OrderFilterBuilder withStatusFilter() {
            initStatusesLayout();
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

        public OrderFilterPopupComponent build() {
            initContent(clickListener);
            return getFilter();
        }
    }
}
