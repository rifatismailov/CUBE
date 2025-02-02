package com.example.cube.contact;

import android.util.Log;
import android.widget.ImageView;

import org.json.JSONObject;

import java.io.Serializable;

public class UserData implements Serializable {
    private String id;
    private String publicKey;
    private String privateKey;
    private String receiverPublicKey;
    private String senderKey;
    private String receiverKey;
    private String name;
    private String messageSize;
    private String avatarImageUrl;
    private String accountImageUrl;
    private int size;
    private int progress;  // Додаємо змінну для прогресу

    // Конструктор
    public UserData() {

    }

    public UserData(String id, String publicKey, String name, String messageSize) {
        this.id = id;
        this.publicKey = publicKey;
        this.name = name;
        this.messageSize = messageSize;
    }


    // Геттери для доступу до полів
    public String getId() {
        return id;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getName() {
        return name;
    }

    // Сеттери для можливого оновлення полів
    public void setId(String id) {
        this.id = id;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessageSize() {
        return messageSize;
    }

    public void setMessageSize(String messageSize) {
        this.messageSize = messageSize;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getSize() {
        return size;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getReceiverPublicKey() {
        return receiverPublicKey;
    }

    public void setReceiverPublicKey(String receiverPublicKey) {
        this.receiverPublicKey = receiverPublicKey;
    }

    public String getSenderKey() {
        return senderKey;
    }

    public void setSenderKey(String senderKey) {
        this.senderKey = senderKey;
    }

    public String getReceiverKey() {
        return receiverKey;
    }

    public void setReceiverKey(String receiverKey) {
        this.receiverKey = receiverKey;
    }

    public String getAvatarImageUrl() {
        return avatarImageUrl;
    }

    public void setAvatarImageUrl(String avatarImageUrl) {
        this.avatarImageUrl = avatarImageUrl;
    }

    public String getAccountImageUrl() {
        return accountImageUrl;
    }

    public void setAccountImageUrl(String accountImageUrl) {
        this.accountImageUrl = accountImageUrl;
    }
    // Інші поля і методи

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    // Конструктор для десеріалізації з JSONObject
    public UserData(JSONObject jsonObject) {
        try {
            this.id = jsonObject.optString("id", "No ID");
            this.publicKey = jsonObject.optString("publicKey", "");
            this.privateKey = jsonObject.optString("privateKey", "");
            this.receiverPublicKey = jsonObject.optString("receiverPublicKey", "");
            this.senderKey = jsonObject.optString("senderKey", "");
            this.receiverKey = jsonObject.optString("receiverKey", "");
            this.name = jsonObject.optString("name", "No name");
            this.avatarImageUrl = jsonObject.optString("avatarImageUrl", "");
            this.accountImageUrl = jsonObject.optString("accountImageUrl", "");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Метод для серіалізації в JSONObject
    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", id);
            jsonObject.put("publicKey", publicKey);
            jsonObject.put("privateKey", privateKey);
            jsonObject.put("receiverPublicKey", receiverPublicKey);
            jsonObject.put("senderKey", senderKey);
            jsonObject.put("receiverKey", receiverKey);
            jsonObject.put("name", name);
            jsonObject.put("avatarImageUrl", avatarImageUrl);
            jsonObject.put("accountImageUrl", accountImageUrl);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
}
