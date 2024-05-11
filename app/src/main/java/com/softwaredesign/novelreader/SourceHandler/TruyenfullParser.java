package com.softwaredesign.novelreader.SourceHandler;

import android.util.Log;

import com.softwaredesign.novelreader.Factory.NovelScrapperFactory;
import com.softwaredesign.novelreader.Models.NovelModel;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

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
                content = content.replaceAll("&nbsp;", "");
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
    public void novelChapterListScrapping(String url) {

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
