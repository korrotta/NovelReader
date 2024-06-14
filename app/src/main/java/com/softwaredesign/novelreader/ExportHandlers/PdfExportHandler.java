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

import com.softwaredesign.novelreader.Interfaces.IChapterExportHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.EventListener;

public class PdfExportHandler implements IChapterExportHandler {
    private final String EXPORTER_EXTENSION = "pdf";
    private final int WIDTH = 595;
    private final int HEIGHT = 842; //842
    private final int MARGIN = 20;
    private final int TEXT_SIZE = 16;

    @Override
    public String getExporterName() {
        return this.EXPORTER_EXTENSION;
    }

    @Override
    public void exportChapter(String content, File directory, String filename) {
        Spanned finalContent = HtmlCompat.fromHtml(content, HtmlCompat.FROM_HTML_MODE_LEGACY);

        PdfDocument pdfDocument = new PdfDocument();

        int startLine = 0;
        int yPosition = MARGIN;

        TextPaint textPaint = new TextPaint();
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(TEXT_SIZE);
        textPaint.setColor(0xFF000000); // Màu đen

        int totalLines = new StaticLayout(finalContent, textPaint, WIDTH - 2 * MARGIN, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false).getLineCount();
        printingLoop(finalContent, pdfDocument, startLine, textPaint, totalLines);

        File pdfFile = new File(directory, filename +".pdf");
        if (pdfFile.exists()) {
            Log.d("PDF EXIST", "YES");
            return;
        }
        try {
            pdfDocument.writeTo(new FileOutputStream(pdfFile));
            Log.d("PDF WRITE", "WRITTEN");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        pdfDocument.close();
    }

    private void printingLoop(Spanned finalContent, PdfDocument pdfDocument, int startLine, TextPaint textPaint, int totalLines) {
        while (startLine < totalLines) {
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(WIDTH, HEIGHT, pdfDocument.getPages().size() + 1).create();
            PdfDocument.Page page = pdfDocument.startPage(pageInfo);

            Canvas canvas = page.getCanvas();
            canvas.translate(MARGIN, MARGIN);

            StaticLayout staticLayout = new StaticLayout(finalContent, textPaint, WIDTH - 2 * MARGIN, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);

            int maxLinesPerPage = (HEIGHT - 2 * MARGIN)/(staticLayout.getLineBottom(1)) * 2;
            int endLine = Math.min(startLine + maxLinesPerPage, totalLines);
            StaticLayout pageLayout = new StaticLayout(finalContent.subSequence(staticLayout.getLineStart(startLine),
                    staticLayout.getLineEnd(endLine - 1)), textPaint, WIDTH - 2 * MARGIN, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
            pageLayout.draw(canvas);

            startLine = endLine;

            pdfDocument.finishPage(page);
        }
    }
}
