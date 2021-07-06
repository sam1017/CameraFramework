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

package com.mediatek.camera.feature.setting.zoom;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import com.mediatek.camera.common.IAppUiListener.OnGestureListener;
import com.mediatek.camera.common.ICameraContext;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil.Tag;
import com.mediatek.camera.common.mode.ICameraMode;
import com.mediatek.camera.common.setting.ISettingManager;
import com.mediatek.camera.common.setting.SettingBase;
import com.mediatek.camera.common.utils.CameraUtil;
import com.mediatek.camera.common.IAppUi;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

//*/ hct.huangfei, 20201030. add customize zoom.
import com.mediatek.camera.common.mode.photo.view.IZoomSliderUI;
//*/

//*/ hct.huangfei, 20201210.add volume key function.
import com.mediatek.camera.Config;
//*/

/**
 * This class is for zoom performing. Receiving scale gesture and perform zoom.
 */

//*/ hct.huangfei, 20201030. add customize zoom.
//public class Zoom extends SettingBase {
public class Zoom extends SettingBase implements IZoomSliderUI.ZoomSliderUIListener{
//*/

private Tag mTag;
    private ZoomGestureImpl mZoomGestureImpl = new ZoomGestureImpl();
    private ZoomViewCtrl mZoomViewCtrl = new ZoomViewCtrl();

    //Device Control
    private IZoomConfig mZoomConfig;
    private ZoomCaptureRequestConfig mCaptureRequestConfig;
    private ISettingChangeRequester mSettingChangeRequester;
    private String mOverrideValue = IZoomConfig.ZOOM_ON;
    private List<String> mSupportValues = new ArrayList<>();
    private Handler mModeHandler;
    // [Add for bit true test] Receive KEYCODE_ZOOM_IN and KEYCODE_ZOOM_OUT @{
    private static final int RATIO_INDEX_NULL = 0;
    private static final int RATIO_INDEX_EMPTY = 1;
    private static final int RATIO_INDEX_X10 = 2;
    private static final int RATIO_INDEX_X20 = 3;
    private static final int RATIO_INDEX_X30 = 4;
    private static final int RATIO_INDEX_X40 = 5;
    private static final String[] ZOOM_IN_TARGET_RATIO =
            new String[]{"x2.", "x2.", "x2.", "x3.", "x4.", "x4."};
    private static final String[] ZOOM_OUT_Target_RATIO =
            new String[]{"x1.0", "x1.0", "x1.0", "x1.0", "x2.0", "x3.0"};
    private static final float DISTANCE_RATIO_STEP = 0.01f;
    private static final int MSG_ZOOM_IN = 0;
    private static final int MSG_ZOOM_OUT = 1;
    private static final int MSG_DELAY = 50;
    private ZoomKeyEventListener mZoomKeyEventListener = new ZoomKeyEventListener();
    private MainHandler mMainHandler;
    private float mLastDistanceRatio = 0.0f;
    private String mCurrentRatioMsg;
    private String mCurrentMode = "";
    private OnOrientationChangeListenerImpl mOrientationChangeListener;
    private boolean isFirstInit = true;
    //bv wuyonglin add for add PanoramaMode should not show zoom view 20200103 start
    private String mModeKey = "com.mediatek.camera.feature.mode.panorama.PanoramaMode";
    //bv liangchangwei add for modify video and photomode show zoom
    private String mVideoModeKey = "com.mediatek.camera.common.mode.video.VideoMode";
    private String mNightModeKey = "com.mediatek.camera.feature.mode.night.NightMode";
    private String mDefaultModeKey = "com.mediatek.camera.common.mode.photo.PhotoMode";
    //bv liangchangwei add for modify video and photomode show zoom
    private String mCurrentModeKey = "com.mediatek.camera.common.mode.photo.PhotoMode";
    private String mLastModeKey = "com.mediatek.camera.common.mode.photo.PhotoMode";
    //bv wuyonglin add for add PanoramaMode should not show zoom view 20200103 end
    //bv wuyonglin add for adjust BrokehMode view 20200227 start
    private String mPiBrokehModeKey = "com.mediatek.camera.feature.mode.pibokeh.PiBokehMode";
    //bv wuyonglin add for adjust BrokehMode view 20200227 end
    //bv wuyonglin add for adjust FaceMode view 20200228 start
    private String mPiFaceBeautyModeKey = "com.mediatek.camera.feature.mode.pifacebeauty.PiFaceBeautyMode";
    //bv wuyonglin add for adjust FaceMode view 20200228 end
    //bv wuyonglin add for aiworks facebeauty and bokeh 20200720 start
    private String mAiWorksBrokehModeKey = "com.mediatek.camera.feature.mode.aiworksbokeh.AiWorksBokehMode";
    private String mAiWorksBrokehColorModeKey = "com.mediatek.camera.feature.mode.aiworksbokehcolor.AiWorksBokehColorMode";
    private String mAiWorksFaceBeautyModeKey = "com.mediatek.camera.feature.mode.aiworksfacebeauty.AiworksFaceBeautyMode";
    //bv wuyonglin add for aiworks facebeauty and bokeh 20200720 end
    // @}

