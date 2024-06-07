package com.example.novelscraperfactory;


import java.util.ArrayList;
import java.util.List;

public interface NovelScraperFactory {
    <T> ArrayList<T> searchPageScraping(String keyword);

    // 0 - Name, 1 - Author, 2 - Description
    <T> T novelDetailScraping(String url);
    int chapterListNumberOfPages(String url);
    <T> List<T> novelChapterListScraping(String url);

    <T>List<T> novelHomePageScraping(String url);

    int getNumberOfChaptersPerPage();


}
