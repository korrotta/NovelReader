package com.softwaredesign.novelreader;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class DetailActivity extends AppCompatActivity {

    private ImageView detailImage;
    private TextView detailName, detailAuthor, detailDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        detailImage = findViewById(R.id.detailImage);
        detailName = findViewById(R.id.detailName);
        detailAuthor = findViewById(R.id.detailAuthor);
        detailDescription = findViewById(R.id.detailDescription);

        // Get info from selected novel
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String ImageUrl, NovelUrl;
            ImageUrl = bundle.getString("ImageUrl");
            NovelUrl = bundle.getString("NovelUrl");
            Picasso.get().load(ImageUrl).placeholder(R.drawable.logo).into(detailImage);
            detailName.setText(bundle.getString("Name"));
        }
    }
}