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

package com.mediatek.camera.feature.mode.pro;

import com.mediatek.camera.common.relation.Relation;

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
import com.mediatek.camera.feature.mode.pro.device.ProDeviceControllerFactory;
import com.mediatek.camera.feature.mode.pro.device.IProDeviceController;
import com.mediatek.camera.feature.mode.pro.device.IProDeviceController.DataCallbackInfo;
import com.mediatek.camera.feature.mode.pro.device.IProDeviceController.DeviceCallback;
import com.mediatek.camera.feature.mode.pro.device.IProDeviceController.CaptureDataCallback;
import com.mediatek.camera.feature.mode.pro.device.IProDeviceController.PreviewSizeCallback;

import com.mediatek.camera.feature.mode.pro.device.IProDeviceController.CaptureImageSavedCallback;
import com.mediatek.camera.feature.mode.pro.view.ProViewCtrl;
import com.mediatek.camera.common.relation.StatusMonitor;
import com.mediatek.camera.common.relation.StatusMonitor.StatusChangeListener;
import com.mediatek.camera.common.setting.ISettingManager;
import com.mediatek.camera.common.setting.SettingManagerFactory;
import com.mediatek.camera.common.storage.MediaSaver.MediaSaverListener;
import com.mediatek.camera.common.utils.BitmapCreator;
import com.mediatek.camera.common.utils.CameraUtil;
import com.mediatek.camera.common.utils.Size;
import com.mediatek.camera.common.mode.photo.heif.HeifWriter;
import java.util.ArrayList;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.mediatek.camera.common.mode.photo.HeifHelper;
import com.mediatek.camera.common.mode.photo.ThumbnailHelper;
import android.util.Log;

//add by huangfei for shutter start
import com.mediatek.camera.Config;
import android.app.Activity;
import android.widget.RelativeLayout; 
import com.mediatek.camera.R;
import com.mediatek.camera.feature.mode.pro.LongExposureView.LongExposureViewState;
//add by huangfei for shutter start

/**
 * Normal photo mode that is used to take normal picture.
 */
