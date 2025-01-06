package com.example.web_socket_service.socket;

public class ServerURL {
    private String senderId;       // ІД відправника
    private String receiverId;
    private String ip;
    private String port;

    public String getReciverId() {
        return receiverId;
    }

    public void setReciverId(String receiverId) {
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
    public String getServerAddress(){
        return String.format("ws://%s:%s", ip, port);
    }
    public String getRegistration(){
        return String.format("REGISTER:{\"userId\":\"%s\"}", senderId);
    }
}
