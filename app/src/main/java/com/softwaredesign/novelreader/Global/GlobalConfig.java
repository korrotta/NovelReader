package com.softwaredesign.novelreader.Global;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.scraper_library.INovelScraper;
import com.softwaredesign.novelreader.Models.NovelSourceModel;
import com.softwaredesign.novelreader.Scrapers.TruyenfullScraper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class GlobalConfig {
    public static List<INovelScraper> Global_Source_List = new ArrayList<>();
    public static INovelScraper Global_Current_Scraper = new TruyenfullScraper(); //default source
}
