package com.azarenka.evebuilders.service.impl.auth;

import com.azarenka.evebuilders.domain.mysql.TokenResponse;
import com.azarenka.evebuilders.service.api.IEveAuthService;
import com.azarenka.evebuilders.service.api.IUserService;
import com.azarenka.evebuilders.service.converter.JsonConverter;
import com.azarenka.evebuilders.service.converter.VaadinImageConverter;
import com.azarenka.evebuilders.service.impl.intergarion.EvePortraitService;
import com.vaadin.flow.component.html.Image;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

@Service
public class EveAuthService implements IEveAuthService {

    @Value("${eve.clientId}")
    private String clientId;
    @Value("${eve.redirectUri}")
    private String redirectUri;
    @Value("${eve.clientSecret}")
    private String clientSecret;
    @Value("${eve.authorize.uri}")
    private  String authorizationEndpoint;
    @Value("${eve.token.uri}")
    private  String tokenEndpoint;
    @Autowired
    private EvePortraitService evePortraitService;
    @Autowired
    private IUserService userService;

    public String generateAuthUrl() {
        String state = UUID.randomUUID().toString();
        return String.format(
                "%s?response_type=code&client_id=%s&redirect_uri=%s&scope=%s&state=%s",
                authorizationEndpoint,
                clientId,
                URLEncoder.encode(redirectUri, StandardCharsets.UTF_8),
                URLEncoder.encode("publicData esi-assets.read_assets.v1", StandardCharsets.UTF_8),
                state
        );
    }

    public TokenResponse exchangeCodeForToken(String authorizationCode) {
        RestTemplate restTemplate = new RestTemplate();
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("code", authorizationCode);
        params.add("redirect_uri", redirectUri);
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(clientId, clientSecret);
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                tokenEndpoint, HttpMethod.POST, requestEntity, String.class
        );
        return JsonConverter.convertJsonToTokenResponse(response.getBody());
    }

    @Override
    public Image getCharacterPortrait() {
        String characterId = userService.getCharacterId();
        if (Objects.nonNull(characterId)) {
            byte[] portrait = evePortraitService.getPortrait(Long.valueOf(userService.getCharacterId()), 64);
            return VaadinImageConverter.createImageFromBytes(portrait);
        }
        return new Image();
    }

    @Override
    public Image getCharacterPortrait128() {
        String characterId = userService.getCharacterId();
        if (Objects.nonNull(characterId)) {
            byte[] portrait = evePortraitService.getPortrait(Long.valueOf(userService.getCharacterId()), 128);
            return VaadinImageConverter.createImageFromBytes(portrait);
        }
        return new Image();
    }
}
