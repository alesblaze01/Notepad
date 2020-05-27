package com.project.notepad.Contract;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.project.notepad.Utility.DatabaseDataWorker;

public class NotepadOpenHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME="notepad.db";
    private static final Integer DATABASE_VERSION=2;

    public NotepadOpenHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(NotesDatabaseContract.NotesInfoEntry.CREATE_TABLE);
        db.execSQL(NotesDatabaseContract.CourseInfoEntry.CREATE_TABLE);
        createIndices(db);
        
        DatabaseDataWorker databaseDataWorker = new DatabaseDataWorker(db);
        databaseDataWorker.insertCourses();
        databaseDataWorker.insertSampleNotes();
    }

    private void createIndices(SQLiteDatabase db) {
        db.execSQL(NotesDatabaseContract.CourseInfoEntry.CREATE_INDEX);
        db.execSQL(NotesDatabaseContract.NotesInfoEntry.CREATE_INDEX);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(oldVersion < 2){
            createIndices(db);
        }
    }
}
