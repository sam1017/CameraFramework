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

package com.mediatek.camera.common.mode.photo.device;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
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
import android.media.ImageReader;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Range;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.net.Uri;

import com.aiworks.android.camera.CameraPreferences;
import com.aiworks.android.hdr.HDRCaptureCallback;
import com.aiworks.android.hdr.HDRCaptureRequest;
import com.aiworks.android.hdr.HDRCaptureRequestInterface;
import com.aiworks.android.hdr.HDRCaptureResult;
import com.aiworks.android.hdr.HDRCaptureResultInterface;
import com.aiworks.android.hdr.HDRConfig;
import com.aiworks.android.utils.Util;
import com.aiworks.hdrdemo.CompareActivity;
import com.aiworks.yuvUtil.YuvEncodeJni;

import com.mediatek.camera.WaterMarkUtil;
import com.mediatek.camera.CameraApplication;
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
import com.android.camera.exif.ExifInterface;
import com.mediatek.camera.common.loader.DeviceDescription;
import com.mediatek.camera.common.mode.CameraApiHelper;
import com.mediatek.camera.common.mode.Device2Controller;
import com.mediatek.camera.common.mode.photo.DeviceInfo;
import com.mediatek.camera.common.mode.photo.heif.HeifCaptureSurface;
import com.mediatek.camera.common.mode.photo.heif.ICompeletedCallback;
import com.mediatek.camera.common.mode.photo.heif.IDeviceListener;
import com.mediatek.camera.common.mode.photo.HeifHelper;
import com.mediatek.camera.common.mode.photo.P2DoneInfo;
import com.mediatek.camera.common.mode.photo.ThumbnailHelper;
import com.aiworks.android.utils.Product;
import com.aiworks.android.utils.CameraHelper;
import com.aiworks.android.utils.ImageFormatUtil;

import com.mediatek.camera.common.setting.ISettingManager;
import com.mediatek.camera.common.setting.ISettingManager.SettingController;
import com.mediatek.camera.common.setting.ISettingManager.SettingDevice2Configurator;
import com.mediatek.camera.common.sound.ISoundPlayback;
import com.mediatek.camera.common.utils.CameraUtil;
import com.mediatek.camera.common.utils.Size;
//bv wuyonglin add for add hdr quickswitch 20191231 end
import android.graphics.ImageFormat;
import android.os.IBinder;
import android.os.RemoteException;

import com.mediatek.camera.feature.setting.zoom.IZoomConfig;
import com.mediatek.campostalgo.FeatureConfig;
import com.mediatek.campostalgo.FeatureParam;
import com.mediatek.campostalgo.FeaturePipeConfig;
import com.mediatek.campostalgo.FeatureResult;
import com.mediatek.campostalgo.ICamPostAlgoCallback;
import com.mediatek.campostalgo.StreamInfo;
//bv wuyonglin add for add hdr quickswitch 20191231 end
//bv wuyonglin add for hd shot 20201013 start
import com.mediatek.camera.common.relation.Relation;
import static android.hardware.camera2.CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE;
import static android.hardware.camera2.CaptureRequest.SCALER_CROP_REGION;
import static com.aiworks.android.utils.SystemProperties.get;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import com.aiworks.android.multiframe.MultiFrameCaptureCallback;
import com.aiworks.android.multiframe.MultiFrameCaptureManager;
import com.mediatek.camera.R;
import com.mediatek.camera.common.relation.DataStore;
//bv wuyonglin add for hd shot 20201013 end

import com.mediatek.camera.portability.SystemProperties;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import com.mediatek.camera.R;
import javax.annotation.Nonnull;

