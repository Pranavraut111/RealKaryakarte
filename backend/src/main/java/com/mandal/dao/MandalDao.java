package com.mandal.dao;

import com.mandal.model.Mandal;
import com.mandal.util.DbConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class MandalDao {

    private Mandal mapRow(ResultSet rs) throws SQLException {
        Mandal m = new Mandal();
        m.setId(rs.getLong("id"));
        m.setMandalName(rs.getString("mandal_name"));
        m.setInviteCode(rs.getString("invite_code"));
        return m;
    }

    public Mandal findById(Long id) throws SQLException {
        String sql = "SELECT * FROM mandals WHERE id = ?";
        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public Mandal findByInviteCode(String inviteCode) throws SQLException {
        String sql = "SELECT * FROM mandals WHERE invite_code = ?";
        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, inviteCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public Mandal createMandal(String name) throws SQLException {
        String sql = "INSERT INTO mandals (mandal_name, invite_code) VALUES (?, ?) RETURNING *";
        String inviteCode = "MANDAL-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, inviteCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public void updateMandalName(Long id, String newName) throws SQLException {
        String sql = "UPDATE mandals SET mandal_name = ? WHERE id = ?";
        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newName);
            ps.setLong(2, id);
            ps.executeUpdate();
        }
    }
}
