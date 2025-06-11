package com.azarenka.evebuilders.service.impl.intergarion;

import com.azarenka.evebuilders.domain.db.Corporation;
import com.azarenka.evebuilders.service.api.IEveCorporationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Service
public class EveCorporationService extends EveAbstractIntegrationConnection implements IEveCorporationService {

    @Value("${eve.corporation.info}")
    private String corpInfoUrl;

    @Override
    public Corporation getCorporation(String corpId) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(corpInfoUrl)
                        .build(corpId))
                .retrieve()
                .bodyToMono(Corporation.class)
                .block();
    }
}
