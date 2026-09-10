package com.mandal.util;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Color;

/**
 * PDF generator for contribution receipts using OpenPDF.
 * Generates a traditional Indian receipt book style PDF.
 */
public class ReceiptPdfGenerator {

    // Colors
    private static final Color BORDER_COLOR = new Color(139, 69, 19); // Dark brown border
    private static final Color HEADER_BG = new Color(255, 248, 240); // Warm cream
    private static final Color BODY_BG = new Color(255, 253, 250); // Off-white
    private static final Color ACCENT = new Color(249, 115, 22); // Orange accent

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

        Document document = new Document(com.lowagie.text.PageSize.A5.rotate(), 30, 30, 30, 30);
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            // ─── Outer Border Table ────────────────────────────────────
            PdfPTable outerTable = new PdfPTable(1);
            outerTable.setWidthPercentage(100);

            PdfPCell outerCell = new PdfPCell();
            outerCell.setBorderWidth(2.5f);
            outerCell.setBorderColor(BORDER_COLOR);
            outerCell.setPadding(15);
            outerCell.setBackgroundColor(BODY_BG);

            // ─── Inner Border Table ────────────────────────────────────
            PdfPTable innerTable = new PdfPTable(1);
            innerTable.setWidthPercentage(100);

            PdfPCell innerCell = new PdfPCell();
            innerCell.setBorderWidth(1f);
            innerCell.setBorderColor(BORDER_COLOR);
            innerCell.setPadding(20);

            // ═══ HEADER SECTION ═══════════════════════════════════════
            PdfPTable headerRow = new PdfPTable(2);
            headerRow.setWidthPercentage(100);
            headerRow.setWidths(new float[]{3f, 2f});

            // Left: Logo + Mandal Name
            PdfPCell leftHeader = new PdfPCell();
            leftHeader.setBorder(0);
            leftHeader.setVerticalAlignment(Element.ALIGN_MIDDLE);

            try {
                com.lowagie.text.Image logo = com.lowagie.text.Image.getInstance("/Users/pranavraut/RealKaryakarte/frontend/public/recieptlogo.png");
                logo.scaleToFit(50, 50);
                PdfPTable logoNameTable = new PdfPTable(2);
                logoNameTable.setWidths(new float[]{1f, 4f});

                PdfPCell logoCell = new PdfPCell(logo);
                logoCell.setBorder(0);
                logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                logoCell.setPaddingRight(8);
                logoNameTable.addCell(logoCell);

                Font mandalFont = new Font(Font.HELVETICA, 14, Font.BOLD, BORDER_COLOR);
                PdfPCell nameCell = new PdfPCell(new Phrase(mandalName, mandalFont));
                nameCell.setBorder(0);
                nameCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                logoNameTable.addCell(nameCell);

                leftHeader.addElement(logoNameTable);
            } catch (Exception e) {
                Font mandalFont = new Font(Font.HELVETICA, 16, Font.BOLD, BORDER_COLOR);
                leftHeader.addElement(new Paragraph(mandalName, mandalFont));
            }

            headerRow.addCell(leftHeader);

            // Right: Receipt No + Date boxes
            PdfPCell rightHeader = new PdfPCell();
            rightHeader.setBorder(0);
            rightHeader.setHorizontalAlignment(Element.ALIGN_RIGHT);

            PdfPTable noDateTable = new PdfPTable(2);
            noDateTable.setWidths(new float[]{1f, 1f});

            Font labelSmall = new Font(Font.HELVETICA, 8, Font.BOLD, Color.GRAY);
            Font valueSmall = new Font(Font.HELVETICA, 10, Font.BOLD, Color.BLACK);

            // No. box
            PdfPCell noLabelCell = new PdfPCell(new Phrase("No.", labelSmall));
            noLabelCell.setBorder(0);
            noLabelCell.setPaddingBottom(2);
            noDateTable.addCell(noLabelCell);

            PdfPCell dateLabelCell = new PdfPCell(new Phrase("Date", labelSmall));
            dateLabelCell.setBorder(0);
            dateLabelCell.setPaddingBottom(2);
            noDateTable.addCell(dateLabelCell);

            PdfPCell noValueCell = new PdfPCell(new Phrase(receiptNo, valueSmall));
            noValueCell.setBorderWidth(1f);
            noValueCell.setBorderColor(Color.GRAY);
            noValueCell.setPadding(6);
            noValueCell.setBackgroundColor(HEADER_BG);
            noDateTable.addCell(noValueCell);

