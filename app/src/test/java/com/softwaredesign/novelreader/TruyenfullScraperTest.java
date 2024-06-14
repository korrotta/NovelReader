package com.softwaredesign.novelreader;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import com.softwaredesign.novelreader.Models.ChapterContentModel;
import com.softwaredesign.novelreader.Models.ChapterModel;
import com.softwaredesign.novelreader.Models.NovelDescriptionModel;
import com.softwaredesign.novelreader.Models.NovelModel;
import com.softwaredesign.novelreader.Scrapers.TruyenfullScraper;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class TruyenfullScraperTest {

    @Test
    public void testGetSearchPageFromKeywordAndPageNumber() {
        // Instantiate TruyenfullScraper
        TruyenfullScraper scraper = new TruyenfullScraper();

        // Call getSearchPageFromKeywordAndPageNumber method
        ArrayList<NovelModel> result = scraper.getSearchPageFromKeywordAndPageNumber("test", 1);

        // Assert the results
        assertNotNull(result);
    }

    @Test
    public void testGetNovelDetail() {
        // Instantiate TruyenfullScraper
        TruyenfullScraper scraper = new TruyenfullScraper();

        // Call getNovelDetail method
        NovelDescriptionModel detail = scraper.getNovelDetail("https://truyenfull.vn/tao-hoa-chi-mon/");

        assertNotNull("Novel detail should not be null", detail);
        assertNotNull("Novel name should not be null", detail.getName());
        assertNotNull("Novel author should not be null", detail.getAuthor());
        assertNotNull("Novel description should not be null", detail.getDescription());
        assertNotNull("Novel image URL should not be null", detail.getImgUrl());
        // Assert the results
        assertNotNull(detail);
    }

    @Test
    public void testGetHomePage() {

        // Instantiate TruyenfullScraper
        TruyenfullScraper scraper = new TruyenfullScraper();

        // Call getHomePage method
        List<NovelModel> result = scraper.getHomePage();

        // Assert the results
        assertNotNull(result);
    }




}

