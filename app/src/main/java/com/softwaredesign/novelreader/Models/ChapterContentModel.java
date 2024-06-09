package com.softwaredesign.novelreader.Models;

public class ChapterContentModel {
    String chapterName;
    String chapterUrl;
    String content;
    String novelName;

    public ChapterContentModel(String chapterName, String chapterUrl, String content, String novelName) {
        this.chapterName = chapterName;
        this.chapterUrl = chapterUrl;
        this.content = content;
        this.novelName = novelName;
    }

    public String getChapterName() {
        return chapterName;
    }

    public String getChapterUrl() {
        return chapterUrl;
    }

    public String getContent() {
        return content;
    }

    public String getNovelName() {
        return novelName;
    }
}
