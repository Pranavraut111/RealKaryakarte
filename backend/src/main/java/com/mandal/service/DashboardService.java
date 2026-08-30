package com.mandal.service;

import com.mandal.dao.DashboardDao;
import com.mandal.model.DashboardSummary;

import java.sql.SQLException;

/**
 * Dashboard service — delegates to DashboardDao for aggregate data.
 */
public class DashboardService {

    private final DashboardDao dashboardDao = new DashboardDao();

    public DashboardSummary getSummary(Long mandalId, Long userId, String role) throws SQLException {
        return dashboardDao.getSummary(mandalId, userId, role);
    }
}
