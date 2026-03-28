package com.azarenka.evebuilders.service.impl.intergarion;

import com.azarenka.evebuilders.domain.dto.Contract;
import com.azarenka.evebuilders.domain.dto.ContractItem;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.HttpHeaders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class EveContractsIntegrationService extends EveAbstractIntegrationConnection {

    private static final Logger LOGGER = LoggerFactory.getLogger(EveContractsIntegrationService.class);
    private final ObjectMapper objectMapper;

    @Value("${eve.corporation.contracts.url}")
    private String corpContractsUrl;
    @Value("${eve.corporation.contracts.items.url}")
    private String corpContractsItemsUrl;
    @Value("${eve.character.contracts.url}")
    private String characterContractsItemsUrl;

    public EveContractsIntegrationService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<Contract> getCorporationContracts(String accessToken, long corporationId) {
        List<Contract> result = new ArrayList<>();

        PageResponse firstPageResponse = webClient.get()
            .uri(uriBuilder -> uriBuilder
                .path(corpContractsUrl)
                .queryParam("page", 1)
                .build(Map.of("corporation_id", corporationId)))
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .onStatus(
                HttpStatusCode::isError,
                response -> {
                    LOGGER.error("Error response while getting corporation contracts CorporationId=[{}], page=1, status={}",
                        corporationId, response.statusCode());
                    return response.createException();
                }
            )
            .toEntity(String.class)
            .map(entity -> {
                String xPages = entity.getHeaders().getFirst("X-Pages");
                int totalPages = xPages != null ? Integer.parseInt(xPages) : 1;
                return new PageResponse(totalPages, entity.getBody());
            })
            .block();

        if (firstPageResponse == null || firstPageResponse.body() == null) {
            LOGGER.warn("No response body for corporationId=[{}]", corporationId);
            return List.of();
        }

        result.addAll(readContracts(firstPageResponse.body()));
        int totalPages = firstPageResponse.totalPages();

        LOGGER.info("Loaded first page for corporationId=[{}], totalPages={}", corporationId, totalPages);

        for (int page = 2; page <= totalPages; page++) {
            int finalPage = page;
            String json = webClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path(corpContractsUrl)
                    .queryParam("page", finalPage)
                    .build(Map.of("corporation_id", corporationId)))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(
                    HttpStatusCode::isError,
                    response -> {
                        LOGGER.error("Error response while getting corporation contracts CorporationId=[{}], page={}, status={}",
                            corporationId, finalPage, response.statusCode());
                        return response.createException();
                    }
                )
                .bodyToMono(String.class)
                .block();

            if (json != null) {
                result.addAll(readContracts(json));
            }
        }

        LOGGER.info("Loaded corporation contracts for corporationId=[{}], totalContracts={}", corporationId, result.size());
        return result;
    }

    private List<Contract> readContracts(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<Contract>>() {
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse corporation contracts", e);
        }
    }

    public List<Contract> getCharacterContracts(String accessToken, long characterId) {
        var json = webClient.get()
            .uri(uriBuilder -> uriBuilder
                .path(characterContractsItemsUrl)
                .build(Map.of("character_id", characterId)))
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .onStatus(
                HttpStatusCode::isError,
                response -> {
                    LOGGER.error("Error response while getting user contracts CharacterId=[{}], status={}",
                        characterId, response.statusCode());
                    return response.createException();
                }
            )
            .bodyToMono(String.class)
            .block();
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse character contracts", e);
        }
    }

    public List<ContractItem> getContractItems(String accessToken, long corporationId, long contractId) {
        var json = webClient.get()
            .uri(uriBuilder -> uriBuilder
                .path(corpContractsItemsUrl)
                .build(Map.of(
                    "corporation_id", corporationId,
                    "contract_id", contractId)))
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .onStatus(
                HttpStatusCode::isError,
                response -> {
                    LOGGER.error(
                        "Error response while getting contract's items CorporationId=[{}], ContractId=[{}] status={}",
                        corporationId, contractId, response.statusCode());
                    return response.createException();
                }
            )
            .bodyToMono(String.class)
            .block();
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse contract items", e);
        }
    }

    private record PageResponse(int totalPages, String body) {
    }
}
