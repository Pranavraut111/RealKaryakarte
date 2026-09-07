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
                // Create data sheets first to compute totals
                Sheet contribSheet = workbook.createSheet("वर्गणी (Contributions)");
                double totalVargani = createContributionsSheet(workbook, contribSheet, mandalId);

                Sheet expenseSheet = workbook.createSheet("खर्च (Expenses)");
                double totalKharch = createExpensesSheet(workbook, expenseSheet, mandalId);

                Sheet roomSheet = workbook.createSheet("Room Tracker");
                double[] roomTotals = createRoomTrackerSheet(workbook, roomSheet, mandalId);
                // roomTotals = [ownerCollected, renterCollected]

                // Create summary sheet FIRST (move to index 0)
                Sheet summarySheet = workbook.createSheet("जमा खर्च");
                workbook.setSheetOrder("जमा खर्च", 0);
                createSummarySheet(workbook, summarySheet, totalVargani, totalKharch, roomTotals[0], roomTotals[1]);

                workbook.write(resp.getOutputStream());
            }

        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(500, "Error generating report: " + e.getMessage());
        }
    }

    private double createContributionsSheet(Workbook workbook, Sheet sheet, Long mandalId) throws SQLException {
        List<Contribution> contributions = contributionDao.findAll(mandalId, null, null, null, null);
        java.util.Collections.reverse(contributions);

        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dateStyle = createDateStyle(workbook, false);
        CellStyle currencyStyle = createCurrencyStyle(workbook, false);
        
        CellStyle evenRowStyle = createEvenRowStyle(workbook);
        CellStyle evenDateStyle = createDateStyle(workbook, true);
        CellStyle evenCurrencyStyle = createCurrencyStyle(workbook, true);

        Row headerRow = sheet.createRow(0);
        String[] headers = {"अ.क्र.", "पावती क्र.", "नाव", "रक्कम (₹)", "दिनांक", "पेमेंट माध्यम", "जमा करणारे"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        double total = 0;
        int rowNum = 1;
        for (Contribution c : contributions) {
            boolean isEven = (rowNum % 2 == 0);
            Row row = sheet.createRow(rowNum++);
            CellStyle currentStyle = isEven ? evenRowStyle : workbook.createCellStyle();
            CellStyle currentDateStyle = isEven ? evenDateStyle : dateStyle;
            CellStyle currentCurrencyStyle = isEven ? evenCurrencyStyle : currencyStyle;

            Cell idCell = row.createCell(0);
            idCell.setCellValue(c.getId());
            idCell.setCellStyle(headerStyle);

            Cell rCell = row.createCell(1);
            rCell.setCellValue(c.getReceiptNo());
            if (isEven) rCell.setCellStyle(currentStyle);

            Cell mCell = row.createCell(2);
            mCell.setCellValue(c.getMemberName());
            if (isEven) mCell.setCellStyle(currentStyle);
            
            Cell amtCell = row.createCell(3);
            double amt = c.getAmount().doubleValue();
            amtCell.setCellValue(amt);
            amtCell.setCellStyle(currentCurrencyStyle);
            total += amt;
            
            Cell dateCell = row.createCell(4);
            if (c.getContributionDate() != null) {
                dateCell.setCellValue(c.getContributionDate());
                dateCell.setCellStyle(currentDateStyle);
            } else if (isEven) {
                dateCell.setCellStyle(currentStyle);
            }
            
            Cell pCell = row.createCell(5);
            String pm = c.getPaymentMethod() != null ? c.getPaymentMethod().name() : "";
            pCell.setCellValue("CASH".equals(pm) ? "रोख" : "UPI".equals(pm) ? "UPI" : "BANK_TRANSFER".equals(pm) ? "बँक ट्रान्सफर" : pm);
            if (isEven) pCell.setCellStyle(currentStyle);

            Cell cbCell = row.createCell(6);
            cbCell.setCellValue(c.getCollectedByName() != null ? c.getCollectedByName() : "");
            if (isEven) cbCell.setCellStyle(currentStyle);
        }

        // Total row
        CellStyle totalStyle = createTotalStyle(workbook);
        CellStyle totalCurrencyStyle = createTotalCurrencyStyle(workbook);
        Row totalRow = sheet.createRow(rowNum + 1);
        Cell tLabel = totalRow.createCell(2);
        tLabel.setCellValue("एकूण वर्गणी");
        tLabel.setCellStyle(totalStyle);
        Cell tVal = totalRow.createCell(3);
        tVal.setCellValue(total);
        tVal.setCellStyle(totalCurrencyStyle);

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
        return total;
    }

    private double createExpensesSheet(Workbook workbook, Sheet sheet, Long mandalId) throws SQLException {
        List<Expense> expenses = expenseDao.findAll(mandalId);
        java.util.Collections.reverse(expenses);

        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dateStyle = createDateStyle(workbook, false);
        CellStyle currencyStyle = createCurrencyStyle(workbook, false);

        CellStyle evenRowStyle = createEvenRowStyle(workbook);
        CellStyle evenDateStyle = createDateStyle(workbook, true);
        CellStyle evenCurrencyStyle = createCurrencyStyle(workbook, true);

        Row headerRow = sheet.createRow(0);
        String[] headers = {"अ.क्र.", "वस्तूचे नाव", "रक्कम (₹)", "दिनांक", "खरेदी करणारे"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        double total = 0;
        int rowNum = 1;
        for (Expense e : expenses) {
            boolean isEven = (rowNum % 2 == 0);
            Row row = sheet.createRow(rowNum++);
            CellStyle currentStyle = isEven ? evenRowStyle : workbook.createCellStyle();
            CellStyle currentDateStyle = isEven ? evenDateStyle : dateStyle;
            CellStyle currentCurrencyStyle = isEven ? evenCurrencyStyle : currencyStyle;

            Cell idCell = row.createCell(0);
            idCell.setCellValue(e.getId());
            idCell.setCellStyle(headerStyle);

            Cell iCell = row.createCell(1);
            iCell.setCellValue(e.getItemName());
            if (isEven) iCell.setCellStyle(currentStyle);
            
            Cell amtCell = row.createCell(2);
            double amt = e.getAmount().doubleValue();
            amtCell.setCellValue(amt);
            amtCell.setCellStyle(currentCurrencyStyle);
            total += amt;
            
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

        // Total row
        CellStyle totalStyle = createTotalStyle(workbook);
        CellStyle totalCurrencyStyle = createTotalCurrencyStyle(workbook);
        Row totalRow = sheet.createRow(rowNum + 1);
        Cell tLabel = totalRow.createCell(1);
        tLabel.setCellValue("एकूण खर्च");
        tLabel.setCellStyle(totalStyle);
        Cell tVal = totalRow.createCell(2);
        tVal.setCellValue(total);
        tVal.setCellStyle(totalCurrencyStyle);

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
        return total;
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

    private double[] createRoomTrackerSheet(Workbook workbook, Sheet sheet, Long mandalId) throws java.sql.SQLException {
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

        return new double[]{ownerCashTotal + ownerOnlineTotal, grandTotal};
    }

    // ── Summary Sheet (जमा खर्च) ────────────────────────────────────────────
    private void createSummarySheet(Workbook workbook, Sheet sheet, double totalVargani, double totalKharch,
                                     double ownerCollected, double renterCollected) {
        CellStyle titleStyle = workbook.createCellStyle();
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 16);
        titleStyle.setFont(titleFont);

        CellStyle labelStyle = workbook.createCellStyle();
        Font labelFont = workbook.createFont();
        labelFont.setBold(true);
        labelFont.setFontHeightInPoints((short) 11);
        labelStyle.setFont(labelFont);

        CellStyle currencyStyle = workbook.createCellStyle();
        DataFormat fmt = workbook.createDataFormat();
        currencyStyle.setDataFormat(fmt.getFormat("#,##0.00"));
        Font cFont = workbook.createFont();
        cFont.setBold(true);
        cFont.setFontHeightInPoints((short) 11);
        currencyStyle.setFont(cFont);

        CellStyle totalLabelStyle = workbook.createCellStyle();
        Font tlFont = workbook.createFont();
        tlFont.setBold(true);
        tlFont.setFontHeightInPoints((short) 13);
        totalLabelStyle.setFont(tlFont);

        CellStyle totalValueStyle = workbook.createCellStyle();
        totalValueStyle.setDataFormat(fmt.getFormat("#,##0.00"));
        Font tvFont = workbook.createFont();
        tvFont.setBold(true);
        tvFont.setFontHeightInPoints((short) 13);
        totalValueStyle.setFont(tvFont);

        // Title
        Row r0 = sheet.createRow(0);
        Cell t = r0.createCell(0);
        t.setCellValue("जमा खर्च");
        t.setCellStyle(titleStyle);

        // Headers
        Row r2 = sheet.createRow(2);
        Cell h1 = r2.createCell(0); h1.setCellValue("तपशील"); h1.setCellStyle(labelStyle);
        Cell h2 = r2.createCell(1); h2.setCellValue("रक्कम"); h2.setCellStyle(labelStyle);

        // Owner collection
        Row r4 = sheet.createRow(4);
        r4.createCell(0).setCellValue("जमा झालेली वर्गणी (घरमालक)");
        Cell v4 = r4.createCell(1); v4.setCellValue(ownerCollected); v4.setCellStyle(currencyStyle);

        // Renter collection
        Row r5 = sheet.createRow(5);
        r5.createCell(0).setCellValue("जमा झालेली वर्गणी (भाडेकरू)");
        Cell v5 = r5.createCell(1); v5.setCellValue(renterCollected); v5.setCellStyle(currencyStyle);

        // Total Vargani
        Row r6 = sheet.createRow(6);
        Cell l6 = r6.createCell(0); l6.setCellValue("एकूण वर्गणी जमा"); l6.setCellStyle(totalLabelStyle);
        Cell v6 = r6.createCell(1); v6.setCellValue(totalVargani); v6.setCellStyle(totalValueStyle);

        // Empty row
        // Total Kharch
        Row r8 = sheet.createRow(8);
        Cell l8 = r8.createCell(0); l8.setCellValue("एकूण खर्च"); l8.setCellStyle(totalLabelStyle);
        Cell v8 = r8.createCell(1); v8.setCellValue(totalKharch); v8.setCellStyle(totalValueStyle);

        // Balance
        Row r10 = sheet.createRow(10);
        Cell l10 = r10.createCell(0); l10.setCellValue("एकूण (बाकी)"); l10.setCellStyle(totalLabelStyle);
        Cell v10 = r10.createCell(1); v10.setCellValue(totalVargani - totalKharch); v10.setCellStyle(totalValueStyle);

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private CellStyle createTotalStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle createTotalCurrencyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0.00"));
        style.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }
}
