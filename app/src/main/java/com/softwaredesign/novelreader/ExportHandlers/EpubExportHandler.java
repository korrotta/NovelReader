package com.softwaredesign.novelreader.ExportHandlers;

import android.util.Log;

import com.softwaredesign.novelreader.Interfaces.IChapterExportHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.TOCReference;
import nl.siegmann.epublib.epub.EpubWriter;

public class EpubExportHandler implements IChapterExportHandler {
    private final String EXPORTER_EXTENSION = "epub";
    @Override
    public String getExporterName() {
        return EXPORTER_EXTENSION;
    }

    @Override
    public void exportChapter(String content, File directory, String filename) {

        File pdfFile = new File(directory, filename +".epub");
        if (pdfFile.exists()) return;

        Book book = new Book();
        content = "<html><head><meta charset=\\\"UTF-8\\\"></head><body>\"" + content+ "</body></html>\"";
        Resource resource = null;
        try {
            resource = new Resource(content.getBytes("UTF-8"), filename +".html");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        TOCReference chapter = book.addSection(filename, resource);
        book.getMetadata().addTitle("Sample Title");

        EpubWriter epubWriter = new EpubWriter();
        try (FileOutputStream fos = new FileOutputStream(pdfFile.getAbsolutePath())) {
            epubWriter.write(book, fos);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
