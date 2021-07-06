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
 *     MediaTek Inc. (C) 2019. All rights reserved.
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

package com.mediatek.camera.feature.mode.aicombo.video.device;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCaptureSession.CaptureCallback;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.CamcorderProfile;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Range;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import com.mediatek.camera.common.ICameraContext;
import com.mediatek.camera.common.bgservice.BGServiceKeeper;
import com.mediatek.camera.common.bgservice.CaptureSurface;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.device.CameraDeviceManager;
import com.mediatek.camera.common.device.CameraDeviceManagerFactory.CameraApi;
import com.mediatek.camera.common.device.CameraOpenException;
//import com.mediatek.camera.common.device.v1.CameraProxy;
import com.mediatek.camera.common.device.v2.Camera2CaptureSessionProxy;
import com.mediatek.camera.common.device.v2.Camera2Proxy;
import com.mediatek.camera.common.device.v2.Camera2Proxy.StateCallback;
import com.mediatek.camera.common.mode.Device2Controller;
import com.mediatek.camera.common.relation.DataStore;
import com.mediatek.camera.common.relation.Relation;
import com.mediatek.camera.common.relation.StatusMonitor;
import com.mediatek.camera.common.relation.StatusMonitor.StatusChangeListener;
import com.mediatek.camera.common.setting.ISettingManager;
import com.mediatek.camera.common.sound.ISoundPlayback;
import com.mediatek.camera.common.utils.CameraUtil;
import com.mediatek.camera.common.utils.Size;
import com.mediatek.camera.portability.CamcorderProfileEx;
import com.mediatek.campostalgo.FeatureConfig;
import com.mediatek.campostalgo.FeatureParam;
import com.mediatek.campostalgo.FeaturePipeConfig;
import com.mediatek.campostalgo.FeatureResult;
import com.mediatek.campostalgo.ICamPostAlgoCallback;
import com.mediatek.campostalgo.StreamInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.Nonnull;
import static android.hardware.camera2.CameraMetadata.LENS_FACING_BACK;
import static android.hardware.camera2.CameraMetadata.LENS_FACING_FRONT;

