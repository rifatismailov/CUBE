package com.example.cube.notification;

public class NotificationLogger {
    private String clas;
    private String log;

    public NotificationLogger(String clas, String log) {
        this.clas = clas;
        this.log = log;
    }


    public String getClas() {
        return clas;
    }

    public void setClas(String clas) {
        this.clas = clas;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }
}
