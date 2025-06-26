package com.azarenka.evebuilders.service.impl.corporation;

import com.azarenka.evebuilders.domain.dto.Contract;
import com.azarenka.evebuilders.domain.dto.ContractItem;
import com.azarenka.evebuilders.service.api.IUserService;
import com.azarenka.evebuilders.service.impl.intergarion.EveContractsIntegrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ContractValidationService {

    @Autowired
    private EveContractsIntegrationService contractsClient;
    @Autowired
    private IUserService userService;

    public Optional<Contract> findContract(long corporationId, long issuerId, String noteContains) {
        var userToken = userService.getUserToken();
        return contractsClient.getCorporationContracts(userToken, corporationId).stream()
                .filter(c -> c.getIssuerId() == issuerId)
                .filter(c -> c.getTitle() != null && c.getTitle().contains(noteContains))
                .findFirst();
    }

    public List<ContractItem> getContractItems(long corporationId, long contractId) {
        var userToken = userService.getUserToken();
        return contractsClient.getContractItems(userToken, corporationId, contractId);
    }
}