/**
 * the implement for IAIComboVideoDeviceController for api2.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class AIComboVideoDevice2Controller extends Device2Controller
        implements IAIComboVideoDeviceController, CaptureSurface.ImageCallback,
        ISettingManager.SettingDevice2Requester {

    private static final LogUtil.Tag TAG = new LogUtil.Tag(AIComboVideoDevice2Controller.class
            .getSimpleName());
    private static final String KEY_MATRIX_DISPLAY = "key_matrix_display_show";
    private static final String KEY_SCENE_MODE = "key_scene_mode";
    private static final String KEY_PICTURE_SIZE = "key_picture_size";
    private static final double ASPECT_TOLERANCE = 0.001;
    private static final int CAPTURE_FORMAT = ImageFormat.JPEG;
    private static final int HANDLER_UPDATE_PREVIEW_SURFACE = 1;
    private static final int CAPTURE_MAX_NUMBER = 2;
    private static final int WAIT_TIME = 5;
    // add for eis and maybe other features which need know whether is recording or not
    private static final int STREAMING_FEATURE_STATE_PREVIEW = 0;
    private static final String AVAILABLE_RECORD_STATES
            = "com.mediatek.streamingfeature.availableRecordStates";
    private static final String RECORD_STATE_REQUEST
            = "com.mediatek.streamingfeature.recordState";
    private CaptureRequest.Key<int[]> mRecordStateKey;
    private CameraCharacteristics.Key<int[]> mAvailableRecordStates;

    //add for quick preview
    private static final String QUICK_PREVIEW_KEY = "com.mediatek.configure.setting.initrequest";
    private static final int[] QUICK_PREVIEW_KEY_VALUE = new int[]{1};
    private CaptureRequest.Key<int[]> mQuickPreviewKey = null;

    private boolean mIsRecorderSurfaceConfigured = false;
    private boolean mIsMatrixDisplayShow = false;
    private boolean mNeedRConfigSession = false;
    private boolean mFirstFrameArrived = false;
    private boolean mIsRecording = false;
    private int mJpegRotation;

    private StateCallback mDeviceCallback = new DeviceStateCallback();
    private CameraState mCameraState = CameraState.CAMERA_UNKNOWN;
    private Object mPreviewSurfaceSync = new Object();
    private Lock mDeviceLock = new ReentrantLock();
    private Lock mLockState = new ReentrantLock();
    CaptureRequest.Builder mBuilder = null;

    private StatusChangeListener mStatusChangeListener = new MyStatusChangeListener();
    private ISettingManager.SettingDevice2Configurator mSettingDevice2Configurator;
    private CameraCharacteristics mCameraCharacteristics;
    private DeviceCallback mModeDeviceCallback;
    private CameraDeviceManager mCameraDeviceManager;
    private PreviewSizeCallback mPreviewSizeCallback;
    private RestrictionProvider mRestrictionProvider;
    private SettingConfigCallback mSettingConfig;
    private Camera2CaptureSessionProxy mSession;
    private PreviewCallback mPreviewCallback;
    private final VideoDeviceHandler mVideoHandler;
    private ISettingManager mSettingManager;
    private CaptureSurface mCaptureSurface;
    private ICameraContext mICameraContext;
    private StatusMonitor mStatusMonitor;
    private CameraManager mCameraManager;
    private Camera2Proxy mCamera2Proxy;
    private JpegCallback mJpegCallback;
    private CamcorderProfile mProfile;
    private Surface mPreviewSurface;
    private Surface mRecordSurface;
    private Surface mPreviewPostAlgoSurface, mCapturePostAlgoSurface, mVideoPostAlgoSurface;
    private Activity mActivity;
    private String mCameraId;
    private static final int LEVEL_DEFAULT = 7;
    private int mCurrentLevel = LEVEL_DEFAULT;
    private ISettingManager.SettingController mSettingController;

    private static final String PREVIEW_SIZE_KEY =
            "com.mediatek.aicombofeature.aicomboFeaturePreviewSize";
    private static final int[] PREVIEW_SIZE_KEY_VALUE = new int[]{720, 1280};

    //add for BG service
    private CaptureRequest.Key<int[]> mBGServicePrereleaseKey = null;
    private CaptureRequest.Key<int[]> mBGServiceImagereaderIdKey = null;
    private static final int[] BGSERVICE_PRERELEASE_KEY_VALUE = new int[]{1};

    private CaptureRequest.Key<int[]> mPreviewSizeKey = null;

    private boolean mIsBGServiceEnabled = false;
    private BGServiceKeeper mBGServiceKeeper;

    private volatile int mPreviewWidth;
    private volatile int mPreviewHeight;
    private volatile int mCaptureWidth;
    private volatile int mCaptureHeight;
    private DataStore mDataStore;
    private FeatureParam mFeatureParam;
    private static final int AI_BEAUTY_MODE = 0;
    private static final int AI_BOKEH_MODE = 1;
    private static final int AI_COLOR_MODE = 2;
    private static final int AI_SLIMMING_MODE = 3;
    private static final int AI_LEGGY_MODE = 4;
    private static final int AI_COMBO_NO_EFFECT = 0;
    private static final int AI_COMBO_BOKEH = 1 << 0;
    private static final int AI_COMBO_COLOR = 1 << 1;
    private static final int AI_COMBO_SLIMMING = 1 << 2;
    private static final int AI_COMBO_LEGGY = 1 << 3;
    private int mAIMode = AI_BEAUTY_MODE;
    private int mAIComboType = AI_COMBO_NO_EFFECT;

    public static final String POSTALGO_PARAMS_JPEG_ORIENTATION_KEY
            = "postalgo.capture.jpegorientation";

    public static final String MTK_POSTALGO_AI_COMBO_TYPE = "postalgo.aicombo.type";
    public static final String MTK_POSTALGO_AI_COMBO_BIGEYE = "postalgo.aicombo.bigeye";
    public static final String MTK_POSTALGO_AI_COMBO_SMALLCHEEK = "postalgo.aicombo.smallCheek";
    public static final String MTK_POSTALGO_AI_COMBO_SMOOTH = "postalgo.aicombo.smooth";
    public static final String MTK_POSTALGO_AI_COMBO_WHITE = "postalgo.aicombo.white";

    /**
     * this enum is used for tag native camera open state.
     */
    private enum CameraState {
        CAMERA_UNKNOWN,
        CAMERA_OPENING,
        CAMERA_OPENED,
        CAMERA_CLOSING,
    }

    /**
     * AIComboVideoDevice2Controller may use activity to get display rotation.
     *
     * @param activity the camera activity.
     * @param context  the camera context.
     */
    public AIComboVideoDevice2Controller(@Nonnull Activity activity,
                                      @Nonnull ICameraContext context) {
        mActivity = activity;
        mICameraContext = context;
        mCaptureSurface = new CaptureSurface();
        mCaptureSurface.setCaptureCallback(this);
        mVideoHandler = new VideoDeviceHandler(Looper.myLooper());
        mCameraDeviceManager = mICameraContext.getDeviceManager(CameraApi.API2);
        mDataStore = mICameraContext.getDataStore();
    }

    @Override
    public void createAndChangeRepeatingRequest() {
        try {
            if (mSession != null) {
                synchronized (mSession) {
                    if (mSession != null) {
                        CaptureRequest.Builder builder = doCreateAndConfigRequest(mIsRecording);
                        setRepeatingRequest(builder);
                    }
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public CaptureRequest.Builder createAndConfigRequest(int templateType) {
        CaptureRequest.Builder builder = null;
        try {
            builder = doCreateAndConfigRequest(mIsRecording);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return builder;
    }

    @Override
    public CaptureSurface getModeSharedCaptureSurface()
            throws IllegalStateException {
        //not support now
        throw new IllegalStateException("get invalid capture surface!");
    }


    @Override
    public Surface getModeSharedPreviewSurface() throws IllegalStateException {
        //not support now
        throw new IllegalStateException("get invalid capture surface!");
    }

    @Override
    public Surface getModeSharedThumbnailSurface() throws IllegalStateException {
        //not support now
        throw new IllegalStateException("get invalid capture surface!");
    }

    @Override
    public Camera2CaptureSessionProxy getCurrentCaptureSession() {
        return mSession;
    }

    @Override
    public void requestRestartSession() {
        try {
            closeSession();
            updatePictureSize();
            mNeedRConfigSession = true;
            updatePreviewSize();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getRepeatingTemplateType() {
        return Camera2Proxy.TEMPLATE_RECORD;
    }

    @Override
    public void openCamera(@Nonnull ISettingManager settingManager, @Nonnull String cameraId,
                           boolean sync, RestrictionProvider relation) {
        //LogHelper.i(TAG, "[openCamera] + cameraId : " + cameraId + "sync = " + sync);
        mRestrictionProvider = relation;
        if (CameraState.CAMERA_UNKNOWN != getCameraState() ||
                (mCameraId != null && cameraId.equalsIgnoreCase(mCameraId))) {
            LogHelper.e(TAG, "[openCamera] mCameraState = " + mCameraState);
            return;
        }
        updateCameraState(CameraState.CAMERA_OPENING);
        mCameraId = cameraId;
        initSettingManager(settingManager);
        try {
            mDeviceLock.tryLock(WAIT_TIME, TimeUnit.SECONDS);
            initDeviceInfo();

            initSettings();
            doOpenCamera(sync);
        } catch (CameraOpenException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            mDeviceLock.unlock();
        }
    }

    @Override
    public void updatePreviewSurface(Object surfaceObject) {
        if (surfaceObject != null) {
            if (surfaceObject instanceof SurfaceHolder) {
                mPreviewSurface = surfaceObject == null ? null :
                        ((SurfaceHolder) surfaceObject).getSurface();
            } else if (surfaceObject instanceof SurfaceTexture) {
                mPreviewSurface = surfaceObject == null ? null :
                        new Surface((SurfaceTexture) surfaceObject);
            }
            //LogHelper.d(TAG, "[updatePreviewSurface] qq send HANDLER_UPDATE_PREVIEW_SURFACE",new Throwable());
            removeUpdatePreviewSurfaceMsg();
            mVideoHandler.sendEmptyMessage(HANDLER_UPDATE_PREVIEW_SURFACE);
        } else {
            mPreviewSurface = null;
        }
    }

    private void doUpdatePreviewSurface() {
        //LogHelper.d(TAG, "[doUpdatePreviewSurface] mPreviewSurface = " + mPreviewSurface
        //        + " state = " + mCameraState + " mNeedRConfigSession = " + mNeedRConfigSession
        //        + " mRecordSurface = " + mRecordSurface + "mIsRecorderSurfaceConfigured=" + mIsRecorderSurfaceConfigured);
        synchronized (mPreviewSurfaceSync) {
            if (CameraState.CAMERA_OPENED == getCameraState() && mPreviewSurface != null
                    && mNeedRConfigSession && mIsRecorderSurfaceConfigured) {
                configureSession(false);
                mNeedRConfigSession = false;
            }
        }
    }

    @Override
    public void stopPreview() {
        closeSession();
    }

    @Override
    public void startPreview() {
        configureSession(false);
    }

    @Override
    public void takePicture(@Nonnull JpegCallback callback) {
        mJpegCallback = callback;
        CaptureRequest.Builder builder;
        try {
            builder = mCamera2Proxy.createCaptureRequest(Camera2Proxy.TEMPLATE_VIDEO_SNAPSHOT);
            configureQuickPreview(builder);
            builder.addTarget(mPreviewPostAlgoSurface);
            builder.addTarget(mCapturePostAlgoSurface);
            setSpecialVendorTag(builder);

            // Config jpeg orientation for postAlgo
            FeatureParam params = new FeatureParam();
            int rotation = CameraUtil.getJpegRotationFromDeviceSpec(
                    Integer.parseInt(mCameraId), mJpegRotation, mActivity);
            params.appendInt(POSTALGO_PARAMS_JPEG_ORIENTATION_KEY, rotation);
            mICameraContext.getCamPostAlgo().configParams(FeaturePipeConfig.INDEX_CAPTURE,
                        params);

            mSettingDevice2Configurator.configCaptureRequest(builder);
            mSession.capture(builder.build(), mCaptureCallback, mModeHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateGSensorOrientation(int orientation) {
        mJpegRotation = orientation;
    }

    @Override
    public void closeCamera(boolean sync) {
        //LogHelper.i(TAG, "[closeCamera] + sync = " + sync + " current state : " + mCameraState);
        if (CameraState.CAMERA_UNKNOWN != mCameraState) {
            try {
                mDeviceLock.tryLock(WAIT_TIME, TimeUnit.SECONDS);
                super.doCameraClosed(mCamera2Proxy);
                updateCameraState(CameraState.CAMERA_CLOSING);
                closeSession();
                mModeDeviceCallback.beforeCloseCamera();
                doCloseCamera(sync);
                updateCameraState(CameraState.CAMERA_UNKNOWN);
                recycleVariables();
                mCaptureSurface.releaseCaptureSurface();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                super.doCameraClosed(mCamera2Proxy);
                mDeviceLock.unlock();
            }
            releaseVariables();
        }
        mCameraId = null;
    }


    @Override
    public void lockCamera() {

    }

    @Override
    public void unLockCamera() {

    }


    @Override
    public void startRecording() {
        mIsRecording = true;
        mICameraContext.getSoundPlayback().play(ISoundPlayback.START_VIDEO_RECORDING);
        configureSession(true);
    }

    @Override
    public void stopRecording() {
        mICameraContext.getSoundPlayback().play(ISoundPlayback.STOP_VIDEO_RECORDING);
        setStopRecordingToCamera();
        mIsRecording = false;
        configureSession(false);
    }

    @Override
    public void configCamera(Surface surface, boolean isNeedWaitConfigSession) {
        //LogHelper.i(TAG, "[configCamera] + ");
        if (surface != null && !surface.equals(mRecordSurface)) {
            mNeedRConfigSession = true;
        }
        mRecordSurface = surface;
        mIsRecorderSurfaceConfigured = true;
        //LogHelper.d(TAG, "[configCamera] qq send HANDLER_UPDATE_PREVIEW_SURFACE",new Throwable());
        removeUpdatePreviewSurfaceMsg();
        mVideoHandler.sendEmptyMessage(HANDLER_UPDATE_PREVIEW_SURFACE);
        if (isNeedWaitConfigSession && mNeedRConfigSession) {
            synchronized (mPreviewSurfaceSync) {
                try {
                    mPreviewSurfaceSync.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        //LogHelper.i(TAG, "[configCamera] - ");
    }

    @Override
    public void setPreviewCallback(PreviewCallback callback1, DeviceCallback callback2) {
        mPreviewCallback = callback1;
        mModeDeviceCallback = callback2;
    }

    @Override
    public void setSettingConfigCallback(SettingConfigCallback callback) {
        mSettingConfig = callback;
    }

    @Override
    public void queryCameraDeviceManager() {
        mCameraDeviceManager = mICameraContext.getDeviceManager(CameraApi.API2);
    }

    @Override
    public Camera.CameraInfo getCameraInfo(int cameraId) {
        return null;
    }

    @Override
    public boolean isVssSupported(int cameraId) {
        return true;
    }

    @Override
    public CamcorderProfile getCamcorderProfile() {
        if (mProfile == null) {
            initProfile();
        }
        return mProfile;
    }

    @Override
    public void release() {
        if (mStatusMonitor != null) {
            mStatusMonitor.unregisterValueChangedListener(KEY_SCENE_MODE, mStatusChangeListener);
            mStatusMonitor.unregisterValueChangedListener(
                    KEY_MATRIX_DISPLAY, mStatusChangeListener);
        }
        if (mCaptureSurface != null) {
            mCaptureSurface.release();
        }
        updateCameraState(CameraState.CAMERA_UNKNOWN);
    }

    @Override
    public void preventChangeSettings() {

    }

    @Override
    public boolean isReadyForCapture() {
        boolean canCapture = mSession != null
                && mCamera2Proxy != null
                && getCameraState() == CameraState.CAMERA_OPENED;
        //LogHelper.i(TAG, "[isReadyForCapture] canCapture = " + canCapture);
        return canCapture;
    }

    @Override
    public void onPictureCallback(byte[] data,
                                  int format, String formatTag, int width, int height) {
        //LogHelper.i(TAG, "[onPictureCallback]");
        if (mJpegCallback != null) {
            mJpegCallback.onDataReceived(data);
        }
    }

    @Override
    public void setPreviewSizeReadyCallback(PreviewSizeCallback callback) {
        mPreviewSizeCallback = callback;
    }

    /**
     * use to handle some thing for video device control.
     */
    private class VideoDeviceHandler extends Handler {
        /**
         * the construction method.
         */
        VideoDeviceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            //LogHelper.d(TAG, "[handleMessage] what = " + msg.what);
            switch (msg.what) {
                case HANDLER_UPDATE_PREVIEW_SURFACE:
                    removeUpdatePreviewSurfaceMsg();
                    doUpdatePreviewSurface();
                    break;
                default:
                    break;
            }
        }
    }

    private void initSettingManager(ISettingManager settingManager) {
        mSettingManager = settingManager;
        mSettingManager.updateModeDevice2Requester(this);
        mSettingDevice2Configurator = settingManager.getSettingDevice2Configurator();
        mStatusMonitor = mICameraContext.getStatusMonitor(mCameraId);
        mStatusMonitor.registerValueChangedListener(KEY_SCENE_MODE, mStatusChangeListener);
        mStatusMonitor.registerValueChangedListener(KEY_MATRIX_DISPLAY, mStatusChangeListener);
        mSettingController = settingManager.getSettingController();
    }

    private void initSettings() {
        mSettingManager.createAllSettings();
        mSettingDevice2Configurator.setCameraCharacteristics(mCameraCharacteristics);
        mSettingManager.getSettingController().postRestriction(
                mRestrictionProvider.getRestriction());
        mSettingController.addViewEntry();
        mSettingController.refreshViewEntry();
    }

    private void doOpenCamera(boolean sync) throws CameraOpenException {
        if (sync) {
            mCameraDeviceManager.openCameraSync(mCameraId, mDeviceCallback, null);
        } else {
            mCameraDeviceManager.openCamera(mCameraId, mDeviceCallback, null);
        }
    }

    private void doCloseCamera(boolean sync) {
        if (sync) {
            mCameraDeviceManager.closeSync(mCameraId);
        } else {
            mCameraDeviceManager.close(mCameraId);
        }
        mCamera2Proxy = null;
    }

    private void recycleVariables() {
        if (mStatusMonitor != null) {
            mStatusMonitor.unregisterValueChangedListener(KEY_SCENE_MODE, mStatusChangeListener);
            mStatusMonitor.unregisterValueChangedListener(
                    KEY_MATRIX_DISPLAY, mStatusChangeListener);
        }
        mIsMatrixDisplayShow = false;
    }

    private void releaseVariables() {
        removeUpdatePreviewSurfaceMsg();
        mIsRecorderSurfaceConfigured = false;
        mCameraId = null;
        mStatusMonitor = null;
        mRecordSurface = null;
        mPreviewSurface = null;
        mCamera2Proxy = null;
        mIsRecorderSurfaceConfigured = false;
    }

    private void configAeFpsRange(CaptureRequest.Builder requestBuilder,
                                  CamcorderProfile profile) {
        //LogHelper.d(TAG, "[configAeFpsRange] + ");
        try {
            Range<Integer>[] a = mCameraCharacteristics.get(
                    CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);
            int low = profile.videoFrameRate;
            for (int i = 0; i < a.length; i++) {
                if (a[i].contains(profile.videoFrameRate) && a[i].getLower() <= low) {
                    low = a[i].getLower();
                }
            }
            Range aeFps = new Range(low, profile.videoFrameRate);
            requestBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, aeFps);
            //LogHelper.i(TAG, "[configAeFpsRange] - " + aeFps.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateCameraState(CameraState state) {
        //LogHelper.d(TAG, "[updateCameraState] new state = " + state + " old =" + mCameraState);
        mLockState.lock();
        try {
            mCameraState = state;
        } finally {
            mLockState.unlock();
        }
    }

    private CameraState getCameraState() {
        mLockState.lock();
        try {
            return mCameraState;
        } finally {
            mLockState.unlock();
        }
    }

    @Override
    public void postRecordingRestriction(List<Relation> relations, boolean isNeedConfigRequest) {
        if (CameraState.CAMERA_OPENED != getCameraState() || mCamera2Proxy == null) {
            LogHelper.e(TAG, "[postRecordingRestriction] state is not right");
            return;
        }
        for (Relation relation : relations) {
            mSettingManager.getSettingController().postRestriction(relation);
        }
        if (isNeedConfigRequest) {
            repeatingPreview(true);
        }
    }


    @Override
    public void doCameraOpened(@Nonnull Camera2Proxy camera2proxy) {
        //LogHelper.i(TAG,
        //        "[onOpened] + camera2proxy = " + camera2proxy + "camera2Proxy id = " + camera2proxy.getId() + " mCameraId = " + mCameraId
        //         + ",mPreviewWidth=" + mPreviewWidth + ",mPreviewHeight=" + mPreviewHeight);
        try {
            if (CameraState.CAMERA_OPENING == getCameraState()
                    && camera2proxy != null && camera2proxy.getId().equals(mCameraId)) {
                mCamera2Proxy = camera2proxy;
                mModeDeviceCallback.onCameraOpened(mCameraId);
                updateCameraState(CameraState.CAMERA_OPENED);
                updatePictureSize();
                mNeedRConfigSession = true;
                updatePreviewSize();
            }
        } catch (CameraAccessException | RuntimeException e) {
            e.printStackTrace();
        }
        //LogHelper.d(TAG, "[onOpened] -");
    }

    @Override
    public void doCameraDisconnected(@Nonnull Camera2Proxy camera2proxy) {
        //LogHelper.i(TAG, "[onDisconnected] camera2proxy = " + camera2proxy);
        if (mCamera2Proxy != null && mCamera2Proxy == camera2proxy) {
            updateCameraState(CameraState.CAMERA_UNKNOWN);
            CameraUtil.showErrorInfoAndFinish(mActivity, CameraUtil.CAMERA_ERROR_SERVER_DIED);
        }
    }

    @Override
    public void doCameraError(@Nonnull Camera2Proxy camera2Proxy, int error) {
        LogHelper.i(TAG, "[onError] camera2proxy = " + camera2Proxy + " error = " + error);
        if ((mCamera2Proxy != null && mCamera2Proxy == camera2Proxy)
                || error == CameraUtil.CAMERA_OPEN_FAIL
                || error == CameraUtil.CAMERA_ERROR_EVICTED) {
            updateCameraState(CameraState.CAMERA_UNKNOWN);
            mModeDeviceCallback.onError();
            CameraUtil.showErrorInfoAndFinish(mActivity, error);
        }
    }

    public void closeSession() {
        //LogHelper.i(TAG, "[closeSession] + ");
        if (mSession != null) {
            synchronized (mSession) {
                if (mSession != null) {
                    try {
                        mSession.abortCaptures();
                        mSession.close();
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        stopPostAlgo();
        mSession = null;
        mBuilder = null;
        //LogHelper.d(TAG, "[closeSession] - ");
    }

    private void configureSession(boolean isRecording) {
        //LogHelper.i(TAG, "[configureSession] + ");
        mDeviceLock.lock();
        mFirstFrameArrived = false;
        try {
            if (mCamera2Proxy != null) {
                closeSession();
                List<Surface> surfaces = new LinkedList<>();
                startPostAlgo(isRecording);

                surfaces.add(mPreviewPostAlgoSurface);
                surfaces.add(mCapturePostAlgoSurface);
                mSettingDevice2Configurator.configSessionSurface(surfaces);
                CaptureRequest.Builder builder = doCreateAndConfigRequest(false);
                mCamera2Proxy.createCaptureSession(surfaces, mSessionCallback, mModeHandler, builder);
            }
        } catch (CameraAccessException e) {
            LogHelper.e(TAG, "[configureSession] error");
        } finally {
            mDeviceLock.unlock();
        }
        //LogHelper.d(TAG, "[configureSession] - ");
    }

    private void configureQuickPreview(CaptureRequest.Builder builder) {
        //LogHelper.d(TAG, "configureQuickPreview mQuickPreviewKey:" + mQuickPreviewKey);
        if (mQuickPreviewKey != null) {
            builder.set(mQuickPreviewKey, QUICK_PREVIEW_KEY_VALUE);
        }
    }

    private final Camera2CaptureSessionProxy.StateCallback mSessionCallback = new
            Camera2CaptureSessionProxy.StateCallback() {

                @Override
                public void onConfigured(@Nonnull Camera2CaptureSessionProxy session) {
                    //LogHelper.i(TAG, "[onConfigured] + session = " + session
                    //        + ", mCameraState = " + mCameraState);
                    mDeviceLock.lock();
                    try {
                        if (CameraState.CAMERA_OPENED == getCameraState()) {
                            mSession = session;
                            synchronized (mPreviewSurfaceSync) {
                                if (mPreviewSurface != null) {
                                    repeatingPreview(false);
                                }
                                mPreviewSurfaceSync.notify();
                            }
                            mModeDeviceCallback.onPreviewStart();
                        }
                    } finally {
                        mDeviceLock.unlock();
                    }
                    //LogHelper.d(TAG, "[onConfigured] -");
                }

                @Override
                public void onConfigureFailed(@Nonnull Camera2CaptureSessionProxy session) {
                    LogHelper.i(TAG, "[onConfigureFailed] session = " + session);
                    if (mSession == session) {
                        mSession = null;
                    }
                    synchronized (mPreviewSurfaceSync) {
                        mPreviewSurfaceSync.notify();
                    }
                }
            };

    private void initDeviceInfo() {
        try {
            mCameraManager = (CameraManager) mActivity.getSystemService(Context.CAMERA_SERVICE);
            mCameraCharacteristics = mCameraManager.getCameraCharacteristics(mCameraId);
            mQuickPreviewKey = CameraUtil.getAvailableSessionKeys(
                    mCameraCharacteristics, QUICK_PREVIEW_KEY);
            mPreviewSizeKey = CameraUtil.getAvailableSessionKeys(
                    mCameraCharacteristics, PREVIEW_SIZE_KEY);
            initRecordStateKey();
        } catch (RuntimeException e) {
            e.printStackTrace();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void setRepeatingRequest(CaptureRequest.Builder builder) {
        if (mSession != null) {
            synchronized (mSession) {
                if (mSession != null) {
                    try {
                        mSession.setRepeatingRequest(builder.build(),
                                mPreviewCapProgressCallback, null);
                    } catch (CameraAccessException e) {
                        LogHelper.e(TAG, "[setRepeatingBurst] fail");
                        e.printStackTrace();
                    }
                } else {
                    LogHelper.e(TAG, "[setRepeatingBurst] mSession is null");
                }
            }
        }
    }

    private void repeatingPreview(boolean needConfigBuiler) {
        //LogHelper.i(TAG, "[repeatingPreview] + with needConfigBuiler " + needConfigBuiler);
        try {
            mFirstFrameArrived = false;
            if (needConfigBuiler) {
                CaptureRequest.Builder builder = doCreateAndConfigRequest(false);
                setRepeatingRequest(builder);
            } else {
                mBuilder.addTarget(mPreviewPostAlgoSurface);
                setRepeatingRequest(mBuilder);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        //LogHelper.d(TAG, "[repeatingPreview] - ");
    }

    private CaptureCallback mPreviewCapProgressCallback = new CaptureCallback() {
        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request,
                                       TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            if (CameraState.CAMERA_OPENED == mCameraState
                    && mCamera2Proxy != null
                    && session.getDevice() == mCamera2Proxy.getCameraDevice()) {
                mSettingDevice2Configurator.getRepeatingCaptureCallback().onCaptureCompleted(
                        session, request, result);
                if (mPreviewCallback != null && !mFirstFrameArrived && mCameraId != null) {
                    mFirstFrameArrived = true;
                    mPreviewCallback.onPreviewCallback(null, 0, mCameraId);
                }
            }
            //notifyWarningKey(result);
        }
    };

    private void initProfile() {
        int cameraId = Integer.parseInt(mCameraId);
        //int quality = Integer.parseInt(
        //         mSettingManager.getSettingController().queryValue("key_video_quality"));
        int quality = 5;
        // force to QUALITY_720P = 5
        //LogHelper.i(TAG, "[initProfile] + cameraId = " + cameraId + " quality = " + quality);

        mProfile = CamcorderProfileEx.getProfile(cameraId, quality);
        reviseVideoCapability();
    }

    private void updatePreviewSize() throws CameraAccessException {
        Size size = getSupportedPreviewSizes(
                (double) mProfile.videoFrameWidth / mProfile.videoFrameHeight);
        mSettingConfig.onConfig(new Size(
                size.getWidth(), size.getHeight()));
                mCaptureWidth = mProfile.videoFrameWidth;
        mPreviewWidth = mProfile.videoFrameWidth;
        mPreviewHeight = mProfile.videoFrameHeight;
        //LogHelper.e(TAG, "[updatePreviewSize] mPreviewWidth: " + mProfile.videoFrameWidth
        //        + ", mPreviewHeight:" + mProfile.videoFrameHeight + ",width=" + size.getWidth() + ",height=" + size.getHeight());
    }

    private void updatePictureSize() {
        initProfile();
        mCaptureSurface.updatePictureInfo(mProfile.videoFrameWidth,
                mProfile.videoFrameHeight, CAPTURE_FORMAT, CAPTURE_MAX_NUMBER);
        mCaptureWidth = mProfile.videoFrameWidth;
        mCaptureHeight = mProfile.videoFrameHeight;
        //LogHelper.d(TAG, "[updatePictureSize] pictureSize: " + mProfile.videoFrameWidth
        //        + ", " + mProfile.videoFrameHeight);
    }

    // Returns the largest picture size which matches the given aspect ratio.
    private static Size getOptimalVideoSnapshotPictureSize(Size[] sizes, double targetRatio) {
        // Use a very small tolerance because we want an exact match.
        // final double ASPECT_TOLERANCE = 0.003;
        if (sizes == null) {
            return null;
        }
        Size optimalSize = null;
        // Try to find a size matches aspect ratio and has the largest width
        for (Size size : sizes) {
            double ratio = (double) size.getWidth() / size.getHeight();
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) {
                continue;
            }
            if (optimalSize == null || size.getWidth() > optimalSize.getHeight()) {
                optimalSize = size;
            }
        }
        return optimalSize;
    }

    private CaptureRequest.Builder doCreateAndConfigRequest(boolean isRecording)
            throws CameraAccessException {
        CaptureRequest.Builder builder = null;
        if (mCamera2Proxy != null) {
            builder = mCamera2Proxy.createCaptureRequest(Camera2Proxy.TEMPLATE_RECORD);
            if (builder == null) {
                LogHelper.d(TAG, "[doCreateAndConfigRequest] builder is null");
                return null;
            }
            // this function set fps range to [5,30],then in dark
            // environment will reduce fps to increase brightness;
            // but not phase out this function to use default value and
            // if happen above issue find tuning owner to fix it.
            // now don't delete code because maybe use this function for
            // other feature in future
            builder.addTarget(mPreviewPostAlgoSurface);
            setSpecialVendorTag(builder);
            mSettingDevice2Configurator.configCaptureRequest(builder);
        }
        mBuilder = builder;
        return builder;
    }

    private Size getSupportedPreviewSizes(double ratio) throws CameraAccessException {
        Size[] rawSizes = getSupportedSizeForClass(SurfaceHolder.class);
        List<Size> sizes = new ArrayList<Size>();
        for (Size sz : rawSizes) {
            if (sz.getWidth() <= PREVIEW_SIZE_KEY_VALUE[1] && sz.getHeight() <= PREVIEW_SIZE_KEY_VALUE[0]) {
                sizes.add(sz);
            }
        }
        Size values = CameraUtil.getOptimalPreviewSize(mActivity, sizes, ratio, true);
        //LogHelper.d(TAG, "[getSupportedPreviewSizes] values = " + values.toString());
        return values;
    }

    private Size[] getSupportedSizeForClass(Class klass) throws CameraAccessException {
        StreamConfigurationMap configMap =
                mCameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        android.util.Size[] availableSizes = configMap.getOutputSizes(klass);
        Size[] result = new Size[availableSizes.length];
        for (int i = 0; i < availableSizes.length; i++) {
            result[i] = new Size(availableSizes[i].getWidth(), availableSizes[i].getHeight());
        }
        return result;
    }

    private void reviseVideoCapability() {
        //LogHelper.d(TAG, "[reviseVideoCapability] + videoFrameRate = " + mProfile.videoFrameRate);
        String sceneMode = mSettingManager.getSettingController().queryValue(KEY_SCENE_MODE);
        if (Camera.Parameters.SCENE_MODE_NIGHT.equals(sceneMode)) {
            mProfile.videoFrameRate /= 2;
            mProfile.videoBitRate /= 2;
        }
        //LogHelper.d(TAG, "[reviseVideoCapability] - videoFrameRate = " + mProfile.videoFrameRate);
    }

    /**
     * Status change listener implement.
     */
    private class MyStatusChangeListener implements StatusChangeListener {
        /**
         * Callback when feature value is changed.
         *
         * @param key   The string used to indicate value changed feature.
         * @param value The changed value of feature.
         */
        @Override
        public void onStatusChanged(String key, String value) {
            //LogHelper.i(TAG, "[onStatusChanged] key = " + key
            //        + "value = " + value + " mCameraState = " + getCameraState());
            if (KEY_SCENE_MODE.equalsIgnoreCase(key)
                    && CameraState.CAMERA_OPENED == getCameraState()) {
                initProfile();
            } else if (KEY_MATRIX_DISPLAY.equals(key)) {
                mIsMatrixDisplayShow = "true".equals(value);
            }
        }
    }

    // notify camera will stop recording,some features need know it before real stop recording.
    private void setStopRecordingToCamera() {
        List<Integer> states = getSupportedRecordStates();
        if (mBuilder != null && isRecordStateSupported() && states.contains(STREAMING_FEATURE_STATE_PREVIEW)) {
            int[] state = {STREAMING_FEATURE_STATE_PREVIEW};
            mBuilder.set(mRecordStateKey, state);
            mBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_IDLE);
            mBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
                    CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_IDLE);
            setRepeatingRequest(mBuilder);
        }
    }

    private List<Integer> getSupportedRecordStates() {
        if (mCameraCharacteristics == null) {
            return Collections.emptyList();
        }
        List<CameraCharacteristics.Key<?>> keys = mCameraCharacteristics.getKeys();
        for (CameraCharacteristics.Key<?> key : keys) {
            if (AVAILABLE_RECORD_STATES.equals(key.getName())) {
                mAvailableRecordStates = (CameraCharacteristics.Key<int[]>) key;
            }
        }
        if (mAvailableRecordStates == null) {
            return Collections.emptyList();
        }
        int[] availableStates = getValueFromKey(mAvailableRecordStates);
        List<Integer> supportedValues = null;
        if (availableStates != null) {
            supportedValues = new ArrayList<Integer>();
            int length = availableStates.length;
            for (int i = 0; i < length; i++) {
                supportedValues.add(availableStates[i]);
                //LogHelper.d(TAG, "AVAILABLE_RECORD_STATES support value is " + availableStates[i]);
            }
        }
        return supportedValues;
    }

    private void initRecordStateKey() {
        List<CaptureRequest.Key<?>> keys = mCameraCharacteristics.getAvailableCaptureRequestKeys();
        for (CaptureRequest.Key<?> key : keys) {
            if (RECORD_STATE_REQUEST.equals(key.getName())) {
                mRecordStateKey = (CaptureRequest.Key<int[]>) key;
                break;
            }
        }
    }

    private boolean isRecordStateSupported() {
        return mRecordStateKey != null && mAvailableRecordStates != null &&
                getSupportedRecordStates().size() > 1;
    }

    private <T> T getValueFromKey(CameraCharacteristics.Key<T> key) {
        T value = null;
        try {
            value = mCameraCharacteristics.get(key);
            if (value == null) {
                LogHelper.e(TAG, key.getName() + "was null");
            }
        } catch (IllegalArgumentException e) {
            LogHelper.e(TAG, key.getName() + " was not supported by this device");
        }
        return value;
    }

    /**
     * Capture callback.
     */
    private final CaptureCallback mCaptureCallback = new CaptureCallback() {

        @Override
        public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request, long
                timestamp, long frameNumber) {
        }

        @Override
        public void onCaptureCompleted(@Nonnull CameraCaptureSession session,
                                       @Nonnull CaptureRequest request,
                                       @Nonnull TotalCaptureResult result) {
        }

        @Override
        public void onCaptureFailed(@Nonnull CameraCaptureSession session,
                                    @Nonnull CaptureRequest request,
                                    @Nonnull CaptureFailure failure) {
            LogHelper.i(TAG, "vss take picture fail:  mJpegCallback = " + mJpegCallback);
            if (mJpegCallback != null) {
                mJpegCallback.onDataReceived(null);
            }
        }
    };

    private void setSpecialVendorTag(CaptureRequest.Builder builder) {
        if (mPreviewSizeKey != null) {
            PREVIEW_SIZE_KEY_VALUE[0] = mProfile.videoFrameWidth;
            PREVIEW_SIZE_KEY_VALUE[1] = mProfile.videoFrameHeight;
            builder.set(mPreviewSizeKey, PREVIEW_SIZE_KEY_VALUE);
            //LogHelper.d(TAG, "[setSpecialVendorTag] set preview size " +
            //        "width " + mProfile.videoFrameWidth + ", height " + mProfile.videoFrameHeight);
        }
        configureBGService(builder);
    }

    private void configureBGService(CaptureRequest.Builder builder) {
        if (mIsBGServiceEnabled) {
            if (mBGServicePrereleaseKey != null) {
                builder.set(mBGServicePrereleaseKey, BGSERVICE_PRERELEASE_KEY_VALUE);
            }
            if (mBGServiceImagereaderIdKey != null) {
                int[] value = new int[1];
                value[0] = mCaptureSurface.getImageReaderId();
                builder.set(mBGServiceImagereaderIdKey, value);
            }
        }
    }

    private void startPostAlgo(boolean isRecording) {
        // feature param
        mFeatureParam = new FeatureParam();
        int facing = getCameraFacingById(mCameraId);
        mFeatureParam.appendInt("postalgo.lens.facing", facing);
        if (facing == LENS_FACING_FRONT) {
            mFeatureParam.appendInt("postalgo.sensor.orientation", 270);
        } else if (facing == LENS_FACING_BACK) {
            mFeatureParam.appendInt("postalgo.sensor.orientation", 90);
        }

        initMetaParams();

        mAIComboType = AI_COMBO_NO_EFFECT;
        if (mAIMode == AI_BOKEH_MODE) {
           mAIComboType |= AI_COMBO_BOKEH;
        } else if (mAIMode == AI_COLOR_MODE) {
           mAIComboType |= AI_COMBO_COLOR;
        } else if (mAIMode == AI_LEGGY_MODE) {
           mAIComboType |= AI_COMBO_LEGGY;
        } else if (mAIMode == AI_SLIMMING_MODE) {
           mAIComboType |= AI_COMBO_SLIMMING;
        }
        mFeatureParam.appendInt(MTK_POSTALGO_AI_COMBO_TYPE, mAIComboType);

        FeatureConfig[] featureConfigs = new FeatureConfig[2];

        // preview config
        FeatureConfig previewFeatureConfig = featureConfigs[0] = new FeatureConfig();

        ArrayList<Surface> previewSurfaceList = new ArrayList<>();
        if (mPreviewSurface != null) {
            previewSurfaceList.add(mPreviewSurface);
        }
        if (isRecording && mRecordSurface != null) {
            previewSurfaceList.add(mRecordSurface);
        }

        //LogHelper.i(TAG, "[startPostAlgo]  mPreviewSurface = " + mPreviewSurface + ",mRecordSurface=" + mRecordSurface);
        previewFeatureConfig.addSurface(previewSurfaceList);

        FeaturePipeConfig previewFeaturePipeConfig = new FeaturePipeConfig();

        previewFeaturePipeConfig.addFeaturePipeConfig(FeaturePipeConfig.INDEX_VIDEO,
                new int[]{FeaturePipeConfig.INDEX_AICOMBO_VIDEO});
        previewFeatureConfig.addFeaturePipeConfig(previewFeaturePipeConfig);

        StreamInfo previewStreamInfo = new StreamInfo();
        previewStreamInfo.addInfo(mPreviewWidth, mPreviewHeight, ImageFormat.YV12, 0);
        ArrayList<StreamInfo> previewStreamInfoList = new ArrayList<>();
        previewStreamInfoList.add(previewStreamInfo);
        previewFeatureConfig.addStreamInfo(previewStreamInfoList);
        previewFeatureConfig.addInterfaceParams(mFeatureParam);

        // capture config
        FeatureConfig captureFeatureConfig = featureConfigs[1] = new FeatureConfig();

        ArrayList<Surface> captureSurfaceList = new ArrayList<>();
        captureSurfaceList.add(mCaptureSurface.getSurface());
        //LogHelper.i(TAG, "[startPostAlgo] mCaptureSurface = " + mCaptureSurface.getSurface());
        captureFeatureConfig.addSurface(captureSurfaceList);

        FeaturePipeConfig captureFeaturePipeConfig = new FeaturePipeConfig();
        captureFeaturePipeConfig.addFeaturePipeConfig(FeaturePipeConfig.INDEX_CAPTURE,
                new int[]{FeaturePipeConfig.INDEX_AICOMBO_CAPTURE});
        
        captureFeatureConfig.addFeaturePipeConfig(captureFeaturePipeConfig);

        StreamInfo captureStreamInfo = new StreamInfo();
        captureStreamInfo.addInfo(mCaptureWidth, mCaptureHeight, ImageFormat.YV12, 0);
        ArrayList<StreamInfo> captureStreamInfoList = new ArrayList<>();
        captureStreamInfoList.add(captureStreamInfo);
        captureFeatureConfig.addStreamInfo(captureStreamInfoList);
        captureFeatureConfig.addInterfaceParams(mFeatureParam);

        FeatureResult result = mICameraContext.getCamPostAlgo().start(featureConfigs,
                new ICamPostAlgoCallback.Stub() {
                    @Override
                    public void processResult(FeatureParam featureParam) throws RemoteException {

                    }

                    @Override
                    public IBinder asBinder() {
                        return this;
                    }
                });

        if (result != null) {
            mPreviewPostAlgoSurface = result.getStreams().elementAt(0).getmSurface();
            mCapturePostAlgoSurface = result.getStreams().elementAt(1).getmSurface();
            //LogHelper.d(TAG, "[startPostAlgo] mPreviewPostAlgoSurface = " + mPreviewPostAlgoSurface);
            //LogHelper.d(TAG, "[startPostAlgo] mCapturePostAlgoSurface = " + mCapturePostAlgoSurface);
        }

    }

    private void initMetaParams() {
        String value = mDataStore.getValue(MTK_POSTALGO_AI_COMBO_BIGEYE,
                "0", mDataStore.getGlobalScope());
        int intValue = Integer.valueOf(value);
        mFeatureParam.appendInt(MTK_POSTALGO_AI_COMBO_BIGEYE, intValue);

        value = mDataStore.getValue(MTK_POSTALGO_AI_COMBO_SMALLCHEEK,
                "0", mDataStore.getGlobalScope());
        intValue = Integer.valueOf(value);
        mFeatureParam.appendInt(MTK_POSTALGO_AI_COMBO_SMALLCHEEK, intValue);

        value = mDataStore.getValue(MTK_POSTALGO_AI_COMBO_SMOOTH,
                "0", mDataStore.getGlobalScope());
        intValue = Integer.valueOf(value);
        mFeatureParam.appendInt(MTK_POSTALGO_AI_COMBO_SMOOTH, intValue);

        value = mDataStore.getValue(MTK_POSTALGO_AI_COMBO_WHITE,
                "0", mDataStore.getGlobalScope());
        intValue = Integer.valueOf(value);
        mFeatureParam.appendInt(MTK_POSTALGO_AI_COMBO_WHITE, intValue);
    }

    private void stopPostAlgo() {
        mICameraContext.getCamPostAlgo().stop();
    }

    private int getCameraFacingById(String deviceId) {
        CameraManager cameraManager = (CameraManager)
                mActivity.getSystemService(Context.CAMERA_SERVICE);
        try {
            CameraCharacteristics characteristics =
                    cameraManager.getCameraCharacteristics(deviceId);
            return characteristics.get(CameraCharacteristics.LENS_FACING);
        } catch (CameraAccessException e) {
            LogHelper.e(TAG, "[getCameraFacingById] CameraAccessException", e);
            return -1;
        } catch (IllegalArgumentException e) {
            LogHelper.e(TAG, "[getCameraFacingById] IllegalArgumentException", e);
            return -1;
        }
    }

    public void setAIMode(int mode) {
        mAIMode = mode;
    }


    private void removeUpdatePreviewSurfaceMsg() {
        if (mVideoHandler != null) {
            mVideoHandler.removeMessages(HANDLER_UPDATE_PREVIEW_SURFACE);
        }
    }

}
