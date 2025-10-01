package com.azarenka.evebuilders.service.impl.intergarion;

import com.azarenka.evebuilders.domain.db.Alliance;
import com.azarenka.evebuilders.service.api.IEveAllianceService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;

@Service
public class EveAllianceService extends EveAbstractIntegrationConnection implements IEveAllianceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EveAllianceService.class);

    @Value("${eve.alliance.info}")
    private String allianceInfoUrl;

    @Override
    public Alliance getAlliance(String allianceId) {
        LOGGER.info("Get Alliance info for Alliance Id=[{}]", allianceId);
        if(allianceId == null || allianceId.isEmpty()) {
            return new Alliance();
        }
        return webClient.get()
            .uri(uriBuilder -> uriBuilder
                .path(allianceInfoUrl)
                .build(allianceId))
            .retrieve()
            .onStatus(
                HttpStatusCode::isError,
                response -> {
                    LOGGER.error("Error response while fetching Alliance Id=[{}], status={}",
                        allianceId, response.statusCode());
                    return response.createException();
                }
            )
            .bodyToMono(Alliance.class)
            .block();
    }
}
