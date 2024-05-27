package com.softwaredesign.novelreader.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.softwaredesign.novelreader.Models.ChapterModel;
import com.softwaredesign.novelreader.R;
import com.softwaredesign.novelreader.Activities.ReadActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChapterListItemAdapter extends RecyclerView.Adapter<ChapterListItemViewHolder> {

    private Context context;
    private List<ChapterModel> chapterList;

    public ChapterListItemAdapter(Context context, List<ChapterModel> chapterList) {
        this.context = context;
        this.chapterList = Collections.synchronizedList(new ArrayList<>(chapterList));
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
                intent.putExtra("ChapterUrl", chapterUrl);
                context.startActivity(intent);
            }
        });
    }

    public void updateList(List<ChapterModel> newList) {
        // Create a synchronized copy of the new list
        List<ChapterModel> synchronizedNewList = Collections.synchronizedList(new ArrayList<>(newList));

        // Use synchronized block to safely modify the chapterList
        synchronized (chapterList) {
            chapterList.clear();
            chapterList.addAll(synchronizedNewList);
        }

        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (chapterList == null) return 0;
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
