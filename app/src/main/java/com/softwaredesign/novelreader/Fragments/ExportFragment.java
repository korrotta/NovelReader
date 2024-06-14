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

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.exporter_library.IChapterExportHandler;
import com.softwaredesign.novelreader.Adapters.ChapterListSpinnerAdapter;
import com.softwaredesign.novelreader.Adapters.ExporterSpinnerAdapter;
import com.softwaredesign.novelreader.BackgroundTask;
import com.softwaredesign.novelreader.Global.GlobalConfig;
import com.softwaredesign.novelreader.Global.ReusableFunction;
import com.softwaredesign.novelreader.Models.ChapterContentModel;
import com.softwaredesign.novelreader.Models.ChapterModel;
import com.softwaredesign.novelreader.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExportFragment extends Fragment {

    private AppCompatSpinner beginPageSpinner, beginChapterSpinner, endPageSpinner, endChapterSpinner, fileFormatSpinner;
    private AppCompatButton exportButton;
    private ProgressBar progressBar;

    private static final String ARG_NOVEL_URL = "novel_url";
    private String NovelUrl;
    private Activity parentActivity;
    private Handler handler = new Handler(Looper.getMainLooper());
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    private List<String> beginPage, endPage;
    private List<ChapterModel> beginChapter, endChapter;
    private ArrayAdapter<String> beginPageAdapter, endPageAdapter;
    private ChapterListSpinnerAdapter beginChapterAdapter, endChapterAdapter;
    private int numberOfPage;
    private List<ChapterModel> chapters;
    private static List<ChapterModel> tempList;

    private int selectedBeginPage, selectedEndPage;
    private ChapterModel selectedBeginChapter, selectedEndChapter;
    private int beginPageId, endPageId, beginChapterId, endChapterId;

    private ExporterSpinnerAdapter exporterAdapter;
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
                getAndExportAll();
            }
        });

        fileFormatSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void initView(View view) {
        beginPageSpinner = view.findViewById(R.id.fromPageSpinner);
        beginChapterSpinner = view.findViewById(R.id.fromChapterSpinner);
        endPageSpinner = view.findViewById(R.id.toPageSpinner);
        endChapterSpinner = view.findViewById(R.id.toChapterSpinner);
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

        exporterAdapter = new ExporterSpinnerAdapter(parentActivity, android.R.layout.simple_spinner_item, GlobalConfig.Global_Exporter_List);

        beginPageSpinner.setAdapter(beginPageAdapter);
        endPageSpinner.setAdapter(endPageAdapter);

        beginChapterSpinner.setAdapter(beginChapterAdapter);
        endChapterSpinner.setAdapter(endChapterAdapter);

        fileFormatSpinner.setAdapter(exporterAdapter);
    }

    private void getNumberOfPagesTask() {
        new BackgroundTask(parentActivity) {
            @Override
            public void onPreExecute() {
                showProgressBar();
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
                }
            }

            @Override
            public void onPostExecute() {
               hideProgressBar();
                beginPageAdapter.notifyDataSetChanged();
                endPageAdapter.notifyDataSetChanged();
            }
        }.execute();
    }

    private int parsePageToId(String page){
        return Integer.parseInt(page.split(" ")[1]);
    }

    private void getAndExportAll(){
        new BackgroundTask(parentActivity){
            long finalDelay;
            @Override
            public void onPreExecute() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.VISIBLE);
                        progressBar.startAnimation(AnimationUtils.loadAnimation(parentActivity, android.R.anim.fade_in));
                    }
                });

                beginPageId = parsePageToId((String )beginPageSpinner.getSelectedItem());
                endPageId = parsePageToId((String) endPageSpinner.getSelectedItem());

                selectedBeginChapter = (ChapterModel) beginChapterSpinner.getSelectedItem();
                selectedEndChapter = (ChapterModel) endChapterSpinner.getSelectedItem();

                int begin = 0, end = 0,diff = 0;
                for (ChapterModel chapter: beginChapter){
                    if (chapter.getChapterName().equals(selectedBeginChapter.getChapterName())){
                        begin = beginChapter.indexOf(chapter)+1;
                        break;
                    }
                }
                for (ChapterModel chapter: endChapter){
                    if (chapter.getChapterName().equals(selectedEndChapter.getChapterName())){
                        end = endChapter.indexOf(chapter)+1;
                        break;
                    }
                }
                if (beginPageId == endPageId) {
                    diff = end -begin +1;
                }
                else {
                    int pageDiff = endPageId - beginPageId;
                    final int MAX_CHAPTER = GlobalConfig.Global_Current_Scraper.getNumberOfChaptersPerPage();
                    diff = pageDiff*MAX_CHAPTER + (beginPage.size() - begin+1) + end;
                 }
                Log.d("diff", String.valueOf(diff));
                finalDelay = diff* 400L;
            }

            @Override
            public void doInBackground() {
                if (beginPageId ==0 || endPageId == 0) return;
                if (beginPageId > endPageId) return;

                firstPageExportHandling();
                middlePagesExportHandling();
                if (beginPageId == endPageId) return;
                lastPageExportHandling();
            }
            @Override
            public void onPostExecute() {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        progressBar.startAnimation(AnimationUtils.loadAnimation(parentActivity, android.R.anim.fade_out));
                        Toast.makeText(parentActivity, "Done after "+ finalDelay, Toast.LENGTH_SHORT).show();
                    }
                }, finalDelay);
            }

            private void lastPageExportHandling() {
                for (ChapterModel chapter: endChapter){
                    exportChapter(chapter);
                    if (chapter.getChapterName().equals(selectedEndChapter.getChapterName())){
                        break;
                    }
                }
            }

            private void middlePagesExportHandling() {
                for (int i = beginPageId+1; i <= endPageId-1; i++){
                    if (endPageId - beginPageId <= 1) {
                        break;
                    }

                    List<Object> tempList = GlobalConfig.Global_Current_Scraper.getChapterListInPage(NovelUrl, i);
                    List<ChapterModel> chapters = identifyingList(tempList);

                    for (ChapterModel chapter: chapters){
                        exportChapter(chapter);
                    }
                }
            }

            private void firstPageExportHandling() {
                boolean beginReach = false;
                for (ChapterModel chapter: beginChapter){
                    if (chapter.getChapterName().equals(selectedEndChapter.getChapterName())){
                        exportChapter(chapter);
                        break;
                    }
                    if (beginReach){
                        exportChapter(chapter);
                        continue;
                    }
                    if (chapter.getChapterName().equals(selectedBeginChapter.getChapterName())){
                        beginReach = true;
                        exportChapter(chapter);
                    }
                }
            }
        }.execute();
    }
    private void getChapterListTask(List<ChapterModel> refChapters, ChapterListSpinnerAdapter refChapterAdapter, String NovelUrl, int pageId){
        new BackgroundTask(this.parentActivity){

            @Override
            public void onPreExecute() {
                showProgressBar();
            }

            @Override
            public void doInBackground() {
                List<Object> tempList = GlobalConfig.Global_Current_Scraper.getChapterListInPage(NovelUrl, pageId);
                List<ChapterModel> chapters = identifyingList(tempList);
                // Replace the current list of page items with the fetched list
                ReusableFunction.ReplaceList(refChapters, chapters);
            }

            @Override
            public void onPostExecute() {
                hideProgressBar();

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
        new ExecutorBackgroundTask(this.parentActivity){
            @Override
            public void onPreExecute() {
            }

            @Override
            public void doInBackground() {
                Object contentOri = GlobalConfig.Global_Current_Scraper.getChapterContent(chapter.getChapterUrl());
                ChapterContentModel content;
                if (contentOri instanceof ChapterContentModel) content = (ChapterContentModel) contentOri;
                else {
                    String[] holder = (String[])contentOri;
                    content = new ChapterContentModel(holder[0], holder[1], holder[2], holder[3]);
                }

                //NOTE: PERMISSION
                if (ContextCompat.checkSelfPermission(ExportFragment.this.parentActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ExportFragment.this.parentActivity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                } else {
                    String dir = parentActivity.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath()+"/Export";
                    File directory = ReusableFunction.MakeDirectory(dir, content.getNovelName());

                    IChapterExportHandler exporter = (IChapterExportHandler) fileFormatSpinner.getSelectedItem();
                    File typeDirectory = ReusableFunction.MakeDirectory(directory.getAbsolutePath(), exporter.getExporterName());

                    exporter.exportChapter(content.getContent(), typeDirectory, content.getChapterName().toString());
                    try {
                        Thread.sleep(200);
                        //sleep 2s between each
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            @Override
            public void onPostExecute() {
            }
        }.execute();
    }

    private void showProgressBar(){
        handler.post(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.VISIBLE);
                progressBar.startAnimation(AnimationUtils.loadAnimation(parentActivity, android.R.anim.fade_in));
            }
        });
    }

    private void hideProgressBar(){
        handler.post(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.GONE);
                progressBar.startAnimation(AnimationUtils.loadAnimation(parentActivity, android.R.anim.fade_out));
            }
        });
    }


    //Note: Thread with executor
    public abstract class ExecutorBackgroundTask {
        private Activity activity;

        public ExecutorBackgroundTask(Activity activity){
            this.activity = activity;
            onPreExecute();
        }

        private void startBackgroundTask() {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    doInBackground();
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onPostExecute();
                        }
                    });
                }
            });
        }

        public void execute(){
            startBackgroundTask();
        }

        public void cancel(){
            executorService.shutdownNow();
        }
        public abstract void onPreExecute();
        public abstract void doInBackground();
        public abstract void onPostExecute();
    }
}