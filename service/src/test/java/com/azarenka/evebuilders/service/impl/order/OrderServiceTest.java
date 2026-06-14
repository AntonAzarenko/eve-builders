package com.azarenka.evebuilders.service.impl.order;

import com.azarenka.evebuilders.domain.db.Order;
import com.azarenka.evebuilders.repository.database.IOrderRepository;
import com.azarenka.evebuilders.repository.database.properties.IDestinationRepository;
import com.azarenka.evebuilders.repository.database.properties.IReceiverRepository;
import com.azarenka.evebuilders.service.api.IAuditService;
import com.azarenka.evebuilders.service.api.integration.INotificationService;
import com.azarenka.evebuilders.service.impl.auth.eve.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private IDestinationRepository destinationRepository;
    @Mock
    private IReceiverRepository receiverRepository;
    @Mock
    private IOrderRepository orderRepository;
    @Mock
    private IAuditService auditService;
    @Mock
    private INotificationService notificationService;

    private OrderService service;

    @BeforeEach
    void setUp() {
        service = new OrderService();
        ReflectionTestUtils.setField(service, "destinationRepository", destinationRepository);
        ReflectionTestUtils.setField(service, "receiverRepository", receiverRepository);
        ReflectionTestUtils.setField(service, "orderRepository", orderRepository);
        ReflectionTestUtils.setField(service, "auditService", auditService);
        ReflectionTestUtils.setField(service, "notificationService", notificationService);
    }

    @Test
    void saveOrderAlwaysUsesFreshCreatedDateForRepeatedOrders() {
        try (MockedStatic<SecurityUtils> utilities = mockStatic(SecurityUtils.class)) {
            utilities.when(SecurityUtils::getUserName).thenReturn("builder-user");
            when(orderRepository.findTodayOrdersCount(LocalDate.now())).thenReturn(0);
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Order order = new Order();
            order.setShipName("Rifter");
            order.setCreatedDate(LocalDate.of(2024, 1, 15));

            Order saved = service.saveOrder(order);

            ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
            verify(orderRepository).save(captor.capture());
            Order persisted = captor.getValue();

            assertNotNull(saved.getId());
            assertEquals(LocalDate.now(), persisted.getCreatedDate());
            assertEquals("builder-user", persisted.getCreatedBy());
        }
    }
}
