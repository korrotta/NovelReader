package com.softwaredesign.novelreader.Fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.softwaredesign.novelreader.Activities.DetailActivity;
import com.softwaredesign.novelreader.Adapters.ChapterListItemAdapter;
import com.softwaredesign.novelreader.BackgroundTask;
import com.softwaredesign.novelreader.Global.GlobalConfig;
import com.softwaredesign.novelreader.Global.ReusableFunction;
import com.softwaredesign.novelreader.Models.ChapterModel;
import com.softwaredesign.novelreader.Models.NovelModel;
import com.softwaredesign.novelreader.R;
import com.softwaredesign.novelreader.Scrapers.TruyenfullScraper;

import java.util.ArrayList;
import java.util.List;

public class ChapterListFragment extends Fragment {

    private TextView pageTextView;
    private RecyclerView chapterListRV;
    private ChapterListItemAdapter chapterListItemAdapter;
    private static volatile int numberOfPages, currentPage, pageSize;
    private static volatile List<ChapterModel> pageItems;
    private ProgressBar chapterListFragmentPB;
    private final Handler handler = new Handler();

    private static String NovelUrl;
    private static final String ARG_NOVEL_URL = "novel_url";

    public abstract class BackgroundTask {

        public abstract void onPreExecute();
        public abstract void doInBackground();
        public abstract void onPostExecute();

        public void execute() {
            onPreExecute();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    doInBackground();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onPostExecute();
                        }
                    });
                }
            }).start();
        }
    }

    public static ChapterListFragment newInstance(String novelUrl) {
        // Create a new instance of ChapterListFragment
        ChapterListFragment fragment = new ChapterListFragment();
        // Create a Bundle object to pass arguments
        Bundle args = new Bundle();
        // Put the novel URL into the Bundle
        args.putString(ARG_NOVEL_URL, novelUrl);
        // Set the arguments for the fragment
        fragment.setArguments(args);
        // Return the newly created fragment instance
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Check if there are any arguments passed to the fragment
        if (getArguments() != null) {
            // Retrieve the novel URL from the arguments
            NovelUrl = getArguments().getString(ARG_NOVEL_URL);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chapter_list, container, false);

        // Initialize the views in the layout
        initView(view);
        // Initialize class variables
        classVarInit();

        // Execute the background task to fetch the number of chapter pages
        getTotalPagesThenNovelListTask();

        // Initialize and set the adapter for the chapter list RecyclerView
        chapterListItemAdapter = new ChapterListItemAdapter(getContext(), pageItems);
        chapterListRV.setAdapter(chapterListItemAdapter);

        // Return the inflated view
        return view;
    }

    private void initView(View view) {
        // Find and initialize the chapter list RecyclerView
        chapterListRV = view.findViewById(R.id.chapterListRV);
        // Find and initialize the page TextView
        pageTextView = view.findViewById(R.id.pageTextView);
        // Find and initialize the progress bar
        chapterListFragmentPB = view.findViewById(R.id.chapterListFragmentPB);

        // Set up RecyclerView with a GridLayoutManager
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 1);
        chapterListRV.setLayoutManager(gridLayoutManager);
    }

    private void classVarInit() {
        // Get the number of chapters per page
        pageSize = GlobalConfig.Global_Current_Scraper.getNumberOfChaptersPerPage();
        // Set the current page to 1
        currentPage = 1;

        // Initialize or clear the list of page items
        if (pageItems != null) {
            pageItems.clear();
        } else {
            pageItems = new ArrayList<>();
        }

        // Initialize the number of pages
        numberOfPages = 0;
    }

    // Method to load a specific page of chapters
    private void loadPage(int page) {
        // Set the current page to the specified page
        currentPage = page;
        // Execute task to fetch chapters for the specified page
        getChapterListTask.execute();
    }

    // Method to set up pagination controls
    @SuppressLint("SetTextI18n")
    private void setupPageControls() {
        // Make the page TextView visible
        pageTextView.setVisibility(View.VISIBLE);
        // Set the text of the page TextView
        pageTextView.setText("Page 1 of " + numberOfPages);

        // Set click listener to show a popup menu for page selection
        pageTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create a PopupMenu
                PopupMenu popupMenu = new PopupMenu(getContext(), pageTextView);

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

    // Background task to fetch chapter list
    private final BackgroundTask getChapterListTask = new ChapterListFragment.BackgroundTask() {
        @Override
        public void onPreExecute() {
            // No pre-execution actions needed
        }

        @Override
        public void doInBackground() {
            // Fetch the list of chapters from the specified page URL
            List<Object> tempList = GlobalConfig.Global_Current_Scraper.getChapterListInPage(NovelUrl, currentPage);
            if (tempList.size() ==0) {
                Log.d("Somehow", "empty here");
            }
            List<ChapterModel> chapters = identifyingList(tempList);
            // Replace the current list of page items with the fetched list
            ReusableFunction.ReplaceList(pageItems, chapters);
        }

        @Override
        public void onPostExecute() {
            // Notify adapter that data has changed
            chapterListItemAdapter.updateList(pageItems);
            chapterListItemAdapter.notifyDataSetChanged();
        }
    };


    // Background task to fetch the number of chapter pages

    private void getTotalPagesThenNovelListTask(){
        new ChapterListFragment.BackgroundTask() {
            @Override
            public void onPreExecute() {
                // Show progress bar with fade-in animation
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        chapterListFragmentPB.setVisibility(View.VISIBLE);
                        chapterListFragmentPB.startAnimation(AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in));
                    }
                });
            }

            @Override
            public void doInBackground() {
                // Fetch the number of chapter pages using the scraper
                numberOfPages = GlobalConfig.Global_Current_Scraper.getChapterListNumberOfPages(NovelUrl);
            }

            @Override
            public void onPostExecute() {
                // Hide progress bar with fade-out animation after a delay
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        chapterListFragmentPB.setVisibility(View.GONE);
                        chapterListFragmentPB.startAnimation(AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_out));
                        // Set up pagination controls and load the first page
                        setupPageControls();
                        // Load the current page
                        loadPage(currentPage);
                    }
                });
            }
        }.execute();
    }
    private List<ChapterModel> identifyingList(List<Object> list){
        List<ChapterModel> chapterModels = new ArrayList<>();
        for (Object item: list){
            if (item instanceof ChapterModel) {
                chapterModels.add((ChapterModel) item);
            }
            else {
                String[] chapterHolder = (String[]) item;
                Log.d("Chapter holder", chapterHolder[1]);
                ChapterModel chapter = new ChapterModel(chapterHolder[0], chapterHolder[1], Integer.parseInt(chapterHolder[2]));
                chapterModels.add(chapter);
            }

        }
        return chapterModels;
    }

}