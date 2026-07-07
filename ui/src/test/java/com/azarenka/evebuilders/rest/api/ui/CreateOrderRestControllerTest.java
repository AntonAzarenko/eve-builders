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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CreateOrderRestControllerTest {

    private IEveMaterialDataService dataService;
    private IOrderService orderService;
    private IRequestOrderService requestOrderService;
    private IEveMailService mailService;
    private ICorporationService corporationService;
    private IUserService userService;
    private IOrderPresetDefaultsService orderPresetDefaultsService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        dataService = mock(IEveMaterialDataService.class);
        orderService = mock(IOrderService.class);
        requestOrderService = mock(IRequestOrderService.class);
        mailService = mock(IEveMailService.class);
        corporationService = mock(ICorporationService.class);
        userService = mock(IUserService.class);
        orderPresetDefaultsService = mock(IOrderPresetDefaultsService.class);

        mockMvc = MockMvcBuilders
            .standaloneSetup(new CreateOrderRestController(
                dataService,
                orderService,
                requestOrderService,
                mailService,
                corporationService,
                userService,
                orderPresetDefaultsService
            ))
            .build();
    }

    @Test
    void getInvGroupsByIdReturnsGroups() throws Exception {
        InvGroup group = new InvGroup();
        group.setGroupID(25);
        group.setGroupName("Ships");
        when(dataService.getInvGroupsById(25)).thenReturn(List.of(group));

        mockMvc.perform(get("/api/create-order/groups/25"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].groupID").value(25))
            .andExpect(jsonPath("$[0].groupName").value("Ships"));
    }

    @Test
    void getTypesByGroupIdsReturnsTypes() throws Exception {
        InvType type = invType(34, "Rifter", 25);
        when(dataService.getTypesByGroupIds(List.of(25, 26))).thenReturn(List.of(type));

        mockMvc.perform(get("/api/create-order/types")
                .param("groupIds", "25", "26"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].typeID").value(34))
            .andExpect(jsonPath("$[0].typeName").value("Rifter"))
            .andExpect(jsonPath("$[0].groupID").value(25));
    }

    @Test
    void getTypesByGroupIdReturnsTypes() throws Exception {
        InvType type = invType(34, "Rifter", 25);
        when(dataService.getTypesByGroupId(25)).thenReturn(List.of(type));

        mockMvc.perform(get("/api/create-order/types/25"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].typeID").value(34))
            .andExpect(jsonPath("$[0].typeName").value("Rifter"))
            .andExpect(jsonPath("$[0].groupID").value(25));
    }

    @Test
    void createOrderSavesWhenOrderDoesNotExist() throws Exception {
        Order order = order("N2026061801");
        when(orderService.getByOrderNumber("N2026061801")).thenReturn(null);
        when(orderService.saveOrder(any())).thenReturn(order);

        mockMvc.perform(post("/api/create-order/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"orderNumber":"N2026061801"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.orderNumber").value("N2026061801"));

        verify(orderService).saveOrder(any());
    }

    @Test
    void createOrderUpdatesWhenOrderExists() throws Exception {
        Order order = order("N2026061801");
        when(orderService.getByOrderNumber("N2026061801")).thenReturn(order);
        when(orderService.updateOrder(any(Order.class))).thenReturn(order);

        mockMvc.perform(post("/api/create-order/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"orderNumber":"N2026061801"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.orderNumber").value("N2026061801"));

        verify(orderService).updateOrder(any(Order.class));
    }

    @Test
    void getAllDestinationReturnsDestinations() throws Exception {
        Destination destination = destination("Jita");
        when(orderService.getAllDestination()).thenReturn(List.of(destination));

        mockMvc.perform(get("/api/create-order/destinations"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].destination").value("Jita"));
    }

    @Test
    void getAllReceiversReturnsReceivers() throws Exception {
        Receiver receiver = receiver("Receiver 1");
        when(orderService.getAllReceivers()).thenReturn(List.of(receiver));

        mockMvc.perform(get("/api/create-order/receivers"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].receiver").value("Receiver 1"));
    }

    @Test
    void getAllManagedCorporationsReturnsCorporations() throws Exception {
        ManagedCorporation corporation = managedCorporation("corp-1", 123456789L, "Managed Corp");
        when(corporationService.getAllCorporations()).thenReturn(List.of(corporation));

        mockMvc.perform(get("/api/create-order/managed-corporations"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value("corp-1"))
            .andExpect(jsonPath("$[0].eveCorporationId").value(123456789))
            .andExpect(jsonPath("$[0].corporationName").value("Managed Corp"));
    }

    @Test
    void getAllReceiverUsersReturnsUsers() throws Exception {
        UserDto user = new UserDto("pilot", "123456", java.util.Set.of());
        when(userService.getUsersDto()).thenReturn(List.of(user));

        mockMvc.perform(get("/api/create-order/receiver-users"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].username").value("pilot"))
            .andExpect(jsonPath("$[0].characterId").value("123456"));
    }

    @Test
    void getOrderPresetDefaultsForCurrentUserReturnsDefaults() throws Exception {
        OrderPresetDefaultsDto defaults = new OrderPresetDefaultsDto();
        defaults.setReceiverName("Managed Corp");
        defaults.setReceiverRefId("123456789");
        defaults.setReceiverMissing(false);
        when(orderPresetDefaultsService.getDefaultsForCurrentUser()).thenReturn(defaults);

        mockMvc.perform(get("/api/create-order/preset-defaults"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.receiverName").value("Managed Corp"))
            .andExpect(jsonPath("$.receiverRefId").value("123456789"))
            .andExpect(jsonPath("$.receiverMissing").value(false));
    }

    @Test
    void addNewDestinationDelegatesToService() throws Exception {
        mockMvc.perform(post("/api/create-order/destinations")
                .contentType(MediaType.TEXT_PLAIN)
                .content("Jita"))
            .andExpect(status().isOk());

        verify(orderService).addNewDestination("Jita");
    }

    @Test
    void addNewReceiverDelegatesToService() throws Exception {
        mockMvc.perform(post("/api/create-order/receivers")
                .contentType(MediaType.TEXT_PLAIN)
                .content("Receiver 1"))
            .andExpect(status().isOk());

        verify(orderService).addNewReceiver("Receiver 1");
    }

    @Test
    void getOriginalOrdersReturnsOrders() throws Exception {
        Order order = order("N2026061801");
        when(orderService.getOriginalOrderList()).thenReturn(List.of(order));

        mockMvc.perform(get("/api/create-order/orders/original"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].orderNumber").value("N2026061801"));
    }

    @Test
    void removeOrderDelegatesToService() throws Exception {
        mockMvc.perform(delete("/api/create-order/orders/N2026061801"))
            .andExpect(status().isOk());

        verify(orderService).removeOrder("N2026061801");
    }

    @Test
    void getRequestOrderByIdReturnsRequestOrder() throws Exception {
        RequestOrder requestOrder = requestOrder("req-1");
        when(requestOrderService.getRequestById("req-1")).thenReturn(requestOrder);

        mockMvc.perform(get("/api/create-order/requests/req-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("req-1"))
            .andExpect(jsonPath("$.requestStatus").value("CREATED"));
    }

    @Test
    void updateRequestStatusOrderUpdatesStatus() throws Exception {
        RequestOrder requestOrder = requestOrder("req-1");
        when(requestOrderService.getRequestById("req-1")).thenReturn(requestOrder);
        when(requestOrderService.update(any())).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(put("/api/create-order/requests/req-1/status")
                .param("status", "IN_PROGRESS"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("req-1"))
            .andExpect(jsonPath("$.requestStatus").value("IN_PROGRESS"));

        verify(requestOrderService).update(any());
    }

    @Test
    void sentMessageDelegatesToMailService() throws Exception {
        Order order = order("N2026061801");
        when(orderService.getByOrderNumber("N2026061801")).thenReturn(order);

        mockMvc.perform(post("/api/create-order/orders/N2026061801/send-message"))
            .andExpect(status().isOk());

        verify(mailService).sendMailToCoordinator(order);
    }

    private InvType invType(Integer typeId, String typeName, Integer groupId) {
        InvType invType = new InvType();
        invType.setTypeID(typeId);
        invType.setTypeName(typeName);
        invType.setGroupId(groupId);
        return invType;
    }

    private Order order(String orderNumber) {
        Order order = new Order();
        order.setOrderNumber(orderNumber);
        order.setPrice(BigDecimal.valueOf(100));
        return order;
    }

    private Destination destination(String value) {
        Destination destination = new Destination();
        destination.setDestId("dest-1");
        destination.setDestination(value);
        return destination;
    }

    private Receiver receiver(String value) {
        Receiver receiver = new Receiver();
        receiver.setResId("res-1");
        receiver.setReceiver(value);
        return receiver;
    }

    private ManagedCorporation managedCorporation(String id, Long eveCorporationId, String corporationName) {
        ManagedCorporation corporation = new ManagedCorporation();
        corporation.setId(id);
        corporation.setEveCorporationId(eveCorporationId);
        corporation.setCorporationName(corporationName);
        return corporation;
    }

    private RequestOrder requestOrder(String id) {
        RequestOrder requestOrder = new RequestOrder();
        requestOrder.setId(id);
        requestOrder.setRequestStatus(RequestOrderStatusEnum.CREATED);
        requestOrder.setPrice(BigDecimal.valueOf(150));
        return requestOrder;
    }
}
