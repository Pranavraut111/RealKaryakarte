package com.mandal.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Singleton HikariCP connection pool manager.
 * Reads DB config from application.properties via ConfigUtil.
 *
 * Usage:
 *   try (Connection conn = DbConnectionManager.getConnection()) {
 *       // execute SQL
 *   }
 */
public class DbConnectionManager {

    private static HikariDataSource dataSource;

    static {
        try {
            HikariConfig config = new HikariConfig();
            config.setDriverClassName("org.postgresql.Driver");
            config.setJdbcUrl(ConfigUtil.get("db.url"));
            config.setUsername(ConfigUtil.get("db.username"));
            config.setPassword(ConfigUtil.get("db.password"));

            // Pool tuning
            config.setMaximumPoolSize(ConfigUtil.getInt("db.pool.size", 10));
            config.setMinimumIdle(ConfigUtil.getInt("db.pool.min.idle", 5));
            config.setConnectionTimeout(ConfigUtil.getLong("db.pool.timeout", 30000L));

            // PostgreSQL optimizations
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

            // Keep connections alive through firewalls
            config.setKeepaliveTime(30000);

            dataSource = new HikariDataSource(config);
            System.out.println("[DbConnectionManager] HikariCP pool initialized — " +
                    config.getJdbcUrl());
        } catch (Exception e) {
            System.err.println("[DbConnectionManager] Failed to initialize connection pool!");
            e.printStackTrace();
        }
    }

    private DbConnectionManager() {}

    /**
     * Borrows a connection from the pool.
     * Always use try-with-resources — closing returns it to the pool.
     */
    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("DataSource is not initialized");
        }
        return dataSource.getConnection();
    }

    /**
     * Shuts down the pool gracefully (call from a ServletContextListener on app shutdown).
     */
    public static void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            System.out.println("[DbConnectionManager] Pool shut down.");
        }
    }
}
