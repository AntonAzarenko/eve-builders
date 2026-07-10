package com.azarenka.evebuilders.config.database;

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("migration")
public class MigrationRuntimeGuardConfiguration {

    @Bean
    public BeanFactoryPostProcessor prohibitMigrationProfileInRuntime() {
        return beanFactory -> {
            throw new IllegalStateException(
                "migration profile must not be used with the ordinary runtime application; use MigrationApplication instead"
            );
        };
    }
}
