package com.softwaredesign.novelreader.Scrapers;

import android.util.JsonReader;
import android.util.Log;

import com.example.novelscraperfactory.INovelScraper;
import com.softwaredesign.novelreader.Global.ReusableFunction;
import com.softwaredesign.novelreader.Models.ChapterContentModel;
import com.softwaredesign.novelreader.Models.ChapterModel;
import com.softwaredesign.novelreader.Models.NovelDescriptionModel;
import com.softwaredesign.novelreader.Models.NovelModel;
import com.softwaredesign.novelreader.Models.NovelSourceModel;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TangthuvienScraper implements INovelScraper {
    private final String SEARCH_DEFAULT_URL = "https://truyen.tangthuvien.vn/ket-qua-tim-kiem?term=";
    private final String HOME_PAGE_URL = "https://truyen.tangthuvien.vn/";
    private final String ITEM_TYPE = "https://schema.org/Book";

    private final int CHAPTERS_PER_PAGE = 75;
    private final String SOURCE_NAME = "Tangthuvien";
    @Override
    public ArrayList<NovelModel> getSearchPageFromKeywordAndPageNumber(String keyword, int page) {
        ArrayList<NovelModel> novels = new ArrayList<>();
        try {
            final String SearchUrl = SEARCH_DEFAULT_URL + keyword + "&page="+page;
            Document doc = Jsoup.connect(SearchUrl).get();

            Element novelListElement = doc.select("div.rank-view-list").first();
            Elements novelElements = novelListElement.select("li");

            for (Element novelElement: novelElements){
                Element imgBox = novelElement.selectFirst("div.book-img-box");
                Element infoBox = novelElement.selectFirst("div.book-mid-info");

                String name = infoBox.selectFirst("h4").text();
                String author = infoBox.select("a.name").text();
                String url = imgBox.select("a[href]").attr("href");
                String imgUrl = imgBox.select("img.lazy").attr("src");

                NovelModel novel = new NovelModel(name, url, author, imgUrl);
                novels.add(novel);
            }
            return novels;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getNumberOfSearchResultPage(String keyword) {
        try {
            Document doc = Jsoup.connect(SEARCH_DEFAULT_URL + keyword).get();

            Element paginationElement = doc.select("ul.pagination").first();
            if (paginationElement != null) {
                int max = 0;
                Elements allPagesElement = paginationElement.select("li");
                for (Element pageElement : allPagesElement) {
                    Log.d("li check", pageElement.toString());
                    String pageNumberAsString = pageElement.text();
                    try {
                        int pageNumber = Integer.parseInt(pageNumberAsString);
                        if (pageNumber > max) max = pageNumber;
                    } catch (NumberFormatException e) {
                        continue;
                    }
                }
                return max;
            }

            Element bookImgTextElement = doc.select("div.book-img-text").first();
            String text = bookImgTextElement.text();
            if (text.contains("Không tìm thấy")) return 0;
            return 1;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public NovelDescriptionModel getNovelDetail(String url) {
        try {
            Document doc = Jsoup.connect(url).get();

            String name = doc.select("h1").first().text();
            String author = doc.getElementById("authorId").selectFirst("p").text();
            String description = "";
            Element descriptionElement = doc.select("div.book-intro").first();
            if (descriptionElement != null){
                description = descriptionElement.select("p").first().toString();
            }
            String thumbnailUrl = doc.getElementById("bookImg").select("img").first().attr("src");

            NovelDescriptionModel ndm = new NovelDescriptionModel(name, author, description, thumbnailUrl);
            return ndm;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getChapterListNumberOfPages(String url) {
        try {
            Document doc = Jsoup.connect(url).get();
            Elements paginationElements = doc.select("ul.pagination").first().select("li");
            if (paginationElements != null){
                int maxPage = 0;

                for (Element page: paginationElements) {
                    Element pageIdElement = page.select("a").first();
                    if (pageIdElement == null) continue;
                    String pageIdString = pageIdElement.text();
                    if (page.text().contains("cuối")) {
                        return parseOnClickGetNumber(pageIdElement.attr("onclick"))+1;
                    }
                    try {
                        int pageId = Integer.parseInt(pageIdString);
                        if (pageId>maxPage) maxPage = pageId;
                    } catch (NumberFormatException e){
                        continue;
                    }
                }
                return maxPage;
            }
            return 1;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ChapterModel> getChapterListInPage(String novelUrl, int pageNumber) {
        List<ChapterModel> chapters = new ArrayList<>();

        String novelId = getStoryId(novelUrl);

        final String CHAPTER_PAGE_URL = "https://truyen.tangthuvien.vn/doc-truyen/page/ " +novelId+ "?page=" +(pageNumber-1)+ "&limit=75&web=1";
        try {
            Document doc = Jsoup.connect(CHAPTER_PAGE_URL).get();
            Elements chapterElements = doc.selectFirst("ul.cf").select("a[href]");
            for (Element chapter: chapterElements){
                String rawName = chapter.attr("title");
                String name = rawName.replaceAll("&nbsp;", " ");
                String url = chapter.attr("href");
                int number = 0;
                try {
                    number = Integer.parseInt(name.split(" ")[1]);
                }catch (NumberFormatException e){
                    number = 0;
                }

                ChapterModel c = new ChapterModel(name, url, number);
                chapters.add(c);
            }
            return chapters;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<NovelModel> getHomePage() {
        List<NovelModel> novels = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(HOME_PAGE_URL).get();
            Element novelListElement = doc.select("div.center-book-list.fl").last();
            assert novelListElement != null;
            Elements novelElements = novelListElement.select("li");
            for (Element novelElement: novelElements) {
                String imgUrl = novelElement.selectFirst("div.book-img").selectFirst("img.lazy").attr("src");
                String name = novelElement.selectFirst("h3").text();
                String url = novelElement.selectFirst("h3").selectFirst("a[href]").attr("href");
                String author = novelElement.selectFirst("a.author").text();

                NovelModel novel = new NovelModel(name, url, author, imgUrl);
                novels.add(novel);
            }
            return novels;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ChapterContentModel getChapterContent(String url) {
        String content ="";
        try {
            Document doc = Jsoup.connect(url).get();
            Element boxchaps = doc.select("div.box-chap").first();
            content = boxchaps.wholeText();
            String name = doc.select("h1.truyen-title").text();
            String chapterName = doc.selectFirst("h2").text().replaceAll("&nbsp;", " ");
            Log.d("Content", content);
            content = content.replaceAll("\n", "<br>");
            ChapterContentModel contentModel = new ChapterContentModel(chapterName, url, content, name);
            return contentModel;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getNextChapterUrl(String url) {
        String novelId = "";
        String chapterId = "";
        try {
            //doc1 fetching
            Document doc = Jsoup.connect(url).get();
            String novelUrl = doc.selectFirst("h1.truyen-title").selectFirst("a[href]").attr("href");

            Log.d("Novel URL", novelUrl);
            novelId = getStoryId(novelUrl);
            Log.d("Novel id latest", novelId);

            Element boxChap = doc.selectFirst("div.box-chap");
            String boxChapName = boxChap.className();
            String[] splitCarriage = boxChapName.split("-");
            chapterId = splitCarriage[splitCarriage.length - 1];

            //Doc2 fetching
            final String ALL_CHAPTERS_URL = "https://truyen.tangthuvien.vn/story/chapters?story_id=" + novelId;
            Document doc2 = Jsoup.connect(ALL_CHAPTERS_URL).get();
            int total = Integer.parseInt(doc2.select("li").last().attr("title"));

            Element current = doc2.selectFirst("li[ng-chap="+chapterId+"]");
            if (current!=null){
                int currentId = Integer.parseInt(current.attr("title"));
                if (currentId >= total){
                    return "";
                }
                int nextId = currentId +1;
                Element next = doc2.selectFirst("li[title="+nextId+"]");
                return next.selectFirst("a[href]").attr("href");
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Log.d("novelId latest", novelId);
        return "";
    }


    @Override
    public String getPreviousChapterUrl(String url) {
        String novelId = "";
        String chapterId = "";
        try {
            Document doc = Jsoup.connect(url).get();
            String novelUrl = doc.selectFirst("h1.truyen-title").selectFirst("a[href]").attr("href");

            novelId = getStoryId(novelUrl);
            Log.d("Novel id latest", novelId);

            Element boxChap = doc.selectFirst("div.box-chap");
            String boxChapName = boxChap.className();
            String[] splitCarriage = boxChapName.split("-");
            chapterId = splitCarriage[splitCarriage.length - 1];

            //Doc2 fetching
            final String ALL_CHAPTERS_URL = "https://truyen.tangthuvien.vn/story/chapters?story_id=" + novelId;
            Document doc2 = Jsoup.connect(ALL_CHAPTERS_URL).get();


            Element current = doc2.selectFirst("li[ng-chap="+chapterId+"]");
            if (current!=null){
                int currentId = Integer.parseInt(current.attr("title"));
                if (currentId <= 1){
                    return "";
                }
                int prevId = currentId -1;
                Element prev = doc2.selectFirst("li[title="+prevId+"]");
                return prev.selectFirst("a[href]").attr("href");
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return "";
    }

    @Override
    public int getNumberOfChaptersPerPage() {
        return this.CHAPTERS_PER_PAGE;
    }

    @Override
    public String getSourceName() {
        return this.SOURCE_NAME;
    }

    //Loading(number)
    private int parseOnClickGetNumber(String loadingCall){
        String rightStr = loadingCall.split("\\(")[1];
        String finalStr = rightStr.split("\\)")[0];
        return Integer.parseInt(finalStr);
    }

    private String getStoryId(String novelUrl){
        try {
            Document doc = Jsoup.connect(novelUrl).get();
            Element idElement = doc.select("meta[name=book_detail]").first();
            if (idElement !=null){
               String novelId = idElement.attr("content");
               return novelId;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return "";
    }

}
