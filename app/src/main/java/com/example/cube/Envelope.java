package com.example.cube;

import org.json.JSONObject;

public class Envelope {

    private String senderId;       // ІД відправника
    private String receiverId;     // ІД отримувача
    private String message;    // Саме повідомлення (може бути null)
    private String fileUrl;        // Посилання на файл (може бути null)
    private String fileHash;       // Хеш-сума файла (може бути null)

    // Конструктор для текстового повідомлення
    public Envelope(String senderId, String receiverId, String message) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
        this.fileUrl = null;
        this.fileHash = null;
    }

    // Конструктор для повідомлення з файлом
    public Envelope(String senderId, String receiverId, String message, String fileUrl, String fileHash) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
        this.fileUrl = fileUrl;
        this.fileHash = fileHash;
    }

    // Конструктор для відправки лише файлу
    public Envelope(String senderId, String receiverId, String fileUrl, String fileHash) {
        this.senderId = senderId;
        this.receiverId = receiverId;
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
            jsonObject.put("message", message);
            jsonObject.put("fileUrl", fileUrl);
            jsonObject.put("fileHash", fileHash);
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
            this.message = jsonObject.getString("message");
            this.fileUrl = jsonObject.optString("fileUrl", null);  // fileUrl може бути необов'язковим
            this.fileHash = jsonObject.optString("fileHash", null);  // fileHash може бути необов'язковим
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

}

