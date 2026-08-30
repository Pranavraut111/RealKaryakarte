package com.mandal.dto;

/**
 * Request body for POST /api/auth/request-otp
 */
public class OtpRequest {
    private String phone;

    public OtpRequest() {}

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