public class ProMode extends CameraModeBase implements CaptureDataCallback,
        DeviceCallback, PreviewSizeCallback, IMemoryManager.IMemoryListener,ProViewCtrl.OnProgressChangeListener,
        CaptureImageSavedCallback  {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(ProMode.class.getSimpleName());
    private static final String KEY_MATRIX_DISPLAY_SHOW = "key_matrix_display_show";
    private static final String MODE_KEY = ProMode.class.getName();
    private static final String KEY_PICTURE_SIZE = "key_picture_size";
    private static final String KEY_FORMTAT = "key_format";
    private static final String KEY_DNG = "key_dng";
    private static final String JPEG_CALLBACK = "jpeg callback";
    private static final String POST_VIEW_CALLBACK = "post view callback";
    private static final int POST_VIEW_FORMAT = ImageFormat.NV21;
    private static final long DNG_IMAGE_SIZE = 45 * 1024 * 1024;

    protected static final String PHOTO_CAPTURE_START = "start";
    protected static final String PHOTO_CAPTURE_STOP = "stop";
    protected static final String KEY_PHOTO_CAPTURE = "key_photo_capture";

    protected IProDeviceController mIProDeviceController;
    protected ProModeHelper mProModeHelper;
    protected int mCaptureWidth;
    // make sure the picture size ratio = mCaptureWidth / mCaptureHeight not to NAN.
    protected int mCaptureHeight = Integer.MAX_VALUE;
    //the reason is if surface is ready, let it to set to device controller, otherwise
    //if surface is ready but activity is not into resume ,will found the preview
    //can not start preview.
    protected volatile boolean mIsResumed = true;
    private String mCameraId;

    private ISurfaceStatusListener mISurfaceStatusListener = new SurfaceChangeListener();
    private ISettingManager mISettingManager;
    private MemoryManagerImpl mMemoryManager;
    private byte[] mPreviewData;
    private int mPreviewFormat;
    private int mPreviewWidth;
    private int mPreviewHeight;
    //make sure it is in capturing to show the saving UI.
    private int mCapturingNumber = 0;
    private boolean mIsMatrixDisplayShow = false;
    private Object mPreviewDataSync = new Object();
    private Object mCaptureNumberSync = new Object();
    private HandlerThread mAnimationHandlerThread;
    private Handler mAnimationHandler;
    private StatusChangeListener mStatusChangeListener = new MyStatusChangeListener();
    private IMemoryManager.MemoryAction mMemoryState = IMemoryManager.MemoryAction.NORMAL;
    protected StatusMonitor.StatusResponder mPhotoStatusResponder;
    private ProViewCtrl mProViewCtrl = new ProViewCtrl();

    
    //add by huangfei for shutter start
    private RelativeLayout mLongExposureRoot;
    private LongExposureView mLongExposureView;
    private Activity mActivity;
    private long mShutterValue = 0L;
    String speed = "";
    //add by huangfei for shutter end

    @Override
    public void init(@Nonnull IApp app, @Nonnull ICameraContext cameraContext,
            boolean isFromLaunch) {
        LogHelper.d(TAG, "[init]+");
        super.init(app, cameraContext, isFromLaunch);
        mProModeHelper = new ProModeHelper(cameraContext);

        mCameraId = getCameraIdByFacing(mDataStore.getValue(
                KEY_CAMERA_SWITCHER, null, mDataStore.getGlobalScope()));
        LogHelper.d(TAG, "[init] mCameraId " + mCameraId);
        // Device controller must be initialize before set preview size, because surfaceAvailable
        // may be called immediately when setPreviewSize.
        ProDeviceControllerFactory deviceControllerFactory = new ProDeviceControllerFactory();
        mIProDeviceController = deviceControllerFactory.createDeviceController(app.getActivity(),
                mCameraApi, mICameraContext);
        initSettingManager(mCameraId);
        initStatusMonitor();
        prepareAndOpenCamera(false, mCameraId, isFromLaunch);
        ThumbnailHelper.setApp(app);
        mMemoryManager = new MemoryManagerImpl(app.getActivity());
        //add by huangfei for shutter start
        mActivity = mIApp.getActivity();
        //add by huangfei for shutter end

		mProViewCtrl.init(app);
        mProViewCtrl.setProgressChangeListener(this);
        LogHelper.d(TAG, "[init]- ");
    }

    @Override
    public void resume(@Nonnull DeviceUsage deviceUsage) {
        super.resume(deviceUsage);
        mIsResumed = true;
        initSettingManager(mCameraId);

        //add by huangfei for shutter start
        if(Config.isShutterSupport(mActivity)){
            initLongExposureView();
        } 
        //add by huangfei for shutter end

        initStatusMonitor();
        mMemoryManager.addListener(this);
        mMemoryManager.initStateForCapture(
                mICameraContext.getStorageService().getCaptureStorageSpace());
        mMemoryState = IMemoryManager.MemoryAction.NORMAL;
        mIProDeviceController.queryCameraDeviceManager();
        prepareAndOpenCamera(false, mCameraId, false);
        mProViewCtrl.refreshSeekArc();
    }

    @Override
    public void pause(@Nullable DeviceUsage nextModeDeviceUsage) {
        LogHelper.i(TAG, "[pause]+");

        super.pause(nextModeDeviceUsage);

        //add by huangfei for shutter start
        updateUiState(LongExposureViewState.STATE_ABORT);
        //add by huangfei for shutter end


        mIsResumed = false;
        mMemoryManager.removeListener(this);
        //clear the surface listener
        mIApp.getAppUi().clearPreviewStatusListener(mISurfaceStatusListener);
        if (mNeedCloseCameraIds.size() > 0) {
            prePareAndCloseCamera(needCloseCameraSync(), mCameraId);
            recycleSettingManager(mCameraId);
        } else if (mNeedCloseSession){
            clearAllCallbacks(mCameraId);
            mIProDeviceController.closeSession();
        } else{
            clearAllCallbacks(mCameraId);
            mIProDeviceController.stopPreview();
        }
        LogHelper.i(TAG, "[pause]-");
    }

    @Override
    public void unInit() {
        super.unInit();
        mProViewCtrl.unInit();
        mProViewCtrl = null;
        mIProDeviceController.destroyDeviceController();
    }

    @Override
    public String getModeKey() {
        return MODE_KEY;
    }

    @Override
    public boolean onCameraSelected(@Nonnull String newCameraId) {
        LogHelper.i(TAG, "[onCameraSelected] ,new id:" + newCameraId + ",current id:" + mCameraId);
        super.onCameraSelected(newCameraId);
        //first need check whether can switch camera or not.
        if (canSelectCamera(newCameraId) && mIsResumed) {
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
        boolean isDeviceReady = mIProDeviceController.isReadyForCapture();
        LogHelper.i(TAG, "onShutterButtonClick, is storage ready : " + storageReady + "," +
                "isDeviceReady = " + isDeviceReady);

        if (storageReady && isDeviceReady && mIsResumed
                && mMemoryState != IMemoryManager.MemoryAction.STOP) {
            //trigger capture animation
            startCaptureAnimation();
            mPhotoStatusResponder.statusChanged(KEY_PHOTO_CAPTURE, PHOTO_CAPTURE_START);
            updateModeDeviceState(MODE_DEVICE_STATE_CAPTURING);
            disableAllUIExceptionShutter();
            mIProDeviceController.updateGSensorOrientation(mIApp.getGSensorOrientation());
            mIProDeviceController.takePicture(this);  


            //add by huangfei for shutter start
            if(mShutterValue>1000000000L){
                speed = mShutterValue/1000000000L+"";
                updateUiState(LongExposureViewState.STATE_START); 
            }
            //add by huangfei for shutter end

        }
        return true;
    }

    @Override
    public boolean onShutterButtonLongPressed() {
        return false;
    }

    @Override
    public void onDataReceived(DataCallbackInfo dataCallbackInfo) {


        //when mode receive the data, need save it.
        byte[] data = dataCallbackInfo.data;
        int format = dataCallbackInfo.mBufferFormat;
        boolean needUpdateThumbnail = dataCallbackInfo.needUpdateThumbnail;
        boolean needRestartPreview = dataCallbackInfo.needRestartPreview;
        LogHelper.d(TAG, "onDataReceived, data = " + data + ",mIsResumed = " + mIsResumed +
                ",needUpdateThumbnail = " + needUpdateThumbnail + ",needRestartPreview = " +
                needRestartPreview);
        if (data != null) {
            CameraSysTrace.onEventSystrace(JPEG_CALLBACK, true);
        }
        //save file first,because save file is in other thread, so will improve the shot to shot
        //performance.
        if (data != null) {
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
            }else if (format==ImageFormat.HEIC){
                saveHeifDataForAospFlow(data);
            }

        }
        //if camera is paused, don't need do start preview and other device related actions.
        if (mIsResumed) {
            //first do start preview in API1.
            if (mCameraApi == CameraApi.API1) {
                if (needRestartPreview && !mIsMatrixDisplayShow) {
                    mIProDeviceController.startPreview();
                }
            }
        }
        //update thumbnail
        if (data != null && needUpdateThumbnail) {
            if (format == ImageFormat.JPEG) {
                updateThumbnail(data);
            } else if (format == HeifHelper.FORMAT_HEIF) {
/*                HeifHelper heifHelper = new HeifHelper(mICameraContext);
                int width = dataCallbackInfo.imageWidth;
                int height = dataCallbackInfo.imageHeight;
                Bitmap thumbnail = heifHelper.createBitmapFromYuv(data,
                        width, height, mIApp.getAppUi().getThumbnailViewWidth());
                mIApp.getAppUi().updateThumbnail(thumbnail);*/
            }

        }
        if (data != null) {
            CameraSysTrace.onEventSystrace(JPEG_CALLBACK, false);
        }
    }

    @Override
    public void onPostViewCallback(byte[] data) {
        LogHelper.d(TAG, "[onPostViewCallback] data = " + data + ",mIsResumed = " + mIsResumed);
        CameraSysTrace.onEventSystrace(POST_VIEW_CALLBACK, true, true);
        if (data != null && mIsResumed) {
            //will update the thumbnail
            int rotation = CameraUtil.getJpegRotationFromDeviceSpec(Integer.parseInt(mCameraId),
                    mIApp.getGSensorOrientation(), mIApp.getActivity());
					
            Bitmap bitmap = BitmapCreator.createBitmapFromYuv(data,
                    ThumbnailHelper.POST_VIEW_FORMAT,
                    ThumbnailHelper.getThumbnailWidth(),
                    ThumbnailHelper.getThumbnailHeight(),
                    mIApp.getAppUi().getThumbnailViewWidth(),
                    rotation, mIApp, mICameraContext);
            //modify by huangfei for mirror end
			
            mIApp.getAppUi().updateThumbnail(bitmap);
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
        Relation relation = ProRestriction.getRestriction().getRelation("on", true);
        mISettingManager.getSettingController().postRestriction(relation);
        mISettingManager.getSettingController().addViewEntry();
        mISettingManager.getSettingController().refreshViewEntry();
        updateModeDeviceState(MODE_DEVICE_STATE_OPENED);
    }

    //add by huangfei for MF start
    @Override
    public void setFoucsDistance(int distance) {
        mProViewCtrl.setFoucsDistance(distance);
    }
    //add by huangfei for MF end

    @Override
    public void beforeCloseCamera() {
        onMfProgressChanged("0");
        mProViewCtrl.resetMF();
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
        if (!mIsMatrixDisplayShow){
            mIApp.getAppUi().applyAllUIEnabled(true);
        }
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
    @Override
    public void onCaptureCallback() {
        mPhotoStatusResponder.statusChanged(KEY_PHOTO_CAPTURE, PHOTO_CAPTURE_STOP);
    }


    @Override
    public void onPreviewSizeReady(Size previewSize) {
        LogHelper.d(TAG, "[onPreviewSizeReady] previewSize: " + previewSize.toString());
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
        mIProDeviceController.setDeviceCallback(this);
        mIProDeviceController.setPreviewSizeReadyCallback(this);
        mIProDeviceController.setSavedDataCallback(this);
        //prepare device info.
        ProDeviceInfo info = new ProDeviceInfo();
        info.setCameraId(mCameraId);
        info.setSettingManager(mISettingManager);
        info.setNeedOpenCameraSync(needOpenCameraSync);
        info.setNeedFastStartPreview(needFastStartPreview);
        mIProDeviceController.openCamera(info);
    }

    private void prePareAndCloseCamera(boolean needSync, String cameraId) {
        clearAllCallbacks(cameraId);
        mIProDeviceController.closeCamera(needSync);
        mIsMatrixDisplayShow = false;
        //reset the preview size
        mPreviewWidth = 0;
        mPreviewHeight = 0;
    }

    private void clearAllCallbacks(String cameraId) {
        mIProDeviceController.setPreviewSizeReadyCallback(null);
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

                    //modify by huangfei for shutter start
                    //mIApp.getAppUi().applyAllUIVisibility(View.VISIBLE);
                    if(mShutterValue<=1000000000L){
                        mIApp.getAppUi().applyAllUIVisibility(View.VISIBLE);
                    }
                    //modify by huangfei for shutter end
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
        if (size != null && mIsResumed) {
            String[] pictureSizes = size.split("x");
            mCaptureWidth = Integer.parseInt(pictureSizes[0]);
            mCaptureHeight = Integer.parseInt(pictureSizes[1]);
            mIProDeviceController.setPictureSize(new Size(mCaptureWidth, mCaptureHeight));
            int width = previewSize.getWidth();
            int height = previewSize.getHeight();
            LogHelper.d(TAG, "[updatePictureSizeAndPreviewSize] picture size: " + mCaptureWidth +
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
            Size exifSize = CameraUtil.getSizeFromSdkExif(data);
            ContentValues contentValues = mProModeHelper.createContentValues(data,
                    fileDirectory, exifSize.getWidth(), exifSize.getHeight());
            //bv liangchangwei add for AiWorks begin
            String FilePath = contentValues.getAsString(MediaStore.Images.ImageColumns.DATA);
            mIProDeviceController.addExif(FilePath, data);
            //bv liangchangwei add for AiWorks end
            mICameraContext.getMediaSaver().addSaveRequest(data, contentValues, null,
                    mMediaSaverListener);
        }
    }

    private void saveHeifDataForAospFlow(byte[] data) {
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
        String fileDirectory = mICameraContext.getStorageService().getFileDirectory();
        HeifHelper heifHelper = new HeifHelper(mICameraContext);
        Size exifSize = CameraUtil.getSizeFromSdkExif(data);
        ContentValues values = heifHelper.getContentValues(data,
                fileDirectory,exifSize.getWidth(),exifSize.getHeight());
        LogHelper.i(TAG, "onDataReceived,heif values =" +values.toString());
        mICameraContext.getMediaSaver().addSaveRequest(data, values, null,
                mMediaSaverListener, ImageFormat.HEIC);
    }

    private void disableAllUIExceptionShutter() {
        mIApp.getAppUi().applyAllUIEnabled(false);
        mIApp.getAppUi().setUIEnabled(IAppUi.SHUTTER_BUTTON, true);
        mIApp.getAppUi().setUIEnabled(IAppUi.SHUTTER_TEXT, false);
    }

    private void updateThumbnail(byte[] data) {
        Bitmap bitmap = BitmapCreator.createBitmapFromJpeg(data, mIApp.getAppUi()
                .getThumbnailViewWidth());
        mIApp.getAppUi().updateThumbnail(bitmap);
    }
	    @Override
    public void onWbProgressChanged(String progress) {
        mIProDeviceController.setWbValue(progress);
    }

    @Override
    public void onISOProgressChanged(String progress) {
        mIProDeviceController.setISOValue(progress);
        //bv wuyonglin add for after iso and shutter speed change not auto exp should unavailable 20200117 start
        if (!progress.equals("0") && mProViewCtrl.getSeekArcFrameLayout().getShutterValue() != 0) {
            mProViewCtrl.getSeekArcFrameLayout().setArcExpViewEnable(false);
        } else {
            mProViewCtrl.getSeekArcFrameLayout().setArcExpViewEnable(true);
        }
        //bv wuyonglin add for after iso and shutter speed change not auto exp should unavailable 20200117 end
    }

    @Override
    public void onMfProgressChanged(String progress) {
        mIProDeviceController.setMFValue(progress);
    }

    @Override
    public void onEXPProgressChanged(String progress) {;
        mIProDeviceController.setExpValue(progress);
    }

    //add by huangfei for shutter start
    @Override
    public void onShutterrogressChanged(String progress) {

        //add by huangfei for shutter start
        mShutterValue = Long.parseLong(progress);
        //add by huangfei for shutter end

        mIProDeviceController.setShutterValue(progress);
        //bv wuyonglin add for after iso and shutter speed change not auto exp should unavailable 20200117 start
        if (!progress.equals("0") && mProViewCtrl.getSeekArcFrameLayout().getIsoValue() != 0) {
            mProViewCtrl.getSeekArcFrameLayout().setArcExpViewEnable(false);
        } else {
            mProViewCtrl.getSeekArcFrameLayout().setArcExpViewEnable(true);
        }
        //bv wuyonglin add for after iso and shutter speed change not auto exp should unavailable 20200117 end
    }

    @Override
    public void onShowSeekArcFrameLayout(boolean show) {
        mIProDeviceController.onShowSeekArcFrameLayout(show);
    }

    //add by huangfei for shutter end

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
            LogHelper.d(TAG, "surfaceAvailable,device controller = " + mIProDeviceController
                    + ",w = " + width + ",h = " + height);
            if (mModeHandler != null) {
                mModeHandler.post(new Runnable() {
                    @Override
                    public void run() {
            if (mIProDeviceController != null && mIsResumed) {
                mIProDeviceController.updatePreviewSurface(surfaceObject);
            }
        }
                });
            }
        }

        @Override
        public void surfaceChanged(Object surfaceObject, int width, int height) {
            LogHelper.d(TAG, "surfaceChanged, device controller = " + mIProDeviceController
                    + ",w = " + width + ",h = " + height);
            if (mModeHandler != null) {
                mModeHandler.post(new Runnable() {
                    @Override
                    public void run() {
            if (mIProDeviceController != null && mIsResumed) {
                mIProDeviceController.updatePreviewSurface(surfaceObject);
            }
        }
                });
            }
        }

        @Override
        public void surfaceDestroyed(Object surfaceObject, int width, int height) {
            LogHelper.d(TAG, "surfaceDestroyed,device controller = " + mIProDeviceController);
        }
    }

    /**
     * Status change listener implement.
     */
    private class MyStatusChangeListener implements StatusChangeListener {
        @Override
        public void onStatusChanged(String key, String value) {
            LogHelper.d(TAG, "[onStatusChanged] key = " + key + ",value = " + value);
            if (KEY_PICTURE_SIZE.equalsIgnoreCase(key)) {
                String[] sizes = value.split("x");
                mCaptureWidth = Integer.parseInt(sizes[0]);
                mCaptureHeight = Integer.parseInt(sizes[1]);
                mIProDeviceController.setPictureSize(new Size(mCaptureWidth, mCaptureHeight));
                Size previewSize = mIProDeviceController.getPreviewSize((double) mCaptureWidth /
                        mCaptureHeight);
                int width = previewSize.getWidth();
                int height = previewSize.getHeight();
                if (width != mPreviewWidth || height != mPreviewHeight) {
                    onPreviewSizeChanged(width, height);
                }
            } else if (KEY_MATRIX_DISPLAY_SHOW.equals(key)) {
                mIsMatrixDisplayShow = "true".equals(value);
            } else if (KEY_FORMTAT.equalsIgnoreCase(key)) {
                mIProDeviceController.setFormat(value);
                LogHelper.i(TAG, "[onStatusChanged] key = " + key + ", set sCaptureFormat = " + value);
            }
        }
    }

    //add by huangfei for shutter start
    private void initLongExposureView() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
              //  initGuideHint();
                mActivity.getLayoutInflater().inflate(R
                                .layout.procapture,
                        mIApp.getAppUi().getModeRootView(), true);
                mLongExposureRoot = (RelativeLayout) mActivity.findViewById(R.id
                        .pro_ui);
                mLongExposureView = (LongExposureView) mActivity.findViewById(R.id
                        .pro_progress);

                mLongExposureView.setAddCountDownListener(mListener);
            }
        });
    }

    public static final String EXPOSURE_TIME_AUTO = "Auto";
    private void updateUiState(LongExposureViewState state) {
        if (mLongExposureView == null) {
            return;
        }
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LogHelper.e(TAG, "[updateUiState] state = " + state);
                mLongExposureView.updateViewState(state);
                if (LongExposureViewState.STATE_FINISH == state) {
                    mIApp.getAppUi().animationEnd(IAppUi.AnimationType.TYPE_CAPTURE);
                    mLongExposureRoot.setVisibility(View.INVISIBLE);
                    mIApp.getAppUi().applyAllUIVisibility(View.VISIBLE);
                    mIApp.getAppUi().applyAllUIEnabled(true);
                } else if (LongExposureViewState.STATE_ABORT == state) {
                    mLongExposureRoot.setVisibility(View.INVISIBLE);
                    mLongExposureRoot.setClickable(false);
                    mIApp.getAppUi().applyAllUIVisibility(View.VISIBLE);
                    mIApp.getAppUi().applyAllUIEnabled(false);
                } else if (LongExposureViewState.STATE_START == state) {
                    mIApp.getAppUi().animationStart(IAppUi.AnimationType.TYPE_CAPTURE, null);
                    ISettingManager.SettingController controller = mISettingManager
                            .getSettingController();
                    //String speed = "5";//controller.queryValue(KEY_SHUTTER_SPEED);
                    LogHelper.e(TAG, "[updateUi] mShutterSpeed speed = " + speed);
                    if (speed != null && !EXPOSURE_TIME_AUTO.equals(speed)) {
                        mIApp.getAppUi().applyAllUIVisibility(View.INVISIBLE);
                        mLongExposureRoot.setVisibility(View.VISIBLE);
                        mLongExposureRoot.setClickable(true);
                        mLongExposureView.setCountdownTime(Integer.parseInt(speed));
                        mLongExposureView.startCountDown();
                    } else {
                       // mIDeviceController.setNeedWaitPictureDone(true);
                    }
                }
            }
        });
    }
    
    private LongExposureView.OnCountDownFinishListener mListener = new LongExposureView.OnCountDownFinishListener() {
        @Override
        public void countDownFinished(boolean isFullProgress) {
            if(isFullProgress){
                updateUiState(LongExposureViewState.STATE_FINISH);
            }

        }
    };
    //add by huangfei for shutter end

}
