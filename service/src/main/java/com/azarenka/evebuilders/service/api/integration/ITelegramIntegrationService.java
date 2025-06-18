package com.azarenka.evebuilders.service.api.integration;

public interface ITelegramIntegrationService {

    void sendMessage(String messageText, String threadId);

    void sendInfoMessage(String text, String threadId);
}
