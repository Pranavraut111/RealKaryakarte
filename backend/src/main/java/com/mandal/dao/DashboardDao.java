package com.mandal.dao;

import com.mandal.model.DashboardSummary;
import com.mandal.util.DbConnectionManager;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access for dashboard aggregates — combines data from
 * contributions, expenses, and users tables.
 */
public class DashboardDao {

    /**
     * Fetch the full dashboard summary in a single method.
     */
    public DashboardSummary getSummary(Long mandalId, Long userId, String role) throws SQLException {
        DashboardSummary summary = new DashboardSummary();

        try (Connection conn = DbConnectionManager.getConnection()) {

            // ── Total collected ─────────────────────────────────────────
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COALESCE(SUM(amount), 0) FROM contributions WHERE mandal_id = ?")) {
                ps.setLong(1, mandalId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) summary.setTotalCollected(rs.getBigDecimal(1));
                }
            }

            // ── Total spent ─────────────────────────────────────────────
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE approval_status = 'APPROVED' AND mandal_id = ?")) {
                ps.setLong(1, mandalId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) summary.setTotalSpent(rs.getBigDecimal(1));
                }
            }

            // ── Balance ─────────────────────────────────────────────────
            BigDecimal collected = summary.getTotalCollected() != null
                    ? summary.getTotalCollected() : BigDecimal.ZERO;
            BigDecimal spent = summary.getTotalSpent() != null
                    ? summary.getTotalSpent() : BigDecimal.ZERO;
            summary.setBalance(collected.subtract(spent));

            // ── Total members ───────────────────────────────────────────
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) FROM users WHERE is_active = TRUE AND mandal_id = ?")) {
                ps.setLong(1, mandalId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) summary.setTotalMembers(rs.getInt(1));
                }
            }

            // ── Total contributions ─────────────────────────────────────
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) FROM contributions WHERE mandal_id = ?")) {
                ps.setLong(1, mandalId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) summary.setTotalContributions(rs.getInt(1));
                }
            }

            // 6. Expense Analytics (Group by Category)
            String analyticsSql = 
                "SELECT COALESCE(c.name_en, 'Other') as name_en, " +
                "COALESCE(c.name_mr, 'इतर') as name_mr, " +
                "SUM(e.amount) as total " +
                "FROM expenses e " +
                "LEFT JOIN expense_categories c ON e.category_id = c.id " +
                "WHERE e.mandal_id = ? AND e.approval_status = 'APPROVED' " +
                "GROUP BY c.name_en, c.name_mr " +
                "ORDER BY total DESC";
            
            List<DashboardSummary.ExpenseCategorySum> analytics = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(analyticsSql)) {
                ps.setLong(1, mandalId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        analytics.add(new DashboardSummary.ExpenseCategorySum(
                                rs.getString("name_en"),
                                rs.getString("name_mr"),
                                rs.getBigDecimal("total")
                        ));
                    }
                }
            summary.setExpenseAnalytics(analytics);

            // 7. Recent activity (last 20 items, mixed contributions + expenses)
            String activitySql = """
                (
                    SELECT 'CONTRIBUTION' AS type,
                           CONCAT(c.member_name, ' contributed') AS description,
                           c.amount,
                           c.created_at AS ts,
                           u.name AS user_name
                    FROM contributions c
                    LEFT JOIN users u ON u.id = c.collected_by
                    WHERE c.mandal_id = ?
                    ORDER BY c.created_at DESC
                    LIMIT 10
                )
                UNION ALL
                (
                    SELECT 'EXPENSE' AS type,
                           CONCAT('Expense: ', e.item_name) AS description,
                           e.amount,
                           e.created_at AS ts,
                           u.name AS user_name
                    FROM expenses e
                    LEFT JOIN users u ON u.id = e.purchased_by
                    WHERE e.approval_status = 'APPROVED' AND e.mandal_id = ?
                    ORDER BY e.created_at DESC
                    LIMIT 10
                )
                ORDER BY ts DESC
                LIMIT 20
                """;

            List<DashboardSummary.RecentActivity> activities = new ArrayList<>();
            try (PreparedStatement psActivity = conn.prepareStatement(activitySql)) {
                psActivity.setLong(1, mandalId);
                psActivity.setLong(2, mandalId);
                
                try (ResultSet rs = psActivity.executeQuery()) {
                    while (rs.next()) {
                        activities.add(new DashboardSummary.RecentActivity(
                                rs.getString("type"),
                                rs.getString("description"),
                                rs.getBigDecimal("amount"),
                                rs.getTimestamp("ts") != null
                                        ? rs.getTimestamp("ts").toLocalDateTime().toString() : null,
                                rs.getString("user_name")
                        ));
                    }
                }
            }
            summary.setRecentActivity(activities);
        }

        return summary;
    }
}
    }