            PdfPCell dateValueCell = new PdfPCell(new Phrase(date, valueSmall));
            dateValueCell.setBorderWidth(1f);
            dateValueCell.setBorderColor(Color.GRAY);
            dateValueCell.setPadding(6);
            dateValueCell.setBackgroundColor(HEADER_BG);
            noDateTable.addCell(dateValueCell);

            rightHeader.addElement(noDateTable);
            headerRow.addCell(rightHeader);

            innerCell.addElement(headerRow);

            // Separator line
            innerCell.addElement(new Paragraph("\n"));
            com.lowagie.text.pdf.draw.LineSeparator sep = new com.lowagie.text.pdf.draw.LineSeparator(1, 100, BORDER_COLOR, Element.ALIGN_CENTER, -2);
            Paragraph sepPara = new Paragraph();
            sepPara.add(sep);
            innerCell.addElement(sepPara);
            innerCell.addElement(new Paragraph("\n"));

            // ═══ RECEIPT TITLE ════════════════════════════════════════
            Font receiptTitleFont = new Font(Font.HELVETICA, 12, Font.BOLD, ACCENT);
            Paragraph receiptTitle = new Paragraph("RECEIPT / पावती", receiptTitleFont);
            receiptTitle.setAlignment(Element.ALIGN_CENTER);
            receiptTitle.setSpacingAfter(15);
            innerCell.addElement(receiptTitle);

            // ═══ BODY — Traditional format ════════════════════════════
            Font bodyFont = new Font(Font.HELVETICA, 11, Font.NORMAL, Color.BLACK);
            Font bodyBold = new Font(Font.HELVETICA, 11, Font.BOLD, Color.BLACK);
            Font bodyUnderline = new Font(Font.HELVETICA, 11, Font.UNDERLINE | Font.BOLD, ACCENT);

            // "RECEIVED with thanks from ___"
            Paragraph line1 = new Paragraph();
            line1.add(new Phrase("RECEIVED with thanks from  ", bodyFont));
            line1.add(new Phrase(memberName, bodyUnderline));
            line1.setSpacingAfter(12);
            innerCell.addElement(line1);

            // Room info if available
            if (roomNumber != null && !roomNumber.isBlank()) {
                String floorLabel = (floorNumber != null && floorNumber == 0) ? "Owner" : (floorNumber != null ? "Floor " + floorNumber : "");
                Paragraph roomLine = new Paragraph();
                roomLine.add(new Phrase("Room No.  ", bodyFont));
                roomLine.add(new Phrase(roomNumber + "  ·  " + floorLabel, bodyBold));
                roomLine.setSpacingAfter(12);
                innerCell.addElement(roomLine);
            }

            // "the sum of Rupees ___"
            String amountInWords = numberToWords(Long.parseLong(amount.replace(",", "").split("\\.")[0]));
            Paragraph line2 = new Paragraph();
            line2.add(new Phrase("the sum of Rupees  ", bodyFont));
            line2.add(new Phrase(amountInWords, bodyBold));
            line2.setSpacingAfter(8);
            innerCell.addElement(line2);

            // Amount box
            Font amountFont = new Font(Font.HELVETICA, 16, Font.BOLD, ACCENT);
            Paragraph amountPara = new Paragraph();
            amountPara.add(new Phrase("₹ ", amountFont));
            amountPara.add(new Phrase(amount + " /-", amountFont));
            amountPara.setAlignment(Element.ALIGN_RIGHT);
            amountPara.setSpacingAfter(12);
            innerCell.addElement(amountPara);

            // "by cheque / draft / cash, in full / part / advance"
            Paragraph line3 = new Paragraph();
            line3.add(new Phrase("by  ", bodyFont));
            line3.add(new Phrase(paymentMethod, bodyBold));
            line3.add(new Phrase("  ,  in full", bodyFont));
            line3.setSpacingAfter(20);
            innerCell.addElement(line3);

            // ═══ FOOTER — Stamp + Signature ══════════════════════════
            PdfPTable footerTable = new PdfPTable(3);
            footerTable.setWidthPercentage(100);
            footerTable.setWidths(new float[]{2f, 1.5f, 2f});

            // Mandal stamp area
            PdfPCell stampCell = new PdfPCell();
            stampCell.setBorder(0);
            stampCell.setVerticalAlignment(Element.ALIGN_BOTTOM);

            Font stampFont = new Font(Font.HELVETICA, 8, Font.BOLD, BORDER_COLOR);
            Paragraph stampText = new Paragraph(mandalName, stampFont);
            stampText.setAlignment(Element.ALIGN_CENTER);
            stampCell.addElement(stampText);

