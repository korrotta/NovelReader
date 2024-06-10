package com.softwaredesign.novelreader.Models;

import com.example.novelscraperfactory.INovelScraper;

public class NovelSourceModel {
    String name;
    INovelScraper scrapperInstance;

    public NovelSourceModel(String name, INovelScraper scrapperInstance) {
        this.name = name;
        this.scrapperInstance = scrapperInstance;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public INovelScraper getScrapperInstance() {
        return scrapperInstance;
    }

    public void setScrapperInstance(INovelScraper scrapperInstance) {
        this.scrapperInstance = scrapperInstance;
    }

}
