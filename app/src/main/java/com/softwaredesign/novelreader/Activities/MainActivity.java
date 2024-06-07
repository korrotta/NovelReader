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

    // URL of the website to scrape novels from
    private final String truyenfullUrl = "https://truyenfull.vn/";
    private SearchView searchView; // SearchView for searching novels
    private RecyclerView recyclerView; // RecyclerView for displaying novels
    private List<NovelModel> novelList = new ArrayList<>(); // List to hold novel data
    private NovelAdapter novelAdapter; // Adapter for the RecyclerView
    private ProgressBar progressBar; // ProgressBar to indicate loading
    private AppCompatSpinner filterSpinner; // Spinner for filtering novels
    //Parser
    private TruyenfullScraper truyenfullScraper; // Scraper object to fetch novels
    private static int NumberOfPages = 0; // Variable to hold number of pages

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Set the layout for this activity

        // Initialize views
        searchView = findViewById(R.id.searchView);
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);

        //filterSpinner = findViewById(R.id.filterSpinner);

        //create parser
        // Initialize the scraper
        truyenfullScraper = new TruyenfullScraper();

        // Handle SearchView
        // Set up SearchView behavior
        handleSearchView();

        // Initialize RecyclerView
        // Set up RecyclerView with a grid layout
        GridLayoutManager gridLayoutManager = new GridLayoutManager(MainActivity.this, 2);
        recyclerView.setLayoutManager(gridLayoutManager);

        // Initialize novel list adapter
        // and set it to the RecyclerView
        novelAdapter = new NovelAdapter(MainActivity.this, novelList);
        recyclerView.setAdapter(novelAdapter);

        // Fetch novel from web
        // Fetch the novels to display on the main page
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
        // Clear focus to avoid automatic keyboard pop-up
        searchView.clearFocus();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                // No action on query submission
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                // Filter the list based on search text
                searchList(newText);
                return false;
            }
        });
    }

    public HashMap<String, String> getCategoryList(String link) {

        HashMap<String, String> typeMap = new HashMap<String, String>();
        try {

            // Fetch the document from the link
            Document doc = Jsoup.connect(link).get();

            // Select elements that contain categories
            Elements types = doc.select("div.col-xs-6");
            for (Element type : types) {
                // Get the link node and its reference
                Element linkNode = type.selectFirst("a[href]");
                assert linkNode != null;
                String ref = linkNode.attr("href");
                // Put the type and reference into the map
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

                // Show and animate the progress bar before starting the task
                progressBar.setVisibility(View.VISIBLE);
                progressBar.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.fade_in));
            }

            @Override
            public void doInBackground() {

                // Clear the current list and fetch new data
                novelList.clear();
                novelList.addAll(truyenfullScraper.getHomePage(truyenfullUrl));
            }

            @Override
            public void onPostExecute() {

                // Hide and animate the progress bar after the task is completed
                progressBar.setVisibility(View.GONE);
                progressBar.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.fade_out));
                novelAdapter.notifyDataSetChanged();
            }
        };
        // Execute the background task
        task.execute();
    }


    private void searchList(String text) {

        List<NovelModel> novelSearchList = new ArrayList<>();
        for (NovelModel n : novelList) {
            // Check if the novel name contains the search text
            if (n.getName().toLowerCase().contains(text.toLowerCase())) {
                novelSearchList.add(n);
            }
        }

        if (novelSearchList.isEmpty()) {

            // If no results found, show a toast message (commented out)
            // Toast.makeText(this, "No results found", Toast.LENGTH_SHORT).show();

        } else {
            // Update the adapter with the search results
            novelAdapter.setSearchList(novelSearchList);
        }
    }
}