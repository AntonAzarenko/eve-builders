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

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public void sendMessage(String messageText, String threadId) {
        sendRequest(getPayloadFormatJson(escapeJson(messageText), true, threadId));
    }

    @Override
    public void sendInfoMessage(String text, String threadId) {
        sendRequest(getPayloadFormatJson(text, false, threadId));
    }

    private void sendRequest(String payloadJson) {
        String url = String.format("https://api.telegram.org/bot%s/sendMessage", token);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payloadJson))
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                System.out.println("Error: " + response.body());
            } else {
                System.out.println("Sent: " + response.body());
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private String escapeJson(String text) {
        return text
                .replace("\"", "\\\"")
                .replace("\n", "\\n");
    }

    private String getPayloadFormatJson(String messageText, boolean useMarkdownV2, String threadId) {
        return String.format("""
                        {
                          "chat_id": "%s",
                          "message_thread_id": %s,
                          "text": "%s"%s
                        }
                        """,
                chatId,
                threadId,
                messageText,
                useMarkdownV2 ? ",\n  \"parse_mode\": \"MarkdownV2\"" : ""
        );
    }
}
