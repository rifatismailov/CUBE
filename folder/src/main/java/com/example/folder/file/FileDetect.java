package com.example.folder.file;

import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.MessageDigest;
public class FileDetect {

    public String getFileHash(String file, String algorithm) {
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

}
