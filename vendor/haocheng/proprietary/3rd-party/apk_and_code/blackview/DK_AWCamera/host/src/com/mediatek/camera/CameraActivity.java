/*
 *   Copyright Statement:
 *
 *     This software/firmware and related documentation ("MediaTek Software") are
 *     protected under relevant copyright laws. The information contained herein is
 *     confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 *     the prior written permission of MediaTek inc. and/or its licensors, any
 *     reproduction, modification, use or disclosure of MediaTek Software, and
 *     information contained herein, in whole or in part, shall be strictly
 *     prohibited.
 *
 *     MediaTek Inc. (C) 2016. All rights reserved.
 *
 *     BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 *    THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 *     RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 *     ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 *     WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 *     WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 *     NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 *     RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 *     INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 *     TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 *     RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 *     OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 *     SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 *     RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 *     STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 *     ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 *     RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 *     MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 *     CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 *     The following software/firmware and/or related documentation ("MediaTek
 *     Software") have been modified by MediaTek Inc. All revisions are subject to
 *     any receiver's applicable license agreements with MediaTek Inc.
 */

package com.mediatek.camera;
import android.view.MotionEvent;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.StatusBarManager;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.mediatek.camera.common.IAppUi;
import com.mediatek.camera.common.IAppUiListener.OnThumbnailClickedListener;
import com.mediatek.camera.common.ICameraContext;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.CameraSysTrace;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil.Tag;
import com.mediatek.camera.common.debug.profiler.IPerformanceProfile;
import com.mediatek.camera.common.debug.profiler.PerformanceTracker;
import com.mediatek.camera.common.device.CameraDeviceManagerFactory;
import com.mediatek.camera.common.mode.IModeListener;
import com.mediatek.camera.common.mode.ModeManager;
import com.mediatek.camera.common.utils.CameraPerformanceTrace;
import com.mediatek.camera.common.utils.CameraUtil;
import com.mediatek.camera.common.utils.PriorityConcurrentSkipListMap;
import com.mediatek.camera.common.widget.RotateLayout;
import com.mediatek.camera.portability.WifiDisplayStatusEx;
import com.mediatek.camera.portability.pq.PictureQuality;
import com.mediatek.camera.ui.CameraAppUI;

import androidx.annotation.Nullable;
/* hct.wangsenhao, underwater camera @{ */
import android.provider.Settings;
import com.mediatek.camera.common.utils.CameraUtil;
import android.graphics.Color;
/* }@ hct.wangsenhao */

//add by huangfei for shutter title change start
import com.mediatek.camera.ui.shutter.ShutterButtonManager;
//add by huangfei for shutter title change end
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import com.mediatek.camera.feature.mode.visualsearch.activity.ImageParseActivity;
import android.content.SharedPreferences;
import android.util.DisplayMetrics;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
//add by huangfei for display abnormal when diplay size change in settings start
import android.view.IWindowManager;
import android.view.WindowManagerGlobal;
import android.view.Display;
//add by huangfei for display abnormal when diplay size change in settings end
//add by huangfei for location abnormal start
import com.mediatek.camera.common.permission.PermissionManager;
//add by huangfei for location abnormal end
import com.mediatek.camera.feature.mode.visualsearch.bean.DataHolder;

//*/ hct.huangfei, 20201121. add bokeh mode.
import com.mediatek.camera.common.mode.ICameraMode;
//*/ 
import com.mediatek.camera.common.relation.DataStore;
import com.mediatek.camera.common.mode.CameraModeBase;

//bv wuyonglin add for aiworks facebeauty and bokeh 20200722 start
import com.mediatek.camera.feature.mode.aiworksfacebeauty.util.FaceModelUtil;
import com.mediatek.camera.feature.mode.aiworksbokeh.util.AiWorksCameraUtil;
import android.os.Environment;
import java.io.File;
//bv wuyonglin add for aiworks facebeauty and bokeh 20200722 end

/**
 * Camera app's activity.
 * used to manager the app's life cycle, transfer system information
 * (such as key event, configuration change event ....).
 * Create app common UI and add to the activity view tree.
 */
