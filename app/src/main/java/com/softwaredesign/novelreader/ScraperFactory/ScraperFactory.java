package com.softwaredesign.novelreader.ScraperFactory;

import com.example.scraper_library.INovelScraper;
import com.softwaredesign.novelreader.Global.GlobalConfig;

public class ScraperFactory {
    private ScraperFactory(){
    }

    public static INovelScraper createScraper(String inputType){
        for (INovelScraper scraper: GlobalConfig.Global_Source_List){
            if (scraper.getSourceName().equals(inputType)){
                return scraper.clone();
            }
        }
        return null;
    }
}
