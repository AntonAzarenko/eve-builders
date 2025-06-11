package com.azarenka.evebuilders.service.impl.intergarion;

import com.azarenka.evebuilders.domain.db.Alliance;
import com.azarenka.evebuilders.service.api.IEveAllianceService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EveAllianceService extends EveAbstractIntegrationConnection implements IEveAllianceService {

    @Value("${eve.alliance.info}")
    private String allianceInfoUrl;

    @Override
    public Alliance getAlliance(String allianceId) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(allianceInfoUrl)
                        .build(allianceId))
                .retrieve()
                .bodyToMono(Alliance.class)
                .block();
    }
}
