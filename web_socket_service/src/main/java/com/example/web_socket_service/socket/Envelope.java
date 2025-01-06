package com.example.web_socket_service.socket;

import org.json.JSONObject;

public class Envelope {
    private String messageId;     // Унікальний ідентифікатор повідомлення
    private String senderId;       // ІД відправника
    private String receiverId;     // ІД отримувача
    private String operation;
    private String message;    // Саме повідомлення (може бути null)
    private String fileUrl;        // Посилання на файл (може бути null)
    private String fileHash;       // Хеш-сума файла (може бути null)
    private String messageStatus;

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

    // Конструктор для відправки лише файлу
    public Envelope(String senderId, String receiverId, String operation, String fileUrl, String fileHash, String messageId) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.operation = operation;
        this.message = null;
        this.fileUrl = fileUrl;
        this.fileHash = fileHash;
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

        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    // Конструктор для десеріалізації з JSONObject
    public Envelope(JSONObject jsonObject) {
        try {
            this.senderId = jsonObject.getString("senderId");
            this.receiverId = jsonObject.getString("receiverId");
            this.operation = jsonObject.getString("operation");
            this.message = jsonObject.getString("message");
            this.fileUrl = jsonObject.optString("fileUrl", null);  // fileUrl може бути необов'язковим
            this.fileHash = jsonObject.optString("fileHash", null);  // fileHash може бути необов'язковим
            this.messageId = jsonObject.getString("messageId");  // fileHash може бути необов'язковим
            this.messageStatus = jsonObject.optString("messageStatus", null);  // fileHash може бути необов'язковим

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Геттери та сеттери для полів
    // Геттери для доступу до полів (якщо потрібно)
    public String getSenderId() {
        return senderId;
    }

    public String getReceiverId() {
        return receiverId;
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

    public void setMessageStatus(String messageStatus) {
        this.messageStatus = messageStatus;
    }
}
