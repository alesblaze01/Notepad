package com.project.notepad.Contract;

import android.net.Uri;
import android.provider.BaseColumns;

import com.project.notepad.Contract.NotesDatabaseContract.CourseInfoEntry;
import com.project.notepad.Contract.NotesDatabaseContract.NotesInfoEntry;

public class NoteContentContract {
    private NoteContentContract(){}
    public static final int LOADER_NOTES = 0;
    public static final int LOADER_COURSES = 1;
    public static final int LOADER_NOTES_COURSE_JOINED = 2;


    public static final String AUTHORITY="com.project.notepad.provider";
    public static final Uri BASE_URI = Uri.parse("content://"+ AUTHORITY);

    public static class Notes implements BaseColumns , NoteColumns , CourseIdColumns {
        public static final String PATH = "notes";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_URI,PATH);
    }

    public static class Courses implements BaseColumns, CourseColumns , CourseIdColumns {
        public static final String PATH = "courses";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_URI,PATH);
    }

    public static class NotesCourseJoined extends  Notes implements CourseColumns {
        public static final String PATH = "notes_courses_joined";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_URI,PATH);
    }

    protected interface NoteColumns{
        String NOTE_TITLE = NotesInfoEntry.COLUMN_NOTE_TITLE;
        String NOTE_TEXT = NotesInfoEntry.COLUMN_NOTE_TEXT;
    }
    protected interface CourseColumns{
        String COURSE_TITLE = CourseInfoEntry.COLUMN_COURSE_TITLE;
    }
    protected interface CourseIdColumns {
        String COURSE_ID = CourseInfoEntry.COLUMN_COURSE_ID;
    }
}
