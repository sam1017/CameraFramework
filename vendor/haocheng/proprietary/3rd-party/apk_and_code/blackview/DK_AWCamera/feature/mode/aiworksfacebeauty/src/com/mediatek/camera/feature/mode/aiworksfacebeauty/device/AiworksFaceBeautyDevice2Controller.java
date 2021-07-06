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

package com.mediatek.camera.feature.mode.aiworksfacebeauty.device;

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
import com.mediatek.camera.feature.mode.aiworksfacebeauty.AiworksFaceBeautyDeviceInfo;
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

import javax.annotation.Nonnull;
import android.util.Range;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import com.mediatek.camera.ui.CHSeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import com.mediatek.camera.R;

import com.mediatek.camera.common.relation.Relation;
import com.mediatek.camera.feature.mode.aiworksfacebeauty.AiworksFaceBeautyRestriction;

import android.media.Image;
import android.media.ImageReader;
import android.graphics.ImageFormat;
import com.mediatek.camera.feature.mode.aiworksfacebeauty.util.ImageFormatUtil;
import com.mediatek.camera.feature.mode.aiworksfacebeauty.util.FaceOrientationUtil;
import com.mediatek.camera.feature.mode.aiworksfacebeauty.util.FaceModelUtil;
import android.os.Handler;
import android.os.HandlerThread;
import com.aiworks.yuvUtil.YuvEncodeJni;
import com.aiworks.facesdk.AwFaceDetectApi;
//bv wuyonglin add for open mfb 20201114 start
import com.aiworks.android.camera.VendorTagRequest;
//bv wuyonglin add for open mfb 20201114 end

