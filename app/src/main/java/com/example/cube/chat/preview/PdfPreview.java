package com.example.cube.chat.preview;

import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;

import java.io.File;

public class PdfPreview {

    public static Bitmap getPdfFirstPage(File file, int pageIndex) {
        try {
            // Отримати дескриптор файлу
            ParcelFileDescriptor fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
            PdfRenderer pdfRenderer = new PdfRenderer(fileDescriptor);

            // Відкрити сторінку PDF
            PdfRenderer.Page page = pdfRenderer.openPage(pageIndex);

            // Створити зображення для прев'ю
            Bitmap bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(), Bitmap.Config.ARGB_8888);
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
