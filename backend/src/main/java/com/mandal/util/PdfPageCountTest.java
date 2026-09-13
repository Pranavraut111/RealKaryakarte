package com.mandal.util;

import com.lowagie.text.pdf.PdfReader;

public class PdfPageCountTest {
    public static void main(String[] args) throws Exception {
        PdfReader reader = new PdfReader("test-receipt.pdf");
        System.out.println("PAGE_COUNT=" + reader.getNumberOfPages());
        reader.close();
    }
}
