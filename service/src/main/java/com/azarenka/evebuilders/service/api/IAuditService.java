package com.azarenka.evebuilders.service.api;

import com.azarenka.evebuilders.domain.OrderStatusEnum;

public interface IAuditService {

    void writeOrderAudit(OrderStatusEnum status, String orderNumber, String reason, String userName);
}
