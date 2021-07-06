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

package com.mediatek.camera.feature.mode.aiworksbokeh;
import com.mediatek.camera.common.relation.Relation;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
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

import com.mediatek.camera.feature.mode.aiworksbokeh.device.IAiWorksBokehDeviceController;
import com.mediatek.camera.feature.mode.aiworksbokeh.device.IAiWorksBokehDeviceController.DataCallbackInfo;
import com.mediatek.camera.feature.mode.aiworksbokeh.device.IAiWorksBokehDeviceController.DeviceCallback;
import com.mediatek.camera.feature.mode.aiworksbokeh.device.IAiWorksBokehDeviceController.CaptureDataCallback;
import com.mediatek.camera.feature.mode.aiworksbokeh.device.IAiWorksBokehDeviceController.PreviewSizeCallback;
import com.mediatek.camera.feature.mode.aiworksbokeh.device.AiWorksBokehDeviceControllerFactory;
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
import com.mediatek.camera.feature.mode.aiworksbokeh.view.AiWorksBokehViewCtrl;
import java.util.ArrayList;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.mediatek.camera.common.mode.photo.HeifHelper;
import com.mediatek.camera.common.mode.photo.ThumbnailHelper;
import android.util.Log;
import com.mediatek.camera.common.relation.Relation;
import com.mediatek.camera.feature.mode.aiworksbokeh.AiWorksBokehRestriction;
import com.mediatek.camera.feature.mode.aiworksbokeh.glrenderer.SurfaceTextureRenderer;
import com.aiworks.android.portrait.PortraitEffect;
import android.os.Environment;
import android.graphics.SurfaceTexture;
import com.mediatek.camera.feature.mode.aiworksbokeh.glrenderer.SurfaceTextureListener;
import android.view.TextureView;
import android.graphics.YuvImage;
import java.io.FileOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import android.graphics.Rect;

//add by huangfeifor front bokeh start
import com.mediatek.camera.Config;
//add by huangfeifor front bokeh end
//bv wuyonglin add for aiworks facebeauty and bokeh 20200722 start
import java.io.ByteArrayOutputStream;
import com.mediatek.camera.feature.mode.aiworksbokeh.util.AiWorksUtil;
import com.mediatek.camera.feature.mode.aiworksbokeh.util.ImageUtil;
import com.aiworks.yuvUtil.YuvEncodeJni;
import android.content.Intent;
import com.aiworks.android.portrait.PhotoEffectListener;
import com.mediatek.camera.R;
//bv wuyonglin add for aiworks facebeauty and bokeh 20200722 end
import android.os.ConditionVariable;
import com.aiworks.android.utils.Util;
import com.mediatek.camera.common.exif.ExifInterface;
import android.provider.MediaStore;
import java.io.IOException;
//bv wuyonglin add for AiWorksBokeh water logo 20200831 start
import com.mediatek.camera.WaterMarkUtil;
//bv wuyonglin add for AiWorksBokeh water logo 20200831 end

/**
 * Normal photo mode that is used to take normal picture.
 */
