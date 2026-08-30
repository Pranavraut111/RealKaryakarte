package com.mandal.dao;

import com.mandal.model.Contribution;
import com.mandal.model.PaymentMethod;
import com.mandal.util.DbConnectionManager;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access for the `contributions` table — raw JDBC.
 */
public class ContributionDao {

    // ─── Row mapper ──────────────────────────────────────────────────────
    private Contribution mapRow(ResultSet rs) throws SQLException {
        Contribution c = new Contribution();
        c.setId(rs.getLong("id"));
        c.setMandalId(rs.getLong("mandal_id"));
        long memberId = rs.getLong("member_id");
        c.setMemberId(rs.wasNull() ? null : memberId);
        c.setMemberName(rs.getString("member_name"));
        c.setAmount(rs.getBigDecimal("amount"));
        String pm = rs.getString("payment_method");
        c.setPaymentMethod(pm != null ? PaymentMethod.fromString(pm) : null);
        long collectedBy = rs.getLong("collected_by");
        c.setCollectedBy(rs.wasNull() ? null : collectedBy);
        c.setReceiptNo(rs.getString("receipt_no"));
        c.setReceiptPdfUrl(rs.getString("receipt_pdf_url"));
        String rawNote = rs.getString("note");
        if (rawNote != null && rawNote.contains("||COLLECTED_BY:")) {
            String[] parts = rawNote.split("\\|\\|COLLECTED_BY:");
            c.setNote(parts[0]);
            c.setCollectedByName(parts.length > 1 ? parts[1] : "");
        } else {
            c.setNote(rawNote);
            try {
                c.setCollectedByName(rs.getString("collected_by_name"));
            } catch (SQLException ignored) {}
        }
        
        c.setContributionDate(rs.getDate("contribution_date") != null
                ? rs.getDate("contribution_date").toLocalDate() : null);
        c.setCreatedAt(rs.getTimestamp("created_at") != null
                ? rs.getTimestamp("created_at").toLocalDateTime() : null);
        c.setUpdatedAt(rs.getTimestamp("updated_at") != null
                ? rs.getTimestamp("updated_at").toLocalDateTime() : null);
        long createdBy = rs.getLong("created_by");
        c.setCreatedBy(rs.wasNull() ? null : createdBy);
        c.setRoomNumber(rs.getString("room_number"));
        int floor = rs.getInt("floor_number");
        c.setFloorNumber(rs.wasNull() ? null : floor);
        c.setPhone(rs.getString("phone"));
        return c;
    }

    // ─── Receipt number generation ───────────────────────────────────────

