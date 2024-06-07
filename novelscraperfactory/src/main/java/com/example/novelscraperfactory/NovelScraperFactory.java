package com.example.novelscraperfactory;


import java.util.ArrayList;
import java.util.List;

public interface NovelScraperFactory {
    <T> ArrayList<T> getSearchPageFromKeyword(String keyword);

    // 0 - Name, 1 - Author, 2 - Description
    <T> T getNovelDetail(String url);
    int getChapterListNumberOfPages(String url);
    <T> List<T> getChapterListFromUrl(String url);

    <T>List<T> getHomePage(String url);

    //Info getter
    int getNumberOfChaptersPerPage();
    String getSourceName();

    //Reader Activity Scraping methods''
    String getChapterContent(String url);
    String getNextChapterUrl(String url);
    String getPreviousChapterUrl(String url);

}
