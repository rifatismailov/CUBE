package com.example.cube.chat.message;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.example.cube.control.Check;
import com.example.cube.control.Side;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Клас, що представляє повідомлення, яке містить текст, зображення та інші метадані.
 * Використовується для передачі повідомлень між користувачами у застосунку.
 */
public class Message implements Serializable {

    private String messageId;     // Унікальний ідентифікатор повідомлення
    private String message;       // Текст повідомлення
    private String senderId;      // Ідентифікатор відправника
    private String receiverId;
    private String timestamp;       // Час відправлення повідомлення
    private int feeling = -1;     // Позиція емоції, пов'язаної з повідомленням (якщо присутня)
    private int emojisPosition = 0; // Позиція емодзі у повідомленні (якщо присутня)
    private Uri selectedUrl;              // Вибрана URL-адреса (якщо присутнє зображення)
    private String fileName;
    private Check check;                  // Тип перевірки для повідомлення (наприклад, текст чи зображення)
    private Side side;                    // Сторона повідомлення (відправлене чи отримане)
    private byte[] image;                 // Зображення в байтах (якщо присутнє)
    private String fileSize;
    private String typeFile;
    private String has;
    private String dataCreate;

    private int imageWidth;               // Ширина зображення
    private int imageHeight;              // Висота зображення
    private boolean statusFile = false;
    private String messageStatus;
    private int progress;


    public Message() {
    }

    /**
     * Конструктор для створення текстового повідомлення.
     *
     * @param message Текст повідомлення.
     * @param side    Сторона повідомлення (відправлене чи отримане).
     */
    public Message(String message, Side side) {
        this.message = message;
        this.check = Check.Message;
        this.side = side;
    }

    /**
     * Конструктор для створення текстового повідомлення.
     *
     * @param message   Текст повідомлення.
     * @param side      Сторона повідомлення (відправлене чи отримане).
     * @param messageId id повідомлення
     */
    public Message(String message, Side side, String messageId) {
        this.message = message;
        this.check = Check.Message;
        this.side = side;
        this.messageId = messageId;
    }

    /**
     * Конструктор для створення повідомлення із файлом (URL).
     *
     * @param message     Текст повідомлення.
     * @param selectedUrl URL вибраного файлу або отриманого файлу.
     * @param side        Сторона повідомлення (відправлене чи отримане).
     */
    public Message(String message, @NonNull Uri selectedUrl, Side side) {
        this.message = message;
        this.selectedUrl = selectedUrl;
        this.check = Check.File;
        this.side = side;
    }

    /**
     * Конструктор для створення повідомлення із файлом (URL).
     *
     * @param message     Текст повідомлення.
     * @param selectedUrl URL вибраного файлу або отриманого файлу.
     * @param side        Сторона повідомлення (відправлене чи отримане).
     * @param messageId   id повідомлення
     */
    public Message(String message, @NonNull Uri selectedUrl, Side side, String messageId) {
        this.message = message;
        this.selectedUrl = selectedUrl;
        this.check = Check.File;
        this.side = side;
        this.messageId = messageId;
    }

    /**
     * Конструктор для створення повідомлення із зображенням у байтовому форматі.
     *
     * @param message     Текст повідомлення.
     * @param image       Масив байтів зображення.
     * @param imageWidth  Ширина зображення.
     * @param imageHeight Висота зображення.
     * @param side        Сторона повідомлення (відправлене чи отримане).
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
     * Конструктор для створення повідомлення із зображенням у байтовому форматі.
     *
     * @param message     Текст повідомлення.
     * @param selectedUrl URL вибраного файлу або отриманого файлу.
     * @param image       Масив байтів зображення.
     * @param imageWidth  Ширина зображення.
     * @param imageHeight Висота зображення.
     */
    public Message(String message, @NonNull Uri selectedUrl, byte[] image, int imageWidth, int imageHeight) {
        this.message = message;
        this.selectedUrl = selectedUrl;
        this.image = image;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        if (selectedUrl.toString().endsWith(".jpg") || selectedUrl.toString().endsWith(".png")) {
            this.check = Check.Image;
        } else {
            this.check = Check.File;
        }
    }

    /**
     * Конструктор для створення повідомлення із зображенням у байтовому форматі.
     *
     * @param message     Текст повідомлення.
     * @param selectedUrl URL вибраного файлу або отриманого файлу.
     * @param image       Масив байтів зображення.
     * @param imageWidth  Ширина зображення.
     * @param imageHeight Висота зображення.
     * @param side        Сторона повідомлення (відправлене чи отримане).
     */
    public Message(String message, @NonNull Uri selectedUrl, byte[] image, int imageWidth, int imageHeight, Side side) {
        this.message = message;
        this.selectedUrl = selectedUrl;
        this.image = image;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        if (selectedUrl.toString().endsWith(".jpg") || selectedUrl.toString().endsWith(".png")) {
            this.check = Check.Image;
        } else {
            this.check = Check.File;
        }
        this.side = side;
    }

