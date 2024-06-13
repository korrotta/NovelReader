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
        if (chapters == null) return null;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.custom_spinner_item, parent, false);
        }

        ChapterModel item = chapters.get(position);

        TextView label = (TextView) convertView.findViewById(R.id.exportSpinnerText);
        label.setTextColor(Color.WHITE);
        label.setText(item.getChapterName());

        return label;
    }
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if (chapters == null) return null;
        if (convertView == null){
            convertView = inflater.inflate(R.layout.custom_spinner_dropdown_item, parent, false);
        }

        ChapterModel chapter = chapters.get(position);
        TextView label = (TextView) convertView.findViewById(R.id.exportSpinnerDropdownText);
        label.setTextColor(Color.WHITE);
        label.setText(chapter.getChapterName());

        return label;
    }

}
