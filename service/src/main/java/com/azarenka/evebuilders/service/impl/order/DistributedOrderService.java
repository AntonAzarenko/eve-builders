package com.azarenka.evebuilders.service.impl.order;

import com.azarenka.evebuilders.domain.OrderStatusEnum;
import com.azarenka.evebuilders.domain.db.DistributedOrder;
import com.azarenka.evebuilders.domain.db.OrderFilter;
import com.azarenka.evebuilders.domain.db.User;
import com.azarenka.evebuilders.domain.dto.ShipOrderDto;
import com.azarenka.evebuilders.domain.dto.TelegramRequestOrder;
import com.azarenka.evebuilders.repository.database.IDistributedOrderRepository;
import com.azarenka.evebuilders.repository.database.OrderSpecification;
import com.azarenka.evebuilders.service.api.IDistributedOrderService;
import com.azarenka.evebuilders.service.api.IOrderService;
import com.azarenka.evebuilders.service.api.IUserService;
import com.azarenka.evebuilders.service.api.integration.ITelegramIntegrationService;
import com.azarenka.evebuilders.service.impl.auth.SecurityUtils;
import com.azarenka.evebuilders.service.util.TelegramMessageCreatorService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class DistributedOrderService implements IDistributedOrderService {

    @Value("${app.telegram_thread_request_id}")
    private String threadRequestId;

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
    public DistributedOrder save(String orderNumber, int count, String userName) {
        DistributedOrder distributedOrder;
        var shipOrderDto = orderService.getOrderById(orderNumber);
        Optional<DistributedOrder> orderOptional = distributedOrderRepository
            .findByOrderNumberAndUserName(orderNumber, userName);
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
    public List<DistributedOrder> getAllByUserName(OrderFilter filter) {
        String userName = SecurityUtils.getUserName();
        filter.setUserId(userName);
        return distributedOrderRepository.findAll(OrderSpecification.withDistributedFilter(filter));
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
    public DistributedOrder distributeOrder(TelegramRequestOrder telegramRequestOrder) {
        return save(telegramRequestOrder.getOrderNumber(), telegramRequestOrder.getCount(),
            telegramRequestOrder.getUserName());
    }

    @Override
    @Transactional
    public List<String> validateRequest(TelegramRequestOrder telegramRequestOrder) {
        var errors = new ArrayList<String>();
        var byOrderNumber = orderService.getByOrderNumber(telegramRequestOrder.getOrderNumber());
        if (Objects.isNull(byOrderNumber)) {
            errors.add("Заказ под номером " + telegramRequestOrder.getOrderNumber() + " не найден.\n");
        }
        Optional<User> byUsername = userService.getByUsername(telegramRequestOrder.getUserName());
        if (byUsername.isEmpty()) {
            errors.add("Пользователь под ником " + telegramRequestOrder.getUserName() + " не найден.\n");
        }
        if (Objects.nonNull(byOrderNumber)) {
            int freeCount = byOrderNumber.getCount() - byOrderNumber.getInProgressCount();
            if (freeCount < telegramRequestOrder.getCount()) {
                errors.add(
                    "Количество запрошенных кораблей превышает количество свободных. Свобоных - " + freeCount + "\n");
            }
        }
        return errors;
    }

    @Override
    public void sendMessage(String message) {
        telegramIntegrationService.sendMessage(message, threadRequestId);
    }

    @Override
    @Transactional
    public List<DistributedOrder> getAllOrders() {
        return distributedOrderRepository.findAll();
    }

    @Override
    @Transactional
    public List<DistributedOrder> getOrdersByOrderNumber(String orderNumber) {
        return distributedOrderRepository.findAllByOrderNumber(orderNumber);
    }

    @Override
    @Transactional
    public void discardOrder(DistributedOrder order) {
        distributedOrderRepository.delete(order);
        var originalOrder = orderService.getByOrderNumber(order.getOrderNumber());
        int inProgressCount = originalOrder.getInProgressCount() - order.getCount();
        originalOrder.setInProgressCount(inProgressCount);
        orderService.updateOrder(originalOrder);
        if (inProgressCount == 0) {
            originalOrder.setOrderStatus(OrderStatusEnum.NEW);
        }
        telegramIntegrationService.sendMessage(
            TelegramMessageCreatorService.createDiscardOrderMessage(order, SecurityUtils.getUserName()),
            threadRequestId);
    }

    private void updateShipOrder(String orderId, int readyCount) {
        var order = orderService.getByOrderNumber(orderId);
        var countReady = order.getCountReady();
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

        var distributedOrder = new DistributedOrder();
        distributedOrder.setId(UUID.randomUUID().toString());
        distributedOrder.setOrderStatus(OrderStatusEnum.IN_PROGRESS);
        distributedOrder.setOrderNumber(shipOrderDto.getOrderNumber());
        distributedOrder.setCount(count);
        distributedOrder.setFitId(shipOrderDto.getFitId());
        distributedOrder.setUserName(userName);
        distributedOrder.setCountReady(0);
        distributedOrder.setShipName(shipOrderDto.getItemName());
        distributedOrder.setOrderRights(shipOrderDto.getOrderRights());
        distributedOrder.setOrderStatus(shipOrderDto.getOrderStatus());
        distributedOrder.setFinishedDate(shipOrderDto.getFinishDate());
        distributedOrder.setCreatedDate(shipOrderDto.getCreatedDate());
        distributedOrder.setCategory(shipOrderDto.getCategory());
        distributedOrder.setPrice(shipOrderDto.getPrice());
        return distributedOrder;
    }
}
