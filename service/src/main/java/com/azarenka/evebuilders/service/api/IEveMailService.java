package com.azarenka.evebuilders.service.api;

import com.azarenka.evebuilders.domain.db.Order;
import com.azarenka.evebuilders.domain.db.RequestOrder;

public interface IEveMailService {

    void sendMailToCoordinator(Order order);
    void sendMailToAdmin(RequestOrder order);
    void sendMailToCoordinator(RequestOrder order);
}
