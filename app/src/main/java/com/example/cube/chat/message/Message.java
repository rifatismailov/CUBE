package com.example.cube.chat.message;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.example.cube.control.Check;
import com.example.cube.control.Side;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * A class that represents a message that contains text, images, and other metadata.
 * Used to send messages between users in an application.
 */
public class Message implements Serializable {

    private String messageId; // Unique message identifier
    private String message; // Message text
    private String senderId; // Sender ID
    private String receiverId;
    private String timestamp; // Time the message was sent
    private int feeling = -1; // Position of the emotion associated with the message (if present)
    private Uri selectedUrl; // Selected URL (if image present)
    private String fileName;
    private Check check; // Type of check for the message (e.g. text or image)
    private Side side; // Side of the message (sent or received)
    private byte[] image; // Image in bytes (if present)
    private String fileSize;
    private String typeFile;
    private String has;
    private String dataCreate;

    private int imageWidth; // Image width
    private int imageHeight; // Image height
    private boolean statusFile = false;
    private String messageStatus;
    private int progress;

    public Message() {
    }

    /**
     * Constructor to create a text message.
     *
     * @param message The message text.
     * @param side    The side of the message (sent or received).
     */
    public Message(String message, Side side) {
        this.message = message;
        this.check = Check.Message;
        this.side = side;
    }

    /**
     * Constructor to create a text message.
     *
     * @param message   The message text.
     * @param side      The side of the message (sent or received).
     * @param messageId The message id
     */
    public Message(String message, Side side, String messageId) {
        this.message = message;
        this.check = Check.Message;
        this.side = side;
        this.messageId = messageId;
    }

    /**
     * Constructor to create a message with a file (URL).
     *
     * @param message     The message text.
     * @param selectedUrl The URL of the selected file or received file.
     * @param side        The side of the message (sent or received).
     */
    public Message(String message, @NonNull Uri selectedUrl, Side side) {
        this.message = message;
        this.selectedUrl = selectedUrl;
        this.check = Check.File;
        this.side = side;
    }

    /**
     * Constructor to create a message with a file (URL).
     *
     * @param message     The message text.
     * @param selectedUrl The URL of the selected file or received file.
     * @param side        The side of the message (sent or received).
     * @param messageId   The message id
     */
    public Message(String message, @NonNull Uri selectedUrl, Side side, String messageId) {
        this.message = message;
        this.selectedUrl = selectedUrl;
        this.check = Check.File;
        this.side = side;
        this.messageId = messageId;
    }

    /**
     * Constructor for creating a message with an image in byte format.
     *
     * @param message     The message text.
     * @param image       An array of image bytes.
     * @param imageWidth  The width of the image.
     * @param imageHeight The height of the image.
     * @param side        The side of the message (sent or received).
     */
    public Message(String message, byte[] image, int imageWidth, int imageHeight, Side side) {
        this.message = message;
        this.image = image;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.check = Check.Image;
        this.side = side;
    }

    /**
     * Constructor for creating a message with an image in byte format.
     *
     * @param message     The message text.
     * @param selectedUrl The URL of the selected file or the received file.
     * @param image       The byte array of the image.
     * @param imageWidth  The width of the image.
     * @param imageHeight The height of the image.
     */
    public Message(String message, @NonNull Uri selectedUrl, byte[] image, int imageWidth, int imageHeight) {
        this.message = message;
        this.selectedUrl = selectedUrl;
        this.image = image;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;

        if (selectedUrl.toString().matches(".*\\.(jpg|jpeg|png|webp|bmp|gif|heic|heif|tiff|tif)$")) {
            this.check = Check.Image;
        } else {
            this.check = Check.File;
        }
    }

    /**
     * Constructor for creating a message with an image in byte format.
     *
     * @param message     The message text.
     * @param selectedUrl The URL of the selected file or the received file.
     * @param image       The image byte array.
     * @param imageWidth  The width of the image.
     * @param imageHeight The height of the image.
     * @param side        The side of the message (sent or received).
     */
    public Message(String message, @NonNull Uri selectedUrl, byte[] image, int imageWidth, int imageHeight, Side side) {
        this.message = message;
        this.selectedUrl = selectedUrl;
        this.image = image;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        if (selectedUrl.toString().matches(".*\\.(jpg|jpeg|png|webp|bmp|gif|heic|heif|tiff|tif)$")) {
            this.check = Check.Image;
        } else {
            this.check = Check.File;
        }
        this.side = side;
    }

