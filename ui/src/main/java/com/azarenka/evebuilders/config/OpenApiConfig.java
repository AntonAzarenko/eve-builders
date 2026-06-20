package com.azarenka.evebuilders.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public GroupedOpenApi restApi() {
        return GroupedOpenApi.builder()
            .group("rest")
            .packagesToScan("com.azarenka.evebuilders.rest")
            .build();
    }
}
