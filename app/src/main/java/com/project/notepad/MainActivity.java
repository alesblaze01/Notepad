package com.project.notepad;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;
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
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.os.PersistableBundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Date;

import static com.project.notepad.Contract.NoteContentContract.*;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "MainActivity";
    public static final String NOTE_ID ="com.project.notepad.NOTE_POSITION";
    public static final int ID_NOT_SET = -1;
    public static final int SHOW_NOTE = 0;
    private static final String NOTE_POSITION = "com.project.notepad.NOTE_POSITION";
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
    private Uri mRowUri;
    private NotificationManager mNotificationManager;
    private ProgressBar mProgressBar;

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
        mProgressBar = findViewById(R.id.note_progress_bar);
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
                new String[]{Courses.COURSE_TITLE},
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
        ContentValues values = new ContentValues();
        values.put(NotesInfoEntry.COLUMN_NOTE_TITLE,"");
        values.put(NotesInfoEntry.COLUMN_NOTE_TEXT,"");
        values.put(NotesInfoEntry.COLUMN_COURSE_ID,"");

        mRowUri = null;
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                mRowUri = getContentResolver().insert(Notes.CONTENT_URI,values);
                if (mRowUri != null) {
                    mNoteId = (int) ContentUris.parseId(mRowUri);
                    Log.d(TAG, "createNewNote: mNoteId init successfully");
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this
                                , "Note Created", Toast.LENGTH_SHORT).show();
                    });
                }else {
                    mNoteId = -1;
                    Log.d(TAG, "createNewNote: mNoteId init Failure");
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this
                                , "Error Creating Note", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();
        while (thread.getState() != Thread.State.TERMINATED) {
            mProgressBar.setVisibility(View.VISIBLE);
        }
        mProgressBar.setVisibility(View.INVISIBLE);
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
            if (mIsNewNote) deleteNote();
            finish();
        }else if(id == R.id.action_next_note){
//            moveNext();
        }else if (id == R.id.menu_item_delete_note){
            mIsCancelling = true;
            deleteNote();
        }else if (id == R.id.menu_reminder) {
            generateNotification();
        }
        return super.onOptionsItemSelected(item);
    }

    int duration = 1;
    private void generateNotification() {
        Intent intent = new Intent(this,MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(NOTE_ID,mNoteId);
        PendingIntent pendingIntent = PendingIntent
                .getActivity(this, SHOW_NOTE , intent , PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this,"Channel1")
                .setContentTitle("Note Reminder : " + mEditTextNoteTitle.getText())
                .setSmallIcon(R.drawable.ic_speaker_notes_black_24dp)
                .setContentText(mEditTextNoteText.getText())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(mEditTextNoteText.getText()).setBigContentTitle("Note Reminder : " + mEditTextNoteTitle.getText()))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                ;
        NotificationManagerCompat.from(this).notify(SHOW_NOTE,notificationBuilder.build());
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
        if (!mIsCancelling){
            saveNote();
        }
    }

    /**
     * deletes Current selected note ,
     * delegates call to content resolver using row uri
     */
    private void deleteNote() {
        Uri notesRowUri = ContentUris.withAppendedId(Notes.CONTENT_URI,mNoteId);
        final int[] response = {0};
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                response[0] = getContentResolver().delete(notesRowUri,null,null);
                if (response[0] == 1) {
                    Log.d(TAG, "deleteNote: Deletion Successful");
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this
                                , "Note Deleted", Toast.LENGTH_SHORT).show();
                    });
                    mNoteId = -1;
                }else {
                    Log.d(TAG, "deleteNote: Deletion Unsuccessful");
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this
                                , "Error : Deleting Note", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();

        while (thread.getState() != Thread.State.TERMINATED) {
            mProgressBar.setVisibility(View.VISIBLE);
        }
        finish();
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
     * saves Note to database using ContentResolver
     * Logs the result of updation
     */
    private void saveNote() {
        int coursePosition = mSpinner.getSelectedItemPosition();
        final Cursor cursor = mSimpleCursorAdapter.getCursor();
        cursor.moveToPosition(coursePosition);
        int courseIdPos = cursor.getColumnIndex(NotesInfoEntry.COLUMN_COURSE_ID);
        String courseId = cursor.getString(courseIdPos);

        ContentValues values = new ContentValues();
        values.put(NotesInfoEntry.COLUMN_COURSE_ID,courseId);
        values.put(NotesInfoEntry.COLUMN_NOTE_TITLE,mEditTextNoteTitle.getText().toString());
        values.put(NotesInfoEntry.COLUMN_NOTE_TEXT,mEditTextNoteText.getText().toString());
        Uri notesRowUri = ContentUris.withAppendedId(Notes.CONTENT_URI,mNoteId);

        Runnable runnable = () -> {
            int response = getContentResolver().update(notesRowUri,values,null,null);
            if ( response == 1 ) {
                Log.d(TAG, "saveNote: 1 Note Updated");
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this
                            , "Note Saved", Toast.LENGTH_SHORT).show();
                });
            }else {
                Log.d(TAG, "saveNote: Note Updation Unsuccessful");
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this
                            , "Error : Saving Note", Toast.LENGTH_SHORT).show();
                });
            }
        };
        Thread thread = new Thread(runnable);
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();
        while (thread.getState() != Thread.State.TERMINATED) {
            mProgressBar.setVisibility(View.VISIBLE);
        }
        finish();
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
            loader = createNotesLoader();
        }else if ( id == LOADER_COURSES) {
            loader = createCourseLoader();
        }
        return loader;
    }

    private Loader<Cursor> createCourseLoader() {
        final String[] courseColumns = {
                Courses.COURSE_ID,
                Courses.COURSE_TITLE,
                Courses._ID
        };
        return new CursorLoader(
                this,Courses.CONTENT_URI,courseColumns,
                null,null, Courses.COURSE_TITLE
        );
    }

    private CursorLoader createNotesLoader() {
        final String[] noteColumns = {
                Notes.COURSE_ID,
                Notes.NOTE_TEXT,
                Notes.NOTE_TITLE,
                Notes._ID
        };
        Uri noteRowUri = ContentUris.withAppendedId(Notes.CONTENT_URI,mNoteId);
        return new CursorLoader(
                this,noteRowUri , noteColumns,
                null, null, null
        );
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
