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

import com.example.scraper_library.INovelScraper;

import java.util.List;

public class ServerSpinnerAdapter extends ArrayAdapter<INovelScraper> {
    private Context context;
    private List<INovelScraper> items;
    private LayoutInflater inflater;

    // Constructor
    public ServerSpinnerAdapter(@NonNull Context context, int resource, @NonNull List<INovelScraper> objects) {
        super(context, resource, objects);
        this.context = context;
        this.items = objects;
        this.inflater = LayoutInflater.from(context);
        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    // Method to render each item in the spinner when it's not expanded
    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        // Check if items list is null, return null if so
        if (items == null) return null;
        // Inflate the layout for the spinner item if convertView is null
        if (convertView == null) {
            convertView = inflater.inflate(android.R.layout.simple_spinner_item, parent, false);
        }

        // Get the INovelScraper object at the current position
        INovelScraper item = items.get(position);

        // Find the TextView within the simple_spinner_item layout and set its properties
        TextView label = (TextView) convertView.findViewById(android.R.id.text1);
        label.setTextColor(Color.WHITE); // Set text color to white
        label.setText(item.getSourceName()); // Set the text to display using getSourceName()

        // Return the modified view
        return label;
    }
    // Method to render each item in the spinner's dropdown list when it's expanded
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        // Check if items list is null, return null if so
        if (items == null) return null;
        // Inflate the layout for the dropdown item if convertView is null
        if (convertView == null){
            convertView = inflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);

        }

        // Get the INovelScraper object at the current position
        INovelScraper item = items.get(position);
        // Find the TextView within the simple_spinner_dropdown_item layout and set its properties
        TextView label = (TextView) convertView.findViewById(android.R.id.text1);
        label.setTextColor(Color.WHITE); // Set text color to white
        label.setText(item.getSourceName()); // Set the text to display using getSourceName()

        // Return the modified view
        return label;
    }
}
