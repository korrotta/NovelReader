package com.softwaredesign.novelreader.Fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.softwaredesign.novelreader.Adapters.ChapterListItemAdapter;
import com.softwaredesign.novelreader.BackgroundTask;
import com.softwaredesign.novelreader.Global.GlobalConfig;
import com.softwaredesign.novelreader.Global.ReusableFunction;
import com.softwaredesign.novelreader.Models.ChapterModel;
import com.softwaredesign.novelreader.R;

import java.util.ArrayList;
import java.util.List;

public class ChapterListFragment extends Fragment {

    private ImageView prevChapterPage, nextChapterPage;
    private TextView pageTextView;
    private RecyclerView chapterListRV;
    private Activity parentActivity;
    private ChapterListItemAdapter chapterListItemAdapter;
    private static volatile int numberOfPages, currentPage, pageSize;
    private static volatile List<ChapterModel> pageItems;
    private LinearLayout chapterListPageControlLayout;
    private ProgressBar chapterListFragmentPB;
    private final Handler handler = new Handler();

    private static String NovelUrl;
    private static final String ARG_NOVEL_URL = "novel_url";

/*    public abstract class BackgroundTask {

        public abstract void onPreExecute();
        public abstract void doInBackground();
        public abstract void onPostExecute();

        public void execute() {
            onPreExecute();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    doInBackground();
                    requireActivity().runOnUiThread(() -> onPostExecute());
                }
            }).start();
        }
    }*/

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

        // Return the inflated view
        this.parentActivity = getActivity();
        return view;
    }

    private void initAdapter() {
        // Initialize and set the adapter for the chapter list RecyclerView
        chapterListItemAdapter = new ChapterListItemAdapter(parentActivity, pageItems);
        chapterListRV.setAdapter(chapterListItemAdapter);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.parentActivity = getActivity();

        // Execute the background task to fetch the number of chapter pages
        getTotalPagesThenNovelListTask();

        // Initialize adapter
        initAdapter();

        // Handle Pagination
        handlePagination();
    }

    @SuppressLint("SetTextI18n")
    private void handlePagination() {
        // Set click listener to show a popup menu for page selection
        pageTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create a PopupMenu
                PopupMenu popupMenu = new PopupMenu(getContext(), pageTextView);

                // Add pages to the PopupMenu
                for (int i = 1; i <= numberOfPages; i++) {
                    popupMenu.getMenu().add(0, i, i, "Trang " + i);
                }

                // Set a click listener for PopupMenu items
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        // Handle page selection
                        loadPage(item.getItemId());
                        pageTextView.setText("Trang " + item.getItemId() + " trên " + numberOfPages);
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
        prevChapterPage.setOnClickListener(v -> {
            if (currentPage <= 1) return;
            currentPage--;
            pageTextView.setText("Trang " + currentPage + " trên " + numberOfPages);
            loadPage(currentPage);

        });

        // Handle Next Page Button
        nextChapterPage.setOnClickListener(v -> {
            if (currentPage >= numberOfPages) return;
            currentPage++;
            pageTextView.setText("Trang " + currentPage + " trên " + numberOfPages);
            loadPage(currentPage);

        });
    }

    private void initView(View view) {
        // Find and initialize the chapter list RecyclerView
        chapterListRV = view.findViewById(R.id.chapterListRV);
        // Find and initialize the page TextView
        pageTextView = view.findViewById(R.id.pageTextView);
        // Find and initialize the progress bar
        chapterListFragmentPB = view.findViewById(R.id.chapterListFragmentPB);
        // Find and initialize the layout of chapter list pagination
        chapterListPageControlLayout = view.findViewById(R.id.chapterListPageControlLayout);
        // Find and initialize previous, next chapter page ImageView
        prevChapterPage = view.findViewById(R.id.previousChapterPage);
        nextChapterPage = view.findViewById(R.id.nextChapterPage);

        // Set up RecyclerView with a GridLayoutManager
        GridLayoutManager gridLayoutManager = new GridLayoutManager(parentActivity, 1);
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
        getChapterListTask();
    }

    // Method to set up pagination controls
    @SuppressLint("SetTextI18n")
    private void setupPageControls() {
        // Set the visibility of the pagination
        chapterListPageControlLayout.setVisibility(View.VISIBLE);
        // Set the text of the page TextView
        pageTextView.setText("Trang 1 trên " + numberOfPages);
    }

    // Background task to fetch chapter list
    private void getChapterListTask() {
        new BackgroundTask(parentActivity) {
            @Override
            public void onPreExecute() {
                // No pre-execution actions needed
                // Show progress bar with fade-in animation
                handler.post(() -> {
                    Log.d("CONTEXT", String.valueOf(parentActivity));
                    chapterListFragmentPB.setVisibility(View.VISIBLE);
                    chapterListFragmentPB.startAnimation(AnimationUtils.loadAnimation(parentActivity, android.R.anim.fade_in));
                });
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

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onPostExecute() {
                // Hide progress bar with fade-out animation after a delay
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        chapterListFragmentPB.setVisibility(View.GONE);
                        chapterListFragmentPB.startAnimation(AnimationUtils.loadAnimation(parentActivity, android.R.anim.fade_out));

                    }
                });

                // Notify adapter that data has changed
                chapterListItemAdapter.updateList(pageItems);
                chapterListItemAdapter.notifyDataSetChanged();
            }
        }.execute();
    }


    // Background task to fetch the number of chapter pages

    private void getTotalPagesThenNovelListTask(){
        new BackgroundTask(parentActivity) {
            @Override
            public void onPreExecute() {

            }

            @Override
            public void doInBackground() {
                // Fetch the number of chapter pages using the scraper
                numberOfPages = GlobalConfig.Global_Current_Scraper.getChapterListNumberOfPages(NovelUrl);
            }

            @Override
            public void onPostExecute() {

                // Set up pagination controls and load the first page
                setupPageControls();
                // Load the current page
                loadPage(currentPage);
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