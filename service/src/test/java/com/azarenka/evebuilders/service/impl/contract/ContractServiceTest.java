package com.azarenka.evebuilders.service.impl.contract;

import com.azarenka.evebuilders.domain.db.DistributedOrder;
import com.azarenka.evebuilders.domain.db.Order;
import com.azarenka.evebuilders.domain.db.User;
import com.azarenka.evebuilders.domain.dto.Contract;
import com.azarenka.evebuilders.domain.dto.ContractItem;
import com.azarenka.evebuilders.service.api.IOrderService;
import com.azarenka.evebuilders.service.api.IUserService;
import com.azarenka.evebuilders.service.impl.intergarion.EveContractsIntegrationService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContractServiceTest {

    @Mock
    private EveContractsIntegrationService contractsClient;
    @Mock
    private ContractValidationService contractValidationService;
    @Mock
    private IOrderService orderService;
    @Mock
    private IUserService userService;
    @InjectMocks
    private ContractService contractService;

    private DistributedOrder distributedOrder;
    private User user;

    @BeforeEach
    void setup() {
        distributedOrder = new DistributedOrder();
        distributedOrder.setUserName("testUser");
        distributedOrder.setOrderNumber("ORD-001");
        user = new User();
        user.setUsername("testUser");
        user.setCharacterId("123456");
    }

    @Test
    void testGetContractReportUserNotFoundReturnsErrorReport() {
        when(userService.getUserToken()).thenReturn("token");
        when(userService.getByUsername("testUser")).thenReturn(Optional.empty());
        List<ContractValidationReport> reports = contractService.getContractReport(distributedOrder);
        assertEquals(1, reports.size());
        ContractValidationReport report = reports.get(0);
        assertFalse(report.isValid());
        assertEquals(1, report.getValidateErrorMessages().size());
        assertEquals("User not found order number ORD-001", report.getValidateErrorMessages().get(0));

        verify(userService).getByUsername("testUser");
        verifyNoMoreInteractions(userService, contractsClient, contractValidationService, orderService);
    }

    @Test
    void testGetContractReportNoContractsFoundReturnsErrorReport() {
        contractService.setCorporationId(98771596L);
        when(userService.getByUsername("testUser")).thenReturn(Optional.of(user));
        when(userService.getUserToken()).thenReturn("token");
        when(contractsClient.getCorporationContracts("token", 98771596L)).thenReturn(Collections.emptyList());

        List<ContractValidationReport> reports = contractService.getContractReport(distributedOrder);

        assertEquals(1, reports.size());
        ContractValidationReport report = reports.get(0);

        assertFalse(report.isValid());
        assertEquals(1, report.getValidateErrorMessages().size());
        assertEquals("No contract found for order number ORD-001", report.getValidateErrorMessages().get(0));

        verify(userService).getByUsername("testUser");
        verify(userService).getUserToken();
        verify(contractsClient).getCorporationContracts("token", 98771596L);
        verifyNoMoreInteractions(userService, contractsClient, contractValidationService, orderService);
    }

    @Test
    void testGetContractReportContractsExistValidationCalled() {
        contractService.setCorporationId(98771596L);
        Contract contract = new Contract();
        contract.setContractId(999L);
        contract.setIssuerId(123456L);
        contract.setTitle("ORD-001");
        contract.setStatus("outstanding");
        Order order = new Order();
        List<ContractItem> contractItems = List.of(new ContractItem());
        when(userService.getByUsername("testUser")).thenReturn(Optional.of(user));
        when(userService.getUserToken()).thenReturn("token");
        when(contractsClient.getCorporationContracts("token", 98771596L)).thenReturn(List.of(contract));
        when(orderService.getByOrderNumber("ORD-001")).thenReturn(order);
        when(contractsClient.getContractItems("token", 98771596L, 999L)).thenReturn(contractItems);
        List<ContractValidationReport> reports = contractService.getContractReport(distributedOrder);
        assertEquals(1, reports.size());
        ContractValidationReport report = reports.get(0);
        assertSame(contract, report.getContract());
        verify(userService).getByUsername("testUser");
        verify(userService).getUserToken();
        verify(contractsClient).getCorporationContracts("token", 98771596L);
        verify(orderService).getByOrderNumber("ORD-001");
        verify(contractsClient).getContractItems("token", 98771596L, 999L);
        verify(contractValidationService).validateContract(contract, contractItems, order, distributedOrder, report);
        verifyNoMoreInteractions(userService, contractsClient, contractValidationService, orderService);
    }

    @Test
    void testIsContractExistsUserNotFoundReturnsFalse() {
        when(userService.getByUsername("testUser")).thenReturn(Optional.empty());
        when(userService.getUserToken()).thenReturn("token");
        boolean result = contractService.isContractExists(distributedOrder);
        assertFalse(result);
        verify(userService).getByUsername("testUser");
        verify(userService).getUserToken();
        verifyNoMoreInteractions(userService, contractsClient, contractValidationService, orderService);
    }

    @Test
    void testIsContractExistsContractFoundReturnsTrue() {
        Contract contract = new Contract();
        contract.setIssuerId(123456L);
        contract.setTitle("ORD-001");
        contract.setStatus("outstanding");
        when(userService.getByUsername("testUser")).thenReturn(Optional.of(user));
        when(userService.getUserToken()).thenReturn("token");
        when(contractsClient.getCharacterContracts("token", 123456L)).thenReturn(List.of(contract));
        boolean result = contractService.isContractExists(distributedOrder);
        assertTrue(result);
        verify(userService).getByUsername("testUser");
        verify(userService).getUserToken();
        verify(contractsClient).getCharacterContracts("token", 123456L);
        verifyNoMoreInteractions(userService, contractsClient, contractValidationService, orderService);
    }

    @Test
    void testIsContractExistsContractNotMatchingReturnsFalse() {
        Contract contract = new Contract();
        contract.setIssuerId(123456L);
        contract.setTitle("OTHER");
        contract.setStatus("outstanding");
    }
}
