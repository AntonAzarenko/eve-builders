package com.azarenka.evebuilders.service.impl.order;

import com.azarenka.evebuilders.domain.db.AuditOrderStatusEnum;
import com.azarenka.evebuilders.domain.db.DistributedOrder;
import com.azarenka.evebuilders.domain.db.OrderFilter;
import com.azarenka.evebuilders.domain.db.RequestOrder;
import com.azarenka.evebuilders.domain.db.RequestOrderStatusEnum;
import com.azarenka.evebuilders.domain.db.User;
import com.azarenka.evebuilders.domain.dto.ShipOrderDto;
import com.azarenka.evebuilders.domain.dto.TelegramRequestOrder;
import com.azarenka.evebuilders.domain.enums.OrderStatusEnum;
import com.azarenka.evebuilders.repository.database.IDistributedOrderRepository;
import com.azarenka.evebuilders.repository.database.OrderSpecification;
import com.azarenka.evebuilders.service.api.IAuditService;
import com.azarenka.evebuilders.service.api.IContractService;
import com.azarenka.evebuilders.service.api.IDistributedOrderService;
import com.azarenka.evebuilders.service.api.IOrderService;
import com.azarenka.evebuilders.service.api.IRequestOrderService;
import com.azarenka.evebuilders.service.api.IUserService;
import com.azarenka.evebuilders.service.api.integration.INotificationService;
import com.azarenka.evebuilders.service.impl.auth.eve.SecurityUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(DistributedOrderService.class);

    @Autowired
    private IDistributedOrderRepository distributedOrderRepository;
    @Autowired
    private IUserService userService;
    @Autowired
    private IOrderService orderService;
    @Autowired
    private INotificationService notificationService;
    @Autowired
    private IAuditService auditService;
    @Autowired
    private IContractService contractService;
    @Autowired
    private IRequestOrderService requestOrderService;

    @Override
    @Transactional
    public DistributedOrder save(String orderNumber, int count, String userName) {
        DistributedOrder distributedOrder = new DistributedOrder();
        var shipOrderDto = orderService.getOrderById(orderNumber);
        if (shipOrderDto.getCount() - shipOrderDto.getInProgressCount() >= count) {
            Optional<DistributedOrder> orderOptional = distributedOrderRepository
                .findByOrderNumberAndUserName(orderNumber, userName);
            if (orderOptional.isPresent() && distributedOrder.getOrderStatus() != OrderStatusEnum.DISCARDED) {
                distributedOrder = orderOptional.get();
                distributedOrder.setCount(distributedOrder.getCount() + count);
                distributedOrder.setOrderStatus(OrderStatusEnum.IN_PROGRESS);
            } else {
                distributedOrder = buildDistributedOrder(shipOrderDto, count, userName);
            }
            DistributedOrder save = distributedOrderRepository.save(distributedOrder);
            int finalCount = shipOrderDto.getInProgressCount() + count;
            shipOrderDto.setInProgressCount(shipOrderDto.getCount() < finalCount ? shipOrderDto.getCount() : finalCount);
            if (shipOrderDto.getOrderStatus() == OrderStatusEnum.NEW) {
                shipOrderDto.setOrderStatus(OrderStatusEnum.IN_PROGRESS);
            }
            if (shipOrderDto.getCount().equals(shipOrderDto.getInProgressCount())) {
                shipOrderDto.setOrderStatus(OrderStatusEnum.DISTRIBUTED);
            }
            orderService.updateOrder(shipOrderDto);
            notificationService.sendOrderTaken(shipOrderDto, count, userName);
            return save;
        }

        return distributedOrder;
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
        int wasReady = distributedOrder.getCountReady();
        int ready = wasReady + value;
        distributedOrder.setCountReady(ready);
        if (distributedOrder.getCount() <= ready) {
            distributedOrder.setOrderStatus(OrderStatusEnum.COMPLETED);
            distributedOrder.setFinishedDate(LocalDate.now());
        } else {
            distributedOrder.setOrderStatus(OrderStatusEnum.IN_PROGRESS);
        }
        distributedOrderRepository.save(distributedOrder);
        updateShipOrder(distributedOrder.getOrderNumber(), value);
        notificationService.sendProgressUpdated(distributedOrder, ready, SecurityUtils.getUserName());
        auditService.writeOrderAudit(AuditOrderStatusEnum.UPDATED, distributedOrder.getOrderNumber(),
            String.format("Count was changed from %s to %s", wasReady, ready), distributedOrder.getUserName());
    }

    @Override
    @Transactional
    public void updateStatus(DistributedOrder distributedOrder, OrderStatusEnum status) {
        OrderStatusEnum oldStatus = distributedOrder.getOrderStatus();
        distributedOrder.setOrderStatus(status);
        distributedOrderRepository.save(distributedOrder);
        auditService.writeOrderAudit(AuditOrderStatusEnum.UPDATED, distributedOrder.getOrderNumber(),
            String.format("Status was changed from %s to %s", oldStatus, status), distributedOrder.getUserName());
    }

    @Override
    @Transactional
    public boolean sendOrderForApproval(DistributedOrder distributedOrder, OrderStatusEnum orderStatusEnum) {
        var username = SecurityUtils.getUserName();
        boolean contractExists = contractService.isContractExists(distributedOrder);
        if (contractExists) {
            LOGGER.info("Sending order for approval from UserName={}. OrderNumber={}", username,
                distributedOrder.getOrderNumber());
            updateStatus(distributedOrder, orderStatusEnum);
            notificationService.sendWaitingForApproval(distributedOrder, username);
            auditService.writeOrderAudit(AuditOrderStatusEnum.UPDATED, distributedOrder.getOrderNumber(),
                String.format("Try to send %s items", distributedOrder.getCount()), username);
            return true;
        }
        return false;
    }

    @Override
    public String getDestination(String orderNumber) {
        return orderService.getOrderById(orderNumber).getDestination();
    }

    @Override
    public String getReceiver(String orderNumber) {
        return orderService.getByOrderNumber(orderNumber).getReceiver();
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
        var originalOrder = orderService.getByOrderNumber(order.getOrderNumber());
        int inProgressCount = originalOrder.getInProgressCount() - order.getCount();
        originalOrder.setInProgressCount(inProgressCount);
        order.setCount(0);
        updateStatus(order, OrderStatusEnum.DISCARDED);
        if (inProgressCount <= 0) {
            originalOrder.setInProgressCount(0);
            originalOrder.setOrderStatus(OrderStatusEnum.NEW);
        }
        if(originalOrder.getCount() < originalOrder.getInProgressCount()) {
            originalOrder.setOrderStatus(OrderStatusEnum.IN_PROGRESS);
        }
        orderService.updateOrder(originalOrder);
        notificationService.sendOrderDiscarded(order, SecurityUtils.getUserName());
        auditService.writeOrderAudit(AuditOrderStatusEnum.DISCARDED, order.getOrderNumber(), "", order.getUserName());
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
            if (Objects.nonNull(order.getRequestId()) && !order.getRequestId().isEmpty()) {
                RequestOrder requestOrder = requestOrderService.getRequestById(order.getRequestId());
                requestOrder.setRequestStatus(RequestOrderStatusEnum.COMPLETED);
                requestOrderService.update(requestOrder);
            }
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
        distributedOrder.setFinishedDate(shipOrderDto.getFinishDate());
        distributedOrder.setCreatedDate(shipOrderDto.getCreatedDate());
        distributedOrder.setCategory(shipOrderDto.getCategory());
        distributedOrder.setPrice(shipOrderDto.getPrice());
        return distributedOrder;
    }
}
