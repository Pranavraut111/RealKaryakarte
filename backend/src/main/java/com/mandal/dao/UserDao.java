package com.mandal.dao;

import com.mandal.model.Role;
import com.mandal.model.User;
import com.mandal.util.DbConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access for the `users` table — raw JDBC, no ORM.
 */
public class UserDao {

    // ─── Row mapper ──────────────────────────────────────────────────────
    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getLong("id"));
        u.setName(rs.getString("name"));
        
        String phone = rs.getString("phone");
        if (phone != null && phone.startsWith("NP_")) {
            phone = "";
        }
        u.setPhone(phone);
        
        u.setEmail(rs.getString("email"));
        u.setMandalId(rs.getLong("mandal_id"));
        if (rs.wasNull()) {
            u.setMandalId(null);
        }
        u.setRole(Role.fromString(rs.getString("role")));
        u.setLanguagePref(rs.getString("language_pref"));
        u.setPhotoUrl(rs.getString("photo_url"));
        u.setActive(rs.getBoolean("is_active"));
        u.setCreatedAt(rs.getTimestamp("created_at") != null
                ? rs.getTimestamp("created_at").toLocalDateTime() : null);
        u.setUpdatedAt(rs.getTimestamp("updated_at") != null
                ? rs.getTimestamp("updated_at").toLocalDateTime() : null);
        return u;
    }

    // ─── Queries ─────────────────────────────────────────────────────────

    public User findByPhone(String phone) throws SQLException {
        String sql = "SELECT * FROM users WHERE phone = ?";
        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, phone);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public User findByPhoneAndMandalId(String phone, Long mandalId) throws SQLException {
        String sql = "SELECT * FROM users WHERE phone = ? AND mandal_id = ?";
        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, phone);
            ps.setLong(2, mandalId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public User findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM users WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User u = mapRow(rs);
                    u.setPasswordHash(com.mandal.util.PasswordStore.getPassword(u.getId()));
                    return u;
                }
            }
        }
        return null;
    }

    public User findById(Long id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public List<User> findAll(Long mandalId) throws SQLException {
        String sql = "SELECT * FROM users WHERE is_active = TRUE AND mandal_id = ? ORDER BY name";
        List<User> users = new ArrayList<>();
        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, mandalId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(mapRow(rs));
                }
            }
        }
        return users;
    }

    public User insert(User user) throws SQLException {
        String sql = """
            INSERT INTO users (name, phone, email, role, language_pref, photo_url, is_active, mandal_id)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            RETURNING *
            """;
        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getName());
            
            String phone = user.getPhone();
            if (phone == null || phone.trim().isEmpty()) {
                phone = "NP_" + java.util.UUID.randomUUID().toString().substring(0, 8);
            }
            ps.setString(2, phone);
            
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getRole().name());
            ps.setString(5, user.getLanguagePref() != null ? user.getLanguagePref() : "en");
            ps.setString(6, user.getPhotoUrl());
            ps.setBoolean(7, user.isActive());
            if (user.getMandalId() != null) {
                ps.setLong(8, user.getMandalId());
            } else {
                ps.setNull(8, Types.BIGINT);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User u = mapRow(rs);
                    if (user.getPasswordHash() != null) {
                        com.mandal.util.PasswordStore.setPassword(u.getId(), user.getPasswordHash());
                    }
                    return u;
                }
            }
        }
        return null;
    }

    public void updateRole(Long userId, Role role, Long mandalId) throws SQLException {
        String sql = "UPDATE users SET role = ?, updated_at = now() WHERE id = ? AND mandal_id = ?";
        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, role.name());
            ps.setLong(2, userId);
            ps.setLong(3, mandalId);
            ps.executeUpdate();
        }
    }

    public void updateProfile(Long userId, String name, String email, String languagePref, String photoUrl) throws SQLException {
        String sql = """
            UPDATE users
            SET name = COALESCE(?, name),
                email = COALESCE(?, email),
                language_pref = COALESCE(?, language_pref),
                photo_url = COALESCE(?, photo_url),
                updated_at = now()
            WHERE id = ?
            """;
        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, languagePref);
            ps.setString(4, photoUrl);
            ps.setLong(5, userId);
            ps.executeUpdate();
        }
    }

    public int countAll(Long mandalId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE is_active = TRUE AND mandal_id = ?";
        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, mandalId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }
}
