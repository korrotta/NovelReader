package com.softwaredesign.novelreader.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

import com.example.novelscraperfactory.INovelScraper;
import com.softwaredesign.novelreader.Adapters.ServerSpinnerAdapter;
import com.softwaredesign.novelreader.BackgroundTask;
import com.softwaredesign.novelreader.Global.GlobalConfig;
import com.softwaredesign.novelreader.Global.ReusableFunction;
import com.softwaredesign.novelreader.Models.NovelModel;
import com.softwaredesign.novelreader.Adapters.NovelAdapter;
import com.softwaredesign.novelreader.Models.NovelSourceModel;
import com.softwaredesign.novelreader.Scrapers.TangthuvienScraper;
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
    private SearchView searchView; // SearchView for searching novels
    private RecyclerView recyclerView; // RecyclerView for displaying novels
    private List<NovelModel> novelList = new ArrayList<>(); // List to hold novel data
    private NovelAdapter novelAdapter; // Adapter for the RecyclerView
    private ProgressBar progressBar; // ProgressBar to indicate loading
    private AppCompatSpinner serverSpinner; // Spinner for server sources

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Set the layout for this activity

        // Initialize views
        searchView = findViewById(R.id.searchView);
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        serverSpinner = findViewById(R.id.serverSpinner);

        //Init server adapter
        ServerSpinnerAdapter serverAdapter = new ServerSpinnerAdapter(this, android.R.layout.simple_spinner_item, GlobalConfig.Global_Source_List);
        serverSpinner.setAdapter(serverAdapter);

        // Add source truyenfull
        INovelScraper truyenfull = new TruyenfullScraper();
        // Add source tangthuvien
        INovelScraper tangthuvien = new TangthuvienScraper();


        //TODO: Plugin architecture for hot plugging new source - truyenchu.vn
        GlobalConfig.Global_Source_List.add(truyenfull);
        GlobalConfig.Global_Source_List.add(tangthuvien);
        //TODO: Implement Tangthuvien scraper
        //GlobalConfig.Global_Source_List.add(tangthuvien);

        serverAdapter.notifyDataSetChanged();
        //TODO: Set spinner

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
        getMainPageTask();

        // Handle Server Spinner
        serverSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                INovelScraper scraperInstance = (INovelScraper) parent.getItemAtPosition(position);
                GlobalConfig.Global_Current_Scraper = scraperInstance;
                getMainPageTask();
                Log.d("Source check: ", GlobalConfig.Global_Current_Scraper.getSourceName());

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

    }

    private void handleSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.setQuery("", false);
                ReusableFunction.ChangeActivityWithString(MainActivity.this, SearchActivity.class, "searchQuery", query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // No action on query text change
                return false;
            }
        });
    }

    //task with Pre-execute - need to renew instance every call
    private void getMainPageTask(){
        new BackgroundTask(MainActivity.this) {
            @Override
            public void onPreExecute() {
                // Show and animate the progress bar before starting the task
                progressBar.setVisibility(View.VISIBLE);
                progressBar.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.fade_in));
            }

            @Override
            public void doInBackground() {
                // Clear the current list and fetch new data
                ReusableFunction.ReplaceList(novelList, GlobalConfig.Global_Current_Scraper.getHomePage());
            }

            @Override
            public void onPostExecute() {
                // Hide and animate the progress bar after the task is completed
                progressBar.setVisibility(View.GONE);
                progressBar.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.fade_out));

                novelAdapter.notifyDataSetChanged();
            }
        }.execute();
    }
}