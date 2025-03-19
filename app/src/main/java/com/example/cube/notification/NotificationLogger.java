package com.example.cube.notification;

public class NotificationLogger {
    private String info;
    private String log;

    public NotificationLogger(String info, String log) {
        this.info = info;
        this.log = log;
    }


    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }
}
