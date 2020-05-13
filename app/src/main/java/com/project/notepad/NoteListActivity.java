package com.project.notepad;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.project.notepad.Adpater.CoursesRecyclerAdapter;
import com.project.notepad.Adpater.NoteRecyclerAdapter;
import com.project.notepad.Contract.NotepadOpenHelper;
import com.project.notepad.Contract.NotesDatabaseContract.CourseInfoEntry;
import com.project.notepad.Contract.NotesDatabaseContract.NotesInfoEntry;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class NoteListActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private NoteRecyclerAdapter mNoteRecyclerAdapter;
    private LinearLayoutManager mLinearLayoutManager;
    private RecyclerView mRecyclerView;
    private CoursesRecyclerAdapter mCoursesRecyclerAdapter;
    private GridLayoutManager mGridLayoutManager;
    private NotepadOpenHelper mNotepadOpenHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //creates Open Helper instance
        mNotepadOpenHelper = new NotepadOpenHelper(this);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(NoteListActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.open_navigation,R.string.close_navigation);
        drawerLayout.setDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_bar);
        navigationView.setNavigationItemSelectedListener(this);

        initializeDisplayContent();
    }

    /**
     * gets called after onStop() when activity resumes
     */
    @Override
    protected void onResume() {
        mNoteRecyclerAdapter.changeCursor(getNotesCursor());
        updateNavigationHeaderDisplay();
        super.onResume();
    }

    private void updateNavigationHeaderDisplay() {
        NavigationView navigationView = findViewById(R.id.nav_bar);
        View headerView = navigationView.getHeaderView(0);

        TextView emailPreference = headerView.findViewById(R.id.nav_user_email_address);
        TextView namePreference = headerView.findViewById(R.id.nav_user_name);

        SharedPreferences preference =  PreferenceManager.getDefaultSharedPreferences(this);

        emailPreference.setText(preference.getString(getString(R.string.preference_key_email_address),""));
        namePreference.setText(preference.getString(getString(R.string.preference_key_user_name),""));
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

        mNoteRecyclerAdapter = new NoteRecyclerAdapter(this,getNotesCursor());
        mCoursesRecyclerAdapter = new CoursesRecyclerAdapter(this, getCourseCursor());

        showNotes();
    }

    private Cursor getCourseCursor(){
        final SQLiteDatabase readableDatabase = mNotepadOpenHelper.getReadableDatabase();
        String[] columns = new String[]{
                CourseInfoEntry.COLUMN_COURSE_TITLE,
        };
        return readableDatabase.query(
                CourseInfoEntry.TABLE_NAME,columns,
                null,null,null,
                null,CourseInfoEntry.COLUMN_COURSE_TITLE);
    }

    private Cursor getNotesCursor() {
        final SQLiteDatabase readableDatabase = mNotepadOpenHelper.getReadableDatabase();
        final String[] noteColumns = {
                CourseInfoEntry.COLUMN_COURSE_TITLE,
                NotesInfoEntry.COLUMN_NOTE_TITLE,
                NotesInfoEntry.getQualifiedName(NotesInfoEntry._ID)
        };
        String joinedTable = String.format(
                "%s JOIN %s ON %s=%s",
                NotesInfoEntry.TABLE_NAME ,
                CourseInfoEntry.TABLE_NAME,
                NotesInfoEntry.getQualifiedName(NotesInfoEntry.COLUMN_COURSE_ID),
                CourseInfoEntry.getQualifiedName(CourseInfoEntry.COLUMN_COURSE_ID)
                );

        String orderNotesBy = CourseInfoEntry.COLUMN_COURSE_TITLE +
                "," + NotesInfoEntry.COLUMN_NOTE_TITLE+" DESC";
        return readableDatabase.query(
                joinedTable, noteColumns,
                null, null, null,
                null, orderNotesBy);
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
        NavigationView navigationView = findViewById(R.id.nav_bar);
        Menu menu = navigationView.getMenu();
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
            startActivity(new Intent(this,SettingsActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
}
