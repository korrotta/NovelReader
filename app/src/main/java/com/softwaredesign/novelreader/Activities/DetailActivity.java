package com.softwaredesign.novelreader.Activities;

// Import necessary Android and Java libraries

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.softwaredesign.novelreader.Adapters.ChapterListItemAdapter;
import com.softwaredesign.novelreader.BackgroundTask;
import com.softwaredesign.novelreader.Fragments.ChapterListFragment;
import com.softwaredesign.novelreader.Fragments.DetailNovelFragment;
import com.softwaredesign.novelreader.Global.ReusableFunction;
import com.softwaredesign.novelreader.Models.ChapterModel;
import com.softwaredesign.novelreader.Models.NovelDescriptionModel;
import com.softwaredesign.novelreader.NovelParsers.TruyenfullScraper;
import com.softwaredesign.novelreader.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class DetailActivity extends AppCompatActivity {

    // Declare UI elements and other necessary variables
    private ImageView detailImage;
    private TextView detailName, detailAuthor, detailDescription;
    private static String NovelUrl;
    private TruyenfullScraper truyenfullScraper;
    private NovelDescriptionModel ndm;
    private ChapterListItemAdapter chapterListItemAdapter;
    private ProgressBar progressBar;
    private BottomNavigationView bottomNavigationView;
    private Handler handler = new Handler(Looper.getMainLooper());
    private static volatile int numberOfPages, currentPage;
    private static volatile List<ChapterModel> pageItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        truyenfullScraper = new TruyenfullScraper();
        currentPage = 1;

        NovelUrl = "";
        if (pageItems != null) {
            pageItems.clear();
        } else {
            pageItems = new ArrayList<>();
        }

        numberOfPages = 0;

        // Initialize UI elements by finding their IDs
        detailImage = findViewById(R.id.detailImage);
        detailName = findViewById(R.id.detailName);
        detailAuthor = findViewById(R.id.detailAuthor);
        progressBar = findViewById(R.id.detailProgressBar);
        bottomNavigationView = findViewById(R.id.detailBottomNav);

        /*
        ATTENTION:
        detailDescription moved to DetailNovelFragment
        chapterListRV moved to ChapterListFragment
        pageTextView moved to ChapterListFragment
         */

        // Initially load the Detail Fragment
        loadFragment(new DetailNovelFragment());

        // Initialize Chapter List RecyclerView
        // Set up RecyclerView with a GridLayoutManager
        GridLayoutManager gridLayoutManager = new GridLayoutManager(DetailActivity.this, 1);
        chapterListRV.setLayoutManager(gridLayoutManager);

        // Get novelUrl from selected novel
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            NovelUrl = bundle.getString("NovelUrl");
            NovelUrl = NovelUrl.replace("chuong-1/", "");
            Log.d("Tag", "onCreate: " + NovelUrl);

            // Execute tasks to fetch novel details and chapters
            getNovelDetailTask.execute();
            getNumberOfChapterPagesTask.execute(); //executed novelchapterlist task
        }

        //Set list active after fetch
        // Initialize and set the adapter for the chapter list RecyclerView
        chapterListItemAdapter = new ChapterListItemAdapter(DetailActivity.this, pageItems);
        chapterListRV.setAdapter(chapterListItemAdapter);

        // Handle Bottom Navigation View Events
        handleBottomNav();

    }

    private void handleBottomNav() {
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                int itemId = item.getItemId();
                if (itemId == R.id.detailBottomNavChapterList) {
                    // Handle Chapter List Option
                    selectedFragment = new ChapterListFragment();
                }
                else if (itemId == R.id.detailBottomNavDetail) {
                    // Handle Detail Option
                    selectedFragment = new DetailNovelFragment();
                }
                else if (itemId == R.id.detailBottomNavExport) {
                    // Handle Export Option

                }
                else if (itemId == R.id.detailBottomNavServer) {
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
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.detailFrameLayout, fragment)
                .commit();
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
            novelDescModel = truyenfullScraper.getNovelDetail(NovelUrl);
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

            // Fetch chapters for each page
//            for (int i = 1; i <= numberOfPages; i++){
//                String finalUrl = preOfFinalUrlForm + i + aftOfFileUrlForm;
//                Log.d("TAG", "doInBackground: " + finalUrl);
//                list.addAll(truyenfullScraper.novelChapterListScraping(finalUrl));
//            }

            // Set URL parts for chapter pagination
            this.preOfFinalUrlForm = NovelUrl + "/trang-";
            this.aftOfFileUrlForm = "/#list-chapter";

            //Fetch chapter list of that page.
            String pageUrl = this.preOfFinalUrlForm + currentPage + this.aftOfFileUrlForm;
            ReusableFunction.LogVariable(pageUrl);

            List<ChapterModel> tempPageList = truyenfullScraper.getChapterListFromUrl(pageUrl);
            replaceList(pageItems, tempPageList);
        }

        @Override
        public void onPostExecute() {

            // Notify adapter that data has changed
            chapterListItemAdapter.updateList(pageItems);
            chapterListItemAdapter.notifyDataSetChanged();
        }
    };

    // Background task to fetch the number of chapter pages
    private BackgroundTask getNumberOfChapterPagesTask = new BackgroundTask(DetailActivity.this) {
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

            numberOfPages = truyenfullScraper.getChapterListNumberOfPages(NovelUrl);
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
    private void setUIData(NovelDescriptionModel ndm) {

        detailName.setText(ndm.getName());
        detailAuthor.setText(ndm.getAuthor());
        detailDescription.setText(HtmlCompat.fromHtml(ndm.getDescription(), HtmlCompat.FROM_HTML_MODE_LEGACY));
        Picasso.get().load(ndm.getImgUrl()).placeholder(R.drawable.logo).into(detailImage);
    }

    private void replaceList(List destinationList, List dataList) {
        destinationList.clear();
        destinationList.addAll(dataList);
    }
}