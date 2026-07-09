package com.azarenka.evebuilders.rest.api.ui;

import com.azarenka.evebuilders.domain.acl.Role;
import com.azarenka.evebuilders.domain.db.DistributedOrder;
import com.azarenka.evebuilders.domain.db.Order;
import com.azarenka.evebuilders.domain.db.OrderAudit;
import com.azarenka.evebuilders.domain.db.OrderFilter;
import com.azarenka.evebuilders.domain.dto.ShipOrderDto;
import com.azarenka.evebuilders.domain.enums.OrderStatusEnum;
import com.azarenka.evebuilders.repository.database.IDistributedOrderRepository;
import com.azarenka.evebuilders.repository.database.IUserRepository;
import com.azarenka.evebuilders.service.api.IAccessControlService;
import com.azarenka.evebuilders.service.api.IAuditService;
import com.azarenka.evebuilders.service.api.IContractService;
import com.azarenka.evebuilders.service.api.IDistributedOrderService;
import com.azarenka.evebuilders.service.api.IOrderService;
import com.azarenka.evebuilders.service.impl.contract.ContractValidationReport;

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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/api/orders-management", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin(origins = "*")
@PreAuthorize("@accessControlSecurity.canAny('CONTRACTS_VIEW','CONTRACTS_EDIT','CONTRACTS_ACCEPT','CONTRACTS_CANCEL','CONTRACTS_DISCARD','CORPORATION_VIEW','CORPORATION_CONTRACT_VIEW','CORPORATION_CONTRACT_EDIT')")
@Tag(name = "Orders Management")
public class OrdersManagementRestController {

    private final IOrderService orderService;
    private final IDistributedOrderService distributedOrderService;
    private final IDistributedOrderRepository distributedOrderRepository;
    private final IUserRepository userRepository;
    private final IAccessControlService accessControlService;
    private final IContractService contractService;
    private final IAuditService auditService;

    public OrdersManagementRestController(IOrderService orderService,
                                          IDistributedOrderService distributedOrderService,
                                          IDistributedOrderRepository distributedOrderRepository,
                                          IUserRepository userRepository,
                                          IAccessControlService accessControlService,
                                          IContractService contractService,
                                          IAuditService auditService) {
        this.orderService = orderService;
        this.distributedOrderService = distributedOrderService;
        this.distributedOrderRepository = distributedOrderRepository;
        this.userRepository = userRepository;
        this.accessControlService = accessControlService;
        this.contractService = contractService;
        this.auditService = auditService;
    }

    @GetMapping
    public List<ShipOrderDto> getOrders(@RequestParam(required = false) List<OrderStatusEnum> statuses,
                                        @RequestParam(required = false) List<String> orderTypes,
                                        @RequestParam(required = false) Integer minFreeCount,
                                        @RequestParam(required = false) Boolean distributed,
                                        @RequestParam(required = false) String search) {
        OrderFilter filter = new OrderFilter();
        filter.setStatuses(statuses);
        filter.setOrderTypes(orderTypes);
        filter.setMinFreeCount(minFreeCount);
        filter.setDistributed(distributed);

        List<ShipOrderDto> orders = orderService.getOrderList(filter)
            .stream()
            .sorted(Comparator.comparing(ShipOrderDto::getCreatedDate, Comparator.reverseOrder()))
            .collect(Collectors.toList());

        if (search == null || search.isBlank()) {
            return orders;
        }

        String lowerCaseValue = search.trim().toLowerCase();
        return orders.stream()
            .filter(order -> matchesSearch(order, lowerCaseValue))
            .toList();
    }

    @GetMapping("/active-users")
    public Map<String, Long> getActiveUsersByOrderNumber() {
        return distributedOrderService.getAllOrders()
            .stream()
            .filter(this::isActiveOrder)
            .collect(Collectors.groupingBy(DistributedOrder::getOrderNumber,
                Collectors.collectingAndThen(
                    Collectors.mapping(DistributedOrder::getUserName, Collectors.toSet()),
                    users -> (long) users.size()
                )));
    }

