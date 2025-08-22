package com.azarenka.evebuilders.main.orders.myorders;

import com.azarenka.evebuilders.common.util.VaadinUtils;
import com.azarenka.evebuilders.component.View;
import com.azarenka.evebuilders.domain.db.DistributedOrder;
import com.azarenka.evebuilders.service.util.DecimalFormatter;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import java.math.BigDecimal;
import java.util.Objects;

public class MetadataDistributedOrderView extends View {

    private final HorizontalLayout header = new HorizontalLayout();
    private final HorizontalLayout priceComponent = new HorizontalLayout();
    private final HorizontalLayout fullPrice = new HorizontalLayout();
    private final HorizontalLayout count = new HorizontalLayout();
    private final HorizontalLayout destinationComponent = new HorizontalLayout();
    private final HorizontalLayout recievierComponent = new HorizontalLayout();
    private final HorizontalLayout finishDate = new HorizontalLayout();
    private final HorizontalLayout fit = new HorizontalLayout();

    public MetadataDistributedOrderView() {
        initComponents();
    }

    private void initComponents() {
        addClassName("meta-card");
        header.addClassName("title");

        for (var hl : new HorizontalLayout[]{
            header, priceComponent, fullPrice, count,
            destinationComponent, recievierComponent, finishDate, fit
        }) {
            hl.setWidthFull();
            hl.addClassName("row");
            hl.setAlignItems(Alignment.CENTER);
            hl.setJustifyContentMode(JustifyContentMode.BETWEEN);
            hl.setSpacing(true);
            hl.setPadding(false);
        }
    }

    public void refresh(DistributedOrder distributedOrder, String destination, String recievier) {
        removeAll();
        if(Objects.nonNull(distributedOrder)) {
            header.removeAll();
            priceComponent.removeAll();
            fullPrice.removeAll();
            count.removeAll();
            destinationComponent.removeAll();
            recievierComponent.removeAll();
            finishDate.removeAll();
            fit.removeAll();
            BigDecimal price = distributedOrder.getPrice();
            BigDecimal total = price.multiply(BigDecimal.valueOf(distributedOrder.getCount()));
            header.add(new Span(distributedOrder.getShipName()));
            initCopyButton(header, e ->
                VaadinUtils.copyToClipboard(header, distributedOrder.getShipName(), "Скопировано"));
            addRow(priceComponent, "Цена за ед.", DecimalFormatter.formatIsk(price), () ->
                VaadinUtils.copyToClipboard(priceComponent, String.valueOf(price), "Скопировано"));
            addRow(fullPrice, "Полная цена", DecimalFormatter.formatIsk(total), () ->
                VaadinUtils.copyToClipboard(fullPrice, String.valueOf(total), "Скопировано"));
            addRow(count, "Номер заказа", String.valueOf(distributedOrder.getOrderNumber()), () ->
                VaadinUtils.copyToClipboard(count, String.valueOf(distributedOrder.getOrderNumber()), "Скопировано"));
            addRow(destinationComponent, "Место Сдачи", destination, () ->
                VaadinUtils.copyToClipboard(destinationComponent, destination, "Скопировано"));
            addRow(recievierComponent, "Приемщик", recievier, () ->
                VaadinUtils.copyToClipboard(recievierComponent, recievier, "Скопировано"));
            add(header, priceComponent, fullPrice, count, recievierComponent, destinationComponent, finishDate, fit);
        }
    }

    private void addRow(HorizontalLayout layout, String label, String value, Runnable copyAction) {
        layout.removeAll();
        var lbl = new Span("* " + label + ":");
        lbl.addClassName("label");
        var val = new Span(value);
        val.addClassName("value");
        var wrapper = new HorizontalLayout(lbl, val);
        wrapper.setSpacing(true);
        wrapper.setPadding(false);
        wrapper.setAlignItems(Alignment.BASELINE);
        var copyBtn = VaadinUtils.createLumoButton(VaadinIcon.COPY_O);
        copyBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_SMALL);
        copyBtn.addClassName("copy-btn");
        copyBtn.addClickListener(e -> copyAction.run());
        layout.add(wrapper, copyBtn);
    }

    private void initCopyButton(HorizontalLayout layout, ComponentEventListener<ClickEvent<Button>> listener) {
        var copyButton = VaadinUtils.createLumoButton(VaadinIcon.COPY_O);
        copyButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_SMALL);
        copyButton.addClassName("copy-btn");
        copyButton.addClickListener(listener);
        layout.add(copyButton);
    }
}
