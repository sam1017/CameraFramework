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

package com.mediatek.camera.feature.setting.location;

import com.mediatek.camera.common.ICameraContext;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.setting.ISettingManager.SettingController;
import com.mediatek.camera.common.setting.SettingBase;

import java.util.List;

import javax.annotation.Nonnull;
//add by huangfei for location abnormal start
import com.mediatek.camera.CameraActivity;
//add by huangfei for location abnormal end
//bv wuyonglin add for setting ui 20200923 start
import com.mediatek.camera.common.IAppUi;
import com.mediatek.camera.R;
//bv wuyonglin add for setting ui 20200923 end

/**
 * This class is for EIS feature interacted with others.
 */

public class Location extends SettingBase {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(Location.class.getSimpleName());
    private static final String KEY_LOCATION = "key_location";
    private static final String LOCATION_OFF = "off";
    private static final String LOCATION_ON = "on";
    private boolean mIsSupported = false;
    private ISettingChangeRequester mSettingChangeRequester;
    private LocationSettingView mSettingView;
    private ICameraContext mICameraContext;
    //add by huangfei for location abnormal start
    private CameraActivity mCameraActivity;
    //add by huangfei for location abnormal end

    @Override
    public void init(IApp app, ICameraContext cameraContext, SettingController settingController) {
        super.init(app, cameraContext, settingController);
        mSettingView = new LocationSettingView();
        mSettingView.setLocationViewListener(mLocationViewListener);
        mICameraContext = cameraContext;
        //add by huangfei for location abnormal start
        mCameraActivity = (CameraActivity)app.getActivity();
        //add by huangfei for location abnormal end

        //bv wuyonglin add for setting ui 20200923 start
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
            mSettingView.setChecked(LOCATION_ON.equals(getValue()));
            mSettingView.setEnabled(getEntryValues().size() > 1);
        }
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
        return KEY_LOCATION;
    }

    @Override
    public ICaptureRequestConfigure getCaptureRequestConfigure() {
        LocationCaptureRequestConfig captureRequestConfig;
        if (mSettingChangeRequester == null) {
            captureRequestConfig = new LocationCaptureRequestConfig(this, mSettingDevice2Requester,
                mCameraActivity.getApplicationContext()); //add by wangshuoshuo for set location value by default
            mSettingChangeRequester = captureRequestConfig;
        }
        return (LocationCaptureRequestConfig) mSettingChangeRequester;
    }

    private LocationSettingView.OnLocationViewListener mLocationViewListener
            = new LocationSettingView.OnLocationViewListener() {
        @Override
        public void onItemViewClick(boolean isOn) {
            LogHelper.d(TAG, "[onItemViewClick], isOn:" + isOn);
            String value = isOn ? LOCATION_ON : LOCATION_OFF;
            setValue(value);
            
            //add by huangfei for location start
            mICameraContext.setLocationEnable(isOn);
            //add by huangfei for location end

            //modify by huangfei for define location value global start
            //mDataStore.setValue(getKey(), value, getStoreScope(), false);
            mDataStore.setValue(getKey(), value, mDataStore.getGlobalScope(), false);
            //modify by huangfei for define location value global start

            mSettingChangeRequester.sendSettingChangeRequest();
        }

        @Override
        public boolean onCachedValue() {
            return LOCATION_ON.equals(

                    //modify by huangfei for define location value global start
                    //mDataStore.getValue(getKey(), LOCATION_OFF, getStoreScope()));
                    mDataStore.getValue(getKey(), LOCATION_OFF, mDataStore.getGlobalScope()));
                    //modify by huangfei for define location value global end

        }
    };

    /**
     * update set value.
     * @param value the default value
     */
    public void updateValue(String value) {
        //add by huangfei for location abnormal start
        mSettingView.refreshViewAfterSetPermission(mCameraActivity.getPermissionManager(),mICameraContext);
        //add by huangfei for location abnormal end

        //modify by huangfei for location start
        //setValue(mDataStore.getValue(getKey(), value, getStoreScope()));
        String mValue = mDataStore.getValue(getKey(), value, mDataStore.getGlobalScope());
        boolean toggle = LOCATION_ON.equals(mValue) ? true : false;
        mICameraContext.setLocationEnable(toggle);
        setValue(mValue);
        //modify by huangfei for location end

    }

    /**
     * update whether the settings is support.
     * @param isSupported the result
     */
    public void updateIsSupported(boolean isSupported) {
        mIsSupported = isSupported;
        LogHelper.d(TAG, "[updateIsSupported] mIsSupported = " + mIsSupported);
    }

    //bv wuyonglin add for setting ui 20200923 start
    private IAppUi.RestoreSettingListener mRestoreSettingListener = new IAppUi.RestoreSettingListener() {
        @Override
        public void restoreSettingtoValue() {
            mSettingView.refreshViewAfterSetPermission(mCameraActivity.getPermissionManager(),mICameraContext);
            //String mLocationEnable = mDataStore.getValue("key_location_permission", "off", mDataStore.getGlobalScope());
            //if ("off".equals(mLocationEnable)) {
                setValue(LOCATION_OFF);
                mICameraContext.setLocationEnable(false);
                mDataStore.setValue(getKey(), LOCATION_OFF, mDataStore.getGlobalScope(), false);
            //} else {
            //    setValue(LOCATION_ON);
            //    mICameraContext.setLocationEnable(true);
            //    mDataStore.setValue(getKey(), LOCATION_ON, mDataStore.getGlobalScope(), false);
            //}
            mSettingChangeRequester.sendSettingChangeRequest();
        }
    };
    //bv wuyonglin add for setting ui 20200923 end
}
