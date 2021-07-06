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

package com.mediatek.camera.common.mode.photo;

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
import com.mediatek.camera.common.mode.photo.device.DeviceControllerFactory;
import com.mediatek.camera.common.mode.photo.device.IDeviceController;
import com.mediatek.camera.common.mode.photo.device.IDeviceController.DataCallbackInfo;
import com.mediatek.camera.common.mode.photo.device.IDeviceController.DeviceCallback;
import com.mediatek.camera.common.mode.photo.device.IDeviceController.CaptureDataCallback;
import com.mediatek.camera.common.mode.photo.device.IDeviceController.PreviewSizeCallback;
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

//*/ hct.huangfei, 20201030. add customize zoom.
import com.mediatek.camera.common.mode.photo.view.IZoomSliderUI;
//*/
import com.mediatek.camera.feature.mode.aiworksfacebeauty.util.ImageUtil;

/**
 * Normal photo mode that is used to take normal picture.
 */
public class PhotoMode extends CameraModeBase implements CaptureDataCallback,
        DeviceCallback, PreviewSizeCallback, IMemoryManager.IMemoryListener,
        CaptureImageSavedCallback {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(PhotoMode.class.getSimpleName());
    private static final String KEY_MATRIX_DISPLAY_SHOW = "key_matrix_display_show";
    private static final String KEY_PICTURE_SIZE = "key_picture_size";
    /*-- add by bv liangchangwei for waterMark modify--*/
    private static final String KEY_WATERMARK = "key_water_mark";
    /*-- add by bv liangchangwei for waterMark modify--*/
    private static final String KEY_FORMTAT = "key_format";
    private static final String KEY_DNG = "key_dng";
    private static final String JPEG_CALLBACK = "jpeg callback";
    private static final String POST_VIEW_CALLBACK = "post view callback";
    private static final long DNG_IMAGE_SIZE = 45 * 1024 * 1024;

    protected static final String PHOTO_CAPTURE_START = "start";
    protected static final String PHOTO_CAPTURE_STOP = "stop";
    protected static final String KEY_PHOTO_CAPTURE = "key_photo_capture";

    protected IDeviceController mIDeviceController;
    protected PhotoModeHelper mPhotoModeHelper;
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
    protected boolean isShowProgressBar = false;

    //*/ hct.huangfei, 20201030. add customize zoom.
    private IZoomSliderUI mIZoomSliderUI;
    private IApp mApp;
    //*/

    //*/ hct.huangfei, 20201110. continuousshot numbers.
    private static final String KEY_CSHOT = "key_continuous_shot";
    private static final String STATE_START = "start";
    private static final String STATE_STOP = "stop";
    //*/
    
    /* hct.wangsenhao, for camera switch @{*/
    private String mCameraIdTriple;
    /* }@ hct.wangsenhao*/
    private int mHdrPhotoCount = 0;
    //bv liangchangwei add for HDR
    
    @Override
    public void init(@Nonnull IApp app, @Nonnull ICameraContext cameraContext,
            boolean isFromLaunch) {
        LogHelper.d(TAG, "[init]+");
        super.init(app, cameraContext, isFromLaunch);

        //*/ hct.huangfei, 20201026. add customize zoom.
        mApp = app;
        //*/

        mCameraId = getCameraIdByFacing(mDataStore.getValue(
                KEY_CAMERA_SWITCHER, null, mDataStore.getGlobalScope()));
        LogHelper.d(TAG, "[init] mCameraId " + mCameraId);
        // Device controller must be initialize before set preview size, because surfaceAvailable
        // may be called immediately when setPreviewSize.
        /* hct.wangsenhao, for camera switch @{ */
        if(isSupportTriple && !FRONT_CAMERA_ID.equals(mCameraId)){
            mCameraIdTriple = getCameraIdByFacing(mDataStore.getValue(
                    KEY_TRIPLE_SWITCH, null, mDataStore.getGlobalScope()));
            mCameraId = mCameraIdTriple;
        }
        /* }@ hct.wangsenhao */
        DeviceControllerFactory deviceControllerFactory = new DeviceControllerFactory();
        mIDeviceController = deviceControllerFactory.createDeviceController(app.getActivity(),
                mCameraApi, mICameraContext);
        initSettingManager(mCameraId);
        initStatusMonitor();
        prepareAndOpenCamera(false, mCameraId, isFromLaunch);
        ThumbnailHelper.setApp(app);
        mMemoryManager = new MemoryManagerImpl(app.getActivity());
        mPhotoModeHelper = new PhotoModeHelper(cameraContext);
        LogHelper.d(TAG, "[init]- ");

        //*/ hct.huangfei, 20201030. add customize zoom.
        if (CameraUtil.isZoomViewCustomizeSupport(app.getActivity())) {
            mIZoomSliderUI = app.getAppUi().getZoomSliderUI();
            mIZoomSliderUI.init();

        }
        //*/
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
        mIDeviceController.queryCameraDeviceManager();
        prepareAndOpenCamera(false, mCameraId, false);
    }

    @Override
    public void pause(@Nullable DeviceUsage nextModeDeviceUsage) {
        LogHelper.i(TAG, "[pause]+ mISurfaceStatusListener ="+mISurfaceStatusListener);
        super.pause(nextModeDeviceUsage);
        mIsResumed = false;
        mMemoryManager.removeListener(this);
        if(CameraUtil.MTKCAM_AIWORKS_HDR_SUPPORT){
            if(isShowProgressBar){
                mIApp.getAppUi().HideShutterProgressBar();
                isShowProgressBar = false;
            }
            mIApp.getAppUi().setPictureProcessing(false);
        }
        //clear the surface listener
        mIApp.getAppUi().clearPreviewStatusListener(mISurfaceStatusListener);
        if (mNeedCloseCameraIds.size() > 0) {
            LogHelper.i(TAG, "[pause] mNeedCloseCameraId = " + mCameraId);
            prePareAndCloseCamera(needCloseCameraSync(), mCameraId);
            recycleSettingManager(mCameraId);
        } else if (mNeedCloseSession){
            clearAllCallbacks(mCameraId);
            mIDeviceController.closeSession();
        } else{
            clearAllCallbacks(mCameraId);
            mIDeviceController.stopPreview();
        }
        LogHelper.i(TAG, "[pause]-");
    }

    @Override
    public void unInit() {
        LogHelper.i(TAG, "[unInit]+");
        super.unInit();
        mIDeviceController.destroyDeviceController();

        //*/ hct.huangfei, 20201030. add customize zoom.
        if (CameraUtil.isZoomViewCustomizeSupport(mApp.getActivity())){
            mIZoomSliderUI.unInit();
        }
        //*/

        LogHelper.i(TAG, "[unInit]-");
    }

    @Override
    public boolean onCameraSelected(@Nonnull String newCameraId) {
        LogHelper.i(TAG, "[onCameraSelected] ,new id:" + newCameraId + ",current id:" + mCameraId+" mIsResumed ="+mIsResumed);
        super.onCameraSelected(newCameraId);
        //first need check whether can switch camera or not.

	if ("com.mediatek.camera.common.mode.photo.intent.IntentPhotoMode".equals(getModeKey())) {
	    if (canSelectCamera(newCameraId) && !mIApp.getAppUi().isHdrPictureProcessing() && mIsResumed) {
            if (CameraUtil.isZoomViewCustomizeSupport(mApp.getActivity())){
                mIZoomSliderUI.showCircleTextView();
            }
            doCameraSelect(mCameraId, newCameraId);
            return true;
        } else {
            return false;
        }
	}
	if (canSelectCamera(newCameraId) && !isShowProgressBar && mIsResumed) {
            //HCT:ouyang Customize zoom
            if (CameraUtil.isZoomViewCustomizeSupport(mApp.getActivity())){
                mIZoomSliderUI.showCircleTextView();
            }
            //*/

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
        boolean isDeviceReady = mIDeviceController.isReadyForCapture();
        LogHelper.i(TAG, "onShutterButtonClick, is storage ready : " + storageReady + "," +
                "isDeviceReady = " + isDeviceReady + " isShowProgressBar = " + isShowProgressBar);

        if (storageReady && isDeviceReady && mIsResumed
                && mMemoryState != IMemoryManager.MemoryAction.STOP && (isShowProgressBar == false)) {
            //trigger capture animation
            startCaptureAnimation();
            mPhotoStatusResponder.statusChanged(KEY_PHOTO_CAPTURE, PHOTO_CAPTURE_START);
            updateModeDeviceState(MODE_DEVICE_STATE_CAPTURING);
            disableAllUIExceptionShutter();
            mIDeviceController.updateGSensorOrientation(mIApp.getGSensorOrientation());
			//bv liangchangwei add for HDR
            if(CameraUtil.MTKCAM_AIWORKS_HDR_SUPPORT){
                String value = mDataStore.getValue("key_hdr", "off", mDataStore.getGlobalScope());
                LogHelper.i(TAG,"doShutterButtonClick key_hdr = " + value);
                //bv wuyonglin add for hd shot 20201013 start
                String mFlashStatus = mDataStore.getValue("key_flash", "off", "_preferences_0");
                if (Integer.parseInt(mCameraId) == 1) {
                    mFlashStatus = "off";
                }
                String mAisStatus = mDataStore.getValue("key_ais", "off", mDataStore.getCameraScope(Integer.parseInt(mCameraId)));
                if(value.equals("on")||value.equals("auto") || (mAisStatus.equals("on") && mFlashStatus.equals("off"))){
                //bv wuyonglin add for hd shot 20201013 end
                    mIApp.getAppUi().ShowShutterProgressBar();
                    isShowProgressBar = true;
                }
                mIApp.getAppUi().setPictureProcessing(true);
            }
            /* add by bv liangchangwei 20200827 for fixbug 2007 start*/
            if(mIApp.getAppUi().getZoomSliderUICtrl().isShowAll()){
                mIApp.getAppUi().getZoomSliderUICtrl().slidingArcViewHide();
            }
            /* add by bv liangchangwei 20200827 for fixbug 2007 end*/

            mIDeviceController.takePicture(this);
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
        LogHelper.i(TAG, "onDataReceived, data = " + data + ",mIsResumed = " + mIsResumed +
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
                    mIDeviceController.startPreview();
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
        LogHelper.d(TAG, "[onPostViewCallback] data = " + data + ",mIsResumed = " + mIsResumed + " size = " + data.length);
        CameraSysTrace.onEventSystrace(POST_VIEW_CALLBACK, true, true);
        if (data != null && mIsResumed) {
            //will update the thumbnail
            int rotation = CameraUtil.getJpegRotationFromDeviceSpec(Integer.parseInt(mCameraId),
                    mIApp.getGSensorOrientation(), mIApp.getActivity());

            //*/ hct.huangfei, 20201027. add camera mirror.
            /*Bitmap bitmap = BitmapCreator.createBitmapFromYuv(data,
                    ThumbnailHelper.POST_VIEW_FORMAT,
                    ThumbnailHelper.getThumbnailWidth(),
                    ThumbnailHelper.getThumbnailHeight(),
                    mIApp.getAppUi().getThumbnailViewWidth(),
                    rotation);*/
            String value = mDataStore.getValue("key_hdr", "off", mDataStore.getGlobalScope());
	    //bv wuyonglin add for hd shot 20201013 start
            String mFlashStatus = mDataStore.getValue("key_flash", "off", "_preferences_0");
            String mAisStatus = mDataStore.getValue("key_ais", "off", mDataStore.getCameraScope(Integer.parseInt(mCameraId)));
            if (Integer.parseInt(mCameraId) == 1) {
		mFlashStatus = "off";
            }
            if(CameraUtil.MTKCAM_AIWORKS_HDR_SUPPORT && ((value.equals("on")||value.equals("auto")) || (mAisStatus.equals("on") && mFlashStatus.equals("off")))){
	    //bv wuyonglin add for hd shot 20201013 end
                LogHelper.d(TAG, "[onPostViewCallback] mPreviewWidth = " + mPreviewWidth + ",mPreviewHeight = " + mPreviewHeight + " targetWidth = " + mIApp.getAppUi().getThumbnailViewWidth());
                updateThumbnail(data);
                //mIApp.getAppUi().updateThumbnail(bitmap);
            }else{
            Bitmap bitmap = BitmapCreator.createBitmapFromYuv(data,
                    ThumbnailHelper.POST_VIEW_FORMAT,
                    ThumbnailHelper.getThumbnailWidth(),
                    ThumbnailHelper.getThumbnailHeight(),
                    mIApp.getAppUi().getThumbnailViewWidth(),
                    rotation, mIApp, mICameraContext);
            //*/

                mIApp.getAppUi().updateThumbnail(bitmap);
            }
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
        if (!mIsMatrixDisplayShow) {
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
        LogHelper.i(TAG, "[onPreviewSizeReady] previewSize: " + previewSize.toString());
        updatePictureSizeAndPreviewSize(previewSize);
    }

    private void onPreviewSizeChanged(int width, int height) {
        mPreviewWidth = width;
        mPreviewHeight = height;
        LogHelper.i(TAG, "[onPreviewSizeChanged] mISurfaceStatusListener: " + mISurfaceStatusListener+" width ="+width+" height ="+height);
        mIApp.getAppUi().setPreviewSize(mPreviewWidth, mPreviewHeight, mISurfaceStatusListener);
        LogHelper.i(TAG, "[onPreviewSizeChanged] end mISurfaceStatusListener: " + mISurfaceStatusListener);
    }

    private void prepareAndOpenCamera(boolean needOpenCameraSync, String cameraId,
            boolean needFastStartPreview) {
        mCameraId = cameraId;
        StatusMonitor statusMonitor = mICameraContext.getStatusMonitor(mCameraId);
        statusMonitor.registerValueChangedListener(KEY_PICTURE_SIZE, mStatusChangeListener);
        /*-- add by bv liangchangwei for waterMark modify--*/
        //statusMonitor.registerValueChangedListener(KEY_WATERMARK, mStatusChangeListener);
        /*-- add by bv liangchangwei for waterMark modify--*/
        statusMonitor.registerValueChangedListener(KEY_FORMTAT, mStatusChangeListener);
        statusMonitor.registerValueChangedListener(KEY_MATRIX_DISPLAY_SHOW, mStatusChangeListener);

        //*/ hct.huangfei, 20201110. continuousshot numbers.
        statusMonitor.registerValueChangedListener(KEY_CSHOT, mStatusChangeListener);
        //*/

        //before open camera, prepare the device callback and size changed callback.
        mIDeviceController.setDeviceCallback(this);
        mIDeviceController.setPreviewSizeReadyCallback(this);
        mIDeviceController.setSavedDataCallback(this);
        //prepare device info.
        DeviceInfo info = new DeviceInfo();
        info.setCameraId(mCameraId);
        info.setSettingManager(mISettingManager);
        info.setNeedOpenCameraSync(needOpenCameraSync);
        info.setNeedFastStartPreview(needFastStartPreview);
        mIDeviceController.openCamera(info);
    }

    private void prePareAndCloseCamera(boolean needSync, String cameraId) {
        clearAllCallbacks(cameraId);
        mIDeviceController.closeCamera(needSync);
        mIsMatrixDisplayShow = false;
        //reset the preview size
        mPreviewWidth = 0;
        mPreviewHeight = 0;
    }

    private void clearAllCallbacks(String cameraId) {
        mIDeviceController.setPreviewSizeReadyCallback(null);
        StatusMonitor statusMonitor = mICameraContext.getStatusMonitor(cameraId);
        statusMonitor.unregisterValueChangedListener(KEY_PICTURE_SIZE, mStatusChangeListener);
        /*-- add by bv liangchangwei for waterMark modify--*/
        //statusMonitor.unregisterValueChangedListener(KEY_WATERMARK, mStatusChangeListener);
        /*-- add by bv liangchangwei for waterMark modify--*/
        statusMonitor.unregisterValueChangedListener(KEY_FORMTAT, mStatusChangeListener);
        statusMonitor.unregisterValueChangedListener(
                KEY_MATRIX_DISPLAY_SHOW, mStatusChangeListener);

        //*/ hct.huangfei, 20201110. continuousshot numbers.
        statusMonitor.unregisterValueChangedListener(KEY_CSHOT, mStatusChangeListener);
        //*/

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
			    //bv liangchangwei add for HDR
                if(mHdrPhotoCount >0){
                    mHdrPhotoCount --;
                }
                if (mCapturingNumber == 0) {
                    mMemoryState = IMemoryManager.MemoryAction.NORMAL;
                    mIApp.getAppUi().hideSavingDialog();
                    mIApp.getAppUi().applyAllUIVisibility(View.VISIBLE);
                }
			    //bv liangchangwei add for HDR
                if(mHdrPhotoCount <= 0){
                    mIApp.getAppUi().setPictureProcessing(false);
                    mHdrPhotoCount = 0;
                }
            }
            LogHelper.i(TAG, "[onFileSaved] uri = " + uri + ", mCapturingNumber = "
                    + mCapturingNumber + " mHdrPhotoCount = " + mHdrPhotoCount);
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
            mIDeviceController.setPictureSize(new Size(mCaptureWidth, mCaptureHeight));
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
                //bv wuyonglin add for bug2564 202011003 start
                mHdrPhotoCount ++;
                //bv wuyonglin add for bug2564 202011003 end
                mCapturingNumber ++;
                mMemoryManager.checkOneShotMemoryAction(saveDataSize);
            }
            String fileDirectory = mICameraContext.getStorageService().getFileDirectory();
            Size exifSize = CameraUtil.getSizeFromSdkExif(data);
            ContentValues contentValues = mPhotoModeHelper.createContentValues(data,
                    fileDirectory, exifSize.getWidth(), exifSize.getHeight());
            //bv liangchangwei add for AiWorks begin
            String FilePath = contentValues.getAsString(MediaStore.Images.ImageColumns.DATA);
            LogHelper.i(TAG, "saveData, data = " + data + ",mIsResumed = " + mIsResumed +
                    ",FilePath = " + FilePath+" mHdrPhotoCount ="+mHdrPhotoCount);
            mIDeviceController.addExif(FilePath, data);
            //bv liangchangwei add for AiWorks end
            mICameraContext.getMediaSaver().addSaveRequest(data, contentValues, null,
                    mMediaSaverListener);
        }
        if(CameraUtil.MTKCAM_AIWORKS_HDR_SUPPORT){
            if(isShowProgressBar){
                mIApp.getAppUi().HideShutterProgressBar();
                isShowProgressBar = false;
            }
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
        //mIApp.getAppUi().updateThumbnail(bitmap);
        if (Integer.parseInt(mCameraId) == 1) {
            int orientation = ImageUtil.getJpegOrientation(data);
            Bitmap bitmapRotation = BitmapCreator.rotateBitmap(bitmap, orientation);
            boolean isMirror = "1".equals(mDataStore.getValue("key_camera_mirror", "1", mDataStore.getCameraScope(Integer.parseInt(mCameraId))));
            LogHelper.i(TAG,"updateThumbnail isMirror = " + isMirror+" mIApp.getGSensorOrientation() ="+mIApp.getGSensorOrientation()+" orientation ="+orientation);
            mIApp.getAppUi().updateThumbnail(BitmapCreator.setMirror(bitmapRotation, isMirror, orientation));
        } else {
            mIApp.getAppUi().updateThumbnail(bitmap);
        }
        LogHelper.i(TAG,"updateThumbnail isShowProgressBar = " + isShowProgressBar);
        if(CameraUtil.MTKCAM_AIWORKS_HDR_SUPPORT){
            if(isShowProgressBar && !"com.mediatek.camera.common.mode.photo.intent.IntentPhotoMode".equals(getModeKey())){
                mIApp.getAppUi().HideShutterProgressBar();
                isShowProgressBar = false;
            }
        }
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
            LogHelper.i(TAG, "surfaceAvailable,device controller = " + mIDeviceController
                    + ",w = " + width + ",h = " + height);
            if (mModeHandler != null) {
                mModeHandler.post(new Runnable() {
                    @Override
                    public void run() {
            if (mIDeviceController != null && mIsResumed) {
                mIDeviceController.updatePreviewSurface(surfaceObject);
            }
        }
                });
            }
        }

        @Override
        public void surfaceChanged(Object surfaceObject, int width, int height) {
            LogHelper.i(TAG, "surfaceChanged, device controller = " + mIDeviceController
                    + ",w = " + width + ",h = " + height);
            if (mModeHandler != null) {
                mModeHandler.post(new Runnable() {
                    @Override
                    public void run() {
            if (mIDeviceController != null && mIsResumed) {
                mIDeviceController.updatePreviewSurface(surfaceObject);
            }
        }
                });
            }
        }

        @Override
        public void surfaceDestroyed(Object surfaceObject, int width, int height) {
            LogHelper.i(TAG, "surfaceDestroyed,device controller = " + mIDeviceController);
        }
    }

    /**
     * Status change listener implement.
     */
    private class MyStatusChangeListener implements StatusChangeListener {
        @Override
        public void onStatusChanged(String key, String value) {
            LogHelper.i(TAG, "[onStatusChanged] key = " + key + ",value = " + value);
            if (KEY_PICTURE_SIZE.equalsIgnoreCase(key)) {
                String[] sizes = value.split("x");
                mCaptureWidth = Integer.parseInt(sizes[0]);
                mCaptureHeight = Integer.parseInt(sizes[1]);
                mIDeviceController.setPictureSize(new Size(mCaptureWidth, mCaptureHeight));
                Size previewSize = mIDeviceController.getPreviewSize((double) mCaptureWidth /
                        mCaptureHeight);
                int width = previewSize.getWidth();
                int height = previewSize.getHeight();
                if (width != mPreviewWidth || height != mPreviewHeight) {
                    onPreviewSizeChanged(width, height);
                }
            } else if (KEY_MATRIX_DISPLAY_SHOW.equals(key)) {
                mIsMatrixDisplayShow = "true".equals(value);
            } else if (KEY_FORMTAT.equalsIgnoreCase(key)) {
                mIDeviceController.setFormat(value);
                LogHelper.i(TAG, "[onStatusChanged] key = " + key + ", set sCaptureFormat = " + value);
            }

            //*/ hct.huangfei, 20201110. continuousshot numbers.
            else if (KEY_CSHOT.equalsIgnoreCase(key)) {
                if(STATE_START.equals(value)){
                    mIDeviceController.releaseCaptureDataCallback();
                }
            }
            //*/
            /*-- add by bv liangchangwei for waterMark modify--*/
            else if(KEY_WATERMARK.equals(key)){
                if(value.equals("on")){
                    mIDeviceController.setFormat("heif");
                    LogHelper.i(TAG,"lcw KEY_WATERMARK on setFormat heif");
                }else if(value.equals("off")){
                    mIDeviceController.setFormat("jpeg");
                    LogHelper.i(TAG,"lcw KEY_WATERMARK off setFormat jpeg");
                }
            }
            /*-- add by bv liangchangwei for waterMark modify--*/
        }
    }
}
