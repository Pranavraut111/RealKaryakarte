package com.mandal.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * SocietyRoom entity — maps to the `society_rooms` table.
 * Represents a single unit (room + floor) in the society,
 * used for tracking vargani collection progress door-to-door.
 */
public class SocietyRoom {

    private Long id;
    private Long mandalId;
    private String roomNumber;
    private int floorNumber;
    private String residentName;
    private String residentPhone;
    private String varganiStatus;   // PENDING, PAID, PARTIALLY_PAID
    private BigDecimal amountPaid;
    private Long contributionId;
    private String notes;
    private Long markedBy;
    private String markedByName;    // joined from users table, not stored
    private LocalDateTime markedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public SocietyRoom() {
        this.varganiStatus = "PENDING";
        this.amountPaid = BigDecimal.ZERO;
        this.floorNumber = 1;
    }

    // ─── Getters & Setters ───────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getMandalId() { return mandalId; }
    public void setMandalId(Long mandalId) { this.mandalId = mandalId; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public int getFloorNumber() { return floorNumber; }
    public void setFloorNumber(int floorNumber) { this.floorNumber = floorNumber; }

    public String getResidentName() { return residentName; }
    public void setResidentName(String residentName) { this.residentName = residentName; }

    public String getResidentPhone() { return residentPhone; }
    public void setResidentPhone(String residentPhone) { this.residentPhone = residentPhone; }

    public String getVarganiStatus() { return varganiStatus; }
    public void setVarganiStatus(String varganiStatus) { this.varganiStatus = varganiStatus; }

    public BigDecimal getAmountPaid() { return amountPaid; }
    public void setAmountPaid(BigDecimal amountPaid) { this.amountPaid = amountPaid; }

    public Long getContributionId() { return contributionId; }
    public void setContributionId(Long contributionId) { this.contributionId = contributionId; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Long getMarkedBy() { return markedBy; }
    public void setMarkedBy(Long markedBy) { this.markedBy = markedBy; }

    public String getMarkedByName() { return markedByName; }
    public void setMarkedByName(String markedByName) { this.markedByName = markedByName; }

    public LocalDateTime getMarkedAt() { return markedAt; }
    public void setMarkedAt(LocalDateTime markedAt) { this.markedAt = markedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
