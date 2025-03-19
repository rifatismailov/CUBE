package com.example.folder;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.example.folder.preview.PdfPreview;
import com.example.folder.preview.WordPreview;
import com.example.folder.view.HashBitmapGenerator;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Клас для обробки файлів та їхнього попереднього перегляду.
 */
public class FileData {
    private byte[] imageBytes;
    private int width;
    private int height;
    private final int newWidth = 320;
    private final int newHeight = 480;

    /**
     * Конструктор за замовчуванням.
     */
    public FileData() {
    }

    /**
     * Конструктор, який приймає масив байтів зображення та його розміри.
     *
     * @param imageBytes Масив байтів зображення.
     * @param width      Ширина зображення.
     * @param height     Висота зображення.
     */
    public FileData(byte[] imageBytes, int width, int height) {
        this.imageBytes = imageBytes;
        this.width = width;
        this.height = height;
    }

    /**
     * Повертає байти зображення.
     *
     * @return Масив байтів зображення.
     */
    public byte[] getImageBytes() {
        return imageBytes;
    }

    /**
     * Повертає ширину зображення.
     *
     * @return Ширина зображення.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Повертає висоту зображення.
     *
     * @return Висота зображення.
     */
    public int getHeight() {
        return height;
    }

    /**
     * Отримує розмір файлу у мегабайтах.
     *
     * @param file Файл, розмір якого потрібно отримати.
     * @return Розмір файлу у форматі "X.XX MB" або повідомлення про помилку.
     */
    public String getFileSize(File file) {
        if (file.exists() && file.isFile()) {
            long sizeInBytes = file.length();
            double sizeInMB = (double) sizeInBytes / (1024 * 1024);
            return String.format("%.2f MB", sizeInMB);
        } else {
            return "File not found or not accessible";
        }
    }

    /**
     * Визначає тип файлу за розширенням.
     *
     * @param file Файл, тип якого потрібно визначити.
     * @return Тип файлу (розширення у верхньому регістрі) або "Unknown".
     */
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

    /**
     * Отримує дату останньої модифікації файлу.
     *
     * @param file Файл, для якого потрібно отримати дату.
     * @return Дата у форматі "dd.MM.yyyy HH:mm:ss" або повідомлення про помилку.
     */
    public String getFileDate(File file) {
        if (file.exists() && file.isFile()) {
            long lastModified = file.lastModified();
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
            return sdf.format(new Date(lastModified));
        } else {
            return "File not found or not accessible";
        }
    }

