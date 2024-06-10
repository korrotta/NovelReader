package com.example.scraper_library;

import java.util.ArrayList;
import java.util.List;

public interface INovelScraper extends Cloneable{
    //Note: NovelModel constructor: (String name, String url, String author, String imageDesk)
    enum NovelModelIndex{
        NAME,
        URL,
        AUTHOR,
        IMG
    }

    //Note: NovelDescriptionModel constructor: (String name, String author, String description, String imgUrl)
    enum NovelDescriptionModelIndex{
        NAME,
        AUTHOR,
        DESCRIPTION,
        IMG
    }

    //Note: ChapterModel constructor: (String chapterName, String chapterUrl, int chapterNumber)
    enum ChapterModelIndex{
        NAME,
        URL,
        NUMBER
    }
    //Note: ChapterContentModel constructor: (String chapterName, String chapterUrl, String content, String novelName)
    enum ChapterContentModelIndex{
        NAME,
        URL,
        CONTENT,
        NOVEL
    }
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