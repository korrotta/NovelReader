package com.softwaredesign.novelreader.Factory;

import com.softwaredesign.novelreader.Models.NovelModel;

import java.util.ArrayList;

public interface NovelScrapperFactory {
    ArrayList<NovelModel> searchPageScrapping(String keyword);

    // 0 - Name, 1 - Author, 2 - Description
    String[] novelDetailScrapping(String url);

    void novelChapterListScrapping(String url);



}
