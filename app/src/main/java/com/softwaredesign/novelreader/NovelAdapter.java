package com.softwaredesign.novelreader;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class NovelAdapter extends RecyclerView.Adapter<NovelViewHolder> {
    private Context context;
    private List<Novel> novelList;

    // Set List to search result
    public void setSearchList(List<Novel> novelSearchList) {
        this.novelList = novelSearchList;
        notifyDataSetChanged();
    }

    public NovelAdapter(Context context, List<Novel> novelList) {
        this.context = context;
        this.novelList = novelList;
    }

    @NonNull
    @Override
    public NovelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.novel_item, parent, false);
        return new NovelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NovelViewHolder holder, int position) {
        String ImageUrl = novelList.get(position).getImageUrl();
        Picasso.get().load(ImageUrl).placeholder(R.drawable.logo).into(holder.recImage);

        // Send info to detail activity
        holder.recLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, DetailActivity.class);
                intent.putExtra("NovelName", novelList.get(holder.getAdapterPosition()).getName());
                intent.putExtra("NovelImageUrl", novelList.get(holder.getAdapterPosition()).getImageUrl());
                intent.putExtra("NovelUrl", novelList.get(holder.getAdapterPosition()).getNovelUrl());

                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return novelList.size();
    }
}

class NovelViewHolder extends RecyclerView.ViewHolder {

    ImageView recImage;
    CardView recLayout;

    public NovelViewHolder(@NonNull View itemView) {
        super(itemView);

        recImage = itemView.findViewById(R.id.novelImage);
        recLayout = itemView.findViewById(R.id.novelLayout);
    }
}