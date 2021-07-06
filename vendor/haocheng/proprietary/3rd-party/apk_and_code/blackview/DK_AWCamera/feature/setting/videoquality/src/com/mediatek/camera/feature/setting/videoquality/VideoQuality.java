/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 * the prior written permission of MediaTek inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of MediaTek Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 *
 * MediaTek Inc. (C) 2016. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 * SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 * MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek
 * Software") have been modified by MediaTek Inc. All revisions are subject to
 * any receiver's applicable license agreements with MediaTek Inc.
 */
package com.mediatek.camera.feature.setting.videoquality;

import android.app.Activity;
import android.content.Intent;
import android.media.CamcorderProfile;
import android.provider.MediaStore;

import com.mediatek.camera.common.ICameraContext;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.relation.StatusMonitor;
import com.mediatek.camera.common.setting.ISettingManager.SettingController;
import com.mediatek.camera.common.setting.SettingBase;
//bv wuyonglin add for videoquality quickSwitch 20191225 start
import javax.annotation.Nonnull;
import java.util.List;
//bv wuyonglin add for videoquality quickSwitch 20191225 end
import com.mediatek.camera.common.IAppUi;//bv wuyonglin add for startVideoRecord not show videoquality quickSwitch 20191225
//bv wuyonglin add for 60fps 20201124 start
import com.mediatek.camera.feature.setting.fps60.Fps60Restriction;
//bv wuyonglin add for 60fps 20201124 end

//add by huangfei for zoom switch start
import com.mediatek.camera.Config;
//add by huangfei for zoom switch end

/**
 * VideoQuality setting item.
 */
