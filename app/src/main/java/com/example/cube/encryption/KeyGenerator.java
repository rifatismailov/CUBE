package com.example.cube.encryption;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;

import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class KeyGenerator {
    public static class AES {
    }

    public static class RSA {
        private PublicKey publicKey;
        private PrivateKey privateKey;

        public KeyPair generateKeyPair() throws Exception {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048); // Розмір ключа: 2048 біт
            return keyPairGenerator.generateKeyPair();
        }

        public void key() {
            try {
                KeyPair keyPair = generateKeyPair();
                publicKey = keyPair.getPublic();
                privateKey = keyPair.getPrivate();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public String getPublicKey() {
            return Base64.getEncoder().encodeToString(publicKey.getEncoded());
        }

        public String getPrivateKey() {
            return Base64.getEncoder().encodeToString(privateKey.getEncoded());
        }

        public PublicKey decodePublicKey(String publicKeyBase64) throws Exception {
            byte[] keyBytes = Base64.getDecoder().decode(publicKeyBase64);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(spec);
        }

        public PrivateKey decodePrivateKey(String privateKeyBase64) throws Exception {
            byte[] keyBytes = Base64.getDecoder().decode(privateKeyBase64);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes); // Використовуємо PKCS8EncodedKeySpec
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(spec);
        }
    }

}
