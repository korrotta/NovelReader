package com.softwaredesign.novelreader.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.novelscraperfactory.INovelScraper;
import com.softwaredesign.novelreader.Models.NovelSourceModel;

import java.util.List;

public class ServerSpinnerAdapter extends ArrayAdapter<INovelScraper> {
    private Context context;
    private List<INovelScraper> items;
    private LayoutInflater inflater;

    public ServerSpinnerAdapter(@NonNull Context context, int resource, @NonNull List<INovelScraper> objects) {
        super(context, resource, objects);
        this.context = context;
        this.items = objects;
        this.inflater = LayoutInflater.from(context);
        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        if (items == null) return null;
        if (convertView == null) {
            convertView = inflater.inflate(android.R.layout.simple_spinner_item, parent, false);
        }

        INovelScraper item = items.get(position);

        TextView label = (TextView) convertView.findViewById(android.R.id.text1);
        label.setTextColor(Color.WHITE);
        label.setText(item.getSourceName());

        return label;
    }
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if (items == null) return null;
        if (convertView == null){
            convertView = inflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);

        }

        INovelScraper item = items.get(position);
        TextView label = (TextView) convertView.findViewById(android.R.id.text1);
        label.setTextColor(Color.WHITE);
        label.setText(item.getSourceName());

        return label;
    }
}
