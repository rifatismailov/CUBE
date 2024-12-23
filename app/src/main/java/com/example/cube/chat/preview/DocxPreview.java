package com.example.cube.chat.preview;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

public class DocxPreview {

    public static String getFirstParagraph(File file) {
        try {
            FileInputStream fis = new FileInputStream(file);
            XWPFDocument document = new XWPFDocument(fis);
            List<XWPFParagraph> paragraphs = document.getParagraphs();

            if (!paragraphs.isEmpty()) {
                return paragraphs.get(0).getText();
            }
            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "No preview available";
    }
}

