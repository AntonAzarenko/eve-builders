package com.azarenka.evebuilders.main.orders.api;

import com.azarenka.evebuilders.domain.db.DistributedOrder;
import com.azarenka.evebuilders.domain.db.OrderFilter;
import com.azarenka.evebuilders.domain.dto.ShipOrderDto;
import com.azarenka.evebuilders.domain.db.Fit;
import com.azarenka.evebuilders.domain.db.Order;
import com.azarenka.evebuilders.service.api.IFitLoaderService;
import com.azarenka.evebuilders.service.util.ImageService;
import com.vaadin.flow.component.textfield.IntegerField;

import java.util.List;
import java.util.Map;

public interface IOrderViewController {

    List<ShipOrderDto> getOrderList(OrderFilter filter);

    Fit getFitById(String id);

    ImageService getImageProviderService();

    void saveOrders(Map<ShipOrderDto, IntegerField> orderTexfieldMap);

    Order getOriginalOrderByOrderNumber(String orderNumber);

    IFitLoaderService getFitLoaderService();

    List<DistributedOrder> getDistributedOrdersByOrderNumber(String orderNumber);
}
