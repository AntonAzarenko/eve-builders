package com.azarenka.evebuilders.service.api.integration;

import com.azarenka.evebuilders.domain.db.DistributedOrder;
import com.azarenka.evebuilders.domain.db.Order;
import com.azarenka.evebuilders.domain.dto.ShipOrderDto;

public interface INotificationService {

    void sendOrderCreated(Order order);

    void sendOrderUpdated(Order order);

    void sendOrderRemoved(String orderNumber);

    void sendOrderTaken(ShipOrderDto orderDto, int count, String userName);

    void sendProgressUpdated(DistributedOrder distributedOrder, int readyCount, String userName);

    void sendWaitingForApproval(DistributedOrder distributedOrder, String userName);

    void sendOrderDiscarded(DistributedOrder distributedOrder, String userName);
}
