package com.azarenka.evebuilders.config.database;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class DatabaseTargetValidator {

    private static final Set<String> UNSAFE_DDL_VALUES = Set.of("update", "create", "create-drop");

    public void validateRuntime(DataSource dataSource,
                                AppDatabaseProperties appDatabaseProperties,
                                Environment environment,
                                JpaHibernateProperties jpaHibernateProperties,
                                JpaHibernateLegacyProperties legacyProperties) {
        validateMode(appDatabaseProperties.mode(), false, appDatabaseProperties.allowMigration());
        validateLiquibaseDisabled(environment);
        validateHibernateSafety(jpaHibernateProperties, legacyProperties);
        validateTarget(dataSource, appDatabaseProperties);
    }

    public void validateMigration(DataSource dataSource,
                                  AppDatabaseProperties appDatabaseProperties,
                                  Environment environment,
                                  JpaHibernateProperties jpaHibernateProperties,
                                  JpaHibernateLegacyProperties legacyProperties) {
        validateMode(appDatabaseProperties.mode(), true, appDatabaseProperties.allowMigration());
        validateLiquibaseEnabled(environment);
        validateHibernateSafety(jpaHibernateProperties, legacyProperties);
        validateTarget(dataSource, appDatabaseProperties);
    }

    private void validateMode(DatabaseMode mode, boolean migrationProfile, boolean allowMigration) {
        if (migrationProfile) {
            if (mode != DatabaseMode.MIGRATION) {
                throw new IllegalStateException("migration profile requires app.database.mode=migration, but was " + mode);
            }
            if (!allowMigration) {
                throw new IllegalStateException("migration profile requires app.database.allow-migration=true");
            }
            return;
        }

        if (mode == DatabaseMode.MIGRATION) {
            throw new IllegalStateException("runtime profile cannot use app.database.mode=migration");
        }
        if (allowMigration) {
            throw new IllegalStateException("runtime profile requires app.database.allow-migration=false");
        }
    }

    private void validateLiquibaseDisabled(Environment environment) {
        boolean liquibaseEnabled = environment.getProperty("spring.liquibase.enabled", Boolean.class, false);
        if (liquibaseEnabled) {
            throw new IllegalStateException("spring.liquibase.enabled must be false for ordinary runtime");
        }
    }

    private void validateLiquibaseEnabled(Environment environment) {
        boolean liquibaseEnabled = environment.getProperty("spring.liquibase.enabled", Boolean.class, false);
        if (!liquibaseEnabled) {
            throw new IllegalStateException("migration profile requires spring.liquibase.enabled=true");
        }
        requireText(environment.getProperty("spring.liquibase.change-log"), "spring.liquibase.change-log");
        requireText(environment.getProperty("spring.liquibase.url"), "spring.liquibase.url");
        requireText(environment.getProperty("spring.liquibase.user"), "spring.liquibase.user");
        requireText(environment.getProperty("spring.liquibase.password"), "spring.liquibase.password");
        requireText(environment.getProperty("spring.liquibase.driver-class-name"), "spring.liquibase.driver-class-name");
    }

    private void validateHibernateSafety(JpaHibernateProperties jpaHibernateProperties,
                                         JpaHibernateLegacyProperties legacyProperties) {
        String ddlAuto = normalize(jpaHibernateProperties == null ? null : jpaHibernateProperties.ddlAuto());
        if (ddlAuto != null && UNSAFE_DDL_VALUES.contains(ddlAuto)) {
            throw new IllegalStateException("hibernate ddl-auto must not be " + ddlAuto);
        }

        String legacyAuto = normalize(legacyProperties == null ? null : legacyProperties.auto());
        if (legacyAuto != null && UNSAFE_DDL_VALUES.contains(legacyAuto)) {
            throw new IllegalStateException("spring.jpa.properties.hibernate.hbm2ddl.auto must not be " + legacyAuto);
        }
    }

    private void validateTarget(DataSource dataSource, AppDatabaseProperties databaseProperties) {
        String expectedDatabase = requireText(databaseProperties.expectedDatabase(), "app.database.expected-database");
        String expectedSchema = requireText(databaseProperties.expectedSchema(), "app.database.expected-schema");

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            String actualDatabase = querySingleValue(statement, "SELECT current_database()");
            String actualSchema = querySingleValue(statement, "SELECT current_schema()");
            String searchPath = querySingleValue(statement, "SHOW search_path");

            if (!expectedDatabase.equals(actualDatabase)) {
                throw new IllegalStateException(buildMismatchMessage(
                    databaseProperties.mode(),
                    expectedDatabase,
                    actualDatabase,
                    expectedSchema,
                    actualSchema,
                    searchPath
                ));
            }

            if (!expectedSchema.equals(actualSchema)) {
                throw new IllegalStateException(buildMismatchMessage(
                    databaseProperties.mode(),
                    expectedDatabase,
                    actualDatabase,
                    expectedSchema,
                    actualSchema,
                    searchPath
                ));
            }

            if (!searchPathContains(searchPath, expectedSchema)) {
                throw new IllegalStateException(buildMismatchMessage(
                    databaseProperties.mode(),
                    expectedDatabase,
                    actualDatabase,
                    expectedSchema,
                    actualSchema,
                    searchPath
                ));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to validate PostgreSQL target database", exception);
        }
    }

    private String buildMismatchMessage(DatabaseMode mode,
                                        String expectedDatabase,
                                        String actualDatabase,
                                        String expectedSchema,
                                        String actualSchema,
                                        String searchPath) {
        return "Database target validation failed: mode=" + mode
            + ", expected-database=" + expectedDatabase
            + ", actual-database=" + actualDatabase
            + ", expected-schema=" + expectedSchema
            + ", actual-schema=" + actualSchema
            + ", search-path=" + searchPath;
    }

    private String querySingleValue(Statement statement, String sql) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery(sql)) {
            if (!resultSet.next()) {
                return null;
            }
            return resultSet.getString(1);
        }
    }

    private boolean searchPathContains(String searchPath, String expectedSchema) {
        if (searchPath == null || searchPath.isBlank()) {
            return false;
        }

        String normalizedExpected = expectedSchema.toLowerCase(Locale.ROOT);
        return Arrays.stream(searchPath.split(","))
            .map(String::trim)
            .map(this::stripQuotes)
            .map(value -> value.toLowerCase(Locale.ROOT))
            .anyMatch(normalizedExpected::equals);
    }

    private String stripQuotes(String value) {
        String trimmed = value.trim();
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"") && trimmed.length() >= 2) {
            return trimmed.substring(1, trimmed.length() - 1);
        }
        return trimmed;
    }

    private String requireText(String value, String propertyName) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(propertyName + " must be provided");
        }
        return value;
    }

    private String normalize(String value) {
        return value == null ? null : value.trim().toLowerCase(Locale.ROOT);
    }
}
