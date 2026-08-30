package com.mandal.servlet;

import com.mandal.dto.ApiResponse;
import com.mandal.model.User;
import com.mandal.service.UserService;
import com.mandal.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * User controller — member listing, creation, role changes, profile updates.
 *
 * GET    /api/users              → list all users
 * POST   /api/users              → create user (admin)
 * PUT    /api/users/{id}/role    → change role (admin)
 * PUT    /api/users/{id}/profile → update profile
 */
@WebServlet(urlPatterns = "/api/users/*")
public class UserServlet extends HttpServlet {

    private final UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Long mandalId = (Long) req.getAttribute("mandalId");
            List<User> users = userService.getAllUsers(mandalId);
            JsonUtil.writeOk(resp, ApiResponse.ok(users));
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.writeError(resp, 500, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String requesterRole = (String) req.getAttribute("userRole");
            if (!"ADMIN".equals(requesterRole)) {
                JsonUtil.writeError(resp, 403, "Only admins can create users");
                return;
            }

            User user = JsonUtil.readBody(req, User.class);
            User created = userService.createUser(user);
            JsonUtil.writeResponse(resp, 201, ApiResponse.ok("User created", created));
        } catch (IllegalArgumentException e) {
            JsonUtil.writeError(resp, 400, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.writeError(resp, 500, e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String pathInfo = req.getPathInfo(); // e.g., /123/role or /123/profile
            if (pathInfo == null) {
                JsonUtil.writeError(resp, 400, "Missing path: expected /users/{id}/role or /users/{id}/profile");
                return;
            }

            String[] parts = pathInfo.split("/");
            // parts: ["", "123", "role"] or ["", "123", "profile"]
            if (parts.length < 3) {
                JsonUtil.writeError(resp, 400, "Invalid path");
                return;
            }

            Long targetUserId = Long.parseLong(parts[1]);
            String action = parts[2];
            Long requesterId = (Long) req.getAttribute("userId");
            String requesterRole = (String) req.getAttribute("userRole");

            if ("role".equals(action)) {
                @SuppressWarnings("unchecked")
                Map<String, String> body = JsonUtil.readBody(req, Map.class);
                String newRole = body.get("role");
                Long mandalId = (Long) req.getAttribute("mandalId");
                userService.changeRole(targetUserId, newRole, requesterId, requesterRole, mandalId);
                JsonUtil.writeOk(resp, ApiResponse.ok("Role updated", null));

            } else if ("profile".equals(action)) {
                @SuppressWarnings("unchecked")
                Map<String, String> body = JsonUtil.readBody(req, Map.class);
                userService.updateProfile(
                        targetUserId,
                        body.get("name"),
                        body.get("email"),
                        body.get("languagePref"),
                        body.get("photoUrl"),
                        requesterId,
                        requesterRole
                );
                JsonUtil.writeOk(resp, ApiResponse.ok("Profile updated", null));

            } else {
                JsonUtil.writeError(resp, 400, "Unknown action: " + action);
            }
        } catch (SecurityException e) {
            JsonUtil.writeError(resp, 403, e.getMessage());
        } catch (NumberFormatException e) {
            JsonUtil.writeError(resp, 400, "Invalid user ID");
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.writeError(resp, 500, e.getMessage());
        }
    }
}
