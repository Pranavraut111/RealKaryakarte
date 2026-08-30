package com.mandal.servlet;

import com.mandal.dao.MandalDao;
import com.mandal.dto.ApiResponse;
import com.mandal.model.Mandal;
import com.mandal.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

@WebServlet("/api/mandal")
public class MandalServlet extends HttpServlet {

    private final MandalDao mandalDao = new MandalDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Long mandalId = (Long) req.getAttribute("mandalId");
            if (mandalId == null) {
                JsonUtil.writeError(resp, 403, "Not associated with any mandal");
                return;
            }

            Mandal mandal = mandalDao.findById(mandalId);
            if (mandal == null) {
                JsonUtil.writeError(resp, 404, "Mandal not found");
                return;
            }

            // Hide invite code for non-admins if desired, or return full object
            String role = (String) req.getAttribute("userRole");
            if (!"ADMIN".equals(role)) {
                mandal.setInviteCode(null); // hide for regular members
            }

            JsonUtil.writeOk(resp, ApiResponse.ok(mandal));
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.writeError(resp, 500, e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Long mandalId = (Long) req.getAttribute("mandalId");
            String role = (String) req.getAttribute("userRole");

            if (!"ADMIN".equals(role)) {
                JsonUtil.writeError(resp, 403, "Only admins can modify mandal settings");
                return;
            }

            if (mandalId == null) {
                JsonUtil.writeError(resp, 403, "Not associated with any mandal");
                return;
            }

            @SuppressWarnings("unchecked")
            Map<String, String> body = JsonUtil.readBody(req, Map.class);
            String newName = body.get("mandalName");

            if (newName == null || newName.trim().isEmpty()) {
                JsonUtil.writeError(resp, 400, "Mandal name cannot be empty");
                return;
            }

            mandalDao.updateMandalName(mandalId, newName.trim());
            
            Mandal updated = mandalDao.findById(mandalId);
            JsonUtil.writeOk(resp, ApiResponse.ok("Mandal renamed successfully", updated));

        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.writeError(resp, 500, e.getMessage());
        }
    }
}
