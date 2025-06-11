package com.azarenka.evebuilders.service.impl.intergarion;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.azarenka.evebuilders.domain.db.Alliance;
import com.azarenka.evebuilders.domain.db.Corporation;
import com.azarenka.evebuilders.service.api.IEveAllianceService;
import com.azarenka.evebuilders.service.api.IEveCharacterService;
import com.azarenka.evebuilders.service.api.IEveCorporationService;
import com.azarenka.evebuilders.service.impl.auth.EveAuthService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class EveCharacterService implements IEveCharacterService {

    @Value("${eve.character.uri}")
    private String characterUrl;
    @Autowired
    private EveAuthService eveAuthService;
    @Autowired
    private EvePortraitService portraitService;
    @Autowired
    private IEveCorporationService corporationService;
    @Autowired
    private IEveAllianceService allianceService;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public String getCharacterInfo(String accessToken, String characterId) {
        String url = String.format(characterUrl, characterId);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        return response.getBody();
    }

    @Override
    public String getCharacterIdFromToken(String accessToken) {
        DecodedJWT decodedJWT = JWT.decode(accessToken);
        String subject = decodedJWT.getClaim("sub").asString();
        if (subject != null && subject.startsWith("CHARACTER:EVE:")) {
            return subject.split(":")[2];
        }
        throw new IllegalArgumentException("Invalid token format: character ID not found");
    }

    @Override
    public String getCharacterNameFromToken(String accessToken) {
        if (accessToken == null || !accessToken.contains(".")) {
            throw new IllegalArgumentException("Invalid JWT format: Token must contain header, payload, and signature.");
        }
        DecodedJWT decodedJWT = JWT.decode(accessToken);
        String characterName = decodedJWT.getClaim("name").asString();
        if (characterName != null) {
            return characterName;
        }
        throw new IllegalArgumentException("Character name not found in token");
    }

    @Override
    public String getCharacterCorporationName(String accessToken) {
        String characterId = getCharacterIdFromToken(accessToken);
        String corpId = getParameter(getCharacterInfo(accessToken, characterId), "corporation_id",
                String.class);
        Corporation corporation = corporationService.getCorporation(corpId);
        return corporation.getName();
    }

    @Override
    public String getCharacterAllianceName(String accessToken) {
        String characterId = getCharacterIdFromToken(accessToken);
        String allianceId = getParameter(getCharacterInfo(accessToken, characterId), "alliance_id",
                String.class);
        Alliance alliance = allianceService.getAlliance(allianceId);
        return alliance.getName();
    }

    public <T> T getParameter(String json, String parameterName, Class<T> type) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode node = objectMapper.readTree(json);
            JsonNode parameterNode = node.get(parameterName);
            if (parameterNode != null) {
                return objectMapper.treeToValue(parameterNode, type);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
