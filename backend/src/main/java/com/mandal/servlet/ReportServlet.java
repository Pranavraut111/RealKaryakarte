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
        List<SocietyRoom> rooms = roomDao.findAll(mandalId, null);

        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle evenRowStyle = createEvenRowStyle(workbook);
        CellStyle currencyStyle = createCurrencyStyle(workbook, false);
        CellStyle evenCurrencyStyle = createCurrencyStyle(workbook, true);

        // Status cell styles
        CellStyle paidStyle = workbook.createCellStyle();
        paidStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        paidStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font paidFont = workbook.createFont();
        paidFont.setBold(true);
        paidStyle.setFont(paidFont);

        CellStyle pendingStyle = workbook.createCellStyle();
        pendingStyle.setFillForegroundColor(IndexedColors.ROSE.getIndex());
        pendingStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font pendingFont = workbook.createFont();
        pendingFont.setBold(true);
        pendingStyle.setFont(pendingFont);

        CellStyle partialStyle = workbook.createCellStyle();
        partialStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        partialStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font partialFont = workbook.createFont();
        partialFont.setBold(true);
        partialStyle.setFont(partialFont);

        Row headerRow = sheet.createRow(0);
        String[] headers = {"Room No.", "Floor", "Resident Name", "Phone", "Status", "Amount Paid (₹)", "Marked By", "Notes"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        for (SocietyRoom r : rooms) {
            boolean isEven = (rowNum % 2 == 0);
            Row row = sheet.createRow(rowNum++);
            CellStyle currentStyle = isEven ? evenRowStyle : workbook.createCellStyle();
            CellStyle currentCurrencyStyle = isEven ? evenCurrencyStyle : currencyStyle;

            Cell roomCell = row.createCell(0);
            roomCell.setCellValue(r.getRoomNumber());
            if (isEven) roomCell.setCellStyle(currentStyle);

            Cell floorCell = row.createCell(1);
            floorCell.setCellValue(r.getFloorNumber() == 0 ? "Owner" : String.valueOf(r.getFloorNumber()));
            if (isEven) floorCell.setCellStyle(currentStyle);

            Cell nameCell = row.createCell(2);
            nameCell.setCellValue(r.getResidentName() != null ? r.getResidentName() : "");
            if (isEven) nameCell.setCellStyle(currentStyle);

            Cell phoneCell = row.createCell(3);
            phoneCell.setCellValue(r.getResidentPhone() != null ? r.getResidentPhone() : "");
            if (isEven) phoneCell.setCellStyle(currentStyle);

            Cell statusCell = row.createCell(4);
            String status = r.getVarganiStatus() != null ? r.getVarganiStatus() : "PENDING";
            statusCell.setCellValue(status);
            switch (status) {
                case "PAID" -> statusCell.setCellStyle(paidStyle);
                case "PENDING" -> statusCell.setCellStyle(pendingStyle);
                case "PARTIALLY_PAID" -> statusCell.setCellStyle(partialStyle);
                default -> { if (isEven) statusCell.setCellStyle(currentStyle); }
            }

            Cell amtCell = row.createCell(5);
            amtCell.setCellValue(r.getAmountPaid() != null ? r.getAmountPaid().doubleValue() : 0);
            amtCell.setCellStyle(currentCurrencyStyle);

            Cell markedByCell = row.createCell(6);
            markedByCell.setCellValue(r.getMarkedByName() != null ? r.getMarkedByName() : "");
            if (isEven) markedByCell.setCellStyle(currentStyle);

            Cell notesCell = row.createCell(7);
            notesCell.setCellValue(r.getNotes() != null ? r.getNotes() : "");
            if (isEven) notesCell.setCellStyle(currentStyle);
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }
}
