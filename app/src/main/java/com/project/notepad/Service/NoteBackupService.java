package com.project.notepad.Service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 */
public class NoteBackupService extends IntentService {

    private static final String TAG = "NoteBackupService";
    public NoteBackupService() {
        super("NoteBackupService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            //uploads notes to remote database
            Log.d(TAG, "onOptionsItemSelected: Backup Started");
            Toast.makeText(this, "Backup Started", Toast.LENGTH_SHORT).show();
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Toast.makeText(this, "Backup Finished", Toast.LENGTH_SHORT).show();
        }
    }
}
