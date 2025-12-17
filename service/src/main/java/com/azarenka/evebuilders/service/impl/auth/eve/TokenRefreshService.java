package com.azarenka.evebuilders.service.impl.auth.eve;

import com.azarenka.evebuilders.domain.db.UserToken;
import com.azarenka.evebuilders.service.api.IUserTokenService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import reactor.core.publisher.Mono;

@Service
public class TokenRefreshService {

    private final WebClient webClient;
    private final IUserTokenService userTokenService;

    @Value("${spring.security.oauth2.client.registration.eveonline.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.eveonline.client-secret}")
    private String clientSecret;

    public TokenRefreshService(IUserTokenService userTokenService) {
        this.userTokenService = userTokenService;
        this.webClient = WebClient.builder()
            .baseUrl("https://login.eveonline.com/v2/oauth")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .build();
    }

    public Mono<String> refreshTokenIfNeeded(String userId) {
        Optional<UserToken> optional = userTokenService.getByUserId(userId);
        return optional.map(Mono::just).orElseGet(Mono::empty)
            .filter(token -> token.getExpiresAt().isBefore(LocalDateTime.now().plusMinutes(1)))
            .flatMap(this::refreshToken);
    }

    private Mono<String> refreshToken(UserToken userToken) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "refresh_token");
        form.add("refresh_token", userToken.getRefreshToken());
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);

        return webClient.post()
            .uri("/token")
            .bodyValue(form)
            .retrieve()
            .bodyToMono(Map.class)
            .doOnNext(body -> {
                String newAccessToken = (String) body.get("access_token");
                String newRefreshToken = (String) body.get("refresh_token");
                Integer expiresIn = (Integer) body.get("expires_in");

                userToken.setAccessToken(newAccessToken);
                if (newRefreshToken != null) {
                    userToken.setRefreshToken(newRefreshToken);
                }
                userToken.setExpiresAt(LocalDateTime.now().plusSeconds(expiresIn));
                userTokenService.save(userToken);
            })
            .map(body -> (String) body.get("access_token"));
    }
}
