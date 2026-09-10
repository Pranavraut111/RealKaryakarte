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

        // Use A5 Portrait with smaller margins to ensure everything fits on 1 page
        Document document = new Document(com.lowagie.text.PageSize.A5, 20, 20, 20, 20);
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            // Fonts
            Font titleFont = new Font(Font.HELVETICA, 16, Font.BOLD, new Color(30, 41, 59));
            Font subtitleFont = new Font(Font.HELVETICA, 10, Font.NORMAL, new Color(100, 116, 139));
            Font labelFont = new Font(Font.HELVETICA, 9, Font.BOLD, new Color(100, 116, 139));
            Font valueFont = new Font(Font.HELVETICA, 11, Font.BOLD, new Color(15, 23, 42));
            Font valueNormal = new Font(Font.HELVETICA, 11, Font.NORMAL, new Color(15, 23, 42));
            Font amountFont = new Font(Font.HELVETICA, 18, Font.BOLD, new Color(249, 115, 22)); // Orange Accent
            Font footerFont = new Font(Font.HELVETICA, 8, Font.ITALIC, new Color(148, 163, 184));

            // Main wrapper table
            PdfPTable wrapperTable = new PdfPTable(1);
            wrapperTable.setWidthPercentage(100);
            
            PdfPCell wrapperCell = new PdfPCell();
            wrapperCell.setBorderWidth(1f);
            wrapperCell.setBorderColor(new Color(226, 232, 240)); // Light gray border
            wrapperCell.setPadding(20);
            wrapperCell.setBackgroundColor(Color.WHITE);

            // --- HEADER ---
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{2.5f, 1.5f});

            // Left Header: Logo & Org Name
            PdfPCell leftHeader = new PdfPCell();
            leftHeader.setBorder(0);
            leftHeader.setVerticalAlignment(Element.ALIGN_TOP);
            
            Paragraph orgName = new Paragraph(mandalName, titleFont);
            orgName.setSpacingAfter(4);
            leftHeader.addElement(orgName);
            leftHeader.addElement(new Paragraph("Official Payment Receipt", subtitleFont));
            
            headerTable.addCell(leftHeader);

            // Right Header: Receipt & Date Info
            PdfPCell rightHeader = new PdfPCell();
            rightHeader.setBorder(0);
            rightHeader.setHorizontalAlignment(Element.ALIGN_RIGHT);
            rightHeader.setVerticalAlignment(Element.ALIGN_TOP);

            Paragraph receiptTitle = new Paragraph("RECEIPT", new Font(Font.HELVETICA, 14, Font.BOLD, ACCENT));
            receiptTitle.setAlignment(Element.ALIGN_RIGHT);
            receiptTitle.setSpacingAfter(8);
            rightHeader.addElement(receiptTitle);

            Paragraph noPara = new Paragraph();
            noPara.setAlignment(Element.ALIGN_RIGHT);
            noPara.add(new Phrase("Receipt No: ", labelFont));
            noPara.add(new Phrase(receiptNo, valueNormal));
            rightHeader.addElement(noPara);

            Paragraph datePara = new Paragraph();
            datePara.setAlignment(Element.ALIGN_RIGHT);
            datePara.add(new Phrase("Date: ", labelFont));
            datePara.add(new Phrase(date, valueNormal));
            rightHeader.addElement(datePara);

            headerTable.addCell(rightHeader);
            wrapperCell.addElement(headerTable);

            // --- SEPARATOR ---
            com.lowagie.text.pdf.draw.LineSeparator sep = new com.lowagie.text.pdf.draw.LineSeparator(1f, 100, new Color(226, 232, 240), Element.ALIGN_CENTER, -2);
            Paragraph sepPara = new Paragraph();
            sepPara.add(sep);
            sepPara.setSpacingBefore(10);
            sepPara.setSpacingAfter(15);
            wrapperCell.addElement(sepPara);

            // --- RECEIVED FROM ---
            Paragraph receivedFromLabel = new Paragraph("RECEIVED WITH THANKS FROM", labelFont);
            receivedFromLabel.setSpacingAfter(4);
            wrapperCell.addElement(receivedFromLabel);

            Paragraph memberNamePara = new Paragraph(memberName, valueFont);
            memberNamePara.setSpacingAfter(6);
            wrapperCell.addElement(memberNamePara);

            if (roomNumber != null && !roomNumber.trim().isEmpty()) {
                String floorStr = (floorNumber != null && floorNumber == 0) ? "Owner" : (floorNumber != null ? "Floor " + floorNumber : "");
                Paragraph roomInfo = new Paragraph("Room No. " + roomNumber + (floorStr.isEmpty() ? "" : " • " + floorStr), valueNormal);
                roomInfo.setSpacingAfter(15);
                wrapperCell.addElement(roomInfo);
            } else {
                Paragraph spacer = new Paragraph(" ");
                spacer.setSpacingAfter(10);
                wrapperCell.addElement(spacer);
            }

            // --- PAYMENT DETAILS ---
            PdfPTable detailsTable = new PdfPTable(2);
            detailsTable.setWidthPercentage(100);
            detailsTable.setWidths(new float[]{3f, 1f});
            detailsTable.setSpacingAfter(15);

            PdfPCell descCell = new PdfPCell(new Phrase("Description", labelFont));
            descCell.setBorderColor(new Color(226, 232, 240));
            descCell.setBorderWidthTop(1f);
            descCell.setBorderWidthBottom(1f);
            descCell.setBorderWidthLeft(0);
            descCell.setBorderWidthRight(0);
            descCell.setPaddingTop(8);
            descCell.setPaddingBottom(8);
            detailsTable.addCell(descCell);

            PdfPCell amtCell = new PdfPCell(new Phrase("Amount", labelFont));
            amtCell.setBorderColor(new Color(226, 232, 240));
            amtCell.setBorderWidthTop(1f);
            amtCell.setBorderWidthBottom(1f);
            amtCell.setBorderWidthLeft(0);
            amtCell.setBorderWidthRight(0);
            amtCell.setPaddingTop(8);
            amtCell.setPaddingBottom(8);
            amtCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            detailsTable.addCell(amtCell);

            PdfPCell descValCell = new PdfPCell();
            descValCell.setBorder(0);
            descValCell.setPaddingTop(12);
            descValCell.addElement(new Paragraph("Contribution / Vargani", valueNormal));
            Paragraph modePara = new Paragraph();
            modePara.add(new Phrase("Payment Mode: ", labelFont));
            modePara.add(new Phrase(paymentMethod, valueNormal));
            modePara.setSpacingBefore(4);
            descValCell.addElement(modePara);
            detailsTable.addCell(descValCell);

            PdfPCell amtValCell = new PdfPCell();
            amtValCell.setBorder(0);
            amtValCell.setPaddingTop(12);
            amtValCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            Paragraph amtValPara = new Paragraph("₹" + amount, valueNormal);
            amtValPara.setAlignment(Element.ALIGN_RIGHT);
            amtValCell.addElement(amtValPara);
            detailsTable.addCell(amtValCell);

            wrapperCell.addElement(detailsTable);

            // --- AMOUNT HIGHLIGHT ---
            PdfPTable amountTable = new PdfPTable(2);
            amountTable.setWidthPercentage(100);
            amountTable.setWidths(new float[]{2.5f, 1.5f});
            amountTable.setSpacingAfter(25);

            PdfPCell wordsCell = new PdfPCell();
            wordsCell.setBorder(0);
            wordsCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            wordsCell.addElement(new Paragraph("Amount in words:", labelFont));
            String amountInWords = numberToWords(Long.parseLong(amount.replace(",", "").split("\\.")[0]));
            wordsCell.addElement(new Paragraph("Rupees " + amountInWords + " Only", valueNormal));
            amountTable.addCell(wordsCell);

            PdfPCell bigAmtCell = new PdfPCell();
            bigAmtCell.setBorder(0);
            bigAmtCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            bigAmtCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            Paragraph bigAmtPara = new Paragraph("₹" + amount + ".00", amountFont);
            bigAmtPara.setAlignment(Element.ALIGN_RIGHT);
            bigAmtCell.addElement(bigAmtPara);
            amountTable.addCell(bigAmtCell);

            wrapperCell.addElement(amountTable);

            // --- BOTTOM SECTION (Signatures) ---
            PdfPTable footerTable = new PdfPTable(2);
            footerTable.setWidthPercentage(100);
            footerTable.setSpacingBefore(30);

            PdfPCell sealCell = new PdfPCell();
            sealCell.setBorder(0);
            sealCell.setVerticalAlignment(Element.ALIGN_BOTTOM);
            
            Paragraph sealLabel = new Paragraph("[ Stamp / Seal ]", footerFont);
            sealLabel.setAlignment(Element.ALIGN_LEFT);
            sealCell.addElement(sealLabel);
            footerTable.addCell(sealCell);

            PdfPCell signCell = new PdfPCell();
            signCell.setBorder(0);
            signCell.setVerticalAlignment(Element.ALIGN_BOTTOM);
            signCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            
            Paragraph signName = new Paragraph(collectorName != null ? collectorName : "Authorized Signatory", valueNormal);
            signName.setAlignment(Element.ALIGN_RIGHT);
            signName.setSpacingAfter(4);
            signCell.addElement(signName);

            com.lowagie.text.pdf.draw.LineSeparator signLine = new com.lowagie.text.pdf.draw.LineSeparator(1, 100, new Color(200, 200, 200), Element.ALIGN_RIGHT, -2);
            Paragraph signLinePara = new Paragraph();
            signLinePara.add(signLine);
            signCell.addElement(signLinePara);

            Paragraph signLabel = new Paragraph("Authorized Signatory", footerFont);
            signLabel.setAlignment(Element.ALIGN_RIGHT);
            signCell.addElement(signLabel);

            footerTable.addCell(signCell);
            wrapperCell.addElement(footerTable);

            // --- DISCLAIMER ---
            wrapperCell.addElement(new Paragraph("\n"));
            Paragraph disclaimer = new Paragraph("This is a system generated receipt. Subject to realisation of cheque.", footerFont);
            disclaimer.setAlignment(Element.ALIGN_CENTER);
            wrapperCell.addElement(disclaimer);

            // Add wrapper to document
            wrapperTable.addCell(wrapperCell);
            document.add(wrapperTable);

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
