package com.project.notepad;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.project.notepad.Utility.NotificationUtil;

public class NoteReminderReceiver extends BroadcastReceiver {
    private static final String TAG = "NoteReminderReceiver";
    public static final String NOTE_TEXT = "com.project.notepad.NOTE_TEXT";
    public static final String NOTE_TITLE = "com.project.notepad.NOTE_TITLE";
    public static final String NOTE_ID = "com.project.notepad.NOTE_POSITION";
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            String noteTitle = intent.getStringExtra(NOTE_TITLE);
            String noteText = intent.getStringExtra(NOTE_TEXT);
            long id = intent.getLongExtra(NOTE_ID,-1);
            if (id == -1) {
                Log.e(TAG, "onReceive: " + " Unable show Note As Note Id is Invalid" );
                return;
            }
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
