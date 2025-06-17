package com.azarenka.evebuilders.main;

import com.azarenka.evebuilders.domain.db.RequestOrderStatusEnum;
import com.azarenka.evebuilders.service.api.IRequestOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MainWidgetController {

    @Autowired
    private IRequestOrderService requestOrderService;

    public int countRequests() {
        return (int) requestOrderService.getAllRequestOrders().stream()
                .filter(requestOrder -> RequestOrderStatusEnum.SUBMITTED == requestOrder.getRequestStatus())
                .count();
    }
}
