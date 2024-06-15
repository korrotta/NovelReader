package com.softwaredesign.novelreader.Activities;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.FragmentManager;

import com.example.exporter_library.IChapterExportHandler;
import com.example.scraper_library.INovelScraper;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.softwaredesign.novelreader.Adapters.ExporterSpinnerAdapter;
import com.softwaredesign.novelreader.BackgroundTask;
import com.softwaredesign.novelreader.Fragments.ChapterListFragment;
import com.softwaredesign.novelreader.Fragments.SettingsDialogFragment;
import com.softwaredesign.novelreader.Global.GlobalConfig;
import com.softwaredesign.novelreader.Global.ReusableFunction;
import com.softwaredesign.novelreader.Models.ChapterContentModel;
import com.softwaredesign.novelreader.R;
import com.softwaredesign.novelreader.ScraperFactory.ScraperFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class ReadActivity extends AppCompatActivity {

    private TextView chapterNameTV, chapterContentTV, searchStatusTV, novelNameTV, chapterTitleTV, serverNameTV;
    private ScrollView contentScrollView;
    private ImageView saveChapterIV, prevChapterIV, nextChapterIV, findInChapterIV, settingsIV, serverIV;
    private ImageButton searchUpIV, searchDownIV, searchCloseButton;
    private EditText searchEditText;
    private ProgressBar progressBar;
    private BottomAppBar bottomAppBar;
    private AppBarLayout topAppBar;
    private LinearLayout searchLayout;
    private AlertDialog.Builder alertDialog;

    private String chapterUrl, chapterTitle, content, novelName, nextChapterUrl, previousChapterUrl;
    private List<Integer> searchResults = new ArrayList<>();
    private int currentSearchIndex = 0;
    final int[] selectedItem = new int[1];
    private List<String> availableSourceList;

    private SharedPreferences sharedPreferences;

    //NOTE: Local reader server
    private INovelScraper readerServer;

    //NOTE: String variable holder
    private static ArrayList<String[]> serverFetchedLink;

    private final Handler handler = new Handler(Looper.getMainLooper());

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read);

        // Initialize some vital variables
        initializeVariables();

        // Initialize view
        InitializeView();

        // Load preferences
        loadPreferences();

        // Get chapterUrl from previous intent
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            chapterUrl = bundle.getString("ChapterUrl");
            assert chapterUrl != null;
            Log.d("BUNDLE CHAPTER URL", chapterUrl);
        }

        // Execute chapter content
        getChapterContentTask();

        // Handle Listeners
        handleListeners();
    }

    private void handleListeners() {
        // Handle Chapter Navigation Buttons
        prevChapterIV.setOnClickListener(prevBtnClickListener);
        nextChapterIV.setOnClickListener(nextBtnClickListener);

        // Handle Server Source Button
        serverIV.setOnClickListener(serverIVClickListener);

        // Handle Find text in page Button
        findInChapterIV.setOnClickListener(findInChapterIVClickListener);

        // Hanlde search EditText
        searchEditText.setOnEditorActionListener(searchEditTextListener);

        // Hanlde searchUp Button
        searchUpIV.setOnClickListener(searchUpIVClickListener);

        // Hanlde searchDown Button
        searchDownIV.setOnClickListener(searchDownIVClickListener);

        // Hanlde searchClose Button
        searchCloseButton.setOnClickListener(searchCloseButtonListener);

        // Handle chapterName TextView
        chapterNameTV.setOnClickListener(chapterNameTVListener);

        // Hanlde chapterList Button
        saveChapterIV.setOnClickListener(saveChapterTVListener);

        // Handle Settings Button
        settingsIV.setOnClickListener(settingsIVListener);
    }

    private void loadPreferences() {
        sharedPreferences = this.getSharedPreferences("ReadSetting", Context.MODE_PRIVATE);
    }

    private void initializeVariables() {
        if (serverFetchedLink == null) {
            serverFetchedLink = new ArrayList<>();
        }

        availableSourceList = new ArrayList<>();
        readerServer = GlobalConfig.Global_Current_Scraper.clone();
        selectedItem[0] = -1; // Initialize selected item for the dialog
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        navigateToDetail();
    }

    // search text in content function
    @SuppressLint("SetTextI18n")
    private void performSearch() {
        String query = searchEditText.getText().toString();
        String plainTextContent  = HtmlCompat.fromHtml(content, HtmlCompat.FROM_HTML_MODE_LEGACY).toString();

        searchResults = new ArrayList<>();
        int index = plainTextContent .indexOf(query);
        while (index >= 0) {
            searchResults.add(index);
            index = plainTextContent .indexOf(query, index + query.length());
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
        if (searchResults.isEmpty()) {
            clearHighlight();
            return;
        }
        int highlightColor = ContextCompat.getColor(this, R.color.saddle_brown);

        Spannable spannable = new SpannableString(HtmlCompat.fromHtml(content, HtmlCompat.FROM_HTML_MODE_LEGACY));

        for (int i = 0; i < searchResults.size(); i++) {
            spannable.setSpan(new BackgroundColorSpan(highlightColor), searchResults.get(i), searchResults.get(i) + searchText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        chapterContentTV.setText(spannable);

        highlightTextWithCurrentHighlight(searchResults.get(currentSearchIndex));
    }

    // clear all highlight text function
    private void clearHighlight() {
        chapterContentTV.setText(HtmlCompat.fromHtml(content, HtmlCompat.FROM_HTML_MODE_LEGACY));
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
                    int scrollViewHeight = contentScrollView.getHeight() - 2*bottomAppBar.getHeight() - 2*searchEditText.getHeight();

                    // Only scroll if the result is not fully visible
                    if (forceScroll || y < scrollY || y > (scrollY + scrollViewHeight - chapterContentTV.getLineHeight())) {
                        int targetScrollY = y - scrollViewHeight / 2 + chapterContentTV.getLineHeight() / 2;
                        contentScrollView.smoothScrollTo(0, targetScrollY);
                    }
                }
            });

            searchStatusTV.setText((currentSearchIndex + 1) + "/" + searchResults.size());
        }
    }


    // Highlight text current result, with other color
    private void highlightTextWithCurrentHighlight(int currentPosition) {
        int highlightColor = ContextCompat.getColor(this, R.color.saddle_brown);

        int currentHighlightColor = ContextCompat.getColor(this, R.color.slate_blue);;
        String plainTextContent = HtmlCompat.fromHtml(content, HtmlCompat.FROM_HTML_MODE_LEGACY).toString();
        Spannable spannable = new SpannableString(plainTextContent);

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
        novelNameTV = findViewById(R.id.novelNameRead);
        chapterTitleTV = findViewById(R.id.chapterTitleRead);
        serverNameTV = findViewById(R.id.serverNameRead);
        chapterNameTV = findViewById(R.id.chapterNameRead);
        chapterContentTV = findViewById(R.id.chapterContentRead);
        saveChapterIV = findViewById(R.id.saveChapterRead);
        prevChapterIV = findViewById(R.id.previousChapterRead);
        nextChapterIV = findViewById(R.id.nextChapterRead);
        findInChapterIV = findViewById(R.id.findTextRead);
        settingsIV = findViewById(R.id.settingsRead);
        bottomAppBar = findViewById(R.id.bottomNavRead);
        topAppBar = findViewById(R.id.topNavRead);
        progressBar = findViewById(R.id.readProgressBar);
        serverIV = findViewById(R.id.serverSourceRead);
        searchEditText = findViewById(R.id.search_edit_text);
        searchLayout = findViewById(R.id.search_layout);
        searchDownIV = findViewById(R.id.search_down);
        searchUpIV = findViewById(R.id.search_up);
        searchStatusTV = findViewById(R.id.search_status);
        contentScrollView = findViewById(R.id.contentScrollView);
        searchCloseButton = findViewById(R.id.search_close);

        //Gone View
        if (nextChapterIV.getVisibility() == View.VISIBLE || prevChapterIV.getVisibility() == View.VISIBLE)
            return;

        nextChapterIV.setVisibility(View.GONE);
        prevChapterIV.setVisibility(View.GONE);
    }

    // get Chapter Content Task function
    private void getChapterContentTask() {
        new BackgroundTask(ReadActivity.this) {
            @Override
            public void onPreExecute() {
                handler.post(() -> {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.startAnimation(AnimationUtils.loadAnimation(ReadActivity.this, android.R.anim.fade_in));
                });
            }

            @Override
            public void doInBackground() {
                // Fetch from chapterURL
                Object item = readerServer.getChapterContent(chapterUrl);

                // Ensure datatype matched
                ChapterContentModel ccm;
                if (item instanceof ChapterContentModel) {
                    ccm = (ChapterContentModel) item;
                } else {
                    String[] holder = (String[]) item;
                    ccm = new ChapterContentModel(holder[0], holder[1], holder[2], holder[3]);
                }


                chapterTitle = ccm.getChapterName();
                content = ccm.getContent();
                novelName = ccm.getNovelName();

                nextChapterUrl = readerServer.getNextChapterUrl(chapterUrl);
                previousChapterUrl = readerServer.getPreviousChapterUrl(chapterUrl);
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onPostExecute() {
                handler.post(() -> {
                    progressBar.setVisibility(View.GONE);
                    progressBar.startAnimation(AnimationUtils.loadAnimation(ReadActivity.this, android.R.anim.fade_out));
                });

                // Update UI after fetch
                novelNameTV.setText(novelName);
                chapterTitleTV.setText(chapterTitle);
                serverNameTV.setText("Server: " + readerServer.getSourceName());
                chapterNameTV.setText(chapterTitle);
                chapterContentTV.setText(HtmlCompat.fromHtml(content, HtmlCompat.FROM_HTML_MODE_LEGACY));

                // Set Bold for these 3 line
                novelNameTV.setTypeface(null, Typeface.BOLD);
                chapterTitleTV.setTypeface(null, Typeface.BOLD);
                serverNameTV.setTypeface(null, Typeface.BOLD);

                applyFontChange();
                applyTextSizeChange();
                applyThemeChange();
                applyLineSpacingChange();

                nextChapterIV.setVisibility(View.VISIBLE);
                prevChapterIV.setVisibility(View.VISIBLE);

                contentScrollView.fullScroll(ScrollView.FOCUS_UP);
            }
        }.execute();
    }

    // Apply Font Change Function
    private void applyFontChange() {
        String font = sharedPreferences.getString("font", "Palatino");
        Typeface typeface = null;
        switch (font) {
            case "Palatino":
                typeface = ResourcesCompat.getFont(this, R.font.palatino);
                break;
            case "Times":
                typeface = ResourcesCompat.getFont(this, R.font.times);
                break;
            case "Arial":
                typeface = ResourcesCompat.getFont(this, R.font.arial);
                break;
            case "Georgia":
                typeface = ResourcesCompat.getFont(this, R.font.georgia);
                break;
        }
        if (typeface != null) {
            chapterContentTV.setTypeface(typeface);
        }
    }


    // apply text size change function
    private void applyTextSizeChange() {
        int textSize = sharedPreferences.getInt("textSize", 22);
        chapterContentTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
    }

    // apply Theme Change Function
    private void applyThemeChange() {
        String theme = sharedPreferences.getString("theme", "dark");

        if (theme.equals("light")) {
            serverNameTV.setTextColor(ContextCompat.getColor(this, R.color.black));
            chapterTitleTV.setTextColor(ContextCompat.getColor(this, R.color.black));
            novelNameTV.setTextColor(ContextCompat.getColor(this, R.color.black));
            chapterNameTV.setTextColor(ContextCompat.getColor(this, R.color.black));
            chapterContentTV.setTextColor(ContextCompat.getColor(this, R.color.black));
            topAppBar.setBackgroundColor(ContextCompat.getColor(this, R.color.floral_white));
            bottomAppBar.setBackgroundColor(ContextCompat.getColor(this, R.color.floral_white));
            prevChapterIV.setColorFilter(ContextCompat.getColor(this, R.color.black));
            nextChapterIV.setColorFilter(ContextCompat.getColor(this, R.color.black));
            saveChapterIV.setColorFilter(ContextCompat.getColor(this, R.color.black));
            serverIV.setColorFilter(ContextCompat.getColor(this, R.color.black));
            settingsIV.setColorFilter(ContextCompat.getColor(this, R.color.black));
            findInChapterIV.setColorFilter(ContextCompat.getColor(this, R.color.black));
            contentScrollView.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
        } else {
            serverNameTV.setTextColor(ContextCompat.getColor(this, R.color.white));
            chapterTitleTV.setTextColor(ContextCompat.getColor(this, R.color.white));
            novelNameTV.setTextColor(ContextCompat.getColor(this, R.color.white));
            chapterNameTV.setTextColor(ContextCompat.getColor(this, R.color.white));
            chapterContentTV.setTextColor(ContextCompat.getColor(this, R.color.white));
            topAppBar.setBackgroundColor(ContextCompat.getColor(this, R.color.backgroundMaterialDark));
            bottomAppBar.setBackgroundColor(ContextCompat.getColor(this, R.color.backgroundMaterialDark));
            prevChapterIV.setColorFilter(ContextCompat.getColor(this, R.color.white));
            nextChapterIV.setColorFilter(ContextCompat.getColor(this, R.color.white));
            saveChapterIV.setColorFilter(ContextCompat.getColor(this, R.color.white));
            serverIV.setColorFilter(ContextCompat.getColor(this, R.color.white));
            settingsIV.setColorFilter(ContextCompat.getColor(this, R.color.white));
            findInChapterIV.setColorFilter(ContextCompat.getColor(this, R.color.white));
            contentScrollView.setBackgroundColor(ContextCompat.getColor(this, androidx.cardview.R.color.cardview_dark_background));
        }
    }

    // Apply Line Spacing Change function
    private void applyLineSpacingChange() {
        float lineSpacing = sharedPreferences.getFloat("lineSpacing", 1.0f);
        chapterContentTV.setLineSpacing(1.0f, lineSpacing);
    }

    // Change chapterUrl into novelUrl
    private String chapterToNovelUrl(String chapterUrl) {
        try {
            // Create a URL object
            java.net.URL urlObj = new java.net.URL(chapterUrl);

            // Get the protocol
            String protocol = urlObj.getProtocol();

            // Get the host
            String host = urlObj.getHost();

            // Get the path
            String path = urlObj.getPath();

            // Remove the last segment of the path
            if (path != null && path.length() > 0 && path.charAt(path.length() - 1) == '/') {
                path = path.substring(0, path.length() - 1);
            }
            int lastIndex = path.lastIndexOf('/');
            if (lastIndex != -1) {
                path = path.substring(0, lastIndex + 1);
            }

            // Combine protocol, host, and path to get the base URL
            return protocol + "://" + host + path;
        } catch (java.net.MalformedURLException e) {
            // Handle the exception if the URL is malformed
            e.printStackTrace();
            return null;
        }
    }

    private void navigateToDetail() {
        // Change chapterUrl to novelUrl
        String novelUrl = chapterToNovelUrl(chapterUrl);
        Log.d("CHAPTERURL", chapterUrl);
        Log.d("NOVELURL", novelUrl);
        // Go back to chapter list
        ReusableFunction.ChangeActivityWithString(ReadActivity.this, DetailActivity.class, "NovelUrl", novelUrl);
        finish();
    }

    // Get Content
    private void getContentFromNameAndChapterTask() {

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
                        Log.d("Reach progress bar", " Bar");
                        progressBar.setVisibility(View.VISIBLE);
                        progressBar.startAnimation(AnimationUtils.loadAnimation(ReadActivity.this, android.R.anim.fade_in));
                    }
                });
            }

            @Override
            public void doInBackground() {
                if (serverFetchedLink == null) serverFetchedLink = new ArrayList<>();
                serverFetchedLink.clear();

                for (INovelScraper scraper : GlobalConfig.Global_Source_List) {
                    if (scraper.getSourceName().equalsIgnoreCase(readerServer.getSourceName()))
                        continue;

                    Object item = scraper.getContentFromNameAndChapName(novelName, chapterTitle);
                    if (item == null) continue;

                    //Ensure datatype matched
                    ChapterContentModel ccm;
                    if (item instanceof ChapterContentModel) {
                        ccm = (ChapterContentModel) item;
                    } else {
                        //Note: inner holder
                        String[] holder = (String[]) item;
                        ccm = new ChapterContentModel(holder[0], holder[1], holder[2], holder[3]);
                    }

                    String[] holder = new String[2];
                    //Note: holder init-ed here
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
                        Log.d("End progress bar", " Bar");
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

                        for (String[] data : serverFetchedLink) {
                            if (data[0].equalsIgnoreCase(scraperName)) {
                                chapterUrl = data[1]; //Note: 2nd para for getChapterContent
                                break;
                            }
                        }

                        getChapterContentTask();
                        dialog.dismiss();
                    }
                });
                // Set the negative button if the user is not interested to select or change already selected item
                alertDialog.setNegativeButton("Hủy", (dialog, which) -> {

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
            if (previousChapterUrl == null) {
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

    View.OnClickListener serverIVClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d("clicked?", "clicked");
            // AlertDialog builder instance to build the alert dialog
            alertDialog = new AlertDialog.Builder(ReadActivity.this);

            // Set the custom icon to the alert dialog
            alertDialog.setIcon(R.drawable.server);

            // Title of the alert dialog
            alertDialog.setTitle("Chọn Server Nguồn: ");
            getContentFromNameAndChapterTask();
        }
    };

    View.OnClickListener findInChapterIVClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            searchLayout.setVisibility(View.VISIBLE);
            searchEditText.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT);
        }
    };

    TextView.OnEditorActionListener searchEditTextListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                String searchText = searchEditText.getText().toString();
                if (!searchText.isEmpty()) {
                    performSearch();
                    highlightText(searchText);
                }
                return true;
            }
            return false;
        }
    };

    View.OnClickListener searchUpIVClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!searchResults.isEmpty()) {
                currentSearchIndex = (currentSearchIndex - 1 + searchResults.size()) % searchResults.size();
                scrollToSearchResult(false);
            }
        }
    };

    View.OnClickListener searchDownIVClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!searchResults.isEmpty()) {
                currentSearchIndex = (currentSearchIndex + 1) % searchResults.size();
                scrollToSearchResult(false);
            }
        }
    };

    View.OnClickListener searchCloseButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            searchResults.clear();
            searchEditText.setText("");
            searchLayout.setVisibility(View.GONE);
            clearHighlight();
            searchUpIV.setAlpha(searchResults.size() > 1 ? 1.0f : 0.2f);
            searchDownIV.setAlpha(searchResults.size() > 1 ? 1.0f : 0.2f);
            searchUpIV.setClickable(searchResults.size() > 1);
            searchDownIV.setClickable(searchResults.size() > 1);
            searchStatusTV.setText("");
        }
    };

    View.OnClickListener chapterNameTVListener = v -> {
        // Switch to DetailNovel with appropriate novelUrl
        navigateToDetail();
    };

    View.OnClickListener saveChapterTVListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // AlertDialog builder instance to build the alert dialog
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(ReadActivity.this);

            // Inflate the custom layout for the spinner
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_spinner, null);
            alertDialog.setView(dialogView);

            // set the custom icon to the alert dialog
            alertDialog.setIcon(R.drawable.logo);

            // title of the alert dialog
            alertDialog.setTitle("Tải xuống chương với định dạng");

            // Get the spinner from the custom layout
            Spinner spinner = dialogView.findViewById(R.id.saveChapterSpinner);

            // List of the items to be displayed in the spinner
            final String[] listItems = new String[]{"EPUB", "PDF"};

            // Create an ArrayAdapter using the string array and a default spinner layout
            ExporterSpinnerAdapter adapter = new ExporterSpinnerAdapter(ReadActivity.this,
                    android.R.layout.simple_spinner_item,GlobalConfig.Global_Exporter_List);

            spinner.setAdapter(adapter);

            // Set the negative button if the user is not interested to select or change already selected item
            alertDialog.setNegativeButton("Cancel", (dialog, which) -> {
                // Dismiss the dialog
                dialog.dismiss();
            });

            // Set the positive button to confirm the selection
            alertDialog.setPositiveButton("OK", (dialog, which) -> {
                // Get the selected item
                IChapterExportHandler selectedItem = (IChapterExportHandler) spinner.getSelectedItem();
                // Handle the selected item
                String dir = ReadActivity.this.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath()+"/Export";

                File directory = ReusableFunction.MakeDirectory(dir, novelName);
                File typeDirectory = ReusableFunction.MakeDirectory(directory.getAbsolutePath(), selectedItem.getExporterName());

                selectedItem.exportChapter(content, typeDirectory, chapterTitle);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ReadActivity.this, "Done!", Toast.LENGTH_SHORT).show();
                    }
                }, 500);
            });

            // Create and build the AlertDialog instance with the AlertDialog builder instance
            AlertDialog customAlertDialog = alertDialog.create();

            // Show the alert dialog when the button is clicked
            customAlertDialog.show();
        }
    };

    View.OnClickListener settingsIVListener = v -> {
        FragmentManager fragmentManager = getSupportFragmentManager();
        SettingsDialogFragment settingsDialog = new SettingsDialogFragment();
        settingsDialog.show(fragmentManager, "settings_dialog");
    };

    @Override
    protected void onStop() {
        super.onStop();

        String[] data = new String[4]; //Note: 0 - server, 1 - name, 2 - chaptername, 3 - chapterUrl
        data[0] = readerServer.getSourceName();
        data[1] = novelName;
        data[2] = chapterTitle;
        data[3] = chapterUrl;

        File file = ReadActivity.this.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        File finalFile = new File(file, "lastrun.log");
        try (FileOutputStream fos = new FileOutputStream(finalFile)) {
            ObjectOutputStream out = new ObjectOutputStream(fos);
            out.writeObject(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}