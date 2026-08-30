package com.mandal.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Expense {
    private Long id;
    private String itemName;
    private Long categoryId;
    private String categoryNameEn;
    private String categoryNameMr;
    private BigDecimal amount;
    private Long purchasedBy;
    private String purchasedByName;
    private String vendorName;
    private String itemPhotoUrl;
    private String receiptPhotoUrl;
    private String approvalStatus;
    private Long approvedBy;
    private String paymentMethod;
    private LocalDate expenseDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long mandalId;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public String getCategoryNameEn() { return categoryNameEn; }
    public void setCategoryNameEn(String categoryNameEn) { this.categoryNameEn = categoryNameEn; }

    public String getCategoryNameMr() { return categoryNameMr; }
    public void setCategoryNameMr(String categoryNameMr) { this.categoryNameMr = categoryNameMr; }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
    public Long getPurchasedBy() { return purchasedBy; }
    public void setPurchasedBy(Long purchasedBy) { this.purchasedBy = purchasedBy; }
    
    public String getPurchasedByName() { return purchasedByName; }
    public void setPurchasedByName(String purchasedByName) { this.purchasedByName = purchasedByName; }
    
    public String getVendorName() { return vendorName; }
    public void setVendorName(String vendorName) { this.vendorName = vendorName; }
    
    public String getItemPhotoUrl() { return itemPhotoUrl; }
    public void setItemPhotoUrl(String itemPhotoUrl) { this.itemPhotoUrl = itemPhotoUrl; }
    
    public String getReceiptPhotoUrl() { return receiptPhotoUrl; }
    public void setReceiptPhotoUrl(String receiptPhotoUrl) { this.receiptPhotoUrl = receiptPhotoUrl; }
    
    public String getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }
    
    public Long getApprovedBy() { return approvedBy; }
    public void setApprovedBy(Long approvedBy) { this.approvedBy = approvedBy; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    
    public LocalDate getExpenseDate() { return expenseDate; }
    public void setExpenseDate(LocalDate expenseDate) { this.expenseDate = expenseDate; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }

    public Long getMandalId() { return mandalId; }
    public void setMandalId(Long mandalId) { this.mandalId = mandalId; }
}
