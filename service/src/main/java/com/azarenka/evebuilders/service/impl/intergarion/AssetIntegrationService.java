package com.azarenka.evebuilders.service.impl.intergarion;

import com.azarenka.evebuilders.domain.db.Asset;
import com.azarenka.evebuilders.domain.dto.EsiAssetsResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Service
public class AssetIntegrationService extends EveAbstractIntegrationConnection {

    Logger LOGGER = LoggerFactory.getLogger(AssetIntegrationService.class);

    @Value("${eve.character.assets.url}")
    private String apiUrl;

    public EsiAssetsResponse findAssetsWithEtag(String characterId, String userToken, String etag) {
        var response = webClient.get()
            .uri(uriBuilder -> uriBuilder
                .path(apiUrl)
                .queryParam("datasource", "tranquility")
                .queryParam("page", 1)
                .build(characterId))
            .header("Authorization", "Bearer " + userToken)
            .ifNoneMatch(etag != null ? etag : "")
            .exchange()
            .block();
        if (response == null) {
            throw new IllegalStateException("ESI did not return any response for character " + characterId);
        }
        var headers = response.headers().asHttpHeaders();
        var newEtag = headers.getETag();
        var expiresStr = headers.getFirst(HttpHeaders.EXPIRES);
        LocalDateTime expiresAt = null;
        if (expiresStr != null) {
            expiresAt = ZonedDateTime.parse(expiresStr, DateTimeFormatter.RFC_1123_DATE_TIME).toLocalDateTime();
        }
        if (response.statusCode() == HttpStatus.NOT_MODIFIED) {
            return new EsiAssetsResponse(true, newEtag, expiresAt, List.of());
        }
        var allAssets = response.bodyToFlux(Asset.class)
            .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(10)))
            .collectList()
            .block();
        int totalPages = getTotalPages(characterId, userToken);
        if (totalPages > 1) {
            for (int page = 2; page <= totalPages; page++) {
                int finalPage = page;
                List<Asset> assetsPage = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                        .path(apiUrl)
                        .queryParam("datasource", "tranquility")
                        .queryParam("page", finalPage)
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
        }

        return new EsiAssetsResponse(false, newEtag, expiresAt, allAssets);
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
