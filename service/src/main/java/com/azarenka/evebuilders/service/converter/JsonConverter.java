package com.azarenka.evebuilders.service.converter;

import com.azarenka.evebuilders.domain.db.TokenResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonConverter {

    public static TokenResponse convertJsonToTokenResponse(String json) {
        TokenResponse tokenResponse;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            tokenResponse = objectMapper.readValue(json, TokenResponse.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return tokenResponse;
    }
}
