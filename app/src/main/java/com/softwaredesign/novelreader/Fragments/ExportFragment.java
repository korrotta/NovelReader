package com.softwaredesign.novelreader.Fragments;

import android.os.Bundle;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.softwaredesign.novelreader.R;

public class ExportFragment extends Fragment {

    private AppCompatSpinner fromPageSpinner, fromChapterSpinner, toPageSpinner, toChapterSpinner, fileFormatSpinner;
    private AppCompatButton exportButton;
    private ProgressBar progressBar;

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

    public static ExportFragment newInstance(String novelUrl) {
        // Create a new instance of ExportFragment
        ExportFragment fragment = new ExportFragment();
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
        View view = inflater.inflate(R.layout.fragment_export, container, false);

        initView(view);

        return view;
    }

    private void initView(View view) {
        fromPageSpinner = view.findViewById(R.id.fromPageSpinner);
        fromChapterSpinner = view.findViewById(R.id.fromChapterSinner);
        toPageSpinner = view.findViewById(R.id.toPageSpinner);
        toChapterSpinner = view.findViewById(R.id.toChapterSinner);
        fileFormatSpinner = view.findViewById(R.id.fileFormatSpinner);
        exportButton = view.findViewById(R.id.exportNovelButton);
        progressBar = view.findViewById(R.id.exportFragmentPB);
    }
}