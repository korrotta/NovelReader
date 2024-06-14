package com.softwaredesign.novelreader.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.scraper_library.INovelScraper;
import com.softwaredesign.novelreader.Interfaces.IChapterExportHandler;

import java.util.List;

public class ExporterSpinnerAdapter extends ArrayAdapter<IChapterExportHandler> {
    private Context context;
    private List<IChapterExportHandler> items;
    private LayoutInflater inflater;
    public ExporterSpinnerAdapter(@NonNull Context context, int resource, @NonNull List<IChapterExportHandler> objects) {
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

        IChapterExportHandler item = items.get(position);

        TextView label = (TextView) convertView.findViewById(android.R.id.text1);
        label.setTextColor(Color.WHITE);
        label.setText(item.getExporterName());

        return label;
    }
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if (items == null) return null;
        if (convertView == null){
            convertView = inflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);

        }

        IChapterExportHandler item = items.get(position);
        TextView label = (TextView) convertView.findViewById(android.R.id.text1);
        label.setTextColor(Color.WHITE);
        label.setText(item.getExporterName());

        return label;
    }
}
