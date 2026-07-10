package com.azarenka.evebuilders.config.database;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

@Configuration
@Profile("!migration")
public class RuntimeDatabaseValidationConfiguration {

    @Bean
    public ApplicationRunner databaseTargetValidationRunner(DatabaseTargetValidator validator,
                                                             @Qualifier("dbDataSource") DataSource dataSource,
                                                             AppDatabaseProperties databaseProperties,
                                                             Environment environment,
                                                             JpaHibernateProperties jpaHibernateProperties,
                                                             JpaHibernateLegacyProperties legacyProperties) {
        return args -> validator.validateRuntime(
            dataSource,
            databaseProperties,
            environment,
            jpaHibernateProperties,
            legacyProperties
        );
    }
}
