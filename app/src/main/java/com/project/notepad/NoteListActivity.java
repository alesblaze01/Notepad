package com.project.notepad;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.loader.app.LoaderManager;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.project.notepad.Adpater.CoursesRecyclerAdapter;
import com.project.notepad.Adpater.NoteRecyclerAdapter;
import com.project.notepad.Contract.NoteContentContract.Courses;
import com.project.notepad.Contract.NoteContentContract.NotesCourseJoined;
import com.project.notepad.Contract.NotepadOpenHelper;
import com.project.notepad.Contract.NotesDatabaseContract.CourseInfoEntry;
import com.project.notepad.Contract.NotesDatabaseContract.NotesInfoEntry;
import com.project.notepad.Service.NoteBackupService;
import com.project.notepad.Utility.UserAccount;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.StrictMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import static com.project.notepad.Contract.NoteContentContract.LOADER_COURSES;
import static com.project.notepad.Contract.NoteContentContract.LOADER_NOTES_COURSE_JOINED;

public class NoteListActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, LoaderManager.LoaderCallbacks<Cursor> {
    private NoteRecyclerAdapter mNoteRecyclerAdapter;
    private LinearLayoutManager mLinearLayoutManager;
    private RecyclerView mRecyclerView;
    private CoursesRecyclerAdapter mCoursesRecyclerAdapter;
    private GridLayoutManager mGridLayoutManager;
    private NotepadOpenHelper mNotepadOpenHelper;
    private NavigationView mNavigationView;
//    private SharedPreferences[] mPreferences;
    private UserAccount mUserAccount;
    private static final String TAG = "NoteListActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mNavigationView = findViewById(R.id.nav_bar);
        mNavigationView.setNavigationItemSelectedListener(this);
        mUserAccount = UserAccount.getInstance(this);
        if(mUserAccount.ifPreviouslyLogin()) {
            updateNavigationHeader();
        }
//        enableStrictMode();
        mNotepadOpenHelper = new NotepadOpenHelper(this);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Menu navMenu = mNavigationView.getMenu();
                if(navMenu.findItem(R.id.nav_courses).isChecked()) {
                }else if (navMenu.findItem(R.id.nav_notes).isChecked()){
                    Intent intent = new Intent(NoteListActivity.this,MainActivity.class);
                    startActivity(intent);
                }
            }
        });

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.open_navigation,R.string.close_navigation);
        drawerLayout.setDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        LoaderManager.getInstance(this).initLoader(LOADER_NOTES_COURSE_JOINED,null,this);
        LoaderManager.getInstance(this).initLoader(LOADER_COURSES , null , this);

        initializeDisplayContent();
    }

    private void enableStrictMode() {
        if(BuildConfig.DEBUG) {
            StrictMode.ThreadPolicy threadPolicy = new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build();
            StrictMode.setThreadPolicy(threadPolicy);
        }
    }

    /**
     * gets called after onStop() when activity resumes
     */
    @Override
    protected void onResume() {
        super.onResume();
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
    protected void onRestart() {
        LoaderManager.getInstance(this).restartLoader(LOADER_NOTES_COURSE_JOINED,null,this);
        updateNavigationHeader();
        super.onRestart();
    }

    private void updateNavigationHeaderDisplay() {
//        NavigationView navigationView = findViewById(R.id.nav_bar);
//        View headerView = navigationView.getHeaderView(0);
//        TextView emailPreference = headerView.findViewById(R.id.nav_user_email_address);
//        TextView namePreference = headerView.findViewById(R.id.nav_user_name);
//
//        final String[] email = {null};
//        final String[] name = {null};
//        mPreferences = new SharedPreferences[]{null};
//
//        Runnable updaterRunner = () -> {
//            if(mPreferences[0] == null)
//                mPreferences[0] = PreferenceManager.getDefaultSharedPreferences(this);
//            email[0] = mPreferences[0].getString(getString(R.string.preference_key_email_address), "");
//            name[0] = mPreferences[0].getString(getString(R.string.preference_key_user_name), "");
//        };
//        Thread thread = new Thread(updaterRunner);
//        thread.setPriority(Thread.MAX_PRIORITY);
//        thread.start();
//        while (thread.getState() != Thread.State.TERMINATED) {}
//        emailPreference.setText(email[0]);
//        namePreference.setText(name[0]);
    }

    @Override
    protected void onDestroy() {
        mNotepadOpenHelper.close();
        super.onDestroy();
    }

    /**
     * initialise a recycler view adds layout manager
     * to it and associates adapter to the recycler view
     */
    private void initializeDisplayContent() {
        mRecyclerView = findViewById(R.id.list_notes);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mGridLayoutManager = new GridLayoutManager(this, 2);
    }

    private Loader<Cursor> getCourseCursor(){
        String[] columns = new String[]{
                CourseInfoEntry.COLUMN_COURSE_TITLE,
        };
        return new CursorLoader(this, Courses.CONTENT_URI ,
                columns , null , null , Courses.COURSE_TITLE);
    }

    private Loader<Cursor> getNotesCursor() {
        final String[] noteColumns = {
                NotesCourseJoined.COURSE_TITLE,
                NotesCourseJoined.NOTE_TITLE,
                NotesCourseJoined._ID
        };
        String orderNotesBy = CourseInfoEntry.COLUMN_COURSE_TITLE +
                "," + NotesInfoEntry.COLUMN_NOTE_TITLE+" DESC";
        return new CursorLoader(
                this, NotesCourseJoined.CONTENT_URI , noteColumns ,
                null, null , orderNotesBy
        );
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
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
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
            if(mUserAccount.isSignedIn()/*User login*/) {
                Intent intent = new Intent(this, NoteBackupService.class);
                startService(intent);
            }else {
                Toast.makeText(this, "You May Login First", Toast.LENGTH_LONG).show();
            }
        } else if (id == R.id.profile) {
            Intent signInIntent = new Intent(this,UserLoginActivity.class);
            startActivity(signInIntent);
            updateNavigationHeader();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
//        MenuItem signInMenu = menu.findItem(R.id.profile);
//        if(mUserAccount != null) {
//            signInMenu.setVisible(false);
//        }
        return super.onPrepareOptionsMenu(menu);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        switch (id) {
            case LOADER_NOTES_COURSE_JOINED :
                return getNotesCursor();
            case LOADER_COURSES:
                return getCourseCursor();
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
                showNotes();
                break;
            case LOADER_COURSES :
                mCourseCursor = data;
                mCoursesRecyclerAdapter = new CoursesRecyclerAdapter(this,mCourseCursor);
                break;
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