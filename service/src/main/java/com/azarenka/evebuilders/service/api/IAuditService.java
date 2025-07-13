package com.azarenka.evebuilders.service.api;

import com.azarenka.evebuilders.domain.db.AuditOrderStatusEnum;

public interface IAuditService {

    void writeOrderAudit(AuditOrderStatusEnum status, String orderNumber, String reason, String userName);
}
