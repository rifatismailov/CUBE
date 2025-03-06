package com.example.cube.chat.message;

import android.os.Bundle;
import com.example.cube.control.FIELD;
import com.example.cube.encryption.KeyGenerator;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Клас BundleProcessor обробляє дані з переданого Bundle.
 * Він витягує інформацію про відправника, одержувача та ключі шифрування.
 */
public class BundleProcessor {
    private String senderId; // ID відправника
    private String receiverName; // Ім'я одержувача
    private String receiverLastName; // Прізвище одержувача
    private String receiverId; // ID одержувача
    private String receiverStatus; // Статус одержувача
    private KeyGenerator.RSA keyGenerator; // Генератор ключів RSA
    private String senderKey; // Ключ відправника
    private String receiverKey; // Ключ одержувача
    private PublicKey publicKey; // Публічний ключ відправника
    private PrivateKey privateKey; // Приватний ключ відправника
    private PublicKey receiverPublicKey; // Публічний ключ одержувача
    private String avatarImageUrl; // URL аватарки
    private String accountImageUrl; // URL зображення профілю
    private String fileServerIP; // IP сервера для файлів
    private String fileServerPort; // Порт сервера для файлів

    /**
     * Конструктор класу, приймає Bundle та обробляє його вміст.
     * @param bundle Переданий об'єкт Bundle
     */
    public BundleProcessor(Bundle bundle) {
        keyGenerator = new KeyGenerator.RSA();
        processBundle(bundle);
    }

    /**
     * Метод для обробки Bundle та витягування необхідних даних.
     * @param bundle Переданий об'єкт Bundle
     */
    private void processBundle(Bundle bundle) {
        try {
            senderId = bundle.getString(FIELD.SENDER_ID.getFIELD());
            receiverName = bundle.getString(FIELD.NAME.getFIELD());
            receiverLastName = bundle.getString(FIELD.LAST_NAME.getFIELD());
            receiverId = bundle.getString(FIELD.RECEIVER_ID.getFIELD());
            receiverStatus = bundle.getString(FIELD.STATUS.getFIELD());

            String sPublicKey = bundle.getString(FIELD.PUBLIC_KEY.getFIELD());
            if (sPublicKey != null && !sPublicKey.isEmpty()) {
                publicKey = keyGenerator.decodePublicKey(sPublicKey);
            }

            String sPrivateKey = bundle.getString(FIELD.PRIVATE_KEY.getFIELD());
            if (sPrivateKey != null && !sPrivateKey.isEmpty()) {
                privateKey = keyGenerator.decodePrivateKey(sPrivateKey);
            }

            String rPublicKey = bundle.getString(FIELD.RECEIVER_PUBLIC_KEY.getFIELD());
            if (rPublicKey != null && !rPublicKey.isEmpty()) {
                receiverPublicKey = keyGenerator.decodePublicKey(rPublicKey);
            }

            senderKey = bundle.getString(FIELD.SENDER_KEY.getFIELD());
            receiverKey = bundle.getString(FIELD.RECEIVER_KEY.getFIELD());
            avatarImageUrl = bundle.getString(FIELD.AVATAR_ORG.getFIELD());
            accountImageUrl = bundle.getString(FIELD.AVATAR.getFIELD());
            fileServerIP = bundle.getString(FIELD.FILE_SERVER_IP.getFIELD());
            fileServerPort = bundle.getString(FIELD.FILE_SERVER_PORT.getFIELD());
        } catch (Exception e) {
            throw new RuntimeException("Помилка при обробці Bundle", e);
        }
    }

    // Гетери для отримання оброблених даних
    public String getSenderId() { return senderId; }
    public String getReceiverName() { return receiverName; }
    public String getReceiverLastName() { return receiverLastName; }
    public String getReceiverId() { return receiverId; }
    public String getReceiverStatus() { return receiverStatus; }
    public PublicKey getPublicKey() { return publicKey; }
    public PrivateKey getPrivateKey() { return privateKey; }
    public PublicKey getReceiverPublicKey() { return receiverPublicKey; }
    public String getSenderKey() { return senderKey; }
    public String getReceiverKey() { return receiverKey; }
    public String getAvatarImageUrl() { return avatarImageUrl; }
    public String getAccountImageUrl() { return accountImageUrl; }
    public String getFileServerIP() { return fileServerIP; }
    public String getFileServerPort() { return fileServerPort; }
}
