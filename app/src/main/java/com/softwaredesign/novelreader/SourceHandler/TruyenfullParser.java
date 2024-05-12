package com.softwaredesign.novelreader.SourceHandler;

import android.util.Log;

import com.softwaredesign.novelreader.ChapterListItem;
import com.softwaredesign.novelreader.Factory.NovelScrapperFactory;
import com.softwaredesign.novelreader.Models.NovelModel;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TruyenfullParser implements NovelScrapperFactory{
    public String SEARCH_DEFAULT_URL = "https://truyenfull.vn/tim-kiem/?tukhoa=";
    public String ITEM_TYPE = "https://schema.org/Book";
    @Override
    public ArrayList<NovelModel> searchPageScrapping(String keyword) {
        String searchUrl = this.SEARCH_DEFAULT_URL + keyword;
        ArrayList<NovelModel> novelList = new ArrayList<>();

        try {
            Document doc = Jsoup.connect(searchUrl)
                    .timeout(6000)
                    .get();
            Elements rowNodes = doc.select("div.row");

            for (Element row: rowNodes){
                String itemScope = row.attr("itemtype");
                if (itemScope.equals(this.ITEM_TYPE)){

                    String novelName = getNovelNameInRowNode(row);
                    String novelHref = getNovelLinkInRowNode(row);
                    String novelAuthor = getNovelAuthorInRowNode(row);
                    String novelThumbnailsUrl = getThumbnailsUrlInRowNode(row);

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
    public String[] novelDetailScrapping(String url) {
        try {
            Document doc = Jsoup.connect(url).timeout(6000).get();
            Element headerNode = doc.getElementById("truyen");
//            Log.d("headerNode", headerNode.toString());
            String novelName = headerNode.selectFirst("h3.title").text();
            String novelAuthor = headerNode.selectFirst("a[itemprop=author]").text();
            Element novelDescriptionNode = headerNode.selectFirst("div[itemprop=description]");
            String novelDeskImgUrl = headerNode.selectFirst("img[itemprop=image]").attr("src");
            String content = null;
            if (novelDescriptionNode!=null){
                content = novelDescriptionNode.toString();
                content = content.replace("<div class=\"desc-text desc-text-full\" itemprop=\"description\">", "");
                content = content.replaceAll("<b>", "");
                content = content.replaceAll("</b>", "");
                content = content.replaceAll("<i>", "");
                content = content.replaceAll("</i>","");
                content = content.replaceAll("&nbsp;", " ");
                content = content.replace("</div>", "");
                content = content.replaceAll("<br>", "");
//                Log.d("content", content);
            }

            String data[] = new String[4];

            data[0] = novelName;
            data[1] = novelAuthor;
            data[2] = content;
            data[3] = novelDeskImgUrl;

            return data;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ChapterListItem> novelChapterListScrapping(String url) {
        int totalPages = 0;
        List<ChapterListItem> chapters = new ArrayList<>();
        String chapterListUrlBefore = url+"trang-";
        String chapterListUrlAfter = "/#list-chapter";

        try {
            Document doc = Jsoup.connect(url).get();
            totalPages = Integer.parseInt(doc.getElementById("total-page").attr("value"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (totalPages != 0){
            for (int i = 1; i <= totalPages; i++) {
                try {
                    Document doc = Jsoup.connect(chapterListUrlBefore+i+chapterListUrlAfter).get();
                    Element chapterListNode = doc.getElementById("list-chapter");
                    Elements chaptersNode = doc.select("ul.list-chapter");

                    for (Element node: chaptersNode){
                        Elements chapterData = node.select("a[href]");
                        for (Element child_node: chapterData){

                            String chapterUrl = child_node.attr("href");
                            String title = parseTitle(child_node.attr("title"));
                            int chapterNumber = parseChapterNumber(title);
                            ChapterListItem chapter = new ChapterListItem(title, chapterUrl, chapterNumber);
                            chapters.add(chapter);
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return chapters;
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

    //Novel description support methods
}
