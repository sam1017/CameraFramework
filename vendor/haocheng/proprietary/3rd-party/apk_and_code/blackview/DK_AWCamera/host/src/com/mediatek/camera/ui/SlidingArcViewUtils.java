package com.mediatek.camera.ui;

/**
 * Created by aland on 17-5-27.
 */
import android.app.Activity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.View;

import com.mediatek.camera.R;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;

public class SlidingArcViewUtils {
    private static final LogUtil.Tag TAG
            = new LogUtil.Tag(SlidingArcViewUtils.class.getSimpleName());
    private static int screenW;
    private static int screenH;
    private static float screenDensity;
    private static Activity mActivity;
    private static View mDestView;
    private static Animation fadeOutAnimation;
    private static Animation fadeInAnimation;

    public static void initScreen(Activity activity){
        mActivity = activity;
        DisplayMetrics metric = new DisplayMetrics();
        mActivity.getWindowManager().getDefaultDisplay().getMetrics(metric);
        screenW = metric.widthPixels;
        screenH = metric.heightPixels;
        screenDensity = metric.density;

        LogHelper.d(TAG, "screenW = " + screenW + " ;screenH = " + screenH);
    }
    public static void fadeInOutView(View view,boolean in){
        mDestView = view;
        if(in){
            mDestView.startAnimation(fadeInAnimation);
        }else{
            mDestView.startAnimation(fadeOutAnimation);
        }
    }
    public static int getScreenW(){
        return screenW;
    }
    public static int getScreenH(){
        return screenH;
    }
    public static float getScreenDensity(){
        return screenDensity;
    }
    public static int dp2px(float dpValue) {
        return (int) (dpValue * getScreenDensity() + 0.5f);
    }
    public static int px2dp(float pxValue) {
        return (int) (pxValue / getScreenDensity() + 0.5f);
    }
}
