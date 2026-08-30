package com.mandal.servlet;

import com.mandal.dto.ApiResponse;
import com.mandal.model.Complaint;
import com.mandal.service.ComplaintService;
import com.mandal.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet("/api/complaints/*")
public class ComplaintServlet extends HttpServlet {

    private ComplaintService service;

    @Override
    public void init() {
        this.service = new ComplaintService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Long mandalId = (Long) req.getAttribute("mandalId");
            String role = (String) req.getAttribute("userRole");
            
            List<Complaint> complaints = service.getComplaints(mandalId, role);
            JsonUtil.writeOk(resp, ApiResponse.ok("Complaints retrieved", complaints));
        } catch (SecurityException e) {
            JsonUtil.writeError(resp, 403, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.writeError(resp, 500, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Long mandalId = (Long) req.getAttribute("mandalId");
            
            @SuppressWarnings("unchecked")
            Map<String, String> body = JsonUtil.readBody(req, Map.class);
            String message = body.get("message");

            Complaint created = service.submitComplaint(message, mandalId);
            JsonUtil.writeOk(resp, ApiResponse.ok("Complaint submitted securely and anonymously", created));
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
            Long mandalId = (Long) req.getAttribute("mandalId");
            String role = (String) req.getAttribute("userRole");
            String pathInfo = req.getPathInfo();
            
            if (pathInfo != null && pathInfo.matches("/\\d+/resolve")) {
                Long id = Long.parseLong(pathInfo.split("/")[1]);
                service.resolveComplaint(id, mandalId, role);
                JsonUtil.writeOk(resp, ApiResponse.ok("Complaint marked as resolved", null));
            } else {
                JsonUtil.writeError(resp, 400, "Invalid endpoint");
            }
        } catch (SecurityException e) {
            JsonUtil.writeError(resp, 403, e.getMessage());
        } catch (IllegalArgumentException e) {
            JsonUtil.writeError(resp, 400, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.writeError(resp, 500, e.getMessage());
        }
    }
}
