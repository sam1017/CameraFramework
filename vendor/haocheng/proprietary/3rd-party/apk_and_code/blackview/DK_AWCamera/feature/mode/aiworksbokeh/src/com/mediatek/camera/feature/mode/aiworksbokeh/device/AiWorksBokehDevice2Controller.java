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

package com.mediatek.camera.feature.mode.aiworksbokeh.device;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCaptureSession.CaptureCallback;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureRequest.Builder;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.OutputConfiguration;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Build;
import android.os.Process;
import android.provider.MediaStore;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.net.Uri;

import com.mediatek.camera.common.ICameraContext;
import com.mediatek.camera.common.bgservice.BGServiceKeeper;
import com.mediatek.camera.common.bgservice.CaptureSurface;
import com.mediatek.camera.common.debug.CameraSysTrace;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil.Tag;
import com.mediatek.camera.common.device.CameraDeviceManager;
import com.mediatek.camera.common.device.CameraDeviceManagerFactory.CameraApi;
import com.mediatek.camera.common.device.CameraOpenException;
import com.mediatek.camera.common.device.v2.Camera2CaptureSessionProxy;
import com.mediatek.camera.common.device.v2.Camera2Proxy;
import com.mediatek.camera.common.device.v2.Camera2Proxy.StateCallback;
import com.mediatek.camera.common.loader.DeviceDescription;
import com.mediatek.camera.common.mode.CameraApiHelper;
import com.mediatek.camera.common.mode.Device2Controller;
import com.mediatek.camera.feature.mode.aiworksbokeh.AiWorksBokehDeviceInfo;
import com.mediatek.camera.common.mode.photo.heif.HeifCaptureSurface;
import com.mediatek.camera.common.mode.photo.heif.ICompeletedCallback;
import com.mediatek.camera.common.mode.photo.heif.IDeviceListener;
import com.mediatek.camera.common.mode.photo.HeifHelper;
import com.mediatek.camera.common.mode.photo.P2DoneInfo;
import com.mediatek.camera.common.mode.photo.ThumbnailHelper;

import com.mediatek.camera.common.setting.ISettingManager;
import com.mediatek.camera.common.setting.ISettingManager.SettingController;
import com.mediatek.camera.common.setting.ISettingManager.SettingDevice2Configurator;
import com.mediatek.camera.common.sound.ISoundPlayback;
import com.mediatek.camera.common.utils.CameraUtil;
import com.mediatek.camera.common.utils.Size;

import com.mediatek.camera.common.relation.DataStore;
import android.hardware.camera2.CameraMetadata;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import com.aiworks.yuvUtil.YuvEncodeJni;
import javax.annotation.Nonnull;

import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import com.mediatek.camera.ui.CHSeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import com.mediatek.camera.R;
import com.mediatek.camera.common.relation.Relation;
import com.mediatek.camera.feature.mode.aiworksbokeh.AiWorksBokehRestriction;

import com.mediatek.camera.common.IAppUiListener;
import com.mediatek.camera.common.IAppUi;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Environment;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.io.IOException;
import java.io.File;
import com.mediatek.camera.R;
import android.os.HandlerThread;
import com.mediatek.camera.BvUtils;
import com.mediatek.camera.BvUtilsCamera4;

//add by huangfeifor front bokeh start
import com.mediatek.camera.Config;
import com.mediatek.camera.feature.setting.facedetection.Face;
import com.mediatek.camera.common.utils.CoordinatesTransform;
import android.graphics.Rect;
//add by huangfeifor front bokeh end
//bv wuyonglin add for pro mode adpte A80Pro pibokeh 20200615 start
import android.os.SystemProperties;
//bv wuyonglin add for pro mode adpte A80Pro pibokeh 20200615 end
import android.media.Image;
import android.media.ImageReader;
import android.graphics.ImageFormat;
import com.aiworks.android.utils.ImageFormatUtil;
//bv wuyonglin add for open mfb 20201114 start
import com.aiworks.android.camera.VendorTagRequest;
//bv wuyonglin add for open mfb 20201114 end

