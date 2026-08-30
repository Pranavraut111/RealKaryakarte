package com.mandal.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Runs schema migrations on application startup using the existing DB connection pool.
 */
public class SchemaMigrator {

    public static void migrate() {
        System.out.println("[SchemaMigrator] Running migrations...");
        try (Connection conn = DbConnectionManager.getConnection()) {
            
            // Drop NOT NULL constraint on phone
            try (PreparedStatement ps = conn.prepareStatement("ALTER TABLE users ALTER COLUMN phone DROP NOT NULL")) {
                ps.executeUpdate();
            } catch (SQLException ignore) {}

            // Create society_rooms table for vargani tracker
            createSocietyRoomsTable(conn);

            // Add room_number and floor_number columns to contributions (for vargani-to-room sync)
            try (PreparedStatement ps = conn.prepareStatement(
                    "ALTER TABLE contributions ADD COLUMN IF NOT EXISTS room_number VARCHAR(20), ADD COLUMN IF NOT EXISTS floor_number INT, ADD COLUMN IF NOT EXISTS phone VARCHAR(20)")) {
                ps.executeUpdate();
            } catch (SQLException e) {
                System.err.println("[SchemaMigrator] contributions room columns: " + e.getMessage());
            }

            createAdminUser(conn);
            System.out.println("[SchemaMigrator] Migrations complete.");
        } catch (SQLException e) {
            System.err.println("[SchemaMigrator] Migration failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void createAdminUser(Connection conn) throws SQLException {
        // Check if admin already exists
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT id FROM users WHERE email = ?")) {
            ps.setString(1, "praut1086@gmail.com");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long id = rs.getLong("id");
                    String hash = PasswordUtil.hashPassword("Pranav@2137");
                    
                    PasswordStore.setPassword(id, hash);
                    
                    try (PreparedStatement up = conn.prepareStatement(
                            "UPDATE users SET role = 'ADMIN', name = 'Pranav Raut' WHERE id = ?")) {
                        up.setLong(1, id);
                        up.executeUpdate();
                    }
                    System.out.println("[SchemaMigrator] Admin user updated (id=" + id + ").");
                    return;
                }
            }
        }

        // Create new admin
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO users (name, phone, email, role, is_active) VALUES (?, ?, ?, 'ADMIN', TRUE) RETURNING id")) {
            ps.setString(1, "Pranav Raut");
            ps.setString(2, "0000000000"); // placeholder phone
            ps.setString(3, "praut1086@gmail.com");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    String hash = PasswordUtil.hashPassword("Pranav@2137");
                    PasswordStore.setPassword(id, hash);
                    System.out.println("[SchemaMigrator] Admin user created: praut1086@gmail.com");
                }
            }
        }
    }

    private static void createSocietyRoomsTable(Connection conn) {
        String sql = """
            CREATE TABLE IF NOT EXISTS society_rooms (
                id BIGSERIAL PRIMARY KEY,
                mandal_id BIGINT NOT NULL,
                room_number VARCHAR(20) NOT NULL,
                floor_number INT NOT NULL DEFAULT 1,
                resident_name VARCHAR(150),
                resident_phone VARCHAR(15),
                vargani_status VARCHAR(20) DEFAULT 'PENDING',
                amount_paid NUMERIC(10,2) DEFAULT 0,
                contribution_id BIGINT REFERENCES contributions(id),
                notes TEXT,
                marked_by BIGINT REFERENCES users(id),
                marked_at TIMESTAMP,
                created_at TIMESTAMP DEFAULT now(),
                updated_at TIMESTAMP DEFAULT now(),
                CONSTRAINT room_status_check CHECK (vargani_status IN ('PENDING', 'PAID', 'PARTIALLY_PAID'))
            )
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
            System.out.println("[SchemaMigrator] society_rooms table ensured.");
        } catch (SQLException e) {
            System.err.println("[SchemaMigrator] society_rooms table creation issue: " + e.getMessage());
        }

        // Create unique index
        String idxSql = "CREATE UNIQUE INDEX IF NOT EXISTS idx_rooms_unique ON society_rooms(mandal_id, room_number, floor_number)";
        try (PreparedStatement ps = conn.prepareStatement(idxSql)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[SchemaMigrator] society_rooms index issue: " + e.getMessage());
        }
    }
}
