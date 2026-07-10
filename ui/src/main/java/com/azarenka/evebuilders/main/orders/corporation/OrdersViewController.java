package com.azarenka.evebuilders.main.orders.corporation;

import com.azarenka.evebuilders.domain.db.DistributedOrder;
import com.azarenka.evebuilders.domain.db.Fit;
import com.azarenka.evebuilders.domain.db.Order;
import com.azarenka.evebuilders.domain.db.OrderFilter;
import com.azarenka.evebuilders.domain.dto.ShipOrderDto;
import com.azarenka.evebuilders.main.orders.api.IOrderViewController;
import com.azarenka.evebuilders.service.api.IContractService;
import com.azarenka.evebuilders.service.api.IDistributedOrderService;
import com.azarenka.evebuilders.service.api.IFitLoaderService;
import com.azarenka.evebuilders.service.api.IOrderFilterService;
import com.azarenka.evebuilders.service.api.IOrderService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class OrdersViewController implements IOrderViewController {

    @Autowired
    private IOrderService orderService;
    @Autowired
    private IFitLoaderService fitLoaderService;
    @Autowired
    private IDistributedOrderService distributedOrderService;
    @Autowired
    private IContractService contractService;
    @Autowired
    private IOrderFilterService orderFilterService;

    @Override
    public List<ShipOrderDto> getOrderList(OrderFilter filter) {
        return orderService.getOrderList(filter)
            .stream()
            .sorted(Comparator.comparing(ShipOrderDto::getCreatedDate, Comparator.reverseOrder()))
            .collect(Collectors.toList());
    }

    @Override
    public Fit getFitById(String id) {
        return fitLoaderService.getFitById(id);
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
    public void saveFilter(OrderFilter filter) {
        orderFilterService.saveFilter(filter);
    }

    @Override
    public OrderFilter getFilter() {
        return orderFilterService.getOrderFilter();
    }
}
