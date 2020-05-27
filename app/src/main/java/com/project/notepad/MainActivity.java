package com.project.notepad;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import com.project.notepad.Contract.NoteContentContract;
import com.project.notepad.Contract.NotepadOpenHelper;
import com.project.notepad.Contract.NotesDatabaseContract.CourseInfoEntry;
import com.project.notepad.Contract.NotesDatabaseContract.NotesInfoEntry;
import com.project.notepad.Model.CourseInfo;
import com.project.notepad.Utility.DataManager;
import com.project.notepad.Model.NoteInfo;
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
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static com.project.notepad.Contract.NoteContentContract.*;

public class MainActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "MainActivity";
    public static final String NOTE_ID ="com.project.notepad.NOTE_POSITION";
    public static final int ID_NOT_SET = -1;
    public static final int SHOW_NOTE = 0;
    public static final int NOTE_REMINDER_REQUEST_CODE = 0;
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
    private ProgressBar mProgressBar;
    private int noteTextChars = 0;
    private TextView mCharsInNoteText;

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        mNoteViewModel.saveState(outState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        InitGlobalVariable();

        Toolbar toolbar = findViewById(R.id.toolbar2);
        toolbar.setNavigationOnClickListener(v -> {
            onNavigateUp();
        });
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.action_send_mail){
                    sendMail();
                    return true;
                }else if(id == R.id.action_cancel){
                    mIsCancelling = true;
                    if (mIsNewNote) deleteNote();
                    finish();
                }else if(id == R.id.action_next_note){

                }else if (id == R.id.menu_item_delete_note){
                    mIsCancelling = true;
                    deleteNote();
                }else if (id == R.id.menu_reminder) {
                    setNoteReminder();
                    return true;
                }
                return false;
            }
        });

        mEditTextNoteText.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    noteTextChars = mEditTextNoteText.getText().length();
                    mCharsInNoteText.setText(String.valueOf(noteTextChars));
                }
        });

        ViewModelProvider viewModelProvider = new ViewModelProvider(this.getViewModelStore(),ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()));
        mNoteViewModel = viewModelProvider.get(NoteViewModel.class);

        if (savedInstanceState!=null && mNoteViewModel.mIsNew){
            mNoteViewModel.restoreState(savedInstanceState);
        }
        mNoteViewModel.mIsNew = false;


        setUpSpinnerAndCourseCursor();
        LoaderManager.getInstance(this).initLoader(LOADER_COURSES,null,this);
        readDisplayStateValues();
        if (!mIsNewNote)
            LoaderManager.getInstance(this).initLoader(LOADER_NOTES, null , this);
    }

    private void setUpSpinnerAndCourseCursor() {
        mSpinner = findViewById(R.id.course_spinner);
        mSimpleCursorAdapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_spinner_item,
                null,
                new String[]{Courses.COURSE_TITLE},
                new int[] {android.R.id.text1},
                0);
        mSimpleCursorAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
        mSpinner.setAdapter(mSimpleCursorAdapter);
    }

    private void InitGlobalVariable() {
        mProgressBar = findViewById(R.id.note_progress_bar);
        mNotepadOpenHelper = new NotepadOpenHelper(this);
        mEditTextNoteTitle = findViewById(R.id.note_title);
        mEditTextNoteText = findViewById(R.id.note_text);
        mCharsInNoteText = findViewById(R.id.chars_in_note_text);
    }

    public static Intent getIntent(Context context) {
        Intent intent = new Intent(context,MainActivity.class);
        return intent;
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
        setCharacterCount();
    }

    private void setCharacterCount() {
        noteTextChars = mEditTextNoteText.getText().length();
        mCharsInNoteText.setText(String.valueOf(noteTextChars));
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
        values.put(Notes.NOTE_TITLE,"");
        values.put(Notes.NOTE_TEXT,"");
        values.put(Notes.COURSE_ID,"");

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
        delegateWorkToWorkerThread(runnable);
        mProgressBar.setVisibility(View.INVISIBLE);
    }

    private void setNoteReminder() {
        Calendar calendar = GregorianCalendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                calendar.set(Calendar.HOUR_OF_DAY,hourOfDay);
                calendar.set(Calendar.MINUTE,minute);
                Log.d(TAG, "onTimeSet: Log Time Is : " + calendar.getTimeInMillis());
                notifyAlarmManger(calendar.getTimeInMillis());
            }
        } , calendar.get(Calendar.HOUR_OF_DAY),calendar.get(Calendar.MINUTE),false);
        timePickerDialog.show();
    }

    private void notifyAlarmManger(long triggerTime) {
        final String noteTitle = mEditTextNoteTitle.getText().toString();
        final String noteText = mEditTextNoteText.getText().toString();
        setAlarm(triggerTime, noteTitle, noteText);
    }

    private void setAlarm(long triggerTime, String noteTitle, String noteText) {
        Intent intent = NoteReminderReceiver.getIntent(this,noteText,noteTitle,mNoteId);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this , NOTE_REMINDER_REQUEST_CODE, intent,PendingIntent.FLAG_UPDATE_CURRENT);
        final AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC, triggerTime ,pendingIntent);
            Toast.makeText(this, "Alarm Set", Toast.LENGTH_SHORT).show();
        } else {
            Log.d(TAG, "notifyAlarmManger: Alarm didn't Set");
        }
    }


    /**
     * shows next note without going back to NoteListActivity
     * uses mNotePosition instance to move to next note
     * save previous Values for the current Note and then displays it
     * invalidates the app bar menu to check if actions are valid or not
     */
    private void moveNext() {
        saveNote();
//        mNoteId++;
//        mNoteInfo = DataManager.getInstance().getNotes().get(mNoteId);
//        savePreviousOriginalNoteValues();
        displayNote();
        invalidateOptionsMenu();
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
        Runnable runnable = () -> {
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
        };
        delegateWorkToWorkerThread(runnable);
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
        int courseIdPos = cursor.getColumnIndex(Notes.COURSE_ID);
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
        delegateWorkToWorkerThread(runnable);
        finish();
    }

    private void delegateWorkToWorkerThread(Runnable runnable) {
        Thread thread = new Thread(runnable);
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();
        mProgressBar.setVisibility(View.VISIBLE);
        while (thread.getState() != Thread.State.TERMINATED) {
        }
    }

    /**
     * sends a mail to the user
     * gets the content from the View in MainActivity
     * sets up an implicit intent and starts the activity
     */
    private void sendMail() {
        final Cursor cursor = mSimpleCursorAdapter.getCursor();
        cursor.moveToPosition(mSpinner.getSelectedItemPosition());
        String courseTitle = cursor.getString(cursor.getColumnIndex(Courses.COURSE_TITLE));
        String noteTitle = mEditTextNoteTitle.getText().toString();
        String noteText = courseTitle +  "/n" +mEditTextNoteText.getText().toString();

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc2822");
        intent.putExtra(Intent.EXTRA_SUBJECT,noteTitle);
        intent.putExtra(Intent.EXTRA_TEXT,noteText);
        startActivity(intent);
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
