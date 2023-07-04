package com.example.cube.models;

import android.net.Uri;

import com.example.cube.control.Check;
import com.example.cube.control.Side;

public class Message {
    private String messageId, message, senderId, imageUrl = "";
    private long timestamp;
    private int feeling = -1;
    private int emojisPosition = 0;
    Uri selectedUrl;
    Check check;
    Side side;
    byte[] image;
    int imageWidth;
    int imageHeight;


    public Message(String message, Check check, Side side) {
        this.message = message;
        this.check = check;
        this.side = side;
    }

    public Message(String message, Uri selectedUrl, Check check, Side side) {
        this.message = message;
        this.selectedUrl = selectedUrl;
        this.check = check;
        this.side = side;
        this.imageUrl = selectedUrl.toString();
    }

    public Message(String message, byte[] image,int imageWidth, int imageHeight, Check check, Side side) {
        this.message = message;
        this.image = image;
        this.imageWidth=imageWidth;
        this.imageHeight=imageHeight;
        this.check = check;
        this.side = side;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public int getImageWidth() {
        return imageWidth;
    }

    public void setImageWidth(int imageWidth) {
        this.imageWidth = imageWidth;
    }

    public int getImageHeight() {
        return imageHeight;
    }

    public void setImageHeight(int imageHeight) {
        this.imageHeight = imageHeight;
    }

    public Check getCheck() {
        return check;
    }

    public void setCheck(Check check) {
        this.check = check;
    }

    public Side getSide() {
        return side;
    }

    public void setSide(Side side) {
        this.side = side;
    }


    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getFeeling() {
        return feeling;
    }

    public void setFeeling(int feeling) {
        this.feeling = feeling;
    }

    public int getEmojisPosition() {
        return emojisPosition;
    }

    public void setEmojisPosition(int emojisPosition) {
        this.emojisPosition = emojisPosition;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
