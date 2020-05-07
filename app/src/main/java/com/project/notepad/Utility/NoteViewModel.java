package com.project.notepad.Utility;

import android.os.Bundle;

import androidx.lifecycle.ViewModel;

public class NoteViewModel extends ViewModel {
    public static final String ORIGINAL_NOTE_COURSE_ID = "com.project.notepad.Utility.ORIGINAL_NOTE_COURSE_ID";
    public static final String ORIGINAL_NOTE_TITLE = "com.project.notepad.Utility.ORIGINAL_NOTE_TITLE";
    public static final String ORIGINAL_NOTE_TEXT = "com.project.notepad.Utility.ORIGINAL_NOTE_TEXT";
    public String mOriginalNoteText;
    public String mOriginalNoteTitle;
    public String mOriginalCourseId;
    public boolean mIsNew = true;
    public void saveState(Bundle outState) {
        outState.putString(ORIGINAL_NOTE_COURSE_ID,mOriginalCourseId);
        outState.putString(ORIGINAL_NOTE_TEXT,mOriginalNoteText);
        outState.putString(ORIGINAL_NOTE_TITLE,mOriginalNoteText);
    }
    public void  restoreState(Bundle inState){
        mOriginalCourseId = inState.getString(ORIGINAL_NOTE_COURSE_ID);
        mOriginalNoteTitle = inState.getString(ORIGINAL_NOTE_TITLE);
        mOriginalNoteText = inState.getString(ORIGINAL_NOTE_TEXT);
    }
}
