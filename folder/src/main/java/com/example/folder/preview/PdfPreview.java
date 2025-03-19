package com.example.folder.preview;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;

import java.io.File;

/**
 * Клас для генерації прев'ю з PDF-файлів.
 */
public class PdfPreview {

    /**
     * Отримує прев'ю для вказаної сторінки PDF-документа.
     *
     * @param file      PDF-файл, з якого потрібно отримати прев'ю.
     * @param pageIndex Індекс сторінки (починається з 0).
     * @param width     Ширина зображення прев'ю.
     * @param height    Висота зображення прев'ю.
     * @return Bitmap із зображенням сторінки PDF або null у разі помилки.
     */
    public static Bitmap getPdfPreview(File file, int pageIndex, int width, int height) {
        try {
            // Отримати дескриптор файлу
            ParcelFileDescriptor fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
            PdfRenderer pdfRenderer = new PdfRenderer(fileDescriptor);

            // Перевірка коректності індексу сторінки
            if (pageIndex < 0 || pageIndex >= pdfRenderer.getPageCount()) {
                pdfRenderer.close();
                fileDescriptor.close();
                return null;
            }

            // Відкрити сторінку PDF
            PdfRenderer.Page page = pdfRenderer.openPage(pageIndex);

            // Створити зображення для прев'ю
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

            // Залити фон білим кольором
            Canvas canvas = new Canvas(bitmap);
            canvas.drawColor(Color.WHITE);

            // Відрендерити сторінку PDF
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

            // Закрити ресурси
            page.close();
            pdfRenderer.close();
            fileDescriptor.close();

            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}