    /**
     * Constructor for creating a message with an image in byte format.
     *
     * @param message     The message text.
     * @param selectedUrl The URL of the selected file or the received file.
     * @param image       An array of image bytes.
     * @param imageWidth  The width of the image.
     * @param imageHeight The height of the image.
     * @param side        The side of the message (sent or received).
     * @param messageId   The message id A
     */
    public Message(String message, @NonNull Uri selectedUrl, byte[] image, int imageWidth, int imageHeight, Side side, String messageId) {
        this.message = message;
        this.selectedUrl = selectedUrl;
        this.image = image;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        if (selectedUrl.toString().matches(".*\\.(jpg|jpeg|png|webp|bmp|gif|heic|heif|tiff|tif)$")) {
            this.check = Check.Image;
        } else {
            this.check = Check.File;
        }
        this.side = side;
        this.messageId = messageId;
    }

    // Getter for image byte array
    public byte[] getImage() {
        return image;
    }

    // Setter for image byte array
    public void setImage(byte[] image) {
        this.image = image;
    }

    // Getter for image width
    public int getImageWidth() {
        return imageWidth;
    }

    // Setter for image width
    public void setImageWidth(int imageWidth) {
        this.imageWidth = imageWidth;
    }

    // Getter for image height
    public int getImageHeight() {
        return imageHeight;
    }

    // Setter for image height
    public void setImageHeight(int imageHeight) {
        this.imageHeight = imageHeight;
    }

    // Getter for check type (Check)
    public Check getCheck() {
        return check;
    }

    // Setter for check type (Check)
    public void setCheck(Check check) {
        this.check = check;
    }

    // Getter for the side of the message (Side)
    public Side getSide() {
        return side;
    }

    // Setter for the side of the message (Side)
    public void setSide(Side side) {
        this.side = side;
    }

    // Getter for the message ID
    public String getMessageId() {
        return messageId;
    }

    // Setter for the message ID
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    // Getter for the message text
    public String getMessage() {
        return message;
    }

    // Setter for the message text
    public void setMessage(String message) {
        this.message = message;
    }

    // Getter for the sender ID
    public String getSenderId() {
        return senderId;
    }

    // Setter for the sender ID
    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    // Getter for the time the message was sent
    public String getTimestamp() {
        return timestamp;
    }

    // Setter for the time the message was sent
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    // Getter for the feeling (emotion)
    public int getFeeling() {
        return feeling;
    }

    // Setter for the feeling (emotion)
    public void setFeeling(int feeling) {
        this.feeling = feeling;
    }

// Getter for the emoji position

    // Getter for the image URL
    public Uri getUrl() {
        return selectedUrl;
    }

    // Setter for the image URL
    public void setUrl(Uri selectedUrl) {
        this.selectedUrl = selectedUrl;
        if (selectedUrl.toString().endsWith(".jpg") || selectedUrl.toString().endsWith(".png")) {
            this.check = Check.Image;
        } else {
            this.check = Check.File;
        }
    }

    public boolean isStatusFile() {
        return statusFile;
    }

    public void setStatusFile(boolean statusFile) {
        this.statusFile = statusFile;
    }

    public String getHas() {
        return has;
    }

    public void setHas(String has) {
        this.has = has;
    }

    public String getMessageStatus() {
        return messageStatus;
    }

    public void setMessageStatus(String messageStatus) {
        this.messageStatus = messageStatus;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public String getTypeFile() {
        return typeFile;
    }

    public void setTypeFile(String typeFile) {
        this.typeFile = typeFile;
    }

    public String getDataCreate() {
        return dataCreate;
    }

    public void setDataCreate(String dataCreate) {
        this.dataCreate = dataCreate;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    /**
     * Method for generating hash
     * get only from message
     */
    public String getHash_m() throws NoSuchAlgorithmException {
        // Create hash using SHA-256
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(message.getBytes());

        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }

    /**
     * Method for generating hash if there is a file
     * get hash from message, file link and file hash
     */
    public String getHash_f() throws NoSuchAlgorithmException {
        // Use only sender, recipient and message content
        String input = message + selectedUrl + has;

        // Create a hash using SHA-256
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(input.getBytes());

        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            hexString.append(String.format("%02x", b));
        }

        return hexString.toString();
    }
}
