package com.mandal.dao;

import com.mandal.model.Expense;
import com.mandal.util.DbConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExpenseDao {

    public List<Expense> findAll(Long mandalId) throws SQLException {
        String sql = "SELECT e.*, c.name_en as category_name_en, c.name_mr as category_name_mr, u.name as purchased_by_name " +
                     "FROM expenses e " +
                     "LEFT JOIN expense_categories c ON e.category_id = c.id " +
                     "LEFT JOIN users u ON e.purchased_by = u.id " +
                     "WHERE e.mandal_id = ? " +
                     "ORDER BY e.expense_date DESC, e.id DESC";

        List<Expense> expenses = new ArrayList<>();
        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, mandalId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    expenses.add(mapRow(rs));
                }
            }
        }
        return expenses;
    }

    public Expense insert(Expense expense) throws SQLException {
        String sql = "INSERT INTO expenses (item_name, category_id, amount, purchased_by, vendor_name, item_photo_url, receipt_photo_url, expense_date, created_by, mandal_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING *";

        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, expense.getItemName());
            if (expense.getCategoryId() != null) {
                stmt.setLong(2, expense.getCategoryId());
            } else {
                stmt.setNull(2, Types.BIGINT);
            }
            stmt.setBigDecimal(3, expense.getAmount());
            if (expense.getPurchasedBy() != null) {
                stmt.setLong(4, expense.getPurchasedBy());
            } else {
                stmt.setNull(4, Types.BIGINT);
            }
            stmt.setString(5, expense.getVendorName());
            
            String packed = expense.getPurchasedByName() != null ? expense.getPurchasedByName() : "";
            if (expense.getPaymentMethod() != null) {
                packed += "||" + expense.getPaymentMethod();
            }
            stmt.setString(6, packed.isEmpty() ? null : packed);
            
            stmt.setString(7, expense.getReceiptPhotoUrl());
            stmt.setDate(8, Date.valueOf(expense.getExpenseDate()));
            if (expense.getCreatedBy() != null) {
                stmt.setLong(9, expense.getCreatedBy());
            } else {
                stmt.setNull(9, Types.BIGINT);
            }
            stmt.setLong(10, expense.getMandalId());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs); // Returns basic expense without joins. Sufficient for insert response.
                }
            }
        }
        return null;
    }

    private Expense mapRow(ResultSet rs) throws SQLException {
        Expense e = new Expense();
        e.setId(rs.getLong("id"));
        e.setMandalId(rs.getLong("mandal_id"));
        e.setItemName(rs.getString("item_name"));
        long categoryId = rs.getLong("category_id");
        e.setCategoryId(rs.wasNull() ? null : categoryId);
        
        try {
            e.setCategoryNameEn(rs.getString("category_name_en"));
            e.setCategoryNameMr(rs.getString("category_name_mr"));
            e.setPurchasedByName(rs.getString("purchased_by_name"));
        } catch (SQLException ignore) {
            // These columns might not exist if simple mapRow is used, ignore if missing.
        }

        e.setAmount(rs.getBigDecimal("amount"));
        long purchasedBy = rs.getLong("purchased_by");
        e.setPurchasedBy(rs.wasNull() ? null : purchasedBy);
        e.setVendorName(rs.getString("vendor_name"));
        
        String rawPacked = rs.getString("item_photo_url");
        if (rawPacked != null && !rawPacked.trim().isEmpty()) {
            if (rawPacked.contains("||")) {
                String[] parts = rawPacked.split("\\|\\|");
                e.setPurchasedByName(parts.length > 0 ? parts[0] : "");
                if (parts.length > 1) {
                    e.setPaymentMethod(parts[1]);
                }
            } else {
                e.setPurchasedByName(rawPacked);
            }
        }

        e.setReceiptPhotoUrl(rs.getString("receipt_photo_url"));
        e.setApprovalStatus(rs.getString("approval_status"));
        long approvedBy = rs.getLong("approved_by");
        e.setApprovedBy(rs.wasNull() ? null : approvedBy);
        e.setExpenseDate(rs.getDate("expense_date").toLocalDate());
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) e.setCreatedAt(createdAt.toLocalDateTime());
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) e.setUpdatedAt(updatedAt.toLocalDateTime());
        
        long createdBy = rs.getLong("created_by");
        e.setCreatedBy(rs.wasNull() ? null : createdBy);
        return e;
    }

    public Expense findById(Long id, Long mandalId) throws SQLException {
        String sql = "SELECT e.*, c.name_en as category_name_en, c.name_mr as category_name_mr, u.name as purchased_by_name " +
                     "FROM expenses e " +
                     "LEFT JOIN expense_categories c ON e.category_id = c.id " +
                     "LEFT JOIN users u ON e.purchased_by = u.id " +
                     "WHERE e.id = ? AND e.mandal_id = ?";
        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.setLong(2, mandalId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public Expense update(Expense expense) throws SQLException {
        String sql = "UPDATE expenses SET item_name = ?, amount = ?, vendor_name = ?, expense_date = ?, receipt_photo_url = ?, item_photo_url = ?, updated_at = now() WHERE id = ? AND mandal_id = ? RETURNING *";
        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, expense.getItemName());
            ps.setBigDecimal(2, expense.getAmount());
            ps.setString(3, expense.getVendorName());
            ps.setDate(4, Date.valueOf(expense.getExpenseDate()));
            ps.setString(5, expense.getReceiptPhotoUrl());
            
            String packed = expense.getPurchasedByName() != null ? expense.getPurchasedByName() : "";
            if (expense.getPaymentMethod() != null) {
                packed += "||" + expense.getPaymentMethod();
            }
            ps.setString(6, packed.isEmpty() ? null : packed);
            
            ps.setLong(7, expense.getId());
            ps.setLong(8, expense.getMandalId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }
}
