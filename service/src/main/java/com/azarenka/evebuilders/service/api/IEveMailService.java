package com.azarenka.evebuilders.service.api;

import com.azarenka.evebuilders.domain.db.Order;

public interface IEveMailService {

    void sendMailToCoordinator(Order order);
}
