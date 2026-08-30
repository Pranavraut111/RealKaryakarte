package com.mandal.dto;

/**
 * Request body for POST /api/auth/verify-otp
 */
public class OtpVerifyRequest {
    private String phone;
    private String otp;
    private String name;

    public OtpVerifyRequest() {}

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getOtp() { return otp; }
    public void setOtp(String otp) { this.otp = otp; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
