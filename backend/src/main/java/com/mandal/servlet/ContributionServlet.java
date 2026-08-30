package com.mandal.servlet;

import com.mandal.dto.ApiResponse;
import com.mandal.dto.ContributionRequest;
import com.mandal.model.Contribution;
import com.mandal.service.ContributionService;
import com.mandal.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

/**
 * Contribution (Vargani) controller — full CRUD.
 *
 * GET    /api/contributions           → list (with optional filters)
 * POST   /api/contributions           → add new
 * PUT    /api/contributions/{id}      → update
 * DELETE /api/contributions/{id}      → delete
 * GET    /api/contributions/{id}      → get single
 */
@WebServlet(urlPatterns = "/api/contributions/*")
public class ContributionServlet extends HttpServlet {

    private final ContributionService service = new ContributionService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String pathInfo = req.getPathInfo();

            // Single contribution by ID
            if (pathInfo != null && pathInfo.length() > 1) {
                String idStr = pathInfo.substring(1).split("/")[0];
                Long id = Long.parseLong(idStr);
                Long mandalId = (Long) req.getAttribute("mandalId");
                Contribution c = service.getById(id, mandalId);
                if (c == null) {
                    JsonUtil.writeError(resp, 404, "Contribution not found");
                    return;
                }
                JsonUtil.writeOk(resp, ApiResponse.ok(c));
                return;
            }

            // List with filters
            String fromStr = req.getParameter("from");
            String toStr = req.getParameter("to");
            String memberIdStr = req.getParameter("memberId");
            String method = req.getParameter("method");

            LocalDate from = fromStr != null ? LocalDate.parse(fromStr) : null;
            LocalDate to = toStr != null ? LocalDate.parse(toStr) : null;
            Long memberId = memberIdStr != null ? Long.parseLong(memberIdStr) : null;

            Long mandalId = (Long) req.getAttribute("mandalId");
            List<Contribution> list = service.getAll(mandalId, from, to, memberId, method);
            JsonUtil.writeOk(resp, ApiResponse.ok(list));

        } catch (NumberFormatException e) {
            JsonUtil.writeError(resp, 400, "Invalid ID format");
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

            ContributionRequest body = JsonUtil.readBody(req, ContributionRequest.class);
            Contribution saved = service.addContribution(body, userId, userRole, mandalId);

            JsonUtil.writeResponse(resp, 201, ApiResponse.ok("Contribution added", saved));
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
                JsonUtil.writeError(resp, 400, "Contribution ID required in path");
                return;
            }

            Long id = Long.parseLong(pathInfo.substring(1));
            Long userId = (Long) req.getAttribute("userId");
            String userRole = (String) req.getAttribute("userRole");
            Long mandalId = (Long) req.getAttribute("mandalId");

            ContributionRequest body = JsonUtil.readBody(req, ContributionRequest.class);
            Contribution updated = service.updateContribution(id, body, userId, userRole, mandalId);

            JsonUtil.writeOk(resp, ApiResponse.ok("Contribution updated", updated));
        } catch (SecurityException e) {
            JsonUtil.writeError(resp, 403, e.getMessage());
        } catch (NumberFormatException e) {
            JsonUtil.writeError(resp, 400, "Invalid contribution ID");
        } catch (IllegalArgumentException e) {
            JsonUtil.writeError(resp, 400, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.writeError(resp, 500, e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String pathInfo = req.getPathInfo();
            if (pathInfo == null || pathInfo.length() <= 1) {
                JsonUtil.writeError(resp, 400, "Contribution ID required in path");
                return;
            }

            Long id = Long.parseLong(pathInfo.substring(1));
            Long userId = (Long) req.getAttribute("userId");
            String userRole = (String) req.getAttribute("userRole");
            Long mandalId = (Long) req.getAttribute("mandalId");

            boolean deleted = service.deleteContribution(id, userId, userRole, mandalId);
            if (deleted) {
                JsonUtil.writeOk(resp, ApiResponse.ok("Contribution deleted", null));
            } else {
                JsonUtil.writeError(resp, 404, "Contribution not found");
            }
        } catch (SecurityException e) {
            JsonUtil.writeError(resp, 403, e.getMessage());
        } catch (NumberFormatException e) {
            JsonUtil.writeError(resp, 400, "Invalid contribution ID");
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.writeError(resp, 500, e.getMessage());
        }
    }
}
