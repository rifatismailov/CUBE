package com.example.cube.chat.preview;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

public class WordPreview {

    public static Bitmap renderDocxToBitmap(File file, int width, int height) {
        try {
            // Відкрити документ
            FileInputStream fis = new FileInputStream(file);
            XWPFDocument document = new XWPFDocument(fis);

            // Отримати текст з документа
            List<XWPFParagraph> paragraphs = document.getParagraphs();

            // Створити Bitmap для рендерингу
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);

            // Налаштувати фон
            canvas.drawColor(Color.WHITE);

            // Налаштувати стиль тексту
            Paint paint = new Paint();
            paint.setColor(Color.BLACK);
            paint.setTextSize(40);
            paint.setAntiAlias(true);

            int x = 10; // Початкова координата X
            int y = 50; // Початкова координата Y

            // Відрендерити перші кілька абзаців (або весь текст)
            for (XWPFParagraph paragraph : paragraphs) {
                String text = paragraph.getText();
                canvas.drawText(text, x, y, paint);

                // Перейти до наступного рядка
                y += 60;
                if (y > height - 60) break; // Зупинити, якщо текст виходить за межі
            }

            document.close();
            return bitmap;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
