package com.azarenka.evebuilders.service.impl.order;

import com.azarenka.evebuilders.domain.OrderStatusEnum;
import com.azarenka.evebuilders.domain.dto.RequestOrder;
import com.azarenka.evebuilders.domain.dto.ShipOrderDto;
import com.azarenka.evebuilders.domain.mysql.DistributedOrder;
import com.azarenka.evebuilders.domain.mysql.Order;
import com.azarenka.evebuilders.domain.mysql.User;
import com.azarenka.evebuilders.repository.mysql.IDistributedOrderRepository;
import com.azarenka.evebuilders.service.api.IDistributedOrderService;
import com.azarenka.evebuilders.service.api.IOrderService;
import com.azarenka.evebuilders.service.api.ITelegramIntegrationService;
import com.azarenka.evebuilders.service.api.IUserService;
import com.azarenka.evebuilders.service.impl.auth.SecurityUtils;
import com.azarenka.evebuilders.service.util.TelegramMessageCreatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
public class DistributedOrderService implements IDistributedOrderService {

    @Autowired
    private IDistributedOrderRepository distributedOrderRepository;
    @Autowired
    private IUserService userService;
    @Autowired
    private IOrderService orderService;
    @Autowired
    private ITelegramIntegrationService telegramIntegrationService;

    @Override
    @Transactional
    public DistributedOrder save(ShipOrderDto shipOrderDto, int count, String userName) {
        DistributedOrder distributedOrder;
        Optional<DistributedOrder> orderOptional = distributedOrderRepository
                .findByOrderNumberAndUserName(shipOrderDto.getOrderNumber(), userName);
        if (orderOptional.isPresent()) {
            distributedOrder = orderOptional.get();
            distributedOrder.setCount(distributedOrder.getCount() + count);
            distributedOrder.setOrderStatus(OrderStatusEnum.IN_PROGRESS);
        } else {
            distributedOrder = buildDistributedOrder(shipOrderDto, count, userName);
        }
        DistributedOrder save = distributedOrderRepository.save(distributedOrder);
        if (shipOrderDto.getOrderStatus() == OrderStatusEnum.NEW) {
            shipOrderDto.setOrderStatus(OrderStatusEnum.IN_PROGRESS);
        }
        shipOrderDto.setInProgressCount(shipOrderDto.getInProgressCount() + count);
        orderService.updateOrder(shipOrderDto);
        sendMessage(TelegramMessageCreatorService.createTakeOrderMessage(shipOrderDto, count, userName));
        return save;
    }

    @Override
    public List<DistributedOrder> getAllByUserName() {
        String userName = SecurityUtils.getUserName();
        return distributedOrderRepository.findAllByUserName(userName);
    }

    @Override
    public DistributedOrder getById(String orderId) {
        return distributedOrderRepository.findById(orderId).orElse(new DistributedOrder());
    }

    @Override
    @Transactional
    public void update(DistributedOrder distributedOrder, Integer value) {
        int ready = distributedOrder.getCountReady() + value;
        distributedOrder.setCountReady(ready);
        if (distributedOrder.getCount() == ready) {
            distributedOrder.setOrderStatus(OrderStatusEnum.COMPLETED);
            distributedOrder.setFinishedDate(LocalDate.now());
        }
        distributedOrderRepository.save(distributedOrder);
        updateShipOrder(distributedOrder.getOrderNumber(), value);
    }

    @Override
    @Transactional
    public DistributedOrder distributeOrder(RequestOrder requestOrder) {
        Order byOrderNumber = orderService.getByOrderNumber(requestOrder.getOrderNumber());
        ShipOrderDto shipOrderDto = new ShipOrderDto(byOrderNumber);
        return save(shipOrderDto, requestOrder.getCount(), requestOrder.getUserName());
    }

    @Override
    public List<String> validateRequest(RequestOrder requestOrder) {
        List<String> errors = new ArrayList<>();
        Order byOrderNumber = orderService.getByOrderNumber(requestOrder.getOrderNumber());
        if (Objects.isNull(byOrderNumber)) {
            errors.add("Заказ под номером " + requestOrder.getOrderNumber() + " не найден.\n");
        }
        Optional<User> byUsername = userService.getByUsername(requestOrder.getUserName());
        if (byUsername.isEmpty()) {
            errors.add("Юзер под ником " + requestOrder.getUserName() + " не найден.\n");
        }
        if (Objects.nonNull(byOrderNumber)) {
            int freeCount = byOrderNumber.getCount() - byOrderNumber.getInProgressCount();
            if (freeCount < requestOrder.getCount()) {
                errors.add("Количество запрошенных кораблей превышает количество свободных. Свобоных - " + freeCount + "\n");
            }
        }
        return errors;
    }

    @Override
    public void sendMessage(String message) {
        telegramIntegrationService.sendMessage(message);
    }

    @Override
    public List<DistributedOrder> getAllOrders() {
        return distributedOrderRepository.findAll();
    }

    private void updateShipOrder(String orderId, int readyCount) {
        Order order = orderService.getByOrderNumber(orderId);
        Integer countReady = order.getCountReady();
        countReady = countReady + readyCount;
        order.setCountReady(countReady);
        order.setUpdatedDate(LocalDate.now());
        order.setUpdatedBy(SecurityUtils.getUserName());
        if (order.getCount().equals(countReady)) {
            order.setOrderStatus(OrderStatusEnum.COMPLETED);
        }
        orderService.updateOrder(order);
    }

    private DistributedOrder buildDistributedOrder(ShipOrderDto shipOrderDto, int count, String userName) {

        DistributedOrder distributedOrder = new DistributedOrder();
        distributedOrder.setId(UUID.randomUUID().toString());
        distributedOrder.setOrderNumber(shipOrderDto.getOrderNumber());
        distributedOrder.setCount(count);
        distributedOrder.setFitId(shipOrderDto.getFitId());
        distributedOrder.setUserName(userName);
        distributedOrder.setCountReady(0);
        distributedOrder.setShipName(shipOrderDto.getShipName());
        distributedOrder.setOrderRights(shipOrderDto.getOrderRights());
        distributedOrder.setOrderStatus(shipOrderDto.getOrderStatus());
        distributedOrder.setFinishedDate(shipOrderDto.getFinishBy());
        return distributedOrder;
    }
}
