package com.softwaredesign.novelreader;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ChapterListItemAdapter extends RecyclerView.Adapter<ChapterListItemViewHolder> {

    private Context context;
    private List<ChapterListItem> chapterList;

    public ChapterListItemAdapter(Context context, List<ChapterListItem> chapterList) {
        this.context = context;
        this.chapterList = chapterList;
    }

    @NonNull
    @Override
    public ChapterListItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chapter_list_item, parent, false);
        return new ChapterListItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChapterListItemViewHolder holder, int position) {
        String chapterName = chapterList.get(position).getChapterName();
        String chapterUrl = chapterList.get(position).getChapterUrl();

        holder.recText.setText(chapterName);
        holder.recText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle click on chapter list item
                Intent intent = new Intent(context, ReadActivity.class);
                intent.putExtra("ChapterUrl", chapterList.get(holder.getAdapterPosition()).getChapterUrl());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return chapterList.size();
    }
}

class ChapterListItemViewHolder extends RecyclerView.ViewHolder {

    TextView recText;

    public ChapterListItemViewHolder(@NonNull View itemView) {
        super(itemView);

        recText = itemView.findViewById(R.id.chapterName);
    }
}
