package com.azarenka.evebuilders.service.impl.contract;

import com.azarenka.evebuilders.domain.db.DistributedOrder;
import com.azarenka.evebuilders.domain.db.Order;
import com.azarenka.evebuilders.domain.db.User;
import com.azarenka.evebuilders.domain.dto.Contract;
import com.azarenka.evebuilders.domain.dto.ContractItem;
import com.azarenka.evebuilders.domain.enums.ReceiverTargetType;
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
import org.springframework.web.reactive.function.client.WebClientException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    // Legacy fallback for orders created before receiver routing fields were introduced.
    @Value("${app.eve.corporation.id}")
    private Long legacyCorporationId;

    public List<ContractValidationReport> getContractReport(DistributedOrder distributedOrder) {
        List<ContractValidationReport> reportList = new ArrayList<>();
        var userToken = userService.getUserToken();
        Optional<User> optionalUser = userService.getByUsername(distributedOrder.getUserName());
        if (optionalUser.isPresent()) {
            var originalOrder = orderService.getByOrderNumber(distributedOrder.getOrderNumber());
            var routing = resolveReceiverRouting(originalOrder);
            LOGGER.info("Find contracts for targetType={}, targetId={}. Searcher={}, ContractFromUser={} ",
                routing.targetType, routing.targetId,
                SecurityUtils.getUserName(), optionalUser.get().getUsername());
            var contracts = findContracts(optionalUser.get(), userToken, distributedOrder.getOrderNumber(), routing);
            if (!contracts.isEmpty()) {
                contracts.forEach(contract -> {
                    var report = new ContractValidationReport();
                    var order = originalOrder;
                    var contractItems = getContractItemsForRouting(optionalUser.get(), userToken, routing,
                        contract.getContractId());
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
            return !filterUserContract(characterContracts, Long.parseLong(optionalUser.get().getCharacterId()),
                distributedOrder.getOrderNumber()).isEmpty();
        }
        return false;
    }

    private List<Contract> filterUserContract
        (List<Contract> contracts, long issuerId, String noteContains) {
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

    private List<Contract> filterContract(
        List<Contract> contracts, long issuerId, String noteContains, ReceiverRouting routing) {
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
        List<Contract> byTitleOrFallback = byTitle;
        if (byTitle.isEmpty() && routing.targetType == ReceiverTargetType.USER) {
            LOGGER.warn(
                "No contracts found by title for USER routing. Fallback to issuer+status+receiver filtering only. targetId={}",
                routing.targetId);
            byTitleOrFallback = byStatus;
        }

        List<Contract> byReceiver = byTitleOrFallback.stream()
            .filter(c -> c.getAssigneeId() == routing.targetId || c.getAcceptorId() == routing.targetId)
            .toList();
        LOGGER.info("After receiver filter targetType={}, targetId={}: count={}",
            routing.targetType, routing.targetId, byReceiver.size());
        return byReceiver;
    }

    private List<Contract> findContracts(User user, String userToken, String orderNumber, ReceiverRouting routing) {
        try {
            var userId = Long.parseLong(user.getCharacterId());
            if (routing.targetType == ReceiverTargetType.CORPORATION) {
                var corporationContracts = contractsClient.getCorporationContracts(userToken, routing.targetId);
                LOGGER.info("Find corporation contracts for corporationID={}. Searcher={}, Count={}", routing.targetId,
                    SecurityUtils.getUserName(),
                    corporationContracts.size());
                return filterContract(corporationContracts, userId, orderNumber, routing);
            }
            var characterContracts = contractsClient.getCharacterContracts(userToken, userId);
            LOGGER.info("Find character contracts for characterID={}. Searcher={}, Count={}", userId,
                SecurityUtils.getUserName(), characterContracts.size());
            return filterContract(characterContracts, userId, orderNumber, routing);
        } catch (WebClientException e) {
            LOGGER.error("ESI request failed while searching contracts. orderNumber={}, targetType={}, targetId={}",
                orderNumber, routing.targetType, routing.targetId, e);
            return List.of();
        }
    }

    private List<ContractItem> getContractItemsForRouting(
        User user, String userToken, ReceiverRouting routing, long contractId) {
        if (routing.targetType == ReceiverTargetType.CORPORATION) {
            return contractsClient.getContractItems(userToken, routing.targetId, contractId);
        }
        return contractsClient.getCharacterContractItems(userToken, Long.parseLong(user.getCharacterId()), contractId);
    }

    private ReceiverRouting resolveReceiverRouting(Order order) {
        if (order != null
            && order.getReceiverType() != null
            && order.getReceiverRefId() != null
            && !order.getReceiverRefId().isBlank()
            && !"0".equals(order.getReceiverRefId())) {
            try {
                return new ReceiverRouting(order.getReceiverType(), Long.parseLong(order.getReceiverRefId()));
            } catch (NumberFormatException e) {
                LOGGER.warn("Invalid receiver_ref_id for order {}: {}. Use legacy fallback.",
                    order.getOrderNumber(), order.getReceiverRefId());
            }
        }
        LOGGER.warn("Legacy receiver routing fallback is used. Order={}", order == null ? "null" : order.getOrderNumber());
        return new ReceiverRouting(ReceiverTargetType.CORPORATION, legacyCorporationId);
    }

    private record ReceiverRouting(ReceiverTargetType targetType, long targetId) {
    }
}
