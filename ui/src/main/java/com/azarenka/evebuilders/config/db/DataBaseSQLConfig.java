package com.azarenka.evebuilders.config.db;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.azarenka.evebuilders.repository.database",
        entityManagerFactoryRef = "dbEntityManager",
        transactionManagerRef = "dbTransactionManager"
)
public class DataBaseSQLConfig {

    @Value("${spring.datasource.mysql.url}")
    private String url;
    @Value("${spring.datasource.mysql.username}")
    private String username;
    @Value("${spring.datasource.mysql.password}")
    private String password;
    @Value("${spring.datasource.mysql.driver-class-name}")
    private String driver;

    @Bean(name = "dbDataSource")
    @Primary
    public DataSource mysqlDataSource() {
        return DataSourceBuilder.create()
                .url(url)
                .username(username)
                .password(password)
                .driverClassName(driver)
                .build();
    }

    @Bean(name = "dbEntityManager")
    @Primary
    public LocalContainerEntityManagerFactoryBean mysqlEntityManagerFactory(EntityManagerFactoryBuilder builder,
                                                                            @Qualifier("dbDataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages("com.azarenka.evebuilders.domain.db")
                .persistenceUnit("postgres")
                .build();
    }

    @Bean(name = "dbTransactionManager")
    @Primary
    public PlatformTransactionManager mysqlTransactionManager(
            @Qualifier("dbEntityManager") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
