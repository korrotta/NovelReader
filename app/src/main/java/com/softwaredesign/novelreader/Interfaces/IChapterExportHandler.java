package com.softwaredesign.novelreader.Interfaces;

import android.text.SpannedString;

public interface IChapterExportHandler {
    String getExporterName();
    void exportChapter(String content);
}

