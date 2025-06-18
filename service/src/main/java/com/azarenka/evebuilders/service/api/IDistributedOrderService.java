package com.azarenka.evebuilders.service.api;

import com.azarenka.evebuilders.domain.db.OrderFilter;
import com.azarenka.evebuilders.domain.dto.TelegramRequestOrder;
import com.azarenka.evebuilders.domain.dto.ShipOrderDto;
import com.azarenka.evebuilders.domain.db.DistributedOrder;

import java.util.List;

public interface IDistributedOrderService {

    DistributedOrder save(String orderNumber, int count, String userName);

    List<DistributedOrder> getAllByUserName(OrderFilter filter);

    DistributedOrder getById(String orderId);

    void update(DistributedOrder distributedOrder, Integer value);

    DistributedOrder distributeOrder(TelegramRequestOrder telegramRequestOrder);

    List<String> validateRequest(TelegramRequestOrder telegramRequestOrder);

    void sendMessage(String message);

    List<DistributedOrder> getAllOrders();

    List<DistributedOrder> getOrdersByOrderNumber(String orderNumber);

    void discardOrder(DistributedOrder order);
}
