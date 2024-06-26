package com.softwaredesign.novelreader.Models;

public class ChapterModel {
    String chapterName, chapterUrl;
    int chapterNumber;

    public ChapterModel(String chapterName, String chapterUrl, int chapterNumber) {
        this.chapterName = chapterName;
        this.chapterUrl = chapterUrl;
        this.chapterNumber = chapterNumber;
    }

    public String getChapterName() {
        return chapterName;
    }

    public void setChapterName(String chapterName) {
        this.chapterName = chapterName;
    }

    public String getChapterUrl() {
        return chapterUrl;
    }

    public void setChapterUrl(String chapterUrl) {
        this.chapterUrl = chapterUrl;
    }

    public int getChapterNumber() {
        return chapterNumber;
    }

    public void setChapterNumber(int chapterNumber) {
        this.chapterNumber = chapterNumber;
    }
}
