package com.mandal.dto;

import com.mandal.model.User;

/**
 * Response body from POST /api/auth/verify-otp — contains JWT + user profile.
 */
public class AuthResponse {
    private String token;
    private User user;

    public AuthResponse() {}

    public AuthResponse(String token, User user) {
        this.token = token;
        this.user = user;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
