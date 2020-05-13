package com.project.notepad;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import com.project.notepad.Contract.NotepadOpenHelper;
import com.project.notepad.Contract.NotesDatabaseContract.CourseInfoEntry;
import com.project.notepad.Contract.NotesDatabaseContract.NotesInfoEntry;
import com.project.notepad.Utility.CourseInfo;
import com.project.notepad.Utility.DataManager;
import com.project.notepad.Utility.NoteInfo;
import com.project.notepad.Utility.NoteViewModel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.os.PersistableBundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "MainActivity";
    public static final String NOTE_ID ="com.project.notepad.NOTE_POSITION";
    public static final int ID_NOT_SET = -1;
    public static final int LOADER_NOTES = 0;
    public static final int LOADER_COURSES = 1;
    private NoteInfo mNoteInfo;
    private boolean mIsNewNote;
    private Spinner mSpinner;
    private EditText mEditTextNoteTitle;
    private EditText mEditTextNoteText;
    private int mNoteId;
    private boolean mIsCancelling;
    private NoteViewModel mNoteViewModel;
    private NotepadOpenHelper mNotepadOpenHelper;
    private int mNoteTitlePos;
    private int mNoteTextPos;
    private int mCourseIdPos;
    private Cursor mCursor;
    private SimpleCursorAdapter mSimpleCursorAdapter;
    private boolean mIsNoteLoaded;
    private boolean mIsCourseLoaded;

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        mNoteViewModel.saveState(outState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mNotepadOpenHelper = new NotepadOpenHelper(this);
        mEditTextNoteTitle = findViewById(R.id.note_title);
        mEditTextNoteText = findViewById(R.id.note_text);

        ViewModelProvider viewModelProvider = new ViewModelProvider(this.getViewModelStore(),ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()));
        mNoteViewModel = viewModelProvider.get(NoteViewModel.class);
        if (savedInstanceState!=null && mNoteViewModel.mIsNew){
            mNoteViewModel.restoreState(savedInstanceState);
        }

        mNoteViewModel.mIsNew = false;

        //setting up spinner
        mSpinner = findViewById(R.id.course_spinner);
        mSimpleCursorAdapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_spinner_item,
                null,
                new String[]{CourseInfoEntry.COLUMN_COURSE_TITLE},
                new int[] {android.R.id.text1},
                0);
        mSimpleCursorAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
        mSpinner.setAdapter(mSimpleCursorAdapter);
        //added spinner

        LoaderManager.getInstance(this).initLoader(LOADER_COURSES,null,this);
        
        readDisplayStateValues();

        if (!mIsNewNote)
            LoaderManager.getInstance(this).initLoader(LOADER_NOTES, null , this);
    }

    private void loadCourseDataForSpinner() {
        final SQLiteDatabase readableDatabase = mNotepadOpenHelper.getReadableDatabase();
        final String[] courseColumns = {
                CourseInfoEntry.COLUMN_COURSE_ID,
                CourseInfoEntry.COLUMN_COURSE_TITLE,
                CourseInfoEntry._ID
        };
        Cursor columnsCursor = readableDatabase.query(CourseInfoEntry.TABLE_NAME ,
                courseColumns,null,
                null,null,
                null,CourseInfoEntry.COLUMN_COURSE_TITLE);
        mSimpleCursorAdapter.changeCursor(columnsCursor);
    }

    private void loadNoteDataFromDatabase() {
        final SQLiteDatabase readableDatabase = mNotepadOpenHelper.getReadableDatabase();
        String[] notesColumns = new String[]{
                NotesInfoEntry.COLUMN_COURSE_ID,
                NotesInfoEntry.COLUMN_NOTE_TITLE,
                NotesInfoEntry.COLUMN_NOTE_TEXT,
                NotesInfoEntry._ID
        };
        String selection = NotesInfoEntry._ID + " = ?";
        String[] selectionArgs = {String.valueOf(mNoteId)};
        mCursor = readableDatabase.query(NotesInfoEntry.TABLE_NAME,
                notesColumns, selection,
                selectionArgs, null,
                null, null);
        mNoteTitlePos = mCursor.getColumnIndex(NotesInfoEntry.COLUMN_NOTE_TITLE);
        mNoteTextPos = mCursor.getColumnIndex(NotesInfoEntry.COLUMN_NOTE_TEXT);
        mCourseIdPos = mCursor.getColumnIndex(NotesInfoEntry.COLUMN_COURSE_ID);
        mCursor.moveToNext();
        displayNote();
    }

    private void savePreviousOriginalNoteValues() {
        if (mIsNewNote) return;
        mNoteViewModel.mOriginalCourseId = mNoteInfo.getCourse().getCourseId();
        mNoteViewModel.mOriginalNoteText = mNoteInfo.getText();
        mNoteViewModel.mOriginalNoteTitle = mNoteInfo.getTitle();
    }

    /** // need to change its documentation
     * get Position of selected item from mNoteInfo object.
     * fetches values from mNoteInfo in MainActivity and updates Views in The UI.
     */
    private void displayNote() {
        String noteTitle = mCursor.getString(mNoteTitlePos);
        String noteText = mCursor.getString(mNoteTextPos);
        String courseId = mCursor.getString(mCourseIdPos);

        int coursePosition = getIndexOfCourse(courseId);
        mSpinner.setSelection(coursePosition);
        mEditTextNoteText.setText(noteText);
        mEditTextNoteTitle.setText(noteTitle);
    }

    private int getIndexOfCourse(String courseId) {
        final Cursor courseCursor = mSimpleCursorAdapter.getCursor();
        courseCursor.move(-1);
        int cPos = 0;
        while(courseCursor.moveToNext()){
            String cId = courseCursor.getString(courseCursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_ID));
            if (cId.equals(courseId)){
                break;
            }
            cPos++;
        }
        return cPos;
    }

    /**
     * gets note position from intent passed ,
     * checks if a note is new ,
     * if it is not new the we get the note from data manager using position variable
     */
    private void readDisplayStateValues() {
        Intent intent = getIntent();
        mNoteId = intent.getIntExtra(NOTE_ID, ID_NOT_SET);
        mIsNewNote = mNoteId == ID_NOT_SET;
        if (mIsNewNote){
            createNewNote();
        }
    }

    /**
     * creates new Note in dataManager
     * and sets mNoteInfo field to new Note object created
     */
    private void createNewNote() {
        final SQLiteDatabase writableDatabase = mNotepadOpenHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(NotesInfoEntry.COLUMN_NOTE_TITLE,"");
        values.put(NotesInfoEntry.COLUMN_NOTE_TEXT,"");
        values.put(NotesInfoEntry.COLUMN_COURSE_ID,"");

        mNoteId = (int) writableDatabase.insert(NotesInfoEntry.TABLE_NAME , null , values);
        writableDatabase.close();
    }

    /**
     * creates menu in app bar
     * associates menu with activity
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * gets called when menu item in action bar gets selected
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_send_mail){
            sendMail();
            return true;
        }else if(id == R.id.action_cancel){
            mIsCancelling = true;
            finish();
        }else if(id == R.id.action_next_note){
            moveNext();
        }else if (id == R.id.menu_item_delete_note){
            deleteNote();
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * shows next note without going back to NoteListActivity
     * uses mNotePosition instance to move to next note
     * save previous Values for the current Note and then displays it
     * invalidates the app bar menu to check if actions are valid or not
     */
    private void moveNext() {
        saveNote();
        mNoteId++;
        mNoteInfo = DataManager.getInstance().getNotes().get(mNoteId);
        savePreviousOriginalNoteValues();
        displayNote();
        invalidateOptionsMenu();
    }

    /**
     * check if appbar actions are valid or not
     * @param menu
     * @return
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem menuItem = menu.findItem(R.id.action_next_note);
        int lastIndex = DataManager.getInstance().getNotes().size()-1;
        menuItem.setVisible(mNoteId < lastIndex);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mIsCancelling){
            if (mIsNewNote) {
                deleteNote();
            }
            else{
//                fetchPreviousValues();
            }
        }else {
            saveNote();
        }
    }

    private void deleteNote() {
        final SQLiteDatabase writableDatabase = mNotepadOpenHelper.getWritableDatabase();
        String whereClause = NotesInfoEntry._ID +"=?";
        String[] whereArgs = new String[]{String.valueOf(mNoteId)};
        int result = writableDatabase.delete(NotesInfoEntry.TABLE_NAME,whereClause,whereArgs);
        Log.d(TAG, "deleteANewNote: "+ result + " Note Delete");
    }

    /**
     * save previous to the NoteInfo object from Custom ViewModel
     * when user cancel the changes made ot the note
     */
    private void fetchPreviousValues() {
        CourseInfo courseInfo = DataManager.getInstance().getCourse(mNoteViewModel.mOriginalCourseId);
        mNoteInfo.setCourse(courseInfo);
        mNoteInfo.setText(mNoteViewModel.mOriginalNoteText);
        mNoteInfo.setTitle(mNoteViewModel.mOriginalNoteTitle);
    }

    /**
     * saves values from views to mNoteInfo object
     */
    private void saveNote() {
        final SQLiteDatabase writableDatabase = mNotepadOpenHelper.getWritableDatabase();

        int coursePosition = mSpinner.getSelectedItemPosition();
        final Cursor cursor = mSimpleCursorAdapter.getCursor();
        cursor.moveToPosition(coursePosition);

        int courseIdPos = cursor.getColumnIndex(NotesInfoEntry.COLUMN_COURSE_ID);
        String courseId = cursor.getString(courseIdPos);

        ContentValues values = new ContentValues();
        values.put(NotesInfoEntry.COLUMN_COURSE_ID,courseId);
        values.put(NotesInfoEntry.COLUMN_NOTE_TITLE,mEditTextNoteTitle.getText().toString());
        values.put(NotesInfoEntry.COLUMN_NOTE_TEXT,mEditTextNoteText.getText().toString());

        String whereClause = String.format("%s=?",NotesInfoEntry._ID);
        String[] whereArgs = new String[]{String.valueOf(mNoteId)};

        int updateRows = writableDatabase.update(
                NotesInfoEntry.TABLE_NAME,values,
                whereClause,whereArgs
        );
        writableDatabase.close();
        Log.d(TAG, "saveNote: "+updateRows+" Note Updated");
    }

    /**
     * sends a mail to the user
     * gets the content from the View in MainActivity
     * sets up an implicit intent and starts the activity
     */
    private void sendMail() {
//        CourseInfo courseInfo = (CourseInfo) mSpinner.getSelectedItem();
//        String subject = mEditTextNoteTitle.getText().toString();
//        String text = courseInfo + "\n" + mEditTextNoteText.getText().toString();
//
//        Intent intent = new Intent(Intent.ACTION_SEND);
//        intent.setType("message/rfc2822");
//        intent.putExtra(Intent.EXTRA_SUBJECT,subject);
//        intent.putExtra(Intent.EXTRA_TEXT,text);
//
//        startActivity(intent);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        Loader<Cursor> loader = null;
        if(id == LOADER_NOTES){
            loader = createNodesLoader();
        }else if ( id == LOADER_COURSES) {
            loader = createCourseLoader();
        }
        return loader;
    }

    private Loader<Cursor> createCourseLoader() {
        return new CursorLoader(this){
            @Override
            public Cursor loadInBackground() {
                SQLiteDatabase readableDatabase = mNotepadOpenHelper.getReadableDatabase();
                final String[] courseColumns = {
                        CourseInfoEntry.COLUMN_COURSE_ID,
                        CourseInfoEntry.COLUMN_COURSE_TITLE,
                        CourseInfoEntry._ID
                };
                return readableDatabase.query(
                        CourseInfoEntry.TABLE_NAME, courseColumns,
                        null, null, null,
                        null, CourseInfoEntry.COLUMN_COURSE_TITLE
                );
            }
        };
    }

    private CursorLoader createNodesLoader() {
        return new CursorLoader(this){
            @Override
            public Cursor loadInBackground() {
                final SQLiteDatabase readableDatabase = mNotepadOpenHelper.getReadableDatabase();
                final String[] noteColumns = {
                        NotesInfoEntry.COLUMN_COURSE_ID,
                        NotesInfoEntry.COLUMN_NOTE_TEXT,
                        NotesInfoEntry.COLUMN_NOTE_TITLE,
                        NotesInfoEntry._ID
                };
                String orderNotesBy = NotesInfoEntry.COLUMN_COURSE_ID + "," + NotesInfoEntry.COLUMN_NOTE_TITLE+" DESC";
                return  readableDatabase.query(
                        NotesInfoEntry.TABLE_NAME, noteColumns,
                        NotesInfoEntry._ID+"=?", new String[]{String.valueOf(mNoteId)},
                        null, null, orderNotesBy
                );
            }
        };
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        int id = loader.getId();
        if(id == LOADER_NOTES){
            mIsNoteLoaded = false;
            loadNoteFromCursor(data);
        }else if ( id == LOADER_COURSES) {
            mSimpleCursorAdapter.changeCursor(data);
            mIsCourseLoaded = true;
            displayNoteWhenCourseAndNoteDataLoaded();
        }
    }

    @Override
    protected void onDestroy() {
        mNotepadOpenHelper.close();
        super.onDestroy();
    }

    private void loadNoteFromCursor(Cursor data) {
        mCursor = data;
        mNoteTitlePos = data.getColumnIndex(NotesInfoEntry.COLUMN_NOTE_TITLE);
        mNoteTextPos = data.getColumnIndex(NotesInfoEntry.COLUMN_NOTE_TEXT);
        mCourseIdPos = data.getColumnIndex(NotesInfoEntry.COLUMN_COURSE_ID);
        data.moveToNext();
        mIsNoteLoaded = true;
        displayNoteWhenCourseAndNoteDataLoaded();
    }

    private void displayNoteWhenCourseAndNoteDataLoaded() {
        if(mIsNoteLoaded && mIsCourseLoaded){
            displayNote();
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        int id = loader.getId();
        if(id == LOADER_NOTES){
            if(mCursor!=null){
                mCursor.close();
            }
        }else if ( id == LOADER_COURSES) {
            if(mSimpleCursorAdapter.getCursor() != null) {
                mSimpleCursorAdapter.changeCursor(null);
            }
        }
    }
}
