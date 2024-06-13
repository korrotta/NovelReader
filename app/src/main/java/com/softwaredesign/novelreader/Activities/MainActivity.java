package com.softwaredesign.novelreader.Activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
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
import com.softwaredesign.novelreader.Scrapers.TangthuvienScraper;
import com.softwaredesign.novelreader.Scrapers.TruyencvScraper;
import com.softwaredesign.novelreader.Scrapers.TruyenfullScraper;
import com.softwaredesign.novelreader.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import dalvik.system.DexClassLoader;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_READ_STORAGE = 100;
    private static final int PERMISSION_REQUEST_CODE = 1;

    // URL of the website to scrape novels from
    private SearchView searchView; // SearchView for searching novels
    private RecyclerView recyclerView; // RecyclerView for displaying novels
    private List<NovelModel> novelList = new ArrayList<>(); // List to hold novel data
    private NovelAdapter novelAdapter; // Adapter for the RecyclerView
    private ProgressBar progressBar; // ProgressBar to indicate loading
    private AppCompatSpinner serverSpinner; // Spinner for server sources
    private AppCompatButton downloadPluginButton; // Button for download pluins

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Set the layout for this activity

        // Initialize views
        searchView = findViewById(R.id.searchView);
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        serverSpinner = findViewById(R.id.serverSpinner);
        downloadPluginButton = findViewById(R.id.downloadPluginButton);

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

        GlobalConfig.Global_Source_List.add(truyenfull);
        GlobalConfig.Global_Source_List.add(tangthuvien);
//        GlobalConfig.Global_Source_List.add(truyencv);
        //TODO: Plugin architecture for hot plugging new source - truyenchu.vn

        loadAllPlugins(downloadDir);

        serverAdapter.notifyDataSetChanged();
        handleSearchView();

        GridLayoutManager gridLayoutManager = new GridLayoutManager(MainActivity.this, 2);
        gridViewInit(gridLayoutManager);

        //Note:Resource getter here
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

    private void gridViewInit(GridLayoutManager gridLayoutManager) {
        recyclerView.setLayoutManager(gridLayoutManager);
        novelAdapter = new NovelAdapter(MainActivity.this, novelList);
        recyclerView.setAdapter(novelAdapter);
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
                List<Object> list = GlobalConfig.Global_Current_Scraper.getHomePage();

                List<NovelModel> novels = identifyingList(list);
                ReusableFunction.ReplaceList(novelList, novels);
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

    //Note: on developing
    private void loadAllPlugins(File downloadDir){
        if (!downloadDir.isDirectory()) return;
        File[] files = downloadDir.listFiles();
        if (files == null) return;
        for (File file: files){
            if (file.isDirectory()) return;
            //note: file is file now.
            //check if it's an apk file
            if (!file.getName().toLowerCase().endsWith(".apk")) return;

            String fileName = file.getName().split("\\.")[0]; //Name Without Extension
            String[] nameHolder = fileName.split("_");

            switch (nameHolder[0]) {
                case "scraper": {
                    String scraperPath = file.getAbsolutePath();
                    Log.d("ScraperPathCheck", scraperPath);
                    loadScraperPlugin(scraperPath, nameHolder[1], nameHolder[2]);
                    break;
                }
                case "exporting": {
                    ;
                }
                default:
                    break;
            }

        }
    }
    private void loadScraperPlugin(String pluginPath, String classPackage, String className){
        try {
            final File tmpDir = getDir("dex", 0);
            final DexClassLoader classloader = new DexClassLoader(pluginPath, tmpDir.getAbsolutePath(), null, this.getClass().getClassLoader());
            Class<?> classToLoad = classloader.loadClass("com.example."+classPackage+"."+className);
            INovelScraper addedScraperPlugin = (INovelScraper) classToLoad.newInstance();
            for (INovelScraper scraper: GlobalConfig.Global_Source_List){
                if (scraper.getSourceName().equals(addedScraperPlugin.getSourceName())){
                    Log.d("Add plugin status", "Failed, source exists");
                    return;
                }
            }
            GlobalConfig.Global_Source_List.add(addedScraperPlugin);
            Log.d("Added plugin: ", addedScraperPlugin.getSourceName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<NovelModel> identifyingList(List<Object> list){
        List<NovelModel> novels = new ArrayList<>();
        for (Object item: list){
            if (item instanceof NovelModel) {
                novels.add((NovelModel) item);
            }

            else {
                String[] novelHolder = (String[]) item;
                NovelModel novel = new NovelModel(novelHolder[0], novelHolder[1], novelHolder[2], novelHolder[3]);
                novels.add(novel);
            }
        }
        return novels;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you can call your export methods here
            } else {
                // Permission denied, handle the case
            }
        }
    }
}