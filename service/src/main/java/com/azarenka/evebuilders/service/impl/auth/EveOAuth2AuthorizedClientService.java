package com.azarenka.evebuilders.service.impl.auth;

import com.azarenka.evebuilders.domain.db.User;
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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EveOAuth2AuthorizedClientService implements OAuth2AuthorizedClientService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EveOAuth2AuthorizedClientService.class);

    private final UserTokenService tokenService;
    private final Map<String, OAuth2AuthorizedClient> store = new ConcurrentHashMap<>();

    public EveOAuth2AuthorizedClientService(ClientRegistrationRepository clientRegistrationRepository,
                                            UserTokenService tokenStorageService) {
        this.tokenService = tokenStorageService;
    }

    @Override
    public void saveAuthorizedClient(OAuth2AuthorizedClient authorizedClient, Authentication principal) {
        store.put(buildKey(authorizedClient, principal), authorizedClient);
        if (principal.getPrincipal() instanceof EveUserPrincipal eveUserPrincipal) {
            User user = eveUserPrincipal.getUser();
            String userId = user.getUid();
            String accessToken = authorizedClient.getAccessToken().getTokenValue();
            String refreshToken = authorizedClient.getRefreshToken() != null
                ? authorizedClient.getRefreshToken().getTokenValue()
                : null;
            Instant expiresAt = authorizedClient.getAccessToken().getExpiresAt();
            UserToken token = new UserToken();
            token.setUserId(userId);
            token.setAccessToken(accessToken);
            token.setRefreshToken(refreshToken);
            token.setExpiresAt(LocalDateTime.ofInstant(expiresAt, ZoneId.systemDefault()));
            tokenService.save(token);
            LOGGER.info("User [{}], Token updated", user.getUsername());
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
}
