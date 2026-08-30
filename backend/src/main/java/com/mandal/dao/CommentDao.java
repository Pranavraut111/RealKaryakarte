package com.mandal.dao;

import com.mandal.model.Comment;
import com.mandal.util.DbConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentDao {

    public List<Comment> findByNoticeId(Long noticeId) throws SQLException {
        String sql = "SELECT * FROM notice_comments WHERE notice_id = ? ORDER BY created_at ASC";
        List<Comment> comments = new ArrayList<>();
        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, noticeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    comments.add(mapRow(rs));
                }
            }
        }
        return comments;
    }

    public Comment insert(Comment c) throws SQLException {
        String sql = "INSERT INTO notice_comments (notice_id, user_id, user_name, body, mandal_id) " +
                     "VALUES (?, ?, ?, ?, ?) RETURNING *";
        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, c.getNoticeId());
            ps.setLong(2, c.getUserId());
            ps.setString(3, c.getUserName());
            ps.setString(4, c.getBody());
            ps.setLong(5, c.getMandalId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public void delete(Long id) throws SQLException {
        String sql = "DELETE FROM notice_comments WHERE id = ?";
        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    public Comment findById(Long id) throws SQLException {
        String sql = "SELECT * FROM notice_comments WHERE id = ?";
        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public int countByNoticeId(Long noticeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM notice_comments WHERE notice_id = ?";
        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, noticeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    private Comment mapRow(ResultSet rs) throws SQLException {
        Comment c = new Comment();
        c.setId(rs.getLong("id"));
        c.setNoticeId(rs.getLong("notice_id"));
        c.setUserId(rs.getLong("user_id"));
        c.setUserName(rs.getString("user_name"));
        c.setBody(rs.getString("body"));
        c.setMandalId(rs.getLong("mandal_id"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) c.setCreatedAt(ts.toLocalDateTime());
        return c;
    }
}
