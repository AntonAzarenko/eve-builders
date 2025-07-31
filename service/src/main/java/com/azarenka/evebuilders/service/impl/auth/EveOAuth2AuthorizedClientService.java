package com.azarenka.evebuilders.service.impl.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.azarenka.evebuilders.domain.db.UserToken;
import com.azarenka.evebuilders.domain.dto.EveUserPrincipal;
import com.azarenka.evebuilders.service.impl.UserService;
import com.azarenka.evebuilders.service.impl.UserTokenService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EveOAuth2AuthorizedClientService implements OAuth2AuthorizedClientService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EveOAuth2AuthorizedClientService.class);

    private final UserTokenService tokenService;
    private final UserService userService;
    private final Map<String, OAuth2AuthorizedClient> store = new ConcurrentHashMap<>();

    public EveOAuth2AuthorizedClientService(ClientRegistrationRepository clientRegistrationRepository,
                                            UserTokenService tokenStorageService, UserService userService) {
        this.tokenService = tokenStorageService;
        this.userService = userService;
    }

    @Override
    public void saveAuthorizedClient(OAuth2AuthorizedClient authorizedClient, Authentication principal) {
        store.put(buildKey(authorizedClient, principal), authorizedClient);
        if (principal.getPrincipal() instanceof EveUserPrincipal eveUserPrincipal) {
            var characterIdFromToken = getCharacterIdFromToken(authorizedClient.getAccessToken().getTokenValue());
            var user = userService.getByCharacterId(characterIdFromToken);
            if (user.getCharacterId().equals(characterIdFromToken)) {
                var accessToken = authorizedClient.getAccessToken().getTokenValue();
                var refreshToken = authorizedClient.getRefreshToken() != null
                    ? authorizedClient.getRefreshToken().getTokenValue()
                    : null;
                var expiresAt = authorizedClient.getAccessToken().getExpiresAt();
                var token = new UserToken();
                token.setUserId(user.getUid());
                token.setAccessToken(accessToken);
                token.setRefreshToken(refreshToken);
                token.setExpiresAt(LocalDateTime.ofInstant(expiresAt, ZoneId.systemDefault()));
                tokenService.save(token);
                LOGGER.info("User [{}], Token updated", user.getUsername());
            }
        }
    }

    @Override
    public void removeAuthorizedClient(String clientRegistrationId, String principalName) {
        store.remove(buildKey(clientRegistrationId, principalName));
    }

    @Override
    public <T extends OAuth2AuthorizedClient> T loadAuthorizedClient(String clientRegistrationId,
                                                                     String principalName) {
        return (T) store.get(buildKey(clientRegistrationId, principalName));
    }

    private String buildKey(OAuth2AuthorizedClient client, Authentication principal) {
        return buildKey(client.getClientRegistration().getRegistrationId(), principal.getName());
    }

    private String buildKey(String registrationId, String principalName) {
        return registrationId + ":" + principalName;
    }

    public String getCharacterIdFromToken(String accessToken) {
        DecodedJWT decodedJWT = JWT.decode(accessToken);
        String subject = decodedJWT.getClaim("sub").asString();
        if (subject != null && subject.startsWith("CHARACTER:EVE:")) {
            return subject.split(":")[2];
        }
        throw new IllegalArgumentException("Invalid token format: character ID not found");
    }
}
