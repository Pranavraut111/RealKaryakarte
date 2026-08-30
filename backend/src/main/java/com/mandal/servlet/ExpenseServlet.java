package com.mandal.servlet;

import com.mandal.dto.ApiResponse;
import com.mandal.model.Expense;
import com.mandal.service.ExpenseService;
import com.mandal.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/api/expenses/*")
public class ExpenseServlet extends HttpServlet {

    private ExpenseService service;

    @Override
    public void init() {
        this.service = new ExpenseService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Long mandalId = (Long) req.getAttribute("mandalId");
            List<Expense> expenses = service.getAllExpenses(mandalId);
            JsonUtil.writeOk(resp, ApiResponse.ok("Expenses retrieved successfully", expenses));
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.writeError(resp, 500, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Long userId = (Long) req.getAttribute("userId");
            String userRole = (String) req.getAttribute("userRole");
            Long mandalId = (Long) req.getAttribute("mandalId");

            Expense body = JsonUtil.readBody(req, Expense.class);
            Expense created = service.addExpense(body, userId, userRole, mandalId);

            JsonUtil.writeOk(resp, ApiResponse.ok("Expense added successfully", created));
        } catch (SecurityException e) {
            JsonUtil.writeError(resp, 403, e.getMessage());
        } catch (IllegalArgumentException e) {
            JsonUtil.writeError(resp, 400, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.writeError(resp, 500, e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String pathInfo = req.getPathInfo();
            if (pathInfo == null || pathInfo.length() <= 1) {
                JsonUtil.writeError(resp, 400, "Expense ID required in path");
                return;
            }

            Long id = Long.parseLong(pathInfo.substring(1));
            Long userId = (Long) req.getAttribute("userId");
            String userRole = (String) req.getAttribute("userRole");
            Long mandalId = (Long) req.getAttribute("mandalId");

            Expense body = JsonUtil.readBody(req, Expense.class);
            Expense updated = service.updateExpense(id, body, userId, userRole, mandalId);

            JsonUtil.writeOk(resp, ApiResponse.ok("Expense updated successfully", updated));
        } catch (SecurityException e) {
            JsonUtil.writeError(resp, 403, e.getMessage());
        } catch (NumberFormatException e) {
            JsonUtil.writeError(resp, 400, "Invalid expense ID");
        } catch (IllegalArgumentException e) {
            JsonUtil.writeError(resp, 400, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.writeError(resp, 500, e.getMessage());
        }
    }
}
