package com.azarenka.evebuilders.main;

import com.azarenka.evebuilders.domain.enums.OrderStatusEnum;
import com.azarenka.evebuilders.domain.db.OrderFilter;
import com.azarenka.evebuilders.domain.db.RequestOrderStatusEnum;
import com.azarenka.evebuilders.service.api.IOrderService;
import com.azarenka.evebuilders.service.api.IRequestOrderService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MainWidgetController {

    @Autowired
    private IRequestOrderService requestOrderService;
    @Autowired
    private IOrderService orderService;

    public int countRequests() {
        return (int) requestOrderService.getAllRequestOrders().stream()
            .filter(requestOrder ->
                RequestOrderStatusEnum.CREATED == requestOrder.getRequestStatus() ||
                    RequestOrderStatusEnum.APPROVED == requestOrder.getRequestStatus())
            .count();
    }

    public int countNewOrders() {
        return (int) orderService.getOrderList(new OrderFilter()).stream()
            .filter(order ->
                OrderStatusEnum.NEW == order.getOrderStatus())
            .count();
    }
}