    @GetMapping("/{orderNumber}/distributed")
    public List<DistributedOrder> getDistributedOrdersByOrderNumber(@PathVariable String orderNumber) {
        return distributedOrderService.getOrdersByOrderNumber(orderNumber);
    }

    @PutMapping("/orders/{orderId}/status")
    @Transactional
    public void updateOrderStatus(@PathVariable String orderId, @RequestParam OrderStatusEnum status) {
        orderService.updateStatus(status, orderId);
    }

    @GetMapping("/{orderNumber}/original")
    public Order getOriginalOrderByOrderNumber(@PathVariable String orderNumber) {
        return orderService.getByOrderNumber(orderNumber);
    }

    @PostMapping("/report")
    public List<ContractValidationReport> getReportOrder(@RequestBody DistributedOrder distributedOrder) {
        return contractService.getContractReport(distributedOrder);
    }

    @PutMapping("/distributed")
    @Transactional
    public void updateDistributedOrder(@RequestBody DistributedOrder distributedOrder,
                                       @RequestParam Integer readyCount) {
        distributedOrderService.update(distributedOrder, readyCount);
    }

    @PutMapping("/distributed/status")
    @Transactional
    public void updateDistributedOrderStatus(@RequestBody DistributedOrder distributedOrder,
                                             @RequestParam OrderStatusEnum status) {
        distributedOrderService.updateStatus(distributedOrder, status);
    }

    @DeleteMapping("/distributed")
    @Transactional
    public void discardDistributedOrder(@RequestBody DistributedOrder distributedOrder) {
        distributedOrderService.discardOrder(distributedOrder);
    }

    @PutMapping("/distributed/{orderNumber}/user")
    @Transactional
    public void updateDistributedOrderUser(@PathVariable String orderNumber,
                                           @RequestParam String userName) {
        if (orderNumber == null || orderNumber.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "orderNumber is required");
        }
        if (userName == null || userName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userName is required");
        }

        List<DistributedOrder> distributedOrders = distributedOrderRepository.findAllByOrderNumber(orderNumber);
        if (distributedOrders.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Distributed order not found: " + orderNumber);
        }
        if (distributedOrders.stream().anyMatch(order -> order.getOrderStatus() == OrderStatusEnum.COMPLETED)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Distributed order cannot be reassigned because it is already completed: " + orderNumber);
        }

        var user = userRepository.findByUsername(userName)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + userName));
        boolean builderRolePresent = accessControlService.getUserRoles(user.getUid()).stream()
            .map(Role::getCode)
            .anyMatch(code -> code != null && code.equalsIgnoreCase("BUILDER"));
        if (!builderRolePresent) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "User does not have BUILDER role: " + userName);
        }

        for (DistributedOrder distributedOrder : distributedOrders) {
            distributedOrder.setUserName(userName);
        }
        distributedOrderRepository.saveAll(distributedOrders);
    }

    @GetMapping("/{orderNumber}/audits")
    public List<OrderAudit> getOrderAuditRecordsByOrderNumber(@PathVariable String orderNumber) {
        return auditService.getOrderAuditRecordsByOrderNumber(orderNumber);
    }

    private boolean isActiveOrder(DistributedOrder distributedOrder) {
        OrderStatusEnum status = distributedOrder.getOrderStatus();
        return status == OrderStatusEnum.IN_PROGRESS || status == OrderStatusEnum.WAITING_FOR_APPROVAL;
    }

    private boolean matchesSearch(ShipOrderDto order, String value) {
        return contains(order.getOrderNumber(), value)
            || contains(order.getItemName(), value)
            || contains(order.getOrderStatus() == null ? null : order.getOrderStatus().name(), value);
    }

    private boolean contains(String source, String value) {
        return source != null && source.toLowerCase().contains(value);
    }
}
