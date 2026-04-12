package com.azarenka.evebuilders.service.api.integration;

public interface IDiscordIntegrationService {

    void sendToOrderChannel(String messageText);

    void sendToRequestChannel(String messageText);
}

