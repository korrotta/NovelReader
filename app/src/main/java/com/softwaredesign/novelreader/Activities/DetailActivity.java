package com.softwaredesign.novelreader.Activities;

// Import necessary Android and Java libraries
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import android.util.Log;
import java.util.ArrayList;
import java.util.List;

import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;

import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.softwaredesign.novelreader.Adapters.ChapterListItemAdapter;
import com.softwaredesign.novelreader.BackgroundTask;
import com.softwaredesign.novelreader.Global.GlobalConfig;
import com.softwaredesign.novelreader.Global.ReusableFunction;
import com.softwaredesign.novelreader.Models.ChapterModel;
import com.softwaredesign.novelreader.Models.NovelDescriptionModel;
import com.softwaredesign.novelreader.Models.NovelModel;
import com.softwaredesign.novelreader.R;

import com.squareup.picasso.Picasso;

public class DetailActivity extends AppCompatActivity {

    private ImageView detailImage;
    private TextView detailName, detailAuthor, detailDescription, pageTextView;
    private RecyclerView chapterListRV;
    private ChapterListItemAdapter chapterListItemAdapter;
    private ProgressBar progressBar;
    private Handler handler = new Handler(Looper.getMainLooper());

    private static String NovelUrl;
    private static volatile int numberOfPages, currentPage, pageSize;
    private static volatile List<ChapterModel> pageItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        classVarInit();
        viewInit();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            getNovelUrlFromPreviousIntent(bundle);
            getNovelDetailTask.execute();
            getNumberOfChapterPagesTask.execute();
        }
    }

    private void getNovelUrlFromPreviousIntent(Bundle bundle) {
        NovelUrl = bundle.getString("NovelUrl");
        NovelUrl = NovelUrl.replace("chuong-1/", "");
        Log.d("Tag", "onCreate: " + NovelUrl);
    }

    private void viewInit() {
        detailImage = findViewById(R.id.detailImage);
        detailName = findViewById(R.id.detailName);
        detailAuthor = findViewById(R.id.detailAuthor);
        detailDescription = findViewById(R.id.detailDescription);
        chapterListRV = findViewById(R.id.chapterListRV);
        progressBar = findViewById(R.id.detailProgressBar);
        pageTextView = findViewById(R.id.pageTextView);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(DetailActivity.this, 1);
        chapterListRV.setLayoutManager(gridLayoutManager);
        chapterListItemAdapter = new ChapterListItemAdapter(DetailActivity.this, pageItems);
        chapterListRV.setAdapter(chapterListItemAdapter);
    }

    private void classVarInit() {
        pageSize = GlobalConfig.Global_Current_Scraper.getNumberOfChaptersPerPage();

        currentPage = 1;

        NovelUrl = "";

        if (pageItems !=null){
            pageItems.clear();
        }
        else {
            pageItems = new ArrayList<>();
        }

        numberOfPages = 0;
    }

    // Method to load a specific page of chapters
    private void loadPage(int page) {
        currentPage = page;
        // Execute task to fetch chapters
        getChapterListTask.execute();
    }

    // Method to set up pagination controls
    private void setupPageControls() {
        pageTextView.setVisibility(View.VISIBLE);
        pageTextView.setText("Page 1 of " + numberOfPages);

        // Set click listener to show a popup menu for page selection
        pageTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Create a PopupMenu
                PopupMenu popupMenu = new PopupMenu(DetailActivity.this, pageTextView);

                // Add pages to the PopupMenu
                for (int i = 1; i <= numberOfPages; i++) {
                    popupMenu.getMenu().add(0, i, i, "Page " + i);
                }

                // Set a click listener for PopupMenu items
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        // Handle page selection
                        loadPage(item.getItemId());
                        pageTextView.setText("Page " + item.getItemId() + " of " + numberOfPages);
                        return true;
                    }
                });

                // Set the gravity of the PopupMenu
                popupMenu.setGravity(Gravity.START);

                // Show the PopupMenu
                popupMenu.show();

            }
        });
    }

    // Background task to fetch novel details
    private BackgroundTask getNovelDetailTask = new BackgroundTask(DetailActivity.this) {

        NovelDescriptionModel novelDescModel;
        @Override
        public void onPreExecute() {
            // No pre-execution actions needed

        }

        @Override
        public void doInBackground() {
            //Fetch from scraped
            // Fetch novel details using the scraper
            novelDescModel = GlobalConfig.Global_Current_Scraper.getNovelDetail(NovelUrl);
        }

        @Override
        public void onPostExecute() {
            // Update UI with the fetched novel details
            DetailActivity.this.setUIData(novelDescModel);
        }

    };

    // Background task to fetch chapter list
    private BackgroundTask getChapterListTask = new BackgroundTask(DetailActivity.this) {
        private String preOfFinalUrlForm, aftOfFileUrlForm;
        @Override
        public void onPreExecute() {
            // No pre-execution actions needed
        }

        @Override
        public void doInBackground() {

            List<ChapterModel> tempPageList = GlobalConfig.Global_Current_Scraper.getChapterListInPage(NovelUrl, currentPage);
            ReusableFunction.ReplaceList(pageItems, tempPageList);
        }

        @Override
        public void onPostExecute() {

            // Notify adapter that data has changed
            chapterListItemAdapter.updateList(pageItems);
            chapterListItemAdapter.notifyDataSetChanged();
        }
    };

    // Background task to fetch the number of chapter pages
    private BackgroundTask getNumberOfChapterPagesTask  = new BackgroundTask(DetailActivity.this) {
        @Override
        public void onPreExecute() {

            // Show progress bar with fade-in animation
            handler.post(new Runnable() {
                @Override
                public void run() {

                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.startAnimation(AnimationUtils.loadAnimation(DetailActivity.this, android.R.anim.fade_in));
                }
            });
        }

        // Fetch the number of chapter pages using the scraper
        @Override
        public void doInBackground() {

            numberOfPages = GlobalConfig.Global_Current_Scraper.getChapterListNumberOfPages(NovelUrl);
            Log.d("NUMBER OF PAGES", String.valueOf(numberOfPages));
        }

        @Override
        public void onPostExecute() {
            // Hide progress bar with fade-out animation after a delay
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    progressBar.setVisibility(View.GONE);
                    progressBar.startAnimation(AnimationUtils.loadAnimation(DetailActivity.this, android.R.anim.fade_out));
                    // Handle page controls
                    // and load the first page
                    setupPageControls();
                    // Load current page
                    loadPage(currentPage);
                }
            }, 3000);
        }
    };


    // Method to update UI with fetched novel details
    private void setUIData(NovelDescriptionModel ndm){

        detailName.setText(ndm.getName());
        detailAuthor.setText(ndm.getAuthor());
        detailDescription.setText(HtmlCompat.fromHtml(ndm.getDescription(), HtmlCompat.FROM_HTML_MODE_LEGACY));
        Picasso.get().load(ndm.getImgUrl()).placeholder(R.drawable.logo).into(detailImage);
    }


}