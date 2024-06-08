package com.softwaredesign.novelreader.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import com.softwaredesign.novelreader.Scrapers.TruyenfullScraper;
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
    private AppCompatSpinner serverSpinner; // Spinner for server sources
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
        serverSpinner = findViewById(R.id.serverSpinner);

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

        // Handle Server Spinner
        serverSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();
                Toast.makeText(MainActivity.this, "You pick " + item, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ArrayList<String> serverList = new ArrayList<>();
        serverList.add("Server 1");
        serverList.add("Server 2");

        ArrayAdapter<String> serverAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, serverList);
        serverAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        serverSpinner.setAdapter(serverAdapter);

    }


    private void handleSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                intent.putExtra("searchQuery", query);
                startActivity(intent);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // No action on query text change
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
}