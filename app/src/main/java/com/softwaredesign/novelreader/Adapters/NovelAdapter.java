package com.softwaredesign.novelreader.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.softwaredesign.novelreader.Activities.DetailActivity;
import com.softwaredesign.novelreader.Models.NovelModel;
import com.softwaredesign.novelreader.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class NovelAdapter extends RecyclerView.Adapter<NovelViewHolder> {

    // Context for inflating layout and starting activities
    private Context context;

    // List to hold novel data
    private List<NovelModel> novelList;

    // Set List to search result
    // Method to set the list to search results and notify the adapter of data changes
    public void setSearchList(List<NovelModel> novelSearchList) {
        this.novelList = novelSearchList;

        // Notify that the data set has changed
        notifyDataSetChanged();
    }


    // Constructor to initialize context and novel list
    public NovelAdapter(Context context, List<NovelModel> novelList) {
        this.context = context;
        this.novelList = novelList;
    }

    @NonNull
    @Override
    public NovelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        // Inflate the layout for each novel list item
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.novel_item, parent, false);

        // Return a new view holder instance
        return new NovelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NovelViewHolder holder, int position) {

        // Get the image URL of the current novel item
        String ImageUrl = novelList.get(position).getImageDesk();

        // Load the image into the ImageView using Picasso
        Picasso.get().load(ImageUrl).into(holder.recImage);

        // Send info to detail activity
        // Set an onClickListener to handle clicks on the novel item
        holder.recLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Create an intent to start DetailActivity
                Intent intent = new Intent(context, DetailActivity.class);

                // Pass the novel URL to the DetailActivity
                intent.putExtra("NovelUrl", novelList.get(holder.getAdapterPosition()).getUrl());

                // Start the DetailActivity
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {

        // Return 0 if the list is null
        if (novelList == null) return 0;

        // Return the size of the novel list
        return novelList.size();
    }
}

class NovelViewHolder extends RecyclerView.ViewHolder {

    // ImageView to display novel image
    ImageView recImage;

    // CardView layout for the novel item
    CardView recLayout;

    public NovelViewHolder(@NonNull View itemView) {
        super(itemView);

        // Find the ImageView and CardView in the layout
        recImage = itemView.findViewById(R.id.novelImage);
        recLayout = itemView.findViewById(R.id.novelLayout);


    }
}