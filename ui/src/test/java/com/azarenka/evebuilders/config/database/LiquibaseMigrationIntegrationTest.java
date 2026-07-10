package com.azarenka.evebuilders.config.database;

import com.azarenka.evebuilders.config.db.DataBasePostgresConfig;
import liquibase.integration.spring.SpringLiquibase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.mock.env.MockEnvironment;
import org.testcontainers.junit.jupiter.Testcontainers;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class LiquibaseMigrationIntegrationTest extends AbstractPostgresTestSupport {

    private final DatabaseTargetValidator validator = new DatabaseTargetValidator();

    @Test
    void appliesLiquibaseToMigrationDatabaseAndIsIdempotent() throws Exception {
        DataSource dataSource = dataSource("builders_v2", "builders");
        AppDatabaseProperties databaseProperties = databaseProperties(DatabaseMode.MIGRATION, "builders_v2", "builders", true);
        MockEnvironment environment = new MockEnvironment()
            .withProperty("spring.liquibase.enabled", "true")
            .withProperty("spring.liquibase.change-log", "classpath:/db/changelog/db.changelog-master.yaml")
            .withProperty("spring.liquibase.url", jdbcUrl("builders_v2"))
            .withProperty("spring.liquibase.user", POSTGRES.getUsername())
            .withProperty("spring.liquibase.password", POSTGRES.getPassword())
            .withProperty("spring.liquibase.driver-class-name", POSTGRES.getDriverClassName());

        validator.validateMigration(
            dataSource,
            databaseProperties,
            environment,
            hibernateProperties("validate"),
            legacyHibernateProperties(null)
        );

        SpringLiquibase liquibase = springLiquibase(dataSource, databaseProperties, environment);
        liquibase.afterPropertiesSet();

        Integer changeLogEntriesAfterFirstRun = queryInt(dataSource, "SELECT COUNT(*) FROM builders.databasechangelog");
        Integer tableCountAfterFirstRun = queryInt(dataSource, """
            SELECT COUNT(*)
            FROM information_schema.tables
            WHERE table_schema = 'builders'
            """);

        SpringLiquibase secondRun = springLiquibase(dataSource, databaseProperties, environment);
        secondRun.afterPropertiesSet();

        Integer changeLogEntriesAfterSecondRun = queryInt(dataSource, "SELECT COUNT(*) FROM builders.databasechangelog");
        Integer tableCountAfterSecondRun = queryInt(dataSource, """
            SELECT COUNT(*)
            FROM information_schema.tables
            WHERE table_schema = 'builders'
            """);

        assertThat(changeLogEntriesAfterSecondRun).isEqualTo(changeLogEntriesAfterFirstRun);
        assertThat(tableCountAfterSecondRun).isEqualTo(tableCountAfterFirstRun);

        validateHibernateAgainstMigratedSchema(dataSource);
    }

    private SpringLiquibase springLiquibase(DataSource dataSource,
                                            AppDatabaseProperties databaseProperties,
                                            MockEnvironment environment) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog(environment.getProperty("spring.liquibase.change-log"));
        liquibase.setDefaultSchema(databaseProperties.expectedSchema());
        liquibase.setLiquibaseSchema(databaseProperties.expectedSchema());
        liquibase.setShouldRun(true);
        return liquibase;
    }

    private Integer queryInt(DataSource dataSource, String sql) throws Exception {
        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement();
             var resultSet = statement.executeQuery(sql)) {
            if (!resultSet.next()) {
                throw new IllegalStateException("No result returned for " + sql);
            }
            return resultSet.getInt(1);
        }
    }

    private void validateHibernateAgainstMigratedSchema(DataSource dataSource) throws Exception {
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.put("hibernate.hbm2ddl.auto", "validate");
        properties.put("hibernate.show_sql", "false");

        EntityManagerFactoryBuilder builder = new EntityManagerFactoryBuilder(
            new HibernateJpaVendorAdapter(),
            properties,
            null
        );
        DataBasePostgresConfig config = new DataBasePostgresConfig();
        var factoryBean = config.postgresEntityManagerFactory(builder, dataSource);
        factoryBean.afterPropertiesSet();

        EntityManagerFactory entityManagerFactory = factoryBean.getObject();
        try {
            assertThat(entityManagerFactory).isNotNull();
        } finally {
            if (entityManagerFactory != null) {
                entityManagerFactory.close();
            }
        }
    }
}
