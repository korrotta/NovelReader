package com.softwaredesign.novelreader.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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
import com.softwaredesign.novelreader.Global.ReusableFunction;
import com.softwaredesign.novelreader.Models.NovelModel;
import com.softwaredesign.novelreader.R;
import com.softwaredesign.novelreader.Scrapers.TruyenfullScraper;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {

    private SearchView searchSearchView;
    private ListView novelSearchListView;
    private NovelSearchAdapter novelSearchAdapter;

    private ProgressBar searchProgressBar;
    private final TruyenfullScraper truyenfullScraper = new TruyenfullScraper();
    private String searchQuery;
    private LinearLayout searchPageControlLayout;
    private ImageView prevSearchPage, nextSearchPage;
    private TextView searchPageTextView;
    private static volatile int numberOfPages, currentPage, maxPage;
    private static ArrayList<NovelModel> novelList;

    private Handler handler = new Handler();
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
        }

        // Handle Search View
        searchView();

        if (novelList == null){
            novelList = new ArrayList<>();
        }
        novelList.clear();

        // Set Adapter and ListView
        novelSearchAdapter = new NovelSearchAdapter(SearchActivity.this, novelList);
        novelSearchListView.setAdapter(novelSearchAdapter);
        novelSearchListView.setClickable(true);

        getTotalPagesThenResult.execute();

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
                if (currentPage <= 1) return;
                currentPage--;
                loadPage(currentPage);

            }
        });

        // Handle Next Page Button
        nextSearchPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentPage >= numberOfPages) return;
                currentPage++;
                loadPage(currentPage);

            }
        });
    }

    private void setupPageControls() {
        currentPage = 1;

        SearchActivity.this.searchPageControlLayout.setVisibility(View.VISIBLE);
        SearchActivity.this.searchPageTextView.setText("Page 1 of " + numberOfPages);
    }

    // Method to load a specific page of search results
    private void loadPage(int page) {
        currentPage = page;
        getSearchResult();

    }

    private void searchView() {
        searchSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchQuery = query;
                getTotalPagesThenResult.execute();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // No action on query text change
                return false;
            }
        });
    }


    //Pre-execute needed, renew instance every load
    private void getSearchResult(){
        new BackgroundTask(SearchActivity.this) {
            @Override
            public void onPreExecute () {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        searchProgressBar.setVisibility(View.VISIBLE);
                        searchProgressBar.startAnimation(AnimationUtils.loadAnimation(SearchActivity.this, android.R.anim.fade_in));
                    }
                });
            }

            @Override
            public void doInBackground () {
                //get data from source
                ReusableFunction.ReplaceList(novelList, truyenfullScraper.getSearchPageFromKeywordAndPageNumber(searchQuery, currentPage));
            }

            @Override
            public void onPostExecute () {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        searchProgressBar.setVisibility(View.GONE);
                        searchProgressBar.startAnimation(AnimationUtils.loadAnimation(SearchActivity.this, android.R.anim.fade_out));
                    }
                });
                novelSearchAdapter.notifyDataSetChanged();
                novelSearchListView.smoothScrollToPosition(0);
                SearchActivity.this.searchPageTextView.setText("Page " + currentPage + " of " + numberOfPages);
            }
        }.execute();
    }




    //No pre-execute needed
    private final BackgroundTask getTotalPagesThenResult = new BackgroundTask(SearchActivity.this) {
        @Override
        public void onPreExecute() {
            //Do nothing
        }

        @Override
        public void doInBackground() {
            numberOfPages = truyenfullScraper.getNumberOfSearchResultPage(searchQuery);
        }

        @Override
        public void onPostExecute() {
            setupPageControls();
            if (numberOfPages > 0) loadPage(currentPage);
            else {
                novelList.clear();
                novelSearchAdapter.notifyDataSetChanged();
            }
        }
    };
}