    /**
     * Конвертує зображення за URL у формат JPEG.
     *
     * @param url Шлях до файлу зображення.
     * @return Об'єкт FileData, що містить байти зображення та його розміри.
     * @throws IOException Якщо файл не існує або не вдалося декодувати зображення.
     */
    public FileData convertImage(String url) throws IOException {
        File file = new File(url);
        if (!file.exists()) {
            throw new IllegalArgumentException("File does not exist: " + url);
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(url, options);

        int inSampleSize = 1;
        if (options.outHeight > 1000 || options.outWidth > 1000) {
            final int halfHeight = options.outHeight / 2;
            final int halfWidth = options.outWidth / 2;
            while ((halfHeight / inSampleSize) > 1000 && (halfWidth / inSampleSize) > 1000) {
                inSampleSize *= 2;
            }
        }
        options.inSampleSize = inSampleSize;
        options.inJustDecodeBounds = false;

        Bitmap bitmap = BitmapFactory.decodeFile(url, options);
        if (bitmap == null) {
            throw new IOException("Failed to decode image from URL: " + url);
        }

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
        stream.close();

        return new FileData(stream.toByteArray(), bitmap.getWidth(), bitmap.getHeight());
    }


    /**
     * Генерує прев'ю файлу на основі хешу.
     *
     * @param fileName назва файлу
     * @param hash     хеш файлу
     * @return об'єкт FileData із зображенням прев'ю
     * @throws IOException якщо не вдалося створити прев'ю
     */
    public FileData convertFilePreview(String fileName, String hash) throws IOException {
        Bitmap bitmap = HashBitmapGenerator.generateHashBitmap(fileName, hash, newWidth, newHeight);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        // Компресія в JPEG з якістю 80%
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);

        // Закриваємо потік після використання
        try {
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new FileData(stream.toByteArray(), bitmap.getWidth(), bitmap.getHeight());
    }

    /**
     * Генерує прев'ю локального файлу (PDF або DOCX).
     *
     * @param fileName назва файлу
     * @param url      шлях до файлу
     * @param hash     хеш файлу
     * @return об'єкт FileData із зображенням прев'ю
     * @throws IOException якщо не вдалося створити прев'ю
     */
    public FileData convertFilePreviewLocal(String fileName, String url, String hash) throws IOException {
        Bitmap bitmap;
        if (url.endsWith(".pdf")) {
            bitmap = PdfPreview.getPdfPreview(new File(url), 0, newWidth, newHeight);
        } else if (url.endsWith(".docx")) {
            bitmap = WordPreview.renderDocxToBitmap(new File(url), newWidth, newHeight); // Ширина та висота прев'ю
        } else {
            bitmap = HashBitmapGenerator.generateHashBitmap(fileName, hash, newWidth, newHeight);
        }

        if (bitmap == null) {
            bitmap = HashBitmapGenerator.generateHashBitmap(fileName, hash, newWidth, newHeight);
        }

        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        // Компресія в JPEG з якістю 80%
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);

        // Закриваємо потік після використання
        try {
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new FileData(stream.toByteArray(), bitmap.getWidth(), bitmap.getHeight());
    }

    /**
     * Метод для отримання розширення файлу
     *
     * @param file Файл, для якого потрібно отримати розширення
     */
    public static String getFileExtension(File file) {
        String fileName = file.getName();
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
    }

    public static String getFileHash(String file, String algorithm) {
        try {
            // Створюємо об'єкт MessageDigest для вказаного алгоритму (наприклад, SHA-256)
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            // Створюємо FileInputStream для читання файлу
            FileInputStream fis = new FileInputStream(file);

            // Читаємо файл блоками і обчислюємо хеш
            byte[] byteArray = new byte[1024];
            int bytesCount;
            while ((bytesCount = fis.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesCount);
            }

            // Закриваємо FileInputStream
            fis.close();

            // Отримуємо байтовий масив хешу
            byte[] hashBytes = digest.digest();

            // Конвертуємо байтовий масив у рядок (hex)
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }

            // Повертаємо хеш як рядок
            return sb.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public void saveJsonToFile(File externalDir, String filename, JSONObject jsonObject) {
        // Створюємо каталог "cube" у зовнішньому сховищі

        if (!externalDir.exists()) {
            boolean mkdirs = externalDir.mkdirs(); // Створюємо каталог, якщо його не існує
            if (!mkdirs) {
                Log.e("FileWrite", "Failed to create directory: " + externalDir.getAbsolutePath());
                return; // Виходимо, якщо не вдалося створити каталог
            }
        }

        FileOutputStream fos = null;
        try {
            // Створюємо новий файл у каталозі "cube"
            File file = new File(externalDir, filename);
            fos = new FileOutputStream(file); // Використовуємо FileOutputStream для запису у файл
            fos.write(jsonObject.toString().getBytes());
            Log.d("FileWrite", "JSON saved to file successfully at: " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("FileWrite", "Error writing JSON to file: " + e.getMessage());
        } finally {
            if (fos != null) {
                try {
                    fos.close(); // Закриваємо потік
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public JSONObject readJsonFromFile(File externalDir, String filename) {
        StringBuilder jsonBuilder = new StringBuilder();
        FileInputStream fis = null;

        // Створюємо шлях до файлу в зовнішньому сховищі

        File file = new File(externalDir, filename);

        if (!file.exists()) {
            Log.e("FileRead", "File does not exist: " + file.getAbsolutePath());
            return null; // Файл не існує
        }

        try {
            fis = new FileInputStream(file); // Використовуємо FileInputStream для зовнішнього файлу
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }
            return new JSONObject(jsonBuilder.toString());
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close(); // Закриваємо потік
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null; // Якщо не вдалося прочитати файл або JSON не дійсний
    }

    public static void deleteFile(Context context, String fileName) {

        // Отримуємо зовнішню директорію додатку
        //   File externalDir = context.getExternalFilesDir(null);
        if (fileName != null) {
            // Створюємо об'єкт файлу, який потрібно видалити
            File fileToDelete = new File(fileName);

            Log.e("FileData", "Шлях до файлу: " + fileToDelete);

            if (fileToDelete.exists()) {
                // Перевіряємо, чи можна записати у файл (чи він не зайнятий іншими процесами)
                if (!fileToDelete.canWrite()) {
                    Log.e("FileData", "Немає доступу до видалення файлу: " + fileName);
                    return;
                }

                // Спробуємо відкрити файл для запису, щоб перевірити, чи він зайнятий
                try (FileOutputStream fos = new FileOutputStream(fileToDelete, true)) {
                    fos.close(); // Якщо файл не зайнятий, закриваємо потік
                } catch (IOException e) {
                    Log.e("FileData", "Файл зайнятий або немає доступу: " + e.getMessage());
                    return;
                }

                // Видаляємо файл
                boolean deleted = fileToDelete.delete();
                if (deleted) {
                    Log.e("FileData", "Файл " + fileName + " було успішно видалено");
                } else {
                    Log.e("FileData", "Не вдалося видалити файл " + fileName);
                }
            } else {
                Log.e("FileData", "Файл " + fileName + " не існує");
            }
        } else {
            Log.e("FileData", "Зовнішня директорія недоступна");
        }
    }

    public static void write(String text, String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(text);  // Записуємо без нового рядка в кінці
            Log.i("FileData", "Текст успішно записаний у файл.");
        } catch (IOException e) {
            Log.e("FileData", "Помилка запису у файл: " + e.getMessage());
        }
    }

    public static String read(String filePath) {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);  // Без додавання "\n", щоб уникнути зайвих символів
            }
        } catch (IOException e) {
            Log.e("FileData", "Помилка читання файлу: " + e.getMessage());
        }
        // Видаляємо зайві пробіли та символи нового рядка
        return content.toString().trim();
    }


}
