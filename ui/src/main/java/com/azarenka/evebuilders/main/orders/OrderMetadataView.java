package com.azarenka.evebuilders.main.orders;

import com.azarenka.evebuilders.domain.db.Fit;
import com.azarenka.evebuilders.domain.dto.ShipOrderDto;
import com.azarenka.evebuilders.main.commonview.FitView;
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

import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

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
    private String DEADLINE = getTranslation("management.label.finish_date_order");
    private String CREATED = getTranslation("management.label.create_date_order");
    private String DELTA = getTranslation("management.label.create_finish_delta");
    /**
     * Values
     */
    private String DISTRIBUTION_NO = getTranslation("management.label.type.distribution_no");
    private String DISTRIBUTION_FULL = getTranslation("management.label.type.distribution_full");
    private String DISTRIBUTION_PARTIAL = getTranslation("management.label.type.distribution_partial");

    private String ORDER_TYPE_REDEMPTION = getTranslation("management.label.type.redemption");
    private String ORDER_TYPE_MARKET = getTranslation("management.label.type.market");
    private String BLUEPRINT_STATUS_YES = getTranslation("management.label.blue_print.yes");
    private String BLUEPRINT_STATUS_NO = getTranslation("management.label.blue_print.no");

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
        HorizontalLayout header = createHeader();
        Span orderInfo = new Span();
        orderInfo.getElement().setProperty("innerHTML", composeInformation());
        add(header, orderInfo);
        HorizontalLayout fitLayout = new HorizontalLayout();
        fitLayout.setWidthFull();
        Button showFitButton = new Button(VaadinIcon.FILE_START.create(), event -> {
            String fitId = orderDto.getFitId();
            if (StringUtils.isNotBlank(fitId)) {
                Fit fitById = controller.getFitById(orderDto.getFitId());
                new FitView(fitById, controller.getFitLoaderService()).open();
            } else {
                Notification.show("Для этого заказа фита не существует", 2000, Notification.Position.MIDDLE);
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
        ORDER_TYPE_REDEMPTION = getTranslation("management.label.type.redemption");
        ORDER_TYPE_MARKET = getTranslation("management.label.type.market");
        BLUEPRINT_STATUS_YES = getTranslation("management.label.blue_print.yes");
        BLUEPRINT_STATUS_NO = getTranslation("management.label.blue_print.no");
        DEADLINE = getTranslation("management.label.finish_date_order");
        CREATED = getTranslation("management.label.create_date_order");
        DELTA = getTranslation("management.label.create_finish_delta");
        updateInformation();
    }

    private String composeInformation() {
        return new StringBuilder()
            .append(createMenu())
            .append("<br/>")
            .append(createInfo(orderDto))
            .append("</ul")
            .toString();
    }

    private String getDistributionStatus() {
        if (orderDto.getInProgressCount() == 0) {
            return DISTRIBUTION_NO;
        }
        return orderDto.getCount() > orderDto.getInProgressCount()
            ? DISTRIBUTION_PARTIAL : DISTRIBUTION_FULL;
    }

    private HorizontalLayout createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidth("70%");
        String itemName = orderDto.getItemName();
        Span itemHeaderName = new Span();
        itemHeaderName.getElement().setProperty("innerHTML", String.format(HEADER_FORMAT, itemName));
        header.add(controller.getImageProviderService().createImage32(itemName), itemHeaderName);
        return header;
    }

    private String createMenu() {
        return "<ul style='margin: 0; padding-left: 10px;'>";
    }

    private String createInfo(ShipOrderDto orderDto) {
        return String.format(MENU_LINE_FORMAT, DISTRIBUTION, getDistributionStatus()) +
            String.format(MENU_LINE_FORMAT, DESTINATION, orderDto.getDestination()) +
            String.format(MENU_LINE_FORMAT, TYPE, getOrderType()) +
            String.format(MENU_LINE_FORMAT, RECEIVER, orderDto.getReceiver()) +
            String.format(MENU_LINE_FORMAT, BLUE_PRINT_FLAG, bluePrintStatus()) +
            "<br/>" +
            String.format(MENU_LINE_FORMAT, CREATED, orderDto.getCreatedDate().toString()) +
            String.format(MENU_LINE_FORMAT, DEADLINE, orderDto.getFinishDate().toString()) +
            String.format(MENU_LINE_FORMAT, DELTA, ChronoUnit.DAYS.between(LocalDate.now(), orderDto.getFinishDate())) +
            "<br/>" +
            String.format(MENU_LINE_FORMAT, COUNT_IN_PROGRESS, orderDto.getInProgressCount()) +
            String.format(MENU_LINE_FORMAT, FREE, orderDto.getCount() - orderDto.getInProgressCount()) +
            String.format(MENU_LINE_FORMAT, READY, orderDto.getCountReady()) +
            String.format(MENU_LINE_PROGRESS_FORMAT, PROGRESS, orderDto.getCount() == 0
                || orderDto.getCountReady() == 0 ? 0 :
                100 / (((double) orderDto.getCount() / orderDto.getCountReady())), "%") +
            "<br/>";
    }

    private String getOrderType() {
        return orderDto.getOrderType().equals("REDEMPTION") ? ORDER_TYPE_REDEMPTION : ORDER_TYPE_MARKET;
    }

    private String bluePrintStatus() {
        return orderDto.isBluePrint() ? BLUEPRINT_STATUS_YES : BLUEPRINT_STATUS_NO;
    }
}
