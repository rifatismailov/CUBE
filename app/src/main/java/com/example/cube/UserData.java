package com.example.cube;

public class UserData {
    private String id;
    private String publicKey;
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
}
