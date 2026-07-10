package com.azarenka.evebuilders.main.orders.myorders;

import com.azarenka.evebuilders.domain.db.DistributedOrder;
import com.azarenka.evebuilders.domain.db.Fit;
import com.azarenka.evebuilders.domain.db.OrderFilter;
import com.azarenka.evebuilders.domain.enums.OrderStatusEnum;
import com.azarenka.evebuilders.main.constructions.api.ICorporationConstructionController;
import com.azarenka.evebuilders.service.api.IDistributedOrderService;
import com.azarenka.evebuilders.service.api.IFitLoaderService;
import com.azarenka.evebuilders.service.api.IOrderFilterService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CorporationConstructionController implements ICorporationConstructionController {

    @Autowired
    private IFitLoaderService fitLoaderService;
    @Autowired
    private IDistributedOrderService distributedOrderService;
    @Autowired
    private IOrderFilterService orderFilterService;

    @Override
    public List<DistributedOrder> getOrderList(OrderFilter filter) {
        return distributedOrderService.getAllByUserName(filter);
    }

    @Override
    public Fit getFitById(String id) {
        return fitLoaderService.getFitById(id);
    }

    @Override
    public void saveOrder(DistributedOrder distributedOrder, Integer value) {
        distributedOrderService.update(distributedOrder, value);
    }

    public IFitLoaderService getFitLoaderService() {
        return fitLoaderService;
    }

    @Override
    public void discardOrder(DistributedOrder distributedOrder) {
        distributedOrderService.discardOrder(distributedOrder);
    }

    @Override
    public boolean sendOrderForApproval(DistributedOrder distributedOrder) {
        return distributedOrderService.sendOrderForApproval(distributedOrder, OrderStatusEnum.WAITING_FOR_APPROVAL);
    }

    @Override
    public void saveFilter(OrderFilter filter) {
        orderFilterService.saveFilter(filter);
    }

    @Override
    public OrderFilter getFilter() {
        return orderFilterService.getOrderFilter();
    }

    @Override
    public String getDestination(String orderNumber) {
        return distributedOrderService.getDestination(orderNumber);
    }

    @Override
    public String getReceiver(String orderNumber) {
        return distributedOrderService.getReceiver(orderNumber);
    }
}
