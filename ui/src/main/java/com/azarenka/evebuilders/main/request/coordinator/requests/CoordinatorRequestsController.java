package com.azarenka.evebuilders.main.request.coordinator.requests;

import com.azarenka.evebuilders.domain.db.RequestOrder;
import com.azarenka.evebuilders.main.request.api.IRequestsController;
import com.azarenka.evebuilders.service.api.IRequestOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CoordinatorRequestsController implements IRequestsController {

    @Autowired
    private IRequestOrderService requestOrderService;

    @Override
    public List<RequestOrder> getRequestOrders() {
        return requestOrderService.getAllRequestOrders();
    }
}
