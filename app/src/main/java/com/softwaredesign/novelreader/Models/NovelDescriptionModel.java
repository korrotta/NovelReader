package com.softwaredesign.novelreader.Models;

public class NovelDescriptionModel {
    String name;
    String author;
    String description;
    String imgUrl;

    public NovelDescriptionModel(String name, String author, String description, String imgUrl) {
        this.name = name;
        this.author = author;
        this.description = description;
        this.imgUrl = imgUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }
}
