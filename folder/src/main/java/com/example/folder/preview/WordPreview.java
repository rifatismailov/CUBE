package com.example.folder.preview;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

/**
 * Клас для створення прев'ю (зображення) з DOCX-документів.
 */
public class WordPreview {

    /**
     * Рендерить перші кілька абзаців документа DOCX у Bitmap-зображення.
     *
     * @param file   Файл DOCX, з якого буде створено прев'ю.
     * @param width  Ширина зображення.
     * @param height Висота зображення.
     * @return Bitmap, що містить текст з документа, або null у разі помилки.
     */
    public static Bitmap renderDocxToBitmap(File file, int width, int height) {
        try {
            // Відкрити документ
            FileInputStream fis = new FileInputStream(file);
            XWPFDocument document = new XWPFDocument(fis);

            // Отримати всі абзаци з документа
            List<XWPFParagraph> paragraphs = document.getParagraphs();

            // Створити Bitmap для рендерингу тексту
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);

            // Заповнити фон білим кольором
            canvas.drawColor(Color.WHITE);

            // Налаштувати параметри тексту
            Paint paint = new Paint();
            paint.setColor(Color.BLACK);
            paint.setTextSize(40);
            paint.setAntiAlias(true);

            int x = 10; // Початкова координата X
            int y = 50; // Початкова координата Y

            // Додати текст на зображення (рендеримо лише перші кілька абзаців)
            for (XWPFParagraph paragraph : paragraphs) {
                String text = paragraph.getText();
                if (!text.isEmpty()) {
                    canvas.drawText(text, x, y, paint);
                    y += 60; // Перехід до наступного рядка
                    if (y > height - 60) break; // Зупинити, якщо текст виходить за межі
                }
            }

            // Закрити документ
            document.close();

            return bitmap;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
