package com.azarenka.evebuilders.service.api;

import com.azarenka.evebuilders.domain.OrderStatusEnum;
import com.azarenka.evebuilders.domain.db.DistributedOrder;
import com.azarenka.evebuilders.domain.db.OrderFilter;
import com.azarenka.evebuilders.domain.dto.TelegramRequestOrder;

import java.util.List;

public interface IDistributedOrderService {

    DistributedOrder save(String orderNumber, int count, String userName);

    List<DistributedOrder> getAllByUserName(OrderFilter filter);

    DistributedOrder getById(String orderId);

    void update(DistributedOrder distributedOrder, Integer value);

    DistributedOrder distributeOrder(TelegramRequestOrder telegramRequestOrder);

    List<String> validateRequest(TelegramRequestOrder telegramRequestOrder);

    List<DistributedOrder> getAllOrders();

    List<DistributedOrder> getOrdersByOrderNumber(String orderNumber);

    void discardOrder(DistributedOrder order);

    void updateStatus(DistributedOrder distributedOrder, OrderStatusEnum status);

    boolean sendOrderForApproval(DistributedOrder distributedOrder, OrderStatusEnum orderStatusEnum);

    String getDestination(String orderNumber);

    String getReceiver(String orderNumber);
}
