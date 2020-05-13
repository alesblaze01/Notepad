package com.project.notepad.Contract;

import android.provider.BaseColumns;

public final class NotesDatabaseContract {
    public static final class NotesInfoEntry implements BaseColumns {
        public static final String TABLE_NAME="notes_info";
        public static final String COLUMN_NOTE_TITLE="note_title";
        public static final String COLUMN_NOTE_TEXT="note_text";
        public static final String COLUMN_COURSE_ID="course_id";
        public static final String CREATE_TABLE = String.format("CREATE TABLE %s(" +
                "%s INTEGER PRIMARY KEY ,"+
                "%s TEXT NOT NULL ," +
                "%s TEXT ,"+
                "%s TEXT NOT NULL"+
                ");" , TABLE_NAME , _ID , COLUMN_NOTE_TITLE , COLUMN_NOTE_TEXT , COLUMN_COURSE_ID);
        public static final String INDEX1 = TABLE_NAME+"_index1";
        public static final String CREATE_INDEX =
                String.format("CREATE INDEX %s ON %s(%s)" , INDEX1 , TABLE_NAME , COLUMN_NOTE_TITLE);

        public static String getQualifiedName(String columnName){
            return TABLE_NAME+"."+ columnName;
        }
    }

    public static final class CourseInfoEntry implements BaseColumns{
        public static final String TABLE_NAME="course_info";
        public static final String COLUMN_COURSE_ID="course_id";
        public static final String COLUMN_COURSE_TITLE="course_title";
        public static final String CREATE_TABLE=String.format(
                "CREATE TABLE %s(" +
                        "%s INTEGER PRIMARY KEY ,"+
                        "%s TEXT NOT NULL UNIQUE ,"+
                        "%s TEXT NOT NULL "+
                        ");"
        , TABLE_NAME , _ID , COLUMN_COURSE_ID , COLUMN_COURSE_TITLE
        );

        public static final String INDEX1 = TABLE_NAME+"_index1";
        public static final String CREATE_INDEX =
                String.format("CREATE INDEX %s ON %s(%s)" , INDEX1 , TABLE_NAME , COLUMN_COURSE_TITLE);

        public static String getQualifiedName(String columnName){
            return TABLE_NAME+"."+ columnName;
        }
    }
}