public class CameraActivity extends PermissionActivity implements IApp {
    private static final Tag TAG = new Tag(CameraActivity.class.getSimpleName());
    private static final int MSG_CLEAR_SCREEN_ON_FLAG = 0;
    private static final int MSG_SET_SCREEN_ON_FLAG = 1;
    private static final int DELAY_MSG_SCREEN_SWITCH = 2 * 60 * 1000; // 2min

    //add by huangfei for watermark start
    private static final int MSG_WATER_MARK_THUMBNAIL_ENABLE = 3;
    private static final int MSG_WATER_MARK_THUMBNAIL_DISABLE = 4;
    //add by huangfei for watermark end

    // Orientation hysteresis amount used in rounding, in degrees
    private static final int ORIENTATION_HYSTERESIS = 5;
    private static final String REVIEW_ACTION = "com.android.camera.action.REVIEW";
    private static final String EXTRA_USE_FRONT_CAMERA_FOR_GOOGLE
      = "com.google.assistant.extra.USE_FRONT_CAMERA";
    private static final String EXTRA_USE_FRONT_CAMERA_FOR_ANDROID
      = "android.intent.extra.USE_FRONT_CAMERA";
    private static final String IS_CAMERA = "isCamera";

    private CameraAppUI mCameraAppUI;
    private PriorityConcurrentSkipListMap<String, KeyEventListener> mKeyEventListeners =
            new PriorityConcurrentSkipListMap<String, KeyEventListener>(true);

    private PriorityConcurrentSkipListMap<String, BackPressedListener> mBackPressedListeners =
            new PriorityConcurrentSkipListMap<String, BackPressedListener>(true);

    private IModeListener mIModeListener;
    private boolean mIsResumed;
    //*/ hct.huangfei, 20210317.add volume key function.
    private boolean isLockLongPressKey = false;
    private KeyLongEventListener mKeyLongEventListener = null;
    //*/
    private DataStore mDataStore;
    //add by Jerry
    private boolean mIsNavBarFlag = false;
    private static final String NAVIGATION_BAR_STATUS = "navigation_bar_status";
    private StatusBarManager mStatusBarManager = null;
    private final List<OnOrientationChangeListener>
            mOnOrientationListeners = new ArrayList<>();
    private int mOrientation = OrientationEventListener.ORIENTATION_UNKNOWN;
    private OrientationEventListener mOrientationListener;
    protected Uri mUri;

    //add by huangfei for watermark start
    private boolean mThumbnailClick = true;
    //add by huangfei for watermark end

    //bv wuyonglin add for aiworks facebeauty and bokeh 20200722 start
    public static final String MODEL_NAME = "AIWorksModels";
    //bv wuyonglin add for aiworks facebeauty and bokeh 20200722 end


    protected OnThumbnailClickedListener mThumbnailClickedListener =
            new OnThumbnailClickedListener() {
                @Override
                public void onThumbnailClicked() {

                    //add by huangfei for watermark start
                    if(!mThumbnailClick && mCameraAppUI.isWaterMarkOn()){
                        LogHelper.i(TAG, "onThumbnailClicked = " + mThumbnailClick);
                        return;
                    }
                    //add by huangfei for watermark end

					//bv liangchangwei for HDR
                    if(mCameraAppUI.isHdrPictureProcessing()){
                        mCameraAppUI.showScreenToastView();
                        return;
                    }
                    goToGallery(mUri);
                }
            };

    @Override
    protected void onNewIntentTasks(Intent newIntent) {
        super.onNewIntentTasks(newIntent);
    }

