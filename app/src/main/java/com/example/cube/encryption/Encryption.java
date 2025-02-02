package com.example.cube.encryption;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Клас для виконання різних операцій шифрування і хешування.
 * Він підтримує алгоритми AES та RSA для шифрування і дешифрування даних,
 * а також надає можливість отримання хешу за допомогою алгоритму SHA-256.
 */
public class Encryption {

    /**
     * Метод для отримання хешу в форматі SHA-256.
     *
     * @param input Текст, який потрібно зашифрувати.
     * @return Хеш у вигляді рядка у шістнадцятковому форматі.
     */
    public static String getHash(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        try {
            // Створення екземпляру MessageDigest з алгоритмом SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes());

            // Перетворення байтів у шістнадцятковий формат
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            return "Hash algorithm not found!";
        }
    }

    /**
     * Вкладений клас для шифрування і дешифрування тексту за допомогою алгоритму AES.
     */
    public static class AES {

        private static final String ALGORITHM = "AES"; // Алгоритм шифрування (AES)
        private static final String TRANSFORMATION = "AES"; // Трансформація (AES)


        /**
         * Метод для шифрування тексту за допомогою алгоритму AES.
         *
         * @param input Текст, який потрібно зашифрувати.
         * @param key   Ключ для шифрування. Довжина ключа повинна відповідати вимогам AES (16, 24 або 32 символи).
         * @return Зашифрований текст у форматі Base64.
         * @throws Exception Якщо виникає помилка під час шифрування.
         */
        public static String encrypt(String input, String key) throws Exception {
            // Створюємо секретний ключ із використанням алгоритму AES
            SecretKey secretKey = new SecretKeySpec(key.getBytes(), ALGORITHM);
            // Ініціалізуємо шифрування
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            // Шифруємо текст
            byte[] encryptedBytes = cipher.doFinal(input.getBytes());
            // Повертаємо результат у вигляді рядка, закодованого в Base64
            return Base64.getEncoder().encodeToString(encryptedBytes);
        }


        /**
         * Метод для дешифрування тексту за допомогою алгоритму AES.
         *
         * @param input Зашифрований текст у форматі Base64.
         * @param key   Ключ для дешифрування. Ключ повинен бути таким самим, як і при шифруванні.
         * @return Дешифрований текст у вигляді рядка.
         * @throws Exception Якщо виникає помилка під час дешифрування.
         */
        public static String decrypt(String input, String key) throws Exception {
            // Створюємо секретний ключ із використанням алгоритму AES
            SecretKey secretKey = new SecretKeySpec(key.getBytes(), ALGORITHM);
            // Ініціалізуємо дешифрування
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            // Декодуємо зашифрований текст з формату Base64
            byte[] decodedBytes = Base64.getDecoder().decode(input);
            // Дешифруємо текст
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            // Повертаємо результат у вигляді рядка
            return new String(decryptedBytes);
        }

        /**
         * Особливості обох методів:
         * Алгоритм AES: Використовує шифрування на основі симетричного ключа, що забезпечує високу швидкість і надійність.
         * Режим CBC (Cipher Block Chaining): Додає вектор ініціалізації (IV), щоб забезпечити унікальність результату навіть для однакових вхідних даних.
         * Використання PKCS5Padding: Забезпечує правильне заповнення блоку даних, якщо їх довжина не є кратною розміру блоку AES (16 байт).
         * IV: Генерується для кожного шифрування випадковим чином і зберігається разом із зашифрованими даними.
         * <p>
         * Шифрує текстовий рядок за допомогою алгоритму AES із використанням режиму CBC та заповнення PKCS5Padding.
         *
         * @param plainText Текст, який потрібно зашифрувати.
         * @param secretKey Секретний ключ для шифрування (повинен бути попередньо згенерований).
         * @return Зашифрований текст у вигляді рядка, який містить IV та зашифрований текст, розділені символом ':'.
         * @throws Exception Якщо виникають помилки під час ініціалізації шифру чи обробки даних.
         *                   <p>
         *                   Алгоритм дій:
         *                   1. Ініціалізуємо `Cipher` з алгоритмом AES/CBC/PKCS5Padding.
         *                   2. Генеруємо випадковий вектор ініціалізації (IV) для безпеки шифрування.
         *                   3. Створюємо `IvParameterSpec` для передачі IV до шифра.
         *                   4. Ініціалізуємо шифр у режимі шифрування (`ENCRYPT_MODE`) із секретним ключем та IV.
         *                   5. Шифруємо текст і кодуємо результат у Base64.
         *                   6. Повертаємо результат як рядок у форматі "IV:зашифрований_текст".
         */
        public static String encryptCBCdb(String plainText, SecretKey secretKey) throws Exception {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            byte[] iv = new byte[16];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
            byte[] encrypted = cipher.doFinal(plainText.getBytes());
            return Base64.getEncoder().encodeToString(iv) + ":" + Base64.getEncoder().encodeToString(encrypted);
        }

