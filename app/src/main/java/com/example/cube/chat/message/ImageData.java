package com.example.cube.chat.message;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class ImageData {
    private byte[] imageBytes;
    private int width;
    private int height;

    public ImageData() {
    }

    public ImageData(byte[] imageBytes, int width, int height) {
        this.imageBytes = imageBytes;
        this.width = width;
        this.height = height;
    }

    public byte[] getImageBytes() {
        return imageBytes;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public ImageData convertImage(String url) throws IOException {
        // Перевірка на існування файлу
        File file = new File(url);
        if (!file.exists()) {
            throw new IllegalArgumentException("File does not exist: " + url);
        }

        // Налаштування для зменшення розміру зображення
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;  // Отримуємо лише розміри зображення без завантаження в пам'ять
        BitmapFactory.decodeFile(url, options);

        // Розрахунок масштабу
        int inSampleSize = 1;
        if (options.outHeight > 1000 || options.outWidth > 1000) {
            final int halfHeight = options.outHeight / 2;
            final int halfWidth = options.outWidth / 2;
            while ((halfHeight / inSampleSize) > 1000 && (halfWidth / inSampleSize) > 1000) {
                inSampleSize *= 2;
            }
        }
        options.inSampleSize = inSampleSize;
        options.inJustDecodeBounds = false; // Тепер можна завантажувати зображення

        // Декодуємо зображення з налаштуванням inSampleSize
        Bitmap bitmap = BitmapFactory.decodeFile(url, options);

        if (bitmap == null) {
            throw new IOException("Failed to decode image from URL: " + url);
        }

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        // Компресія в JPEG з якістю 80%
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);

        // Закриваємо потік після використання
        try {
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ImageData(stream.toByteArray(), width, height);
    }
}
