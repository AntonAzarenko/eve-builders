package com.azarenka.evebuilders.main.managment.dashboard;

import com.azarenka.evebuilders.common.util.VaadinUtils;
import com.azarenka.evebuilders.domain.enums.OrderStatusEnum;
import com.azarenka.evebuilders.domain.db.DistributedOrder;
import com.azarenka.evebuilders.main.commonview.CommonDialogComponent;
import com.azarenka.evebuilders.main.managment.api.IDashBoardController;
import com.azarenka.evebuilders.service.impl.contract.ContractValidationReport;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
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
    private IDashBoardController controller;

    public OrderContractReportWindow(List<ContractValidationReport> reports, DistributedOrder order,
                                     IDashBoardController controller) {
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
        completeButton = VaadinUtils.createLumoButton(VaadinIcon.COMPILE);
        completeButton.addClickListener(event -> {
            showCompleterOrderWindow(order);
        });
        applyButton = VaadinUtils.createLumoButton(VaadinIcon.CHECK);
        applyButton.addClickListener(event -> {
            reports.forEach(contract -> {
                showCompleterReportsWindow(order, contract);
            });
            UI.getCurrent().refreshCurrentRoute(true);
        });
        updateButtonStatus();
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

    private void showCompleterOrderWindow(DistributedOrder order) {
        var confirmDialog = new ConfirmDialog(
            "Confirmation Window",
            String.format("Are you sure you want to  fully COMPLETE this order without validation for %s", order.getUserName()), "Ok",
            event -> {
                var readyCount = order.getCount();
                controller.update(order, readyCount);
                UI.getCurrent().refreshCurrentRoute(true);
            });
        confirmDialog.open();
    }

    private void showCompleterReportsWindow(DistributedOrder distributedOrder,
                                            ContractValidationReport contractReport) {
        var confirmDialog = new ConfirmDialog();
        confirmDialog.setHeader("Confirmation Window");
        confirmDialog.setText(
            String.format("Are you sure you want to apply the contact \n %s",
                contractReport.getContract().getContractId()));
        confirmDialog.setConfirmText("Принять");
        confirmDialog.addConfirmListener(event -> {
            var readyCount = contractReport.getCountItems();
            distributedOrder.setOrderStatus(OrderStatusEnum.IN_PROGRESS);
            controller.update(distributedOrder, readyCount);
        });
        confirmDialog.setCancelText("Cancel");
        confirmDialog.addCancelListener(event -> confirmDialog.close());
        confirmDialog.open();
    }

    private void updateButtonStatus() {
        var isEnabled = !reports.isEmpty() && reports.get(0).getCountItems() != 0;
        applyButton.setEnabled(isEnabled);
    }
}
