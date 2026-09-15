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
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

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

    // ── Premium Color Palette ────────────────────────────────────────────
    // Warm terracotta/cream theme inspired by premium budget templates
    private static final byte[] COLOR_HEADER     = {(byte)0xC4, (byte)0x95, (byte)0x6A}; // Warm terracotta
    private static final byte[] COLOR_HEADER_DARK = {(byte)0x8B, (byte)0x6B, (byte)0x4A}; // Darker brown
    private static final byte[] COLOR_ALT_ROW    = {(byte)0xFD, (byte)0xF6, (byte)0xEE}; // Soft cream
    private static final byte[] COLOR_TOTAL_BG   = {(byte)0xF5, (byte)0xE6, (byte)0xD3}; // Warm tan
    private static final byte[] COLOR_ACCENT     = {(byte)0xE8, (byte)0xD5, (byte)0xB7}; // Light gold
    private static final byte[] COLOR_WHITE      = {(byte)0xFF, (byte)0xFF, (byte)0xFF};
    private static final byte[] COLOR_TITLE_BG   = {(byte)0x3C, (byte)0x2F, (byte)0x27}; // Dark espresso
    private static final byte[] COLOR_PAID_BG    = {(byte)0xE7, (byte)0xF5, (byte)0xE7}; // Soft green
    private static final byte[] COLOR_PENDING_BG = {(byte)0xFF, (byte)0xF0, (byte)0xE0}; // Soft orange
    private static final byte[] COLOR_NA_BG      = {(byte)0xF5, (byte)0xF5, (byte)0xF5}; // Light grey

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

            // Fetch previous year balance from mandal settings
            com.mandal.dao.MandalDao mandalDao = new com.mandal.dao.MandalDao();
            com.mandal.model.Mandal mandal = mandalDao.findById(mandalId);
            double previousBalance = (mandal != null && mandal.getPreviousBalance() != null)
                    ? mandal.getPreviousBalance().doubleValue() : 0;

            try (XSSFWorkbook workbook = new XSSFWorkbook()) {
                // Fetch raw data for both sheets and karyakarta balance computation
                List<Contribution> contributions = contributionDao.findAll(mandalId, null, null, null, null);
                List<Expense> expenses = expenseDao.findAll(mandalId);

                // Create data sheets first to compute totals
                Sheet contribSheet = workbook.createSheet("वर्गणी (Contributions)");
                double totalVargani = createContributionsSheet(workbook, contribSheet, contributions);

                Sheet expenseSheet = workbook.createSheet("खर्च (Expenses)");
                double totalKharch = createExpensesSheet(workbook, expenseSheet, expenses);

                double[] roomTotals = createRoomTrackerSheets(workbook, mandalId);
                // roomTotals = [ownerCollected, renterCollected]

                // Create summary sheet FIRST (move to index 0)
                Sheet summarySheet = workbook.createSheet("जमा खर्च");
                workbook.setSheetOrder("जमा खर्च", 0);
                createSummarySheet(workbook, summarySheet, totalVargani, totalKharch,
                        roomTotals[0], roomTotals[1], previousBalance, contributions, expenses);

                workbook.write(resp.getOutputStream());
            }

        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(500, "Error generating report: " + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  STYLE FACTORY METHODS
    // ═══════════════════════════════════════════════════════════════════════

    private XSSFCellStyle createPremiumHeaderStyle(XSSFWorkbook wb) {
        XSSFCellStyle style = wb.createCellStyle();
        XSSFFont font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        font.setFontName("Arial");
        font.setColor(new XSSFColor(COLOR_WHITE, null));
        style.setFont(font);
        style.setFillForegroundColor(new XSSFColor(COLOR_HEADER, null));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorders(style, BorderStyle.THIN, COLOR_HEADER_DARK);
        return style;
    }

    private XSSFCellStyle createTitleBarStyle(XSSFWorkbook wb) {
        XSSFCellStyle style = wb.createCellStyle();
        XSSFFont font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        font.setFontName("Arial");
        font.setColor(new XSSFColor(COLOR_WHITE, null));
        style.setFont(font);
        style.setFillForegroundColor(new XSSFColor(COLOR_TITLE_BG, null));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private XSSFCellStyle createSubtitleStyle(XSSFWorkbook wb) {
        XSSFCellStyle style = wb.createCellStyle();
        XSSFFont font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        font.setFontName("Arial");
        font.setColor(new XSSFColor(COLOR_HEADER_DARK, null));
        style.setFont(font);
        style.setFillForegroundColor(new XSSFColor(COLOR_ACCENT, null));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    /** Creates a data cell style — optionally with alternating row background */
    private XSSFCellStyle createDataStyle(XSSFWorkbook wb, boolean isAlt) {
        XSSFCellStyle style = wb.createCellStyle();
        XSSFFont font = wb.createFont();
        font.setFontHeightInPoints((short) 10);
        font.setFontName("Arial");
        style.setFont(font);
        if (isAlt) {
            style.setFillForegroundColor(new XSSFColor(COLOR_ALT_ROW, null));
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorders(style, BorderStyle.THIN, new byte[]{(byte)0xE0, (byte)0xD5, (byte)0xC8});
        return style;
    }

    private XSSFCellStyle createCurrencyDataStyle(XSSFWorkbook wb, boolean isAlt) {
        XSSFCellStyle style = createDataStyle(wb, isAlt);
        DataFormat fmt = wb.createDataFormat();
        style.setDataFormat(fmt.getFormat("#,##0.00"));
        style.setAlignment(HorizontalAlignment.RIGHT);
        return style;
    }

    private XSSFCellStyle createDateDataStyle(XSSFWorkbook wb, boolean isAlt) {
        XSSFCellStyle style = createDataStyle(wb, isAlt);
        CreationHelper helper = wb.getCreationHelper();
        style.setDataFormat(helper.createDataFormat().getFormat("dd-MM-yyyy"));
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private XSSFCellStyle createTotalRowStyle(XSSFWorkbook wb) {
        XSSFCellStyle style = wb.createCellStyle();
        XSSFFont font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        font.setFontName("Arial");
        font.setColor(new XSSFColor(COLOR_TITLE_BG, null));
        style.setFont(font);
        style.setFillForegroundColor(new XSSFColor(COLOR_TOTAL_BG, null));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorders(style, BorderStyle.THIN, COLOR_HEADER_DARK);
        return style;
    }

    private XSSFCellStyle createTotalCurrencyRowStyle(XSSFWorkbook wb) {
        XSSFCellStyle style = createTotalRowStyle(wb);
        DataFormat fmt = wb.createDataFormat();
        style.setDataFormat(fmt.getFormat("#,##0.00"));
        style.setAlignment(HorizontalAlignment.RIGHT);
        return style;
    }

    private XSSFCellStyle createSrNoStyle(XSSFWorkbook wb, boolean isAlt) {
        XSSFCellStyle style = createDataStyle(wb, isAlt);
        XSSFFont font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 10);
        font.setFontName("Arial");
        font.setColor(new XSSFColor(COLOR_HEADER_DARK, null));
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private XSSFCellStyle createNaStyle(XSSFWorkbook wb) {
        XSSFCellStyle style = wb.createCellStyle();
        XSSFFont font = wb.createFont();
        font.setFontHeightInPoints((short) 10);
        font.setFontName("Arial");
        font.setItalic(true);
        font.setColor(new XSSFColor(new byte[]{(byte)0x99, (byte)0x99, (byte)0x99}, null));
        style.setFont(font);
        style.setFillForegroundColor(new XSSFColor(COLOR_NA_BG, null));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorders(style, BorderStyle.THIN, new byte[]{(byte)0xE0, (byte)0xD5, (byte)0xC8});
        return style;
    }

    private XSSFCellStyle createStatusPaidStyle(XSSFWorkbook wb) {
        XSSFCellStyle style = wb.createCellStyle();
        XSSFFont font = wb.createFont();
        font.setFontHeightInPoints((short) 10);
        font.setFontName("Arial");
        font.setBold(true);
        font.setColor(new XSSFColor(new byte[]{(byte)0x2E, (byte)0x7D, (byte)0x32}, null));
        style.setFont(font);
        style.setFillForegroundColor(new XSSFColor(COLOR_PAID_BG, null));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorders(style, BorderStyle.THIN, new byte[]{(byte)0xE0, (byte)0xD5, (byte)0xC8});
        return style;
    }

    private XSSFCellStyle createStatusPendingStyle(XSSFWorkbook wb) {
        XSSFCellStyle style = wb.createCellStyle();
        XSSFFont font = wb.createFont();
        font.setFontHeightInPoints((short) 10);
        font.setFontName("Arial");
        font.setColor(new XSSFColor(new byte[]{(byte)0xE6, (byte)0x5C, (byte)0x00}, null));
        style.setFont(font);
        style.setFillForegroundColor(new XSSFColor(COLOR_PENDING_BG, null));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorders(style, BorderStyle.THIN, new byte[]{(byte)0xE0, (byte)0xD5, (byte)0xC8});
        return style;
    }

    private void setBorders(XSSFCellStyle style, BorderStyle bs, byte[] color) {
        XSSFColor c = new XSSFColor(color, null);
        style.setBorderBottom(bs);
        style.setBorderTop(bs);
        style.setBorderLeft(bs);
        style.setBorderRight(bs);
        style.setBottomBorderColor(c);
        style.setTopBorderColor(c);
        style.setLeftBorderColor(c);
        style.setRightBorderColor(c);
    }

    // Helper: set a cell with value and style
    private Cell setCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        if (style != null) cell.setCellStyle(style);
        return cell;
    }

    private Cell setCell(Row row, int col, double value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        if (style != null) cell.setCellStyle(style);
        return cell;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  CONTRIBUTIONS SHEET
    // ═══════════════════════════════════════════════════════════════════════

    private double createContributionsSheet(XSSFWorkbook wb, Sheet sheet, List<Contribution> contributions) {
        Collections.reverse(contributions);

        // Pre-create styles (to avoid creating too many)
        XSSFCellStyle titleStyle = createTitleBarStyle(wb);
        XSSFCellStyle headerStyle = createPremiumHeaderStyle(wb);
        XSSFCellStyle[] dataStyles = {createDataStyle(wb, false), createDataStyle(wb, true)};
        XSSFCellStyle[] currStyles = {createCurrencyDataStyle(wb, false), createCurrencyDataStyle(wb, true)};
        XSSFCellStyle[] dateStyles = {createDateDataStyle(wb, false), createDateDataStyle(wb, true)};
        XSSFCellStyle[] srStyles  = {createSrNoStyle(wb, false), createSrNoStyle(wb, true)};
        XSSFCellStyle totalLabelStyle = createTotalRowStyle(wb);
        XSSFCellStyle totalCurrStyle  = createTotalCurrencyRowStyle(wb);

        // Title bar
        Row titleRow = sheet.createRow(0);
        titleRow.setHeightInPoints(30);
        setCell(titleRow, 0, "वर्गणी यादी (Contributions)", titleStyle);
        for (int i = 1; i <= 6; i++) setCell(titleRow, i, "", titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));

        // Spacer
        sheet.createRow(1);

        // Header row
        Row headerRow = sheet.createRow(2);
        headerRow.setHeightInPoints(24);
        String[] headers = {"अ.क्र.", "पावती क्र.", "नाव", "रक्कम (₹)", "दिनांक", "पेमेंट माध्यम", "जमा करणारे"};
        for (int i = 0; i < headers.length; i++) {
            setCell(headerRow, i, headers[i], headerStyle);
        }

        // Data rows
        double total = 0;
        int rowNum = 3;
        int srNo = 1;
        for (Contribution c : contributions) {
            boolean isAlt = ((rowNum - 3) % 2 == 1);
            Row row = sheet.createRow(rowNum++);
            row.setHeightInPoints(22);

            setCell(row, 0, String.valueOf(srNo++), srStyles[isAlt ? 1 : 0]);
            setCell(row, 1, c.getReceiptNo() != null ? c.getReceiptNo() : "", dataStyles[isAlt ? 1 : 0]);
            setCell(row, 2, c.getMemberName() != null ? c.getMemberName() : "", dataStyles[isAlt ? 1 : 0]);

            double amt = c.getAmount() != null ? c.getAmount().doubleValue() : 0;
            setCell(row, 3, amt, currStyles[isAlt ? 1 : 0]);
            total += amt;

            Cell dateCell = row.createCell(4);
            if (c.getContributionDate() != null) {
                dateCell.setCellValue(c.getContributionDate());
                dateCell.setCellStyle(dateStyles[isAlt ? 1 : 0]);
            } else {
                dateCell.setCellStyle(dataStyles[isAlt ? 1 : 0]);
            }

            String pm = c.getPaymentMethod() != null ? c.getPaymentMethod().name() : "";
            String pmLabel = "CASH".equals(pm) ? "रोख" : "UPI".equals(pm) ? "UPI" :
                    "BANK_TRANSFER".equals(pm) ? "बँक ट्रान्सफर" : pm;
            setCell(row, 5, pmLabel, dataStyles[isAlt ? 1 : 0]);
            setCell(row, 6, c.getCollectedByName() != null ? c.getCollectedByName() : "", dataStyles[isAlt ? 1 : 0]);
        }

        // Total row
        rowNum++; // blank spacer
        Row totalRow = sheet.createRow(rowNum);
        totalRow.setHeightInPoints(26);
        for (int i = 0; i < headers.length; i++) {
            if (i == 2) setCell(totalRow, i, "एकूण वर्गणी", totalLabelStyle);
            else if (i == 3) setCell(totalRow, i, total, totalCurrStyle);
            else setCell(totalRow, i, "", totalLabelStyle);
        }

        // Auto-size + minimum widths
        int[] minWidths = {2500, 3500, 6000, 4000, 4000, 4500, 5000};
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
            if (sheet.getColumnWidth(i) < minWidths[i]) sheet.setColumnWidth(i, minWidths[i]);
        }

        return total;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  EXPENSES SHEET
    // ═══════════════════════════════════════════════════════════════════════

    private double createExpensesSheet(XSSFWorkbook wb, Sheet sheet, List<Expense> expenses) {
        Collections.reverse(expenses);

        XSSFCellStyle titleStyle = createTitleBarStyle(wb);
        XSSFCellStyle headerStyle = createPremiumHeaderStyle(wb);
        XSSFCellStyle[] dataStyles = {createDataStyle(wb, false), createDataStyle(wb, true)};
        XSSFCellStyle[] currStyles = {createCurrencyDataStyle(wb, false), createCurrencyDataStyle(wb, true)};
        XSSFCellStyle[] dateStyles = {createDateDataStyle(wb, false), createDateDataStyle(wb, true)};
        XSSFCellStyle[] srStyles  = {createSrNoStyle(wb, false), createSrNoStyle(wb, true)};
        XSSFCellStyle totalLabelStyle = createTotalRowStyle(wb);
        XSSFCellStyle totalCurrStyle  = createTotalCurrencyRowStyle(wb);

        // Title bar
        Row titleRow = sheet.createRow(0);
        titleRow.setHeightInPoints(30);
        setCell(titleRow, 0, "खर्च यादी (Expenses)", titleStyle);
        for (int i = 1; i <= 4; i++) setCell(titleRow, i, "", titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));

        sheet.createRow(1);

        // Header row
        Row headerRow = sheet.createRow(2);
        headerRow.setHeightInPoints(24);
        String[] headers = {"अ.क्र.", "वस्तूचे नाव", "रक्कम (₹)", "दिनांक", "खरेदी करणारे"};
        for (int i = 0; i < headers.length; i++) {
            setCell(headerRow, i, headers[i], headerStyle);
        }

        double total = 0;
        int rowNum = 3;
        int srNo = 1;
        for (Expense e : expenses) {
            boolean isAlt = ((rowNum - 3) % 2 == 1);
            Row row = sheet.createRow(rowNum++);
            row.setHeightInPoints(22);

            setCell(row, 0, String.valueOf(srNo++), srStyles[isAlt ? 1 : 0]);
            setCell(row, 1, e.getItemName() != null ? e.getItemName() : "", dataStyles[isAlt ? 1 : 0]);

            double amt = e.getAmount() != null ? e.getAmount().doubleValue() : 0;
            setCell(row, 2, amt, currStyles[isAlt ? 1 : 0]);
            total += amt;

            Cell dateCell = row.createCell(3);
            if (e.getExpenseDate() != null) {
                dateCell.setCellValue(e.getExpenseDate());
                dateCell.setCellStyle(dateStyles[isAlt ? 1 : 0]);
            } else {
                dateCell.setCellStyle(dataStyles[isAlt ? 1 : 0]);
            }

            setCell(row, 4, e.getPurchasedByName() != null ? e.getPurchasedByName() : "", dataStyles[isAlt ? 1 : 0]);
        }

        // Total row
        rowNum++;
        Row totalRow = sheet.createRow(rowNum);
        totalRow.setHeightInPoints(26);
        for (int i = 0; i < headers.length; i++) {
            if (i == 1) setCell(totalRow, i, "एकूण खर्च", totalLabelStyle);
            else if (i == 2) setCell(totalRow, i, total, totalCurrStyle);
            else setCell(totalRow, i, "", totalLabelStyle);
        }

        int[] minWidths = {2500, 7000, 4000, 4000, 5000};
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
            if (sheet.getColumnWidth(i) < minWidths[i]) sheet.setColumnWidth(i, minWidths[i]);
        }

        return total;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  ROOM TRACKER SHEETS (OWNER + RENTAL)
    // ═══════════════════════════════════════════════════════════════════════

    private double[] createRoomTrackerSheets(XSSFWorkbook wb, Long mandalId) throws SQLException {
        List<SocietyRoom> allRooms = roomDao.findAll(mandalId, null, null);

        // Separate owners and renters
        List<SocietyRoom> owners = new ArrayList<>();
        List<SocietyRoom> renters = new ArrayList<>();
        for (SocietyRoom r : allRooms) {
            if ("OWNER".equals(r.getResidentType()) || r.getFloorNumber() == 0) {
                owners.add(r);
            } else {
                renters.add(r);
            }
        }

        // Build master room list from owners (every physical room)
        Set<String> masterRoomNums = new TreeSet<>((a, b) -> {
            try { return Integer.parseInt(a) - Integer.parseInt(b); }
            catch (NumberFormatException e) { return a.compareTo(b); }
        });
        for (SocietyRoom r : owners) masterRoomNums.add(r.getRoomNumber());
        // Also add any renter-only rooms (edge case)
        for (SocietyRoom r : renters) masterRoomNums.add(r.getRoomNumber());
        List<String> sortedRooms = new ArrayList<>(masterRoomNums);

        double ownerTotal = createOwnerSheet(wb, owners, sortedRooms);
        double renterTotal = createRentalSheet(wb, renters, sortedRooms);

        return new double[]{ownerTotal, renterTotal};
    }

    // ── OWNER SHEET ──────────────────────────────────────────────────────

    private double createOwnerSheet(XSSFWorkbook wb, List<SocietyRoom> owners, List<String> sortedRooms) {
        Sheet sheet = wb.createSheet("घरमालक (Owner)");

        XSSFCellStyle titleStyle = createTitleBarStyle(wb);
        XSSFCellStyle headerStyle = createPremiumHeaderStyle(wb);
        XSSFCellStyle[] dataStyles = {createDataStyle(wb, false), createDataStyle(wb, true)};
        XSSFCellStyle[] currStyles = {createCurrencyDataStyle(wb, false), createCurrencyDataStyle(wb, true)};
        XSSFCellStyle paidStyle = createStatusPaidStyle(wb);
        XSSFCellStyle pendingStyle = createStatusPendingStyle(wb);
        XSSFCellStyle totalLabelStyle = createTotalRowStyle(wb);
        XSSFCellStyle totalCurrStyle = createTotalCurrencyRowStyle(wb);

        // Title bar
        Row titleRow = sheet.createRow(0);
        titleRow.setHeightInPoints(30);
        setCell(titleRow, 0, "जमा झालेली वर्गणी — घरमालक (Owner)", titleStyle);
        for (int i = 1; i <= 5; i++) setCell(titleRow, i, "", titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));

        sheet.createRow(1);

        // Header
        Row headerRow = sheet.createRow(2);
        headerRow.setHeightInPoints(24);
        String[] headers = {"रूम नंबर", "नाव", "रक्कम (₹)", "पेमेंट माध्यम", "स्थिती", "टिपणी"};
        for (int i = 0; i < headers.length; i++) {
            setCell(headerRow, i, headers[i], headerStyle);
        }

        // Sort owners by room number
        owners.sort((a, b) -> {
            try { return Integer.parseInt(a.getRoomNumber()) - Integer.parseInt(b.getRoomNumber()); }
            catch (NumberFormatException e) { return a.getRoomNumber().compareTo(b.getRoomNumber()); }
        });

        int rowNum = 3;
        double totalCollected = 0;
        for (SocietyRoom r : owners) {
            boolean isAlt = ((rowNum - 3) % 2 == 1);
            Row row = sheet.createRow(rowNum++);
            row.setHeightInPoints(22);

            setCell(row, 0, r.getRoomNumber(), dataStyles[isAlt ? 1 : 0]);
            setCell(row, 1, r.getResidentName() != null ? r.getResidentName() : "", dataStyles[isAlt ? 1 : 0]);

            double amt = r.getAmountPaid() != null ? r.getAmountPaid().doubleValue() : 0;
            if (amt > 0) {
                setCell(row, 2, amt, currStyles[isAlt ? 1 : 0]);
                totalCollected += amt;
            } else {
                setCell(row, 2, "", dataStyles[isAlt ? 1 : 0]);
            }

            String pm = r.getPaymentMethod();
            String pmLabel = pm != null ? ("CASH".equals(pm) ? "रोख (Cash)" : "ऑनलाइन (Online)") : "";
            setCell(row, 3, pmLabel, dataStyles[isAlt ? 1 : 0]);

            // Status column with color coding
            String status = r.getVarganiStatus();
            if ("PAID".equals(status)) {
                setCell(row, 4, "✓ दिली", paidStyle);
            } else if ("PARTIALLY_PAID".equals(status)) {
                setCell(row, 4, "अर्धवट", pendingStyle);
            } else {
                setCell(row, 4, "बाकी", pendingStyle);
            }

            setCell(row, 5, r.getNotes() != null ? r.getNotes() : "", dataStyles[isAlt ? 1 : 0]);
        }

        // Total row
        rowNum++;
        Row totalRow = sheet.createRow(rowNum);
        totalRow.setHeightInPoints(26);
        for (int i = 0; i < headers.length; i++) {
            if (i == 1) setCell(totalRow, i, "एकूण घरमालक वर्गणी", totalLabelStyle);
            else if (i == 2) setCell(totalRow, i, totalCollected, totalCurrStyle);
            else setCell(totalRow, i, "", totalLabelStyle);
        }

        int[] minWidths = {3000, 5500, 4000, 5000, 3500, 5000};
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
            if (sheet.getColumnWidth(i) < minWidths[i]) sheet.setColumnWidth(i, minWidths[i]);
        }

        return totalCollected;
    }

    // ── RENTAL SHEET ─────────────────────────────────────────────────────

    private double createRentalSheet(XSSFWorkbook wb, List<SocietyRoom> renters, List<String> masterRooms) {
        Sheet sheet = wb.createSheet("भाडेकरू (Rental)");

        XSSFCellStyle titleStyle = createTitleBarStyle(wb);
        XSSFCellStyle headerStyle = createPremiumHeaderStyle(wb);
        XSSFCellStyle[] dataStyles = {createDataStyle(wb, false), createDataStyle(wb, true)};
        XSSFCellStyle[] currStyles = {createCurrencyDataStyle(wb, false), createCurrencyDataStyle(wb, true)};
        XSSFCellStyle naStyle = createNaStyle(wb);
        XSSFCellStyle totalLabelStyle = createTotalRowStyle(wb);
        XSSFCellStyle totalCurrStyle = createTotalCurrencyRowStyle(wb);

        // Group renters by floor
        Map<Integer, List<SocietyRoom>> byFloor = new TreeMap<>();
        for (SocietyRoom r : renters) {
            byFloor.computeIfAbsent(r.getFloorNumber(), k -> new ArrayList<>()).add(r);
        }

        List<Integer> floors = new ArrayList<>(byFloor.keySet());

        // Floor label mapping: position-based lettering (A, B, C...)
        String[] floorLabels = {"पहिला मजला A", "दुसरा मजला B", "तिसरा मजला C", "चौथा मजला D", "पाचवा मजला E"};

        // Title bar
        int totalCols = 1 + floors.size() * 3;
        Row titleRow = sheet.createRow(0);
        titleRow.setHeightInPoints(30);
        setCell(titleRow, 0, "जमा झालेली वर्गणी — भाडेकरू (Rental)", titleStyle);
        for (int i = 1; i < totalCols; i++) setCell(titleRow, i, "", titleStyle);
        if (totalCols > 1) sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, totalCols - 1));

        sheet.createRow(1);

        // Floor group headers (row 2) — same warm style for all floors
        Row floorHdrRow = sheet.createRow(2);
        floorHdrRow.setHeightInPoints(26);

        // Create a single floor header style — warm terracotta, consistent
        XSSFCellStyle floorHeaderStyle = wb.createCellStyle();
        XSSFFont floorFont = wb.createFont();
        floorFont.setBold(true);
        floorFont.setFontHeightInPoints((short) 12);
        floorFont.setFontName("Arial");
        floorFont.setColor(new XSSFColor(COLOR_WHITE, null));
        floorHeaderStyle.setFont(floorFont);
        floorHeaderStyle.setFillForegroundColor(new XSSFColor(COLOR_HEADER_DARK, null));
        floorHeaderStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        floorHeaderStyle.setAlignment(HorizontalAlignment.CENTER);
        floorHeaderStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorders(floorHeaderStyle, BorderStyle.THIN, COLOR_HEADER_DARK);

        // Room number corner cell
        setCell(floorHdrRow, 0, "", floorHeaderStyle);

        for (int fi = 0; fi < floors.size(); fi++) {
            int col = 1 + fi * 3;
            String label = fi < floorLabels.length ? floorLabels[fi] : "मजला " + (char)('A' + fi);
            setCell(floorHdrRow, col, label, floorHeaderStyle);
            setCell(floorHdrRow, col + 1, "", floorHeaderStyle);
            setCell(floorHdrRow, col + 2, "", floorHeaderStyle);
            sheet.addMergedRegion(new CellRangeAddress(2, 2, col, col + 2));
        }

        // Column headers (row 3)
        Row colHdrRow = sheet.createRow(3);
        colHdrRow.setHeightInPoints(24);
        setCell(colHdrRow, 0, "रूम नंबर", headerStyle);
        for (int fi = 0; fi < floors.size(); fi++) {
            int base = 1 + fi * 3;
            setCell(colHdrRow, base, "नाव", headerStyle);
            setCell(colHdrRow, base + 1, "रक्कम", headerStyle);
            setCell(colHdrRow, base + 2, "पेमेंट", headerStyle);
        }

        // Data rows — one per room number from master list
        int dataStart = 4;
        double[] floorTotals = new double[floors.size()];

        for (int ri = 0; ri < masterRooms.size(); ri++) {
            String rn = masterRooms.get(ri);
            boolean isAlt = (ri % 2 == 1);
            Row row = sheet.createRow(dataStart + ri);
            row.setHeightInPoints(22);
            setCell(row, 0, rn, dataStyles[isAlt ? 1 : 0]);

            for (int fi = 0; fi < floors.size(); fi++) {
                int base = 1 + fi * 3;
                int floor = floors.get(fi);

                // Find room for this floor + room number
                SocietyRoom match = null;
                List<SocietyRoom> floorList = byFloor.get(floor);
                if (floorList != null) {
                    for (SocietyRoom r : floorList) {
                        if (rn.equals(r.getRoomNumber())) { match = r; break; }
                    }
                }

                if (match == null) {
                    // Room was deleted / doesn't exist for this floor → NA
                    setCell(row, base, "NA", naStyle);
                    setCell(row, base + 1, "", naStyle);
                    setCell(row, base + 2, "", naStyle);
                } else if (match.getResidentName() != null && !match.getResidentName().isBlank()
                        && match.getAmountPaid() != null && match.getAmountPaid().doubleValue() > 0) {
                    // Room exists AND has data filled → show everything
                    setCell(row, base, match.getResidentName(), dataStyles[isAlt ? 1 : 0]);
                    double amt = match.getAmountPaid().doubleValue();
                    setCell(row, base + 1, amt, currStyles[isAlt ? 1 : 0]);
                    floorTotals[fi] += amt;
                    String pm = match.getPaymentMethod();
                    String pmLabel = pm != null ? ("CASH".equals(pm) ? "Cash" : "Online") : "";
                    setCell(row, base + 2, pmLabel, dataStyles[isAlt ? 1 : 0]);
                } else {
                    // Room exists but no contribution data yet → leave blank
                    String name = (match.getResidentName() != null && !match.getResidentName().isBlank())
                            ? match.getResidentName() : "";
                    setCell(row, base, name, dataStyles[isAlt ? 1 : 0]);
                    setCell(row, base + 1, "", dataStyles[isAlt ? 1 : 0]);
                    setCell(row, base + 2, "", dataStyles[isAlt ? 1 : 0]);
                }
            }
        }

        // Per-floor totals
        int sumStart = dataStart + masterRooms.size() + 1;
        Row sumRow = sheet.createRow(sumStart);
        sumRow.setHeightInPoints(26);
        setCell(sumRow, 0, "एकूण", totalLabelStyle);
        for (int fi = 0; fi < floors.size(); fi++) {
            int base = 1 + fi * 3;
            setCell(sumRow, base, "", totalLabelStyle);
            setCell(sumRow, base + 1, floorTotals[fi], totalCurrStyle);
            setCell(sumRow, base + 2, "", totalLabelStyle);
        }

        // Grand total row
        double grandTotal = 0;
        for (double ft : floorTotals) grandTotal += ft;
        Row grandRow = sheet.createRow(sumStart + 1);
        grandRow.setHeightInPoints(26);
        setCell(grandRow, 0, "एकूण भाडेकरू वर्गणी", totalLabelStyle);
        for (int i = 1; i < totalCols; i++) {
            if (i == 2) setCell(grandRow, i, grandTotal, totalCurrStyle);
            else setCell(grandRow, i, "", totalLabelStyle);
        }

        // Auto-size columns
        for (int i = 0; i < totalCols; i++) {
            sheet.autoSizeColumn(i);
            if (sheet.getColumnWidth(i) < 3500) sheet.setColumnWidth(i, 3500);
        }

        return grandTotal;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  SUMMARY SHEET (जमा खर्च)
    // ═══════════════════════════════════════════════════════════════════════

    private void createSummarySheet(XSSFWorkbook wb, Sheet sheet, double totalVargani, double totalKharch,
                                     double ownerCollected, double renterCollected, double previousBalance,
                                     List<Contribution> contributions, List<Expense> expenses) {
        XSSFCellStyle titleStyle = createTitleBarStyle(wb);
        XSSFCellStyle headerStyle = createPremiumHeaderStyle(wb);
        XSSFCellStyle subtitleStyle = createSubtitleStyle(wb);

        // Label styles
        XSSFCellStyle labelStyle = createDataStyle(wb, false);
        XSSFFont labelFont = wb.createFont();
        labelFont.setFontHeightInPoints((short) 11);
        labelFont.setFontName("Arial");
        labelStyle.setFont(labelFont);

        XSSFCellStyle valueStyle = createCurrencyDataStyle(wb, false);
        XSSFFont valueFont = wb.createFont();
        valueFont.setFontHeightInPoints((short) 11);
        valueFont.setFontName("Arial");
        valueFont.setBold(true);
        valueStyle.setFont(valueFont);

        XSSFCellStyle totalLabelStyle = createTotalRowStyle(wb);
        XSSFCellStyle totalValueStyle = createTotalCurrencyRowStyle(wb);

        // Grand total styles (bigger, darker)
        XSSFCellStyle grandLabelStyle = wb.createCellStyle();
        grandLabelStyle.cloneStyleFrom(totalLabelStyle);
        XSSFFont grandFont = wb.createFont();
        grandFont.setBold(true);
        grandFont.setFontHeightInPoints((short) 14);
        grandFont.setFontName("Arial");
        grandFont.setColor(new XSSFColor(COLOR_WHITE, null));
        grandLabelStyle.setFont(grandFont);
        grandLabelStyle.setFillForegroundColor(new XSSFColor(COLOR_TITLE_BG, null));
        grandLabelStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        XSSFCellStyle grandValueStyle = wb.createCellStyle();
        grandValueStyle.cloneStyleFrom(totalValueStyle);
        grandValueStyle.setFont(grandFont);
        grandValueStyle.setFillForegroundColor(new XSSFColor(COLOR_TITLE_BG, null));
        grandValueStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // Alt label style for visual variety
        XSSFCellStyle altLabelStyle = createDataStyle(wb, true);
        altLabelStyle.setFont(labelFont);
        XSSFCellStyle altValueStyle = createCurrencyDataStyle(wb, true);
        altValueStyle.setFont(valueFont);

        // ── Title bar ──
        Row r0 = sheet.createRow(0);
        r0.setHeightInPoints(36);
        setCell(r0, 0, "जमा खर्च — Financial Summary", titleStyle);
        for (int i = 1; i <= 4; i++) setCell(r0, i, "", titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));

        sheet.createRow(1);

        // ── Column Headers ──
        Row r2 = sheet.createRow(2);
        r2.setHeightInPoints(24);
        setCell(r2, 0, "तपशील (Description)", headerStyle);
        setCell(r2, 1, "रक्कम (Amount ₹)", headerStyle);

        // ── INCOME SECTION ──
        Row r3 = sheet.createRow(3);
        r3.setHeightInPoints(22);
        setCell(r3, 0, "── जमा (Income) ──", subtitleStyle);
        setCell(r3, 1, "", subtitleStyle);

        // Previous Year Balance
        Row r4 = sheet.createRow(4);
        r4.setHeightInPoints(22);
        setCell(r4, 0, "मागील वर्षाची शिल्लक (Previous Year Balance)", labelStyle);
        setCell(r4, 1, previousBalance, valueStyle);

        // Owner collection
        Row r5 = sheet.createRow(5);
        r5.setHeightInPoints(22);
        setCell(r5, 0, "जमा झालेली वर्गणी — घरमालक (Owner)", altLabelStyle);
        setCell(r5, 1, ownerCollected, altValueStyle);

        // Renter collection
        Row r6 = sheet.createRow(6);
        r6.setHeightInPoints(22);
        setCell(r6, 0, "जमा झालेली वर्गणी — भाडेकरू (Rental)", labelStyle);
        setCell(r6, 1, renterCollected, valueStyle);

        // Total Income
        Row r7 = sheet.createRow(7);
        r7.setHeightInPoints(26);
        setCell(r7, 0, "एकूण जमा (Total Income)", totalLabelStyle);
        setCell(r7, 1, previousBalance + totalVargani, totalValueStyle);

        // Spacer
        sheet.createRow(8);

        // ── EXPENSE SECTION ──
        Row r9 = sheet.createRow(9);
        r9.setHeightInPoints(22);
        setCell(r9, 0, "── खर्च (Expenses) ──", subtitleStyle);
        setCell(r9, 1, "", subtitleStyle);

        Row r10 = sheet.createRow(10);
        r10.setHeightInPoints(22);
        setCell(r10, 0, "एकूण खर्च (Total Expenses)", labelStyle);
        setCell(r10, 1, totalKharch, valueStyle);

        // Spacer
        sheet.createRow(11);

        // ── BALANCE ──
        Row r12 = sheet.createRow(12);
        r12.setHeightInPoints(32);
        setCell(r12, 0, "शिल्लक रक्कम (Balance in Hand)", grandLabelStyle);
        setCell(r12, 1, previousBalance + totalVargani - totalKharch, grandValueStyle);

        // ═══════════════════════════════════════════════════════════════════
        //  KARYAKARTA BALANCE SECTION
        // ═══════════════════════════════════════════════════════════════════

        // Compute per-karyakarta: collected (cash/online) and spent
        // Key = userId, Value = [name, cashCollected, onlineCollected, totalSpent]
        Map<Long, Object[]> karyakartaData = new LinkedHashMap<>();

        for (Contribution c : contributions) {
            Long userId = c.getCollectedBy();
            String name = c.getCollectedByName();
            if (userId == null || name == null || name.isBlank()) continue;
            Object[] data = karyakartaData.computeIfAbsent(userId, k -> new Object[]{name, 0.0, 0.0, 0.0});
            double amt = c.getAmount() != null ? c.getAmount().doubleValue() : 0;
            String pm = c.getPaymentMethod() != null ? c.getPaymentMethod().name() : "";
            if ("CASH".equals(pm)) {
                data[1] = (double) data[1] + amt; // cash collected
            } else {
                data[2] = (double) data[2] + amt; // online collected (UPI, bank transfer, etc.)
            }
        }

        for (Expense e : expenses) {
            Long userId = e.getPurchasedBy();
            String name = e.getPurchasedByName();
            if (userId == null || name == null || name.isBlank()) continue;
            Object[] data = karyakartaData.computeIfAbsent(userId, k -> new Object[]{name, 0.0, 0.0, 0.0});
            double amt = e.getAmount() != null ? e.getAmount().doubleValue() : 0;
            data[3] = (double) data[3] + amt; // total spent
        }

        // Only show if there's data
        if (!karyakartaData.isEmpty()) {
            int rowIdx = 14;

            // Section title
            Row secTitle = sheet.createRow(rowIdx++);
            secTitle.setHeightInPoints(30);
            setCell(secTitle, 0, "कार्यकर्ता शिल्लक — Karyakarta Balance", titleStyle);
            for (int i = 1; i <= 4; i++) setCell(secTitle, i, "", titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowIdx - 1, rowIdx - 1, 0, 4));

            rowIdx++; // spacer

            // Table headers
            Row kHdr = sheet.createRow(rowIdx++);
            kHdr.setHeightInPoints(24);
            setCell(kHdr, 0, "कार्यकर्ता (Name)", headerStyle);
            setCell(kHdr, 1, "रोख जमा (Cash)", headerStyle);
            setCell(kHdr, 2, "ऑनलाइन जमा (Online)", headerStyle);
            setCell(kHdr, 3, "खर्च केला (Spent)", headerStyle);
            setCell(kHdr, 4, "अपेक्षित शिल्लक (Expected Balance)", headerStyle);

            // Data rows
            int idx = 0;
            for (Map.Entry<Long, Object[]> entry : karyakartaData.entrySet()) {
                boolean isAlt = (idx % 2 == 1);
                XSSFCellStyle lbl = isAlt ? altLabelStyle : labelStyle;
                XSSFCellStyle val = isAlt ? altValueStyle : valueStyle;

                Row kRow = sheet.createRow(rowIdx++);
                kRow.setHeightInPoints(22);
                Object[] d = entry.getValue();
                String personName = (String) d[0];
                double cashCollected = (double) d[1];
                double onlineCollected = (double) d[2];
                double totalSpent = (double) d[3];
                double expectedBalance = cashCollected + onlineCollected - totalSpent;

                setCell(kRow, 0, personName, lbl);
                setCell(kRow, 1, cashCollected, val);
                setCell(kRow, 2, onlineCollected, val);
                setCell(kRow, 3, totalSpent, val);

                // Color the balance: green if positive, orange if negative
                if (expectedBalance >= 0) {
                    XSSFCellStyle balStyle = createStatusPaidStyle(wb);
                    DataFormat fmt = wb.createDataFormat();
                    balStyle.setDataFormat(fmt.getFormat("#,##0.00"));
                    setCell(kRow, 4, expectedBalance, balStyle);
                } else {
                    XSSFCellStyle balStyle = createStatusPendingStyle(wb);
                    DataFormat fmt = wb.createDataFormat();
                    balStyle.setDataFormat(fmt.getFormat("#,##0.00"));
                    setCell(kRow, 4, expectedBalance, balStyle);
                }

                idx++;
            }
        }

        // Column widths
        sheet.setColumnWidth(0, 14000);
        sheet.setColumnWidth(1, 5500);
        sheet.setColumnWidth(2, 5500);
        sheet.setColumnWidth(3, 5500);
        sheet.setColumnWidth(4, 6500);
    }
}
