package com.mandal.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Contribution (Vargani) entity — maps to the `contributions` table.
 */
public class Contribution {

    private Long id;
    private Long memberId;
    private String memberName;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private Long collectedBy;
    private String collectedByName;   // joined from users table, not stored
    private String receiptNo;
    private String receiptPdfUrl;
    private String note;
    private LocalDate contributionDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long mandalId;
    private String roomNumber;        // optional — links to society_rooms
    private Integer floorNumber;      // optional — 0 = owner
    private String phone;             // optional — contributor's phone

    public Contribution() {}

    // ─── Getters & Setters ───────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }

    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }

    public Long getCollectedBy() { return collectedBy; }
    public void setCollectedBy(Long collectedBy) { this.collectedBy = collectedBy; }

    public String getCollectedByName() { return collectedByName; }
    public void setCollectedByName(String collectedByName) { this.collectedByName = collectedByName; }

    public String getReceiptNo() { return receiptNo; }
    public void setReceiptNo(String receiptNo) { this.receiptNo = receiptNo; }

    public String getReceiptPdfUrl() { return receiptPdfUrl; }
    public void setReceiptPdfUrl(String receiptPdfUrl) { this.receiptPdfUrl = receiptPdfUrl; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public LocalDate getContributionDate() { return contributionDate; }
    public void setContributionDate(LocalDate contributionDate) { this.contributionDate = contributionDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }

    public Long getMandalId() { return mandalId; }
    public void setMandalId(Long mandalId) { this.mandalId = mandalId; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public Integer getFloorNumber() { return floorNumber; }
    public void setFloorNumber(Integer floorNumber) { this.floorNumber = floorNumber; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
