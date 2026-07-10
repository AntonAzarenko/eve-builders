package com.azarenka.evebuilders.config.db;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!migration")
@EnableJpaRepositories(
    basePackages = "com.azarenka.evebuilders.repository.auth",
    entityManagerFactoryRef = "mariaEntityManager",
    transactionManagerRef = "mariaTransactionManager"
)
public class DataBaseMySqlConfig {

    @Value("${spring.datasource.mariadb.url}")
    private String url;
    @Value("${spring.datasource.mariadb.username}")
    private String username;
    @Value("${spring.datasource.mariadb.password}")
    private String password;
    @Value("${spring.datasource.mariadb.driver-class-name}")
    private String driver;

    @Bean(name = "mariaDataSource")
    public DataSource mariaDataSource() {
        return DataSourceBuilder.create()
            .url(url)
            .username(username)
            .password(password)
            .driverClassName(driver)
            .build();
    }

    @Bean(name = "mariaEntityManager")
    public LocalContainerEntityManagerFactoryBean mariaEntityManagerFactory(
        EntityManagerFactoryBuilder builder,
        @Qualifier("mariaDataSource") DataSource dataSource) {

        Map<String, String> jpaProps = new HashMap<>();
        jpaProps.put("hibernate.dialect", "org.hibernate.dialect.MariaDBDialect");

        return builder
            .dataSource(dataSource)
            .packages("com.azarenka.evebuilders.domain.authdb")
            .persistenceUnit("mariadb")
            .properties(jpaProps)
            .build();
    }

    @Bean(name = "mariaTransactionManager")
    public PlatformTransactionManager mariaTransactionManager(
        @Qualifier("mariaEntityManager") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
