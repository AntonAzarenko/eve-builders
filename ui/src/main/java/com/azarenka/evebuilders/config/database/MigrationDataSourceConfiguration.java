package com.azarenka.evebuilders.config.database;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

@Configuration
@Profile("migration")
@EnableConfigurationProperties({
    AppDatabaseProperties.class,
    PostgresConnectionProperties.class,
    JpaHibernateProperties.class,
    JpaHibernateLegacyProperties.class
})
public class MigrationDataSourceConfiguration {

    @Bean(name = "dbDataSource")
    public DataSource postgresDataSource(PostgresConnectionProperties connectionProperties,
                                         AppDatabaseProperties databaseProperties) {
        return PostgresDataSourceFactory.create(connectionProperties, databaseProperties);
    }
}
