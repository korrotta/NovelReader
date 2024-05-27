package com.softwaredesign.novelreader.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.softwaredesign.novelreader.Adapters.ChapterListItemAdapter;
import com.softwaredesign.novelreader.BackgroundTask;
import com.softwaredesign.novelreader.Models.ChapterModel;
import com.softwaredesign.novelreader.Models.NovelDescriptionModel;
import com.softwaredesign.novelreader.NovelParsers.TruyenfullScraper;

import com.softwaredesign.novelreader.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class DetailActivity extends AppCompatActivity {

    private ImageView detailImage;
    private TextView detailName, detailAuthor, detailDescription, pageTextView;
    private static String NovelUrl;
    private TruyenfullScraper truyenfullScraper = new TruyenfullScraper();
    private NovelDescriptionModel ndm;
    private RecyclerView chapterListRV;
    private static List<ChapterModel> list, pageItems;
    private ChapterListItemAdapter chapterListItemAdapter;
    private static int numberOfPages;
    private String preOfFinalUrlForm;
    private String aftOfFileUrlForm;
    private ProgressBar progressBar;
    private int currentPage = 1;
    private int PAGE_SIZE = 10;
    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        //clear static variable:
        NovelUrl = "";
        if (list != null) list.clear();
        else list = new ArrayList<>();
        numberOfPages = 0;

        detailImage = findViewById(R.id.detailImage);
        detailName = findViewById(R.id.detailName);
        detailAuthor = findViewById(R.id.detailAuthor);
        detailDescription = findViewById(R.id.detailDescription);
        chapterListRV = findViewById(R.id.chapterListRV);
        progressBar = findViewById(R.id.detailProgressBar);
        pageTextView = findViewById(R.id.pageTextView);

        // Initialize Chapter List RecyclerView
        GridLayoutManager gridLayoutManager = new GridLayoutManager(DetailActivity.this, 1);
        chapterListRV.setLayoutManager(gridLayoutManager);

        // Get novelUrl from selected novel
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            NovelUrl = bundle.getString("NovelUrl");
            NovelUrl = NovelUrl.replace("chuong-1/", "");
            Log.d("Tag", "onCreate: " + NovelUrl);

            this.preOfFinalUrlForm = NovelUrl + "/trang-";
            this.aftOfFileUrlForm = "/#list-chapter";
            getNovelDetailTask.execute();
            getNumberOfChapterPagesTask.execute(); //executed novelchapterlist task
        }

        //Set list active after fetch
        chapterListItemAdapter = new ChapterListItemAdapter(DetailActivity.this, list);
        chapterListRV.setAdapter(chapterListItemAdapter);

    }

    private void loadPage(int page) {
        currentPage = page;
        int start = (page - 1) * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, list.size());
        Log.d("LIST SIZE", String.valueOf(list.size()));
        Log.d("START / END", start + " / " + String.valueOf(end));
        pageItems = list.subList(start, end);
        chapterListItemAdapter.updateList(pageItems);
    }

    private void setupPageControls() {
        pageTextView.setVisibility(View.VISIBLE);
        int totalPages = (int) Math.ceil((double) list.size() / PAGE_SIZE);
        pageTextView.setText("Page 1 of " + totalPages);

        pageTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create a PopupMenu
                PopupMenu popupMenu = new PopupMenu(DetailActivity.this, pageTextView);
                // Add pages to the PopupMenu
                for (int i = 1; i <= totalPages; i++) {
                    popupMenu.getMenu().add(0, i, i, "Page " + i);
                }
                // Set a click listener for PopupMenu items
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        // Handle page selection
                        loadPage(item.getItemId());
                        pageTextView.setText("Page " + item.getItemId() + " of " + totalPages);
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

    private BackgroundTask getNovelDetailTask = new BackgroundTask(DetailActivity.this) {
        NovelDescriptionModel ndm;
        @Override
        public void onPreExecute() {

        }

        @Override
        public void doInBackground() {
            //Fetch from scraped
            ndm = truyenfullScraper.novelDetailScraping(NovelUrl);
        }

        @Override
        public void onPostExecute() {
            DetailActivity.this.setUIData(ndm);
        }
    };

    private BackgroundTask getChapterListTask = new BackgroundTask(DetailActivity.this) {
        @Override
        public void onPreExecute() {
        }

        @Override
        public void doInBackground() {
            for (int i = 1; i <= numberOfPages; i++){
                String finalUrl = preOfFinalUrlForm + i + aftOfFileUrlForm;
                Log.d("TAG", "doInBackground: " + finalUrl);
                list.addAll(truyenfullScraper.novelChapterListScraping(finalUrl));
            }
        }

        @Override
        public void onPostExecute() {
            chapterListItemAdapter.notifyDataSetChanged();
        }
    };

    private BackgroundTask getNumberOfChapterPagesTask  = new BackgroundTask(DetailActivity.this) {
        @Override
        public void onPreExecute() {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.startAnimation(AnimationUtils.loadAnimation(DetailActivity.this, android.R.anim.fade_in));
                }
            });
        }

        @Override
        public void doInBackground() {
            numberOfPages = truyenfullScraper.chapterListNumberOfPages(NovelUrl);
            Log.d("NUMBER OF PAGES", String.valueOf(numberOfPages));
        }

        @Override
        public void onPostExecute() {
            getChapterListTask.execute();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    progressBar.setVisibility(View.GONE);
                    progressBar.startAnimation(AnimationUtils.loadAnimation(DetailActivity.this, android.R.anim.fade_out));
                    // Handle page controls
                    setupPageControls();
                    // Load current page
                    loadPage(currentPage);
                }
            }, 3000);
        }
    };


    private void setUIData(NovelDescriptionModel ndm){
        detailName.setText(ndm.getName());
        detailAuthor.setText(ndm.getAuthor());
        detailDescription.setText(HtmlCompat.fromHtml(ndm.getDescription(), HtmlCompat.FROM_HTML_MODE_LEGACY));
        Picasso.get().load(ndm.getImgUrl()).placeholder(R.drawable.logo).into(detailImage);
    }

}