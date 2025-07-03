package com.azarenka.evebuilders.service.impl.intergarion;

import com.azarenka.evebuilders.domain.db.UserToken;
import com.azarenka.evebuilders.service.impl.UserTokenService;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.Objects;

@Service
public class TokenRefresherService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TokenRefresherService.class);

    private final WebClient webClient;
    private final UserTokenService userTokenService;

    @Value("${eve.client-id}")
    private String clientId;
    @Value("${eve.client-secret}")
    private String clientSecret;
    @Value("${eve.token.uri}")
    private String tokenUri;

    public TokenRefresherService(UserTokenService userTokenService) {
        this.userTokenService = userTokenService;
        this.webClient = WebClient.builder().build();
    }

    public UserToken refresh(UserToken token) {
        LOGGER.info("Refreshing token for user: {}", token.getUserId());
        var form = createParams(token);
        var response = webClient.post()
                .uri(tokenUri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(form))
                .retrieve()
                .bodyToMono(Map.class)
                .onErrorResume(e -> {
                    LOGGER.error("Failed to refresh token: {}", e.getMessage());
                    return Mono.empty();
                })
                .block();
        if (Objects.nonNull(response) && response.containsKey("access_token")) {
            var newAccessToken = (String) response.get("access_token");
            var expiresIn = (Integer) response.get("expires_in");
            var newExpiry = Instant.now().plusSeconds(expiresIn);
            token.setAccessToken(newAccessToken);
            token.setExpiresAt(LocalDateTime.ofInstant(newExpiry, ZoneId.systemDefault()));
            userTokenService.save(token);
            LOGGER.info("Token refreshed for user: {}", token.getUserId());
        }
        return token;
    }

    private MultiValueMap<String, String> createParams(UserToken token) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "refresh_token");
        form.add("refresh_token", token.getRefreshToken());
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        return form;
    }

    public void setUidCookie(String uid, HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("UID", uid)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofDays(7))
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }
}
