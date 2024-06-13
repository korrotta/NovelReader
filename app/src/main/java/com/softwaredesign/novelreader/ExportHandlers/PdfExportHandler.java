package com.softwaredesign.novelreader.ExportHandlers;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import android.text.Layout;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;

import androidx.core.text.HtmlCompat;

import com.softwaredesign.novelreader.Interfaces.IChapterExportHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.EventListener;

public class PdfExportHandler implements IChapterExportHandler {
    private final String EXPORTER_EXTENSION = "pdf";
    @Override
    public String getExporterName() {
        return this.EXPORTER_EXTENSION;
    }

    @Override
    public void exportChapter(String content) {
        Spanned finalContent = HtmlCompat.fromHtml(content, HtmlCompat.FROM_HTML_MODE_LEGACY);

        PdfDocument pdfDocument = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        TextPaint textPaint = new TextPaint();
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(14);
        textPaint.setColor(0xFF000000); // Màu đen

        StaticLayout staticLayout = new StaticLayout(finalContent, textPaint, canvas.getWidth() - 20,
                                                Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);

        canvas.save();
        canvas.translate(10,10);
        staticLayout.draw(canvas);
        canvas.restore();

        pdfDocument.finishPage(page);

        File pdfFile = new File(Environment.getExternalStorageDirectory(), "test.pdf");
        try {
            pdfDocument.writeTo(new FileOutputStream(pdfFile));
            Log.d("Success", pdfFile.getAbsolutePath());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        pdfDocument.close();
    }
}
