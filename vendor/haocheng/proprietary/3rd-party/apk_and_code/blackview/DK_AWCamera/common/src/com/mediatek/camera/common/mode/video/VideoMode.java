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
package com.mediatek.camera.common.mode.video;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.CamcorderProfile;
import android.media.MediaCodec;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.view.Surface;
import android.view.View;

import android.view.WindowManager;
import com.mediatek.camera.R;
import com.mediatek.camera.common.IAppUi;
import com.mediatek.camera.common.ICameraContext;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.CameraSysTrace;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil.Tag;
import com.mediatek.camera.common.device.CameraDeviceManagerFactory;
import com.mediatek.camera.common.device.CameraDeviceManagerFactory.CameraApi;
import com.mediatek.camera.common.mode.CameraApiHelper;
import com.mediatek.camera.common.mode.CameraModeBase;
import com.mediatek.camera.common.mode.DeviceUsage;
import com.mediatek.camera.common.mode.video.device.DeviceControllerFactory;
import com.mediatek.camera.common.mode.video.device.IDeviceController;
import com.mediatek.camera.common.mode.video.device.v2.LiveHDRConfig;
import com.mediatek.camera.common.mode.video.glrenderer.LiveHDRDrawer;
import com.mediatek.camera.common.mode.video.glrenderer.VideoTextureRenderCallback;
import com.mediatek.camera.common.mode.video.glrenderer.VideoTextureRenderInterface;
import com.mediatek.camera.common.mode.video.recorder.IRecorder;
import com.mediatek.camera.common.mode.video.recorder.NormalRecorder;
import com.mediatek.camera.common.mode.video.recorder.VideoBaseRecord;
import com.mediatek.camera.common.mode.video.recorder.VideoRecordFactory;
import com.mediatek.camera.common.mode.video.videoui.IVideoUI;
import com.mediatek.camera.common.mode.video.videoui.IVideoUI.VideoUIState;
import com.mediatek.camera.common.relation.DataStore;
import com.mediatek.camera.common.relation.Relation;
import com.mediatek.camera.common.relation.StatusMonitor;
import com.mediatek.camera.common.setting.ISettingManager;
import com.mediatek.camera.common.storage.IStorageService;
import com.mediatek.camera.common.storage.IStorageService.IStorageStateListener;
import com.mediatek.camera.common.storage.MediaSaver.MediaSaverListener;
import com.mediatek.camera.common.utils.BitmapCreator;
import com.mediatek.camera.common.utils.CameraUtil;
import com.mediatek.camera.common.utils.Size;
import com.aiworks.yuvUtil.YuvEncodeJni;
import com.mediatek.camera.portability.SystemProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import android.os.Process;

//*/ hct.huangfei, 20201030. add customize zoom.
import com.mediatek.camera.common.mode.photo.view.IZoomSliderUI;
//*/

//bv wuyonglin add for bug4496 20210402 start
import com.mediatek.camera.common.sound.ISoundPlayback;
//bv wuyonglin add for bug4496 20210402 end

/**
 * VideoMode is use for video record,user can extend it
 * to implement itself features.
 */
public class VideoMode extends CameraModeBase implements VideoTextureRenderInterface {

    private static final Tag TAG = new Tag(VideoMode.class.getSimpleName());
    private static final String SCENE_MODE_AUTO = "auto-scene-detection";
    private static final String VIDEO_STATUS_KEY = "key_video_status";
    private static final String KEY_STOP_RECORDING = "stop-recording";
    private static final String SCENE_MODE_FIREWORKS = "fireworks";
    private static final String SCENE_MODE_KEY = "key_scene_mode";
    private static final String KEY_SLOW_MOTION_QUALITY = "key_slow_motion_quality";
    private static final String KEY_FLASH = "key_flash";
    private static final String KEY_HDR10_PLUS = "key_hdr10";

    private static final String KEY_FLASH_OFF = "off";
    private static final String KEY_RECORDING = "recording";
    private static final String KEY_PREVIEW = "preview";

    private static final int HANDLER_STOP_RECORDING = 1;
    private static final int VIDEO_BAD_PERFORMANCE_AUTO_STOP = 1;
    private static final int VIDEO_RECORDING_NOT_AVAILABLE = 2;
    private static final int VIDEO_RECORDING_ERROR = 4;
    private static final int REACH_SIZE_LIMIT = 5;
    private static final String HDR10_ON = "on";
    private static final String FPS60_ON = "on";
    private static final String FPS60_KEY = "key_fps60";
    private static final String MTKCAM_RECORD_DELAY = "vendor.mtk.camera.app.record.delay";
    private static final int AVOID_ANR_TIME = 4800;
    private static final int MTKCAM_RECORD_SOUND_DELAY_DEFAULT = 3;
    private static final int MTKCAM_RECORDER_INFO_ERROR = 268436456;
    private static final int DELAY_TIME = SystemProperties.getInt
            (MTKCAM_RECORD_DELAY,MTKCAM_RECORD_SOUND_DELAY_DEFAULT);

    private int mSaveDataVersion = SystemProperties.getInt("ro.vendor.mtk_camera_app_data_save_version",
            1);

    private StatusMonitor.StatusResponder mVideoStatusResponder;
    private ConditionVariable mWaitStopRecording = new ConditionVariable(true);
    private Lock mResumePauseLock = new ReentrantLock();
    private Lock mLock = new ReentrantLock();
    private boolean mCanPauseResumeRecording = false;
    protected boolean mIsParameterExtraCanUse = false;
    private boolean mCanTakePicture = true;
    private int mOrientationHint = 0;
    private String HdrStatus = "off";
    protected ModeState mModeState = ModeState.STATE_PAUSED;
    protected Surface mSurface = null;

    protected VideoState mVideoState = VideoState.STATE_UNKNOWN;
    protected IDeviceController mCameraDevice;
    protected ISettingManager mSettingManager;
    protected IStorageService mStorageService;
    protected ICameraContext mCameraContext;
    protected VideoHelper mVideoHelper;
    protected Handler mVideoHandler;
    protected IRecorder mRecorder;
    protected IVideoUI mVideoUi;
    protected String mCameraId;
    protected IAppUi mAppUi;
    protected IApp mApp;
    /* hct.wangsenhao, for camera switch @{*/
    protected String mCameraIdTriple;
    /* }@ hct.wangsenhao*/
    
