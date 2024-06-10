package com.softwaredesign.novelreader.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.scraper_library.INovelScraper;

import com.softwaredesign.novelreader.Adapters.ServerSpinnerAdapter;
import com.softwaredesign.novelreader.BackgroundTask;
import com.softwaredesign.novelreader.Global.GlobalConfig;
import com.softwaredesign.novelreader.Global.ReusableFunction;
import com.softwaredesign.novelreader.Models.NovelModel;
import com.softwaredesign.novelreader.Adapters.NovelAdapter;
import com.softwaredesign.novelreader.Models.NovelSourceModel;
import com.softwaredesign.novelreader.Scrapers.TangthuvienScraper;
import com.softwaredesign.novelreader.Scrapers.TruyencvScraper;
import com.softwaredesign.novelreader.Scrapers.TruyenfullScraper;
import com.softwaredesign.novelreader.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import dalvik.system.DexClassLoader;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_READ_STORAGE = 100;

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

        File downloadDir =MainActivity.this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        String downloadDirPath = downloadDir.getAbsolutePath();
        //make directory
        makeDirectory(downloadDirPath);

        // Add source truyenfull
        INovelScraper truyenfull = new TruyenfullScraper();
        // Add source tangthuvien
        INovelScraper tangthuvien = new TangthuvienScraper();
        INovelScraper truyencv = new TruyencvScraper();

        loadScraperPlugin(downloadDirPath);

        GlobalConfig.Global_Source_List.add(truyenfull);
        GlobalConfig.Global_Source_List.add(tangthuvien);
        GlobalConfig.Global_Source_List.add(truyencv);
        //TODO: Plugin architecture for hot plugging new source - truyenchu.vn

        serverAdapter.notifyDataSetChanged();

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

    private void makeDirectory(String downloadDirPath){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_READ_STORAGE);
            }
        }
        try {
            final String libPath = downloadDirPath + "/myPlugin1.apk";
            final File tmpDir = getDir("dex", 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadAllPlugins(File downloadDir){
        if (!downloadDir.isDirectory()) return;
        File[] files = downloadDir.listFiles();
        if (files == null) return;
        for (File file: files){
            if (file.isDirectory()) return;
            //note: file is file now.
            //check if it's an apk file
            if (file.getName().toLowerCase().endsWith(".apk"));

        }
    }
    private void loadScraperPlugin(String pluginPath){
        try {
            final String libPath = pluginPath + "/scraperplugin_truyencv.apk";
            final File tmpDir = getDir("dex", 0);

            final DexClassLoader classloader = new DexClassLoader(libPath, tmpDir.getAbsolutePath(), null, this.getClass().getClassLoader());

            Class<?> classToLoad = classloader.loadClass("com.example.truyencvtest.TruyencvScraper");
            INovelScraper addedPlugin = (INovelScraper) classToLoad.newInstance();
            Log.d("Added plugin: ", addedPlugin.getSourceName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}