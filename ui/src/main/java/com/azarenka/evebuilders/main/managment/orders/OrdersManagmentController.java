package com.azarenka.evebuilders.main.managment.orders;

import com.azarenka.evebuilders.domain.db.DistributedOrder;
import com.azarenka.evebuilders.domain.db.Order;
import com.azarenka.evebuilders.domain.db.OrderAudit;
import com.azarenka.evebuilders.domain.db.OrderFilter;
import com.azarenka.evebuilders.domain.dto.ShipOrderDto;
import com.azarenka.evebuilders.domain.enums.OrderStatusEnum;
import com.azarenka.evebuilders.main.managment.api.IOrdersManagmentController;
import com.azarenka.evebuilders.service.api.IAuditService;
import com.azarenka.evebuilders.service.api.IContractService;
import com.azarenka.evebuilders.service.api.IDistributedOrderService;
import com.azarenka.evebuilders.service.api.IOrderService;
import com.azarenka.evebuilders.service.impl.contract.ContractValidationReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class OrdersManagmentController implements IOrdersManagmentController {

    @Autowired
    private IOrderService orderService;
    @Autowired
    private IDistributedOrderService distributedOrderService;
    @Autowired
    private IContractService contractService;
    @Autowired
    private IAuditService auditService;

    @Override
    public List<ShipOrderDto> getOrders() {
        return orderService.getOrderList(new OrderFilter())
            .stream()
            .sorted(Comparator.comparing(ShipOrderDto::getCreatedDate, Comparator.reverseOrder()))
            .collect(Collectors.toList());
    }

    @Override
    public Map<String, Long> getActiveUsersByOrderNumber() {
        return distributedOrderService.getAllOrders()
            .stream()
            .filter(this::isActiveOrder)
            .collect(Collectors.groupingBy(DistributedOrder::getOrderNumber,
                Collectors.collectingAndThen(
                    Collectors.mapping(DistributedOrder::getUserName, Collectors.toSet()),
                    users -> (long) users.size()
                )));
    }

    @Override
    public List<DistributedOrder> getDistributedOrdersByOrderNumber(String orderNumber) {
        return distributedOrderService.getOrdersByOrderNumber(orderNumber);
    }

    @Override
    public void updateOrderStatus(String orderId, OrderStatusEnum status) {
        orderService.updateStatus(status, orderId);
    }

    @Override
    public Order getOriginalOrderByOrderNumber(String orderNumber) {
        return orderService.getByOrderNumber(orderNumber);
    }

    @Override
    public List<ContractValidationReport> getReportOrder(DistributedOrder distributedOrder) {
        return contractService.getContractReport(distributedOrder);
    }

    @Override
    public void updateDistributedOrder(DistributedOrder distributedOrder, Integer readyCount) {
        distributedOrderService.update(distributedOrder, readyCount);
    }

    @Override
    public void updateDistributedOrderStatus(DistributedOrder distributedOrder, OrderStatusEnum status) {
        distributedOrderService.updateStatus(distributedOrder, status);
    }
    @Override
    public void discardDistributedOrder(DistributedOrder distributedOrder) {
        distributedOrderService.discardOrder(distributedOrder);
    }
    @Override
    public List<OrderAudit> getOrderAuditRecordsByOrderNumber(String orderNumber) {
        return auditService.getOrderAuditRecordsByOrderNumber(orderNumber);
    }

    private boolean isActiveOrder(DistributedOrder distributedOrder) {
        OrderStatusEnum status = distributedOrder.getOrderStatus();
        return status == OrderStatusEnum.IN_PROGRESS || status == OrderStatusEnum.WAITING_FOR_APPROVAL;
    }
}

