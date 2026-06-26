package com.azarenka.evebuilders.rest.api.ui;

import com.azarenka.evebuilders.domain.db.Destination;
import com.azarenka.evebuilders.domain.db.Fit;
import com.azarenka.evebuilders.domain.db.Order;
import com.azarenka.evebuilders.domain.db.Receiver;
import com.azarenka.evebuilders.domain.db.RequestOrder;
import com.azarenka.evebuilders.domain.db.RequestOrderStatusEnum;
import com.azarenka.evebuilders.domain.sqllite.InvGroup;
import com.azarenka.evebuilders.domain.sqllite.InvType;
import com.azarenka.evebuilders.service.api.IEveMailService;
import com.azarenka.evebuilders.service.api.IEveMaterialDataService;
import com.azarenka.evebuilders.service.api.IFitLoaderService;
import com.azarenka.evebuilders.service.api.IOrderService;
import com.azarenka.evebuilders.service.api.IRequestOrderService;
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
    private IFitLoaderService fitLoaderService;
    private IOrderService orderService;
    private IRequestOrderService requestOrderService;
    private IEveMailService mailService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        dataService = mock(IEveMaterialDataService.class);
        fitLoaderService = mock(IFitLoaderService.class);
        orderService = mock(IOrderService.class);
        requestOrderService = mock(IRequestOrderService.class);
        mailService = mock(IEveMailService.class);

        mockMvc = MockMvcBuilders
            .standaloneSetup(new CreateOrderRestController(
                dataService,
                fitLoaderService,
                orderService,
                requestOrderService,
                mailService
            ))
            .build();
    }

    @Test
    void gitAllFitsReturnsFits() throws Exception {
        Fit fit = fit("fit-1", "Rifter fit", "text-fit");
        when(fitLoaderService.getAll()).thenReturn(List.of(fit));

        mockMvc.perform(get("/api/create-order/fits"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value("fit-1"))
            .andExpect(jsonPath("$[0].name").value("Rifter fit"))
            .andExpect(jsonPath("$[0].textFit").value("text-fit"));
    }

    @Test
    void getFitByIdReturnsFit() throws Exception {
        Fit fit = fit("fit-1", "Rifter fit", "text-fit");
        when(fitLoaderService.getFitById("fit-1")).thenReturn(fit);

        mockMvc.perform(get("/api/create-order/fits/fit-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("fit-1"))
            .andExpect(jsonPath("$.name").value("Rifter fit"))
            .andExpect(jsonPath("$.textFit").value("text-fit"));
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
    void uploadFitReturnsResult() throws Exception {
        when(fitLoaderService.upload("fit text")).thenReturn(true);

        mockMvc.perform(post("/api/create-order/fits/upload")
                .contentType(MediaType.TEXT_PLAIN)
                .content("fit text"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").value(true));

        verify(fitLoaderService).upload("fit text");
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

    private Fit fit(String id, String name, String textFit) {
        Fit fit = new Fit();
        fit.setId(id);
        fit.setName(name);
        fit.setTextFit(textFit);
        return fit;
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

    private RequestOrder requestOrder(String id) {
        RequestOrder requestOrder = new RequestOrder();
        requestOrder.setId(id);
        requestOrder.setRequestStatus(RequestOrderStatusEnum.CREATED);
        requestOrder.setPrice(BigDecimal.valueOf(150));
        return requestOrder;
    }
}
