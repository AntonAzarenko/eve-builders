package com.azarenka.evebuilders.main.orders;

import com.azarenka.evebuilders.domain.OrderStatusEnum;
import com.azarenka.evebuilders.domain.db.DistributedOrder;
import com.azarenka.evebuilders.domain.db.Fit;
import com.azarenka.evebuilders.domain.db.Order;
import com.azarenka.evebuilders.domain.db.OrderFilter;
import com.azarenka.evebuilders.domain.dto.Contract;
import com.azarenka.evebuilders.domain.dto.ShipOrderDto;
import com.azarenka.evebuilders.main.orders.api.IOrderViewController;
import com.azarenka.evebuilders.service.api.IContractService;
import com.azarenka.evebuilders.service.api.IDistributedOrderService;
import com.azarenka.evebuilders.service.api.IFitLoaderService;
import com.azarenka.evebuilders.service.api.IOrderService;
import com.azarenka.evebuilders.service.impl.auth.SecurityUtils;
import com.azarenka.evebuilders.service.impl.contract.ContractValidationReport;
import com.azarenka.evebuilders.service.util.ImageService;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.textfield.IntegerField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Controller
@Scope(scopeName = "prototype")
public class OrdersViewController implements IOrderViewController {

    @Autowired
    private IOrderService orderService;
    @Autowired
    private IFitLoaderService fitLoaderService;
    @Autowired
    private ImageService imageService;
    @Autowired
    private IDistributedOrderService distributedOrderService;
    @Autowired
    private IContractService contractService;

    @Override
    public List<ShipOrderDto> getOrderList(OrderFilter filter) {
        return orderService.getOrderList(filter);
    }

    @Override
    public Fit getFitById(String id) {
        return fitLoaderService.getFitById(id);
    }

    @Override
    public ImageService getImageProviderService() {
        return imageService;
    }

    @Override
    public void saveOrders(Map<ShipOrderDto, IntegerField> orderTexfieldMap) {
        String userName = SecurityUtils.getUserName();
        orderTexfieldMap.forEach((order, textField) -> {
            Integer value = textField.getValue();
            distributedOrderService.save(order.getOrderNumber(), value, userName);
        });
    }

    @Override
    public Order getOriginalOrderByOrderNumber(String orderNumber) {
        return orderService.getByOrderNumber(orderNumber);
    }

    public IFitLoaderService getFitLoaderService() {
        return fitLoaderService;
    }

    @Override
    public List<DistributedOrder> getDistributedOrdersByOrderNumber(String orderNumber) {
        return distributedOrderService.getOrdersByOrderNumber(orderNumber);
    }

    @Override
    public void checkOrder(DistributedOrder distributedOrder) {
        var contractReports = contractService.getContractReport(distributedOrder);
        contractReports.forEach(contractReport -> {
            if (contractReport.isValid()) {
                var readyCount = contractReport.getCountItems();
                distributedOrderService.update(distributedOrder, readyCount);
            }
        });

        new OrderContractReportWindow(contractReports, distributedOrder).open();
    }

    @Override
    public void completeOrder(DistributedOrder distributedOrder) {
        var contractReports = contractService.getContractReport(distributedOrder);
        ConfirmDialog confirmDialog = new ConfirmDialog(
            "Confirmation Window",
            "Action Required",
            String.format("Are you sure you want to move to COMPLETE status all contracts \n %s",
            contractReports.stream()
                .filter(report -> Objects.nonNull(report.getContract()))
                .map(ContractValidationReport::getContract)
                .map(Contract::getContractId)
                .map(String::valueOf)
                .collect(Collectors.joining("\n"))),
            event -> {
                contractReports.forEach(contractReport -> {
                    if (contractReport.isValid()) {
                        var readyCount = contractReport.getCountItems();
                        distributedOrderService.update(distributedOrder, readyCount);
                    }
                });
            });
        confirmDialog.open();
    }
}
