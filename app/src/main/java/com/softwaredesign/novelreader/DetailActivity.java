package com.softwaredesign.novelreader;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

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
            detailImage.setImageResource(bundle.getInt("Image"));
            detailName.setText(bundle.getString("Name"));
            detailAuthor.setText(bundle.getString("Author"));
            detailDescription.setText(bundle.getString("Description"));
        }
    }
}