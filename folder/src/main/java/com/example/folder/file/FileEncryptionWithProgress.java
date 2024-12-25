package com.example.folder.file;

import android.annotation.SuppressLint;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.io.*;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.io.*;

/**
 * Клас для збереження та завантаження файлів з використанням шифрування AES та відстеженням прогресу.
 */
public class FileEncryptionWithProgress {
    private static final String ALGORITHM = "AES";
    private static final int BUFFER_SIZE = 4096; // Розмір блоку для обробки (4KB)

    /**
     * Зберігає файл з шифруванням та відстеженням прогресу.
     *
     * @param fileName   Ім'я файлу, в який буде збережено дані.
     * @param secretKey  Секретний ключ для шифрування.
     * @throws Exception Якщо виникає помилка при шифруванні або збереженні файлу.
     */
    public void saveToFileWithProgress(String fileName, SecretKey secretKey) throws Exception {
        // 1. Серіалізація об'єкта в байти
        File file = new File(fileName);
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(byteOut)) {
            oos.writeObject(file);
        }
        byte[] serializedBytes = byteOut.toByteArray();

        // 2. Підготовка до шифрування
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] buffer = new byte[BUFFER_SIZE];
        int totalBytes = serializedBytes.length;
        int processedBytes = 0;

        // 3. Запис шифрованих байтів у файл
        try (FileOutputStream fos = new FileOutputStream(fileName);
             ByteArrayInputStream byteIn = new ByteArrayInputStream(serializedBytes)) {
            int bytesRead;
            while ((bytesRead = byteIn.read(buffer)) != -1) {
                byte[] encryptedChunk = cipher.update(buffer, 0, bytesRead); // Шифруємо блок
                fos.write(encryptedChunk); // Записуємо шифровані байти у файл
                processedBytes += bytesRead;

                // Відображення прогресу
                int progress = (int) ((processedBytes / (double) totalBytes) * 100);
                System.out.printf("Шифрування: %d%% завершено%n", progress);
            }
            fos.write(cipher.doFinal()); // Завершуємо шифрування
        }
    }

    /**
     * Завантажує файл з розшифруванням та відстеженням прогресу.
     *
     * @param fileName   Ім'я файлу, з якого буде завантажено дані.
     * @param secretKey  Секретний ключ для розшифрування.
     * @return Десеріалізований файл.
     * @throws Exception Якщо виникає помилка при розшифруванні або завантаженні файлу.
     */
    public File loadFromFileWithProgress(String fileName, SecretKey secretKey) throws Exception {
        // 1. Зчитуємо зашифровані дані
        File file = new File(fileName);
        long totalBytes = file.length();
        long processedBytes = 0;

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);

        try (FileInputStream fis = new FileInputStream(file);
             ByteArrayOutputStream decryptedOut = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                byte[] decryptedChunk = cipher.update(buffer, 0, bytesRead); // Розшифруємо блок
                decryptedOut.write(decryptedChunk); // Записуємо розшифровані дані
                processedBytes += bytesRead;

                // Відображення прогресу
                int progress = (int) ((processedBytes / (double) totalBytes) * 100);
                System.out.printf("Розшифрування: %d%% завершено%n", progress);
            }
            decryptedOut.write(cipher.doFinal()); // Завершуємо розшифрування

            // 2. Десеріалізація
            try (ObjectInputStream ois = new ObjectInputStream(
                    new ByteArrayInputStream(decryptedOut.toByteArray()))) {
                return (File) ois.readObject();
            }
        }
    }
}
