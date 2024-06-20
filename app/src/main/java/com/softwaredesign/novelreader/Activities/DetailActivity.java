package com.softwaredesign.novelreader.Activities;

// Import necessary Android and Java libraries

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.softwaredesign.novelreader.BackgroundTask;
import com.softwaredesign.novelreader.Fragments.ChapterListFragment;
import com.softwaredesign.novelreader.Fragments.DetailNovelFragment;
import com.softwaredesign.novelreader.Fragments.ExportFragment;
import com.softwaredesign.novelreader.Global.GlobalConfig;
import com.softwaredesign.novelreader.Models.NovelDescriptionModel;
import com.softwaredesign.novelreader.R;
import com.squareup.picasso.Picasso;

public class DetailActivity extends AppCompatActivity {

    private ImageView detailImage;
    private TextView detailName, detailAuthor;
    private BottomNavigationView bottomNavigationView;
    private static String NovelUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        viewInit();

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
        assert NovelUrl != null;
        NovelUrl = NovelUrl.replace("chuong-1/", "");
        Log.d("Tag", "onCreate: " + NovelUrl);
    }

    private void viewInit() {
        detailImage = findViewById(R.id.detailImage);
        detailName = findViewById(R.id.detailName);
        detailAuthor = findViewById(R.id.detailAuthor);
        bottomNavigationView = findViewById(R.id.detailBottomNav);
    }

    private void handleBottomNav() {
        // Initially select the detail tab
        bottomNavigationView.setSelectedItemId(R.id.detailBottomNavDetail);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            if (itemId == R.id.detailBottomNavChapterList) {
                // Handle Chapter List Option
                selectedFragment = ChapterListFragment.newInstance(NovelUrl);
                toggleDetailVisibility(false);
            } else if (itemId == R.id.detailBottomNavDetail) {
                // Handle Detail Option
                selectedFragment = DetailNovelFragment.newInstance(NovelUrl);
                toggleDetailVisibility(true);
            } else if (itemId == R.id.detailBottomNavExport) {
                // Handle Export Option
                selectedFragment = ExportFragment.newInstance(NovelUrl);
                toggleDetailVisibility(false);
            }

            // Switch to selected Fragment
            if (selectedFragment != null) {
                loadFragment(selectedFragment);
            }

            return true;
        });
    }

    // Method to toggle visibility of details
    private void toggleDetailVisibility(boolean visible) {
        int visibility = visible ? View.VISIBLE : View.GONE;
        detailImage.setVisibility(visibility);
        detailName.setVisibility(visibility);
        detailAuthor.setVisibility(visibility);
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
        Picasso.get().load(ndm.getImgUrl()).into(detailImage);
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
            Object desc = GlobalConfig.Global_Current_Scraper.getNovelDetail(NovelUrl);
            identifyingNovelDescription(desc);

        }
        @Override
        public void onPostExecute() {
            // Update UI with the fetched novel details
            setUIData(novelDescModel);
        }

        private void identifyingNovelDescription(Object desc) {
            if (desc instanceof NovelDescriptionModel) {
                novelDescModel = (NovelDescriptionModel) desc;
            }
            else {
                String[] realDesc = (String[]) desc;
                novelDescModel = new NovelDescriptionModel(realDesc[0], realDesc[1], realDesc[2], realDesc[3]);
            }
        }

    };

}