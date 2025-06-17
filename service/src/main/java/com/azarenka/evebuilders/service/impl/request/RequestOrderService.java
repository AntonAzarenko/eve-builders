package com.azarenka.evebuilders.service.impl.request;

import com.azarenka.evebuilders.domain.db.RequestOrder;
import com.azarenka.evebuilders.domain.db.RequestOrderStatusEnum;
import com.azarenka.evebuilders.repository.database.IRequestOrderRepository;
import com.azarenka.evebuilders.service.api.IRequestOrderService;
import com.azarenka.evebuilders.service.impl.auth.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class RequestOrderService implements IRequestOrderService {

    @Autowired
    private IRequestOrderRepository requestOrderRepository;

    @Override
    public RequestOrder save(RequestOrder requestOrder) {
        requestOrder.setId(UUID.randomUUID().toString());
        requestOrder.setRequestStatus(RequestOrderStatusEnum.SUBMITTED);
        requestOrder.setCreatedBy(SecurityUtils.getUserName());
        return requestOrderRepository.save(requestOrder);
    }

    @Override
    public RequestOrder update(RequestOrder requestOrder) {
        requestOrder.setUpdatedBy(SecurityUtils.getUserName());
        requestOrder.setUpdatedDate(LocalDate.now());
        return requestOrderRepository.save(requestOrder);
    }

    @Override
    public void removeOrder(String id) {
        Optional<RequestOrder> optionalRequestOrder = requestOrderRepository.findById(id);
        optionalRequestOrder.ifPresent(order -> requestOrderRepository.delete(order));
    }

    @Override
    public List<RequestOrder> getAllRequestOrders() {
        return requestOrderRepository.findAll();
    }

    @Override
    public RequestOrder getRequestById(String id) {
        return requestOrderRepository.findById(id).orElse(null);
    }
}
