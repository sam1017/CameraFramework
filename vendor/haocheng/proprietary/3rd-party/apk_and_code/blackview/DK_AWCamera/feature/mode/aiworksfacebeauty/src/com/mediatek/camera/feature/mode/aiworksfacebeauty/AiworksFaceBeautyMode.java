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

package com.mediatek.camera.feature.mode.aiworksfacebeauty;
import com.aiworks.android.utils.Util;
//bv wuyonglin add for bug2058 thumbnailView not update right 20200907 start
import com.mediatek.camera.common.exif.ExifInterface;
//bv wuyonglin add for bug2058 thumbnailView not update right 20200907 end
import com.mediatek.camera.WaterMarkUtil;
import com.mediatek.camera.common.relation.Relation;

import android.app.Activity;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.view.View;

import com.mediatek.camera.common.IAppUi;
import com.mediatek.camera.common.IAppUi.AnimationData;
import com.mediatek.camera.common.IAppUiListener.ISurfaceStatusListener;
import com.mediatek.camera.common.ICameraContext;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.CameraSysTrace;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.device.CameraDeviceManagerFactory;
import com.mediatek.camera.common.device.CameraDeviceManagerFactory.CameraApi;
import com.mediatek.camera.common.memory.IMemoryManager;
import com.mediatek.camera.common.memory.MemoryManagerImpl;
import com.mediatek.camera.common.mode.CameraApiHelper;
import com.mediatek.camera.common.mode.CameraModeBase;
import com.mediatek.camera.common.mode.DeviceUsage;

import com.mediatek.camera.feature.mode.aiworksfacebeauty.device.IAiworksFaceBeautyDeviceController;
import com.mediatek.camera.feature.mode.aiworksfacebeauty.device.IAiworksFaceBeautyDeviceController.DataCallbackInfo;
import com.mediatek.camera.feature.mode.aiworksfacebeauty.device.IAiworksFaceBeautyDeviceController.DeviceCallback;
import com.mediatek.camera.feature.mode.aiworksfacebeauty.device.IAiworksFaceBeautyDeviceController.CaptureDataCallback;
import com.mediatek.camera.feature.mode.aiworksfacebeauty.device.IAiworksFaceBeautyDeviceController.PreviewSizeCallback;
import com.mediatek.camera.feature.mode.aiworksfacebeauty.device.AiworksFaceBeautyDeviceControllerFactory;
import com.mediatek.camera.common.mode.photo.device.IDeviceController.CaptureImageSavedCallback;
import com.mediatek.camera.common.relation.StatusMonitor;
import com.mediatek.camera.common.relation.StatusMonitor.StatusChangeListener;
import com.mediatek.camera.common.setting.ISettingManager;
import com.mediatek.camera.common.setting.SettingManagerFactory;
import com.mediatek.camera.common.storage.MediaSaver.MediaSaverListener;
import com.mediatek.camera.common.utils.BitmapCreator;
import com.mediatek.camera.common.utils.CameraUtil;
import com.mediatek.camera.common.utils.Size;
import com.mediatek.camera.common.mode.photo.heif.HeifWriter;
import com.mediatek.camera.feature.mode.aiworksfacebeauty.view.BeautyViewCtrl;

import java.io.IOException;
import java.util.ArrayList;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.mediatek.camera.common.mode.photo.HeifHelper;
import com.mediatek.camera.common.mode.photo.ThumbnailHelper;
import android.util.Log;
import com.mediatek.camera.feature.mode.aiworksfacebeauty.glrenderer.SurfaceTextureRenderer;
import com.mediatek.camera.feature.mode.aiworksfacebeauty.glrenderer.SurfaceTextureListener;
import android.graphics.SurfaceTexture;
import android.view.OrientationEventListener;
import com.aiworks.facesdk.FaceInfo;
import com.mediatek.camera.feature.mode.aiworksfacebeauty.util.FaceOrientationUtil;
import com.aiworks.awfacebeauty.AwFaceInfo;
import com.aiworks.awfacebeauty.AwFaceBeautyApi;
import com.mediatek.camera.feature.mode.aiworksfacebeauty.util.Accelerometer;
import android.hardware.Camera;
import com.aiworks.yuvUtil.YuvEncodeJni;
import com.aiworks.awfacebeauty.AwBeautyShot;
import com.mediatek.camera.feature.mode.aiworksfacebeauty.util.ImageUtil;
import java.nio.ByteBuffer;
import com.mediatek.camera.feature.mode.aiworksfacebeauty.util.ImageSaveUtil;
import java.io.ByteArrayOutputStream;
import com.aiworks.facesdk.AwFaceDetectApi;
import com.mediatek.camera.feature.mode.aiworksfacebeauty.glrenderer.FacePointBeautyTransUtil;
import com.mediatek.camera.feature.mode.aiworksfacebeauty.util.FaceModelUtil;
import android.widget.Toast;

/**
 * Normal photo mode that is used to take normal picture.
 */
