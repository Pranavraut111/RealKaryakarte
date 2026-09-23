package com.mandal.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Database-backed password hash store.
 * Previously this was file-backed, but ephemeral environments (like Render) wipe the filesystem.
 */
public class PasswordStore {

    public static void setPassword(Long userId, String hash) throws SQLException {
        String sql = "UPDATE users SET password_hash = ? WHERE id = ?";
        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hash);
            ps.setLong(2, userId);
            ps.executeUpdate();
        }
    }

    /**
     * Returns the password hash, or null if no password is set.
     * Throws SQLException if the DB is unreachable — this prevents
     * false "needsPassword = true" on cold starts.
     */
    public static String getPassword(Long userId) throws SQLException {
        String sql = "SELECT password_hash FROM users WHERE id = ?";
        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("password_hash");
                }
            }
        }
        return null;
    }
}
