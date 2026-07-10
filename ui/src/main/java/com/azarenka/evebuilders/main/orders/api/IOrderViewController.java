package com.azarenka.evebuilders.main.orders.api;

import com.azarenka.evebuilders.domain.db.DistributedOrder;
import com.azarenka.evebuilders.domain.db.Fit;
import com.azarenka.evebuilders.domain.db.Order;
import com.azarenka.evebuilders.domain.db.OrderFilter;
import com.azarenka.evebuilders.domain.dto.ShipOrderDto;
import com.azarenka.evebuilders.service.api.IFitLoaderService;

import java.util.List;

public interface IOrderViewController {

    List<ShipOrderDto> getOrderList(OrderFilter filter);

    Fit getFitById(String id);

    Order getOriginalOrderByOrderNumber(String orderNumber);

    IFitLoaderService getFitLoaderService();

    List<DistributedOrder> getDistributedOrdersByOrderNumber(String orderNumber);

    void saveFilter(OrderFilter filter);

    OrderFilter getFilter();
}
