package com.azarenka.evebuilders.config.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

public final class PostgresDataSourceFactory {

    private PostgresDataSourceFactory() {
    }

    public static DataSource create(PostgresConnectionProperties connectionProperties,
                                    AppDatabaseProperties databaseProperties) {
        String expectedSchema = DatabaseIdentifierValidator.requireSafeIdentifier(
            databaseProperties.expectedSchema(),
            "app.database.expected-schema"
        );

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(connectionProperties.url());
        config.setUsername(connectionProperties.username());
        config.setPassword(connectionProperties.password());
        config.setDriverClassName(connectionProperties.driverClassName());
        config.setSchema(expectedSchema);
        config.setConnectionInitSql("SET search_path TO " + DatabaseIdentifierValidator.quoteIdentifier(expectedSchema));
        return new HikariDataSource(config);
    }
}
