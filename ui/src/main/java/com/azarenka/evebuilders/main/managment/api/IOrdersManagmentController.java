package com.azarenka.evebuilders.main.managment.api;

import com.azarenka.evebuilders.domain.db.DistributedOrder;
import com.azarenka.evebuilders.domain.db.Order;
import com.azarenka.evebuilders.domain.db.OrderAudit;
import com.azarenka.evebuilders.domain.dto.ShipOrderDto;
import com.azarenka.evebuilders.domain.enums.OrderStatusEnum;
import com.azarenka.evebuilders.service.impl.contract.ContractValidationReport;

import java.util.List;
import java.util.Map;

public interface IOrdersManagmentController {

    List<ShipOrderDto> getOrders();

    Map<String, Long> getActiveUsersByOrderNumber();

    List<DistributedOrder> getDistributedOrdersByOrderNumber(String orderNumber);

    void updateOrderStatus(String orderId, OrderStatusEnum status);

    Order getOriginalOrderByOrderNumber(String orderNumber);

    List<ContractValidationReport> getReportOrder(DistributedOrder distributedOrder);

    void updateDistributedOrder(DistributedOrder distributedOrder, Integer readyCount);

    void updateDistributedOrderStatus(DistributedOrder distributedOrder, OrderStatusEnum status);

    void discardDistributedOrder(DistributedOrder distributedOrder);

    List<OrderAudit> getOrderAuditRecordsByOrderNumber(String orderNumber);
}
