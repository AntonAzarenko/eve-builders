package com.azarenka.evebuilders.service.impl.intergarion;

import com.azarenka.evebuilders.service.api.integration.IDiscordIntegrationService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Service
public class DiscordIntegrationService implements IDiscordIntegrationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiscordIntegrationService.class);

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Value("${app.discord.webhook.order_url:}")
    private String orderWebhookUrl;
    @Value("${app.discord.webhook.request_url:}")
    private String requestWebhookUrl;
    @Value("${app.discord.thread.order_id:}")
    private String orderThreadId;
    @Value("${app.discord.thread.request_id:}")
    private String requestThreadId;
    @Value("${app.discord.wait_response:false}")
    private boolean waitResponse;

    @Override
    public void sendToOrderChannel(String messageText) {
        sendRequest("order", orderWebhookUrl, orderThreadId, messageText);
    }

    @Override
    public void sendToRequestChannel(String messageText) {
        sendRequest("request", requestWebhookUrl, requestThreadId, messageText);
    }

    private void sendRequest(String channelName, String webhookUrl, String threadId, String messageText) {
        String normalizedWebhookUrl = normalize(webhookUrl);
        String normalizedThreadId = normalize(threadId);
        if (isBlank(normalizedWebhookUrl)) {
            LOGGER.debug("Discord webhook is not configured. Channel={}", channelName);
            return;
        }
        String payloadJson = getPayloadFormatJson(messageText);
        String endpoint = buildEndpoint(normalizedWebhookUrl, normalizedThreadId);
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(endpoint))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(payloadJson))
            .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                LOGGER.error(
                    "Discord notification failed. Channel={}, StatusCode={}, Body={}",
                    channelName,
                    response.statusCode(),
                    sanitizeResponse(response.body())
                );
            }
        } catch (IOException e) {
            throw new RuntimeException("Discord notification failed due to IO error", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Discord notification interrupted", e);
        }
    }

    private String buildEndpoint(String webhookUrl, String threadId) {
        StringBuilder endpoint = new StringBuilder(webhookUrl);
        boolean hasQuery = webhookUrl.contains("?");
        if (waitResponse) {
            endpoint.append(hasQuery ? "&" : "?").append("wait=true");
            hasQuery = true;
        }
        if (!isBlank(threadId)) {
            endpoint.append(hasQuery ? "&" : "?")
                .append("thread_id=")
                .append(URLEncoder.encode(threadId, StandardCharsets.UTF_8));
        }
        return endpoint.toString();
    }

    private String getPayloadFormatJson(String messageText) {
        return String.format("""
            {
              "content": "%s"
            }
            """, escapeJson(messageText));
    }

    private String escapeJson(String text) {
        if (text == null) {
            return "";
        }
        StringBuilder escaped = new StringBuilder(text.length() + 16);
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '\\' -> escaped.append("\\\\");
                case '"' -> escaped.append("\\\"");
                case '\n' -> escaped.append("\\n");
                case '\r' -> escaped.append("\\r");
                case '\t' -> escaped.append("\\t");
                case '\b' -> escaped.append("\\b");
                case '\f' -> escaped.append("\\f");
                default -> {
                    if (c < 0x20) {
                        escaped.append(String.format("\\u%04x", (int) c));
                    } else {
                        escaped.append(c);
                    }
                }
            }
        }
        return escaped.toString();
    }

    private String sanitizeResponse(String responseBody) {
        if (responseBody == null) {
            return "";
        }
        int maxLength = 512;
        return responseBody.length() <= maxLength ? responseBody : responseBody.substring(0, maxLength) + "...";
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }
}