    //*/ hct.huangfei, 20201026. add storagepath.
    protected boolean isStorageStateEject = false;	
    //*/

    //*/ hct.huangfei, 20201030. add customize zoom.
    private IZoomSliderUI mIZoomSliderUI;
    //*/
    //add by liangchangwei for Video HDR
    private VideoBaseRecord mVideoRecordManager = null;
    private CameraCharacteristics mCameraCharacteristics;
    private Size mPreviewSize = null;

    @Override
    public void setVideoRecordCallback(VideoTextureRenderCallback callback) {
        mVideoHelper.getDrawer().setVideoRecordCallback(callback);
    }

    @Override
    public void queueEvent(Runnable runnable) {
        mVideoHelper.getsurfaceTextureRenderer().queueEvent(runnable);
    }
    /**
     * enum video state used to indicate the video mode state.
     */
    public enum VideoState {            //bv wuyonglin modify from protected to public for bug3677 20210204
        STATE_UNKNOWN,
        STATE_PREVIEW,
        STATE_PRE_RECORDING,
        STATE_RECORDING,
        STATE_PAUSED,
        STATE_PRE_SAVING,
        STATE_SAVING,
        STATE_REVIEW_UI
    }
    /**
     * enum mode state used to indicate the current mode state.
     */
    protected enum ModeState {
        STATE_RESUMED,
        STATE_PAUSED
    }

    @Override
    public void init(@Nonnull IApp app,
                     @Nonnull ICameraContext cameraContext, boolean isFromLaunch) {
        LogHelper.i(TAG, "[init]");
        super.init(app, cameraContext, isFromLaunch);
        mVideoHandler = new VideoHandler(Looper.myLooper());
        mCameraContext = cameraContext;
        mApp = app;
        initVideoVariables();
        doInitMode();

        //*/ hct.huangfei, 20201030. add customize zoom.
        if (CameraUtil.isZoomViewCustomizeSupport(app.getActivity())) {
            mIZoomSliderUI = app.getAppUi().getZoomSliderUI();
            mIZoomSliderUI.init();
        }
        //*/
    }

    @Override
    public void resume(@Nonnull DeviceUsage deviceUsage) {
        LogHelper.i(TAG, "[resume]");
        super.resume(deviceUsage);
        mCameraDevice.modePaused(false);
        updateModeState(ModeState.STATE_RESUMED);
        doInitDeviceManager();
        initVideoVariables();
        updateVideoState(VideoState.STATE_UNKNOWN);
        doResumeMode();
    }

    @Override
    public void pause(@Nullable DeviceUsage nextModeDeviceUsage) {
        LogHelper.i(TAG, "[pause]");
        super.pause(nextModeDeviceUsage);
        mCameraDevice.modePaused(true);
        updateModeState(ModeState.STATE_PAUSED);
        doPauseMode(mNeedCloseCameraIds);
        mVideoHelper.releaseSurfaceRenderer();
    }

    @Override
    public void unInit() {
        LogHelper.i(TAG, "[unInit]");
        super.unInit();

        //*/ hct.huangfei, 20201030. add customize zoom.
        if (CameraUtil.isZoomViewCustomizeSupport(mApp.getActivity())){
            mIZoomSliderUI.unInit();
        }

        if (mVideoRecordManager != null){
            mVideoRecordManager.release();
        }

        release();
    }

    @Override
    protected ISettingManager getSettingManager() {
        return mSettingManager;
    }

    @Override
    public boolean onCameraSelected(@Nonnull String newCameraId) {
        LogHelper.i(TAG, "[onCameraSelected] new id = " + newCameraId + " old id = " + mCameraId);
        super.onCameraSelected(newCameraId);
        if (canSelectCamera(newCameraId)) {

            //*/ hct.huangfei, 20201030. add customize zoom.
            if (CameraUtil.isZoomViewCustomizeSupport(mApp.getActivity())){
                mIZoomSliderUI.showCircleTextView();
            }
            //*/

            mIApp.getAppUi().onCameraSelected(newCameraId);
            mCameraDevice.setPreviewCallback(null, getPreviewStartCallback());
            doCameraSelect(newCameraId);
            return true;
        } else {
            mAppUi.applyAllUIEnabled(true);
            return false;
        }
    }

