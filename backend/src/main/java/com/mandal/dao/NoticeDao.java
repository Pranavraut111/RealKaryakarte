package com.mandal.dao;

import com.mandal.model.Notice;
import com.mandal.util.DbConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NoticeDao {

    public List<Notice> findAll(Long mandalId) throws SQLException {
        String sql = "SELECT n.*, u.name as posted_by_name " +
                     "FROM notices n " +
                     "LEFT JOIN users u ON n.posted_by = u.id " +
                     "WHERE n.mandal_id = ? " +
                     "ORDER BY n.is_pinned DESC, n.publish_at DESC";

        List<Notice> notices = new ArrayList<>();
        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, mandalId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    notices.add(mapRow(rs));
                }
            }
        }
        return notices;
    }

    public Notice insert(Notice notice) throws SQLException {
        String sql = "INSERT INTO notices (title, body, photo_url, posted_by, is_pinned, publish_at, mandal_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING *";

        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, notice.getTitle());
            stmt.setString(2, notice.getBody());
            stmt.setString(3, notice.getPhotoUrl());
            if (notice.getPostedBy() != null) {
                stmt.setLong(4, notice.getPostedBy());
            } else {
                stmt.setNull(4, Types.BIGINT);
            }
            stmt.setBoolean(5, notice.getIsPinned() != null && notice.getIsPinned());
            if (notice.getPublishAt() != null) {
                stmt.setTimestamp(6, Timestamp.valueOf(notice.getPublishAt()));
            } else {
                stmt.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
            }
            stmt.setLong(7, notice.getMandalId());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public void delete(Long id, Long mandalId) throws SQLException {
        String sql = "DELETE FROM notices WHERE id = ? AND mandal_id = ?";
        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.setLong(2, mandalId);
            stmt.executeUpdate();
        }
    }

    private Notice mapRow(ResultSet rs) throws SQLException {
        Notice n = new Notice();
        n.setId(rs.getLong("id"));
        n.setMandalId(rs.getLong("mandal_id"));
        n.setTitle(rs.getString("title"));
        n.setBody(rs.getString("body"));
        n.setPhotoUrl(rs.getString("photo_url"));
        n.setPostedBy(rs.getLong("posted_by"));
        
        try {
            n.setPostedByName(rs.getString("posted_by_name"));
        } catch (SQLException ignore) {
        }
        
        n.setIsPinned(rs.getBoolean("is_pinned"));
        
        Timestamp publishAt = rs.getTimestamp("publish_at");
        if (publishAt != null) n.setPublishAt(publishAt.toLocalDateTime());
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) n.setCreatedAt(createdAt.toLocalDateTime());
        
        return n;
    }
}
