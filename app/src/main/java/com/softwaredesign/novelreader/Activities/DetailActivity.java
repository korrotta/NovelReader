package com.softwaredesign.novelreader.Activities;

// Import necessary Android and Java libraries

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.softwaredesign.novelreader.BackgroundTask;
import com.softwaredesign.novelreader.Global.GlobalConfig;
import com.softwaredesign.novelreader.Global.ReusableFunction;
import com.softwaredesign.novelreader.Models.ChapterModel;
import com.softwaredesign.novelreader.Models.NovelDescriptionModel;
import com.softwaredesign.novelreader.Models.NovelModel;
import com.softwaredesign.novelreader.Fragments.ChapterListFragment;
import com.softwaredesign.novelreader.Fragments.DetailNovelFragment;
import com.softwaredesign.novelreader.Models.NovelDescriptionModel;
import com.softwaredesign.novelreader.R;
import com.softwaredesign.novelreader.Scrapers.TruyenfullScraper;
import com.squareup.picasso.Picasso;

import java.util.List;

public class DetailActivity extends AppCompatActivity {

    private ImageView detailImage;
    private TextView detailName, detailAuthor, detailDescription, pageTextView;
    private RecyclerView chapterListRV;
    private ChapterListItemAdapter chapterListItemAdapter;
    private ProgressBar progressBar;
    private Handler handler = new Handler(Looper.getMainLooper());
    private TruyenfullScraper truyenfullScraper;
    private BottomNavigationView bottomNavigationView;
    private static String NovelUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        viewInit();

        truyenfullScraper = new TruyenfullScraper();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            getNovelUrlFromPreviousIntent(bundle);
            // Initially load the Detail Fragment
            loadFragment(DetailNovelFragment.newInstance(NovelUrl));
            getNovelDetailTask.execute();
        }

        // Handle Bottom Navigation View Events
        handleBottomNav();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
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
        bottomNavigationView = findViewById(R.id.detailBottomNav);
    }

    private void classVarInit() {
        pageSize = GlobalConfig.Global_Current_Scraper.getNumberOfChaptersPerPage();

        currentPage = 1;
    }
    private void handleBottomNav() {
        // Initially select the detail tab
        bottomNavigationView.setSelectedItemId(R.id.detailBottomNavDetail);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                int itemId = item.getItemId();
                if (itemId == R.id.detailBottomNavChapterList) {
                    // Handle Chapter List Option
                    selectedFragment = ChapterListFragment.newInstance(NovelUrl);
                } else if (itemId == R.id.detailBottomNavDetail) {
                    // Handle Detail Option
                    selectedFragment = DetailNovelFragment.newInstance(NovelUrl);
                } else if (itemId == R.id.detailBottomNavExport) {
                    // Handle Export Option

                } else if (itemId == R.id.detailBottomNavServer) {
                    // Handle Server Option

                }

                // Switch to selected Fragment
                if (selectedFragment != null) {
                    loadFragment(selectedFragment);
                }

                return true;
            }
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.detailFrameLayout, fragment)
                .addToBackStack(null)
                .commit();
    }

    // Method to update UI with fetched novel details
    private void setUIData(NovelDescriptionModel ndm) {
        detailName.setText(ndm.getName());
        detailAuthor.setText(ndm.getAuthor());
        Picasso.get().load(ndm.getImgUrl()).placeholder(R.drawable.logo).into(detailImage);
    }

    private void replaceList(List destinationList, List dataList) {
        destinationList.clear();
        destinationList.addAll(dataList);
    }

    // Background task to fetch novel details
    private final BackgroundTask getNovelDetailTask = new BackgroundTask(DetailActivity.this) {

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
            setUIData(novelDescModel);
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