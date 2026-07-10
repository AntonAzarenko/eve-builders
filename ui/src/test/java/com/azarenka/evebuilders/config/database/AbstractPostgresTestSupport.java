package com.azarenka.evebuilders.config.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Assumptions;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.DockerClientFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

abstract class AbstractPostgresTestSupport {

    protected static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16.4-alpine")
        .withDatabaseName("postgres")
        .withUsername("test")
        .withPassword("test");

    @BeforeAll
    static void startContainer() {
        Assumptions.assumeTrue(DockerClientFactory.instance().isDockerAvailable(),
            "Docker is required for PostgreSQL Testcontainers integration tests");

        POSTGRES.start();
        createDatabase("builders");
        createDatabase("builders_v2");
        createSchema("builders", "builders");
        createSchema("builders_v2", "builders");
        createSchema("builders_v2", "legacy_schema");
    }

    protected static DataSource dataSource(String databaseName, String schema) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl(databaseName));
        config.setUsername(POSTGRES.getUsername());
        config.setPassword(POSTGRES.getPassword());
        config.setDriverClassName(POSTGRES.getDriverClassName());
        config.setSchema(schema);
        config.setConnectionInitSql("SET search_path TO " + DatabaseIdentifierValidator.quoteIdentifier(schema));
        return new HikariDataSource(config);
    }

    protected static PostgresConnectionProperties connectionProperties(String databaseName) {
        return new PostgresConnectionProperties(
            jdbcUrl(databaseName),
            POSTGRES.getUsername(),
            POSTGRES.getPassword(),
            POSTGRES.getDriverClassName()
        );
    }

    protected static AppDatabaseProperties databaseProperties(DatabaseMode mode, String expectedDatabase,
                                                              String expectedSchema, boolean allowMigration) {
        return new AppDatabaseProperties(mode, expectedDatabase, expectedSchema, allowMigration);
    }

    protected static LiquibaseProperties liquibaseProperties() {
        LiquibaseProperties properties = new LiquibaseProperties();
        properties.setChangeLog("classpath:/db/changelog/db.changelog-master.yaml");
        properties.setUrl(jdbcUrl("builders"));
        properties.setUser(POSTGRES.getUsername());
        properties.setPassword(POSTGRES.getPassword());
        properties.setDriverClassName(POSTGRES.getDriverClassName());
        return properties;
    }

    protected static JpaHibernateProperties hibernateProperties(String ddlAuto) {
        return new JpaHibernateProperties(ddlAuto);
    }

    protected static JpaHibernateLegacyProperties legacyHibernateProperties(String auto) {
        return new JpaHibernateLegacyProperties(auto);
    }

    protected static void executeSql(String databaseName, String sql) {
        try (Connection connection = DriverManager.getConnection(
            jdbcUrl(databaseName),
            POSTGRES.getUsername(),
            POSTGRES.getPassword());
             Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to execute SQL against " + databaseName, exception);
        }
    }

    protected static String jdbcUrl(String databaseName) {
        return "jdbc:postgresql://localhost:" + POSTGRES.getMappedPort(5432) + "/" + databaseName;
    }

    private static void createDatabase(String databaseName) {
        try (Connection connection = DriverManager.getConnection(
            jdbcUrl("postgres"),
            POSTGRES.getUsername(),
            POSTGRES.getPassword());
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE DATABASE " + databaseName);
        } catch (SQLException exception) {
            if (!databaseExists(exception)) {
                throw new IllegalStateException("Failed to create database " + databaseName, exception);
            }
        }
    }

    private static void createSchema(String databaseName, String schemaName) {
        executeSql(databaseName, "CREATE SCHEMA IF NOT EXISTS " + schemaName);
    }

    private static boolean databaseExists(SQLException exception) {
        String message = exception.getMessage();
        return message != null && message.contains("already exists");
    }
}
