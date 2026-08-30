package com.mandal.dao;

import com.mandal.model.SocietyRoom;
import com.mandal.util.DbConnectionManager;


import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data access for the `society_rooms` table — raw JDBC.
 */
public class SocietyRoomDao {

    // ─── Row mapper ──────────────────────────────────────────────────────
    private SocietyRoom mapRow(ResultSet rs) throws SQLException {
        SocietyRoom r = new SocietyRoom();
        r.setId(rs.getLong("id"));
        r.setMandalId(rs.getLong("mandal_id"));
        r.setRoomNumber(rs.getString("room_number"));
        r.setFloorNumber(rs.getInt("floor_number"));
        r.setResidentName(rs.getString("resident_name"));
        r.setResidentPhone(rs.getString("resident_phone"));
        r.setVarganiStatus(rs.getString("vargani_status"));
        r.setAmountPaid(rs.getBigDecimal("amount_paid"));
        long contribId = rs.getLong("contribution_id");
        r.setContributionId(rs.wasNull() ? null : contribId);
        r.setNotes(rs.getString("notes"));
        long markedBy = rs.getLong("marked_by");
        r.setMarkedBy(rs.wasNull() ? null : markedBy);
        r.setMarkedAt(rs.getTimestamp("marked_at") != null
                ? rs.getTimestamp("marked_at").toLocalDateTime() : null);
        r.setCreatedAt(rs.getTimestamp("created_at") != null
                ? rs.getTimestamp("created_at").toLocalDateTime() : null);
        r.setUpdatedAt(rs.getTimestamp("updated_at") != null
                ? rs.getTimestamp("updated_at").toLocalDateTime() : null);
        // Try to get joined marked_by_name
        try {
            r.setMarkedByName(rs.getString("marked_by_name"));
        } catch (SQLException ignored) {}
        return r;
    }

    // ─── CRUD ────────────────────────────────────────────────────────────

