package com.mandal.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request body for creating/updating a contribution.
 */
public class ContributionRequest {

    private Long memberId;
    private String memberName;
    private BigDecimal amount;
    private String paymentMethod;
    private String collectedByName;
    private String note;
    private LocalDate contributionDate;
    private String roomNumber;      // optional — links to society_rooms tracker
    private Integer floorNumber;    // optional — links to society_rooms tracker
    private String phone;           // optional — phone number of contributor

    public ContributionRequest() {}

    // ─── Getters & Setters ───────────────────────────────────────────────

    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }

    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getCollectedByName() { return collectedByName; }
    public void setCollectedByName(String collectedByName) { this.collectedByName = collectedByName; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public LocalDate getContributionDate() { return contributionDate; }
    public void setContributionDate(LocalDate contributionDate) { this.contributionDate = contributionDate; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public Integer getFloorNumber() { return floorNumber; }
    public void setFloorNumber(Integer floorNumber) { this.floorNumber = floorNumber; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
