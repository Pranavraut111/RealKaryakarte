package com.mandal.model;

import java.time.LocalDateTime;

public class Complaint {
    private Long id;
    private String message;
    private String status;
    private Long mandalId;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getMandalId() { return mandalId; }
    public void setMandalId(Long mandalId) { this.mandalId = mandalId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
