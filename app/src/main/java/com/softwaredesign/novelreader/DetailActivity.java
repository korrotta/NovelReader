package com.softwaredesign.novelreader;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.softwaredesign.novelreader.databinding.ActivityDetailBinding;
import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class DetailActivity extends AppCompatActivity {

    private ImageView detailImage;
    private TextView detailName, detailAuthor, detailDescription;
    private String NovelUrl, NovelName, NovelImgUrl;
    private Novel novel;

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
            NovelName = bundle.getString("NovelName");
            NovelImgUrl = bundle.getString("NovelImageUrl");
            NovelUrl = bundle.getString("NovelUrl");

            // Get rid of chapter link if needed
            NovelUrl = NovelUrl.replaceFirst("chuong-1/", "");

            Log.d("NovelName", NovelName);
            Log.d("NovelImgUrl", NovelImgUrl);
            Log.d("NovelUrl", NovelUrl);

            novel = new Novel(NovelName, "Author", "Description", NovelImgUrl, NovelUrl);
        }

        // Handle get novel info
        Content content = new Content();
        content.execute();

        setDetailTextView(novel);
    }

    private String getNovelAuthorInRowNode(Element row) {
        return row.select("col-xs-12 col-sm-4 col-md-4 info-holder").select("info").attr("h3");
    }

    private String getNovelDescription(Element row) {
        return row.select("desc-text desc-text-full").text();
    }

    private class Content extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            getNovelDetail(NovelUrl);
            return null;
        }
    }

    private void getNovelDetail(String novelUrl) {
        try {
            Document document = Jsoup.connect(novelUrl)
                    .timeout(6000)
                    .get();

            Elements novelElements = document.getElementsByClass("col-xs-12 col-sm-12 col-md-9 col-truyen-main");
            
            for (Element row : novelElements) {
                // Get Author name and other infos
                String NovelAuthor = getNovelAuthorInRowNode(row);
                String NovelDescription = getNovelDescription(row);
                Log.d("NOVELAUTHOR", NovelAuthor);
                Log.d("NOVELDESC", NovelDescription);

                novel.setAuthor(NovelAuthor);
                novel.setDescription(NovelDescription);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void setDetailTextView(Novel selectedNovel) {
        detailName.setText(selectedNovel.getName());
        Picasso.get().load(selectedNovel.getImageUrl()).placeholder(R.drawable.logo).into(detailImage);
        detailAuthor.setText(selectedNovel.getAuthor());
        detailDescription.setText(selectedNovel.getDescription());
    }

}