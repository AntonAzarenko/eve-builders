package com.azarenka.evebuilders.service.impl.intergarion;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;


@Service
public class EvePortraitService {

    @Value("${eve.character.portrait.url}")
    private String characterPortraitUrl;

    private final WebClient webClient;

    public EvePortraitService() {
        this.webClient = WebClient.create("https://images.evetech.net");
    }

    public byte[] getPortrait(Long characterId, int size) {
        String portraitUrl = getPortraitUrl(characterId, size);

        try {
            return webClient.get()
                    .uri(portraitUrl)
                    .retrieve()
                    .onStatus(
                            status -> !status.is2xxSuccessful(), // Перехват любых неудачных статусов (например, 4xx или 5xx)
                            clientResponse -> Mono.error(new RuntimeException(
                                    "Ошибка при запросе портрета: " + clientResponse.statusCode()
                            ))
                    )
                    .bodyToMono(byte[].class)
                    .block();
        } catch (WebClientResponseException e) {
            System.err.println("HTTP Error: " + e.getStatusCode());
            System.err.println("Response Body: " + e.getResponseBodyAsString());
            throw e;
        } catch (Exception e) {
            System.err.println("Unexpected Error: " + e.getMessage());
            throw e;
        }
    }

    private String getPortraitUrl(Long characterId, int size) {
        return String.format(characterPortraitUrl + "?size=%d", characterId, size);
    }
}
