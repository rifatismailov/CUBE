package com.example.web_socket_service.socket;

public class ServerURL {
    private String senderId;       // ІД відправника
    private String receiverId;     // ІД отримувача
    private String ip;
    private String port;
    public ServerURL(){

    }
    public ServerURL(String senderId, String receiverId, String ip, String port) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.ip = ip;
        this.port = port;
    }
    public void setReciverId(String receiverId) {
        this.receiverId = receiverId;
    }
    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
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

    public String getServerAddress() {
        if ("443".equals(port)) {
            return String.format("wss://%s:%s", ip, port);
        }
        return String.format("ws://%s:%s", ip, port);
    }

    public String getRegistration() {
        return "{\"userId\":\"" + senderId + "\"}";
    }
}
