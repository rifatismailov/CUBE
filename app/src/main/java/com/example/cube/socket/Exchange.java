package com.example.cube.socket;

import com.example.cube.control.Check;

import java.io.Serializable;

public class Exchange implements Serializable {

    String message;
    byte[] bytes = null;
    String fileName;
    Check check;


    public Exchange(String message, Check check) {
        this.message = message;
        this.check = check;
    }

    public Exchange(String message, Check check, byte[] bytes) {
        this.message = message;
        this.check = check;
        this.bytes = bytes;
    }

    public Exchange(Check check, byte[] bytes, String fileName) {
        this.bytes = bytes;
        this.check = check;
        this.fileName = fileName;
    }

    public Exchange(String message, Check check, byte[] bytes, String fileName) {
        this.message = message;
        this.check = check;
        this.bytes = bytes;
        this.fileName = fileName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Check getCheck() {
        return check;
    }

    public void setCheck(Check check) {
        this.check = check;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
