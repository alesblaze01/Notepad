package com.project.notepad.ContentProvider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import androidx.annotation.NonNull;

import com.project.notepad.Contract.NoteContentContract;
import com.project.notepad.Contract.NoteContentContract.Courses;
import com.project.notepad.Contract.NoteContentContract.Notes;
import com.project.notepad.Contract.NoteContentContract.NotesCourseJoined;
import com.project.notepad.Contract.NotepadOpenHelper;
import com.project.notepad.Contract.NotesDatabaseContract;
import com.project.notepad.Contract.NotesDatabaseContract.CourseInfoEntry;
import com.project.notepad.Contract.NotesDatabaseContract.NotesInfoEntry;

public class NotepadContentProvider extends ContentProvider {

    private static final String TAG = "NotepadContentProvider";
    private NotepadOpenHelper mNotepadOpenHelper;
    private static UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    public static final int COURSES_CODE = 0;
    public static final int NOTES_CODE = 1;
    public static final int NOTES_COURSE_JOINED_CODE = 2;
    private static final int NOTES_ROW_CODE = 3;

    static {
        sUriMatcher.addURI( NoteContentContract.AUTHORITY , Courses.PATH , COURSES_CODE);
        sUriMatcher.addURI( NoteContentContract.AUTHORITY , Notes.PATH , NOTES_CODE);
        sUriMatcher.addURI( NoteContentContract.AUTHORITY , NotesCourseJoined.PATH , NOTES_COURSE_JOINED_CODE);
        sUriMatcher.addURI( NoteContentContract.AUTHORITY , Notes.PATH +"/#", NOTES_ROW_CODE);
    }
    public NotepadContentProvider() {
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mNotepadOpenHelper.getWritableDatabase();
        int uriCode = sUriMatcher.match(uri);
        int response = 0;

        switch (uriCode) {
            case NOTES_ROW_CODE :
                long rowId = ContentUris.parseId(uri);
                selection = NotesInfoEntry._ID+"=?";
                selectionArgs = new String[]{String.valueOf(rowId)};
                response = db.delete(NotesInfoEntry.TABLE_NAME,selection,selectionArgs);
                break;
            default:
                Log.d(TAG, "delete: No Case defined for this table");
        }
        return response;
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        int uriCode = sUriMatcher.match(uri);
        Uri RowUri = null;
        long rowId = -1;
        final SQLiteDatabase db = mNotepadOpenHelper.getWritableDatabase();
        switch (uriCode) {
            case NOTES_CODE :
                rowId = db.insert(NotesInfoEntry.TABLE_NAME , null , values);
                RowUri = ContentUris.withAppendedId(Notes.CONTENT_URI,rowId);
                break;
            case COURSES_CODE :
                //TODO: haven't provided a way for user to enter course
                rowId = db.insert(CourseInfoEntry.TABLE_NAME,null,values);
                RowUri = ContentUris.withAppendedId(Courses.CONTENT_URI,rowId);
            default:
//                return throw new IllegalAccessException("No Other table to enter information in");
        }
        return RowUri;
    }

    @Override
    public boolean onCreate() {
        mNotepadOpenHelper = new NotepadOpenHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        final SQLiteDatabase db = mNotepadOpenHelper.getReadableDatabase();
        int code =  sUriMatcher.match(uri);
        switch (code) {
            case COURSES_CODE :
                return db.query(CourseInfoEntry.TABLE_NAME,projection,
                        selection,selectionArgs,null,null,sortOrder);
            case NOTES_CODE :
                return db.query(NotesInfoEntry.TABLE_NAME,projection,
                        selection,selectionArgs,null,null,sortOrder);
            case NOTES_COURSE_JOINED_CODE :
                return getNotesCourseJoinedCursor(projection,selection,selectionArgs,sortOrder);
            case NOTES_ROW_CODE:
                long rowId = ContentUris.parseId(uri);
                selection = NotesInfoEntry._ID+"=?";
                selectionArgs = new String[]{Long.toString(rowId)};
                return db.query(
                        NotesInfoEntry.TABLE_NAME,projection,selection,selectionArgs,
                        null,null,null
                );
        }
        return null;
    }

    private Cursor getNotesCourseJoinedCursor(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final SQLiteDatabase database = mNotepadOpenHelper.getReadableDatabase();

        String[] columns = new String[projection.length];
        int index = 0;
        for(String col : projection) {
            if(col.equals(BaseColumns._ID) || col.equals(Notes.COURSE_ID)) {
                columns[index++] = NotesInfoEntry.getQualifiedName(col);
            }else{
                columns[index++] = col;
            }
        }

        String joinedTable = String.format(
                "%s JOIN %s ON %s=%s",
                NotesInfoEntry.TABLE_NAME ,
                CourseInfoEntry.TABLE_NAME,
                NotesInfoEntry.getQualifiedName(NotesInfoEntry.COLUMN_COURSE_ID),
                CourseInfoEntry.getQualifiedName(CourseInfoEntry.COLUMN_COURSE_ID)
        );
        return database.query(
                joinedTable,columns,selection,selectionArgs,
                null,null,sortOrder
        );
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        final SQLiteDatabase db = mNotepadOpenHelper.getWritableDatabase();
        int uriCode = sUriMatcher.match(uri);
        int result = 0;
        switch (uriCode) {
            case NOTES_CODE:
                break;
            case COURSES_CODE :
                //TODO: yet to implement
                break;
            case NOTES_ROW_CODE :
                long rowId = ContentUris.parseId(uri);
                String whereClause=NotesInfoEntry._ID+"=?";
                String[] whereArgs = {String.valueOf(rowId)};
                result = db.update(NotesInfoEntry.TABLE_NAME,values,whereClause,whereArgs);
                break;
            default:
                Log.d(TAG, "update: No Case for Update Content Provider");
        }
        return result;
    }
}
