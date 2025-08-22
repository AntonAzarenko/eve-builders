package com.azarenka.evebuilders.main.request;

import com.azarenka.evebuilders.domain.OrderStatusEnum;
import com.azarenka.evebuilders.domain.db.OrderFilter;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.popover.Popover;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.IntegerField;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class RequestFilterView extends Div {

    private final Button openFilterButton = new Button(VaadinIcon.FILTER.create());
    private Popover popover = new Popover();
    private ComponentEventListener<ClickEvent<Button>> listener;
    private Consumer<OrderFilter> saveConsumer;
    private Supplier<OrderFilter> updateFilter;
    private Button applyButton;
    private CheckboxGroup<OrderStatusEnum> statusesCheckboxGroup;
    private CheckboxGroup<String> typeOrderCombobox;
    private IntegerField countFreeField = new IntegerField();
    private RadioButtonGroup<String> radioButtonGroup;
    private final Checkbox persistCheckbox = new Checkbox("Сохранить настройки");
    private List<OrderStatusEnum> listStatuses;
    private final FormLayout form = new FormLayout();
    private OrderFilter filter = new OrderFilter();
}
