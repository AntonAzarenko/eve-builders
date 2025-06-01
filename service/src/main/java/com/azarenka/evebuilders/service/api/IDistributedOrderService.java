package com.azarenka.evebuilders.service.api;

import com.azarenka.evebuilders.domain.dto.RequestOrder;
import com.azarenka.evebuilders.domain.dto.ShipOrderDto;
import com.azarenka.evebuilders.domain.db.DistributedOrder;

import java.util.List;

public interface IDistributedOrderService {

    DistributedOrder save(ShipOrderDto shipOrderDto, int count, String userName);

    List<DistributedOrder> getAllByUserName();

    DistributedOrder getById(String orderId);

    void update(DistributedOrder distributedOrder, Integer value);

    DistributedOrder distributeOrder(RequestOrder requestOrder);

    List<String> validateRequest(RequestOrder requestOrder);

    void sendMessage(String message);

    List<DistributedOrder> getAllOrders();
}