public class VideoQuality extends SettingBase implements
VideoQualitySettingView.OnValueChangeListener {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(VideoQuality.class.getSimpleName());
    private static final String KEY_VIDEO_QUALITY = "key_video_quality";
    private ISettingChangeRequester mSettingChangeRequester;
    private VideoQualitySettingView mSettingView;
    private StatusMonitor.StatusResponder mViewStatusResponder;
    private static final String KEY_VIDEO_QUALITY_STATUS = "key_video_quality_status";
    private VideoQualityViewController mVideoQualityViewController;	//bv wuyonglin add for videoquality quickSwitch 20191225

	//add by huangfei for zoom switch start
    private static final String KEY_VIDEO_RATIO = "key_video_ratio";
	//add by huangfei for zoom switch end
    @Override
    public void init(IApp app,
                     ICameraContext cameraContext,
                     SettingController settingController) {
        super.init(app, cameraContext, settingController);
        mSettingView = new VideoQualitySettingView(getKey(), this);
        mSettingView.setOnValueChangeListener(this);
        mViewStatusResponder = mStatusMonitor.getStatusResponder(KEY_VIDEO_QUALITY_STATUS);
        //bv wuyonglin add for videoquality quickSwitch 20191225 start
        if (mVideoQualityViewController == null) {
            mVideoQualityViewController = new VideoQualityViewController(this, app);
        }
        //bv wuyonglin add for videoquality quickSwitch 20191225 end
        mAppUi.setVideoQualitySwitcherListener(mVideoQualitySwitcherListener);	//bv wuyonglin add for startVideoRecord not show videoquality quickSwitch 20191225
    }

    @Override
    public void unInit() {

    }

    @Override
    public void addViewEntry() {
        if (!isCaptureByIntent()) {
            mAppUi.addSettingView(mSettingView);
            //bv wuyonglin add for videoquality quickSwitch 20191225 start
            LogHelper.d(TAG, "[addViewEntry] getvalue="+getValue());
            mVideoQualityViewController.addQuickSwitchIcon();
            mVideoQualityViewController.showQuickSwitchIcon(getEntryValues().size() > 1);
            //bv wuyonglin add for videoquality quickSwitch 20191225 end
        }
    }

    @Override
    public void removeViewEntry() {
        mAppUi.removeSettingView(mSettingView);
        //bv wuyonglin add for videoquality quickSwitch 20191225 start
        LogHelper.d(TAG, "[removeViewEntry] getvalue="+getValue());
        mVideoQualityViewController.removeQuickSwitchIcon();
        //bv wuyonglin add for videoquality quickSwitch 20191225 end
    }

    @Override
    public void refreshViewEntry() {
        mSettingView.setValue(getValue());
        mSettingView.setEntryValues(getEntryValues());
        mSettingView.setEnabled(getEntryValues().size() > 1);
        //bv wuyonglin add for videoquality quickSwitch 20191225 start
        int size = getEntryValues().size();
            LogHelper.d(TAG, "[refreshViewEntry], size ="+size+" mAppUi.getContentViewValue() ="+mAppUi.getContentViewValue());
        if (size <= 1 || mAppUi.getContentViewValue()) {
            mVideoQualityViewController.showQuickSwitchIcon(false);
        } else {
            mVideoQualityViewController.showQuickSwitchIcon(true);
        }
        //bv wuyonglin add for videoquality quickSwitch 20191225 end
    }

    @Override
    public void postRestrictionAfterInitialized() {
        checkAndPostRestriction(getValue());
        mSettingController.refreshViewEntry();
        mAppUi.refreshSettingView();
    }

    @Override
    public SettingType getSettingType() {
        return SettingType.VIDEO;
    }

    @Override
    public String getKey() {
        return KEY_VIDEO_QUALITY;
    }

    @Override
    public ICaptureRequestConfigure getCaptureRequestConfigure() {
        VideoQualityCaptureRequestConfig captureRequestConfig;
        if (mSettingChangeRequester == null) {
            captureRequestConfig =
                    new VideoQualityCaptureRequestConfig(this, mSettingDevice2Requester,
                            mActivity.getApplicationContext());
            mSettingChangeRequester = captureRequestConfig;
        }
        return (VideoQualityCaptureRequestConfig) mSettingChangeRequester;
    }

    //bv wuyonglin add for videoquality quickSwitch 20191225 start
    @Override
    public void onModeClosed(String modeKey) {
        LogHelper.d(TAG, "onModeClosed modeKey :" + modeKey);
        mVideoQualityViewController.hideVideoQualityChoiceView();
        super.onModeClosed(modeKey);
    }
    //bv wuyonglin add for videoquality quickSwitch 20191225 end

    public String getCameraId() {
        return mSettingController.getCameraId();
    }

    public IApp getApp() {
        return mApp;
    }
    /**
     * Invoked after setting's all values are initialized.
     */
    public void onValueInitialized() {
        mSettingView.setValue(getValue());
        mSettingView.setEntryValues(getEntryValues());
        //bv wuyonglin add for videoquality quickSwitch 20191225 start
        String value = mDataStore.getValue(KEY_VIDEO_QUALITY, getDefaultQuality(), getStoreScope());
        LogHelper.d(TAG, "[onValueInitialized] value="+value);
	    mVideoQualityViewController.initQuickSwitchIcon();
        //setValue(value);
        //bv wuyonglin add for videoquality quickSwitch 20191225 end
    }
    /**
     * Callback when video quality value changed.
     * @param value The changed video quality, such as "1920x1080".
     */
    @Override
    public void onValueChanged(String value) {
        LogHelper.d(TAG, "[onValueChanged], value:" + value);
        if (!getValue().equals(value)) {
            //bv wuyonglin add for 60fps 20201124 start
            if ("60".equals(value)) {
                mDataStore.setValue("key_fps60", "on", getStoreScope(), false);
                    mSettingController.postRestriction(
                                Fps60Restriction.getFps60Restriction().getRelation("on",true));
            } else {
                mDataStore.setValue("key_fps60", "off", getStoreScope(), false);
                    mSettingController.postRestriction(
                                Fps60Restriction.getFps60Restriction().getRelation("off", true));
            }
            //bv wuyonglin add for 60fps 20201124 end
            setValue(value);
            mDataStore.setValue(getKey(), value, getStoreScope(), false);

            checkAndPostRestriction(value);
            //add by huangfei for zoom switch start
            if(mAppUi.isZoomSwitchSupportCameraId()){
                mDataStore.setValue(KEY_VIDEO_RATIO, value, mDataStore.getGlobalScope(), false);
            }
            //add by huangfei for zoom switch end
            //bv wuyonglin add for bug2771 20201031 start
            if (Integer.toString(CamcorderProfile.QUALITY_2160P).equals(value) || "60".equals(value)) {
	        mAppUi.updateIs4KVideo(true);
            } else {
	        mAppUi.updateIs4KVideo(false);
            }
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mViewStatusResponder.statusChanged(KEY_VIDEO_QUALITY_STATUS, value);
                    mSettingChangeRequester.sendSettingChangeRequest();
                }
            });
            mSettingController.refreshViewEntry();
            mAppUi.refreshSettingView();
        }
    }

    /**
     * update set value.
     * @param defaultValue the default value
     */
    public void updateValue(String defaultValue) {
        String value = parseIntent();
        if (value == null) {
            value = mDataStore.getValue(getKey(), defaultValue, getStoreScope());
        }
         //bv wuyonglin delete for bug1914 20200819 start
		//add by huangfei for zoom switch start
        if(mAppUi.isZoomSwitchSupportCameraId()){
			value = mDataStore.getValue(KEY_VIDEO_RATIO, defaultValue,  mDataStore.getGlobalScope());
        }
        //add by huangfei for zoom switch end
        //bv wuyonglin delete for bug1914 20200819 end
        //bv wuyonglin add for bug2771 20201031 start
        if (Integer.parseInt(getCameraId()) == 2 && (value.equals(Integer.toString(CamcorderProfile.QUALITY_2160P)) || value.equals("60"))) {
            value = Integer.toString(CamcorderProfile.QUALITY_1080P);
            setValue(value);
            mDataStore.setValue(getKey(), value, getStoreScope(), false);

            if(mAppUi.isZoomSwitchSupportCameraId()){
                mDataStore.setValue(KEY_VIDEO_RATIO, value, mDataStore.getGlobalScope(), false);
            }
            mAppUi.updateIs4KVideo(false);
        } else {
            setValue(value);
        }
        //bv wuyonglin add for bug2771 20201031 end
        checkAndPostRestriction(value);
        //mSettingChangeRequester.sendSettingChangeRequest();
    }

    private void checkAndPostRestriction(String qualityValue){
        //bv wuyonglin add for 60fps 20201124 start
        if ("60".equals(qualityValue)) {
            qualityValue = Integer.toString(CamcorderProfile.QUALITY_1080P);
        }
        //bv wuyonglin add for 60fps 20201124 end
        mSettingController.postRestriction(
                VideoQualityRestriction.getVideoQualityRelationGroup().getRelation(qualityValue, true));
        VideoQualityCaptureRequestConfig captureRequestConfig = (VideoQualityCaptureRequestConfig)getCaptureRequestConfigure();
        if(!captureRequestConfig.isFPS60Support(qualityValue)){
            mSettingController.postRestriction(
                    VideoQualityRestriction.getOffHFPSrelationGroup(qualityValue).getRelation(qualityValue, true));
        }else {
            mSettingController.postRestriction(
                    VideoQualityRestriction.getOnHFPSrelationGroup(getValue()).getRelation(qualityValue, true));
        }
    }
    private boolean isCaptureByIntent() {
        boolean isCaptureIntent = false;
        Activity activity = mApp.getActivity();
        Intent intent = activity.getIntent();
        String action = intent.getAction();
        if (MediaStore.ACTION_VIDEO_CAPTURE.equals(action)) {
            isCaptureIntent = true;
        }
        return isCaptureIntent;
    }

    private String parseIntent() {
        String quality = null;
        Activity activity = mApp.getActivity();
        Intent intent = activity.getIntent();
        String action = intent.getAction();
        if (MediaStore.ACTION_VIDEO_CAPTURE.equals(action)) {
            boolean userLimitQuality = intent.hasExtra(MediaStore.EXTRA_VIDEO_QUALITY);
            if (userLimitQuality) {
                int extraVideoQuality = intent.getIntExtra(
                        MediaStore.EXTRA_VIDEO_QUALITY, 0);
                if (extraVideoQuality > 0 &&
                        CamcorderProfile.hasProfile(
                                Integer.parseInt(getCameraId()), extraVideoQuality))  {
                    quality = Integer.toString(extraVideoQuality);
                } else {
                    quality = Integer.toString(CamcorderProfile.QUALITY_LOW);
                }
            } else {
                quality = Integer.toString(CamcorderProfile.QUALITY_LOW);
            }
        }
        return quality;
    }
    protected SettingController getSettingController(){
        return mSettingController;
    }

    //bv wuyonglin add for videoquality quickSwitch 20191225 start
    public void onVideoQualityValueChanged(String value) {
	//bv wuyonglin modify for setting add video quality option 20200219 start
        LogHelper.d(TAG, "[onVideoQualityValueChanged] value = " + value+" getValue() ="+getValue());
        if (!getValue().equals(value)) {
            LogHelper.d(TAG, "[onVideoQualityValueChanged] value = " + value+" getStoreScope ="+getStoreScope()+" getKey() ="+getKey());
            //bv wuyonglin add for 60fps 20201124 start
            if ("60".equals(value)) {
                mDataStore.setValue("key_fps60", "on", getStoreScope(), false);
                    mSettingController.postRestriction(
                                Fps60Restriction.getFps60Restriction().getRelation("on", true));
            } else {
                mDataStore.setValue("key_fps60", "off", getStoreScope(), false);
                    mSettingController.postRestriction(
                                Fps60Restriction.getFps60Restriction().getRelation("off", true));
            }
            //bv wuyonglin add for 60fps 20201124 end
            setValue(value);
            //add by huangfei for zoom switch start
            if(mAppUi.isZoomSwitchSupportCameraId()){
                mDataStore.setValue(KEY_VIDEO_RATIO, value, mDataStore.getGlobalScope(), false);
            }
            checkAndPostRestriction(value);
            //add by huangfei for zoom switch end
            mDataStore.setValue(getKey(), value, getStoreScope(), false);
            //bv wuyonglin add for bug2771 20201031 start
            if (Integer.toString(CamcorderProfile.QUALITY_2160P).equals(value) || "60".equals(value)) {
	            mAppUi.updateIs4KVideo(true);
            } else {
	            mAppUi.updateIs4KVideo(false);
            }
            mSettingController.refreshViewEntry();
            //bv wuyonglin add for bug2771 20201031 start
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                        //updateRestriction(value);
                        mViewStatusResponder.statusChanged(KEY_VIDEO_QUALITY_STATUS, value);
                        mSettingChangeRequester.sendSettingChangeRequest();
                    }
            });
            mSettingController.refreshViewEntry();
            mAppUi.refreshSettingView();
        }
        //bv wuyonglin modify for setting add video quality option 20200219 end
    }

    private String getDefaultQuality() {
        int defaultIndex = 0;
        if (getSupportedPlatformValues().size() > 2) {
            defaultIndex = 1;
        }
        String defaultSize = getSupportedPlatformValues().get(defaultIndex);
        return defaultSize;
    }
    //bv wuyonglin add for videoquality quickSwitch 20191225 end

    //bv wuyonglin add for startVideoRecord not show videoquality quickSwitch 20191225 start
    private IAppUi.VideoQualitySwitcherListener mVideoQualitySwitcherListener = new IAppUi.VideoQualitySwitcherListener() {
        @Override
        public void onConfigVideoQualityUIVisibility(int visibility) {
        LogHelper.d(TAG, "[onConfigVideoQualityUIVisibility], mVideoQualityViewController:" + mVideoQualityViewController
                + ", visibility:" + visibility);
            if (mVideoQualityViewController != null) {
                if (getEntryValues().size() > 1) {
                    mApp.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mVideoQualityViewController.getVideoQualityEntryView() != null) {
                                mVideoQualityViewController.getVideoQualityEntryView().setVisibility(visibility);
                            }
                        }
                    });
                }
            }
        }
    };
    //bv wuyonglin add for startVideoRecord not show videoquality quickSwitch 20191225 end

    //bv wuyonglin add for 60fps 20201124 start
    public boolean isFPS60Support1080P() {
       return ((VideoQualityCaptureRequestConfig)mSettingChangeRequester).isFPS60Support(Integer.toString(CamcorderProfile.QUALITY_1080P));
    }
    //bv wuyonglin add for 60fps 20201124 end

    public String getCurrentMode(){
        return mAppUi.getCurrentMode();
    }
}
