package com.azarenka.evebuilders.service.impl.auth.eve;

import com.azarenka.evebuilders.domain.db.TokenResponse;
import com.azarenka.evebuilders.service.api.IEveAuthService;
import com.azarenka.evebuilders.service.api.IUserService;
import com.azarenka.evebuilders.service.converter.JsonConverter;
import com.azarenka.evebuilders.service.converter.VaadinImageConverter;
import com.azarenka.evebuilders.service.impl.intergarion.EvePortraitService;
import com.vaadin.flow.component.html.Image;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class EveAuthService implements IEveAuthService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EveAuthService.class);

    @Value("${eve.clientId}")
    private String clientId;
    @Value("${eve.frontendClientId}")
    private String frontendClientId;
    @Value("${eve.redirectUri}")
    private String redirectUri;
    @Value("${eve.frontendRedirectUri}")
    private String frontendRedirectUri;
    @Value("${eve.frontendСlientSecret}")
    private String clientSecret;
    @Value("${eve.authorize.uri}")
    private String authorizationEndpoint;
    @Value("${eve.token.uri}")
    private String tokenEndpoint;
    @Value("${app.version}")
    private String appVersion;

    @Autowired
    private EvePortraitService evePortraitService;
    @Autowired
    private IUserService userService;
    private final RestTemplate restTemplate = new RestTemplate();


    public String generateAuthUrl() {
        var state = UUID.randomUUID().toString();
        return String.format(
            "%s?response_type=code&client_id=%s&redirect_uri=%s&scope=%s&state=%s",
            authorizationEndpoint,
            frontendClientId,
            URLEncoder.encode(frontendRedirectUri, StandardCharsets.UTF_8),
            URLEncoder.encode(
                "publicData " +
                    "esi-assets.read_assets.v1 " +
                    "esi-mail.send_mail.v1",
                StandardCharsets.UTF_8),
            state
        );
    }

    public TokenResponse exchangeCodeForToken(String authorizationCode) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("code", authorizationCode);
        params.add("redirect_uri", frontendRedirectUri);
        params.add("client_id", frontendClientId);
        params.add("client_secret", clientSecret);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);
        try {
            ResponseEntity<String> response =
                restTemplate.exchange(tokenEndpoint, HttpMethod.POST, requestEntity, String.class);
            return JsonConverter.convertJsonToTokenResponse(response.getBody());
        } catch (HttpStatusCodeException ex) {
            LOGGER.info("EVE token exchange failed: status={}, body={}", ex.getStatusCode(),
                ex.getResponseBodyAsString());
            throw new IllegalStateException(ex);
        }
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

    @Override
    public String getAppVersion() {
        return appVersion;
    }
}
