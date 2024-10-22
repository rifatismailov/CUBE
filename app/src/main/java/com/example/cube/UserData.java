package com.example.cube;

import java.io.Serializable;

public class UserData implements Serializable {
    private String id;
    private String publicKey;
    private String privateKey;
    private String receiverPublicKey;
    private String name;
    private String messageSize;

    // Конструктор
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
}