public class AiWorksBokehMode extends CameraModeBase implements CaptureDataCallback,
        DeviceCallback, PreviewSizeCallback, IMemoryManager.IMemoryListener,
        CaptureImageSavedCallback {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(AiWorksBokehMode.class.getSimpleName());
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

    protected IAiWorksBokehDeviceController mIAiWorksBokehDeviceController;
    protected AiWorksBokehModeHelper mAiWorksBokehModeHelper;
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
    private AiWorksBokehViewCtrl mBokehViewCtrl;
    //bv wuyonglin add for aiworks facebeauty and bokeh 20200722 start
    private int fNum = 4;
    private PortraitEffect mPortraitEffect;
    private SurfaceTextureRenderer surfaceTextureRenderer;
    private Bitmap mMask;
    //private byte[] mJpeg = null;
    private boolean isTakingPicture = false;
    private int jpegRotation;
    private int mPhoneSensorOrientation;
    private int mDefaultBokehValue;
    private String modelPath;
    //bv wuyonglin add for aiworks facebeauty and bokeh 20200722 end
    //bv wuyonglin add for AiWorksBokeh water logo 20200827 start
    private byte[] mOriginalData = null;
    //bv wuyonglin add for AiWorksBokeh water logo 20200827 end
    private final Object mLock = new Object();
    private int mTakePictureRotation = -1;
    

    @Override
    public void init(@Nonnull IApp app, @Nonnull ICameraContext cameraContext,
            boolean isFromLaunch) {
        LogHelper.d(TAG, "[init]+");
        mBokehViewCtrl = new AiWorksBokehViewCtrl(cameraContext);
        super.init(app, cameraContext, isFromLaunch);
        mCameraId = getCameraIdByFacing(mDataStore.getValue(
                KEY_CAMERA_SWITCHER, null, mDataStore.getGlobalScope()));
        LogHelper.d(TAG, "[init] mCameraId " + mCameraId);
        // Device controller must be initialize before set preview size, because surfaceAvailable
        // may be called immediately when setPreviewSize.
        AiWorksBokehDeviceControllerFactory deviceControllerFactory = new AiWorksBokehDeviceControllerFactory();
        mIAiWorksBokehDeviceController = deviceControllerFactory.createDeviceController(app.getActivity(),
                mCameraApi, mICameraContext);
        initSettingManager(mCameraId);
        initStatusMonitor();
        prepareAndOpenCamera(false, mCameraId, isFromLaunch);
        ThumbnailHelper.setApp(app);
        mMemoryManager = new MemoryManagerImpl(app.getActivity());
        mAiWorksBokehModeHelper = new AiWorksBokehModeHelper(cameraContext);
        LogHelper.d(TAG, "[init]- ");
        mBokehViewCtrl.setViewChangeListener(mViewChangeListener);
        mBokehViewCtrl.init(app);
        mBokehViewCtrl.onOrientationChanged(mIApp.getGSensorOrientation());
        mDefaultBokehValue = mIApp.getActivity().getResources().getInteger(R.integer.aiworks_bokeh_level_default);
        fNum = Integer.parseInt(mDataStore.getValue("aiworks_bokeh_progress",mDefaultBokehValue+"",mDataStore.getCameraScope(Integer.parseInt(mCameraId))));
        LogHelper.d(TAG, "[init]- fNum ="+fNum);
        mBokehViewCtrl.showView(fNum);
        mIApp.getAppUi().setBokehViewListener(mBokehViewListener);
    }

    @Override
    public void resume(@Nonnull DeviceUsage deviceUsage) {
        super.resume(deviceUsage);

        mIsResumed = true;
        initSettingManager(mCameraId);
        initStatusMonitor();
        mMemoryManager.addListener(this);
        mMemoryManager.initStateForCapture(
                mICameraContext.getStorageService().getCaptureStorageSpace());
        mMemoryState = IMemoryManager.MemoryAction.NORMAL;
        mIAiWorksBokehDeviceController.queryCameraDeviceManager();
        prepareAndOpenCamera(false, mCameraId, false);
        //bv wuyonglin add for aiworks facebeauty and bokeh 20200722 start
        File dir = mIApp.getActivity().getFilesDir();
        modelPath = dir.getAbsolutePath() + File.separator + "AIWorksModels";

        PortraitEffect.supportSwitch(false);
        mPortraitEffect = new PortraitEffect(mIApp.getActivity());
        LogHelper.i(TAG, "resume mPortraitEffect init start");
        mPortraitEffect.init(modelPath);
        LogHelper.i(TAG, "resume mPortraitEffect init end");
        //mPortraitEffect.setPortraitType(PortraitEffect.TYPE_COLOUR);
        mPortraitEffect.setCameraID(Integer.parseInt(mCameraId));
        mPortraitEffect.setFNum(fNum);
        mPortraitEffect.setOrientation(mIApp.getGSensorOrientation());
        //bv wuyonglin add for aiworks facebeauty and bokeh 20200722 end
    }

    //bv wuyonglin add for aiworks facebeauty and bokeh 20200722 start
    @Override
    public void onOrientationChanged(int orientation) {
	mPhoneSensorOrientation = orientation;
	if (mPortraitEffect != null) {
            mPortraitEffect.setOrientation(orientation);
	}
    }
    //bv wuyonglin add for aiworks facebeauty and bokeh 20200722 end

    @Override
    public void pause(@Nullable DeviceUsage nextModeDeviceUsage) {
        /*-- modify by bv liangchangwei for fixbug 3680--*/
        mIsResumed = false;
        //bv wuyonglin add for monkey test lib happend null exception 20210202 start
        synchronized (mLock) {
        if (surfaceTextureRenderer != null) {
            surfaceTextureRenderer.releaseSurfaceTexture();
            surfaceTextureRenderer.release();
            surfaceTextureRenderer = null;
        }
        }
        //bv wuyonglin add for monkey test lib happend null exception 20210202 end
        LogHelper.i(TAG, "geek [pause]+ 1");
        super.pause(nextModeDeviceUsage);
        //mShutterDone.block(3000);
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
        LogHelper.i(TAG,"mNeedCloseSession = " + mNeedCloseSession + " mNeedCloseCameraIds.size() = " + mNeedCloseCameraIds.size());
        if (mNeedCloseCameraIds.size() > 0) {
            prePareAndCloseCamera(needCloseCameraSync(), mCameraId);
            recycleSettingManager(mCameraId);
        } else if (mNeedCloseSession){
            clearAllCallbacks(mCameraId);
            mIAiWorksBokehDeviceController.closeSession();
        } else{
            clearAllCallbacks(mCameraId);
            mIAiWorksBokehDeviceController.stopPreview();
        }
        //bv wuyonglin add for aiworks facebeauty and bokeh 20200722 start
        //bv wuyonglin delete for monkey test lib happend null exception 20210202 start
        /*synchronized (mLock) {
        if (surfaceTextureRenderer != null) {
            surfaceTextureRenderer.releaseSurfaceTexture();
            surfaceTextureRenderer.release();
            surfaceTextureRenderer = null;
        }*/
        //bv wuyonglin add for monkey test lib happend null exception 20210202 end
        if (!isTakingPicture) {
            mPortraitEffect.destroy();
        }
        LogHelper.i(TAG, "geek [pause]-- 1");
        //bv wuyonglin add for aiworks facebeauty and bokeh 20200722 end
    }

    @Override
    public void unInit() {
        super.unInit();
        mBokehViewCtrl.unInit();
        mIAiWorksBokehDeviceController.destroyDeviceController();
    }

    @Override
    public boolean onCameraSelected(@Nonnull String newCameraId) {
        LogHelper.i(TAG, "[onCameraSelected] ,new id:" + newCameraId + ",current id:" + mCameraId);
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
        boolean isDeviceReady = mIAiWorksBokehDeviceController.isReadyForCapture();
        LogHelper.i(TAG, "onShutterButtonClick, is storage ready : " + storageReady + "," +
                "isDeviceReady = " + isDeviceReady);

        if (storageReady && isDeviceReady && mIsResumed
                && mMemoryState != IMemoryManager.MemoryAction.STOP
                && (isShowProgressBar == false)) {
            //trigger capture animation
            startCaptureAnimation();
            mIApp.getAppUi().ShowShutterProgressBar();
            isShowProgressBar = true;
            isTakingPicture = true;
            mPhotoStatusResponder.statusChanged(KEY_PHOTO_CAPTURE, PHOTO_CAPTURE_START);
            updateModeDeviceState(MODE_DEVICE_STATE_CAPTURING);
            disableAllUIExceptionShutter();
            mIAiWorksBokehDeviceController.updateGSensorOrientation(mIApp.getGSensorOrientation());
            mTakePictureRotation = mIApp.getGSensorOrientation();
            LogHelper.i(TAG,"doShutterButtonClick mTakePictureRotation ="+mTakePictureRotation);
            mIAiWorksBokehDeviceController.takePicture(this);
            mIApp.getAppUi().setPictureProcessing(true);
            mExif = null;
            //mShutterDone.close();
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
        //bv wuyonglin add for AiWorksBokeh water logo 20200827 start
        mOriginalData = data;
        //bv wuyonglin add for AiWorksBokeh water logo 20200827 end
        int format = dataCallbackInfo.mBufferFormat;
        boolean needUpdateThumbnail = dataCallbackInfo.needUpdateThumbnail;
        boolean needRestartPreview = dataCallbackInfo.needRestartPreview;
        LogHelper.d(TAG, "onDataReceived, data = " + data + ",mIsResumed = " + mIsResumed +
                ",needUpdateThumbnail = " + needUpdateThumbnail + ",needRestartPreview = " +
                needRestartPreview+" format ="+format+" dataCallbackInfo.imageHeight ="+dataCallbackInfo.imageHeight+" dataCallbackInfo.imageWidth ="+dataCallbackInfo.imageWidth);
        if (data != null) {
            CameraSysTrace.onEventSystrace(JPEG_CALLBACK, true);
        }
        //bv wuyonglin add for aiworks facebeauty and bokeh 20200722 start
        int rotation = 0;
        int format1 = ImageFormat.YUV_420_888;
        jpegRotation = ImageUtil.getJpegOrientation(data);
        byte[] yuv = YuvEncodeJni.getInstance().Jpeg2Yuv(data, dataCallbackInfo.imageHeight, dataCallbackInfo.imageWidth, 1);
        if (jpegRotation == 0) {
            rotation = PortraitEffect.FACE_UP;
        } else if (jpegRotation == 90) {
            rotation = PortraitEffect.FACE_LEFT;
        } else if (jpegRotation == 180) {
            rotation = PortraitEffect.FACE_DOWN;
        } else if (jpegRotation == 270) {
            rotation = PortraitEffect.FACE_RIGHT;
        }

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
        } catch (IOException e) {
            LogHelper.e(TAG, "Failed to read EXIF data", e);
        }
        //mJpeg = YuvEncodeJni.getInstance().EncodeYuvToJpeg(yuv, format1, picutreW, picutreH, 80, jpegRotation);
        mPortraitEffect.procImageYuv(yuv, format1, picutreW, picutreH, rotation, false, new MyPhotoEffectListener(jpegRotation));

        //save file first,because save file is in other thread, so will improve the shot to shot
        //performance.
        /*if (data != null) {
            if (format == ImageFormat.JPEG) {
                saveData(data);
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

        }*/
        //bv wuyonglin add for aiworks facebeauty and bokeh 20200722 end
        //if camera is paused, don't need do start preview and other device related actions.
        if (mIsResumed) {
            //first do start preview in API1.
            if (mCameraApi == CameraApi.API1) {
                if (needRestartPreview && !mIsMatrixDisplayShow) {
                    mIAiWorksBokehDeviceController.startPreview();
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
			
            mIApp.getAppUi().updateThumbnail(bitmap);*/
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
        //modify by huangfeifor front bokeh start
        //Relation relation = PiBokehRestriction.getRestriction().getRelation("on", true);
        Relation relation = null;
        if(Config.isFrontBokehSupport(mIApp.getActivity())){
            relation = AiWorksBokehRestriction.getFrontRestriction().getRelation("on", true);
        }else{
            relation = AiWorksBokehRestriction.getRestriction().getRelation("on", true);
        }
        //modify by huangfeifor front bokeh end

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
        mIApp.getAppUi().onPreviewStarted(mCameraId);
        mIApp.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                stopCaptureAnimation();
            }
        });
        updateModeDeviceState(MODE_DEVICE_STATE_PREVIEWING);
        mPreviewFormat = format;
    }

    @Override
    public void onAiworksPreviewCallback(byte[] data, int format, int width, int height) {
        mPortraitEffect.onPreviewFrame(data, ImageFormat.NV21, mPreviewWidth, mPreviewHeight);
    }

    @Override
    public void onPreviewSizeReady(Size previewSize) {
        LogHelper.i(TAG, "[onPreviewSizeReady] previewSize: " + previewSize.toString());
        updatePictureSizeAndPreviewSize(previewSize);
    }

    private void onPreviewSizeChanged(int width, int height) {
        mPreviewWidth = width;
        mPreviewHeight = height;
        mIApp.getAppUi().setPreviewSize(mPreviewWidth, mPreviewHeight, mISurfaceStatusListener);
    }

    private void prepareAndOpenCamera(boolean needOpenCameraSync, String cameraId,
            boolean needFastStartPreview) {
        mCameraId = cameraId;
        StatusMonitor statusMonitor = mICameraContext.getStatusMonitor(mCameraId);
        statusMonitor.registerValueChangedListener(KEY_PICTURE_SIZE, mStatusChangeListener);
        statusMonitor.registerValueChangedListener(KEY_FORMTAT, mStatusChangeListener);
        statusMonitor.registerValueChangedListener(KEY_MATRIX_DISPLAY_SHOW, mStatusChangeListener);

        //before open camera, prepare the device callback and size changed callback.
        mIAiWorksBokehDeviceController.setDeviceCallback(this);
        mIAiWorksBokehDeviceController.setPreviewSizeReadyCallback(this);

        //prepare device info.
        AiWorksBokehDeviceInfo info = new AiWorksBokehDeviceInfo();
        info.setCameraId(mCameraId);
        info.setSettingManager(mISettingManager);
        info.setNeedOpenCameraSync(needOpenCameraSync);
        info.setNeedFastStartPreview(needFastStartPreview);
        mIAiWorksBokehDeviceController.openCamera(info);
    }

    private void prePareAndCloseCamera(boolean needSync, String cameraId) {
        clearAllCallbacks(cameraId);
        mIAiWorksBokehDeviceController.closeCamera(needSync);
        mIsMatrixDisplayShow = false;
        //reset the preview size
        mPreviewWidth = 0;
        mPreviewHeight = 0;
    }

    private void clearAllCallbacks(String cameraId) {
        LogHelper.i(TAG, " ++ clearAllCallbacks cameraId = " + cameraId);
        mIAiWorksBokehDeviceController.setPreviewSizeReadyCallback(null);
        StatusMonitor statusMonitor = mICameraContext.getStatusMonitor(cameraId);
        statusMonitor.unregisterValueChangedListener(KEY_PICTURE_SIZE, mStatusChangeListener);
        statusMonitor.unregisterValueChangedListener(KEY_FORMTAT, mStatusChangeListener);
        statusMonitor.unregisterValueChangedListener(
                KEY_MATRIX_DISPLAY_SHOW, mStatusChangeListener);
        LogHelper.i(TAG, " -- clearAllCallbacks cameraId = " + cameraId);
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
            surfaceTextureRenderer.release();
            surfaceTextureRenderer = null;
        }
        LogHelper.i(TAG, "[doCameraSelect] release surfaceTextureRenderer ="+surfaceTextureRenderer+" newCamera ="+newCamera);
        //mPortraitEffect.init(modelPath);
        fNum = Integer.parseInt(mDataStore.getValue("aiworks_bokeh_progress",mDefaultBokehValue+"",mDataStore.getCameraScope(Integer.parseInt(newCamera))));
        LogHelper.i(TAG, "[doCameraSelect]- setFNum fNum ="+fNum);
        mPortraitEffect.setCameraID(Integer.parseInt(newCamera));
        mBokehViewCtrl.unInit();
        mBokehViewCtrl.showView(fNum);
        mPortraitEffect.setFNum(fNum);
        mPortraitEffect.setOrientation(mIApp.getGSensorOrientation());
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
            LogHelper.d(TAG, "[onFileSaved] uri = " + uri + ", mCapturingNumber = "
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
            mIAiWorksBokehDeviceController.setPictureSize(new Size(mCaptureWidth, mCaptureHeight));
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
            ContentValues contentValues = mAiWorksBokehModeHelper.createContentValues(data,
                    fileDirectory, exifSize.getWidth(), exifSize.getHeight());
            String FilePath = contentValues.getAsString(MediaStore.Images.ImageColumns.DATA);
            addExif(FilePath, data);
            mICameraContext.getMediaSaver().addSaveRequest(data, contentValues, null,
                    mMediaSaverListener);
        }
    }

    //bv wuyonglin add for AiWorksBokeh water logo 20200827 start
    private void saveBitmapData(Bitmap bitmap) {
        if (bitmap != null) {
            //check memory to decide whether it can take next picture.
            //if not, show saving
            ISettingManager.SettingController controller = mISettingManager.getSettingController();
            String dngState = controller.queryValue(KEY_DNG);
            long saveDataSize = mOriginalData.length;
            if (dngState != null && "on".equalsIgnoreCase(dngState)) {
                saveDataSize = saveDataSize + DNG_IMAGE_SIZE;
            }
            synchronized (mCaptureNumberSync) {
                mCapturingNumber ++;
                mMemoryManager.checkOneShotMemoryAction(saveDataSize);
            }

            String fileDirectory = mICameraContext.getStorageService().getFileDirectory();
            Size exifSize = CameraUtil.getSizeFromExif(mOriginalData);
            ContentValues contentValues = mAiWorksBokehModeHelper.createContentValues(mOriginalData,
                    fileDirectory, bitmap.getHeight(), bitmap.getHeight());
            String FilePath = contentValues.getAsString(MediaStore.Images.ImageColumns.DATA);
            WaterMarkUtil.compressAndSaveBitmap(bitmap, FilePath, 80,mExif);
            mICameraContext.getMediaSaver().addSaveRequest(mOriginalData, contentValues, null,
                    mMediaSaverListener, false, bitmap);
        }
    }
    //bv wuyonglin add for AiWorksBokeh water logo 20200827 end

    public void addExif(String path, byte[] data){
        LogHelper.i(TAG,"addExif path = " + path + " mExif = " + mExif + " data.size = " + data.length);
        //Util.writeImage(path, mExif, data);
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
            LogHelper.i(TAG, "onSurfaceTextureAvailable surfaceAvailable,device controller = " + mIAiWorksBokehDeviceController
                    + ",w = " + width + ",h = " + height+" mIsResumed ="+mIsResumed);
            //bv wuyonglin add for aiworks facebeauty and bokeh 20200722 start
            if (surfaceObject != null && surfaceObject instanceof SurfaceTexture && mIsResumed) {	//bv wuyonglin modify for monkey test exception failed 20210202
                surfaceTextureRenderer = new SurfaceTextureRenderer(mIApp.getAppUi().getPreviewTextureView(), mPortraitEffect, (SurfaceTexture) surfaceObject);
                surfaceTextureRenderer.acquireSurfaceTexture(width, height , mSurfaceTextureListener);
            }
            /*if (mModeHandler != null) {
                mModeHandler.post(new Runnable() {
                    @Override
                    public void run() {
            if (mIAiWorksBokehDeviceController != null && mIsResumed) {
                mIAiWorksBokehDeviceController.updatePreviewSurface(surfaceObject);
            }
        }
                });
            }*/
            //bv wuyonglin add for aiworks facebeauty and bokeh 20200722 end
        }

        @Override
        public void surfaceChanged(Object surfaceObject, int width, int height) {
            LogHelper.i(TAG, "onSurfaceTextureAvailable surfaceChanged, device controller = " + mIAiWorksBokehDeviceController
                    + ",w = " + width + ",h = " + height);
            //bv wuyonglin add for aiworks facebeauty and bokeh 20200722 start
            if (surfaceObject != null && surfaceObject instanceof SurfaceTexture) {
		//mPortraitEffect.init(modelPath);
                surfaceTextureRenderer = new SurfaceTextureRenderer(mIApp.getAppUi().getPreviewTextureView(), mPortraitEffect, (SurfaceTexture) surfaceObject);
                surfaceTextureRenderer.acquireSurfaceTexture(width, height , mSurfaceTextureListener);
            }
            /*if (mModeHandler != null) {
                mModeHandler.post(new Runnable() {
                    @Override
                    public void run() {
            if (mIAiWorksBokehDeviceController != null && mIsResumed) {
                mIAiWorksBokehDeviceController.updatePreviewSurface(surfaceObject);
            }
        }
                });
            }*/
            //bv wuyonglin add for aiworks facebeauty and bokeh 20200722 end
        }

        @Override
        public void surfaceDestroyed(Object surfaceObject, int width, int height) {
            LogHelper.i(TAG, "surfaceDestroyed");
            //bv wuyonglin add for aiworks facebeauty and bokeh 20200722 start
            synchronized (mLock) {
            if (surfaceTextureRenderer != null) {
                surfaceTextureRenderer.releaseSurfaceTexture();
                surfaceTextureRenderer.release();
                surfaceTextureRenderer = null;
            }
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
            LogHelper.d(TAG, "[onStatusChanged] key = " + key + ",value = " + value + " mIsResumed = " + mIsResumed);
            /*-- modify by bv liangchangwei for fixbug 3680--*/
            if (KEY_PICTURE_SIZE.equalsIgnoreCase(key) && mIsResumed) {
                //mShutterDone.block(3000);
                String[] sizes = value.split("x");
                mCaptureWidth = Integer.parseInt(sizes[0]);
                mCaptureHeight = Integer.parseInt(sizes[1]);
                mIAiWorksBokehDeviceController.setPictureSize(new Size(mCaptureWidth, mCaptureHeight));
                Size previewSize = mIAiWorksBokehDeviceController.getPreviewSize((double) mCaptureWidth /
                        mCaptureHeight);
                int width = previewSize.getWidth();
                int height = previewSize.getHeight();
                if (width != mPreviewWidth || height != mPreviewHeight) {
                    onPreviewSizeChanged(width, height);
                }
            } else if (KEY_MATRIX_DISPLAY_SHOW.equals(key)) {
                mIsMatrixDisplayShow = "true".equals(value);
            } else if (KEY_FORMTAT.equalsIgnoreCase(key)) {
                mIAiWorksBokehDeviceController.setFormat(value);
                LogHelper.i(TAG, "[onStatusChanged] key = " + key + ", set sCaptureFormat = " + value);
            }
        }
    }
    private AiWorksBokehViewCtrl.ViewChangeListener mViewChangeListener
            = new AiWorksBokehViewCtrl.ViewChangeListener() {
        @Override
        public void onBokehParameterChanged(int level,int x,int y,int radius) {
            LogHelper.d(TAG, "new setFNum onBokehParameterChanged level ="+level+" mPortraitEffect ="+mPortraitEffect);
		if (mPortraitEffect != null) {
                mPortraitEffect.setFNum(level);
                mDataStore.setValue("aiworks_bokeh_progress",level+"",mDataStore.getCameraScope(Integer.parseInt(mCameraId)),false);
		}
        }


    };

    @Override
    public void showApertureView(int x,int y) {
        //if(mBokehViewCtrl!=null){
        //    mBokehViewCtrl.showApertureView(x,y,mPreviewWidth,mPreviewHeight);
        //}
    }

    //bv wuyonglin add for aiworks facebeauty and bokeh 20200722 start
    private SurfaceTextureListener mSurfaceTextureListener = new SurfaceTextureListener() {

        public void onSurfaceTextureAvailable(final SurfaceTexture surface) {
            LogHelper.i(TAG, "new onSurfaceTextureAvailable surface ="+surface);
            if (mModeHandler != null) {
                mModeHandler.post(new Runnable() {
                    @Override
                    public void run() {
                    	if (mIAiWorksBokehDeviceController != null && mIsResumed) {
                    		mIAiWorksBokehDeviceController.updatePreviewSurface(surface);
                    	}
                    }
                });
            }
        }
    };

    private class MyPhotoEffectListener implements PhotoEffectListener {

        private int mJpegRotation;

        private MyPhotoEffectListener(int jpegRotation) {
            mJpegRotation = jpegRotation;
        }

        @Override
        public void onPortraitApply(byte[] yuv, int format, int width, int height) {
            //mShutterDone.open();
            LogHelper.i(TAG, "onPortraitApply jpegRotation format = " + format + ", width = " + width + ", height = " + height + ", mJpegRotation = " + mJpegRotation+" mMask ="+mMask);
            if (yuv != null) {
                //bv wuyonglin add for AiWorksBokeh water logo 20200827 start
                if (CameraUtil.isWaterMarkOn(mDataStore,mIApp.getActivity())) {
                    byte[] data = WaterMarkUtil.yuvAddWaterMark(mIApp.getActivity(), yuv, width, height, mJpegRotation);
                    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    YuvEncodeJni.getInstance().Yuv2Bmp(data, bitmap, format,
                            width, height, mJpegRotation);
                    LogHelper.i(TAG, " onPortraitApply2 Yuv2Bmp updateThumbnail jpegRotation data = " + data);
                    saveBitmapData(bitmap);
                } else {
                    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    YuvEncodeJni.getInstance().Yuv2Bmp(yuv, bitmap, format,
                            width, height, mJpegRotation);
                    LogHelper.i(TAG, " onPortraitApply2 Yuv2Bmp updateThumbnail jpegRotation bitmap = " + bitmap);
                    saveBitmapData(bitmap);
                }
                //bv wuyonglin add for AiWorksBokeh water logo 20200827 end
            }

            if (isTakingPicture) {
                isTakingPicture = false;
                if (!mIsResumed) {
                    mPortraitEffect.destroy();
                }
            }
        }

        @Override
        public void onPortraitApply(Bitmap result) {
            LogHelper.i(TAG, "onPortraitApply");
            //mShutterDone.open();
            if (result != null) {
                ByteArrayOutputStream jpgStream;
                jpgStream = new ByteArrayOutputStream();
                result.compress(Bitmap.CompressFormat.JPEG, 90, jpgStream);

                byte[] data = jpgStream.toByteArray();
                //String path = Environment.getExternalStorageDirectory().getPath() + "/DCIM/Portrait/";
                //String fileName = "onPortraitApply2" + "_" + System.currentTimeMillis() + ".jpg";
                //Uri uri = AiWorksUtil.saveJpeg(mIApp.getActivity(), data, path, fileName);

                result.recycle();
                try {
                    jpgStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (isTakingPicture) {
                isTakingPicture = false;
                if (!mIsResumed) {
                    mPortraitEffect.destroy();
                }
            }
        }

        @Override
        public void onPortraitMaskCallback(Bitmap mask) {
            LogHelper.i(TAG, "onPortraitMaskCallback mask = " + mask);
            /*if (mask != null) {
                if (mJpegRotation != 0) {
                    mask = ImageUtil.rotateBitmapByDegree(mask, mJpegRotation);
                }
                ByteArrayOutputStream jpgStream;
                jpgStream = new ByteArrayOutputStream();
                mask.compress(Bitmap.CompressFormat.JPEG, 90, jpgStream);

                byte[] data = jpgStream.toByteArray();
                String path = Environment.getExternalStorageDirectory().getPath() + "/DCIM/Portrait/";
                String fileName = "onPortraitMaskCallback" + "_" + System.currentTimeMillis() + ".jpg";

                AiWorksUtil.saveJpeg(mIApp.getActivity(), data, path, fileName);

//                mask.recycle();
                mMask = mask;
                try {
                    jpgStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }*/

        }
    }
    //bv wuyonglin add for aiworks facebeauty and bokeh 20200722 end

    private IAppUi.BokehViewListener mBokehViewListener = new IAppUi.BokehViewListener() {
        @Override
        public void onConfigBokehUIVisibility(int visibility) {
                    LogHelper.d(TAG, "applyBokehViewVisibilityImmediately onConfigBokehUIVisibility visibility ="+visibility);
                    mIApp.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mBokehViewCtrl != null && mBokehViewCtrl.getBokehView() !=null) {
                                mBokehViewCtrl.getBokehView().setVisibility(visibility);
                            }
                        }
                    });
        }
    };

}
