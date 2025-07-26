package com.azarenka.evebuilders.service.impl.intergarion;

import com.azarenka.evebuilders.domain.db.Asset;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import reactor.util.retry.Retry;

@Service
public class AssetIntegrationService extends EveAbstractIntegrationConnection {

    @Value("${eve.character.assets.url}")
    private String apiUrl;

    public List<Asset> findAssets(String characterId, String userToken) {
        int totalPages = getTotalPages(characterId, userToken);
        var allAssets = new ArrayList<Asset>();
        for (int page = 1; page <= totalPages; page++) {
            int finalPage = page;
            var assetsPage = webClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path(apiUrl)
                    .queryParam("datasource", "tranquility")
                    .queryParam("page", finalPage) // Укажите страницу, если необходимо
                    .build(characterId))
                .header("Authorization", "Bearer " + userToken)
                .retrieve()
                .bodyToFlux(Asset.class)
                .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(10)))
                .collectList()
                .block();
            if (assetsPage != null) {
                allAssets.addAll(assetsPage);
            }
        }
        return allAssets;
    }

    private int getTotalPages(String characterId, String accessToken) {
        var responseSpec = webClient.get()
            .uri(apiUrl + "?datasource=tranquility&page=1", characterId)
            .header("Authorization", "Bearer " + accessToken)
            .retrieve();
        return Objects.requireNonNull(responseSpec.toBodilessEntity()
                .block())
            .getHeaders()
            .getFirst("X-Pages") != null
            ? Integer.parseInt(responseSpec.toBodilessEntity().block().getHeaders().getFirst("X-Pages"))
            : 1;
    }
}
