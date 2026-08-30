package com.mandal.util;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

/**
 * PDF generator for contribution receipts using OpenPDF.
 */
public class ReceiptPdfGenerator {

    /**
     * Generate a receipt PDF for a contribution.
     * Returns the relative URL path of the generated PDF, or null if generation fails.
     */
    public static String generate(
            String receiptNo,
            String memberName,
            String amount,
            String paymentMethod,
            String date,
            String mandalName,
            String language,
            String collectorName,
            String roomNumber,
            Integer floorNumber
    ) {
        String fileName = receiptNo + "-" + System.currentTimeMillis() + ".pdf";

        Document document = new Document(com.lowagie.text.PageSize.A5.rotate(), 36, 36, 36, 36);
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            // Header Table for Logo and Title
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{1.2f, 4.8f});

            try {
                com.lowagie.text.Image logo = com.lowagie.text.Image.getInstance("/Users/pranavraut/RealKaryakarte/frontend/public/recieptlogo.png");
                logo.scaleToFit(70, 70);
                PdfPCell logoCell = new PdfPCell(logo);
                logoCell.setBorder(0);
                logoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                headerTable.addCell(logoCell);
            } catch (Exception e) {
                PdfPCell empty = new PdfPCell(new Paragraph(""));
                empty.setBorder(0);
                headerTable.addCell(empty);
            }

            PdfPCell textCell = new PdfPCell();
            textCell.setBorder(0);
            textCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            
            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            Paragraph title = new Paragraph(mandalName, titleFont);
            textCell.addElement(title);
            
            Font subTitleFont = new Font(Font.HELVETICA, 11, Font.NORMAL);
            Paragraph subTitle = new Paragraph("Contribution Receipt (Vargani)", subTitleFont);
            subTitle.setSpacingBefore(3f);
            textCell.addElement(subTitle);
            
            headerTable.addCell(textCell);
            document.add(headerTable);
            
            document.add(new Paragraph("\n"));
            com.lowagie.text.pdf.draw.LineSeparator ls = new com.lowagie.text.pdf.draw.LineSeparator();
            document.add(ls);
            document.add(new Paragraph("\n"));

            // Table with details
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setSpacingBefore(5f);
            table.setSpacingAfter(10f);

            addTableRow(table, "Name:", memberName, true);
            addTableRow(table, "Receipt No:", receiptNo, false);
            addTableRow(table, "Date:", date, true);
            addTableRow(table, "Amount:", "INR " + amount, false);
            addTableRow(table, "Payment Method:", paymentMethod, true);
            if (roomNumber != null && !roomNumber.isBlank()) {
                String floorLabel = (floorNumber != null && floorNumber == 0) ? "Owner" : (floorNumber != null ? String.valueOf(floorNumber) : "-");
                addTableRow(table, "Room / Floor:", "Room " + roomNumber + " · " + floorLabel, false);
            }

            document.add(table);

            document.add(new Paragraph("\n"));

            // Footer / Signatures
            PdfPTable footerTable = new PdfPTable(3);
            footerTable.setWidthPercentage(100);
            footerTable.setWidths(new float[]{1, 1.5f, 1});

            PdfPCell c1 = new PdfPCell();
            c1.setBorder(0);
            footerTable.addCell(c1); // Empty left

            Font footerFont = new Font(Font.HELVETICA, 9, Font.ITALIC);
            Paragraph footer = new Paragraph("|| Shree Ganeshay Namah ||\nThank you for your generous contribution!\nThis is a system generated receipt.", footerFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            PdfPCell c2 = new PdfPCell(footer);
            c2.setBorder(0);
            c2.setHorizontalAlignment(Element.ALIGN_CENTER);
            c2.setVerticalAlignment(Element.ALIGN_BOTTOM);
            footerTable.addCell(c2);

            PdfPCell c3 = new PdfPCell();
            c3.setBorder(0);
            c3.setHorizontalAlignment(Element.ALIGN_CENTER);
            c3.setVerticalAlignment(Element.ALIGN_BOTTOM);
            
            Font signFont = new Font(Font.HELVETICA, 10, Font.BOLDITALIC);
            Paragraph sign = new Paragraph(collectorName, signFont);
            sign.setAlignment(Element.ALIGN_CENTER);
            c3.addElement(sign);
            
            com.lowagie.text.pdf.draw.LineSeparator line = new com.lowagie.text.pdf.draw.LineSeparator(1, 80, java.awt.Color.BLACK, Element.ALIGN_CENTER, -5);
            c3.addElement(line);
            
            Font labelFont = new Font(Font.HELVETICA, 9, Font.NORMAL);
            Paragraph label = new Paragraph("Authorized Signatory", labelFont);
            label.setAlignment(Element.ALIGN_CENTER);
            label.setSpacingBefore(5f);
            c3.addElement(label);
            
            footerTable.addCell(c3);

            document.add(footerTable);

            document.close();
            
            String publicUrl = com.mandal.service.SupabaseStorageService.uploadFile(fileName, baos.toByteArray(), "application/pdf");
            System.out.println("[ReceiptPdfGenerator] Uploaded PDF to Supabase: " + publicUrl);
            
            return publicUrl;

        } catch (Exception e) {
            System.err.println("[ReceiptPdfGenerator] Error generating PDF: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private static void addTableRow(PdfPTable table, String label, String value, boolean isZebra) {
        Font bold = new Font(Font.HELVETICA, 11, Font.BOLD);
        Font normal = new Font(Font.HELVETICA, 11, Font.NORMAL);
        
        PdfPCell cell1 = new PdfPCell(new Paragraph(label, bold));
        cell1.setBorderWidth(0);
        cell1.setBorderWidthBottom(0.5f);
        cell1.setBorderColorBottom(java.awt.Color.LIGHT_GRAY);
        cell1.setPadding(8);
        if (isZebra) cell1.setBackgroundColor(new java.awt.Color(248, 248, 248));
        
        PdfPCell cell2 = new PdfPCell(new Paragraph(value, normal));
        cell2.setBorderWidth(0);
        cell2.setBorderWidthBottom(0.5f);
        cell2.setBorderColorBottom(java.awt.Color.LIGHT_GRAY);
        cell2.setPadding(8);
        if (isZebra) cell2.setBackgroundColor(new java.awt.Color(248, 248, 248));
        
        table.addCell(cell1);
        table.addCell(cell2);
    }
}
