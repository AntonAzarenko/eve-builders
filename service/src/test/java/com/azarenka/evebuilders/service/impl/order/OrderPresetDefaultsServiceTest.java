package com.azarenka.evebuilders.service.impl.order;

import com.azarenka.evebuilders.domain.db.BlueprintOption;
import com.azarenka.evebuilders.domain.db.ManagedCorporation;
import com.azarenka.evebuilders.domain.db.OrderPresetDefaults;
import com.azarenka.evebuilders.domain.db.OrderPresetDefaultsHistory;
import com.azarenka.evebuilders.domain.db.OrderType;
import com.azarenka.evebuilders.domain.db.PriorityOption;
import com.azarenka.evebuilders.domain.dto.OrderPresetDefaultsDto;
import com.azarenka.evebuilders.domain.dto.UserDto;
import com.azarenka.evebuilders.domain.enums.ReceiverTargetType;
import com.azarenka.evebuilders.domain.sqllite.OrderRights;
import com.azarenka.evebuilders.repository.database.properties.IOrderPresetDefaultsHistoryRepository;
import com.azarenka.evebuilders.repository.database.properties.IOrderPresetDefaultsRepository;
import com.azarenka.evebuilders.service.api.ICorporationService;
import com.azarenka.evebuilders.service.api.IUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderPresetDefaultsServiceTest {

    @Mock
    private IOrderPresetDefaultsRepository presetRepository;
    @Mock
    private IOrderPresetDefaultsHistoryRepository historyRepository;
    @Mock
    private ICorporationService corporationService;
    @Mock
    private IUserService userService;

    private OrderPresetDefaultsService service;

    @BeforeEach
    void setUp() {
        service = new OrderPresetDefaultsService(presetRepository, historyRepository, corporationService, userService);
    }

    @Test
    void getDefaultsFallbackUsesScanStakanWhenExists() {
        ManagedCorporation corp = new ManagedCorporation();
        corp.setCorporationName("Scan Stakan");
        corp.setEveCorporationId(98771596L);

        when(presetRepository.findByOwnerUsername("SYSTEM")).thenReturn(Optional.empty());
        when(corporationService.getAllCorporations()).thenReturn(List.of(corp));

        OrderPresetDefaultsDto dto = service.getDefaultsForCurrentUser();

        assertEquals(OrderType.REDEMPTION, dto.getOrderType());
        assertEquals(ReceiverTargetType.CORPORATION, dto.getReceiverType());
        assertEquals("98771596", dto.getReceiverRefId());
        assertEquals("Scan Stakan", dto.getReceiverName());
        assertEquals(PriorityOption.MEDIUM, dto.getPriority());
        assertEquals(BlueprintOption.NO, dto.getBlueprint());
        assertEquals(OrderRights.GROUP, dto.getOrderRights());
        assertFalse(dto.isReceiverMissing());
    }

    @Test
    void getDefaultsFallbackMarksMissingWhenCorporationNotFound() {
        when(presetRepository.findByOwnerUsername("SYSTEM")).thenReturn(Optional.empty());
        when(corporationService.getAllCorporations()).thenReturn(List.of());

        OrderPresetDefaultsDto dto = service.getDefaultsForCurrentUser();

        assertTrue(dto.isReceiverMissing());
        assertEquals("", dto.getReceiverRefId());
        assertEquals("", dto.getReceiverName());
    }

    @Test
    void saveDefaultsPersistsPresetAndHistory() {
        OrderPresetDefaultsDto dto = new OrderPresetDefaultsDto();
        dto.setOrderType(OrderType.REDEMPTION);
        dto.setReceiverType(ReceiverTargetType.CORPORATION);
        dto.setReceiverRefId("98771596");
        dto.setReceiverName("Scan Stakan");
        dto.setPriority(PriorityOption.MEDIUM);
        dto.setBlueprint(BlueprintOption.NO);
        dto.setOrderRights(OrderRights.GROUP);
        dto.setRightsholder("GROUP");

        ManagedCorporation corp = new ManagedCorporation();
        corp.setCorporationName("Scan Stakan");
        corp.setEveCorporationId(98771596L);

        when(corporationService.getAllCorporations()).thenReturn(List.of(corp));
        when(presetRepository.findByOwnerUsername("SYSTEM")).thenReturn(Optional.empty());
        when(presetRepository.save(any(OrderPresetDefaults.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(historyRepository.save(any(OrderPresetDefaultsHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderPresetDefaultsDto saved = service.saveDefaultsForCurrentUser(dto);

        assertEquals(OrderType.REDEMPTION, saved.getOrderType());
        assertEquals(ReceiverTargetType.CORPORATION, saved.getReceiverType());
        assertEquals("98771596", saved.getReceiverRefId());
        assertFalse(saved.isReceiverMissing());

        ArgumentCaptor<OrderPresetDefaultsHistory> captor = ArgumentCaptor.forClass(OrderPresetDefaultsHistory.class);
        verify(historyRepository).save(captor.capture());
        assertEquals("SYSTEM", captor.getValue().getOwnerUsername());
    }

    @Test
    void saveDefaultsThrowsOnInvalidReceiver() {
        OrderPresetDefaultsDto dto = new OrderPresetDefaultsDto();
        dto.setOrderType(OrderType.REDEMPTION);
        dto.setReceiverType(ReceiverTargetType.USER);
        dto.setReceiverRefId("1");
        dto.setReceiverName("Missing");
        dto.setPriority(PriorityOption.MEDIUM);
        dto.setBlueprint(BlueprintOption.NO);
        dto.setOrderRights(OrderRights.GROUP);
        dto.setRightsholder("GROUP");

        when(userService.getUsersDto()).thenReturn(List.of(new UserDto("another", "2", null)));

        assertThrows(IllegalArgumentException.class, () -> service.saveDefaultsForCurrentUser(dto));
    }
}
