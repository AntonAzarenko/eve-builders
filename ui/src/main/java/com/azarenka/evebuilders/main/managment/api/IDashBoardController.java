package com.azarenka.evebuilders.main.managment.api;

import com.azarenka.evebuilders.domain.mysql.DistributedOrder;
import com.azarenka.evebuilders.domain.mysql.Order;

import java.util.List;

public interface IDashBoardController {

    List<Order> getOrders();

    List<DistributedOrder> getDistributedOrders();
}
