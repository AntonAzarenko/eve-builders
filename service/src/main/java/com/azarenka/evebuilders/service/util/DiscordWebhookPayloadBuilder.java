package com.azarenka.evebuilders.service.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class DiscordWebhookPayloadBuilder {

    private static final String EVERYONE_MENTION = "@everyone";
    private static final int CONTENT_LIMIT = 2000;
    private static final int TITLE_LIMIT = 256;
    private static final int DESCRIPTION_LIMIT = 4096;
    private static final int FIELD_NAME_LIMIT = 256;
    private static final int FIELD_VALUE_LIMIT = 1024;
    private static final int MAX_FIELDS = 25;
    private static final int EMBED_COLOR = 0x2B6CB0;

    private DiscordWebhookPayloadBuilder() {
    }

    public static Map<String, Object> buildCardPayload(String messageText) {
        List<String> lines = splitLines(messageText);
        ParsedCard parsedCard = parse(lines);

        Map<String, Object> embed = new LinkedHashMap<>();
        embed.put("title", trim(parsedCard.title, TITLE_LIMIT));
        embed.put("color", EMBED_COLOR);
        if (!parsedCard.description.isBlank()) {
            embed.put("description", trim(parsedCard.description, DESCRIPTION_LIMIT));
        }
        if (!parsedCard.fields.isEmpty()) {
            embed.put("fields", parsedCard.fields);
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("content", buildContent(parsedCard.title));
        payload.put("allowed_mentions", Map.of("parse", List.of("everyone")));
        payload.put("embeds", List.of(embed));
        return payload;
    }

    private static String buildContent(String summary) {
        if (summary == null || summary.isBlank()) {
            return EVERYONE_MENTION;
        }
        if (summary.startsWith(EVERYONE_MENTION)) {
            return trim(summary, CONTENT_LIMIT);
        }
        return trim(EVERYONE_MENTION + "\n" + summary, CONTENT_LIMIT);
    }

    private static List<String> splitLines(String messageText) {
        if (messageText == null || messageText.isBlank()) {
            return List.of();
        }
        String[] rawLines = messageText.split("\\R");
        List<String> lines = new ArrayList<>(rawLines.length);
        for (String rawLine : rawLines) {
            String line = rawLine == null ? "" : rawLine.trim();
            if (!line.isEmpty()) {
                lines.add(line);
            }
        }
        return lines;
    }

    private static ParsedCard parse(List<String> lines) {
        if (lines.isEmpty()) {
            return new ParsedCard("EVE Builders Notification", "", List.of());
        }

        String title = "EVE Builders Notification";
        List<Map<String, Object>> fields = new ArrayList<>();
        List<String> descriptionLines = new ArrayList<>();
        boolean titleSet = false;

        for (String sourceLine : lines) {
            String line = sourceLine.startsWith("- ") ? sourceLine.substring(2).trim() : sourceLine;
            int separatorIndex = line.indexOf(':');
            if (separatorIndex > 0 && fields.size() < MAX_FIELDS) {
                String fieldName = line.substring(0, separatorIndex).trim();
                String fieldValue = line.substring(separatorIndex + 1).trim();
                if (!fieldName.isEmpty() && !fieldValue.isEmpty()) {
                    Map<String, Object> field = new LinkedHashMap<>();
                    field.put("name", trim(fieldName, FIELD_NAME_LIMIT));
                    field.put("value", trim(fieldValue, FIELD_VALUE_LIMIT));
                    field.put("inline", false);
                    fields.add(field);
                    continue;
                }
            }

            if (!titleSet) {
                title = line;
                titleSet = true;
            } else {
                descriptionLines.add(line);
            }
        }

        String description = String.join("\n", descriptionLines);
        return new ParsedCard(trim(title, TITLE_LIMIT), trim(description, DESCRIPTION_LIMIT), fields);
    }

    private static String trim(String value, int limit) {
        if (value == null) {
            return "";
        }
        if (value.length() <= limit) {
            return value;
        }
        return value.substring(0, limit - 3) + "...";
    }

    private record ParsedCard(String title, String description, List<Map<String, Object>> fields) {
    }
}
