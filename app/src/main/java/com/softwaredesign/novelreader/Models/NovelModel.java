package com.softwaredesign.novelreader.Models;

import android.util.Log;

public class NovelModel{
    private String name;
    private String url;
    private String author;
    private String imageDesk;

    public NovelModel(String name, String url, String author, String imageDesk) {
        this.name = name;
        this.url = url;
        this.author = author;
        this.imageDesk = imageDesk;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getImageDesk() {
        return imageDesk;
    }

    public void setImageDesk(String imageDesk) {
        this.imageDesk = imageDesk;
    }

}
