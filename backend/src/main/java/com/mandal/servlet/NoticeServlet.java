package com.mandal.servlet;

import com.mandal.dto.ApiResponse;
import com.mandal.model.Comment;
import com.mandal.model.Notice;
import com.mandal.service.NoticeService;
import com.mandal.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Notice controller with comments and reactions.
 *
 * GET    /api/notices                           → list all notices (+ reaction summaries)
 * POST   /api/notices                           → create notice
 * DELETE /api/notices/{id}                      → delete notice
 * GET    /api/notices/{id}/comments             → list comments
 * POST   /api/notices/{id}/comments             → add comment
 * DELETE /api/notices/{id}/comments/{cId}       → delete comment
 * POST   /api/notices/{id}/react                → toggle reaction
 */
@WebServlet("/api/notices/*")
public class NoticeServlet extends HttpServlet {

    private NoticeService service;

    @Override
    public void init() {
        this.service = new NoticeService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Long mandalId = (Long) req.getAttribute("mandalId");
            Long userId = (Long) req.getAttribute("userId");
            String pathInfo = req.getPathInfo();

            if (pathInfo != null && pathInfo.matches("/\\d+/comments")) {
                // GET /api/notices/{id}/comments
                String[] parts = pathInfo.split("/");
                Long noticeId = Long.parseLong(parts[1]);
                List<Comment> comments = service.getComments(noticeId);
                JsonUtil.writeOk(resp, ApiResponse.ok("Comments retrieved", comments));
            } else {
                // GET /api/notices — return notices + reaction data
                List<Notice> notices = service.getAllNotices(mandalId);
                Map<Long, Map<String, Integer>> allReactions = service.getAllReactionSummaries(mandalId);
                Map<Long, String> userReactions = service.getUserReactions(userId, mandalId);

                // Build enriched response
                Map<String, Object> data = new LinkedHashMap<>();
                data.put("notices", notices);
                data.put("reactions", allReactions);
                data.put("userReactions", userReactions);

                JsonUtil.writeOk(resp, ApiResponse.ok("Notices retrieved", data));
            }
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.writeError(resp, 500, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Long userId = (Long) req.getAttribute("userId");
            String userRole = (String) req.getAttribute("userRole");
            Long mandalId = (Long) req.getAttribute("mandalId");
            String pathInfo = req.getPathInfo();

            if (pathInfo != null && pathInfo.matches("/\\d+/react")) {
                // POST /api/notices/{id}/react — toggle reaction
                String[] parts = pathInfo.split("/");
                Long noticeId = Long.parseLong(parts[1]);

                @SuppressWarnings("unchecked")
                Map<String, String> body = JsonUtil.readBody(req, Map.class);
                String reaction = body.get("reaction");

                boolean added = service.toggleReaction(noticeId, userId, reaction, mandalId);
                Map<String, Integer> summary = service.getReactionSummary(noticeId);

                Map<String, Object> result = new LinkedHashMap<>();
                result.put("added", added);
                result.put("summary", summary);

                JsonUtil.writeOk(resp, ApiResponse.ok(added ? "Reacted" : "Removed", result));

            } else if (pathInfo != null && pathInfo.matches("/\\d+/comments")) {
                // POST /api/notices/{id}/comments
                String[] parts = pathInfo.split("/");
                Long noticeId = Long.parseLong(parts[1]);

                @SuppressWarnings("unchecked")
                Map<String, String> body = JsonUtil.readBody(req, Map.class);
                String commentBody = body.get("body");

                Comment created = service.addComment(noticeId, commentBody, userId, mandalId);
                JsonUtil.writeOk(resp, ApiResponse.ok("Comment added", created));

            } else {
                // POST /api/notices — create notice
                Notice body = JsonUtil.readBody(req, Notice.class);
                Notice created = service.addNotice(body, userId, userRole, mandalId);
                JsonUtil.writeOk(resp, ApiResponse.ok("Notice posted", created));
            }
        } catch (SecurityException e) {
            JsonUtil.writeError(resp, 403, e.getMessage());
        } catch (IllegalArgumentException e) {
            JsonUtil.writeError(resp, 400, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.writeError(resp, 500, e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Long userId = (Long) req.getAttribute("userId");
            String role = (String) req.getAttribute("userRole");
            Long mandalId = (Long) req.getAttribute("mandalId");
            String pathInfo = req.getPathInfo();

            if (pathInfo == null || pathInfo.length() <= 1) {
                throw new IllegalArgumentException("ID required");
            }

            if (pathInfo.matches("/\\d+/comments/\\d+")) {
                // DELETE /api/notices/{id}/comments/{commentId}
                String[] parts = pathInfo.split("/");
                Long commentId = Long.parseLong(parts[3]);
                service.deleteComment(commentId, userId, role);
                JsonUtil.writeOk(resp, ApiResponse.ok("Comment deleted", null));
            } else {
                // DELETE /api/notices/{id}
                Long id = Long.parseLong(pathInfo.substring(1));
                service.deleteNotice(id, role, mandalId);
                JsonUtil.writeOk(resp, ApiResponse.ok("Notice deleted", null));
            }
        } catch (SecurityException e) {
            JsonUtil.writeError(resp, 403, e.getMessage());
        } catch (Exception e) {
            JsonUtil.writeError(resp, 500, e.getMessage());
        }
    }
}
