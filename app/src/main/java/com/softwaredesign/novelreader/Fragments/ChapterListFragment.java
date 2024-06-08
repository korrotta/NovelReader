package com.softwaredesign.novelreader.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.softwaredesign.novelreader.NovelParsers.TruyenfullScraper;
import com.softwaredesign.novelreader.R;

import java.util.ArrayList;

public class ChapterListFragment extends Fragment {


    private TextView pageTextView;
    private RecyclerView chapterListRV;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chapter_list, container, false);

        chapterListRV = view.findViewById(R.id.chapterListRV);
        pageTextView = view.findViewById(R.id.pageTextView);

        return view;
    }
}