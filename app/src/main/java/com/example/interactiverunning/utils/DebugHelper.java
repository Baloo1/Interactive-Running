package com.example.interactiverunning.utils;

import android.os.Debug;
import android.util.Log;
import android.view.WindowManager;

import com.example.interactiverunning.BuildConfig;
import com.example.interactiverunning.MainActivity;

public class DebugHelper {
    public static void keepScreenOn(MainActivity mainActivity) {
        if (BuildConfig.DEBUG) {
            if (Debug.isDebuggerConnected()) {
                Log.d("SCREEN", "Keeping screen on for debugging, detach debugger and force an onResume to turn it off.");
                mainActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            } else {
                mainActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                Log.d("SCREEN", "Keeping screen on for debugging is now deactivated.");
            }
        }
    }
}