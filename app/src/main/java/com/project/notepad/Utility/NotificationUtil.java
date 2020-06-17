package com.project.notepad.Utility;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.project.notepad.MainActivity;
import com.project.notepad.R;

import static com.project.notepad.MainActivity.SHOW_NOTE;
import static com.project.notepad.NoteReminderReceiver.NOTE_ID;

public class NotificationUtil {

    public static final String NOTIFICATION_CHANNEL_1 = "Channel1";

    public static void generateNotification(Context context, long noteId, String noteTitle , String noteText) {
        PendingIntent pendingIntent = getPendingIntent(context,noteId);
        NotificationCompat.Builder notificationBuilder = getNotificationBuild(context, noteTitle, noteText, pendingIntent);
        NotificationManagerCompat.from(context).notify(SHOW_NOTE,notificationBuilder.build());
    }

    private static PendingIntent getPendingIntent(Context context,long noteId) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(NOTE_ID,noteId);
        return PendingIntent
                .getActivity(context , SHOW_NOTE , intent , PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static NotificationCompat.Builder getNotificationBuild(Context context, String noteTitle, String noteText, PendingIntent pendingIntent) {
        return new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_1)
                .setContentTitle("Note Reminder : " + noteTitle)
                .setSmallIcon(R.drawable.ic_speaker_notes_black_24dp)
                .setContentText(noteText)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(noteText).setBigContentTitle("Note Reminder : " + noteTitle))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
    }
}
