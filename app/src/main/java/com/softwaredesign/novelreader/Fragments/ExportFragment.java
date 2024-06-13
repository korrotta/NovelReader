package com.softwaredesign.novelreader.Fragments;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.text.SpannedString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;

import com.softwaredesign.novelreader.Adapters.ChapterListSpinnerAdapter;
import com.softwaredesign.novelreader.BackgroundTask;
import com.softwaredesign.novelreader.ExportHandlers.PdfExportHandler;
import com.softwaredesign.novelreader.Global.GlobalConfig;
import com.softwaredesign.novelreader.Global.ReusableFunction;
import com.softwaredesign.novelreader.Models.ChapterContentModel;
import com.softwaredesign.novelreader.Models.ChapterModel;
import com.softwaredesign.novelreader.R;

import java.util.ArrayList;
import java.util.List;

public class ExportFragment extends Fragment {

    private AppCompatSpinner beginPageSpinner, beginChapterSpinner, endPageSpinner, endChapterSpinner, fileFormatSpinner;
    private AppCompatButton exportButton;
    private ProgressBar progressBar;

    private static final String ARG_NOVEL_URL = "novel_url";
    private String NovelUrl;
    private Activity parentActivity;
    private Handler handler = new Handler(Looper.getMainLooper());

    private List<String> beginPage, endPage;
    private List<ChapterModel> beginChapter, endChapter;
    private ArrayAdapter<String> beginPageAdapter, endPageAdapter;
    private ChapterListSpinnerAdapter beginChapterAdapter, endChapterAdapter;
    private int numberOfPage;
    private List<ChapterModel> chapters;

    private int selectedBeginPage, selectedEndPage;
    private ChapterModel selectedBeginChapter, selectedEndChapter;
    private int beginPageId, endPageId, beginChapterId, endChapterId;
    private static final int PERMISSION_REQUEST_CODE = 1;

//    public abstract class BackgroundTask {
//
//        public abstract void onPreExecute();
//        public abstract void doInBackground();
//        public abstract void onPostExecute();
//
//        public void execute() {
//            onPreExecute();
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    doInBackground();
//                    requireActivity().runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            onPostExecute();
//                        }
//                    });
//                }
//            }).start();
//        }
//    }

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
        initArrayList();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.parentActivity = getActivity();
        initAdapter();

        getNumberOfPagesTask();

        beginPageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //
                beginPageId = parsePageToId((String) parent.getItemAtPosition(position));
                getChapterListTask(beginChapter, beginChapterAdapter, NovelUrl ,beginPageId);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        endPageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //
                endPageId = parsePageToId((String) parent.getItemAtPosition(position));
                getChapterListTask(endChapter, endChapterAdapter, NovelUrl ,endPageId);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        beginChapterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ChapterModel chapter = (ChapterModel) parent.getItemAtPosition(position);
                selectedBeginChapter =chapter;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        endChapterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ChapterModel chapter = (ChapterModel) parent.getItemAtPosition(position);
                selectedEndChapter =chapter;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        exportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //NOTE: Test export
                getAndExportChapterContentTask(selectedBeginChapter.getChapterUrl());

                //Export in first page:
                boolean beginReach = false;
                for (ChapterModel chapter: beginChapter){
                    if (beginReach){
                        exportChapter(chapter);
                        continue;
                    }
                    if (chapter.equals(selectedBeginChapter)){
                        beginReach = true;
                        exportChapter(chapter);
                    }
                }

                //Export all pages between:
                for (int i = beginPageId+1; i <= endPageId-1; i++){
                    List<ChapterModel> tempArr = new ArrayList<>();
                    getChapterListTask(tempArr, null, NovelUrl, i);
                    for (ChapterModel chapter: tempArr){
                        exportChapter(chapter);
                    }
                }

