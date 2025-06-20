package com.azarenka.evebuilders.service.api;

import com.azarenka.evebuilders.domain.OrderStatusEnum;
import com.azarenka.evebuilders.domain.db.OrderFilter;
import com.azarenka.evebuilders.domain.dto.ShipOrderDto;
import com.azarenka.evebuilders.domain.db.Destination;
import com.azarenka.evebuilders.domain.db.Receiver;
import com.azarenka.evebuilders.domain.db.Order;

import java.util.List;

public interface IOrderService {

    List<Destination> getAllDestination();

    List<Receiver> getAllReceivers();

    Order saveOrder(Order order);

    List<ShipOrderDto> getOrderList(OrderFilter filter);

    ShipOrderDto getOrderById(String orderNumber);

    void updateStatus(OrderStatusEnum orderStatusEnum, String id);

    Order updateOrder(ShipOrderDto orderDto);

    Order updateOrder(Order orderDto);

    Order getByOrderNumber(String orderId);

    void addNewDestination(String value);

    void addNewReceiver(String value);

    List<Order> getOriginalOrderList();

    void removeOrder(String orderNumber);
}
