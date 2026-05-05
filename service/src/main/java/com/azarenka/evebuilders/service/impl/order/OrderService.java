package com.azarenka.evebuilders.service.impl.order;

import com.azarenka.evebuilders.domain.enums.OrderStatusEnum;
import com.azarenka.evebuilders.domain.db.AuditOrderStatusEnum;
import com.azarenka.evebuilders.domain.db.Destination;
import com.azarenka.evebuilders.domain.db.Order;
import com.azarenka.evebuilders.domain.db.OrderFilter;
import com.azarenka.evebuilders.domain.db.Receiver;
import com.azarenka.evebuilders.domain.enums.ReceiverTargetType;
import com.azarenka.evebuilders.domain.dto.ShipOrderDto;
import com.azarenka.evebuilders.repository.database.IOrderRepository;
import com.azarenka.evebuilders.repository.database.OrderSpecification;
import com.azarenka.evebuilders.repository.database.properties.IDestinationRepository;
import com.azarenka.evebuilders.repository.database.properties.IReceiverRepository;
import com.azarenka.evebuilders.service.api.IAuditService;
import com.azarenka.evebuilders.service.api.IOrderService;
import com.azarenka.evebuilders.service.api.integration.INotificationService;
import com.azarenka.evebuilders.service.impl.auth.eve.SecurityUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderService implements IOrderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderService.class);
    private static final String ORDER_NUMBER_FORMAT = "N%s%d";

    @Autowired
    private IDestinationRepository destinationRepository;
    @Autowired
    private IReceiverRepository receiverRepository;
    @Autowired
    private IOrderRepository orderRepository;
    @Autowired
    private IAuditService auditService;
    @Autowired
    private INotificationService notificationService;

    @Override
    public List<Destination> getAllDestination() {
        return destinationRepository.findAll();
    }

    @Override
    public List<Receiver> getAllReceivers() {
        return receiverRepository.findAll();
    }

    @Override
    @Transactional
    public Order saveOrder(Order order) {
        var userName = SecurityUtils.getUserName();
        var orderNumber = createOrderNumber();
        LOGGER.info("Creating order. Started. OrderNumber={}, ItemName={}, UserName={}", orderNumber,
            order.getShipName(), userName);
        order.setId(UUID.randomUUID().toString());
        order.setOrderStatus(OrderStatusEnum.NEW);
        order.setOrderNumber(orderNumber);
        order.setCreatedBy(userName);
        order.setInProgressCount(0);
        order.setCountReady(0);
        normalizeReceiverFields(order);
        notificationService.sendOrderCreated(order);
        var savedOrder = orderRepository.save(order);
        LOGGER.info("Creating order. Finished. OrderNumber={}, ItemName={}, UserName={}", orderNumber,
            order.getShipName(), userName);
        auditService.writeOrderAudit(AuditOrderStatusEnum.CREATED, orderNumber, order.getRequestId(), userName);
        return savedOrder;
    }

    @Override
    @Transactional
    public List<ShipOrderDto> getOrderList(OrderFilter filter) {
        Specification<Order> spec = OrderSpecification.withFilter(filter);
        return orderRepository.findAll(spec).stream()
            .map(ShipOrderDto::new)
            .toList();
    }

    @Override
    @Transactional
    public ShipOrderDto getOrderById(String orderNumber) {
        Optional<Order> optionalOrder = orderRepository.findByOrderNumber(orderNumber);
        return new ShipOrderDto(optionalOrder.orElseThrow());
    }

    @Override
    @Transactional
    public void updateStatus(OrderStatusEnum status, String id) {
        var userName = SecurityUtils.getUserName();
        orderRepository.update(status, id);
        LOGGER.info("Order status. Updated. OrderID={}, OrderStatus={}, UserName={}", id, status, userName);
        auditService.writeOrderAudit(AuditOrderStatusEnum.UPDATED, id, "", userName);
    }

    @Override
    @Transactional
    public Order updateOrder(ShipOrderDto orderDto) {
        var order = orderRepository.findById(orderDto.getId()).get();
        var userName = SecurityUtils.getUserName();
        LOGGER.info("Updating order. Started. OrderNumber={}, ItemName={}, UserName={}", orderDto.getOrderNumber(),
            orderDto.getItemName(), userName);
        order.setOrderStatus(orderDto.getOrderStatus());
        order.setInProgressCount(orderDto.getInProgressCount());
        order = orderRepository.save(order);
        LOGGER.info("Updating order. Finished. OrderNumber={}, ItemName={}, UserName={}", orderDto.getOrderNumber(),
            orderDto.getItemName(), userName);
        auditService.writeOrderAudit(AuditOrderStatusEnum.DISTRIBUTED, orderDto.getOrderNumber(), "", userName);
        return order;
    }

    @Override
    @Transactional
    public Order updateOrder(Order orderDto) {
        normalizeReceiverFields(orderDto);
        return orderRepository.save(orderDto);
    }

    @Override
    @Transactional
    public Order getByOrderNumber(String orderId) {
        return orderRepository.findByOrderNumber(orderId).orElse(null);
    }

    @Override
    public void addNewDestination(String value) {
        Destination destination = new Destination();
        destination.setDestination(value);
        destination.setDestId(UUID.randomUUID().toString());
        destinationRepository.save(destination);
    }

    @Override
    public void updateDestination(String destinationId, String value) {
        destinationRepository.findById(destinationId).ifPresent(destination -> {
            destination.setDestination(value);
            destinationRepository.save(destination);
        });
    }

    @Override
    public void removeDestination(String destinationId) {
        destinationRepository.deleteById(destinationId);
    }

    @Override
    public void addNewReceiver(String value) {
        Receiver receiver = new Receiver();
        receiver.setReceiver(value);
        receiver.setResId(UUID.randomUUID().toString());
        receiverRepository.save(receiver);
    }

    @Override
    public List<Order> getOriginalOrderList() {
        return orderRepository.findAll();
    }

    @Override
    @Transactional
    public void removeOrder(String orderNumber) {
        orderRepository.deleteByOrderNumber(orderNumber);
        notificationService.sendOrderRemoved(orderNumber);
    }

    @Override
    public Order getByRequestId(String requestId) {
        return orderRepository.findOrderByRequestId(requestId);
    }

    private String createOrderNumber() {
        var date = LocalDate.now();
        int seqNum = orderRepository.findTodayOrdersCount(date);
        var number = date.toString().replace("-", "");
        return String.format(ORDER_NUMBER_FORMAT, number, seqNum + 1);
    }

    private void normalizeReceiverFields(Order order) {
        if (order.getReceiverType() == null) {
            order.setReceiverType(ReceiverTargetType.CORPORATION);
        }
        if (order.getReceiverRefId() == null || order.getReceiverRefId().isBlank()) {
            order.setReceiverRefId("0");
        }
        if (order.getReceiverName() == null) {
            order.setReceiverName(order.getReceiver() == null ? "" : order.getReceiver());
        }
        if (order.getReceiver() == null) {
            order.setReceiver(order.getReceiverName());
        }
    }
}
