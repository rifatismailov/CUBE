package com.example.folder.file;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.MessageDigest;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class FileDetect {
    private static final String ALGORITHM = "AES";

    // Метод для серіалізації та шифрування
    public void saveToFile(Object o,String fileName, SecretKey secretKey) throws Exception {
        // Спочатку серіалізуємо об'єкт в байти
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(byteOut)) {
            oos.writeObject(o);
        }

        // Шифруємо байти
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(byteOut.toByteArray());

        // Записуємо зашифровані байти у файл
        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            fos.write(encryptedBytes);
        }
    }

    // Метод для генерації секретного ключа AES
    public static SecretKey generateKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
        keyGen.init(128);  // AES-128
        return keyGen.generateKey();
    }
    // Метод для розшифрування та десеріалізації
    @SuppressWarnings("unchecked")
    public Object loadFromFile(String fileName, SecretKey secretKey) throws Exception {
        byte[] encryptedBytes;

        // Зчитуємо байти з файлу
        try (FileInputStream fis = new FileInputStream(fileName);
             ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {

            byte[] data = new byte[1024];  // Буфер для зчитування
            int bytesRead;
            while ((bytesRead = fis.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, bytesRead);
            }
            encryptedBytes = buffer.toByteArray();  // Отримуємо всі зашифровані байти
        }

        // Розшифровуємо байти
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

        // Десеріалізуємо об'єкт з розшифрованих байтів
        ByteArrayInputStream byteIn = new ByteArrayInputStream(decryptedBytes);
        try (ObjectInputStream ois = new ObjectInputStream(byteIn)) {
            return ois.readObject();
        }
    }
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
    public void saveJsonToFile(Context context, String filename, JSONObject jsonObject) {
        // Створюємо каталог "cube" у зовнішньому сховищі
        File externalDir = new File(context.getExternalFilesDir(null), "cube");
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


    public JSONObject readJsonFromFile(Context context, String filename) {
        StringBuilder jsonBuilder = new StringBuilder();
        FileInputStream fis = null;

        // Створюємо шлях до файлу в зовнішньому сховищі
        File externalDir = new File(context.getExternalFilesDir(null), "cube");
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
