package com.example.exporter_library;

import java.io.File;

public interface IChapterExportHandler {
    String getExporterName();
    void exportChapter(String content, File directory, String filename);
}
