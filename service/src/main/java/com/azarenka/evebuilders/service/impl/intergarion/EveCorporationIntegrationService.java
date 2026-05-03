package com.azarenka.evebuilders.service.impl.intergarion;

import com.azarenka.evebuilders.domain.db.Corporation;
import com.azarenka.evebuilders.service.api.IEveCorporationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Objects;

@Service
public class EveCorporationIntegrationService extends EveAbstractIntegrationConnection implements IEveCorporationService {

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

    @Override
    public Long findCorporationIdByName(String corporationName) {
        try {
            String targetName = corporationName == null ? "" : corporationName.trim();
            if (targetName.isEmpty()) {
                return null;
            }
            UniverseIdsResponse response = webClient.post()
                .uri(uriBuilder -> uriBuilder
                    .path("/latest/universe/ids/")
                    .queryParam("datasource", "tranquility")
                    .queryParam("language", "en")
                    .build())
                .bodyValue(List.of(targetName))
                .retrieve()
                .bodyToMono(UniverseIdsResponse.class)
                .block();
            if (response == null || response.getCorporations() == null || response.getCorporations().isEmpty()) {
                return null;
            }
            for (UniverseEntity corporation : response.getCorporations()) {
                if (corporation == null || corporation.getId() == null || corporation.getId() <= 0) {
                    continue;
                }
                if (Objects.equals(targetName, corporation.getName())) {
                    return corporation.getId();
                }
            }
            return response.getCorporations().get(0).getId();
        } catch (WebClientResponseException.NotFound e) {
            return null;
        }
    }

    private static class UniverseIdsResponse {
        private List<UniverseEntity> corporations;

        public List<UniverseEntity> getCorporations() {
            return corporations;
        }

        public void setCorporations(List<UniverseEntity> corporations) {
            this.corporations = corporations;
        }
    }

    private static class UniverseEntity {
        private Long id;
        private String name;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
