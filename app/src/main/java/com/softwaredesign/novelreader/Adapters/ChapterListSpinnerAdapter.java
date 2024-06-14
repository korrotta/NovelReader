package com.softwaredesign.novelreader.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.softwaredesign.novelreader.Models.ChapterModel;
import com.softwaredesign.novelreader.R;

import java.util.List;

public class ChapterListSpinnerAdapter extends ArrayAdapter<ChapterModel> {
    private Context context;
    private List<ChapterModel> chapters;
    private LayoutInflater inflater;

    public ChapterListSpinnerAdapter(@NonNull Context context, int resource, @NonNull List<ChapterModel> objects) {
        super(context, resource, objects);
        this.context = context;
        this.chapters = objects;
        this.inflater = LayoutInflater.from(context);
        setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        // Check if chapters list is null, return null if so
        if (chapters == null) return null;
        // Inflate the layout for the spinner item if convertView is null
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.custom_spinner_item, parent, false);
        }

        // Get the ChapterModel object at the current position
        ChapterModel item = chapters.get(position);

        // Find the TextView within the custom layout and set its properties
        TextView label = (TextView) convertView.findViewById(R.id.customSpinnerText);
        label.setTextColor(Color.WHITE); // Set text color to white
        label.setText(item.getChapterName()); // Set the text to display

        // Return the modified view
        return label;
    }
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        // Check if chapters list is null, return null if so
        if (chapters == null) return null;
        // Inflate the layout for the dropdown item if convertView is null
        if (convertView == null){
            convertView = inflater.inflate(R.layout.custom_spinner_dropdown_item, parent, false);
        }

        // Get the ChapterModel object at the current position
        ChapterModel chapter = chapters.get(position);

        // Find the TextView within the custom layout and set its properties
        TextView label = (TextView) convertView.findViewById(R.id.customSpinnerDropdownText);
        label.setTextColor(Color.WHITE); // Set text color to white
        label.setText(chapter.getChapterName()); // Set the text to display

        // Return the modified view
        return label;
    }

}
