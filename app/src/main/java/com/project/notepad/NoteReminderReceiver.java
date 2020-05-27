package com.project.notepad;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.project.notepad.Utility.NotificationUtil;

public class NoteReminderReceiver extends BroadcastReceiver {
    public static final String NOTE_TEXT = "com.project.notepad.NOTE_TEXT";
    public static final String NOTE_TITLE = "com.project.notepad.NOTE_TITLE";
    public static final String NOTE_ID = "com.project.notepad.NOTE_ID";
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            String noteTitle = intent.getStringExtra(NOTE_TITLE);
            String noteText = intent.getStringExtra(NOTE_TEXT);
            long id = intent.getLongExtra(NOTE_ID,-1);
            NotificationUtil.generateNotification(context,id,noteTitle,noteText);
        }
    }
    public static Intent getIntent(Context context , String noteText ,String noteTitle,long noteId){
        Intent intent = new Intent(context,NoteReminderReceiver.class);
        intent.putExtra(NOTE_TEXT,noteText);
        intent.putExtra(NOTE_TITLE,noteTitle);
        intent.putExtra(NOTE_ID,noteId);
        return intent;
    }
}
