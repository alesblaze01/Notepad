package com.project.notepad.Utility;

import com.project.notepad.Model.CourseInfo;
import com.project.notepad.Model.ModuleInfo;
import com.project.notepad.Model.NoteInfo;

import java.util.ArrayList;
import java.util.List;

public class DataManager {
    private static DataManager ourInstance = null;

    private List<CourseInfo> mCourses = new ArrayList<>();
    private List<NoteInfo> mNotes = new ArrayList<>();

    public static DataManager getInstance() {
        if(ourInstance == null) {
            ourInstance = new DataManager();
        }
        return ourInstance;
    }
    public List<NoteInfo> getNotes() {
        return mNotes;
    }

    public List<CourseInfo> getCourses() {
        return mCourses;
    }

    public CourseInfo getCourse(String id) {
        for (CourseInfo course : mCourses) {
            if (id.equals(course.getCourseId()))
                return course;
        }
        return null;
    }

    private DataManager() {
    }
}
