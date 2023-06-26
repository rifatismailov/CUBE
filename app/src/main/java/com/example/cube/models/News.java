package com.example.cube.models;

import android.net.Uri;

import com.example.cube.control.Check;
import com.example.cube.control.Side;

public class News {
    private String personId, news, imageUrl = "";
    private int feeling = -1;
    private int emojisPosition = 0;
    Uri selectedUrl;
    Check check;
    Side side;


    public News(String news, Check check, Side side) {
        this.news = news;
        this.check = check;
        this.side = side;
    }

    public News(String news, Uri selectedUrl, Check check, Side side) {
        this.news = news;
        this.selectedUrl = selectedUrl;
        this.check = check;
        this.side = side;
        this.imageUrl = selectedUrl.toString();
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


    public String getPersonId() {
        return personId;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

    public String getNews() {
        return news;
    }

    public void setNews(String news) {
        this.news = news;
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