public class AiworksFaceBeautyMode extends CameraModeBase implements CaptureDataCallback,
        DeviceCallback, PreviewSizeCallback, IMemoryManager.IMemoryListener,
        CaptureImageSavedCallback {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(AiworksFaceBeautyMode.class.getSimpleName());
    private static final String KEY_MATRIX_DISPLAY_SHOW = "key_matrix_display_show";
    private static final String KEY_PICTURE_SIZE = "key_picture_size";
    private static final String KEY_FORMTAT = "key_format";
    private static final String KEY_DNG = "key_dng";
    private static final String JPEG_CALLBACK = "jpeg callback";
    private static final String POST_VIEW_CALLBACK = "post view callback";
    private static final long DNG_IMAGE_SIZE = 45 * 1024 * 1024;

    protected static final String PHOTO_CAPTURE_START = "start";
    protected static final String PHOTO_CAPTURE_STOP = "stop";
    protected static final String KEY_PHOTO_CAPTURE = "key_photo_capture";

    protected IAiworksFaceBeautyDeviceController mIAiworksFaceBeautyDeviceController;
    protected AiworksFaceBeautyModeHelper mAiworksFaceBeautyModeHelper;
    protected int mCaptureWidth;
    // make sure the picture size ratio = mCaptureWidth / mCaptureHeight not to NAN.
    protected int mCaptureHeight = Integer.MAX_VALUE;
    //the reason is if surface is ready, let it to set to device controller, otherwise
    //if surface is ready but activity is not into resume ,will found the preview
    //can not start preview.
    protected volatile boolean mIsResumed = true;
    private String mCameraId;

    private boolean isShowProgressBar = false;
    private ExifInterface mExif;
    private Activity mActivity;

    private ISurfaceStatusListener mISurfaceStatusListener = new SurfaceChangeListener();
    private ISettingManager mISettingManager;
    private MemoryManagerImpl mMemoryManager;
    private int mPreviewFormat;
    private int mPreviewWidth;
    private int mPreviewHeight;
    //make sure it is in capturing to show the saving UI.
    private int mCapturingNumber = 0;
    private boolean mIsMatrixDisplayShow = false;
    private Object mCaptureNumberSync = new Object();
    private StatusChangeListener mStatusChangeListener = new MyStatusChangeListener();
    private IMemoryManager.MemoryAction mMemoryState = IMemoryManager.MemoryAction.NORMAL;
    protected StatusMonitor.StatusResponder mPhotoStatusResponder;
    private BeautyViewCtrl mBeautyViewCtrl;
    private SurfaceTextureRenderer surfaceTextureRenderer;
    private FaceInfo[] mFaceInfos = null;
    private int mPhoneSensorOrientation;
    //private Accelerometer mAccelerometer;
    private int mjpegRotation = -1;
    private int mFaceDetectOrientation = -1;
    private AwBeautyShot mAwBeautyShot;
    private HandlerThread mBackgroundThread = null;
    private Handler mBackgroundHandler = null;
    private float mDefaultBeatyValue = 0.5f;
    private float mBeautyAlllevel = -1;
    //private float mBeautySmoothlevel = -1;
    private int mCustomBeautyType = -1;
    int status = -1;
    public static final String[] LevelSelectorKey = {
            "small_face",
            "big_eye",
            "bright_eye",
            "big_nose",
            "smooth_face",
            "bright_white",
            "beauty_all"
    };
    private int mTakePictureRotation = -1;

    @Override
    public void init(@Nonnull IApp app, @Nonnull ICameraContext cameraContext,
            boolean isFromLaunch) {
        LogHelper.d(TAG, "[init]+");
		mBeautyViewCtrl = new BeautyViewCtrl(cameraContext);
        super.init(app, cameraContext, isFromLaunch);
        mActivity = app.getActivity();
        mCameraId = getCameraIdByFacing(mDataStore.getValue(
                KEY_CAMERA_SWITCHER, null, mDataStore.getGlobalScope()));
        LogHelper.d(TAG, "[init] mCameraId " + mCameraId);
        // Device controller must be initialize before set preview size, because surfaceAvailable
        // may be called immediately when setPreviewSize.
        AiworksFaceBeautyDeviceControllerFactory deviceControllerFactory = new AiworksFaceBeautyDeviceControllerFactory();
        mIAiworksFaceBeautyDeviceController = deviceControllerFactory.createDeviceController(app.getActivity(),
                mCameraApi, mICameraContext);
        initSettingManager(mCameraId);
        initStatusMonitor();
        prepareAndOpenCamera(false, mCameraId, isFromLaunch);
        ThumbnailHelper.setApp(app);
        mMemoryManager = new MemoryManagerImpl(app.getActivity());
        mAiworksFaceBeautyModeHelper = new AiworksFaceBeautyModeHelper(cameraContext);
        LogHelper.d(TAG, "[init]- ");
        mBeautyViewCtrl.setViewChangeListener(mViewChangeListener);
        mBeautyViewCtrl.init(app);
        mBeautyViewCtrl.onOrientationChanged(mIApp.getGSensorOrientation());
        //bv wuyonglin add for aiworks facebeauty and bokeh 20200722 start
        mBeautyAlllevel = Float.parseFloat(mDataStore.getValue("aiworks_beauty_all_level","0.5",mDataStore.getCameraScope(Integer.parseInt(mCameraId))));
        mBeautyViewCtrl.showView(Integer.parseInt(mCameraId));
        if (mBackgroundThread == null) {
            mBackgroundThread = new HandlerThread("CameraBackground");
        }
        mBackgroundThread.start();
        if(mBackgroundHandler == null){
            mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
        }
        mBackgroundHandler.post(new Runnable() {
            @Override
            public void run() {
                LogHelper.i(TAG,"mBackgroundHandler faceDetectInit start ");
                status = AwFaceDetectApi.init(FaceModelUtil.getFaceModelPath(app.getActivity()));
                LogHelper.i(TAG,"mBackgroundHandler faceDetectInit status = "+status);
            }
        });
        //bv wuyonglin add for aiworks facebeauty and bokeh 20200722 end
        mIApp.getAppUi().setFaceBeautyViewListener(mFaceBeautyViewListener);
    }

    @Override
    public void resume(@Nonnull DeviceUsage deviceUsage) {
        super.resume(deviceUsage);
        LogHelper.d(TAG, "[resume]+");
        mIsResumed = true;
        initSettingManager(mCameraId);
        initStatusMonitor();
        mMemoryManager.addListener(this);
        mMemoryManager.initStateForCapture(
                mICameraContext.getStorageService().getCaptureStorageSpace());
        mMemoryState = IMemoryManager.MemoryAction.NORMAL;
        mIAiworksFaceBeautyDeviceController.queryCameraDeviceManager();
        prepareAndOpenCamera(false, mCameraId, false);
    }

    @Override
    public void onOrientationChanged(int orientation) {
	mPhoneSensorOrientation = orientation;
    }

    @Override
    public void pause(@Nullable DeviceUsage nextModeDeviceUsage) {
        LogHelper.i(TAG, "[pause]+");
        /*-- modify by bv liangchangwei for fixbug 3680--*/
        mIsResumed = false;
        super.pause(nextModeDeviceUsage);
        mMemoryManager.removeListener(this);
        if(CameraUtil.MTKCAM_AIWORKS_HDR_SUPPORT){
            if(isShowProgressBar){
                mIApp.getAppUi().HideShutterProgressBar();
                mIApp.getAppUi().setPictureProcessing(false);
                isShowProgressBar = false;
            }
        }
        //clear the surface listener
        mIApp.getAppUi().clearPreviewStatusListener(mISurfaceStatusListener);
        if (mNeedCloseCameraIds.size() > 0) {
            prePareAndCloseCamera(needCloseCameraSync(), mCameraId);
            recycleSettingManager(mCameraId);
        } else if (mNeedCloseSession){
            clearAllCallbacks(mCameraId);
            mIAiworksFaceBeautyDeviceController.closeSession();
        } else{
            clearAllCallbacks(mCameraId);
            mIAiworksFaceBeautyDeviceController.stopPreview();
        }
        //bv wuyonglin add for aiworks facebeauty and bokeh 20200722 start
        if (surfaceTextureRenderer != null) {
            surfaceTextureRenderer.releaseSurfaceTexture();
            surfaceTextureRenderer.release();
            surfaceTextureRenderer = null;
        }
        //bv wuyonglin add for aiworks facebeauty and bokeh 20200722 end
        LogHelper.i(TAG, "[pause]-");
    }

    @Override
    public void unInit() {
        super.unInit();
        mBeautyViewCtrl.unInit();
        mBeautyViewCtrl = null;
        mIAiworksFaceBeautyDeviceController.destroyDeviceController();
        mBackgroundHandler.getLooper().quitSafely();
    }

    @Override
    public boolean onCameraSelected(@Nonnull String newCameraId) {
        LogHelper.i(TAG, "[onCameraSelected] ,new id:" + newCameraId + ",current id:" + mCameraId+" mIsResumed ="+mIsResumed);
        super.onCameraSelected(newCameraId);
        //first need check whether can switch camera or not.
        if (canSelectCamera(newCameraId) && !isShowProgressBar && mIsResumed) {
            doCameraSelect(mCameraId, newCameraId);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onShutterButtonFocus(boolean pressed) {
        return true;
    }

    @Override
    protected boolean doShutterButtonClick() {
        //Storage case
        boolean storageReady = mICameraContext.getStorageService().getCaptureStorageSpace() > 0;
        boolean isDeviceReady = mIAiworksFaceBeautyDeviceController.isReadyForCapture();
        LogHelper.i(TAG, "onShutterButtonClick, is storage ready : " + storageReady + "," +
                "isDeviceReady = " + isDeviceReady);

        if (storageReady && isDeviceReady && mIsResumed
                && mMemoryState != IMemoryManager.MemoryAction.STOP
                && (isShowProgressBar == false)) {
            //trigger capture animation
            startCaptureAnimation();
            mIApp.getAppUi().ShowShutterProgressBar();
            isShowProgressBar = true;
            mPhotoStatusResponder.statusChanged(KEY_PHOTO_CAPTURE, PHOTO_CAPTURE_START);
            updateModeDeviceState(MODE_DEVICE_STATE_CAPTURING);
            disableAllUIExceptionShutter();
            mIAiworksFaceBeautyDeviceController.updateGSensorOrientation(mIApp.getGSensorOrientation());
            mTakePictureRotation = mIApp.getGSensorOrientation();
            LogHelper.i(TAG,"doShutterButtonClick mTakePictureRotation ="+mTakePictureRotation);
            mIAiworksFaceBeautyDeviceController.takePicture(this);
            LogHelper.i(TAG,"doShutterButtonClick showCircleProgressBar");
            mIApp.getAppUi().setPictureProcessing(true);
            mExif = null;
        }
        return true;
    }

    @Override
    public boolean onShutterButtonLongPressed() {
        return false;
    }

    @Override
    public void onDataReceived(DataCallbackInfo dataCallbackInfo) {
        //add by huangfei for shutter button status start
        //bv wuyonglin delete for take picture quick slide second to change mode happened can not connect camera 20200306 start
        //mIApp.getAppUi().applyAllUIEnabled(true);
        //bv wuyonglin delete for take picture quick slide second to change mode happened can not connect camera 20200306 end
        //mOnButtonClick = false;
        //add by huangfei for shutter button status end
        //when mode receive the data, need save it.
        byte[] data = dataCallbackInfo.data;
        int format = dataCallbackInfo.mBufferFormat;
        boolean needUpdateThumbnail = dataCallbackInfo.needUpdateThumbnail;
        boolean needRestartPreview = dataCallbackInfo.needRestartPreview;
        LogHelper.d(TAG, "AwFaceBeauty onDataReceived, data = " + data + ",mIsResumed = " + mIsResumed +
                ",needUpdateThumbnail = " + needUpdateThumbnail + ",needRestartPreview = " +
                needRestartPreview);
        if (data != null) {
            CameraSysTrace.onEventSystrace(JPEG_CALLBACK, true);
        }
        //bv wuyonglin add for aiworks facebeauty and bokeh 20200722 start
        final int picutreW;
        final int picutreH;
        if (mTakePictureRotation % 180 != 0) {
            picutreW = dataCallbackInfo.imageWidth;
            picutreH = dataCallbackInfo.imageHeight;
        } else {
            picutreW = dataCallbackInfo.imageHeight;
            picutreH = dataCallbackInfo.imageWidth;
        }

        mExif = new ExifInterface();
        try {
            mExif.readExif(data);
            //bv wuyonglin add for thumbnail have problem 20200923 start
            mExif.removeCompressedThumbnail();
            //bv wuyonglin add for thumbnail have problem 20200923 end
        } catch (IOException e) {
            LogHelper.e(TAG, "Failed to read EXIF data", e);
        }

        byte[] mJpegBeauty = processBeauty(data, picutreW, picutreH, true);
        //save file first,because save file is in other thread, so will improve the shot to shot
        //performance.
        if (mJpegBeauty != null) {
            if (format == ImageFormat.JPEG) {
                saveData(mJpegBeauty);
        //bv wuyonglin add for aiworks facebeauty and bokeh 20200722 end
            } else if (format == HeifHelper.FORMAT_HEIF){
                //check memory to decide whether it can take next picture.
                //if not, show saving
                ISettingManager.SettingController controller
                  = mISettingManager.getSettingController();
                String dngState = controller.queryValue(KEY_DNG);
                long saveDataSize = data.length;
                if (dngState != null && "on".equalsIgnoreCase(dngState)) {
                    saveDataSize = saveDataSize + DNG_IMAGE_SIZE;
                }
                synchronized (mCaptureNumberSync) {
                    mCapturingNumber ++;
                    mMemoryManager.checkOneShotMemoryAction(saveDataSize);
                }
                HeifHelper heifHelper = new HeifHelper(mICameraContext);
                ContentValues values = heifHelper.getContentValues(dataCallbackInfo.imageWidth,
                        dataCallbackInfo.imageHeight);
                LogHelper.i(TAG, "onDataReceived,heif values =" +values.toString());
                mICameraContext.getMediaSaver().addSaveRequest(data, values, null,
                        mMediaSaverListener, HeifHelper.FORMAT_HEIF);
            }

        }
        //if camera is paused, don't need do start preview and other device related actions.
        if (mIsResumed) {
            //first do start preview in API1.
            if (mCameraApi == CameraApi.API1) {
                if (needRestartPreview && !mIsMatrixDisplayShow) {
                LogHelper.i(TAG, "onDataReceived,startPreview ");
                    mIAiworksFaceBeautyDeviceController.startPreview();
                }
            }
        }
        //update thumbnail
        if (data != null && needUpdateThumbnail) {
            if (format == ImageFormat.JPEG) {
                updateThumbnail(data);
            } else if (format == HeifHelper.FORMAT_HEIF) {
                HeifHelper heifHelper = new HeifHelper(mICameraContext);
                int width = dataCallbackInfo.imageWidth;
                int height = dataCallbackInfo.imageHeight;
                Bitmap thumbnail = heifHelper.createBitmapFromYuv(data,
                        width, height, mIApp.getAppUi().getThumbnailViewWidth());
                mIApp.getAppUi().updateThumbnail(thumbnail);
            }

        }
        if (data != null) {
            CameraSysTrace.onEventSystrace(JPEG_CALLBACK, false);
        }
        //add by huangfei for continuousshot abnormal start
        mIApp.getAppUi().setCaptureStatus(false);
        //add by huangfei for continuousshot abnormal end
    }

    @Override
    public void onPostViewCallback(byte[] data) {
        LogHelper.d(TAG, "[onPostViewCallback] data = " + data + ",mIsResumed = " + mIsResumed);
        CameraSysTrace.onEventSystrace(POST_VIEW_CALLBACK, true, true);
        if (data != null && mIsResumed) {
            //will update the thumbnail
            //bv wuyonglin add for aiworks facebeauty and bokeh 20200722 start
            Bitmap bitmap = mIApp.getAppUi().getPreviewController().getPreviewBitmap(1);
            LogHelper.d(TAG, "[onPostViewCallback] bitmap = " + bitmap + ",bitmap.getWidth() = " + bitmap.getWidth()+" bitmap.getHeight() ="+bitmap.getHeight());
            if (bitmap != null) {
                ByteArrayOutputStream jpgStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, jpgStream);
                byte[] jpgStream1 = jpgStream.toByteArray();
                updateThumbnail(jpgStream1);
            }
            //bv wuyonglin add for aiworks facebeauty and bokeh 20200722 end
            //modify by huangfei for mirror start
            /*Bitmap bitmap = BitmapCreator.createBitmapFromYuv(data,
                    ThumbnailHelper.POST_VIEW_FORMAT,
                    ThumbnailHelper.getThumbnailWidth(),
                    ThumbnailHelper.getThumbnailHeight(),
                    mIApp.getAppUi().getThumbnailViewWidth(),
                    rotation);
            Bitmap bitmap = BitmapCreator.createBitmapFromYuv(data,
                    ThumbnailHelper.POST_VIEW_FORMAT,
                    ThumbnailHelper.getThumbnailWidth(),
                    ThumbnailHelper.getThumbnailHeight(),
                    mIApp.getAppUi().getThumbnailViewWidth(),
                    rotation, mIApp, mICameraContext);
            //modify by huangfei for mirror end
			
            //mIApp.getAppUi().updateThumbnail(bitmap);*/
        }
        CameraSysTrace.onEventSystrace(POST_VIEW_CALLBACK, false, true);
    }

    @Override
    public void onFinishSaved(Uri uri) {
        mMediaSaverListener.onFileSaved(uri);
    }

    @Override
    protected ISettingManager getSettingManager() {
        return mISettingManager;
    }

    @Override
    public void onCameraOpened(String cameraId) {
        updateModeDeviceState(MODE_DEVICE_STATE_OPENED);
        //bv wuyonglin delete for click thumbnail gotoGallery then come back camera top switcher icon posistion happened error change quickly 20200307 start
        //bv wuyonglin add for hdr open from photo mode to other mode should not support continue shot 20200309 start
        Relation relation = AiworksFaceBeautyRestriction.getRestriction().getRelation("on", true);

        mISettingManager.getSettingController().postRestriction(relation);
        //bv wuyonglin add for hdr open from photo mode to other mode should not support continue shot 20200309 end
        /*mISettingManager.getSettingController().addViewEntry();
        mISettingManager.getSettingController().refreshViewEntry();*/
        //bv wuyonglin delete for click thumbnail gotoGallery then come back camera top switcher icon posistion happened error change quickly 20200307 end
    }

    @Override
    public void beforeCloseCamera() {
        updateModeDeviceState(MODE_DEVICE_STATE_CLOSED);
    }

    @Override
    public void afterStopPreview() {
        updateModeDeviceState(MODE_DEVICE_STATE_OPENED);
    }

    @Override
    public void onPreviewCallback(byte[] data, int format) {
        // Because we want use the one preview frame for doing switch camera animation
        // so will dismiss the later frames.
        // The switch camera data will be restore to null when camera close done.
        if (!mIsResumed) {
            return;
        }
            //modify by huangfei for shutter button status start
            //bv wuyonglin modify for take picture top quick switcher can not open 20200306 start
            if (!mIsMatrixDisplayShow){
            //if (!mIsMatrixDisplayShow&&!mOnButtonClick) {
            //bv wuyonglin modify for take picture top quick switcher can not open 20200306 end
                mIApp.getAppUi().applyAllUIEnabled(true);
            }
            //modify by huangfei for shutter button status end
                LogHelper.d(TAG, "[onPreviewCallback] to onPreviewStarted format ="+format+" NV21 ="+(format == ImageFormat.NV21)+" mPreviewWidth ="+mPreviewWidth+" mPreviewHeight ="+mPreviewHeight+" ImageFormat.NV21 ="+ImageFormat.NV21+" data ="+data);
        mIApp.getAppUi().onPreviewStarted(mCameraId);
        mIApp.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LogHelper.d(TAG, "[stopAllAnimations] run");
                stopCaptureAnimation();
            }
        });
        updateModeDeviceState(MODE_DEVICE_STATE_PREVIEWING);
        mPreviewFormat = format;
    }

    //bv wuyonglin add for aiworks facebeauty and bokeh 20200722 start
    @Override
    public void onAiworksPreviewCallback(byte[] data, int format, int width, int height) {
        mjpegRotation = getJpegRotation(mPhoneSensorOrientation);
        mFaceDetectOrientation = FaceOrientationUtil.dupFaceOrientation(mjpegRotation);
	doFaceDetectYUV(data, mPreviewWidth, mPreviewHeight, mFaceDetectOrientation);
    }
    //bv wuyonglin add for aiworks facebeauty and bokeh 20200722 end

    @Override
    public void onPreviewSizeReady(Size previewSize) {
        LogHelper.i(TAG, "[onPreviewSizeReady] previewSize: " + previewSize.toString());
        updatePictureSizeAndPreviewSize(previewSize);
    }

    private void onPreviewSizeChanged(int width, int height) {
        mPreviewWidth = width;
        mPreviewHeight = height;
        LogHelper.i(TAG, "doFaceDetectYUV [onPreviewSizeChanged] mPreviewWidth: " + mPreviewWidth+" mPreviewHeight ="+mPreviewHeight+" mISurfaceStatusListener ="+mISurfaceStatusListener);
        mIApp.getAppUi().setPreviewSize(mPreviewWidth, mPreviewHeight, mISurfaceStatusListener);
        /*if (surfaceTextureRenderer != null) {
            surfaceTextureRenderer.acquireSurfaceTexture(width, height, mSurfaceTextureListener);
        }*/
    }

    private void prepareAndOpenCamera(boolean needOpenCameraSync, String cameraId,
            boolean needFastStartPreview) {
        mCameraId = cameraId;
        StatusMonitor statusMonitor = mICameraContext.getStatusMonitor(mCameraId);
        statusMonitor.registerValueChangedListener(KEY_PICTURE_SIZE, mStatusChangeListener);
        statusMonitor.registerValueChangedListener(KEY_FORMTAT, mStatusChangeListener);
        statusMonitor.registerValueChangedListener(KEY_MATRIX_DISPLAY_SHOW, mStatusChangeListener);

        //before open camera, prepare the device callback and size changed callback.
        mIAiworksFaceBeautyDeviceController.setDeviceCallback(this);
        mIAiworksFaceBeautyDeviceController.setPreviewSizeReadyCallback(this);

        //prepare device info.
        AiworksFaceBeautyDeviceInfo info = new AiworksFaceBeautyDeviceInfo();
        info.setCameraId(mCameraId);
        info.setSettingManager(mISettingManager);
        info.setNeedOpenCameraSync(needOpenCameraSync);
        info.setNeedFastStartPreview(needFastStartPreview);
        mIAiworksFaceBeautyDeviceController.openCamera(info);
    }

    private void prePareAndCloseCamera(boolean needSync, String cameraId) {
        clearAllCallbacks(cameraId);
        mIAiworksFaceBeautyDeviceController.closeCamera(needSync);
        mIsMatrixDisplayShow = false;
        //reset the preview size
        mPreviewWidth = 0;
        mPreviewHeight = 0;
        LogHelper.i(TAG, " [prePareAndCloseCamera] mPreviewWidth: " + mPreviewWidth+" mPreviewHeight ="+mPreviewHeight);
    }

    private void clearAllCallbacks(String cameraId) {
        mIAiworksFaceBeautyDeviceController.setPreviewSizeReadyCallback(null);
        StatusMonitor statusMonitor = mICameraContext.getStatusMonitor(cameraId);
        statusMonitor.unregisterValueChangedListener(KEY_PICTURE_SIZE, mStatusChangeListener);
        statusMonitor.unregisterValueChangedListener(KEY_FORMTAT, mStatusChangeListener);
        statusMonitor.unregisterValueChangedListener(
                KEY_MATRIX_DISPLAY_SHOW, mStatusChangeListener);
    }

    private void initSettingManager(String cameraId) {
        CameraSysTrace.onEventSystrace("photoMode.initSettingManager", true);
        SettingManagerFactory smf = mICameraContext.getSettingManagerFactory();
        mISettingManager = smf.getInstance(
                cameraId,
                getModeKey(),
                ModeType.PHOTO,
                mCameraApi);
        CameraSysTrace.onEventSystrace("photoMode.initSettingManager", false);
    }

    private void recycleSettingManager(String cameraId) {
        CameraSysTrace.onEventSystrace("photoMode.recycleSettingManager", true);
        mICameraContext.getSettingManagerFactory().recycle(cameraId);
        CameraSysTrace.onEventSystrace("photoMode.recycleSettingManager", false);
    }

    private boolean canSelectCamera(@Nonnull String newCameraId) {
        boolean value = true;

        if (newCameraId == null || mCameraId.equalsIgnoreCase(newCameraId)) {
            value = false;
        }
        LogHelper.d(TAG, "[canSelectCamera] +: " + value);
        return value;
    }

    private void doCameraSelect(String oldCamera, String newCamera) {
        mIApp.getAppUi().applyAllUIEnabled(false);
        mIApp.getAppUi().onCameraSelected(newCamera);
        prePareAndCloseCamera(true, oldCamera);
        recycleSettingManager(oldCamera);
        initSettingManager(newCamera);
        //bv wuyonglin add for aiworks facebeauty and bokeh 20200722 start
        if (surfaceTextureRenderer != null) {
            surfaceTextureRenderer.releaseSurfaceTexture();
            LogHelper.e(TAG, "[doCameraSelect] release surfaceTextureRenderer ="+surfaceTextureRenderer+" newCamera ="+newCamera);
            surfaceTextureRenderer.release();
            surfaceTextureRenderer = null;
        }
	if (mCustomBeautyType == -1) {
        mBeautyAlllevel = Float.parseFloat(mDataStore.getValue("aiworks_beauty_all_level","0.5",mDataStore.getCameraScope(Integer.parseInt(newCamera))));
        mBeautyViewCtrl.unInit();
        mBeautyViewCtrl.showView(Integer.parseInt(newCamera));
	} else {
        //mBeautySmoothlevel = Float.parseFloat(mDataStore.getValue("aiworks_beauty_level_4",mDefaultBeatyValue+"",mDataStore.getCameraScope(Integer.parseInt(newCamera))));
        mBeautyViewCtrl.unInit();
        mBeautyViewCtrl.showView(Integer.parseInt(newCamera));
	}
        //bv wuyonglin add for aiworks facebeauty and bokeh 20200722 end
        prepareAndOpenCamera(false, newCamera, true);
    }

    private MediaSaverListener mMediaSaverListener = new MediaSaverListener() {

        @Override
        public void onFileSaved(Uri uri) {
            mIApp.notifyNewMedia(uri, true);
            synchronized (mCaptureNumberSync) {
                mCapturingNumber--;
                if (mCapturingNumber == 0) {
                    mMemoryState = IMemoryManager.MemoryAction.NORMAL;
                    mIApp.getAppUi().hideSavingDialog();
                    mIApp.getAppUi().applyAllUIVisibility(View.VISIBLE);
                }

                if(isShowProgressBar){
                    mIApp.getAppUi().setPictureProcessing(false);
                    mIApp.getAppUi().HideShutterProgressBar();
                    isShowProgressBar = false;
                }
            }
            LogHelper.i(TAG, "[onFileSaved] uri = " + uri + ", mCapturingNumber = "
                    + mCapturingNumber);
        }
    };

    private void startCaptureAnimation() {
        mIApp.getAppUi().animationStart(IAppUi.AnimationType.TYPE_CAPTURE, null);
    }

    private void stopCaptureAnimation() {
        mIApp.getAppUi().animationEnd(IAppUi.AnimationType.TYPE_CAPTURE);
    }

    private void updatePictureSizeAndPreviewSize(Size previewSize) {
        ISettingManager.SettingController controller = mISettingManager.getSettingController();
        String size = controller.queryValue(KEY_PICTURE_SIZE);
        LogHelper.i(TAG, "[onPreviewSizeReady] size: " + size+" mIsResumed ="+mIsResumed);
        if (size != null && mIsResumed) {
            String[] pictureSizes = size.split("x");
            mCaptureWidth = Integer.parseInt(pictureSizes[0]);
            mCaptureHeight = Integer.parseInt(pictureSizes[1]);
            mIAiworksFaceBeautyDeviceController.setPictureSize(new Size(mCaptureWidth, mCaptureHeight));
            int width = previewSize.getWidth();
            int height = previewSize.getHeight();
            LogHelper.i(TAG, "[updatePictureSizeAndPreviewSize] picture size: " + mCaptureWidth +
                    " X" + mCaptureHeight + ",current preview size:" + mPreviewWidth + " X " +
                    mPreviewHeight + ",new value :" + width + " X " + height);
            if (width != mPreviewWidth || height != mPreviewHeight) {
                onPreviewSizeChanged(width, height);
            }
        }

    }

    private void initStatusMonitor() {
        StatusMonitor statusMonitor = mICameraContext.getStatusMonitor(mCameraId);
        mPhotoStatusResponder = statusMonitor.getStatusResponder(KEY_PHOTO_CAPTURE);
    }


    private void saveData(byte[] data) {
        if (data != null) {
            //check memory to decide whether it can take next picture.
            //if not, show saving
            ISettingManager.SettingController controller = mISettingManager.getSettingController();
            String dngState = controller.queryValue(KEY_DNG);
            long saveDataSize = data.length;
            if (dngState != null && "on".equalsIgnoreCase(dngState)) {
                saveDataSize = saveDataSize + DNG_IMAGE_SIZE;
            }
            synchronized (mCaptureNumberSync) {
                mCapturingNumber ++;
                mMemoryManager.checkOneShotMemoryAction(saveDataSize);
            }
            String fileDirectory = mICameraContext.getStorageService().getFileDirectory();
            Size exifSize = CameraUtil.getSizeFromExif(data);
            ContentValues contentValues = mAiworksFaceBeautyModeHelper.createContentValues(data,
                    fileDirectory, exifSize.getWidth(), exifSize.getHeight());
            //bv wuyonglin add for bug2058 thumbnailView not update right 20200907 start
            //String FilePath = contentValues.getAsString(MediaStore.Images.ImageColumns.DATA);
            //addExif(FilePath, data);

            mICameraContext.getMediaSaver().addSaveRequest(data, contentValues, null,
                    mMediaSaverListener, false, mExif);
            //bv wuyonglin add for bug2058 thumbnailView not update right 20200907 end
        }
    }

    public void addExif(String path, byte[] data){
        LogHelper.i(TAG,"addExif path = " + path + " mExif = " + mExif + " data.size = " + data.length);
        //Util.writeImage(path, mExif, data);            //bv wuyonglin delete for bug2058 thumbnailView not update right 20200907
    }

    private void disableAllUIExceptionShutter() {
        mIApp.getAppUi().applyAllUIEnabled(false);
        mIApp.getAppUi().setUIEnabled(IAppUi.SHUTTER_BUTTON, true);
        mIApp.getAppUi().setUIEnabled(IAppUi.SHUTTER_TEXT, false);
    }

    private void updateThumbnail(byte[] data) {
	//bv wuyonglin add for aiworks facebeauty and bokeh 20200722 start
	int orientation = ImageUtil.getJpegOrientation(data);
        Bitmap bitmap = BitmapCreator.createBitmapFromJpeg(data, mIApp.getAppUi()
                .getThumbnailViewWidth());
	//ImageSaveUtil.compressToFile1(mIApp.getActivity(), bitmap);
	Bitmap bitmapRotation = BitmapCreator.rotateBitmap(bitmap, mPhoneSensorOrientation);
	//ImageSaveUtil.compressToFile2(mIApp.getActivity(), bitmapRotation);
	boolean isMirror = "1".equals(mDataStore.getValue("key_camera_mirror", "1", mDataStore.getCameraScope(Integer.parseInt(mCameraId))));
	int a = CameraUtil.getJpegRotationFromDeviceSpec(Integer.parseInt(mCameraId),
                    mIApp.getGSensorOrientation(), mIApp.getActivity());
            LogHelper.d(TAG, "onPostViewCallback new BitmapCreator updateThumbnail3 mFaceDetectOrientation ="+mFaceDetectOrientation+" mjpegRotation ="+mjpegRotation+" mPhoneSensorOrientation ="+mPhoneSensorOrientation+" a="+a);
            LogHelper.d(TAG, "onPostViewCallback new BitmapCreator updateThumbnail3 isMirror ="+isMirror+" orientation ="+orientation+" bitmap.getWidth() ="+bitmap.getWidth()+" bitmap.getHeight() ="+bitmap.getHeight());
        mIApp.getAppUi().updateThumbnail(BitmapCreator.setMirror(bitmapRotation, !isMirror, orientation));
	//bv wuyonglin add for aiworks facebeauty and bokeh 20200722 end
    }

    @Override
    public void onMemoryStateChanged(IMemoryManager.MemoryAction state) {
        if (state == IMemoryManager.MemoryAction.STOP && mCapturingNumber != 0) {
            //show saving
            LogHelper.d(TAG, "memory low, show saving");
            mIApp.getAppUi().showSavingDialog(null, true);
            mIApp.getAppUi().applyAllUIVisibility(View.INVISIBLE);
        }
    }

    /**
     * surface changed listener.
     */
    private class SurfaceChangeListener implements ISurfaceStatusListener {

        @Override
        public void surfaceAvailable(Object surfaceObject, int width, int height) {
            LogHelper.i(TAG, "onSurfaceTextureAvailable surfaceAvailable,device controller = " + mIAiworksFaceBeautyDeviceController
                    + ",w = " + width + ",h = " + height+" mIsResumed ="+mIsResumed);
            LogHelper.i(TAG, "onSurfaceTextureAvailable surfaceAvailable, surfaceObject ="+surfaceObject+" isSurfaceTexture ="+(surfaceObject instanceof SurfaceTexture)+" mIApp.getAppUi().getPreviewTextureView().getSurfaceTexture() ="+mIApp.getAppUi().getPreviewTextureView().getSurfaceTexture()+" surfaceTextureRenderer ="+surfaceTextureRenderer);
            //bv wuyonglin add for aiworks facebeauty and bokeh 20200722 start
            if (surfaceObject != null && surfaceObject instanceof SurfaceTexture && surfaceTextureRenderer == null && mIsResumed) {//bv wuyonglin modify for monkey test exception failed 20210202
            LogHelper.i(TAG, "onSurfaceTextureAvailable surfaceAvailable, surfaceTextureRenderer ="+surfaceTextureRenderer);
            surfaceTextureRenderer = new SurfaceTextureRenderer(mIApp.getAppUi().getPreviewTextureView(), mIApp.getActivity(), (SurfaceTexture) surfaceObject);
            LogHelper.i(TAG, "onSurfaceTextureAvailable surfaceAvailable, final surfaceTextureRenderer ="+surfaceTextureRenderer+" mCustomBeautyType ="+mCustomBeautyType);
            surfaceTextureRenderer.acquireSurfaceTexture(width, height , mSurfaceTextureListener);
            }
            /*if (mModeHandler != null) {
                mModeHandler.post(new Runnable() {
                    @Override
                    public void run() {
            if (mIAiworksFaceBeautyDeviceController != null && mIsResumed) {
                mIAiworksFaceBeautyDeviceController.updatePreviewSurface(surfaceObject);
            }
        }
                });
            }*/
            //bv wuyonglin add for aiworks facebeauty and bokeh 20200722 end
        }

        @Override
        public void surfaceChanged(Object surfaceObject, int width, int height) {
            LogHelper.i(TAG, "onSurfaceTextureAvailable surfaceChanged, device controller = " + mIAiworksFaceBeautyDeviceController
                    + ",w = " + width + ",h = " + height);
            LogHelper.i(TAG, "onSurfaceTextureAvailable surfaceChanged, surfaceObject ="+surfaceObject+" isSurfaceTexture ="+(surfaceObject instanceof SurfaceTexture));
            //bv wuyonglin add for aiworks facebeauty and bokeh 20200722 start
            if (surfaceObject != null && surfaceObject instanceof SurfaceTexture) {
            LogHelper.i(TAG, "onSurfaceTextureAvailable surfaceChanged, surfaceTextureRenderer ="+surfaceTextureRenderer);
            //bv wuyonglin add for bug3529 20210126 start
            if (surfaceTextureRenderer == null) {
            //bv wuyonglin add for bug3529 20210126 end
            surfaceTextureRenderer = new SurfaceTextureRenderer(mIApp.getAppUi().getPreviewTextureView(), mIApp.getActivity(), (SurfaceTexture) surfaceObject);
            surfaceTextureRenderer.acquireSurfaceTexture(width, height , mSurfaceTextureListener);
            //bv wuyonglin add for bug3529 20210126 start
            } else {
            surfaceTextureRenderer.updateSurfaceTextureSize(width, height);
            }
            //bv wuyonglin add for bug3529 20210126 end
            }
            /*if (mModeHandler != null) {
                mModeHandler.post(new Runnable() {
                    @Override
                    public void run() {
            if (mIAiworksFaceBeautyDeviceController != null && mIsResumed) {
                mIAiworksFaceBeautyDeviceController.updatePreviewSurface(surfaceObject);
            }
        }
                });
            }*/
            //bv wuyonglin add for aiworks facebeauty and bokeh 20200722 end
        }

        @Override
        public void surfaceDestroyed(Object surfaceObject, int width, int height) {
            //LogHelper.d(TAG, "surfaceDestroyed,device controller = " + mIAiworksFaceBeautyDeviceController);
            LogHelper.d(TAG, "onSurfaceTextureAvailable surfaceDestroyed,surfaceTextureRenderer device controller = " + mIAiworksFaceBeautyDeviceController+" surfaceTextureRenderer ="+surfaceTextureRenderer);
            //bv wuyonglin add for aiworks facebeauty and bokeh 20200722 start
            if (surfaceTextureRenderer != null) {
                surfaceTextureRenderer.releaseSurfaceTexture();
                surfaceTextureRenderer.release();
                surfaceTextureRenderer = null;
            }
            //bv wuyonglin add for aiworks facebeauty and bokeh 20200722 end
        }
    }

    /**
     * Status change listener implement.
     */
    private class MyStatusChangeListener implements StatusChangeListener {
        @Override
        public void onStatusChanged(String key, String value) {
            LogHelper.i(TAG, "[onStatusChanged] key = " + key + ",value = " + value+" mPreviewWidth ="+mPreviewWidth+" mPreviewHeight ="+mPreviewHeight);
            /*-- modify by bv liangchangwei for fixbug 3680--*/
            if (KEY_PICTURE_SIZE.equalsIgnoreCase(key)&&mIsResumed) {
                String[] sizes = value.split("x");
                mCaptureWidth = Integer.parseInt(sizes[0]);
                mCaptureHeight = Integer.parseInt(sizes[1]);
                mIAiworksFaceBeautyDeviceController.setPictureSize(new Size(mCaptureWidth, mCaptureHeight));
                Size previewSize = mIAiworksFaceBeautyDeviceController.getPreviewSize((double) mCaptureWidth /
                        mCaptureHeight);
                int width = previewSize.getWidth();
                int height = previewSize.getHeight();
                LogHelper.i(TAG, "[onStatusChanged] width = " + width + ", height = " + height+" mCaptureWidth ="+mCaptureWidth+" mCaptureHeight ="+mCaptureHeight);
                if (width != mPreviewWidth || height != mPreviewHeight) {
                    onPreviewSizeChanged(width, height);
                }
            } else if (KEY_MATRIX_DISPLAY_SHOW.equals(key)) {
                mIsMatrixDisplayShow = "true".equals(value);
            } else if (KEY_FORMTAT.equalsIgnoreCase(key)) {
                mIAiworksFaceBeautyDeviceController.setFormat(value);
                LogHelper.i(TAG, "[onStatusChanged] key = " + key + ", set sCaptureFormat = " + value);
            }
        }
    }
    private BeautyViewCtrl.ViewChangeListener mViewChangeListener
            = new BeautyViewCtrl.ViewChangeListener() {
        @Override
        public void onBeautyLevelChanged(float level,int beautyType) {
                LogHelper.i(TAG, "[onBeautyLevelChanged] level = " + level + ", beautyType = " + beautyType);
            //if(mCurrentSelect >= 0) {
                if(beautyType == -1) {
                    mBeautyAlllevel = level;
                    for(int i = 0; i < AwFaceBeautyApi.BEAUTY_ALL; i++) {
                        if (surfaceTextureRenderer != null) {
                            surfaceTextureRenderer.getFaceBeautyDrawer().setBeautyLevel(i, level);
                        }
                    }
                            mDataStore.setValue("aiworks_beauty_all_level",level+"",mDataStore.getCameraScope(Integer.parseInt(mCameraId)),false);
                } else {
                        if (surfaceTextureRenderer != null) {
                            surfaceTextureRenderer.getFaceBeautyDrawer().setBeautyLevel(beautyType, level);
                            mDataStore.setValue("aiworks_beauty_level_"+beautyType+"",level+"",mDataStore.getCameraScope(Integer.parseInt(mCameraId)),false);
                        }
                }

            //}
            //mIAiworksFaceBeautyDeviceController.setBeautyParameter(smooth,whilen,thin,bigeye,brighteye,bignose);
        }

        @Override
        public void onBeautyModeChanged(int beautyType) {
                LogHelper.i(TAG, "[onBeautyLevelChanged]  beautyType = " + beautyType);
                mCustomBeautyType = beautyType;
                if(beautyType == -1) {
                    for(int i = 0; i < AwFaceBeautyApi.BEAUTY_ALL; i++) {
                        if (surfaceTextureRenderer != null) {
                            surfaceTextureRenderer.getFaceBeautyDrawer().setBeautyLevel(i, mBeautyAlllevel);
                        }
                    }
                } else {
                    for(int i = 0; i < AwFaceBeautyApi.BEAUTY_ALL; i++) {
                        if (surfaceTextureRenderer != null) {
                            surfaceTextureRenderer.getFaceBeautyDrawer().setBeautyLevel(i, Float.parseFloat(mDataStore.getValue("aiworks_beauty_level_"+i+"",
					mDefaultBeatyValue+"",mDataStore.getCameraScope(Integer.parseInt(mCameraId)))));
                        }
                    }
                }
        }
    };

    //bv wuyonglin add for aiworks facebeauty and bokeh 20200722 start
    private SurfaceTextureListener mSurfaceTextureListener = new SurfaceTextureListener() {

        public void onSurfaceTextureAvailable(final SurfaceTexture surface) {
            LogHelper.d(TAG, "FaceBeautyDrawer new onSurfaceTextureAvailable surface ="+surface);
		if (mCustomBeautyType == -1) {
                    for(int i = 0; i < AwFaceBeautyApi.BEAUTY_ALL; i++) {
                        if (surfaceTextureRenderer != null) {
                            surfaceTextureRenderer.getFaceBeautyDrawer().setBeautyLevel(i, mBeautyAlllevel);
                        }
                    }
		} else {
                    for(int i = 0; i < AwFaceBeautyApi.BEAUTY_ALL; i++) {
                        if (surfaceTextureRenderer != null) {
                            surfaceTextureRenderer.getFaceBeautyDrawer().setBeautyLevel(i, Float.parseFloat(mDataStore.getValue("aiworks_beauty_level_"+i+"",
					mDefaultBeatyValue+"",mDataStore.getCameraScope(Integer.parseInt(mCameraId)))));
                        }
                    }
		}
            if (mModeHandler != null) {
                mModeHandler.post(new Runnable() {
                    @Override
                    public void run() {
                    	LogHelper.d(TAG, "FaceBeautyDrawer new onSurfaceTextureAvailable mIAiworksFaceBeautyDeviceController ="+mIAiworksFaceBeautyDeviceController+" mIsResumed ="+mIsResumed);
                    	if (mIAiworksFaceBeautyDeviceController != null && mIsResumed) {
                    		mIAiworksFaceBeautyDeviceController.updatePreviewSurface(surface);
                    	}
                    }
                });
            }
        }
    };

    /**
    *Call real-time face detection function to return current face information
     */
    private void doFaceDetectYUV(byte[] data, int previewWidth, int previewHeight, int orientation) {
//LogHelper.i(TAG,"doFaceDetectYUV data = "+data+" previewWidth ="+previewWidth+" previewHeight ="+previewHeight+" orientation ="+orientation+" surfaceTextureRenderer ="+surfaceTextureRenderer);
        /*if (data != null) {
            mFaceInfos = FaceDetectApi.faceDetectYUV(data, FaceDetectApi.RESIZE_320,
                    previewWidth, previewHeight, orientation);
        }

        if (mFaceInfos == null || mFaceInfos.length == 0) {
            LogHelper.e(TAG,"doFaceDetectYUV mFaceInfos = null");
            surfaceTextureRenderer.getFaceBeautyDrawer().updateFaceInfo(null);
            return;
        }
        //surfaceTextureRenderer.getFaceBeautyDrawer().setBeautyLevel(AwFaceBeautyApi.BEAUTY_ALL, level);
        for(int i = 0; i < AwFaceBeautyApi.BEAUTY_ALL; i++) {
            surfaceTextureRenderer.getFaceBeautyDrawer().setBeautyLevel(i, 1.0f);
        }

LogHelper.i(TAG,"doFaceDetectYUV mFaceInfos = "+mFaceInfos+" mCameraId ="+mCameraId);
        AwFaceInfo[] awFaceInfos = new AwFaceInfo[mFaceInfos.length];
        for (int i = 0; i < mFaceInfos.length; i++) {
LogHelper.i(TAG,"doFaceDetectYUV i = "+i);
            AwFaceInfo mFrameFaceInfo = new AwFaceInfo();
            mFrameFaceInfo.gender = 1;
            mFrameFaceInfo.imgWidth = previewWidth;
            mFrameFaceInfo.imgHeight = previewHeight;
            mFrameFaceInfo.pointItemCount = mFaceInfos[i].points.length;
            mFrameFaceInfo.facePoints = new float[mFaceInfos[i].points.length];
            FaceOrientationUtil.dupSoftFacePointsFrame(mFaceInfos[i].points, mFrameFaceInfo.facePoints,  //转换人脸坐标
                    mFrameFaceInfo.pointItemCount,
                    mFrameFaceInfo.imgWidth, mFrameFaceInfo.imgHeight,
                    orientation, "1".equals(mCameraId));
            awFaceInfos[i] = mFrameFaceInfo;
        }

        surfaceTextureRenderer.getFaceBeautyDrawer().updateFaceInfo(awFaceInfos);*/
        if (status != 0) {
            LogHelper.i(TAG,"doFaceDetectYUV status not ok return ");
            return;
        }
        if (data != null) {
            mFaceInfos = AwFaceDetectApi.detectBuffer(data,
                    previewWidth, previewHeight, AwFaceDetectApi.PIX_FMT_NV21, orientation, AwFaceDetectApi.RESIZE_320,10,false);
        }
	if (surfaceTextureRenderer == null) {
            LogHelper.i(TAG,"doFaceDetectYUV surfaceTextureRenderer null return ");
            return;
	}
        if (mFaceInfos == null || mFaceInfos.length == 0) {
            //LogHelper.e(TAG,"mFaceInfos = null surfaceTextureRenderer ="+surfaceTextureRenderer);
            if (surfaceTextureRenderer != null) {
            surfaceTextureRenderer.getFaceBeautyDrawer().updateFaceInfo(null);
            }
            return;
        }

        /*for(int i = 0; i < AwFaceBeautyApi.BEAUTY_ALL; i++) {
            if (surfaceTextureRenderer != null) {
            surfaceTextureRenderer.getFaceBeautyDrawer().setBeautyLevel(i, 1.0f);
            }
        }*/
//LogHelper.i(TAG,"doFaceDetectYUV mFaceInfos = "+mFaceInfos+" mCameraId ="+mCameraId);
        AwFaceInfo[] awFaceInfos = new AwFaceInfo[mFaceInfos.length];
        for (int i = 0; i < mFaceInfos.length; i++) {
            AwFaceInfo mFrameFaceInfo = new AwFaceInfo();
            mFrameFaceInfo.gender = 1;
            mFrameFaceInfo.imgWidth = previewWidth;
            mFrameFaceInfo.imgHeight = previewHeight;
            mFrameFaceInfo.pointItemCount = mFaceInfos[i].points.length;
            mFrameFaceInfo.facePoints = new float[mFaceInfos[i].points.length];
            mFrameFaceInfo.opt = 1;
            //FacePointBeautyTransUtil.dupFacePointsFrame(mFaceInfos[i].points, mFrameFaceInfo.facePoints,  //转换人脸坐标
            //        mFrameFaceInfo.pointItemCount,
            //        mFrameFaceInfo.imgWidth, mFrameFaceInfo.imgHeight,
            //        orientation, "1".equals(mCameraId));
            System.arraycopy(mFaceInfos[i].points,0,mFrameFaceInfo.facePoints,0, mFrameFaceInfo.pointItemCount);
            awFaceInfos[i] = mFrameFaceInfo;
        }
	if (surfaceTextureRenderer != null) {
        surfaceTextureRenderer.getFaceBeautyDrawer().updateFaceInfo(awFaceInfos);
	}
    }

    public int getJpegRotation(int orientation) {
        // See android.hardware.Camera.Parameters.setRotation for
        // documentation.
        int rotation = 0;
        if (orientation != OrientationEventListener.ORIENTATION_UNKNOWN) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(Integer.parseInt(mCameraId), info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                rotation = (info.orientation - orientation + 360) % 360;
            } else { // back-facing camera
                rotation = (info.orientation + orientation) % 360;
            }
        }
        return rotation;
    }

    private byte[] processBeauty(byte[] data, int w, int h, boolean isTestYuv) {
        if (data == null || data.length <= 0) {
            return null;
        }
        LogHelper.e(TAG, "AwFaceBeauty Jpeg2Nv21 new data.length ="+data.length+"w ="+w+" h ="+h);

        //ImageSaveUtil.saveFile(mIApp.getActivity(), data, "first.jpg");
        /*Bitmap bitmap = ImageUtil.createSourceBitmap(data, 1, false);
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();*/
        long start = System.currentTimeMillis();

        if (mAwBeautyShot == null) {
            mAwBeautyShot = new AwBeautyShot();
        }
        byte[] jpegBeauty;
        if (isTestYuv) {
            byte[] nv21 = YuvEncodeJni.getInstance().Jpeg2Nv21(data, w, h, 1, 0, false);
            LogHelper.e(TAG, "AwFaceBeauty Jpeg2Nv21 time:"+(System.currentTimeMillis()-start)+" nv21 ="+nv21);

            start = System.currentTimeMillis();
            FaceInfo[] faceInfos = AwFaceDetectApi.detectPicData(nv21, w, h, AwFaceDetectApi.PIX_FMT_NV21,  AwFaceDetectApi.FACE_UP, AwFaceDetectApi.RESIZE_320,false);
            LogHelper.e(TAG, "AwFaceBeauty detectPicData time:"+(System.currentTimeMillis()-start));

            start = System.currentTimeMillis();
            setFaceInfoAndBeautyLevel(faceInfos,w, h);
            byte[] result = mAwBeautyShot.processNv21(nv21, w, h);
            LogHelper.e(TAG, "AwFaceBeauty processNv21 time:"+(System.currentTimeMillis()-start));

            if(CameraUtil.isWaterMarkOn(mDataStore,mActivity)){
                LogHelper.i(TAG,"isWaterMarkOn on + ");
                WaterMarkUtil.yuvAddWaterMark(mActivity,result, w, h, 0);
                LogHelper.i(TAG,"isWaterMarkOn on - ");
            }else{
                LogHelper.i(TAG,"isWaterMarkOn off");
            }

            start = System.currentTimeMillis();
            jpegBeauty = YuvEncodeJni.getInstance().EncodeYuvToJpeg(result, ImageFormat.NV21, w, h, 100, 0);
            LogHelper.e(TAG, "AwFaceBeauty saveFile time:"+(System.currentTimeMillis()-start));

        } else {

            Bitmap bitmap = ImageUtil.createSourceBitmap(data, 1, false);
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();

            byte[] argb = new byte[width * height * 4];
            ByteBuffer byteBuffer = ByteBuffer.wrap(argb);
            bitmap.copyPixelsToBuffer(byteBuffer);
            bitmap.recycle();
            LogHelper.e(TAG, "AwFaceBeauty getArgb time:"+(System.currentTimeMillis()-start));

            start = System.currentTimeMillis();
            FaceInfo[] faceInfos = AwFaceDetectApi.detectPicData(argb, width, height, AwFaceDetectApi.PIX_FMT_RGBA8888,  AwFaceDetectApi.FACE_UP, AwFaceDetectApi.RESIZE_320,false);
            LogHelper.e(TAG, "AwFaceBeauty detectPicData time:"+(System.currentTimeMillis()-start));

            start = System.currentTimeMillis();
            setFaceInfoAndBeautyLevel(faceInfos, width, height);
            byte[] result = mAwBeautyShot.processArgb(argb, width, height);
            LogHelper.e(TAG, "AwFaceBeauty processArgb time:"+(System.currentTimeMillis()-start));

            start = System.currentTimeMillis();
            jpegBeauty = YuvEncodeJni.getInstance().Rgb2Jpeg(result, width, height,100, 1);
            LogHelper.e(TAG, "AwFaceBeauty saveFile time:"+(System.currentTimeMillis()-start));
        }

        /*start = System.currentTimeMillis();
        FaceInfo[] faceInfos = FaceDetectApi.faceDetectBitmap(bitmap, FaceDetectApi.RESIZE_320, FaceDetectApi.FACE_UP);
        LogHelper.e(TAG, "detectPicData time:"+(System.currentTimeMillis()-start)+" width ="+width+" height ="+height);

        start = System.currentTimeMillis();
        byte[] argb = new byte[width * height * 4];
        ByteBuffer byteBuffer = ByteBuffer.wrap(argb);
        bitmap.copyPixelsToBuffer(byteBuffer);
        bitmap.recycle();
        LogHelper.e(TAG, "getArgb time:"+(System.currentTimeMillis()-start));

        start = System.currentTimeMillis();
        setFaceInfoAndBeautyLevel(faceInfos, width, height);
        byte[] result = mAwBeautyShot.processArgb(argb, width, height);
            LogHelper.e(TAG, "processArgb time:"+(System.currentTimeMillis()-start)+" result ="+result+" result.length ="+result.length);
        LogHelper.e(TAG, "processArgb time:"+(System.currentTimeMillis()-start));

        start = System.currentTimeMillis();
        byte[] jpegBeauty = YuvEncodeJni.getInstance().Rgb2Jpeg(result, width, height,100, 1);
        LogHelper.e(TAG, "saveFile time:"+(System.currentTimeMillis()-start));*/

            /*LogHelper.e(TAG, "AwFaceBeauty Jpeg2Nv21 data.length ="+data.length+" width ="+width+" height ="+height);
            byte[] nv21 = YuvEncodeJni.getInstance().Jpeg2Nv21(data, w, h, 1, 0, false);
            LogHelper.e(TAG, "AwFaceBeauty Jpeg2Nv21 time:"+(System.currentTimeMillis()-start)+" nv21 ="+nv21);

            start = System.currentTimeMillis();
            FaceInfo[] faceInfos = FaceDetectApi.faceDetectBitmap(bitmap, FaceDetectApi.RESIZE_320, FaceDetectApi.FACE_UP);
            LogHelper.e(TAG, "AwFaceBeauty faceDetectBitmap time:"+(System.currentTimeMillis()-start)+" faceInfos ="+faceInfos);

            start = System.currentTimeMillis();
            setFaceInfoAndBeautyLevel(faceInfos,w, h);
            byte[] result = mAwBeautyShot.processNv21(nv21, w, h);
            LogHelper.e(TAG, "AwFaceBeauty processNv21 time:"+(System.currentTimeMillis()-start)+" result ="+result+" result.length ="+result.length);

            start = System.currentTimeMillis();
            byte[] jpegBeauty = YuvEncodeJni.getInstance().EncodeYuvToJpeg(result, ImageFormat.NV21, w, h, 100, 0);
            LogHelper.e(TAG, "AwFaceBeauty saveFile time:"+(System.currentTimeMillis()-start));
            LogHelper.d(TAG, "AwFaceBeauty processBeauty, jpegBeauty = " + jpegBeauty+" jpegBeauty.length ="+jpegBeauty.length);*/
            //ImageSaveUtil.saveFile(mIApp.getActivity(), jpegBeauty,"end.jpg");
        return jpegBeauty;
    }


    private void setFaceInfoAndBeautyLevel(FaceInfo[] faceInfos,int w, int h) {
        if (faceInfos != null && faceInfos.length > 0) {
            AwFaceInfo[] awFaceInfos = new AwFaceInfo[faceInfos.length];
            for (int i= 0; i < faceInfos.length; i++) {
            LogHelper.e(TAG, "setFaceInfoAndBeautyLevel i="+i);
                AwFaceInfo shotFaceInfo = new AwFaceInfo();
                shotFaceInfo.gender = 1;
                shotFaceInfo.pointItemCount = faceInfos[i].points.length;
                shotFaceInfo.facePoints = faceInfos[i].points;
                shotFaceInfo.imgWidth = w;
                shotFaceInfo.imgHeight = h;
                shotFaceInfo.opt = 1;
                awFaceInfos[i] = shotFaceInfo;
            }
            mAwBeautyShot.setFaceInfo(awFaceInfos);

        } else {
            LogHelper.e(TAG, "faceInfos is null");
            mAwBeautyShot.setFaceInfo(null);
        }
            LogHelper.e(TAG, "setFaceInfoAndBeautyLevel mCustomBeautyType ="+mCustomBeautyType);
		if (mCustomBeautyType == -1) {
                    for(int i = 0; i < AwFaceBeautyApi.BEAUTY_ALL; i++) {
                            mAwBeautyShot.setBeautyLevel(i, mBeautyAlllevel);
                    }
		} else {
                    for(int i = 0; i < AwFaceBeautyApi.BEAUTY_ALL; i++) {
                            mAwBeautyShot.setBeautyLevel(i, Float.parseFloat(mDataStore.getValue("aiworks_beauty_level_"+i+"",
					mDefaultBeatyValue+"",mDataStore.getCameraScope(Integer.parseInt(mCameraId)))));
                    }
		}
    }
    //bv wuyonglin add for aiworks facebeauty and bokeh 20200722 end

    private IAppUi.FaceBeautyViewListener mFaceBeautyViewListener = new IAppUi.FaceBeautyViewListener() {
        @Override
        public void onConfigFaceBeautyUIVisibility(int visibility) {
                    LogHelper.d(TAG, "applyFaceBeautyViewVisibilityImmediately onConfigFaceBeautyUIVisibility visibility ="+visibility);
                    mIApp.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mBeautyViewCtrl != null && mBeautyViewCtrl.getFaceBeautyView() !=null) {
                                mBeautyViewCtrl.getFaceBeautyView().setVisibility(visibility);
                            }
                        }
                    });
        }
    };
}
