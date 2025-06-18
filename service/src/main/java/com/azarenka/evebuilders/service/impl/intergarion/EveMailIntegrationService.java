package com.azarenka.evebuilders.service.impl.intergarion;

import com.azarenka.evebuilders.service.api.integration.IEveMailIntegrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class EveMailIntegrationService extends EveAbstractIntegrationConnection implements IEveMailIntegrationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EveMailIntegrationService.class);

    @Value("${eve.mail.send.url}")
    private String mailSendUrl;

    public void sendMail(String accessToken, String characterId, String recipientId, String subject, String body) {
        Map<String, Object> requestBody = Map.of("subject", subject, "body", body, "recipients", List.of(Map.of(
                "recipient_id", recipientId,
                "recipient_type", "character"
        )));
        webClient.post()
                .uri(uriBuilder -> uriBuilder.path(mailSendUrl).build(Map.of("characterId", characterId)))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .toBodilessEntity()
                .doOnSuccess(resp -> LOGGER.info("Mail sent from={} to={}", characterId, recipientId))
                .doOnError(err -> LOGGER.error("Failed to send mail: " + err.getMessage()))
                .then().subscribe();
    }
}
