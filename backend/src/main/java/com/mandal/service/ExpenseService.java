package com.mandal.service;

import com.mandal.dao.ExpenseDao;
import com.mandal.model.Expense;

import java.sql.SQLException;
import java.util.List;

public class ExpenseService {

    private final ExpenseDao expenseDao;

    public ExpenseService() {
        this.expenseDao = new ExpenseDao();
    }

    public List<Expense> getAllExpenses(Long mandalId) throws SQLException {
        return expenseDao.findAll(mandalId);
    }

    public Expense addExpense(Expense expense, Long userId, String role, Long mandalId) throws SQLException {
        if (!"ADMIN".equals(role) && !"KARYAKARTA".equals(role)) {
            throw new SecurityException("Only Admins and Karyakartas can add expenses");
        }

        if (expense.getAmount() == null || expense.getAmount().doubleValue() <= 0) {
            throw new IllegalArgumentException("Expense amount must be greater than 0");
        }
        if (expense.getItemName() == null || expense.getItemName().trim().isEmpty()) {
            throw new IllegalArgumentException("Item name is required");
        }
        if (expense.getExpenseDate() == null) {
            throw new IllegalArgumentException("Expense date is required");
        }

        expense.setCreatedBy(userId);
        if (expense.getPurchasedBy() == null) {
            expense.setPurchasedBy(userId); // Default to the person adding it
        }

        expense.setMandalId(mandalId);

        return expenseDao.insert(expense);
    }

    public Expense updateExpense(Long id, Expense req, Long userId, String role, Long mandalId) throws SQLException {
        Expense existing = expenseDao.findById(id, mandalId);
        if (existing == null) {
            throw new IllegalArgumentException("Expense not found");
        }

        if ("KARYAKARTA".equals(role)) {
            if (!userId.equals(existing.getCreatedBy())) {
                throw new SecurityException("You can only edit your own entries");
            }
        } else if (!"ADMIN".equals(role)) {
            throw new SecurityException("Only Admins and Karyakartas can edit expenses");
        }

        if (req.getItemName() != null) existing.setItemName(req.getItemName());
        if (req.getAmount() != null) existing.setAmount(req.getAmount());
        if (req.getVendorName() != null) existing.setVendorName(req.getVendorName());
        if (req.getExpenseDate() != null) existing.setExpenseDate(req.getExpenseDate());
        if (req.getReceiptPhotoUrl() != null) existing.setReceiptPhotoUrl(req.getReceiptPhotoUrl());
        if (req.getPurchasedByName() != null) existing.setPurchasedByName(req.getPurchasedByName());
        if (req.getPaymentMethod() != null) existing.setPaymentMethod(req.getPaymentMethod());

        return expenseDao.update(existing);
    }
}
