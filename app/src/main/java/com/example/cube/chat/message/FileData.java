package com.example.cube.chat.message;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.example.cube.draw.HashBitmapGenerator;
import com.example.cube.chat.preview.PdfPreview;
import com.example.cube.chat.preview.WordPreview;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileData {
    private byte[] imageBytes;
    private int width;
    private int height;
    private static int newWidth=320;
    private static int newHeight=480;

    public FileData() {
    }

    public FileData(byte[] imageBytes, int width, int height) {
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

    public String getFileSize(File file) {
        if (file.exists() && file.isFile()) {
            long sizeInBytes = file.length();
            double sizeInMB = (double) sizeInBytes / (1024 * 1024);
            return String.format("%.2f MB", sizeInMB);
        } else {
            return "File not found or not accessible";
        }
    }

    public String getFileType(File file) {
        if (file.exists() && file.isFile()) {
            String fileName = file.getName();
            int dotIndex = fileName.lastIndexOf('.');
            if (dotIndex != -1 && dotIndex < fileName.length() - 1) {
                return fileName.substring(dotIndex + 1).toUpperCase();
            } else {
                return "Unknown";
            }
        } else {
            return "File not found or not accessible";
        }
    }

    public String getFileDate(File file) {
        if (file.exists() && file.isFile()) {
            long lastModified = file.lastModified();
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
            return sdf.format(new Date(lastModified));
        } else {
            return "File not found or not accessible";
        }
    }

    public FileData convertImage(String url) throws IOException {
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

        return new FileData(stream.toByteArray(), width, height);
    }

    public FileData convertFilePreview(String fileName, String hash) throws IOException {
        Bitmap bitmap = HashBitmapGenerator.generateHashBitmap(fileName, hash, newWidth, newHeight);
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
        return new FileData(stream.toByteArray(), width, height);
    }

    public FileData convertFilePreviewLocal(String fileName, String url, String hash) throws IOException {
        Bitmap bitmap = null;
        if (url.endsWith(".pdf")) {
            bitmap = PdfPreview.getPdfPreview(new File(url), 0, newWidth, newHeight);
        } else if (url.endsWith(".docx")) {
            bitmap = WordPreview.renderDocxToBitmap(new File(url), newWidth, newHeight); // Ширина та висота прев'ю
            if (bitmap == null) {
                bitmap = HashBitmapGenerator.generateHashBitmap(fileName, hash, newWidth, newHeight);
            }
        } else {
            bitmap = HashBitmapGenerator.generateHashBitmap(fileName, hash, newWidth, newHeight);
        }

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
        return new FileData(stream.toByteArray(), width, height);
    }


    public static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (drawable instanceof VectorDrawable) {
            VectorDrawable vectorDrawable = (VectorDrawable) drawable;
            Bitmap bitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            vectorDrawable.draw(canvas);
            return bitmap;
        } else {
            Log.e("VectorToBitmap", "Drawable is not a vector drawable");
            return null;
        }
    }

}
