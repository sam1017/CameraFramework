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

package com.mediatek.camera.feature.setting.watermark;

import com.mediatek.camera.common.ICameraContext;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.relation.Relation;
import com.mediatek.camera.common.setting.ISettingManager.SettingController;
import com.mediatek.camera.common.setting.SettingBase;
import com.mediatek.camera.R;

import java.util.ArrayList;
import java.util.List;
import com.mediatek.camera.common.utils.CameraUtil;
import javax.annotation.Nonnull;
import com.mediatek.camera.CameraActivity;
//bv wuyonglin add for setting ui 20200923 start
import com.mediatek.camera.common.IAppUi;
//bv wuyonglin add for setting ui 20200923 end

/**
 * This class is for WaterMark feature interacted with others.
 */

public class WaterMark extends SettingBase {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(WaterMark.class.getSimpleName());
    private static final String KEY_WATERMARK = "key_water_mark";
    private static final String KEY_MIRROR = "key_camera_mirror";
    private static final String MIRROR_OFF = "0";
    private static final String MIRROR_ON = "1";
    private String defaultMirrorValue = MIRROR_OFF;

    //addb huangfei by watermark enable start
    private static final String KEY_WATERMARK_ENABLE = "key_water_mark_enable";
    //addb huangfei by watermark enable end

    private static final String WATERMARK_OFF = "off";
    private static final String WATERMARK_ON = "on";
    private static final String KEY_PICTURE_SIZE = "key_picture_size";
    private boolean mIsSupported = false;
    private WaterMarkSettingView mSettingView;
    private ISettingChangeRequester mSettingChangeRequester;
    private ICameraContext mICameraContext;
    private CameraActivity mCameraActivity;
    //bv wuyonglin add for setting ui 20200923 start
    private String defaultValue = "off";
    //bv wuyonglin add for setting ui 20200923 end

    @Override
    public void init(IApp app, ICameraContext cameraContext, SettingController settingController) {
        super.init(app, cameraContext, settingController);
        mSettingView = new WaterMarkSettingView();
        mSettingView.setWaterMarkViewListener(mWaterMarkViewListener);
        mICameraContext = cameraContext;
        mCameraActivity = (CameraActivity)app.getActivity();
        defaultMirrorValue = mCameraActivity.getResources().getString(R.string.pref_camera_mirror_default);
        //bv wuyonglin add for setting ui 20200923 start
        defaultValue = mCameraActivity.getResources().getString(R.string.config_watermark_default_value);
        mAppUi.setRestoreSettingListener(mRestoreSettingListener);
        //bv wuyonglin add for setting ui 20200923 end
    }

    @Override
    public void unInit() {
        //bv wuyonglin add for setting ui 20200923 start
        mAppUi.removeRestoreSettingListener(mRestoreSettingListener);
        //bv wuyonglin add for setting ui 20200923 end
    }

    @Override
    public void addViewEntry() {
        mAppUi.addSettingView(mSettingView);
    }

    @Override
    public void removeViewEntry() {
        mAppUi.removeSettingView(mSettingView);
    }

    @Override
    public void refreshViewEntry() {
        if (mSettingView != null) {
            mSettingView.setChecked(WATERMARK_ON.equals(getValue()));
            mSettingView.setEnabled(getEntryValues().size() > 1);
                
            //add by huangfei by watermark enable start
            if(getEntryValues().size() > 1){
                mDataStore.setValue(KEY_WATERMARK_ENABLE,"1", mDataStore.getGlobalScope(), false);
            }else{
                mDataStore.setValue(KEY_WATERMARK_ENABLE,"0", mDataStore.getGlobalScope(), false);
            }
            //add by huangfei by watermark enable end
        }
    }

    @Override
    public void postRestrictionAfterInitialized() {
        Relation relation = WaterMarkRestriction.getRestrictionGroup()
                .getRelation(getValue(), false);
        if (relation != null) {
            mSettingController.postRestriction(relation);
        }
    }

    @Override
    public SettingType getSettingType() {
        return SettingType.PHOTO_AND_VIDEO;
    }

    @Override
    public String getKey() {
        return KEY_WATERMARK;
    }

