package com.mandal.service;

import com.mandal.dao.ComplaintDao;
import com.mandal.model.Complaint;

import java.sql.SQLException;
import java.util.List;

public class ComplaintService {
    
    private final ComplaintDao dao;

    public ComplaintService() {
        this.dao = new ComplaintDao();
    }

    public Complaint submitComplaint(String message, Long mandalId) throws SQLException {
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Complaint message cannot be empty");
        }
        Complaint c = new Complaint();
        c.setMessage(message.trim());
        c.setMandalId(mandalId);
        return dao.insert(c);
    }

    public List<Complaint> getComplaints(Long mandalId, String role) throws SQLException {
        return dao.findAllForMandal(mandalId);
    }

    public void resolveComplaint(Long id, Long mandalId, String role) throws SQLException {
        if (!"ADMIN".equals(role) && !"KARYAKARTA".equals(role)) {
            throw new SecurityException("Only Admins and Karyakartas can resolve complaints");
        }
        boolean updated = dao.resolve(id, mandalId);
        if (!updated) {
            throw new IllegalArgumentException("Complaint not found");
        }
    }
}
