package com.example.scraper_library;

import java.util.ArrayList;
import java.util.List;

public interface INovelScraper extends Cloneable{
    <T> ArrayList<T> getSearchPageFromKeywordAndPageNumber(String keyword, int page);
    int getNumberOfSearchResultPage(String keyword);

    // 0 - Name, 1 - Author, 2 - Description
    <T> T getNovelDetail(String url);
    int getChapterListNumberOfPages(String url);
    <T> List<T> getChapterListInPage(String novelUrl, int pageNumber);

    <T>List<T> getHomePage();

    //Note: Scraper info getters
    int getNumberOfChaptersPerPage();
    String getSourceName();

    //Note: Reader Activity Scraping methods
    <T> T getChapterContent(String url);
    String getNextChapterUrl(String url);
    String getPreviousChapterUrl(String url);

    <T> T getContentFromNameAndChapName(String name, String chapterName);

    //NOTE: Clone method for prototype pattern
    INovelScraper clone();

}