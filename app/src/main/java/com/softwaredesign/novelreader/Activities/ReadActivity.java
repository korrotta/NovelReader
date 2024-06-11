package com.softwaredesign.novelreader.Activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;

import android.annotation.SuppressLint;

import android.content.DialogInterface;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;

import android.util.Log;

import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.scraper_library.INovelScraper;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.softwaredesign.novelreader.BackgroundTask;
import com.softwaredesign.novelreader.Global.GlobalConfig;
import com.softwaredesign.novelreader.Models.ChapterContentModel;
import com.softwaredesign.novelreader.R;
import com.softwaredesign.novelreader.ScraperFactory.ScraperFactory;

import java.util.ArrayList;
import java.util.List;

public class ReadActivity extends AppCompatActivity {

    private TextView chapterNameTV, chapterContentTV, searchStatusTV;
    private ScrollView contentScrollView;
    private ImageView chapterListIV, prevChapterIV, nextChapterIV, findInChapterIV, settingsIV, serverIV;
    private ImageButton searchUpIV, searchDownIV, searchCloseButton;
    private EditText searchEditText;
    private ProgressBar progressBar;
    private BottomAppBar bottomAppBar;
    private LinearLayout search_layout;
    private AlertDialog.Builder alertDialog;

    private String chapterUrl, chapterTitle, content, novelName, nextChapterUrl, previousChapterUrl;
    private List<Integer> searchResults = new ArrayList<>();
    private int currentSearchIndex = 0;
    final int[] selectedItem = new int[1];
    private List<String> availableSourceList;


    //NOTE: Local reader server:
    private INovelScraper readerServer;

    //NOTE: String variable holder:
    private static ArrayList<String[]> serverFetchedLink;

