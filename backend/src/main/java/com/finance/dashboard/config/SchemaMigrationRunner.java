package com.finance.dashboard.config;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import javax.sql.DataSource;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(0)
public class SchemaMigrationRunner implements CommandLineRunner {

    private final DataSource dataSource;

    public SchemaMigrationRunner(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            if (tableExists(connection, "users")) {
                migrateUsersTable(connection);
            }
        }
    }

    private void migrateUsersTable(Connection connection) throws SQLException {
        Set<String> columns = getColumns(connection, "users");
        boolean hasLegacyUniqueName = hasUniqueSingleColumnIndex(connection, "users", "name");
        boolean needsRebuild = !columns.contains("password_hash")
                || !columns.contains("last_login_at")
                || hasLegacyUniqueName
                || hasNumericTimestampValues(connection, "users", "created_at")
                || hasNumericTimestampValues(connection, "users", "updated_at")
                || hasNumericTimestampValues(connection, "users", "last_login_at");

        if (!needsRebuild) {
            return;
        }

        String passwordHashExpression = columns.contains("password_hash")
                ? """
                    CASE
                        WHEN password_hash IS NULL OR password_hash = '' THEN ''
                        ELSE password_hash
                    END
                    """
                : "''";
        String lastLoginExpression = columns.contains("last_login_at")
                ? """
                    CASE
                        WHEN last_login_at IS NULL THEN NULL
                        WHEN typeof(last_login_at) IN ('integer', 'real') OR trim(last_login_at) GLOB '[0-9]*' OR trim(last_login_at) GLOB '-[0-9]*'
                            THEN CASE
                                WHEN abs(CAST(last_login_at AS INTEGER)) >= 100000000000
                                    THEN strftime('%Y-%m-%d %H:%M:%f', CAST(last_login_at AS INTEGER) / 1000, 'unixepoch')
                                ELSE strftime('%Y-%m-%d %H:%M:%f', CAST(last_login_at AS INTEGER), 'unixepoch')
                            END
                        ELSE last_login_at
                    END
                    """
                : "NULL";

        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys=OFF");
            statement.execute("DROP TABLE IF EXISTS users_new");
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS users_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        name TEXT NOT NULL,
                        email TEXT NOT NULL UNIQUE,
                        password_hash TEXT NOT NULL DEFAULT '',
                        role TEXT NOT NULL,
                        status TEXT NOT NULL,
                        created_at TEXT NOT NULL,
                        updated_at TEXT NOT NULL,
                        last_login_at TEXT
                    )
                    """);
            statement.execute("""
                    INSERT INTO users_new (id, name, email, password_hash, role, status, created_at, updated_at, last_login_at)
                    SELECT
                        id,
                        name,
                        email,
                    """ + passwordHashExpression + """
                        ,
                        role,
                        status,
                        CASE
                            WHEN typeof(created_at) IN ('integer', 'real') OR trim(created_at) GLOB '[0-9]*' OR trim(created_at) GLOB '-[0-9]*'
                                THEN CASE
                                    WHEN abs(CAST(created_at AS INTEGER)) >= 100000000000
                                        THEN strftime('%Y-%m-%d %H:%M:%f', CAST(created_at AS INTEGER) / 1000, 'unixepoch')
                                    ELSE strftime('%Y-%m-%d %H:%M:%f', CAST(created_at AS INTEGER), 'unixepoch')
                                END
                            ELSE created_at
                        END,
                        CASE
                            WHEN typeof(updated_at) IN ('integer', 'real') OR trim(updated_at) GLOB '[0-9]*' OR trim(updated_at) GLOB '-[0-9]*'
                                THEN CASE
                                    WHEN abs(CAST(updated_at AS INTEGER)) >= 100000000000
                                        THEN strftime('%Y-%m-%d %H:%M:%f', CAST(updated_at AS INTEGER) / 1000, 'unixepoch')
                                    ELSE strftime('%Y-%m-%d %H:%M:%f', CAST(updated_at AS INTEGER), 'unixepoch')
                                END
                            ELSE updated_at
                        END,
                    """ + lastLoginExpression + """
                    FROM users
                    """);
            statement.execute("DROP TABLE users");
            statement.execute("ALTER TABLE users_new RENAME TO users");
            statement.execute("PRAGMA foreign_keys=ON");
        }
    }

    private boolean tableExists(Connection connection, String tableName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet resultSet = metaData.getTables(null, null, tableName, null)) {
            return resultSet.next();
        }
    }

    private Set<String> getColumns(Connection connection, String tableName) throws SQLException {
        Set<String> columns = new HashSet<>();
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("PRAGMA table_info(" + tableName + ")")) {
            while (resultSet.next()) {
                columns.add(resultSet.getString("name"));
            }
        }
        return columns;
    }

    private boolean hasUniqueSingleColumnIndex(Connection connection, String tableName, String columnName) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet indexes = statement.executeQuery("PRAGMA index_list(" + tableName + ")")) {
            while (indexes.next()) {
                if (indexes.getInt("unique") != 1) {
                    continue;
                }
                String indexName = indexes.getString("name");
                try (Statement indexStatement = connection.createStatement();
                     ResultSet indexInfo = indexStatement.executeQuery("PRAGMA index_info(" + indexName + ")")) {
                    int count = 0;
                    String indexedColumn = null;
                    while (indexInfo.next()) {
                        count++;
                        indexedColumn = indexInfo.getString("name");
                    }
                    if (count == 1 && columnName.equalsIgnoreCase(indexedColumn)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean hasNumericTimestampValues(Connection connection, String tableName, String columnName) throws SQLException {
        Set<String> columns = getColumns(connection, tableName);
        if (!columns.contains(columnName)) {
            return false;
        }

        String sql = "SELECT 1 FROM " + tableName + " WHERE " + columnName + " IS NOT NULL AND (typeof(" + columnName
                + ") IN ('integer','real') OR trim(" + columnName + ") GLOB '[0-9]*' OR trim(" + columnName + ") GLOB '-[0-9]*') LIMIT 1";

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            return resultSet.next();
        }
    }
}
