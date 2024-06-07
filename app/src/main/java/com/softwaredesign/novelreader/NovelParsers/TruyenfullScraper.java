package com.softwaredesign.novelreader.NovelParsers;

import android.util.Log;

import com.example.novelscraperfactory.NovelScraperFactory;
import com.softwaredesign.novelreader.Models.ChapterModel;
import com.softwaredesign.novelreader.Models.NovelDescriptionModel;
import com.softwaredesign.novelreader.Models.NovelModel;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TruyenfullScraper implements NovelScraperFactory {
    public String SEARCH_DEFAULT_URL = "https://truyenfull.vn/tim-kiem/?tukhoa=";
    public String ITEM_TYPE = "https://schema.org/Book";

    private final int CHAPTERS_PER_PAGE = 50;
    @Override
    public ArrayList<NovelModel> searchPageScraping(String keyword) {

        // Construct the search URL using the keyword
        String searchUrl = this.SEARCH_DEFAULT_URL + keyword;
        ArrayList<NovelModel> novelList = new ArrayList<>();

        try {

            // Fetch and parse the search result page
            Document doc = Jsoup.connect(searchUrl)
                    .timeout(10000)
                    .get();

            // Select rows containing novel information
            Elements rowNodes = doc.select("div.row");

            for (Element row: rowNodes){

                String itemScope = row.attr("itemtype");

                // Check if the item type matches the novel item type
                if (itemScope.equals(this.ITEM_TYPE)){

                    // Extract novel details
                    String novelName = getNovelNameInRowNode(row);
                    String novelHref = getNovelLinkInRowNode(row);
                    String novelAuthor = getNovelAuthorInRowNode(row);
                    String novelThumbnailsUrl = getThumbnailsUrlInRowNode(row);

                    // Create a new NovelModel object and add it to the list
                    NovelModel novel = new NovelModel(novelName, novelHref, novelAuthor, novelThumbnailsUrl);
                    novelList.add(novel);
                    Log.d("Novel data", novel.toString());
                }
            }
            return novelList;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public NovelDescriptionModel novelDetailScraping(String url) {
        try {

            // Fetch and parse the novel detail page
            Document doc = Jsoup.connect(url).timeout(6000).get();
            Element headerNode = doc.getElementById("truyen");
//            Log.d("headerNode", headerNode.toString());

            // Extract novel details
            String novelName = headerNode.selectFirst("h3.title").text();
            String novelAuthor = headerNode.selectFirst("a[itemprop=author]").text();
            Element novelDescriptionNode = headerNode.selectFirst("div[itemprop=description]");
            String novelDeskImgUrl = headerNode.selectFirst("img[itemprop=image]").attr("src");
            String content = null;
            if (novelDescriptionNode!=null){
                content = novelDescriptionNode.toString();
                content = content.replace("<div class=\"desc-text desc-text-full\" itemprop=\"description\">", "");
//                content = content.replaceAll("<b>", "");
//                content = content.replaceAll("</b>", "");
//                content = content.replaceAll("<i>", "");
//                content = content.replaceAll("</i>","");
//                content = content.replaceAll("&nbsp;", " ");
//                content = content.replace("</div>", "");
//                content = content.replaceAll("<br>", "");
                Log.d("content", content);
            }

            String data[] = new String[4];

            data[0] = novelName;
            data[1] = novelAuthor;
            data[2] = content;
            data[3] = novelDeskImgUrl;

            // Create and return a new NovelDescriptionModel object
            NovelDescriptionModel ndm = new NovelDescriptionModel(novelName, novelAuthor, content, novelDeskImgUrl);
            return ndm;

        } catch (IOException e) {

            throw new RuntimeException(e);
        }
    }

    @Override
    public List<NovelModel> novelHomePageScraping(String url) {
        List<NovelModel> novels = new ArrayList<>();

        try{

            // Fetch and parse the home page
            Document doc = Jsoup.connect(url).get();
            Element novelHolderTag = doc.selectFirst("div.index-intro");
            Elements novelListTag = novelHolderTag.select("div.item");
            for (Element novel: novelListTag) {
                if (novel.attr("itemtype").equals(this.ITEM_TYPE)){

                    // Extract novel details
                    String name = novel.selectFirst("h3").text();
                    String imgUrl = novel.selectFirst("img[src]").attr("src");
                    String novelUrl = novel.selectFirst("a[href]").attr("href");
                    String author = "";

                    // Create a new NovelModel object and add it to the list
                    NovelModel novelToAdd = new NovelModel(name, novelUrl, author, imgUrl);
                    logToCheck(novelToAdd);
                    novels.add(novelToAdd);
                }
            }
        }   catch (IOException e) {
            // Handle exceptions
            throw new RuntimeException(e);
        }
        return novels;
    }

    @Override
    public List<ChapterModel> novelChapterListScraping(String url) {

        List<ChapterModel> chapters = new ArrayList<>();

        try {
            Document doc = Jsoup.connect(url).get();
            Element chapterListNode = doc.getElementById("list-chapter");
            Elements chaptersNode = doc.select("ul.list-chapter");

            for (Element node: chaptersNode){
                Elements chapterData = node.select("a[href]");
                for (Element child_node: chapterData){

                    String chapterUrl = child_node.attr("href");
                    String title = parseTitle(child_node.attr("title"));
                    int chapterNumber = parseChapterNumber(title);
                    ChapterModel chapter = new ChapterModel(title, chapterUrl, chapterNumber);
                    Log.d("chapter: ", chapter.getChapterName());
                    chapters.add(chapter);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return chapters;
    }

    @Override
    public int chapterListNumberOfPages(String url) {
        int totalPages = 0;
        try {
            Document doc = Jsoup.connect(url).get();
            totalPages = Integer.parseInt(doc.getElementById("total-page").attr("value"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return totalPages;
    }

    @Override
    public int getNumberOfChaptersPerPage() {
        return this.CHAPTERS_PER_PAGE;
    }

    //Chapter list support methods:
    private String parseTitle(String title){
        String[] carriage = title.split(" - ");
        if (carriage.length >= 2) return carriage[1];
        return title;
    }

    private int parseChapterNumber(String name){
        int chuongPos = name.indexOf("Chương ");
        int colonPos = name.indexOf(":");

        if (chuongPos != -1 && colonPos !=-1){
            String finalString = name.substring(chuongPos + "Chương ".length(), colonPos);
            try {
                int number = Integer.parseInt(finalString);
                return number;
            }
            catch (NumberFormatException e){
                return 0;
            }
        }
        else return 0;
    }

    //Novel list support methods
    private String getNovelLinkInRowNode(Element row){
        Element linkNode = row.select("a[href]").first();
        if (linkNode != null)
        {
            return linkNode.attr("href");
        }
        return null;
    }
    private String getNovelAuthorInRowNode(Element row){
        Element authorNode = row.select("span.author").first();
        if (authorNode != null){
            return authorNode.text();
        }
        return null;
    }
    private String getNovelNameInRowNode(Element row){
        Element nameNode = row.select("h3.truyen-title").first();
        if (nameNode!=null){
            return nameNode.text();
        }
        else return null;
    }
    private String getThumbnailsUrlInRowNode(Element row){
        Element imgNode = row.select("div[data-image]").first();

        if (imgNode!=null){
            String data = imgNode.attr("data-desk-image");
            return imgNode.attr("data-desk-image");
        }
        return null;
    }
    public String getChapterTitleAndName(String url) {
        Document doc = null;
        try {
            doc = Jsoup.connect(url).get();
            String title = doc.select("a.truyen-title").first().text();
            String chapterName = doc.select("a.chapter-title").first().text();
            Log.d("TITLE", title);
            Log.d("CHAPTERNAME", chapterName);

            return chapterName;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //Novel description support methods

    // Chapter content support methods
    public String chapterContent(String url) {
        try {
            String content;
            Document doc = Jsoup.connect(url).get();

            Element chapterBody = doc.getElementById("chapter-c");
            Log.d("CHAPTER BODY", chapterBody.html());
            content = chapterBody.html();

            // Replace <br> tags with newline characters
            String formattedContent = content.replaceAll("(?i)<br[^>]*>", "\n");

            // Strip other HTML tags
            formattedContent = formattedContent.replaceAll("<[^>]+>", "");

            // Replace multiple consecutive newline characters with a single newline
            formattedContent = formattedContent.replaceAll("\n+", "\n\n");

            Log.d(" CONTENT", formattedContent);
            return formattedContent;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //Other methods
    private void logToCheck(NovelModel n){
        Log.d("Novel Model object", n.getName() + " src: " + n.getUrl() + " " + n.getAuthor() + " img: " + n.getImageDesk());
    }


}
