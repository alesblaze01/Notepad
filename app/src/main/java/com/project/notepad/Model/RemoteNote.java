package com.project.notepad.Model;
public class RemoteNote {
    public int id;
    public String courseTitle;
    public String userEmail;
    public String noteTitle;
    public String noteText;

    public RemoteNote() {
    }

    public RemoteNote(String courseTitle, String userEmail,
                      String noteTitle, String noteText) {
        this.courseTitle = courseTitle;
        this.userEmail = userEmail;
        this.noteTitle = noteTitle;
        this.noteText = noteText;
    }

    @Override
    public String toString() {
        return "RemoteNote{" +
                "id=" + id +
                ", courseTitle='" + courseTitle + '\'' +
                ", userEmail='" + userEmail + '\'' +
                ", noteTitle='" + noteTitle + '\'' +
                ", noteText='" + noteText +
                '}';
    }
}
