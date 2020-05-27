package com.project.notepad;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.project.notepad.Contract.NoteContentContract;
import com.project.notepad.Contract.NoteContentContract.Courses;

public class CourseActivity extends AppCompatActivity {

    public static final String COURSE_ID = "courseId";
    public static final String COURSE_TITLE = "courseTitle";
    public static final String COURSE_DB_ID = "courseDbId";
    private EditText mCourseTitleEditView;
    private EditText mCourseIdEditView;
    private String mCourseId;
    private String mCourseTitle;
    private int mCourseDbId;
    private static final String TAG = "CourseActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course);

        mCourseIdEditView = findViewById(R.id.course_id);
        mCourseTitleEditView = findViewById(R.id.course_title);

        //take courseId and course Title from intent
        getCourseDataFromIntent();
    }

    private void getCourseDataFromIntent() {
        final Intent intent = getIntent();
        if (intent != null) {
            mCourseId = intent.getStringExtra(COURSE_ID);
            mCourseTitle = intent.getStringExtra(COURSE_TITLE);
            mCourseDbId = intent.getIntExtra(COURSE_DB_ID, -1);
            displayCourse(mCourseId, mCourseTitle);
        } else {
            mCourseId = null;
            mCourseTitle = null;
            mCourseDbId = -1;
        }
    }

    private boolean isNew() {
        return mCourseId == null && mCourseTitle == null;
    }

    private void displayCourse(String courseId, String courseTitle) {
        mCourseIdEditView.setText(courseId);
        mCourseTitleEditView.setText(courseTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = new MenuInflater(this);
        menuInflater.inflate(R.menu.course_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.save_course) {
            if (isNew()) {
                mCourseId = mCourseIdEditView.getText().toString();
                mCourseTitle = mCourseTitleEditView.getText().toString();
                insertCourseToDB();
            } else {
                mCourseId = mCourseIdEditView.getText().toString();
                mCourseTitle = mCourseTitleEditView.getText().toString();
                updateCourse();
            }
        } else if (id == R.id.delete_course) {
            if (!isNew()) {
                deleteCourseForDb();
            }
        }
        finish();
        return super.onOptionsItemSelected(item);
    }

    private void deleteCourseForDb() {
        Uri courseRowUri = ContentUris.withAppendedId(Courses.CONTENT_URI,mCourseDbId);

        final int[] delete = {0};
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                delete[0] = getContentResolver().delete(courseRowUri, null, null);
            }
        });
        thread.start();
        while (thread.getState() != Thread.State.TERMINATED) {}

        if (delete[0] >= 1) {
            Log.d(TAG, "deleteCourseForDb: " + delete[0] + " Items deleted");
            Toast.makeText(this, delete[0]+" Course Deleted", Toast.LENGTH_SHORT).show();
        }else {
            Log.d(TAG, "deleteCourseForDb: " + delete[0] + " Items deleted");
            Toast.makeText(this, "Failed to delete course", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateCourse() {
        Uri courseRowUri = Uri.parse(Courses.CONTENT_URI + "/" + mCourseDbId);
        final int[] update = {0};
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                 update[0] = getContentResolver().update(courseRowUri,
                        getContentValues(), null, null);
            }
        });
        thread.start();
        while (thread.getState() != Thread.State.TERMINATED) {}

        handleOutput(update[0]);
    }

    private void handleOutput(int updateResult) {
        if (updateResult == 1) {
            finish();
            Log.d(TAG, "onOptionsItemSelected: Updated Course Successfully");
            Toast.makeText(this, "Course Updated", Toast.LENGTH_SHORT).show();
        }
        else {
            Log.d(TAG, "onOptionsItemSelected: Failed to Update Course");
            Toast.makeText(this, "Course Update Failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void insertCourseToDB() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                final Uri insertUri = getContentResolver().insert(Courses.CONTENT_URI, getContentValues());
                if (insertUri != null) {
                    mCourseDbId = (int) ContentUris.parseId(insertUri);
                } else {
                    Log.d(TAG, "run: Failed to insert");
                }
            }
        });
        thread.start();
        while (thread.getState() != Thread.State.TERMINATED) {
        }

        Log.d(TAG, "onOptionsItemSelected: " + mCourseDbId);
        Toast.makeText(this, "Course Created!", Toast.LENGTH_SHORT).show();
    }

    private ContentValues getContentValues() {
        ContentValues values = new ContentValues();
        values.put(Courses.COURSE_ID, mCourseId);
        values.put(Courses.COURSE_TITLE, mCourseTitle);
        return values;
    }

    public static Intent getIntent(Context context){
        Intent intent = new Intent(context, CourseActivity.class);
        return intent;
    }
}
