package com.azarenka.evebuilders.rest.api.ui;

import com.azarenka.evebuilders.domain.db.Destination;
import com.azarenka.evebuilders.domain.db.ManagedCorporation;
import com.azarenka.evebuilders.domain.db.Order;
import com.azarenka.evebuilders.domain.db.Receiver;
import com.azarenka.evebuilders.domain.db.RequestOrder;
import com.azarenka.evebuilders.domain.db.RequestOrderStatusEnum;
import com.azarenka.evebuilders.domain.dto.OrderPresetDefaultsDto;
import com.azarenka.evebuilders.domain.dto.UserDto;
import com.azarenka.evebuilders.domain.sqllite.InvGroup;
import com.azarenka.evebuilders.domain.sqllite.InvType;
import com.azarenka.evebuilders.service.api.ICorporationService;
import com.azarenka.evebuilders.service.api.IEveMailService;
import com.azarenka.evebuilders.service.api.IEveMaterialDataService;
import com.azarenka.evebuilders.service.api.IOrderService;
import com.azarenka.evebuilders.service.api.IOrderPresetDefaultsService;
import com.azarenka.evebuilders.service.api.IRequestOrderService;
import com.azarenka.evebuilders.service.api.IUserService;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping(path = "/api/create-order", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin(origins = "*")
@PreAuthorize("@accessControlSecurity.can('CONTRACTS_CREATE')")
@Tag(name = "Create Order")
public class CreateOrderRestController {

    private final IEveMaterialDataService dataService;
    private final IOrderService orderService;
    private final IRequestOrderService requestOrderService;
    private final IEveMailService mailService;
    private final ICorporationService corporationService;
    private final IUserService userService;
    private final IOrderPresetDefaultsService orderPresetDefaultsService;

    public CreateOrderRestController(IEveMaterialDataService dataService,
                                     IOrderService orderService,
                                     IRequestOrderService requestOrderService,
                                     IEveMailService mailService,
                                     ICorporationService corporationService,
                                     IUserService userService,
                                     IOrderPresetDefaultsService orderPresetDefaultsService) {
        this.dataService = dataService;
        this.orderService = orderService;
        this.requestOrderService = requestOrderService;
        this.mailService = mailService;
        this.corporationService = corporationService;
        this.userService = userService;
        this.orderPresetDefaultsService = orderPresetDefaultsService;
    }

    @GetMapping("/groups/{id}")
    public List<InvGroup> getInvGroupsById(@PathVariable Integer id) {
        return dataService.getInvGroupsById(id);
    }

    @GetMapping("/types")
    public List<InvType> getTypesByGroupIds(@RequestParam List<Integer> groupIds) {
        return dataService.getTypesByGroupIds(groupIds);
    }

    @GetMapping("/types/{groupId}")
    public List<InvType> getTypesByGroupId(@PathVariable Integer groupId) {
        return dataService.getTypesByGroupId(groupId);
    }

    @PostMapping("/orders")
    @Transactional
    public Order createOrder(@RequestBody Order order) {
        validateOrder(order);
        Order byOrderNumber = orderService.getByOrderNumber(order.getOrderNumber());
        if (Objects.nonNull(byOrderNumber)) {
            return orderService.updateOrder(order);
        }
        return orderService.saveOrder(order);
    }

    @GetMapping("/destinations")
    public List<Destination> getAllDestination() {
        return orderService.getAllDestination();
    }

    @GetMapping("/receivers")
    public List<Receiver> getAllReceivers() {
        return orderService.getAllReceivers();
    }

    @GetMapping("/managed-corporations")
    public List<ManagedCorporation> getAllManagedCorporations() {
        return corporationService.getAllCorporations();
    }

    @GetMapping("/receiver-users")
    public List<UserDto> getAllReceiverUsers() {
        return userService.getUsersDto();
    }

    @GetMapping("/preset-defaults")
    public OrderPresetDefaultsDto getOrderPresetDefaultsForCurrentUser() {
        return orderPresetDefaultsService.getDefaultsForCurrentUser();
    }

    @PostMapping(value = "/destinations", consumes = {MediaType.TEXT_PLAIN_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public void addNewDestination(@RequestBody String value) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "value is required");
        }
        orderService.addNewDestination(value);
    }

    @PostMapping(value = "/receivers", consumes = {MediaType.TEXT_PLAIN_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public void addNewReceiver(@RequestBody String value) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "value is required");
        }
        orderService.addNewReceiver(value);
    }

    @GetMapping("/orders/original")
    public List<Order> getOriginalOrders() {
        return orderService.getOriginalOrderList();
    }

    @DeleteMapping("/orders/{orderNumber}")
    public void removeOrder(@PathVariable String orderNumber) {
        orderService.removeOrder(orderNumber);
    }

    @PutMapping("/requests/{requestId}/status")
    @Transactional
    public RequestOrder updateRequestStatusOrder(@PathVariable String requestId,
                                                 @RequestParam RequestOrderStatusEnum status) {
        RequestOrder requestOrder = getRequestOrderById(requestId);
        requestOrder.setRequestStatus(status);
        return requestOrderService.update(requestOrder);
    }

    @GetMapping("/requests/{requestId}")
    public RequestOrder getRequestOrderById(@PathVariable String requestId) {
        RequestOrder requestOrder = requestOrderService.getRequestById(requestId);
        if (requestOrder == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Request order not found: " + requestId);
        }
        return requestOrder;
    }

    @PostMapping("/orders/{orderNumber}/send-message")
    public void sentMessage(@PathVariable String orderNumber) {
        Order order = orderService.getByOrderNumber(orderNumber);
        if (order == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found: " + orderNumber);
        }
        mailService.sendMailToCoordinator(order);
    }

    private void validateOrder(Order order) {
        if (order == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "order is required");
        }
        if (order.getOrderNumber() == null || order.getOrderNumber().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "orderNumber is required");
        }
    }
}