/**
 * An implementation of {@link IDeviceController} with Camera2Proxy.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class AiWorksBokehDevice2Controller extends Device2Controller implements
        IAiWorksBokehDeviceController, ICompeletedCallback,
        CaptureSurface.ImageCallback,
        ISettingManager.SettingDevice2Requester {
    private static final Tag TAG = new Tag(AiWorksBokehDevice2Controller.class.getSimpleName());
    private static final String KEY_PICTURE_SIZE = "key_picture_size";
    private static final int CAPTURE_MAX_NUMBER = 5;
    private static final int WAIT_TIME = 5;
    //add for quick preview
    private static final String QUICK_PREVIEW_KEY = "com.mediatek.configure.setting.initrequest";
    private static final int[] QUICK_PREVIEW_KEY_VALUE = new int[]{1};
    private CaptureRequest.Key<int[]> mQuickPreviewKey = null;
    //add for BG service
    private CaptureRequest.Key<int[]> mBGServicePrereleaseKey = null;
    private CaptureRequest.Key<int[]> mBGServiceImagereaderIdKey = null;
    private static final int[] BGSERVICE_PRERELEASE_KEY_VALUE = new int[]{1};

    private final Activity mActivity;
    private final CameraManager mCameraManager;
    private final CaptureSurface mCaptureSurface;
    private final CaptureSurface mThumbnailSurface;
    private final ICameraContext mICameraContext;
    private final Object mSurfaceHolderSync = new Object();
    private final StateCallback mDeviceCallback = new DeviceStateCallback();

    private int mJpegRotation;
    private volatile int mPreviewWidth;
    private volatile int mPreviewHeight;
    private volatile Camera2Proxy mCamera2Proxy;
    private volatile Camera2CaptureSessionProxy mSession;

    private boolean mFirstFrameArrived = false;
    private boolean mIsPictureSizeChanged = false;
    private boolean mNeedSubSectionInitSetting = false;
    private volatile boolean mNeedFinalizeOutput = false;

    private Lock mLockState = new ReentrantLock();
    private Lock mDeviceLock = new ReentrantLock();
    private CameraState mCameraState = CameraState.CAMERA_UNKNOWN;

    private String mCurrentCameraId;
    private Surface mPreviewSurface;
    private CaptureDataCallback mCaptureDataCallback;
    private Object mSurfaceObject;
    private ISettingManager mSettingManager;
    private DeviceCallback mModeDeviceCallback;
    private SettingController mSettingController;
    private PreviewSizeCallback mPreviewSizeCallback;
    private CameraDeviceManager mCameraDeviceManager;
    private SettingDevice2Configurator mSettingDevice2Configurator;
    private CaptureRequest.Builder mBuilder = null;
    private CaptureRequest.Builder mDefaultBuilder = null;
    private String mZsdStatus = "on";
    private List<OutputConfiguration> mOutputConfigs;
    private CameraCharacteristics mCameraCharacteristics;
    private boolean mIsBGServiceEnabled = false;
    private BGServiceKeeper mBGServiceKeeper;
    private ConcurrentHashMap mCaptureFrameMap = new ConcurrentHashMap<String, Boolean>();
    private IDeviceListener mIDeviceListener;
    private CaptureImageSavedCallback mCaptureImageSavedCallback;
    private CHSeekBar mBeautySeekbarLayout;
    private  int mbokehLevel = -1;
    private int seekbarmax = 8;
    private DataStore mDataStore;
    private ViewGroup mRootViewGroup;
    private static final int[] BOKEH_PARAMETER_VALUE = new int[]{0,0,-1,-1,-1,-1} ;
    private static final int[] BOKEH_MODE_VALUE = new int[]{0,0,0} ;
    private static final String MTK_CONTROL_CAPTURE_BRIGHT_VALUE = "com.mediatek.control.capture.brightvalue";
    private CaptureResult.Key<int[]> mBrightValueKey = null;;
    private int brightvalue;
    private int mPictureWidth = 0;
    private int mPictureHeight = 0;
    private int mRadius = 0;
    private int bokeh_x = -1;
    private int bokeh_y = -1;
    private int mDefaultBokehLevel = -1;
    private static final int HCT_READ_BRIGHT_CAMERA = 101;
    private static final String yuvcamera_state_file = "/sys/bus/platform/drivers/image_sensor/yuvcamera";
    private int last_level = 0;
    private int keep_time = 0;
    private boolean allow_boker = true;
    private int invalid_count = 0;
    private boolean mCoveredTips;
    private IAppUi.HintInfo mGuideHint;
    private static final int SHOW_INFO_LENGTH_LONG = 1 * 1000;
    private int[][] bright_map = null;
    private boolean isNowSubcameraCovered = false;
    private CameraHandler mCameraHandler;
    //start, wangsenhao, yuvcamera2 bokeh sensor, 2020.03.31
    private boolean isNowSubcamera4Covered = false;
    private boolean isDoubleSubCameraSupport = false;
    private static final String yuvcamera_state_file2 = "/sys/bus/platform/drivers/image_sensor/yuvcamera2";
    //end, wangsenhao, yuvcamera2 bokeh sensor, 2020.03.31

    //add by huangfeifor front bokeh start
    private int position_x = -1;
    private int position_y = -1;
    //add by huangfeifor front bokeh end

    //bv wuyonglin add for aiworks facebeauty and bokeh 20200722 start
    private HandlerThread mPreviewReaderThread = null;
    private Handler mPreviewReaderHandler = null;
    private ImageReader mPreviewReader;
    private Surface mPreviewReaderSurface;
    //bv wuyonglin add for aiworks facebeauty and bokeh 20200722 end
    private static Object sPreviewLock = new Object();

    /**
     * this enum is used for tag native camera open state.
     */
    private enum CameraState {
        CAMERA_UNKNOWN,
        CAMERA_OPENING,
        CAMERA_OPENED,
        CAMERA_CAPTURING,
        CAMERA_CLOSING,
    }

    /**
     * PhotoDeviceController may use activity to get display rotation.
     * @param activity the camera activity.
     */
    AiWorksBokehDevice2Controller(@Nonnull Activity activity, @Nonnull ICameraContext context) {
        LogHelper.d(TAG, "[AiWorksBokehDevice2Controller]");
        mActivity = activity;
        mCameraManager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        mICameraContext = context;
        mBGServiceKeeper = mICameraContext.getBGServiceKeeper();
        if (mBGServiceKeeper != null) {
            DeviceDescription deviceDescription = CameraApiHelper.getDeviceSpec(
                    mActivity.getApplicationContext()).getDeviceDescriptionMap().get("0");
            if (deviceDescription != null && !isThirdPartyIntent(mActivity)
                    && mBGServiceKeeper.getBGHidleService() != null) {
                mIsBGServiceEnabled = false;
                //mBGServicePrereleaseKey = deviceDescription.getKeyBGServicePrerelease();
                //mBGServiceImagereaderIdKey = deviceDescription.getKeyBGServiceImagereaderId();
            }
        }
        LogHelper.d(TAG, "mBGServiceKeeper = " + mBGServiceKeeper
                + ", isThirdPartyIntent = " + isThirdPartyIntent(mActivity)
                + ", mIsBGServiceEnabled = " + mIsBGServiceEnabled
                + ", mBGServicePrereleaseKey = " + mBGServicePrereleaseKey
                + ", mBGServiceImagereaderIdKey = " + mBGServiceImagereaderIdKey);
        int currentMode = HeifHelper.getCurrentMode();
        if (currentMode == HeifHelper.HEIF_MODE_SURFACE) {
            mCaptureSurface = new HeifCaptureSurface(mICameraContext, this);
            mIDeviceListener = (HeifCaptureSurface)mCaptureSurface;
        } else if (mIsBGServiceEnabled) {
            mCaptureSurface = new CaptureSurface(mBGServiceKeeper.getBGCaptureHandler());
            LogHelper.d(TAG, "BG mCaptureSurface = " + mCaptureSurface);
        } else {
            mCaptureSurface = new CaptureSurface();
        }
        mCaptureSurface.setCaptureCallback(this);
        mThumbnailSurface = new CaptureSurface();
        mThumbnailSurface.setCaptureCallback(this);
        mThumbnailSurface.setFormat(ThumbnailHelper.FORMAT_TAG);
        mCameraDeviceManager = mICameraContext.getDeviceManager(CameraApi.API2);

        mDataStore = mICameraContext.getDataStore();
        mDefaultBokehLevel = mActivity.getResources().getInteger(R.integer.aiworks_bokeh_level_default);

        mCoveredTips = activity.getResources().getBoolean(R.bool.config_bokeh_covered_tips_support);
        //add by Jerry +
        mGuideHint = new IAppUi.HintInfo();
        int id = activity.getResources().getIdentifier("hint_text_background",
                "drawable", activity.getPackageName());
        mGuideHint.mBackground = activity.getDrawable(id);
        mGuideHint.mType = IAppUi.HintType.TYPE_MANUAL_HIDE;
        mGuideHint.mDelayTime = SHOW_INFO_LENGTH_LONG;
        HandlerThread handlerThread2 = new HandlerThread("Camera2");
        handlerThread2.start();
        mCameraHandler = new CameraHandler(handlerThread2.getLooper());
        //start, wangsenhao, yuvcamera2 bokeh sensor, 2020.03.31
        isDoubleSubCameraSupport = activity.getResources().getBoolean(R.bool.config_double_bokeh_coveded_tips_support);
        //end, wangsenhao, yuvcamera2 bokeh sensor, 2020.03.31
        //bv wuyonglin add for aiworks facebeauty and bokeh 20200722 start
        startPreviewReaderThread();
        //bv wuyonglin add for aiworks facebeauty and bokeh 20200722 end
    }

    @Override
    public void queryCameraDeviceManager() {
        mCameraDeviceManager = mICameraContext.getDeviceManager(CameraApi.API2);
    }

    @Override
    public void openCamera(AiWorksBokehDeviceInfo info) {
        CameraSysTrace.onEventSystrace("AiWorksBokehDevice.openCamera", true, true);
        String cameraId = info.getCameraId();
        boolean sync = info.getNeedOpenCameraSync();
        LogHelper.i(TAG, "[openCamera] cameraId : " + cameraId + ",sync = " + sync);
        if (canOpenCamera(cameraId)) {
            try {
                mDeviceLock.tryLock(WAIT_TIME, TimeUnit.SECONDS);
                mNeedSubSectionInitSetting = info.getNeedFastStartPreview();
                mCurrentCameraId = cameraId;
        LogHelper.i(TAG, "[openCamera] mNeedSubSectionInitSetting : " + mNeedSubSectionInitSetting);
                //add by huangfeifor front bokeh start
                allow_boker = true;
                //add by huangfeifor front bokeh end

                updateCameraState(CameraState.CAMERA_OPENING);
                initSettingManager(info.getSettingManager());
                CameraSysTrace.onEventSystrace("openCamera.doOpenCamera", true, true);
                doOpenCamera(sync);
                CameraSysTrace.onEventSystrace("openCamera.doOpenCamera", false, true);
                if (mNeedSubSectionInitSetting) {
                    CameraSysTrace.onEventSystrace("openCamera.createSettingsByStage1", true, true);
                    mSettingManager.createSettingsByStage(1);
                    CameraSysTrace.onEventSystrace("openCamera.createSettingsByStage1", false, true);
                } else {
                    CameraSysTrace.onEventSystrace("openCamera.createAllSettings", true);
                    mSettingManager.createAllSettings();
                    CameraSysTrace.onEventSystrace("openCamera.createAllSettings", false);
                }
                CameraSysTrace.onEventSystrace("openCamera.getCameraCharac", true);
                mCameraCharacteristics
                        = mCameraManager.getCameraCharacteristics(mCurrentCameraId);
                CameraSysTrace.onEventSystrace("openCamera.getCameraCharac", false);
                mQuickPreviewKey = CameraUtil.getAvailableSessionKeys(
                        mCameraCharacteristics, QUICK_PREVIEW_KEY); 
            } catch (CameraOpenException e) {
                if (CameraOpenException.ExceptionType.SECURITY_EXCEPTION == e.getExceptionType()) {
                    CameraUtil.showErrorInfoAndFinish(mActivity,
                            CameraUtil.CAMERA_HARDWARE_EXCEPTION);
                    updateCameraState(CameraState.CAMERA_UNKNOWN);
                    mCurrentCameraId = null;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (CameraAccessException | IllegalArgumentException e) {
                CameraUtil.showErrorInfoAndFinish(mActivity, CameraUtil.CAMERA_HARDWARE_EXCEPTION);
                updateCameraState(CameraState.CAMERA_UNKNOWN);
                mCurrentCameraId = null;
            } finally {
                mDeviceLock.unlock();
            }
        }
        CameraSysTrace.onEventSystrace("AiWorksBokehDevice.openCamera", false, true);
    }

    @Override
    public void updatePreviewSurface(Object surfaceObject) {
        LogHelper.i(TAG, "CamAp_AiWorksBokehMode [updatePreviewSurface234] onSurfaceTextureAvailable surfaceTextureRenderer surfaceHolder = " + surfaceObject + " state = "
                + mCameraState + ", session :" + mSession + ", mNeedSubSectionInitSetting:"
                + mNeedSubSectionInitSetting+" mPreviewReader ="+mPreviewReader+" mIsPictureSizeChanged ="+mIsPictureSizeChanged);
        //bv wuyonglin add for aiworks facebeauty and bokeh 20200722 start
        if (surfaceObject != null && mIsPictureSizeChanged) {
            synchronized (sPreviewLock) {
            if (null != mPreviewReader) {
                mPreviewReader.setOnImageAvailableListener(null, null);
                mPreviewReader.close();
                mPreviewReader = null;
            }
            LogHelper.i(TAG,"updatePreviewSurface2345 onSurfaceTextureAvailable mPreviewReader5 newInstance mPreviewWidth = " + mPreviewWidth+" mPreviewHeight ="+mPreviewHeight+" mPreviewReader ="+mPreviewReader);
            mPreviewReader = ImageReader.newInstance(mPreviewWidth, mPreviewHeight,
                    ImageFormat.YUV_420_888, 1);
            mPreviewReader.setOnImageAvailableListener(mPreviewReaderListener, mPreviewReaderHandler);
	    }
	}
        //bv wuyonglin add for aiworks facebeauty and bokeh 20200722 end
        synchronized (mSurfaceHolderSync) {
            if (surfaceObject instanceof SurfaceHolder) {
                mPreviewSurface = surfaceObject == null ? null :
                        ((SurfaceHolder) surfaceObject).getSurface();
            } else if (surfaceObject instanceof SurfaceTexture) {
                mPreviewSurface = surfaceObject == null ? null :
                        new Surface((SurfaceTexture) surfaceObject);
            }
            boolean isStateReady = CameraState.CAMERA_OPENED == mCameraState;
            if (isStateReady && mCamera2Proxy != null) {
                boolean onlySetSurface = mSurfaceObject == null && surfaceObject != null;
                mSurfaceObject = surfaceObject;
                if (surfaceObject == null) {
                    stopPreview();
                } else if (onlySetSurface && mNeedSubSectionInitSetting) {
                    mOutputConfigs.get(0).addSurface(mPreviewSurface);
                    if (mSession != null) {
                        mSession.finalizeOutputConfigurations(mOutputConfigs);
                        mNeedFinalizeOutput = false;
                        if (CameraState.CAMERA_OPENED == getCameraState()) {
                            repeatingPreview(false);
                            configSettingsByStage2();
                            repeatingPreview(false);
                        }
                    } else {
                        mNeedFinalizeOutput = true;
                    }
                } else {
                    configureSession(false);
                }
            }
        }
        LogHelper.i(TAG, "CamAp_AiWorksBokehMode [updatePreviewSurface234] onSurfaceTextureAvailable end");
    }

    @Override
    public void setDeviceCallback(DeviceCallback callback) {
        mModeDeviceCallback = callback;
    }

    @Override
    public void setPreviewSizeReadyCallback(PreviewSizeCallback callback) {
        mPreviewSizeCallback = callback;
    }

    /**
     * Set the new picture size.
     *
     * @param size current picture size.
     */
    @Override
    public void setPictureSize(Size size) {
        String formatTag = mSettingController.queryValue(HeifHelper.KEY_FORMAT);
        int format = HeifHelper.getCaptureFormat(formatTag);
        mCaptureSurface.setFormat(formatTag);
        int rotation = CameraUtil.getJpegRotationFromDeviceSpec(
                Integer.parseInt(mCurrentCameraId), mJpegRotation, mActivity);

        HeifHelper.orientation = rotation;
        mIsPictureSizeChanged = mCaptureSurface.updatePictureInfo(size.getWidth(),
                size.getHeight(), format, CAPTURE_MAX_NUMBER);
        LogHelper.d(TAG, "[setPictureSize] getJpegRotationFromDeviceSpec rotation = " + rotation + ",mJpegRotation = " + mJpegRotation+" format ="+format+" size.getWidth() ="+size.getWidth()+" size.getHeight() ="+size.getHeight()+" format ="+format+" mIsPictureSizeChanged ="+mIsPictureSizeChanged);
        if (mIsBGServiceEnabled) {
            mBGServiceKeeper.setBGCaptureSurface(mCaptureSurface);
        }
        double ratio = (double) size.getWidth() / size.getHeight();
        mPictureWidth = size.getWidth();
        mPictureHeight = size.getHeight();
        ThumbnailHelper.updateThumbnailSize(ratio);
        if (ThumbnailHelper.isPostViewSupported()) {
            mThumbnailSurface.updatePictureInfo(ThumbnailHelper.getThumbnailWidth(),
                    ThumbnailHelper.getThumbnailHeight(),
                    ThumbnailHelper.IMAGE_BUFFER_FORMAT,
                    CAPTURE_MAX_NUMBER);
        }
    }

    /**
     * Check whether can take picture or not.
     *
     * @return true means can take picture; otherwise can not take picture.
     */
    @Override
    public boolean isReadyForCapture() {
        boolean canCapture = mSession != null
                && mCamera2Proxy != null && getCameraState() == CameraState.CAMERA_OPENED;
        LogHelper.i(TAG, "[isReadyForCapture] canCapture = " + canCapture);
        return canCapture;
    }

    @Override
    public void destroyDeviceController() {
        //bv wuyonglin add for from bokeh mode to panorama mode bokeh covered tips should not appeared 20200306 start
        if (mCoveredTips) {
            mCameraHandler.removeMessages(HCT_READ_BRIGHT_CAMERA);
            mICameraContext.getIApp().getAppUi().hideScreenHint(mGuideHint);
        }
        //bv wuyonglin add for from bokeh mode to panorama mode bokeh covered tips should not appeared 20200306 end
        if (mCaptureSurface != null) {
            releaseJpegCaptureSurface();
        }
        if (mThumbnailSurface != null) {
            mThumbnailSurface.release();
        }
        mPreviewReaderHandler.getLooper().quitSafely();
    }

    @Override
    public void startPreview() {
        LogHelper.i(TAG, "[startPreview]");
        configureSession(false);
    }

    @Override
    public void stopPreview() {
        LogHelper.i(TAG, "surfaceTextureRenderer [stopPreview1] mPreviewReader ="+mPreviewReader);
        if(mCoveredTips){
            mCameraHandler.removeMessages(HCT_READ_BRIGHT_CAMERA);
            mICameraContext.getIApp().getAppUi().hideScreenHint(mGuideHint);
        }
        //bv wuyonglin add for aiworks facebeauty and bokeh 20200722 start
        synchronized (sPreviewLock) {
        mPreviewReaderHandler.removeCallbacksAndMessages(null);
        if (null != mPreviewReader) {
            mPreviewReader.setOnImageAvailableListener(null, null);
            mPreviewReader.close();
            mPreviewReader = null;
        }
        }
        abortOldSession();
        //bv wuyonglin add for aiworks facebeauty and bokeh 20200722 end
    }

    @Override
    public void takePicture(@Nonnull IAiWorksBokehDeviceController.CaptureDataCallback callback) {
        LogHelper.i(TAG, "[takePicture] mSession= " + mSession);
        CameraSysTrace.onEventSystrace("AiWorksBokehDevice.takePicture", true, true);
        if (mSession != null && mCamera2Proxy != null) {
            mCaptureDataCallback = callback;
            updateCameraState(CameraState.CAMERA_CAPTURING);
            try {
                Builder builder = doCreateAndConfigRequest(Camera2Proxy.TEMPLATE_STILL_CAPTURE);
                mSession.capture(builder.build(), mCaptureCallback, mModeHandler);
                if (mIDeviceListener != null) {
                    mIDeviceListener.onTakePicture();
                }
            } catch (CameraAccessException e) {
                e.printStackTrace();
                LogHelper.e(TAG, "[takePicture] error because create build fail.");
            }
        }
        CameraSysTrace.onEventSystrace("AiWorksBokehDevice.takePicture", false, true);
    }

    @Override
    public void updateGSensorOrientation(int orientation) {
        //modify by huangfei  for mJpegRotation abnormal;
        LogHelper.i(TAG, "[updateGSensorOrientation] orientation= " + orientation);
        if(orientation==-1){
            mJpegRotation = 0;
        }else{
            mJpegRotation = orientation;
        }
        //modify by huangfei  for mJpegRotation end;
    }

    @Override
    public void closeSession() {
        if (mSession != null) {
            try {
        LogHelper.i(TAG, "surfaceTextureRenderer [closeSession] " );
                mSession.abortCaptures();
                mSession.close();
                mPreviewSurface.release();
                mPreviewReaderSurface.release();
            } catch (CameraAccessException e) {
                LogHelper.e(TAG, "[closeSession] exception", e);
            }
        }
        mSession = null;
        mBuilder = null;
        mDefaultBuilder = null;
    }

    @Override
    public void closeCamera(boolean sync) {
        LogHelper.i(TAG, "surfaceTextureRenderer [closeCamera] + sync = " + sync + " current state : " + mCameraState);
        CameraSysTrace.onEventSystrace("AiWorksBokehDevice.closeCamera", true, true);

        if (CameraState.CAMERA_UNKNOWN != mCameraState) {
            try {
                mDeviceLock.tryLock(WAIT_TIME, TimeUnit.SECONDS);
                super.doCameraClosed(mCamera2Proxy);
                updateCameraState(CameraState.CAMERA_CLOSING);
                abortOldSession();
                if (mModeDeviceCallback != null) {
                    mModeDeviceCallback.beforeCloseCamera();
                }
                doCloseCamera(sync);
                updateCameraState(CameraState.CAMERA_UNKNOWN);
                recycleVariables();
                releaseJpegCaptureSurface();
                mThumbnailSurface.releaseCaptureSurface();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                super.doCameraClosed(mCamera2Proxy);
                mDeviceLock.unlock();
            }
            recycleVariables();
        }
        mCurrentCameraId = null;
        CameraSysTrace.onEventSystrace("AiWorksBokehDevice.closeCamera", false, true);
        LogHelper.i(TAG, "[closeCamera] -");
    }

    @Override
    public Size getPreviewSize(double targetRatio) {
        int oldPreviewWidth = mPreviewWidth;
        int oldPreviewHeight = mPreviewHeight;
        getTargetPreviewSize(targetRatio);
        boolean isSameSize = oldPreviewHeight == mPreviewHeight && oldPreviewWidth == mPreviewWidth;
        LogHelper.i(TAG, "[getPreviewSize] old size : " + oldPreviewWidth + " X " +
                oldPreviewHeight + " new  size :" + mPreviewWidth + " X " + mPreviewHeight);
        //if preview size don't change, but picture size changed,need do configure the surface.
        //if preview size changed,do't care the picture size changed,because surface will be
        //changed.
        if (isSameSize && mIsPictureSizeChanged) {
            configureSession(false);
        }
        return new Size(mPreviewWidth, mPreviewHeight);
    }

    @Override
    public void onPictureCallback(byte[] data,
                                  int format, String formatTag, int width, int height) {
        LogHelper.d(TAG, "<onPictureCallback> data = " + data + ", format = " + format
                + ", formatTag" + formatTag + ", width = " + width + ", height = " + height
                + ", mCaptureDataCallback = " + mCaptureDataCallback+" ThumbnailHelper.isPostViewSupported() ="+ThumbnailHelper.isPostViewSupported());
        if (mCaptureDataCallback != null) {
            DataCallbackInfo info = new DataCallbackInfo();
            info.data = data;
            info.needUpdateThumbnail = true;
            info.needRestartPreview = false;
            info.mBufferFormat = format;
            info.imageHeight = height;
            info.imageWidth = width;
            if (ThumbnailHelper.isPostViewSupported()) {
                info.needUpdateThumbnail = false;
            }         
            if (ThumbnailHelper.FORMAT_TAG.equalsIgnoreCase(formatTag)) {
                CameraSysTrace.onEventSystrace("AiWorksBokehDevice.onPostViewCallback", true, true);
                mCaptureDataCallback.onPostViewCallback(data);
                CameraSysTrace.onEventSystrace("AiWorksBokehDevice.onPostViewCallback", false, true);
            } else {
                CameraSysTrace.onEventSystrace("AiWorksBokehDevice.onJpegCallback", true, true);
                mCaptureDataCallback.onDataReceived(info);
                CameraSysTrace.onEventSystrace("AiWorksBokehDevice.onJpegCallback", false, true);
                if (mIsBGServiceEnabled && mCaptureSurface != null) {
                    mCaptureSurface.decreasePictureNum();
                    if (mCaptureSurface.shouldReleaseCaptureSurface()
                            && mCaptureSurface.getPictureNumLeft() == 0) {
                        mCaptureSurface.releaseCaptureSurface();
                        mCaptureSurface.releaseCaptureSurfaceLater(false);
                    }
                }
            }
        }
    }

    @Override
    public void createAndChangeRepeatingRequest() {
            LogHelper.e(TAG, "new createAndChangeRepeatingRequest to  repeatingPreview true");
        if (mCamera2Proxy == null || mCameraState != CameraState.CAMERA_OPENED) {
            LogHelper.e(TAG, "camera is closed or in opening state can't request ");
            return;
        }
        repeatingPreview(true);
    }

    @Override
    public CaptureRequest.Builder createAndConfigRequest(int templateType) {
        CaptureRequest.Builder builder = null;
        try {
            builder = doCreateAndConfigRequest(templateType);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return builder;
    }

    @Override
    public CaptureSurface getModeSharedCaptureSurface() throws IllegalStateException {
        if (CameraState.CAMERA_UNKNOWN == getCameraState()
                || CameraState.CAMERA_CLOSING == getCameraState()) {
            throw new IllegalStateException("get invalid capture surface!");
        } else {
            return mCaptureSurface;
        }
    }

    @Override
    public Surface getModeSharedPreviewSurface() throws IllegalStateException {
        if (CameraState.CAMERA_UNKNOWN == getCameraState()
                || CameraState.CAMERA_CLOSING == getCameraState()) {
            throw new IllegalStateException("get invalid capture surface!");
        } else {
            return mPreviewSurface;
        }
    }

    @Override
    public Surface getModeSharedThumbnailSurface() throws IllegalStateException {
        if (CameraState.CAMERA_UNKNOWN == getCameraState()
                || CameraState.CAMERA_CLOSING == getCameraState()) {
            throw new IllegalStateException("get invalid capture surface!");
        } else {
            return mThumbnailSurface.getSurface();
        }
    }

    @Override
    public Camera2CaptureSessionProxy getCurrentCaptureSession() {
        return mSession;
    }

    @Override
    public void requestRestartSession() {
        configureSession(false);
    }

    @Override
    public int getRepeatingTemplateType() {
        return Camera2Proxy.TEMPLATE_PREVIEW;
    }

    /**
     * Judge current is launch by intent.
     * @param activity the launch activity.
     * @return true means is launch by intent; otherwise is false.
     */
    protected boolean isThirdPartyIntent(Activity activity) {
        Intent intent = activity.getIntent();
        String action = intent.getAction();
        boolean value = MediaStore.ACTION_IMAGE_CAPTURE.equals(action) ||
                MediaStore.ACTION_VIDEO_CAPTURE.equals(action);
        return value;
    }

    private void releaseJpegCaptureSurface() {
        if (!mIsBGServiceEnabled) {
            mCaptureSurface.releaseCaptureSurface();
        } else {
            if (mCaptureSurface.getPictureNumLeft() != 0) {
                mCaptureSurface.releaseCaptureSurfaceLater(true);
            } else {
                mBGServiceKeeper.releaseCaptureSurfaceList();  
                mCaptureSurface.releaseCaptureSurface();
            }
        }
    }

    private void initSettingManager(ISettingManager settingManager) {
        mSettingManager = settingManager;
        settingManager.updateModeDevice2Requester(this);
        mSettingDevice2Configurator = settingManager.getSettingDevice2Configurator();
        mSettingController = settingManager.getSettingController();
    }

    private void doOpenCamera(boolean sync) throws CameraOpenException {
        if (sync) {
            mCameraDeviceManager.openCameraSync(mCurrentCameraId, mDeviceCallback, null);
        } else {
            mCameraDeviceManager.openCamera(mCurrentCameraId, mDeviceCallback, null);
        }
    }

    private void updateCameraState(CameraState state) {
        LogHelper.d(TAG, "[updateCameraState] new state = " + state + " old =" + mCameraState);
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

    private void doCloseCamera(boolean sync) {
        if (sync) {
            mCameraDeviceManager.closeSync(mCurrentCameraId);
        } else {
            mCameraDeviceManager.close(mCurrentCameraId);
        }
        mCaptureFrameMap.clear();
        if(mCoveredTips){
            mCameraHandler.removeMessages(HCT_READ_BRIGHT_CAMERA);
            mICameraContext.getIApp().getAppUi().hideScreenHint(mGuideHint);
        }
        mCamera2Proxy = null;
        synchronized (mSurfaceHolderSync) {
            mSurfaceObject = null;
            mPreviewSurface = null;
            mPreviewReaderSurface = null;
        }
    }

    private void recycleVariables() {
        mCurrentCameraId = null;
        updatePreviewSurface(null);
        mCamera2Proxy = null;
        mIsPictureSizeChanged = false;
    }

    private boolean canOpenCamera(String newCameraId) {
        boolean isSameCamera = newCameraId.equalsIgnoreCase(mCurrentCameraId);
        boolean isStateReady = mCameraState == CameraState.CAMERA_UNKNOWN;
        boolean value = !isSameCamera && isStateReady;
        LogHelper.i(TAG, "[canOpenCamera] new id: " + newCameraId + " current camera :" +
                mCurrentCameraId + " isSameCamera = " + isSameCamera + " current state : " +
                mCameraState + " isStateReady = " + isStateReady + " can open : " + value);
        return value;
    }

    private void configureSession(boolean isFromOpen) {
        LogHelper.i(TAG, "onSurfaceTextureAvailable surfaceTextureRenderer [configureSession] +" + ", isFromOpen :" + isFromOpen+" ThumbnailHelper.isPostViewSupported() ="+ThumbnailHelper.isPostViewSupported());
	//bv wuyonglin add for restore settings happened error 20201029 start
	if (mPreviewReader == null) {
	    LogHelper.e(TAG, "[configureSession] mPreviewReader null return");
	    return;
	}
	//bv wuyonglin add for restore settings happened error 20201029 end
        mDeviceLock.lock();
        mFirstFrameArrived = false;
        try {
            if (mCamera2Proxy != null) {
                abortOldSession();
                mCaptureSurface.releaseCaptureSurfaceLater(false);
                //bv wuyonglin add for aiworks facebeauty and bokeh 20200722 start
                mPreviewReaderSurface = mPreviewReader.getSurface();
                //bv wuyonglin add for aiworks facebeauty and bokeh 20200722 end
                if (isFromOpen) {
                    mOutputConfigs = new ArrayList<>();
                    android.util.Size previewSize = new android.util.Size(mPreviewWidth,
                            mPreviewHeight);
                    OutputConfiguration previewConfig = new OutputConfiguration(previewSize,
                            SurfaceTexture.class);
                    OutputConfiguration captureConfig
                            = new OutputConfiguration(mCaptureSurface.getSurface());
                    OutputConfiguration rawConfig
                            = mSettingDevice2Configurator.getRawOutputConfiguration();
                    mOutputConfigs.add(previewConfig);
                    mOutputConfigs.add(captureConfig);
                    if (rawConfig != null) {
                        mOutputConfigs.add(rawConfig);
                    }
                    if (ThumbnailHelper.isPostViewSupported()) {
                        OutputConfiguration thumbnailConfig
                                = new OutputConfiguration(mThumbnailSurface.getSurface());
                        mOutputConfigs.add(thumbnailConfig);
                    }

                    //bv wuyonglin add for aiworks facebeauty and bokeh 20200722 start
                    mOutputConfigs.add(new OutputConfiguration(mPreviewReaderSurface));
                    //bv wuyonglin add for aiworks facebeauty and bokeh 20200722 end
                    mBuilder = getDefaultPreviewBuilder();
                    //bv wuyonglin add for aiworks facebeauty and bokeh 20200722 start
                    mBuilder.addTarget(mPreviewReaderSurface);
                    //bv wuyonglin add for aiworks facebeauty and bokeh 20200722 end
                    mSettingDevice2Configurator.configCaptureRequest(mBuilder);
                    configureQuickPreview(mBuilder);
                    configureBGService(mBuilder);
                    configurePdafImgo(mBuilder);
                    CameraSysTrace.onEventSystrace("configureSession.createCaptureSession.fromopen", true, true);
                    mCamera2Proxy.createCaptureSession(mSessionCallback,
                            mModeHandler, mBuilder, mOutputConfigs);
                    CameraSysTrace.onEventSystrace("configureSession.createCaptureSession.fromopen", false, true);
                    mIsPictureSizeChanged = false;
                    return;
                }
                List<Surface> surfaces = new LinkedList<>();
                surfaces.add(mPreviewSurface);
                surfaces.add(mCaptureSurface.getSurface());
                if (ThumbnailHelper.isPostViewSupported()) {
                    surfaces.add(mThumbnailSurface.getSurface());
                }
                //bv wuyonglin add for aiworks facebeauty and bokeh 20200722 start
                surfaces.add(mPreviewReaderSurface);
                //bv wuyonglin add for aiworks facebeauty and bokeh 20200722 end
                mNeedFinalizeOutput = false;
                mSettingDevice2Configurator.configSessionSurface(surfaces);
                LogHelper.d(TAG, "[configureSession] surface size : " + surfaces.size());
                mBuilder = doCreateAndConfigRequest(Camera2Proxy.TEMPLATE_PREVIEW);
                //bv wuyonglin add for aiworks facebeauty and bokeh 20200722 start
                mBuilder.addTarget(mPreviewReaderSurface);
                //bv wuyonglin add for aiworks facebeauty and bokeh 20200722 end
                CameraSysTrace.onEventSystrace("configureSession.createCaptureSession", true, true);
                mCamera2Proxy.createCaptureSession(surfaces, mSessionCallback,
                        mModeHandler, mBuilder);
                CameraSysTrace.onEventSystrace("configureSession.createCaptureSession", false, true);
                mIsPictureSizeChanged = false;
            }
        } catch (CameraAccessException e) {
            LogHelper.e(TAG, "[configureSession] error");
        } finally {
            mDeviceLock.unlock();
        }
    }

    private void configSettingsByStage2() {
        CameraSysTrace.onEventSystrace("photoDevice.configSettingsByStage2", true);
        mSettingManager.createSettingsByStage(2);
        mSettingDevice2Configurator.setCameraCharacteristics(mCameraCharacteristics);
        P2DoneInfo.setCameraCharacteristics(mActivity.getApplicationContext(),
                    Integer.parseInt(mCurrentCameraId));
        mSettingDevice2Configurator.configCaptureRequest(mBuilder);

        //modify by huangfeifor front bokeh start
        //Relation relation = AiWorksBokehRestriction.getRestriction().getRelation("on", true);
        Relation relation = null;
        if(Config.isFrontBokehSupport(mActivity)){
            relation = AiWorksBokehRestriction.getFrontRestriction().getRelation("on", true);
        }else{
            relation = AiWorksBokehRestriction.getRestriction().getRelation("on", true);
        }
        //modify by huangfeifor front bokeh end

        mSettingManager.getSettingController().postRestriction(relation);
        mSettingController.addViewEntry();
        mSettingController.refreshViewEntry();
        CameraSysTrace.onEventSystrace("photoDevice.configSettingsByStage2", false);
    }

    private void abortOldSession() {
        if (mSession != null) {
            try {
                mSession.abortCaptures();
            } catch (CameraAccessException e) {
                LogHelper.e(TAG, "[abortOldSession] exception", e);
            }
        }
        mSession = null;
        mBuilder = null;
        mDefaultBuilder = null;
    }

    private void configureQuickPreview(Builder builder) {
        LogHelper.d(TAG, "configureQuickPreview mQuickPreviewKey:" + mQuickPreviewKey);
        if (mQuickPreviewKey != null) {
            builder.set(mQuickPreviewKey, QUICK_PREVIEW_KEY_VALUE);
        }
    }

    private void configurePdafImgo(Builder builder) {
        if (mCurrentCameraId != null) {
            DeviceDescription deviceDescription = CameraApiHelper.getDeviceSpec(
                    mActivity.getApplicationContext())
                    .getDeviceDescriptionMap()
                    .get(mCurrentCameraId);
            CaptureRequest.Key<int[]> pdafImgo = deviceDescription.getKeyPlatformCamera();
            LogHelper.d(TAG, "configurePdafImgo pdafImgo:" + pdafImgo);
            if (pdafImgo != null) {
                int[] value = new int[1];
                value[0] = 1;
                builder.set(pdafImgo, value);
            }
        }
    }
    private void configureBGService(Builder builder) {
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

    private void repeatingPreview(boolean needConfigBuiler) {
        LogHelper.i(TAG, "[repeatingPreview] mSession =" + mSession + " mCamera =" +
                mCamera2Proxy + ",needConfigBuiler " + needConfigBuiler);
        CameraSysTrace.onEventSystrace("AiWorksBokehDevice.repeatingPreview", true, true);
        if (mSession != null && mCamera2Proxy != null) {
            try {
                if (needConfigBuiler) {
                    Builder builder = doCreateAndConfigRequest(Camera2Proxy.TEMPLATE_PREVIEW);
                    //bv wuyonglin add for aiworks facebeauty and bokeh 20200722 start
                    builder.addTarget(mPreviewReaderSurface);
                    //bv wuyonglin add for aiworks facebeauty and bokeh 20200722 end
                    mSession.setRepeatingRequest(builder.build(), mCaptureCallback, mModeHandler);
                } else {
                    mBuilder.addTarget(mPreviewSurface);
                    mSession.setRepeatingRequest(mBuilder.build(), mCaptureCallback, mModeHandler);
                }
                mCaptureSurface.setCaptureCallback(this);
            } catch (CameraAccessException | RuntimeException e) {
                LogHelper.e(TAG, "[repeatingPreview] error");
            }
        }
        CameraSysTrace.onEventSystrace("AiWorksBokehDevice.repeatingPreview", false, true);
    }

    private Builder doCreateAndConfigRequest(int templateType) throws CameraAccessException {
        LogHelper.i(TAG, "[doCreateAndConfigRequest] mCamera2Proxy =" + mCamera2Proxy);
        /*int progress = getBokehLevel();
        if (progress== -1){
            mbokehLevel = seekbarmax/2;
            mDataStore.setValue("pi_bokeh_progress",mbokehLevel+"",mDataStore.getGlobalScope(),false);
        }else{
            mbokehLevel = progress;
        }*/

        CaptureRequest.Builder builder = null;
        if (mCamera2Proxy != null) {
            builder = mCamera2Proxy.createCaptureRequest(templateType);
            if (builder == null) {
                LogHelper.d(TAG, "Builder is null, ignore this configuration");
                return null;
            }
            mSettingDevice2Configurator.configCaptureRequest(builder);
            ThumbnailHelper.configPostViewRequest(builder);
            configureQuickPreview(builder);
            configureBGService(builder);
            configurePdafImgo(builder);
            if (Camera2Proxy.TEMPLATE_PREVIEW == templateType) {
                builder.addTarget(mPreviewSurface);
            } else if (Camera2Proxy.TEMPLATE_STILL_CAPTURE == templateType) {
                builder.addTarget(mCaptureSurface.getSurface());
                if ("off".equalsIgnoreCase(mZsdStatus)) {
                    builder.addTarget(mPreviewSurface);
                }
                if (ThumbnailHelper.isPostViewOverrideSupported()) {
                    builder.addTarget(mThumbnailSurface.getSurface());
                }
                ThumbnailHelper.setDefaultJpegThumbnailSize(builder);
                P2DoneInfo.enableP2Done(builder);
                CameraUtil.enable4CellRequest(mCameraCharacteristics, builder);
                int rotation = CameraUtil.getJpegRotationFromDeviceSpec(
                        Integer.parseInt(mCurrentCameraId), mJpegRotation, mActivity);
        LogHelper.d(TAG, "[doCreateAndConfigRequest] getJpegRotationFromDeviceSpec rotation = " + rotation + ",mJpegRotation = " + mJpegRotation+" mPreviewReaderSurface ="+mPreviewReaderSurface);	
                HeifHelper.orientation = rotation;
                builder.set(CaptureRequest.JPEG_ORIENTATION, rotation);
                        
                if (mICameraContext.getLocation() != null) {
                    if (!CameraUtil.is3rdPartyIntentWithoutLocationPermission(mActivity)) {
                        builder.set(CaptureRequest.JPEG_GPS_LOCATION,
                                mICameraContext.getLocation());
                    }
                }
            }
            //setBokehParameter(builder,allow_boker);
            //bv wuyonglin add for open mfb 20201114 start
            builder.set(VendorTagRequest.MTK_MFB_MODE, new int[]{255});
            //bv wuyonglin add for open mfb 20201114 end

        }
        return builder;
    }

    private Builder getDefaultPreviewBuilder() throws CameraAccessException {
        if (mCamera2Proxy != null && mDefaultBuilder == null) {
            mDefaultBuilder = mCamera2Proxy.createCaptureRequest(Camera2Proxy.TEMPLATE_PREVIEW);
            //setBokehParameter(mDefaultBuilder,allow_boker);
            //bv wuyonglin add for open mfb 20201114 start
            mDefaultBuilder.set(VendorTagRequest.MTK_MFB_MODE, new int[]{255});
            //bv wuyonglin add for open mfb 20201114 end
            ThumbnailHelper.configPostViewRequest(mDefaultBuilder);
        }
        return mDefaultBuilder;
    }

    private Size getTargetPreviewSize(double ratio) {
        Size values = null;
        try {
            CameraCharacteristics cs = mCameraManager.getCameraCharacteristics(mCurrentCameraId);
            StreamConfigurationMap streamConfigurationMap =
                    cs.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            android.util.Size previewSizes[] =
                    streamConfigurationMap.getOutputSizes(SurfaceHolder.class);
            int length = previewSizes.length;
            List<Size> sizes = new ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                sizes.add(i, new Size(previewSizes[i].getWidth(), previewSizes[i].getHeight()));
            }
            values = CameraUtil.getOptimalPreviewSize(mActivity, sizes, ratio, true);
            mPreviewWidth = values.getWidth();
            mPreviewHeight = values.getHeight();
        } catch (Exception e) {
            e.printStackTrace();
            LogHelper.e(TAG, "camera process killed due to getCameraCharacteristics() error");
            Process.killProcess(Process.myPid());
        }
        LogHelper.d(TAG, "[getTargetPreviewSize] " + mPreviewWidth + " X " + mPreviewHeight);
        return values;
    }

    private void updatePreviewSize() {
        ISettingManager.SettingController controller = mSettingManager.getSettingController();
        String pictureSize = controller.queryValue(KEY_PICTURE_SIZE);
        LogHelper.i(TAG, "[updatePreviewSize] :" + pictureSize);
        if (pictureSize != null) {
            String[] pictureSizes = pictureSize.split("x");
            int width = Integer.parseInt(pictureSizes[0]);
            int height = Integer.parseInt(pictureSizes[1]);
            double ratio = (double) width / height;
            getTargetPreviewSize(ratio);
        }
    }

    private void updatePictureSize() {
        ISettingManager.SettingController controller = mSettingManager.getSettingController();
        String pictureSize = controller.queryValue(KEY_PICTURE_SIZE);
        LogHelper.i(TAG, "[updatePictureSize] :" + pictureSize);
        if (pictureSize != null) {
            String[] pictureSizes = pictureSize.split("x");
            int width = Integer.parseInt(pictureSizes[0]);
            int height = Integer.parseInt(pictureSizes[1]);
            mPictureWidth = width;
            mPictureHeight = height;
            setPictureSize(new Size(width, height));
        }
    }


    @Override
    public void doCameraOpened(@Nonnull Camera2Proxy camera2proxy) {
        LogHelper.i(TAG, "[onOpened]  camera2proxy = " + camera2proxy + " preview surface = "
                + mPreviewSurface + "  mCameraState = " + mCameraState + "camera2Proxy id = "
                + camera2proxy.getId() + " mCameraId = " + mCurrentCameraId);
            try {
                if (CameraState.CAMERA_OPENING == getCameraState()
                        && camera2proxy != null && camera2proxy.getId().equals(mCurrentCameraId)) {
                    mCamera2Proxy = camera2proxy;
                    mFirstFrameArrived = false;
                    CameraSysTrace.onEventSystrace("donCameraOpened.onCameraOpened", true, true);
                    if (mModeDeviceCallback != null) {
                        mModeDeviceCallback.onCameraOpened(mCurrentCameraId);
                    }
                    CameraSysTrace.onEventSystrace("donCameraOpened.onCameraOpened", false, true);
                    updateCameraState(CameraState.CAMERA_OPENED);
                    ThumbnailHelper.setCameraCharacteristics(mCameraCharacteristics,
                            mActivity.getApplicationContext(), Integer.parseInt(mCurrentCameraId));
                    CameraSysTrace.onEventSystrace("donCameraOpened.setCameraCharacteristics", true, true);
                    mSettingDevice2Configurator.setCameraCharacteristics(mCameraCharacteristics);
                    CameraSysTrace.onEventSystrace("donCameraOpened.setCameraCharacteristics", false, true);
                    CameraSysTrace.onEventSystrace("donCameraOpened.updatePreviewPictureSize", true, true);
                    updatePreviewSize();
                    updatePictureSize();
                    //bv wuyonglin add for aiworks facebeauty and bokeh 20200722 start
                    if (null != mPreviewReader) {
                        mPreviewReader.setOnImageAvailableListener(null, null);
                        mPreviewReader.close();
                        mPreviewReader = null;
                    }
                    LogHelper.i(TAG,"doCameraOpened mPreviewReader5 newInstance mPreviewWidth = " + mPreviewWidth+" mPreviewHeight ="+mPreviewHeight+" mPreviewReader ="+mPreviewReader);
                    mPreviewReader = ImageReader.newInstance(mPreviewWidth, mPreviewHeight, ImageFormat.YUV_420_888, 1);
                    mPreviewReader.setOnImageAvailableListener(mPreviewReaderListener, mPreviewReaderHandler);
                    //bv wuyonglin add for aiworks facebeauty and bokeh 20200722 end
                    CameraSysTrace.onEventSystrace("donCameraOpened.updatePreviewPictureSize", false, true);
                    if (mPreviewSizeCallback != null) {
                        mPreviewSizeCallback.onPreviewSizeReady(new Size(mPreviewWidth,
                                mPreviewHeight));
                    }
                    //bv wuyonglin delete for switcher camera in top quickswitch setting icon not show with other icon 20200307 start
                    /*try {
                        initSettings();
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }*/
                    //bv wuyonglin delete for switcher camera in top quickswitch setting icon not show with other icon 20200307 end
                    //bv wuyonglin add for pro mode adpte A80Pro pibokeh 20200615 start
                    /*if (CameraUtil.getDeviceModel().equals("BV5900")) {
                    if("0".equals(mCurrentCameraId)){
                        bright_map = Bv5900Utils.getBrightMap();
                    }else{
                        bright_map = Bv5900Utils.getBrightMap1();
                    }
                    } else {
                    if("0".equals(mCurrentCameraId)){
                        bright_map = BvUtils.getBrightMap();
                    }else{
                        bright_map = BvUtils.getBrightMap1();
    
                    }
                    }
                    //bv wuyonglin add for pro mode adpte A80Pro pibokeh 20200615 end
                    if(mCoveredTips){
                        mCameraHandler.removeMessages(HCT_READ_BRIGHT_CAMERA);
                        mCameraHandler.sendEmptyMessageDelayed(HCT_READ_BRIGHT_CAMERA, 1600);
                    }*/
                    if (mNeedSubSectionInitSetting) {
                        configureSession(true);
                    } else {
                        //bv wuyonglin add for switcher camera in top quickswitch setting icon not show with other icon 20200307 start
                        Relation relation = AiWorksBokehRestriction.getRestriction().getRelation("on",true);
                        mSettingController.postRestriction(relation);
                        //bv wuyonglin add for switcher camera in top quickswitch setting icon not show with other icon 20200307 end
                        mSettingController.addViewEntry();
                        mSettingController.refreshViewEntry();
                    }
                }
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
    }

    @Override
    public void doCameraDisconnected(@Nonnull Camera2Proxy camera2proxy) {
        LogHelper.i(TAG, "[onDisconnected] camera2proxy = " + camera2proxy);
        if (mCamera2Proxy != null && mCamera2Proxy == camera2proxy) {
            CameraUtil.showErrorInfoAndFinish(mActivity, CameraUtil.CAMERA_ERROR_SERVER_DIED);
            updateCameraState(CameraState.CAMERA_UNKNOWN);
            mCurrentCameraId = null;
        }
    }

    @Override
    public void doCameraError(@Nonnull Camera2Proxy camera2Proxy, int error) {
        LogHelper.i(TAG, "[onError] camera2proxy = " + camera2Proxy + " error = " + error);
        if ((mCamera2Proxy != null && mCamera2Proxy == camera2Proxy)
                || error == CameraUtil.CAMERA_OPEN_FAIL
                || error == CameraUtil.CAMERA_ERROR_EVICTED) {
            updateCameraState(CameraState.CAMERA_UNKNOWN);
            CameraUtil.showErrorInfoAndFinish(mActivity, error);
            updateCameraState(CameraState.CAMERA_UNKNOWN);
            mCurrentCameraId = null;
        }
    }

    /**
     * Camera session callback.
     */
    private final Camera2CaptureSessionProxy.StateCallback mSessionCallback = new
            Camera2CaptureSessionProxy.StateCallback() {

                @Override
                public void onConfigured(@Nonnull Camera2CaptureSessionProxy session) {
                    LogHelper.i(TAG, "[onConfigured],session = " + session
                            + ", mNeedFinalizeOutput:" + mNeedFinalizeOutput+" mOutputConfigs ="+mOutputConfigs);
                    mDeviceLock.lock();
		if (mOutputConfigs != null) {
                    LogHelper.i(TAG, "[onConfigured],session = " + session
                            + ", mNeedFinalizeOutput:" + mNeedFinalizeOutput+" mOutputConfigs.size ="+mOutputConfigs.size());
		}
                    try {
                        mSession = session;
                        if (mNeedFinalizeOutput) {
                            mSession.finalizeOutputConfigurations(mOutputConfigs);
                            mNeedFinalizeOutput = false;
                            if (CameraState.CAMERA_OPENED == getCameraState()) {
                                synchronized (mSurfaceHolderSync) {
                                    if (mPreviewSurface != null) {
                                        repeatingPreview(false);
                                        configSettingsByStage2();
                                        repeatingPreview(false);
                                    }
                                }
                            }
                            return;
                        }
                        if (CameraState.CAMERA_OPENED == getCameraState()) {
                            synchronized (mSurfaceHolderSync) {
                                if (mPreviewSurface != null) {
                                    repeatingPreview(false);
                                }
                            }
                        }
                    } finally {
                        mDeviceLock.unlock();
                    }
                }

                @Override
                public void onConfigureFailed(@Nonnull Camera2CaptureSessionProxy session) {
                    LogHelper.i(TAG, "[onConfigureFailed],session = " + session);
                    if (mSession == session) {
                        mSession = null;
                    }
                }

                @Override
                public void onClosed(@Nonnull Camera2CaptureSessionProxy session) {
                    super.onClosed(session);
                    LogHelper.i(TAG, "[onClosed],session = " + session);
                    if (mSession == session) {
                        mSession = null;
                    }
                }
            };

    /**
     * Capture callback.
     */
    private final CaptureCallback mCaptureCallback = new CaptureCallback() {

        @Override
        public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request, long
                timestamp, long frameNumber) {
            super.onCaptureStarted(session, request, timestamp, frameNumber);
            if (mCamera2Proxy == null || session.getDevice() != mCamera2Proxy.getCameraDevice()) {
                return;
            }
            if (CameraUtil.isStillCaptureTemplate(request)) {
                LogHelper.d(TAG, "[onCaptureStarted] capture started, frame: " + frameNumber);
                if (mIsBGServiceEnabled) {
                    mCaptureSurface.increasePictureNum();
                }
                mCaptureFrameMap.put(String.valueOf(frameNumber), Boolean.FALSE);
                mICameraContext.getSoundPlayback().play(ISoundPlayback.SHUTTER_CLICK);
            }
        }

        @Override
        public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request,
                CaptureResult partialResult) {
            super.onCaptureProgressed(session, request, partialResult);           
            if (mCamera2Proxy == null || session.getDevice() != mCamera2Proxy.getCameraDevice()) {
                return;
            }
            if (CameraUtil.isStillCaptureTemplate(request)
                    && P2DoneInfo.checkP2DoneResult(partialResult)) {
                //p2done comes, it can do next capture
                long num = partialResult.getFrameNumber();
                if (mCaptureFrameMap.containsKey(String.valueOf(num))) {
                    mCaptureFrameMap.put(String.valueOf(num), Boolean.TRUE);
                }
                CameraSysTrace.onEventSystrace("AiWorksBokehDevice.onP2Done", true, true);
                LogHelper.d(TAG, "[onCaptureProgressed] P2done comes, frame: " + num);
                updateCameraState(CameraState.CAMERA_OPENED);
                mModeDeviceCallback.onPreviewCallback(null, 0);
                CameraSysTrace.onEventSystrace("AiWorksBokehDevice.onP2Done", false, true);
            }
        }

        @Override
        public void onCaptureCompleted(@Nonnull CameraCaptureSession session,
                @Nonnull CaptureRequest request, @Nonnull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            if (mCamera2Proxy == null
                    || mModeDeviceCallback == null
                    || session.getDevice() != mCamera2Proxy.getCameraDevice()) {
                return;
            }

            //add by huangfeifor front bokeh start
            android.hardware.camera2.params.Face[] faces
                = result.get(CaptureResult.STATISTICS_FACES); ;
            Rect cropRegion = result.get(CaptureResult.SCALER_CROP_REGION);
            Rect previewRect = getPreviewRect(faces, cropRegion);
            Rect captureRect = getCaptureRect(faces, cropRegion);
            Face face =  getFaces(faces,previewRect, cropRegion);
            Face faceCapture =  getFaces(faces,captureRect, cropRegion);
            boolean isFaceExist = (faces != null && faces.length > 0 && face!=null );
            //LogHelper.e(TAG, "onCaptureCompleted new createAndChangeRepeatingRequest to  repeatingPreview true");
            /*if("1".equals(mCurrentCameraId)){
                if (isFaceExist) {
                    Rect rect = CoordinatesTransform.normalizedPreviewToUi(face.rect,
                            mPreviewWidth, mPreviewHeight,getDisplayOrientation(), true);
                    bokeh_x = Math.abs(rect.left-rect.right)/2+rect.left;
                    bokeh_y = Math.abs(rect.top-rect.bottom)/2+rect.top-Math.abs(rect.top-rect.bottom)/5;
                    Rect rectCapture = CoordinatesTransform.normalizedPreviewToUi(face.rect,
                            mPictureWidth, mPictureHeight,getDisplayOrientation(), true);
                    position_x = Math.abs(rectCapture.left-rectCapture.right)/2+rectCapture.left;
                    position_y = Math.abs(rectCapture.top-rectCapture.bottom)/2+rectCapture.top-Math.abs(rectCapture.top-rectCapture.bottom)/5;
                    allow_boker = true;
                    createAndChangeRepeatingRequest();
                } else {
                    allow_boker = false;
                    createAndChangeRepeatingRequest();
                }
            }*/
            //add by huangfeifor front bokeh end

            if (CameraUtil.isStillCaptureTemplate(result)) {
                long num = result.getFrameNumber();
                if (mCaptureFrameMap.containsKey(String.valueOf(num))
                        && Boolean.FALSE == mCaptureFrameMap.get(String.valueOf(num))) {
                    mFirstFrameArrived = true;
                    updateCameraState(CameraState.CAMERA_OPENED);
                    mModeDeviceCallback.onPreviewCallback(null, 0);
                }
                mCaptureFrameMap.remove(String.valueOf(num));
            } else if (!CameraUtil.isStillCaptureTemplate(result) && !mFirstFrameArrived) {
                CameraSysTrace.onEventSystrace("AiWorksBokehDevice.onFirstFrameArrived", true, true);
                mFirstFrameArrived = true;
                updateCameraState(CameraState.CAMERA_OPENED);
                mModeDeviceCallback.onPreviewCallback(null, 0);
                mICameraContext.getSoundPlayback().init();
                CameraSysTrace.onEventSystrace("AiWorksBokehDevice.onFirstFrameArrived", false, true);
            }
            mSettingDevice2Configurator.getRepeatingCaptureCallback().onCaptureCompleted(
                    session, request, result);
            if (mBrightValueKey == null) {
                mBrightValueKey = CameraUtil.getResultKey(mCameraCharacteristics, MTK_CONTROL_CAPTURE_BRIGHT_VALUE);
            }
            if(mBrightValueKey!=null){
                int[] brightvalues = result.get(mBrightValueKey);
                if (brightvalues != null && brightvalues.length > 0) {
                    brightvalue = brightvalues[0];
                }
            }
        }

        @Override
        public void onCaptureFailed(@Nonnull CameraCaptureSession session,
                @Nonnull CaptureRequest request, @Nonnull CaptureFailure failure) {
            super.onCaptureFailed(session, request, failure);
            LogHelper.e(TAG, "[onCaptureFailed], framenumber: " + failure.getFrameNumber()
                    + ", reason: " + failure.getReason() + ", sequenceId: "
                    + failure.getSequenceId() + ", isCaptured: " + failure.wasImageCaptured()
                    + ", mCurrentCameraId = " + mCurrentCameraId);
            if (mCamera2Proxy == null || session.getDevice() != mCamera2Proxy.getCameraDevice()) {
                return;
            }
            mSettingDevice2Configurator.getRepeatingCaptureCallback()
                    .onCaptureFailed(session, request, failure);
            if (mCurrentCameraId != null && mModeDeviceCallback != null
                    && CameraUtil.isStillCaptureTemplate(request)) {
                mCaptureFrameMap.remove(String.valueOf(failure.getFrameNumber()));
                updateCameraState(CameraState.CAMERA_OPENED);
                mModeDeviceCallback.onPreviewCallback(null, 0);
            }
        }

        @Override
        public void onCaptureSequenceAborted(CameraCaptureSession session, int sequenceId) {
            super.onCaptureSequenceAborted(session, sequenceId);
            LogHelper.d(TAG, "<onCaptureSequenceAborted>");
        }

        @Override
        public void onCaptureBufferLost(CameraCaptureSession session, CaptureRequest request,
                                        Surface target, long frameNumber) {
            super.onCaptureBufferLost(session, request, target, frameNumber);
            LogHelper.d(TAG, "<onCaptureBufferLost> frameNumber: " + frameNumber);
        }
    };

    @Override
    public void setZSDStatus(String value) {
        mZsdStatus = value;
    }

    @Override
    public void setFormat(String value) {
        LogHelper.i(TAG, "[setCaptureFormat] value = " + value + " mCameraState = " +
                getCameraState());
        if (CameraState.CAMERA_OPENED == getCameraState() && mCaptureSurface != null) {
            int format = HeifHelper.getCaptureFormat(value);
            mCaptureSurface.setFormat(value);
            mCaptureSurface.updatePictureInfo(format);
        }
    }

    @Override
    public void onFinishSaveDataCallback(Uri uri) {
        mCaptureImageSavedCallback.onFinishSaved(uri);
    }

    @Override
    public void setSavedDataCallback(CaptureImageSavedCallback callback) {
        mCaptureImageSavedCallback = callback;
    }
    @Override
    public void setBokehParameter(int level,int x,int y,int r) {
        //mbokehLevel = seekbarmax -level;
        mbokehLevel = level;
        bokeh_x = x;
        bokeh_y = y;
        mRadius = r;
        LogHelper.e(TAG, "setBokehParameter createAndChangeRepeatingRequest to  repeatingPreview true");
        createAndChangeRepeatingRequest();
    }

    public int getBokehLevel(){
        int progress = Integer.parseInt(mDataStore.getValue("aiworks_bokeh_progress",mDefaultBokehLevel+"",mDataStore.getCameraScope(Integer.parseInt(mCurrentCameraId))));
        return progress;
    }

    private void initSettings() throws CameraAccessException {
        LogHelper.i(TAG, "[openCamera] cameraId : " + "initSettings");

        //modify by huangfeifor front bokeh start
        //Relation relation = AiWorksBokehRestriction.getRestriction().getRelation("on", true);
        Relation relation = null;
        if(Config.isFrontBokehSupport(mActivity)){
            relation = AiWorksBokehRestriction.getFrontRestriction().getRelation("on", true);
        }else{
            relation = AiWorksBokehRestriction.getRestriction().getRelation("on", true);
        }
        //modify by huangfeifor front bokeh end

        mSettingController.postRestriction(relation);
        mSettingController.addViewEntry();
        mSettingController.refreshViewEntry();
    }

    private void setBokehParameter(CaptureRequest.Builder builder,boolean enable){
        
        /*CaptureRequest.Key<int[]> mBokehmode =  new CaptureRequest.Key<int[]>("com.mediatek.control.capture.xapifeaturemode", int[].class);
        CaptureRequest.Key<int[]> mBokehLevel =  new CaptureRequest.Key<int[]>("com.mediatek.control.capture.xapibokeh", int[].class);
        BOKEH_MODE_VALUE[0] = 1;
        BOKEH_MODE_VALUE[1] = 1;
        BOKEH_MODE_VALUE[2] = 1;
        if(enable){
            BOKEH_PARAMETER_VALUE[0] = getBokehLevel();
        }else{
            BOKEH_PARAMETER_VALUE[0] = 0;
        }

        //modify by huangfeifor front bokeh start
        //BOKEH_PARAMETER_VALUE[1] = mRadius;
        if("1".equals(mCurrentCameraId)){
            BOKEH_PARAMETER_VALUE[2] = bokeh_x;
            BOKEH_PARAMETER_VALUE[3] = bokeh_y;
            BOKEH_PARAMETER_VALUE[4] = position_x;
            BOKEH_PARAMETER_VALUE[5] = position_y;
            BOKEH_PARAMETER_VALUE[1] = 10;
        }else{
            BOKEH_PARAMETER_VALUE[2] = -1;
            BOKEH_PARAMETER_VALUE[3] = -1;
            BOKEH_PARAMETER_VALUE[4] = -1;
            BOKEH_PARAMETER_VALUE[5] = -1;
            BOKEH_PARAMETER_VALUE[1] = mRadius;
        }
        if(builder==null){
            LogHelper.i(TAG, "builder = null");
            return;
        }
        //modify by huangfeifor front bokeh end

        //builder.set(mBokehmode, BOKEH_MODE_VALUE);
        //builder.set(mBokehLevel, BOKEH_PARAMETER_VALUE);*/
    }
    
    private class CameraHandler extends Handler {

        public CameraHandler(Looper l) {
            super(l);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case HCT_READ_BRIGHT_CAMERA:
                    hct_set_bright_as_polling_mode();
                    break;
                default:
                    break;
            }
        }
    }
    
    private void hct_set_bright_as_polling_mode() {
        if(true){
            return;
        }
        int maincam_bright = 0;
        int subcam2_bright = 0;
        int subcam4_bright = 0;
        int reg1 = 0;
        int reg2 = 0;
        int reg3 = 0;
        int reg4 = 0;
        int level = 0;
        maincam_bright = brightvalue;
        try {
            subcam2_bright = readBrightState();
        } catch (IOException e) {
            LogHelper.e(TAG,
                    "hct_set_bright_as_polling_mode state = " + e.getMessage());
        }
        reg1 = subcam2_bright & 0xffff;
        reg2 = (subcam2_bright & 0xffff0000) >> 16;
        for (level = 0; level < bright_map.length; level++) {
            if (maincam_bright <= bright_map[level][0]) {
                break;
            }
        }
        if(level <bright_map.length) {
            /*LogHelper.i(TAG, "main:" + maincam_bright + ". reg1 = " + reg1 + ". reg2 = "
                + reg2 + ". prefered reg1 = " + bright_map[level][1]
                + ".prefered reg2 = " + bright_map[level][2]);
                LogHelper.i(TAG, "current_level = " + level + ". last_level = " + last_level
                + ". keep_time = " + keep_time + ". allow_boker = "
                + allow_boker);*/
        }
        int mTimes = mActivity.getResources().getInteger(R.integer.bright_covered_times);
        //bv wuyonglin add for pro mode adpte A80Pro pibokeh 20200616 start
        if("0".equals(mCurrentCameraId)){
            isNowSubcameraCovered = BvUtils.is_now_subcam2_covered(level, reg1, reg2);
        }else{
            isNowSubcameraCovered = BvUtils.is_now_subcam2_covered1(level, reg1, reg2);
        }
        //bv wuyonglin add for pro mode adpte A80Pro pibokeh 20200616 end
        //start, wangsenhao, yuvcamera2 bokeh sensor, 2020.03.31 
        if(isDoubleSubCameraSupport && "0".equals(mCurrentCameraId)){
            try {
                subcam4_bright = readBrightState2();
            } catch (IOException e) {
                LogHelper.e(TAG, "hct_set_bright_as_polling_mode state = " + e.getMessage());
            }
            reg3 = subcam4_bright & 0xffff;
            reg4 = (subcam4_bright & 0xffff0000) >> 16;
            isNowSubcamera4Covered = BvUtilsCamera4.is_now_subcam2_covered(level, reg3, reg4);
        }
        //end, wangsenhao, yuvcamera2 bokeh sensor, 2020.03.31
        if (isNowSubcameraCovered || isNowSubcamera4Covered) {
            if(keep_time >= mTimes){
                if(level <bright_map.length) {
                    /*LogHelper.i(TAG, "current_level = " + level + ". last_level = " + last_level
                            + "main:" + maincam_bright + ". reg1 = " + reg1 + ". reg2 = "
                            + reg2+ ". prefered reg1 = " + bright_map[level][1]
                            + ".prefered reg2 = " + bright_map[level][2]);*/
                }
                mGuideHint.mHintText = mActivity.getString(R.string.hct_bokeh_tip);
                mICameraContext.getIApp().getAppUi().showScreenHint(mGuideHint);
                allow_boker = false;
            LogHelper.e(TAG, "hct_set_bright_as_polling_mode1 createAndChangeRepeatingRequest to  repeatingPreview true");
                createAndChangeRepeatingRequest();
            }else{
                keep_time++;
            }
        } else {
            if (!allow_boker) {
                mGuideHint.mHintText = "";
                mICameraContext.getIApp().getAppUi().hideScreenHint(mGuideHint);
                allow_boker = true;
            LogHelper.e(TAG, "hct_set_bright_as_polling_mode2 createAndChangeRepeatingRequest to  repeatingPreview true");
                createAndChangeRepeatingRequest();
                keep_time = 0;
            }
        }

        if (maincam_bright == 0 && reg1 == 0 && reg2 == 0) {
            invalid_count++;
        } else {
            invalid_count = 0;
        }
        mCameraHandler.removeMessages(HCT_READ_BRIGHT_CAMERA);
        if (invalid_count > 10) {
            invalid_count = 0;
        } else {
            mCameraHandler.sendEmptyMessageDelayed(HCT_READ_BRIGHT_CAMERA, 300);
        }

        last_level = level;
    }

    private int readBrightState() throws IOException {
        java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.FileReader(yuvcamera_state_file), 56);
        String str;
        int state = 0;
        try {
            str = reader.readLine();
            state = Integer.parseInt(str);
            // Log.e(TAG, "read yuvcamera state = " + state);
            return state;
        } finally {
            reader.close();
        }
    }
    //start, wangsenhao, yuvcamera2 bokeh sensor, 2020.03.31
    private int readBrightState2() throws IOException {
        java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.FileReader(yuvcamera_state_file2), 56);
        String str;
        int state = 0;
        try {
            str = reader.readLine();
            state = Integer.parseInt(str);
            return state;
        } finally {
            reader.close();
        }
    }
    //end, wangsenhao, yuvcamera2 bokeh sensor, 2020.03.31

    //add by huangfei for front bokeh start
    private int getDisplayOrientation() {
        //orientation, g-sensor, no used
        int displayRotation = CameraUtil.getDisplayRotation(mActivity);
        int displayOrientation = CameraUtil.getDisplayOrientationFromDeviceSpec(
                displayRotation, 1, mActivity);
        return displayOrientation;
    }

    private Face getFaces(android.hardware.camera2.params.Face[] faces,
                            Rect previewRect, Rect cropRegion) {
        if (faces == null || (faces != null && faces.length != 1)) {
            return null;
        }
        Face  faceTemp = new Face();
        faceTemp.id = faces[0].getId();
        faceTemp.score = faces[0].getScore();
        faceTemp.cropRegion = cropRegion;
        faceTemp.rect = previewRect;
        return faceTemp;
    }

    private Rect getPreviewRect(android.hardware.camera2.params.Face[] faces, Rect cropRegion) {
        if (faces == null || (faces != null && faces.length == 0)) {
            return null;
        }
        Rect rectTemp = null;
        rectTemp = CoordinatesTransform.sensorToNormalizedPreview(faces[0].getBounds(),
            mPreviewWidth,mPreviewHeight, cropRegion);
        return rectTemp;
    }

    private Rect getCaptureRect(android.hardware.camera2.params.Face[] faces, Rect cropRegion) {
        if (faces == null || (faces != null && faces.length == 0)) {
            return null;
        }
        Rect rectTemp = null;
        rectTemp = CoordinatesTransform.sensorToNormalizedPreview(faces[0].getBounds(),
            mPictureWidth,mPictureHeight, cropRegion);
        return rectTemp;
    }
    //add by huangfei for front bokeh end

    //bv wuyonglin add for aiworks facebeauty and bokeh 20200722 start
    private void startPreviewReaderThread() {
        if(mPreviewReaderThread == null){
            mPreviewReaderThread = new HandlerThread("CameraPreviewReader");
        }
        mPreviewReaderThread.start();
        if(mPreviewReaderHandler == null){
            mPreviewReaderHandler = new Handler(mPreviewReaderThread.getLooper());
        }
    }

    private ImageReader.OnImageAvailableListener mPreviewReaderListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            synchronized (sPreviewLock) {
	    try {
                Image image = reader.acquireNextImage();
                byte[] captureYuv = YuvEncodeJni.getInstance().getBuffer(image,false);
                int width = image.getWidth();
                int height = image.getHeight();
                    //LogHelper.i(TAG,"mPreviewReader onImageAvailable width = " + width+" height ="+height+" captureYuv.length ="+captureYuv.length);
                image.close();
                mModeDeviceCallback.onAiworksPreviewCallback(captureYuv, ImageFormat.NV21, mPreviewWidth, mPreviewHeight);
	    } catch (Exception e) {
                e.printStackTrace();
                LogHelper.e(TAG, "onImageAvailable1 error");
	    }
	    }
        }
    };
    //bv wuyonglin add for aiworks facebeauty and bokeh 20200722 end
}
