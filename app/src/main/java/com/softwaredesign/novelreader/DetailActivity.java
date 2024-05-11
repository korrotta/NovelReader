package com.softwaredesign.novelreader;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.softwaredesign.novelreader.Models.NovelModel;
import com.softwaredesign.novelreader.SourceHandler.TruyenfullParser;
import com.squareup.picasso.Picasso;

import org.checkerframework.checker.units.qual.C;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DetailActivity extends AppCompatActivity {

    private ImageView detailImage;
    private TextView detailName, detailAuthor, detailDescription;
    private String NovelUrl;
    private TruyenfullParser truyenfullParser = new TruyenfullParser();
    private static String data[] = null;
    private RecyclerView chapterListRV;
    private ChapterListItem chapterListItem;
    private ChapterListItemAdapter chapterListItemAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        detailImage = findViewById(R.id.detailImage);
        detailName = findViewById(R.id.detailName);
        detailAuthor = findViewById(R.id.detailAuthor);
        detailDescription = findViewById(R.id.detailDescription);
        chapterListRV = findViewById(R.id.chapterListRV);

        // Initialize Chapter List RecyclerView
        GridLayoutManager gridLayoutManager = new GridLayoutManager(DetailActivity.this, 1);
        chapterListRV.setLayoutManager(gridLayoutManager);

        // Get novelUrl from selected novel
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            NovelUrl = bundle.getString("NovelUrl");

            NovelUrl = NovelUrl.replace("chuong-1/", "");
            Log.d("Url", NovelUrl.toString());

            DetailActivity.Content content = new DetailActivity.Content();
            content.execute();

        }

        // TODO: Fetch Chapter List
        String[] chapterLists = {"Chapter 1", "Chapter 2"};
        String[] chapterUrls = {"ABC", "ABC"};

        ArrayList<ChapterListItem> chapterListItemArrayList = new ArrayList<>();
        chapterListItem = new ChapterListItem(chapterLists[0], chapterUrls[0]);
        chapterListItemArrayList.add(chapterListItem);
        chapterListItem = new ChapterListItem(chapterLists[1], chapterUrls[1]);
        chapterListItemArrayList.add(chapterListItem);

        chapterListItemAdapter = new ChapterListItemAdapter(DetailActivity.this, chapterListItemArrayList);
        chapterListRV.setAdapter(chapterListItemAdapter);

    }

    private void setUIData(){
        detailName.setText(data[0]);
        detailAuthor.setText(data[1]);
        detailDescription.setText(data[2]);
        Picasso.get().load(data[3]).placeholder(R.drawable.logo).into(detailImage);
    }

    private class Content extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            setUIData();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            data = truyenfullParser.novelDetailScrapping(NovelUrl);
            return null;
        }

    }

}