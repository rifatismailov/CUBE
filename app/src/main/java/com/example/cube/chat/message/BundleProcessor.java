package com.example.cube.chat.message;

import android.os.Bundle;
import android.widget.Toast;

import com.example.cube.control.FIELD;
import com.example.cube.encryption.KeyGenerator;

import java.security.PrivateKey;
import java.security.PublicKey;

public class BundleProcessor {
    private String senderId;
    private String receiverName;
    private String receiverId;
    private String receiverStatus;
    private KeyGenerator.RSA keyGenerator;
    private String senderKey;
    private String receiverKey;
    private PublicKey publicKey;
    private PrivateKey privateKey;
    private PublicKey receiverPublicKey;
    private String avatarImageUrl;
    private String accountImageUrl;

    public BundleProcessor(Bundle bundle) {
        keyGenerator = new KeyGenerator.RSA();
        processBundle(bundle);
    }

    private void processBundle(Bundle bundle) {
        try {
            senderId = bundle.getString(FIELD.SENDER_ID.getFIELD());
            receiverName = bundle.getString(FIELD.NAME.getFIELD());
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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Getters for the processed data
    public String getSenderId() {
        return senderId;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public String getReceiverStatus() {
        return receiverStatus;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public PublicKey getReceiverPublicKey() {
        return receiverPublicKey;
    }

    public String getSenderKey() {
        return senderKey;
    }

    public String getReceiverKey() {
        return receiverKey;
    }

    public String getAvatarImageUrl() {
        return avatarImageUrl;
    }

    public String getAccountImageUrl() {
        return accountImageUrl;
    }

}

