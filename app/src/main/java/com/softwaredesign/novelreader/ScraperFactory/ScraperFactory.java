package com.softwaredesign.novelreader.ScraperFactory;

import com.example.scraper_library.INovelScraper;
import com.softwaredesign.novelreader.Global.GlobalConfig;

public class ScraperFactory {
    private ScraperFactory(){
    }

    public static INovelScraper createScraper(String inputType){
        for (INovelScraper scraper: GlobalConfig.Global_Source_List){
            // Check if the name of the current scraper matches the inputType
            if (scraper.getSourceName().equals(inputType)){
                // If there is a match, return a clone of the scraper
                return scraper.clone();
            }
        }
        // If no matching scraper is found, return null
        return null;
    }
}
