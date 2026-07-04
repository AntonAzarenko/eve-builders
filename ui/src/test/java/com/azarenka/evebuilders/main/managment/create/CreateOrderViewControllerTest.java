package com.azarenka.evebuilders.main.managment.create;

import com.azarenka.evebuilders.domain.db.Order;
import com.azarenka.evebuilders.domain.db.RequestOrder;
import com.azarenka.evebuilders.domain.db.RequestOrderStatusEnum;
import com.azarenka.evebuilders.service.api.IOrderService;
import com.azarenka.evebuilders.service.api.IRequestOrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateOrderViewControllerTest {

    @Mock
    private IOrderService orderService;
    @Mock
    private IRequestOrderService requestOrderService;

    private CreateOrderViewController controller;

    @BeforeEach
    void setUp() {
        controller = new CreateOrderViewController();
        ReflectionTestUtils.setField(controller, "orderService", orderService);
        ReflectionTestUtils.setField(controller, "requestOrderService", requestOrderService);
    }

    @Test
    void createOrderWhenOrderExistsCallsUpdate() {
        Order input = new Order();
        input.setOrderNumber("ORD-100");

        Order existing = new Order();
        existing.setOrderNumber("ORD-100");

        Order updated = new Order();
        updated.setOrderNumber("ORD-100");

        when(orderService.getByOrderNumber("ORD-100")).thenReturn(existing);
        when(orderService.updateOrder(input)).thenReturn(updated);

        Order result = controller.createOrder(input);

        assertSame(updated, result);
        verify(orderService).getByOrderNumber("ORD-100");
        verify(orderService).updateOrder(input);
        verifyNoMoreInteractions(orderService);
    }

    @Test
    void createOrderWhenOrderDoesNotExistCallsSave() {
        Order input = new Order();
        input.setOrderNumber("ORD-200");

        Order saved = new Order();
        saved.setOrderNumber("ORD-200");

        when(orderService.getByOrderNumber("ORD-200")).thenReturn(null);
        when(orderService.saveOrder(input)).thenReturn(saved);

        Order result = controller.createOrder(input);

        assertSame(saved, result);
        verify(orderService).getByOrderNumber("ORD-200");
        verify(orderService).saveOrder(input);
        verifyNoMoreInteractions(orderService);
    }

    @Test
    void updateRequestStatusOrderUpdatesStatusAndPersists() {
        RequestOrder requestOrder = new RequestOrder();

        controller.updateRequestStatusOrder(requestOrder, RequestOrderStatusEnum.IN_PROGRESS);

        verify(requestOrderService).update(requestOrder);
        verifyNoMoreInteractions(requestOrderService);
    }
}

