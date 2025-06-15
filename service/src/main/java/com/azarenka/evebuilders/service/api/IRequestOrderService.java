package com.azarenka.evebuilders.service.api;

import com.azarenka.evebuilders.domain.db.Order;
import com.azarenka.evebuilders.domain.db.RequestOrder;

import java.util.List;

public interface IRequestOrderService {

    RequestOrder save(RequestOrder requestOrder);

    RequestOrder update(RequestOrder requestOrder);

    void removeOrder(String id);

    List<RequestOrder> getAllRequestOrders();

    RequestOrder getRequestById(String id);
}
