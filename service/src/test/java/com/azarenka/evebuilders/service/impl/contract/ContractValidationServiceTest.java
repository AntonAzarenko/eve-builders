package com.azarenka.evebuilders.service.impl.contract;

import com.azarenka.evebuilders.domain.db.*;
import com.azarenka.evebuilders.domain.dto.Contract;
import com.azarenka.evebuilders.domain.dto.ContractItem;
import com.azarenka.evebuilders.domain.sqllite.InvType;
import com.azarenka.evebuilders.service.api.IFitLoaderService;
import com.azarenka.evebuilders.service.api.IInvTypeService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContractValidationServiceTest {

    @Mock
    private IFitLoaderService fitLoaderService;

    @Mock
    private IInvTypeService invTypeService;

    @InjectMocks
    private ContractValidationService contractValidationService;

    private Order order;
    private DistributedOrder distributedOrder;
    private Contract contract;
    private InvType shipType;

    @BeforeEach
    void setUp() {
        order = new Order();
        order.setFitId("fit-1");
        order.setPrice(new BigDecimal("100.00"));
        distributedOrder = new DistributedOrder();
        distributedOrder.setOrderNumber("ORD-001");
        contract = new Contract();
        contract.setPrice(100.0);
        shipType = new InvType();
        shipType.setTypeID(1);
    }

    private String buildFitText(String shipName, Map<String,Integer> items) {
        StringBuilder sb = new StringBuilder()
            .append("[").append(shipName).append(", some fit]\n");
        items.forEach((name, qty) ->
            sb.append(qty == 1 ? name : name + " x" + qty).append("\n"));
        return sb.toString();
    }

    private void mockFitLoader(String fitId, String textFit) {
        Fit fit = new Fit();
        fit.setTextFit(textFit);
        when(fitLoaderService.getFitById(fitId)).thenReturn(fit);
    }

    @Test
    void validContract_AllGood() {
        String shipName = "Rifter";
        Map<String,Integer> fitItems = Map.of("ModuleA", 2);
        mockFitLoader("fit-1", buildFitText(shipName, fitItems));
        ContractItem shipItem = new ContractItem();
        shipItem.setIncluded(true);
        shipItem.setTypeId(1);
        shipItem.setQuantity(1);
        ContractItem moduleItem = new ContractItem();
        moduleItem.setIncluded(true);
        moduleItem.setTypeId(100);
        moduleItem.setQuantity(2);
        when(invTypeService.getInvTypeByModuleName(shipName)).thenReturn(shipType);
        when(invTypeService.getInvTypeByModuleName("ModuleA"))
            .thenReturn(new InvType() {{ setTypeID(100); }});
        ContractValidationReport report = new ContractValidationReport();
        contractValidationService.validateContract(
            contract, List.of(shipItem, moduleItem), order, distributedOrder, report);
        assertTrue(report.isValid());
        assertEquals(1, report.getCountItems());
        assertTrue(report.getValidateErrorMessages().isEmpty());
        verifyNoMoreInteractions(invTypeService, fitLoaderService);
    }

    @Test
    void missingShip() {
        String shipName = "Rifter";
        mockFitLoader("fit-1", buildFitText(shipName, Map.of("ModuleA", 1)));
        when(invTypeService.getInvTypeByModuleName(shipName)).thenReturn(shipType);
        ContractValidationReport report = new ContractValidationReport();
        contractValidationService.validateContract(
            contract, Collections.emptyList(), order, distributedOrder, report);
        assertFalse(report.isValid());
        assertTrue(report.getValidateErrorMessages()
            .stream().anyMatch(m -> m.contains("кораблей")));
    }

    @Test
    void wrongPrice() {
        String shipName = "Rifter";
        mockFitLoader("fit-1", buildFitText(shipName, Map.of("ModuleA", 1)));
        contract.setPrice(200.0); // специально завышаем
        ContractItem shipItem = new ContractItem();
        shipItem.setIncluded(true); shipItem.setTypeId(1); shipItem.setQuantity(1);
        ContractItem moduleItem = new ContractItem();
        moduleItem.setIncluded(true); moduleItem.setTypeId(100); moduleItem.setQuantity(1);
        when(invTypeService.getInvTypeByModuleName(shipName)).thenReturn(shipType);
        when(invTypeService.getInvTypeByModuleName("ModuleA"))
            .thenReturn(new InvType() {{ setTypeID(100); }});
        ContractValidationReport report = new ContractValidationReport();
        contractValidationService.validateContract(
            contract, List.of(shipItem, moduleItem), order, distributedOrder, report);
        assertFalse(report.isValid());
        assertTrue(report.getValidateErrorMessages()
            .stream().anyMatch(m -> m.contains("цена")));
    }

    @Test
    void missingModules() {
        String shipName = "Rifter";
        mockFitLoader("fit-1", buildFitText(shipName, Map.of("ModuleA", 2)));
        ContractItem shipItem = new ContractItem();
        shipItem.setIncluded(true); shipItem.setTypeId(1); shipItem.setQuantity(1);
        ContractItem moduleItem = new ContractItem();           // только 1, нужно 2
        moduleItem.setIncluded(true); moduleItem.setTypeId(100); moduleItem.setQuantity(1);
        when(invTypeService.getInvTypeByModuleName(shipName)).thenReturn(shipType);
        when(invTypeService.getInvTypeByModuleName("ModuleA"))
            .thenReturn(new InvType() {{ setTypeID(100); }});
        ContractValidationReport report = new ContractValidationReport();
        contractValidationService.validateContract(
            contract, List.of(shipItem, moduleItem), order, distributedOrder, report);
        assertFalse(report.isValid());
        assertTrue(report.getValidateErrorMessages()
            .stream().anyMatch(m -> m.contains("Недостаточно")));
    }

    @Test
    void invalidModuleName() {
        String shipName = "Rifter";
        mockFitLoader("fit-1", buildFitText(shipName, Map.of("UnknownModule", 1)));
        ContractItem shipItem = new ContractItem();
        shipItem.setIncluded(true); shipItem.setTypeId(1); shipItem.setQuantity(1);
        when(invTypeService.getInvTypeByModuleName(shipName)).thenReturn(shipType);
        when(invTypeService.getInvTypeByModuleName("UnknownModule"))
            .thenThrow(new RuntimeException("Not found"));
        ContractValidationReport report = new ContractValidationReport();
        contractValidationService.validateContract(
            contract, List.of(shipItem), order, distributedOrder, report);
        assertFalse(report.isValid());
        assertTrue(report.getValidateErrorMessages()
            .stream().anyMatch(m -> m.contains("Не удалось")));
    }

    @Test
    void shipCountGreaterThanModules() {
        String shipName = "Rifter";
        mockFitLoader("fit-1", buildFitText(shipName, Map.of("ModuleA", 1))); // 1 per ship
        ContractItem shipItem = new ContractItem();
        shipItem.setIncluded(true); shipItem.setTypeId(1); shipItem.setQuantity(4);
        ContractItem moduleItem = new ContractItem();   // только 3, нужно 4
        moduleItem.setIncluded(true); moduleItem.setTypeId(100); moduleItem.setQuantity(3);
        when(invTypeService.getInvTypeByModuleName(shipName)).thenReturn(shipType);
        when(invTypeService.getInvTypeByModuleName("ModuleA"))
            .thenReturn(new InvType() {{ setTypeID(100); }});
        ContractValidationReport report = new ContractValidationReport();
        contractValidationService.validateContract(
            contract, List.of(shipItem, moduleItem), order, distributedOrder, report);
        assertFalse(report.isValid());
        assertTrue(report.getValidateErrorMessages()
            .stream().anyMatch(m -> m.contains("Недостаточно")));
    }
}