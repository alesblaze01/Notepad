package com.project.notepad;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.project.notepad.Adpater.CoursesRecyclerAdapter;
import com.project.notepad.Adpater.NoteRecyclerAdapter;
import com.project.notepad.Contract.NoteContentContract.Courses;
import com.project.notepad.Contract.NoteContentContract.NotesCourseJoined;
import com.project.notepad.Contract.NotepadOpenHelper;
import com.project.notepad.Contract.NotesDatabaseContract.CourseInfoEntry;
import com.project.notepad.Contract.NotesDatabaseContract.NotesInfoEntry;
import com.project.notepad.Utility.CursorUtility;
import com.project.notepad.Utility.GeneralUtility;
import com.project.notepad.Utility.UserAccount;

import static com.project.notepad.Contract.NoteContentContract.LOADER_COURSES;
import static com.project.notepad.Contract.NoteContentContract.LOADER_NOTES_COURSE_JOINED;
import static com.project.notepad.Utility.CursorUtility.NoteListCursor.getCourseCursor;
import static com.project.notepad.Utility.CursorUtility.NoteListCursor.getNotesCursor;

public class NoteListActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, LoaderManager.LoaderCallbacks<Cursor> {
    public static final String CHECKED_NAV_MENU_ITEM = "checkedNavMenuItem";

    private NoteRecyclerAdapter mNoteRecyclerAdapter;
    private LinearLayoutManager mLinearLayoutManager;
    private RecyclerView mRecyclerView;
    private CoursesRecyclerAdapter mCoursesRecyclerAdapter;
    private GridLayoutManager mGridLayoutManager;
    private NotepadOpenHelper mNotepadOpenHelper;
    private NavigationView mNavigationView;
    private UserAccount mUserAccount;
    private static final String TAG = "NoteListActivity";
    private Bundle mSavedInstanceState;
    private int mSelectedNavMenuItemId=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        GeneralUtility.StrictMode.enableStrictMode();

        if (savedInstanceState != null) {
            mSavedInstanceState = savedInstanceState;
        }else {
            mSelectedNavMenuItemId = R.id.nav_notes;
        }
        initializeGlobalViews();
        mUserAccount = UserAccount.getInstance(this);
        if(mUserAccount.ifPreviouslyLogin()) {
            updateNavigationHeader();
        }
        setUpDrawer(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Menu navMenu = mNavigationView.getMenu();
                if(navMenu.findItem(R.id.nav_courses).isChecked()) {
                    startActivity(CourseActivity.getIntent(NoteListActivity.this));
                }else if (navMenu.findItem(R.id.nav_notes).isChecked()){
                    startActivity(MainActivity.getIntent(NoteListActivity.this));
                }
            }
        });
        restartLoader();
    }

    private void restartLoader() {
        LoaderManager.getInstance(this).restartLoader(LOADER_NOTES_COURSE_JOINED, null, this);
        LoaderManager.getInstance(this).restartLoader(LOADER_COURSES, null, this);
    }

    private void setUpDrawer(Toolbar toolbar) {
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.open_navigation,R.string.close_navigation);
        drawerLayout.setDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
    }

    private void initializeGlobalViews() {
        mRecyclerView = findViewById(R.id.list_notes);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mGridLayoutManager = new GridLayoutManager(this, 2);
        mNotepadOpenHelper = new NotepadOpenHelper(this);
        mNavigationView = findViewById(R.id.nav_bar);
        mNavigationView.setNavigationItemSelectedListener(this);
    }

    public void updateNavigationHeader() {
        View navHeader = mNavigationView.getHeaderView(0);
        TextView userName = navHeader.findViewById(R.id.nav_user_name);
        TextView userEmail = navHeader.findViewById(R.id.nav_user_email_address);
        if(mUserAccount.isSignedIn()) {
            userName.setText(mUserAccount.getAccount().getDisplayName());
            userEmail.setText(mUserAccount.getAccount().getEmail());
        }else {
            userName.setText("Not Set");
            userEmail.setText("Not Set");
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt(CHECKED_NAV_MENU_ITEM, mSelectedNavMenuItemId);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        mSelectedNavMenuItemId = savedInstanceState.getInt(CHECKED_NAV_MENU_ITEM,0);
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mSavedInstanceState != null) {
            mSelectedNavMenuItemId = mSavedInstanceState.getInt(CHECKED_NAV_MENU_ITEM);
        }
    }

    @Override
    protected void onRestart(){
        restartLoader();
        updateNavigationHeader();
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        mNotepadOpenHelper.close();
        super.onDestroy();
    }
    /**
     * sets course adapter and layout manager to recycler View
     */
    private void displayCourse(){
        mRecyclerView.setAdapter(mCoursesRecyclerAdapter);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        checkNavigationMenuItem(R.id.nav_courses);
    }
    /**
     * displays notes when notes section selected in drawer
     */
    private void showNotes() {
        mRecyclerView.setLayoutManager(mGridLayoutManager);
        mRecyclerView.setAdapter(mNoteRecyclerAdapter);
        checkNavigationMenuItem(R.id.nav_notes);
    }

    /**
     * marks the selected menu item as checked in navigation drawer
     * @param id
     */
    private void checkNavigationMenuItem(int id){
        Menu menu = mNavigationView.getMenu();
        menu.findItem(id).setChecked(true);
    }

    /**
     * gets called when an item in navigation bar gets selected
     * @param item
     * @return
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.nav_notes){
            showNotes();
        }else if(id == R.id.nav_courses){
            displayCourse();
        }
        mSelectedNavMenuItemId = id;

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * closes drawer if it is open when back button pressed
     */
    @Override
    public void onBackPressed() {
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }else{
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.note_list_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.note_list_settings){
        } else if (id == R.id.backup_note_menu) {
        } else if (id == R.id.profile) {
            startActivity(UserLoginActivity.getIntent(this));
        }
        return super.onOptionsItemSelected(item);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        switch (id) {
            case LOADER_NOTES_COURSE_JOINED :
                return getNotesCursor(this);
            case LOADER_COURSES:
                return getCourseCursor(this);
        }
        return null;
    }
    Cursor mNoteCourseJoinedCursor = null;
    Cursor mCourseCursor = null;
    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        int loaderId = loader.getId();
        switch (loaderId) {
            case LOADER_NOTES_COURSE_JOINED :
                mNoteCourseJoinedCursor = data;
                mNoteRecyclerAdapter = new NoteRecyclerAdapter(this,mNoteCourseJoinedCursor);
                checkRequestType();
                break;
            case LOADER_COURSES :
                mCourseCursor = data;
                mCoursesRecyclerAdapter = new CoursesRecyclerAdapter(this,mCourseCursor);
                checkRequestType();
                break;
        }
    }

    private void checkRequestType() {
        if (mSelectedNavMenuItemId == R.id.nav_courses) {
            displayCourse();
        } else if (mSelectedNavMenuItemId == R.id.nav_notes) {
            showNotes();
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        int loaderId = loader.getId();
        switch (loaderId) {
            case LOADER_NOTES_COURSE_JOINED :
                mNoteCourseJoinedCursor.close();
                break;
            case LOADER_COURSES :
                mCourseCursor.close();
                break;
        }
    }
}