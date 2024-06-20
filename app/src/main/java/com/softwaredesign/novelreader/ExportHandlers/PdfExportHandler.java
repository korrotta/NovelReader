package com.softwaredesign.novelreader.ExportHandlers;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import android.text.Layout;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;

import androidx.core.content.res.ResourcesCompat;
import androidx.core.text.HtmlCompat;

import com.example.exporter_library.IChapterExportHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.EventListener;

public class PdfExportHandler implements IChapterExportHandler {
    private final String EXPORTER_EXTENSION = "pdf";
    private final int WIDTH = 595;
    private final int HEIGHT = 842; //842 // Standard A4 size in points (1 point = 1/72 inch)
    private final int MARGIN = 20; // Margin in points
    private final int TEXT_SIZE = 16; // Font size in points

    @Override
    public String getExporterName() {
        return this.EXPORTER_EXTENSION;
    }

    @Override
    public void exportChapter(String content, File directory, String filename) {
        // Convert content to Spanned HTML format
        Spanned finalContent = HtmlCompat.fromHtml(content, HtmlCompat.FROM_HTML_MODE_LEGACY);

        // Create a new PdfDocument
        PdfDocument pdfDocument = new PdfDocument();

        // Initialize variables for the printing loop
        int startLine = 0;
        int yPosition = MARGIN;

        // Create a TextPaint object for text rendering
        TextPaint textPaint = new TextPaint();
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(TEXT_SIZE);
        textPaint.setColor(0xFF000000); // Black color

        // Get total lines in the content using StaticLayout
        int totalLines = new StaticLayout(finalContent, textPaint, WIDTH - 2 * MARGIN, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false).getLineCount();
        // Perform the printing loop to add content to pages
        printingLoop(finalContent, pdfDocument, startLine, textPaint, totalLines);

        // Create the PDF file
        File pdfFile = new File(directory, filename +".pdf");
        if (pdfFile.exists()) {
            Log.d("PDF EXIST", "YES");
            return; // If PDF file already exists, log and return
        }
        try {
            // Write the PDF document to the file
            pdfDocument.writeTo(new FileOutputStream(pdfFile));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Close the PDF document
        pdfDocument.close();
    }

    // Method to handle the printing loop for creating pages
    private void printingLoop(Spanned finalContent, PdfDocument pdfDocument, int startLine, TextPaint textPaint, int totalLines) {
        while (startLine < totalLines) {
            // Create PageInfo for the page
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(WIDTH, HEIGHT, pdfDocument.getPages().size() + 1).create();
            PdfDocument.Page page = pdfDocument.startPage(pageInfo);

            // Get Canvas from the page
            Canvas canvas = page.getCanvas();
            canvas.translate(MARGIN, MARGIN); // Apply margin to the canvas

            // Create a StaticLayout for the page content
            StaticLayout staticLayout = new StaticLayout(finalContent, textPaint, WIDTH - 2 * MARGIN, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);

            // Calculate maximum lines per page based on page height and text layout
            int maxLinesPerPage = (HEIGHT - 2 * MARGIN)/(staticLayout.getLineBottom(1)) * 2;
            // Calculate end line for the current page
            int endLine = Math.min(startLine + maxLinesPerPage, totalLines);
            // Create a StaticLayout for the current page content and draw it on the canvas
            StaticLayout pageLayout = new StaticLayout(finalContent.subSequence(staticLayout.getLineStart(startLine),
                    staticLayout.getLineEnd(endLine - 1)), textPaint, WIDTH - 2 * MARGIN, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
            pageLayout.draw(canvas);

            // Update startLine to start from the next line for the next page
            startLine = endLine;

            // Finish the page and add it to the PDF document
            pdfDocument.finishPage(page);
        }
    }
}