    //*/ hct.huangfei, 20201030. add customize zoom.
    private IZoomSliderUI mZoomSliderUI;
    //*/
    //add by huangfei for zoom switch start
    private float mZoomSwitchSpan;
    //add by huangfei for zoom switch end

    /**
     * Initialize setting. This will be called when do open camera.
     *
     * @param app the instance of IApp.
     * @param cameraContext the CameraContext.
     * @param settingController the SettingController.
     */
    public void init(IApp app,
                     ICameraContext cameraContext,
                     ISettingManager.SettingController settingController) {
        super.init(app, cameraContext, settingController);
        mTag = new Tag(Zoom.class.getSimpleName() + "-" + settingController.getCameraId());
        mModeHandler = new Handler(Looper.myLooper());

        //*/ hct.huangfei, 20201030. add customize zoom.
        if(isZoomViewCustomizeSupport()){
            mZoomSliderUI = mAppUi.getZoomSliderUI();
            mZoomSliderUI.setZoomSliderUIListener(this);
        }
        //*/

        mZoomGestureImpl.init();
        mZoomViewCtrl.init(app);
        initSettingValue();
        mApp.registerOnOrientationChangeListener(mOrientationListener);
        mAppUi.registerGestureListener(mZoomGestureImpl, IApp.DEFAULT_PRIORITY);
        LogHelper.d(mTag, "[init] zoom: " + this + ", Gesture: " + mZoomGestureImpl);
        // [Add for bit true test] Receive KEYCODE_ZOOM_IN and KEYCODE_ZOOM_OUT @{
        mMainHandler = new MainHandler(mActivity.getMainLooper());
        mApp.registerKeyEventListener(mZoomKeyEventListener, IApp.DEFAULT_PRIORITY);
        mAppUi.setZoomViewListener(mZoomViewListener);
        // @}
    }

    /**
     * Un-initialize setting, this will be called before close camera.
     */
    @Override
    public void unInit() {
        //add by huangfeifor front tripleswitchhorizontal start
        mZoomSliderUI.removeZoomSliderUIListener(this);
        mAppUi.setZoomConfig(null);
        //add by huangfeifor front tripleswitchhorizontal end

        //bv wuyonglin delete for bug4488 20210329 start
        //*/ hct.huangfei, 20201030. add customize zoom.
        /*if(isZoomViewCustomizeSupport()){
            mZoomSliderUI.setZoomSliderUIListener(null);
        }*/
        //*/
        //bv wuyonglin delete for bug4488 20210329 end
        //add by huangfei for zoom switch start
        if(mAppUi.isZoomSwitchSupportCameraId()){
            mCaptureRequestConfig.removeOnZoomSwitchListener(mZoomSwitchListener);
        }
        //add by huangfei for zoom switch end

        mZoomViewCtrl.unInit();
        mApp.unregisterOnOrientationChangeListener(mOrientationListener);
        mAppUi.unregisterGestureListener(mZoomGestureImpl);
        LogHelper.d(mTag, "[unInit] zoom: " + this  + ", Gesture: " + mZoomGestureImpl);
        // [Add for bit true test] Receive KEYCODE_ZOOM_IN and KEYCODE_ZOOM_OUT @{
        mApp.unRegisterKeyEventListener(mZoomKeyEventListener);
        // @}
    }

    @Override
    public void postRestrictionAfterInitialized() {
    }

    @Override
    public SettingType getSettingType() {
        return SettingType.PHOTO_AND_VIDEO;
    }

    @Override
    public String getKey() {
        return IZoomConfig.KEY_CAMERA_ZOOM;
    }

