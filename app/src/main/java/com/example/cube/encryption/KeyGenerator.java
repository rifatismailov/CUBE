package com.example.cube.encryption;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Клас, що містить методи для генерації та обробки криптографічних ключів для алгоритмів AES та RSA.
 */
public class KeyGenerator {

    /**
     * Внутрішній клас для генерації AES ключів.
     */
    public static class AES {

        /**
         * Генерує AES-ключ заданої довжини.
         * Довжина ключа повинна бути однією з: 16, 24 або 32 байти.
         *
         * @param length Довжина ключа в байтах (16, 24 або 32).
         * @return Згенерований ключ у вигляді рядка у форматі Hex.
         * @throws IllegalArgumentException Якщо довжина ключа не відповідає вимогам.
         */
        public static String generateKey(int length) {
            if (length != 16 && length != 24 && length != 32) {
                throw new IllegalArgumentException("Довжина ключа має бути 16, 24 або 32 байти.");
            }
            SecureRandom secureRandom = new SecureRandom();
            byte[] key = new byte[length];
            secureRandom.nextBytes(key);
            return bytesToHex(key);
        }

        public static byte[] hexToBytes(String hex) {
            int len = hex.length();
            byte[] data = new byte[len / 2];
            for (int i = 0; i < len; i += 2) {
                data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                        + Character.digit(hex.charAt(i + 1), 16));
            }
            return data;
        }

        /**
         * Конвертує масив байтів у рядок у форматі Hex.
         *
         * @param bytes Масив байтів.
         * @return Рядок у форматі Hex.
         */
        private static String bytesToHex(byte[] bytes) {
            StringBuilder hexString = new StringBuilder();
            for (byte b : bytes) {
                String hex = Integer.toHexString(0xFF & b);
                if (hex.length() == 1) {
                    hexString.append('0'); // Додаємо 0 для вирівнювання
                }
                hexString.append(hex);
            }
            return hexString.toString();
        }
    }

    /**
     * Внутрішній клас для генерації та обробки RSA ключів.
     */
    public static class RSA {

        private PublicKey publicKey;
        private PrivateKey privateKey;

        /**
         * Генерує пару ключів RSA з розміром 2048 біт.
         *
         * @return Пару публічного та приватного ключів.
         * @throws Exception Якщо сталася помилка під час генерації ключів.
         */
        public KeyPair generateKeyPair() throws Exception {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048); // Розмір ключа: 2048 біт
            return keyPairGenerator.generateKeyPair();
        }

        /**
         * Генерує пару RSA ключів та зберігає їх в екземплярі класу.
         */
        public void key() {
            try {
                KeyPair keyPair = generateKeyPair();
                publicKey = keyPair.getPublic();
                privateKey = keyPair.getPrivate();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * Отримує публічний ключ у вигляді рядка, закодованого в Base64.
         *
         * @return Публічний ключ у форматі Base64.
         */
        public String getPublicKey() {
            return Base64.getEncoder().encodeToString(publicKey.getEncoded());
        }

        /**
         * Отримує приватний ключ у вигляді рядка, закодованого в Base64.
         *
         * @return Приватний ключ у форматі Base64.
         */
        public String getPrivateKey() {
            return Base64.getEncoder().encodeToString(privateKey.getEncoded());
        }

        /**
         * Декодує публічний ключ з Base64 рядка.
         *
         * @param publicKeyBase64 Публічний ключ у форматі Base64.
         * @return Декодований публічний ключ.
         * @throws Exception Якщо сталася помилка під час декодування.
         */
        public PublicKey decodePublicKey(String publicKeyBase64) throws Exception {
            byte[] keyBytes = Base64.getDecoder().decode(publicKeyBase64);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(spec);
        }

        /**
         * Декодує приватний ключ з Base64 рядка.
         *
         * @param privateKeyBase64 Приватний ключ у форматі Base64.
         * @return Декодований приватний ключ.
         * @throws Exception Якщо сталася помилка під час декодування.
         */
        public PrivateKey decodePrivateKey(String privateKeyBase64) throws Exception {
            byte[] keyBytes = Base64.getDecoder().decode(privateKeyBase64);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes); // Використовуємо PKCS8EncodedKeySpec
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(spec);
        }
    }

}
