package com.example.folder.explorer;

public class Explorer {
    private String number;
    private String time;
    private String information;
    private boolean check;
    private int image;


    public Explorer(String number, String time, String information, int image, boolean check) {
        this.number = number;
        this.time = time;
        this.information = information;
        this.image = image;
        this.check = check;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getInformation() {
        return information;
    }

    public void setInformation(String information) {
        this.information = information;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public boolean getCheck() {
        return check;
    }

    public boolean setCheck(boolean check) {
        this.check = check;
        return check;

    }
}
