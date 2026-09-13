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

public class PdfTest {
    public static void main(String[] args) throws Exception {
        System.out.println("Generating dummy PDF...");
        
        // Copied from the main logic but writing to file instead
        String fileName = "test-receipt.pdf";
        Document document = new Document(com.lowagie.text.PageSize.A5, 20, 20, 20, 20);
        java.io.FileOutputStream fos = new java.io.FileOutputStream(fileName);
        PdfWriter.getInstance(document, fos);
        document.open();

        Font titleFont = new Font(Font.HELVETICA, 16, Font.BOLD, new Color(30, 41, 59));
        Font subtitleFont = new Font(Font.HELVETICA, 10, Font.NORMAL, new Color(100, 116, 139));
        Font labelFont = new Font(Font.HELVETICA, 9, Font.BOLD, new Color(100, 116, 139));
        Font valueFont = new Font(Font.HELVETICA, 11, Font.BOLD, new Color(15, 23, 42));
        Font valueNormal = new Font(Font.HELVETICA, 11, Font.NORMAL, new Color(15, 23, 42));
        Font amountFont = new Font(Font.HELVETICA, 18, Font.BOLD, new Color(249, 115, 22)); // Orange Accent
        Font footerFont = new Font(Font.HELVETICA, 8, Font.ITALIC, new Color(148, 163, 184));

        PdfPTable wrapperTable = new PdfPTable(1);
        wrapperTable.setWidthPercentage(100);
        
        PdfPCell wrapperCell = new PdfPCell();
        wrapperCell.setBorderWidth(1f);
        wrapperCell.setBorderColor(new Color(226, 232, 240));
        wrapperCell.setPadding(20);
        wrapperCell.setBackgroundColor(Color.WHITE);

        // --- HEADER ---
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{2.5f, 1.5f});

        PdfPCell leftHeader = new PdfPCell();
        leftHeader.setBorder(0);
        leftHeader.setVerticalAlignment(Element.ALIGN_TOP);
        
        Paragraph orgName = new Paragraph("Test Mandal Name", titleFont);
        orgName.setSpacingAfter(4);
        leftHeader.addElement(orgName);
        leftHeader.addElement(new Paragraph("Official Payment Receipt", subtitleFont));
        
        headerTable.addCell(leftHeader);

        PdfPCell rightHeader = new PdfPCell();
        rightHeader.setBorder(0);
        rightHeader.setHorizontalAlignment(Element.ALIGN_RIGHT);
        rightHeader.setVerticalAlignment(Element.ALIGN_TOP);

        Paragraph receiptTitle = new Paragraph("RECEIPT", new Font(Font.HELVETICA, 14, Font.BOLD, new Color(249, 115, 22)));
        receiptTitle.setAlignment(Element.ALIGN_RIGHT);
        receiptTitle.setSpacingAfter(8);
        rightHeader.addElement(receiptTitle);

        Paragraph noPara = new Paragraph();
        noPara.setAlignment(Element.ALIGN_RIGHT);
        noPara.add(new Phrase("Receipt No: ", labelFont));
        noPara.add(new Phrase("GM-2026-00013", valueNormal));
        rightHeader.addElement(noPara);

        Paragraph datePara = new Paragraph();
        datePara.setAlignment(Element.ALIGN_RIGHT);
        datePara.add(new Phrase("Date: ", labelFont));
        datePara.add(new Phrase("10 Sep 2026", valueNormal));
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

        Paragraph memberNamePara = new Paragraph("John Doe Test User", valueFont);
        memberNamePara.setSpacingAfter(6);
        wrapperCell.addElement(memberNamePara);

        Paragraph roomInfo = new Paragraph("Room No. 101 • Floor 1", valueNormal);
        roomInfo.setSpacingAfter(15);
        wrapperCell.addElement(roomInfo);

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
        modePara.add(new Phrase("UPI", valueNormal));
        modePara.setSpacingBefore(4);
        descValCell.addElement(modePara);
        detailsTable.addCell(descValCell);

        PdfPCell amtValCell = new PdfPCell();
        amtValCell.setBorder(0);
        amtValCell.setPaddingTop(12);
        amtValCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        Paragraph amtValPara = new Paragraph("₹500", valueNormal);
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
        wordsCell.addElement(new Paragraph("Rupees Five Hundred Only", valueNormal));
        amountTable.addCell(wordsCell);

        PdfPCell bigAmtCell = new PdfPCell();
        bigAmtCell.setBorder(0);
        bigAmtCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        bigAmtCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        Paragraph bigAmtPara = new Paragraph("₹500.00", amountFont);
        bigAmtPara.setAlignment(Element.ALIGN_RIGHT);
        bigAmtCell.addElement(bigAmtPara);
        amountTable.addCell(bigAmtCell);

        wrapperCell.addElement(amountTable);

        // --- BOTTOM SECTION ---
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
        
        Paragraph signName = new Paragraph("Admin Collector", valueNormal);
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

        wrapperTable.addCell(wrapperCell);
        document.add(wrapperTable);
        document.close();
        System.out.println("Done! Saved as " + fileName);
    }
}
