package com.project.notepad.Utility;

import android.os.StrictMode;

import com.project.notepad.BuildConfig;

public class GeneralUtility {
    public static class StrictMode{
        public static void enableStrictMode() {
            if(BuildConfig.DEBUG) {
                android.os.StrictMode.ThreadPolicy threadPolicy = new android.os.StrictMode.ThreadPolicy.Builder()
                        .detectAll()
                        .penaltyLog()
                        .build();
                android.os.StrictMode.setThreadPolicy(threadPolicy);
            }
        }
    }
}
