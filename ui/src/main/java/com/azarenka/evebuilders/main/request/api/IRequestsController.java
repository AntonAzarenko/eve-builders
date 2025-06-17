package com.azarenka.evebuilders.main.request.api;

import com.azarenka.evebuilders.domain.db.RequestOrder;

import java.util.List;

public interface IRequestsController {

    List<RequestOrder> getRequestOrders();
}
