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

package com.mediatek.camera.feature.mode.night.device;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.SurfaceTexture;
import android.graphics.Rect;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCaptureSession.CaptureCallback;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureRequest.Builder;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.OutputConfiguration;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.location.Location;
import android.media.Image;
import android.os.Build;
import android.os.Process;
import android.provider.MediaStore;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.graphics.ImageFormat;
import java.io.File;
import android.os.Environment;
import android.media.ImageReader;

import java.io.IOException;
import java.util.Arrays;
import android.graphics.Rect;

import com.aiworks.android.lowlight.NightCaptureRequest;
import com.aiworks.android.lowlight.NightCaptureResult;
import com.mediatek.camera.WaterMarkUtil;
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
import com.mediatek.camera.common.relation.DataStore;
import com.mediatek.camera.feature.mode.night.NightDeviceInfo;
import com.mediatek.camera.feature.setting.zoom.IZoomConfig;
import android.os.Message;

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

import android.view.OrientationEventListener;
import com.aiworks.android.lowlight.NightCaptureCallback;
import com.aiworks.android.lowlight.NightCaptureRequestInterface;
import com.aiworks.android.lowlight.NightCaptureResultInterface;
import com.aiworks.android.lowlight.NightShotConfig;
import com.aiworks.android.utils.Util;
import com.aiworks.android.utils.ImageFormatUtil;
import com.aiworks.yuvUtil.YuvEncodeJni;
import com.android.camera.exif.ExifInterface;
import com.aiworks.android.camera.CameraPreferences;

import android.hardware.camera2.CameraMetadata;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import android.widget.Toast;
import java.util.concurrent.Semaphore;
import javax.annotation.Nonnull;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.HashMap;
//add by huangfei for Restrition no works when switch camera start
import com.mediatek.camera.common.relation.Relation;
import com.mediatek.camera.feature.mode.night.NightRestriction;
//add by huangfei for Restrition no works when switch camera end

import static android.hardware.camera2.CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES;
import static android.hardware.camera2.CameraMetadata.CONTROL_AF_MODE_AUTO;
import static android.hardware.camera2.CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_PICTURE;
import static android.hardware.camera2.CaptureRequest.SCALER_CROP_REGION;
import static com.aiworks.android.utils.SystemProperties.get;

