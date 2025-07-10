package com.azarenka.evebuilders.service.impl.contract;

import com.azarenka.evebuilders.domain.db.DistributedOrder;
import com.azarenka.evebuilders.domain.db.User;
import com.azarenka.evebuilders.domain.dto.Contract;
import com.azarenka.evebuilders.service.api.IContractService;
import com.azarenka.evebuilders.service.api.IOrderService;
import com.azarenka.evebuilders.service.api.IUserService;
import com.azarenka.evebuilders.service.impl.intergarion.EveContractsIntegrationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ContractService implements IContractService {

    @Autowired
    private EveContractsIntegrationService contractsClient;
    @Autowired
    private ContractValidationService contractValidationService;
    @Autowired
    private IOrderService orderService;
    @Autowired
    private IUserService userService;

    //TODO reimplement logic to remove hardcoded value and use database
    private final Long corporationId = 98771596L;

    public List<ContractValidationReport> getContractReport(DistributedOrder distributedOrder) {
        List<ContractValidationReport> reportList = new ArrayList<>();
        var userToken = userService.getUserToken();
        Optional<User> optionalUser = userService.getByUsername(distributedOrder.getUserName());
        if (optionalUser.isPresent()) {
            var contracts = findContracts(optionalUser.get(), userToken, distributedOrder.getOrderNumber());
            if (!contracts.isEmpty()) {
                contracts.forEach(contract -> {
                    var report = new ContractValidationReport();
                    var order = orderService.getByOrderNumber(distributedOrder.getOrderNumber());
                    var contractItems =
                        contractsClient.getContractItems(userToken, corporationId, contract.getContractId());
                    contractValidationService.validateContract(contract, contractItems, order, distributedOrder,
                        report);
                    report.setContract(contract);
                    reportList.add(report);
                });
            } else {
                var report = new ContractValidationReport();
                report.setValid(false);
                report.setErrorMessage("No contract found for order number " + distributedOrder.getOrderNumber());
                reportList.add(report);
            }
        } else {
            var report = new ContractValidationReport();
            report.setValid(false);
            report.setErrorMessage("User not found order number " + distributedOrder.getOrderNumber());
            reportList.add(report);
        }
        return reportList;
    }

    @Override
    public boolean isContractExists(DistributedOrder distributedOrder) {
        var userToken = userService.getUserToken();
        Optional<User> optionalUser = userService.getByUsername(distributedOrder.getUserName());
        if (optionalUser.isPresent()) {
            List<Contract> characterContracts =
                contractsClient.getCharacterContracts(userToken, Long.parseLong(optionalUser.get().getCharacterId()));
            return !filterContract(characterContracts, Long.parseLong(optionalUser.get().getCharacterId()),
                distributedOrder.getOrderNumber()).isEmpty();
        }
        return false;
    }

    private List<Contract> filterContract(List<Contract> contracts, long issuerId, String noteContains) {
        return contracts.stream()
            .filter(contract -> contract.getIssuerId() == issuerId)
            .filter(contract -> Objects.nonNull(contract.getTitle()) && contract.getTitle().contains(noteContains))
            .filter(
                contract -> Objects.nonNull(contract.getStatus()) && contract.getStatus()
                    .equalsIgnoreCase("outstanding"))
            .toList();
    }

    private List<Contract> findContracts(User user, String userToken, String orderNumber) {
        var userId = Long.parseLong(user.getCharacterId());
        var corporationContracts = contractsClient.getCorporationContracts(userToken, corporationId);
        return filterContract(corporationContracts, userId, orderNumber);
    }
}
