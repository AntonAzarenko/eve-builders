package com.azarenka.evebuilders.service.impl.request;

import com.azarenka.evebuilders.domain.db.RequestOrder;
import com.azarenka.evebuilders.domain.db.RequestOrderStatusEnum;
import com.azarenka.evebuilders.repository.database.IRequestOrderRepository;
import com.azarenka.evebuilders.service.api.IEveMailService;
import com.azarenka.evebuilders.service.api.IRequestOrderService;
import com.azarenka.evebuilders.service.impl.auth.SecurityUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class RequestOrderService implements IRequestOrderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestOrderService.class);

    @Autowired
    private IRequestOrderRepository requestOrderRepository;
    @Autowired
    private IEveMailService mailService;

    @Override
    public RequestOrder save(RequestOrder requestOrder) {
        String userName = SecurityUtils.getUserName();
        LOGGER.info("Create request order. Started. RequestID={}, UserName={}", requestOrder.getId(), userName);
        requestOrder.setId(UUID.randomUUID().toString());
        requestOrder.setRequestStatus(RequestOrderStatusEnum.CREATED);
        requestOrder.setCreatedBy(userName);
        RequestOrder save = requestOrderRepository.save(requestOrder);
        sendMail(requestOrder);
        LOGGER.info("Create request order. Finished. RequestID={}, UserName={}", requestOrder.getId(), userName);
        return save;
    }

    @Override
    @Transactional
    public RequestOrder update(RequestOrder requestOrder) {
        String userName = SecurityUtils.getUserName();
        LOGGER.info("Update request order. Started. RequestID={}, UserName={}", requestOrder.getId(), userName);
        requestOrder.setUpdatedBy(userName);
        requestOrder.setUpdatedDate(LocalDate.now());
        RequestOrder save = requestOrderRepository.save(requestOrder);
        sendMail(requestOrder);
        LOGGER.info("Update request order. Finished. RequestID={}, UserName={}", requestOrder.getId(), userName);
        return save;

    }

    private void sendMail(RequestOrder requestOrder) {
        switch (requestOrder.getRequestStatus()) {
            case CREATED: mailService.sendMailToAdmin(requestOrder); break;
            case SUBMITTED: mailService.sendMailToCoordinator(requestOrder); break;
            case APPROVED: mailService.sendMailToAdmin(requestOrder); break;
            case IN_PROGRESS: mailService.sendMailToCoordinator(requestOrder); break;
            case COMPLETED: mailService.sendMailToCoordinator(requestOrder); break;
        }
    }

    @Override
    public void removeOrder(String id) {
        Optional<RequestOrder> optionalRequestOrder = requestOrderRepository.findById(id);
        optionalRequestOrder.ifPresent(order -> requestOrderRepository.delete(order));
    }

    @Override
    public List<RequestOrder> getAllRequestOrders() {
        return requestOrderRepository.findAll().stream()
            .sorted(Comparator.comparing(RequestOrder::getRequestStatus))
            .toList();
    }

    @Override
    public RequestOrder getRequestById(String id) {
        return requestOrderRepository.findById(id).orElse(null);
    }
}
