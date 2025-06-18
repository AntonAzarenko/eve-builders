package com.azarenka.evebuilders.service.api.integration;

public interface IEveMailIntegrationService {

    void sendMail(String accessToken, String characterId, String recipientId, String subject, String body);
}
