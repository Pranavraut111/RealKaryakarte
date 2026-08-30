package com.mandal.servlet;

import com.mandal.dto.ApiResponse;
import com.mandal.model.DashboardSummary;
import com.mandal.service.DashboardService;
import com.mandal.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Dashboard controller — GET /api/dashboard/summary
 */
@WebServlet(urlPatterns = "/api/dashboard/*")
public class DashboardServlet extends HttpServlet {

    private final DashboardService service = new DashboardService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String pathInfo = req.getPathInfo();
            if (pathInfo == null || "/summary".equals(pathInfo)) {
                Long mandalId = (Long) req.getAttribute("mandalId");
                                Long userId = (Long) req.getAttribute("userId");
                String role = (String) req.getAttribute("role");
                DashboardSummary summary = service.getSummary(mandalId, userId, role);
                JsonUtil.writeOk(resp, ApiResponse.ok(summary));
            } else {
                JsonUtil.writeError(resp, 404, "Unknown dashboard endpoint");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.writeError(resp, 500, e.getMessage());
        }
    }
}
