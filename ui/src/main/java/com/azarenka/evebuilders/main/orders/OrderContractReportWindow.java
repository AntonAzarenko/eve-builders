package com.azarenka.evebuilders.main.orders;

import com.azarenka.evebuilders.domain.db.DistributedOrder;
import com.azarenka.evebuilders.main.commonview.CommonDialogComponent;
import com.azarenka.evebuilders.main.orders.api.IOrderViewController;
import com.azarenka.evebuilders.service.impl.contract.ContractValidationReport;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import java.util.List;
import java.util.Objects;

public class OrderContractReportWindow extends CommonDialogComponent {

    private final List<ContractValidationReport> reports;
    private Button completeButton;
    private Button applyButton;
    private DistributedOrder order;
    private IOrderViewController controller;

    public OrderContractReportWindow(List<ContractValidationReport> reports, DistributedOrder order,
                                     IOrderViewController controller) {
        super("order_contract_report_header", true);
        this.reports = reports;
        this.order = order;
        this.controller = controller;
        setHeaderTitle(String.format(
            getTranslation("window.order_contract_report_header"), order.getOrderNumber(), order.getUserName()));
        initContent();
        getFooter().add(initButtonsLayout());
    }

    private void initContent() {
        reports.forEach(report -> {
            if (Objects.nonNull(report.getContract())) {
                add(createLayout("Contract: ", String.valueOf(report.getContract().getContractId())));
                add(createLayout("Valid: ", String.valueOf(report.isValid())));
                add(createLayout("Count: ", String.valueOf(report.getCountItems())));
                add(createLayout("ErrorMessages: ",
                    report.getValidateErrorMessages().isEmpty() ? "Not Found" : String.join(",\n",
                        report.getValidateErrorMessages())));
            } else {
                add(createLayout("ErrorMessages: ",
                    report.getValidateErrorMessages().isEmpty() ? "Not Found" : String.join(",\n",
                        report.getValidateErrorMessages())));
            }
        });
    }

    private HorizontalLayout initButtonsLayout() {
        completeButton = new Button(VaadinIcon.COMPILE.create());
        completeButton.addClickListener(event -> {
            controller.completeOrder(order, true, reports);
        });
        applyButton = new Button(VaadinIcon.CHECK.create());
        applyButton.addClickListener(event -> {
            controller.completeOrder(order, false, reports);
        });
        HorizontalLayout horizontalLayout = new HorizontalLayout(completeButton, applyButton, createCloseButton());
        horizontalLayout.setWidthFull();
        horizontalLayout.setJustifyContentMode(JustifyContentMode.END);
        return horizontalLayout;
    }

    private HorizontalLayout createLayout(String title, String value) {
        var horizontalLayout = new HorizontalLayout();
        horizontalLayout.setWidthFull();
        horizontalLayout.add(new Span(title), new Span(value));
        return horizontalLayout;
    }
}
