package com.azarenka.evebuilders.rest.api.ui;

import com.azarenka.evebuilders.domain.db.DistributedOrder;
import com.azarenka.evebuilders.domain.db.Fit;
import com.azarenka.evebuilders.domain.db.Order;
import com.azarenka.evebuilders.domain.db.OrderFilter;
import com.azarenka.evebuilders.domain.dto.OrderDistributionRequest;
import com.azarenka.evebuilders.domain.dto.order.DistributedOrderViewDto;
import com.azarenka.evebuilders.domain.enums.OrderStatusEnum;
import com.azarenka.evebuilders.service.api.IDistributedOrderService;
import com.azarenka.evebuilders.service.api.IFitLoaderService;
import com.azarenka.evebuilders.service.api.IOrderService;
import com.azarenka.evebuilders.service.impl.auth.eve.SecurityUtils;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping(path = "/api/distributed-orders", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin(origins = "*")
@PreAuthorize("@accessControlSecurity.canAny('CONTRACTS_VIEW','CONTRACTS_EDIT','CONTRACTS_ACCEPT','CONTRACTS_CANCEL','CONTRACTS_DISCARD','CORPORATION_VIEW','CORPORATION_CONTRACT_VIEW','CORPORATION_CONTRACT_EDIT')")
@Tag(name = "Distributed Orders")
public class DistributedOrderRestController {

    private final IOrderService orderService;
    private final IDistributedOrderService distributedOrderService;
    private final IFitLoaderService fitLoaderService;

    public DistributedOrderRestController(IOrderService orderService,
                                          IDistributedOrderService distributedOrderService,
                                          IFitLoaderService fitLoaderService) {
        this.orderService = orderService;
        this.distributedOrderService = distributedOrderService;
        this.fitLoaderService = fitLoaderService;
    }

    @GetMapping
    public List<DistributedOrderViewDto> getOrders(@RequestParam(required = false) List<OrderStatusEnum> statuses,
                                                   @RequestParam(required = false) List<String> orderTypes,
                                                   @RequestParam(required = false) Integer minFreeCount,
                                                   @RequestParam(required = false) Boolean distributed,
                                                   @RequestParam(required = false) String search) {
        OrderFilter filter = new OrderFilter();
        filter.setStatuses(statuses);
        filter.setOrderTypes(orderTypes);
        filter.setMinFreeCount(minFreeCount);
        filter.setDistributed(distributed);

        List<DistributedOrderViewDto> orders = distributedOrderService.getAllByUserName(filter).stream()
            .map(this::toDistributedViewDto)
            .sorted(Comparator.comparing(DistributedOrderViewDto::createdDate, Comparator.nullsLast(Comparator.reverseOrder())))
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
    public List<DistributedOrderViewDto> getDistributedOrders(@PathVariable String orderNumber) {
        Order order = orderService.getByOrderNumber(orderNumber);
        if (order == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found: " + orderNumber);
        }
        return distributedOrderService.getOrdersByOrderNumber(orderNumber).stream()
            .map(this::toDistributedViewDto)
            .toList();
    }

    @GetMapping("/fits/{id}")
    public Fit getFitById(@PathVariable String id) {
        Fit fit = fitLoaderService.getFitById(id);
        if (fit == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Fit not found: " + id);
        }
        return fit;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public void distributeOrders(@RequestBody OrderDistributionRequest orderRequest) {
        if (orderRequest == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "orderRequest is required");
        }
        String userName = SecurityUtils.getUserName();
        if (userName == null || userName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Current user is required");
        }
        if (orderRequest.orderNumber() == null || orderRequest.orderNumber().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "orderNumber is required");
        }
        if (orderRequest.count() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "count must be positive");
        }

        distributedOrderService.save(orderRequest.orderNumber(), orderRequest.count(), userName);
    }

    @PutMapping("/{orderId}/ready-count")
    @Transactional
    public void saveOrder(@PathVariable String orderId, @RequestParam Integer value) {
        if (value == null || value <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "value must be positive");
        }
        DistributedOrder distributedOrder = getDistributedOrderById(orderId);
        distributedOrderService.update(distributedOrder, value);
    }

    @DeleteMapping("/{orderId}")
    @Transactional
    public void discardOrder(@PathVariable String orderId) {
        DistributedOrder distributedOrder = getDistributedOrderById(orderId);
        distributedOrderService.discardOrder(distributedOrder);
    }

    @PostMapping("/{orderId}/approval")
    @Transactional
    public boolean sendOrderForApproval(@PathVariable String orderId) {
        DistributedOrder distributedOrder = getDistributedOrderById(orderId);
        return distributedOrderService.sendOrderForApproval(distributedOrder, OrderStatusEnum.WAITING_FOR_APPROVAL);
    }

    @GetMapping("/{orderNumber}/destination")
    public String getDestination(@PathVariable String orderNumber) {
        return distributedOrderService.getDestination(orderNumber);
    }

    @GetMapping("/{orderNumber}/receiver")
    public String getReceiver(@PathVariable String orderNumber) {
        return distributedOrderService.getReceiver(orderNumber);
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

    private DistributedOrder getDistributedOrderById(String orderId) {
        DistributedOrder distributedOrder = distributedOrderService.getById(orderId);
        if (distributedOrder == null || distributedOrder.getId() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Distributed order not found: " + orderId);
        }
        return distributedOrder;
    }

    private boolean matchesSearch(DistributedOrderViewDto order, String value) {
        return contains(order.orderNumber(), value)
            || contains(order.shipName(), value)
            || contains(order.userName(), value)
            || contains(order.orderStatus() == null ? null : order.orderStatus().name(), value);
    }

    private boolean contains(String source, String value) {
        return source != null && source.toLowerCase().contains(value);
    }
}
