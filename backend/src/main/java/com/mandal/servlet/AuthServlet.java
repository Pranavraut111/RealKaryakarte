package com.mandal.servlet;

import com.mandal.dto.ApiResponse;
import com.mandal.dto.AuthResponse;
import com.mandal.model.User;
import com.mandal.service.AuthService;
import com.mandal.util.JsonUtil;
import com.mandal.util.PasswordStore;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

/**
 * Auth controller — handles login, registration, passwordless member join,
 * and mandal create/join.
 *
 * POST /api/auth/login        → { email, password }
 * POST /api/auth/login-phone  → { phone, password }
 * POST /api/auth/register     → { name, email, password, inviteCode? }
 * POST /api/auth/member-join  → { name, phone, inviteCode }
 * POST /api/auth/set-password → { password }   (requires JWT)
 * POST /api/auth/create-mandal → { name }      (requires JWT)
 * POST /api/auth/join-mandal   → { inviteCode } (requires JWT)
 */
@WebServlet(urlPatterns = "/api/auth/*")
public class AuthServlet extends HttpServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo();

        try {
            if ("/login".equals(path)) {
                handleLogin(req, resp);
            } else if ("/login-phone".equals(path)) {
                handlePhoneLogin(req, resp);
            } else if ("/register".equals(path)) {
                handleRegister(req, resp);
            } else if ("/member-join".equals(path)) {
                handleMemberJoin(req, resp);
            } else if ("/set-password".equals(path)) {
                handleSetPassword(req, resp);
            } else if ("/create-mandal".equals(path)) {
                handleCreateMandal(req, resp);
            } else if ("/join-mandal".equals(path)) {
                handleJoinMandal(req, resp);
            } else {
                JsonUtil.writeError(resp, 404, "Unknown auth endpoint");
            }
        } catch (IllegalArgumentException e) {
            JsonUtil.writeError(resp, 400, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.writeError(resp, 500, "Internal server error: " + e.getMessage());
        }
    }

    private void handleLogin(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        @SuppressWarnings("unchecked")
        Map<String, String> body = JsonUtil.readBody(req, Map.class);
        String email = body.get("email");
        String password = body.get("password");

        Object[] result = authService.login(email, password);
        if (result == null) {
            JsonUtil.writeError(resp, 401, "Invalid email or password");
            return;
        }

        String token = (String) result[0];
        User user = (User) result[1];

        // Check if this karyakarta needs to set a password
        boolean needsPassword = false;
        if ("KARYAKARTA".equals(user.getRole().name())) {
            String hash = PasswordStore.getPassword(user.getId());
            needsPassword = (hash == null || hash.isBlank());
        }

        Map<String, Object> data = Map.of(
            "token", token,
            "user", user,
            "needsPassword", needsPassword
        );
        JsonUtil.writeOk(resp, ApiResponse.ok("Login successful", data));
    }

    private void handlePhoneLogin(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        @SuppressWarnings("unchecked")
        Map<String, String> body = JsonUtil.readBody(req, Map.class);
        String phone = body.get("phone");
        String password = body.get("password");

        Object[] result = authService.loginWithPhone(phone, password);
        if (result == null) {
            JsonUtil.writeError(resp, 401, "Invalid phone number or password");
            return;
        }

        String token = (String) result[0];
        User user = (User) result[1];
        JsonUtil.writeOk(resp, ApiResponse.ok("Login successful", new AuthResponse(token, user)));
    }

    private void handleRegister(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        @SuppressWarnings("unchecked")
        Map<String, String> body = JsonUtil.readBody(req, Map.class);
        String name = body.get("name");
        String email = body.get("email");
        String password = body.get("password");
        String inviteCode = body.get("inviteCode");

        Object[] result = authService.register(name, email, password, inviteCode);
        String token = (String) result[0];
        User user = (User) result[1];
        JsonUtil.writeOk(resp, ApiResponse.ok("Registration successful", new AuthResponse(token, user)));
    }

    /**
     * Passwordless member join — name + phone only.
     */
    private void handleMemberJoin(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        @SuppressWarnings("unchecked")
        Map<String, String> body = JsonUtil.readBody(req, Map.class);
        String name = body.get("name");
        String phone = body.get("phone");
        String inviteCode = body.get("inviteCode");

        Object[] result = authService.memberJoin(name, phone, inviteCode);
        String token = (String) result[0];
        User user = (User) result[1];

        // Check if this user was previously promoted (needs password)
        boolean needsPassword = false;
        if ("KARYAKARTA".equals(user.getRole().name())) {
            String hash = PasswordStore.getPassword(user.getId());
            needsPassword = (hash == null || hash.isBlank());
        }

        Map<String, Object> data = Map.of(
            "token", token,
            "user", user,
            "needsPassword", needsPassword
        );
        JsonUtil.writeOk(resp, ApiResponse.ok("Welcome!", data));
    }

    /**
     * Set password for a promoted karyakarta.
     */
    private void handleSetPassword(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String authHeader = req.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            JsonUtil.writeError(resp, 401, "Missing or invalid Authorization header");
            return;
        }

        String token = authHeader.substring(7);
        io.jsonwebtoken.Claims claims = com.mandal.util.JwtUtil.validateToken(token);
        if (claims == null) {
            JsonUtil.writeError(resp, 401, "Invalid or expired token");
            return;
        }

        Long userId = com.mandal.util.JwtUtil.getUserId(claims);

        @SuppressWarnings("unchecked")
        Map<String, String> body = JsonUtil.readBody(req, Map.class);
        String password = body.get("password");

        Object[] result = authService.setPassword(userId, password);
        String newToken = (String) result[0];
        User user = (User) result[1];
        JsonUtil.writeOk(resp, ApiResponse.ok("Password set successfully", new AuthResponse(newToken, user)));
    }

    private void handleCreateMandal(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        // Extract token to get user info, since this endpoint doesn't pass through AuthFilter
        String authHeader = req.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            JsonUtil.writeError(resp, 401, "Missing or invalid Authorization header");
            return;
        }

        String token = authHeader.substring(7);
        io.jsonwebtoken.Claims claims = com.mandal.util.JwtUtil.validateToken(token);
        if (claims == null) {
            JsonUtil.writeError(resp, 401, "Invalid or expired token");
            return;
        }
        
        Long userId = com.mandal.util.JwtUtil.getUserId(claims);
        Long mandalId = com.mandal.util.JwtUtil.getMandalId(claims);
        
        if (mandalId != null) {
            JsonUtil.writeError(resp, 400, "You already belong to a Mandal");
            return;
        }
        
        @SuppressWarnings("unchecked")
        Map<String, String> body = JsonUtil.readBody(req, Map.class);
        String mandalName = body.get("name");
        
        if (mandalName == null || mandalName.trim().isEmpty()) {
            JsonUtil.writeError(resp, 400, "Mandal name is required");
            return;
        }
        
        com.mandal.dao.MandalDao mandalDao = new com.mandal.dao.MandalDao();
        com.mandal.model.Mandal mandal = mandalDao.createMandal(mandalName);
        
        if (mandal != null) {
            // Update user with this mandal_id and make them ADMIN
            com.mandal.dao.UserDao userDao = new com.mandal.dao.UserDao();
            com.mandal.model.User user = userDao.findById(userId);
            if (user != null) {
                user.setMandalId(mandal.getId());
                user.setRole(com.mandal.model.Role.ADMIN);
                // We use raw sql here to update mandal_id and role without needing old mandal_id
                try (java.sql.Connection conn = com.mandal.util.DbConnectionManager.getConnection();
                     java.sql.PreparedStatement ps = conn.prepareStatement("UPDATE users SET mandal_id = ?, role = 'ADMIN' WHERE id = ?")) {
                    ps.setLong(1, mandal.getId());
                    ps.setLong(2, userId);
                    ps.executeUpdate();
                }
                
                // Generate new token with new mandal_id and ADMIN role
                String newToken = com.mandal.util.JwtUtil.generateToken(user.getId(), "ADMIN", user.getEmail(), mandal.getId());
                user.setMandalId(mandal.getId());
                user.setRole(com.mandal.model.Role.ADMIN);
                
                JsonUtil.writeOk(resp, ApiResponse.ok("Mandal created successfully", new AuthResponse(newToken, user)));
            } else {
                JsonUtil.writeError(resp, 404, "User not found");
            }
        } else {
            JsonUtil.writeError(resp, 500, "Failed to create Mandal");
        }
    }

    private void handleJoinMandal(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String authHeader = req.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            JsonUtil.writeError(resp, 401, "Missing or invalid Authorization header");
            return;
        }

        String token = authHeader.substring(7);
        io.jsonwebtoken.Claims claims = com.mandal.util.JwtUtil.validateToken(token);
        if (claims == null) {
            JsonUtil.writeError(resp, 401, "Invalid or expired token");
            return;
        }

        Long userId = com.mandal.util.JwtUtil.getUserId(claims);
        Long mandalId = com.mandal.util.JwtUtil.getMandalId(claims);

        if (mandalId != null) {
            JsonUtil.writeError(resp, 400, "You already belong to a Mandal");
            return;
        }

        @SuppressWarnings("unchecked")
        Map<String, String> body = JsonUtil.readBody(req, Map.class);
        String inviteCode = body.get("inviteCode");

        if (inviteCode == null || inviteCode.trim().isEmpty()) {
            JsonUtil.writeError(resp, 400, "Invite code is required");
            return;
        }

        com.mandal.dao.MandalDao mandalDao = new com.mandal.dao.MandalDao();
        com.mandal.model.Mandal mandal = mandalDao.findByInviteCode(inviteCode);

        if (mandal == null) {
            JsonUtil.writeError(resp, 404, "Invalid invite code");
            return;
        }

        com.mandal.dao.UserDao userDao = new com.mandal.dao.UserDao();
        com.mandal.model.User user = userDao.findById(userId);
        
        if (user != null) {
            user.setMandalId(mandal.getId());
            user.setRole(com.mandal.model.Role.MEMBER);
            
            try (java.sql.Connection conn = com.mandal.util.DbConnectionManager.getConnection();
                 java.sql.PreparedStatement ps = conn.prepareStatement("UPDATE users SET mandal_id = ?, role = 'MEMBER' WHERE id = ?")) {
                ps.setLong(1, mandal.getId());
                ps.setLong(2, userId);
                ps.executeUpdate();
            }

            String newToken = com.mandal.util.JwtUtil.generateToken(user.getId(), "MEMBER", user.getEmail(), mandal.getId());
            user.setMandalId(mandal.getId());
            user.setRole(com.mandal.model.Role.MEMBER);

            JsonUtil.writeOk(resp, ApiResponse.ok("Joined Mandal successfully", new AuthResponse(newToken, user)));
        } else {
            JsonUtil.writeError(resp, 404, "User not found");
        }
    }
}
