package com.azarenka.evebuilders.rest.api.ui;

import com.azarenka.evebuilders.domain.db.DistributedOrder;
import com.azarenka.evebuilders.domain.db.Order;
import com.azarenka.evebuilders.domain.db.OrderFilter;
import com.azarenka.evebuilders.domain.dto.ShipOrderDto;
import com.azarenka.evebuilders.domain.dto.order.DistributedOrderViewDto;
import com.azarenka.evebuilders.domain.dto.order.OrderViewDto;
import com.azarenka.evebuilders.domain.enums.OrderStatusEnum;
import com.azarenka.evebuilders.service.api.IDistributedOrderService;
import com.azarenka.evebuilders.service.api.IOrderService;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping(path = "/api/orders", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin(origins = "*")
@PreAuthorize("@accessControlSecurity.canAny('CONTRACTS_VIEW','CONTRACTS_EDIT','CONTRACTS_ACCEPT','CONTRACTS_CANCEL','CONTRACTS_DISCARD','CORPORATION_VIEW','CORPORATION_CONTRACT_VIEW','CORPORATION_CONTRACT_EDIT')")
@Tag(name = "Orders")
public class OrderRestController {

    private final IOrderService orderService;
    private final IDistributedOrderService distributedOrderService;

    public OrderRestController(IOrderService orderService, IDistributedOrderService distributedOrderService) {
        this.orderService = orderService;
        this.distributedOrderService = distributedOrderService;
    }

    @GetMapping
    public List<OrderViewDto> getOrders(@RequestParam(required = false) List<OrderStatusEnum> statuses,
                                        @RequestParam(required = false) List<String> orderTypes,
                                        @RequestParam(required = false) Integer minFreeCount,
                                        @RequestParam(required = false) Boolean distributed,
                                        @RequestParam(required = false) String search) {
        OrderFilter filter = new OrderFilter();
        filter.setStatuses(statuses);
        filter.setOrderTypes(orderTypes);
        filter.setMinFreeCount(minFreeCount);
        filter.setDistributed(distributed);

        List<OrderViewDto> orders = orderService.getOrderList(filter).stream()
            .map(this::toViewDto)
            .sorted(Comparator.comparing(OrderViewDto::createdDate, Comparator.nullsLast(Comparator.reverseOrder())))
            .toList();

        if (search == null || search.isBlank()) {
            return orders;
        }

        String lowerCaseValue = search.trim().toLowerCase();
        return orders.stream()
            .filter(order -> matchesSearch(order, lowerCaseValue))
            .toList();
    }

    @GetMapping("/{orderNumber}")
    public OrderViewDto getOrder(@PathVariable String orderNumber) {
        Order order = orderService.getByOrderNumber(orderNumber);
        if (order == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found: " + orderNumber);
        }
        return toViewDto(new ShipOrderDto(order));
    }

    @GetMapping("/{orderNumber}/distributed-orders")
    public List<DistributedOrderViewDto> getDistributedOrders(@PathVariable String orderNumber) {
        Order order = orderService.getByOrderNumber(orderNumber);
        if (order == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found: " + orderNumber);
        }
        return distributedOrderService.getOrdersByOrderNumber(orderNumber).stream()
            .map(this::toDistributedViewDto)
            .toList();
    }

    private boolean matchesSearch(OrderViewDto order, String value) {
        return contains(order.orderNumber(), value)
            || contains(order.itemName(), value)
            || contains(order.orderStatus() == null ? null : order.orderStatus().name(), value);
    }

    private boolean contains(String source, String value) {
        return source != null && source.toLowerCase().contains(value);
    }

    private OrderViewDto toViewDto(ShipOrderDto order) {
        Integer count = safeInt(order.getCount());
        Integer inProgressCount = safeInt(order.getInProgressCount());
        Integer countReady = safeInt(order.getCountReady());
        Integer freeCount = Math.max(count - inProgressCount, 0);
        Integer progressPercent = count == 0 || countReady == 0 ? 0 : (int) Math.round((countReady * 100.0) / count);
        LocalDate finishDate = order.getFinishDate();
        Integer daysToFinish = finishDate == null ? null : (int) ChronoUnit.DAYS.between(LocalDate.now(), finishDate);

        return new OrderViewDto(
            order.getId(),
            order.getOrderNumber(),
            order.getItemName(),
            count,
            inProgressCount,
            freeCount,
            countReady,
            order.getPrice(),
            order.getOrderType(),
            order.getDestination(),
            order.getReceiver(),
            order.getPriority(),
            order.isBluePrint(),
            order.getOrderStatus(),
            order.getCreatedBy(),
            order.getCreatedDate(),
            order.getUpdatedBy(),
            order.getUpdatedDate(),
            order.getFitId(),
            order.getOrderRights(),
            order.getRightsholder(),
            order.getCategory(),
            finishDate,
            getDistributionStatus(count, inProgressCount),
            daysToFinish,
            progressPercent
        );
    }

    private DistributedOrderViewDto toDistributedViewDto(DistributedOrder distributedOrder) {
        return new DistributedOrderViewDto(
            distributedOrder.getId(),
            distributedOrder.getOrderNumber(),
            distributedOrder.getShipName(),
            distributedOrder.getUserName(),
            distributedOrder.getCount(),
            distributedOrder.getCountReady(),
            distributedOrder.getFitId(),
            distributedOrder.getOrderRights(),
            distributedOrder.getOrderStatus(),
            distributedOrder.getCreatedDate(),
            distributedOrder.getAppliedDate(),
            distributedOrder.getFinishedDate(),
            distributedOrder.getCategory(),
            distributedOrder.getPrice(),
            distributedOrder.isAssembly()
        );
    }

    private String getDistributionStatus(Integer count, Integer inProgressCount) {
        if (inProgressCount == 0) {
            return "NO";
        }
        return count > inProgressCount ? "PARTIAL" : "FULL";
    }

    private Integer safeInt(Integer value) {
        return value == null ? 0 : value;
    }
}
