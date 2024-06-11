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
import com.softwaredesign.novelreader.Global.GlobalConfig;
import com.softwaredesign.novelreader.Global.ReusableFunction;
import com.softwaredesign.novelreader.Models.NovelModel;
import com.softwaredesign.novelreader.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SearchActivity extends AppCompatActivity {

    private SearchView searchSearchView;
    private ListView novelSearchListView;
    private ImageView prevSearchPage, nextSearchPage;
    private TextView searchPageTextView;

    private LinearLayout searchPageControlLayout;

    private ProgressBar searchProgressBar;

    private NovelSearchAdapter novelSearchAdapter;

    private String searchQuery;

    private Handler handler = new Handler();

    private static ArrayList<NovelModel> novelList;
    private static volatile int numberOfPages, currentPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        viewInit();

        // Handle fetch Novel from query
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            searchQuery = bundle.getString("searchQuery");
        }

        // Handle Search View
        searchView();
        listViewInit();

        getTotalPagesThenResultTask();

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

    private void listViewInit() {
        if (novelList == null){
            novelList = new ArrayList<>();
        }
        // Set Adapter and ListView
        novelSearchAdapter = new NovelSearchAdapter(SearchActivity.this, novelList);
        novelSearchListView.setAdapter(novelSearchAdapter);
        novelSearchListView.setClickable(true);
    }

    private void viewInit() {
        searchSearchView = findViewById(R.id.searchSearchView);
        novelSearchListView = findViewById(R.id.searchListView);
        searchProgressBar = findViewById(R.id.searchProgressBar);
        searchPageControlLayout = findViewById(R.id.searchPageControlLayout);
        prevSearchPage = findViewById(R.id.previousSearchPage);
        nextSearchPage = findViewById(R.id.nextSearchPage);
        searchPageTextView = findViewById(R.id.searchPageTextView);
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
                getTotalPagesThenResultTask();
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
                List<Object> novelsResult = GlobalConfig.Global_Current_Scraper.getSearchPageFromKeywordAndPageNumber(searchQuery, currentPage);
                List<NovelModel> novels = identifyingList(novelsResult);
                //get data from source
                ReusableFunction.ReplaceList(novelList, novels);
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
    private void getTotalPagesThenResultTask(){
        new BackgroundTask(SearchActivity.this) {
            @Override
            public void onPreExecute() {
                //Do nothing
            }

            @Override
            public void doInBackground() {
                numberOfPages = GlobalConfig.Global_Current_Scraper.getNumberOfSearchResultPage(searchQuery);
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
        }.execute();
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
}