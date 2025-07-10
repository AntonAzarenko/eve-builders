package com.azarenka.evebuilders.service.impl;

import com.azarenka.evebuilders.domain.OrderStatusEnum;
import com.azarenka.evebuilders.domain.db.OrderAudit;
import com.azarenka.evebuilders.repository.database.IDistributedOrderAuditRepository;
import com.azarenka.evebuilders.repository.database.IOrderAuditRepository;
import com.azarenka.evebuilders.service.api.IAuditService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuditService implements IAuditService {

    @Autowired
    private IOrderAuditRepository orderAuditRepository;
    @Autowired
    private IDistributedOrderAuditRepository distributedOrderAuditRepository;

    @Override
    public void writeOrderAudit(OrderStatusEnum status, String orderNumber, String reason, String userName) {
        var orderAudit = new OrderAudit();
        orderAudit.setId(UUID.randomUUID().toString());
        orderAudit.setOrderNumber(orderNumber);
        orderAudit.setCreatedBy(userName);
        orderAudit.setReason(reason);
        orderAuditRepository.save(orderAudit);
    }
}
