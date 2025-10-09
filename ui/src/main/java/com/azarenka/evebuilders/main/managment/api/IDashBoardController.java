package com.azarenka.evebuilders.main.managment.api;

import com.azarenka.evebuilders.domain.db.DistributedOrder;
import com.azarenka.evebuilders.domain.db.Order;
import com.azarenka.evebuilders.service.impl.contract.ContractValidationReport;

import java.util.List;

public interface IDashBoardController {

    List<Order> getOrders();

    List<DistributedOrder> getDistributedOrders();

    List<ContractValidationReport> getReportOtrder(DistributedOrder distributedOrder);

    void update(DistributedOrder distributedOrder, Integer readyCount);
}
