package com.softwaredesign.novelreader.Scrapers;

import android.util.Log;

import com.example.novelscraperfactory.INovelScraper;
import com.softwaredesign.novelreader.Global.ReusableFunction;
import com.softwaredesign.novelreader.Models.ChapterContentModel;
import com.softwaredesign.novelreader.Models.ChapterModel;
import com.softwaredesign.novelreader.Models.NovelDescriptionModel;
import com.softwaredesign.novelreader.Models.NovelModel;


import org.checkerframework.checker.units.qual.C;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TruyencvScraper implements INovelScraper {

    private final String SEARCH_DEFAULT_URL = "https://truyencv.vn/tim-kiem?tukhoa=";
    private final String HOME_PAGE_URL = "https://truyencv.vn";
    private final String ITEM_TYPE = "https://schema.org/Book";


    private final int CHAPTERS_PER_PAGE = 50;
    private final String SOURCE_NAME = "Truyencv";
    @Override
    public ArrayList<NovelModel> getSearchPageFromKeywordAndPageNumber(String keyword, int page) {
        ArrayList<NovelModel> novels = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(SEARCH_DEFAULT_URL + keyword + "&page=" + page).get();
            Elements books = doc.select("div.grid[itemtype=" + ITEM_TYPE+ "]");
            for (Element book: books){
                String url = HOME_PAGE_URL + book.selectFirst("a[href]").attr("href");
                //NOTE: Relati/ve url, so it could be HOME + url (Fixed but not checked)
                String img = book.selectFirst("img[itemprop=image]").attr("src");
                String name = book.selectFirst("h3[itemprop=name]").text();
                String author = book.selectFirst("span[itemprop=author]").text();

                NovelModel novel = new NovelModel(name, url, author, img);
                novels.add(novel);
            }
            return novels;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getNumberOfSearchResultPage(String keyword) {
        final String searchUrl = SEARCH_DEFAULT_URL +keyword;
        try {
            Document doc = Jsoup.connect(searchUrl).get();
            Element pagination = doc.selectFirst("ul.flex.mx-auto");
            if (pagination!=null){
                Elements pages = pagination.select("li");
                if (pages.size() == 0){
                    Element book = doc.select("div.grid[itemtype=" + ITEM_TYPE+ "]").first();
                    if (book == null) return 0;
                    return 1;
                }
                Element lastPage = pages.last();
                if (lastPage.text().contains("Cuối")) {
                    String holder = lastPage.selectFirst("a[href]").attr("href");
                    int page = Integer.parseInt(parseUrlForPageId(holder));
                    //NOTE: The split can be wrong
                    return page;
                }

                if (pages.size() > 2) {
                    ReusableFunction.LogVariable(pages.toString());
                    int page = Integer.parseInt(pages.get(pages.size()-2).text());
                    //NOTE: -2 to ignore >>, but can go wrong.
                    return page;
                }
            }

            Element book = doc.select("div.grid[itemtype=" + ITEM_TYPE+ "]").first();
            if (book == null) return 0;
            return 1;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public NovelDescriptionModel getNovelDetail(String url) {
        try {
            Document doc = Jsoup.connect(url).get();
            Element info = doc.selectFirst("div[itemtype="+ITEM_TYPE+"]");
            String name = info.selectFirst("h3").text();
            String author = info.selectFirst("div[itemprop=author]").selectFirst("span[itemprop=name]").text();
            String description = doc.getElementById("gioi-thieu-truyen").toString();
            String img = info.selectFirst("img[src]").attr("src");
            NovelDescriptionModel ndm = new NovelDescriptionModel(name, author, description, img);
            return ndm;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getChapterListNumberOfPages(String url) {
        try {
            Document doc = Jsoup.connect(url+"#danh-sach-chuong").get();

            Element pagination = doc.selectFirst("ul.flex.mx-auto");
            if (pagination == null) {
                return 1;
            }

            Elements pages = pagination.select("li");
            if (pages.size() == 0 ) {
                return 1;
            }

            Element lastPage = pages.last();
            if (lastPage.text().contains("Cuối")){
                String pageId = parseChapterListUrlForPageId(lastPage.selectFirst("a[href]").attr("href"));
                return Integer.parseInt(pageId);
            }
            //Note: -2 to ignore > but can be wrong
            Element preLastPage = pages.get(pages.size()-2);
            String pageId = parseChapterListUrlForPageId(preLastPage.selectFirst("a[href]").attr("href"));
            return Integer.parseInt(pageId);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ChapterModel> getChapterListInPage(String novelUrl, int pageNumber) {
        final String finalUrl = novelUrl + "?page=" + pageNumber + "#danh-sach-chuong";
        List<ChapterModel> chapters = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(finalUrl).get();
            Element dsChuong = doc.getElementById("danh-sach-chuong");
            Elements cols = dsChuong.select("ul.col-span-6");
            for (Element col: cols){
                Elements rows = col.select("li");
                for (Element row: rows){
                    String name = capitalizeFirstLetterOfWord(row.text());
                    String url = row.selectFirst("a[href]").attr("href");
                    int number = 0;
                    chapters.add(new ChapterModel(name, url, number));
                    //FIXME: 0 here for faster dev, changing later
                }
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
            Document document = Jsoup.connect(HOME_PAGE_URL).get();
            Element hot = document.selectFirst("div.grid");
            Elements books = hot.select("div[itemtype=" + ITEM_TYPE + "]");
            for (Element book: books){
                String name = book.selectFirst("a[href]").attr("title");
                String url = book.selectFirst("a[href]").attr("href");
                String img = book.selectFirst("img[src]").attr("src");
                String author = ""; //Note: No author needed here for main page, not a fault.

                NovelModel novel = new NovelModel(name, url, author, img);
                novels.add(novel);

            }
            return novels;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public int getNumberOfChaptersPerPage() {
        return this.CHAPTERS_PER_PAGE;
    }

    @Override
    public String getSourceName() {
        return this.SOURCE_NAME;
    }

    @Override
    public ChapterContentModel getChapterContent(String url) {
        try {
            Document doc = Jsoup.connect(HOME_PAGE_URL+url).get();
            String name = doc.selectFirst("a.capitalize.flex").text();
            String chapterName = doc.select("a.capitalize.flex").last().text();
            String content = doc.getElementById("content-chapter").toString();
            //NOTE: If htmlCompat failed, can use replaceAll to customize it.

            ChapterContentModel ccm = new ChapterContentModel(chapterName, url, content, name);
            return ccm;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getNextChapterUrl(String url) {
        try {
            Document doc = Jsoup.connect(HOME_PAGE_URL+url).get();
            Element control = doc.selectFirst("div.flex.justify-center");
            String nextUrl = control.select("a[href]").last().attr("href");
            if (nextUrl.equals("#")){
                return null;
            }
            return nextUrl;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getPreviousChapterUrl(String url) {
        try {
            Document doc = Jsoup.connect(HOME_PAGE_URL+url).get();
            Element control = doc.selectFirst("div.flex.justify-center");
            String prevUrl = control.select("a[href]").first().attr("href");
            if (prevUrl.equals("#")){
                return null;
            }
            return prevUrl;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //NOTE:BORDER-----------------------------------------------------------------------------------
    //NOTE:BORDER-----------------------------------------------------------------------------------

    private String parseUrlForPageId(String url){
        String[] holder = url.split("=");
        return holder[holder.length-1];

    }

    private String parseChapterListUrlForPageId(String url){
        String[] holder = url.split("\\?");
        String[] holder2 = holder[holder.length-1].split("/");
        String finalString = holder2[0].split("=")[1];
        return finalString;
    }

    private String capitalizeFirstLetterOfWord(String sentence){
        String[] holder = sentence.split(" ");
        String finalSentence = "";
        for (int i= 0; i< holder.length; i++){
            finalSentence = finalSentence + captializeLetter(holder[i]);
            finalSentence += " ";
        }
        finalSentence.substring(0, finalSentence.length()-1);
        return finalSentence;
    }

    private String captializeLetter(String word){
        return word.substring(0,1).toUpperCase() + word.substring(1).toLowerCase();
    }
}
