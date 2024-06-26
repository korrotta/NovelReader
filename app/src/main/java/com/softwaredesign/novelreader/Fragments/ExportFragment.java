package com.softwaredesign.novelreader.Fragments;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

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
    private RelativeLayout overlayProgress;
    private TextView exportFromChapterLabel, exportToChapterLabel, exportFileFormatLabel;

    private static final String ARG_NOVEL_URL = "novel_url";
    private String NovelUrl;
    private Activity parentActivity;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private List<String> beginPage, endPage;
    private List<ChapterModel> beginChapter, endChapter;
    private ArrayAdapter<String> beginPageAdapter, endPageAdapter;
    private ChapterListSpinnerAdapter beginChapterAdapter, endChapterAdapter;
    private int numberOfPage;

    private ChapterModel selectedBeginChapter, selectedEndChapter;
    private int beginPageId, endPageId;

    private static final int PERMISSION_REQUEST_CODE = 1;

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
        // Initialize parentActivity with the activity hosting the fragment
        this.parentActivity = getActivity();
        // Initialize adapter for the spinners
        initAdapter();

        // Perform initial tasks to get the number of pages
        getNumberOfPagesTask();

        // Hanlde Click Listeners
        handleListeners();
    }

    private void handleListeners() {
        // Set up listener for beginPageSpinner
        beginPageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Parse selected item to page ID
                beginPageId = parsePageToId((String) parent.getItemAtPosition(position));
                // Perform task to get chapter list based on selected begin page ID
                getChapterListTask(beginChapter, beginChapterAdapter, NovelUrl, beginPageId);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle case where nothing is selected
            }
        });

        // Set up listener for endPageSpinner
        endPageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Parse selected item to page ID
                endPageId = parsePageToId((String) parent.getItemAtPosition(position));
                // Perform task to get chapter list based on selected end page ID
                getChapterListTask(endChapter, endChapterAdapter, NovelUrl, endPageId);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle case where nothing is selected
            }
        });

        // Set up listener for beginChapterSpinner
        beginChapterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Get selected ChapterModel from spinner
                selectedBeginChapter = (ChapterModel) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle case where nothing is selected
            }
        });

        endChapterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Get selected ChapterModel from spinner
                selectedEndChapter = (ChapterModel) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle case where nothing is selected
            }
        });

        exportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Perform task to get and export all selected chapters
                getAndExportAll();
            }
        });

        fileFormatSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Handle item selection in file format spinner
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle case where nothing is selected
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
        overlayProgress = view.findViewById(R.id.progressOverlay);
        progressBar = view.findViewById(R.id.exportFragmentPB);
        exportFromChapterLabel = view.findViewById(R.id.exportFromChapterLabel);
        exportToChapterLabel = view.findViewById(R.id.exportToChapterLabel);
        exportFileFormatLabel = view.findViewById(R.id.exportFileFormatLabel);
    }

    private void initArrayList() {
        beginPage = new ArrayList<>();
        endPage = new ArrayList<>();

        beginChapter = new ArrayList<>();
        endChapter = new ArrayList<>();
    }

    private void initAdapter() {

        beginPageAdapter = new ArrayAdapter<String>(parentActivity, android.R.layout.simple_spinner_item, beginPage);
        beginPageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        endPageAdapter = new ArrayAdapter<String>(parentActivity, android.R.layout.simple_spinner_item, endPage);
        endPageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        beginChapterAdapter = new ChapterListSpinnerAdapter(parentActivity, android.R.layout.simple_spinner_item, beginChapter);

        endChapterAdapter = new ChapterListSpinnerAdapter(parentActivity, android.R.layout.simple_spinner_item, endChapter);

        ExporterSpinnerAdapter exporterAdapter = new ExporterSpinnerAdapter(parentActivity, android.R.layout.simple_spinner_item, GlobalConfig.Global_Exporter_List);

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
                // Show progress bar before executing background task
                showProgressBar();

                // Hide UI when show ProgressBar
                hideUI();
            }

            @Override
            public void doInBackground() {
                // Perform background operation to fetch number of pages
                Log.d("reached here", "read");
                numberOfPage = GlobalConfig.Global_Current_Scraper.getChapterListNumberOfPages(NovelUrl);
                Log.d("Reached here", String.valueOf(numberOfPage));

                // Clear existing lists and populate with page numbers
                beginPage.clear();
                endPage.clear();
                for (int i = 1; i <= numberOfPage; i++) {
                    beginPage.add("Page " + i);
                    endPage.add("Page " + i);
                }
            }

            @Override
            public void onPostExecute() {
                // Notify adapters to update UI with new data
                beginPageAdapter.notifyDataSetChanged();
                endPageAdapter.notifyDataSetChanged();
            }
        }.execute();
    }

    private int parsePageToId(String page) {
        return Integer.parseInt(page.split(" ")[1]);
    }

    private void getAndExportAll() {
        new BackgroundTask(parentActivity) {
            long finalDelay;

            @Override
            public void onPreExecute() {
                handler.post(() -> {
                    // Disable user interactions
                    parentActivity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                    // Show progress bar with animation
                    showProgressBar();
                });

                // Extract selected page IDs and chapters
                beginPageId = parsePageToId((String) beginPageSpinner.getSelectedItem());
                endPageId = parsePageToId((String) endPageSpinner.getSelectedItem());
                selectedBeginChapter = (ChapterModel) beginChapterSpinner.getSelectedItem();
                selectedEndChapter = (ChapterModel) endChapterSpinner.getSelectedItem();

                // Calculate the difference in chapters to determine final delay
                int begin = 0, end = 0, diff = 0;
                for (ChapterModel chapter : beginChapter) {
                    if (chapter.getChapterName().equals(selectedBeginChapter.getChapterName())) {
                        begin = beginChapter.indexOf(chapter) + 1;
                        break;
                    }
                }

                for (ChapterModel chapter : endChapter) {
                    if (chapter.getChapterName().equals(selectedEndChapter.getChapterName())) {
                        end = endChapter.indexOf(chapter) + 1;
                        break;
                    }
                }

                if (beginPageId == endPageId) {
                    diff = end - begin + 1;
                } else {
                    int pageDiff = endPageId - beginPageId;
                    final int MAX_CHAPTER = GlobalConfig.Global_Current_Scraper.getNumberOfChaptersPerPage();
                    diff = pageDiff * MAX_CHAPTER + (beginPage.size() - begin + 1) + end;
                }

                Log.d("diff", String.valueOf(diff));
                finalDelay = diff * 400L; // Delay calculation based on chapter count
            }

            @Override
            public void doInBackground() {
                if (beginPageId == 0 || endPageId == 0) return;
                if (beginPageId > endPageId) return;

                // Export handling for first, middle, and last pages
                firstPageExportHandling();
                middlePagesExportHandling();
                if (beginPageId == endPageId) return;
                lastPageExportHandling();
            }

            @Override
            public void onPostExecute() {
                handler.postDelayed(() -> {
                    // Hide progress bar with animation
                    hideProgressBar();

                    // Enable user interactions
                    parentActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                    // Show completion message with calculated delay
                    Toast.makeText(parentActivity, "Hoàn tất!", Toast.LENGTH_SHORT).show();
                }, finalDelay * 10); // Delayed execution after task completion
            }

            private void lastPageExportHandling() {
                for (ChapterModel chapter : endChapter) {
                    exportChapter(chapter);
                    if (chapter.getChapterName().equals(selectedEndChapter.getChapterName())) {
                        break;
                    }
                }
            }

            private void middlePagesExportHandling() {
                for (int i = beginPageId + 1; i <= endPageId - 1; i++) {
                    if (endPageId - beginPageId <= 1) {
                        break;
                    }

                    List<Object> tempList = GlobalConfig.Global_Current_Scraper.getChapterListInPage(NovelUrl, i);
                    List<ChapterModel> chapters = identifyingList(tempList);

                    for (ChapterModel chapter : chapters) {
                        exportChapter(chapter);
                    }
                }
            }

            private void firstPageExportHandling() {
                boolean beginReach = false;
                for (ChapterModel chapter : beginChapter) {
                    if (chapter.getChapterName().equals(selectedEndChapter.getChapterName())) {
                        exportChapter(chapter);
                        break;
                    }
                    if (beginReach) {
                        exportChapter(chapter);
                        continue;
                    }
                    if (chapter.getChapterName().equals(selectedBeginChapter.getChapterName())) {
                        beginReach = true;
                        exportChapter(chapter);
                    }
                }
            }
        }.execute();
    }

    private void getChapterListTask(List<ChapterModel> refChapters, ChapterListSpinnerAdapter refChapterAdapter, String NovelUrl, int pageId) {
        new BackgroundTask(this.parentActivity) {

            @Override
            public void onPreExecute() {
            }

            @Override
            public void doInBackground() {
                // Fetch list of chapters for the given page ID from the novel URL
                List<Object> tempList = GlobalConfig.Global_Current_Scraper.getChapterListInPage(NovelUrl, pageId);
                List<ChapterModel> chapters = identifyingList(tempList);
                // Replace the current list of page items with the fetched list
                ReusableFunction.ReplaceList(refChapters, chapters);
            }

            @Override
            public void onPostExecute() {
                // Hide progress bar after data fetching is complete
                hideProgressBar();

                // Show UI after Progress Bar is finished
                showUI();

                if (refChapterAdapter == null) return;
                refChapterAdapter.notifyDataSetChanged();
            }
        }.execute();
    }

    private List<ChapterModel> identifyingList(List<Object> list) {
        List<ChapterModel> chapterModels = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof ChapterModel) {
                chapterModels.add((ChapterModel) item);
            } else {
                String[] chapterHolder = (String[]) item;
                Log.d("Chapter holder", chapterHolder[1]);
                ChapterModel chapter = new ChapterModel(chapterHolder[0], chapterHolder[1], Integer.parseInt(chapterHolder[2]));
                chapterModels.add(chapter);
            }

        }
        return chapterModels;
    }

    private void exportChapter(ChapterModel chapter) {
        new ExecutorBackgroundTask(this.parentActivity) {
            @Override
            public void onPreExecute() {
                // Preparation before executing the task (e.g., showing progress)
            }

            @Override
            public void doInBackground() {
                // Fetch the original content of the chapter from the scraper
                Object contentOri = GlobalConfig.Global_Current_Scraper.getChapterContent(chapter.getChapterUrl());
                ChapterContentModel content;
                // Check the type of the fetched content and convert if necessary
                if (contentOri instanceof ChapterContentModel)
                    content = (ChapterContentModel) contentOri;
                else {
                    String[] holder = (String[]) contentOri;
                    content = new ChapterContentModel(holder[0], holder[1], holder[2], holder[3]);
                }

                //NOTE: PERMISSION
                // Check for WRITE_EXTERNAL_STORAGE permission
                if (ContextCompat.checkSelfPermission(ExportFragment.this.parentActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    // Request WRITE_EXTERNAL_STORAGE permission if not granted
                    ActivityCompat.requestPermissions(ExportFragment.this.parentActivity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                }

                // Proceed with exporting the chapter
                String dir = parentActivity.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + "/Export";
                File directory = ReusableFunction.MakeDirectory(dir, content.getNovelName());
                // Get the selected exporter from fileFormatSpinner (assuming it's a Spinner)
                IChapterExportHandler exporter = (IChapterExportHandler) fileFormatSpinner.getSelectedItem();
                // Create a subdirectory based on the exporter's name
                File typeDirectory = ReusableFunction.MakeDirectory(directory.getAbsolutePath(), exporter.getExporterName());
                // Create a subdirectory based on the exporter's name
                exporter.exportChapter(content.getContent(), typeDirectory, content.getChapterName());
                try {
                    Thread.sleep(200);
                    //sleep 2s between each
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onPostExecute() {
                // Post-execution tasks (e.g., update UI, handle completion)
            }
        }.execute();
    }

    private void showProgressBar() {
        handler.post(() -> {
            overlayProgress.setVisibility(View.VISIBLE);
            progressBar.startAnimation(AnimationUtils.loadAnimation(parentActivity, android.R.anim.fade_in));
        });
    }

    private void hideProgressBar() {
        handler.post(() -> {
            progressBar.startAnimation(AnimationUtils.loadAnimation(parentActivity, android.R.anim.fade_out));
            overlayProgress.setVisibility(View.GONE);
        });
    }

    private void showUI() {
        handler.post(() -> {
            exportFromChapterLabel.setVisibility(View.VISIBLE);
            exportToChapterLabel.setVisibility(View.VISIBLE);
            exportFileFormatLabel.setVisibility(View.VISIBLE);
            beginChapterSpinner.setVisibility(View.VISIBLE);
            endChapterSpinner.setVisibility(View.VISIBLE);
            beginPageSpinner.setVisibility(View.VISIBLE);
            endPageSpinner.setVisibility(View.VISIBLE);
            fileFormatSpinner.setVisibility(View.VISIBLE);
            exportButton.setVisibility(View.VISIBLE);
        });
    }

    private void hideUI() {
        handler.post(() -> {
            exportFromChapterLabel.setVisibility(View.INVISIBLE);
            exportToChapterLabel.setVisibility(View.INVISIBLE);
            exportFileFormatLabel.setVisibility(View.INVISIBLE);
            beginChapterSpinner.setVisibility(View.INVISIBLE);
            endChapterSpinner.setVisibility(View.INVISIBLE);
            beginPageSpinner.setVisibility(View.INVISIBLE);
            endPageSpinner.setVisibility(View.INVISIBLE);
            fileFormatSpinner.setVisibility(View.INVISIBLE);
            exportButton.setVisibility(View.INVISIBLE);
        });
    }

    //Note: Thread with executor
    public abstract class ExecutorBackgroundTask {
        private final Activity activity;

        public ExecutorBackgroundTask(Activity activity) {
            this.activity = activity;
            onPreExecute();
        }

        private void startBackgroundTask() {
            executorService.execute(() -> {
                doInBackground();
                activity.runOnUiThread(this::onPostExecute);
            });
        }

        public void execute() {
            startBackgroundTask();
        }

        public void cancel() {
            executorService.shutdownNow();
        }

        public abstract void onPreExecute();

        public abstract void doInBackground();

        public abstract void onPostExecute();
    }
}