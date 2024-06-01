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

    // Context for inflating layout and starting activities
    private Context context;

    // List to hold chapter data
    private List<ChapterModel> chapterList;


    // Constructor to initialize context and chapter list
    public ChapterListItemAdapter(Context context, List<ChapterModel> chapterList) {
        this.context = context;
        // Create a synchronized list
        this.chapterList = Collections.synchronizedList(new ArrayList<>(chapterList));
    }

    @NonNull
    @Override
    public ChapterListItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        // Inflate the layout for each chapter list item
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chapter_list_item, parent, false);
        return new ChapterListItemViewHolder(view);
        // Return a new view holder instance
    }

    @Override
    public void onBindViewHolder(@NonNull ChapterListItemViewHolder holder, int position) {

        // Get the chapter name and URL from the current chapter item
        String chapterName = chapterList.get(position).getChapterName();
        String chapterUrl = chapterList.get(position).getChapterUrl();


        // Set the chapter name text to the text view
        holder.recText.setText(chapterName);

        // Set an onClickListener to handle clicks on the chapter item
        holder.recText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Handle click on chapter list item
                Intent intent = new Intent(context, ReadActivity.class);

                // Pass the chapter URL to the ReadActivity
                intent.putExtra("ChapterUrl", chapterUrl);

                // Start the ReadActivity
                context.startActivity(intent);
            }
        });
    }


    // Method to update the chapter list with a new list
    public void updateList(List<ChapterModel> newList) {

        // Create a synchronized copy of the new list
        List<ChapterModel> synchronizedNewList = Collections.synchronizedList(new ArrayList<>(newList));

        // Use synchronized block to safely modify the chapterList
        synchronized (chapterList) {
            // Clear the current list
            chapterList.clear();

            // Add all items from the new list
            chapterList.addAll(synchronizedNewList);
        }

        // Notify the adapter that the data set has changed
        notifyDataSetChanged();

    }

    @Override
    public int getItemCount() {

        // Return 0 if the list is null
        if (chapterList == null) return 0;

        // Return the size of the chapter list
        return chapterList.size();
    }

}

class ChapterListItemViewHolder extends RecyclerView.ViewHolder {

    // TextView to display chapter name
    TextView recText;

    public ChapterListItemViewHolder(@NonNull View itemView) {
        super(itemView);

        // Find the TextView in the layout
        recText = itemView.findViewById(R.id.chapterName);

    }
}
