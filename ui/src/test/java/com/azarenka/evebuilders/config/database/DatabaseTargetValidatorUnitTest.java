package com.azarenka.evebuilders.config.database;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DatabaseTargetValidatorUnitTest {

    private final DatabaseTargetValidator validator = new DatabaseTargetValidator();

    @Test
    void validatesMatchingTargetWithoutLiquibase() throws Exception {
        DataSource dataSource = mockDataSource("builders_v2", "builders", "builders, pg_catalog");
        MockEnvironment environment = new MockEnvironment().withProperty("spring.liquibase.enabled", "false");

        validator.validateRuntime(
            dataSource,
            new AppDatabaseProperties(DatabaseMode.V2, "builders_v2", "builders", false),
            environment,
            new JpaHibernateProperties("validate"),
            new JpaHibernateLegacyProperties(null)
        );
    }

    @Test
    void failsWhenSearchPathDoesNotContainExpectedSchema() throws Exception {
        DataSource dataSource = mockDataSource("builders_v2", "builders", "public");
        MockEnvironment environment = new MockEnvironment().withProperty("spring.liquibase.enabled", "false");

        assertThatThrownBy(() -> validator.validateRuntime(
            dataSource,
            new AppDatabaseProperties(DatabaseMode.V2, "builders_v2", "builders", false),
            environment,
            new JpaHibernateProperties("validate"),
            new JpaHibernateLegacyProperties(null)
        ))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("search-path=public");
    }

    @Test
    void failsWhenLiquibaseIsEnabledForRuntime() throws Exception {
        DataSource dataSource = mockDataSource("builders", "builders", "builders, pg_catalog");
        MockEnvironment environment = new MockEnvironment().withProperty("spring.liquibase.enabled", "true");

        assertThatThrownBy(() -> validator.validateRuntime(
            dataSource,
            new AppDatabaseProperties(DatabaseMode.LEGACY, "builders", "builders", false),
            environment,
            new JpaHibernateProperties("validate"),
            new JpaHibernateLegacyProperties(null)
        ))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("spring.liquibase.enabled must be false");
    }

    private DataSource mockDataSource(String database, String schema, String searchPath) throws Exception {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        ResultSet databaseResultSet = singleValueResultSet(database);
        ResultSet schemaResultSet = singleValueResultSet(schema);
        ResultSet searchPathResultSet = singleValueResultSet(searchPath);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery("SELECT current_database()")).thenReturn(databaseResultSet);
        when(statement.executeQuery("SELECT current_schema()")).thenReturn(schemaResultSet);
        when(statement.executeQuery("SHOW search_path")).thenReturn(searchPathResultSet);

        return dataSource;
    }

    private ResultSet singleValueResultSet(String value) throws Exception {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString(1)).thenReturn(value);
        return resultSet;
    }
}
