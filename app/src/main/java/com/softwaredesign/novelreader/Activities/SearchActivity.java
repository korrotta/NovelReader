package com.softwaredesign.novelreader.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import com.softwaredesign.novelreader.Adapters.NovelSearchAdapter;
import com.softwaredesign.novelreader.BackgroundTask;
import com.softwaredesign.novelreader.Models.NovelModel;
import com.softwaredesign.novelreader.NovelParsers.TruyenfullScraper;
import com.softwaredesign.novelreader.R;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {

    private SearchView searchSearchView;
    private ListView novelSearchListView;
    private NovelSearchAdapter novelSearchAdapter;
    private ArrayList<NovelModel> novelList = new ArrayList<>();
    private ProgressBar searchProgressBar;
    private final TruyenfullScraper truyenfullScraper = new TruyenfullScraper();
    private String searchQuery;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        searchSearchView = findViewById(R.id.searchSearchView);
        novelSearchListView = findViewById(R.id.searchListView);
        searchProgressBar = findViewById(R.id.searchProgressBar);

        // Handle fetch Novel from query
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            searchQuery = bundle.getString("searchQuery");
            Log.d("SEARCH QUERY", searchQuery);
            fetchSearchNovels.execute();
        }

        // Handle Search View
        searchView();

        // Set Adapter and ListView
        novelSearchAdapter = new NovelSearchAdapter(SearchActivity.this, novelList);
        novelSearchListView.setAdapter(novelSearchAdapter);
        novelSearchListView.setClickable(true);

        // Handle Item Click
        novelSearchListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Create an intent to start DetailActivity
                Intent intent = new Intent(getApplicationContext(), DetailActivity.class);

                // Pass the novel URL to the DetailActivity
                intent.putExtra("NovelUrl", novelList.get(position).getUrl());

                // Start the DetailActivity
                startActivity(intent);
            }
        });

    }

    private void searchView() {
        searchSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchQuery = query;
                fetchSearchNovels.execute();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // No action on query text change
                return false;
            }
        });
    }

    private final BackgroundTask fetchSearchNovels = new BackgroundTask(SearchActivity.this) {
        @Override
        public void onPreExecute() {
            // Show and animate the progress bar before starting the task
            handler.post(new Runnable() {
                @Override
                public void run() {
                    searchProgressBar.setVisibility(View.VISIBLE);
                    searchProgressBar.startAnimation(AnimationUtils.loadAnimation(SearchActivity.this, android.R.anim.fade_in));
                }
            });
        }

        @Override
        public void doInBackground() {
            // Fetch novel List
            novelList = truyenfullScraper.searchPageScraping(searchQuery);
            Log.d("SEARCH NOVE LIST FETCHED", novelList.toString());
        }

        @Override
        public void onPostExecute() {
            // Hide and animate the progress bar after the task is completed
            handler.post(new Runnable() {
                @Override
                public void run() {
                    searchProgressBar.setVisibility(View.GONE);
                    searchProgressBar.startAnimation(AnimationUtils.loadAnimation(SearchActivity.this, android.R.anim.fade_out));
                    novelSearchAdapter.notifyDataSetChanged();
                }
            });
        }
    };

}