    @Override
    public ICaptureRequestConfigure getCaptureRequestConfigure() {
        WaterMarkCaptureRequestConfig captureRequestConfig;
        if (mSettingChangeRequester == null) {
            captureRequestConfig = new WaterMarkCaptureRequestConfig(this, mSettingDevice2Requester,
                mCameraActivity.getApplicationContext()); //add by wangshuoshuo for set location value by default
            mSettingChangeRequester = captureRequestConfig;
        }
        return (WaterMarkCaptureRequestConfig) mSettingChangeRequester;
    }

    private WaterMarkSettingView.OnWaterMarkViewListener mWaterMarkViewListener
            = new WaterMarkSettingView.OnWaterMarkViewListener() {
        @Override
        public void onItemViewClick(boolean isOn) {
			
            LogHelper.d(TAG, "[onItemViewClick], isOn:" + isOn);
            String value = isOn ? WATERMARK_ON : WATERMARK_OFF;
            setValue(value);
            mDataStore.setValue(getKey(), value, mDataStore.getGlobalScope(), false);
            Relation relation = WaterMarkRestriction.getRestrictionGroup().getRelation(value, true);
            mSettingController.postRestriction(relation);
            mSettingController.refreshViewEntry();
            mAppUi.refreshSettingView();
            mSettingChangeRequester.sendSettingChangeRequest();
        }

        @Override
        public boolean onCachedValue() {
            return WATERMARK_ON.equals(
                    mDataStore.getValue(getKey(), WATERMARK_OFF, mDataStore.getGlobalScope()));
        }
    };

    public void updateSupportedValues() {
        List<String> supported = new ArrayList<>();
        supported.add(WATERMARK_ON);
        supported.add(WATERMARK_OFF);
        setSupportedPlatformValues(supported);
        setSupportedEntryValues(supported);
        setEntryValues(supported);

        //bv wuyonglin delete for setting ui 20200923 start
        //String defaultValue = mApp.getActivity().getResources().getString(R.string.config_watermark_default_value);
        //bv wuyonglin delete for setting ui 20200923 end
        String value = mDataStore.getValue(getKey(), defaultValue, mDataStore.getGlobalScope());
        setValue(value);
        mDataStore.setValue(getKey(), value, mDataStore.getGlobalScope(), false);
        Relation relation = WaterMarkRestriction.getRestrictionGroup().getRelation(KEY_WATERMARK, true);
        mSettingController.postRestriction(relation);
    }
    
    public void updateIsSupported(boolean isSupported) {
        mIsSupported = isSupported;
        LogHelper.d(TAG, "[updateIsSupported] mIsSupported = " + mIsSupported);
    }

    public int getJpegRotationFromDeviceSpec(){
        int rotation = CameraUtil.getJpegRotationFromDeviceSpec(Integer.parseInt(getCameraId()),
                mApp.getGSensorOrientation(), mApp.getActivity());
        return rotation;
    }

    public String getCameraId(){
        return mAppUi.getCameraId();
    }

    public String getPictureSize(){
        return mSettingController.queryValue(KEY_PICTURE_SIZE);
    }

    public int getMirrorValue(){
        String value = mDataStore.getValue(KEY_MIRROR, defaultMirrorValue, getStoreScope());
        int mirror = Integer.parseInt(value);
        return mirror;
    }

    //bv wuyonglin add for AiWorksBokeh water logo 20200827 start
    public boolean isAiWorksBokehMode(){
        if("AiWorksBokeh".equals(mAppUi.getCurrentMode()) || "AiworksFaceBeauty".equals(mAppUi.getCurrentMode())){
            return true;
        }else{
            return false;
        }
    }
    //bv wuyonglin add for AiWorksBokeh water logo 20200827 end

    //bv wuyonglin delete for setting ui 20200923 start
    private IAppUi.RestoreSettingListener mRestoreSettingListener = new IAppUi.RestoreSettingListener() {
        @Override
        public void restoreSettingtoValue() {
            setValue(defaultValue);
            mDataStore.setValue(getKey(), defaultValue, mDataStore.getGlobalScope(), false);
            Relation relation = WaterMarkRestriction.getRestrictionGroup().getRelation(defaultValue, true);
            mSettingController.postRestriction(relation);
            mSettingController.refreshViewEntry();
            mAppUi.refreshSettingView();
            mSettingChangeRequester.sendSettingChangeRequest();
        }
    };
    //bv wuyonglin delete for setting ui 20200923 end
}
