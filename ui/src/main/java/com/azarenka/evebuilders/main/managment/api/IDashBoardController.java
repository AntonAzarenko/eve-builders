package com.azarenka.evebuilders.main.managment.api;

import com.azarenka.evebuilders.domain.db.DistributedOrder;
import com.azarenka.evebuilders.domain.db.Order;

import java.util.List;

public interface IDashBoardController {

    List<Order> getOrders();

    List<DistributedOrder> getDistributedOrders();
}
