package com.azarenka.evebuilders.config.database;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Validated
@ConfigurationProperties(prefix = "app.database")
public record AppDatabaseProperties(
    @NotNull
    DatabaseMode mode,
    @NotBlank
    String expectedDatabase,
    @NotBlank
    String expectedSchema,
    boolean allowMigration
) {
}
