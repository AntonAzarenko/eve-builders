package com.azarenka.evebuilders.service.api;

import com.azarenka.evebuilders.domain.db.AuditOrderStatusEnum;
import com.azarenka.evebuilders.domain.db.OrderAudit;

import java.util.List;

public interface IAuditService {

    void writeOrderAudit(AuditOrderStatusEnum status, String orderNumber, String reason, String userName);

    List<OrderAudit> getOrderAuditRecordsByOrderNumber(String orderNumber);
}
