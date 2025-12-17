package com.azarenka.evebuilders.domain.auth.auth.ui;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
    String issuer,
    String secret,
    long accessTtlSeconds,
    long refreshTtlSeconds,
    String refreshCookieName
) {}
