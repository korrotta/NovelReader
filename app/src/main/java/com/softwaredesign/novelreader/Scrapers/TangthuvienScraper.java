package com.softwaredesign.novelreader.Scrapers;

import android.util.Log;

import com.example.scraper_library.INovelScraper;
import com.softwaredesign.novelreader.Models.ChapterContentModel;
import com.softwaredesign.novelreader.Models.ChapterModel;
import com.softwaredesign.novelreader.Models.NovelDescriptionModel;
import com.softwaredesign.novelreader.Models.NovelModel;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.SocketTimeoutException;
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
            // Construct the search URL with the keyword and page number
            final String SearchUrl = SEARCH_DEFAULT_URL + keyword + "&page="+page;
            // Connect to the URL and fetch the document using Jsoup
            Document doc = Jsoup.connect(SearchUrl).get();

            // Select the container element containing the list of novels
            Element novelListElement = doc.select("div.rank-view-list").first();
            // Select all individual novel elements within the container
            Elements novelElements = novelListElement.select("li");

            for (Element novelElement: novelElements){
                // Select specific elements within each novel element
                Element imgBox = novelElement.selectFirst("div.book-img-box");
                Element infoBox = novelElement.selectFirst("div.book-mid-info");

                // Extract relevant data for each novel
                String name = infoBox.selectFirst("h4").text();
                String author = infoBox.select("a.name").text();
                String url = imgBox.select("a[href]").attr("href");
                String imgUrl = imgBox.select("img.lazy").attr("src");

                // Create a NovelModel object and add it to the list
                NovelModel novel = new NovelModel(name, url, author, imgUrl);
                novels.add(novel);
            }
            // Return the list of novels retrieved from the page
            return novels;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getNumberOfSearchResultPage(String keyword) {
        try {
            // Connect to the search URL with the keyword
            Document doc = Jsoup.connect(SEARCH_DEFAULT_URL + keyword).get();

            // Check if pagination element exists
            Element paginationElement = doc.select("ul.pagination").first();
            if (paginationElement != null) {
                int max = 0;
                // Select all page number elements within pagination
                Elements allPagesElement = paginationElement.select("li");
                for (Element pageElement : allPagesElement) {
                    Log.d("li check", pageElement.toString());
                    // Iterate through each page number element
                    String pageNumberAsString = pageElement.text();
                    try {
                        // Parse page number as integer
                        int pageNumber = Integer.parseInt(pageNumberAsString);
                        // Track the maximum page number encountered
                        if (pageNumber > max) max = pageNumber;
                    } catch (NumberFormatException e) {
                        // Ignore and continue if the text is not a valid number
                        continue;
                    }
                }
                // Return the maximum page number found
                return max;
            }

            // If pagination element is not found, check for no result message
            Element bookImgTextElement = doc.select("div.book-img-text").first();
            String text = bookImgTextElement.text();
            if (text.contains("Không tìm thấy")) return 0; // Return 0 if no results found
            return 1; // Return 1 if there is only one page of results
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public NovelDescriptionModel getNovelDetail(String url) {
        try {
            // Connect to the provided URL and retrieve the HTML document
            Document doc = Jsoup.connect(url).get();

            // Extract the novel name
            String name = doc.select("h1").first().text();
            // Extract the author information
            String author = doc.getElementById("authorId").selectFirst("p").text();
            // Initialize description with an empty string
            String description = "";
            // Extract the novel description if available
            Element descriptionElement = doc.select("div.book-intro").first();
            if (descriptionElement != null){
                // Get the first paragraph inside the description element
                description = descriptionElement.select("p").first().toString();
            }
            // Extract the URL for the novel's thumbnail image
            String thumbnailUrl = doc.getElementById("bookImg").select("img").first().attr("src");

            // Create a NovelDescriptionModel object with the extracted information
            NovelDescriptionModel ndm = new NovelDescriptionModel(name, author, description, thumbnailUrl);
            return ndm; // Return the NovelDescriptionModel object
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getChapterListNumberOfPages(String url) {
        try {
            // Connect to the given URL and retrieve the HTML document
            Document doc = Jsoup.connect(url).get();
            // Select the first <ul> element with the class 'pagination' and then get all <li> elements within it
            Elements paginationElements = doc.select("ul.pagination").first().select("li");
            if (paginationElements != null){
                int maxPage = 0;

                for (Element page: paginationElements) {
                    // Select the first <a> element within the current <li> element
                    Element pageIdElement = page.select("a").first();
                    // If no <a> element is found, skip to the next iteration
                    if (pageIdElement == null) continue;
                    // Get the text content of the <a> element
                    String pageIdString = pageIdElement.text();
                    if (page.text().contains("cuối")) {
                        // Extract the page number from the 'onclick' attribute and return it, incremented by 1
                        return parseOnClickGetNumber(pageIdElement.attr("onclick"))+1;
                    }
                    try {
                        // Attempt to parse the text content as an integer (page number)
                        int pageId = Integer.parseInt(pageIdString);
                        // Update the maximum page number if the current one is greater
                        if (pageId>maxPage) maxPage = pageId;
                    } catch (NumberFormatException e){
                        // If parsing fails (text is not a number), continue to the next iteration
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

        // Retrieve the novel ID from the novel URL
        String novelId = getStoryId(novelUrl);

        // Construct the URL for the chapter page using the novel ID and page number
        final String CHAPTER_PAGE_URL = "https://truyen.tangthuvien.vn/doc-truyen/page/ " +novelId+ "?page=" +(pageNumber-1)+ "&limit=75&web=1";
        try {
            // Connect to the chapter page URL and retrieve the HTML document
            Document doc = Jsoup.connect(CHAPTER_PAGE_URL).get();
            // Select the first <ul> element with class 'cf' and then select all <a> elements with href attributes within it
            Elements chapterElements = doc.selectFirst("ul.cf").select("a[href]");
            for (Element chapter: chapterElements){
                // Get the raw name of the chapter from the title attribute of the <a> element
                String rawName = chapter.attr("title");
                // Replace HTML non-breaking space entities with actual spaces in the chapter name
                String name = rawName.replaceAll("&nbsp;", " ");
                // Get the URL of the chapter from the href attribute of the <a> element
                String url = chapter.attr("href");
                int number = 0;
                try {
                    // Attempt to extract the chapter number from the chapter name
                    number = Integer.parseInt(name.split(" ")[1]);
                }catch (NumberFormatException e){
                    // If parsing fails, set the chapter number to 0
                    number = 0;
                }

                // Create a new ChapterModel object with the chapter name, URL, and number
                ChapterModel c = new ChapterModel(name, url, number);
                // Add the ChapterModel object to the list of chapters
                chapters.add(c);
            }
            return chapters; // Return the list of chapters
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<NovelModel> getHomePage() {
        List<NovelModel> novels = new ArrayList<>();
        try {
            // Connect to the home page URL and retrieve the HTML document
            Document doc = Jsoup.connect(HOME_PAGE_URL).get();
            // Select the last <div> element with the class 'center-book-list fl'
            Element novelListElement = doc.select("div.center-book-list.fl").last();
            // Ensure the selected element is not null
            assert novelListElement != null;
            // Select all <li> elements within the novel list element
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
        } catch (SocketTimeoutException e){
            Log.d("Socket time out", "Socket Time out");
            return new ArrayList<>();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ChapterContentModel getChapterContent(String url) {
        String content ="";
        try {
            // Connect to the given URL and retrieve the HTML document
            Document doc = Jsoup.connect(url).get();
            // Select the first <div> element with the class 'box-chap'
            Element boxchaps = doc.select("div.box-chap").first();
            // Extract the whole text content of the 'box-chap' element
            content = boxchaps.wholeText();
            String name = doc.select("h1.truyen-title").text();
            String chapterName = doc.selectFirst("h2").text().replaceAll("&nbsp;", " ");

            // Replace newline characters in the content with HTML line break tags
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
            // Extract the URL of the novel from the first <a> element within the <h1> element with class 'truyen-title'
            String novelUrl = doc.selectFirst("h1.truyen-title").selectFirst("a[href]").attr("href");

            Log.d("Novel URL", novelUrl);
            // Retrieve the novel ID using a helper method
            novelId = getStoryId(novelUrl);
            Log.d("Novel id latest", novelId);

            // Select the first <div> element with class 'box-chap'
            Element boxChap = doc.selectFirst("div.box-chap");
            // Get the class name of the 'box-chap' element and split it to extract the chapter ID
            String boxChapName = boxChap.className();
            String[] splitCarriage = boxChapName.split("-");
            chapterId = splitCarriage[splitCarriage.length - 1];

            //Doc2 fetching
            final String ALL_CHAPTERS_URL = "https://truyen.tangthuvien.vn/story/chapters?story_id=" + novelId;
            Document doc2 = Jsoup.connect(ALL_CHAPTERS_URL).get();
            int total = Integer.parseInt(doc2.select("li").last().attr("title"));

            // Select the current chapter element using the 'ng-chap' attribute
            Element current = doc2.selectFirst("li[ng-chap="+chapterId+"]");
            if (current!=null){
                // Get the current chapter ID from the 'title' attribute
                int currentId = Integer.parseInt(current.attr("title"));
                // Check if the current chapter is the last one
                if (currentId >= total){
                    return null; //NO next chap
                }
                // Calculate the next chapter ID
                int nextId = currentId +1;
                // Select the next chapter element using the 'title' attribute
                Element next = doc2.selectFirst("li[title="+nextId+"]");
                // Return the URL of the next chapter
                return next.selectFirst("a[href]").attr("href");
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Log.d("novelId latest", novelId);
        return null;
    }


    @Override
    public String getPreviousChapterUrl(String url) {
        String novelId = "";
        String chapterId = "";
        try {
            // Fetch the HTML document from the given URL
            Document doc = Jsoup.connect(url).get();
            // Extract the URL of the novel from the first <a> element within the <h1> element with class 'truyen-title'
            String novelUrl = doc.selectFirst("h1.truyen-title").selectFirst("a[href]").attr("href");

            // Retrieve the novel ID using a helper method
            novelId = getStoryId(novelUrl);
            Log.d("Novel id latest", novelId);

            // Select the first <div> element with class 'box-chap'
            Element boxChap = doc.selectFirst("div.box-chap");
            // Get the class name of the 'box-chap' element and split it to extract the chapter ID
            String boxChapName = boxChap.className();
            String[] splitCarriage = boxChapName.split("-");
            chapterId = splitCarriage[splitCarriage.length - 1];

            //Doc2 fetching
            final String ALL_CHAPTERS_URL = "https://truyen.tangthuvien.vn/story/chapters?story_id=" + novelId;
            Document doc2 = Jsoup.connect(ALL_CHAPTERS_URL).get();

            // Select the current chapter element using the 'ng-chap' attribute
            Element current = doc2.selectFirst("li[ng-chap="+chapterId+"]");
            if (current!=null){
                // Get the current chapter ID from the 'title' attribute
                int currentId = Integer.parseInt(current.attr("title"));
                // Check if the current chapter is the first one
                if (currentId <= 1){
                    return null; // no prev chap
                }
                // Calculate the previous chapter ID
                int prevId = currentId -1;
                // Select the previous chapter element using the 'title' attribute
                Element prev = doc2.selectFirst("li[title="+prevId+"]");
                // Return the URL of the previous chapter
                return prev.selectFirst("a[href]").attr("href");
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public ChapterContentModel getContentFromNameAndChapName(String name, String chapterName){
        //NOTE 1: First step to search name on source
        boolean isBreak = false;
        // Get the number of pages of search results for the given name
        int numberOfPage = getNumberOfSearchResultPage(name); //name as a keyword;
        NovelModel wantedNovel =null;
        List<NovelModel> results = new ArrayList<>();
        // Retrieve all search results across all pages
        for (int i = 1; i<= numberOfPage; i++){
            results.addAll(getSearchPageFromKeywordAndPageNumber(name, i));
        }
        Log.d("length", String.valueOf(results.size()));
        for (NovelModel novel: results){
            Log.d("novel name vs search name: ", novel.getName() + " - " + name);
            if (novel.getName().equalsIgnoreCase(name)) {
                wantedNovel = novel;
                isBreak = true;
                break; //get first one only, who care?
            }
        }
        // If the novel is not found, return null
        if (!isBreak) return null;
        Log.d("Wanted novel ", wantedNovel.getUrl()); //NOTE: ok

        //NOTE 2: Search for the wanted chapter
        ChapterModel resultChapter = smartChapterSearch(wantedNovel.getUrl(), chapterName);
        // If the chapter is not found, return null
        if (resultChapter == null) return null;
        // Retrieve and return the content of the found chapter
        return getChapterContent(resultChapter.getChapterUrl());
    }

    @Override
    public INovelScraper clone() {
        try {
            return (INovelScraper) super.clone();
        } catch (CloneNotSupportedException e) {
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

    //Loading(number)
    //NOTE: BORDER==================================================================================
    //NOTE: BORDER==================================================================================
    private int parseOnClickGetNumber(String loadingCall){
        // Split the string by "(" and take the part after it
        String rightStr = loadingCall.split("\\(")[1];
        // Split the resulting string by ")" and take the part before it
        String finalStr = rightStr.split("\\)")[0];
        // Convert the extracted string to an integer and return it
        return Integer.parseInt(finalStr);
    }

    // This method retrieves the story ID from the novel URL
    private String getStoryId(String novelUrl){
        try {
            // Connect to the given novel URL and retrieve the HTML document
            Document doc = Jsoup.connect(novelUrl).get();
            // Select the first <meta> element with the name attribute 'book_detail'
            Element idElement = doc.select("meta[name=book_detail]").first();
            // If the element is found, extract and return the value of the 'content' attribute
            if (idElement !=null){
               String novelId = idElement.attr("content");
               return novelId;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // Return an empty string if the story ID is not found
        return "";
    }


    private ChapterModel smartChapterSearch(String novelUrl, String chapterName){
        //need to parse chapterName to chapterNumber first.
        int id = parseIdFromChapterName(chapterName);
        Log.d("id can search", String.valueOf(id));
        //then get maximum pages of the novel chapter list
        int totalPages = getChapterListNumberOfPages(novelUrl);
        //after all, use lambda to get the different

        if (id == -1) {
            //Final chance: Search by name
            return null;
        }
        else {
            int possiblePage = id/CHAPTERS_PER_PAGE + 1;
            int runPage = possiblePage;
            if (possiblePage > totalPages) return null;
            while (true) {
                List<ChapterModel> results = getChapterListInPage(novelUrl, runPage);
                //Binary search
                ChapterModel result = searchChapterById(results, id);

                if (result != null) {
                    Log.d("Result", result.getChapterUrl());
                    return result;

                }
                if (result == null) {
                    runPage ++;
                    if (runPage - possiblePage == 3) runPage -=5;
                    if (runPage == possiblePage) return null;
                }
            }
        }
    }
    private int parseIdFromChapterName(String chapterName){
        chapterName = chapterName.replaceAll(":", "");
        String[] holder = chapterName.split("\\s+");
        String possibleId;

//        for (int i = 0; i< holder.length; i++) {
//            Log.d("hlsder", holder[i]);
//        }
        int id;
        if (chapterName.toUpperCase().contains("CHƯƠNG")){
            possibleId = holder[1];
        }
        else {
            possibleId = holder[0];
        }
        try {
            id = Integer.parseInt(possibleId);
            return id;
        }catch (NumberFormatException e){
            return -1;
        }
    }

    private ChapterModel searchChapterById(List<ChapterModel> list, int id){
        for (ChapterModel chapterModel: list){
            int chapterId = parseIdFromChapterName(chapterModel.getChapterName());

            if (chapterId == id) return chapterModel;
        }
//        int low = 0;
//        int high = list.size()-1;
//        while (low <= high){
//            int mid = (low+high) >>> 1;
//            ChapterModel model = list.get(mid);
//
//            int modelId = parseIdFromChapterName(model.getChapterName());
//
//            if (modelId < id){
//                low = mid+1;
//            }
//            else if (modelId > id) {
//                high = mid-1;
//            }
//            else return model;
//        }
        return null;
    }
}
