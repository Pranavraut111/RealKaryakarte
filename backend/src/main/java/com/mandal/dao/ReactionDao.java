package com.mandal.dao;

import com.mandal.util.DbConnectionManager;

import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class ReactionDao {

    /**
     * Toggle a reaction for a user on a notice.
     * If same reaction exists, remove it. If different, update it.
     * @return true if reaction was added/updated, false if removed
     */
    public boolean toggle(Long noticeId, Long userId, String reaction, Long mandalId) throws SQLException {
        // Check existing
        String checkSql = "SELECT reaction FROM notice_reactions WHERE notice_id = ? AND user_id = ?";
        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setLong(1, noticeId);
            ps.setLong(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String existing = rs.getString("reaction");
                    if (existing.equals(reaction)) {
                        // Same reaction → remove (un-react)
                        delete(noticeId, userId);
                        return false;
                    } else {
                        // Different reaction → update
                        update(noticeId, userId, reaction);
                        return true;
                    }
                }
            }
        }
        // No existing reaction → insert
        insert(noticeId, userId, reaction, mandalId);
        return true;
    }

    private void insert(Long noticeId, Long userId, String reaction, Long mandalId) throws SQLException {
        String sql = "INSERT INTO notice_reactions (notice_id, user_id, reaction, mandal_id) VALUES (?, ?, ?, ?)";
        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, noticeId);
            ps.setLong(2, userId);
            ps.setString(3, reaction);
            ps.setLong(4, mandalId);
            ps.executeUpdate();
        }
    }

    private void update(Long noticeId, Long userId, String reaction) throws SQLException {
        String sql = "UPDATE notice_reactions SET reaction = ? WHERE notice_id = ? AND user_id = ?";
        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, reaction);
            ps.setLong(2, noticeId);
            ps.setLong(3, userId);
            ps.executeUpdate();
        }
    }

    private void delete(Long noticeId, Long userId) throws SQLException {
        String sql = "DELETE FROM notice_reactions WHERE notice_id = ? AND user_id = ?";
        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, noticeId);
            ps.setLong(2, userId);
            ps.executeUpdate();
        }
    }

    /**
     * Get reaction summary for a notice: { "👍": 3, "❤️": 2, ... }
     */
    public Map<String, Integer> getSummary(Long noticeId) throws SQLException {
        String sql = "SELECT reaction, COUNT(*) as cnt FROM notice_reactions WHERE notice_id = ? GROUP BY reaction ORDER BY cnt DESC";
        Map<String, Integer> summary = new LinkedHashMap<>();
        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, noticeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    summary.put(rs.getString("reaction"), rs.getInt("cnt"));
                }
            }
        }
        return summary;
    }

    /**
     * Get the current user's reaction on a notice (null if none).
     */
    public String getUserReaction(Long noticeId, Long userId) throws SQLException {
        String sql = "SELECT reaction FROM notice_reactions WHERE notice_id = ? AND user_id = ?";
        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, noticeId);
            ps.setLong(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("reaction");
            }
        }
        return null;
    }

    /**
     * Batch get reactions for all notices (to avoid N+1 queries).
     * Returns Map<noticeId, Map<reaction, count>>
     */
    public Map<Long, Map<String, Integer>> getSummariesForMandal(Long mandalId) throws SQLException {
        String sql = "SELECT notice_id, reaction, COUNT(*) as cnt FROM notice_reactions WHERE mandal_id = ? GROUP BY notice_id, reaction";
        Map<Long, Map<String, Integer>> result = new LinkedHashMap<>();
        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, mandalId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Long nid = rs.getLong("notice_id");
                    result.computeIfAbsent(nid, k -> new LinkedHashMap<>())
                          .put(rs.getString("reaction"), rs.getInt("cnt"));
                }
            }
        }
        return result;
    }

    /**
     * Get all of a user's reactions in a mandal: Map<noticeId, reaction>
     */
    public Map<Long, String> getUserReactionsInMandal(Long userId, Long mandalId) throws SQLException {
        String sql = "SELECT notice_id, reaction FROM notice_reactions WHERE user_id = ? AND mandal_id = ?";
        Map<Long, String> result = new LinkedHashMap<>();
        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, mandalId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getLong("notice_id"), rs.getString("reaction"));
                }
            }
        }
        return result;
    }
}
