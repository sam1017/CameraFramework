package com.aiworks.android.lowlight;

import android.util.Log;

import java.io.File;

public class NightShotConfig {

    private static final String TAG = NightShotConfig.class.getSimpleName();

    private static String mDumpFilePath;
    private static String mDumpFileTitle;
    private static boolean mIsDumpFile;

    public static void setDumpFilePath(String path) {
        mDumpFilePath = path;
        mIsDumpFile = new File(mDumpFilePath + "dump").exists();
        Log.w(TAG, "setDumpFilePath = " + path + ", mIsDumpFile = " + mIsDumpFile);
    }
    public static String getDumpFilePath() {
        return mDumpFilePath + mDumpFileTitle;
    }

    public static String getDumpFileTitle() {
        return mDumpFileTitle;
    }

    public static void setDumpFileTitle(String title) {
        mDumpFileTitle = title;
    }

    public static boolean isDump() {
        return mIsDumpFile;
    }

}
