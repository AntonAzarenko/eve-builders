package com.azarenka.evebuilders.config.database;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.boot.context.properties.bind.Binder;

import static org.assertj.core.api.Assertions.assertThat;

class LiquibasePropertyBindingTest {

    @Test
    void bindsFalseValueFromSpringLiquibaseEnabled() {
        MockEnvironment environment = new MockEnvironment().withProperty("spring.liquibase.enabled", "false");

        assertThat(Binder.get(environment).bind("spring.liquibase.enabled", Boolean.class).orElse(true))
            .isFalse();
    }

    @Test
    void bindsTrueValueFromSpringLiquibaseEnabled() {
        MockEnvironment environment = new MockEnvironment().withProperty("spring.liquibase.enabled", "true");

        assertThat(Binder.get(environment).bind("spring.liquibase.enabled", Boolean.class).orElse(false))
            .isTrue();
    }
}
