package com.softwaredesign.novelreader.Factory;

import com.softwaredesign.novelreader.ChapterListItem;
import com.softwaredesign.novelreader.Models.NovelModel;

import java.util.ArrayList;
import java.util.List;

public interface NovelScrapperFactory {
    ArrayList<NovelModel> searchPageScrapping(String keyword);

    // 0 - Name, 1 - Author, 2 - Description
    String[] novelDetailScrapping(String url);

    List<ChapterListItem> novelChapterListScrapping(String url);



}
