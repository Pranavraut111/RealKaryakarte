package com.mandal.filter;

import com.mandal.util.JsonUtil;
import com.mandal.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * JWT authentication filter — intercepts all /api/* requests except /api/auth/*.
 *
 * On success, attaches userId and userRole as request attributes
 * so Servlets can access the authenticated user without re-parsing.
 */
@WebFilter(urlPatterns = "/api/*")
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String path = request.getRequestURI().substring(request.getContextPath().length());

        // ── Skip auth endpoints ─────────────────────────────────────────
        if (path.startsWith("/api/auth/") || path.startsWith("/api/uploads/") || path.startsWith("/api/receipts/")) {
            chain.doFilter(req, res);
            return;
        }

        // ── Skip OPTIONS (CORS preflight) ───────────────────────────────
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            chain.doFilter(req, res);
            return;
        }

        // ── Extract & validate JWT ──────────────────────────────────────
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            JsonUtil.writeError(response, 401, "Missing or invalid Authorization header");
            return;
        }

        String token = authHeader.substring(7);
        Claims claims = JwtUtil.validateToken(token);
        if (claims == null) {
            JsonUtil.writeError(response, 401, "Invalid or expired token");
            return;
        }

        // ── Attach user context to request ──────────────────────────────
        Long userId = JwtUtil.getUserId(claims);
        String userRole = JwtUtil.getRole(claims);

        request.setAttribute("userId", userId);
        request.setAttribute("userRole", userRole);
        
        Long mandalId = JwtUtil.getMandalId(claims);
        request.setAttribute("mandalId", mandalId);

        chain.doFilter(req, res);
    }
}
