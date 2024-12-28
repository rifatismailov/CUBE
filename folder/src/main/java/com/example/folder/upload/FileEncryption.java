package com.example.folder.upload;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.folder.file.FileOMG;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

public class FileEncryption {
    private static final String ALGORITHM = "AES";
    private static final int BUFFER_SIZE = 4096; // Розмір блоку для обробки (4KB)
    private final FileOMG fileOMG;
    private final String messageId;
    private final Context context;
    private final String server_address; // Змініть IP на ваш
    private String encryptedFileName;
    private File inputFile;
    private SecretKey secretKey;

    public FileEncryption(Context context, String messageId, String server_address) {
        this.context = context;
        this.fileOMG = (FileOMG) context;
        this.messageId = messageId;
        this.server_address = server_address;

    }

    /**
     * Отримуємо данні для шифрування та повертаємо назву зашифрованого фала .
     * @param inputFile Оригінальний файл для шифрування.
     * @param secretKey Секретний ключ для шифрування.
     */
    public String getEncFile(File inputFile, SecretKey secretKey) throws Exception {
        this.inputFile = inputFile;
        this.secretKey = secretKey;
        encryptedFileName = generateEncryptedFileName(inputFile);
        return encryptedFileName;
    }

    /**
     * Зберігає файл із шифруванням та відстеженням прогресу.
     *
     * @throws Exception Якщо виникає помилка при шифруванні або збереженні файлу.
     */
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public void fileEncryption() throws Exception {


        // 1. Зчитуємо вміст файлу в байтовий масив
        byte[] fileBytes;
        try (FileInputStream fis = new FileInputStream(inputFile)) {
            fileBytes = fis.readAllBytes();
        }

        // 2. Підготовка до шифрування
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] buffer = new byte[BUFFER_SIZE];
        int totalBytes = fileBytes.length;
        int processedBytes = 0;

        // Створюємо нову назву для зашифрованого файлу
        Log.e("FileEncryption"," encryptedFileName "+encryptedFileName);

        // 3. Запис шифрованих байтів у новий файл
        try (FileOutputStream fos = new FileOutputStream(encryptedFileName);
             ByteArrayInputStream byteIn = new ByteArrayInputStream(fileBytes)) {

            int bytesRead;
            while ((bytesRead = byteIn.read(buffer)) != -1) {
                byte[] encryptedChunk = cipher.update(buffer, 0, bytesRead); // Шифруємо блок
                fos.write(encryptedChunk); // Записуємо шифровані байти у файл
                processedBytes += bytesRead;

                // Оновлення прогресу
                int progress = (int) ((processedBytes / (double) totalBytes) * 100);
//                if (context instanceof Activity) {
//                    ((Activity) context).runOnUiThread(() ->
//                            fileOMG.setProgressShow(messageId, progress, ""));
//                }
                if (context instanceof Activity) {
                    ((Activity) context).runOnUiThread(() -> fileOMG.setProgressShow(messageId, progress, ""));
                }
            }

            // Завершуємо шифрування
            fos.write(cipher.doFinal());

            // Повідомляємо про успішне завершення
            if (context instanceof Activity) {
                ((Activity) context).runOnUiThread(() -> fileOMG.setProgressShow(messageId, 100, "Шифрування завершено"));
            }

            // Додатково: передача файлу на сервер (необов'язково)
            Uploader uploader = new Uploader(context, messageId, server_address);
            uploader.uploadFile(new File(encryptedFileName));

        } catch (Exception e) {
            e.printStackTrace();
            // Обробка помилки
            if (context instanceof Activity) {
                ((Activity) context).runOnUiThread(() -> fileOMG.setProgressShow(messageId, 0, "Помилка шифрування"));
            }
        }

    }
    private static final String TRANSFORMATION = "AES"; // Трансформація (AES)

    // Метод для шифрування даних без спеціальних символів
    public String encrypt(String input, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        // Шифруємо текст
        byte[] encryptedBytes = cipher.doFinal(input.getBytes(StandardCharsets.UTF_8));
        // Повертаємо результат у вигляді рядка, закодованого у власному форматі
        return encodeToCustomBase(encryptedBytes);
    }

    // Метод для дешифрування даних
    public String decrypt(String encryptedData, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        // Декодуємо власний формат до байтів
        byte[] decodedBytes = decodeFromCustomBase(encryptedData);
        // Дешифруємо байти
        byte[] decryptedBytes = cipher.doFinal(decodedBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    // Метод для отримання розширення файлу
    public String getFileExtension(File file) {
        String fileName = file.getName();
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
    }

    public String generateEncryptedFileName(File inputFile) throws Exception {
        // Отримуємо тип файлу або розширення
        String extension = encrypt(getFileExtension(inputFile), secretKey);
        // Генеруємо нову назву файлу
        return inputFile.getParent() + File.separator +
                "enc-" + UUID.randomUUID().toString() + (extension.isEmpty() ? "" : "." + extension);
    }

    // Метод для кодування байтів у власний формат
    private String encodeToCustomBase(byte[] data) {
        StringBuilder encoded = new StringBuilder();
        for (byte b : data) {
            encoded.append(String.format("%02x", b & 0xff));
        }
        return encoded.toString();
    }

    // Метод для декодування з власного формату
    private byte[] decodeFromCustomBase(String data) {
        int len = data.length();
        byte[] decoded = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            decoded[i / 2] = (byte) ((Character.digit(data.charAt(i), 16) << 4)
                    + Character.digit(data.charAt(i + 1), 16));
        }
        return decoded;
    }
}
