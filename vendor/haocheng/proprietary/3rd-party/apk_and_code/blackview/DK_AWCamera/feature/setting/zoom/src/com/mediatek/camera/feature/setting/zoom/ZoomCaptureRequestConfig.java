/*
 * Copyright Statement:
 *
 *   This software/firmware and related documentation ("MediaTek Software") are
 *   protected under relevant copyright laws. The information contained herein is
 *   confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 *   the prior written permission of MediaTek inc. and/or its licensors, any
 *   reproduction, modification, use or disclosure of MediaTek Software, and
 *   information contained herein, in whole or in part, shall be strictly
 *   prohibited.
 *
 *   MediaTek Inc. (C) 2016. All rights reserved.
 *
 *   BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 *   THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 *   RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 *   ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 *   WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 *   WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 *   NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 *   RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 *   INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 *   TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 *   RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 *   OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 *   SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 *   RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 *   STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 *   ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 *   RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 *   MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 *   CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 *   The following software/firmware and/or related documentation ("MediaTek
 *   Software") have been modified by MediaTek Inc. All revisions are subject to
 *   any receiver's applicable license agreements with MediaTek Inc.
 */

package com.mediatek.camera.feature.setting.zoom;

import android.annotation.TargetApi;
import android.graphics.Rect;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.os.Build;
import android.util.Range;
import android.view.Surface;

import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.setting.ICameraSetting;
import com.mediatek.camera.common.setting.ISettingManager;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static com.mediatek.camera.feature.setting.dualcamerazoom.DualZoomCaptureRequestConfig.getCameraCharacteristicsKey;

//*/ hct.huangfei, 20201028. add water mark.
import com.mediatek.camera.Config;
//*/
//add by huangfei for zoom switch start
import com.mediatek.camera.common.IAppUi;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import com.mediatek.camera.Config;
//add by huangfei for zoom switch end

