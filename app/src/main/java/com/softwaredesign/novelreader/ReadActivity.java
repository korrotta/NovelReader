package com.softwaredesign.novelreader;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class ReadActivity extends AppCompatActivity {

    private TextView novelNameTV, chapterNameTV;
    private ImageView chapterListIV, prevChapterIV, nextChapterIV, findInChapterIV;
    private Button server1Button, server2Button, themeButton, fontButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read);

        InitializeView();

    }

    private void InitializeView() {
        novelNameTV = findViewById(R.id.novelNameRead);
        chapterNameTV = findViewById(R.id.chapterNameRead);
        chapterListIV = findViewById(R.id.chapterListRead);
        prevChapterIV = findViewById(R.id.previousChapterRead);
        nextChapterIV = findViewById(R.id.nextChapterRead);
        findInChapterIV = findViewById(R.id.findTextRead);
        server1Button = findViewById(R.id.server1Read);
        server2Button = findViewById(R.id.server2Read);
        themeButton = findViewById(R.id.themeRead);
        fontButton = findViewById(R.id.fontFormatRead);
    }
}