package com.azarenka.evebuilders.component;

import com.azarenka.evebuilders.common.util.VaadinUtils;
import com.azarenka.evebuilders.domain.enums.GroupTypeEnum;
import com.azarenka.evebuilders.domain.enums.OrderStatusEnum;
import com.azarenka.evebuilders.domain.db.OrderFilter;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.checkbox.CheckboxGroupVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;
import com.vaadin.flow.component.shared.SelectionPreservationMode;
import com.vaadin.flow.component.textfield.IntegerField;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class OrderFilterPopupComponent extends Div {

    private final Button openFilterButton = new Button(VaadinIcon.FILTER.create());
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

    public OrderFilterPopupComponent() {
        this.addClassName("material-popup");
        super.setVisible(false);
        setWidth("650px");
        add(createHeader());
    }

    public OrderFilterBuilder builder(ComponentEventListener<ClickEvent<Button>> clickListener,
                                      Consumer<OrderFilter> saveConsumer, Supplier<OrderFilter> updateFilter) {
        return new OrderFilterBuilder(clickListener, saveConsumer, updateFilter);
    }

    public Button getOpenFilterButton() {
        return openFilterButton;
    }

    public OrderFilter getAppliedFilter() {
        return filter;
    }

    private void initContent(ComponentEventListener<ClickEvent<Button>> clickListener,
                             Consumer<OrderFilter> saveConsumer, Supplier<OrderFilter> updateFilter) {
        this.listener = clickListener;
        this.saveConsumer = saveConsumer;
        this.updateFilter = updateFilter;
        add(new Hr());
        add(form);
        add(new Hr());
        add(persistCheckbox);
        add(createFooter());
        initOpenButton();
    }

    private HorizontalLayout createHeader() {
        Icon closeIcon = new Icon(VaadinIcon.CLOSE_SMALL);
        closeIcon.addClassName("popup-close-button");
        closeIcon.addClickListener(e -> super.setVisible(false));
        var header = new HorizontalLayout(new H4("\uD83D\uDD0D Фильтр заказов"), closeIcon);
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        return header;
    }

    private void initApplyButton() {
        applyButton = new Button("\u2714");
        applyButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        applyButton.addClickListener(event -> {
            super.setVisible(false);
            extractFilterValues();
            if (persistCheckbox.getValue()) {
                saveConsumer.accept(filter);
                VaadinUtils.showNotification("Фильтр сохранен");
            }
        });
        applyButton.addClickListener(listener);
    }

    private void extractFilterValues() {
        filter.setStatuses(statusesCheckboxGroup.getSelectedItems().stream().toList());
        filter.setOrderTypes(typeOrderCombobox.getSelectedItems().stream().toList());
        filter.setMinFreeCount(countFreeField.getValue());
        filter.setDistributed(Objects.isNull(radioButtonGroup) ? null :
            Objects.equals(radioButtonGroup.getValue(), "Полностью") ? Boolean.TRUE :
                Objects.equals(radioButtonGroup.getValue(), "Не распределен") ? Boolean.FALSE : null
        );
    }

    private HorizontalLayout createFooter() {
        initApplyButton();
        var footer = new HorizontalLayout(applyButton);
        footer.setWidthFull();
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        return footer;
    }

    private void initOpenButton() {
        openFilterButton.setTooltipText(getTranslation("message.button.tooltip.filter_window"));
        openFilterButton.addThemeVariants(ButtonVariant.LUMO_ICON);
        openFilterButton.addClickListener(e -> {
            updateFilter(updateFilter.get());
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
                    
                        const outsideClickListener = (event) => {
                            if (!popup.contains(event.target) && !btn.contains(event.target)) {
                                popup.$server.closePopupFromClient();
                                document.removeEventListener('click', outsideClickListener);
                            }
                        };
                    
                        document.addEventListener('click', outsideClickListener);
                    """, getElement());
            } else {
                super.setVisible(false);
                super.getStyle().remove("top");
                super.getStyle().remove("left");
            }
        });
    }

    private void initRadioButtonGroupLayout() {
        VerticalLayout radioButtonLayoout = VaadinUtils.initCommonVerticalLayout();
        radioButtonGroup = new RadioButtonGroup<>();
        radioButtonGroup.setLabel("Распределение");
        radioButtonGroup.setItems("Все", "Полностью", "Не распределен");
        radioButtonGroup.setValue(Objects.isNull(filter.isDistributed()) ? "Все" :
            filter.isDistributed() ? "Полностью" : "Не распределен");
        radioButtonGroup.setSelectionPreservationMode(SelectionPreservationMode.DISCARD);
        radioButtonGroup.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        radioButtonLayoout.add(radioButtonGroup);
        form.add(radioButtonGroup);
    }

    private void initCountFreeLayout() {
        VerticalLayout countFreeLayout = VaadinUtils.initCommonVerticalLayout();
        countFreeField = new IntegerField();
        countFreeField.setLabel("Свободно");
        countFreeField.setValue(Objects.nonNull(filter.getMinFreeCount()) ? filter.getMinFreeCount() : null);
        countFreeLayout.add(countFreeField);
        form.add(countFreeLayout);
    }

    private void initTypeOrderLayout() {
        VerticalLayout typesOrderLayout = VaadinUtils.initCommonVerticalLayout();
        String[] values = new String[]{
            GroupTypeEnum.SHIPS.name(),
            GroupTypeEnum.MODULES.name()
        };
        typeOrderCombobox = new CheckboxGroup<>();
        typeOrderCombobox.setWidthFull();
        typeOrderCombobox.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);
        typeOrderCombobox.setLabel("Тип заказа");
        typeOrderCombobox.setItems(values);
        typeOrderCombobox.select(Objects.isNull(filter.getOrderTypes()) ||
            filter.getOrderTypes().isEmpty() ? values : filter.getOrderTypes().toArray(new String[0]));
        if (Objects.nonNull(radioButtonGroup)) {
            form.remove(radioButtonGroup);
            typesOrderLayout.add(typeOrderCombobox, radioButtonGroup);
        } else {
            typesOrderLayout.add(typeOrderCombobox);
        }
        form.add(typesOrderLayout);
    }

    private void initStatusesLayout(OrderStatusEnum... statuses) {
        listStatuses = Arrays.asList(statuses);
        statusesCheckboxGroup = new CheckboxGroup<>();
        statusesCheckboxGroup.setLabel("Статусы");
        statusesCheckboxGroup.setItems(listStatuses);
        statusesCheckboxGroup.setItemLabelGenerator(OrderStatusEnum::name);
        statusesCheckboxGroup.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);
        statusesCheckboxGroup.select(
            Objects.isNull(filter.getStatuses()) || filter.getStatuses()
                .isEmpty() ? listStatuses : filter.getStatuses());
        VerticalLayout statusesLayout = VaadinUtils.initCommonVerticalLayout();
        statusesLayout.add(statusesCheckboxGroup);
        form.add(statusesLayout);
    }

    private void setFilter(OrderFilter filter) {
        this.filter = filter;
    }

    private OrderFilterPopupComponent getFilter() {
        return this;
    }

    public void updateFilter(OrderFilter filter) {
        this.filter = filter;
        updateFields();
    }

    private void updateFields() {
        statusesCheckboxGroup.select(
            Objects.isNull(filter.getStatuses()) || filter.getStatuses()
                .isEmpty() ? listStatuses : filter.getStatuses());
        filter.setDistributed(Objects.isNull(radioButtonGroup) ? null :
            Objects.equals(radioButtonGroup.getValue(), "Полностью") ? Boolean.TRUE :
                Objects.equals(radioButtonGroup.getValue(), "Не распределен") ? Boolean.FALSE : null
        );
        typeOrderCombobox.select(Objects.isNull(filter.getOrderTypes()) ||
            filter.getOrderTypes().isEmpty()
            ? GroupTypeEnum.SHIPS.getValues()
            : filter.getOrderTypes().toArray(new String[0]));
        countFreeField.setValue(Objects.nonNull(filter.getMinFreeCount()) ? filter.getMinFreeCount() : null);
    }

    public class OrderFilterBuilder {

        private ComponentEventListener<ClickEvent<Button>> clickListener;
        private Consumer<OrderFilter> saveConsumer;
        private Supplier<OrderFilter> updateFilter;

        OrderFilterBuilder(ComponentEventListener<ClickEvent<Button>> clickListener,
                           Consumer<OrderFilter> saveConsumer, Supplier<OrderFilter> updateFilter) {
            this.clickListener = clickListener;
            this.saveConsumer = saveConsumer;
            this.updateFilter = updateFilter;
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

        public OrderFilterBuilder withLoadedFilter(OrderFilter loaded) {
            setFilter(loaded);
            return this;
        }

        public OrderFilterPopupComponent build() {
            initContent(clickListener, saveConsumer, updateFilter);
            return getFilter();
        }
    }

    @ClientCallable
    public void closePopupFromClient() {
        setVisible(false);
        getStyle().remove("top");
        getStyle().remove("left");
    }
}
