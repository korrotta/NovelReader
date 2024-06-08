package com.softwaredesign.novelreader.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.view.Gravity;
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
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.softwaredesign.novelreader.BackgroundTask;
import com.softwaredesign.novelreader.Scrapers.TruyenfullScraper;
import com.softwaredesign.novelreader.R;

import java.util.ArrayList;
import java.util.List;

public class ReadActivity extends AppCompatActivity {

    private TextView chapterNameTV, chapterContentTV, searchStatusTV;
    private View overlayView;
    private ScrollView contentScrollView;
    private EditText searchEditText;
    private ImageButton searchUpIV, searchDownIV, searchCloseButton;
    private LinearLayout search_layout;
    private ImageView chapterListIV, prevChapterIV, nextChapterIV, findInChapterIV, settingsIV, serverIV;
    private String chapterUrl, chapterTitle, content;
    private TruyenfullScraper truyenfullScraper = new TruyenfullScraper();
    private ProgressBar progressBar;
    private BottomAppBar bottomAppBar;
    private Handler handler = new Handler(Looper.getMainLooper());

    private List<Integer> searchResults = new ArrayList<>();
    private int currentSearchIndex = 0;

    private String nextChapterUrl, previousChapterUrl;


    // List of servers
    String[] servers = new String[]{"Server 1", "Server 2"};
    String selectedServer = servers[0]; // Initially, select the first server


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read);

        InitializeView();

        // Get Chapter Url from bundle
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            chapterUrl = bundle.getString("ChapterUrl");
            Log.d("Tag", "ChapterURL: " + chapterUrl);
        }



        //execute chapter content
        getChapterContent.execute();

        // Handle Previous Chapter Button
        prevChapterIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (previousChapterUrl== null) {
                    Toast.makeText(ReadActivity.this, "Không có chương trước", Toast.LENGTH_SHORT).show();
                    return;
                }
                chapterUrl = previousChapterUrl;
                getChapterContent.execute();
            }
        });

        // Handle Next Chapter Button
        nextChapterIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nextChapterUrl == null) {
                    Toast.makeText(ReadActivity.this, "Không có chương tiếp", Toast.LENGTH_SHORT).show();
                    return;
                }
                chapterUrl = nextChapterUrl;
                getChapterContent.execute();
            }
        });

        // Handle Server Source Button
        serverIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showServerMenu(v);
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

            }
        });

        // Handle Settings Button
        settingsIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

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

    private void showServerMenu(View view) {
        // Inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.server_items, null);

        // Create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true;
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        // Get a reference for the custom layout's widgets
        TextView server1 = popupView.findViewById(R.id.server_1);
        TextView server2 = popupView.findViewById(R.id.server_2);

        // Update the text and background color based on the selected server
        updateSelection(server1, server2);

        // Set click listener for the TextViews
        server1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle server 1 click
                selectedServer = servers[0];
                updateSelection(server1, server2);
                popupWindow.dismiss(); // Close the popup window
            }
        });

        server2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle server 2 click
                selectedServer = servers[1];
                updateSelection(server1, server2);
                popupWindow.dismiss(); // Close the popup window
            }
        });

        // Show the popup window above the ServerImageView
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int yOffset = location[1] - view.getHeight() - popupView.getMeasuredHeight();

        popupWindow.showAtLocation(view, Gravity.NO_GRAVITY, location[0] - 200, yOffset - 130);
    }

    private void updateSelection(TextView server1, TextView server2) {
        if (selectedServer.equals(servers[0])) {
            server1.setBackground(ContextCompat.getDrawable(this, R.drawable.selected_rounded_corner_bg));
            server1.setTextColor(ContextCompat.getColor(this, R.color.white));
            server2.setBackground(ContextCompat.getDrawable(this, R.drawable.rounded_corner_bg));
            server2.setTextColor(ContextCompat.getColor(this, R.color.black));
        } else if (selectedServer.equals(servers[1])) {
            server2.setBackground(ContextCompat.getDrawable(this, R.drawable.selected_rounded_corner_bg));
            server2.setTextColor(ContextCompat.getColor(this, R.color.white));
            server1.setBackground(ContextCompat.getDrawable(this, R.drawable.rounded_corner_bg));
            server1.setTextColor(ContextCompat.getColor(this, R.color.black));
        }
    }

    private void InitializeView() {
        chapterNameTV = findViewById(R.id.chapterNameRead);
        chapterContentTV = findViewById(R.id.chapterContentRead);
        chapterListIV = findViewById(R.id.chapterListRead);
        prevChapterIV = findViewById(R.id.previousChapterRead);
        nextChapterIV = findViewById(R.id.nextChapterRead);
        findInChapterIV = findViewById(R.id.findTextRead);
        serverIV = findViewById(R.id.serverSourceRead);
        settingsIV = findViewById(R.id.settingsRead);
        bottomAppBar = findViewById(R.id.bottomNavRead);
        progressBar = findViewById(R.id.readProgressBar);
        searchEditText = findViewById(R.id.search_edit_text);
        //overlayView = findViewById(R.id.overlay_view);
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

    private final BackgroundTask getChapterContent = new BackgroundTask(ReadActivity.this) {
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
            chapterTitle = truyenfullScraper.getChapterTitleAndName(chapterUrl);
            content = truyenfullScraper.getChapterContent(chapterUrl);
            ReadActivity.this.nextChapterUrl = truyenfullScraper.getNextChapterUrl(ReadActivity.this.chapterUrl);
            ReadActivity.this.previousChapterUrl = truyenfullScraper.getPreviousChapterUrl(ReadActivity.this.chapterUrl);
//            Log.d("FETCHED CONTENT", content);
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
    };

}