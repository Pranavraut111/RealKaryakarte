package com.mandal.service;

import com.mandal.dao.CommentDao;
import com.mandal.dao.NoticeDao;
import com.mandal.dao.ReactionDao;
import com.mandal.dao.UserDao;
import com.mandal.model.Comment;
import com.mandal.model.Notice;
import com.mandal.model.User;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class NoticeService {

    private final NoticeDao noticeDao;
    private final CommentDao commentDao;
    private final ReactionDao reactionDao;
    private final UserDao userDao;

    public NoticeService() {
        this.noticeDao = new NoticeDao();
        this.commentDao = new CommentDao();
        this.reactionDao = new ReactionDao();
        this.userDao = new UserDao();
    }

    public List<Notice> getAllNotices(Long mandalId) throws SQLException {
        return noticeDao.findAll(mandalId);
    }

    public Notice addNotice(Notice notice, Long userId, String role, Long mandalId) throws SQLException {
        if (!"ADMIN".equals(role) && !"KARYAKARTA".equals(role)) {
            throw new SecurityException("Only Admins and Karyakartas can post notices");
        }

        if (notice.getTitle() == null || notice.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (notice.getBody() == null || notice.getBody().trim().isEmpty()) {
            throw new IllegalArgumentException("Body is required");
        }

        notice.setPostedBy(userId);
        notice.setMandalId(mandalId);

        return noticeDao.insert(notice);
    }

    public void deleteNotice(Long id, String role, Long mandalId) throws SQLException {
        if (!"ADMIN".equals(role)) {
            throw new SecurityException("Only Admins can delete notices");
        }
        noticeDao.delete(id, mandalId);
    }

    // ─── Comments ─────────────────────────────────────────────────────────

    public List<Comment> getComments(Long noticeId) throws SQLException {
        return commentDao.findByNoticeId(noticeId);
    }

    public Comment addComment(Long noticeId, String body, Long userId, Long mandalId) throws SQLException {
        if (body == null || body.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment cannot be empty");
        }

        // Get user name
        User user = userDao.findById(userId);
        String userName = user != null ? user.getName() : "Unknown";

        Comment c = new Comment();
        c.setNoticeId(noticeId);
        c.setUserId(userId);
        c.setUserName(userName);
        c.setBody(body.trim());
        c.setMandalId(mandalId);

        return commentDao.insert(c);
    }

    public void deleteComment(Long commentId, Long userId, String role) throws SQLException {
        Comment c = commentDao.findById(commentId);
        if (c == null) {
            throw new IllegalArgumentException("Comment not found");
        }

        // Only the author or an admin can delete
        if (!c.getUserId().equals(userId) && !"ADMIN".equals(role)) {
            throw new SecurityException("You can only delete your own comments");
        }

        commentDao.delete(commentId);
    }

    public int getCommentCount(Long noticeId) throws SQLException {
        return commentDao.countByNoticeId(noticeId);
    }

    // ─── Reactions ─────────────────────────────────────────────────────────

    public boolean toggleReaction(Long noticeId, Long userId, String reaction, Long mandalId) throws SQLException {
        return reactionDao.toggle(noticeId, userId, reaction, mandalId);
    }

    public Map<String, Integer> getReactionSummary(Long noticeId) throws SQLException {
        return reactionDao.getSummary(noticeId);
    }

    public Map<Long, Map<String, Integer>> getAllReactionSummaries(Long mandalId) throws SQLException {
        return reactionDao.getSummariesForMandal(mandalId);
    }

    public Map<Long, String> getUserReactions(Long userId, Long mandalId) throws SQLException {
        return reactionDao.getUserReactionsInMandal(userId, mandalId);
    }
}
