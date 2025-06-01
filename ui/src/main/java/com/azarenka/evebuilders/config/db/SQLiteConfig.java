package com.azarenka.evebuilders.config.db;

import jakarta.persistence.EntityManagerFactory;
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

import javax.sql.DataSource;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.azarenka.evebuilders.repository.litesql",
        entityManagerFactoryRef = "sqliteEntityManager",
        transactionManagerRef = "sqliteTransactionManager"
)
public class SQLiteConfig {

    @Value("${app.sqlite.path}")
    private String sqlitePath;

    @Bean(name = "sqliteDataSource")
    public DataSource sqliteDataSource() {
        Path dbFile = Paths.get(sqlitePath).toAbsolutePath();
        return DataSourceBuilder.create()
                .url("jdbc:sqlite:" + dbFile)
                .driverClassName("org.sqlite.JDBC")
                .build();
    }

    @Bean(name = "sqliteEntityManager")
    public LocalContainerEntityManagerFactoryBean sqliteEntityManagerFactory(EntityManagerFactoryBuilder builder,
                                                                             @Qualifier("sqliteDataSource") DataSource dataSource) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "none"); // Выключаем автообновление схемы
        return builder
                .dataSource(dataSource)
                .packages("com.azarenka.evebuilders.domain.sqllite")
                .persistenceUnit("sqlite")
                .properties(properties)
                .build();
    }

    @Bean(name = "sqliteTransactionManager")
    public PlatformTransactionManager sqliteTransactionManager(
            @Qualifier("sqliteEntityManager") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
