package com.project.notepad.Utility;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.graphics.text.MeasuredText;
import android.os.Environment;
import android.print.PrintAttributes;
import android.print.pdf.PrintedPdfDocument;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.project.notepad.BuildConfig;
import com.project.notepad.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class GeneralUtility {
    private static final String TAG = "GeneralUtility";
    public static final String PDF_PUBLIC_DIRECTORY = "PDF";

    public static class StrictMode {
        public static void enableStrictModeWithAllDetection() {
            if (BuildConfig.DEBUG) {
                android.os.StrictMode.ThreadPolicy threadPolicy = new android.os.StrictMode.ThreadPolicy.Builder()
                        .detectAll()
                        .penaltyLog()
                        .build();
                android.os.StrictMode.setThreadPolicy(threadPolicy);
            }
        }

        public static void enableStrictModeWithNetworkDetectionOnly() {
            if (BuildConfig.DEBUG) {
                android.os.StrictMode.ThreadPolicy threadPolicy = new android.os.StrictMode.ThreadPolicy.Builder()
                        .detectNetwork()
                        .penaltyLog()
                        .build();
                android.os.StrictMode.setThreadPolicy(threadPolicy);
            }
        }
    }

    public static void createPdfDocument(Context context, String[] notesContent) {
        DisplayMetrics displaymetrics = getDimensions(context);
        int height = displaymetrics.heightPixels;
        int width = displaymetrics.widthPixels;

        String fileName = notesContent[1] + notesContent[0] + ".pdf";
        PrintAttributes attributes = new PrintAttributes.Builder()
                .setResolution(new PrintAttributes.Resolution("Res", "Phone Res", 20, 20))
                .setMediaSize(new PrintAttributes.MediaSize("Res", "Phone Res",width*10,height*10))
                .setMinMargins(new PrintAttributes.Margins(1,1,1,1))
                .build();
        PrintedPdfDocument document = new PrintedPdfDocument(context, attributes);
        PdfDocument.Page firstPage = document.startPage(1);

        final Canvas canvas = firstPage.getCanvas();
        Paint heading = new Paint();
        Paint content = new Paint();
//        content.setTextSize(12);
//        heading.setTextSize(14);
        heading.setColor(Color.GREEN);
        writeContentToPDF(notesContent, document, firstPage, canvas, heading, content);

        try {
            document.writeTo(getFileOutputStream(fileName));
        } catch (IOException e) {
            e.printStackTrace();
            document.close();
            return;
        }
        document.close();
    }

    private static void writeContentToPDF(String[] notesContent, PrintedPdfDocument document, PdfDocument.Page firstPage, Canvas canvas, Paint heading, Paint content) {
        String data = "Course Title :";
        canvas.drawText(data, 4, 20, heading);
        canvas.drawText(notesContent[0], 5, 40, content);
        data = "Note Title : ";
        canvas.drawText(data, 4, 60, heading);
        canvas.drawText(notesContent[1], 5, 80, content);
        data = "Note Text : ";
        canvas.drawText(data, 4, 100, heading);
        canvas.drawText(notesContent[2],5,120,content);
        document.finishPage(firstPage);
    }

    private static DisplayMetrics getDimensions(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displaymetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(displaymetrics);
        return displaymetrics;
    }

    private static FileOutputStream getFileOutputStream(String fileName) {
        final String externalStorageState = Environment.getExternalStorageState();
        final boolean isMediaAvailable = externalStorageState.equals(Environment.MEDIA_MOUNTED) && !externalStorageState.equals(Environment.MEDIA_MOUNTED_READ_ONLY);

        if (isMediaAvailable) {
            final File parentDir = new File(Environment.getExternalStorageDirectory(), PDF_PUBLIC_DIRECTORY);
            if (!parentDir.exists()) {
                final boolean mkdirs = parentDir.mkdirs();
                if (!mkdirs) return null;
            }
            File notesFile = new File(parentDir, fileName);
            Log.d(TAG, "createPdfDocument: " + "directory : " + parentDir);
            final boolean exists = notesFile.exists();
            if (!exists) {
                try {
                    notesFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                return new FileOutputStream(notesFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
