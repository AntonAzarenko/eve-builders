package com.azarenka.evebuilders.main.commonview;

import com.azarenka.evebuilders.domain.dto.ShipOrderDto;
import com.azarenka.evebuilders.domain.db.Fit;
import com.azarenka.evebuilders.main.orders.api.IOrderViewController;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;

import java.util.Objects;

public class OrderMetadataView extends VerticalLayout implements LocaleChangeObserver {

    /**
     * Labels
     */
    private String DISTRIBUTION = getTranslation("management.label.type.distribution");
    private String DESTINATION = getTranslation("management.label.destination");
    private String TYPE = getTranslation("management.label.type");
    private String RECEIVER = getTranslation("management.label.receiver");
    private String BLUE_PRINT_FLAG = getTranslation("management.label.blue_print");
    private String COUNT_IN_PROGRESS = getTranslation("management.label.count_in_progress");
    private String FREE = getTranslation("management.label.order_free");
    private String READY = getTranslation("management.label.order_ready");
    private String PROGRESS = getTranslation("management.label.order_progress");
    private String FIT = getTranslation("management.label.fit");
    /**
     * Values
     */
    private String DISTRIBUTION_NO = getTranslation("management.label.type.distribution_no");
    private String DISTRIBUTION_FULL = getTranslation("management.label.type.distribution_full");
    private String DISTRIBUTION_PARTIAL = getTranslation("management.label.type.distribution_partial");

    private final String HEADER_FORMAT = "<b>%s</b>";
    private final String MENU_LINE_FORMAT = "<li><b>%s:</b> %s</li>";
    private final String MENU_LINE_PROGRESS_FORMAT = "<li><b>%s:</b> %s%s</li>";

    private final ShipOrderDto orderDto;
    private final IOrderViewController controller;

    public OrderMetadataView(ShipOrderDto orderDto, IOrderViewController controller) {
        setWidthFull();
        setPadding(false);
        setSpacing(false);
        setMargin(false);
        this.orderDto = orderDto;
        this.controller = controller;
        updateInformation();
    }

    private void updateInformation() {
        removeAll();
        Span orderInfo = new Span();
        orderInfo.getElement().setProperty("innerHTML", composeInformation());
        add(orderInfo);
        HorizontalLayout fitLayout = new HorizontalLayout();
        fitLayout.setWidthFull();
        Button showFitButton = new Button(VaadinIcon.FILE_START.create(), event -> {
            Fit fitById = controller.getFitById(orderDto.getFitId());
            if (Objects.nonNull(fitById)) {
                new FitView(fitById).open();
            } else {
                Notification.show("Для этого заказа фита не существует");
            }
        });
        showFitButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ICON);
        Span fit = new Span();
        fit.getElement().setProperty("innerHTML", String.format("<div style=\"padding-left: 10px;\"><b>%s</b>", FIT));
        fitLayout.add(fit, showFitButton);
        fitLayout.setVerticalComponentAlignment(Alignment.CENTER);
        fitLayout.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        add(fitLayout);
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        DISTRIBUTION = getTranslation("management.label.type.distribution");
        DESTINATION = getTranslation("management.label.destination");
        TYPE = getTranslation("management.label.type");
        RECEIVER = getTranslation("management.label.receiver");
        BLUE_PRINT_FLAG = getTranslation("management.label.blue_print");
        COUNT_IN_PROGRESS = getTranslation("management.label.count_in_progress");
        FREE = getTranslation("management.label.order_free");
        READY = getTranslation("management.label.order_ready");
        PROGRESS = getTranslation("management.label.order_progress");
        DISTRIBUTION_NO = getTranslation("management.label.type.distribution_no");
        DISTRIBUTION_FULL = getTranslation("management.label.type.distribution_full");
        DISTRIBUTION_PARTIAL = getTranslation("management.label.type.distribution_partial");
        FIT = getTranslation("management.label.fit");
        updateInformation();
    }

    private String composeInformation() {
        return new StringBuilder()
                .append(createHeader())
                .append(createMenu())
                .append("<br/>")
                .append(createInfo(orderDto))
                .append("</ul")
                .toString();
    }

    private String getDistributionStatus() {
        if (orderDto.getInProgressCount() == 0) return DISTRIBUTION_NO;
        return orderDto.getCount() > orderDto.getInProgressCount()
                ? DISTRIBUTION_PARTIAL : DISTRIBUTION_FULL;
    }

    private String createHeader() {
        return String.format(HEADER_FORMAT, orderDto.getShipName());
    }

    private String createMenu() {
        return "<ul style='margin: 0; padding-left: 10px;'>";
    }

    private String createInfo(ShipOrderDto orderDto) {
        return String.format(MENU_LINE_FORMAT, DISTRIBUTION, getDistributionStatus()) +
                String.format(MENU_LINE_FORMAT, DESTINATION, orderDto.getDestination()) +
                String.format(MENU_LINE_FORMAT, TYPE, orderDto.getOrderType()) +
                String.format(MENU_LINE_FORMAT, RECEIVER, orderDto.getReceiver()) +
                String.format(MENU_LINE_FORMAT, BLUE_PRINT_FLAG, orderDto.isBluePrint()) +
                "<br/>" +
                String.format(MENU_LINE_FORMAT, COUNT_IN_PROGRESS, orderDto.getInProgressCount()) +
                String.format(MENU_LINE_FORMAT, FREE, orderDto.getCount() - orderDto.getInProgressCount()) +
                String.format(MENU_LINE_FORMAT, READY, orderDto.getCountReady()) +
                String.format(MENU_LINE_PROGRESS_FORMAT, PROGRESS, orderDto.getCount() == 0
                        || orderDto.getCountReady() == 0 ? 0 :
                         100 / (((double)orderDto.getCount() / orderDto.getCountReady())), "%");
    }
}
