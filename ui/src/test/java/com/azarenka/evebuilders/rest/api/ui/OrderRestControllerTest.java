package com.azarenka.evebuilders.rest.api.ui;

import com.azarenka.evebuilders.domain.db.Order;
import com.azarenka.evebuilders.domain.enums.OrderStatusEnum;
import com.azarenka.evebuilders.domain.dto.ShipOrderDto;
import com.azarenka.evebuilders.domain.sqllite.OrderRights;
import com.azarenka.evebuilders.service.api.IOrderService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrderRestControllerTest {

    private IOrderService orderService;
    private MockMvc mockMvc;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @BeforeEach
    void setUp() {
        orderService = mock(IOrderService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new OrderRestController(orderService)).build();
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

}
