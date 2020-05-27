package com.project.notepad.Utility;

import android.content.Context;
import android.database.Cursor;

import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.project.notepad.Contract.NoteContentContract.Courses;
import com.project.notepad.Contract.NoteContentContract.NotesCourseJoined;

public class CursorUtility {
    public static class NoteListCursor {
        public static Loader<Cursor> getCourseCursor(Context context){
            String[] columns = new String[]{
                    Courses.COURSE_TITLE,
                    Courses.COURSE_ID,
                    Courses._ID
            };
            return new CursorLoader(context, Courses.CONTENT_URI ,
                    columns , null , null , Courses.COURSE_TITLE);
        }

        public static Loader<Cursor> getNotesCursor(Context context) {
            final String[] noteColumns = {
                    NotesCourseJoined.COURSE_TITLE,
                    NotesCourseJoined.NOTE_TITLE,
                    NotesCourseJoined._ID
            };
            String orderNotesBy = NotesCourseJoined.COURSE_TITLE +
                    "," + NotesCourseJoined.NOTE_TITLE+" DESC";
            return new CursorLoader(
                    context, NotesCourseJoined.CONTENT_URI , noteColumns ,
                    null, null , orderNotesBy
            );
        }
    }
}
