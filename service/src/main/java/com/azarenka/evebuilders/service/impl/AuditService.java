package com.azarenka.evebuilders.service.impl;

import com.azarenka.evebuilders.domain.db.AuditOrderStatusEnum;
import com.azarenka.evebuilders.domain.db.OrderAudit;
import com.azarenka.evebuilders.repository.database.IDistributedOrderAuditRepository;
import com.azarenka.evebuilders.repository.database.IOrderAuditRepository;
import com.azarenka.evebuilders.service.api.IAuditService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AuditService implements IAuditService {

    @Autowired
    private IOrderAuditRepository orderAuditRepository;
    @Autowired
    private IDistributedOrderAuditRepository distributedOrderAuditRepository;

    @Override
    public void writeOrderAudit(AuditOrderStatusEnum status, String orderNumber, String reason, String userName) {
        var orderAudit = new OrderAudit();
        orderAudit.setId(UUID.randomUUID().toString());
        orderAudit.setOrderNumber(orderNumber);
        orderAudit.setStatus(status);
        orderAudit.setCreatedBy(userName);
        orderAudit.setReason(reason);
        orderAudit.setUpdatedBy(userName);
        orderAuditRepository.save(orderAudit);
    }

    @Override
    public List<OrderAudit> getOrderAuditRecordsByOrderNumber(String orderNumber) {
        return orderAuditRepository.findByOrderNumberOrderByCreatedDateDesc(orderNumber);
    }
}
