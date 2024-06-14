package com.softwaredesign.novelreader.ExportHandlers;

import android.text.Spanned;
import android.util.Log;

import androidx.core.text.HtmlCompat;

import com.softwaredesign.novelreader.Interfaces.IChapterExportHandler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class TxtExportHandler implements IChapterExportHandler {
    private final String EXPORTER_EXTENSION = "html";
    @Override
    public String getExporterName() {
        return EXPORTER_EXTENSION;
    }

    @Override
    public void exportChapter(String content, File directory, String filename) {
        File htmlFile = new File(directory, filename +".txt");
        if (htmlFile.exists()) {
            Log.d("Exist", "Exisss");
            return;
        }

        Spanned htmlContent = HtmlCompat.fromHtml(content, HtmlCompat.FROM_HTML_MODE_LEGACY);
        String finalContent = HtmlCompat.toHtml(htmlContent, HtmlCompat.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL);
        finalContent = finalContent.replaceAll("<br>", "\n");

        try (FileOutputStream out = new FileOutputStream(htmlFile.getAbsolutePath())) {
            out.write(finalContent.getBytes());
            Log.d("Done", "Done");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