    private Handler handler = new Handler(Looper.getMainLooper());
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read);

        vitalValueInit();

        InitializeView();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            chapterUrl = bundle.getString("ChapterUrl");
        }

        //execute chapter content
        getChapterContentTask();

        prevChapterIV.setOnClickListener(prevBtnClickListener);
        nextChapterIV.setOnClickListener(nextBtnClickListener);

        // Handle Server Source Button
        serverIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("clicked?", "clicked");
                // AlertDialog builder instance to build the alert dialog
                alertDialog = new AlertDialog.Builder(ReadActivity.this);

                // Set the custom icon to the alert dialog
                alertDialog.setIcon(R.drawable.server);

                // Title of the alert dialog
                alertDialog.setTitle("Choose Server Source");
                getContentFromNameAndChapterTask();
            }
        });

        // Handle Find text in page Button
        findInChapterIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //searchEditText.setVisibility(View.VISIBLE);
                search_layout.setVisibility(View.VISIBLE);
                searchEditText.requestFocus();
                //overlayView.setVisibility(View.VISIBLE);  // Hiển thị overlay khi EditText hiển thị
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        /*overlayView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //searchEditText.setVisibility(View.GONE);
                search_layout.setVisibility(View.GONE);
                overlayView.setVisibility(View.GONE);  // Ẩn overlay khi nhấp vào
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
            }
        });*/

        // Hanlde search EditText
        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                String searchText = searchEditText.getText().toString();
                if (!searchText.isEmpty()) {
                    performSearch();
                    highlightText(searchText);
                }
                return true;
            }
            return false;
        });

        // Hanlde searchUp Button
        searchUpIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!searchResults.isEmpty()) {
                    currentSearchIndex = (currentSearchIndex - 1 + searchResults.size()) % searchResults.size();
                    scrollToSearchResult(false);
                }
            }
        });

        // Hanlde searchDown Button
        searchDownIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!searchResults.isEmpty()) {
                    currentSearchIndex = (currentSearchIndex + 1) % searchResults.size();
                    scrollToSearchResult(false);
                }
            }
        });

        // Hanlde searchClose Button
        searchCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchResults.clear();
                searchEditText.setText("");
                search_layout.setVisibility(View.GONE);
                clearHighlight();
                searchUpIV.setAlpha(searchResults.size() > 1 ? 1.0f : 0.2f);
                searchDownIV.setAlpha(searchResults.size() > 1 ? 1.0f : 0.2f);
                searchUpIV.setClickable(searchResults.size() > 1);
                searchDownIV.setClickable(searchResults.size() > 1);
                searchStatusTV.setText("");
            }
        });

        // Hanlde chapterList Button
        chapterListIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ReadActivity.this, "You clicked Chapter List", Toast.LENGTH_SHORT).show();
            }
        });

        // Handle Settings Button
        settingsIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ReadActivity.this, "You clicked Settings", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void vitalValueInit() {
        if (serverFetchedLink == null){
            serverFetchedLink = new ArrayList<>();
        }

        availableSourceList = new ArrayList<>();
        readerServer = GlobalConfig.Global_Current_Scraper.clone();

        //NOTE: final to pass reference in new scope
        selectedItem[0] = -1;
    }

    // search text in content function
    private void performSearch() {
        String query = searchEditText.getText().toString();
        String content = chapterContentTV.getText().toString();

        searchResults = new ArrayList<>();
        int index = content.indexOf(query);
        while (index >= 0) {
            searchResults.add(index);
            index = content.indexOf(query, index + query.length());
        }

        if (!searchResults.isEmpty()) {
            currentSearchIndex = 0;
            scrollToSearchResult(true);
            searchStatusTV.setText((currentSearchIndex + 1) + "/" + searchResults.size());
        } else {
            searchStatusTV.setText("0/0");
        }

        searchUpIV.setAlpha(searchResults.size() > 1 ? 1.0f : 0.2f);
        searchDownIV.setAlpha(searchResults.size() > 1 ? 1.0f : 0.2f);
        searchUpIV.setClickable(searchResults.size() > 1);
        searchDownIV.setClickable(searchResults.size() > 1);
    }

    // highlight text after found function
    private void highlightText(String searchText) {
        if (searchResults.isEmpty()){
            clearHighlight();
            return;
        }
        int highlightColor = ContextCompat.getColor(this, R.color.saddle_brown);

        Spannable spannable = new SpannableString(content);

        for (int i = 0; i < searchResults.size(); i++) {
            spannable.setSpan(new BackgroundColorSpan(highlightColor), searchResults.get(i), searchResults.get(i) + searchText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        chapterContentTV.setText(spannable);

        highlightTextWithCurrentHighlight(searchResults.get(currentSearchIndex));
    }

    // clear all highlight text function
    private void clearHighlight() {
        chapterContentTV.setText(content);
    }

    // scroll to search result function
    private void scrollToSearchResult(boolean forceScroll) {
        if (!searchResults.isEmpty() && currentSearchIndex < searchResults.size()) {
            int position = searchResults.get(currentSearchIndex);

            // Highlight current search result with a different color
            highlightTextWithCurrentHighlight(position);

            chapterContentTV.post(() -> {
                Layout layout = chapterContentTV.getLayout();
                if (layout != null) {
                    int line = layout.getLineForOffset(position);
                    int y = layout.getLineTop(line);

                    int scrollY = contentScrollView.getScrollY();
                    int scrollViewHeight = contentScrollView.getHeight() - bottomAppBar.getHeight() - searchEditText.getHeight();

                    // Only scroll if the result is not fully visible
                    if (forceScroll || y < scrollY || y > scrollY + scrollViewHeight - chapterContentTV.getLineHeight()) {
                        int targetScrollY = y - scrollViewHeight / 2 + chapterContentTV.getLineHeight() / 2;
                        contentScrollView.smoothScrollTo(0, targetScrollY);
                    }
                }
            });

            searchStatusTV.setText((currentSearchIndex + 1) + "/" + searchResults.size());
        }
    }


    // highlight text current result, with other color
    private void highlightTextWithCurrentHighlight(int currentPosition) {
        int highlightColor = ContextCompat.getColor(this, R.color.saddle_brown);
        int currentHighlightColor = ContextCompat.getColor(this, R.color.slate_blue);;
        String content = chapterContentTV.getText().toString();
        Spannable spannable = new SpannableString(content);

        for (int i = 0; i < searchResults.size(); i++) {
            int start = searchResults.get(i);
            int end = start + searchEditText.getText().toString().length();
            if (start == currentPosition) {
                spannable.setSpan(new BackgroundColorSpan(currentHighlightColor), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                spannable.setSpan(new BackgroundColorSpan(highlightColor), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        chapterContentTV.setText(spannable);
    }

    private void InitializeView() {
        chapterNameTV = findViewById(R.id.chapterNameRead);
        chapterContentTV = findViewById(R.id.chapterContentRead);
        chapterListIV = findViewById(R.id.chapterListRead);
        prevChapterIV = findViewById(R.id.previousChapterRead);
        nextChapterIV = findViewById(R.id.nextChapterRead);
        findInChapterIV = findViewById(R.id.findTextRead);
        settingsIV = findViewById(R.id.settingsRead);
        bottomAppBar = findViewById(R.id.bottomNavRead);
        progressBar = findViewById(R.id.readProgressBar);
        serverIV = findViewById(R.id.serverSourceRead);
        searchEditText = findViewById(R.id.search_edit_text);
        search_layout = findViewById(R.id.search_layout);
        searchDownIV = findViewById(R.id.search_down);
        searchUpIV = findViewById(R.id.search_up);
        searchStatusTV = findViewById(R.id.search_status);
        contentScrollView = findViewById(R.id.contentScrollView);
        searchCloseButton = findViewById(R.id.search_close);

        //Gone View
        if (nextChapterIV.getVisibility() == View.VISIBLE || prevChapterIV.getVisibility() == View.VISIBLE) return;

        nextChapterIV.setVisibility(View.GONE);
        prevChapterIV.setVisibility(View.GONE);
    }

    private void getChapterContentTask(){
        new BackgroundTask(ReadActivity.this) {
            @Override
            public void onPreExecute() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.VISIBLE);
                        progressBar.startAnimation(AnimationUtils.loadAnimation(ReadActivity.this, android.R.anim.fade_in));
                    }
                });
            }

            @Override
            public void doInBackground() {
                //Fetch from chapterURL
                Object item = readerServer.getChapterContent(chapterUrl);

                //Ensure datatype matched
                ChapterContentModel ccm;
                if (item instanceof ChapterContentModel) {
                    ccm = (ChapterContentModel) item;
                }
                else {
                    String[] holder = (String[]) item;
                    ccm = new ChapterContentModel(holder[0], holder[1], holder[2], holder[3]);
                }

                chapterTitle = ccm.getChapterName();
                content = ccm.getContent();
                novelName = ccm.getNovelName();

                nextChapterUrl = readerServer.getNextChapterUrl(chapterUrl);
                previousChapterUrl = readerServer.getPreviousChapterUrl(chapterUrl);
            }

            @Override
            public void onPostExecute() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        progressBar.startAnimation(AnimationUtils.loadAnimation(ReadActivity.this, android.R.anim.fade_out));
                    }
                });
                // Update UI after fetch
                chapterNameTV.setText(chapterTitle);
                chapterContentTV.setText(HtmlCompat.fromHtml(content, HtmlCompat.FROM_HTML_MODE_LEGACY));

                nextChapterIV.setVisibility(View.VISIBLE);
                prevChapterIV.setVisibility(View.VISIBLE);

                contentScrollView.fullScroll(ScrollView.FOCUS_UP);
            }
        }.execute();
    }
    private void getContentFromNameAndChapterTask(){

        if (availableSourceList == null) return;
        availableSourceList.clear();
        availableSourceList.add(readerServer.getSourceName());
        selectedItem[0] = 0; //as the first server added

        new BackgroundTask(ReadActivity.this) {
            @Override
            public void onPreExecute() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.VISIBLE);
                        progressBar.startAnimation(AnimationUtils.loadAnimation(ReadActivity.this, android.R.anim.fade_in));
                    }
                });
            }

            @Override
            public void doInBackground() {
                if (serverFetchedLink == null) serverFetchedLink = new ArrayList<>();
                serverFetchedLink.clear();

                for (INovelScraper scraper: GlobalConfig.Global_Source_List){
                    if (scraper.getSourceName().equalsIgnoreCase(readerServer.getSourceName())) continue;

                    Object item = scraper.getContentFromNameAndChapName(novelName, chapterTitle);
                    if (item == null) continue;

                    //Ensure datatype matched
                    ChapterContentModel ccm;
                    if (item instanceof ChapterContentModel) {
                        ccm = (ChapterContentModel) item;
                    }
                    else {
                        //note: inner holder
                        String[] holder = (String[]) item;
                        ccm = new ChapterContentModel(holder[0], holder[1], holder[2], holder[3]);
                    }

                    String[] holder = new String[2];
                    //note: holder init-ed here
                    holder[0] = scraper.getSourceName();
                    holder[1] = ccm.getChapterUrl();

                    availableSourceList.add(holder[0]);
                    serverFetchedLink.add(holder);
                }
            }

            @Override
            public void onPostExecute() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        progressBar.startAnimation(AnimationUtils.loadAnimation(ReadActivity.this, android.R.anim.fade_out));
                    }
                });


                String[] tempServerArray = availableSourceList.toArray(new String[availableSourceList.size()]);

                //Dialog setup & run below
                alertDialog.setSingleChoiceItems(tempServerArray, selectedItem[0], new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Update the selected item which is selected by the user so that it should be selected
                        // When user opens the dialog next time and pass the instance to setSingleChoiceItems method
                        selectedItem[0] = which;
                        String scraperName = availableSourceList.get(which);
                        //Note: 1st para for getChapterContent
                        readerServer = ScraperFactory.createScraper(scraperName);

                        for (String[] data: serverFetchedLink){
                            if (data[0].equalsIgnoreCase(scraperName)){
                                chapterUrl = data[1]; //Note: 2nd para for getChapterContent
                                break;
                            }
                        }

                        getChapterContentTask();
                        dialog.dismiss();
                    }
                });
                // Set the negative button if the user is not interested to select or change already selected item
                alertDialog.setNegativeButton("Cancel", (dialog, which) -> {

                });

                // Create and build the AlertDialog instance with the AlertDialog builder instance
                AlertDialog customAlertDialog = alertDialog.create();

                // Show the alert dialog when the button is clicked
                customAlertDialog.show();
            }
        }.execute();
    }

    //NOTE:-----------------------------------------------------------------------
    //NOTE: Some Listener

    View.OnClickListener prevBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (previousChapterUrl== null) {
                Toast.makeText(ReadActivity.this, "Không có chương trước", Toast.LENGTH_SHORT).show();
                return;
            }
            chapterUrl = previousChapterUrl;
            getChapterContentTask();
        }
    };

    View.OnClickListener nextBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (nextChapterUrl == null) {
                Toast.makeText(ReadActivity.this, "Không có chương tiếp", Toast.LENGTH_SHORT).show();
                return;
            }
            chapterUrl = nextChapterUrl;
            getChapterContentTask();
        }
    };
}