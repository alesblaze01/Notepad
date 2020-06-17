package com.project.notepad;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.project.notepad.Utility.GeneralUtility;

public class SaveAsPdfWorker extends Worker {

    public SaveAsPdfWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        final Data inputData = getInputData();
        final String[] notesContent = inputData.getStringArray(MainActivity.NOTES_CONTENT);
        if (notesContent != null) {
            GeneralUtility.createPdfDocument(this.getApplicationContext(),notesContent);
        }else
            return Result.failure();

        return Result.success();
    }
}
