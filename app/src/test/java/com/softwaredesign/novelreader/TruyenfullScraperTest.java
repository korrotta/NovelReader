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




    @Test
    public void testGetChapterListInPage() {

        // Instantiate TruyenfullScraper
        TruyenfullScraper scraper = new TruyenfullScraper();

        // Call getChapterListInPage method
        List<ChapterModel> chapters = scraper.getChapterListInPage("https://truyenfull.vn/tao-hoa-chi-mon/", 1);

        assertNotNull("Chapters list should not be null", chapters);
        assertFalse("Chapters list should not be empty", chapters.isEmpty());
        for (ChapterModel chapter : chapters) {
            assertNotNull("Chapter name should not be null", chapter.getChapterName());
            assertNotNull("Chapter URL should not be null", chapter.getChapterUrl());
        }

        // Assert the results
        assertNotNull(chapters);
    }



    @Test
    public void testGetNumberOfSearchResultPage() {

        // Instantiate TruyenfullScraper
        TruyenfullScraper scraper = new TruyenfullScraper();

        // Call getNumberOfSearchResultPage method
        Integer result = scraper.getNumberOfSearchResultPage("test");

        // Assert the results
        assertNotNull(result);
    }




    @Test
    public void testGetChapterListNumberOfPages() {

        // Instantiate TruyenfullScraper
        TruyenfullScraper scraper = new TruyenfullScraper();

        // Call getChapterListNumberOfPages method
        Integer result = scraper.getChapterListNumberOfPages("https://truyenfull.vn/tao-hoa-chi-mon/");

        // Assert the results
        assertTrue("Number of pages should be greater than zero", result > 0);

        // Assert the results
        assertNotNull(result);
    }



    @Test
    public void testGetChapterContent() {

        // Instantiate TruyenfullScraper
        TruyenfullScraper scraper = new TruyenfullScraper();

        // Call getChapterContent method
        ChapterContentModel result = scraper.getChapterContent("https://truyenfull.vn/tao-hoa-chi-mon/chuong-1-1/");

        // Kiểm tra kết quả trả về
        assertNotNull("Chapter content should not be null", result);

        // Assert the results
        assertNotNull("Chapter content text should not be null", result.getContent());

        // Kiểm tra  content có hợp lệ không (không rỗng)
        assertFalse("Chapter content should not be empty", result.getContent().isEmpty());

    }



    @Test
    public void testGetNextChapterUrl() {

        // Instantiate TruyenfullScraper
        TruyenfullScraper scraper = new TruyenfullScraper();

        // Call getNextChapterUrl method
        String result = scraper.getNextChapterUrl("https://truyenfull.vn/tao-hoa-chi-mon/chuong-1-1/");

        // Kiểm tra kết quả trả về
        assertNotNull("Next chapter URL should not be null", result);
        assertTrue("Next chapter URL should start with 'https://'", result.startsWith("https://"));
        assertTrue("Next chapter URL should contain '/chuong-'", result.contains("/chuong-"));

    }




    @Test
    public void testGetPreviousChapterUrl() {

        // Instantiate TruyenfullScraper
        TruyenfullScraper scraper = new TruyenfullScraper();

        // Call getPreviousChapterUrl method
        String result = scraper.getPreviousChapterUrl("https://truyenfull.vn/tao-hoa-chi-mon/chuong-1-2/");

        // Kiểm tra kết quả trả về
        assertNotNull("Previous chapter URL should not be null", result);
        assertTrue("Previous chapter URL should start with 'https://'", result.startsWith("https://"));
        assertTrue("Previous chapter URL should contain '/chuong-'", result.contains("/chuong-"));
    }




    @Test
    public void testClone() {

        // Instantiate TruyenfullScraper
        TruyenfullScraper scraper = new TruyenfullScraper();

        // Call clone method
        TruyenfullScraper result = (TruyenfullScraper) scraper.clone();

        // Kiểm tra kết quả trả về
        assertNotNull("Cloned scraper should not be null", result);

        // Kiểm tra đối tượng clone không cùng tham chiếu với đối tượng ban đầu
        assertNotSame("Cloned scraper should not be the same instance as the original", scraper, result);
    }



    @Test
    public void testGetNumberOfChaptersPerPage() {

        // Instantiate TruyenfullScraper
        TruyenfullScraper scraper = new TruyenfullScraper();

        // Call getNumberOfChaptersPerPage method
        Integer result = scraper.getNumberOfChaptersPerPage();

        // Kiểm tra kết quả trả về
        assertNotNull("Number of chapters per page should not be null", result);
        assertTrue("Number of chapters per page should be greater than zero", result > 0);
    }






}