/**
 * This is for zoom perform for api2.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class ZoomCaptureRequestConfig implements ICameraSetting.ICaptureRequestConfigure,
        IZoomConfig {
    private static final LogUtil.Tag TAG
            = new LogUtil.Tag(ZoomCaptureRequestConfig.class.getSimpleName());
    private static final String STEREO_PHOTO_MODE
            = "com.mediatek.camera.feature.mode.vsdof.photo.SdofPhotoMode";
    private static final String STEREO_VIDEO_MODE
            = "com.mediatek.camera.feature.mode.vsdof.photo.SdofPhotoMode";
    private static final float ZOOM_UNSUPPORTED_DEFAULT_VALUE = 1.0f;
    private static final float DEFAULT_VALUE = -1.0f;
    private Rect mSensorRect;
    private double mDistanceRatio;
    /* modify by bv liangchangwei for fixbug 2616 begin --*/
    private double mLastDistanceRatio;
    /* modify by bv liangchangwei for fixbug 2616 end --*/
    private OnZoomLevelUpdateListener mZoomUpdateListener;
    private boolean mIsUserInteraction;
    private float mLastZoomRatio = DEFAULT_VALUE;
    private float mBasicZoomRatio = ZOOM_UNSUPPORTED_DEFAULT_VALUE;
    private float mCurZoomRatio = ZOOM_UNSUPPORTED_DEFAULT_VALUE;
    private float mMaxZoom;
    private ISettingManager.SettingDevice2Requester mSettingDevice2Requester;
    private boolean mZoomRatioSupported = false;
    private String mCurrentMode = "";
    private static final String MULTICAM_RANGE_KEY
            = "com.mediatek.multicamfeature.multiCamZoomRange";

    //*/ hct.huangfei, 20201028. add water mark.
    private static final float[] ZOOM_VALUE = new float[]{1.0f};
    //*/

    //*/ hct.huangfei, 20201030. add customize zoom
    private float mRatio;
    private boolean isReady = false;
    private ZoomLevelSliderListener mListener;
    //*/
    private boolean isFirstInit = true;

    //add by huangfei for wide angle start
    private IAppUi mAppUi;
    private static final int[] WIDE_ANGLE_VALUE = new int[]{0,0} ;
    //add by huangfei for wide angle end
    //add by huangfei for zoom switch start
    private int mAngleOutDecreaseCount = 0;
    private int mAngleOutIncreaseCount = 0;
    private OnZoomSwitchListener mOnZoomSwitchListener;
    private boolean isZoomSwitch = false;
    float basicZoomRatio = 0.0f;
    private CopyOnWriteArrayList<OnZoomSwitchListener> mListeners;
    private float mSlideZoomRatio = 1.0f;
    //add by huangfei for zoom switch end

    /**
     * Constructor of zoom parameter config in api2.
     * @param settingDevice2Requester device requester.
     */
    public ZoomCaptureRequestConfig(ISettingManager.SettingDevice2Requester
                                            settingDevice2Requester, String modeKey, IAppUi appui) {
        mSettingDevice2Requester = settingDevice2Requester;
        mCurrentMode = modeKey;
        mAppUi = appui;
		//add by huangfei for zoom switch start
        basicZoomRatio = mAppUi.getBasicZoomRatio();
        mListeners = new CopyOnWriteArrayList<OnZoomSwitchListener>();
		//add by huangfei for zoom switch end
        //modify by bv liangchangwei for fixbug 3518
        if(isAngleCamera() && basicZoomRatio != 0){
            mBasicZoomRatio = 1.6f;
            mCurZoomRatio = 1.6f;
        }else{
            mBasicZoomRatio = ZOOM_UNSUPPORTED_DEFAULT_VALUE;
            mCurZoomRatio = ZOOM_UNSUPPORTED_DEFAULT_VALUE;
        }
        //modify by bv liangchangwei for fixbug 3518
    }

    @Override
    public void setCameraCharacteristics(CameraCharacteristics characteristics) {
        mSensorRect = characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
        if (STEREO_PHOTO_MODE.equals(mCurrentMode)
                || STEREO_VIDEO_MODE.equals(mCurrentMode)) {
            CameraCharacteristics.Key<float[]> mZoomRatiosKey =
                    getCameraCharacteristicsKey(characteristics, MULTICAM_RANGE_KEY);
            if (mZoomRatiosKey == null) {
                LogHelper.d(TAG,
                        "[setCameraCharacteristics], mZoomRatiosKey is null");
            } else {
                float[] zoomRatios = characteristics.get(mZoomRatiosKey);
                if (zoomRatios == null || zoomRatios.length < 2) {
                    LogHelper.d(TAG,
                            "[setCameraCharacteristics], mZoomRatios is illegal");
                } else {
                    mMaxZoom = zoomRatios[1];
                    LogHelper.d(TAG,
                            "[setCameraCharacteristics], mZoomRatios is " + Arrays.toString(zoomRatios));
                }
            }
        } else {
            mMaxZoom = characteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM);
        }
        LogHelper.d(TAG, "[setCameraCharacteristics] MaxZoom: " + mMaxZoom);

        //judge whether support zoom ratio or not
        Range<Float> zoomRatioRange =characteristics.get(CameraCharacteristics.CONTROL_ZOOM_RATIO_RANGE);
        mZoomRatioSupported = zoomRatioRange != null && zoomRatioRange.getUpper() >0;


    }

    @Override
    public void configCaptureRequest(CaptureRequest.Builder captureBuilder) {
        if (captureBuilder == null) {
            LogHelper.d(TAG, "[configCaptureRequest] captureBuilder is null");
            return;
        }
        //if (ZOOM_OFF.equals(mZoomUpdateListener.onGetOverrideValue())) {
            //modify by huangfei for zoom abnormal start
            //if (ZOOM_OFF.equals(mZoomUpdateListener.onGetOverrideValue())) {
        if (ZOOM_OFF.equals(mZoomUpdateListener.onGetOverrideValue()) && mAppUi.isWideAngleDistortionSupport()) {
                //modify by huangfei for zoom abnormal end
                //add by huangfei for zoom switch start
            isZoomSwitch = false;
            //add by huangfei for zoom switch end
            reset(captureBuilder);
            return;
        }

        //*/ hct.huangfei, 20201030. add customize zoom.
        //mCurZoomRatio = calculateZoomRatio(mDistanceRatio);
        if (isReady){
            LogHelper.d(TAG, "[configCaptureRequest] isReady = "+ isReady);
            mCurZoomRatio = mRatio > mMaxZoom ? mMaxZoom : mRatio;
            mDistanceRatio = 0;
            isReady = false;
        //bv wuyonglin delete for bug559 click zoom view can not from 1x to 2x 20200416 start
        /*}else if (mLastZoomRatio != DEFAULT_VALUE && !mIsUserInteraction){
            mCurZoomRatio = mLastZoomRatio;*/
        //bv wuyonglin delete for bug559 click zoom view can not from 1x to 2x 20200416 end
        }else{
            /* modify by bv liangchangwei for fixbug 2616 begin --*/
            if(mDistanceRatio == 0){
                mLastDistanceRatio = 0;
            }
            mCurZoomRatio = calculateZoomRatio(mDistanceRatio - mLastDistanceRatio);
            /* modify by bv liangchangwei for fixbug 2616 end --*/
            //add by huangfei for zoom switch start
            if(mAppUi.isZoomSwitchSupportCameraId() && isZoomSwitch && mSlideZoomRatio==0){
                mCurZoomRatio = calculateZoomRatio(mDistanceRatio);

            }
            //add by huangfei for zoom switch end

            /* remove by bv liangchangwei for fixbug start */
            /*
            if(mAppUi.isZoomSwitchSupportCameraId() &&basicZoomRatio !=0){
                mCurZoomRatio = basicZoomRatio;
                basicZoomRatio = 0.0f;
            }*/
            /* remove by bv liangchangwei for fixbug  end*/
        }

        //*/

        // apply crop region android.scaler.cropRegion
        if (mZoomRatioSupported){
            captureBuilder.set(CaptureRequest.SCALER_CROP_REGION, mSensorRect);
            captureBuilder.set(CaptureRequest.CONTROL_ZOOM_RATIO,mCurZoomRatio);
        }else{
            captureBuilder.set(CaptureRequest.SCALER_CROP_REGION, cropRegionForZoom(mCurZoomRatio));
        }
        mLastZoomRatio = mCurZoomRatio;
        /* modify by bv liangchangwei for fixbug 2616 begin --*/
        mLastDistanceRatio = mDistanceRatio;
        /* modify by bv liangchangwei for fixbug 2616 end --*/
        calculateBasicRatio();

        //*/ hct.huangfei, 20201028. add water mark.
        if(Config.hctHalWaterMarkSupport()){
            CaptureRequest.Key<float[]> mZoomValue =  new CaptureRequest.Key<float[]>("com.mediatek.control.capture.hctHalWatermakrZoom", float[].class);
            ZOOM_VALUE  [0] = mCurZoomRatio;
            captureBuilder.set(mZoomValue, ZOOM_VALUE);
        }
        //*/

	//modify by huangfei for zoom switch start
        //if (mIsUserInteraction) {
        if (mIsUserInteraction||isZoomSwitch) {
            //*/ hct.huangfei, 20201030. add customize zoom.
            if (mListener != null){
                if (mAppUi.isZoomSwitchSupport() && isAngleCamera()) {
                    mListener.onZoomLevelUpdateNotify(String.format(Locale.ENGLISH, PATTERN, getAngleRatio(mCurZoomRatio)));
                } else {
                mListener.onZoomLevelUpdateNotify(String.format(Locale.ENGLISH, PATTERN, mCurZoomRatio));
            }
            //*/
            }
        } else { //add by huangfei for zoom switch start
            //modify by bv liangchangwei for fixbug 3518
            if(mAppUi.isZoomSwitchSupportCameraId() /*&& mAppUi.getBasicZoomRatio() == mCurZoomRatio*/){
                if(isAngleCamera()){
                    mListener.onZoomLevelUpdateNotify(String.format(Locale.ENGLISH, PATTERN,getAngleRatio(mCurZoomRatio)));
                }else{
                    mListener.onZoomLevelUpdateNotify(String.format(Locale.ENGLISH, PATTERN, mCurZoomRatio));
                }
            }
        }
        //add by huangfei for zoom switch end

        if (isFirstInit) {
            mZoomUpdateListener.onZoomLevelUpdate(getSpecialPatternRatio(getPatternRatio()));
            isFirstInit =false;
        }
        LogHelper.d(TAG, "[configCaptureRequest] this: " + this + ", mCurZoomRatio = "
                + mCurZoomRatio + ", mDistanceRatio = " + mDistanceRatio
                + " mZoomRatioSupported = " + mZoomRatioSupported
                + " mSensorRect = " + mSensorRect);
    }

    @Override
    public void configSessionSurface(List<Surface> surfaces) {
    }

    @Override
    public Surface configRawSurface() {
        return null;
    }

    @Override
    public CameraCaptureSession.CaptureCallback getRepeatingCaptureCallback() {
        return null;
    }

    @Override
    public void sendSettingChangeRequest() {

        //*/ hct.huangfei, 20201030. add customize zoom.
        //if (isZoomValid()) {
        if (isZoomValid() || isReady) {
        //*/

            LogHelper.d(TAG, "[sendSettingChangeRequest]");
            mSettingDevice2Requester.createAndChangeRepeatingRequest();
        }
    }

    @Override
    public void setZoomUpdateListener(OnZoomLevelUpdateListener zoomUpdateListener) {
        mZoomUpdateListener = zoomUpdateListener;
    }

    @Override
    public void onScalePerformed(double distanceRatio) {
        mDistanceRatio = distanceRatio;
        /* modify by bv liangchangwei for fixbug 2616 begin --*/
        if(mDistanceRatio == 0){
            mLastDistanceRatio = 0;
        }
        /* modify by bv liangchangwei for fixbug 2616 end --*/
    }

    @Override
    public void onScaleStatus(boolean isBegin) {
        mIsUserInteraction = isBegin;
        //must set to 0, since if not scale, it should not zoom.
        mDistanceRatio = 0;
        calculateBasicRatio();
        //add by huangfeifor front tripleswitchhorizontal start
        if (mListener != null){
            mListener.onScaleStatus(isBegin);            
        }
        //add by huangfeifor front tripleswitchhorizontal end        

    }

    //HCT: ouyang customize zoom begin
    @Override
    public void onScale(float ratio){
        isReady = true;
        mRatio = ratio;
    }

    @Override
    public String getZoomLevel(){

        //add by huangfei for zoom switch start
        if(mAppUi.isZoomSwitchSupport() && isAngleCamera()){
            return String.format(Locale.ENGLISH, PATTERN, getAngleRatio(mLastZoomRatio));
        }
        //add by huangfei for zoom switch end

        return String.valueOf(mLastZoomRatio == DEFAULT_VALUE
                ? "1.0" : String.format(Locale.ENGLISH, PATTERN, mLastZoomRatio));
    }

    @Override
    public void setZoomSliderUpdateListener(ZoomLevelSliderListener zoomSliderUpdateListener) {
        mListener = zoomSliderUpdateListener;
    }
    //*/

    private boolean isZoomValid() {
        LogHelper.d(TAG, "[isZoomValid] mCurZoomRatio = " + mCurZoomRatio + ", zoomRatio = "
            + calculateZoomRatio(mDistanceRatio) + ", mLastZoomRatio = " + mLastZoomRatio);
        /* modify by bv liangchangwei for fixbug 2616 begin --*/
        boolean needZoom = mCurZoomRatio >= ZOOM_UNSUPPORTED_DEFAULT_VALUE
                && mCurZoomRatio <= mMaxZoom
                && calculateZoomRatio(mDistanceRatio - mLastDistanceRatio) != mLastZoomRatio;
        /* modify by bv liangchangwei for fixbug 2616 end --*/
        LogHelper.d(TAG, "[isZoomValid] needZoom = " + needZoom);
        return needZoom;
    }

    private void calculateBasicRatio() {
        //modify by bv liangchangwei for fixbug 3518
        if (mLastZoomRatio == DEFAULT_VALUE) {
            if(isAngleCamera()){
                mBasicZoomRatio = 1.6f;
            }else{
                mBasicZoomRatio = ZOOM_UNSUPPORTED_DEFAULT_VALUE;
            }
        } else {
            mBasicZoomRatio = mLastZoomRatio;
        }
        //modify by bv liangchangwei for fixbug 3518
    }

    /**
     * Calculates sensor crop region for a zoom level (zoom >= 1.0).
     * @param ratio the zoom level.
     * @return Crop region.
     */
    private Rect cropRegionForZoom(float ratio) {
        int xCenter = mSensorRect.width() / 2;
        int yCenter = mSensorRect.height() / 2;
        int xDelta = (int) (0.5f * mSensorRect.width() / ratio);
        int yDelta = (int) (0.5f * mSensorRect.height() / ratio);
        return new Rect(xCenter - xDelta, yCenter - yDelta, xCenter + xDelta, yCenter + yDelta);
    }

    private void reset(CaptureRequest.Builder captureBuilder) {
        LogHelper.d(TAG, "[reset]");
        // apply crop region
        //modify by bv liangchangwei for fixbug 3518
        if(isAngleCamera()&& mAppUi.getBasicZoomRatio()!=0){
            mLastZoomRatio = 1.6f;
            captureBuilder.set(CaptureRequest.SCALER_CROP_REGION,
                    cropRegionForZoom(mLastZoomRatio));
        }else{
            captureBuilder.set(CaptureRequest.SCALER_CROP_REGION,
                    cropRegionForZoom(ZOOM_UNSUPPORTED_DEFAULT_VALUE));
            mLastZoomRatio = ZOOM_UNSUPPORTED_DEFAULT_VALUE;
        }
        //modify by bv liangchangwei for fixbug 3518
    }

    private String getPatternRatio() {
        //add by huangfei for zoom switch start
        if(mAppUi.isZoomSwitchSupport() && isAngleCamera()){
            return "x" + String.format(Locale.ENGLISH, PATTERN, getAngleRatio(mCurZoomRatio));
        }
        //add by huangfei for zoom switch end
        return String.format(Locale.ENGLISH, PATTERN, mCurZoomRatio) ;
    }

    private float calculateZoomRatio(double distanceRatio) {
        float find = ZOOM_UNSUPPORTED_DEFAULT_VALUE; // if not find, return 1.0f.
        float maxRatio = mMaxZoom;
        float minRatio = ZOOM_UNSUPPORTED_DEFAULT_VALUE;
        float curRatio = (float) (mBasicZoomRatio + (maxRatio - minRatio) * distanceRatio);
        //add by huangfei for zoom switch start
        if(mAppUi.isZoomSwitchSupport() && isAngleCamera() && curRatio >= 1.6f){
            return 1.6f;
        }
        //add by huangfei for zoom switch end
     
        if (distanceRatio != 1.0f) {
            if (curRatio <= minRatio) {
                find = minRatio;
            } else if (curRatio >= maxRatio) {
                find = maxRatio;
            } else {
                find = curRatio;
            }
        } else {
            mBasicZoomRatio = ZOOM_UNSUPPORTED_DEFAULT_VALUE;
        }
        return find;
    }

    private String getSpecialPatternRatio(String mZoomRatio) {
        if (mZoomRatio.split("\\.")[1].equals("0")){
            mZoomRatio = mZoomRatio.split( "\\.")[0];
        } 
        return mZoomRatio + "x";
    }

    public void resetListenRation(){
        LogHelper.i(TAG,"resetListenRation ");
        mLastZoomRatio = 1.0f;
        mCurZoomRatio = 1.0f;
        if (mListener != null){
            LogHelper.i(TAG,"resetListenRation mCurZoomRatio = " + mCurZoomRatio);
            //mListener.onZoomLevelUpdateNotify(String.format(Locale.ENGLISH, PATTERN, mCurZoomRatio));
            mListener.reset();
        }
    }

    public void hideListenRation(){
        LogHelper.i(TAG,"hideListenRation ");
        if (mListener != null){
            mListener.hide();
        }
    }

	//add by huangfei for zoom switch start
    public float getCurrentRatio(){
        return mCurZoomRatio;
    }

    public void setOnZoomSwitchListener(OnZoomSwitchListener onZoomSwitchListener) {
        if (!mListeners.contains(onZoomSwitchListener)) {
            mListeners.add(onZoomSwitchListener);
        };
    }

    public void removeOnZoomSwitchListener(OnZoomSwitchListener onZoomSwitchListener){
        mListeners.remove(onZoomSwitchListener);
    }

    public boolean getCameraSwitchByZoom(double distanceRatio,float basicZoom,float ratio,float direction){
        float curRatio = 1.0f;
        float maxRatio = mMaxZoom;
        float minRatio = ZOOM_UNSUPPORTED_DEFAULT_VALUE;
        mSlideZoomRatio = ratio;
        if(basicZoomRatio!=0.0f){
            if(ratio==0){
                //modify by bv liangchangwei for fixbug 3518
                //mBasicZoomRatio = basicZoom;
                isZoomSwitch = true;
            }
        }
        if(ratio==0){
            if(mBasicZoomRatio==1.0 && distanceRatio ==-1.0){
                calculateBasicRatio();
            }
            curRatio = (float) (mBasicZoomRatio + (maxRatio - minRatio) * distanceRatio);
        }else{
            curRatio = ratio;
        }

        if(mAppUi.isVideoRecording()){
            return false;
        }

        String wideId = Config.getWideAngleId();
        if(ratio<0.9 && isMainCamera() && direction>0){
            for(OnZoomSwitchListener listener:mListeners) {
                if(listener!=null){
                    LogHelper.i(TAG,"onZoomSwitchByDecrease 1");
                    listener.onZoomSwitchByDecrease(wideId,1.6f);
                    isZoomSwitch = true;
                }
            }
            return true;
        }

        if(ratio>=0.9 && isAngleCamera()&& direction <0){
            for(OnZoomSwitchListener listener:mListeners) {
                if(listener!=null){
                    LogHelper.i(TAG,"onZoomSwitchByIncrease 1");
                    listener.onZoomSwitchByIncrease("0",1.0f);
                    isZoomSwitch = true;
                }
            }
            return true;
        }
        if (direction == 0 && ratio ==0 && distanceRatio < 0) {
            if(isMainCamera()&&mCurZoomRatio <=1.0){
                mAngleOutDecreaseCount++;
                if(mAngleOutDecreaseCount>=3){
                    for(OnZoomSwitchListener listener:mListeners) {
                        if(listener!=null){
                            LogHelper.i(TAG,"onZoomSwitchByDecrease 2");
                            listener.onZoomSwitchByDecrease(wideId,1.6f);
                            //modify by bv liangchangwei for fixbug 3518
                            mBasicZoomRatio = 1.6f;
                            mCurZoomRatio = 1.5f;
                        }
                    }
                    mAngleOutDecreaseCount = 0;
                    return true;
                }
            }
        } else if (curRatio >= maxRatio) {
            mAngleOutDecreaseCount = 0;
        } else {
            mAngleOutDecreaseCount = 0;
        }
        if (direction == 0 && ratio ==0 && distanceRatio > 0) {
            if(isAngleCamera() && mCurZoomRatio >= 1.6){
                mAngleOutIncreaseCount++;
                if(mAngleOutIncreaseCount>=3){
                    for(OnZoomSwitchListener listener:mListeners) {
                        if(listener!=null){
                        LogHelper.i(TAG,"onZoomSwitchByIncrease 2");
                            listener.onZoomSwitchByIncrease("0",1.0f);
                            //modify by bv liangchangwei for fixbug 3518
                            mBasicZoomRatio = 1.0f;
                            mCurZoomRatio = 1.0f;
                        }
                    }
                    mAngleOutIncreaseCount = 0;
                    return true;
                }
            }else{
                mAngleOutIncreaseCount = 0;
            }
        }
        //modify by bv liangchangwei for fixbug 3518
        //calculateBasicRatio();
        return false;
    }
    private boolean isMainCamera(){
        boolean isMainCamera = "0".equals(mAppUi.getCameraId()) ? true : false;
        return isMainCamera;
    }

    public boolean isAngleCamera(){
        boolean isAngleCamera = Config.getWideAngleId().equals(mAppUi.getCameraId()) ? true : false;
        return isAngleCamera;
    }

    public float getAngleRatio(float ratio){
        float wideZoomRatio = 0.6f;
        if (ratio <=1.1f){
            wideZoomRatio = 0.6f;
        }else if(ratio <=1.2f){
            wideZoomRatio = 0.7f;
        }else if(ratio <=1.4f){
            wideZoomRatio = 0.8f;
        }else if(ratio <=1.6f){
            wideZoomRatio = 0.9f;
        }
        return wideZoomRatio;
    }

	//add by huangfei for zoom switch end

    //*/ hct.huangfei, 20201210.add volume key function.
    public float getCurZoomRatio(){
        if(mCurZoomRatio<1.0f){
            mCurZoomRatio = ZOOM_UNSUPPORTED_DEFAULT_VALUE;
        }
        return mCurZoomRatio;
    }

    public boolean isOutZoomRange(float ratio){
        if(ratio<ZOOM_UNSUPPORTED_DEFAULT_VALUE || ratio>mMaxZoom){
            return true;
        }
        return false;
    }
    //*/
}
