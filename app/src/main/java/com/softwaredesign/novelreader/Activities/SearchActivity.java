package com.softwaredesign.novelreader.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

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
    private LinearLayout searchPageControlLayout;
    private ImageView prevSearchPage, nextSearchPage;
    private TextView searchPageTextView;
    private Handler handler = new Handler();
    private static volatile int numberOfPages, currentPage, maxPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        searchSearchView = findViewById(R.id.searchSearchView);
        novelSearchListView = findViewById(R.id.searchListView);
        searchProgressBar = findViewById(R.id.searchProgressBar);
        searchPageControlLayout = findViewById(R.id.searchPageControlLayout);
        prevSearchPage = findViewById(R.id.previousSearchPage);
        nextSearchPage = findViewById(R.id.nextSearchPage);
        searchPageTextView = findViewById(R.id.searchPageTextView);

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

        // Setup search pagination
        setupPageControls();

        // Handle Paginagtion
        handlePagination();
    }

    private void handlePagination() {
        // Set click listener to show a popup menu for page selection
        searchPageTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Create a PopupMenu
                PopupMenu popupMenu = new PopupMenu(SearchActivity.this, searchPageTextView);

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
                        searchPageTextView.setText("Page " + item.getItemId() + " of " + numberOfPages);
                        return true;
                    }
                });

                // Set the gravity of the PopupMenu
                popupMenu.setGravity(Gravity.START);

                // Show the PopupMenu
                popupMenu.show();

            }
        });

        // Handle Previous Page Button
        prevSearchPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        // Handle Next Page Button
        nextSearchPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    private void setupPageControls() {
        currentPage = 1;
        maxPage = novelList.size();

        // TODO: Get total search results pages
        numberOfPages = maxPage / 5;

        // Set Search Page TextView
        searchPageControlLayout.setVisibility(View.VISIBLE);
        searchPageTextView.setText("Page 1 of " + numberOfPages);
    }

    // Method to load a specific page of search results
    private void loadPage(int page) {
        currentPage = page;
        //TODO: Fetch search results list of that page.
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
            novelList = truyenfullScraper.getSearchPageFromKeyword(searchQuery);
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