/**
 * An implementation of {@link IDeviceController} with Camera2Proxy.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class NightDevice2Controller extends Device2Controller implements
        INightDeviceController, ICompeletedCallback,
        CaptureSurface.ImageCallback,
        NightCaptureCallback,
        ISettingManager.SettingDevice2Requester {
    //private static final Tag TAG = new Tag(NightDevice2Controller.class.getSimpleName());
    private static final String TAG = "NightDevice2Controller";
    private static final String KEY_PICTURE_SIZE = "key_picture_size";
    private static final int CAPTURE_MAX_NUMBER = 5;
    private static final int WAIT_TIME = 5;
    private static final int NIGHT_CAPTURERESULT_INIT = 0;
    private static final int NIGHT_CAPTURERESULT_SET_PHOTOSIZE = 1;

    // customized_camera_preferences文件所在路径
    private static final String CAMERA_OUTSIDE_PREFERENCES_PATH = Environment.getExternalStorageDirectory().getPath() + "/night/";
    private static final String CAMERA_OUTSIDE_PREFERENCES_FILE_NAME = "customized_night_camera_preferences.xml";
    private static final String KEY_CAMERA_NIGHT_CONFIG = "pref_night_mode_config_key";


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

    private CameraDevice mCameraDevice;
    private int mJpegRotation;
    private ExifInterface mExif;
    private boolean mStart = false;
    //private boolean isWriteThumb = false;
    private Uri mUri;
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);
    private Thread mThread;
    private boolean mRun = true;
    private ImageReader mImageReader;
    private ImageReader mPreviewReader;

    private HandlerThread mCaptureThread;
    private Handler mCaptureHandler;
    private HandlerThread mPreviewReaderThread;
    private Handler mPreviewReaderHandler;
    private boolean mUsePreviewReader = true;
    public int mOrientation = -1;
    private MyOrientationEventListener mOrientationListener;

    private volatile int mPreviewWidth;
    private volatile int mPreviewHeight;
    private volatile Camera2Proxy mCamera2Proxy;
    private volatile Camera2CaptureSessionProxy mSession;
    private volatile CameraCaptureSession mCaptureSession;

    private boolean mFirstFrameArrived = false;
    private boolean mIsPictureSizeChanged = false;
    private boolean mNeedSubSectionInitSetting = false;
    private volatile boolean mNeedFinalizeOutput = false;
    private boolean isInit = false;

    private Lock mLockState = new ReentrantLock();
    private Lock mDeviceLock = new ReentrantLock();
    private CameraState mCameraState = CameraState.CAMERA_UNKNOWN;

    private NightCaptureRequestInterface mNightCaptureRequest;
    private NightCaptureResultInterface mNightCaptureResult;

    private int mNightInitCameraID;

    private boolean mCapturePreview = false;

    private android.util.Size mPhotoSize;
    private android.util.Size mPreviewSize;

    private String mCurrentCameraId;
    private Surface mPreviewReaderSurface;
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
    private CaptureRequest.Builder mPreviewRequestBuilder = null;
    private CaptureRequest.Builder mDefaultBuilder = null;
    private String mZsdStatus = "on";
    private List<OutputConfiguration> mOutputConfigs;
    private CameraCharacteristics mCameraCharacteristics;
    private boolean mIsBGServiceEnabled = false;
    private BGServiceKeeper mBGServiceKeeper;
    private ConcurrentHashMap mCaptureFrameMap = new ConcurrentHashMap<String, Boolean>();
    private IDeviceListener mIDeviceListener;
    private CaptureImageSavedCallback mCaptureImageSavedCallback;
    private DataStore mDataStore;

    private IZoomConfig mZoomConfig = null;
    private Handler mConfigNightSettingsHandler = new ConfigNightSettingsHandler();
    //bv wuyonglin add for bug5276 20210415 start
    private Rect mSensorRect;
    //bv wuyonglin add for bug5276 20210415 end

    private String PREFERENCES_FILE_NAME = null;
    private String WIDE_PREFERENCES_FILE_NAME = null;
    private String NIGHT_DUMP_PATH = null;


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
    NightDevice2Controller(@Nonnull Activity activity, @Nonnull ICameraContext context) {
        Log.d(TAG, "[NightDevice2Controller]");
        mActivity = activity;
        mCameraManager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        mICameraContext = context;
        mDataStore = context.getDataStore();
        mBGServiceKeeper = mICameraContext.getBGServiceKeeper();
        if (mBGServiceKeeper != null) {
            DeviceDescription deviceDescription = CameraApiHelper.getDeviceSpec(
                    mActivity.getApplicationContext()).getDeviceDescriptionMap().get("0");
            if (deviceDescription != null && !isThirdPartyIntent(mActivity)
                    && mBGServiceKeeper.getBGHidleService() != null) {
                mIsBGServiceEnabled = true;
                mBGServicePrereleaseKey = deviceDescription.getKeyBGServicePrerelease();
                mBGServiceImagereaderIdKey = deviceDescription.getKeyBGServiceImagereaderId();
            }
        }
        Log.d(TAG, "mBGServiceKeeper = " + mBGServiceKeeper
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
            Log.d(TAG, "BG mCaptureSurface = " + mCaptureSurface);
        } else {
            mCaptureSurface = new CaptureSurface();
        }
        mCaptureSurface.setCaptureCallback(this);
        mThumbnailSurface = new CaptureSurface();
        mThumbnailSurface.setCaptureCallback(this);
        mThumbnailSurface.setFormat(ThumbnailHelper.FORMAT_TAG);
        mCameraDeviceManager = mICameraContext.getDeviceManager(CameraApi.API2);

        NIGHT_DUMP_PATH = mActivity.getApplicationContext().getExternalFilesDir("night").getPath() + "/";
        PREFERENCES_FILE_NAME = "aiworks_night_config.xml";
        WIDE_PREFERENCES_FILE_NAME = "aiworks_night_config_wide.xml";

        NightShotConfig.setDumpFilePath(NIGHT_DUMP_PATH);
        String model = get("ro.product.model");
        Log.i(TAG,"model = " + model);
        String path = model + "/" + PREFERENCES_FILE_NAME;
        Log.i(TAG,"path = " + path);

        //mCameraId = CameraPreferences.cameraId;
        //mUsePreviewReader = CameraPreferences.usePreviewReader;
        Log.i(TAG,"mUsePreviewReader = " + mUsePreviewReader);
        File xmlFile = new File(NIGHT_DUMP_PATH + PREFERENCES_FILE_NAME);
        if (!xmlFile.exists()) {
            try {
                AssetManager assetManager = mActivity.getApplicationContext().getAssets();
                new File(NIGHT_DUMP_PATH).mkdirs();
                if(model != null){
                    Util.copyFile(assetManager.open(path), xmlFile);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        path = model + "/" + WIDE_PREFERENCES_FILE_NAME;
        xmlFile = new File(NIGHT_DUMP_PATH + WIDE_PREFERENCES_FILE_NAME);
        if (!xmlFile.exists()) {
            try {
                AssetManager assetManager = mActivity.getApplicationContext().getAssets();
                new File(NIGHT_DUMP_PATH).mkdirs();
                if(model != null){
                    Util.copyFile(assetManager.open(path), xmlFile);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        CameraPreferences.parseCameraPreferencesXml(mActivity.getApplicationContext(), NIGHT_DUMP_PATH, PREFERENCES_FILE_NAME);

        mNightInitCameraID = CameraPreferences.cameraId;
        mNightCaptureRequest = new NightCaptureRequest();
        mNightCaptureResult = new NightCaptureResult();
        if(mNightInitCameraID != 2){
            Log.i(TAG,"mNightCaptureRequest.init PREFERENCES_FILE_NAME");
            mNightCaptureRequest.init(mActivity.getApplicationContext(), NIGHT_DUMP_PATH + PREFERENCES_FILE_NAME, mNightInitCameraID);
        }else{
            Log.i(TAG,"mNightCaptureRequest.init WIDE_PREFERENCES_FILE_NAME");
            mNightCaptureRequest.init(mActivity.getApplicationContext(), NIGHT_DUMP_PATH + WIDE_PREFERENCES_FILE_NAME, mNightInitCameraID);
        }
        mNightCaptureResult.setCaptureCallback(this);


        //String nightConfigJsonStr = null;

/*
        Log.d(TAG, "loadCustomizedCameraPreferences");
        HashMap<String, String> customPreferences = Util.loadCustomizedCameraPreferences(
                mActivity, CAMERA_OUTSIDE_PREFERENCES_PATH, CAMERA_OUTSIDE_PREFERENCES_FILE_NAME);
        if (customPreferences != null) {
            nightConfigJsonStr = customPreferences.get(KEY_CAMERA_NIGHT_CONFIG);
            Log.d(TAG,"nightConfigJsonStr = " + nightConfigJsonStr);
        }
        boolean enable = NightShotConfig.nightConfigData(nightConfigJsonStr);
        NightShotConfig.setBinFilePath(CAMERA_OUTSIDE_PREFERENCES_PATH);
        Log.d(TAG,"enable = " + enable + " setBinFilePath = " + CAMERA_OUTSIDE_PREFERENCES_PATH);
        if (!enable) {
            Toast.makeText(mActivity, "customized_camera_preferences is not correct", Toast.LENGTH_LONG).show();
        }*/
        //Log.d(TAG, "nightConfigData enable = " + enable);
    }

    public void initNightCapture(String cameraId) {

        Log.d(TAG, "initNightCapture cameraId = " + cameraId);

/*         if (NightShotConfig.NIGHT_MODE == NightShotConfig.NightMode.MFNR_SOFTWARE) {
            if (mNightCaptureRequest == null) {
                mNightCaptureRequest = new MTKMutilFrameCaptureRequest();
            }
            if (mNightCaptureResult == null) {
                mNightCaptureResult = new MTKMutilFrameCaptureResult();
            }
            Log.d(TAG,"nightConfigData new mNightCaptureResult mNightCaptureRequest");
        } else if (NightShotConfig.NIGHT_MODE == NightShotConfig.NightMode.HDRPLUS) {
            //mNightCaptureRequest = new HDRPlusCaptureRequest();
            //mNightCaptureResult = new HDRPlusCaptureResult();
        }*/
        if (mOrientationListener == null) {
            mOrientationListener = new MyOrientationEventListener(mActivity.getApplicationContext());
        }
        mOrientationListener.setEnable(true);
        if(Integer.valueOf(cameraId) != mNightInitCameraID){
            if((mNightInitCameraID == 2)||(cameraId.equals("2"))){
                mNightInitCameraID = Integer.parseInt(cameraId);
                if(mNightCaptureRequest != null){
                    mNightCaptureRequest.destory();
                    mNightCaptureRequest = new NightCaptureRequest();
                    if(mNightInitCameraID != 2){
                        Log.i(TAG,"initNightCapture mNightCaptureRequest.init PREFERENCES_FILE_NAME");
                        mNightCaptureRequest.init(mActivity.getApplicationContext(), NIGHT_DUMP_PATH + PREFERENCES_FILE_NAME, mNightInitCameraID);
                    }else{
                        Log.i(TAG,"initNightCapture mNightCaptureRequest.init WIDE_PREFERENCES_FILE_NAME");
                        mNightCaptureRequest.init(mActivity.getApplicationContext(), NIGHT_DUMP_PATH + WIDE_PREFERENCES_FILE_NAME, mNightInitCameraID);
                    }
                }
            }
        }

/*
        if (mTextureView.isAvailable()) {
            openCamera(mTextureView.getWidth(), mTextureView.getHeight());
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
*/
    }

    @Override
    public void queryCameraDeviceManager() {
        mCameraDeviceManager = mICameraContext.getDeviceManager(CameraApi.API2);
    }

    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@Nonnull CameraDevice cameraDevice) {

            mCameraOpenCloseLock.release();
            mCameraDevice = cameraDevice;
            Log.d(TAG, "Camera onOpened mCameraDevice = " + mCameraDevice);
            //createCameraPreviewSession();
            mThread = new Thread(new PreviewRunnable());
            mThread.start();
            mRun = true;
        }

        @Override
        public void onDisconnected(@Nonnull CameraDevice cameraDevice) {
            Log.d(TAG, "Camera onDisconnected");
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mThread.stop();
            mCameraDevice = null;
        }

        @Override
        public void onError(@Nonnull CameraDevice cameraDevice, int error) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
            mActivity.finish();
        }
    };

    @Override
    public void openCamera(NightDeviceInfo info) {
        CameraSysTrace.onEventSystrace("NightDevice.openCamera", true, true);
        String cameraId = info.getCameraId();
        boolean sync = true; //info.getNeedOpenCameraSync();
        Log.i(TAG, "[openCamera] cameraId : " + cameraId + ",sync = " + sync);
        //Log.d(TAG, Log.getStackTraceString(new Throwable()));

        if (canOpenCamera(cameraId)) {
            try {
                mDeviceLock.tryLock(WAIT_TIME, TimeUnit.SECONDS);
                mNeedSubSectionInitSetting = false; //info.getNeedFastStartPreview();
                Log.d(TAG,"openCamera mNeedSubSectionInitSetting = " + mNeedSubSectionInitSetting);
                mCurrentCameraId = cameraId;
                updateCameraState(CameraState.CAMERA_OPENING);
                initSettingManager(info.getSettingManager());
                CameraSysTrace.onEventSystrace("openCamera.doOpenCamera", true, true);
                doOpenCamera(sync);
                //mCameraManager.openCamera(cameraId + "", mStateCallback, mModeHandler);
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
                //bv wuyonglin add for bug5276 20210415 start
                mSensorRect = mCameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
                //bv wuyonglin add for bug5276 20210415 end
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
            if(mCurrentCameraId != null){
                initNightCapture(mCurrentCameraId);
            }
        }
        CameraSysTrace.onEventSystrace("NightDevice.openCamera", false, true);
    }

    @Override
    public void updatePreviewSurface(Object surfaceObject) {
        Log.d(TAG, "[updatePreviewSurface] surfaceHolder = " + surfaceObject + " state = "
                + mCameraState + ", session :" + mSession + ", mNeedSubSectionInitSetting:"
                + mNeedSubSectionInitSetting);
        //Log.d(TAG, Log.getStackTraceString(new Throwable()));
        Log.d(TAG, "updatePreviewSurface");
        synchronized (mSurfaceHolderSync) {
            if (surfaceObject instanceof SurfaceHolder) {
                mPreviewSurface = surfaceObject == null ? null :
                        ((SurfaceHolder) surfaceObject).getSurface();
            } else if (surfaceObject instanceof SurfaceTexture) {
                ((SurfaceTexture) surfaceObject).setDefaultBufferSize(mPreviewWidth,mPreviewHeight);
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
        Log.d(TAG,"setPictureSize mIsPictureSizeChanged = " + mIsPictureSizeChanged + " width = " + size.getWidth() + " height = " + size.getHeight());
        if(mIsPictureSizeChanged){
            mPhotoSize = new android.util.Size(size.getWidth(), size.getHeight());
            if(mConfigNightSettingsHandler != null){
                mConfigNightSettingsHandler.sendEmptyMessage(NIGHT_CAPTURERESULT_SET_PHOTOSIZE);
            }
        }
        if (mIsBGServiceEnabled) {
            mBGServiceKeeper.setBGCaptureSurface(mCaptureSurface);
        }
        double ratio = (double) size.getWidth() / size.getHeight();
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
        Log.i(TAG, "[isReadyForCapture] canCapture = " + canCapture);
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
        if(mNightCaptureRequest != null){
            mNightCaptureRequest.destory();
        }
        if (mOrientationListener != null) {
            mOrientationListener.setEnable(false);
            mOrientationListener = null;
	}
        Log.d(TAG,"destroyDeviceController mNightCaptureResult.destory");
        mStart = false;
        if(mNightCaptureResult != null){
            /*add by bv liangchangwei for night shot init 20200902 start  */
            //mNightCaptureResult.setCaptureCallback(null);
            if(isInit){
                mNightCaptureResult.destory();
                isInit = false;
            }
            /*add by bv liangchangwei for night shot init 20200902 end  */
        }

        mNightCaptureRequest = null;
        mNightCaptureResult = null;

    }

    @Override
    public void startPreview() {
        Log.i(TAG, "[startPreview]");
        configureSession(false);
    }

    @Override
    public void stopPreview() {
        Log.i(TAG, "[stopPreview]");
        abortOldSession();
    }

    @Override
    public void takePicture(@Nonnull INightDeviceController.CaptureDataCallback callback) {
        Log.i(TAG, "[takePicture] mSession= " + mSession + " mStart = " + mStart);
        CameraSysTrace.onEventSystrace("NightDevice.takePicture", true, true);
        //Log.d(TAG, Log.getStackTraceString(new Throwable()));
        if (mStart) {
            return;
        }
        mStart = true ;
        mExif = null;

        if (mSession != null && mCamera2Proxy != null) {
            try{
                mSession.stopRepeating();
            }catch (CameraAccessException e){
                Log.e(TAG,"mSession.stopRepeating() Error!!");
            }

            mCaptureDataCallback = callback;
            updateCameraState(CameraState.CAMERA_CAPTURING);
            try {
/*
                Builder builder = doCreateAndConfigRequest(Camera2Proxy.TEMPLATE_STILL_CAPTURE);
                builder.set(CaptureRequest.CONTROL_EFFECT_MODE, CameraMetadata.CONTROL_EFFECT_MODE_MONO);
                mSession.capture(builder.build(), mCaptureCallback, mModeHandler);
                if (mIDeviceListener != null) {
                    mIDeviceListener.onTakePicture();
                }
*/
                Log.d(TAG, "captureStillPicture mNightCaptureRequest.createCaptureRequest");
                ArrayList<Builder> builders = mNightCaptureRequest.createCaptureRequest(mCameraDevice);

                ArrayList<CaptureRequest> requests = new ArrayList<>();
                for (CaptureRequest.Builder builder : builders) {
                    builder.addTarget(mImageReader.getSurface());
                    CaptureRequest request = builder.build();
                    requests.add(request);
                }
                Log.d(TAG,"captureStillPicture mNightCaptureResult.getImageAvailableListener() mCaptureHandler = " + mCaptureHandler);

                mImageReader.setOnImageAvailableListener(mNightCaptureResult.getImageAvailableListener(), mCaptureHandler);

                Log.d(TAG,"captureStillPicture mNightCaptureResult onStartCapture!");

                Log.d(TAG, "captureStillPicture mNightCaptureRequest.getPhotoForamt mJpegRotation = " + mJpegRotation + " requests.size() = " + requests.size() + " mModeHandler = " + mModeHandler);
                mNightCaptureResult.onStartCapture(requests.size(), mPreviewReader.getImageFormat(), mJpegRotation);

                if (requests.size() == 1) {
                    mSession.capture(requests.get(0), mNightCaptureResult.getCaptureCallback(), mModeHandler);
                } else {
                    mSession.captureBurst(requests, mNightCaptureResult.getCaptureCallback(), mModeHandler);
                }
                if (mIDeviceListener != null) {
                    Log.i(TAG,"mIDeviceListener.onTakePicture()");
                    mIDeviceListener.onTakePicture();
                }
            } catch (CameraAccessException e) {
                e.printStackTrace();
                Log.e(TAG, "[takePicture] error because create build fail.");
            }
        }
        CameraSysTrace.onEventSystrace("NightDevice.takePicture", false, true);
    }

    @Override
    public void updateGSensorOrientation(int orientation) {
        //modify by huangfei  for mJpegRotation abnormal;
        Log.i(TAG,"updateGSensorOrientation orientation = " + orientation);
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
            } catch (CameraAccessException e) {
                Log.e(TAG, "[closeSession] exception", e);
            }
        }
        mSession = null;
        mBuilder = null;
        mPreviewRequestBuilder = null;
        mDefaultBuilder = null;
    }

    @Override
    public void closeCamera(boolean sync) {
        Log.i(TAG, "[closeCamera] + sync = " + sync + " current state : " + mCameraState);
        CameraSysTrace.onEventSystrace("NightDevice.closeCamera", true, true);
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
        mZoomConfig = null;
        mStart = false;
        CameraSysTrace.onEventSystrace("NightDevice.closeCamera", false, true);
        Log.i(TAG, "[closeCamera] -");
    }

    @Override
    public Size getPreviewSize(double targetRatio) {
        int oldPreviewWidth = mPreviewWidth;
        int oldPreviewHeight = mPreviewHeight;
        getTargetPreviewSize(targetRatio);
        boolean isSameSize = oldPreviewHeight == mPreviewHeight && oldPreviewWidth == mPreviewWidth;
        Log.i(TAG, "[getPreviewSize] old size : " + oldPreviewWidth + " X " +
                oldPreviewHeight + " new  size :" + mPreviewWidth + " X " + mPreviewHeight);
        //if preview size don't change, but picture size changed,need do configure the surface.
        //if preview size changed,do't care the picture size changed,because surface will be
        //changed.
        if (isSameSize && mIsPictureSizeChanged) {
            configureSession(false);
        }
        if(mIsPictureSizeChanged){
            mConfigNightSettingsHandler.sendEmptyMessage(NIGHT_CAPTURERESULT_SET_PHOTOSIZE);
        }
        return new Size(mPreviewWidth, mPreviewHeight);
    }

    @Override
    public void onPictureCallback(byte[] data,
                                  int format, String formatTag, int width, int height) {
        Log.d(TAG, "<onPictureCallback> data = " + data + ", format = " + format
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
                CameraSysTrace.onEventSystrace("NightDevice.onPostViewCallback", true, true);
                mCaptureDataCallback.onPostViewCallback(data);
                CameraSysTrace.onEventSystrace("NightDevice.onPostViewCallback", false, true);
            } else {
                CameraSysTrace.onEventSystrace("NightDevice.onJpegCallback", true, true);
                mCaptureDataCallback.onDataReceived(info);
                CameraSysTrace.onEventSystrace("NightDevice.onJpegCallback", false, true);
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
            Log.e(TAG, "camera is closed or in opening state can't request ");
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
        Log.d(TAG, "doOpenCamera");
        if (sync) {
            mCameraDeviceManager.openCameraSync(mCurrentCameraId, mDeviceCallback, null);
        } else {
            mCameraDeviceManager.openCamera(mCurrentCameraId, mDeviceCallback, null);
        }
        startBackgroundThread();
    }

    private void updateCameraState(CameraState state) {
        Log.d(TAG, "[updateCameraState] new state = " + state + " old =" + mCameraState);
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

    public void stop(){
        //do nothing
    }

    private void doCloseCamera(boolean sync) {
        if (sync) {
            mCameraDeviceManager.closeSync(mCurrentCameraId);
        } else {
            mCameraDeviceManager.close(mCurrentCameraId);
        }
        mCaptureFrameMap.clear();
        mCamera2Proxy = null;
        if (mOrientationListener != null) {
            mOrientationListener.setEnable(false);
            mOrientationListener = null;
	}
        synchronized (mSurfaceHolderSync) {
            mSurfaceObject = null;
            mPreviewSurface = null;
        }

        Log.e(TAG, "doCloseCamera");
        try {
            mPreviewRequestBuilder = null;
            mCameraOpenCloseLock.acquire();
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            if (null != mImageReader) {
                mImageReader.setOnImageAvailableListener(null, null);
                mImageReader.close();
                mImageReader = null;
            }
            if (null != mPreviewReader) {
                mPreviewReader.setOnImageAvailableListener(null, null);
                mPreviewReader.close();
                mPreviewReader = null;
            }
        } catch (Exception e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            mCameraOpenCloseLock.release();
        }
        stopBackgroundThread();
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
        Log.i(TAG, "[canOpenCamera] new id: " + newCameraId + " current camera :" +
                mCurrentCameraId + " isSameCamera = " + isSameCamera + " current state : " +
                mCameraState + " isStateReady = " + isStateReady + " can open : " + value);
        return value;
    }

    private void configureSession(boolean isFromOpen) {
        Log.i(TAG, "[configureSession] +" + ", isFromOpen :" + isFromOpen);
        //Log.d(TAG, Log.getStackTraceString(new Throwable()));
        mDeviceLock.lock();
        mFirstFrameArrived = false;
        try {
            if (mCamera2Proxy != null) {
                abortOldSession();
                mCaptureSurface.releaseCaptureSurfaceLater(false);
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
                    mBuilder = getDefaultPreviewBuilder();
                    Log.i(TAG,"[configureSession] isFromOpen " + ", mPreviewRequestBuilder :" + mPreviewRequestBuilder + " mCameraDevice = " + mCameraDevice);
                    if(mPreviewRequestBuilder == null){
                        mPreviewRequestBuilder = CameraPreferences.createPreviewRequest(mCameraDevice);
                    }
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
                //surfaces.add(mPreviewSurface);
                //surfaces.add(mCaptureSurface.getSurface());
                //if (ThumbnailHelper.isPostViewSupported()) {
                //    surfaces.add(mThumbnailSurface.getSurface());
                //}
                //bv wuyonglin add for bug2615 20201029 start
                Log.i(TAG,"mNightCaptureRequest.createPreviewRequest mCameraDevice = " + mCameraDevice+" mPreviewReader ="+mPreviewReader);
                if(mCameraDevice != null && mNightCaptureRequest != null && mPreviewReader != null){
                //bv wuyonglin add for bug2615 20201029 end
                    mPreviewRequestBuilder = CameraPreferences.createPreviewRequest(mCameraDevice);
                    mPreviewRequestBuilder.addTarget(mPreviewSurface);
                    mPreviewReaderSurface = mPreviewReader.getSurface();
                    mPreviewRequestBuilder.addTarget(mPreviewReaderSurface);
                    Log.i(TAG,"[configureSession] mZoomConfig = " + mZoomConfig);
                    if(mZoomConfig != null && mZoomConfig.getCurZoomRatio() != 1.0f){
                        //bv wuyonglin add for bug5276 20210415 start
                        //mPreviewRequestBuilder.set(SCALER_CROP_REGION, mNightCaptureRequest.getScalerCropRegion());
                        mPreviewRequestBuilder.set(CaptureRequest.SCALER_CROP_REGION, mSensorRect);
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_ZOOM_RATIO,mZoomConfig.getCurZoomRatio());
                        //bv wuyonglin add for bug5276 20210415 end
                    }
                    surfaces = Arrays.asList(mPreviewSurface, mImageReader.getSurface(), mPreviewReaderSurface);
                    //surfaces = Arrays.asList(mPreviewSurface, mImageReader.getSurface());
                    mNeedFinalizeOutput = false;
                    mSettingDevice2Configurator.configSessionSurface(surfaces);
                    Log.d(TAG, "[configureSession] surface size : " + surfaces.size());
                    mBuilder = doCreateAndConfigRequest(Camera2Proxy.TEMPLATE_PREVIEW);
                    CameraSysTrace.onEventSystrace("configureSession.createCaptureSession", true, true);
                    Log.d(TAG, "createCaptureSession");
                    mCamera2Proxy.createCaptureSession(surfaces, mSessionCallback,
                            mModeHandler, mBuilder);
                    CameraSysTrace.onEventSystrace("configureSession.createCaptureSession", false, true);
                    mIsPictureSizeChanged = false;
                }else{
                    abortOldSession();
                    mCaptureSurface.releaseCaptureSurfaceLater(false);
                }


            }
        } catch (CameraAccessException e) {
            Log.e(TAG, "[configureSession] error");
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
        //add by huangfei for Restrition no works when switch camera start
        Relation relation = NightRestriction.getRestriction().getRelation("on", true);
        mSettingManager.getSettingController().postRestriction(relation);
        //add by huangfei for Restrition no works when switch camera end

        mSettingController.addViewEntry();
        mSettingController.refreshViewEntry();
        CameraSysTrace.onEventSystrace("photoDevice.configSettingsByStage2", false);
    }

    private void abortOldSession() {
        Log.i(TAG, "[abortOldSession]");
        if (mSession != null) {
            try {
                mSession.abortCaptures();
            } catch (CameraAccessException e) {
                Log.e(TAG, "[abortOldSession] exception", e);
            }
        }
        mSession = null;
        mBuilder = null;
        mDefaultBuilder = null;
    }

    private void configureQuickPreview(Builder builder) {

        //add by huangei for disable quick preview of nightmode start
        if(true){
            return;
        }
        //add by huangei for disable quick preview of nightmode end

        Log.d(TAG, "configureQuickPreview mQuickPreviewKey:" + mQuickPreviewKey);
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
            Log.d(TAG, "configurePdafImgo pdafImgo:" + pdafImgo);
            if (pdafImgo != null) {
                int[] value = new int[1];
                value[0] = 1;
                
                //add by huangfei for night does not works when manual focus start
                //builder.set(CaptureRequest.CONTROL_EFFECT_MODE, CameraMetadata.CONTROL_EFFECT_MODE_MONO);
                //add by huangfei for night does not works when manual focus end

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
        Log.i(TAG, "[repeatingPreview] mSession =" + mSession + " mCamera =" +
                mCamera2Proxy + ",needConfigBuiler " + needConfigBuiler);
        CameraSysTrace.onEventSystrace("NightDevice.repeatingPreview", true, true);
        if (mSession != null && mCamera2Proxy != null) {
            try {
                //bv wuyonglin add for bug5276 20210415 start
                mZoomConfig = mICameraContext.getIApp().getAppUi().getZoomConfig();
                if (mZoomConfig.getCurZoomRatio() != 1.0f) {
                    mPreviewRequestBuilder.set(CaptureRequest.SCALER_CROP_REGION, mSensorRect);
                    mPreviewRequestBuilder.set(CaptureRequest.CONTROL_ZOOM_RATIO,mZoomConfig.getCurZoomRatio());
                }
                //bv wuyonglin add for bug5276 20210415 end
                if (needConfigBuiler) {
                    Builder builder = doCreateAndConfigRequest(Camera2Proxy.TEMPLATE_PREVIEW);
                    builder.addTarget(mPreviewReaderSurface);
                    mSession.setRepeatingRequest(builder.build(), mCaptureCallback, mModeHandler);
                    //bv wuyonglin delete for bug5276 20210415 start
                    //mZoomConfig = mICameraContext.getIApp().getAppUi().getZoomConfig();
                    //bv wuyonglin delete for bug5276 20210415 end
                } else {
                    mBuilder.addTarget(mPreviewSurface);
                    mSession.setRepeatingRequest(mBuilder.build(), mCaptureCallback, mModeHandler);
                }
                mCaptureSurface.setCaptureCallback(this);
            } catch (CameraAccessException | RuntimeException e) {
                Log.e(TAG, "[repeatingPreview] error");
            }
        }
        CameraSysTrace.onEventSystrace("NightDevice.repeatingPreview", false, true);
    }

    private Builder doCreateAndConfigRequest(int templateType) throws CameraAccessException {
        Log.i(TAG, "[doCreateAndConfigRequest] mCamera2Proxy =" + mCamera2Proxy);
        CaptureRequest.Builder builder = null;
        if (mCamera2Proxy != null) {
            builder = mCamera2Proxy.createCaptureRequest(templateType);
            if (builder == null) {
                Log.d(TAG, "Builder is null, ignore this configuration");
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
                HeifHelper.orientation = rotation;
                builder.set(CaptureRequest.JPEG_ORIENTATION, rotation);
                if (mICameraContext.getLocation() != null) {
                    if (!CameraUtil.is3rdPartyIntentWithoutLocationPermission(mActivity)) {
                        builder.set(CaptureRequest.JPEG_GPS_LOCATION,
                                mICameraContext.getLocation());
                    }
                }
            }

        }
        return builder;
    }

    private Builder getDefaultPreviewBuilder() throws CameraAccessException {
        if (mCamera2Proxy != null && mDefaultBuilder == null && mCameraDevice != null) {
            mDefaultBuilder = CameraPreferences.createPreviewRequest(mCameraDevice);
            //mDefaultBuilder = mCamera2Proxy.createCaptureRequest(Camera2Proxy.TEMPLATE_PREVIEW);
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
            Log.e(TAG, "camera process killed due to getCameraCharacteristics() error");
            Process.killProcess(Process.myPid());
        }
        Log.d(TAG, "[getTargetPreviewSize] " + mPreviewWidth + " X " + mPreviewHeight);
        mPreviewSize = new android.util.Size(mPreviewWidth, mPreviewHeight);
        //mImageReader = ImageReader.newInstance(mPreviewWidth, mPreviewHeight,
        //        mNightCaptureRequest.getPhotoForamt(), 1);
        return values;
    }

    private void updatePreviewSize() {
        ISettingManager.SettingController controller = mSettingManager.getSettingController();
        String pictureSize = controller.queryValue(KEY_PICTURE_SIZE);
        Log.i(TAG, "[updatePreviewSize] :" + pictureSize);
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
        Log.i(TAG, "[updatePictureSize] :" + pictureSize);
        if (pictureSize != null) {
            String[] pictureSizes = pictureSize.split("x");
            int width = Integer.parseInt(pictureSizes[0]);
            int height = Integer.parseInt(pictureSizes[1]);
            setPictureSize(new Size(width, height));
        }
    }

    private void setUpCameraOutputs(){
        try {
            CameraManager manager = (CameraManager) mActivity.getSystemService(Context.CAMERA_SERVICE);
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(0 + "");

            int displayRotation = mActivity.getWindowManager().getDefaultDisplay().getRotation();
            int mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            //mJpegRotation = mSensorOrientation;
            Log.i(TAG,"setUpCameraOutputs mJpegRotation = " + mJpegRotation + " mSensorOrientation = " + mSensorOrientation);

/*            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mActivity);

            boolean swappedDimensions = false;
            switch (displayRotation) {
                case Surface.ROTATION_0:
                case Surface.ROTATION_180:
                    if (mSensorOrientation == 90 || mSensorOrientation == 270) {
                        swappedDimensions = true;
                    }
                    break;
                case Surface.ROTATION_90:
                case Surface.ROTATION_270:
                    if (mSensorOrientation == 0 || mSensorOrientation == 180) {
                        swappedDimensions = true;
                    }
                    break;
                default:
                    Log.e(TAG, "Display rotation is invalid: " + displayRotation);
            }

            Point displaySize = new Point();
            mActivity.getWindowManager().getDefaultDisplay().getSize(displaySize);
            int rotatedPreviewWidth = width;
            int rotatedPreviewHeight = height;

            if (swappedDimensions) {
                rotatedPreviewWidth = height;
                rotatedPreviewHeight = width;
            }

            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            Log.i(TAG, "PreviewSize =  " + mPreviewSize + " mPhotoSize = " + mPhotoSize);

            isFocusFixed = true;
            int[] focuses = characteristics.get(CONTROL_AF_AVAILABLE_MODES);
            if (focuses != null) {
                for (int focus : focuses) {
                    if (focus == CONTROL_AF_MODE_CONTINUOUS_PICTURE || focus == CONTROL_AF_MODE_AUTO) {
                        isFocusFixed = false;
                    }
                }
            }*/

            Rect activeRect = characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);//获取成像区域
            Log.d(TAG, "mNightCaptureResult.init mPhotoSize = " + mPhotoSize + " mPreviewSize = " + mPreviewSize + " activeRect = " + activeRect);
            //mNightCaptureResult.init(mActivity, mPhotoSize/*, mPreviewSize, activeRect*/);
/*            if(mConfigNightSettingsHandler != null){
                Log.i(TAG,"mConfigNightSettingsHandler NIGHT_CAPTURERESULT_INIT");
                mConfigNightSettingsHandler.sendEmptyMessage(NIGHT_CAPTURERESULT_INIT);
            }*/
            if(mNightCaptureResult != null && mActivity != null && !isInit){
                /*add by bv liangchangwei for night shot init 20200902 start  */
                Log.d(TAG, "mNightCaptureResult.setCaptureCallback");
                mNightCaptureResult.setCaptureCallback(this);
                new Thread(){
                    @Override
                    public void run() {
                        super.run();
                        mNightCaptureResult.init(mActivity, mPhotoSize/*, mPreviewSize, activeRect*/);
                        isInit = true;
                    }
                }.start();
                /*add by bv liangchangwei for night shot init 20200902 end  */
                Log.d(TAG,"mNightCaptureResult.init mPhotoSize = " + mPhotoSize);
            }
            mImageReader = ImageReader.newInstance(mPhotoSize.getWidth(), mPhotoSize.getHeight(),
                    CameraPreferences.photoForamt, 3);
            Log.d(TAG,"setUpCameraOutputs ImageReader.newInstance width = " + mPhotoSize.getWidth() + " height = " + mPhotoSize.getHeight() + " format = " + mImageReader.getImageFormat());

            if (mUsePreviewReader) {
                if (null != mPreviewReader) {
                    mPreviewReader.setOnImageAvailableListener(null, null);
                    mPreviewReader.close();
                    mPreviewReader = null;
                }
                Log.d(TAG,"mPreviewReader ImageReader.newInstance width = " + mPreviewSize.getWidth() + " height = " + mPreviewSize.getHeight() + " format = " + ImageFormat.YUV_420_888);
                mPreviewReader = ImageReader.newInstance(mPreviewSize.getWidth(), mPreviewSize.getHeight(),
                        ImageFormat.YUV_420_888, 1);
                Log.d(TAG, "setOnImageAvailableListener mPreviewReaderListener");
                mPreviewReader.setOnImageAvailableListener(mPreviewReaderListener, mPreviewReaderHandler);
            }
            Log.d(TAG, "setUpCameraOutputs mPreviewReader = " + mPreviewReader);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void doCameraOpened(@Nonnull Camera2Proxy camera2proxy) {
        Log.i(TAG, "[onOpened]  camera2proxy = " + camera2proxy + " preview surface = "
                + mPreviewSurface + "  mCameraState = " + mCameraState + "camera2Proxy id = "
                + camera2proxy.getId() + " mCameraId = " + mCurrentCameraId);
            Log.d(TAG, "doCameraOpened");
            try {
                if (CameraState.CAMERA_OPENING == getCameraState()
                        && camera2proxy != null && camera2proxy.getId().equals(mCurrentCameraId)) {
                    mCamera2Proxy = camera2proxy;
                    mFirstFrameArrived = false;
                    mCameraDevice = mCamera2Proxy.getCameraDevice();
                    Log.i(TAG,"[onOpened] mCameraDevice = " + mCameraDevice);
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
                    setUpCameraOutputs();
                    CameraSysTrace.onEventSystrace("donCameraOpened.updatePreviewPictureSize", false, true);
                    if (mPreviewSizeCallback != null) {
                        mPreviewSizeCallback.onPreviewSizeReady(new Size(mPreviewWidth,
                                mPreviewHeight));
                    }

                    //add by huangfei for restriction do not works start
                    //bv wuyonglin delete for switcher camera in top quickswitch setting icon not show with other icon 20200307 start
                    /*try {
                        initSettings();
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }*/
                    //bv wuyonglin delete for switcher camera in top quickswitch setting icon not show with other icon 20200307 end
                    //add by huangfei for restriction do not works end

/*                    if (mUsePreviewReader) {
                        if (null != mPreviewReader) {
                            mPreviewReader.setOnImageAvailableListener(null, null);
                            mPreviewReader.close();
                            mPreviewReader = null;
                        }
                        mPreviewReader = ImageReader.newInstance(mPreviewWidth, mPreviewHeight,
                                ImageFormat.YUV_420_888, 1);
                        mPreviewReader.setOnImageAvailableListener(mPreviewReaderListener, mPreviewReaderHandler);
                    }*/

                    if (mNeedSubSectionInitSetting) {
                        configureSession(true);
                    } else {
                        //bv wuyonglin add for switcher camera in top quickswitch setting icon not show with other icon 20200307 start
                        Relation relation = NightRestriction.getRestriction().getRelation("on",true);
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
        Log.i(TAG, "[onDisconnected] camera2proxy = " + camera2proxy);
        if (mCamera2Proxy != null && mCamera2Proxy == camera2proxy) {
            CameraUtil.showErrorInfoAndFinish(mActivity, CameraUtil.CAMERA_ERROR_SERVER_DIED);
            updateCameraState(CameraState.CAMERA_UNKNOWN);
            mCurrentCameraId = null;
            mCameraDevice = null;
        }
    }

    @Override
    public void doCameraError(@Nonnull Camera2Proxy camera2Proxy, int error) {
        Log.i(TAG, "[onError] camera2proxy = " + camera2Proxy + " error = " + error);
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
                    Log.i(TAG, "[onConfigured],session = " + session
                            + ", mNeedFinalizeOutput:" + mNeedFinalizeOutput);
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
                        try {
                            Log.d(TAG, "onConfigured setRepeatingRequest");

                            if(mPreviewRequestBuilder != null){
                                mSession.setRepeatingRequest(mPreviewRequestBuilder.build(),
                                        mCaptureCallback, mModeHandler);
                            }
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                    } finally {
                        mDeviceLock.unlock();
                    }
                }

                @Override
                public void onConfigureFailed(@Nonnull Camera2CaptureSessionProxy session) {
                    Log.i(TAG, "[onConfigureFailed],session = " + session);
                    if (mSession == session) {
                        mSession = null;
                    }
                }

                @Override
                public void onClosed(@Nonnull Camera2CaptureSessionProxy session) {
                    super.onClosed(session);
                    Log.i(TAG, "[onClosed],session = " + session);
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
                Log.d(TAG, "[onCaptureStarted] capture started, frame: " + frameNumber);
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
                CameraSysTrace.onEventSystrace("NightDevice.onP2Done", true, true);
                    Log.d(TAG, "[onCaptureProgressed] P2done comes, frame: " + num);
                updateCameraState(CameraState.CAMERA_OPENED);
                mModeDeviceCallback.onPreviewCallback(null, 0);
                CameraSysTrace.onEventSystrace("NightDevice.onP2Done", false, true);
            }
        }

        @Override
        public void onCaptureCompleted(@Nonnull CameraCaptureSession session,
                @Nonnull CaptureRequest request, @Nonnull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            //Log.d(TAG,"onCaptureCompleted");
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
                    mModeDeviceCallback.onPreviewCallback(null, 0);
                }
                mCaptureFrameMap.remove(String.valueOf(num));
            } else if (!CameraUtil.isStillCaptureTemplate(result) && !mFirstFrameArrived) {
                CameraSysTrace.onEventSystrace("NightDevice.onFirstFrameArrived", true, true);
                mFirstFrameArrived = true;
                updateCameraState(CameraState.CAMERA_OPENED);
                mModeDeviceCallback.onPreviewCallback(null, 0);
                mICameraContext.getSoundPlayback().init();
                CameraSysTrace.onEventSystrace("NightDevice.onFirstFrameArrived", false, true);
            }
            mSettingDevice2Configurator.getRepeatingCaptureCallback().onCaptureCompleted(
                    session, request, result);
            //Log.d(TAG,"onCaptureProgressed mNightCaptureRequest.onPreviewCaptureCompleted");
            if(mNightCaptureRequest != null){
                mNightCaptureRequest.onPreviewCaptureCompleted(result);
            }
        }

        @Override
        public void onCaptureFailed(@Nonnull CameraCaptureSession session,
                @Nonnull CaptureRequest request, @Nonnull CaptureFailure failure) {
            super.onCaptureFailed(session, request, failure);
            Log.e(TAG, "[onCaptureFailed], framenumber: " + failure.getFrameNumber()
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
            Log.d(TAG, "<onCaptureSequenceAborted>");
        }

        @Override
        public void onCaptureBufferLost(CameraCaptureSession session, CaptureRequest request,
                                        Surface target, long frameNumber) {
            super.onCaptureBufferLost(session, request, target, frameNumber);
            Log.d(TAG, "<onCaptureBufferLost> frameNumber: " + frameNumber);
        }
    };

    @Override
    public void setZSDStatus(String value) {
        mZsdStatus = value;
    }

    @Override
    public void setFormat(String value) {
        Log.i(TAG, "[setCaptureFormat] value = " + value + " mCameraState = " +
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

    //add by huangfei for restriction do not works start
    private void initSettings() throws CameraAccessException {
        Log.i(TAG, "[openCamera] cameraId : " + "initSettings");
        Relation relation = NightRestriction.getRestriction().getRelation("on",true);
        mSettingController.postRestriction(relation);
        mSettingController.addViewEntry();
        mSettingController.refreshViewEntry();
    }
    //add by huangfei for restriction do not works end

    @Override
    public void onCaptureStart(TotalCaptureResult result) {
        Log.i(TAG, "onCaptureStart");
        Location location = mICameraContext.getLocation();
        mExif = ExifInterface.addExifTags(0, result, location);
        mICameraContext.getSoundPlayback().play(ISoundPlayback.SHUTTER_CLICK);
    }

    @Override
    public void onCaptureCompleted(TotalCaptureResult result) {
        //Log.i(TAG, "onCaptureCompleted");
        startPreview();
        mCapturePreview = true;
        mStart = false;
/*        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "isWriteThumb : " + isWriteThumb);
                if (!isWriteThumb) {
                    isWriteThumb = true;
                    //writeThumb();
                }
                //hideCircleProgressBar();
            }
        });*/
    }

    @Override
    public void showToast(final String text) {
/*
        final Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity, text, Toast.LENGTH_LONG).show();
                }
            });
        }
*/
    }

    @Override
    public void saveNightData(byte[] data, int format, android.util.Size photosize) {
        String formatTag = mCaptureSurface.getCaptureType();
        Log.d(TAG, " <onPictureCallback> data = " + data + ", format = " + format + " formatTag = " + formatTag
                + ", width = " + photosize.getWidth() + ", height = " + photosize.getHeight()
                + ", mCaptureDataCallback = " + mCaptureDataCallback + " mJpegRotation = " + mJpegRotation );
        if (format == ImageFormat.YUV_420_888 || format == ImageFormat.NV21) {
            int rotation = (mJpegRotation + 90)%360;
            Log.i(TAG," saveNightData mJpegRotation = " + mJpegRotation);
            if("1".equals(mCurrentCameraId)){
                if(rotation == 90 || rotation == 270){
                    rotation = (rotation + 180)%360;
                }
                boolean isMirror = "1".equals(mDataStore.getValue("key_camera_mirror", "1", mDataStore.getCameraScope(Integer.parseInt(mCurrentCameraId))));
                Log.i(TAG," saveNightData isMirror = " + isMirror+" mJpegRotation ="+mJpegRotation+" mOrientation ="+mOrientation);
                if (isMirror) {
                    if (rotation % 180 == 0) {
                        data = YuvEncodeJni.getInstance().RotateYuv(data, format, photosize.getWidth(), photosize.getHeight(), 0, true, true);
                    } else {
                        data = YuvEncodeJni.getInstance().RotateYuv(data, format, photosize.getWidth(), photosize.getHeight(), 90, true, true);
                        data = YuvEncodeJni.getInstance().RotateYuv(data, format, photosize.getHeight(), photosize.getWidth(), 270, true, false);
                    }
                }
            }
            if(CameraUtil.isWaterMarkOn(mDataStore,mActivity)){
                Log.i(TAG,"isWaterMarkOn on + ");
                WaterMarkUtil.yuvAddWaterMark(mActivity,data, photosize.getWidth(), photosize.getHeight(), rotation);
                Log.i(TAG,"isWaterMarkOn on - ");
            }else{
                Log.i(TAG,"isWaterMarkOn off");
            }

            rotation = rotation%360 ;
            Log.i(TAG," EncodeYuvToJpeg width = " + photosize.getWidth() + " height = " + photosize.getHeight() + " rotation = " + rotation);
            data = YuvEncodeJni.getInstance().EncodeYuvToJpeg(data, ImageFormat.NV21,
                    photosize.getWidth(), photosize.getHeight(), 95, rotation);
        }

        if (mCaptureDataCallback != null) {
            DataCallbackInfo info = new DataCallbackInfo();
            info.data = data;
            info.needUpdateThumbnail = false;
            info.needRestartPreview = false;
            info.mBufferFormat = ImageFormat.JPEG;
            info.imageHeight = photosize.getHeight();
            info.imageWidth = photosize.getWidth();
            if (ThumbnailHelper.isPostViewSupported()) {
                //info.needUpdateThumbnail = false;
            }
            if (ThumbnailHelper.FORMAT_TAG.equalsIgnoreCase(formatTag)) {
                CameraSysTrace.onEventSystrace("NightDevice.onPostViewCallback", true, true);
                Log.i(TAG,"mCaptureDataCallback.onPostViewCallback(data)");
                mCaptureDataCallback.onPostViewCallback(data);
                CameraSysTrace.onEventSystrace("NightDevice.onPostViewCallback", false, true);
            } else {
                CameraSysTrace.onEventSystrace("NightDevice.onJpegCallback", true, true);
                Log.i(TAG,"mCaptureDataCallback.onDataReceived(data)");
                mCaptureDataCallback.onDataReceived(info);
                CameraSysTrace.onEventSystrace("NightDevice.onJpegCallback", false, true);
                Log.i(TAG,"mIsBGServiceEnabled = " + mIsBGServiceEnabled + " mCaptureSurface = " + mCaptureSurface);
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

/*
        if (data != null) {
            if (format == ImageFormat.YUV_420_888 || format == ImageFormat.NV21) {
                data = YuvEncodeJni.getInstance().EncodeYuvToJpeg(data, ImageFormat.NV21,
                        mPhotoSize.getWidth(), mPhotoSize.getHeight(), 95, mJpegRotation);
            }

            String title = NightShotExpEngine.getInstance().getDumpFileTitle();
            String path = Environment.getExternalStorageDirectory().getPath() + "/DCIM/night/";
            new File(path).mkdirs();
            Log.d(TAG,"title = " + title + " path = " + path);
            long fileLength = Util.writeImage(path + title + ".jpg", mExif, data);
            Log.d(TAG," fileLength = " + fileLength);            if (fileLength > 0) {
                mUri = Util.insertContent(mActivity, path + title + ".jpg", title, fileLength);
            }
            //mThumbnail.updateThumbnailUri(mUri);
        }
*/
        //mStart = false;
//        isWriteThumb = false;
//        startPreview();
    }

/*    @Override
    public void onCaptureCompleted(int captureNum){
        //do nothing && not used !!
    }*/

    @Override
    public void addExif(String path, byte[] data){
        Log.i(TAG,"addExif path = " + path + " mExif = " + mExif + " data.size = " + data.length);
        Util.writeImage(path, mExif, data);
    }

    private void startBackgroundThread() {
        Log.d(TAG, "startBackgroundThread");

        if(mCaptureThread == null){
            mCaptureThread = new HandlerThread("CameraCapture");
            mCaptureThread.start();
        }
        if(mCaptureHandler == null){
            mCaptureHandler = new Handler(mCaptureThread.getLooper());
        }

        if(mPreviewReaderThread == null){
            mPreviewReaderThread = new HandlerThread("CameraPreviewReader");
            mPreviewReaderThread.start();
        }
        if(mPreviewReaderHandler == null){
            mPreviewReaderHandler = new Handler(mPreviewReaderThread.getLooper());
        }
    }

    private void stopBackgroundThread() {
        Log.i(TAG,"stopBackgroundThread");
        if (mCaptureHandler != null) {
            mCaptureHandler.removeCallbacksAndMessages(null);
        }
        if(mCaptureThread != null){
            mCaptureThread.quitSafely();
        }
        try {
            if(mCaptureThread != null){
                mCaptureThread.join();
            }
            mCaptureThread = null;
            mCaptureHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(mPreviewReaderThread != null){
            mPreviewReaderThread.quitSafely();
        }
        try {
            if(mPreviewReaderThread != null){
                mPreviewReaderThread.join();
            }
            mPreviewReaderThread = null;
            mPreviewReaderHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public class PreviewRunnable implements Runnable {

        @Override
        public void run() {
            Log.e(TAG, "PreviewRunnable running");

/*            while (mRun) {
                Bitmap bitmap = mTextureView.getBitmap(mPreviewWidth, mPreviewHeight);
                if (bitmap != null) {
                    int width = bitmap.getWidth();
                    int height = bitmap.getHeight();
                    int bytes = bitmap.getByteCount();
                    ByteBuffer buffer = ByteBuffer.allocate(bytes);
                    bitmap.copyPixelsToBuffer(buffer);
                    byte[] data = buffer.array();
                    byte[] nv21 = YuvEncodeJni.getInstance().Argb2Yuv(data, width, height, width * 4, ImageFormat.NV21);
                    if (mNightCaptureRequest != null) {
                        mNightCaptureRequest.onPreviewFrame(nv21, width, height, mOrientation);
                    }
                    bitmap.recycle();
                    buffer.clear();
                }
            }*/
        }
    }

    private ImageReader.OnImageAvailableListener mPreviewReaderListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            if (mNightCaptureRequest != null) {
                try{
                    Image image = reader.acquireNextImage();
                    //Log.i(TAG, " image Format = " + image.getFormat());
                    //byte[] captureYuv = ImageFormatUtil.getDataFromImage(image, ImageFormatUtil.COLOR_FormatNV21);
                    byte[] captureYuv = YuvEncodeJni.getInstance().getBuffer(image,false);
                    int width = image.getWidth();
                    int height = image.getHeight();
                    image.close();
                    synchronized (this) {
                        //Log.d(TAG,"mPreviewReaderListener mNightCaptureRequest.onPreviewFrame captureYuv.length = " + captureYuv.length + " width = " + width + " height = " + height );
                        if(mNightCaptureRequest != null){
                                mNightCaptureRequest.onPreviewFrame(captureYuv, width, height, mOrientation);
                        }
                    }

                    if(mCapturePreview == true){
                        mCapturePreview = false;
                        Log.i(TAG, "onPostViewCallback mCapturePreview = " + mCapturePreview + " width = " + width + " height = " + height + " size = " + captureYuv.length);
                        int rotation = (mJpegRotation + 90)%360;
                        if(mCurrentCameraId.equals("1")){
                            if(rotation == 90 || rotation == 270){
                                rotation = (rotation + 180)%360;
                            }
                            boolean isMirror = "1".equals(mDataStore.getValue("key_camera_mirror", "1", mDataStore.getCameraScope(Integer.parseInt(mCurrentCameraId))));
                            Log.i(TAG,"saveNightData isMirror = " + isMirror+" mJpegRotation ="+mJpegRotation+" mOrientation ="+mOrientation);
                            if (isMirror) {
                                if (rotation % 180 == 0) {
                                    captureYuv = YuvEncodeJni.getInstance().RotateYuv(captureYuv, ImageFormat.NV21, width, height, 0, true, true);
                                } else {
                                    captureYuv = YuvEncodeJni.getInstance().RotateYuv(captureYuv, ImageFormat.NV21, width, height, 90, true, true);
                                    captureYuv = YuvEncodeJni.getInstance().RotateYuv(captureYuv, ImageFormat.NV21, height, width, 270, true, false);
                                }
                            }
                        }

                        byte[] data = YuvEncodeJni.getInstance().EncodeYuvToJpeg(captureYuv, ImageFormat.NV21,
                                width, height, 95, rotation);
                        Log.i(TAG, "onPostViewCallback mCapturePreview = " + mCapturePreview + " width = " + width + " height = " + height + " size = " + data.length + " rotation = " + rotation);
                        mCaptureDataCallback.onPostViewCallback(data);
                    }
                }catch (IllegalStateException error){
                    Log.i(TAG,"catch IllegalStateException error!!!!");
                }

            }
        }
    };

    private class MyOrientationEventListener
            extends OrientationEventListener {

        private MyOrientationEventListener(Context context) {
            super(context);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            if (orientation == ORIENTATION_UNKNOWN) {
                return;
            }

            int tmp = roundOrientation(orientation, mOrientation);
            if (mOrientation != tmp) {
                mOrientation = tmp;
            }
        }

        public void setEnable(boolean enable) {
            Log.i(TAG, "set orientation event listener enable:" + enable);
            if (enable) {
                enable();
            } else {
                disable();
            }
        }
    }

    public static int roundOrientation(int orientation, int orientationHistory) {
        boolean changeOrientation = false;
        if (orientationHistory == OrientationEventListener.ORIENTATION_UNKNOWN) {
            changeOrientation = true;
        } else {
            int dist = Math.abs(orientation - orientationHistory);
            dist = Math.min(dist, 360 - dist);
            changeOrientation = (dist >= 50);
        }
        if (changeOrientation) {
            return ((orientation + 45) / 90 * 90) % 360;
        }
        return orientationHistory;
    }

    private class ConfigNightSettingsHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.d(TAG, "handleMessage what =  " + msg.what);
            switch (msg.what){
                case NIGHT_CAPTURERESULT_INIT:
                    Log.d(TAG, "handleMessage NIGHT_CAPTURERESULT_INIT");
                    if(mNightCaptureResult != null && mActivity != null){
                        mNightCaptureResult.init(mActivity, mPhotoSize/*, mPreviewSize, activeRect*/);
                        Log.d(TAG,"mNightCaptureResult.init mPhotoSize = " + mPhotoSize);
                    }
                    //Log.d(TAG,"setUpCameraOutputs ImageReader.newInstance width = " + mPhotoSize.getWidth() + " height = " + mPhotoSize.getHeight() + " format = " + mNightCaptureRequest.getPhotoForamt());
                    mImageReader = ImageReader.newInstance(mPhotoSize.getWidth(), mPhotoSize.getHeight(),
                            CameraPreferences.photoForamt, 3);

                    break;
                case NIGHT_CAPTURERESULT_SET_PHOTOSIZE:
                    Log.d(TAG, "handleMessage NIGHT_CAPTURERESULT_SET_PHOTOSIZE");
                    if(mNightCaptureResult != null && mActivity != null){
                        mNightCaptureResult.init(mActivity, mPhotoSize);
                        Log.d(TAG,"mNightCaptureResult.updatePhotoSize mPhotoSize = " + mPhotoSize);
                    }
                    if (mNightCaptureRequest != null) {
                    Log.d(TAG,"setUpCameraOutputs ImageReader.newInstance width = " + mPhotoSize.getWidth() + " height = " + mPhotoSize.getHeight() + " format = " + CameraPreferences.photoForamt);
                    mImageReader = ImageReader.newInstance(mPhotoSize.getWidth(), mPhotoSize.getHeight(),
                            CameraPreferences.photoForamt, 2);
                    }

                    break;
                default:
                    break;
            }
        }
    }
}
