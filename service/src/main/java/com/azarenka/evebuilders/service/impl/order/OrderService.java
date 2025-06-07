package com.azarenka.evebuilders.service.impl.order;

import com.azarenka.evebuilders.domain.OrderStatusEnum;
import com.azarenka.evebuilders.domain.db.OrderFilter;
import com.azarenka.evebuilders.domain.dto.ShipOrderDto;
import com.azarenka.evebuilders.domain.db.Destination;
import com.azarenka.evebuilders.domain.db.Receiver;
import com.azarenka.evebuilders.domain.db.Order;
import com.azarenka.evebuilders.repository.database.OrderSpecification;
import com.azarenka.evebuilders.repository.database.properties.IDestinationRepository;
import com.azarenka.evebuilders.repository.database.IOrderRepository;
import com.azarenka.evebuilders.repository.database.properties.IReceiverRepository;
import com.azarenka.evebuilders.service.api.IOrderService;
import com.azarenka.evebuilders.service.impl.auth.SecurityUtils;
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

    private static final String ORDER_NUMBER_FORMAT = "N-%s-%d";

    @Autowired
    private IDestinationRepository destinationRepository;
    @Autowired
    private IReceiverRepository receiverRepository;
    @Autowired
    private IOrderRepository orderRepository;

    @Override
    public List<Destination> getAllDestination() {
        return destinationRepository.findAll();
    }

    @Override
    public List<Receiver> getAllReceivers() {
        return receiverRepository.findAll();
    }

    @Override
    public Order saveOrder(Order order) {
        order.setId(UUID.randomUUID().toString());
        order.setOrderStatus(OrderStatusEnum.NEW);
        order.setOrderNumber(createOrderNumber());
        order.setCreatedBy(SecurityUtils.getUserName());
        order.setInProgressCount(0);
        order.setCountReady(0);
        return orderRepository.save(order);
    }

    @Override
    public List<ShipOrderDto> getOrderList(OrderFilter filter) {
        Specification<Order> spec = OrderSpecification.withFilter(filter);
        return orderRepository.findAll(spec).stream()
                .map(ShipOrderDto::new)
                .toList();
    }

    @Override
    @Transactional
    public void updateStatus(OrderStatusEnum orderStatusEnum, String id) {
        orderRepository.update(orderStatusEnum, id);
    }

    @Override
    public Order updateOrder(ShipOrderDto orderDto) {
        Optional<Order> byId = orderRepository.findById(orderDto.getId());
        Order order = null;
        if (byId.isPresent()) {
            order = byId.get();
            order.setOrderStatus(orderDto.getOrderStatus());
            order.setInProgressCount(orderDto.getInProgressCount());
            order = orderRepository.save(order);
        }
        return order;
    }

    @Override
    public Order updateOrder(Order orderDto) {
        return orderRepository.save(orderDto);
    }

    @Override
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
    }

    private String createOrderNumber() {
        var today = LocalDate.now();
        int seqNum = orderRepository.findTodayOrdersCount(today);
        return String.format(ORDER_NUMBER_FORMAT, today, seqNum + 1);
    }
}
