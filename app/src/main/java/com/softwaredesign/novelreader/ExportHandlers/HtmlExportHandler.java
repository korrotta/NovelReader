package com.softwaredesign.novelreader.ExportHandlers;

import android.text.Spanned;
import android.util.Log;

import androidx.core.text.HtmlCompat;

import com.example.exporter_library.IChapterExportHandler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class HtmlExportHandler implements IChapterExportHandler {
    private final String EXPORTER_EXTENSION = "html";
    @Override
    public String getExporterName() {
        return null;
    }

    @Override
    public void exportChapter(String content, File directory, String filename) {
        File htmlFile = new File(directory, filename +".html");
        if (htmlFile.exists()) {
            Log.d("Exist", "Exisss");
            return;
        }

        Spanned htmlContent = HtmlCompat.fromHtml(content, HtmlCompat.FROM_HTML_MODE_LEGACY);

        try (FileOutputStream out = new FileOutputStream(htmlFile.getAbsolutePath())) {
            out.write(content.getBytes());
            Log.d("Done", "Done");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
