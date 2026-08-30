package com.mandal.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * Dashboard summary — aggregated view returned by /api/dashboard/summary.
 */
public class DashboardSummary {

    private BigDecimal totalCollected;
    private BigDecimal totalSpent;
    private BigDecimal balance;
    private int totalMembers;
    private int totalContributions;
    private List<ExpenseCategorySum> expenseAnalytics;
    private List<RecentActivity> recentActivity;

    public DashboardSummary() {}

    // ─── Getters & Setters ───────────────────────────────────────────────

    public BigDecimal getTotalCollected() { return totalCollected; }
    public void setTotalCollected(BigDecimal totalCollected) { this.totalCollected = totalCollected; }

    public BigDecimal getTotalSpent() { return totalSpent; }
    public void setTotalSpent(BigDecimal totalSpent) { this.totalSpent = totalSpent; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public int getTotalMembers() { return totalMembers; }
    public void setTotalMembers(int totalMembers) { this.totalMembers = totalMembers; }

    public int getTotalContributions() { return totalContributions; }
    public void setTotalContributions(int totalContributions) { this.totalContributions = totalContributions; }

    public List<ExpenseCategorySum> getExpenseAnalytics() { return expenseAnalytics; }
    public void setExpenseAnalytics(List<ExpenseCategorySum> expenseAnalytics) { this.expenseAnalytics = expenseAnalytics; }

    public List<RecentActivity> getRecentActivity() { return recentActivity; }
    public void setRecentActivity(List<RecentActivity> recentActivity) { this.recentActivity = recentActivity; }

    /**
     * A single item in the recent activity feed.
     */
    public static class RecentActivity {
        private String type;       // "CONTRIBUTION" or "EXPENSE"
        private String description;
        private BigDecimal amount;
        private String timestamp;
        private String userName;

        public RecentActivity() {}

        public RecentActivity(String type, String description, BigDecimal amount, String timestamp, String userName) {
            this.type = type;
            this.description = description;
            this.amount = amount;
            this.timestamp = timestamp;
            this.userName = userName;
        }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }

        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }
    }

    /**
     * Expense analytics by category.
     */
    public static class ExpenseCategorySum {
        private String categoryNameEn;
        private String categoryNameMr;
        private BigDecimal totalAmount;

        public ExpenseCategorySum() {}

        public ExpenseCategorySum(String categoryNameEn, String categoryNameMr, BigDecimal totalAmount) {
            this.categoryNameEn = categoryNameEn;
            this.categoryNameMr = categoryNameMr;
            this.totalAmount = totalAmount;
        }

        public String getCategoryNameEn() { return categoryNameEn; }
        public void setCategoryNameEn(String categoryNameEn) { this.categoryNameEn = categoryNameEn; }

        public String getCategoryNameMr() { return categoryNameMr; }
        public void setCategoryNameMr(String categoryNameMr) { this.categoryNameMr = categoryNameMr; }

        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    }
}
