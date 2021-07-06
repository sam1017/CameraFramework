/*
 * Copyright Statement:
 *
 *   This software/firmware and related documentation ("MediaTek Software") are
 *   protected under relevant copyright laws. The information contained herein is
 *   confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 *   the prior written permission of MediaTek inc. and/or its licensors, any
 *   reproduction, modification, use or disclosure of MediaTek Software, and
 *   information contained herein, in whole or in part, shall be strictly
 *   prohibited.
 *
 *   MediaTek Inc. (C) 2016. All rights reserved.
 *
 *   BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 *   THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 *   RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 *   ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 *   WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 *   WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 *   NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 *   RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 *   INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 *   TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 *   RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 *   OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 *   SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 *   RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 *   STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 *   ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 *   RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 *   MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 *   CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 *   The following software/firmware and/or related documentation ("MediaTek
 *   Software") have been modified by MediaTek Inc. All revisions are subject to
 *   any receiver's applicable license agreements with MediaTek Inc.
 */

package com.mediatek.camera.feature.setting;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.View;
import android.widget.Toast;

import com.mediatek.camera.R;
import com.mediatek.camera.common.IAppUi;
import com.mediatek.camera.common.IAppUiListener;
import com.mediatek.camera.common.ICameraContext;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.memory.IMemoryManager;
import com.mediatek.camera.common.memory.MemoryManagerImpl;
import com.mediatek.camera.common.mode.ICameraMode;
import com.mediatek.camera.common.relation.DataStore;
import com.mediatek.camera.common.relation.Relation;
import com.mediatek.camera.common.setting.ISettingManager;
import com.mediatek.camera.common.setting.SettingBase;
import com.mediatek.camera.common.storage.IStorageService;
import com.mediatek.camera.common.storage.MediaSaver;
import com.mediatek.camera.common.utils.BitmapCreator;
import com.mediatek.camera.common.utils.CameraUtil;
import com.mediatek.camera.common.utils.Size;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.mediatek.camera.common.relation.StatusMonitor.*;

//*/ hct.huangfei, 20201027. continuous shot numbers.
import android.content.SharedPreferences;
import android.content.Context;
import com.mediatek.camera.common.relation.StatusMonitor;
import com.mediatek.camera.Config;
//*/

/**
 * control the continuous shot main flow.
 */

