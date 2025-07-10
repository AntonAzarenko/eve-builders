package com.azarenka.evebuilders.main.orders;

import com.azarenka.evebuilders.domain.db.DistributedOrder;
import com.azarenka.evebuilders.main.commonview.CommonDialogComponent;
import com.azarenka.evebuilders.service.impl.contract.ContractValidationReport;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import java.util.List;
import java.util.Objects;

public class OrderContractReportWindow extends CommonDialogComponent {

    private final List<ContractValidationReport> reports;

    public OrderContractReportWindow(List<ContractValidationReport> reports, DistributedOrder order) {
        this.reports = reports;
        setHeaderTitle(String.format(
            getTranslation("window.order_contract_report_header"), order.getOrderNumber(), order.getUserName()));
        initContent();
        getFooter().add(createCloseButton());
    }

    private void initContent() {
        reports.forEach(report -> {
            if(Objects.nonNull(report.getContract())){
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

    private HorizontalLayout createLayout(String title, String value) {
        var horizontalLayout = new HorizontalLayout();
        horizontalLayout.setWidthFull();
        horizontalLayout.add(new Span(title), new Span(value));
        return horizontalLayout;
    }
}
