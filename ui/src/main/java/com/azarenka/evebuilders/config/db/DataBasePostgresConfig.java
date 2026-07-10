package com.azarenka.evebuilders.config.db;

import com.azarenka.evebuilders.config.database.AppDatabaseProperties;
import com.azarenka.evebuilders.config.database.PostgresConnectionProperties;
import com.azarenka.evebuilders.config.database.PostgresDataSourceFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

import jakarta.persistence.EntityManagerFactory;

@Configuration
@EnableJpaRepositories(
    basePackages = "com.azarenka.evebuilders.repository.database",
    entityManagerFactoryRef = "dbEntityManager",
    transactionManagerRef = "dbTransactionManager"
)
public class DataBasePostgresConfig {

    @Bean(name = "dbDataSource")
    @Primary
    public DataSource postgresDataSource(PostgresConnectionProperties connectionProperties,
                                         AppDatabaseProperties databaseProperties) {
        return PostgresDataSourceFactory.create(connectionProperties, databaseProperties);
    }

    @Bean(name = "dbEntityManager")
    @Primary
    public LocalContainerEntityManagerFactoryBean postgresEntityManagerFactory(EntityManagerFactoryBuilder builder,
                                                                               @Qualifier("dbDataSource") DataSource dataSource) {
        return builder
            .dataSource(dataSource)
            .packages(
                "com.azarenka.evebuilders.domain.db",
                "com.azarenka.evebuilders.domain.casino",
                "com.azarenka.evebuilders.domain.acl"
            )
            .persistenceUnit("postgres")
            .build();
    }

    @Bean(name = "dbTransactionManager")
    @Primary
    public PlatformTransactionManager postgresTransactionManager(
        @Qualifier("dbEntityManager") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
