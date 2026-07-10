package com.azarenka.evebuilders.config.database;

import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.mock.env.MockEnvironment;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
class DatabaseTargetValidationIntegrationTest extends AbstractPostgresTestSupport {

    private final DatabaseTargetValidator validator = new DatabaseTargetValidator();

    @Test
    void legacyModeSucceedsOnBuildersDatabase() {
        DataSource dataSource = dataSource("builders", "builders");
        MockEnvironment environment = new MockEnvironment().withProperty("spring.liquibase.enabled", "false");

        validator.validateRuntime(
            dataSource,
            databaseProperties(DatabaseMode.LEGACY, "builders", "builders", false),
            environment,
            hibernateProperties("validate"),
            legacyHibernateProperties(null)
        );
    }

    @Test
    void v2ModeSucceedsOnBuildersV2Database() {
        DataSource dataSource = dataSource("builders_v2", "builders");
        MockEnvironment environment = new MockEnvironment().withProperty("spring.liquibase.enabled", "false");

        validator.validateRuntime(
            dataSource,
            databaseProperties(DatabaseMode.V2, "builders_v2", "builders", false),
            environment,
            hibernateProperties("validate"),
            legacyHibernateProperties(null)
        );
    }

    @Test
    void failsWhenExpectedDatabaseDoesNotMatch() {
        DataSource dataSource = dataSource("builders_v2", "builders");
        MockEnvironment environment = new MockEnvironment().withProperty("spring.liquibase.enabled", "false");

        assertThatThrownBy(() -> validator.validateRuntime(
            dataSource,
            databaseProperties(DatabaseMode.V2, "builders", "builders", false),
            environment,
            hibernateProperties("validate"),
            legacyHibernateProperties(null)
        ))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("expected-database=builders")
            .hasMessageContaining("actual-database=builders_v2");
    }

    @Test
    void failsWhenSchemaDoesNotMatch() {
        DataSource dataSource = dataSource("builders_v2", "legacy_schema");
        MockEnvironment environment = new MockEnvironment().withProperty("spring.liquibase.enabled", "false");

        assertThatThrownBy(() -> validator.validateRuntime(
            dataSource,
            databaseProperties(DatabaseMode.V2, "builders_v2", "builders", false),
            environment,
            hibernateProperties("validate"),
            legacyHibernateProperties(null)
        ))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("expected-schema=builders")
            .hasMessageContaining("actual-schema=legacy_schema");
    }

    @Test
    void failsWhenMigrationModeIsDisabled() {
        DataSource dataSource = dataSource("builders_v2", "builders");
        MockEnvironment environment = new MockEnvironment().withProperty("spring.liquibase.enabled", "true");

        assertThatThrownBy(() -> validator.validateMigration(
            dataSource,
            databaseProperties(DatabaseMode.MIGRATION, "builders_v2", "builders", false),
            environment,
            hibernateProperties("validate"),
            legacyHibernateProperties(null)
        ))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("allow-migration=true");
    }

    @Test
    void failsWhenLiquibaseIsEnabledInRuntime() {
        DataSource dataSource = dataSource("builders", "builders");
        MockEnvironment environment = new MockEnvironment().withProperty("spring.liquibase.enabled", "true");

        assertThatThrownBy(() -> validator.validateRuntime(
            dataSource,
            databaseProperties(DatabaseMode.LEGACY, "builders", "builders", false),
            environment,
            hibernateProperties("validate"),
            legacyHibernateProperties(null)
        ))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("spring.liquibase.enabled must be false");
    }

    @Test
    void failsWhenHibernateDdlAutoIsUnsafe() {
        DataSource dataSource = dataSource("builders", "builders");
        MockEnvironment environment = new MockEnvironment().withProperty("spring.liquibase.enabled", "false");

        assertThatThrownBy(() -> validator.validateRuntime(
            dataSource,
            databaseProperties(DatabaseMode.LEGACY, "builders", "builders", false),
            environment,
            hibernateProperties("update"),
            legacyHibernateProperties("update")
        ))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("ddl-auto must not be update");
    }
}
