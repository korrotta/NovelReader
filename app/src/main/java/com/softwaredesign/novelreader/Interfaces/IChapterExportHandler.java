package com.softwaredesign.novelreader.Interfaces;

import android.text.SpannedString;

import java.io.File;

public interface IChapterExportHandler {
    String getExporterName();
    void exportChapter(String content, File directory, String filename);
}

