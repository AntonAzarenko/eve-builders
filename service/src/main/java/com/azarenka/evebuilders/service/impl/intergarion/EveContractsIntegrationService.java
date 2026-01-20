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
        var json = webClient.get()
            .uri(uriBuilder -> uriBuilder
                .path(corpContractsUrl)
                .build(Map.of("corporation_id", corporationId)))
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .onStatus(
                HttpStatusCode::isError,
                response -> {
                    LOGGER.error("Error response while getting corporation contracts CorporationId=[{}], status={}",
                        corporationId, response.statusCode());
                    return response.createException();
                }
            )
            .bodyToMono(String.class)
            .block();
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
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
}