    @Override
    public void onModeOpened(String modeKey, ICameraMode.ModeType modeType) {
        LogHelper.d(mTag, "[onModeOpened] modeKey " + modeKey + ",modeType " + modeType+" mZoomConfig ="+mZoomConfig);
        //bv wuyonglin add for add PanoramaMode should not show zoom view 20200103 start
        mCurrentModeKey = modeKey;
        mCurrentMode = modeKey;
        if((mLastModeKey.equals(mNightModeKey) || mLastModeKey.equals(mDefaultModeKey) || mLastModeKey.equals(mVideoModeKey))&& !(mCurrentModeKey.equals(mDefaultModeKey)||mCurrentModeKey.equals(mVideoModeKey) || mCurrentModeKey.equals(mNightModeKey))){
            if(mCaptureRequestConfig != null){
                mCaptureRequestConfig.hideListenRation();
            }
        }

        //bv wuyonglin add for aiworks facebeauty and bokeh 20200720 start
        if (!mModeKey.equals(mCurrentModeKey) && !mPiBrokehModeKey.equals(mCurrentModeKey) && !mAiWorksBrokehModeKey.equals(mCurrentModeKey)
		&& !mAiWorksFaceBeautyModeKey.equals(mCurrentModeKey) && !mAiWorksBrokehColorModeKey.equals(mCurrentModeKey)) { //bv wuyonglin modify for bokeh mode should not show zoom view 20200309
        //bv wuyonglin add for aiworks facebeauty and bokeh 20200720 end
            //bv wuyonglin add for add PanoramaMode should not show zoom view 20200103 end
            //bv wuyonglin add for adjust BrokehMode view 20200227 start
            //bv wuyonglin add for bokeh mode should not show zoom view 20200309 start
            //if (mPiBrokehModeKey.equals(mCurrentModeKey) || mPiFaceBeautyModeKey.equals(mCurrentModeKey)) {    //bv wuyonglin modify for adjust FaceMode view 20200228
            //bv liangchangwei add for modify video and photomode show zoom
            if (mPiFaceBeautyModeKey.equals(mCurrentModeKey) || mDefaultModeKey.equals(mCurrentModeKey) || mVideoModeKey.equals(mCurrentModeKey) || mNightModeKey.equals(mCurrentModeKey)) {
            //bv wuyonglin add for bokeh mode should not show zoom view 20200309 end
                        mZoomViewCtrl.onMarginBottomChanged(true);
            } else {
                        mZoomViewCtrl.onMarginBottomChanged(false);
            }
            //bv wuyonglin add for adjust BrokehMode view 20200227 end
            if (mZoomConfig != null) {
                /* modify by bv liangchangwei 20200827 fixbug 2009 start */
                //mZoomConfig.onScalePerformed(1.0f);
                mZoomConfig.onScale(1.0f);
                /* modify by bv liangchangwei 20200827 fixbug 2009 end */
                mZoomViewCtrl.showView("1x");
                if(mDefaultModeKey.equals(mCurrentModeKey) || mVideoModeKey.equals(mCurrentModeKey) || mNightModeKey.equals(mCurrentModeKey)){
                    if(mCaptureRequestConfig != null){
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mCaptureRequestConfig.resetListenRation();
                            }
                        });
                    }
                }
            }
            //bv wuyonglin add for add PanoramaMode should not show zoom view 20200103 start
        } else {
            //bv wuyonglin add for enter AiWorksBrokeh AiWorksBrokehColor AiWorksFaceBeauty mode zoom should be 1.0x 20200827 start
            if (mZoomConfig != null) {
                mZoomConfig.onScale(1.0f);
            }
            //bv wuyonglin add for enter AiWorksBrokeh AiWorksBrokehColor AiWorksFaceBeauty mode zoom should be 1.0x 20200827 end
		    mApp.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        LogHelper.d(mTag,"onModeOpened onConfigZoomUIVisibility GONE getZoomView = "
                            + mZoomViewCtrl.getZoomView());
                        if (mZoomViewCtrl.getZoomView() !=null) {
                            mZoomViewCtrl.getZoomView().setVisibility(View.GONE);
                        }
                    }
		    });
	    }
        //bv wuyonglin modify for add PanoramaMode should not show zoom view 20200103 end
    }

    @Override
    public synchronized void onModeClosed(String modeKey) {
        super.onModeClosed(modeKey);
        LogHelper.d(mTag, "[onModeClosed] modeKey " + modeKey );
        mLastModeKey = modeKey;
/*        if(mDefaultModeKey.equals(modeKey) || mVideoModeKey.equals(modeKey)){
            if(mCaptureRequestConfig != null){
                mCaptureRequestConfig.hideListenRation();
            }
        }*/

    }

    @Override
    public ICaptureRequestConfigure getCaptureRequestConfigure() {
        if (mCaptureRequestConfig == null) {
            mCaptureRequestConfig
                    = new ZoomCaptureRequestConfig(mSettingDevice2Requester,mCurrentMode,mAppUi);
            mCaptureRequestConfig.setZoomUpdateListener(mZoomLevelUpdateListener);
			//add by huangfei for zoom switch start
            if(mAppUi.isZoomSwitchSupportCameraId()){
				mCaptureRequestConfig.setOnZoomSwitchListener(mZoomSwitchListener);
            }
            //add by huangfei for zoom switch end
            mSettingChangeRequester = mCaptureRequestConfig;
            mZoomConfig = mCaptureRequestConfig;

            //*/ hct.huangfei, 20201030. add customize zoom.
            if(isZoomViewCustomizeSupport()){
                mZoomSliderUI.setZoomConfig(mZoomConfig);
            }
            //*/
            //add by huangfeifor front tripleswitchhorizontal start
            mAppUi.setZoomConfig(mZoomConfig);
            //add by huangfeifor front tripleswitchhorizontal end

            LogHelper.d(mTag, "[getCaptureRequestConfigure]mZoomConfig: "
                    + mSettingChangeRequester);
        }
        return (ZoomCaptureRequestConfig) mSettingChangeRequester;
    }

    @Override
    public void overrideValues(@Nonnull String headerKey, String currentValue,
                               List<String> supportValues) {
        super.overrideValues(headerKey, currentValue, supportValues);
        LogHelper.i(mTag, "[overrideValues] headerKey = " + headerKey
        + ", currentValue = " + currentValue);
        String curValue = getValue();
        updateRestrictionValue(curValue);
    }

    @Override
    public PreviewStateCallback getPreviewStateCallback() {
        return null;
    }

    private void updateRestrictionValue(String value) {
        mOverrideValue = value;
        if (IZoomConfig.ZOOM_OFF.equals(value)) {
            mZoomViewCtrl.hideView();
        }
    }

    private void initSettingValue() {
        mSupportValues.add(IZoomConfig.ZOOM_OFF);
        mSupportValues.add(IZoomConfig.ZOOM_ON);
        setSupportedPlatformValues(mSupportValues);
        setSupportedEntryValues(mSupportValues);
        setEntryValues(mSupportValues);
        String value = mDataStore.getValue(getKey(), IZoomConfig.ZOOM_ON, getStoreScope());
        setValue(value);
    }

    private IZoomConfig.OnZoomLevelUpdateListener mZoomLevelUpdateListener
            = new ZoomCaptureRequestConfig.OnZoomLevelUpdateListener() {
        @Override
        public void onZoomLevelUpdate(String ratio) {
            // [Add for bit true test] Receive KEYCODE_ZOOM_IN and KEYCODE_ZOOM_OUT @{
            mCurrentRatioMsg = ratio;
            // @}
	    if (isFirstInit) {
            LogHelper.d(mTag, "[onZoomLevelUpdate]  isFirstInit= "+isFirstInit);
            if (mZoomViewCtrl.getZoomView() != null) {
        mZoomViewCtrl.getZoomView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
		if (mZoomConfig != null) {
		if (mZoomConfig.getCurZoomRatio() != 1.0f && !mPiBrokehModeKey.equals(mCurrentModeKey) && !mAiWorksBrokehModeKey.equals(mCurrentModeKey)
		&& !mAiWorksFaceBeautyModeKey.equals(mCurrentModeKey) && !mAiWorksBrokehColorModeKey.equals(mCurrentModeKey)) {
                    mZoomConfig.onScalePerformed(1.0f);
            LogHelper.d(mTag, "[getCaptureRequestConfigure]mZoomConfig:showView 1x ");
                    mZoomViewCtrl.showView("1x");
		} else {
                    mZoomConfig.onScalePerformed(1/3f);
                    mZoomViewCtrl.showView("2x");
		}
		}
		requestZoom();
            }
        });
	}
		isFirstInit = false;
            }
            LogHelper.d(mTag, "[onZoomLevelUpdate]  end isFirstInit= "+isFirstInit);
            if (!mPiBrokehModeKey.equals(mCurrentModeKey) && !mAiWorksBrokehModeKey.equals(mCurrentModeKey)
		&& !mAiWorksFaceBeautyModeKey.equals(mCurrentModeKey) && !mAiWorksBrokehColorModeKey.equals(mCurrentModeKey)) {
            mZoomViewCtrl.showView(ratio);
            }
        }

        public String onGetOverrideValue() {
            return mOverrideValue;
        }
    };

    private IApp.OnOrientationChangeListener mOrientationListener =
            new IApp.OnOrientationChangeListener() {
                @Override
                public void onOrientationChanged(int orientation) {
                    if (mZoomViewCtrl != null && mZoomViewCtrl.getZoomView() != null) {
                        CameraUtil.rotateRotateLayoutChildView(mApp.getActivity(), mZoomViewCtrl.getZoomView(), orientation, true);
                    }
                }
            };

    /**
     * Class for zoom gesture listener.
     */
    private class ZoomGestureImpl implements OnGestureListener {
        private static final double MAX_DISTANCE_RATIO_WITH_SCREEN = 1.0 / 5.0;
        private float mPreviousSpan;
        private int mScreenDistance;
        private double mLastDistanceRatio;

        /**
         * Init distance ratio.
         */
        public void init() {
            int high = mApp.getActivity().getWindowManager().getDefaultDisplay().getHeight();
            int width = mApp.getActivity().getWindowManager().getDefaultDisplay().getWidth();
            mScreenDistance = high >= width ? high : width;
            mScreenDistance *= MAX_DISTANCE_RATIO_WITH_SCREEN;
        }

        @Override
        public boolean onDown(MotionEvent event) {
            return false;
        }

        @Override
        public boolean onUp(MotionEvent event) {
            return false;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float dx, float dy) {
            return false;
        }

        @Override
        public boolean onSingleTapUp(float x, float y) {
            return false;
        }

        @Override
        public boolean onSingleTapConfirmed(float x, float y) {
            return false;
        }

        @Override
        public boolean onDoubleTap(float x, float y) {
            return false;
        }

        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
            //bv wuyonglin add for bug3751 20210224 start
            if (CameraUtil.isVideo_HDR_changing) {
                LogHelper.i(mTag, "[onScale], don't do scale change for when isVideo_HDR_changing = true");
                return false;
            }
            if(mAppUi.isHdrPictureProcessing()){
                LogHelper.i(mTag, "[onItemClick], don't do camera id change for when mCameraAppUI.isHdrPictureProcessing() = " + mAppUi.isHdrPictureProcessing());
                return false;
            }
            //bv wuyonglin add for bug3751 20210224 end

            //add by huangfei for zoom switch start
            //modify by bv liangchangwei for fixbug 3660
            if(/*mAppUi.isZoomSwitchSupportCameraId() && */mZoomSliderUI.isShowAll()){
                return false;
            }
            //add by huangfei for zoom switch end
            //First, if it should not zoom, return false.
            String curValue = getValue();
            if (IZoomConfig.ZOOM_OFF.equals(curValue)) {
                return false;
            }
            if (mZoomConfig != null) {
                //add by huangfei for zoom switch start
                float span = mAppUi.getPreviousSpan();
                float basicZoomRatio = mAppUi.getBasicZoomRatio();
                if(mAppUi.isZoomSwitchSupportCameraId() && mPreviousSpan==0){
                    if(mPreviousSpan==0){
                        mPreviousSpan = scaleGestureDetector.getCurrentSpan();
                    }
                }
				//add by huangfei for zoom switch end
                double distanceRatio = calculateDistanceRatio(scaleGestureDetector);
                mZoomConfig.onScalePerformed(distanceRatio);
                //modify by huangfei for zoom switch start
                //if (Math.abs(distanceRatio - mLastDistanceRatio) > 0.08) {
                //modify by bv liangchangwei for fixbug 3518
                float direction = (float)(distanceRatio - mLastDistanceRatio);
                if (Math.abs(distanceRatio - mLastDistanceRatio) > 0.02 ||span!=0.0f) {
                //modify by huangfei for zoom switch end
                    requestZoom();
                    mLastDistanceRatio = distanceRatio;
                    //add by huangfei for zoom switch start
                    if(mAppUi.isZoomSwitchSupportCameraId() && mAppUi.isZoomSwitchMode()){
                        LogHelper.i(mTag,"onScale getCameraSwitchByZoom 1 distanceRatio = " + distanceRatio + " basicZoomRatio = " + basicZoomRatio + " direction = " + direction);
                        if(direction > 0 && mAppUi.getCameraId().equals("2")){
                            mZoomConfig.getCameraSwitchByZoom(distanceRatio,basicZoomRatio,0,0);
                        }else if(direction < 0 && mAppUi.getCameraId().equals("0")){
                            mZoomConfig.getCameraSwitchByZoom(distanceRatio,basicZoomRatio,0,0);
                        }else{
                            LogHelper.i(mTag,"do not need getCameraSwitchByZoom");
                        }
                    }
                    //add by huangfei for zoom switch end
                    //modify by bv liangchangwei for fixbug 3518
                }
            }
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
            //bv wuyonglin add for bug3751 20210224 start
            if (CameraUtil.isVideo_HDR_changing) {
                LogHelper.i(mTag, "[onScaleBegin], don't do scale begin change for when isVideo_HDR_changing = true");
                return false;
            }
            if(mAppUi.isHdrPictureProcessing()){
                LogHelper.i(mTag, "[onItemClick], don't do camera id change for when mCameraAppUI.isHdrPictureProcessing() = " + mAppUi.isHdrPictureProcessing());
                return false;
            }
            //bv wuyonglin add for bug3751 20210224 end

            //add by huangfei for zoom switch start
            if(mAppUi.isZoomSwitchSupportCameraId() && mZoomSliderUI.isShowAll()){
                return false;
            }
            mOverrideValue = IZoomConfig.ZOOM_ON;
            //add by huangfei for zoom switch end

            String curValue = getValue();
            if (IZoomConfig.ZOOM_OFF.equals(curValue)) {
                return false;
            }
            LogHelper.d(mTag, "[onScaleBegin], Gesture: " + this + ", mZoomConfig: " + mZoomConfig);
            if (mZoomConfig != null) {
                mZoomViewCtrl.clearInvalidView();
                mZoomConfig.onScaleStatus(true);
                mPreviousSpan = scaleGestureDetector.getCurrentSpan();
                mLastDistanceRatio = 0;
            }
            return true;
        }

        @Override
        public boolean onScaleEnd(ScaleGestureDetector scaleGestureDetector) {
            String curValue = getValue();

            //add by huangfei for zoom switch start
            if(mAppUi.isZoomSwitchSupportCameraId() && mZoomSliderUI.isShowAll()){
                return false;
            }
            mOverrideValue = IZoomConfig.ZOOM_OFF;
            //add by huangfei for zoom switch end

            if (IZoomConfig.ZOOM_OFF.equals(curValue)) {
                return false;
            }
            LogHelper.d(mTag, "[onScaleEnd]");
            if (mZoomConfig != null) {
                mZoomViewCtrl.resetView();
                mZoomConfig.onScaleStatus(false);
                mPreviousSpan = 0;
                mLastDistanceRatio = 0;
                //add by huangfei for zoom switch start
                if(mAppUi.isZoomSwitchSupportCameraId()){
                    float basicZoomRatio = mAppUi.getBasicZoomRatio();
                    if(basicZoomRatio!=0.0f){
                        mAppUi.setZoomSwitchPreviousSpan(0, 0);
                    }
                }
                //add by huangfei for zoom switch end
            }
            return true;
        }

        @Override
        public boolean onLongPress(float x, float y) {
            return false;
        }

        private double calculateDistanceRatio(ScaleGestureDetector scaleGestureDetector) {
            float currentSpan = scaleGestureDetector.getCurrentSpan();
            double distanceRatio = (currentSpan - mPreviousSpan) / mScreenDistance;
            LogHelper.d(mTag, "[calculateDistanceRatio] distanceRatio = " + distanceRatio);
            return distanceRatio;
        }
    }

    private void requestZoom() {
        if (mModeHandler == null) {
            return;
        }
        mModeHandler.post(new Runnable() {
            @Override
            public void run() {
                mSettingChangeRequester.sendSettingChangeRequest();
            }
        });
    }

    // [Add for bit true test] Receive keycode and do zoom in/out @{
    private class ZoomKeyEventListener implements IApp.KeyEventListener {
        @Override
        public boolean onKeyDown(int keyCode, KeyEvent event) {

            //*/ hct.huangfei, 20201210.add volume key function.
            if(isVolumeZoom() && ((keyCode == KeyEvent.KEYCODE_VOLUME_UP 
                        || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN))){
                return true;
            }
            //*/

            if ((keyCode != CameraUtil.KEYCODE_ZOOM_IN
                    && keyCode != CameraUtil.KEYCODE_ZOOM_OUT)
                    || !CameraUtil.isSpecialKeyCodeEnabled()) {
                return false;
            }
            if (IZoomConfig.ZOOM_OFF.equals(getValue())) {
                return false;
            }
            if (mZoomConfig != null) {
                mZoomViewCtrl.clearInvalidView();
                mZoomConfig.onScaleStatus(true);
            }
            return true;
        }

        @Override
        public boolean onKeyUp(int keyCode, KeyEvent event) {

            //*/ hct.huangfei, 20201210.add volume key function.
            if(isVolumeZoom() && keyCode == KeyEvent.KEYCODE_VOLUME_UP){
                if(mZoomConfig != null){
                    float zoomValue = mZoomConfig.getCurZoomRatio()+0.5f;
                    if(mZoomConfig.isOutZoomRange(zoomValue)){
                        return true;
                    }
                    mZoomConfig.onScale(zoomValue);
                    mZoomViewCtrl.showView(zoomValue+"");
                    requestZoom();
                    return true;
                }
            }
            if(isVolumeZoom() && keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
                if(mZoomConfig != null){
                    float zoomValue = mZoomConfig.getCurZoomRatio()-0.5f;
                    if(mZoomConfig.isOutZoomRange(zoomValue)){
                        return true;
                    }
                    mZoomConfig.onScale(zoomValue);
                    mZoomViewCtrl.showView(zoomValue+"");
                    requestZoom();
                    return true;
                }
            }
            //*/

            if (!CameraUtil.isSpecialKeyCodeEnabled()) {
                return false;
            }
            if (keyCode != CameraUtil.KEYCODE_ZOOM_IN
                    && keyCode != CameraUtil.KEYCODE_ZOOM_OUT) {
                return false;
            }
            if (IZoomConfig.ZOOM_OFF.equals(getValue())) {
                LogHelper.w(mTag, "onKeyUp keyCode zoom is OFF");
                return false;
            }

            if (keyCode == CameraUtil.KEYCODE_ZOOM_IN) {
                mMainHandler.obtainMessage(MSG_ZOOM_IN, getTargetRatioMsg(true)).sendToTarget();
            } else if (keyCode == CameraUtil.KEYCODE_ZOOM_OUT) {
                mMainHandler.obtainMessage(MSG_ZOOM_OUT, getTargetRatioMsg(false)).sendToTarget();
            }
            return true;
        }
    }

    private class MainHandler extends Handler {
        public MainHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_ZOOM_IN:
                    if (mZoomConfig == null) {
                        return;
                    }
                    String targetZoomInRatioMsg = (String) msg.obj;
                    if (mCurrentRatioMsg != null
                            && mCurrentRatioMsg.startsWith(targetZoomInRatioMsg)) {
                        mLastDistanceRatio = 0.0f;
                        mZoomViewCtrl.resetView();
                        mZoomConfig.onScaleStatus(false);
                        mMainHandler.removeMessages(MSG_ZOOM_IN);
                        LogHelper.d(mTag,"[handleMessage] zoom in, mCurrentRatioMsg = "
                                + mCurrentRatioMsg + ", done");
                    } else {
                        mLastDistanceRatio += DISTANCE_RATIO_STEP;
                        mZoomConfig.onScalePerformed(mLastDistanceRatio);
                        requestZoom();
                        Message message = mMainHandler.obtainMessage(MSG_ZOOM_IN,
                                targetZoomInRatioMsg);
                        mMainHandler.sendMessageDelayed(message, MSG_DELAY);
                    }
                    break;
                case MSG_ZOOM_OUT:
                    if (mZoomConfig == null) {
                        return;
                    }
                    String targetZoomOutRatioMsg = (String) msg.obj;
                    if (targetZoomOutRatioMsg.equals(mCurrentRatioMsg)) {
                        mLastDistanceRatio = 0.0f;
                        mZoomViewCtrl.resetView();
                        mZoomConfig.onScaleStatus(false);
                        mMainHandler.removeMessages(MSG_ZOOM_OUT);
                        LogHelper.d(mTag, "[handleMessage] zoom out, mCurrentRatioMsg = "
                                + mCurrentRatioMsg + ", done");
                    } else {
                        mLastDistanceRatio -= DISTANCE_RATIO_STEP;
                        mZoomConfig.onScalePerformed(mLastDistanceRatio);
                        requestZoom();
                        Message message = mMainHandler.obtainMessage(MSG_ZOOM_OUT,
                                targetZoomOutRatioMsg);
                        mMainHandler.sendMessageDelayed(message, MSG_DELAY);
                    }
                    break;
            }
        }
    }

    private String getTargetRatioMsg(boolean isZoomIn) {
        String[] targetRatio = isZoomIn ? ZOOM_IN_TARGET_RATIO : ZOOM_OUT_Target_RATIO;
        String result;
        if (mCurrentRatioMsg == null) {
            result = targetRatio[RATIO_INDEX_NULL];
        } else if (mCurrentRatioMsg.equals("")) {
            result = targetRatio[RATIO_INDEX_EMPTY];
        } else if (mCurrentRatioMsg.startsWith("x1.")) {
            result = targetRatio[RATIO_INDEX_X10];
        } else if (mCurrentRatioMsg.startsWith("x2.")) {
            result = targetRatio[RATIO_INDEX_X20];
        } else if (mCurrentRatioMsg.startsWith("x3.")) {
            result = targetRatio[RATIO_INDEX_X30];
        } else if (mCurrentRatioMsg.startsWith("x4.")) {
            result = targetRatio[RATIO_INDEX_X40];
        } else {
            result = "x1.0";
        }
        LogHelper.d(mTag, "[getTargetRatioMsg] isZoomIn = " + isZoomIn
                + ", mCurrentRatioMsg = " + mCurrentRatioMsg + ", return " + result);
        return result;
    }
    // @}

    /*@Override
    public void onModeOpened(String modeKey, ICameraMode.ModeType modeType) {
        super.onModeOpened(modeKey, modeType);
        mCurrentMode = modeKey;
    }*/

    //*/ hct.huangfei, 20201030. add customize zoom.
    public boolean isZoomViewCustomizeSupport(){
        return CameraUtil.isZoomViewCustomizeSupport(mActivity);
    }

    @Override
    public void onZoomSliderReady(String ratio,float direction) {
         if (mZoomConfig != null){
             LogHelper.d(mTag, "ratio = " + ratio);

            //modify by huangfei for zoom switch start
            //mZoomConfig.onScale(Float.parseFloat(ratio));
            mOverrideValue = IZoomConfig.ZOOM_ON;
            float zoomRatio = Float.parseFloat(ratio);

            mZoomConfig.onScale(calculateWideAngleRatio(zoomRatio));
            //add by huangfei for zoom switch start
            if(mAppUi.isZoomSwitchSupportCameraId() && mAppUi.isZoomSwitchMode()){
                float basicZoomRatio = mAppUi.getBasicZoomRatio();
                LogHelper.i(mTag,"getCameraSwitchByZoom 2 basicZoomRatio = " + basicZoomRatio + " zoomRatio = " + zoomRatio + " direction = " + direction);
                mZoomConfig.getCameraSwitchByZoom(0,basicZoomRatio,zoomRatio,direction);
            }
            //modify by huangfei for zoom switch end
            requestZoom();
        }
    }

    @Override
    public void onSlidingArcViewHide(float ratio) {
        mOverrideValue = IZoomConfig.ZOOM_OFF;
        if(mAppUi.isZoomSwitchSupportCameraId() && mZoomConfig!=null){
            LogHelper.i(mTag,"getCameraSwitchByZoom 3");
            mZoomConfig.getCameraSwitchByZoom(-1.0,0,0,0);
        }
    }

    //*/

    //*/ hct.huangfei, 20201210.add volume key function.
    private boolean isVolumeZoom(){
        if(Config.isVolumeKeySupport(mActivity)){
            String voluemValue = CameraUtil.getVolumeKeyValue(mDataStore,mActivity);
            if("zoom".equals(voluemValue)){
                return true;
            }
        }
        return false;
    }
    //*/

    //add by huangfei for zoom switch start
    private float calculateWideAngleRatio(float ratio) {
        float realRatio = 1.0f;

        if(ratio>=1.0){
            realRatio = ratio;
        }else if(ratio>=0.89){
            realRatio = 1.6f;
        }else if(ratio>=0.79){
            realRatio = 1.4f;
        }else if(ratio>=0.69){
            realRatio = 1.2f;
        }else if(ratio>=0.59){
            realRatio = 1.0f;
        }
        LogHelper.d(mTag,"calculateWideAngleRatio ratio = " + ratio + " realRatio = " + realRatio);
        return realRatio;
    }

    private IZoomConfig.OnZoomSwitchListener mZoomSwitchListener = new IZoomConfig.OnZoomSwitchListener() {
        @Override
        public void onZoomSwitchByDecrease(String cameraId,float basicZoomRatio) {
            if(!isZoomSwitchSupportByDecrease(mAppUi.getCameraId(),cameraId)){
                return;
            }
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mAppUi.setZoomSwitchPreviousSpan(basicZoomRatio,mZoomSwitchSpan);
                    mApp.notifyCameraSelected(cameraId);
                }
            });
        }

        @Override
        public void onZoomSwitchByIncrease(String cameraId,float basicZoomRatio) {
            if(!isZoomSwitchSupportByIncrease(mAppUi.getCameraId(),cameraId)){
                return;
            }
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mAppUi.setZoomSwitchPreviousSpan(basicZoomRatio,mZoomSwitchSpan);
                    mApp.notifyCameraSelected(cameraId);
                }
            });
        }
    };

    public boolean isZoomSwitchSupportByDecrease(String currentId,String newId){
        if("0".equals(currentId)&& newId.equals(Config.getWideAngleId())){
            return true;
        }
        return false;
    }

    public boolean isZoomSwitchSupportByIncrease(String currentId,String newId){
        if(Config.getWideAngleId().equals(currentId)&& newId.equals("0")){
            return true;
        }
        return false;
    }

    //add by huangfei for zoom switch end

    private IAppUi.ZoomViewListener mZoomViewListener = new IAppUi.ZoomViewListener() {
        @Override
        public void onConfigZoomUIVisibility(int visibility) {
            LogHelper.d(mTag, "applyZoomViewVisibilityImmediately onConfigZoomUIVisibility visibility ="+visibility);
            //bv wuyonglin add for aiworks facebeauty and bokeh 20200720 start
            //bv wuyonglin modify for add PanoramaMode should not show zoom view 20200103 start
            if (mZoomViewCtrl != null && !mModeKey.equals(mCurrentModeKey) && !mPiBrokehModeKey.equals(mCurrentModeKey) && !mAiWorksBrokehModeKey.equals(mCurrentModeKey)
            && !mAiWorksFaceBeautyModeKey.equals(mCurrentModeKey)&& !mAiWorksBrokehColorModeKey.equals(mCurrentModeKey) && !mDefaultModeKey.equals(mCurrentModeKey)
            && !mVideoModeKey.equals(mCurrentModeKey)) {
                //bv wuyonglin modify for bokeh mode should not show zoom view 20200309
                //bv wuyonglin modify for add PanoramaMode should not show zoom view 20200103 end
                //bv wuyonglin add for aiworks facebeauty and bokeh 20200720 end
                mApp.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        LogHelper.d(mTag,"applyZoomViewVisibilityImmediately onConfigZoomUIVisibility  getZoomView = "
                            + mZoomViewCtrl.getZoomView());
                        if (mZoomViewCtrl.getZoomView() !=null) {
                            //mZoomViewCtrl.getZoomView().setVisibility(visibility);
                            mZoomViewCtrl.getZoomView().setVisibility(View.INVISIBLE);
                            //bv liangchangwei modify
                        }
                    }
                });
            }
        }
    };

    private class OnOrientationChangeListenerImpl implements IApp.OnOrientationChangeListener {

        @Override
        public void onOrientationChanged(int orientation) {
            LogHelper.d(mTag, "onOrientationChanged orientation ="+orientation);
            if (mZoomViewCtrl != null && mZoomViewCtrl.getZoomView() != null) {
                CameraUtil.rotateRotateLayoutChildView(mApp.getActivity(), mZoomViewCtrl.getZoomView(),
                        orientation, true);
            }
        }
    }
}
