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

package com.mediatek.camera.feature.mode.night;

import com.mediatek.camera.common.mode.photo.view.IZoomSliderUI;
import com.mediatek.camera.common.relation.Relation;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.view.View;
import android.os.Environment;
import android.widget.Toast;
import android.app.Activity;
import android.content.Context;

import com.mediatek.camera.common.IAppUi;
import com.mediatek.camera.common.IAppUi.AnimationData;
import com.mediatek.camera.common.IAppUiListener.ISurfaceStatusListener;
import com.mediatek.camera.common.ICameraContext;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.CameraSysTrace;
//import com.mediatek.camera.common.debug.Log;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.device.CameraDeviceManagerFactory;
import com.mediatek.camera.common.device.CameraDeviceManagerFactory.CameraApi;
import com.mediatek.camera.common.memory.IMemoryManager;
import com.mediatek.camera.common.memory.MemoryManagerImpl;
import com.mediatek.camera.common.mode.CameraApiHelper;
import com.mediatek.camera.common.mode.CameraModeBase;
import com.mediatek.camera.common.mode.DeviceUsage;
import com.mediatek.camera.feature.mode.night.device.INightDeviceController;
import com.mediatek.camera.feature.mode.night.device.INightDeviceController.DataCallbackInfo;
import com.mediatek.camera.feature.mode.night.device.INightDeviceController.DeviceCallback;
import com.mediatek.camera.feature.mode.night.device.INightDeviceController.CaptureDataCallback;
import com.mediatek.camera.feature.mode.night.device.INightDeviceController.PreviewSizeCallback;
import com.mediatek.camera.feature.mode.night.device.NightDeviceControllerFactory;

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
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.mediatek.camera.common.mode.photo.HeifHelper;
import com.mediatek.camera.common.mode.photo.ThumbnailHelper;
import android.util.Log;

/**
 * Normal photo mode that is used to take normal picture.
 */
