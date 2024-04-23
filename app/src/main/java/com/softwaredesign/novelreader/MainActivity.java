package com.softwaredesign.novelreader;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private SearchView searchView;
    private RecyclerView recyclerView;
    private List<Novel> novelList = new ArrayList<>();
    private NovelAdapter novelAdapter;
    private Novel novel;
    private ProgressBar progressBar;

    public final static String TRUYENFULL_VN = "https://truyenfull.vn";
    public final static String ItemType = "https://schema.org/Book";
    public final static String LinkTruyen = "https://truyenfull.vn/bat-diet-kiem-the/";

    private static HashMap<String, String> typeMap;
    private static int numberOfPages = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchView = findViewById(R.id.searchView);
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);

        searchView.clearFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchList(newText);
                return false;
            }
        });

        GridLayoutManager gridLayoutManager = new GridLayoutManager(MainActivity.this, 1);
        recyclerView.setLayoutManager(gridLayoutManager);

        // Initialize novel list adapter
        novelAdapter = new NovelAdapter(MainActivity.this, novelList);
        recyclerView.setAdapter(novelAdapter);

        // TODO: Add novel parsing from web here
        Content content = new Content();
        content.execute();

        /*typeMap = new HashMap<String, String>();
        typeMap = getCategoryList(TruyenFull);
        numberOfPages = getNumberOfPages(typeMap.get("Tiên Hiệp"));

        //first page:
        getNovelListFromPageLink(typeMap.get("Tiên Hiệp"));*/

        /*// Example for testing purposes
        novel = new Novel("A", "Author A", R.drawable.logo, "1234");
        novelList.add(novel);
        novel = new Novel("B", "Author B", R.drawable.logo, "5678");
        novelList.add(novel);
        novel = new Novel("C", "Author C", R.drawable.logo, "91011");
        novelList.add(novel);
        novel = new Novel("D", "Author D", R.drawable.logo, "121314");
        novelList.add(novel);
        novel = new Novel("E", "Author E", R.drawable.logo, "151617");
        novelList.add(novel);*/
    }

    /*public static HashMap<String, String> getCategoryList(String link) {
        HashMap<String, String> typeMap = new HashMap<String, String>();
        try {
            Document doc = Jsoup.connect(link).get();
            Elements types = doc.select("div.col-xs-6");
            for (Element type : types) {
                Element linkNode = type.selectFirst("a[href]");
                assert linkNode != null;
                String ref = linkNode.attr("href");
                typeMap.put(type.text(), ref);
            }
            return typeMap;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void printList(HashMap<String, String> l) {
        for (Map.Entry<String, String> entry : l.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }

    public static int getNumberOfPages(String link) {
        try {
            Document pageDoc = Jsoup.connect(link).get();
            Element pagination = pageDoc.select("ul.pagination.pagination-sm").first();
            if (pagination == null) return -1;
            Elements pageLinkNodes = pagination.select("a[href]");
            for (Element item : pageLinkNodes) {
                if (item.text().equals("Cuối »")) {
                    String itemTitle = (item.attr("title"));
                    String[] carriage = itemTitle.split(" ");
                    String finalPage = carriage[carriage.length - 1];
                    return Integer.parseInt(finalPage);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }

    public static void getNovelListFromPageLink(String pageLink) {
        try {
            Document doc = Jsoup.connect(pageLink).get();
            Elements rowNodes = doc.select("div.row");
            for (Element row : rowNodes) {
                String itemScope = row.attr("itemtype");
                if (itemScope.equals(ItemType)) {

                    String novelName = getNovelNameInRowNode(row);
                    String novelHref = getNovelLinkInRowNode(row);
                    String novelAuthor = getNovelAuthorInRowNode(row);
                    String novelThumbnailsUrl = getThumbnailsUrlInRowNode(row);

                    Novel novel = new Novel(novelName, novelAuthor, novelThumbnailsUrl, novelHref);

                    novel.printNovelData();
                    System.out.println();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getNovelNameInRowNode(Element row) {
        Element nameNode = row.select("h3.truyen-title").first();
        if (nameNode != null) {
            return nameNode.text();
        } else return null;
    }

    private static String getNovelLinkInRowNode(Element row) {
        Element linkNode = row.select("a[href]").first();
        if (linkNode != null) {
            return linkNode.attr("href");
        }
        return null;
    }

    private static String getNovelAuthorInRowNode(Element row) {
        Element authorNode = row.select("span.author").first();
        if (authorNode != null) {
            return authorNode.text();
        }
        return null;
    }

    public static String getThumbnailsUrlInRowNode(Element row) {
        Element imgNode = row.select("div[data-image]").first();

        if (imgNode != null) {
            String data = imgNode.attr("data-desk-image");
            return imgNode.attr("data-desk-image");
        }
        return null;
    }*/

    private class Content extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            progressBar.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.fade_in));
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            progressBar.setVisibility(View.GONE);
            progressBar.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.fade_out));
            novelAdapter.notifyDataSetChanged();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Document document = Jsoup.connect(TRUYENFULL_VN)
                        .timeout(6000)
                        .get();
                Elements novelElements = document.getElementsByClass("col-xs-4 col-sm-3 col-md-2");
                Log.d("NOVELELEMENTS", String.valueOf(novelElements.size()));

                int size = novelElements.size();
                for (int i = 0; i < size; i++) {
                    String name = novelElements.select("a")
                            .eq(i)
                            .attr("title");

                    String imageUrl = novelElements.select("div[data-image]")
                            .eq(i)
                            .attr("data-desk-image");

                    String novelUrl = novelElements.select("a")
                            .eq(i)
                            .attr("href");

                    String author = "";
                    Log.d("NAME", name);
                    Log.d("IMAGEURL", imageUrl);
                    Log.d("NOVELURL", novelUrl);
                    novel = new Novel(name, "ABC", imageUrl, novelUrl);
                    novelList.add(novel);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return null;
        }
    }

    private void searchList(String text) {
        List<Novel> novelSearchList = new ArrayList<>();
        for (Novel n : novelList) {
            if (n.getName().toLowerCase().contains(text.toLowerCase())) {
                novelSearchList.add(n);
            }
        }

        if (novelSearchList.isEmpty()) {
            Toast.makeText(this, "Not Found", Toast.LENGTH_SHORT).show();
        } else {
            novelAdapter.setSearchList(novelSearchList);
        }
    }
}