                //Export in last page:
                for (ChapterModel chapter: endChapter){
                    exportChapter(chapter);
                    if (chapter.equals(selectedEndChapter)){
                        break;
                    }
                }
            }
        });
    }

    private void initView(View view) {
        beginPageSpinner = view.findViewById(R.id.fromPageSpinner);
        beginChapterSpinner = view.findViewById(R.id.fromChapterSinner);
        endPageSpinner = view.findViewById(R.id.toPageSpinner);
        endChapterSpinner = view.findViewById(R.id.toChapterSinner);
        fileFormatSpinner = view.findViewById(R.id.fileFormatSpinner);
        exportButton = view.findViewById(R.id.exportNovelButton);
        progressBar = view.findViewById(R.id.exportFragmentPB);
    }

    private void initArrayList(){
        beginPage = new ArrayList<>();
        endPage = new ArrayList<>();

        beginChapter = new ArrayList<>();
        endChapter = new ArrayList<>();
    }

    private void initAdapter(){

        beginPageAdapter = new ArrayAdapter<String>(parentActivity, android.R.layout.simple_spinner_item, beginPage);
        beginPageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        endPageAdapter = new ArrayAdapter<String>(parentActivity, android.R.layout.simple_spinner_item, endPage);
        endPageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        beginChapterAdapter = new ChapterListSpinnerAdapter(parentActivity, android.R.layout.simple_spinner_item, beginChapter);

        endChapterAdapter = new ChapterListSpinnerAdapter(parentActivity, android.R.layout.simple_spinner_item, endChapter);

        beginPageSpinner.setAdapter(beginPageAdapter);
        endPageSpinner.setAdapter(endPageAdapter);

        beginChapterSpinner.setAdapter(beginChapterAdapter);
        endChapterSpinner.setAdapter(endChapterAdapter);
    }

    private void getNumberOfPagesTask() {
        new BackgroundTask(parentActivity) {
            @Override
            public void onPreExecute() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.VISIBLE);
                        progressBar.startAnimation(AnimationUtils.loadAnimation(parentActivity, android.R.anim.fade_in));
                    }
                });
            }

            @Override
            public void doInBackground() {
                Log.d("reached here", "read");
                numberOfPage = GlobalConfig.Global_Current_Scraper.getChapterListNumberOfPages(NovelUrl);
                Log.d("Reached here", String.valueOf(numberOfPage));

                beginPage.clear();
                endPage.clear();

                for (int i =1; i <= numberOfPage; i++){
                    beginPage.add("Page "+ i);
                    endPage.add("Page " + i);
                    Log.d("Export fragment check", String.valueOf(i));
                }
            }

            @Override
            public void onPostExecute() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        progressBar.startAnimation(AnimationUtils.loadAnimation(parentActivity, android.R.anim.fade_out));
                    }
                });
                beginPageAdapter.notifyDataSetChanged();
                endPageAdapter.notifyDataSetChanged();
            }
        }.execute();
    }

    private int parsePageToId(String page){
        return Integer.parseInt(page.split(" ")[1]);
    }

    private void getChapterListTask(List<ChapterModel> refChapters, ChapterListSpinnerAdapter refChapterAdapter, String NovelUrl, int pageId){
        new BackgroundTask(this.parentActivity){

            @Override
            public void onPreExecute() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.VISIBLE);
                        progressBar.startAnimation(AnimationUtils.loadAnimation(parentActivity, android.R.anim.fade_in));
                    }
                });
            }

            @Override
            public void doInBackground() {
                List<Object> tempList = GlobalConfig.Global_Current_Scraper.getChapterListInPage(NovelUrl, pageId);
                if (tempList.size() ==0) {
                    Log.d("Somehow", "empty here");
                }
                List<ChapterModel> chapters = identifyingList(tempList);
                // Replace the current list of page items with the fetched list
                ReusableFunction.ReplaceList(refChapters, chapters);
            }

            @Override
            public void onPostExecute() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        progressBar.startAnimation(AnimationUtils.loadAnimation(parentActivity, android.R.anim.fade_out));
                    }
                });
                if (refChapterAdapter == null) return;
                refChapterAdapter.notifyDataSetChanged();
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

    private void exportChapter(ChapterModel chapter){
        Log.d("Export Chapter", chapter.getChapterName());

    }

    private void getAndExportChapterContentTask(String chapterUrl){
        new BackgroundTask(this.parentActivity){

            @Override
            public void onPreExecute() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.VISIBLE);
                        progressBar.startAnimation(AnimationUtils.loadAnimation(parentActivity, android.R.anim.fade_in));
                    }
                });
            }

            @Override
            public void doInBackground() {
                ChapterContentModel content =  GlobalConfig.Global_Current_Scraper.getChapterContent(chapterUrl);
                PdfExportHandler ePdf = new PdfExportHandler();

                //NOTE: PERMISSION
                if (ContextCompat.checkSelfPermission(ExportFragment.this.parentActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ExportFragment.this.parentActivity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                } else {
                    ePdf.exportChapter(content.getContent());
                }

            }

            @Override
            public void onPostExecute() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        progressBar.startAnimation(AnimationUtils.loadAnimation(parentActivity, android.R.anim.fade_out));
                    }
                });
            }
        }.execute();
    }

}