package com.azarenka.evebuilders.rest.api.ui;

import com.azarenka.evebuilders.domain.db.DistributedOrder;
import com.azarenka.evebuilders.domain.db.Fit;
import com.azarenka.evebuilders.domain.db.Order;
import com.azarenka.evebuilders.domain.db.OrderFilter;
import com.azarenka.evebuilders.domain.dto.OrderDistributionRequest;
import com.azarenka.evebuilders.domain.enums.OrderStatusEnum;
import com.azarenka.evebuilders.domain.sqllite.OrderRights;
import com.azarenka.evebuilders.service.api.IDistributedOrderService;
import com.azarenka.evebuilders.service.api.IFitLoaderService;
import com.azarenka.evebuilders.service.api.IOrderService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DistributedOrderRestControllerTest {

    private IOrderService orderService;
    private IDistributedOrderService distributedOrderService;
    private IFitLoaderService fitLoaderService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        orderService = mock(IOrderService.class);
        distributedOrderService = mock(IDistributedOrderService.class);
        fitLoaderService = mock(IFitLoaderService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new DistributedOrderRestController(
                orderService,
                distributedOrderService,
                fitLoaderService))
            .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getDistributedOrdersReturnsSelectedOrderBreakdown() throws Exception {
        LocalDate today = LocalDate.now();
        DistributedOrder distributedOrder = distributedOrder(
            "dist-1",
            "N2026061802",
            "Megathron",
            "pilot",
            3,
            2
        );

        when(orderService.getByOrderNumber("N2026061802")).thenReturn(order(
            "id-1",
            "N2026061802",
            "Megathron",
            8,
            3,
            2,
            today.minusDays(4).toString(),
            today.plusDays(7).toString()
        ));
        when(distributedOrderService.getOrdersByOrderNumber("N2026061802")).thenReturn(List.of(distributedOrder));

        mockMvc.perform(get("/api/distributed-orders/N2026061802"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].orderNumber").value("N2026061802"))
            .andExpect(jsonPath("$[0].shipName").value("Megathron"))
            .andExpect(jsonPath("$[0].userName").value("pilot"))
            .andExpect(jsonPath("$[0].count").value(3))
            .andExpect(jsonPath("$[0].countReady").value(2))
            .andExpect(jsonPath("$[0].orderRights").value("CORPORATION"))
            .andExpect(jsonPath("$[0].orderStatus").value("IN_PROGRESS"));
    }

    @Test
    void distributeOrdersSavesSingleDistributedOrderUsingCurrentUser() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(
                "pilot",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
            )
        );

        when(distributedOrderService.save("N2026061802", 2, "pilot")).thenReturn(distributedOrder(
            "dist-1",
            "N2026061802",
            "Megathron",
            "pilot",
            2,
            0
        ));

        mockMvc.perform(post("/api/distributed-orders")
                .contentType("application/json")
                .content("""
                    {"order_number":"N2026061802","count":2}
                    """))
            .andExpect(status().isOk());

        verify(distributedOrderService).save("N2026061802", 2, "pilot");
    }

    @Test
    void getOrdersReturnsCurrentUsersDistributedOrders() throws Exception {
        OrderFilter filter = new OrderFilter();
        filter.setStatuses(List.of(OrderStatusEnum.IN_PROGRESS));
        filter.setOrderTypes(List.of("MARKET"));
        filter.setMinFreeCount(2);
        filter.setDistributed(true);

        DistributedOrder distributedOrder = distributedOrder(
            "dist-1",
            "N2026061802",
            "Megathron",
            "pilot",
            4,
            1
        );
        when(distributedOrderService.getAllByUserName(filter)).thenReturn(List.of(distributedOrder));

        mockMvc.perform(get("/api/distributed-orders")
                .param("statuses", "IN_PROGRESS")
                .param("orderTypes", "MARKET")
                .param("minFreeCount", "2")
                .param("distributed", "true"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].orderNumber").value("N2026061802"))
            .andExpect(jsonPath("$[0].shipName").value("Megathron"))
            .andExpect(jsonPath("$[0].userName").value("pilot"))
            .andExpect(jsonPath("$[0].count").value(4))
            .andExpect(jsonPath("$[0].countReady").value(1));

        verify(distributedOrderService).getAllByUserName(filter);
    }

    @Test
    void getOrdersFiltersBySearchAndSortsByCreatedDate() throws Exception {
        LocalDate today = LocalDate.now();
        DistributedOrder older = distributedOrder(
            "dist-1",
            "N2026061701",
            "Rifter",
            "pilot",
            4,
            1
        );
        older.setCreatedDate(today.minusDays(5));
        older.setOrderStatus(OrderStatusEnum.IN_PROGRESS);

        DistributedOrder newer = distributedOrder(
            "dist-2",
            "N2026061802",
            "Megathron",
            "pilot",
            4,
            1
        );
        newer.setCreatedDate(today.minusDays(3));
        newer.setOrderStatus(OrderStatusEnum.IN_PROGRESS);

        OrderFilter filter = new OrderFilter();
        when(distributedOrderService.getAllByUserName(filter)).thenReturn(List.of(older, newer));

        mockMvc.perform(get("/api/distributed-orders").param("search", "N20260618"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].orderNumber").value("N2026061802"))
            .andExpect(jsonPath("$[0].shipName").value("Megathron"))
            .andExpect(jsonPath("$[1]").doesNotExist());
    }

    @Test
    void saveOrderUpdatesReadyCountForDistributedOrder() throws Exception {
        DistributedOrder distributedOrder = distributedOrder(
            "dist-1",
            "N2026061802",
            "Megathron",
            "pilot",
            4,
            1
        );
        when(distributedOrderService.getById("dist-1")).thenReturn(distributedOrder);

        mockMvc.perform(put("/api/distributed-orders/dist-1/ready-count")
                .param("value", "2"))
            .andExpect(status().isOk());

        verify(distributedOrderService).update(distributedOrder, 2);
    }

    @Test
    void discardOrderRemovesDistributedOrder() throws Exception {
        DistributedOrder distributedOrder = distributedOrder(
            "dist-1",
            "N2026061802",
            "Megathron",
            "pilot",
            4,
            1
        );
        when(distributedOrderService.getById("dist-1")).thenReturn(distributedOrder);

        mockMvc.perform(delete("/api/distributed-orders/dist-1"))
            .andExpect(status().isOk());

        verify(distributedOrderService).discardOrder(distributedOrder);
    }

    @Test
    void sendOrderForApprovalReturnsServiceResult() throws Exception {
        DistributedOrder distributedOrder = distributedOrder(
            "dist-1",
            "N2026061802",
            "Megathron",
            "pilot",
            4,
            1
        );
        when(distributedOrderService.getById("dist-1")).thenReturn(distributedOrder);
        when(distributedOrderService.sendOrderForApproval(distributedOrder, OrderStatusEnum.WAITING_FOR_APPROVAL))
            .thenReturn(true);

        mockMvc.perform(post("/api/distributed-orders/dist-1/approval"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").value(true));

        verify(distributedOrderService).sendOrderForApproval(distributedOrder, OrderStatusEnum.WAITING_FOR_APPROVAL);
    }

    @Test
    void getDestinationAndReceiverReturnStrings() throws Exception {
        when(distributedOrderService.getDestination("N2026061802")).thenReturn("Jita");
        when(distributedOrderService.getReceiver("N2026061802")).thenReturn("Receiver");

        mockMvc.perform(get("/api/distributed-orders/N2026061802/destination"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").value("Jita"));

        mockMvc.perform(get("/api/distributed-orders/N2026061802/receiver"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").value("Receiver"));
    }

    @Test
    void getFitReturnsSelectedFit() throws Exception {
        Fit fit = new Fit();
        fit.setId("fit-1");
        fit.setName("Megathron fit");
        when(fitLoaderService.getFitById("fit-1")).thenReturn(fit);

        mockMvc.perform(get("/api/distributed-orders/fits/fit-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("fit-1"));
    }

    private Order order(String id,
                        String orderNumber,
                        String shipName,
                        Integer count,
                        Integer inProgressCount,
                        Integer countReady,
                        String createdDate,
                        String finishDate) {
        Order order = new Order();
        order.setId(id);
        order.setOrderNumber(orderNumber);
        order.setShipName(shipName);
        order.setCount(count);
        order.setInProgressCount(inProgressCount);
        order.setCountReady(countReady);
        order.setPrice(BigDecimal.valueOf(123.45));
        order.setOrderType("MARKET");
        order.setDestination("Jita");
        order.setReceiver("Receiver");
        order.setPriority("High");
        order.setBluePrint(true);
        order.setOrderStatus(inProgressCount != null && inProgressCount.equals(count)
            ? OrderStatusEnum.DISTRIBUTED : OrderStatusEnum.IN_PROGRESS);
        order.setCreatedBy("pilot");
        order.setCreatedDate(LocalDate.parse(createdDate));
        order.setUpdatedBy("pilot");
        order.setUpdatedDate(LocalDate.parse("2026-06-18"));
        order.setFitId("fit-1");
        order.setOrderRights(OrderRights.CORPORATION);
        order.setRightsholder("holder");
        order.setCategory("Battleship");
        order.setFinishBy(LocalDate.parse(finishDate));
        return order;
    }

    private DistributedOrder distributedOrder(String id,
                                              String orderNumber,
                                              String shipName,
                                              String userName,
                                              Integer count,
                                              Integer countReady) {
        DistributedOrder distributedOrder = new DistributedOrder();
        distributedOrder.setId(id);
        distributedOrder.setOrderNumber(orderNumber);
        distributedOrder.setShipName(shipName);
        distributedOrder.setUserName(userName);
        distributedOrder.setCount(count);
        distributedOrder.setCountReady(countReady);
        distributedOrder.setFitId("fit-1");
        distributedOrder.setOrderRights(OrderRights.CORPORATION);
        distributedOrder.setOrderStatus(OrderStatusEnum.IN_PROGRESS);
        distributedOrder.setCreatedDate(LocalDate.parse("2026-06-11"));
        distributedOrder.setAppliedDate(LocalDate.parse("2026-06-18"));
        distributedOrder.setFinishedDate(null);
        distributedOrder.setCategory("Battleship");
        distributedOrder.setPrice(BigDecimal.valueOf(123.45));
        distributedOrder.setAssembly(true);
        return distributedOrder;
    }
}
