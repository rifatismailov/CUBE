package com.example.folder.upload;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.folder.file.FileOMG;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;

/**
 * Клас FileEncryption призначений для шифрування та відправки до файл сервери зашифрований файл
 */
public class FileEncryption {
    private static final String ALGORITHM = "AES";
    private static final int BUFFER_SIZE = 4096; // Розмір блоку для обробки (4KB)
    private final FileOMG fileOMG;
    private final String positionId;
    private final Context context;
    private final String server_address; // Змініть IP на ваш
    private String encryptedFileName;
    private File inputFile;
    private SecretKey secretKey;

    public FileEncryption(Context context, String positionId, String server_address) {
        this.context = context;
        this.fileOMG = (FileOMG) context;
        this.positionId = positionId;
        this.server_address = server_address;
    }

    /**
     * Отримуємо данні для шифрування та повертаємо назву зашифрованого файлу.
     *
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
        Log.e("FileEncryption", "encryptedFileName[ START ENCRYPT] " + encryptedFileName);

        // Підготовка до шифрування
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] buffer = new byte[BUFFER_SIZE];
        long totalBytes = inputFile.length();
        long processedBytes = 0;

        // Створюємо нову назву для зашифрованого файлу

        Log.e("FileEncryption", "encryptedFileName[ START SEND] " + encryptedFileName);

        // Запис шифрованих байтів у новий файл
        try (InputStream fis = new FileInputStream(inputFile);
             OutputStream fos = new FileOutputStream(encryptedFileName);
             CipherOutputStream cos = new CipherOutputStream(fos, cipher)) {
            Log.e("FileEncryption", "encryptedFileName[ START SEND try CATH] " + encryptedFileName);

            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                cos.write(buffer, 0, bytesRead);
                processedBytes += bytesRead;

                // Оновлення прогресу
                int progress = (int) ((processedBytes / (double) totalBytes) * 100);
                if (context instanceof Activity) {
                    ((Activity) context).runOnUiThread(() -> fileOMG.setProgressShow(positionId, progress, ""));
                    Log.e("progress", "[progress ] " + progress);

                }
            }

            // Повідомляємо про успішне завершення
            if (context instanceof Activity) {
                ((Activity) context).runOnUiThread(() -> fileOMG.setProgressShow(positionId, 100, "Шифрування завершено"));
            }

            // Додатково: передача файлу на сервер (необов'язково)
            Uploader uploader = new Uploader(context, positionId, server_address);
            Log.e("FileEncryption", "[encryptedFileName] " + encryptedFileName);
            uploader.uploadFile(new File(encryptedFileName));

        } catch (Exception e) {
            Log.e("FileEncryption", "[encryptedFileName] " + encryptedFileName + " [ Помилка шифрування ] " + e);

            e.printStackTrace();
            // Обробка помилки
            if (context instanceof Activity) {
                ((Activity) context).runOnUiThread(() -> fileOMG.setProgressShow(positionId, 0, "Помилка шифрування"));
            }
        }
    }

    private static final String TRANSFORMATION = "AES"; // Трансформація (AES)

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

    // Метод для шифрування даних без спеціальних символів
    public String encrypt(String input, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        // Шифруємо текст
        byte[] encryptedBytes = cipher.doFinal(input.getBytes(StandardCharsets.UTF_8));
        // Повертаємо результат у вигляді рядка, закодованого у власному форматі
        return encodeToCustomBase(encryptedBytes);
    }

    // Метод для кодування байтів у власний формат
    private String encodeToCustomBase(byte[] data) {
        StringBuilder encoded = new StringBuilder();
        for (byte b : data) {
            encoded.append(String.format("%02x", b & 0xff));
        }
        return encoded.toString();
    }
}
