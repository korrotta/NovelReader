package com.softwaredesign.novelreader;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private SearchView searchView;
    private RecyclerView recyclerView;
    private List<Novel> novelList = new ArrayList<>();
    private NovelAdapter novelAdapter;
    private Novel novel;
    private ProgressBar progressBar;
    private AppCompatSpinner filterSpinner;

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
        //filterSpinner = findViewById(R.id.filterSpinner);

        // Handle SearchView
        handleSearchView();

        // Initialize RecyclerView
        GridLayoutManager gridLayoutManager = new GridLayoutManager(MainActivity.this, 2);
        recyclerView.setLayoutManager(gridLayoutManager);

        // Initialize novel list adapter
        novelAdapter = new NovelAdapter(MainActivity.this, novelList);
        recyclerView.setAdapter(novelAdapter);

        // Fetch novel from web
        Content content = new Content();
        content.execute();

        // Handle filter Spinner
       /* filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedFilter = parent.getItemAtPosition(position).toString();
                // Filter novel list
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });*/

        // TODO: Initialize filter Spinner
        // Get category list from web
        //typeMap = new HashMap<String, String>();
        //typeMap = getCategoryList(TRUYENFULL_VN);

        // Get number of pages on a category
        //numberOfPages = getNumberOfPages(typeMap.get("Tiên Hiệp"));

        //first page of the selected category
        //getNovelListFromPageLink(typeMap.get("Tiên Hiệp"));

    }

    private void handleSearchView() {
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
    }

    public HashMap<String, String> getCategoryList(String link) {
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

    public int getNumberOfPages(String link) {
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

    public void getNovelListFromPageLink(String pageLink) {
        try {
            Document document = Jsoup.connect(pageLink)
                    .timeout(6000)
                    .get();

            Elements novelElements = document.getElementsByClass("col-xs-4 col-sm-3 col-md-2");

            for (Element row : novelElements) {

                String name = getNovelNameInRowNode(row);
                String imageUrl = getThumbnailsUrlInRowNode(row);
                String novelUrl = getNovelLinkInRowNode(row);
                String author = getNovelAuthorInRowNode(row);

                novel = new Novel(name, author, imageUrl, novelUrl);
                novelList.add(novel);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getNovelNameInRowNode(Element row) {
        return row.select("a").attr("title");
    }

    private String getNovelLinkInRowNode(Element row) {
        return row.select("a").attr("href");
    }

    private String getNovelAuthorInRowNode(Element row) {
        return row.select("span.author").text();
    }

    private String getThumbnailsUrlInRowNode(Element row) {
        return row.select("div[data-image]").attr("data-desk-image");
    }

    // Handle fetch novel form web
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
            getNovelListFromPageLink(TRUYENFULL_VN);
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