package com.example.web_socket_service.socket;

public class ConnectionInfo {
    private volatile String senderId;       // ІД відправника
    private volatile String receiverId;     // ІД отримувача
    private String ip;
    private String port;
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