//add by huang fei disable setting items start
import com.mediatek.camera.common.relation.Relation;
import com.mediatek.camera.Config;
import com.mediatek.camera.common.mode.photo.PhotoRestriction;
import android.view.OrientationEventListener;
import android.hardware.Camera;
/**
 * An implementation of {@link IDeviceController} with Camera2Proxy.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class PhotoDevice2Controller extends Device2Controller implements
        IDeviceController, ICompeletedCallback,
        CaptureSurface.ImageCallback,
        ISettingManager.SettingDevice2Requester, HDRCaptureCallback, MultiFrameCaptureCallback, SensorEventListener {
    private static final Tag TAG = new Tag(PhotoDevice2Controller.class.getSimpleName());
    //add by liangchangwei for AiWorks HDR
    private static final int MAX_PREVIEW_WIDTH = 1920;
    private static final int MAX_PREVIEW_HEIGHT = 1080;
    private static final String CAMERA_OUTSIDE_PREFERENCES_PATH = "/sdcard/hdr/"; // customized_camera_preferences文件所在路径
    private static final String CAMERA_OUTSIDE_PREFERENCES_FILE_NAME = "customized_camera_preferences.xml";
    private static final String KEY_BACK_CAMERA_HDR_CONFIG = "pref_hdr_back_config_key";
    private static final String KEY_FRONT_CAMERA_HDR_CONFIG = "pref_hdr_front_config_key";
    private static final String KEY_WIDE_CAMERA_HDR_CONFIG = "pref_hdr_wide_config_key";
    public static final String MODEL_DIR = "AIWorksModels";
    public static final String MODEL_DIR_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()
            + "/Android/data/";
    private static final int HDR_CAPTURERESULT_INIT = 0;
    private static final int HDR_CAPTURERESULT_SET_PHOTOSIZE = 1;
    //add by liangchangwei for AiWorks HDR
    private static final String KEY_PICTURE_SIZE = "key_picture_size";
    private static final int CAPTURE_MAX_NUMBER = 5;
    private static final int WAIT_TIME = 5;
    //bv wuyonglin add for add hdr quickswitch 20191231 start
    private static final String HDR_DEFAULT_VALUE = "off";
    private static final int CAPTURE_REQUEST_NUM = 3;
    private int mCaptureNum;
    //3 capture request for hdr algo
    private static final int CAPTURE_REQUEST_SIZE_BY_ALGO = 3;
    private static final String POSTALGO_PARAMS_JPEG_ORIENTATION_KEY = "postalgo.capture.jpegorientation";
    //bv wuyonglin add for add hdr quickswitch 20191231 end
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
    //bv wuyonglin modify for add hdr quickswitch 20191231 start
    //private final CaptureSurface mCaptureSurface;
    private CaptureSurface mCaptureSurface;
    private Surface mCapturePostAlgoSurface;
    //bv wuyonglin modify for add hdr quickswitch 20191231 end
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


    //add by liangchangwei for AiWorks HDR
    private int mCameraID = 0;
    private boolean isInit = false;
    private HDRCaptureRequestInterface mHDRCaptureRequest;
    private HDRCaptureResultInterface mHDRCaptureResult;
    private HandlerThread mBackgroundThread = null;
    private Handler mBackgroundHandler = null;
    private HandlerThread mCaptureThread = null;
    private Handler mCaptureHandler = null;
    private HandlerThread mPreviewReaderThread = null;
    private Handler mPreviewReaderHandler = null;
    private ExifInterface mExif;
    private CameraCaptureSession mCaptureSession;
    private CaptureRequest.Builder mPreviewRequestBuilder;
    private ImageReader mImageReader;
    private ImageReader mPreviewReader;
    private Surface mPreviewReaderSurface;
    private android.util.Size mPhotoSize;
    private android.util.Size mPreviewSize;
    private CameraDevice mCameraDevice;
    private boolean mStart = false;
    private boolean mCapturePreview = false;
    private Rect mScalerCropRegion;
    private IZoomConfig mZoomConfig = null;
    private Handler mConfigHdrSettingsHandler = new ConfigHDRSettingsHandler();
    private boolean AutoHDRStatus = false;
    private boolean HDRInit = false;
    private int AutoHDRStatuscount = 0;
	//bv liangchangwei for HDR

    //bv wuyonglin add for hd shot 20201013 start
    private MultiFrameCaptureManager mManager;
    private SensorManager sensorManager;
    private String mAisStatus = "off";
    private String mFlashStatus = "off";
    private String FLASH_DEFAULT_VALUE ="off";
    //bv wuyonglin add for hd shot 20201013 end

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
    //add by huangfei for  picture size ratio 1:1 start
    private DataStore mDataStore;
    public static final String PICTURE_SIZE_1_1 = "picture_size_1_1";
    public static final double RATIO_1_1 = 1d / 1;
    //add by huangfei for  picture size ratio 1:1 end
    //bv wuyonglin add for add hdr quickswitch 20191231 start
    private boolean mIsHdrOpened = false;
    private boolean mRequestRestartSession = false;
    //bv wuyonglin add for add hdr quickswitch 20191231 end
    //bv wuyonglin add for hdr open take photo picture have mirror 20200713 start
    private static final String MTK_POSTALGO_MIRROR
            = "postalgo.facebeauty.mirror";
    //bv wuyonglin add for hdr open take photo picture have mirror 20200713 end
    //bv wuyonglin add for bug5276 20210415 start
    private Rect mSensorRect;
    //bv wuyonglin add for bug5276 20210415 end
    private MyOrientationEventListener mOrientationListener;
    private int mOrientation = -1;

     //add by liangchangwei for AiWorks HDR
	private String mHdrStatus = "off";

    String BackCameraconfigJsonStr = null;
    String FrontCameraconfigJsonStr = null;
    String WideCameraconfigJsonStr = null;
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
     * PhotoDevice2Controller may use activity to get display rotation.
     * @param activity the camera activity.
     */
    PhotoDevice2Controller(@Nonnull Activity activity, @Nonnull ICameraContext context) {
        LogHelper.d(TAG, "[PhotoDevice2Controller]");
        //bv wuyonglin add for add hdr quickswitch 20191231 start
        //add by huangfei for  picture size ratio 1:1 start
        mDataStore = context.getDataStore();
        //add by huangfei for  picture size ratio 1:1 end
        String value = mDataStore.getValue("key_hdr", HDR_DEFAULT_VALUE, mDataStore.getGlobalScope());
        HDRInit = false;
        LogHelper.i(TAG,"HDRInit = " + HDRInit);
	    //add by liangchangwei for AiWorks HDR

        String PREFERENCES_FILE_NAME = "customized_camera_preferences.xml";
        String HDR_DUMP_PATH = activity.getExternalFilesDir("hdr").getPath() + "/";

        File xmlFile = new File(HDR_DUMP_PATH + PREFERENCES_FILE_NAME);
        if (!xmlFile.exists()) {
            try {
                AssetManager assetManager = activity.getAssets();
                String model = get("ro.product.model");
                LogHelper.i(TAG,"model = " + model);
                new File(HDR_DUMP_PATH).mkdirs();
                if(model != null){
                    String path = model + "/" + PREFERENCES_FILE_NAME;
                    LogHelper.i(TAG,"path = " + path);
                    Util.copyFile(assetManager.open(path), xmlFile);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(!CameraUtil.MTKCAM_AIWORKS_HDR_SUPPORT){
            if ("on".equals(value)) {
                mIsHdrOpened = true;
            } else {
                mIsHdrOpened = false;
            }
        }else{
            mHdrStatus = value;

            //HashMap<String, String> customPreferences = CameraPreferences.loadCustomizedCameraPreferences(
            //        activity, CAMERA_OUTSIDE_PREFERENCES_PATH, CAMERA_OUTSIDE_PREFERENCES_FILE_NAME);
            HashMap<String, String> customPreferences = CameraPreferences.loadCustomizedCameraPreferences(activity, HDR_DUMP_PATH, PREFERENCES_FILE_NAME);

            if (customPreferences != null) {
                BackCameraconfigJsonStr = customPreferences.get(KEY_BACK_CAMERA_HDR_CONFIG);
                FrontCameraconfigJsonStr = customPreferences.get(KEY_FRONT_CAMERA_HDR_CONFIG);
                WideCameraconfigJsonStr = customPreferences.get(KEY_WIDE_CAMERA_HDR_CONFIG);
            }

            //HDRConfig.setBinFilePath(CAMERA_OUTSIDE_PREFERENCES_PATH);
            HDRConfig.setDumpFilePath(HDR_DUMP_PATH);

            String modelPath = MODEL_DIR_PATH + activity.getPackageName() + "/" + MODEL_DIR;
            com.aiworks.android.utils.Util.copyModle(activity, modelPath, MODEL_DIR);
            HDRConfig.setFaceModelPath(modelPath);

        }
        //bv wuyonglin add for add hdr quickswitch 20191231 end
        mActivity = activity;
        mCameraManager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        mICameraContext = context;
        mBGServiceKeeper = mICameraContext.getBGServiceKeeper();
        if (mBGServiceKeeper != null) {
            DeviceDescription deviceDescription = CameraApiHelper.getDeviceSpec(
                    mActivity.getApplicationContext()).getDeviceDescriptionMap().get("0");
            if (deviceDescription != null && !isThirdPartyIntent(mActivity)
                    && mBGServiceKeeper.getBGHidleService() != null) {
                if(!CameraUtil.MTKCAM_AIWORKS_HDR_SUPPORT){ ///add by liangchangwei
                    //bv wuyonglin add for add hdr quickswitch 20191231 start
                    if (!mIsHdrOpened) {
                        //bv wuyonglin add for add hdr quickswitch 20191231 end
                        mIsBGServiceEnabled = true;
                        //bv wuyonglin add for add hdr quickswitch 20191231 start
                    } else {
                        mIsBGServiceEnabled = false;
                    }
                    //bv wuyonglin add for add hdr quickswitch 20191231 end
                }else{
                    mIsBGServiceEnabled = true;
                }
                mBGServicePrereleaseKey = deviceDescription.getKeyBGServicePrerelease();
                mBGServiceImagereaderIdKey = deviceDescription.getKeyBGServiceImagereaderId();
            }
        }
        LogHelper.d(TAG, "mBGServiceKeeper = " + mBGServiceKeeper
                + ", isThirdPartyIntent = " + isThirdPartyIntent(mActivity)
                + ", mIsBGServiceEnabled = " + mIsBGServiceEnabled
                + ", mBGServicePrereleaseKey = " + mBGServicePrereleaseKey
                + ", mBGServiceImagereaderIdKey = " + mBGServiceImagereaderIdKey);
        int currentMode = HeifHelper.getCurrentMode();
		//add by liangchangwei for AiWorks HDR
        if(CameraUtil.MTKCAM_AIWORKS_HDR_SUPPORT){
            if(currentMode == HeifHelper.HEIF_MODE_SURFACE /*&& !mHdrStatus.equals("off")*/){
                mCaptureSurface = new HeifCaptureSurface(mICameraContext, this);
                mIDeviceListener = (HeifCaptureSurface)mCaptureSurface;
            }else if(mIsBGServiceEnabled){
                mCaptureSurface = new CaptureSurface(mBGServiceKeeper.getBGCaptureHandler());
                LogHelper.i(TAG, "BG mCaptureSurface = " + mCaptureSurface);
            }else{
                mCaptureSurface = new CaptureSurface();
            }
        }else{
            //bv wuyonglin modify for add hdr quickswitch 20191231 start
            if (currentMode == HeifHelper.HEIF_MODE_SURFACE && !mIsHdrOpened) {
                //bv wuyonglin modify for add hdr quickswitch 20191231 end
                mCaptureSurface = new HeifCaptureSurface(mICameraContext, this);
                mIDeviceListener = (HeifCaptureSurface)mCaptureSurface;
            } else if (mIsBGServiceEnabled) {
                mCaptureSurface = new CaptureSurface(mBGServiceKeeper.getBGCaptureHandler());
                LogHelper.d(TAG, "BG mCaptureSurface = " + mCaptureSurface);
            } else {
                mCaptureSurface = new CaptureSurface();
            }
        }
        mCaptureSurface.setCaptureCallback(this);
        mThumbnailSurface = new CaptureSurface();
        mThumbnailSurface.setCaptureCallback(this);
        mThumbnailSurface.setFormat(ThumbnailHelper.FORMAT_TAG);
        mCameraDeviceManager = mICameraContext.getDeviceManager(CameraApi.API2);
		//add by liangchangwei for AiWorks HDR
        startBackgroundThread();
        //bv wuyonglin delete for bug3791 20200202 start
        /*if (mOrientationListener == null) {
            mOrientationListener = new MyOrientationEventListener(mActivity.getApplicationContext());
        }
        mOrientationListener.setEnable(true);*/
        //bv wuyonglin delete for bug3791 20200202 end
        //bv wuyonglin add for hd shot 20201013 start
        FLASH_DEFAULT_VALUE = mActivity.getString(R.string.flash_default_value);
        if (Config.isAisSupport(mActivity.getApplicationContext())) {
        registerSensors();
        }
        //bv wuyonglin add for hd shot 20201013 end
    }

    //add by liangchangwei for AiWorks HDR
    private void startBackgroundThread() {
        if(mBackgroundThread == null){
            mBackgroundThread = new HandlerThread("CameraBackground");
        }
        mBackgroundThread.start();
        if(mBackgroundHandler == null){
            mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
        }

        if(mCaptureThread == null){
            mCaptureThread = new HandlerThread("CameraCapture");
        }
        mCaptureThread.start();
        if(mCaptureHandler == null){
            mCaptureHandler = new Handler(mCaptureThread.getLooper());
        }

        if(mPreviewReaderThread == null){
            mPreviewReaderThread = new HandlerThread("CameraPreviewReader");
        }
        mPreviewReaderThread.start();
        if(mPreviewReaderHandler == null){
            mPreviewReaderHandler = new Handler(mPreviewReaderThread.getLooper());
        }
    }

    private void stopBackgroundThread() {
        if(mBackgroundThread != null){
            mBackgroundThread.quitSafely();
            try {
                mBackgroundThread.join();
                mBackgroundThread = null;
                mBackgroundHandler = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if(mCaptureThread != null){
            mCaptureThread.quitSafely();
            try {
                mCaptureThread.join();
                mCaptureThread = null;
                mCaptureHandler = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if(mPreviewReaderThread != null){
            mPreviewReaderThread.quitSafely();
            try {
                mPreviewReaderThread.join();
                mPreviewReaderThread = null;
                mPreviewReaderHandler = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        LogHelper.i(TAG,"stopBackgroundThread");
    }

    @Override
    public void queryCameraDeviceManager() {
        mCameraDeviceManager = mICameraContext.getDeviceManager(CameraApi.API2);
    }

    @Override
    public void openCamera(DeviceInfo info) {
        synchronized (CameraApplication.class) {
            CameraSysTrace.onEventSystrace("photoDevice.openCamera", true, true);
            String cameraId = info.getCameraId();
		//add by liangchangwei for AiWorks HDR
        if(!CameraUtil.MTKCAM_AIWORKS_HDR_SUPPORT) {
            //bv wuyonglin add for front camera can not support hdr 20200108 start
            LogHelper.i(TAG, "[openCamera] mIsHdrOpened : " + mIsHdrOpened + " cameraId =" + cameraId);
            if (cameraId.equals("0")) {
                String value = mDataStore.getValue("key_hdr", HDR_DEFAULT_VALUE, mDataStore.getGlobalScope());
                LogHelper.i(TAG, "[openCamera] value : " + value);
                if ("on".equals(value)) {
                    mIsHdrOpened = true;
                } else {
                    mIsHdrOpened = false;
                }
            } else {
                mIsHdrOpened = false;
            }
            //bv wuyonglin add for front camera can not support hdr 20200108 end

            //bv wuyonglin add for add hdr quickswitch 20191231 start
            if (!mIsHdrOpened) {
                //bv wuyonglin add for add hdr quickswitch 20191231 end
                boolean sync = info.getNeedOpenCameraSync();
                LogHelper.i(TAG, "[openCamera] cameraId : " + cameraId + ",sync = " + sync + " mNeedSubSectionInitSetting =" + mNeedSubSectionInitSetting);
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
                //bv wuyonglin add for add hdr quickswitch 20191231 start
            } else {
                LogHelper.i(TAG, "[openCamera] mIsHdrOpened : " + mIsHdrOpened);
                initSettingManager(info.getSettingManager());
                if (canOpenCamera(cameraId)) {
                    try {
                        mDeviceLock.tryLock(WAIT_TIME, TimeUnit.SECONDS);
                        mCurrentCameraId = cameraId;
                        updateCameraState(CameraState.CAMERA_OPENING);
                        mCameraDeviceManager.openCamera(mCurrentCameraId, mDeviceCallback, null);
                        mSettingManager.createAllSettings();
                        mCameraCharacteristics
                                = mCameraManager.getCameraCharacteristics(mCurrentCameraId);

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
                    } catch (CameraAccessException e) {
                        LogHelper.i(TAG, "[openCamera] initsettings  CameraAccessException ");
                        CameraUtil.showErrorInfoAndFinish(mActivity, CameraUtil.CAMERA_HARDWARE_EXCEPTION);
                        updateCameraState(CameraState.CAMERA_UNKNOWN);
                        mCurrentCameraId = null;
                    } finally {
                        mDeviceLock.unlock();
                    }
                }
                //bv wuyonglin add for add hdr quickswitch 20191231 end
            }
        } else {
            mHdrStatus = mDataStore.getValue("key_hdr", HDR_DEFAULT_VALUE, mDataStore.getGlobalScope());

            boolean sync = info.getNeedOpenCameraSync();
            LogHelper.i(TAG, "[openCamera] cameraId : " + cameraId + ",sync = " + sync + " mNeedSubSectionInitSetting =" + mNeedSubSectionInitSetting);
            if (canOpenCamera(cameraId)) {
                if (mHDRCaptureResult == null) {
                    if (cameraId.equals("1")) {
                        HDRConfig.configData(FrontCameraconfigJsonStr);
                        LogHelper.i(TAG,"HDRConfig.configData FrontCameraconfigJsonStr = " + FrontCameraconfigJsonStr);
                    } else if(cameraId.equals("0")){
                        HDRConfig.configData(BackCameraconfigJsonStr);
                        LogHelper.i(TAG,"HDRConfig.configData BackCameraconfigJsonStr = " + BackCameraconfigJsonStr);
                    } else if(cameraId.equals("2")){
                        HDRConfig.configData(WideCameraconfigJsonStr);
                        LogHelper.i(TAG,"HDRConfig.configData WideCameraconfigJsonStr = " + WideCameraconfigJsonStr);
                    }else{
                        LogHelper.i(TAG, "[openCamera] cameraId : " + cameraId + " is error!!");
                    }
                    mHDRCaptureResult = new HDRCaptureResult();
                }
                if (mHDRCaptureRequest == null) {
                    mHDRCaptureRequest = new HDRCaptureRequest();
                }

                //bv wuyonglin add for hd shot 20201013 start
		if (Config.isAisSupport(mActivity.getApplicationContext())) {
                if (mManager == null) {
                mManager = new MultiFrameCaptureManager(mActivity);
                }
                }
                //bv wuyonglin add for hd shot 20201013 end
                try {
                    mDeviceLock.tryLock(WAIT_TIME, TimeUnit.SECONDS);
                    mNeedSubSectionInitSetting = false;//info.getNeedFastStartPreview();
                    LogHelper.i(TAG," mNeedSubSectionInitSetting = " + mNeedSubSectionInitSetting);
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
            }
            //bv wuyonglin add for hd shot 20201013 start
            if (mCurrentCameraId != null) {
                mAisStatus = mDataStore.getValue("key_ais", "off", mDataStore.getCameraScope(Integer.parseInt(mCurrentCameraId)));
                if (Integer.parseInt(mCurrentCameraId) != 1) {
                    mFlashStatus = mDataStore.getValue("key_flash", FLASH_DEFAULT_VALUE, "_preferences_0");
                } else {
                    mFlashStatus = "off";
                }
            }
            LogHelper.i(TAG, "[openCamera] mHdrStatus : " + mHdrStatus + " cameraId =" + cameraId+" mFlashStatus ="+mFlashStatus+" mAisStatus ="+mAisStatus+" mCurrentCameraId ="+mCurrentCameraId);
            //bv wuyonglin add for hd shot 20201013 end
        }
        CameraSysTrace.onEventSystrace("photoDevice.openCamera", false, true);
	}
    }

    @Override
    public void updatePreviewSurface(Object surfaceObject) {
        LogHelper.i(TAG, "[updatePreviewSurface] surfaceHolder = " + surfaceObject + " state = "
                + mCameraState + ", session :" + mSession + ", mNeedSubSectionInitSetting:"
                + mNeedSubSectionInitSetting);
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
			    //add by liangchangwei for AiWorks HDR
                if(CameraUtil.MTKCAM_AIWORKS_HDR_SUPPORT){
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
                }else{
                    //bv wuyonglin add for add hdr quickswitch 20191231 start
                    if (!mIsHdrOpened) {
                        //bv wuyonglin add for add hdr quickswitch 20191231 end
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
                        //bv wuyonglin add for add hdr quickswitch 20191231 start
                    } else {
                        mSurfaceObject = surfaceObject;
                        if (surfaceObject == null) {
                            stopPreview();
                        } else {
                            configureSession(false);
                        }
                    }
                    //bv wuyonglin add for add hdr quickswitch 20191231 end
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
        String captureType = mSettingController.queryValue(HeifHelper.KEY_FORMAT);
        if (captureType == null) {
            captureType = HeifHelper.CAPTURE_TYPE_JPEG;
        }
        LogHelper.i(TAG, "[setPictureSize] get current captureType = " + captureType);
        int format = HeifHelper.getCaptureFormat(captureType);
        int rotation = CameraUtil.getJpegRotationFromDeviceSpec(
                Integer.parseInt(mCurrentCameraId), mJpegRotation, mActivity);
        HeifHelper.orientation = rotation;
        mIsPictureSizeChanged = mCaptureSurface.updatePictureInfo(size.getWidth(),
                size.getHeight(), format, CAPTURE_MAX_NUMBER, captureType);
        if(mIsPictureSizeChanged){
            //add by liangchangwei for AiWorks HDR
            if(CameraUtil.MTKCAM_AIWORKS_HDR_SUPPORT){
                mPhotoSize = new android.util.Size(size.getWidth(), size.getHeight());
                LogHelper.i(TAG,"setPictureSize mPhotoSize = " + mPhotoSize);
                if(mConfigHdrSettingsHandler != null){
                    mConfigHdrSettingsHandler.sendEmptyMessage(HDR_CAPTURERESULT_SET_PHOTOSIZE);
                }
            }
        }
        if (mIsBGServiceEnabled && BGServiceKeeper.supportByBGService(captureType)) {
            mBGServiceKeeper.setBGCaptureSurface(mCaptureSurface);
        }
        double ratio = (double) size.getWidth() / size.getHeight();
        ThumbnailHelper.updateThumbnailSize(ratio);
        if (ThumbnailHelper.isPostViewSupported()) {
            mThumbnailSurface.updatePictureInfo(ThumbnailHelper.getThumbnailWidth(),
                    ThumbnailHelper.getThumbnailHeight(),
                    ThumbnailHelper.IMAGE_BUFFER_FORMAT,
                    CAPTURE_MAX_NUMBER, ThumbnailHelper.CAPTURE_TYPE_THUMBNAIL);
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
        //bv wuyonglin add for add hdr quickswitch 20191231 start
        if (mCapturePostAlgoSurface != null) {
            mCapturePostAlgoSurface.release();
        }
        //bv wuyonglin add for add hdr quickswitch 20191231 end
        if (mOrientationListener != null) {
            mOrientationListener.setEnable(false);
            mOrientationListener = null;
        }
        //add by liangchangwei for AiWorks HDR
        if (CameraUtil.MTKCAM_AIWORKS_HDR_SUPPORT) {
            /*if (mHDRCaptureRequest != null) {
                mHDRCaptureRequest.destory();
            }*/
            LogHelper.i(TAG,"doCloseCamera mHDRCaptureResult.destory");
            mStart = false;
            //bv wuyonglin add for hd shot 20201013 start
            //bv wuyonglin add for bug3401 20210114 start
            unregisterSensorListener();
            //bv wuyonglin add for bug3401 20210114 end
            new Thread(){
                @Override
                public void run() {
                    super.run();
                    if (mManager != null) {
                        LogHelper.i(TAG,"doCloseCamera mManager.destory");
                        mManager.destory();
                        mManager = null;
                    }
                }
            }.start();
            //unregisterSensorListener();	//bv wuyonglin delete for bug2605 20201027
            //bv wuyonglin add for hd shot 20201013 end
            stopBackgroundThread();
        }
    }

    @Override
    public void startPreview() {
        LogHelper.i(TAG, "[startPreview]");
        configureSession(false);
    }

    @Override
    public void stopPreview() {
        LogHelper.i(TAG, "[stopPreview]");
        abortOldSession();
    }

    @Override
    public void takePicture(@Nonnull IDeviceController.CaptureDataCallback callback) {
        LogHelper.i(TAG, "[takePicture] mSession= " + mSession);
        CameraSysTrace.onEventSystrace("photoDevice.takePicture", true, true);
        if (mSession != null && mCamera2Proxy != null) {
            mCaptureDataCallback = callback;
            updateCameraState(CameraState.CAMERA_CAPTURING);
            mCameraID = Integer.parseInt(mCurrentCameraId);
            try {
			    //add by liangchangwei for AiWorks HDR
                if(CameraUtil.MTKCAM_AIWORKS_HDR_SUPPORT){
                    LogHelper.i(TAG,"takePicture mHdrStatus = " + mHdrStatus+" mStart ="+mStart + " AutoHDRStatus = " + AutoHDRStatus);
                    mExif = null;

                    //bv wuyonglin add for add hdr quickswitch 20191231 start
                    if (mHdrStatus.equals("off") && !(mAisStatus.equals("on") && mFlashStatus.equals("off"))) {	//bv wuyonglin modify for hd shot 20201013 start
                        //bv wuyonglin add for add hdr quickswitch 20191231 end
                        Builder builder = doCreateAndConfigRequest(Camera2Proxy.TEMPLATE_STILL_CAPTURE);
                        mSession.capture(builder.build(), mCaptureCallback, mModeHandler);
                        if (mIDeviceListener != null) {
                            mIDeviceListener.onTakePicture();
                        }
                        //bv wuyonglin add for add hdr quickswitch 20191231 start
                    } else {
                        if (mStart) {
                            return;
                        }
                        mStart = true ;

                        try{
                            mSession.stopRepeating();
                        }catch (CameraAccessException e){
                            LogHelper.e(TAG,"mSession.stopRepeating() Error!!");
                        }
                        if (OrientationEventListener.ORIENTATION_UNKNOWN == mOrientation) {
                            mJpegRotation = getJpegRotation(Integer.parseInt(mCurrentCameraId), 0);
                        } else {
                            mJpegRotation = getJpegRotation(Integer.parseInt(mCurrentCameraId), mOrientation);
                        }
                        try{
                            LogHelper.i(TAG, "captureStillPicture mHdrCaptureRequest.createCaptureRequest HDRInit = " + HDRInit);
                            //bv wuyonglin add for hd shot 20201013 start
                            if (!mHdrStatus.equals("off")) {
                                //bv wuyonglin add for hd shot 20201013 end
                                ArrayList<Builder> builders = mHDRCaptureRequest.createCaptureRequest(mCameraDevice);
                                ArrayList<CaptureRequest> requests = new ArrayList<>();

                                if(mHdrStatus.equals("auto")&&!AutoHDRStatus /*|| HDRInit == false*/){
                                    CaptureRequest request = null;
                                    for (CaptureRequest.Builder builder : builders) {
                                        builder.addTarget(mImageReader.getSurface());
                                        request = builder.build();
                                        //bv wuyonglin add for hdr auto take photo have noise 20210604 start
                                        requests.add(request);
                                        break;
                                        //bv wuyonglin add for hdr auto take photo have noise 20210604 end
                                    }
                                }else{
                                    for (CaptureRequest.Builder builder : builders) {
                                        builder.addTarget(mImageReader.getSurface());
                                        CaptureRequest request = builder.build();
                                        requests.add(request);
                                    }
                                }
                                LogHelper.i(TAG, "captureStillPicture mHDRCaptureRequest.getPhotoForamt mJpegRotation = " + mJpegRotation + " requests.size() = " + requests.size());

                                mImageReader.setOnImageAvailableListener(mHDRCaptureResult.getImageAvailableListener(), mCaptureHandler);
                                mHDRCaptureResult.onStartCapture(requests.size(), mHDRCaptureRequest.getPhotoForamt(), mJpegRotation);
                                if (requests.size() == 1) {
                                    mSession.capture(requests.get(0), mHDRCaptureResult.getCaptureCallback(), mModeHandler);
                                } else {
                                    mSession.captureBurst(requests, mHDRCaptureResult.getCaptureCallback(), mModeHandler);
                                }
                                LogHelper.i(TAG,"mSession.captureBurst");
                                if (mIDeviceListener != null) {
                                    LogHelper.i(TAG,"mIDeviceListener.onTakePicture()");
                                    mIDeviceListener.onTakePicture();
                                }
                                //bv wuyonglin add for hd shot 20201013 start
                            } else {
                                ArrayList<CaptureRequest.Builder> builders = mManager.createCaptureRequest(mCameraDevice);
                                ArrayList<CaptureRequest> requests = new ArrayList<>();
                                for (CaptureRequest.Builder builder : builders) {
                                    builder.addTarget(mImageReader.getSurface());
                                    CaptureRequest request = builder.build();
                                    requests.add(request);
                                }
                                LogHelper.i(TAG, "mManager captureStillPicture requests ="+requests.size());
                                    mImageReader.setOnImageAvailableListener(mManager.getImageAvailableListener(), mCaptureHandler);
                                    mManager.onStartCapture(requests.size(), mManager.getPhotoForamt(), mJpegRotation);
                                if (requests.size() == 1) {
                                    mSession.capture(requests.get(0), mManager.getCaptureCallback(), mModeHandler);
                                } else {
                                    mSession.captureBurst(requests, mManager.getCaptureCallback(), mModeHandler);
                                }
                                LogHelper.i(TAG,"mManager1 mSession.captureBurst");
                                if (mIDeviceListener != null) {
                                    LogHelper.i(TAG,"mIDeviceListener.onTakePicture()");
                                    mIDeviceListener.onTakePicture();
                                }
                            }
                            //bv wuyonglin add for hd shot 20201013 end
                        }catch (CameraAccessException e) {
                            e.printStackTrace();
                            LogHelper.i(TAG, "[takePicture] error because create build fail.");
                        }
                    }
                    //bv wuyonglin add for add hdr quickswitch 20191231 end
                }else{
                    //bv wuyonglin add for add hdr quickswitch 20191231 start
                    if (!mIsHdrOpened) {
                        //bv wuyonglin add for add hdr quickswitch 20191231 end
                        Builder builder = doCreateAndConfigRequest(Camera2Proxy.TEMPLATE_STILL_CAPTURE);
                        mSession.capture(builder.build(), mCaptureCallback, mModeHandler);
                        if (mIDeviceListener != null) {
                            mIDeviceListener.onTakePicture();
                        }
                        //bv wuyonglin add for add hdr quickswitch 20191231 start
                    } else {
                        // Config jpeg orientation for postAlgo
                        FeatureParam params = new FeatureParam();
                        int rotation = CameraUtil.getJpegRotationFromDeviceSpec(
                                Integer.parseInt(mCurrentCameraId), mJpegRotation, mActivity);
                        params.appendInt(POSTALGO_PARAMS_JPEG_ORIENTATION_KEY, rotation);
                        //bv wuyonglin add for hdr open take photo picture have mirror 20200713 start
                        params.appendInt(MTK_POSTALGO_MIRROR, 1);
                        //bv wuyonglin add for hdr open take photo picture have mirror 20200713 end
                        //mICameraContext.getCamPostAlgo().configParams(FeaturePipeConfig.INDEX_CAPTURE, params);
                        mCaptureNum = CAPTURE_REQUEST_NUM;
                        // 3 capture request with lock AE and EV 0,-1,1
                        List<CaptureRequest> captureRequests = new ArrayList<>();
                        Builder builder = doCreateAndConfigRequest(Camera2Proxy.TEMPLATE_STILL_CAPTURE);
                        builder.set(CaptureRequest.CONTROL_AE_LOCK, true);
                        builder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, 0);
                        captureRequests.add(builder.build());
                        builder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, -2);
                        captureRequests.add(builder.build());
                        builder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, 2);
                        captureRequests.add(builder.build());
                        LogHelper.i(TAG, "[takePicture hdrOpened] captureBurst= " + captureRequests.size());
                        mSession.captureBurst(captureRequests, mCaptureCallback, mModeHandler);
                        // 3 preview request with lock AE
                        List<CaptureRequest> previewRequests = new ArrayList<>();
                        Builder previewBuilder = doCreateAndConfigRequest(Camera2Proxy.TEMPLATE_PREVIEW);
                        previewBuilder.set(CaptureRequest.CONTROL_AE_LOCK, true);
                        for (int i = 0; i <= CAPTURE_REQUEST_SIZE_BY_ALGO - 1; i++) {
                            previewRequests.add(previewBuilder.build());
                        }
                        mSession.captureBurst(previewRequests, mCaptureCallback, mModeHandler);
                        // 1 preview request with unlock AE
                        previewBuilder.set(CaptureRequest.CONTROL_AE_LOCK, false);
                        mSession.capture(previewBuilder.build(), mCaptureCallback, mModeHandler);
                    }
                    //bv wuyonglin add for add hdr quickswitch 20191231 end
                }
            } catch (CameraAccessException e) {
                e.printStackTrace();
                LogHelper.e(TAG, "[takePicture] error because create build fail.");
            }
        }
        CameraSysTrace.onEventSystrace("photoDevice.takePicture", false, true);
    }

    @Override
    public void updateGSensorOrientation(int orientation) {
        //add by liangchangwei for AiWorks HDR
        if(orientation==-1){
            mJpegRotation = 0;
        }else{
            mJpegRotation = orientation;
        }
        LogHelper.i(TAG,"updateGSensorOrientation mJpegRotation = " + mJpegRotation);
        //add by liangchangwei for AiWorks HDR
    }

    @Override
    public void closeSession() {
        if (mSession != null) {
            try {
                mSession.abortCaptures();
                mSession.close();
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
        synchronized (CameraApplication.class) {
            LogHelper.i(TAG, "[closeCamera] + sync = " + sync + " current state : " + mCameraState);
            CameraSysTrace.onEventSystrace("photoDevice.closeCamera", true, true);
            if (CameraState.CAMERA_UNKNOWN != mCameraState) {
                try {
                    mDeviceLock.tryLock(WAIT_TIME, TimeUnit.SECONDS);
                    super.doCameraClosed(mCamera2Proxy);
                    updateCameraState(CameraState.CAMERA_CLOSING);
                    abortOldSession();
                    LogHelper.d(TAG, "[closeCamera] mModeDeviceCallback is null = " +(mModeDeviceCallback == null));
                    if (mModeDeviceCallback != null) {
                        mModeDeviceCallback.beforeCloseCamera();
                    }
                    LogHelper.d(TAG, "[closeCamera] doCloseCamera(sync) +");
                    doCloseCamera(sync);
                    LogHelper.d(TAG, "[closeCamera] doCloseCamera(sync) -");
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
            /*add by bv liangchangwei for HDR shot init 20200902 start  */
            if (mHDRCaptureResult != null) {
                //mHDRCaptureResult.setCaptureCallback(null);
                if (isInit) {
                    mHDRCaptureResult.destory();
                    isInit = false;
                }
            }
            /*add by bv liangchangwei for HDR shot init 20200902 end  */
            mHDRCaptureRequest = null;
            mHDRCaptureResult = null;
            if (mOrientationListener != null) {
                mOrientationListener.setEnable(false);
                mOrientationListener = null;
            }
            unregisterSensorListener();
            if (mManager != null) {
                LogHelper.i(TAG,"CloseCamera mManager.destory");
                mManager.destory();
                mManager = null;
            }
            CameraSysTrace.onEventSystrace("photoDevice.closeCamera", false, true);
            LogHelper.i(TAG, "[closeCamera] -");
        }
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
        if(CameraUtil.MTKCAM_AIWORKS_HDR_SUPPORT){
            if(mIsPictureSizeChanged){
                //bv wuyonglin add for ais take picture have green 20201116 start
                if(mManager != null){
                    mManager.init(mActivity, mPhotoSize, mPreviewSize, null, null, PhotoDevice2Controller.this);
                }
                //bv wuyonglin add for ais take picture have green 20201116 end
                if(mConfigHdrSettingsHandler != null){
                    mConfigHdrSettingsHandler.sendEmptyMessage(HDR_CAPTURERESULT_SET_PHOTOSIZE);
                }
            }
        }
        return new Size(mPreviewWidth, mPreviewHeight);
    }

    @Override
    public void onPictureCallback(byte[] data,
                                  int format, String captureType, int width, int height) {
        LogHelper.d(TAG, "<onPictureCallback> data = " + data + ", format = " + format
                + ", captureType = " + captureType + ", width = " + width + ", height = " + height
                + ", mCaptureDataCallback = " + mCaptureDataCallback);
        if (mCaptureDataCallback != null) {
            DataCallbackInfo info = new DataCallbackInfo();
            info.data = data;
            info.needUpdateThumbnail = true;
            info.needRestartPreview = false;
            info.mBufferFormat = format;
            info.imageHeight = height;
            info.imageWidth = width;
		    //add by liangchangwei for AiWorks HDR
            if(CameraUtil.MTKCAM_AIWORKS_HDR_SUPPORT){
/*
                if("jpeg".equalsIgnoreCase(captureType) && (CameraUtil.isWaterMarkOn(mDataStore,mActivity))){
                    byte[] captureYuv;
                    try {
                        mExif = new ExifInterface();
                        mExif.readExif(data);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    captureYuv = YuvEncodeJni.getInstance().Jpeg2Nv21(data, width, height, 1, 0, false);

                    int rotation = CameraUtil.getJpegRotationFromDeviceSpec(
                            Integer.parseInt(mCurrentCameraId), mJpegRotation, mActivity);
                    LogHelper.i(TAG,"onPictureCallback format = " + format + " EncodeYuvToJpeg start!! mJpegRotation = " + mJpegRotation + " rotation = " + rotation);
                    LogHelper.i(TAG,"isWaterMarkOn on + ");
                    if(mJpegRotation%360 == 90 || mJpegRotation%360 == 270){
                        WaterMarkUtil.yuvAddWaterMark(mActivity,captureYuv, width, height, 0);
                    }else{
                        WaterMarkUtil.yuvAddWaterMark(mActivity,captureYuv, height, width, 0);
                    }
                    LogHelper.i(TAG,"isWaterMarkOn on - ");

                    if(mJpegRotation%360 == 90 || mJpegRotation%360 == 270){
                        data = YuvEncodeJni.getInstance().EncodeYuvToJpeg(captureYuv, ImageFormat.NV21,
                                width, height, 95, 0);
                    }else{
                        data = YuvEncodeJni.getInstance().EncodeYuvToJpeg(captureYuv, ImageFormat.NV21,
                                height, width, 95, 0);
                    }
                    info.data = data;
                    info.mBufferFormat = ImageFormat.JPEG;
                    LogHelper.i(TAG,"onPictureCallback format = " + format + " EncodeYuvToJpeg Done ");
                }
*/
                //bv wuyonglin add for add hdr quickswitch 20191231 start
                if (mHdrStatus.equals("off")) {
                    //bv wuyonglin add for add hdr quickswitch 20191231 end
                    if (ThumbnailHelper.isPostViewSupported()) {
                        info.needUpdateThumbnail = false;
                    }
                    if (ThumbnailHelper.CAPTURE_TYPE_THUMBNAIL.equalsIgnoreCase(captureType)) {
                        CameraSysTrace.onEventSystrace("photoDevice.onPostViewCallback", true, true);
                        mCaptureDataCallback.onPostViewCallback(data);
                        CameraSysTrace.onEventSystrace("photoDevice.onPostViewCallback", false, true);
                    } else {
                        CameraSysTrace.onEventSystrace("photoDevice.onJpegCallback", true, true);
                        /*-- add by bv liangchangwei for waterMark modify--*/
                        if(format == ImageFormat.YUV_420_888){
                            int rotation = CameraUtil.getJpegRotationFromDeviceSpec(
                                    mCameraID, mJpegRotation, mActivity);
                            LogHelper.i(TAG,"rotation = " + rotation + " mJpegRotation = " + mJpegRotation);
                            if(CameraUtil.isWaterMarkOn(mDataStore,mActivity)){
                                LogHelper.i(TAG,"isWaterMarkOn on + ");
                                WaterMarkUtil.yuvAddWaterMark(mActivity,data, width, height, rotation);
                                LogHelper.i(TAG,"isWaterMarkOn on - ");
                            }else{
                                LogHelper.i(TAG,"isWaterMarkOn off");
                            }
                            data = YuvEncodeJni.getInstance().EncodeYuvToJpeg(data, ImageFormat.NV21,
                                    width, height, 95, rotation);
                            info.data = data;
                            info.mBufferFormat = ImageFormat.JPEG;
                            LogHelper.i(TAG,"onPictureCallback format = " + format + " EncodeYuvToJpeg Done ");
                        }
                        /*-- add by bv liangchangwei for waterMark modify--*/
                        mCaptureDataCallback.onDataReceived(info);
                        CameraSysTrace.onEventSystrace("photoDevice.onJpegCallback", false, true);
                        boolean supportByBGService
                        = BGServiceKeeper.supportByBGService(captureType);
                        if (mIsBGServiceEnabled && mCaptureSurface != null && supportByBGService) {
                            mCaptureSurface.decreasePictureNum();
                            if (mCaptureSurface.shouldReleaseCaptureSurface()
                                    && mCaptureSurface.getPictureNumLeft() == 0) {
                                mCaptureSurface.releaseCaptureSurface();
                                mCaptureSurface.releaseCaptureSurfaceLater(false);
                            }
                        }
                        //add by huangfei for continuousshot number start
                        mCaptureDataCallback = null;
                        //add by huangfei for continuousshot number end
                    }
                    //bv wuyonglin add for add hdr quickswitch 20191231 start
                } else {
                    /*if (!ThumbnailHelper.FORMAT_TAG.equalsIgnoreCase(captureType)) {
                        updateCameraState(CameraState.CAMERA_OPENED);
                        mModeDeviceCallback.onPreviewCallback(null, 0);
                        mCaptureDataCallback.onDataReceived(info);
                        if (mIsBGServiceEnabled && mCaptureSurface != null) {
                            mCaptureSurface.decreasePictureNum();
                            if (mCaptureSurface.shouldReleaseCaptureSurface()
                                    && mCaptureSurface.getPictureNumLeft() == 0) {
                                mCaptureSurface.releaseCaptureSurface();
                                mCaptureSurface.releaseCaptureSurfaceLater(false);
                            }
                        }
                    }*/
                }
                //bv wuyonglin add for add hdr quickswitch 20191231 end
            }else{
                //bv wuyonglin add for add hdr quickswitch 20191231 start
                if (!mIsHdrOpened) {
                    //bv wuyonglin add for add hdr quickswitch 20191231 end
                    if (ThumbnailHelper.isPostViewSupported()) {
                        info.needUpdateThumbnail = false;
                    }
                    if (ThumbnailHelper.FORMAT_TAG.equalsIgnoreCase(captureType)) {
                        CameraSysTrace.onEventSystrace("photoDevice.onPostViewCallback", true, true);
                        mCaptureDataCallback.onPostViewCallback(data);
                        CameraSysTrace.onEventSystrace("photoDevice.onPostViewCallback", false, true);
                    } else {
                        CameraSysTrace.onEventSystrace("photoDevice.onJpegCallback", true, true);
                        mCaptureDataCallback.onDataReceived(info);
                        CameraSysTrace.onEventSystrace("photoDevice.onJpegCallback", false, true);
                boolean supportByBGService
                        = BGServiceKeeper.supportByBGService(captureType);
                        if (mIsBGServiceEnabled && mCaptureSurface != null && supportByBGService) {
                            mCaptureSurface.decreasePictureNum();
                            if (mCaptureSurface.shouldReleaseCaptureSurface()
                                    && mCaptureSurface.getPictureNumLeft() == 0) {
                                mCaptureSurface.releaseCaptureSurface();
                                mCaptureSurface.releaseCaptureSurfaceLater(false);
                            }
                        }
                        //add by huangfei for continuousshot number start
                        mCaptureDataCallback = null;
                        //add by huangfei for continuousshot number end
                    }
                    //bv wuyonglin add for add hdr quickswitch 20191231 start
                } else {
                    if (!ThumbnailHelper.FORMAT_TAG.equalsIgnoreCase(captureType)) {
                        updateCameraState(CameraState.CAMERA_OPENED);
                        mModeDeviceCallback.onPreviewCallback(null, 0);
                        mCaptureDataCallback.onDataReceived(info);
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
                //bv wuyonglin add for add hdr quickswitch 20191231 end
            }
        }
    }

    @Override
    public void createAndChangeRepeatingRequest() {
        LogHelper.d(TAG, "Hdr createAndChangeRepeatingRequest mCamera2Proxy ="+mCamera2Proxy);
        if (mCamera2Proxy == null || mCameraState != CameraState.CAMERA_OPENED) {
            LogHelper.e(TAG, "camera is closed or in opening state can't request ");
            return;
        }
        LogHelper.d(TAG, "Hdr createAndChangeRepeatingRequest to repeatingPreview true");
        //bv wuyonglin add for hd shot 20201013 start
        mAisStatus = mDataStore.getValue("key_ais", "off", mDataStore.getCameraScope(Integer.parseInt(mCurrentCameraId)));
        if (mCurrentCameraId != null && !mCurrentCameraId.equals("1")) {
            mFlashStatus = mDataStore.getValue("key_flash", FLASH_DEFAULT_VALUE, "_preferences_0");
        } else {
            mFlashStatus = "off";
        }
        //bv wuyonglin add for hd shot 20201013 end
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
        //bv wuyonglin add for add hdr quickswitch 20191231 start
        LogHelper.e(TAG, "requestRestartSession");
        mRequestRestartSession = true;
        //bv wuyonglin add for add hdr quickswitch 20191231 end
        //add by liangchangwei for AiWorks HDR
        if (CameraUtil.MTKCAM_AIWORKS_HDR_SUPPORT) {
            mHdrStatus = mDataStore.getValue("key_hdr", HDR_DEFAULT_VALUE, mDataStore.getGlobalScope());
            //bv wuyonglin add for hd shot 20201013 start
            if (mCurrentCameraId != null) {
	        mAisStatus = mDataStore.getValue("key_ais", "off", mDataStore.getCameraScope(Integer.parseInt(mCurrentCameraId)));
            }
            if (mCurrentCameraId != null && !mCurrentCameraId.equals("1")) {
	        mFlashStatus = mDataStore.getValue("key_flash", FLASH_DEFAULT_VALUE, "_preferences_0");
            } else {
	        mFlashStatus = "off";
            }
            //bv wuyonglin add for hd shot 20201013 end
        }
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
            LogHelper.d(TAG, "[releaseJpegCaptureSurface] " +
                    "mCaptureSurface.getPictureNumLeft() = "+ mCaptureSurface.getPictureNumLeft());
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
        if (mPreviewSurface == null) {
            LogHelper.i(TAG, "[configureSession] +" + ", return mPreviewSurface :" + mPreviewSurface);
            return ;
        }
        //bv wuyonglin add for add hdr quickswitch 20191231 start
        String value = mDataStore.getValue("key_hdr", HDR_DEFAULT_VALUE, mDataStore.getGlobalScope());
        LogHelper.d(TAG, "[configureSession] Hdr value =" + value + " mRequestRestartSession =" + mRequestRestartSession);
        //bv wuyonglin add for add hdr quickswitch 20191231 end
        mDeviceLock.lock();
        mFirstFrameArrived = false;
        try {
            if (mCamera2Proxy != null) {
                abortOldSession();
                //bv wuyonglin add for switcher to front camera take photo, repeat this operation twice then take picture more times happened exception 20200303 start
                mCaptureSurface.releaseCaptureSurfaceLater(false);
                //bv wuyonglin add for switcher to front camera take photo, repeat this operation twice then take picture more times happened exception 20200303 end
                //bv wuyonglin add for add hdr quickswitch 20191231 start
                //add by liangchangwei for AiWorks HDR
                if(!CameraUtil.MTKCAM_AIWORKS_HDR_SUPPORT){
                    if (mRequestRestartSession) {
                        //bv wuyonglin delete for switcher to front camera take photo, repeat this operation twice then take picture more times happened exception 20200303 start
                        //mCaptureSurface.releaseCaptureSurfaceLater(false);
                        //bv wuyonglin delete for switcher to front camera take photo, repeat this operation twice then take picture more times happened exception 20200303 end
                        if ("on".equals(value)) {
                            mIsHdrOpened = true;
                            mIsBGServiceEnabled = false;
                            LogHelper.d(TAG, "[configureSession] fisrt Hdr mCaptureSurface =" + mCaptureSurface + " mCaptureSurface.getSurface() =" + mCaptureSurface.getSurface());
                            mCaptureSurface = new CaptureSurface();
                            updatePictureSize();
                            LogHelper.d(TAG, "[configureSession] end Hdr value =" + value + " mCaptureSurface =" + mCaptureSurface + " mCaptureSurface.getSurface() =" + mCaptureSurface.getSurface());
                        } else {
                            LogHelper.d(TAG, "[configureSession] fisrt BG mCaptureSurface =" + mCaptureSurface);
                            mCaptureSurface = new CaptureSurface(mBGServiceKeeper.getBGCaptureHandler());
                            mIsHdrOpened = false;
                            mIsBGServiceEnabled = true;
                            updatePictureSize();
                            LogHelper.d(TAG, "[configureSession] end Hdr BG value =" + value + " mCaptureSurface =" + mCaptureSurface + " mCaptureSurface.getSurface() =" + mCaptureSurface.getSurface());
                        }
                        mRequestRestartSession = false;
                    }

                    if (!mIsHdrOpened) {
                    //bv wuyonglin add for add hdr quickswitch 20191231 end
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
                            mSettingDevice2Configurator.configCaptureRequest(mBuilder);
                            configureQuickPreview(mBuilder);
                            configureBGService(mBuilder);
                            configurePlatformCamera(mBuilder);
                            configDemoSettings(mBuilder);
                            CameraSysTrace.onEventSystrace("configureSession.createCaptureSession.fromopen", true, true);
                            mCamera2Proxy.createCaptureSession(mSessionCallback,
                                    mModeHandler, mBuilder, mOutputConfigs);
                            CameraSysTrace.onEventSystrace("configureSession.createCaptureSession.fromopen", false, true);
                            mIsPictureSizeChanged = false;
                            return;
                        }
                    //bv wuyonglin add for add hdr quickswitch 20191231 start
                    }
                }else{
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
                        LogHelper.i(TAG,"mHdrStatus = " + mHdrStatus);
                        mBuilder = getDefaultPreviewBuilder();
                        LogHelper.i(TAG," getDefaultPreviewBuilder ");
                        mSettingDevice2Configurator.configCaptureRequest(mBuilder);
                        configureQuickPreview(mBuilder);
                        configureBGService(mBuilder);
                        configurePlatformCamera(mBuilder);
                        configDemoSettings(mBuilder);
                        CameraSysTrace.onEventSystrace("configureSession.createCaptureSession.fromopen", true, true);
                        mCamera2Proxy.createCaptureSession(mSessionCallback,
                                mModeHandler, mBuilder, mOutputConfigs);
                        CameraSysTrace.onEventSystrace("configureSession.createCaptureSession.fromopen", false, true);
                        mIsPictureSizeChanged = false;
                        return;
                    }
                    //bv wuyonglin add for add hdr quickswitch 20191231 start
                }
                //bv wuyonglin add for add hdr quickswitch 20191231 end
                List<Surface> surfaces = new LinkedList<>();
                //bv wuyonglin add for add hdr quickswitch 20191231 start
				//add by liangchangwei for AiWorks HDR
                //bv wuyonglin add for hd shot 20201013 start
                mPreviewReaderSurface = mPreviewReader.getSurface();
                //bv wuyonglin add for hd shot 20201013 end
                if(CameraUtil.MTKCAM_AIWORKS_HDR_SUPPORT){
                    LogHelper.i(TAG, "[configureSession] launchCameraisClosed mCameraDevice= : " + mCameraDevice+" mCamera2Proxy ="+mCamera2Proxy+" isCameraClosed() ="+mCamera2Proxy.getRequestHandler().isCameraClosed());
                    if(value.equals("on")||value.equals("auto")){
                        if(mCameraDevice != null && mHDRCaptureRequest != null && !mCamera2Proxy.getRequestHandler().isCameraClosed()){	//bv wuyonglin add for bug3401 20210123
                            mPreviewRequestBuilder = mHDRCaptureRequest.createPreviewRequest(mCameraDevice);
                            if (mPreviewRequestBuilder != null) {
                            mPreviewRequestBuilder.addTarget(mPreviewSurface);
                            //mPreviewReaderSurface = mPreviewReader.getSurface();	//bv wuyonglin delete for hd shot 20201013
                            mPreviewRequestBuilder.addTarget(mPreviewReaderSurface);
                            if(mZoomConfig != null && mZoomConfig.getCurZoomRatio() != 1.0f){
                                //bv wuyonglin add for bug5276 20210415 start
                                //mPreviewRequestBuilder.set(SCALER_CROP_REGION, mHDRCaptureRequest.getScalerCropRegion());
                                mPreviewRequestBuilder.set(CaptureRequest.SCALER_CROP_REGION, mSensorRect);
                                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_ZOOM_RATIO,mZoomConfig.getCurZoomRatio());
                                //bv wuyonglin add for bug5276 20210415 end
                            }
			    }
                            mNeedFinalizeOutput = false;
                            surfaces = Arrays.asList(mPreviewSurface, mImageReader.getSurface(), mPreviewReaderSurface);
                            mSettingDevice2Configurator.configSessionSurface(surfaces);
                            LogHelper.i(TAG, "[configureSession] surface size : " + surfaces.size());
                            mBuilder = doCreateAndConfigRequest(Camera2Proxy.TEMPLATE_PREVIEW);
                            //bv wuyonglin add for hdr preview 20201118 start
                            if (mBuilder != null) {
                            mBuilder.addTarget(mPreviewReaderSurface);
                            }
                            //bv wuyonglin add for hdr preview 20201118 end
                            CameraSysTrace.onEventSystrace("configureSession.createCaptureSession", true, true);
                            mCamera2Proxy.createCaptureSession(surfaces, mSessionCallback,
                                    mModeHandler, mBuilder);
                            CameraSysTrace.onEventSystrace("configureSession.createCaptureSession", false, true);
                            mIsPictureSizeChanged = false;
                        }
                    //bv wuyonglin add for hd shot 20201013 start
                    } else {
		                if (mAisStatus.equals("on") && mFlashStatus.equals("off")) {
                            LogHelper.i(TAG, "mManager [configureSession] mCameraDevice : " + mCameraDevice+" mManager ="+mManager);
                            if (mCameraDevice != null && mManager != null && !mCamera2Proxy.getRequestHandler().isCameraClosed()) {	//bv wuyonglin add for bug3401 20210123
                                mPreviewRequestBuilder = mManager.createPreviewRequest(mCameraDevice);
                            if (mPreviewRequestBuilder != null) {
                                mPreviewRequestBuilder.addTarget(mPreviewSurface);
                                //mPreviewReaderSurface = mPreviewReader.getSurface();
                                mPreviewRequestBuilder.addTarget(mPreviewReaderSurface);
                                if(mZoomConfig != null && mZoomConfig.getCurZoomRatio() != 1.0f){
                                    LogHelper.i(TAG, "[mManager] mManager.getScalerCropRegion() : " + mManager.getScalerCropRegion() + " mZoomConfig.getCurZoomRatio() = " + mZoomConfig.getCurZoomRatio());
                                    //bv wuyonglin add for bug5276 20210415 start
                                    //mPreviewRequestBuilder.set(SCALER_CROP_REGION, mManager.getScalerCropRegion());
                                    mPreviewRequestBuilder.set(CaptureRequest.SCALER_CROP_REGION, mSensorRect);
                                    mPreviewRequestBuilder.set(CaptureRequest.CONTROL_ZOOM_RATIO,mZoomConfig.getCurZoomRatio());
                                    //bv wuyonglin add for bug5276 20210415 end
                                }
                            }
                                mNeedFinalizeOutput = false;
                                surfaces = Arrays.asList(mPreviewSurface, mImageReader.getSurface(), mPreviewReaderSurface);
                                mSettingDevice2Configurator.configSessionSurface(surfaces);
                                LogHelper.i(TAG, "mManager [configureSession] surface size : " + surfaces.size());
                                mBuilder = doCreateAndConfigRequest(Camera2Proxy.TEMPLATE_PREVIEW);
                                //bv wuyonglin add for hdshot preview 20201118 start
				if (mBuilder != null) {
                                mBuilder.addTarget(mPreviewReaderSurface);
				}
                                //bv wuyonglin add for hdshot preview 20201118 end
                                CameraSysTrace.onEventSystrace("configureSession.createCaptureSession", true, true);
                                mCamera2Proxy.createCaptureSession(surfaces, mSessionCallback,
                                        mModeHandler, mBuilder);
                                CameraSysTrace.onEventSystrace("configureSession.createCaptureSession", false, true);
                                mIsPictureSizeChanged = false;
                            }
                        //bv wuyonglin add for hd shot 20201013 end
                        }else{
                            surfaces.add(mPreviewSurface);
                            surfaces.add(mCaptureSurface.getSurface());
                            if (ThumbnailHelper.isPostViewSupported()) {
                                surfaces.add(mThumbnailSurface.getSurface());
                            }
                            mNeedFinalizeOutput = false;
                            mSettingDevice2Configurator.configSessionSurface(surfaces);
                            LogHelper.i(TAG, "[configureSession] surface size : " + surfaces.size());
                            mBuilder = doCreateAndConfigRequest(Camera2Proxy.TEMPLATE_PREVIEW);
                            CameraSysTrace.onEventSystrace("configureSession.createCaptureSession", true, true);
                            mCamera2Proxy.createCaptureSession(surfaces, mSessionCallback,
                                    mModeHandler, mBuilder);
                            CameraSysTrace.onEventSystrace("configureSession.createCaptureSession", false, true);
                            mIsPictureSizeChanged = false;
                        }
                    }
                }else{
                    if (mIsHdrOpened) {
                        //configPostAlgo();
                    }
                    //bv wuyonglin add for add hdr quickswitch 20191231 end
                    surfaces.add(mPreviewSurface);
                    //bv wuyonglin modify for add hdr quickswitch 20191231 start
                    if (!mIsHdrOpened) {
                        surfaces.add(mCaptureSurface.getSurface());
                    } else {
                        surfaces.add(mCapturePostAlgoSurface);
                    }
                    //bv wuyonglin modify for add hdr quickswitch 20191231 end
                    if (ThumbnailHelper.isPostViewSupported()) {
                        surfaces.add(mThumbnailSurface.getSurface());
                    }
                    //bv wuyonglin modify for add hdr quickswitch 20191231 start
                    if (!mIsHdrOpened) {
                        mNeedFinalizeOutput = false;
                    }
                    //bv wuyonglin modify for add hdr quickswitch 20191231 end
                    mSettingDevice2Configurator.configSessionSurface(surfaces);
                    LogHelper.d(TAG, "[configureSession] surface size : " + surfaces.size());
                    mBuilder = doCreateAndConfigRequest(Camera2Proxy.TEMPLATE_PREVIEW);
                    CameraSysTrace.onEventSystrace("configureSession.createCaptureSession", true, true);
                    mCamera2Proxy.createCaptureSession(surfaces, mSessionCallback,
                            mModeHandler, mBuilder);
                    CameraSysTrace.onEventSystrace("configureSession.createCaptureSession", false, true);
                    mIsPictureSizeChanged = false;
                }
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

        //add by huang fei disable setting items start
        if(Config.isZsdOnAndHide(mActivity)){
            Relation relation = PhotoRestriction.getRestriction().getRelation("on", true);
            mSettingManager.getSettingController().postRestriction(relation);
        }
        //add by huang fei disable setting items end

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

    private void configurePlatformCamera(Builder builder) {
        if (mCurrentCameraId != null) {
            DeviceDescription deviceDescription = CameraApiHelper.getDeviceSpec(
                    mActivity.getApplicationContext())
                    .getDeviceDescriptionMap()
                    .get(mCurrentCameraId);
            CaptureRequest.Key<int[]> keyPlatformCamera = deviceDescription.getKeyPlatformCamera();
            LogHelper.d(TAG, "configurePlatformCamera keyPlatformCamera:" + keyPlatformCamera);
            if (keyPlatformCamera != null) {
                int[] value = new int[1];
                value[0] = 1;
                builder.set(keyPlatformCamera, value);
            }
        }
    }
    private void configureBGService(Builder builder) {
        boolean supportbyBGService
                = BGServiceKeeper.supportByBGService(mCaptureSurface.getCaptureType());
        if (mIsBGServiceEnabled && supportbyBGService) {
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

    private void configDemoSettings(Builder builder) {
        if (mCurrentCameraId != null) {
            DeviceDescription deviceDescription = CameraApiHelper.getDeviceSpec(
                    mActivity.getApplicationContext())
                    .getDeviceDescriptionMap()
                    .get(mCurrentCameraId);
            CaptureRequest.Key<int[]> keyDemoFb = deviceDescription.getKeyTpiFb();
            if (keyDemoFb != null) {
                int[] value = new int[1];
                int valueProp = SystemProperties.getInt("vendor.mtk.camera.app.demofb", -1);
                value[0] = valueProp;
                if (valueProp != -1){
                    builder.set(keyDemoFb, value);
                    LogHelper.d(TAG, "[configDemoSettings] demofb 0");
                }
            }
            CaptureRequest.Key<int[]> keyDemoAsd = deviceDescription.getKeyTpiAsync();
            if (keyDemoAsd != null) {
                int[] value = new int[1];
                int valueProp = SystemProperties.getInt("vendor.mtk.camera.app.demoasd", -1);
                value[0] = valueProp;
                if (valueProp != -1){
                    builder.set(keyDemoAsd, value);
                    LogHelper.d(TAG, "[configDemoSettings] demoasd 0");
                }
            }
            CaptureRequest.Key<int[]> keyDemoEis = deviceDescription.getKeyTpiEis();
            if (keyDemoEis != null) {
                int[] value = new int[1];
                int valueProp = SystemProperties.getInt("vendor.mtk.camera.app.demoeis", -1);
                value[0] = valueProp;
                if (valueProp != -1){
                    builder.set(keyDemoEis, value);
                    LogHelper.d(TAG, "[configDemoSettings] demoeis 0");
                }
            }
        }
    }
    private void repeatingPreview(boolean needConfigBuiler) {
        LogHelper.i(TAG, "[repeatingPreview] mSession =" + mSession + " mCamera =" +
                mCamera2Proxy + ",needConfigBuiler " + needConfigBuiler);
        CameraSysTrace.onEventSystrace("photoDevice.repeatingPreview", true, true);
        if (mSession != null && mCamera2Proxy != null) {
            try {
				//add by liangchangwei for AiWorks HDR
                if(!CameraUtil.MTKCAM_AIWORKS_HDR_SUPPORT){
                    //bv wuyonglin add for add hdr quickswitch 20191231 start
                    if (mIsHdrOpened) {
                        mFirstFrameArrived = false;
                    }
                    //bv wuyonglin add for add hdr quickswitch 20191231 end
                }else{
                    if(!mHdrStatus.equals("off")){
                        mFirstFrameArrived = false;
                    }
                }
                //bv wuyonglin add for bug5276 20210415 start
                if(CameraUtil.MTKCAM_AIWORKS_HDR_SUPPORT){
                    if (!mHdrStatus.equals("off") || (mAisStatus.equals("on") && mFlashStatus.equals("off"))) {
                        mZoomConfig = mICameraContext.getIApp().getAppUi().getZoomConfig();
                        if (mZoomConfig.getCurZoomRatio() != 1.0f) {
                            LogHelper.i(TAG, "[repeatingPreview] getCurZoomRatio() = " + mZoomConfig.getCurZoomRatio()+" mSensorRect ="+mSensorRect);
                            mPreviewRequestBuilder.set(CaptureRequest.SCALER_CROP_REGION, mSensorRect);
                            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_ZOOM_RATIO,mZoomConfig.getCurZoomRatio());
                        }
                    }
		}
                //bv wuyonglin add for bug5276 20210415 end
                if (needConfigBuiler) {
					//add by liangchangwei for AiWorks HDR
                    if(CameraUtil.MTKCAM_AIWORKS_HDR_SUPPORT){
                        //bv wuyonglin add for hd shot 20201013 start
                        if(!mHdrStatus.equals("off") || (mAisStatus.equals("on") && mFlashStatus.equals("off"))){
                        //bv wuyonglin add for hd shot 20201013 end
                            Builder builder = doCreateAndConfigRequest(Camera2Proxy.TEMPLATE_PREVIEW);
                            builder.addTarget(mPreviewReaderSurface);
                            mSession.setRepeatingRequest(builder.build(), mCaptureCallback, mModeHandler);
                        }else{
                            Builder builder = doCreateAndConfigRequest(Camera2Proxy.TEMPLATE_PREVIEW);
                            mSession.setRepeatingRequest(builder.build(), mCaptureCallback, mModeHandler);
                        }
                        //bv wuyonglin delete for bug5276 20210415 start
                        //mZoomConfig = mICameraContext.getIApp().getAppUi().getZoomConfig();
                        //bv wuyonglin delete for bug5276 20210415 end
                    }else{
                        Builder builder = doCreateAndConfigRequest(Camera2Proxy.TEMPLATE_PREVIEW);
                        mSession.setRepeatingRequest(builder.build(), mCaptureCallback, mModeHandler);
                    }
                } else {
					//add by liangchangwei for AiWorks HDR
                    if(!CameraUtil.MTKCAM_AIWORKS_HDR_SUPPORT){
                        //bv wuyonglin modify for add hdr quickswitch 20191231 start
                        if (!mIsHdrOpened) {
                            mBuilder.addTarget(mPreviewSurface);
                        }
                        //bv wuyonglin modify for add hdr quickswitch 20191231 end
                    }else{
                        //bv wuyonglin add for hd shot 20201013 start
                        if(mHdrStatus.equals("off") && !(mAisStatus.equals("on") && mFlashStatus.equals("off"))){
                        //bv wuyonglin add for hd shot 20201013 end
                            mBuilder.addTarget(mPreviewSurface);
                        }
                    }
                    mSession.setRepeatingRequest(mBuilder.build(), mCaptureCallback, mModeHandler);
                }
                mCaptureSurface.setCaptureCallback(this);
            } catch (CameraAccessException | RuntimeException e) {
                LogHelper.e(TAG, "[repeatingPreview] error");
            }
        }
        CameraSysTrace.onEventSystrace("photoDevice.repeatingPreview", false, true);
    }

    private Builder doCreateAndConfigRequest(int templateType) throws CameraAccessException {
        LogHelper.i(TAG, "[doCreateAndConfigRequest] mCamera2Proxy =" + mCamera2Proxy + " templateType =" + templateType + " mCamera2Proxy =" + mCamera2Proxy);
        CaptureRequest.Builder builder = null;
        if (mCamera2Proxy != null) {
            builder = mCamera2Proxy.createCaptureRequest(templateType);
            if (builder == null) {
                LogHelper.d(TAG, "Builder is null, ignore this configuration");
                return null;
            }
            /* add by bv liangchangwei for fixbug 3301 start --*/
            if (mPreviewSurface == null) {
                LogHelper.d(TAG, "mPreviewSurface is null, ignore this configuration");
                return null;
            }
            /* add by bv liangchangwei for fixbug 3301 end --*/

            mSettingDevice2Configurator.configCaptureRequest(builder);
            ThumbnailHelper.configPostViewRequest(builder);
            configureQuickPreview(builder);
            configureBGService(builder);
			//add by liangchangwei for AiWorks HDR
            if(!CameraUtil.MTKCAM_AIWORKS_HDR_SUPPORT){
                //bv wuyonglin modify for add hdr quickswitch 20191231 start
                if (!mIsHdrOpened) {
                configurePlatformCamera(builder);
                }
                //bv wuyonglin modify for add hdr quickswitch 20191231 end
            }else{
                configurePlatformCamera(builder);
            }
            if (Camera2Proxy.TEMPLATE_PREVIEW == templateType) {
                LogHelper.i(TAG, "[doCreateAndConfigRequest] TEMPLATE_PREVIEW mPreviewSurface =" + mPreviewSurface);
                builder.addTarget(mPreviewSurface);
            } else if (Camera2Proxy.TEMPLATE_STILL_CAPTURE == templateType) {
				//add by liangchangwei for AiWorks HDR
                if(CameraUtil.MTKCAM_AIWORKS_HDR_SUPPORT){
                    LogHelper.i(TAG, "[doCreateAndConfigRequest] TEMPLATE_STILL_CAPTURE mCaptureSurface.getSurface() =" + mCaptureSurface.getSurface());
                    builder.addTarget(mCaptureSurface.getSurface());
                }else{
                    //bv wuyonglin modify for add hdr quickswitch 20191231 start
                    if (!mIsHdrOpened) {
                        LogHelper.i(TAG, "[doCreateAndConfigRequest] TEMPLATE_STILL_CAPTURE mCaptureSurface.getSurface() =" + mCaptureSurface.getSurface());
                        builder.addTarget(mCaptureSurface.getSurface());
                    } else {
                        LogHelper.i(TAG, "[doCreateAndConfigRequest] TEMPLATE_STILL_CAPTURE mCapturePostAlgoSurface =" + mCapturePostAlgoSurface);
                        builder.addTarget(mCapturePostAlgoSurface);
                    }
                    //bv wuyonglin modify for add hdr quickswitch 20191231 end
                }
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
        if (mCamera2Proxy != null && mDefaultBuilder == null) {
            mDefaultBuilder = mCamera2Proxy.createCaptureRequest(Camera2Proxy.TEMPLATE_PREVIEW);
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
				//add by liangchangwei for AiWorks HDR
                if(CameraUtil.MTKCAM_AIWORKS_HDR_SUPPORT){
                    mCameraDevice = mCamera2Proxy.getCameraDevice();
                    LogHelper.i(TAG,"[onOpened]  mCameraDevice = " + mCameraDevice);
                }
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
				//add by liangchangwei for AiWorks HDR
                if(CameraUtil.MTKCAM_AIWORKS_HDR_SUPPORT){
                    //if(!mHdrStatus.equals("off")){
                        setUpCameraOutputs(mPreviewWidth, mPreviewHeight);
                    //}
                }
                    CameraSysTrace.onEventSystrace("donCameraOpened.updatePreviewPictureSize", false, true);
                    if (mPreviewSizeCallback != null) {
                        mPreviewSizeCallback.onPreviewSizeReady(new Size(mPreviewWidth,
                                mPreviewHeight));
                    }
				//add by liangchangwei for AiWorks HDR
                if(CameraUtil.MTKCAM_AIWORKS_HDR_SUPPORT){
                    if (mNeedSubSectionInitSetting) {
                        configureSession(true);
                    } else {
                        //bv wuyonglin add for switcher camera in top quickswitch setting icon not show with other icon 20200307 start
                        if(Config.isZsdOnAndHide(mActivity)){
                            Relation relation = PhotoRestriction.getRestriction().getRelation("on", true);
                            mSettingManager.getSettingController().postRestriction(relation);
                        }
                        //bv wuyonglin add for switcher camera in top quickswitch setting icon not show with other icon 20200307 end
                        mSettingController.addViewEntry();
                        mSettingController.refreshViewEntry();
                    }

                }else{
                	//bv wuyonglin add for add hdr quickswitch 20191231 start
                    if (!mIsHdrOpened) {
                        //bv wuyonglin add for add hdr quickswitch 20191231 end
                        //add by huang fei disable setting items start
                        //bv wuyonglin delete for switcher camera in top quickswitch setting icon not show with other icon 20200307 start
                    /*try {
                        initSettings();
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }*/
                        //bv wuyonglin delete for switcher camera in top quickswitch setting icon not show with other icon 20200307 end
                        //add by huang fei disable setting items end

                        if (mNeedSubSectionInitSetting) {
                            configureSession(true);
                        } else {
                            //bv wuyonglin add for switcher camera in top quickswitch setting icon not show with other icon 20200307 start
                            if(Config.isZsdOnAndHide(mActivity)){
                                Relation relation = PhotoRestriction.getRestriction().getRelation("on", true);
                                mSettingManager.getSettingController().postRestriction(relation);
                            }
                            //bv wuyonglin add for switcher camera in top quickswitch setting icon not show with other icon 20200307 end
                            mSettingController.addViewEntry();
                            mSettingController.refreshViewEntry();
                        }
                        //bv wuyonglin add for add hdr quickswitch 20191231 start
                    } else {
                        try {
                            initHdrSettings();
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                    }
                    //bv wuyonglin add for add hdr quickswitch 20191231 end
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
                            + ", mNeedFinalizeOutput:" + mNeedFinalizeOutput);
                    mDeviceLock.lock();
                    try {
                        mSession = session;
						//add by liangchangwei for AiWorks HDR
                        if(CameraUtil.MTKCAM_AIWORKS_HDR_SUPPORT){
                            //bv wuyonglin add for add hdr quickswitch 20191231 start
                            //bv wuyonglin add for add hdr quickswitch 20191231 end
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
                            //bv wuyonglin add for add hdr quickswitch 20191231 start
                            if (CameraState.CAMERA_OPENED == getCameraState()) {
                                synchronized (mSurfaceHolderSync) {
                                    if (mPreviewSurface != null) {
                                        repeatingPreview(false);
                                    }
                                }
                                return;
                            }
                            //bv wuyonglin add for add hdr quickswitch 20191231 end
                        }else{
                            //bv wuyonglin add for add hdr quickswitch 20191231 start
                            if (!mIsHdrOpened) {
                                //bv wuyonglin add for add hdr quickswitch 20191231 end
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
                                //bv wuyonglin add for add hdr quickswitch 20191231 start
                            } else {
                                if (CameraState.CAMERA_OPENED == getCameraState()) {
                                    synchronized (mSurfaceHolderSync) {
                                        if (mPreviewSurface != null) {
                                            repeatingPreview(false);
                                        }
                                    }
                                    return;
                                }
                            }
                            //bv wuyonglin add for add hdr quickswitch 20191231 end
                        }
/*
                        if (CameraState.CAMERA_OPENED == getCameraState()) {
                            synchronized (mSurfaceHolderSync) {
                                if (mPreviewSurface != null) {
                                    repeatingPreview(false);
                                }
                            }
                        }
*/
						//add by liangchangwei for AiWorks HDR
                        if(CameraUtil.MTKCAM_AIWORKS_HDR_SUPPORT){
                            try {
                                LogHelper.i(TAG, "onConfigured setRepeatingRequest mPreviewRequestBuilder = " + mPreviewRequestBuilder);
                                if(mPreviewRequestBuilder != null && ("on".equals(mHdrStatus) || "auto".equals(mHdrStatus) || (mAisStatus.equals("on") && mFlashStatus.equals("off")))){
                                    mSession.setRepeatingRequest(mPreviewRequestBuilder.build(),
                                            mCaptureCallback, mModeHandler);
                                    HDRInit = true;
                                    LogHelper.i(TAG, "onConfigured setRepeatingRequest HDRInit = " + HDRInit);
                                }
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
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
                boolean useBGService
                        = BGServiceKeeper.supportByBGService(mCaptureSurface.getCaptureType());
                if (mIsBGServiceEnabled && useBGService) {
                    mCaptureSurface.increasePictureNum();
                }
                mCaptureFrameMap.put(String.valueOf(frameNumber), Boolean.FALSE);
				//add by liangchangwei for AiWorks HDR
                if(CameraUtil.MTKCAM_AIWORKS_HDR_SUPPORT){
                    mICameraContext.getSoundPlayback().play(ISoundPlayback.SHUTTER_CLICK);
                }else{
                    //bv wuyonglin add for add hdr quickswitch 20191231 start
                    if (!mIsHdrOpened) {
                        //bv wuyonglin add for add hdr quickswitch 20191231 end
                        mICameraContext.getSoundPlayback().play(ISoundPlayback.SHUTTER_CLICK);
                        //bv wuyonglin add for add hdr quickswitch 20191231 start
                    } else {
                        if (mCaptureNum == CAPTURE_REQUEST_NUM) {
                            mCaptureNum--;
                            mICameraContext.getSoundPlayback().play(ISoundPlayback.SHUTTER_CLICK);
                        }
                    }
                    //bv wuyonglin add for add hdr quickswitch 20191231 end
                }
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
                CameraSysTrace.onEventSystrace("photoDevice.onP2Done", true, true);
                LogHelper.i(TAG, "[onCaptureProgressed] P2done comes, frame: " + num);
				//add by liangchangwei for AiWorks HDR
                if(CameraUtil.MTKCAM_AIWORKS_HDR_SUPPORT){
                    updateCameraState(CameraState.CAMERA_OPENED);
                    mModeDeviceCallback.onPreviewCallback(null, 0);
                }else{
                    //bv wuyonglin modify for add hdr quickswitch 20191231 start
                    if (!mIsHdrOpened) {
                        updateCameraState(CameraState.CAMERA_OPENED);
                        mModeDeviceCallback.onPreviewCallback(null, 0);
                    }
                    //bv wuyonglin modify for add hdr quickswitch 20191231 end
                }
                CameraSysTrace.onEventSystrace("photoDevice.onP2Done", false, true);
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
			//add by liangchangwei for AiWorks HDR
            if(!CameraUtil.MTKCAM_AIWORKS_HDR_SUPPORT){
                //bv wuyonglin add for add hdr quickswitch 20191231 start
                if (mIsHdrOpened) {
                    mSettingDevice2Configurator.getRepeatingCaptureCallback().onCaptureCompleted(
                            session, request, result);
                }
                //bv wuyonglin add for add hdr quickswitch 20191231 end
            }else{
/*                mSettingDevice2Configurator.getRepeatingCaptureCallback().onCaptureCompleted(
                        session, request, result);*/
            }
            if (CameraUtil.isStillCaptureTemplate(result)) {
                mModeDeviceCallback.onCaptureCallback();
                long num = result.getFrameNumber();
                if (mCaptureFrameMap.containsKey(String.valueOf(num))
                        && Boolean.FALSE == mCaptureFrameMap.get(String.valueOf(num))) {
                    mFirstFrameArrived = true;
					//add by liangchangwei for AiWorks HDR
                    if(CameraUtil.MTKCAM_AIWORKS_HDR_SUPPORT){
                        updateCameraState(CameraState.CAMERA_OPENED);
                        mModeDeviceCallback.onPreviewCallback(null, 0);
                    }else{
                        //bv wuyonglin modify for add hdr quickswitch 20191231 start
                        if (!mIsHdrOpened) {
                            updateCameraState(CameraState.CAMERA_OPENED);
                            mModeDeviceCallback.onPreviewCallback(null, 0);
                        }
                        //bv wuyonglin modify for add hdr quickswitch 20191231 end
                    }
                }
                mCaptureFrameMap.remove(String.valueOf(num));
                LogHelper.i(TAG, "[onCaptureCompleted] result: "
                        + result.get(CaptureResult.CONTROL_AE_EXPOSURE_COMPENSATION));
            } else if (!CameraUtil.isStillCaptureTemplate(result) && !mFirstFrameArrived) {
                CameraSysTrace.onEventSystrace("photoDevice.onFirstFrameArrived", true, true);
                mFirstFrameArrived = true;
                updateCameraState(CameraState.CAMERA_OPENED);
                mModeDeviceCallback.onPreviewCallback(null, 0);
                mICameraContext.getSoundPlayback().init();
                CameraSysTrace.onEventSystrace("photoDevice.onFirstFrameArrived", false, true);
            }
			//add by liangchangwei for AiWorks HDR
            if(CameraUtil.MTKCAM_AIWORKS_HDR_SUPPORT){
                /* modify by liangchangwei 2020-10-10 begin --*/
                /*if(mHdrStatus.equals("off")){
                    mSettingDevice2Configurator.getRepeatingCaptureCallback().onCaptureCompleted(
                            session, request, result);
                }else{
                    mHDRCaptureRequest.onPreviewCaptureCompleted(result);
                }*/
                /* modify by liangchangwei 2020-10-10 end --*/
		//bv wuyonglin add for hd shot 20201013 start
                //bv wuyonglin add for bug2807 after hdr and hd shot open focus animate not run 20201105 start
                mSettingDevice2Configurator.getRepeatingCaptureCallback().onCaptureCompleted(
                        session, request, result);
                //bv wuyonglin add for bug2807 after hdr and hd shot open focus animate not run 20201105 end
                if (!mHdrStatus.equals("off") && mHDRCaptureRequest != null) {
                    mHDRCaptureRequest.onPreviewCaptureCompleted(result);
                } else if (mAisStatus.equals("on") && mHdrStatus.equals("off") && mFlashStatus.equals("off") && mManager != null) {
                    mManager.onPreviewCaptureCompleted(result);
                }
		//bv wuyonglin add for hd shot 20201013 end
            }else{
                //bv wuyonglin modify for add hdr quickswitch 20191231 start
                if (!mIsHdrOpened) {
                    mSettingDevice2Configurator.getRepeatingCaptureCallback().onCaptureCompleted(
                            session, request, result);
                }
                //bv wuyonglin modify for add hdr quickswitch 20191231 end
            }
        }

        @Override
        public void onCaptureFailed(@Nonnull CameraCaptureSession session,
                @Nonnull CaptureRequest request, @Nonnull CaptureFailure failure) {
            super.onCaptureFailed(session, request, failure);
            long frameNumber = failure.getFrameNumber();
            LogHelper.e(TAG, "[onCaptureFailed], framenumber: " + frameNumber
                    + ", reason: " + failure.getReason() + ", sequenceId: "
                    + failure.getSequenceId() + ", isCaptured: " + failure.wasImageCaptured()
                    + ", mCurrentCameraId = " + mCurrentCameraId);
            if (mCaptureFrameMap.containsKey(String.valueOf(frameNumber))){
                mModeDeviceCallback.onCaptureCallback();
            }
            if (mCamera2Proxy == null || session.getDevice() != mCamera2Proxy.getCameraDevice()) {
                return;
            }
            mSettingDevice2Configurator.getRepeatingCaptureCallback()
                    .onCaptureFailed(session, request, failure);
            if (mCurrentCameraId != null && mModeDeviceCallback != null
                    && CameraUtil.isStillCaptureTemplate(request)) {
                mCaptureFrameMap.remove(String.valueOf(frameNumber));
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
            mCaptureSurface.updatePictureInfo(format, value);

            if (mIsBGServiceEnabled && BGServiceKeeper.supportByBGService(value)) {
                mBGServiceKeeper.setBGCaptureSurface(mCaptureSurface);
            }
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

    //*/ hct.huangfei, 20201110. continuousshot numbers.
    public void releaseCaptureDataCallback (){
        LogHelper.i(TAG, "releaseCaptureDataCallback");
        mCaptureDataCallback = null;
    }
    //*/
	
    //bv wuyonglin modify for add hdr quickswitch 20191231 start
    private Size getPictureSize() {
        ISettingManager.SettingController controller = mSettingManager.getSettingController();
        String pictureSize = controller.queryValue(KEY_PICTURE_SIZE);
        LogHelper.i(TAG, "[updatePictureSize] :" + pictureSize);
        if (pictureSize != null) {
            String[] pictureSizes = pictureSize.split("x");
            int width = Integer.parseInt(pictureSizes[0]);
            int height = Integer.parseInt(pictureSizes[1]);
            return new Size(width, height);
        }
        return null;
    }

    private void initHdrSettings() throws CameraAccessException {
        LogHelper.i(TAG, "[openCamera] initHdrSettings cameraId : " + "initSettings");
        Relation relation = HdrRestriction.getRestriction().getRelation("on",
                false);
        mSettingController.postRestriction(relation);
        mSettingController.addViewEntry();
        mSettingController.refreshViewEntry();
    }
    //bv wuyonglin modify for add hdr quickswitch 20191231 end

	//add by liangchangwei for AiWorks HDR
    @Override
    public void onCaptureStart(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result){
        LogHelper.i(TAG, "onCaptureStart");
        Location location = mICameraContext.getLocation();
        mExif = ExifInterface.addExifTags(0, result, location);
        mICameraContext.getSoundPlayback().play(ISoundPlayback.SHUTTER_CLICK);
        mCaptureCallback.onCaptureCompleted(session, request, result);
    }

    @Override
    public void onCaptureCompleted(TotalCaptureResult result){
        LogHelper.i(TAG, "onCaptureCompleted");
        mCapturePreview = true;
        startPreview();
        mStart = false;
    }

    @Override
    public void showToast(String message){
        LogHelper.i(TAG, "showToast");
    }

    //bv liangchangwei add for AiWorks begin
    @Override
    public void addExif(String path, byte[] data){
        LogHelper.i(TAG,"addExif path = " + path + " mExif = " + mExif + " data.size = " + data.length);
        if(!mHdrStatus.equals("off") || (mFlashStatus.equals("off") && mAisStatus.equals("on"))){ //bv wuyonglin modify for hd shot 20201013 start
            if(mExif != null){
                Util.writeImage(path, mExif, data);
            }
        }/*else if(CameraUtil.isWaterMarkOn(mDataStore,mActivity)){
            if(mExif != null){
                Util.writeImage(path, mExif, data);
            }
        }*/
    }
    //bv liangchangwei add for AiWorks end

    @Override
    public void saveHDRData(byte[] data, int format, android.util.Size photosize){
        LogHelper.i(TAG, "saveHDRData ");
        String formatTag = mCaptureSurface.getCaptureType();
/*        if(mCurrentCameraId.equals("1")){
            mJpegRotation = mJpegRotation + 180;
        }*/
        LogHelper.i(TAG, "<saveHDRData> data = " + data + ", format = " + format + " formatTag = " + formatTag
                + ", width = " + photosize.getWidth() + ", height = " + photosize.getHeight()
                + ", mCaptureDataCallback = " + mCaptureDataCallback + " mJpegRotation = " + mJpegRotation + " mCurrentCameraId = " + mCurrentCameraId);

        if (format == ImageFormat.YUV_420_888 || format == ImageFormat.NV21) {
            if("1".equals(mCurrentCameraId)){
                boolean isMirror = "1".equals(mDataStore.getValue("key_camera_mirror", "1", mDataStore.getCameraScope(Integer.parseInt(mCurrentCameraId))));
                LogHelper.i(TAG,"saveHDRData isMirror = " + isMirror+" mJpegRotation ="+mJpegRotation+" mOrientation ="+mOrientation);
                if (isMirror) {
                    if (mJpegRotation % 180 == 0) {
                        data = YuvEncodeJni.getInstance().RotateYuv(data, format, photosize.getWidth(), photosize.getHeight(), 0, true, true);
                    } else {
                        data = YuvEncodeJni.getInstance().RotateYuv(data, format, photosize.getWidth(), photosize.getHeight(), 90, true, true);
                        data = YuvEncodeJni.getInstance().RotateYuv(data, format, photosize.getHeight(), photosize.getWidth(), 270, true, false);
                    }
                }
            }
            LogHelper.i(TAG,"saveHDRData mJpegRotation = " + mJpegRotation);
            if(CameraUtil.isWaterMarkOn(mDataStore,mActivity)){
                LogHelper.i(TAG,"isWaterMarkOn on + ");
                WaterMarkUtil.yuvAddWaterMark(mActivity,data, photosize.getWidth(), photosize.getHeight(), mJpegRotation);
                LogHelper.i(TAG,"isWaterMarkOn on - ");
            }else{
                LogHelper.i(TAG,"isWaterMarkOn off");
            }
            LogHelper.i(TAG,"EncodeYuvToJpeg start! ");
            data = YuvEncodeJni.getInstance().EncodeYuvToJpeg(data, ImageFormat.NV21,
                    photosize.getWidth(), photosize.getHeight(), 95, mJpegRotation);
            LogHelper.i(TAG,"EncodeYuvToJpeg end! ");
        }


        if (mCaptureDataCallback != null) {
            DataCallbackInfo info = new DataCallbackInfo();
            info.data = data;
            info.needUpdateThumbnail = true;
            info.needRestartPreview = false;
            info.mBufferFormat = ImageFormat.JPEG;
            info.imageHeight = photosize.getHeight();
            info.imageWidth = photosize.getWidth();
            if (ThumbnailHelper.isPostViewSupported()) {
                info.needUpdateThumbnail = false;
            }
            if (ThumbnailHelper.FORMAT_TAG.equalsIgnoreCase(formatTag)) {
                LogHelper.i(TAG,"mCaptureDataCallback.onPostViewCallback(data)");
                mCaptureDataCallback.onPostViewCallback(data);
            } else {
                LogHelper.i(TAG,"mCaptureDataCallback.onDataReceived(data)");
                mCaptureDataCallback.onDataReceived(info);
                LogHelper.i(TAG,"mIsBGServiceEnabled = " + mIsBGServiceEnabled + " mCaptureSurface = " + mCaptureSurface);
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
        //mStart = false;
    }

    @Override
    public void compare(byte[] origin, byte[] result, int format){
        LogHelper.i(TAG, "compare");
        if (mJpegRotation % 180 == 0) {
            CompareActivity.mOriginImage = Bitmap.createBitmap(mPhotoSize.getWidth(), mPhotoSize.getHeight(), Bitmap.Config.ARGB_8888);
            CompareActivity.mResultImage = Bitmap.createBitmap(mPhotoSize.getWidth(), mPhotoSize.getHeight(), Bitmap.Config.ARGB_8888);
        } else {
            CompareActivity.mOriginImage = Bitmap.createBitmap(mPhotoSize.getHeight(), mPhotoSize.getWidth(), Bitmap.Config.ARGB_8888);
            CompareActivity.mResultImage = Bitmap.createBitmap(mPhotoSize.getHeight(), mPhotoSize.getWidth(), Bitmap.Config.ARGB_8888);
        }
        YuvEncodeJni.getInstance().Yuv2Bmp(origin, CompareActivity.mOriginImage, format,
                mPhotoSize.getWidth(), mPhotoSize.getHeight(), mJpegRotation);
        YuvEncodeJni.getInstance().Yuv2Bmp(result, CompareActivity.mResultImage, format,
                mPhotoSize.getWidth(), mPhotoSize.getHeight(), mJpegRotation);
        Intent intent = new Intent(mActivity, CompareActivity.class);
        mActivity.startActivity(intent);
    }

    private void setUpCameraOutputs(int width, int height){
        LogHelper.i(TAG, "setUpCameraOutputs");
        int CameraId = HDRConfig.getCameraID();
        try {
            CameraManager manager = (CameraManager) mActivity.getSystemService(Context.CAMERA_SERVICE);
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(CameraId + "");

            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            ArrayList<android.util.Size> supportedPhotoSizes = new ArrayList<>();
            if (Product.mPlatformID == Product.HARDWARE_PLATFORM_MTK) {
                android.util.Size[] highJPEG = map.getHighResolutionOutputSizes(mHDRCaptureRequest.getPhotoForamt());
                if (highJPEG != null && highJPEG.length > 0) {
                    supportedPhotoSizes.addAll(CameraHelper.buildListFromAndroidSizes(Arrays.asList(highJPEG)));
                }
            }
            supportedPhotoSizes.addAll(CameraHelper.buildListFromAndroidSizes(Arrays.asList(
                    map.getOutputSizes(mHDRCaptureRequest.getPhotoForamt()))));

            for (android.util.Size size : supportedPhotoSizes) {
                //LogHelper.i(TAG, "supportedPhotoSizes:" + size);
            }

            //mPhotoSize = mHDRCaptureRequest.getPhotoSize();

            LogHelper.i(TAG,"setUpCameraOutputs mPhotoSize = " + mPhotoSize + " Foramt = " + mHDRCaptureRequest.getPhotoForamt());
            if (mPhotoSize == null) {
                mPhotoSize = Collections.max(supportedPhotoSizes, new CompareSizesByArea());
            }

            int displayRotation = mActivity.getWindowManager().getDefaultDisplay().getRotation();
            int mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            //mJpegRotation = mSensorOrientation;
            boolean swappedDimensions = false;
            LogHelper.i(TAG,"displayRotation = " + displayRotation + " mSensorOrientation = " + mSensorOrientation + " mJpegRotation = " + mJpegRotation);
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
                    LogHelper.e(TAG, "Display rotation is invalid: " + displayRotation);
            }

            Point displaySize = new Point();
            mActivity.getWindowManager().getDefaultDisplay().getSize(displaySize);
            int rotatedPreviewWidth = width;
            int rotatedPreviewHeight = height;

            if (swappedDimensions) {
                rotatedPreviewWidth = height;
                rotatedPreviewHeight = width;
            }

            mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), rotatedPreviewWidth, rotatedPreviewHeight,
                    MAX_PREVIEW_WIDTH, MAX_PREVIEW_HEIGHT, mPhotoSize);

/*            int orientation = getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                mTextureView.setAspectRatio(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            } else {
                mTextureView.setAspectRatio(mPreviewSize.getHeight(), mPreviewSize.getWidth());
            }*/

            LogHelper.i(TAG, "PreviewSize =  " + mPreviewSize + " mPhotoSize = " + mPhotoSize);

/*            isFocusFixed = true;
            int[] focuses = characteristics.get(CONTROL_AF_AVAILABLE_MODES);
            if (focuses != null) {
                for (int focus : focuses) {
                    if (focus == CONTROL_AF_MODE_CONTINUOUS_PICTURE || focus == CONTROL_AF_MODE_AUTO) {
                        isFocusFixed = false;
                    }
                }
            }*/

/*
            Range<Integer> evRange = characteristics.get(CONTROL_AE_COMPENSATION_RANGE);

            LogHelper.i(TAG,"mHDRCaptureResult.init mPhotoSize = " + mPhotoSize + " mPreviewSize = " + mPreviewSize);
            mHDRCaptureResult.init(mActivity, mPhotoSize, mPreviewSize, evRange);
            mImageReader = ImageReader.newInstance(mPhotoSize.getWidth(), mPhotoSize.getHeight(),
                    mHDRCaptureRequest.getPhotoForamt(), 1);
*/
            //mConfigHdrSettingsHandler.sendEmptyMessage(HDR_CAPTURERESULT_INIT);
            Range<Integer> evRange = characteristics.get(CONTROL_AE_COMPENSATION_RANGE);
            //bv wuyonglin add for hd shot 20201013 start
            if (Config.isAisSupport(mActivity.getApplicationContext())) {
            Rect activeRect = characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);//获取成像区域
            mBackgroundHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mManager != null) {
                    mManager.init(mActivity, mPhotoSize, mPreviewSize, evRange, activeRect, PhotoDevice2Controller.this);
                    }
                }
            });
            LogHelper.d(TAG,"mManager.init end mPreviewSize ="+mPreviewSize );
            }
            //bv wuyonglin add for hd shot 20201013 end
            /*add by bv liangchangwei for HDR shot init 20200902 start  */
            if(mHDRCaptureResult != null && mActivity != null && !isInit){
                mHDRCaptureResult.setCaptureCallback(this);
                new Thread(){
                    @Override
                    public void run() {
                        super.run();
                        if (mHDRCaptureResult != null) {
                        mHDRCaptureResult.init(mActivity, mPhotoSize, evRange);
                        isInit = true;
                        }
                    }
                }.start();
                LogHelper.d(TAG,"mNightCaptureResult.init mPhotoSize = " + mPhotoSize);
            }
            /*add by bv liangchangwei for HDR shot init 20200902 start  */
            //bv wuyonglin add for bug3791 20200202 start
            if (mOrientationListener == null) {
                mOrientationListener = new MyOrientationEventListener(mActivity.getApplicationContext());
            }
            mOrientationListener.setEnable(true);
            //bv wuyonglin add for bug3791 20200202 end
            LogHelper.d(TAG," ImageReader.newInstance width = " + mPhotoSize.getWidth() + " height = " + mPhotoSize.getHeight() + " format = " + mHDRCaptureRequest.getPhotoForamt());
            mImageReader = ImageReader.newInstance(mPhotoSize.getWidth(), mPhotoSize.getHeight(),
                    mHDRCaptureRequest.getPhotoForamt(), 4);


            if (null != mPreviewReader) {
                mPreviewReader.setOnImageAvailableListener(null, null);
                mPreviewReader.close();
                mPreviewReader = null;
            }
            LogHelper.i(TAG,"mPreviewReader newInstance mPreviewSize = " + mPreviewSize);
            mPreviewReader = ImageReader.newInstance(mPreviewSize.getWidth(), mPreviewSize.getHeight(),
                    ImageFormat.YUV_420_888, 1);
            mPreviewReader.setOnImageAvailableListener(mPreviewReaderListener, mPreviewReaderHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private ImageReader.OnImageAvailableListener mPreviewReaderListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            try{
            Image image = reader.acquireNextImage();
            byte[] captureYuv = YuvEncodeJni.getInstance().getBuffer(image,false);
            int width = image.getWidth();
            int height = image.getHeight();
            image.close();
            synchronized (this) {
                if (mHDRCaptureRequest != null) {
                    boolean isHDR = mHDRCaptureRequest.onPreviewFrame(captureYuv, width, height, mOrientation);
                    if(isHDR != AutoHDRStatus){
                        AutoHDRStatuscount ++;
                        if(AutoHDRStatuscount >= 5){
                            AutoHDRStatus = isHDR;
                        }
                    }else{
                        AutoHDRStatuscount = 0;
                    }
                    //updateHDRIcon(isHDR);
                }

            }
            if(mCapturePreview == true){
                mCapturePreview = false;
                LogHelper.i(TAG, "onPostViewCallback mCapturePreview = " + mCapturePreview + " width = " + width + " height = " + height + " size = " + captureYuv.length);
                int rotation = (mJpegRotation + 90)%360;
                if("1".equals(mCurrentCameraId)){
                    if(rotation%180 != 0){
                        rotation = (rotation + 180)%360;
                    }
                }
                LogHelper.i(TAG,"CapturePreview mJpegRotation = " + mJpegRotation + " rotation = " + rotation);
                byte[] data = YuvEncodeJni.getInstance().EncodeYuvToJpeg(captureYuv, ImageFormat.NV21,
                        width, height, 95, mJpegRotation);
                LogHelper.i(TAG, "onPostViewCallback mCapturePreview = " + mCapturePreview + " width = " + width + " height = " + height + " size = " + data.length + " rotation = " + rotation);
                mCaptureDataCallback.onPostViewCallback(data);
            }
            }catch(Exception e){
                e.printStackTrace();
                LogHelper.e(TAG, "onImageAvailable1 error");
            }

        }
    };
    static class CompareSizesByArea implements Comparator<android.util.Size> {

        @Override
        public int compare(android.util.Size lhs, android.util.Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }

    }

    private static android.util.Size chooseOptimalSize(android.util.Size[] choices, int textureViewWidth,
                                                       int textureViewHeight, int maxWidth, int maxHeight, android.util.Size aspectRatio) {

        // Collect the supported resolutions that are at least as big as the preview Surface
        List<android.util.Size> bigEnough = new ArrayList<>();
        // Collect the supported resolutions that are smaller than the preview Surface
        List<android.util.Size> notBigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (android.util.Size option : choices) {
            if (option.getWidth() <= maxWidth && option.getHeight() <= maxHeight &&
                    option.getHeight() == option.getWidth() * h / w) {
                if (option.getWidth() >= textureViewWidth &&
                        option.getHeight() >= textureViewHeight) {
                    bigEnough.add(option);
                } else {
                    notBigEnough.add(option);
                }
            }
        }

        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else if (notBigEnough.size() > 0) {
            return Collections.max(notBigEnough, new CompareSizesByArea());
        } else {
            LogHelper.e(TAG, "Couldn't find any suitable preview size");
            return choices[0];
        }
    }

    public boolean isHdrOn(){
        boolean status = false;
        if(CameraUtil.MTKCAM_AIWORKS_HDR_SUPPORT){
            status = mHdrStatus.equals("on");
        }
        return status;
    }
    private class ConfigHDRSettingsHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            LogHelper.d(TAG, "handleMessage what =  " + msg.what);
            switch (msg.what){
                case HDR_CAPTURERESULT_INIT:
                    LogHelper.d(TAG, "handleMessage HDR_CAPTURERESULT_INIT");
                    try {
                        int CameraId = HDRConfig.getCameraID();
                        CameraManager manager = (CameraManager) mActivity.getSystemService(Context.CAMERA_SERVICE);
                        CameraCharacteristics characteristics = manager.getCameraCharacteristics(CameraId + "");
                        Range<Integer> evRange = characteristics.get(CONTROL_AE_COMPENSATION_RANGE);
                        if(mHDRCaptureResult != null && mActivity != null){
                            mHDRCaptureResult.init(mActivity, mPhotoSize, evRange);
                            LogHelper.d(TAG,"mNightCaptureResult.init mPhotoSize = " + mPhotoSize);
                        }
                        LogHelper.d(TAG," ImageReader.newInstance width = " + mPhotoSize.getWidth() + " height = " + mPhotoSize.getHeight() + " format = " + mHDRCaptureRequest.getPhotoForamt());
                        mImageReader = ImageReader.newInstance(mPhotoSize.getWidth(), mPhotoSize.getHeight(),
                                mHDRCaptureRequest.getPhotoForamt(), 4);
                    }catch (CameraAccessException e){
                        e.printStackTrace();
                    }
                    break;
                case HDR_CAPTURERESULT_SET_PHOTOSIZE:
                    LogHelper.d(TAG, "handleMessage HDR_CAPTURERESULT_SET_PHOTOSIZE");
                    try{
                        int CameraId = HDRConfig.getCameraID();
                        CameraManager manager = (CameraManager) mActivity.getSystemService(Context.CAMERA_SERVICE);
                        CameraCharacteristics characteristics = manager.getCameraCharacteristics(CameraId + "");
                        Range<Integer> evRange = characteristics.get(CONTROL_AE_COMPENSATION_RANGE);

                        if(mHDRCaptureResult != null && mActivity != null){
                            mHDRCaptureResult.init(mActivity, mPhotoSize, evRange);
                            LogHelper.d(TAG,"mNightCaptureResult.updatePhotoSize mPhotoSize = " + mPhotoSize);
                        }
                        //LogHelper.d(TAG," ImageReader.newInstance width = " + mPhotoSize.getWidth() + " height = " + mPhotoSize.getHeight() + " format = " + mHDRCaptureRequest.getPhotoForamt());
                        if(mHDRCaptureRequest != null){
                            mImageReader = ImageReader.newInstance(mPhotoSize.getWidth(), mPhotoSize.getHeight(),
                                    mHDRCaptureRequest.getPhotoForamt(), 4);
                        }
                    }catch (CameraAccessException e){
                        e.printStackTrace();
                    }

                    break;
                default:
                    break;
            }
        }
    }

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
            LogHelper.d(TAG, "set orientation event listener enable:" + enable);
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

    private static int getJpegRotation(int cameraId, int orientation) {
        int rotation = 0;
        if (cameraId > 1) {
            cameraId = 0;
        }
        if (orientation != OrientationEventListener.ORIENTATION_UNKNOWN) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            android.hardware.Camera.getCameraInfo(cameraId, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                rotation = (info.orientation - orientation + 360) % 360;
            } else { // back-facing camera
                rotation = (info.orientation + orientation) % 360;
            }
        }
        return rotation;
    }

    //bv wuyonglin add for hd shot 20201013 start
    @Override
    public void onSensorChanged(SensorEvent event) {
        int type = event.sensor.getType();
        if (type == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            if (mManager != null) {
                mManager.setAccData(x, y, z);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onCaptureStart(TotalCaptureResult result) {
        LogHelper.d(TAG, "mManager onCaptureStart");
        Location location = mICameraContext.getLocation();
        mExif = ExifInterface.addExifTags(0, result, location);
        mICameraContext.getSoundPlayback().play(ISoundPlayback.SHUTTER_CLICK);
        //mCaptureCallback.onCaptureCompleted(session, request, result);
    }

    @Override
    public void saveData(byte[] data, int format, String title) {
        if (data != null) {
        String formatTag = mCaptureSurface.getCaptureType();
        LogHelper.i(TAG, "<saveData> data = " + data + ", format = " + format + " formatTag = " + formatTag
                + ", width = " + mPhotoSize.getWidth() + ", height = " + mPhotoSize.getHeight()
                + ", mCaptureDataCallback = " + mCaptureDataCallback + " mJpegRotation = " + mJpegRotation + " mCurrentCameraId = " + mCurrentCameraId);
        if (format == ImageFormat.YUV_420_888 || format == ImageFormat.NV21) {
            if("1".equals(mCurrentCameraId)){
                boolean isMirror = "1".equals(mDataStore.getValue("key_camera_mirror", "1", mDataStore.getCameraScope(Integer.parseInt(mCurrentCameraId))));
                LogHelper.i(TAG,"saveData isMirror = " + isMirror+" mJpegRotation ="+mJpegRotation+" mOrientation ="+mOrientation);
                if (isMirror) {
                    if (mJpegRotation % 180 == 0) {
                        data = YuvEncodeJni.getInstance().RotateYuv(data, format, mPhotoSize.getWidth(), mPhotoSize.getHeight(), 0, true, true);
                    } else {
                        data = YuvEncodeJni.getInstance().RotateYuv(data, format, mPhotoSize.getWidth(), mPhotoSize.getHeight(), 90, true, true);
                        data = YuvEncodeJni.getInstance().RotateYuv(data, format, mPhotoSize.getHeight(), mPhotoSize.getWidth(), 270, true, false);
                    }
                }
            }
            LogHelper.i(TAG,"saveData mJpegRotation = " + mJpegRotation);
            if(CameraUtil.isWaterMarkOn(mDataStore,mActivity)){
                LogHelper.i(TAG,"saveData isWaterMarkOn on + ");
                WaterMarkUtil.yuvAddWaterMark(mActivity,data, mPhotoSize.getWidth(), mPhotoSize.getHeight(), mJpegRotation);
                LogHelper.i(TAG,"saveData isWaterMarkOn on - ");
            }else{
                LogHelper.i(TAG,"saveData isWaterMarkOn off");
            }
            LogHelper.i(TAG,"saveData EncodeYuvToJpeg start! ");
            data = YuvEncodeJni.getInstance().EncodeYuvToJpeg(data, ImageFormat.NV21,
                    mPhotoSize.getWidth(), mPhotoSize.getHeight(), 95, mJpegRotation);
            LogHelper.i(TAG,"saveData EncodeYuvToJpeg end! ");
        }

        if (mCaptureDataCallback != null) {
            DataCallbackInfo info = new DataCallbackInfo();
            info.data = data;
            info.needUpdateThumbnail = true;
            info.needRestartPreview = false;
            info.mBufferFormat = ImageFormat.JPEG;
            info.imageHeight = mPhotoSize.getHeight();
            info.imageWidth = mPhotoSize.getWidth();
            if (ThumbnailHelper.isPostViewSupported()) {
                info.needUpdateThumbnail = false;
            }
            if (ThumbnailHelper.FORMAT_TAG.equalsIgnoreCase(formatTag)) {
                LogHelper.i(TAG,"mCaptureDataCallback.onPostViewCallback(data)");
                mCaptureDataCallback.onPostViewCallback(data);
            } else {
                LogHelper.i(TAG,"saveData mCaptureDataCallback.onDataReceived(data)");
                mCaptureDataCallback.onDataReceived(info);
                LogHelper.i(TAG,"saveData mIsBGServiceEnabled = " + mIsBGServiceEnabled + " mCaptureSurface = " + mCaptureSurface);
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
            //mStart = false;
        }
    }

    private void registerSensors() {
        if (sensorManager == null) {
            LogHelper.d(TAG, "registerSensors ");
            sensorManager = (SensorManager) mActivity.getSystemService(Context.SENSOR_SERVICE);
            Sensor a = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (a != null) {
                sensorManager.registerListener(this, a, SensorManager.SENSOR_DELAY_FASTEST);
            }
        }
    }

    private void unregisterSensorListener() {
        if (sensorManager != null) {
            LogHelper.i(TAG, "unregisterSensorListener");
            sensorManager.unregisterListener(this);
            sensorManager = null;
        }
    }
    //bv wuyonglin add for hd shot 20201013 end
}
