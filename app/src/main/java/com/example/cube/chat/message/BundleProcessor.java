package com.example.cube.chat.message;

import android.os.Bundle;
import com.example.cube.control.FIELD;
import com.example.cube.encryption.KeyGenerator;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * The BundleProcessor class processes data from the passed Bundle.
 * It extracts information about the sender, recipient, and encryption keys.
 */
public class BundleProcessor {
    private String senderId; // Sender ID
    private String receiverName; // Recipient First Name
    private String receiverLastName; // Recipient Last Name
    private String receiverId; // Recipient ID
    private String receiverStatus; // Recipient Status
    private final KeyGenerator.RSA keyGenerator; // RSA Key Generator
    private String senderKey; // Sender Key
    private String receiverKey; // Recipient Key
    private PublicKey publicKey; // Sender Public Key
    private PrivateKey privateKey; // Sender Private Key
    private PublicKey receiverPublicKey; // Recipient Public Key
    private String avatarImageUrl; // Avatar URL
    private String accountImageUrl; // Profile Image URL
    private String fileServerIP; // File Server IP
    private String fileServerPort; // File Server Port

    /**
     * Class constructor, accepts a Bundle and processes its contents.
     * @param bundle The passed Bundle object
     */
    public BundleProcessor(Bundle bundle) {
        keyGenerator = new KeyGenerator.RSA();
        processBundle(bundle);
    }

    /**
     * Method to process the Bundle and extract the required data.
     * @param bundle The passed Bundle object
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
            throw new RuntimeException("Error processing Bundle", e);
        }
    }

    // Getters for receiving processed data
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
