package com.azarenka.evebuilders.main.managment.dashboard;

import com.azarenka.evebuilders.domain.db.DistributedOrder;
import com.azarenka.evebuilders.domain.db.Order;
import com.azarenka.evebuilders.main.managment.api.IDashBoardController;
import com.azarenka.evebuilders.service.api.IContractService;
import com.azarenka.evebuilders.service.api.IDistributedOrderService;
import com.azarenka.evebuilders.service.api.IOrderService;
import com.azarenka.evebuilders.service.impl.contract.ContractValidationReport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class DashboardController implements IDashBoardController {

    @Autowired
    private IOrderService orderService;
    @Autowired
    private IDistributedOrderService distributedOrderService;
    @Autowired
    private IContractService contractService;

    @Override
    public List<Order> getOrders() {
        return orderService.getOriginalOrderList();
    }

    @Override
    public List<DistributedOrder> getDistributedOrders() {
        return distributedOrderService.getAllOrders();
    }

    @Override
    public List<ContractValidationReport> getReporOtrder(DistributedOrder distributedOrder) {
        return contractService.getContractReport(distributedOrder);
    }

    @Override
    public void update(DistributedOrder distributedOrder, Integer readyCount) {
        distributedOrderService.update(distributedOrder, readyCount);
    }
}
