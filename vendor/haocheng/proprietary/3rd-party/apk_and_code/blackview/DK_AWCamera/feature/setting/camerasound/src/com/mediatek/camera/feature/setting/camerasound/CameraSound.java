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
package com.mediatek.camera.feature.setting.camerasound;

import com.mediatek.camera.R;
import com.mediatek.camera.common.ICameraContext;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.mode.ICameraMode;
import com.mediatek.camera.common.setting.ISettingManager.SettingController;
import com.mediatek.camera.common.setting.SettingBase;
import com.mediatek.camera.common.utils.CameraUtil;

import android.content.SharedPreferences;
import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is for CameraSound feature interacted with others.
 */

public class CameraSound extends SettingBase {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(CameraSound.class.getSimpleName());
    private static final String KEY_CAMERASOUND = "key_camera_sound";
    private static final String CAMERASOUND_OFF = "off";
    private static final String CAMERASOUND_ON = "on";
    private CameraSoundSettingView mSettingView;
    private List<String> mSupportValues = new ArrayList<>();
    private SharedPreferences mCameraSoundSharedPreferences;
    private ICameraMode.ModeType mModeType = ICameraMode.ModeType.PHOTO;

    @Override
    public void init(IApp app, ICameraContext cameraContext, SettingController settingController) {
        super.init(app, cameraContext, settingController);
        mSettingView = new CameraSoundSettingView();
        mSettingView.setCameraSoundViewListener(mCameraSoundViewListener);
        initSettingValue(app);
    }

    @Override
    public void unInit() {

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
            mSettingView.setChecked(CAMERASOUND_ON.equals(getValue()));
            mSettingView.setEnabled(getEntryValues().size() > 1);
        }
    }

    @Override
    public void onModeOpened(String modeKey, ICameraMode.ModeType modeType) {
        super.onModeOpened(modeKey, modeType);
        mModeType = modeType;
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
        return KEY_CAMERASOUND;
    }

    @Override
    public ICaptureRequestConfigure getCaptureRequestConfigure() {
        return null;
    }

    private CameraSoundSettingView.OnCameraSoundViewListener mCameraSoundViewListener
            = new CameraSoundSettingView.OnCameraSoundViewListener() {
        @Override
        public void onItemViewClick(boolean isOn) {
			
            LogHelper.d(TAG, "[onItemViewClick], isOn:" + isOn);
            String value = isOn ? CAMERASOUND_ON : CAMERASOUND_OFF;
            setValue(value);
            mCameraSoundSharedPreferences.edit().putBoolean("camera_sound_value",isOn).commit();
        }
    };

    private void initSettingValue(IApp app) {
        mSupportValues.add(CAMERASOUND_OFF);
        mSupportValues.add(CAMERASOUND_ON);
        setSupportedPlatformValues(mSupportValues);
        setSupportedEntryValues(mSupportValues);
        setEntryValues(mSupportValues);

        mCameraSoundSharedPreferences = app.getActivity().getSharedPreferences("camera_sound", Context.MODE_PRIVATE);
        //start, wangsenhao, default camera sound value, 2019.06.03
        boolean isSoundOn = mActivity.getResources().getBoolean(R.bool.config_default_camerasound_on);
        String value = mCameraSoundSharedPreferences.getBoolean("camera_sound_value", isSoundOn) ? CAMERASOUND_ON : CAMERASOUND_OFF;
        //end, wangsenhao, default camera sound value, 2019.06.03
        setValue(value);
    }
}