    @Override
    protected void onCreateTasks(Bundle savedInstanceState) {
		//add by huangfei for display abnormal when diplay size change in settings start
        setDefaultDisplay(this);
        //add by huangfei for display abnormal when diplay size change in settings end
        CameraSysTrace.onEventSystrace("CameraActivity.onCreateTasks", true, true);
        if (ActivityManager.isUserAMonkey()) {
            LogHelper.d(TAG, "[onCreateTasks] from Monkey");
            invalidateOptionsMenu();
        }
        if (!isThirdPartyIntent(this) && !isOpenFront(this)) {
            CameraUtil.launchCamera(this);
        }else{
            if(CameraUtil.MTKCAM_AIWORKS_VIDEO_HDR_SUPPORT){
                CameraUtil.is_videoHdr_Force = true;
                LogHelper.d(TAG, "[onCreateTasks] CameraUtil.is_videoHdr_Force = true ! ");
            }
        }
        /* hct.wangsenhao, underwater camera @{ */
        isUnderWater();
        /* }@ hct.wangsenhao */
        IPerformanceProfile profile = PerformanceTracker.create(TAG, "onCreate").start();
        super.onCreateTasks(savedInstanceState);
        //if (CameraUtil.isTablet() || WifiDisplayStatusEx.isWfdEnabled(this)) {
        //  setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        //  setRequestedOrientation(CameraUtil.calculateCurrentScreenOrientation(this));
        //}
       //bv wuyonglin add for check do 20201103 start
       com.blackview.service.check.Check.Do(this);
       //bv wuyonglin add for check do 20201103 end

       //bv wuyonglin add for check brand 20200819 start
       if (!android.os.SystemProperties.get("ro.product.brand").equals("Blackview")) {
            android.os.Process.killProcess(android.os.Process.myPid());
       }
       //bv wuyonglin add for check brand 20200819 end

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_LAYOUT_FLAGS
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

	//bv wuyonglin add for hide status bar 20200314 start
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
	//bv wuyonglin add for hide status bar 20200314 end

        CameraSysTrace.onEventSystrace("onCreateTasks.setContentView", true, true);
        setContentView(R.layout.activity_main);
        CameraSysTrace.onEventSystrace("onCreateTasks.setContentView", false, true);
        mOrientationListener = new OrientationEventListenerImpl(this);
        //create common ui module.
        mCameraAppUI = new CameraAppUI(this);
        profile.mark("CameraAppUI initialized.");
        CameraSysTrace.onEventSystrace("onCreateTasks.CameraAppUIInit", true);
        mCameraAppUI.onCreate();
        CameraSysTrace.onEventSystrace("onCreateTasks.CameraAppUIInit", false);
        profile.mark("CameraAppUI.onCreate done.");
        mIModeListener = new ModeManager();
        mIModeListener.create(this);
        profile.mark("ModeManager.create done.");
        profile.stop();
        CameraSysTrace.onEventSystrace("CameraActivity.onCreateTasks", false, true);
        ////HCT liuyi add for volume key continuous increase
        mDataStore = getCameraContext().getDataStore();

        //add by huangfei for watermark start
        setListener();
        //add by huangfei for watermark end
        CameraUtil.isRTL = isRTL();
    }

    private boolean isRTL() {
        Configuration config = getResources().getConfiguration();
        if(config.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL){
            return true;
        }else{
            return false;
        }
    }

    @Override
    protected void onStartTasks() {
        super.onStartTasks();
    }