    @Override
    public boolean onUserInteraction() {
        switch (getVideoState()) {
            case STATE_PREVIEW:
            case STATE_UNKNOWN:
            case STATE_PRE_SAVING:
            case STATE_SAVING:
            case STATE_REVIEW_UI:
                super.onUserInteraction();
                return true;
            case STATE_PRE_RECORDING:
            case STATE_RECORDING:
            case STATE_PAUSED:
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean onShutterButtonClick() {
        LogHelper.i(TAG, "video onShutterButtonClick mVideoState = " + mVideoState);
        switch (getVideoState()) {
            case STATE_PREVIEW:
                if(getModeState() == ModeState.STATE_RESUMED) {
                    mAppUi.applyAllUIEnabled(false);
                    //bv wuyonglin add for bug4496 20210402 start
                    //mICameraContext.getSoundPlayback().play(R.raw.video_shutter,100);
                    //mICameraContext.getSoundPlayback().play(ISoundPlayback.START_VIDEO_RECORDING);
                    //bv wuyonglin add for bug4496 20210402 end
                    LogHelper.i(TAG, "onShutterButtonClick mDelayTime = " + DELAY_TIME*100);
                    //SystemClock.sleep(DELAY_TIME * 100);
                    startRecording();
                }
                    return true;
            case STATE_UNKNOWN:
            case STATE_PRE_RECORDING:
            case STATE_PRE_SAVING:
            case STATE_REVIEW_UI:
            case STATE_SAVING:
                return true;
            case STATE_RECORDING:
            case STATE_PAUSED:
                stopRecording();
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean onBackPressed() {
        LogHelper.d(TAG, "onBackPressed mVideoState = " + mVideoState);
        switch (getVideoState()) {
            case STATE_UNKNOWN:
            case STATE_SAVING:
            case STATE_PRE_SAVING:
            case STATE_PRE_RECORDING:
                return true;
            case STATE_PAUSED:
            case STATE_RECORDING:
                mVideoHandler.sendEmptyMessage(HANDLER_STOP_RECORDING);
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onOrientationChanged(int orientation) {
        if (mVideoUi != null) {
            mVideoUi.updateOrientation(orientation);
        }
        if(mVideoRecordManager != null){
            LogHelper.i(TAG,"mVideoRecordManager.setVideoSize ");
            mVideoRecordManager.setOrientation(orientation);
        }
    }

    protected CamcorderProfile getProfile() {
        return mCameraDevice.getCamcorderProfile();
    }

    protected boolean initRecorder(boolean isStartRecording) {
        LogHelper.d(TAG, "[initRecorder]");
        releaseRecorder();
        mRecorder = new NormalRecorder();
        try {
            CameraSysTrace.onEventSystrace("videoMode.mediaRecorderinit", true, true);
            IRecorder.RecorderSpec spec = configRecorderSpec(isStartRecording);
            mRecorder.init(spec);
            CameraSysTrace.onEventSystrace("videoMode.mediaRecorderinit", false, true);
            initForHal3(isStartRecording);
            if(mVideoRecordManager != null){
                LogHelper.i(TAG,"mVideoRecordManager.setVideoSize mVideoHelper.getVideoTempPath() ="+mVideoHelper.getVideoTempPath());
                mVideoRecordManager.setVideoSize(new android.util.Size(spec.profile.videoFrameWidth,spec.profile.videoFrameHeight));
                mVideoRecordManager.setAudioSource(spec.isRecordAudio);
                mVideoRecordManager.setVideoFilename(mVideoHelper.getVideoTempPath());  //bv wuyonglin add for bug5746 20210425
                //mVideoRecordManager.setHEVC(spec.isHEVC);
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
            releaseRecorder();
            return false;
        }

        return true;
    }

    protected void initVideoUi() {
        mVideoUi = mAppUi.getVideoUi();
    }

    protected void initCameraDevice(CameraApi api) {
        mCameraDevice = DeviceControllerFactory.createDeviceCtroller(
                mApp.getActivity(), api, mCameraContext);
        mCameraDevice.setHDRChangeListener(mHDRChangeListener);
    }

    protected void addFileToMediaStore() {
        if(mSaveDataVersion == 0){
            mCameraContext.getMediaSaver().addSaveRequest(
                    mVideoHelper.getVideoFilePath(mVideoHelper.getVideoFileUri()),
                    mVideoHelper.getVideoFileUri(),mFileSavedListener);
        }else {
        ContentValues contentValues
                = mVideoHelper.prepareContentValues(true, mOrientationHint, null);
        mCameraContext.getMediaSaver().addSaveRequest(modifyContentValues(contentValues),
                mVideoHelper.getVideoTempPath(), mFileSavedListener);
        }
    }

    protected IRecorder.RecorderSpec modifyRecorderSpec(
            IRecorder.RecorderSpec recorderSpec, boolean isRecording) {
        if(FPS60_ON.equals(mSettingManager.getSettingController().queryValue(FPS60_KEY))){
            recorderSpec.captureRate = getProfile().videoFrameRate;
        }
        if(isRecording && (mSaveDataVersion == 0)){
            recorderSpec.outFileDes = mVideoHelper.getVideoFD();
        }
        LogHelper.d(TAG, "[modifyRecorderSpec] captureRate = " + recorderSpec.captureRate);
        return recorderSpec;
    }

    protected IVideoUI.UISpec modifyUISpec(IVideoUI.UISpec spec) {
        return spec;
    }

    protected ContentValues modifyContentValues(ContentValues contentValues) {
        return contentValues;
    }

    protected void updateVideoState(VideoState state) {
        LogHelper.d(TAG, "[updateVideoState] new state = " + state + " old state =" + mVideoState);
        mLock.lock();
        try {
            //bv wuyonglin add for bug3677 20210204 start
            mAppUi.updateVideoState(state);
            //bv wuyonglin add for bug3677 20210204 end
            mVideoState = state;
        } finally {
            mLock.unlock();
        }
    }

    protected VideoState getVideoState() {
        mLock.lock();
        try {
            return mVideoState;
        } finally {
            mLock.unlock();
        }
    }

    private void updateModeState(ModeState state) {
        LogHelper.d(TAG, "[updateModeState] new state = " + state + " old state =" + mModeState);
        mResumePauseLock.lock();
        try {
            mModeState = state;
        } finally {
            mResumePauseLock.unlock();
        }
    }

    private ModeState getModeState() {
        mResumePauseLock.lock();
        try {
            return mModeState;
        } finally {
            mResumePauseLock.unlock();
        }
    }

    protected Relation getPreviewedRestriction() {
        Relation relation = VideoRestriction.getPreviewRelation().getRelation(KEY_PREVIEW, true);
        String sceneValue = mSettingManager.getSettingController().queryValue(SCENE_MODE_KEY);
        if (!SCENE_MODE_AUTO.equals(sceneValue) && !SCENE_MODE_FIREWORKS.equals(sceneValue)) {
            relation.addBody(
                    SCENE_MODE_KEY, sceneValue, VideoRestriction.getVideoSceneRestriction());
        }
        String smQualityValue =
                mSettingManager.getSettingController().queryValue(KEY_SLOW_MOTION_QUALITY);
        relation.addBody(KEY_SLOW_MOTION_QUALITY, smQualityValue, smQualityValue);
        if (CameraUtil.isCameraFacingFront(mIApp.getActivity(), Integer.parseInt(mCameraId))) {
            relation.addBody(KEY_FLASH, KEY_FLASH_OFF, KEY_FLASH_OFF);
        }
        return relation;
    }

    protected List<Relation> getRecordedRestriction(boolean isRecording) {
        List<Relation> relationList = new ArrayList<>();
        return relationList;
    }

    protected void release() {
        releaseRecorder();
        if (mVideoUi != null) {
            mVideoUi.unInitVideoUI();
            mVideoUi = null;
        }
        if(mVideoHandler != null) {
            mVideoHandler.removeCallbacksAndMessages(null);
        }
        if (mCameraDevice != null) {
            mCameraDevice.release();
            mCameraDevice = null;
        }
    }

    protected MediaSaverListener mFileSavedListener = new MediaSaverListener() {
        @Override
        public void onFileSaved(Uri uri) {
            LogHelper.i(TAG, "[onFileSaved] uri = " + uri);
            mApp.notifyNewMedia(uri, true);
            if(mSaveDataVersion == 0) {
                updateThumbnail(uri);
            }else {
            updateThumbnail();
            }
            if (VideoState.STATE_SAVING == getVideoState()) {
                updateVideoState(VideoState.STATE_PREVIEW);
            }
        }
    };

    protected MediaSaverListener mVssSavedListener = new MediaSaverListener() {
        @Override
        public void onFileSaved(Uri uri) {
            LogHelper.d(TAG, "[onFileSaved] mVssSavedListener uri = " + uri);
            mApp.notifyNewMedia(uri, true);
        }
    };

    protected IStorageStateListener mStorageStateListener = new IStorageStateListener() {
        @Override
        public void onStateChanged(int storageState, Intent intent) {
            if (Intent.ACTION_MEDIA_EJECT.equals(intent.getAction())) {
                LogHelper.i(TAG, "[onStateChanged] storage out service Intent.ACTION_MEDIA_EJECT");

                //*/ hct.huangfei, 20201026. add storagepath.
                isStorageStateEject = true;   
                //*/

                mVideoHandler.sendEmptyMessage(HANDLER_STOP_RECORDING);
            }
        }
    };


    protected IDeviceController.DeviceCallback getPreviewStartCallback() {
        return mPreviewStartCallback;
    }

    /**
     * use to handle some thing for video mode.
     */
    private class VideoHandler extends Handler {
        /**
         * the construction method.
         */
        VideoHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            LogHelper.d(TAG, "[handleMessage] msg = " + msg.what);
            switch (msg.what) {
                case HANDLER_STOP_RECORDING:
                    stopRecording();
                    break;
                default:
                    break;
            }
        }
    }

    private IDeviceController.SettingConfigCallback mSettingConfigCallback =
                    new IDeviceController.SettingConfigCallback() {
        @Override
        public void onConfig(Size previewSize) {
            //bv wuyonglin add for bug3677 20210204 start
            updateVideoState(VideoState.STATE_UNKNOWN);
            //bv wuyonglin add for bug3677 20210204 end
            onSettingConfigCallback(previewSize);
            mVideoHelper.releasePreviewFrameData();
            mVideoHelper.updatePreviewSize(previewSize);
            mVideoHelper.releaseSurfaceRenderer();
            mAppUi.setPreviewSize(previewSize.getWidth(),
                    previewSize.getHeight(), mVideoHelper.getSurfaceListener());
            mPreviewSize = previewSize;
            initRecorderForHal3();
        }
    };

    protected void onSettingConfigCallback(Size previewSize) {

    }

    private IDeviceController.RestrictionProvider mRestrictionProvider
            = new IDeviceController.RestrictionProvider() {
        @Override
        public Relation getRestriction() {
            return getPreviewedRestriction();
        }
    };
    private void updateThumbnail(Uri uri) {
        Bitmap bitmap = BitmapCreator.createBitmapFromVideo(
                mVideoHelper.getVideoFilePath(uri), mAppUi.getThumbnailViewWidth());
        if (bitmap != null) {
            mAppUi.updateThumbnail(bitmap);
        }
    }

    private void updateThumbnail() {
        Bitmap bitmap = BitmapCreator.createBitmapFromVideo(
                mVideoHelper.getVideoFilePath(), mAppUi.getThumbnailViewWidth());
        if (bitmap != null) {
            mAppUi.updateThumbnail(bitmap);
        }
    }

    private void deleteCurrentFile() {
        if (mVideoHelper != null && mVideoHelper.getVideoTempPath() != null) {
            mVideoHelper.deleteVideoFile(mVideoHelper.getVideoTempPath());
        }
    }

    private void startRecording() {
        LogHelper.d(TAG, "[startRecording] + ");
        CameraSysTrace.onEventSystrace("videoMode.startRecording", true, true);
        DataStore mDataStore = mCameraContext.getDataStore();
        HdrStatus = mDataStore.getValue("key_hdr_video", "off", mDataStore.getGlobalScope());
        //bv wuyonglin add for bug4063 20210226 start
        if (mApp.getAppUi().getCurrentMode() != null && mApp.getAppUi().getCurrentMode().equals("SlowMotion")) {
            HdrStatus = "off";
        }
        //bv wuyonglin add for bug4063 20210226 end

        if (getModeState() == ModeState.STATE_PAUSED) {
            LogHelper.e(TAG, "[startRecording] error mode state is paused");
            return;
        }
        if (mCameraContext.getStorageService().getRecordStorageSpace() <= 0) {
            LogHelper.e(TAG, "[startRecording] storage is full");
            mAppUi.applyAllUIEnabled(true);
            return;
        }
        if (!mCameraDevice.isReadyForCapture()) {
            LogHelper.i(TAG, "[startRecording] not ready for capture");
            mAppUi.applyAllUIEnabled(true);
            return;
        }
        mICameraContext.getSoundPlayback().play(ISoundPlayback.START_VIDEO_RECORDING);
        // add by liangchangwei for fixbug 4496
        SystemClock.sleep(DELAY_TIME * 100);
        updateVideoState(VideoState.STATE_PRE_RECORDING);
        mCameraDevice.postRecordingRestriction(getRecordedRestriction(true), false);
        if (!initRecorder(true)) {
            initRecorderFail();
            return;
        }
        prepareStartRecording();
        boolean startSuccess = true;
        try {
            CameraSysTrace.onEventSystrace("startRecording.start", true, true);
            if(CameraUtil.MTKCAM_AIWORKS_VIDEO_HDR_SUPPORT&&!CameraUtil.is_videoHdr_Force && HdrStatus.equals("on")){
                mVideoRecordManager.videoRecord();
            }else{
                mRecorder.start();
            }
            CameraSysTrace.onEventSystrace("startRecording.start", false, true);
            mWaitStopRecording.close();
        } catch (RuntimeException e) {
            startSuccess = false;
            startRecordingFail();
            e.printStackTrace();
        }
        if (!startSuccess) {
            mVideoStatusResponder.statusChanged(VIDEO_STATUS_KEY, KEY_PREVIEW);
            return;
        }
        mApp.enableKeepScreenOn(true);
        //updateVideoState(VideoState.STATE_RECORDING);
        if (!mIsParameterExtraCanUse || HdrStatus.equals("on")) {
            mVideoUi.updateUIState(VideoUIState.STATE_RECORDING);
        }
        CameraSysTrace.onEventSystrace("videoMode.startRecording", false, true);
        mCanPauseResumeRecording = true;
        CameraSysTrace.onEventSystrace("update 00:00", true, true);
        mVideoUi.updateUIState(VideoUIState.STATE_RECORDING);
        CameraSysTrace.onEventSystrace("update 00:00", false, true);
        mAppUi.setUIEnabled(mAppUi.SHUTTER_BUTTON, true);
        LogHelper.d(TAG, "[startRecording] - ");
    }

    private void stopRecording() {
        LogHelper.d(TAG, "[stopRecording]+ VideoState = " + mVideoState);
        CameraSysTrace.onEventSystrace("stopRecording", true, true);
        if (getVideoState() == VideoState.STATE_RECORDING
                || getVideoState() == VideoState.STATE_PAUSED) {
            prepareStopRecording();
            boolean isNeedSaveVideo = true;
            mApp.enableKeepScreenOn(true);
            mAppUi.applyAllUIVisibility(View.VISIBLE);
            try {
                LogHelper.i(TAG, "[stopRecording] media recorder stop + ");
                CameraSysTrace.onEventSystrace("stopRecording.stop", true, true);
                if(CameraUtil.MTKCAM_AIWORKS_VIDEO_HDR_SUPPORT&&!CameraUtil.is_videoHdr_Force && HdrStatus.equals("on")){
                    mVideoRecordManager.videoRecord();
                }else{
                    mRecorder.stop();
                }
                CameraSysTrace.onEventSystrace("stopRecording.stop", false, true);
                LogHelper.i(TAG, "[stopRecording] media recorder stop - ");
            } catch (RuntimeException e) {
                deleteCurrentFile();
                e.printStackTrace();
                isNeedSaveVideo = false;
            }
            doAfterRecorderStoped(isNeedSaveVideo);
            mApp.enableKeepScreenOn(false);
            if (getModeState() == ModeState.STATE_RESUMED) {
                mCameraDevice.postRecordingRestriction(getRecordedRestriction(false), true);
                updateModeDeviceState(MODE_DEVICE_STATE_PREVIEWING);
            }
        }
        mWaitStopRecording.open();
        mAppUi.applyAllUIEnabled(true);
        CameraSysTrace.onEventSystrace("stopRecording", false, true);
        LogHelper.d(TAG, "[stopRecording] -");
    }

    private void initRecorderFail() {
        mVideoUi.showInfo(VIDEO_RECORDING_ERROR);
        updateVideoState(VideoState.STATE_PREVIEW);
        mAppUi.applyAllUIEnabled(true);
        mCameraDevice.postRecordingRestriction(getRecordedRestriction(false), true);
    }

    private void prepareStartRecording() {
        mVideoHelper.pauseAudioPlayBack(mApp);
        updateModeDeviceState(MODE_DEVICE_STATE_RECORDING);
        mVideoStatusResponder.statusChanged(VIDEO_STATUS_KEY, KEY_RECORDING);
        mCanTakePicture = true;
        mCanPauseResumeRecording = false;
        mVideoUi.initVideoUI(configUISpec());
        mAppUi.applyAllUIVisibility(View.GONE);
        mAppUi.setUIVisibility(IAppUi.INDICATOR, View.VISIBLE);
        mAppUi.setUIVisibility(IAppUi.PREVIEW_FRAME, View.VISIBLE);
        mAppUi.setUIVisibility(IAppUi.SCREEN_HINT, View.VISIBLE);
        mAppUi.setUIEnabled(IAppUi.VIDEO_FLASH, true);
        mAppUi.setUIVisibility(IAppUi.VIDEO_FLASH, View.VISIBLE);
        mAppUi.getShutterButtonManager().showMoreModeText(false);
        mAppUi.setContentViewValue(true);
        updateVideoState(VideoState.STATE_RECORDING);
        mCameraDevice.startRecording();
        mVideoUi.updateUIState(VideoUIState.STATE_PRE_RECORDING);
        mVideoUi.updateOrientation(mApp.getGSensorOrientation());
    }

    private void startRecordingFail() {
        releaseRecorder();
        updateVideoState(VideoState.STATE_PREVIEW);
        mVideoUi.updateUIState(VideoUIState.STATE_PREVIEW);
        mAppUi.applyAllUIVisibility(View.VISIBLE);
        mAppUi.applyAllUIEnabled(true);
        mVideoUi.showInfo(VIDEO_RECORDING_ERROR);
    }

    private void prepareStopRecording() {
            updateVideoState(VideoState.STATE_PRE_SAVING);
            mAppUi.applyAllUIEnabled(false);
            mAppUi.getShutterButtonManager().showMoreModeText(true);
            mAppUi.setContentViewValue(false);
            mVideoHelper.releaseAudioFocus(mApp);
            mVideoUi.updateUIState(VideoUIState.STATE_PREVIEW);
            mVideoStatusResponder.statusChanged(VIDEO_STATUS_KEY, KEY_PREVIEW);
            mCameraDevice.stopRecording();
            mCanPauseResumeRecording = false;
    }

    private void doAfterRecorderStoped(boolean isNeedSaveVideo) {
        //*/ hct.huangfei, 20201026. add storagepath.
        //if (isNeedSaveVideo) {
        LogHelper.i(TAG, "[doAfterRecorderStoped] isStorageStateEject: " + isStorageStateEject);
        if (isNeedSaveVideo && !isStorageStateEject) {
        //*/ 
            updateVideoState(VideoState.STATE_SAVING);
            addFileToMediaStore();
        } else {
            mAppUi.applyAllUIVisibility(View.VISIBLE);
            mAppUi.applyAllUIEnabled(true);
            updateVideoState(VideoState.STATE_PREVIEW);
        }

        //*/ hct.huangfei, 20201026. add storagepath.
        isStorageStateEject = false;	
        //*/ 
    }

    private void releaseRecorder() {
        if (mRecorder != null) {
            mRecorder.reset();
            mRecorder.release();
            mRecorder = null;
        }
    }

    private void initRecorderForHal3() {
        initRecorder(false);
        if(CameraUtil.MTKCAM_AIWORKS_VIDEO_HDR_SUPPORT&&!CameraUtil.is_videoHdr_Force){
            initVideoRecorder();
            setCameraParameters();
        }
    }

    private void initForHal3(boolean isStartRecording) {
        CameraSysTrace.onEventSystrace("videoMode.mediaRecorderPrepare", true, true);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (mSurface == null) {
                mSurface = MediaCodec.createPersistentInputSurface();
            }
            mRecorder.getMediaRecorder().setInputSurface(mSurface);
        }
        try {
            mRecorder.prepare();
        } catch (RuntimeException e) {
            if (!isStartRecording) {
                mCameraDevice.configCamera(null, false);
            }
            throw new RuntimeException(e);
        }
        CameraSysTrace.onEventSystrace("videoMode.mediaRecorderPrepare", false, true);
        if (mSurface != null) {
            mCameraDevice.configCamera(mSurface, isStartRecording);
        } else {
            mCameraDevice.configCamera(mRecorder.getSurface(), isStartRecording);
        }
    }

    private void pauseRecording() {
        LogHelper.d(TAG, "[pauseRecording] +");
        try {
            mVideoUi.updateUIState(VideoUIState.STATE_PAUSE_RECORDING);
            if (CameraUtil.MTKCAM_AIWORKS_VIDEO_HDR_SUPPORT&&!CameraUtil.is_videoHdr_Force && HdrStatus.equals("on")) {
                mVideoRecordManager.getMediaRecorder().pause();
            } else {
                mRecorder.getMediaRecorder().pause();
            }
        } catch (IllegalStateException e) {
            mVideoUi.showInfo(VIDEO_RECORDING_NOT_AVAILABLE);
            e.printStackTrace();
        }
        LogHelper.d(TAG, "[pauseRecording] -");
    }

    private void resumeRecording() {
        LogHelper.d(TAG, "[resumeRecording] +");
        try {
            mVideoUi.updateUIState(VideoUIState.STATE_RESUME_RECORDING);
            if (CameraUtil.MTKCAM_AIWORKS_VIDEO_HDR_SUPPORT&&!CameraUtil.is_videoHdr_Force && HdrStatus.equals("on")) {
                mVideoRecordManager.getMediaRecorder().resume();
            } else {
                mRecorder.getMediaRecorder().resume();
			}
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
        LogHelper.d(TAG, "[resumeRecording] -");
    }

    private boolean isSupportPauseResume() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            return true;
        }
        return false;
    }

    private IRecorder.RecorderSpec configRecorderSpec(boolean isStartRecording) {
        IRecorder.RecorderSpec recorderSpec = mVideoHelper.configRecorderSpec(
                getProfile(), mCameraId, mCameraApi, mSettingManager);
        mOrientationHint = recorderSpec.orientationHint;
        recorderSpec.infoListener = mOnInfoListener;
        recorderSpec.errorListener = mOnErrorListener;
        recorderSpec.releaseListener = mOnInfoListener;
        recorderSpec = modifyRecorderSpec(recorderSpec, isStartRecording);
        return recorderSpec;
    }

    private IVideoUI.UISpec configUISpec() {
        IVideoUI.UISpec spec = new IVideoUI.UISpec();
        spec.isSupportedPause = isSupportPauseResume();
        spec.recordingTotalSize = 0;
        spec.stopListener = mStopRecordingListener;
        spec.isSupportedVss = isVssSupported();
        spec.vssListener = mVssListener;
        spec.pauseResumeListener = mPauseResumeListener;
        spec = modifyUISpec(spec);
        return spec;
    }

    private boolean isVssSupported() {
        if((mSettingManager.getSettingController().queryValue(KEY_HDR10_PLUS))!=null){
            if(mSettingManager.getSettingController().queryValue(KEY_HDR10_PLUS).equals("on")){
            	return false;
            }
        }
        return mCameraDevice.isVssSupported(Integer.parseInt(mCameraId));
    }

    private void initStatusMonitor() {
        mVideoStatusResponder = mCameraContext.getStatusMonitor(mCameraId)
                .getStatusResponder(VIDEO_STATUS_KEY);
    }

    private void initVideoVariables() {
        mAppUi = mApp.getAppUi();
        mCameraId = getCameraIdByFacing(mDataStore.getValue(
                KEY_CAMERA_SWITCHER, null, mDataStore.getGlobalScope()));
        /* hct.wangsenhao, for camera switch @{ */
        if(isSupportTriple && !FRONT_CAMERA_ID.equals(mCameraId)){
            mCameraIdTriple = getCameraIdByFacing(mDataStore.getValue(
                    KEY_TRIPLE_SWITCH, null, mDataStore.getGlobalScope()));
            mCameraId = mCameraIdTriple;
        }
        /* }@ hct.wangsenhao */
        mStorageService = mCameraContext.getStorageService();
        mSettingManager = mCameraContext.getSettingManagerFactory().getInstance(
                mCameraId, getModeKey(), ModeType.VIDEO, mCameraApi);
    }

    private void doInitMode() {
        CameraSysTrace.onEventSystrace("videoMode.doInitMode", true, true);
        initStatusMonitor();
        initCameraDevice(mCameraApi);
        mCameraDevice.setSettingConfigCallback(mSettingConfigCallback);
        mVideoHelper = new VideoHelper(mCameraContext, mApp, mCameraDevice,
                mVideoHandler);
        mCameraDevice.setPreviewCallback(
                mVideoHelper.getPreviewFrameCallback(), getPreviewStartCallback());
        initVideoUi();
        mCameraDevice.openCamera(
                mSettingManager, mCameraId, false, mRestrictionProvider);
        CameraSysTrace.onEventSystrace("videoMode.doInitMode", false, true);
    }

    private void doInitDeviceManager() {
        mCameraDevice.queryCameraDeviceManager();
    }

    private void doResumeMode() {
        initStatusMonitor();
        mCameraDevice.openCamera(
                mSettingManager, mCameraId, false, mRestrictionProvider);
        mCameraContext.getStorageService().registerStorageStateListener(mStorageStateListener);
    }

    private void pauseForRecorder() {
        if (getVideoState() == VideoState.STATE_RECORDING
                || getVideoState() == VideoState.STATE_PAUSED
                || getVideoState() == VideoState.STATE_PRE_RECORDING) {
            stopRecording();
        } else if (getVideoState() == VideoState.STATE_REVIEW_UI
                || getVideoState() == VideoState.STATE_PREVIEW) {
            updateVideoState(VideoState.STATE_UNKNOWN);
        }
    }

    private void pauseForDevice(ArrayList<String> needCloseCameraIdList) {
        mWaitStopRecording.block(AVOID_ANR_TIME);
        if (needCloseCameraIdList == null || needCloseCameraIdList.size() > 0) {
            mCameraDevice.preventChangeSettings();
            mCameraDevice.closeCamera(true);
            mCameraContext.getSettingManagerFactory().recycle(mCameraId);
        } else if (mNeedCloseSession) {
            mCameraDevice.closeSession();
        } else {
            mCameraDevice.stopPreview();
        }
    }

    private void doPauseMode(ArrayList<String> needCloseCameraIdList) {
        if (mCameraDevice != null) {
            mCameraContext.getStorageService().unRegisterStorageStateListener(
                    mStorageStateListener);
            pauseForRecorder();
            pauseForDevice(needCloseCameraIdList);
            mSurface = null;
        }
    }

    private View.OnClickListener mPauseResumeListener = new View.OnClickListener() {
        public void onClick(View view) {
            LogHelper.d(TAG, "[mPauseResumeListener] click video mVideoState = " + mVideoState
                    + " mCanPauseResumeRecording = " + mCanPauseResumeRecording);
            if (mCanPauseResumeRecording) {
                if (getVideoState() == VideoState.STATE_RECORDING) {
                    pauseRecording();
                    updateVideoState(VideoState.STATE_PAUSED);
                } else if (getVideoState() == VideoState.STATE_PAUSED) {
                    resumeRecording();
                    updateVideoState(VideoState.STATE_RECORDING);
                }
            }
        }
    };

    private View.OnClickListener mVssListener = new View.OnClickListener() {
        public void onClick(View view) {
            LogHelper.i(TAG, "[mVssListener] click video state = "
                                + mVideoState + "mCanTakePicture = " + mCanTakePicture);
            if ((getVideoState() == VideoState.STATE_PAUSED ||
                    getVideoState() == VideoState.STATE_RECORDING) && mCanTakePicture) {
                mAppUi.animationStart(IAppUi.AnimationType.TYPE_CAPTURE, null);
                mCameraDevice.updateGSensorOrientation(mApp.getGSensorOrientation());
                /*add by liangchangwei for VideoHdr begin */
                if(CameraUtil.MTKCAM_AIWORKS_VIDEO_HDR_SUPPORT&&!CameraUtil.is_videoHdr_Force && HdrStatus.equals("on")){
                    saveHDRVideoPicture();
                }else{
                    mCameraDevice.takePicture(mJpegCallback);
                }
                /*add by liangchangwei for VideoHdr end */
                mCanTakePicture = false;
            }
        }
    };

    /*add by liangchangwei for VideoHdr begin */
    private void saveHDRVideoPicture(){
        LogHelper.i(TAG,"saveHDRVideoPicture +");
        if(mVideoHelper.getDrawer() != null){
            mVideoHelper.getDrawer().setCaptureCallback(new LiveHDRDrawer.CaptureRenderCallback() {
                @Override
                public void onCaptureRender(byte[] source, float[] coeffs_data, byte[] result, int outWidth, int outHeight) {
                    mVideoHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            LogHelper.i(TAG,"onCaptureRender run +");
                            byte[] jpeg = YuvEncodeJni.getInstance().Rgb2Jpeg(result, outWidth, outHeight, 95, 1);
                            if(mCameraId.equalsIgnoreCase("1")){
                                jpeg = YuvEncodeJni.getInstance().RotateJpeg(jpeg, outWidth, outHeight,270);
                            }else{
                                jpeg = YuvEncodeJni.getInstance().RotateJpeg(jpeg, outWidth, outHeight,90);
                            }
                            int orientation = CameraUtil.getOrientationFromExif(jpeg);
                            Size size = CameraUtil.getSizeFromExif(jpeg);
                            mCameraContext.getMediaSaver().addSaveRequest(jpeg,
                                    mVideoHelper.prepareContentValues(false, orientation, size),
                                    null, mVssSavedListener);
                            LogHelper.i(TAG,"addSaveRequest run +");
                            mCanTakePicture = true;
                        }
                    });
                }
            });
        }
        LogHelper.i(TAG,"saveHDRVideoPicture -");
    }
    /*add by liangchangwei for VideoHdr end */

    private IDeviceController.JpegCallback mJpegCallback = new IDeviceController.JpegCallback() {
        @Override
        public void onDataReceived(byte[] jpegData) {
            LogHelper.d(TAG, "[onDataReceived] jpegData = " + jpegData);
            if (jpegData != null) {
                int orientation = CameraUtil.getOrientationFromSdkExif(jpegData);
                Size size = CameraUtil.getSizeFromSdkExif(jpegData);
                mCameraContext.getMediaSaver().addSaveRequest(jpegData,
                        mVideoHelper.prepareContentValues(false, orientation, size),
                        null, mVssSavedListener);
            }
            mCanTakePicture = true;
        }
    };

    private View.OnClickListener mStopRecordingListener = new View.OnClickListener() {
        public void onClick(View view) {
            LogHelper.i(TAG, "[mStopRecordingListener] click video state = " + mVideoState);
            mVideoHandler.sendEmptyMessage(HANDLER_STOP_RECORDING);
        }
    };

    private IDeviceController.DeviceCallback mPreviewStartCallback = new
            IDeviceController.DeviceCallback() {
                @Override
                public void onCameraOpened(String cameraId) {
                    updateModeDeviceState(MODE_DEVICE_STATE_OPENED);
                }

                @Override
                public void afterStopPreview() {
                    updateModeDeviceState(MODE_DEVICE_STATE_OPENED);
                }

                @Override
                public void beforeCloseCamera() {
                    updateModeDeviceState(MODE_DEVICE_STATE_CLOSED);
                }

                @Override
                public void onPreviewStart() {
                    if (getModeState() == ModeState.STATE_PAUSED) {
                        LogHelper.e(TAG, "[onPreviewStart] error mode state is paused");
                        return;
                    }
                    updateVideoState(VideoState.STATE_PREVIEW);
                    /*--add by bv liangchangwei for fix bug 3752--*/
                    CameraUtil.isVideo_opening = false;
                    /*--add by bv liangchangwei for fix bug 3752--*/
                    mAppUi.applyAllUIEnabled(true);
                    updateModeDeviceState(MODE_DEVICE_STATE_PREVIEWING);
                    LogHelper.d(TAG, "[onPreviewStart]");
                }

                @Override
                public void onError() {
                    if (getVideoState() == VideoState.STATE_PAUSED
                            || getVideoState() == VideoState.STATE_RECORDING) {
                        mVideoHandler.sendEmptyMessage(HANDLER_STOP_RECORDING);
                    }
                }
            };

    private MediaRecorder.OnErrorListener mOnErrorListener = new MediaRecorder.OnErrorListener() {
        @Override
        public void onError(MediaRecorder mr, int what, int extra) {
            LogHelper.e(TAG, "[onError] what = " + what + ". extra = " + extra);
            if (MediaRecorder.MEDIA_RECORDER_ERROR_UNKNOWN == what) {
                mVideoHandler.sendEmptyMessage(HANDLER_STOP_RECORDING);
            }
        }
    };

    private MediaRecorder.OnInfoListener mOnInfoListener = new MediaRecorder.OnInfoListener() {
        @Override
        public void onInfo(MediaRecorder mr, int what, int extra) {
            LogHelper.d(TAG, "MediaRecorder =" + mr + "what = " + what + " extra = " + extra);
            switch (what) {
                case MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED:
                    stopRecording();
                    break;
                case MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED:
                    stopRecording();
                    break;
                case MTKCAM_RECORDER_INFO_ERROR :
                    stopRecording();
                    break;
                default:
                    break;
            }
        }
    };

    private boolean canSelectCamera(@Nonnull String newCameraId) {
        if (newCameraId == null || mCameraId.equalsIgnoreCase(newCameraId)
                || getVideoState() != VideoState.STATE_PREVIEW) {
            return false;
        }
        /* -- add by bv liangchangwei for fixbug 3751 --*/
        if(CameraUtil.isVideo_HDR_changing){
            LogHelper.i(TAG,"canSelectCamera isVideo_HDR_changing = true");
            return false;
        }
        /* -- add by bv liangchangwei for fixbug 3751 --*/
        return true;
    }

    private void doCameraSelect(String newCameraId) {
        LogHelper.i(TAG, "[doCameraSelect] + mVideoState = " + mVideoState);
        if (getVideoState() == VideoState.STATE_PREVIEW
                && getModeState() == ModeState.STATE_RESUMED) {
            mCameraDevice.preventChangeSettings();
            updateVideoState(VideoState.STATE_UNKNOWN);
            /*--add by bv liangchangwei for fix bug 3752--*/
            CameraUtil.isVideo_opening = true;
            /*--add by bv liangchangwei for fix bug 3752--*/
            mCameraDevice.closeCamera(true);
            mCameraContext.getSettingManagerFactory().recycle(mCameraId);
            mCameraId = newCameraId;
            mSettingManager = mCameraContext.getSettingManagerFactory().getInstance(
                    mCameraId, getModeKey(), ModeType.VIDEO, mCameraApi);
            initStatusMonitor();
            mVideoHelper.releasePreviewFrameData();
            mVideoHelper.releaseSurfaceRenderer();
            mCameraDevice.setPreviewCallback(
                    mVideoHelper.getPreviewFrameCallback(), getPreviewStartCallback());
            mCameraDevice.openCamera(mSettingManager, mCameraId, false, mRestrictionProvider);
        }
    }

    public interface HDRChangeListener {
        /**
         * This method used for notify mode Bokeh level.
         * @param isOn  HDR is On
         */
        public void onHDRChanged(boolean isOn);

    }

    private HDRChangeListener mHDRChangeListener
            = new HDRChangeListener() {
        @Override
        public void onHDRChanged(boolean isOn) {
            LogHelper.d(TAG, "onHDRChanged isOn =" + isOn);
            if (mVideoHelper.getDrawer() != null){
                mVideoHelper.getDrawer().setDrawEffect(isOn);
            }
        }
    };

    private void initVideoRecorder(){
        mVideoRecordManager = VideoRecordFactory.create(mApp.getActivity(), this, false);
    }

    private void setCameraParameters() {

        LogHelper.i(TAG,"setCameraParameters mPreviewSize = " + mPreviewSize + " mOrientationHint = " + mOrientationHint + " mCameraId = " + mCameraId);
/*        int[] screenSize = CameraUtil.getScreenSize(mApp.getActivity());
        int displayWidth = screenSize[0];*/
        //listener.setPreviewSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        CameraManager camManager =
                (CameraManager) mApp.getActivity().getSystemService(Context.CAMERA_SERVICE);
        try {
            mCameraCharacteristics = camManager.getCameraCharacteristics(mCameraId);
        } catch (Exception e) {
            e.printStackTrace();
            LogHelper.e(TAG, "camera process killed due to getCameraCharacteristics() error");
            Process.killProcess(Process.myPid());
        }
        /*mCameraCapabilities = new Camera2Capabilities(mCameraCharacteristics);*/
        if (mVideoRecordManager != null) {
            int sensorOrientation = mCameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            mVideoRecordManager.setCameraId(Integer.valueOf(mCameraId), sensorOrientation);
        }
        if(mVideoRecordManager != null){
            WindowManager mgr = ((WindowManager) mApp.getActivity().getSystemService(Context.WINDOW_SERVICE));

            int lastOrientation = mApp.getGSensorOrientation(); //mgr.getDefaultDisplay().getRotation();
            //int orientation = mApp.getActivity().getRequestedOrientation();
            LogHelper.i(TAG,"lastOrientation = " + lastOrientation);
            //bv wuyonglin add for hdr open phone orientation not change record video file can not open 20210428 start
            if (lastOrientation == -1) {
                lastOrientation = 0;
            }
            //bv wuyonglin add for hdr open phone orientation not change record video file can not open 20210428 end
            mVideoRecordManager.setOrientation(lastOrientation);
            String filepath = mVideoHelper.getVideoTempPath();
            LogHelper.i(TAG,"mVideoRecordManager.setVideoFilename filepath = " + filepath);
            mVideoRecordManager.setVideoFilename(filepath);
        }
    }

}
