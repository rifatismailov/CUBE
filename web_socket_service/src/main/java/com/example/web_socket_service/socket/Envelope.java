package com.example.web_socket_service.socket;

import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Envelope {
    private String messageId;     // Унікальний ідентифікатор повідомлення
    private String senderId;       // ІД відправника
    private String receiverId;     // ІД отримувача
    private String operation;
    private String message;    // Саме повідомлення (може бути null)
    private String fileUrl;        // Посилання на файл (може бути null)
    private String fileHash;       // Хеш-сума файла (може бути null)
    private String messageStatus;
    private String timestamp;       // Час відправлення повідомлення

    // Конструктор для текстового повідомлення
    public Envelope(String senderId, String receiverId, String operation, String message, String messageId) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.operation = operation;
        this.message = message;
        this.fileUrl = null;
        this.fileHash = null;
        this.messageId = messageId;
    }

    public Envelope(String senderId, String receiverId, String operation, String message, String messageId, String timestamp) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.operation = operation;
        this.message = message;
        this.fileUrl = null;
        this.fileHash = null;
        this.messageId = messageId;
        this.timestamp = timestamp;
    }

    // Конструктор для повідомлення з файлом
    public Envelope(String senderId, String receiverId, String operation, String message, String fileUrl, String fileHash, String messageId) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.operation = operation;
        this.message = message;
        this.fileUrl = fileUrl;
        this.fileHash = fileHash;
        this.messageId = messageId;
    }

    public Envelope(String senderId, String receiverId, String operation, String message, String fileUrl, String fileHash, String messageId, String timestamp) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.operation = operation;
        this.message = message;
        this.fileUrl = fileUrl;
        this.fileHash = fileHash;
        this.messageId = messageId;
        this.timestamp = timestamp;
    }

    // Конструктор для відправки лише файлу
//    public Envelope(String senderId, String receiverId, String operation, String fileUrl, String fileHash, String messageId) {
//        this.senderId = senderId;
//        this.receiverId = receiverId;
//        this.operation = operation;
//        this.message = null;
//        this.fileUrl = fileUrl;
//        this.fileHash = fileHash;
//    }

    // Приватний конструктор для білдера
    private Envelope(Builder builder) {
        this.senderId = builder.senderId;
        this.receiverId = builder.receiverId;
        this.operation = builder.operation;
        this.message = builder.message;
        this.fileUrl = builder.fileUrl;
        this.fileHash = builder.fileHash;
        this.messageId = builder.messageId;
        this.messageStatus = builder.messageStatus;
        this.timestamp = builder.timestamp;
    }

    // Builder клас для Envelope
    public static class Builder {
        private String messageId;
        private String senderId;
        private String receiverId;
        private String operation;
        private String message;
        private String fileUrl;
        private String fileHash;
        private String messageStatus;
        public String timestamp;

        public Builder setSenderId(String senderId) {
            this.senderId = senderId;
            return this;
        }

        public Builder setReceiverId(String receiverId) {
            this.receiverId = receiverId;
            return this;
        }

        public Builder setOperation(String operation) {
            this.operation = operation;
            return this;
        }

        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder setFileUrl(String fileUrl) {
            this.fileUrl = fileUrl;
            return this;
        }

        public Builder setFileHash(String fileHash) {
            this.fileHash = fileHash;
            return this;
        }

        public Builder setMessageId(String messageId) {
            this.messageId = messageId;
            return this;
        }

        public Builder setMessageStatus(String messageStatus) {
            this.messageStatus = messageStatus;
            return this;
        }

        public Builder setTime(String timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Envelope build() {
            return new Envelope(this);
        }
    }

    // Конструктор для десеріалізації з JSONObject
    public Envelope(JSONObject jsonObject) {
        try {
            this.senderId = jsonObject.getString("senderId");
            this.receiverId = jsonObject.getString("receiverId");
            this.operation = jsonObject.getString("operation");
            this.message = jsonObject.optString("message", null);
            this.fileUrl = jsonObject.optString("fileUrl", null);
            this.fileHash = jsonObject.optString("fileHash", null);
            this.messageId = jsonObject.optString("messageId", null);
            this.messageStatus = jsonObject.optString("messageStatus", null);
            this.timestamp = jsonObject.optString("timestamp", null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Серіалізує поточний об’єкт UserSetting у JSONObject.
     *
     * @param fields параметри за якими ми будемо повертати Json
     * @return JSONObject, що представляє налаштування користувача
     */
    public JSONObject toJson(String... fields) {
        JSONObject jsonObject = new JSONObject();
        try {
            for (String field : fields) {
                switch (field) {
                    case "senderId":
                        jsonObject.put("senderId", senderId);
                        break;
                    case "receiverId":
                        jsonObject.put("receiverId", receiverId);
                        break;
                    case "operation":
                        jsonObject.put("operation", operation);
                        break;
                    case "message":
                        jsonObject.put("message", message);
                        break;
                    case "fileUrl":
                        jsonObject.put("fileUrl", fileUrl);
                        break;
                    case "fileHash":
                        jsonObject.put("fileHash", fileHash);
                        break;
                    case "messageId":
                        jsonObject.put("messageId", messageId);
                        break;
                    case "messageStatus":
                        jsonObject.put("messageStatus", messageStatus);
                        break;
                    case "timestamp":
                        jsonObject.put("messageStatus", timestamp);
                        break;
                    default:
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    // Метод для серіалізації в JSONObject
    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("senderId", senderId);
            jsonObject.put("receiverId", receiverId);
            jsonObject.put("operation", operation);
            jsonObject.put("message", message);
            jsonObject.put("fileUrl", fileUrl);
            jsonObject.put("fileHash", fileHash);
            jsonObject.put("messageId", messageId);
            jsonObject.put("messageStatus", messageStatus);
            jsonObject.put("timestamp", timestamp);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    // Геттери та сеттери для полів
    // Геттери для доступу до полів (якщо потрібно)
    public String getSenderId() {
        return senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public String getOperation() {
        return operation;
    }

    public String getMessage() {
        return message;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public String getFileHash() {
        return fileHash;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getMessageStatus() {
        return messageStatus;
    }

    public String getTime() {
        return timestamp;
    }

    public void setTime(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setMessageStatus(String messageStatus) {
        this.messageStatus = messageStatus;
    }

    // Метод для генерації хешу
    public String getHash_m() throws NoSuchAlgorithmException {
        // Використовуємо лише відправника, отримувача і вміст повідомлення
        String input = messageId + message + senderId + receiverId;

        // Створюємо хеш за допомогою SHA-256
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(input.getBytes());

        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            hexString.append(String.format("%02x", b));
        }

        return hexString.toString();
    }

    // Метод для генерації хешу з файлом
    public String getHash_f() throws NoSuchAlgorithmException {
        // Використовуємо лише відправника, отримувача і вміст повідомлення
        String input = messageId + message + senderId + receiverId + fileUrl + fileHash;

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
