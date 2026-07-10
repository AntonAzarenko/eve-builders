package com.azarenka.evebuilders.config.database;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.jpa.properties.hibernate.hbm2ddl")
public record JpaHibernateLegacyProperties(
    String auto
) {
}
