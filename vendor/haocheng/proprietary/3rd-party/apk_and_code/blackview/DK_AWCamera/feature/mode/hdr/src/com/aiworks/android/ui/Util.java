package com.aiworks.android.ui;

import android.content.Context;
import android.util.DisplayMetrics;

public class Util {
    public static final float DENSTIY_240_VALUE = 0.9f;
    public static boolean isNeedRotate = false;
    private static Context mContext;
    private static float sPixelDensity = 1;
    public static DisplayMetrics displayMetrics;
    public static float actualDensity = 1;
    public static boolean SHOW_SHUTTBUTTON = true;

    public static float getDisplayDensity(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        float density = dm.densityDpi;
        String string = String.valueOf((int) density);
        if ("320".equalsIgnoreCase(string)) {
            density = 1f;
        } else if ("240".equalsIgnoreCase(string)) {
            density = DENSTIY_240_VALUE;
        } else {
            density = 1f;
        }
        return density;
    }

    public static int dpToPixel(int dp) {
        return Math.round(sPixelDensity * dp);
    }

    public static int dpToActualPixel(int dp) {
        return Math.round(actualDensity * dp);
    }

    public static float dpToPixel(float dp) {
        return Math.round(sPixelDensity * dp);
    }

    public static float dpToActualPixel(float dp) {
        return Math.round(actualDensity * dp);
    }

    public static void setMetrics(Context context, DisplayMetrics metrics) {
        mContext = context;
        displayMetrics = metrics;
        sPixelDensity = metrics.density;
        actualDensity = sPixelDensity;
    }
}
