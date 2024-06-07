package com.softwaredesign.novelreader.Adapters;

import android.content.Context;
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
    public NovelSearchAdapter(Context context, ArrayList<NovelModel> novelModelArrayList) {
        super(context, R.layout.novel_search_item, novelModelArrayList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View view, @NonNull ViewGroup parent) {

        NovelModel novelModel = getItem(position);

        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.novel_search_item, parent, false);
        }

        ImageView novelSearchImage = view.findViewById(R.id.novelSearchImage);
        TextView novelSearchName = view.findViewById(R.id.novelSearchName);
        TextView novelSearchAuthor = view.findViewById(R.id.novelSearchAuthor);

        assert novelModel != null;
        Picasso.get().load(novelModel.getImageDesk()).into(novelSearchImage);
        novelSearchName.setText(novelModel.getName());
        novelSearchAuthor.setText(novelModel.getAuthor());

        return view;
    }
}
