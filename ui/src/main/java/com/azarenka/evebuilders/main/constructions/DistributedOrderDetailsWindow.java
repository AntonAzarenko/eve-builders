package com.azarenka.evebuilders.main.constructions;

import com.azarenka.evebuilders.domain.db.DistributedOrder;
import com.azarenka.evebuilders.main.commonview.CommonDialogComponent;
import com.azarenka.evebuilders.service.util.DecimalFormatter;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.math.BigDecimal;

public class DistributedOrderDetailsWindow extends CommonDialogComponent {

    //TODO rewrite this class
    public DistributedOrderDetailsWindow(DistributedOrder order) {

        setHeaderTitle("Информация о заказе " + order.getOrderNumber());
        super.setWidth("800px");
        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(false);
        layout.setPadding(false);

        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 2)
        );
        addRow(formLayout, "Название корабля", order.getShipName());
        addRow(formLayout, "Пользователь", order.getUserName());
        addRow(formLayout, "Количество", String.valueOf(order.getCount()));
        addRow(formLayout, "Готово", order.getCountReady() + " из " + order.getCount());
        addRow(formLayout, "Осталось сдать", String.valueOf(order.getCount() - order.getCountReady()));
        addRow(formLayout, "Фит", order.getFitId());

        BigDecimal price = order.getPrice(); // поле должно быть добавлено
        BigDecimal total = price.multiply(BigDecimal.valueOf(order.getCount()));
        addRow(formLayout, "Цена за единицу", DecimalFormatter.formatIsk(price));
        addRow(formLayout, "Общая сумма", DecimalFormatter.formatIsk(total));
        addRow(formLayout, "Статус", order.getOrderStatus().name());
        addRow(formLayout, "Категория", order.getCategory());
        addRow(formLayout, "Дата создания", order.getCreatedDate().toString());
        addRow(formLayout, "Дата завершения", order.getFinishedDate() != null
                ? order.getFinishedDate().toString()
                : "-");
        layout.add(formLayout);
        Button closeButton = new Button("Закрыть", e -> close());
        HorizontalLayout buttons = new HorizontalLayout(closeButton);
        buttons.setWidthFull();
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        layout.add(buttons);
        getFooter().add(layout);
    }

    private void addRow(FormLayout form, String label, String value) {
        Span labelSpan = new Span(label);
        labelSpan.getStyle()
                .set("font-weight", "900")
                .set("align-self", "center")
                .set("margin", "0");

        Span valueSpan = new Span(value);
        valueSpan.getStyle()
                .set("align-self", "center")
                .set("margin", "0");
        form.addFormItem(valueSpan, labelSpan);
    }
}