    /**
     * Конструктор для створення повідомлення із зображенням у байтовому форматі.
     *
     * @param message     Текст повідомлення.
     * @param selectedUrl URL вибраного файлу або отриманого файлу.
     * @param image       Масив байтів зображення.
     * @param imageWidth  Ширина зображення.
     * @param imageHeight Висота зображення.
     * @param side        Сторона повідомлення (відправлене чи отримане).
     * @param messageId   id повідомлення A
     */
    public Message(String message, @NonNull Uri selectedUrl, byte[] image, int imageWidth, int imageHeight, Side side, String messageId) {
        this.message = message;
        this.selectedUrl = selectedUrl;
        this.image = image;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        if (selectedUrl.toString().endsWith(".jpg") ||
                selectedUrl.toString().endsWith(".jpeg") ||
                selectedUrl.toString().endsWith(".png") ||
                selectedUrl.toString().endsWith(".webp") ||
                selectedUrl.toString().endsWith(".bmp") ||
                selectedUrl.toString().endsWith(".gif") ||
                selectedUrl.toString().endsWith(".heic") ||
                selectedUrl.toString().endsWith(".heif") ||
                selectedUrl.toString().endsWith(".tiff") ||
                selectedUrl.toString().endsWith(".tif")) {
            this.check = Check.Image;
        } else {
            this.check = Check.File;
        }
        this.side = side;
        this.messageId = messageId;
    }

    // Геттер для масиву байтів зображення
    public byte[] getImage() {
        return image;
    }

    // Сеттер для масиву байтів зображення
    public void setImage(byte[] image) {
        this.image = image;
    }


    // Геттер для ширини зображення
    public int getImageWidth() {
        return imageWidth;
    }

    // Сеттер для ширини зображення
    public void setImageWidth(int imageWidth) {
        this.imageWidth = imageWidth;
    }

    // Геттер для висоти зображення
    public int getImageHeight() {
        return imageHeight;
    }

    // Сеттер для висоти зображення
    public void setImageHeight(int imageHeight) {
        this.imageHeight = imageHeight;
    }

    // Геттер для типу перевірки (Check)
    public Check getCheck() {
        return check;
    }

    // Сеттер для типу перевірки (Check)
    public void setCheck(Check check) {
        this.check = check;
    }

    // Геттер для сторони повідомлення (Side)
    public Side getSide() {
        return side;
    }

    // Сеттер для сторони повідомлення (Side)
    public void setSide(Side side) {
        this.side = side;
    }

    // Геттер для ID повідомлення
    public String getMessageId() {
        return messageId;
    }

    // Сеттер для ID повідомлення
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    // Геттер для тексту повідомлення
    public String getMessage() {
        return message;
    }

    // Сеттер для тексту повідомлення
    public void setMessage(String message) {
        this.message = message;
    }

    // Геттер для ID відправника
    public String getSenderId() {
        return senderId;
    }

    // Сеттер для ID відправника
    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    // Геттер для часу відправки повідомлення
    public String getTimestamp() {
        return timestamp;
    }

    // Сеттер для часу відправки повідомлення
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    // Геттер для почуття (емоції)
    public int getFeeling() {
        return feeling;
    }

    // Сеттер для почуття (емоції)
    public void setFeeling(int feeling) {
        this.feeling = feeling;
    }

    // Геттер для позиції емодзі


    // Геттер для URL зображення
    public Uri getUrl() {
        return selectedUrl;
    }

    // Сеттер для URL зображення
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
     * Метод для генерації хешу
     * отримуємо тільки з повідомлення
     */
    public String getHash_m() throws NoSuchAlgorithmException {
        // Створюємо хеш за допомогою SHA-256
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(message.getBytes());

        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }

    /**
     * Метод для генерації хешу якщо є файл
     * отримуємо хеш з повідомлення, посилання на файл та хешу файлу
     */
    public String getHash_f() throws NoSuchAlgorithmException {
        // Використовуємо лише відправника, отримувача і вміст повідомлення
        String input = message + selectedUrl + has;

        // Створюємо хеш за допомогою SHA-256
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(input.getBytes());

        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            hexString.append(String.format("%02x", b));
        }

        return hexString.toString();
    }
}
