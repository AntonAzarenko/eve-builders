package com.azarenka.evebuilders.config.database;

import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LiquibaseConfigurationBindingTest {

    private final YamlPropertySourceLoader loader = new YamlPropertySourceLoader();

    @Test
    void applicationYamlDisablesLiquibaseByDefault() throws IOException {
        assertLiquibaseEnabled("application.yml", false);
    }

    @Test
    void applicationLocalYamlDisablesLiquibase() throws IOException {
        assertLiquibaseEnabled("application-local.yml", false);
    }

    @Test
    void applicationProdYamlDisablesLiquibase() throws IOException {
        assertLiquibaseEnabled("application-prod.yml", false);
    }

    @Test
    void applicationMigrationYamlEnablesLiquibase() throws IOException {
        assertLiquibaseEnabled("application-migration.yml", true);
    }

    private void assertLiquibaseEnabled(String resourcePath, boolean expectedValue) throws IOException {
        ConfigurableEnvironment environment = new StandardEnvironment();
        MutablePropertySources propertySources = environment.getPropertySources();

        for (PropertySource<?> propertySource : loadYaml(resourcePath)) {
            propertySources.addLast(propertySource);
        }

        assertThat(environment.getProperty("spring.liquibase.enabled", Boolean.class, !expectedValue))
            .isEqualTo(expectedValue);
    }

    private List<PropertySource<?>> loadYaml(String resourcePath) throws IOException {
        return loader.load(resourcePath, new ClassPathResource(resourcePath));
    }
}
