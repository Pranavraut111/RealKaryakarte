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
 * try (Connection conn = DbConnectionManager.getConnection()) {
 * // execute SQL
 * }
 */
public class DbConnectionManager {

    private static HikariDataSource dataSource;

    static {
        int maxRetries = 3;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                HikariConfig config = new HikariConfig();
                config.setDriverClassName("org.postgresql.Driver");
                config.setJdbcUrl(ConfigUtil.get("db.url"));
                config.setUsername(ConfigUtil.get("db.username"));
                config.setPassword(ConfigUtil.get("db.password"));

                // Pool tuning
                config.setMaximumPoolSize(ConfigUtil.getInt("db.pool.size", 10));
                config.setMinimumIdle(ConfigUtil.getInt("db.pool.min.idle", 2));
                config.setConnectionTimeout(ConfigUtil.getLong("db.pool.timeout", 30000L));
                config.setInitializationFailTimeout(60000); // wait up to 60s on startup

                // PostgreSQL optimizations
                config.addDataSourceProperty("cachePrepStmts", "true");
                config.addDataSourceProperty("prepStmtCacheSize", "250");
                config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

                // Keep connections alive through firewalls
                config.setKeepaliveTime(30000);

                dataSource = new HikariDataSource(config);
                System.out.println("[DbConnectionManager] HikariCP pool initialized — " +
                        config.getJdbcUrl());

                // Auto-migrate: Add password_hash column
                try (Connection conn = dataSource.getConnection();
                     java.sql.Statement stmt = conn.createStatement()) {
                    stmt.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS password_hash VARCHAR(255)");
                    System.out.println("[DbConnectionManager] Auto-migration complete. password_hash column ensured.");
                } catch (Exception e) {
                    System.err.println("[DbConnectionManager] Auto-migration failed: " + e.getMessage());
                }

                // Auto-migrate: Add previous_balance column to mandals
                try (Connection conn = dataSource.getConnection();
                     java.sql.Statement stmt = conn.createStatement()) {
                    stmt.execute("ALTER TABLE mandals ADD COLUMN IF NOT EXISTS previous_balance NUMERIC(12,2) DEFAULT 0");
                    System.out.println("[DbConnectionManager] Auto-migration complete. previous_balance column ensured.");
                } catch (Exception e) {
                    System.err.println("[DbConnectionManager] Auto-migration (previous_balance) failed: " + e.getMessage());
                }

                break; // success — exit retry loop

            } catch (Exception e) {
                System.err.println("[DbConnectionManager] Attempt " + attempt + "/" + maxRetries +
                        " failed to initialize connection pool: " + e.getMessage());
                if (attempt < maxRetries) {
                    try { Thread.sleep(2000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                } else {
                    System.err.println("[DbConnectionManager] All retries exhausted. DataSource will be null!");
                    e.printStackTrace();
                }
            }
        }
    }

    private DbConnectionManager() {
    }

    /**
     * Borrows a connection from the pool.
     * If the pool failed to initialize on startup, attempts one lazy re-init.
     * Always use try-with-resources — closing returns it to the pool.
     */
    public static synchronized Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            System.out.println("[DbConnectionManager] DataSource is null/closed — attempting lazy re-init...");
            try {
                HikariConfig config = new HikariConfig();
                config.setDriverClassName("org.postgresql.Driver");
                config.setJdbcUrl(ConfigUtil.get("db.url"));
                config.setUsername(ConfigUtil.get("db.username"));
                config.setPassword(ConfigUtil.get("db.password"));
                config.setMaximumPoolSize(ConfigUtil.getInt("db.pool.size", 10));
                config.setMinimumIdle(2);
                config.setConnectionTimeout(30000L);
                config.setInitializationFailTimeout(60000);
                config.setKeepaliveTime(30000);
                dataSource = new HikariDataSource(config);
                System.out.println("[DbConnectionManager] Lazy re-init succeeded.");
            } catch (Exception e) {
                throw new SQLException("DataSource is not initialized and lazy re-init failed: " + e.getMessage(), e);
            }
        }
        return dataSource.getConnection();
    }

    /**
     * Shuts down the pool gracefully (call from a ServletContextListener on app
     * shutdown).
     */
    public static void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            System.out.println("[DbConnectionManager] Pool shut down.");
        }
    }
}
