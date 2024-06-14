package com.softwaredesign.novelreader.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.softwaredesign.novelreader.Models.NovelModel;
import com.softwaredesign.novelreader.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class NovelSearchAdapter extends ArrayAdapter<NovelModel> {
    // Constructor
    public NovelSearchAdapter(Context context, ArrayList<NovelModel> novelModelArrayList) {
        super(context, R.layout.novel_search_item, novelModelArrayList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View view, @NonNull ViewGroup parent) {

        // Get the NovelModel object at the current position
        NovelModel novelModel = getItem(position);

        // Inflate the layout for each item if view is null
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.novel_search_item, parent, false);
        }

        // Find views within the layout and bind data to them
        ImageView novelSearchImage = view.findViewById(R.id.novelSearchImage);
        TextView novelSearchName = view.findViewById(R.id.novelSearchName);
        TextView novelSearchAuthor = view.findViewById(R.id.novelSearchAuthor);

        // Ensure novelModel is not null before setting data
        assert novelModel != null;

        // Use Picasso to load image from URL into ImageView
        Picasso.get().load(novelModel.getImageDesk()).into(novelSearchImage);
        // Set novel name and author to TextViews
        novelSearchName.setText(novelModel.getName());
        novelSearchAuthor.setText(novelModel.getAuthor());

        // Return the modified view for display
        return view;
    }

}