            com.lowagie.text.pdf.draw.LineSeparator stampLine = new com.lowagie.text.pdf.draw.LineSeparator(0.5f, 90, Color.GRAY, Element.ALIGN_CENTER, -3);
            Paragraph stampLinePara = new Paragraph();
            stampLinePara.add(stampLine);
            stampCell.addElement(stampLinePara);

            Font tinyFont = new Font(Font.HELVETICA, 7, Font.ITALIC, Color.GRAY);
            Paragraph stampLabel = new Paragraph("Mandal Seal", tinyFont);
            stampLabel.setAlignment(Element.ALIGN_CENTER);
            stampLabel.setSpacingBefore(3);
            stampCell.addElement(stampLabel);

            footerTable.addCell(stampCell);

            // Center — Thank you message
            PdfPCell centerCell = new PdfPCell();
            centerCell.setBorder(0);
            centerCell.setVerticalAlignment(Element.ALIGN_BOTTOM);

            Font thankFont = new Font(Font.HELVETICA, 7, Font.ITALIC, Color.GRAY);
            Paragraph thankText = new Paragraph("|| श्री गणेशाय नमः ||\nThank you for your\ngenerous contribution!", thankFont);
            thankText.setAlignment(Element.ALIGN_CENTER);
            centerCell.addElement(thankText);
            footerTable.addCell(centerCell);

            // Right — Authorized Signatory
            PdfPCell signCell = new PdfPCell();
            signCell.setBorder(0);
            signCell.setVerticalAlignment(Element.ALIGN_BOTTOM);

            Font signNameFont = new Font(Font.HELVETICA, 10, Font.BOLDITALIC, Color.BLACK);
            Paragraph signName = new Paragraph(collectorName != null ? collectorName : "", signNameFont);
            signName.setAlignment(Element.ALIGN_CENTER);
            signCell.addElement(signName);

            com.lowagie.text.pdf.draw.LineSeparator signLine = new com.lowagie.text.pdf.draw.LineSeparator(1, 80, Color.BLACK, Element.ALIGN_CENTER, -3);
            Paragraph signLinePara = new Paragraph();
            signLinePara.add(signLine);
            signCell.addElement(signLinePara);

            Font signLabelFont = new Font(Font.HELVETICA, 8, Font.NORMAL, Color.GRAY);
            Paragraph signLabel = new Paragraph("Authorized Signatory", signLabelFont);
            signLabel.setAlignment(Element.ALIGN_CENTER);
            signLabel.setSpacingBefore(3);
            signCell.addElement(signLabel);

            footerTable.addCell(signCell);
            innerCell.addElement(footerTable);

            // ─── Note at bottom ────────────────────────────────────────
            innerCell.addElement(new Paragraph("\n"));
            Font noteFont = new Font(Font.HELVETICA, 6, Font.ITALIC, Color.GRAY);
            Paragraph note = new Paragraph("This is a system generated receipt. · Subject to realisation of cheque.", noteFont);
            note.setAlignment(Element.ALIGN_CENTER);
            innerCell.addElement(note);

            // Assemble
            innerTable.addCell(innerCell);
            outerCell.addElement(innerTable);
            outerTable.addCell(outerCell);
            document.add(outerTable);

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

    /**
     * Convert a number to words (Indian English style).
     * e.g., 1100 → "One Thousand One Hundred"
     */
    private static String numberToWords(long n) {
        if (n == 0) return "Zero";

        String[] ones = {"", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine",
                "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen",
                "Seventeen", "Eighteen", "Nineteen"};
        String[] tens = {"", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"};

        if (n < 0) return "Minus " + numberToWords(-n);

        StringBuilder sb = new StringBuilder();

        // Crore
        if (n >= 10000000) {
            sb.append(numberToWords(n / 10000000)).append(" Crore ");
            n %= 10000000;
        }
        // Lakh
        if (n >= 100000) {
            sb.append(numberToWords(n / 100000)).append(" Lakh ");
            n %= 100000;
        }
        // Thousand
        if (n >= 1000) {
            sb.append(numberToWords(n / 1000)).append(" Thousand ");
            n %= 1000;
        }
        // Hundred
        if (n >= 100) {
            sb.append(ones[(int)(n / 100)]).append(" Hundred ");
            n %= 100;
        }
        // Tens and ones
        if (n >= 20) {
            sb.append(tens[(int)(n / 10)]);
            if (n % 10 != 0) sb.append(" ").append(ones[(int)(n % 10)]);
        } else if (n > 0) {
            sb.append(ones[(int)n]);
        }

        return sb.toString().trim();
    }
}