    public SocietyRoom insert(SocietyRoom r) throws SQLException {
        String sql = """
            INSERT INTO society_rooms (mandal_id, room_number, floor_number, resident_name,
                                       resident_phone, vargani_status, amount_paid, notes)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            RETURNING *
            """;
        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, r.getMandalId());
            ps.setString(2, r.getRoomNumber());
            ps.setInt(3, r.getFloorNumber());
            ps.setString(4, r.getResidentName());
            ps.setString(5, r.getResidentPhone());
            ps.setString(6, r.getVarganiStatus());
            ps.setBigDecimal(7, r.getAmountPaid());
            ps.setString(8, r.getNotes());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    /**
     * Bulk insert rooms. Uses a single transaction for efficiency.
     */
    public int insertBulk(List<SocietyRoom> rooms) throws SQLException {
        String sql = """
            INSERT INTO society_rooms (mandal_id, room_number, floor_number, resident_name,
                                       resident_phone, vargani_status, amount_paid, notes)
            VALUES (?, ?, ?, ?, ?, 'PENDING', 0, NULL)
            ON CONFLICT (mandal_id, room_number, floor_number) DO NOTHING
            """;
        int count = 0;
        try (Connection conn = DbConnectionManager.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (SocietyRoom r : rooms) {
                    ps.setLong(1, r.getMandalId());
                    ps.setString(2, r.getRoomNumber());
                    ps.setInt(3, r.getFloorNumber());
                    ps.setString(4, r.getResidentName());
                    ps.setString(5, r.getResidentPhone());
                    ps.addBatch();
                }
                int[] results = ps.executeBatch();
                for (int res : results) {
                    if (res > 0) count++;
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
        return count;
    }

    public SocietyRoom update(SocietyRoom r) throws SQLException {
        String sql = """
            UPDATE society_rooms
            SET resident_name = ?, resident_phone = ?,
                vargani_status = ?, amount_paid = ?, contribution_id = ?, notes = ?,
                marked_by = ?, marked_at = ?, updated_at = now()
            WHERE id = ? AND mandal_id = ?
            RETURNING *
            """;
        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, r.getResidentName());
            ps.setString(2, r.getResidentPhone());
            ps.setString(3, r.getVarganiStatus());
            ps.setBigDecimal(4, r.getAmountPaid());
            if (r.getContributionId() == null) ps.setNull(5, Types.BIGINT); else ps.setLong(5, r.getContributionId());
            ps.setString(6, r.getNotes());
            if (r.getMarkedBy() == null) ps.setNull(7, Types.BIGINT); else ps.setLong(7, r.getMarkedBy());
            if (r.getMarkedAt() == null) ps.setNull(8, Types.TIMESTAMP); else ps.setTimestamp(8, Timestamp.valueOf(r.getMarkedAt()));
            ps.setLong(9, r.getId());
            ps.setLong(10, r.getMandalId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public boolean delete(Long id, Long mandalId) throws SQLException {
        String sql = "DELETE FROM society_rooms WHERE id = ? AND mandal_id = ?";
        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.setLong(2, mandalId);
            return ps.executeUpdate() > 0;
        }
    }

    public SocietyRoom findById(Long id, Long mandalId) throws SQLException {
        String sql = """
            SELECT r.*, u.name AS marked_by_name
            FROM society_rooms r
            LEFT JOIN users u ON u.id = r.marked_by
            WHERE r.id = ? AND r.mandal_id = ?
            """;
        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.setLong(2, mandalId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    /**
     * Fetch all rooms with optional status filter.
     * Ordered by room_number (natural sort), then floor_number.
     */
    public List<SocietyRoom> findAll(Long mandalId, String status) throws SQLException {
        StringBuilder sql = new StringBuilder("""
            SELECT r.*, u.name AS marked_by_name
            FROM society_rooms r
            LEFT JOIN users u ON u.id = r.marked_by
            WHERE r.mandal_id = ?
            """);
        List<Object> params = new ArrayList<>();
        params.add(mandalId);

        if (status != null && !status.isBlank()) {
            sql.append(" AND r.vargani_status = ?");
            params.add(status.toUpperCase());
        }

        // Natural sort: try numeric then alpha
        sql.append(" ORDER BY CASE WHEN r.room_number ~ '^[0-9]+$' THEN CAST(r.room_number AS INT) ELSE 999999 END, r.room_number, r.floor_number");

        List<SocietyRoom> results = new ArrayList<>();
        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
        }
        return results;
    }

    // ─── Summary / Aggregates ────────────────────────────────────────────

    /**
     * Returns summary stats: total rooms, paid count, pending count,
     * partially paid count, total amount collected.
     */
    public Map<String, Object> getSummary(Long mandalId) throws SQLException {
        String sql = """
            SELECT
                COUNT(*) AS total_rooms,
                COUNT(*) FILTER (WHERE vargani_status = 'PAID') AS paid_count,
                COUNT(*) FILTER (WHERE vargani_status = 'PENDING') AS pending_count,
                COUNT(*) FILTER (WHERE vargani_status = 'PARTIALLY_PAID') AS partial_count,
                COALESCE(SUM(amount_paid), 0) AS total_collected
            FROM society_rooms
            WHERE mandal_id = ?
            """;
        Map<String, Object> summary = new HashMap<>();
        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, mandalId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    summary.put("totalRooms", rs.getInt("total_rooms"));
                    summary.put("paidCount", rs.getInt("paid_count"));
                    summary.put("pendingCount", rs.getInt("pending_count"));
                    summary.put("partialCount", rs.getInt("partial_count"));
                    summary.put("totalCollected", rs.getBigDecimal("total_collected"));
                }
            }
        }
        return summary;
    }

    /**
     * Find a room by its unique key: mandal_id + room_number + floor_number.
     */
    public SocietyRoom findByRoomAndFloor(Long mandalId, String roomNumber, int floorNumber) throws SQLException {
        String sql = """
            SELECT r.*, u.name AS marked_by_name
            FROM society_rooms r
            LEFT JOIN users u ON u.id = r.marked_by
            WHERE r.mandal_id = ? AND r.room_number = ? AND r.floor_number = ?
            """;
        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, mandalId);
            ps.setString(2, roomNumber);
            ps.setInt(3, floorNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    /**
     * Clear the contribution link from a room (revert to PENDING) when
     * the linked contribution is deleted or re-assigned to a different room.
     */
    public void clearContributionLink(Long contributionId, Long mandalId) throws SQLException {
        String sql = """
            UPDATE society_rooms
            SET vargani_status = 'PENDING', amount_paid = 0, contribution_id = NULL,
                marked_by = NULL, marked_at = NULL, updated_at = now()
            WHERE contribution_id = ? AND mandal_id = ?
            """;
        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, contributionId);
            ps.setLong(2, mandalId);
            ps.executeUpdate();
        }
    }
}
