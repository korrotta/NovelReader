package com.softwaredesign.novelreader.Fragments;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;

import com.softwaredesign.novelreader.BackgroundTask;
import com.softwaredesign.novelreader.Global.GlobalConfig;
import com.softwaredesign.novelreader.Models.NovelDescriptionModel;
import com.softwaredesign.novelreader.Models.NovelModel;
import com.softwaredesign.novelreader.R;
import com.softwaredesign.novelreader.Scrapers.TruyenfullScraper;

public class DetailNovelFragment extends Fragment {

    private TextView detailDescription;
    private Handler handler = new Handler();
    private ProgressBar detailNovelFragmentPB;

    private static final String ARG_NOVEL_URL = "novel_url";
    private String NovelUrl;

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
                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onPostExecute();
                        }
                    });
                }
            }).start();
        }
    }

    public static DetailNovelFragment newInstance(String novelUrl) {
        // Create a new instance of DetailNovelFragment
        DetailNovelFragment fragment = new DetailNovelFragment();
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
        View view = inflater.inflate(R.layout.fragment_detail_novel, container, false);

        // Initialize the views in the layout
        initView(view);

        // Execute the background task to fetch novel details
        getNovelDetailTask.execute();

        // Return the inflated view
        return view;
    }

    private void initView(View view) {
        // Find and initialize the description TextView
        detailDescription = view.findViewById(R.id.detailDescription);
        // Find and initialize the progress bar
        detailNovelFragmentPB = view.findViewById(R.id.detailNovelFragmentPB);
    }

    // Background task to fetch novel details
    private final BackgroundTask getNovelDetailTask = new DetailNovelFragment.BackgroundTask() {
        NovelDescriptionModel novelDescModel;

        @Override
        public void onPreExecute() {
            // Show progress bar with fade-in animation before task starts
            handler.post(new Runnable() {
                @Override
                public void run() {
                    detailNovelFragmentPB.setVisibility(View.VISIBLE);
                    detailNovelFragmentPB.startAnimation(AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in));
                }
            });
        }

        @Override
        public void doInBackground() {
            // Fetch novel details using the scraper in the background
            Object desc = GlobalConfig.Global_Current_Scraper.getNovelDetail(NovelUrl);
            if (desc instanceof NovelDescriptionModel) novelDescModel = (NovelDescriptionModel) desc;
            else {
                String[] realDesc = (String[]) desc;
                novelDescModel = new NovelDescriptionModel(realDesc[0], realDesc[1], realDesc[2],realDesc[3]);
            }
        }

        @Override
        public void onPostExecute() {
            // Update UI with the fetched novel details after task completion
            // Hide progress bar with fade-out animation after a delay
            detailNovelFragmentPB.setVisibility(View.GONE);
            detailNovelFragmentPB.startAnimation(AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_out));
            // Set the description text in the TextView
            detailDescription.setText(HtmlCompat.fromHtml(novelDescModel.getDescription(), HtmlCompat.FROM_HTML_MODE_LEGACY));
        }

    };

}