/**
 * An implementation of {@link IDeviceController} with Camera2Proxy.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class AiworksFaceBeautyDevice2Controller extends Device2Controller implements
        IAiworksFaceBeautyDeviceController, ICompeletedCallback,
        CaptureSurface.ImageCallback,
        ISettingManager.SettingDevice2Requester {
    private static final Tag TAG = new Tag(AiworksFaceBeautyDevice2Controller.class.getSimpleName());
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

    private  int mBeautyEnable = 0;
    private  int mSmoothValue = -1;
    private int mWhilenValue;
    private int mThinValue;
    private int mBigEyeValue;
    private int mBrightEyeValue;
    private int mBigNoseValue;

    private int seekbarmax = 150;
    private DataStore mDataStore;
    private ViewGroup mRootViewGroup;
    private static final int[] BEAUTY_PARAMETER_VALUE = new int[]{0,0,0,0} ;
    private static final int[] BEAUTY_MODE_VALUE = new int[]{0,0} ;
    private int mPictureWidth = 0;
    private int mPictureHeight = 0;
    private int mDefaultBeautyValue = 75;

    //bv wuyonglin add for aiworks facebeauty and bokeh 20200722 start
    private HandlerThread mPreviewReaderThread = null;
    private Handler mPreviewReaderHandler = null;
    private ImageReader mPreviewReader;
    private Surface mPreviewReaderSurface;
    private static Object sPreviewLock = new Object();
    private HandlerThread mFaceDetectThread = null;
    private Handler mFaceDetectHandler = null;
    //bv wuyonglin add for aiworks facebeauty and bokeh 20200722 end

    //bv wuyonglin add for bug 3457 aiworks facebeauty take picture happend oom 20201226 start
    private byte mPreviewNv21[];
    private volatile boolean isFaceDetecting = false;
    //bv wuyonglin add for bug 3457 aiworks facebeauty take picture happend oom 20201226 end

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
    AiworksFaceBeautyDevice2Controller(@Nonnull Activity activity, @Nonnull ICameraContext context) {
        LogHelper.d(TAG, "[AiworksFaceBeautyDevice2Controller]");
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

        mDefaultBeautyValue = mActivity.getResources().getInteger(R.integer.beauty_seekbar_default);	
        startPreviewReaderThread();
    }

    @Override
    public void queryCameraDeviceManager() {
        mCameraDeviceManager = mICameraContext.getDeviceManager(CameraApi.API2);
    }

    @Override
    public void openCamera(AiworksFaceBeautyDeviceInfo info) {
        CameraSysTrace.onEventSystrace("AiworksFaceBeautyDevice.openCamera", true, true);
        String cameraId = info.getCameraId();
        boolean sync = info.getNeedOpenCameraSync();
        LogHelper.i(TAG, "[openCamera] cameraId : " + cameraId + ",sync = " + sync);
        if (canOpenCamera(cameraId)) {
            try {
                mDeviceLock.tryLock(WAIT_TIME, TimeUnit.SECONDS);
                mNeedSubSectionInitSetting = info.getNeedFastStartPreview();
                mCurrentCameraId = cameraId;
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
        CameraSysTrace.onEventSystrace("AiworksFaceBeautyDevice.openCamera", false, true);
    }

    @Override
    public void updatePreviewSurface(Object surfaceObject) {
        LogHelper.i(TAG, "CamAp_AiworksFaceBeautyMode FaceBeautyDrawer [updatePreviewSurface] surfaceHolder = " + surfaceObject + " state = "
                + mCameraState + ", session :" + mSession + ", mNeedSubSectionInitSetting:"
                + mNeedSubSectionInitSetting+" mIsPictureSizeChanged ="+mIsPictureSizeChanged);
        //bv wuyonglin add for aiworks facebeauty and bokeh 20200722 start
        if (surfaceObject != null && mIsPictureSizeChanged) {
            synchronized (sPreviewLock) {
            if (null != mPreviewReader) {
                mPreviewReader.setOnImageAvailableListener(null, null);
                mPreviewReader.close();
                mPreviewReader = null;
            }
            LogHelper.i(TAG,"updatePreviewSurface23456 onSurfaceTextureAvailable mPreviewReader5 newInstance mPreviewWidth = " + mPreviewWidth+" mPreviewHeight ="+mPreviewHeight+" mPreviewReader ="+mPreviewReader);
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
        LogHelper.i(TAG, "CamAp_AiworksFaceBeautyMode FaceBeautyDrawer [updatePreviewSurface] onSurfaceTextureAvailable to configureSession");
                    configureSession(false);
                }
            }
        }
        LogHelper.i(TAG, "CamAp_AiworksFaceBeautyMode FaceBeautyDrawer [updatePreviewSurface] onSurfaceTextureAvailable end");
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
LogHelper.e(TAG,"setPictureSize updatePictureInfo mJpegRotation: "+mJpegRotation+" rotation ="+rotation+" format ="+format+" formatTag ="+formatTag+" ImageFormat.YUV_420_888 ="+ImageFormat.YUV_420_888);
        HeifHelper.orientation = rotation;
        mIsPictureSizeChanged = mCaptureSurface.updatePictureInfo(size.getWidth(),
                size.getHeight(), format, CAPTURE_MAX_NUMBER);
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
        if (mCaptureSurface != null) {
            releaseJpegCaptureSurface();
        }
        if (mThumbnailSurface != null) {
            mThumbnailSurface.release();
        }
        LogHelper.i(TAG, "[destroyDeviceController]");
        mPreviewReaderHandler.getLooper().quitSafely();
        mFaceDetectHandler.getLooper().quitSafely();
        AwFaceDetectApi.destroy();
    }

    @Override
    public void startPreview() {
        LogHelper.i(TAG, "[startPreview]");
        configureSession(false);
    }

    @Override
    public void stopPreview() {
        LogHelper.i(TAG, "[stopPreview] mPreviewReader ="+mPreviewReader);
        //bv wuyonglin add for aiworks facebeauty and bokeh 20200722 start
        mFaceDetectHandler.removeCallbacksAndMessages(null);
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
    public void takePicture(@Nonnull IAiworksFaceBeautyDeviceController.CaptureDataCallback callback) {
        LogHelper.i(TAG, "[takePicture] mSession= " + mSession);
        CameraSysTrace.onEventSystrace("AiworksFaceBeautyDevice.takePicture", true, true);
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
        CameraSysTrace.onEventSystrace("AiworksFaceBeautyDevice.takePicture", false, true);
    }

    @Override
    public void updateGSensorOrientation(int orientation) {
        //modify by huangfei  for mJpegRotation abnormal;
                    LogHelper.e(TAG,"updateGSensorOrientation mJpegRotation: "+mJpegRotation+" orientation ="+orientation);
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
        LogHelper.i(TAG, "[closeCamera] + sync = " + sync + " current state : " + mCameraState);
        CameraSysTrace.onEventSystrace("AiworksFaceBeautyDevice.closeCamera", true, true);
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
        CameraSysTrace.onEventSystrace("AiworksFaceBeautyDevice.closeCamera", false, true);
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
            //configureSession(false);
        }
        return new Size(mPreviewWidth, mPreviewHeight);
    }

    @Override
    public void onPictureCallback(byte[] data,
                                  int format, String formatTag, int width, int height) {
        LogHelper.d(TAG, "<onPictureCallback> data = " + data + ", format = " + format
                + ", formatTag" + formatTag + ", width = " + width + ", height = " + height
                + ", mCaptureDataCallback = " + mCaptureDataCallback);
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
                CameraSysTrace.onEventSystrace("AiworksFaceBeautyDevice.onPostViewCallback", true, true);
                mCaptureDataCallback.onPostViewCallback(data);
                CameraSysTrace.onEventSystrace("AiworksFaceBeautyDevice.onPostViewCallback", false, true);
            } else {
                CameraSysTrace.onEventSystrace("AiworksFaceBeautyDevice.onJpegCallback", true, true);
                mCaptureDataCallback.onDataReceived(info);
                CameraSysTrace.onEventSystrace("AiworksFaceBeautyDevice.onJpegCallback", false, true);
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
        if (mCamera2Proxy == null || mCameraState != CameraState.CAMERA_OPENED) {
            LogHelper.e(TAG, "camera is closed or in opening state can't request ");
            return;
        }
        LogHelper.i(TAG, "[createAndChangeRepeatingRequest] to  repeatingPreview true");
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
        LogHelper.i(TAG, "[configureSession] +" + ", isFromOpen :" + isFromOpen);
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

        Relation relation = AiworksFaceBeautyRestriction.getRestriction().getRelation("on", true);
        mSettingManager.getSettingController().postRestriction(relation);


        mSettingController.addViewEntry();
        mSettingController.refreshViewEntry();
        CameraSysTrace.onEventSystrace("photoDevice.configSettingsByStage2", false);
    }

    private void abortOldSession() {
        if (mSession != null) {
            try {
            LogHelper.e(TAG, "[abortOldSession] come");
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
        CameraSysTrace.onEventSystrace("AiworksFaceBeautyDevice.repeatingPreview", true, true);
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
        LogHelper.i(TAG, "[repeatingPreview] end");
        CameraSysTrace.onEventSystrace("AiworksFaceBeautyDevice.repeatingPreview", false, true);
    }

    private Builder doCreateAndConfigRequest(int templateType) throws CameraAccessException {
        LogHelper.i(TAG, "[doCreateAndConfigRequest] mCamera2Proxy =" + mCamera2Proxy);
        int progress = getGYBeautyLevel();

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
                //configAeFpsRange(builder);
                //setBeautyParameter(builder);
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
LogHelper.e(TAG,"doCreateAndConfigRequest mJpegRotation: "+mJpegRotation+" rotation ="+rotation);
                HeifHelper.orientation = rotation;
                builder.set(CaptureRequest.JPEG_ORIENTATION, rotation);	
				//setBeautyParameter(builder);
                if (mICameraContext.getLocation() != null) {
                    if (!CameraUtil.is3rdPartyIntentWithoutLocationPermission(mActivity)) {
                        builder.set(CaptureRequest.JPEG_GPS_LOCATION,
                                mICameraContext.getLocation());
                    }
                }
            }
            //bv wuyonglin add for open mfb 20201114 start
            builder.set(VendorTagRequest.MTK_MFB_MODE, new int[]{255});
            //bv wuyonglin add for open mfb 20201114 end

        }
        return builder;
    }

    private Builder getDefaultPreviewBuilder() throws CameraAccessException {
        if (mCamera2Proxy != null && mDefaultBuilder == null) {
            mDefaultBuilder = mCamera2Proxy.createCaptureRequest(Camera2Proxy.TEMPLATE_PREVIEW);
            //bv wuyonglin add for open mfb 20201114 start
            mDefaultBuilder.set(VendorTagRequest.MTK_MFB_MODE, new int[]{255});
            //bv wuyonglin add for open mfb 20201114 end
            //setBeautyParameter(mDefaultBuilder);
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
                    if (mNeedSubSectionInitSetting) {
                        configureSession(true);
                    } else {
                        //bv wuyonglin add for switcher camera in top quickswitch setting icon not show with other icon 20200307 start
                        Relation relation = AiworksFaceBeautyRestriction.getRestriction().getRelation("on",true);
                        mSettingController.postRestriction(relation);
                        //bv wuyonglin end for switcher camera in top quickswitch setting icon not show with other icon 20200307 end
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
                            + ", mNeedFinalizeOutput:" + mNeedFinalizeOutput+" mPreviewSurface ="+mPreviewSurface);
                    mDeviceLock.lock();
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
                LogHelper.d(TAG, "onPostViewCallback [onCaptureStarted] 0 to onPreviewCallback");
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
                CameraSysTrace.onEventSystrace("AiworksFaceBeautyDevice.onP2Done", true, true);
                LogHelper.d(TAG, "[onCaptureProgressed] P2done comes, frame: " + num);
                updateCameraState(CameraState.CAMERA_OPENED);
                LogHelper.d(TAG, "onPostViewCallback [onCaptureProgressed] 0 to onPreviewCallback");
                mModeDeviceCallback.onPreviewCallback(null, 0);
                CameraSysTrace.onEventSystrace("AiworksFaceBeautyDevice.onP2Done", false, true);
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
            if (CameraUtil.isStillCaptureTemplate(result)) {
                long num = result.getFrameNumber();
                if (mCaptureFrameMap.containsKey(String.valueOf(num))
                        && Boolean.FALSE == mCaptureFrameMap.get(String.valueOf(num))) {
                    mFirstFrameArrived = true;
                    updateCameraState(CameraState.CAMERA_OPENED);
                LogHelper.d(TAG, "onPostViewCallback [onCaptureCompleted] 1 to onPreviewCallback");
                    mModeDeviceCallback.onPreviewCallback(null, 0);
                }
                mCaptureFrameMap.remove(String.valueOf(num));
            } else if (!CameraUtil.isStillCaptureTemplate(result) && !mFirstFrameArrived) {
                CameraSysTrace.onEventSystrace("AiworksFaceBeautyDevice.onFirstFrameArrived", true, true);
                mFirstFrameArrived = true;
                updateCameraState(CameraState.CAMERA_OPENED);
                LogHelper.d(TAG, "onPostViewCallback [onCaptureCompleted] 2 to onPreviewCallback");
                mModeDeviceCallback.onPreviewCallback(null, 0);
                mICameraContext.getSoundPlayback().init();
                CameraSysTrace.onEventSystrace("AiworksFaceBeautyDevice.onFirstFrameArrived", false, true);
            }
            mSettingDevice2Configurator.getRepeatingCaptureCallback().onCaptureCompleted(
                    session, request, result);
        }

        @Override
        public void onCaptureFailed(@Nonnull CameraCaptureSession session,
                @Nonnull CaptureRequest request, @Nonnull CaptureFailure failure) {
            super.onCaptureFailed(session, request, failure);
            LogHelper.e(TAG, "onPostViewCallback [onCaptureFailed], framenumber: " + failure.getFrameNumber()
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
        LogHelper.i(TAG, "[setCaptureFormat] updatePictureInfo format = " + format);
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
    public void setBeautyParameter(int smooth,int whilen,int thin,int bigeye,int brighteye, int bignose) {
        //mDataStore.setValue("pi_facebeauty_progress",progress+"",mDataStore.getGlobalScope(),false);
        mSmoothValue = smooth;
        mWhilenValue = whilen;
        mThinValue = thin;
        mBigEyeValue = bigeye;
        mBrightEyeValue = brighteye;
        mBigNoseValue = bignose;
        //createAndChangeRepeatingRequest();
    }

    public int getGYBeautyLevel(){
        int progress = Integer.parseInt(mDataStore.getValue("pi_facebeauty_progress",mDefaultBeautyValue+"",mDataStore.getGlobalScope()));
        return progress;
    }

    private void setBeautyParameter(CaptureRequest.Builder builder){
        /*CaptureRequest.Key<int[]> mBeautymode =  new CaptureRequest.Key<int[]>("com.mediatek.control.capture.xapifeaturemode", int[].class);
        CaptureRequest.Key<int[]> mBeautyLevel =  new CaptureRequest.Key<int[]>("com.mediatek.control.capture.xapibeauty", int[].class);
        BEAUTY_MODE_VALUE[0] = 32;
        BEAUTY_MODE_VALUE[1] = 64;
        BEAUTY_PARAMETER_VALUE[0] = mSmoothValue;
        BEAUTY_PARAMETER_VALUE[1] = mLightenValue;
        BEAUTY_PARAMETER_VALUE[2] = mRuddyValue;
        BEAUTY_PARAMETER_VALUE[3] = mSlenderValue;
        builder.set(mBeautymode, BEAUTY_MODE_VALUE);
        builder.set(mBeautyLevel, BEAUTY_PARAMETER_VALUE);*/
    }

    private void initSettings() throws CameraAccessException {
        LogHelper.i(TAG, "[openCamera] cameraId : " + "initSettings");
        Relation relation = AiworksFaceBeautyRestriction.getRestriction().getRelation("on",true);
        mSettingController.postRestriction(relation);
        mSettingController.addViewEntry();
        mSettingController.refreshViewEntry();
    }

    private void configAeFpsRange(CaptureRequest.Builder requestBuilder) {
        LogHelper.d(TAG, "[configAeFpsRange] + ");
        double ratio = (double) mPictureWidth / mPictureHeight;
        String aspect = String.valueOf(ratio);
        if(aspect.startsWith("1.0")||aspect.startsWith("1.3")){
            LogHelper.i(TAG, "[disable configAeFpsRange]  ");
            return;
        }
        try {
            Range<Integer>[] a = mCameraCharacteristics.get(
                    CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);

            Range aeFps = new Range(5,24);
            requestBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, aeFps);
            LogHelper.i(TAG, "[configAeFpsRange] - " + aeFps.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //bv wuyonglin add for aiworks facebeauty and bokeh 20200722 start
    private void startPreviewReaderThread() {
        if(mPreviewReaderThread == null){
            mPreviewReaderThread = new HandlerThread("CameraPreviewReader");
        }
        mPreviewReaderThread.start();
        if(mPreviewReaderHandler == null){
            mPreviewReaderHandler = new Handler(mPreviewReaderThread.getLooper());
        }
        if(mFaceDetectThread == null){
            mFaceDetectThread = new HandlerThread("CameraFaceDetect");
        }
        mFaceDetectThread.start();
        if(mFaceDetectHandler == null){
            mFaceDetectHandler = new Handler(mFaceDetectThread.getLooper());
        }
    }

    private ImageReader.OnImageAvailableListener mPreviewReaderListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            synchronized (sPreviewLock) {
	    try {
                Image image = reader.acquireNextImage();
                //LogHelper.i(TAG,"onImageAvailable image = "+image+" reader ="+reader);
                //bv wuyonglin add for bug 3457 aiworks facebeauty take picture happend oom 20201226 start
                if (isFaceDetecting) {
                    image.close();
                    return;
                }
                isFaceDetecting = true;
                byte[] captureYuv = YuvEncodeJni.getInstance().getBuffer(image,false);
                //int width = image.getWidth();
                //int height = image.getHeight();
                //LogHelper.i(TAG,"onAiworksPreviewCallback mPreviewReader onImageAvailable width = " + width+" height ="+height+" captureYuv.length ="+captureYuv.length);
                if(mPreviewNv21 == null || mPreviewNv21.length != captureYuv.length) {
                    mPreviewNv21 = new byte[captureYuv.length];
                }
                System.arraycopy(captureYuv, 0, mPreviewNv21, 0, captureYuv.length);
                //bv wuyonglin add for bug 3457 aiworks facebeauty take picture happend oom 20201226 end
                image.close();
                mFaceDetectHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        //bv wuyonglin modify for bug 3457 aiworks facebeauty take picture happend oom 20201226 start
                        mFaceDetectHandler.removeCallbacksAndMessages(null);
                        mModeDeviceCallback.onAiworksPreviewCallback(mPreviewNv21, ImageFormat.NV21, mPreviewWidth, mPreviewHeight);
                        isFaceDetecting = false;
                        //bv wuyonglin modify for bug 3457 aiworks facebeauty take picture happend oom 20201226 end
                    }
                });
	    } catch (Exception e) {
                e.printStackTrace();
                LogHelper.e(TAG, "onImageAvailable1 error");
	    }
           }
        }
    };
    //bv wuyonglin add for aiworks facebeauty and bokeh 20200722 end
}
