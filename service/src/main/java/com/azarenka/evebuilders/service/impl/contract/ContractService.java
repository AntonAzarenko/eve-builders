package com.azarenka.evebuilders.service.impl.contract;

import com.azarenka.evebuilders.domain.db.DistributedOrder;
import com.azarenka.evebuilders.domain.db.User;
import com.azarenka.evebuilders.domain.dto.Contract;
import com.azarenka.evebuilders.service.api.IContractService;
import com.azarenka.evebuilders.service.api.IOrderService;
import com.azarenka.evebuilders.service.api.IUserService;
import com.azarenka.evebuilders.service.impl.auth.eve.SecurityUtils;
import com.azarenka.evebuilders.service.impl.intergarion.EveContractsIntegrationService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

@Service
public class ContractService implements IContractService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContractService.class);

    @Autowired
    private EveContractsIntegrationService contractsClient;
    @Autowired
    private ContractValidationService contractValidationService;
    @Autowired
    private IOrderService orderService;
    @Autowired
    private IUserService userService;

    //TODO reimplement logic to remove hardcoded value and use database
    @Value("${app.eve.corporation.id}")
    private Long corporationId;

    public List<ContractValidationReport> getContractReport(DistributedOrder distributedOrder) {
        List<ContractValidationReport> reportList = new ArrayList<>();
        var userToken = userService.getUserToken();
        Optional<User> optionalUser = userService.getByUsername(distributedOrder.getUserName());
        if (optionalUser.isPresent()) {
            LOGGER.info("Find contracts for: {}. Searcher={}, ContractFromUser={} ", corporationId,
                SecurityUtils.getUserName(), optionalUser.get().getUsername());
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
            LOGGER.info("Found {} contracts", contracts.size());
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
        LOGGER.info("Start filterContract: totalContracts={}, issuerId={}, noteContains={}",
            contracts.size(), issuerId, noteContains);
        List<Contract> byIssuer = contracts.stream()
            .filter(c -> c.getIssuerId() == issuerId)
            .toList();
        LOGGER.info("After issuerId filter: count={}", byIssuer.size());
        List<Contract> base = byIssuer.isEmpty() ? contracts : byIssuer;
        if (byIssuer.isEmpty()) {
            LOGGER.warn("No contracts found by issuerId={}, fallback to all corporation contracts", issuerId);
        }
        List<Contract> byStatus = base.stream()
            .filter(c -> c.getStatus() != null && c.getStatus().equalsIgnoreCase("outstanding"))
            .toList();
        LOGGER.info("After status=outstanding filter: count={}", byStatus.size());
        List<Contract> byTitle = byStatus.stream()
            .filter(c -> c.getTitle() != null && c.getTitle().contains(noteContains))
            .toList();
        LOGGER.info("After title contains note filter: count={}", byTitle.size());
        return byTitle;
    }

    private List<Contract> findContracts(User user, String userToken, String orderNumber) {
        var userId = Long.parseLong(user.getCharacterId());
        var corporationContracts = contractsClient.getCorporationContracts(userToken, corporationId);
        LOGGER.info("Find corporation's contract for corporationID={}. Searcher={}, Count={}", corporationId,
            SecurityUtils.getUserName(),
            corporationContracts.size());
        return filterContract(corporationContracts, userId, orderNumber);
    }

    public void setCorporationId(Long corporationId) {
        this.corporationId = corporationId;
    }
}
