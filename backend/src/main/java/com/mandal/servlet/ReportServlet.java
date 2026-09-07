package com.mandal.servlet;

import com.mandal.dao.ContributionDao;
import com.mandal.dao.ExpenseDao;
import com.mandal.dao.SocietyRoomDao;
import com.mandal.model.Contribution;
import com.mandal.model.Expense;
import com.mandal.model.SocietyRoom;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/api/reports/export")
public class ReportServlet extends HttpServlet {

    private ContributionDao contributionDao;
    private ExpenseDao expenseDao;
    private SocietyRoomDao roomDao;

    @Override
    public void init() {
        this.contributionDao = new ContributionDao();
        this.expenseDao = new ExpenseDao();
        this.roomDao = new SocietyRoomDao();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String role = (String) req.getAttribute("userRole");
            if (!"ADMIN".equals(role) && !"KARYAKARTA".equals(role) && !"MEMBER".equals(role)) {
                resp.sendError(403, "You do not have permission to export reports.");
                return;
            }

            resp.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            resp.setHeader("Content-Disposition", "attachment; filename=\"mandal_report.xlsx\"");

            Long mandalId = (Long) req.getAttribute("mandalId");

            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet contribSheet = workbook.createSheet("Contributions");
                createContributionsSheet(workbook, contribSheet, mandalId);

                Sheet expenseSheet = workbook.createSheet("Expenses");
                createExpensesSheet(workbook, expenseSheet, mandalId);

                Sheet roomSheet = workbook.createSheet("Room Tracker");
                createRoomTrackerSheet(workbook, roomSheet, mandalId);

                workbook.write(resp.getOutputStream());
            }

        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(500, "Error generating report: " + e.getMessage());
        }
    }

    private void createContributionsSheet(Workbook workbook, Sheet sheet, Long mandalId) throws SQLException {
        List<Contribution> contributions = contributionDao.findAll(mandalId, null, null, null, null);
        java.util.Collections.reverse(contributions);

        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dateStyle = createDateStyle(workbook, false);
        CellStyle currencyStyle = createCurrencyStyle(workbook, false);
        
        CellStyle evenRowStyle = createEvenRowStyle(workbook);
        CellStyle evenDateStyle = createDateStyle(workbook, true);
        CellStyle evenCurrencyStyle = createCurrencyStyle(workbook, true);

        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Receipt No", "Member Name", "Amount (₹)", "Date", "Payment Method", "Collected By"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        for (Contribution c : contributions) {
            boolean isEven = (rowNum % 2 == 0);
            Row row = sheet.createRow(rowNum++);
            CellStyle currentStyle = isEven ? evenRowStyle : workbook.createCellStyle();
            CellStyle currentDateStyle = isEven ? evenDateStyle : dateStyle;
            CellStyle currentCurrencyStyle = isEven ? evenCurrencyStyle : currencyStyle;

            Cell idCell = row.createCell(0);
            idCell.setCellValue(c.getId());
            idCell.setCellStyle(headerStyle); // ID column gets header style

            Cell rCell = row.createCell(1);
            rCell.setCellValue(c.getReceiptNo());
            if (isEven) rCell.setCellStyle(currentStyle);

            Cell mCell = row.createCell(2);
            mCell.setCellValue(c.getMemberName());
            if (isEven) mCell.setCellStyle(currentStyle);
            
            Cell amtCell = row.createCell(3);
            amtCell.setCellValue(c.getAmount().doubleValue());
            amtCell.setCellStyle(currentCurrencyStyle);
            
            Cell dateCell = row.createCell(4);
            if (c.getContributionDate() != null) {
                dateCell.setCellValue(c.getContributionDate());
                dateCell.setCellStyle(currentDateStyle);
            } else if (isEven) {
                dateCell.setCellStyle(currentStyle);
            }
            
            Cell pCell = row.createCell(5);
            pCell.setCellValue(c.getPaymentMethod() != null ? c.getPaymentMethod().name() : "");
            if (isEven) pCell.setCellStyle(currentStyle);

            Cell cbCell = row.createCell(6);
            cbCell.setCellValue(c.getCollectedByName() != null ? c.getCollectedByName() : "");
            if (isEven) cbCell.setCellStyle(currentStyle);
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createExpensesSheet(Workbook workbook, Sheet sheet, Long mandalId) throws SQLException {
        List<Expense> expenses = expenseDao.findAll(mandalId);
        java.util.Collections.reverse(expenses);

        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dateStyle = createDateStyle(workbook, false);
        CellStyle currencyStyle = createCurrencyStyle(workbook, false);

        CellStyle evenRowStyle = createEvenRowStyle(workbook);
        CellStyle evenDateStyle = createDateStyle(workbook, true);
        CellStyle evenCurrencyStyle = createCurrencyStyle(workbook, true);

        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Item Name", "Amount (₹)", "Date", "Purchased By"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        for (Expense e : expenses) {
            boolean isEven = (rowNum % 2 == 0);
            Row row = sheet.createRow(rowNum++);
            CellStyle currentStyle = isEven ? evenRowStyle : workbook.createCellStyle();
            CellStyle currentDateStyle = isEven ? evenDateStyle : dateStyle;
            CellStyle currentCurrencyStyle = isEven ? evenCurrencyStyle : currencyStyle;

            Cell idCell = row.createCell(0);
            idCell.setCellValue(e.getId());
            idCell.setCellStyle(headerStyle); // ID column gets header style

            Cell iCell = row.createCell(1);
            iCell.setCellValue(e.getItemName());
            if (isEven) iCell.setCellStyle(currentStyle);
            
            Cell amtCell = row.createCell(2);
            amtCell.setCellValue(e.getAmount().doubleValue());
            amtCell.setCellStyle(currentCurrencyStyle);
            
            Cell dateCell = row.createCell(3);
            if (e.getExpenseDate() != null) {
                dateCell.setCellValue(e.getExpenseDate());
                dateCell.setCellStyle(currentDateStyle);
            } else if (isEven) {
                dateCell.setCellStyle(currentStyle);
            }
            
            Cell pbCell = row.createCell(4);
            pbCell.setCellValue(e.getPurchasedByName() != null ? e.getPurchasedByName() : "");
            if (isEven) pbCell.setCellStyle(currentStyle);
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        // Using a color matching Numbers' default blue
        style.setFillForegroundColor(IndexedColors.CORNFLOWER_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createEvenRowStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle createDateStyle(Workbook workbook, boolean isEven) {
        CellStyle style = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
        if (isEven) {
            style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }
        return style;
    }

    private CellStyle createCurrencyStyle(Workbook workbook, boolean isEven) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        // Remove currency symbol to prevent warnings in Numbers, rely on header
        style.setDataFormat(format.getFormat("#,##0.00"));
        if (isEven) {
            style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }
        return style;
    }

    private void createRoomTrackerSheet(Workbook workbook, Sheet sheet, Long mandalId) throws java.sql.SQLException {
        // This sheet is now the OWNER sheet — rename it
        workbook.setSheetName(workbook.getSheetIndex(sheet), "घरमालक (Owner)");
        List<SocietyRoom> allRooms = roomDao.findAll(mandalId, null, null);

        // Separate owners and renters
        List<SocietyRoom> owners = new java.util.ArrayList<>();
        List<SocietyRoom> renters = new java.util.ArrayList<>();
        for (SocietyRoom r : allRooms) {
            if ("OWNER".equals(r.getResidentType()) || r.getFloorNumber() == 0) {
                owners.add(r);
            } else {
                renters.add(r);
            }
        }

        // ── OWNER SHEET ─────────────────────────────────────────────────
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle currencyStyle = createCurrencyStyle(workbook, false);
        CellStyle titleStyle = workbook.createCellStyle();
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 14);
        titleStyle.setFont(titleFont);

        CellStyle subtitleStyle = workbook.createCellStyle();
        Font subtitleFont = workbook.createFont();
        subtitleFont.setBold(true);
        subtitleFont.setFontHeightInPoints((short) 11);
        subtitleStyle.setFont(subtitleFont);

        CellStyle paidStyle = workbook.createCellStyle();
        paidStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        paidStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        CellStyle pendingStyle = workbook.createCellStyle();
        pendingStyle.setFillForegroundColor(IndexedColors.ROSE.getIndex());
        pendingStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // Title rows
        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("जमा झालेली वर्गणी (घरमालक)");
        titleCell.setCellStyle(titleStyle);

        // Header row
        Row hdrRow = sheet.createRow(2);
        String[] ownerHeaders = {"रूम नंबर", "नाव", "रक्कम", "पेमेंट माध्यम", "टिपणी"};
        for (int i = 0; i < ownerHeaders.length; i++) {
            Cell c = hdrRow.createCell(i);
            c.setCellValue(ownerHeaders[i]);
            c.setCellStyle(headerStyle);
        }

        // Data rows
        owners.sort((a, b) -> {
            try { return Integer.parseInt(a.getRoomNumber()) - Integer.parseInt(b.getRoomNumber()); }
            catch (NumberFormatException e) { return a.getRoomNumber().compareTo(b.getRoomNumber()); }
        });

        int rowNum = 3;
        double ownerCashTotal = 0, ownerOnlineTotal = 0;
        for (SocietyRoom r : owners) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(r.getRoomNumber());
            row.createCell(1).setCellValue(r.getResidentName() != null ? r.getResidentName() : "");

            Cell amtCell = row.createCell(2);
            double amt = r.getAmountPaid() != null ? r.getAmountPaid().doubleValue() : 0;
            if (amt > 0) {
                amtCell.setCellValue(amt);
                amtCell.setCellStyle(currencyStyle);
            }

            String pm = r.getPaymentMethod();
            row.createCell(3).setCellValue(pm != null ? ("CASH".equals(pm) ? "Cash" : "Online") : "");
            row.createCell(4).setCellValue(r.getNotes() != null ? r.getNotes() : "");

            // Color coding
            if ("PAID".equals(r.getVarganiStatus())) {
                row.getCell(0).setCellStyle(paidStyle);
            } else if ("PENDING".equals(r.getVarganiStatus())) {
                row.getCell(0).setCellStyle(pendingStyle);
            }

            if ("CASH".equals(pm)) ownerCashTotal += amt;
            else if (pm != null) ownerOnlineTotal += amt;
        }

        // Summary row
        Row sumRow = sheet.createRow(rowNum + 1);
        sumRow.createCell(1).setCellValue("एकूण");
        Cell sumCell = sumRow.createCell(2);
        sumCell.setCellValue(ownerCashTotal + ownerOnlineTotal);
        sumCell.setCellStyle(currencyStyle);
        sumRow.getCell(1).setCellStyle(subtitleStyle);

        for (int i = 0; i < ownerHeaders.length; i++) sheet.autoSizeColumn(i);

        // ── RENTAL SHEET ────────────────────────────────────────────────
        Sheet rentalSheet = workbook.createSheet("भाडेकरू (Rental)");

        // Group renters by floor
        java.util.Map<Integer, List<SocietyRoom>> byFloor = new java.util.TreeMap<>();
        for (SocietyRoom r : renters) {
            byFloor.computeIfAbsent(r.getFloorNumber(), k -> new java.util.ArrayList<>()).add(r);
        }

        // Get all unique room numbers for row alignment
        java.util.Set<String> roomNums = new java.util.TreeSet<>((a, b) -> {
            try { return Integer.parseInt(a) - Integer.parseInt(b); }
            catch (NumberFormatException e) { return a.compareTo(b); }
        });
        for (SocietyRoom r : renters) roomNums.add(r.getRoomNumber());
        List<String> sortedRooms = new java.util.ArrayList<>(roomNums);

        // Title
        Row rTitleRow = rentalSheet.createRow(0);
        Cell rTitleCell = rTitleRow.createCell(0);
        rTitleCell.setCellValue("जमा झालेली वर्गणी (भाडेकरू)");
        rTitleCell.setCellStyle(titleStyle);

        // Floor headers (row 2)
        List<Integer> floors = new java.util.ArrayList<>(byFloor.keySet());
        String[] floorNames = {"तळ मजला A", "पहिला मजला B", "दुसरा मजला C", "तिसरा मजला D"};
        Row floorHdrRow = rentalSheet.createRow(2);
        for (int fi = 0; fi < floors.size(); fi++) {
            int col = 1 + fi * 3;
            int floor = floors.get(fi);
            String label = floor < floorNames.length ? floorNames[floor] : "मजला " + floor;
            Cell c = floorHdrRow.createCell(col);
            c.setCellValue(label);
            c.setCellStyle(subtitleStyle);
        }

        // Column headers (row 3)
        Row colHdrRow = rentalSheet.createRow(3);
        Cell roomHdrCell = colHdrRow.createCell(0);
        roomHdrCell.setCellValue("रूम नंबर");
        roomHdrCell.setCellStyle(headerStyle);

        for (int fi = 0; fi < floors.size(); fi++) {
            int base = 1 + fi * 3;
            String[] subHeaders = {"नाव", "रक्कम", "पेमेंट माध्यम"};
            for (int si = 0; si < subHeaders.length; si++) {
                Cell c = colHdrRow.createCell(base + si);
                c.setCellValue(subHeaders[si]);
                c.setCellStyle(headerStyle);
            }
        }

        // Data rows — one per room number, floors side by side
        int dataStart = 4;
        double[] floorTotals = new double[floors.size()];

        for (int ri = 0; ri < sortedRooms.size(); ri++) {
            String rn = sortedRooms.get(ri);
            Row row = rentalSheet.createRow(dataStart + ri);
            row.createCell(0).setCellValue(rn);

            for (int fi = 0; fi < floors.size(); fi++) {
                int base = 1 + fi * 3;
                int floor = floors.get(fi);
                // Find room for this floor
                SocietyRoom match = null;
                List<SocietyRoom> floorList = byFloor.get(floor);
                if (floorList != null) {
                    for (SocietyRoom r : floorList) {
                        if (rn.equals(r.getRoomNumber())) { match = r; break; }
                    }
                }

                if (match != null && match.getResidentName() != null && !match.getResidentName().isBlank()) {
                    row.createCell(base).setCellValue(match.getResidentName());
                    double amt = match.getAmountPaid() != null ? match.getAmountPaid().doubleValue() : 0;
                    if (amt > 0) {
                        Cell ac = row.createCell(base + 1);
                        ac.setCellValue(amt);
                        ac.setCellStyle(currencyStyle);
                        floorTotals[fi] += amt;
                    }
                    String pm = match.getPaymentMethod();
                    row.createCell(base + 2).setCellValue(pm != null ? ("CASH".equals(pm) ? "Cash" : "Online") : "");
                } else {
                    row.createCell(base).setCellValue(match != null ? "NA" : "");
                }
            }
        }

        // Summary rows
        int sumStart = dataStart + sortedRooms.size() + 1;
        Row rSumRow = rentalSheet.createRow(sumStart);
        rSumRow.createCell(0).setCellValue("एकूण");
        rSumRow.getCell(0).setCellStyle(subtitleStyle);
        for (int fi = 0; fi < floors.size(); fi++) {
            Cell c = rSumRow.createCell(2 + fi * 3);
            c.setCellValue(floorTotals[fi]);
            c.setCellStyle(currencyStyle);
        }

        // Grand total
        Row grandRow = rentalSheet.createRow(sumStart + 1);
        grandRow.createCell(0).setCellValue("एकूण भाडेकरू");
        grandRow.getCell(0).setCellStyle(subtitleStyle);
        double grandTotal = 0;
        for (double ft : floorTotals) grandTotal += ft;
        Cell gtCell = grandRow.createCell(2);
        gtCell.setCellValue(grandTotal);
        gtCell.setCellStyle(currencyStyle);

        // Auto-size columns
        int totalCols = 1 + floors.size() * 3;
        for (int i = 0; i < totalCols; i++) rentalSheet.autoSizeColumn(i);
    }
}
