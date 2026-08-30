package com.mandal.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Date;

/**
 * JWT utility — generates and verifies tokens using JJWT 0.13.
 *
 * Tokens contain:
 *   - subject:  userId (as string)
 *   - claim "role":  user's role (ADMIN/KARYAKARTA/MEMBER)
 *   - claim "email": user's email
 *   - claim "mandalId": user's mandal ID
 */
public class JwtUtil {

    private static final SecretKey SECRET_KEY;
    private static final long EXPIRATION_MS;

    static {
        String secret = ConfigUtil.get("jwt.secret", "ZGVmYXVsdC1kZXYta2V5LTI1Ni1iaXRzLWxvbmctZW5vdWdoLWZvci1obWFjLXNoYTI1Ng==");
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        // Ensure at least 256 bits for HS256
        if (keyBytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, keyBytes.length);
            keyBytes = padded;
        }
        SECRET_KEY = new SecretKeySpec(keyBytes, "HmacSHA256");
        EXPIRATION_MS = ConfigUtil.getLong("jwt.expiration.hours", 72) * 3600 * 1000;
    }

    private JwtUtil() {}

    /**
     * Generate a signed JWT for the given user.
     * Members get a 30-day token; Admin/Karyakarta get the default (72h).
     */
    public static String generateToken(Long userId, String role, String email, Long mandalId) {
        Date now = new Date();
        long expiry = "MEMBER".equals(role)
                ? 30L * 24 * 3600 * 1000  // 30 days for members
                : EXPIRATION_MS;           // default for others
        Date expiryDate = new Date(now.getTime() + expiry);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("role", role)
                .claim("email", email)
                .claim("mandalId", mandalId)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(SECRET_KEY)
                .compact();
    }

    /**
     * Parse and validate a JWT. Returns null if invalid/expired.
     */
    public static Claims validateToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(SECRET_KEY)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Extract user ID from token claims.
     */
    public static Long getUserId(Claims claims) {
        return Long.parseLong(claims.getSubject());
    }

    /**
     * Extract role from token claims.
     */
    public static String getRole(Claims claims) {
        return claims.get("role", String.class);
    }

    /**
     * Extract mandal ID from token claims.
     */
    public static Long getMandalId(Claims claims) {
        Number mandalId = claims.get("mandalId", Number.class);
        return mandalId != null ? mandalId.longValue() : null;
    }
}