    @Override
    protected void onResumeTasks() {
        //add by huangfei for display abnormal when diplay size change in settings start
        setDefaultDisplay(this);
        //add by huangfei for display abnormal when diplay size change in settings end
        CameraSysTrace.onEventSystrace("CameraActivity.onResumeTasks", true, true);
        CameraDeviceManagerFactory.setCurrentActivity(this);
        IPerformanceProfile profile = PerformanceTracker.create(TAG, "onResume").start();
        mIsResumed = true;
        /* hct.wangsenhao, underwater camera @{ */
        mStatusBarManager = (StatusBarManager)getSystemService(Context.STATUS_BAR_SERVICE);
        if(CameraUtil.getUnderWaterSupport(this)==1){
            mStatusBarManager.disable(StatusBarManager.DISABLE_EXPAND);
        }
        /* }@ hct.wangsenhao */
        mOrientationListener.enable();
        super.onResumeTasks();
	//bv wuyonglin add for hide status bar 20200314 start
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
	//bv wuyonglin add for hide status bar 20200314 end
       //bv wuyonglin add for bug5934 20210505 start
        if (!isThirdPartyIntent(this)) {
            CameraUtil.is_videoHdr_Force = false;
        }else{
            if(CameraUtil.MTKCAM_AIWORKS_VIDEO_HDR_SUPPORT){
                CameraUtil.is_videoHdr_Force = true;
            }
        }
       //bv wuyonglin add for bug5934 20210505 end
        PictureQuality.enterCameraMode();
        mIModeListener.resume();
        profile.mark("ModeManager resume done.");
        CameraSysTrace.onEventSystrace("onResumeTasks.CameraAppUIResume", true);
        mCameraAppUI.onResume();
        CameraSysTrace.onEventSystrace("onResumeTasks.CameraAppUIResume", false);
        profile.mark("CameraAppUI resume done.");
        mCameraAppUI.setThumbnailClickedListener(mThumbnailClickedListener);
        keepScreenOnForAWhile();
        profile.stop();
        if(isInMultiWindowMode()){
            Toast.makeText(this, R.string.multi_window_mode_toast_lct, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        //add by huangfei for watermark start
        mThumbnailClick = true;
        //add by huangfei for watermark end

        //bv wuyonglin add for aiworks facebeauty and bokeh 20200722 start
        LogHelper.i(TAG, "[onResumeTasks] FaceModelUtil doCopy start");
        if(!FaceModelUtil.isFaceModleExists(this)) {
            FaceModelUtil copy = new FaceModelUtil();
            copy.doCopy(this);
        }
        LogHelper.i(TAG, "[onResumeTasks] FaceModelUtil doCopy end");
        File dir = this.getFilesDir();
        String modelPath = dir.getAbsolutePath() + File.separator + MODEL_NAME;
        AiWorksCameraUtil.copyModle(this, modelPath, MODEL_NAME);
        LogHelper.i(TAG, "[onResumeTasks] AiWorksCameraUtil copyModle end  modelPath ="+modelPath);
        //bv wuyonglin add for aiworks facebeauty and bokeh 20200722 end

        CameraSysTrace.onEventSystrace("CameraActivity.onResumeTasks", false, true);
    }
    @Override
    public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {
        if(isInMultiWindowMode){
            Toast.makeText(this, R.string.multi_window_mode_toast_lct, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onPauseTasks() {
        CameraSysTrace.onEventSystrace("CameraActivity.onPauseTasks", true, true);
        mIsResumed = false;
        super.onPauseTasks();
        PictureQuality.exitCameraMode();
        mIModeListener.pause();
        mCameraAppUI.onPause();
        mOrientationListener.disable();
        resetScreenOn();
        //add by huangfei for watermark start
        mThumbnailClick = true;
        //add by huangfei for watermark end
        /* hct.wangsenhao, underwater camera @{ */
        if(CameraUtil.getUnderWaterSupport(this)==1){
            LogHelper.d(TAG, "UnderWater pause");
            mStatusBarManager.disable(StatusBarManager.DISABLE_NONE);
            Settings.Global.putInt(getContentResolver(), "under_water_value", 0);
            finish();
        }
        /* }@ hct.wangsenhao */
        CameraSysTrace.onEventSystrace("CameraActivity.onPauseTasks", false, true);
    }

    @Override
    protected void onStopTasks() {
        super.onStopTasks();
    }

    @Override
    protected void onDestroyTasks() {
        super.onDestroyTasks();
        mIModeListener.destroy();
        mCameraAppUI.onDestroy();
        if(CameraUtil.MTKCAM_AIWORKS_VIDEO_HDR_SUPPORT){
            CameraUtil.is_videoHdr_Force = false;
        }
        CameraDeviceManagerFactory.release(this);
    }

     /* hct.wangsenhao, underwater camera @{ */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if(CameraUtil.getUnderWaterSupport(this)==1){
            return true;
        } else {
            return super.dispatchTouchEvent(event);
        }
    }
     /* }@ hct.wangsenhao */

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        RotateLayout root = (RotateLayout) findViewById(R.id.app_ui);
        LogHelper.d(TAG, "onConfigurationChanged orientation = " + newConfig.orientation);
        if (root != null) {
            if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
                root.setOrientation(0, false);
            } else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                root.setOrientation(90, false);
            }
            mCameraAppUI.onConfigurationChanged(newConfig);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //*/ hct.huangfei, 20210317.add volume key function.
        //HCT liuyi modify for volume key continuous increase
        if (event.getRepeatCount() != 0 && (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)
                && mKeyLongEventListener != null && isZoomLongPress()) {
            isLockLongPressKey = true;
            mKeyLongEventListener.onKeyLongPress(keyCode, event);
            return true;
        }else {
            isLockLongPressKey=false;
        }
        //*/
        Iterator iterator = mKeyEventListeners.entrySet().iterator();
        KeyEventListener listener = null;
        while (iterator.hasNext()) {
            Map.Entry map = (Map.Entry) iterator.next();
            listener = (KeyEventListener) map.getValue();
            if (listener != null && listener.onKeyDown(keyCode, event)) {
                return true;
            }
            //if (!mCameraAppUI.isSettingShow() && !mCameraAppUI.getModePickerManager().isMoreFragmentShow() && !mCameraAppUI.getIsSelfTimerTextViewShow()) {
            if(keyCode == KeyEvent.KEYCODE_BACK && (mCameraAppUI.getShutterButtonManager().getIsShowMoreModeView() || mCameraAppUI.getShutterButtonManager().getIsInMoreModeView())
			&& !mCameraAppUI.getIsSelfTimerTextViewShow() && !mCameraAppUI.isSettingShow()){	//bv wuyonglin add for bug2019 20200827
                //moveTaskToBack(true);
               if (mCameraAppUI.getShutterButtonManager().getIsCanSwitchMode()) {
                mCameraAppUI.getShutterButtonManager().goToPhotoMode();
               }
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        //*/ hct.huangfei, 20210317.add volume key function.
        if (isLockLongPressKey){
            return true;
        }
        //*/
        Iterator iterator = mKeyEventListeners.entrySet().iterator();
        KeyEventListener listener = null;
        while (iterator.hasNext()) {
            Map.Entry map = (Map.Entry) iterator.next();
            listener = (KeyEventListener) map.getValue();
            if (listener != null && listener.onKeyUp(keyCode, event)) {
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        Iterator iterator = mBackPressedListeners.entrySet().iterator();
        BackPressedListener listener = null;
        while (iterator.hasNext()) {
            Map.Entry map = (Map.Entry) iterator.next();
            listener = (BackPressedListener) map.getValue();
            if (listener != null && listener.onBackPressed()) {
                return;
            }
        }
        super.onBackPressed();
    }

    @Override
    public void onUserInteraction() {
        if (mIModeListener == null || !mIModeListener.onUserInteraction()) {
            super.onUserInteraction();
        }
    }

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public IAppUi getAppUi() {
        return mCameraAppUI;
    }

    @Override
    public void enableKeepScreenOn(boolean enabled) {
        LogHelper.d(TAG, "enableKeepScreenOn enabled " + enabled);
        if (mIsResumed) {
            mMainHandler.removeMessages(MSG_SET_SCREEN_ON_FLAG);
            Message msg = Message.obtain();
            msg.arg1 = enabled ? 1 : 0;
            msg.what = MSG_SET_SCREEN_ON_FLAG;
            mMainHandler.sendMessage(msg);
        }

    }

    @Override
    public void notifyNewMedia(Uri uri, boolean needNotify) {
        mUri = uri;
        if (uri != null && needNotify) {
            ContentResolver cr = getContentResolver();
            String mimeType = cr.getType(mUri);
            if (mimeType != null) {
                if (mimeType.startsWith("image/")) {
                    getApplicationContext().sendBroadcast(
                            new Intent("android.hardware.action.NEW_PICTURE", uri));
                    // Keep compatibility
                    getApplicationContext().sendBroadcast(
                            new Intent("com.android.camera.NEW_PICTURE", uri));
                } else if (mimeType.startsWith("video/")) {
                    getApplicationContext().sendBroadcast(
                            new Intent("android.hardware.action.NEW_VIDEO", uri));
                }
            }
        }
    }

    @Override
    public boolean notifyCameraSelected(String newCameraId) {
        return mIModeListener.onCameraSelected(newCameraId);
    }

    @Override
    public void registerKeyEventListener(KeyEventListener keyEventListener, int priority) {
        if (keyEventListener == null) {
            LogHelper.e(TAG, "registerKeyEventListener error [why null]");
        }
        mKeyEventListeners.put(mKeyEventListeners.getPriorityKey(priority, keyEventListener),
                keyEventListener);
    }

    @Override
    public void registerBackPressedListener(BackPressedListener backPressedListener,
            int priority) {
        if (backPressedListener == null) {
            LogHelper.e(TAG, "registerKeyEventListener error [why null]");
        }
        mBackPressedListeners.put(mBackPressedListeners.getPriorityKey(priority,
                backPressedListener), backPressedListener);
    }

    @Override
    public void unRegisterKeyEventListener(KeyEventListener keyEventListener) {
        if (keyEventListener == null) {
            LogHelper.e(TAG, "unRegisterKeyEventListener error [why null]");
        }
        if (mKeyEventListeners.containsValue(keyEventListener)) {
            mKeyEventListeners.remove(mKeyEventListeners.findKey(keyEventListener));
        }
    }

    @Override
    public void unRegisterBackPressedListener(BackPressedListener backPressedListener) {
        if (backPressedListener == null) {
            LogHelper.e(TAG, "unRegisterBackPressedListener error [why null]");
        }
        if (mBackPressedListeners.containsValue(backPressedListener)) {
            mBackPressedListeners.remove(mBackPressedListeners.findKey(backPressedListener));
        }
    }

    @Override
    public void registerOnOrientationChangeListener(OnOrientationChangeListener listener) {
        synchronized (mOnOrientationListeners) {
            if (!mOnOrientationListeners.contains(listener)) {
                if (mOrientation != OrientationEventListener.ORIENTATION_UNKNOWN) {
                    listener.onOrientationChanged(mOrientation);
                }
                mOnOrientationListeners.add(listener);
            }
        }
    }

    @Override
    public void unregisterOnOrientationChangeListener(OnOrientationChangeListener listener) {
        synchronized (mOnOrientationListeners) {
            if (mOnOrientationListeners.contains(listener)) {
                mOnOrientationListeners.remove(listener);
            }
        }
    }

    @Override
    public int getGSensorOrientation() {
        synchronized (mOnOrientationListeners) {
            return mOrientation;
        }
    }

    @Override
    public void enableGSensorOrientation() {
        if (mIsResumed) {
            // can not enable, after activity paused
            mOrientationListener.enable();
        }
    }

    @Override
    public void disableGSensorOrientation() {
        //always run disable, since settings request
        mOrientationListener.disable();
    }

    /**
     * start gallery activity to browse the file withe specified uri.
     *
     * @param uri The specified uri of file to browse.
     */
    protected void goToGallery(Uri uri) {
        if (uri == null) {
            LogHelper.d(TAG, "uri is null, can not go to gallery");
            return;
        }
        String mimeType = getContentResolver().getType(uri);

        //add by huangfei for minetye null start
        if(mimeType == null){
            LogHelper.e(TAG, "mimeType is null,use jpeg minetype");
            mimeType = "image/jpeg";
        }
        //add by huangfei for minetye null end

        LogHelper.d(TAG, "[goToGallery] uri: " + uri + ", mimeType = " + mimeType);
        Intent intent = new Intent(REVIEW_ACTION);
        intent.setDataAndType(uri, mimeType);
        intent.putExtra(IS_CAMERA, true);
        // add this for screen pinning
        ActivityManager activityManager = (ActivityManager)
                getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (activityManager.LOCK_TASK_MODE_PINNED == activityManager
                    .getLockTaskModeState()) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT
                        | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            }
            if (isVoiceInteractionRoot()) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
        }
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            LogHelper.e(TAG, "[startGalleryActivity] Couldn't view ", ex);
        }
    }

    private Handler mMainHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            LogHelper.d(TAG, "handleMessage what = " + msg.what + " arg1 = " + msg.arg1);
            switch (msg.what) {
                case MSG_CLEAR_SCREEN_ON_FLAG:
                    getWindow().clearFlags(
                            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    //bv wuyonglin add for 120s not touch screen will exit camera 20200924 start
                    finish();
                    //bv wuyonglin add for 120s not touch screen will exit camera 20200924 end
                    break;
                case MSG_SET_SCREEN_ON_FLAG:
                    boolean enabled = msg.arg1 == 1;
                    if (enabled) {
                        keepScreenOn();
                    } else {
                        keepScreenOnForAWhile();
                    }
                    break;

                //add by huangfei for watermark start     
                case MSG_WATER_MARK_THUMBNAIL_ENABLE:
                    mMainHandler.removeMessages(MSG_WATER_MARK_THUMBNAIL_ENABLE);
                    mThumbnailClick = true;
                    break;
                case MSG_WATER_MARK_THUMBNAIL_DISABLE:
                    mMainHandler.removeMessages(MSG_WATER_MARK_THUMBNAIL_DISABLE);
                    mThumbnailClick = false;
                    break;  
                //add by huangfei for watermark end    
                    
                default:
                    break;
            }
        };
    };

    private void resetScreenOn() {
        mMainHandler.removeMessages(MSG_SET_SCREEN_ON_FLAG);
        mMainHandler.removeMessages(MSG_CLEAR_SCREEN_ON_FLAG);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void keepScreenOnForAWhile() {
        mMainHandler.removeMessages(MSG_CLEAR_SCREEN_ON_FLAG);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mMainHandler.sendEmptyMessageDelayed(MSG_CLEAR_SCREEN_ON_FLAG,
                DELAY_MSG_SCREEN_SWITCH);
    }

    private void keepScreenOn() {
        mMainHandler.removeMessages(MSG_CLEAR_SCREEN_ON_FLAG);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /**
     * The implementer of OrientationEventListener.
     */
    private class OrientationEventListenerImpl extends OrientationEventListener {
        public OrientationEventListenerImpl(Context context) {
            super(context);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            // We keep the last known orientation. So if the user first orient
            // the camera then point the camera to floor or sky, we still have
            // the correct orientation.
            if (orientation == ORIENTATION_UNKNOWN) {
                return;
            }
            synchronized (mOnOrientationListeners) {
                final int roundedOrientation = roundOrientation(orientation, mOrientation);
                if (mOrientation != roundedOrientation) {
                    mOrientation = roundedOrientation;
                    LogHelper.i(TAG, "mOrientation = " + mOrientation);
                    for (OnOrientationChangeListener l : mOnOrientationListeners) {
                        l.onOrientationChanged(mOrientation);
                    }
                }
            }
        }
    }

    private static int roundOrientation(int orientation, int orientationHistory) {
        boolean changeOrientation = false;
        if (orientationHistory == OrientationEventListener.ORIENTATION_UNKNOWN) {
            changeOrientation = true;
        } else {
            int dist = Math.abs(orientation - orientationHistory);
            dist = Math.min(dist, 360 - dist);
            changeOrientation = (dist >= 45 + ORIENTATION_HYSTERESIS);
        }
        if (changeOrientation) {
            return ((orientation + 45) / 90 * 90) % 360;
        }
        return orientationHistory;
    }

    /**
     * Judge current is launch by intent.
     * @param activity the launch activity.
     * @return true means is launch by intent; otherwise is false.
     */
    public boolean isThirdPartyIntent(Activity activity) {
        Intent intent = activity.getIntent();
        String action = intent.getAction();
        boolean value = MediaStore.ACTION_IMAGE_CAPTURE.equals(action) ||
                MediaStore.ACTION_IMAGE_CAPTURE_SECURE.equals(action) ||
                MediaStore.ACTION_VIDEO_CAPTURE.equals(action);
        return value;
    }

    //add by huangfei for campostalgo.FeatureResult null start
    public boolean isSecureCameraIntent(Activity activity) {
        Intent intent = activity.getIntent();
        String action = intent.getAction();
        boolean value = "android.media.action.STILL_IMAGE_CAMERA_SECURE".equals(action);
        return value;
    }
    //add by huangfei for campostalgo.FeatureResult null end
    //add by Jerry
    public boolean isVideoIntent() {
        Intent intent = getIntent();
        String action = intent.getAction();
        boolean value = MediaStore.ACTION_VIDEO_CAPTURE.equals(action);
        return value;
    }
    //add by huangfei for display abnormal when diplay size change in settings start
    public int getDefaultDisplayDensity(int displayId) {
        try {
            
            final IWindowManager wm = WindowManagerGlobal.getWindowManagerService();
            return wm.getInitialDisplayDensity(displayId);
        } catch (RemoteException exc) {
            return -1;
        }
    }
    public  void setDefaultDisplay(Context context) {
        if(Build.VERSION.SDK_INT > 23) {
            Configuration origConfig = context.getResources().getConfiguration();
            origConfig.densityDpi = getDefaultDisplayDensity(Display.DEFAULT_DISPLAY);//获取手机出厂时默认的densityDpi
            context.getResources().updateConfiguration(origConfig, context.getResources().getDisplayMetrics()); 
        }
    }
    //add by huangfei for display abnormal when diplay size change in settings end
    //HCT:ouyang
    public ICameraContext getCameraContext(){
        return mIModeListener.getCameraContext();
    }
    //add by huangfei for shutter title change start
    public View inflate(int layoutId) {
        return mCameraAppUI.inflate(layoutId);
    }

    public ShutterButtonManager getShutterButtonManager(){
        return mCameraAppUI.getShutterButtonManager();
    }
    //add by huangfei for shutter title change end
	
    /* hct.wangsenhao, underwater camera @{ */
    private void isUnderWater(){
        Intent intent = getIntent();
        String action = intent.getAction();
        int flag = intent.getFlags();
        int uwFlag = Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK;
        boolean isUnderWater = intent.getBooleanExtra("isUnderWater", false);
        int underWaterEnable = Settings.Global.getInt(getContentResolver(), "under_water_value", 0);
        LogHelper.i(TAG, "underWaterEnable = " + underWaterEnable + " ;flag = " + flag
                + "; action = " + action + " ;isUnderWater = " + isUnderWater);
        if (underWaterEnable == 0 && MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA.equals(action)
                && isUnderWater && (flag & uwFlag) == uwFlag) {
            Settings.Global.putInt(getContentResolver(), "under_water_value", 1);
        }else{
            Settings.Global.putInt(getContentResolver(), "under_water_value", 0);
        }
    }
    /* }@ hct.wangsenhao */

    //*/ hct.huangfei, 20201024. add location.
    public PermissionManager getPermissionManager(){
       return mPermissionManager;
    }
    //*/
    /**
     * add this for GTS test,because GTS will open front
     * @param activity current activity
     * @return whether is front
     */
    private boolean isOpenFront(Activity activity) {
        Intent intent = activity.getIntent();
        boolean isOpenFront =
                intent.getBooleanExtra(EXTRA_USE_FRONT_CAMERA_FOR_ANDROID,false) ||
                        intent.getBooleanExtra(EXTRA_USE_FRONT_CAMERA_FOR_GOOGLE,false);
        return isOpenFront;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //determine that the return code is not equal to 0
        if (requestCode != RESULT_CANCELED && data != null ) {
            switch (requestCode) {
                case ImageParseActivity.STATE_REQUEST_CODE:   //get the data returned from the album (the return code of the album)
                    DataHolder.getInstance().setUri(data.getData());
                    DataHolder.getInstance().setFlag(0);
                    ImageParseActivity.startActivity(CameraActivity.this);
                    break;
            }
        }
    }

    //*/ hct.huangfei, 20201121. add bokeh mode.
    @Override
    public ICameraMode getCurrentCameraMode() {
        return mIModeListener.getCurrentICameraMode();
    }
    //*/
	
	    //*/ hct.huangfei, 20210317.add volume key function.
    @Override
    public void registerKeyLongEventListener(KeyLongEventListener listener) {
        mKeyLongEventListener = listener;
    }
    //*/
    //HCT liuyi add for volume key continuous increase start
    public boolean isZoomLongPress() {
        if (Config.isVolumeKeySupport(getActivity())) {
            String voluemValue = CameraUtil.getVolumeKeyValue(mDataStore, getActivity());
            if (CameraModeBase.VOLUME_KEY_ZOOM.equals(voluemValue)) {
                return true;
            }
        }
        return false;
    }
    //HCT liuyi add for volume key continuous increase end

    //add by huangfei for watermark start
    private WaterMarkUtil.WaterMarkStatusListener mWaterMarkStatusListener = new WaterMarkUtil.WaterMarkStatusListener() {
        @Override
        public void onWaterMarkCaptureEnd() {
            mMainHandler.sendEmptyMessage(MSG_WATER_MARK_THUMBNAIL_ENABLE);
        }
    };

    private ShutterButtonManager.WaterMarkCaptureListener mWaterMarkCaptureListener = new ShutterButtonManager.WaterMarkCaptureListener() {
        @Override
        public void onWaterMarkCaptureStart() {
            mMainHandler.sendEmptyMessage(MSG_WATER_MARK_THUMBNAIL_DISABLE);
        }
    };

    public void setListener(){
        WaterMarkUtil.setWaterMarkStatusListener(mWaterMarkStatusListener);
        WaterMarkUtil.inityuvWaterMark(this);
        mCameraAppUI.getShutterButtonManager().setWaterMarkCaptureListener(mWaterMarkCaptureListener);
    }

    public boolean geThumbnailClicked(){
        return mThumbnailClick;
    }

    public void setThumbnailClicked(boolean click){
        mThumbnailClick = click;
    }
    //add by huangfei for watermark end
}