    /**
     * Generate the next receipt number from the DB sequence.
     * Format: GM-2026-00001
     */
    public String nextReceiptNo(String prefix, int year) throws SQLException {
        String sql = "SELECT nextval('receipt_no_seq')";
        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                long seq = rs.getLong(1);
                return String.format("%s-%d-%05d", prefix, year, seq);
            }
        }
        throw new SQLException("Failed to generate receipt number");
    }

    // ─── CRUD ────────────────────────────────────────────────────────────

    public Contribution insert(Contribution c) throws SQLException {
        String sql = """
            INSERT INTO contributions (member_id, member_name, amount, payment_method,
                                       collected_by, receipt_no, note, contribution_date, created_by, mandal_id,
                                       room_number, floor_number, phone)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            RETURNING *
            """;
        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (c.getMemberId() == null) ps.setNull(1, Types.BIGINT); else ps.setLong(1, c.getMemberId());
            ps.setString(2, c.getMemberName());
            ps.setBigDecimal(3, c.getAmount());
            ps.setString(4, c.getPaymentMethod() != null ? c.getPaymentMethod().name() : null);
            if (c.getCollectedBy() == null) ps.setNull(5, Types.BIGINT); else ps.setLong(5, c.getCollectedBy());
            ps.setString(6, c.getReceiptNo());
            
            String note = c.getNote() != null ? c.getNote() : "";
            if (c.getCollectedByName() != null && !c.getCollectedByName().isEmpty()) {
                note += "||COLLECTED_BY:" + c.getCollectedByName();
            }
            ps.setString(7, note);
            ps.setDate(8, Date.valueOf(c.getContributionDate()));
            if (c.getCreatedBy() == null) ps.setNull(9, Types.BIGINT); else ps.setLong(9, c.getCreatedBy());
            ps.setLong(10, c.getMandalId());
            ps.setString(11, c.getRoomNumber());
            if (c.getFloorNumber() == null) ps.setNull(12, Types.INTEGER); else ps.setInt(12, c.getFloorNumber());
            ps.setString(13, c.getPhone());
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public Contribution update(Contribution c) throws SQLException {
        String sql = """
            UPDATE contributions
            SET member_id = ?, member_name = ?, amount = ?, payment_method = ?,
                note = ?, contribution_date = ?, receipt_pdf_url = ?,
                room_number = ?, floor_number = ?, phone = ?, updated_at = now()
            WHERE id = ? AND mandal_id = ?
            RETURNING *
            """;
        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (c.getMemberId() == null) ps.setNull(1, Types.BIGINT); else ps.setLong(1, c.getMemberId());
            ps.setString(2, c.getMemberName());
            ps.setBigDecimal(3, c.getAmount());
            ps.setString(4, c.getPaymentMethod() != null ? c.getPaymentMethod().name() : null);
            String note = c.getNote() != null ? c.getNote() : "";
            if (c.getCollectedByName() != null && !c.getCollectedByName().isEmpty()) {
                note += "||COLLECTED_BY:" + c.getCollectedByName();
            }
            ps.setString(5, note);
            ps.setDate(6, Date.valueOf(c.getContributionDate()));
            ps.setString(7, c.getReceiptPdfUrl());
            ps.setString(8, c.getRoomNumber());
            if (c.getFloorNumber() == null) ps.setNull(9, Types.INTEGER); else ps.setInt(9, c.getFloorNumber());
            ps.setString(10, c.getPhone());
            ps.setLong(11, c.getId());
            ps.setLong(12, c.getMandalId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public boolean delete(Long id, Long mandalId) throws SQLException {
        String sql = "DELETE FROM contributions WHERE id = ? AND mandal_id = ?";
        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.setLong(2, mandalId);
            return ps.executeUpdate() > 0;
        }
    }

    public Contribution findById(Long id, Long mandalId) throws SQLException {
        String sql = """
            SELECT c.*, u.name AS collected_by_name
            FROM contributions c
            LEFT JOIN users u ON u.id = c.collected_by
            WHERE c.id = ? AND c.mandal_id = ?
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
     * Fetch contributions with optional filters.
     * All filter parameters are nullable.
     */
    public List<Contribution> findAll(Long mandalId, LocalDate from, LocalDate to,
                                       Long memberId, String method) throws SQLException {
        StringBuilder sql = new StringBuilder("""
            SELECT c.*, u.name AS collected_by_name
            FROM contributions c
            LEFT JOIN users u ON u.id = c.collected_by
            WHERE c.mandal_id = ?
            """);
        List<Object> params = new ArrayList<>();
        params.add(mandalId);

        if (from != null) {
            sql.append(" AND c.contribution_date >= ?");
            params.add(Date.valueOf(from));
        }
        if (to != null) {
            sql.append(" AND c.contribution_date <= ?");
            params.add(Date.valueOf(to));
        }
        if (memberId != null) {
            sql.append(" AND c.member_id = ?");
            params.add(memberId);
        }
        if (method != null && !method.isBlank()) {
            sql.append(" AND c.payment_method = ?");
            params.add(method.toUpperCase());
        }

        sql.append(" ORDER BY c.contribution_date DESC, c.created_at DESC");

        List<Contribution> results = new ArrayList<>();
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

    // ─── Aggregates ──────────────────────────────────────────────────────

    public BigDecimal sumTotal(Long mandalId) throws SQLException {
        String sql = "SELECT SUM(amount) FROM contributions WHERE mandal_id = ?";
        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, mandalId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BigDecimal sum = rs.getBigDecimal(1);
                    return sum != null ? sum : BigDecimal.ZERO;
                }
            }
        }
        return BigDecimal.ZERO;
    }

    public int countAll(Long mandalId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM contributions WHERE mandal_id = ?";
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
