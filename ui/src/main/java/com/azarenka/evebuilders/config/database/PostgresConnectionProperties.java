package com.azarenka.evebuilders.config.database;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;

@Validated
@ConfigurationProperties(prefix = "spring.datasource.postgresql")
public record PostgresConnectionProperties(
    @NotBlank
    String url,
    @NotBlank
    String username,
    @NotBlank
    String password,
    @NotBlank
    String driverClassName
) {
}
