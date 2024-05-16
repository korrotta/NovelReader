package com.softwaredesign.novelreader.Activities;

import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.softwaredesign.novelreader.BackgroundTask;
import com.softwaredesign.novelreader.Models.NovelModel;
import com.softwaredesign.novelreader.Adapters.NovelAdapter;
import com.softwaredesign.novelreader.NovelParsers.TruyenfullScraper;
import com.softwaredesign.novelreader.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final String truyenfullUrl = "https://truyenfull.vn/";
    private SearchView searchView;
    private RecyclerView recyclerView;
    private List<NovelModel> novelList = new ArrayList<>();
    private NovelAdapter novelAdapter;
    private ProgressBar progressBar;
    private AppCompatSpinner filterSpinner;
    //Parser
    private TruyenfullScraper truyenfullScraper;
    private static int numberOfPages = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchView = findViewById(R.id.searchView);
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        //filterSpinner = findViewById(R.id.filterSpinner);

        //create parser
        truyenfullScraper = new TruyenfullScraper();

        // Handle SearchView
        handleSearchView();

        // Initialize RecyclerView
        GridLayoutManager gridLayoutManager = new GridLayoutManager(MainActivity.this, 2);
        recyclerView.setLayoutManager(gridLayoutManager);

        // Initialize novel list adapter
        novelAdapter = new NovelAdapter(MainActivity.this, novelList);
        recyclerView.setAdapter(novelAdapter);

        // Fetch novel from web
        fetchMainPageNovels();

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

    private void fetchMainPageNovels(){
        BackgroundTask task = new BackgroundTask(MainActivity.this) {
            @Override
            public void onPreExecute() {
                progressBar.setVisibility(View.VISIBLE);
                progressBar.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.fade_in));
            }

            @Override
            public void doInBackground() {
                novelList.clear();
                novelList.addAll(truyenfullScraper.novelHomePageScraping(truyenfullUrl));
            }

            @Override
            public void onPostExecute() {
                progressBar.setVisibility(View.GONE);
                progressBar.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.fade_out));
                novelAdapter.notifyDataSetChanged();
            }
        };
        task.execute();
    }


    private void searchList(String text) {
        List<NovelModel> novelSearchList = new ArrayList<>();
        for (NovelModel n : novelList) {
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