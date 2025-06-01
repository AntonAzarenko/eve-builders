package com.azarenka.evebuilders.service.impl.intergarion;

import com.azarenka.evebuilders.service.api.ITelegramIntegrationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class TelegramIntegrationService implements ITelegramIntegrationService {

    @Value("${app.telegram_bot.token}")
    private String token;
    @Value("${app.telegram_chat_id}")
    private String chatId;
    private String threadId = "2";

    @Override
    public void sendMessage(String messageText) {
        String url = String.format("https://api.telegram.org/bot%s/sendMessage", token);
        String payloadJson = String.format("""
                {
                  "chat_id": "%s",
                  "message_thread_id": %s,
                  "text": "%s",
                  "parse_mode": "MarkdownV2"
                }
                """, chatId, threadId, escapeJson(messageText));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payloadJson))
                .build();

        HttpResponse<String> response = null;
        try {
            response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (response.statusCode() != 200) {
            System.out.println("Error: " + response.body());
        } else {
            System.out.println("Sent: " + response.body());
        }
    }

    private String escapeJson(String text) {
        return text
                .replace("\"", "\\\"")
                .replace("\n", "\\n");
    }
}