class ContinuousShotBase extends SettingBase implements
        IAppUiListener.OnShutterButtonListener, IMemoryManager.IMemoryListener {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(
            ContinuousShotBase.class.getSimpleName());
    private static final String KEY_ZSD = "key_zsd";
    protected static final String VALUE_ZSD_ON = "on";
    protected static final String VALUE_ZSD_OFF = "off";
    protected static String sZsdValue = VALUE_ZSD_ON;
    private static final String KEY_FOCUS = "key_focus_state";
    private static final String KEY_PICTURE_SIZE = "key_picture_size";
    private static final String KEY_SCENE_MODE = "key_scene_mode";
    private static final String VALUE_OF_AUTO_SCENE_DETECTION = "auto-scene-detection";
    protected static final String KEY_CSHOT = "key_continuous_shot";
    protected static final String KEY_CSHOT_START = "start";
    protected static final String KEY_CSHOT_STOP = "stop";
    protected static final String CONTINUOUSSHOT_ON = "on";
    protected static final String CONTINUOUSSHOT_OFF = "off";
    protected static final int SHUTTER_BUTTON_PRIORITY = 50;
    protected Byte JPEG_QUALITY_VALUE = 90;
    protected int MAX_CAPTURE_NUMBER = 100;

    //*/ hct.huangfei, 20201027. continuous shot numbers.
    private ContinuousShotSettingView mContinuousShotSettingView;
    public int maxNuM = 20;
    private List<String> mEntryValues = new ArrayList<>();
    private SharedPreferences mPreferences;
    //*/

    private final Object mCSlock = new Object();
    private volatile int mCurrentShotsNum;
    private volatile int mSavedNum;
    private long mShutterTime = 0;
    private long mFreeStorageForCapture;
    private String mFileDirectory;
    private CsNamingRule mCsNamingRule;
    private CaptureSound mCaptureSound;
    private MemoryManagerImpl mMemoryManager;
    protected ICameraContext mCameraContext;
    private ContinuousShotView mContinuousShotView;
    private ICameraMode.ModeType mModeType = ICameraMode.ModeType.PHOTO;
    private ArrayList<String> mSavingList = new ArrayList<>();
    private ArrayList<CshotBaseState> mStateList = new ArrayList();

    //*/ hct.huangfei, 20201106. continuousshot unsupprit tips of watermark.
    //bv wuyonglin add for after hdr open slide to pro mode then lock screen and unlock screen to camera should not support continuous shot 20200310 start
    private String mModeKey;
    //bv wuyonglin add for after hdr open slide to pro mode then lock screen and unlock screen to camera should not support continuous shot 20200310 end
    public IApp mApp;
    private IAppUi.HintInfo mContinuousHint;
    private static final int SHOW_INFO_LENGTH_LONG = 2000;
    public String unsupportTips;
    public String watermarkTips;
    //*/

    @Override
    public void init(IApp app, ICameraContext cameraContext,
                     ISettingManager.SettingController settingController) {
        super.init(app, cameraContext, settingController);
        LogHelper.d(TAG, "[init]");
        mCameraContext = cameraContext;
        mFileDirectory = mCameraContext.getStorageService().getFileDirectory();
        mCsNamingRule = new CsNamingRule();
        mCaptureSound = new CaptureSound(app.getActivity());
        mCaptureSound.load();
        mMemoryManager = new MemoryManagerImpl(app.getActivity());
        mContinuousShotView = new ContinuousShotView();
        mContinuousShotView.initIndicatorView(app);
        mStatusMonitor.registerValueChangedListener(KEY_FOCUS, mStatusListener);
        mStatusMonitor.registerValueChangedListener(KEY_ZSD, mStatusListener);
        DataStore dataStore = cameraContext.getDataStore();
        sZsdValue = dataStore.getValue(KEY_ZSD, VALUE_ZSD_ON, getStoreScope());

        //*/ hct.huangfei, 20201106. continuousshot unsupprit tips of watermark.
        mApp = app;
        unsupportTips = mApp.getActivity().getString(R.string.continuous_unsupport_mode);
        watermarkTips = mApp.getActivity().getString(R.string.continuous_unsupport_watermark);
        //*/
        //*/ hct.huangfei, 20201027. continuous shot numbers.
        mContinuousShotSettingView = new ContinuousShotSettingView(this);	//bv wuyonglin modify for hd shot 20201013 start
        mPreferences = mApp.getActivity().getSharedPreferences(CameraUtil.CAMERA_SHARED_PREFS_BACK, Context.MODE_PRIVATE);
        //*/
        //add by huangfei for watermark start
        watermarkTips = mApp.getActivity().getString(R.string.continuous_unsupport_watermark);
        //add by huangfei for watermark end

        //bv wuyonglin add for setting ui 20200923 start
        mAppUi.setRestoreSettingListener(mRestoreSettingListener);
        //bv wuyonglin add for setting ui 20200923 end
    }

    @Override
    public void unInit() {
        LogHelper.d(TAG, "[unInit]");
        //bv wuyonglin add for setting ui 20200923 start
        mAppUi.removeRestoreSettingListener(mRestoreSettingListener);
        //bv wuyonglin add for setting ui 20200923 end
        mAppUi.unregisterOnShutterButtonListener(this);
        mCaptureSound.release();
        dismissSavingProcess();
        mContinuousShotView.clearIndicatorAllMessage();
        mContinuousShotView.unInitIndicatorView();
        mStatusMonitor.unregisterValueChangedListener(KEY_FOCUS, mStatusListener);
        mStatusMonitor.unregisterValueChangedListener(KEY_ZSD, mStatusListener);
    }

    @Override
    public void postRestrictionAfterInitialized() {

    }

    //*/ hct.huangfei, 20201027. continuous shot numbers.
    @Override
    public void addViewEntry() {
        mContinuousShotSettingView.setOnValueChangeListener(mValueChangeListener);
        if(Config.isContinuousShotNumSupport(mApp.getActivity())){
            mAppUi.addSettingView(mContinuousShotSettingView);
        }
        LogHelper.d(TAG, "[addViewEntry] getValue() :" + getValue());
    }

    @Override
    public void removeViewEntry() {
        if(Config.isContinuousShotNumSupport(mApp.getActivity())){
            mAppUi.removeSettingView(mContinuousShotSettingView);
        }
        LogHelper.d(TAG, "[removeViewEntry]");
    }

    @Override
    public void refreshViewEntry() {
        mEntryValues.clear();
        mEntryValues.add(IContinuousShotViewListener.CONTINUOUS_SHOT_NUM10);
        mEntryValues.add(IContinuousShotViewListener.CONTINUOUS_SHOT_NUM20);
        int size = getEntryValues().size();
        if (mContinuousShotSettingView != null) {
            mContinuousShotSettingView.setEntryValues(mEntryValues);
            String value = mPreferences.getString(IContinuousShotViewListener.KEY_CONTINUOUS_SHOT_NUM,"20");
            maxNuM = Integer.parseInt(value);
            mContinuousShotSettingView.setValue(value);
            //bv wuyonglin add for hd shot 20201013 start
            String mHdrValue = mDataStore.getValue("key_hdr", "off", mDataStore.getGlobalScope());
            String mAisStatus = mDataStore.getValue("key_ais", "off", mDataStore.getCameraScope(getCameraId()));
            String mFlashStatus = mDataStore.getValue("key_flash", mApp.getActivity().getString(R.string.flash_default_value), "_preferences_0");
            if (!mHdrValue.equals("off") || (mFlashStatus.equals("off") && mAisStatus.equals("on"))) {
                mContinuousShotSettingView.setEnabled(false);
            } else {
                mContinuousShotSettingView.setEnabled(size > 1);
            }
            //bv wuyonglin add for hd shot 20201013 end
        }
    }

    private IContinuousShotViewListener.OnValueChangeListener mValueChangeListener
            = new IContinuousShotViewListener.OnValueChangeListener() {
        @Override
        public void onValueChanged(String value) {
            SharedPreferences.Editor editor = mPreferences.edit();
            editor.putString(IContinuousShotViewListener.KEY_CONTINUOUS_SHOT_NUM,value);
            editor.commit();
            maxNuM = Integer.parseInt(value);
        }
    };
    //*/

    @Override
    public SettingType getSettingType() {
        return SettingType.PHOTO;
    }

    @Override
    public String getKey() {
        return KEY_CSHOT;
    }

    @Override
    public ICaptureRequestConfigure getCaptureRequestConfigure() {
        return null;
    }

    @Override
    public void onModeOpened(String modeKey, ICameraMode.ModeType modeType) {
        super.onModeOpened(modeKey, modeType);
        LogHelper.d(TAG, "[onModeOpened] modeType = " + modeType);
        mModeType = modeType;
        //bv wuyonglin add for after hdr open slide to pro mode then lock screen and unlock screen to camera should not support continuous shot 20200310 start
        mModeKey = modeKey;
        //bv wuyonglin add for after hdr open slide to pro mode then lock screen and unlock screen to camera should not support continuous shot 20200310 end
        if (ICameraMode.ModeType.PHOTO == modeType) {
            mAppUi.registerOnShutterButtonListener(this, SHUTTER_BUTTON_PRIORITY);
        }
    }

    @Override
    public void onModeClosed(String modeKey) {
        super.onModeClosed(modeKey);
        LogHelper.d(TAG, "[onModeClosed]");
        //error handler? how about wait for jpeg
        synchronized (mCSlock) {
            mStateList.clear();
        }
        mContinuousShotView.clearIndicatorMessage(
                ContinuousShotView.SHOW_CONTINUOUS_SHOT_VIEW);
        mContinuousShotView.hideIndicatorView();
        mMemoryManager.removeListener(this);
        mCameraContext.getStorageService().unRegisterStorageStateListener(
                mStorageStateListener);
        dismissSavingProcess();
        mAppUi.unregisterOnShutterButtonListener(this);
    }

    @Override
    public void onMemoryStateChanged(IMemoryManager.MemoryAction state) {
        LogHelper.d(TAG, "[onMemoryStateChanged] memory state = " + state);
        switch (state) {
            case ADJUST_SPEED:
                slowDownContinuousShot();
                break;
            case STOP:
                stopContinuousShot();
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onShutterButtonFocus(boolean pressed) {
        //if release shutter button, stop continuous shot.
        LogHelper.d(TAG, "[onShutterButtonFocus], pressed = " + pressed);
        synchronized (mCSlock) {
            if (!pressed && ICameraMode.ModeType.PHOTO == mModeType && !mStateList.isEmpty()) {
                //all files captured, notify 3a cshot stopped
                mStateList.clear();
                stopContinuousShot();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onShutterButtonClick() {
        LogHelper.d(TAG, "[onShutterButtonClick]");
        return false;
    }

    @Override
    public boolean onShutterButtonLongPressed() {
        // start continuous shot

        //*/ hct.huangfei, 20201106. continuousshot unsupprit tips of watermark.
        //bv wuyonglin add for after flash open and at not support continuous shot mode long press shutter button flash is always bright and shutter button is unable click 2020313 start
        String hdrValue = mDataStore.getValue("key_hdr", "off", mDataStore.getGlobalScope());
            LogHelper.d(TAG, "[onShutterButtonLongPressed] getValue() ="+getValue()+" hdrValue ="+hdrValue);
        //bv wuyonglin add for hd shot 20201013 start
        String mAisStatus = mDataStore.getValue("key_ais", "off", mDataStore.getCameraScope(getCameraId()));
        String mFlashStatus = mDataStore.getValue("key_flash", mApp.getActivity().getString(R.string.flash_default_value), "_preferences_0");
	if ("off".equals(getValue()) || hdrValue.equals("on") || hdrValue.equals("auto") || (mFlashStatus.equals("off") && mAisStatus.equals("on"))) {
        //bv wuyonglin add for hd shot 20201013 end
            if(CameraUtil.isWaterMarkOn(mDataStore, mApp.getActivity())&&"Photo".equals(mAppUi.getCurrentMode())){
                continuousUnsupport(watermarkTips);
            }else{
                continuousUnsupport(unsupportTips);
            }
            return false;
        }

        if(getValue()==null){
            continuousUnsupport(unsupportTips);
            return false;
        }
        //*/

        if (ICameraMode.ModeType.PHOTO == mModeType && isReadyForCapture()) {
            LogHelper.d(TAG, "[onShutterButtonLongPressed]");
            //notify 3a cshot started
            CshotBaseState baseState = new CshotBaseState();
            synchronized (mCSlock) {
                mStateList.add(baseState);
            }
            mStatusResponder.statusChanged(KEY_CSHOT, KEY_CSHOT_START);
            return true;
        }
        return false;
    }

    //*/ hct.huangfei, 20201106. continuousshot unsupprit tips of watermark.
    public void continuousUnsupport(String tips){
        mContinuousHint = new IAppUi.HintInfo();
        int id = mApp.getActivity().getResources().getIdentifier("hint_text_background",
                "drawable", mApp.getActivity().getPackageName());
        mContinuousHint.mBackground = mApp.getActivity().getDrawable(id);
        mContinuousHint.mType = IAppUi.HintType.TYPE_AUTO_HIDE;
        mContinuousHint.mDelayTime = SHOW_INFO_LENGTH_LONG;
        mContinuousHint.mHintText = tips;
        mApp.getAppUi().showScreenHint(mContinuousHint);
    }
    //*/
    protected ICameraMode.ModeType getModeType() {
        return mModeType;
    }

    private StatusChangeListener mStatusListener = new StatusChangeListener() {
        @Override
        public void onStatusChanged(String key, String value) {
            LogHelper.d(TAG, "mStatusListener, key: " + key + ", value: " + value);
            //if focused and long pressed, do cshot
            if (KEY_ZSD.equalsIgnoreCase(key)) {
                sZsdValue = value;
                LogHelper.d(TAG, "zsd value is " + value);
            }
            synchronized (mCSlock) {
                if (mStateList.isEmpty()) {
                    if (KEY_FOCUS.equalsIgnoreCase(key)
                            && ("ACTIVE_FOCUSED".equalsIgnoreCase(value)
                            || "ACTIVE_UNFOCUSED".equalsIgnoreCase(value))) {
                        LogHelper.d(TAG, "Notify 3a stop focus, if can't start continuousShot.");
                        mStatusResponder.statusChanged(KEY_CSHOT, KEY_CSHOT_STOP);
                    }
                } else if (KEY_FOCUS.equalsIgnoreCase(key) && !mStateList.get(0).mIsCshotStopped
                        && ("ACTIVE_FOCUSED".equalsIgnoreCase(value)
                        || "ACTIVE_UNFOCUSED".equalsIgnoreCase(value))) {
                    startContinuousShot();
                }
            }
        }
    };

    protected boolean startContinuousShot() {
        // send start restriction
        return false;
    }

    protected boolean stopContinuousShot() {
        // send stop restriction
        return false;
    }

    protected void onContinuousShotStarted() {
        synchronized (mCSlock) {
            mCurrentShotsNum = 0;
            mSavedNum = 0;
            if (!mStateList.isEmpty()) {
                mStateList.get(0).mIsCshotStopped = false;
            }
        }
        mShutterTime = System.currentTimeMillis();
        mSavingList.add(KEY_CSHOT_START);
        disableAllUIExceptShutter();
        mContinuousShotView.setIndicatorViewOrientation(mApp.getGSensorOrientation());
        //mApp.disableGSensorOrientation();
        mFileDirectory = mCameraContext.getStorageService().getFileDirectory();
        mMemoryManager.addListener(this);
        mMemoryManager.initStateForCapture(mFreeStorageForCapture);
        mMemoryManager.initStartTime();
        mCameraContext.getStorageService().registerStorageStateListener(
                mStorageStateListener);
        postRestriction(CONTINUOUSSHOT_ON);
        //bv wuyonglin delete for continuous shot view show at same time with zoom view 20200604 start
       //mAppUi.applyZoomViewVisibilityImmediately(View.INVISIBLE);
        //bv wuyonglin delete for continuous shot view show at same time with zoom view 20200604 end
    }

    protected void onContinuousShotStopped() {
        //mApp.enableGSensorOrientation();
        mStatusResponder.statusChanged(KEY_CSHOT, KEY_CSHOT_STOP);
        synchronized (mCSlock) {
            mContinuousShotView.hideIndicatorView();
            if (!mStateList.isEmpty()) {
                mStateList.get(0).mIsCshotStopped = true;
            }
        }
        mMemoryManager.removeListener(this);
        mCameraContext.getStorageService().unRegisterStorageStateListener(
                mStorageStateListener);
        postRestriction(CONTINUOUSSHOT_OFF);
        //bv wuyonglin delete for continuous shot view show at same time with zoom view 20200604 start
       //mAppUi.applyZoomViewVisibilityImmediately(View.VISIBLE);
        //bv wuyonglin delete for continuous shot view show at same time with zoom view 20200604 end
    }

    protected void onContinuousShotDone(int captureNum) {
        LogHelper.i(TAG, "onContinuousShotDone(), captureNum = " + captureNum);
        mAppUi.applyAllUIEnabled(true);
        synchronized (mCSlock) {
            mContinuousShotView.clearIndicatorMessage(
                    ContinuousShotView.SHOW_CONTINUOUS_SHOT_VIEW);
            if (mSavedNum < captureNum) {
                showSavingProgress(true);
            } else {
                dismissSavingProcess();
            }
        }
    }

    protected void requestChangeOverrideValues() {

    }

    protected void slowDownContinuousShot() {
        stopContinuousShot();
        LogHelper.w(TAG, "Stop continousShot for avoid OOM!");
    }

    protected void playSound() {
        //modify huangfei for camerasound start
		//mCaptureSound.play();
        if (CameraUtil.isCameraSoundOn(mApp.getActivity()))
            mCaptureSound.play();
        //modify huangfei for camerasound end
    }

    protected void stopSound() {
        mCaptureSound.stop();
    }

    protected void disableIndicator() {
        LogHelper.d(TAG, "disableIndicator()");
        mContinuousShotView.disableIndicator();
    }

    protected void initializeValue(boolean isCshotSupported) {
        List<String> supportedList = new ArrayList<>();
        String defaultValue = CONTINUOUSSHOT_OFF;
        if (isCshotSupported) {
            defaultValue = CONTINUOUSSHOT_ON;
            //bv wuyonglin add for after hdr open slide to pro mode then back to photo mode close hdr enter to setting should show continuous shot setting view 20200314 start
            supportedList.add(CONTINUOUSSHOT_ON);
            supportedList.add(CONTINUOUSSHOT_OFF);
        } else {
            supportedList.add(CONTINUOUSSHOT_OFF);
            //bv wuyonglin add for after hdr open slide to pro mode then back to photo mode close hdr enter to setting should show continuous shot setting view 20200314 end
        }
        setSupportedPlatformValues(supportedList);
        setSupportedEntryValues(supportedList);
        setEntryValues(supportedList);
        mDataStore.getValue(getKey(), defaultValue, getStoreScope());
        setValue(defaultValue);
    }

    protected void postRestriction(String isOn) {
        Relation baseRalation = ContinuousShotRestriction.getRestriction().getRelation(isOn, true);
        /*if (CONTINUOUSSHOT_ON.equalsIgnoreCase(isOn)) {
            if (ContinuousShotEntry.mIsBurstMode) {
                baseRalation.addBody(KEY_ZSD, "off", "off, on");
            } else {
                baseRalation.addBody(KEY_ZSD, "on", "off, on");
            }
        }*/
        mSettingController.postRestriction(baseRalation);
        mSettingController.postRestriction(ContinuousShotRestriction.getFocusUiRestriction()
                .getRelation(isOn, true));
        mSettingController.postRestriction(ContinuousShotRestriction.getFocusSoundRestriction()
                .getRelation(isOn, true));
        if (CONTINUOUSSHOT_ON.equals(isOn)) {
            if (VALUE_OF_AUTO_SCENE_DETECTION
                    .equals(mSettingController.queryValue(KEY_SCENE_MODE))) {
                mSettingController.postRestriction(
                        ContinuousShotRestriction.getAsdRestriction()
                                .getRelation(CONTINUOUSSHOT_ON, true));
            }
        } else {
            mSettingController.postRestriction(
                    ContinuousShotRestriction.getAsdRestriction()
                    .getRelation(CONTINUOUSSHOT_OFF, true));
        }
        mSettingController.refreshViewEntry();
        requestChangeOverrideValues();
    }

    protected void saveJpeg(byte[] data,int format) {
        if (data == null) {
            stopContinuousShot();
            return;
        }

        //*/ hct.huangfei, 20201027. continuous shot numbers.
        if(mStateList.isEmpty()){
            dismissSavingProcess();
            LogHelper.d(TAG, "[saveJpeg] mStateList isEmpty= " + mStateList.isEmpty());
            return;
        }
        //*/
        synchronized (mCSlock) {
            mCurrentShotsNum++;
            LogHelper.i(TAG, "[saveJpeg] data = " + data + ", mCurrentShotsNum = "
                    + mCurrentShotsNum);
            if (!mStateList.isEmpty() && !mStateList.get(0).mIsCshotStopped

                    //*/ hct.huangfei, 20201027. continuous shot numbers.
                    //&& mCurrentShotsNum <= MAX_CAPTURE_NUMBER) {
                    && mCurrentShotsNum <= maxNuM) {
                    //*/

                updateThumbnail(data);
                playSound();
                mContinuousShotView.showIndicatorView(mCurrentShotsNum);
            }
        }
        //Save image.
        Size jpegSize = CameraUtil.getSizeFromSdkExif(data);
        ContentValues contentValues = mCsNamingRule.createContentValues(data,format, mFileDirectory,
                jpegSize.getWidth(), jpegSize.getHeight(), mShutterTime, mCurrentShotsNum);

        mCameraContext.getMediaSaver().addSaveRequest(data, contentValues, null,
                mMediaSaverListener);

        //*/ hct.huangfei, 20201027. continuous shot numbers.
        //if (mCurrentShotsNum == MAX_CAPTURE_NUMBER) {
        if (mCurrentShotsNum == maxNuM) {
        //*/

            stopContinuousShot();
            return;
        }
        //check DVM and system memory
        mMemoryManager.checkContinuousShotMemoryAction(data.length,
                mCameraContext.getMediaSaver().getBytesWaitingToSave());
    }

    protected int getSuitableSpeed() {
        return mMemoryManager.getSuitableSpeed();
    }


    MediaSaver.MediaSaverListener mMediaSaverListener = new MediaSaver.MediaSaverListener() {
        @Override
        public void onFileSaved(Uri uri) {
            synchronized (mCSlock) {
                mSavedNum++;
                LogHelper.d(TAG, "[onFileSaved] uri = " + uri + ", savedNum = " + mSavedNum);
                if (mSavedNum >= mCurrentShotsNum) {
                    dismissSavingProcess();
                }
            }
            mApp.notifyNewMedia(uri, true);
        }
    };

    private boolean isReadyForCapture() {
        if (mSavingList.contains(KEY_CSHOT_START)) {
            return false;
        }
        // check storage space.
        mFreeStorageForCapture = mCameraContext.getStorageService().getCaptureStorageSpace();
        if (mFreeStorageForCapture <= 0) {
            LogHelper.w(TAG, "[isReadyForCapture] there is not enough storage space!");
            return false;
        }
        //check picture size
        String pictureSize = mSettingController.queryValue(KEY_PICTURE_SIZE);
        if (pictureSize == null) {
            LogHelper.w(TAG, "[isReadyForCapture] there is no picture size,need check");
            return false;
        }
        return true;
    }

    private IStorageService.IStorageStateListener mStorageStateListener =
            new IStorageService.IStorageStateListener() {
                @Override
                public void onStateChanged(int storageState, Intent intent) {
                    if (Intent.ACTION_MEDIA_EJECT.equals(intent.getAction())) {
                        LogHelper.i(
                                TAG,
                                "[onStateChanged] storage out service Intent.ACTION_MEDIA_EJECT");
                        stopContinuousShot();
                    }
                }
            };

    private void updateThumbnail(byte[] bytes) {
        LogHelper.d(TAG, "updateThumbnail()");
        Bitmap bitmap = BitmapCreator.createBitmapFromJpeg(bytes, mApp.getAppUi()
                .getThumbnailViewWidth());
        mApp.getAppUi().updateThumbnail(bitmap);
    }

    private void showSavingProgress(boolean isShotDone) {
        synchronized (mSavingList) {
            if (mCurrentShotsNum == 0 || !mSavingList.contains(KEY_CSHOT_START)) {
                return;
            }
            String message = null;
            if (isShotDone) {
                //show the message such as:saving 99 ....
                message = String.format(Locale.ENGLISH, mActivity.getString(R.string
                        .continuous_saving_pictures), mCurrentShotsNum);
            }
            LogHelper.d(TAG, "[showSavingProgress],isShotDone = "
                    + isShotDone + ",msg = " + message);
            mAppUi.showSavingDialog(message, true);
            mAppUi.applyAllUIVisibility(View.INVISIBLE);
        }
    }

    private void dismissSavingProcess() {
        synchronized (mSavingList) {
            if (!mSavingList.contains(KEY_CSHOT_START)) {
                return;
            }
            mSavingList.clear();
            LogHelper.d(TAG, "[dismissSavingProcess]");
            mAppUi.hideSavingDialog();
            mAppUi.applyAllUIVisibility(View.VISIBLE);
        }
    }

    private void disableAllUIExceptShutter() {
        mAppUi.applyAllUIEnabled(false);
        mAppUi.setUIEnabled(IAppUi.SHUTTER_BUTTON, true);
        mAppUi.setUIEnabled(IAppUi.SHUTTER_TEXT, false);
    }

    /**
     * indicate whether the cshot flow stopped state.
     * if it is true, it means the cshot had been started and stopped.
     */
    private class CshotBaseState {
        public volatile boolean mIsCshotStopped = false;
    }

    //bv wuyonglin add for after hdr open slide to pro mode then lock screen and unlock screen to camera should not support continuous shot 20200310 start
    public String getCurrentModeKey() {
        return mModeKey;
    }
    //bv wuyonglin add for after hdr open slide to pro mode then lock screen and unlock screen to camera should not support continuous shot 20200310 end

    //bv wuyonglin add for setting ui 20200923 start
    private IAppUi.RestoreSettingListener mRestoreSettingListener = new IAppUi.RestoreSettingListener() {
        @Override
        public void restoreSettingtoValue() {
            SharedPreferences.Editor editor = mPreferences.edit();
            editor.putString(IContinuousShotViewListener.KEY_CONTINUOUS_SHOT_NUM, IContinuousShotViewListener.CONTINUOUS_SHOT_NUM20);
            editor.commit();
            maxNuM = Integer.parseInt(IContinuousShotViewListener.CONTINUOUS_SHOT_NUM20);
        }
    };
    //bv wuyonglin add for setting ui 20200923 end

    //bv wuyonglin add for hd shot 20201013 start
    /**
     * Get current camera id.
     * @return The current camera id.
     */
    protected int getCameraId() {
        int cameraId = Integer.parseInt(mSettingController.getCameraId());
        return cameraId;
    }

    public String getCurrentMode(){
        return mAppUi.getCurrentMode();
    }
    //bv wuyonglin add for hd shot 20201013 end
}
