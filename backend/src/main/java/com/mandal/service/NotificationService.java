package com.mandal.service;

import com.mandal.util.ConfigUtil;
import java.util.List;

/**
 * Service to handle SMS and WhatsApp notifications.
 * Currently stubs out the integration and logs to console.
 */
public class NotificationService {
    
    private final String smsApiKey = ConfigUtil.get("sms_api_key", "");
    @SuppressWarnings("unused")
    private final String whatsappApiKey = ConfigUtil.get("whatsapp_api_key", "");

    public void sendOtp(String phone, String otp) {
        String message = "Your Ganpati Mandal OTP is " + otp + ". Valid for 5 minutes.";
        
        if (!smsApiKey.isEmpty()) {
            System.out.println("[NotificationService] Sending real SMS to " + phone + " via Provider API...");
            // Implement actual HTTP client call to SMS provider
        } else {
            System.out.println("[NotificationService] DEV/STUB — SMS to " + phone + ": " + message);
        }
    }

    public void sendBroadcast(String message, String channel, List<String> recipients) {
        System.out.println("[NotificationService] Broadcasting via " + channel + " to " + recipients.size() + " recipients.");
        System.out.println("[NotificationService] Message: " + message);
        
        // Implement actual HTTP client call to SMS/WhatsApp provider
    }
}
