package com.azarenka.evebuilders.service.impl.intergarion;

import com.azarenka.evebuilders.domain.dto.LocationInfo;
import com.fasterxml.jackson.databind.JsonNode;

import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

@Service
public class LocationIntegrationService extends EveAbstractIntegrationConnection {

    private static final String SYSTEM_URL = "/universe/systems/{id}/";
    private static final String STATION_URL = "/universe/stations/{id}/";
    private static final String STRUCTURE_URL = "/universe/structures/{id}/"; // Requires auth

    public LocationInfo resolveLocation(Long locationId, String accessToken) {
        if (locationId == null) {
            return null;
        }

        if (locationId < 1_000_000) {
            return resolveSystem(locationId);
        } else if (locationId < 1_000_000_000) {
            return resolveStation(locationId);
        } else {
            return resolveStructure(locationId, accessToken);
        }
    }

    private LocationInfo resolveSystem(Long id) {
        return webClient.get()
            .uri(SYSTEM_URL, id)
            .retrieve()
            .bodyToMono(JsonNode.class)
            .map(json -> new LocationInfo(id, json.get("name").asText(), "SYSTEM"))
            .onErrorResume(e -> Mono.just(new LocationInfo(id, "Unknown System", "SYSTEM")))
            .block();
    }

    private LocationInfo resolveStation(Long id) {
        return webClient.get()
            .uri(STATION_URL, id)
            .retrieve()
            .bodyToMono(JsonNode.class)
            .map(json -> new LocationInfo(id, json.get("name").asText(), "STATION"))
            .onErrorResume(e -> Mono.just(new LocationInfo(id, "Unknown Station", "STATION")))
            .block();
    }

    private LocationInfo resolveStructure(Long id, String token) {
        return webClient.get()
            .uri(STRUCTURE_URL, id)
            .header("Authorization", "Bearer " + token)
            .retrieve()
            .bodyToMono(JsonNode.class)
            .map(json -> new LocationInfo(id, json.get("name").asText(), "STRUCTURE"))
            .onErrorResume(e -> Mono.just(new LocationInfo(id, "Unknown Structure", "STRUCTURE")))
            .block();
    }
}

