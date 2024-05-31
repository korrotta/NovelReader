package com.softwaredesign.novelreader.Activities;

import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListPopupWindow;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.softwaredesign.novelreader.BackgroundTask;
import com.softwaredesign.novelreader.Models.NovelDescriptionModel;
import com.softwaredesign.novelreader.NovelParsers.TruyenfullScraper;
import com.softwaredesign.novelreader.R;

public class ReadActivity extends AppCompatActivity {

    private TextView chapterNameTV, chapterContentTV;
    private View overlayView;
    private EditText searchEditText;
    private ImageView chapterListIV, prevChapterIV, nextChapterIV, findInChapterIV, settingsIV, serverIV;
    private String chapterUrl, chapterTitle, content;
    private TruyenfullScraper truyenfullScraper = new TruyenfullScraper();
    private ProgressBar progressBar;
    private BottomAppBar bottomAppBar;
    private Handler handler = new Handler(Looper.getMainLooper());

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

        getChapterContent.execute();

        // Handle Previous Chapter Button
        prevChapterIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        // Handle Next Chapter Button
        nextChapterIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
                searchEditText.setVisibility(View.VISIBLE);
                searchEditText.requestFocus();
                overlayView.setVisibility(View.VISIBLE);  // Hiển thị overlay khi EditText hiển thị
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        overlayView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchEditText.setVisibility(View.GONE);
                overlayView.setVisibility(View.GONE);  // Ẩn overlay khi nhấp vào
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
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
        overlayView = findViewById(R.id.overlay_view);
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
            content = truyenfullScraper.chapterContent(chapterUrl);
            Log.d("FETCHED CONTENT", content);
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
            chapterContentTV.setText(content);
        }
    };

}