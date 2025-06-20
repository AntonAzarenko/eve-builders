package com.azarenka.evebuilders.service.impl.auth;

import com.azarenka.evebuilders.service.api.IAuthIntegrationService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class AuthIntegrationService implements IAuthIntegrationService {

    private final WebClient webClient;

    @Value("${timrod.api.url}")
    private String apiUrl;
    @Value("${timrod.api.token}")
    private String token;
    @Value("${timrod.api.group-id}")
    private String groupId;

    public AuthIntegrationService(WebClient.Builder builder) {
        this.webClient = builder.build();
    }

    @Override
    public Boolean checkUser(String characterName) {
        return webClient.post()
                .uri(apiUrl)
                .body(BodyInserters.fromFormData("character_name", characterName)
                        .with("group_id", groupId)
                        .with("token", token))
                .retrieve()
                .bodyToMono(Response.class)
                .map(Response::isResult)
                .onErrorReturn(false)
                .block();
    }

    private static class Response {
        private boolean result;

        public boolean isResult() {
            return result;
        }

        public void setResult(boolean result) {
            this.result = result;
        }
    }
}
