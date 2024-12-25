package com.example.folder.dialogwindows;

import android.app.Activity;
import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.example.folder.file.FileOMG;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
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

    }/**
     * @param inputFile  Оригінальний файл для шифрування.
     * @param secretKey  Секретний ключ для шифрування.
     * */
    public String getEncFile(File inputFile, SecretKey secretKey) throws Exception {
        this.inputFile=inputFile;
        this.secretKey=secretKey;
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
                if (context instanceof Activity) {
                    ((Activity) context).runOnUiThread(() ->
                            fileOMG.setProgressShow(messageId, progress, ""));
                }
            }

            // Завершуємо шифрування
            fos.write(cipher.doFinal());

            // Повідомляємо про успішне завершення
            if (context instanceof Activity) {
                ((Activity) context).runOnUiThread(() ->
                        fileOMG.setProgressShow(messageId, 100, "Шифрування завершено"));
            }

            // Додатково: передача файлу на сервер (необов'язково)
            Uploader uploader = new Uploader(context, messageId, server_address);
            uploader.uploadFile(new File(encryptedFileName));

        } catch (Exception e) {
            e.printStackTrace();
            // Обробка помилки
            if (context instanceof Activity) {
                ((Activity) context).runOnUiThread(() ->
                        fileOMG.setProgressShow(messageId, 0, "Помилка шифрування"));
            }
        }

    }
    public String getFileType(File file) throws Exception {
        return Files.probeContentType(file.toPath());
    }
    public String generateEncryptedFileName(File inputFile) throws Exception {
        // Отримуємо тип файлу або розширення
        String fileType = Files.probeContentType(inputFile.toPath());
        String extension = getFileExtension(inputFile); // Альтернативний варіант отримати розширення

        // Генеруємо нову назву файлу
        return inputFile.getParent() + File.separator +
                "enc-" + UUID.randomUUID().toString() + (extension.isEmpty() ? "" : "." + extension);
    }

    // Метод для отримання розширення файлу
    public String getFileExtension(File file) {
        String fileName = file.getName();
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
    }
}
