package com.softwaredesign.novelreader;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.softwaredesign.novelreader.Models.NovelModel;
import com.softwaredesign.novelreader.SourceHandler.TruyenfullParser;
import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class DetailActivity extends AppCompatActivity {

    private ImageView detailImage;
    private TextView detailName, detailAuthor, detailDescription;
    private String NovelUrl;
    private TruyenfullParser truyenfullParser = new TruyenfullParser();
    private static String data[] = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        detailImage = findViewById(R.id.detailImage);
        detailName = findViewById(R.id.detailName);
        detailAuthor = findViewById(R.id.detailAuthor);
        detailDescription = findViewById(R.id.detailDescription);


        // Get novelUrl from selected novel
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {

            NovelUrl = bundle.getString("NovelUrl");

            NovelUrl = NovelUrl.replace("chuong-1/", "");
            Log.d("Url", NovelUrl.toString());

            DetailActivity.Content content = new DetailActivity.Content();
            content.execute();

        }
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
            DetailActivity.this.setUIData();
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