package com.mandal.dao;

import com.mandal.util.DbConnectionManager;

import java.sql.*;

/**
 * Data access for the `otp_store` table — stores and verifies OTPs.
 */
public class OtpDao {

    /**
     * Store a new OTP for the given phone number.
     * Invalidates any existing unused OTPs for this phone first.
     */
    public void storeOtp(String phone, String otpCode, int expiryMinutes) throws SQLException {
        try (Connection conn = DbConnectionManager.getConnection()) {
            // Invalidate old OTPs for this phone
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM otp_store WHERE phone = ? AND verified = FALSE")) {
                ps.setString(1, phone);
                ps.executeUpdate();
            }

            // Insert new OTP
            String sql = """
                INSERT INTO otp_store (phone, otp_code, expires_at)
                VALUES (?, ?, now() + (? || ' minutes')::interval)
                """;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, phone);
                ps.setString(2, otpCode);
                ps.setString(3, String.valueOf(expiryMinutes));
                ps.executeUpdate();
            }
        }
    }

    /**
     * Verify an OTP. Returns true if the OTP is valid and not expired.
     * Marks the OTP as verified on success.
     */
    public boolean verifyOtp(String phone, String otpCode) throws SQLException {
        String sql = """
            UPDATE otp_store
            SET verified = TRUE
            WHERE phone = ?
              AND otp_code = ?
              AND verified = FALSE
              AND expires_at > now()
            """;
        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, phone);
            ps.setString(2, otpCode);
            return ps.executeUpdate() > 0;
        }
    }
}