        /**
         * Розшифровує текстовий рядок, зашифрований методом `encrypt`, за допомогою алгоритму AES із використанням режиму CBC.
         *
         * @param encryptedText Зашифрований текст у форматі "IV:зашифрований_текст".
         * @param secretKey     Секретний ключ для розшифрування (той самий, який використовувався для шифрування).
         * @return Розшифрований текстовий рядок.
         * @throws Exception Якщо виникають помилки під час ініціалізації шифру чи обробки даних.
         *                   <p>
         *                   Алгоритм дій:
         *                   1. Розділяємо вхідний текст на дві частини: IV та зашифрований текст.
         *                   2. Декодуємо IV та зашифрований текст із Base64.
         *                   3. Створюємо `IvParameterSpec` для передачі IV до шифра.
         *                   4. Ініціалізуємо шифр у режимі розшифрування (`DECRYPT_MODE`) із секретним ключем та IV.
         *                   5. Розшифровуємо текст і повертаємо його у вигляді рядка.
         */
        public static String decryptCBCdb(String encryptedText, SecretKey secretKey) throws Exception {
            String[] parts = encryptedText.split(":");
            byte[] iv = Base64.getDecoder().decode(parts[0]);
            byte[] encrypted = Base64.getDecoder().decode(parts[1]);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
            return new String(cipher.doFinal(encrypted));
        }


        /**
         * Метод для серіалізації об'єкта, шифрування його та запису у файл.
         *
         * @param o         Об'єкт, який потрібно серіалізувати та зашифрувати.
         * @param fileName  Назва файлу, в який буде записано зашифровані дані.
         * @param secretKey Ключ для шифрування (SecretKey).
         * @throws Exception У випадку помилок серіалізації, шифрування чи запису у файл.
         */
        public void saveToFile(Object o, String fileName, SecretKey secretKey) throws Exception {
            // 1. Серіалізація об'єкта в масив байтів
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            try (ObjectOutputStream oos = new ObjectOutputStream(byteOut)) {
                oos.writeObject(o); // Записуємо об'єкт у потік
            }

            // 2. Шифрування масиву байтів за допомогою AES
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey); // Ініціалізуємо шифрування
            byte[] encryptedBytes = cipher.doFinal(byteOut.toByteArray()); // Шифруємо дані

            // 3. Запис зашифрованих байтів у файл
            try (FileOutputStream fos = new FileOutputStream(fileName)) {
                fos.write(encryptedBytes);
            }
        }

        /**
         * Метод для зчитування даних із файлу, їх розшифрування та десеріалізації в об'єкт.
         *
         * @param fileName  Назва файлу, з якого потрібно зчитати дані.
         * @param secretKey Ключ для розшифрування (SecretKey).
         * @return Десеріалізований об'єкт, отриманий після розшифрування.
         * @throws Exception У випадку помилок зчитування, розшифрування чи десеріалізації.
         */
        public Object loadFromFile(String fileName, SecretKey secretKey) throws Exception {
            byte[] encryptedBytes;

            // 1. Зчитуємо байти із файлу
            try (FileInputStream fis = new FileInputStream(fileName);
                 ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {

                byte[] data = new byte[1024]; // Буфер для зчитування
                int bytesRead;
                while ((bytesRead = fis.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, bytesRead); // Записуємо зчитані байти в буфер
                }
                encryptedBytes = buffer.toByteArray(); // Отримуємо всі зашифровані байти
            }

            // 2. Розшифровуємо зчитані байти за допомогою AES
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey); // Ініціалізуємо розшифрування
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes); // Розшифровуємо дані

            // 3. Десеріалізація розшифрованих байтів в об'єкт
            ByteArrayInputStream byteIn = new ByteArrayInputStream(decryptedBytes);
            try (ObjectInputStream ois = new ObjectInputStream(byteIn)) {
                return ois.readObject(); // Десеріалізуємо об'єкт
            }
        }
    }


    /**
     * Вкладений клас для шифрування і дешифрування тексту за допомогою алгоритму RSA.
     */
    public static class RSA {

        /**
         * Шифрування тексту за допомогою публічного ключа.
         *
         * @param message   Текст, який потрібно зашифрувати.
         * @param publicKey Публічний ключ для шифрування.
         * @return Зашифрований текст у форматі Base64.
         * @throws Exception Якщо виникає помилка під час шифрування.
         */
        public static String encrypt(String message, PublicKey publicKey) throws Exception {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encryptedBytes = cipher.doFinal(message.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        }

        /**
         * Дешифрування тексту за допомогою приватного ключа.
         *
         * @param encryptedMessage Зашифрований текст у форматі Base64.
         * @param privateKey       Приватний ключ для дешифрування.
         * @return Дешифрований текст у вигляді рядка.
         * @throws Exception Якщо виникає помилка під час дешифрування.
         */
        public static String decrypt(String encryptedMessage, PrivateKey privateKey) throws Exception {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedMessage);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        }
    }
}
