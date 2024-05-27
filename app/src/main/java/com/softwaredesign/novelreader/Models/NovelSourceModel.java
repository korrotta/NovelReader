package com.softwaredesign.novelreader.Models;

import com.example.novelscraperfactory.NovelScraperFactory;

public class NovelSourceModel{
    String name;
    NovelScraperFactory scrapperInstance;

    public NovelSourceModel(String name, NovelScraperFactory scrapperInstance) {
        this.name = name;
        this.scrapperInstance = scrapperInstance;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public NovelScraperFactory getScrapperInstance() {
        return scrapperInstance;
    }

    public void setScrapperInstance(NovelScraperFactory scrapperInstance) {
        this.scrapperInstance = scrapperInstance;
    }
}
