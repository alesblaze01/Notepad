package com.project.notepad;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.project.notepad.Utility.CourseInfo;
import com.project.notepad.Utility.DataManager;
import com.project.notepad.Utility.NoteInfo;
import com.project.notepad.Utility.NoteViewModel;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import android.os.PersistableBundle;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

public class MainActivity extends AppCompatActivity {
    public static final String NOTE_POSITION ="com.project.notepad.NOTE_POSITION";
    public static final int POSITION_NOT_SET = -1;
    private NoteInfo mNoteInfo;
    private boolean mIsNewNote;
    private Spinner mSpinner;
    private EditText mEditTextNoteTitle;
    private EditText mEditTextNoteText;
    private int mNotePosition;
    private boolean mIsCancelling;
    private NoteViewModel mNoteViewModel;

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

        ViewModelProvider viewModelProvider = new ViewModelProvider(this.getViewModelStore(),ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()));
        mNoteViewModel = viewModelProvider.get(NoteViewModel.class);
        if (savedInstanceState!=null && mNoteViewModel.mIsNew){
            mNoteViewModel.restoreState(savedInstanceState);
        }

        mNoteViewModel.mIsNew = false;

        //setting up spinner
        mSpinner = findViewById(R.id.course_spinner);
        ArrayAdapter<CourseInfo> courseInfoArrayAdapter = new ArrayAdapter<>(this,android.R.layout.simple_spinner_item, DataManager.getInstance().getCourses());
        courseInfoArrayAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
        mSpinner.setAdapter(courseInfoArrayAdapter);
        //added spinner

        readDisplayStateValues();
        savePreviousOriginalNoteValues();
        mEditTextNoteTitle = findViewById(R.id.note_title);
        mEditTextNoteText = findViewById(R.id.note_text);

        if (!mIsNewNote) // only display if not new note
            displayNote(mSpinner, mEditTextNoteTitle, mEditTextNoteText);
    }

    private void savePreviousOriginalNoteValues() {
        if (mIsNewNote) return;
        mNoteViewModel.mOriginalCourseId = mNoteInfo.getCourse().getCourseId();
        mNoteViewModel.mOriginalNoteText = mNoteInfo.getText();
        mNoteViewModel.mOriginalNoteTitle = mNoteInfo.getTitle();
//        mOriginalCourseId = mNoteInfo.getCourse().getCourseId();
//        mOriginalNoteText = mNoteInfo.getText();
//        mOriginalNoteTitle = mNoteInfo.getTitle();
    }

    /**
     * get Position of selected item from mNoteInfo object.
     * fetches values from mNoteInfo in MainActivity and updates Views in The UI.
     * @param spinner
     * @param editTextNoteTitle
     * @param editTextNoteText
     */
    private void displayNote(Spinner spinner, EditText editTextNoteTitle, EditText editTextNoteText) {
        int coursePosition = DataManager.getInstance().getCourses().indexOf(mNoteInfo.getCourse());
        spinner.setSelection(coursePosition);
        editTextNoteText.setText(mNoteInfo.getText());
        editTextNoteTitle.setText(mNoteInfo.getTitle());
    }

    /**
     * gets note position from intent passed ,
     * checks if a note is new ,
     * if it is not new the we get the note from datamanager using position variable
     */
    private void readDisplayStateValues() {
        Intent intent = getIntent();
        int notePosition = intent.getIntExtra(NOTE_POSITION, POSITION_NOT_SET);
        mIsNewNote = notePosition == POSITION_NOT_SET;
        if (mIsNewNote){
            createNewNote();
        }else{
            mNoteInfo = DataManager.getInstance().getNotes().get(notePosition);
        }
    }

    /**
     * creates new Note in dataManager
     * and sets mNoteInfo field to new Note object created
     */
    private void createNewNote() {
        DataManager dataManager = DataManager.getInstance();
        mNotePosition = dataManager.createNewNote();
        mNoteInfo = dataManager.getNotes().get(mNotePosition);
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
        if (id == R.id.action_send_mail) {
            sendMail();
            return true;
        }else if(id == R.id.action_cancel){
            mIsCancelling = true;
            finish();
        }else if(id == R.id.action_next_note){
            moveNext();
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
        mNotePosition++;
        mNoteInfo = DataManager.getInstance().getNotes().get(mNotePosition);
        savePreviousOriginalNoteValues();
        displayNote(mSpinner,mEditTextNoteTitle,mEditTextNoteText);
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
//        menuItem.setEnabled();
        menuItem.setVisible(mNotePosition<lastIndex);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mIsCancelling){
            if (mIsNewNote) {
                DataManager.getInstance().removeNote(mNotePosition);
            }
            else{
                fetchPreviousValues();
            }
        }else {
            saveNote();
        }
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
        mNoteInfo.setCourse((CourseInfo) mSpinner.getSelectedItem());
        mNoteInfo.setTitle(mEditTextNoteTitle.getText().toString());
        mNoteInfo.setText(mEditTextNoteText.getText().toString());
    }

    /**
     * sends a mail to the user
     * gets the content from the View in MainActivity
     * sets up an implicit intent and starts the activity
     */
    private void sendMail() {
        CourseInfo courseInfo = (CourseInfo) mSpinner.getSelectedItem();
        String subject = mEditTextNoteTitle.getText().toString();
        String text = courseInfo + "\n" + mEditTextNoteText.getText().toString();

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc2822");
        intent.putExtra(Intent.EXTRA_SUBJECT,subject);
        intent.putExtra(Intent.EXTRA_TEXT,text);

        startActivity(intent);
    }
}
