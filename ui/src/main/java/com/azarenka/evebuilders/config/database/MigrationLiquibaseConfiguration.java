package com.azarenka.evebuilders.config.database;

import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

@Configuration
@Profile("migration")
public class MigrationLiquibaseConfiguration {

    @Bean
    public SpringLiquibase springLiquibase(DatabaseTargetValidator validator,
                                           @Qualifier("dbDataSource") DataSource dataSource,
                                           AppDatabaseProperties databaseProperties,
                                           Environment environment,
                                           JpaHibernateProperties jpaHibernateProperties,
                                           JpaHibernateLegacyProperties legacyProperties) {
        validator.validateMigration(
            dataSource,
            databaseProperties,
            environment,
            jpaHibernateProperties,
            legacyProperties
        );

        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog(environment.getProperty("spring.liquibase.change-log"));
        liquibase.setDefaultSchema(databaseProperties.expectedSchema());
        liquibase.setLiquibaseSchema(databaseProperties.expectedSchema());
        liquibase.setShouldRun(true);
        liquibase.setDropFirst(false);
        liquibase.setContexts("migration");
        return liquibase;
    }
}
