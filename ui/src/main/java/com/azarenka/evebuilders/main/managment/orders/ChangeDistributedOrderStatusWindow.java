package com.azarenka.evebuilders.main.managment.orders;

import com.azarenka.evebuilders.common.util.VaadinUtils;
import com.azarenka.evebuilders.domain.db.DistributedOrder;
import com.azarenka.evebuilders.domain.enums.OrderStatusEnum;
import com.azarenka.evebuilders.main.commonview.CommonDialogComponent;
import com.azarenka.evebuilders.main.managment.api.IOrdersManagmentController;
import com.azarenka.evebuilders.service.util.IOrderStatusToStringConverter;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;

import java.util.Arrays;

public class ChangeDistributedOrderStatusWindow extends CommonDialogComponent
    implements LocaleChangeObserver, IOrderStatusToStringConverter {

    private final IOrdersManagmentController controller;
    private final DistributedOrder distributedOrder;
    private final Runnable onSave;

    private final ComboBox<OrderStatusEnum> statusComboBox = new ComboBox<>();
    private final Button saveButton = new Button();
    private final Button closeButton = new Button();

    public ChangeDistributedOrderStatusWindow(IOrdersManagmentController controller,
                                              DistributedOrder distributedOrder,
                                              Runnable onSave) {
        super("change-distributed-order-status-window", false);
        this.controller = controller;
        this.distributedOrder = distributedOrder;
        this.onSave = onSave;

        setWidth("460px");
        setHeaderTitle(getTranslation("window.header.change_distributed_order_status"));
        add(initContent());
        getFooter().add(initButtonsLayout());
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        setHeaderTitle(getTranslation("window.header.change_distributed_order_status"));
        statusComboBox.setLabel(getTranslation("table.column.status"));
        saveButton.setText(getTranslation("button.app.save"));
        closeButton.setText(getTranslation("button.app.close"));
        statusComboBox.getDataProvider().refreshAll();
    }

    private VerticalLayout initContent() {
        statusComboBox.setWidthFull();
        statusComboBox.setLabel(getTranslation("table.column.status"));
        statusComboBox.setItems(Arrays.asList(OrderStatusEnum.values()));
        statusComboBox.setItemLabelGenerator(this::convertOrderStatus);
        statusComboBox.setValue(distributedOrder.getOrderStatus());
        statusComboBox.setRequiredIndicatorVisible(true);

        VerticalLayout layout = VaadinUtils.initCommonVerticalLayout();
        layout.setWidthFull();
        layout.add(statusComboBox);
        return layout;
    }

    private HorizontalLayout initButtonsLayout() {
        saveButton.setText(getTranslation("button.app.save"));
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        saveButton.addClickListener(event -> {
            OrderStatusEnum selectedStatus = statusComboBox.getValue();
            if (selectedStatus == null) {
                return;
            }
            controller.updateDistributedOrderStatus(distributedOrder, selectedStatus);
            close();
            onSave.run();
        });

        closeButton.setText(getTranslation("button.app.close"));
        closeButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        closeButton.addClickListener(event -> close());

        HorizontalLayout layout = new HorizontalLayout(saveButton, closeButton);
        layout.setWidthFull();
        return layout;
    }
}