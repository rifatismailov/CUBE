package com.example.web_socket_service.socket;

import android.util.Log;

import org.json.JSONObject;

public class ConnectionInfo {
    private volatile String senderId;       // ІД відправника
    private volatile String receiverId;     // ІД отримувача
    private volatile String contacts;
    private volatile String ip;
    private volatile String port;
    private volatile String registration;
    private volatile String life;
    public ConnectionInfo(){
    }
    public ConnectionInfo(String senderId, String receiverId, String ip, String port) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.ip = ip;
        this.port = port;
    }
    public synchronized void setReciverId(String receiverId) {
        this.receiverId = receiverId;
    }
    public synchronized String getReceiverId() {
        return receiverId;
    }

    public synchronized void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public synchronized String getSenderId() {
        return senderId;
    }

    public synchronized void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getContacts() {
        return contacts;
    }

    public void setContacts(String contacts) {
        this.contacts = contacts;
    }

    public String getServerAddress() {
        if ("443".equals(port)) {
            return String.format("wss://%s:%s", ip, port);
        }
        return String.format("ws://%s:%s", ip, port);
    }

    public void setRegistration(String registration) {
        this.registration = registration;
    }

    public String getRegistration() {

        return registration;
    }

    public String getLife() {
        return life;
    }

    public void setLife(String life) {
        this.life = life;
    }
}
