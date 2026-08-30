package com.mandal.service;

import com.mandal.dao.UserDao;
import com.mandal.model.Role;
import com.mandal.model.User;
import com.mandal.util.JwtUtil;
import com.mandal.util.PasswordUtil;
import com.mandal.util.PasswordStore;

import java.sql.SQLException;

/**
 * Authentication service — email+password login, phone+password login,
 * passwordless member join, and registration.
 */
public class AuthService {

    private final UserDao userDao = new UserDao();
    private final com.mandal.dao.MandalDao mandalDao = new com.mandal.dao.MandalDao();

    /**
     * Login with email and password (Admin / Karyakarta).
     * @return Object[] { String token, User user }, or null if credentials are invalid
     */
    public Object[] login(String email, String password) throws SQLException {
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            return null;
        }

        User user = userDao.findByEmail(email.trim());
        if (user == null) {
            return null;
        }

        if (!PasswordUtil.verifyPassword(password, user.getPasswordHash())) {
            return null;
        }

        String token = JwtUtil.generateToken(user.getId(), user.getRole().name(), user.getEmail(), user.getMandalId());
        return new Object[]{ token, user };
    }

    /**
     * Login with phone and password (Karyakarta).
     * @return Object[] { String token, User user }, or null if credentials are invalid
     */
    public Object[] loginWithPhone(String phone, String password) throws SQLException {
        if (phone == null || phone.isBlank() || password == null || password.isBlank()) {
            return null;
        }

        User user = userDao.findByPhone(phone.trim());
        if (user == null) {
            return null;
        }

        String hash = PasswordStore.getPassword(user.getId());
        if (hash == null || !PasswordUtil.verifyPassword(password, hash)) {
            return null;
        }

        String token = JwtUtil.generateToken(user.getId(), user.getRole().name(), user.getEmail(), user.getMandalId());
        return new Object[]{ token, user };
    }

    /**
     * Passwordless member join — enter name + phone, get instant access.
     * If the phone already exists in this mandal, log them in directly.
     * If not, create a new MEMBER user (no password).
     */
    public Object[] memberJoin(String name, String phone, String inviteCode) throws SQLException {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("Phone number is required");
        }
        if (inviteCode == null || inviteCode.isBlank()) {
            throw new IllegalArgumentException("Invite code is required");
        }

        // Find mandal by invite code
        com.mandal.model.Mandal mandal = mandalDao.findByInviteCode(inviteCode.trim());
        if (mandal == null) {
            throw new IllegalArgumentException("Invalid invite link");
        }

        // Check if user with this phone already exists in this mandal
        User existing = userDao.findByPhoneAndMandalId(phone.trim(), mandal.getId());
        if (existing != null) {
            // If they are a karyakarta/admin AND have a password, force them to use login page
            if (existing.getRole() != Role.MEMBER) {
                String hash = PasswordStore.getPassword(existing.getId());
                if (hash != null && !hash.isBlank()) {
                    throw new IllegalArgumentException("You are a Karyakarta. Please use the Admin/Karyakarta login page with your password.");
                }
            }

            // Returning user (member, or karyakarta without password) — log them in directly
            String token = JwtUtil.generateToken(existing.getId(), existing.getRole().name(),
                    existing.getEmail(), existing.getMandalId());
            return new Object[]{ token, existing };
        }

        // New member — create account without password
        User user = new User();
        user.setName(name.trim());
        user.setPhone(phone.trim());
        user.setEmail(""); // no email for members
        user.setRole(Role.MEMBER);
        user.setLanguagePref("en");
        user.setMandalId(mandal.getId());

        User saved = userDao.insert(user);
        if (saved == null) {
            throw new RuntimeException("Failed to create member");
        }

        String token = JwtUtil.generateToken(saved.getId(), saved.getRole().name(), saved.getEmail(), saved.getMandalId());
        return new Object[]{ token, saved };
    }

    /**
     * Set password for a promoted karyakarta (who previously had no password).
     */
    public Object[] setPassword(Long userId, String password) throws SQLException {
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }

        User user = userDao.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        String hash = PasswordUtil.hashPassword(password);
        PasswordStore.setPassword(userId, hash);

        // Return fresh token
        String token = JwtUtil.generateToken(user.getId(), user.getRole().name(), user.getEmail(), user.getMandalId());
        return new Object[]{ token, user };
    }

    /**
     * Register a new user with email and password.
     * New users are always MEMBER by default.
     */
    public Object[] register(String name, String email, String password, String inviteCode) throws SQLException {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }

        // Check if email already exists
        User existing = userDao.findByEmail(email.trim());
        if (existing != null) {
            throw new IllegalArgumentException("An account with this email already exists");
        }

        Long mandalId = null;
        if (inviteCode != null && !inviteCode.isBlank()) {
            com.mandal.model.Mandal mandal = mandalDao.findByInviteCode(inviteCode.trim());
            if (mandal == null) {
                throw new IllegalArgumentException("Invalid invite code");
            }
            mandalId = mandal.getId();
        }

        User user = new User();
        user.setName(name.trim());
        user.setEmail(email.trim().toLowerCase());
        user.setPhone(""); // phone is optional now
        user.setRole(Role.MEMBER);
        user.setLanguagePref("en");
        user.setPasswordHash(PasswordUtil.hashPassword(password));
        user.setMandalId(mandalId);

        User saved = userDao.insert(user);
        if (saved == null) {
            throw new RuntimeException("Failed to create user");
        }

        String token = JwtUtil.generateToken(saved.getId(), saved.getRole().name(), saved.getEmail(), saved.getMandalId());
        return new Object[]{ token, saved };
    }
}
