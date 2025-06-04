package com.azarenka.evebuilders.service.api;

public interface ITelegramIntegrationService {

    void sendMessage(String messageText);

    void sendInfoMessage(String text);
}
