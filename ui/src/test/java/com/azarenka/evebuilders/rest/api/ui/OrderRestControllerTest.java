package com.azarenka.evebuilders.rest.api.ui;

import com.azarenka.evebuilders.domain.db.DistributedOrder;
import com.azarenka.evebuilders.domain.db.Order;
import com.azarenka.evebuilders.domain.enums.OrderStatusEnum;
import com.azarenka.evebuilders.domain.dto.ShipOrderDto;
import com.azarenka.evebuilders.domain.sqllite.OrderRights;
import com.azarenka.evebuilders.service.api.IDistributedOrderService;
import com.azarenka.evebuilders.service.api.IOrderService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrderRestControllerTest {

    private IOrderService orderService;
    private IDistributedOrderService distributedOrderService;
    private MockMvc mockMvc;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @BeforeEach
    void setUp() {
        orderService = mock(IOrderService.class);
        distributedOrderService = mock(IDistributedOrderService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new OrderRestController(orderService, distributedOrderService)).build();
    }

    @Test
    void getOrdersReturnsSortedAndFilteredList() throws Exception {
        LocalDate today = LocalDate.now();
        Order older = order(
            "id-older",
            "N2026061701",
            "Rifter",
            10,
            2,
            1,
            today.minusDays(5).toString(),
            today.plusDays(1).toString()
        );
        Order newer = order(
            "id-newer",
            "N2026061801",
            "Hecate",
            5,
            5,
            5,
            today.minusDays(3).toString(),
            today.plusDays(2).toString()
        );

        when(orderService.getOrderList(any())).thenReturn(List.of(new ShipOrderDto(older), new ShipOrderDto(newer)));

        mockMvc.perform(get("/api/orders").param("search", "N20260618"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].orderNumber").value("N2026061801"))
            .andExpect(jsonPath("$[0].itemName").value("Hecate"))
            .andExpect(jsonPath("$[0].freeCount").value(0))
            .andExpect(jsonPath("$[0].countReady").value(5))
            .andExpect(jsonPath("$[0].orderStatus").value("DISTRIBUTED"))
            .andExpect(jsonPath("$[0].distributionStatus").value("FULL"))
            .andExpect(jsonPath("$[0].daysToFinish").value(2))
            .andExpect(jsonPath("$[0].progressPercent").value(100));
    }

    @Test
    void getOrderReturnsSelectedOrderSnapshot() throws Exception {
        LocalDate today = LocalDate.now();
        Order order = order(
            "id-1",
            "N2026061802",
            "Megathron",
            8,
            3,
            2,
            today.minusDays(4).toString(),
            today.plusDays(7).toString()
        );

        when(orderService.getByOrderNumber("N2026061802")).thenReturn(order);

        mockMvc.perform(get("/api/orders/N2026061802"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.orderNumber").value("N2026061802"))
            .andExpect(jsonPath("$.itemName").value("Megathron"))
            .andExpect(jsonPath("$.freeCount").value(5))
            .andExpect(jsonPath("$.countReady").value(2))
            .andExpect(jsonPath("$.orderStatus").value("IN_PROGRESS"))
            .andExpect(jsonPath("$.distributionStatus").value("PARTIAL"))
            .andExpect(jsonPath("$.daysToFinish").value(7))
            .andExpect(jsonPath("$.progressPercent").value(25));
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

        mockMvc.perform(get("/api/orders/N2026061802/distributed-orders"))
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
    void distributeOrdersSavesBatchUsingCurrentUser() throws Exception {
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
        when(distributedOrderService.save("N2026061803", 3, "pilot")).thenReturn(distributedOrder(
            "dist-2",
            "N2026061803",
            "Hecate",
            "pilot",
            3,
            0
        ));

        mockMvc.perform(post("/api/orders/distributed-orders")
                .contentType("application/json")
                .content("""
                    {"order_number":"N2026061802","count":2}
                    """))
            .andExpect(status().isOk());

        verify(distributedOrderService).save("N2026061802", 2, "pilot");
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
