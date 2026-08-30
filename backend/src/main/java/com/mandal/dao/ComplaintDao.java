package com.mandal.dao;

import com.mandal.model.Complaint;
import com.mandal.util.DbConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ComplaintDao {

    public Complaint insert(Complaint c) throws SQLException {
        String sql = "INSERT INTO complaints (message, mandal_id) VALUES (?, ?) RETURNING *";
        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getMessage());
            ps.setLong(2, c.getMandalId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public List<Complaint> findAllForMandal(Long mandalId) throws SQLException {
        String sql = "SELECT * FROM complaints WHERE mandal_id = ? ORDER BY created_at DESC";
        List<Complaint> complaints = new ArrayList<>();
        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, mandalId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    complaints.add(mapRow(rs));
                }
            }
        }
        return complaints;
    }

    public boolean resolve(Long id, Long mandalId) throws SQLException {
        String sql = "UPDATE complaints SET status = 'RESOLVED' WHERE id = ? AND mandal_id = ?";
        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.setLong(2, mandalId);
            return ps.executeUpdate() > 0;
        }
    }

    private Complaint mapRow(ResultSet rs) throws SQLException {
        Complaint c = new Complaint();
        c.setId(rs.getLong("id"));
        c.setMessage(rs.getString("message"));
        c.setStatus(rs.getString("status"));
        c.setMandalId(rs.getLong("mandal_id"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) c.setCreatedAt(ts.toLocalDateTime());
        return c;
    }
}