public class NightMode extends CameraModeBase implements CaptureDataCallback,
        DeviceCallback, PreviewSizeCallback, IMemoryManager.IMemoryListener,
        CaptureImageSavedCallback {
    //private static final LogUtil.Tag TAG = new LogUtil.Tag(NightMode.class.getSimpleName());
    private static final String TAG = "NightMode";
    private static final String KEY_MATRIX_DISPLAY_SHOW = "key_matrix_display_show";
    private static final String MODE_KEY = NightMode.class.getName();
    private static final String KEY_PICTURE_SIZE = "key_picture_size";
    private static final String KEY_FORMTAT = "key_format";
    private static final String KEY_DNG = "key_dng";
    private static final String JPEG_CALLBACK = "jpeg callback";
    private static final String POST_VIEW_CALLBACK = "post view callback";
    private static final long DNG_IMAGE_SIZE = 45 * 1024 * 1024;

    protected static final String PHOTO_CAPTURE_START = "start";
    protected static final String PHOTO_CAPTURE_STOP = "stop";
    protected static final String KEY_PHOTO_CAPTURE = "key_photo_capture";

    protected INightDeviceController mINightDeviceController;
    protected NightModeHelper mNightModeHelper;
    protected int mCaptureWidth;
    // make sure the picture size ratio = mCaptureWidth / mCaptureHeight not to NAN.
    protected int mCaptureHeight = Integer.MAX_VALUE;
    //the reason is if surface is ready, let it to set to device controller, otherwise
    //if surface is ready but activity is not into resume ,will found the preview
    //can not start preview.
    protected volatile boolean mIsResumed = true;
    private String mCameraId;
    private Activity mActivity;
    private Context mContext;

    private ISurfaceStatusListener mISurfaceStatusListener = new SurfaceChangeListener();
    private ISettingManager mISettingManager;
    private MemoryManagerImpl mMemoryManager;
    private int mPreviewFormat;
    private int mPreviewWidth;
    private int mPreviewHeight;
    //make sure it is in capturing to show the saving UI.
    private int mCapturingNumber = 0;
    private int mNightPhotoCount = 0;
    private boolean mIsMatrixDisplayShow = false;
    private Object mCaptureNumberSync = new Object();
    private StatusChangeListener mStatusChangeListener = new MyStatusChangeListener();
    private IMemoryManager.MemoryAction mMemoryState = IMemoryManager.MemoryAction.NORMAL;
    protected StatusMonitor.StatusResponder mPhotoStatusResponder;
    private boolean isShowProgressBar = false;
    //start, wangsenhao for triple switch, 2020.02.25
    private String mCameraIdTriple;
    //end, wangsenhao for triple switch, 2020.02.25
    private IZoomSliderUI mIZoomSliderUI; //HCT:ouyang Customize zoom

    @Override
    public void init(@Nonnull IApp app, @Nonnull ICameraContext cameraContext,
            boolean isFromLaunch) {
        Log.d(TAG, "[init]+");
        super.init(app, cameraContext, isFromLaunch);
        mActivity = app.getActivity();
        mCameraId = getCameraIdByFacing(mDataStore.getValue(
                KEY_CAMERA_SWITCHER, null, mDataStore.getGlobalScope()));
        Log.d(TAG, "[init] mCameraId " + mCameraId);
        // Device controller must be initialize before set preview size, because surfaceAvailable
        // may be called immediately when setPreviewSize.
        //initNightCapture(); // init Night data

        //start, wangsenhao for triple switch, 2020.02.25
        if(isSupportTriple){
            mCameraIdTriple = getCameraIdByFacing(mDataStore.getValue(
                    KEY_TRIPLE_SWITCH, null, mDataStore.getGlobalScope()));
            if(wide_camera_id.equals(mCameraIdTriple)){
                mCameraId = mCameraIdTriple;
            } else if(macro_camera_id.equals(mCameraIdTriple)){
                mCameraId = mCameraIdTriple;
            }
            Log.d(TAG, "[init] isSupportTriple mCameraId " + mCameraId);
        }
        //end, wangsenhao for triple switch, 2020.02.25

        NightDeviceControllerFactory deviceControllerFactory = new NightDeviceControllerFactory();
        mINightDeviceController = deviceControllerFactory.createDeviceController(app.getActivity(),
                mCameraApi, mICameraContext);
        initSettingManager(mCameraId);
        initStatusMonitor();
        //mNightCaptureResult.setCaptureCallback(mINightDeviceController);
        prepareAndOpenCamera(false, mCameraId, isFromLaunch);
        ThumbnailHelper.setApp(app);
        mMemoryManager = new MemoryManagerImpl(app.getActivity());
        mNightModeHelper = new NightModeHelper(cameraContext);
        //HCT:ouyang Customize zoom begin
        if (CameraUtil.isZoomViewCustomizeSupport(app.getActivity())) {
            mIZoomSliderUI = app.getAppUi().getZoomSliderUI();
            mIZoomSliderUI.init();
        }
        //HCT:young end
        Log.d(TAG, "[init]- ");
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
        mINightDeviceController.queryCameraDeviceManager();
        prepareAndOpenCamera(false, mCameraId, false);
    }

    @Override
    public void pause(@Nullable DeviceUsage nextModeDeviceUsage) {
        Log.i(TAG, "[pause]+");

        super.pause(nextModeDeviceUsage);
        mIsResumed = false;
        mMemoryManager.removeListener(this);
        if(CameraUtil.MTKCAM_AIWORKS_HDR_SUPPORT){
            if(isShowProgressBar){
                mIApp.getAppUi().HideShutterProgressBar();
                mIApp.getAppUi().setPictureProcessing(false);
                mNightPhotoCount = 0;
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
            mINightDeviceController.closeSession();
        } else{
            clearAllCallbacks(mCameraId);
            mINightDeviceController.stopPreview();
        }
        mINightDeviceController.stop();
        Log.i(TAG, "[pause]-");
    }

    @Override
    public void unInit() {
        super.unInit();
        mINightDeviceController.destroyDeviceController();
        //HCT:ouyang Customize zoom
        if (CameraUtil.isZoomViewCustomizeSupport(mActivity)){
            mIZoomSliderUI.unInit();
        }
    }

    @Override
    public boolean onCameraSelected(@Nonnull String newCameraId) {
        Log.i(TAG, "[onCameraSelected] ,new id:" + newCameraId + ",current id:" + mCameraId+" isShowProgressBar ="+isShowProgressBar+" mIsResumed ="+mIsResumed);
        super.onCameraSelected(newCameraId);
        //first need check whether can switch camera or not.
        if (canSelectCamera(newCameraId) && !isShowProgressBar && mIsResumed) {
            doCameraSelect(mCameraId, newCameraId);
            //HCT:ouyang Customize zoom
            if (CameraUtil.isZoomViewCustomizeSupport(mActivity)){
                mIZoomSliderUI.init();
            }
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
        boolean isDeviceReady = mINightDeviceController.isReadyForCapture();
        Log.i(TAG, "onShutterButtonClick, is storage ready : " + storageReady + "," +
                "isDeviceReady = " + isDeviceReady);

        if (storageReady && isDeviceReady && mIsResumed
                && mMemoryState != IMemoryManager.MemoryAction.STOP && (isShowProgressBar == false)) {
            //trigger capture animation
            startCaptureAnimation();
            mPhotoStatusResponder.statusChanged(KEY_PHOTO_CAPTURE, PHOTO_CAPTURE_START);
            updateModeDeviceState(MODE_DEVICE_STATE_CAPTURING);
            disableAllUIExceptionShutter();
            mINightDeviceController.updateGSensorOrientation(mIApp.getGSensorOrientation());
            mINightDeviceController.takePicture(this);
            mIApp.getAppUi().ShowShutterProgressBar();
            isShowProgressBar = true;
            Log.i(TAG,"doShutterButtonClick showCircleProgressBar");
            mIApp.getAppUi().setPictureProcessing(true);
            //bv wuyonglin add for bug2945 20201121 start
            mNightPhotoCount ++;
            //bv wuyonglin add for bug2945 20201121 end
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
        Log.d(TAG, "onDataReceived, data = " + data + ",mIsResumed = " + mIsResumed +
                ",needUpdateThumbnail = " + needUpdateThumbnail + ",needRestartPreview = " +
                needRestartPreview);
        if (data != null) {
            CameraSysTrace.onEventSystrace(JPEG_CALLBACK, true);
        }
        //save file first,because save file is in other thread, so will improve the shot to shot
        //performance.
        if (data != null) {
            Log.i(TAG,"onDataReceived format = " + format);
            if (format == ImageFormat.JPEG) {
                Log.i(TAG,"onDataReceived saveData ");
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
                Log.i(TAG, "onDataReceived,heif values =" +values.toString());
                mICameraContext.getMediaSaver().addSaveRequest(data, values, null,
                        mMediaSaverListener, HeifHelper.FORMAT_HEIF);
            }

        }
        //if camera is paused, don't need do start preview and other device related actions.
        if (mIsResumed) {
            //first do start preview in API1.
            if (mCameraApi == CameraApi.API1) {
                if (needRestartPreview && !mIsMatrixDisplayShow) {
                    mINightDeviceController.startPreview();
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
        //add by huangfei for continuousshot abnormal start
        mIApp.getAppUi().setCaptureStatus(false);
        //add by huangfei for continuousshot abnormal end
    }

    @Override
    public void onPostViewCallback(byte[] data) {
        Log.d(TAG, "[onPostViewCallback] data = " + data + ",mIsResumed = " + mIsResumed);
        CameraSysTrace.onEventSystrace(POST_VIEW_CALLBACK, true, true);
        if (data != null && mIsResumed) {
            //bv wuyonglin delete for bug2945 20201121 start
            //mNightPhotoCount++;
            //bv wuyonglin delete for bug2945 20201121 end
            updateThumbnail(data);
            /*
            int rotation = CameraUtil.getJpegRotationFromDeviceSpec(Integer.parseInt(mCameraId),
                    mIApp.getGSensorOrientation(), mIApp.getActivity());
					
            Bitmap bitmap = BitmapCreator.createBitmapFromYuv(data,
                    ThumbnailHelper.POST_VIEW_FORMAT,
                    ThumbnailHelper.getThumbnailWidth(),
                    ThumbnailHelper.getThumbnailHeight(),
                    mIApp.getAppUi().getThumbnailViewWidth(),
                    rotation, mIApp, mICameraContext);

            mIApp.getAppUi().updateThumbnail(bitmap);
*/
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
        Relation relation = NightRestriction.getRestriction().getRelation("on", true);

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
                Log.d(TAG, "[stopAllAnimations] run");
                stopCaptureAnimation();
            }
        });
        updateModeDeviceState(MODE_DEVICE_STATE_PREVIEWING);
        mPreviewFormat = format;
    }


    @Override
    public void onPreviewSizeReady(Size previewSize) {
        Log.d(TAG, "[onPreviewSizeReady] previewSize: " + previewSize.toString());
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
        mINightDeviceController.setDeviceCallback(this);
        mINightDeviceController.setPreviewSizeReadyCallback(this);
        //prepare device info.
        NightDeviceInfo info = new NightDeviceInfo();
        info.setCameraId(mCameraId);
        info.setSettingManager(mISettingManager);
        info.setNeedOpenCameraSync(needOpenCameraSync);
        info.setNeedFastStartPreview(needFastStartPreview);
        mINightDeviceController.openCamera(info);
    }

    private void prePareAndCloseCamera(boolean needSync, String cameraId) {
        clearAllCallbacks(cameraId);
        mINightDeviceController.closeCamera(needSync);
        mIsMatrixDisplayShow = false;
        //reset the preview size
        mPreviewWidth = 0;
        mPreviewHeight = 0;
    }

    private void clearAllCallbacks(String cameraId) {
        mINightDeviceController.setPreviewSizeReadyCallback(null);
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
        Log.d(TAG, "[canSelectCamera] +: " + value);
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
                    mIApp.getAppUi().applyAllUIVisibility(View.VISIBLE);
                }
                if(mNightPhotoCount >0){
                    mNightPhotoCount --;
                }
                if(mNightPhotoCount <= 0){
                    mIApp.getAppUi().setPictureProcessing(false);
                    mNightPhotoCount = 0;
                }
            }
            Log.d(TAG, "[onFileSaved] uri = " + uri + ", mCapturingNumber = "
                    + mCapturingNumber + " mNightPhotoCount = " + mNightPhotoCount);
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
            mINightDeviceController.setPictureSize(new Size(mCaptureWidth, mCaptureHeight));
            int width = previewSize.getWidth();
            int height = previewSize.getHeight();
            Log.d(TAG, "[updatePictureSizeAndPreviewSize] picture size: " + mCaptureWidth +
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
            ContentValues contentValues = mNightModeHelper.createContentValues(data,
                    fileDirectory, exifSize.getWidth(), exifSize.getHeight());
            String FilePath = contentValues.getAsString(MediaStore.Images.ImageColumns.DATA);
            mINightDeviceController.addExif(FilePath, data);
            mICameraContext.getMediaSaver().addSaveRequest(data, contentValues, null,
                    mMediaSaverListener);
        }
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
        mIApp.getAppUi().HideShutterProgressBar();
        isShowProgressBar = false;
        Log.i(TAG,"updateThumbnail hideCircleProgressBar");

    }

    @Override
    public void onMemoryStateChanged(IMemoryManager.MemoryAction state) {
        if (state == IMemoryManager.MemoryAction.STOP && mCapturingNumber != 0) {
            //show saving
            Log.d(TAG, "memory low, show saving");
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
            Log.d(TAG, "surfaceAvailable,device controller = " + mINightDeviceController
                    + ",w = " + width + ",h = " + height);
            if (mModeHandler != null) {
                mModeHandler.post(new Runnable() {
                    @Override
                    public void run() {
            if (mINightDeviceController != null && mIsResumed) {
                mINightDeviceController.updatePreviewSurface(surfaceObject);
            }
        }
                });
            }
        }

        @Override
        public void surfaceChanged(Object surfaceObject, int width, int height) {
            Log.d(TAG, "surfaceChanged, device controller = " + mINightDeviceController
                    + ",w = " + width + ",h = " + height);
            if (mModeHandler != null) {
                mModeHandler.post(new Runnable() {
                    @Override
                    public void run() {
            if (mINightDeviceController != null && mIsResumed) {
                mINightDeviceController.updatePreviewSurface(surfaceObject);
            }
        }
                });
            }
        }

        @Override
        public void surfaceDestroyed(Object surfaceObject, int width, int height) {
            Log.d(TAG, "surfaceDestroyed,device controller = " + mINightDeviceController);
        }
    }

    /**
     * Status change listener implement.
     */
    private class MyStatusChangeListener implements StatusChangeListener {
        @Override
        public void onStatusChanged(String key, String value) {
            Log.d(TAG, "[onStatusChanged] key = " + key + ",value = " + value);
            if (KEY_PICTURE_SIZE.equalsIgnoreCase(key)) {
                String[] sizes = value.split("x");
                mCaptureWidth = Integer.parseInt(sizes[0]);
                mCaptureHeight = Integer.parseInt(sizes[1]);
                mINightDeviceController.setPictureSize(new Size(mCaptureWidth, mCaptureHeight));
                Size previewSize = mINightDeviceController.getPreviewSize((double) mCaptureWidth /
                        mCaptureHeight);
                int width = previewSize.getWidth();
                int height = previewSize.getHeight();
                if (width != mPreviewWidth || height != mPreviewHeight) {
                    onPreviewSizeChanged(width, height);
                }
            } else if (KEY_MATRIX_DISPLAY_SHOW.equals(key)) {
                mIsMatrixDisplayShow = "true".equals(value);
            } else if (KEY_FORMTAT.equalsIgnoreCase(key)) {
                mINightDeviceController.setFormat(value);
                Log.i(TAG, "[onStatusChanged] key = " + key + ", set sCaptureFormat = " + value);
            }
        }
    }
}
