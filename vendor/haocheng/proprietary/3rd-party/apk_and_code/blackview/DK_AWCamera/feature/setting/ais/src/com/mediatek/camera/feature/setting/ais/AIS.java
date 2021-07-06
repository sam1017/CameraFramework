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

package com.mediatek.camera.feature.setting.ais;


import com.mediatek.camera.common.ICameraContext;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.relation.Relation;
import com.mediatek.camera.common.setting.ISettingManager.SettingController;
import com.mediatek.camera.common.setting.SettingBase;

import java.util.List;

import javax.annotation.Nonnull;
//bv wuyonglin add for hd shot 20201013 start
import com.mediatek.camera.Config;
import com.mediatek.camera.common.IAppUi;
import com.mediatek.camera.R;
//bv wuyonglin add for hd shot 20201013 end

/**
 * This class is for AIS feature interacted with others.
 */

public class AIS extends SettingBase implements AISSettingView.OnAisClickListener {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(AIS.class.getSimpleName());
    private static final String KEY_AIS = "key_ais";
    private static final String AIS_OFF = "off";
    private static final String AIS_ON = "on";
    private ISettingChangeRequester mSettingChangeRequester;
    private AISSettingView mSettingView;
    private String mOverrideValue;

    @Override
    public void init(IApp app, ICameraContext cameraContext, SettingController settingController) {
        super.init(app, cameraContext, settingController);
        //bv wuyonglin add for hd shot 20201013 start
        if (Config.isAisSupport(mActivity)) {
            mAppUi.setRestoreSettingListener(mRestoreSettingListener);
        }
        //bv wuyonglin add for hd shot 20201013 end
    }

    @Override
    public void unInit() {
        //bv wuyonglin add for hd shot 20201013 start
        mAppUi.removeRestoreSettingListener(mRestoreSettingListener);
        //bv wuyonglin add for hd shot 20201013 end
    }

    @Override
    public void addViewEntry() {
        if (Config.isAisSupport(mActivity)) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mSettingView == null) {
                    mSettingView = new AISSettingView(AIS.this);
                    mSettingView.setAisClickListener(AIS.this);
                }
                mAppUi.addSettingView(mSettingView);
            }
        });
        }
    }

    @Override
    public void removeViewEntry() {
        if (Config.isAisSupport(mActivity)) {
        mAppUi.removeSettingView(mSettingView);
        }
    }

    @Override
    public void refreshViewEntry() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mSettingView != null) {
                    mSettingView.setChecked(AIS_ON.equals(mDataStore.getValue(getKey(), "off", getStoreScope())));  //bv wuyonglin modify for ais switch status 20201116
                    //bv wuyonglin add for hd shot 20201013 start
                    String mHdrValue = mDataStore.getValue("key_hdr", "off", mDataStore.getGlobalScope());
                    String mFlashStatus = mDataStore.getValue("key_flash", mApp.getActivity().getString(R.string.flash_default_value), "_preferences_0");
                LogHelper.d(TAG, "[refreshViewEntry], mHdrValue ="+mHdrValue+" mFlashStatus ="+mFlashStatus+" getEntryValues().size() ="+getEntryValues().size());
                    if ((!mHdrValue.equals("off") || !mFlashStatus.equals("off"))  && 1 != getCameraId()) {
                    mSettingView.setEnabled(false);
		    } else {
                    mSettingView.setEnabled(getEntryValues().size() > 1);
                LogHelper.d(TAG, "[refreshViewEntry], setEnabled ="+(getEntryValues().size() > 1));
		    }
                    //bv wuyonglin add for hd shot 20201013 end
                }
            }
        });
    }

    @Override
    public void postRestrictionAfterInitialized() {
        Relation relation = AISRestriction.getRestrictionGroup()
                .getRelation(getValue(), false);
        if (relation != null) {
            mSettingController.postRestriction(relation);
        }
    }

    @Override
    public SettingType getSettingType() {
        return SettingType.PHOTO;
    }

    @Override
    public String getKey() {
        return KEY_AIS;
    }

    @Override
    public void overrideValues(@Nonnull String headerKey, String currentValue,
                               List<String> supportValues) {
        mOverrideValue = currentValue;
        super.overrideValues(headerKey, currentValue, supportValues);
    }

    @Override
    public ICaptureRequestConfigure getCaptureRequestConfigure() {
        AISCaptureRequestConfig captureRequestConfig;
        if (mSettingChangeRequester == null) {
            captureRequestConfig = new AISCaptureRequestConfig(this, mSettingDevice2Requester,
                    mActivity.getApplicationContext());
            mSettingChangeRequester = captureRequestConfig;
        }
        return (AISCaptureRequestConfig) mSettingChangeRequester;
    }

    @Override
    public void onAisClicked(boolean isOn) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                LogHelper.d(TAG, "[onItemViewClick], isOn:" + isOn);
                String value = isOn ? AIS_ON : AIS_OFF;
                setValue(value);
                mDataStore.setValue(getKey(), value, getStoreScope(), false);
                Relation relation = AISRestriction.getRestrictionGroup().getRelation(value, true);
                mSettingController.postRestriction(relation);
                mSettingController.refreshViewEntry();
                mAppUi.refreshSettingView();
                //bv wuyonglin add for hd shot 20201013 start
                mSettingDevice2Requester.requestRestartSession();
                //bv wuyonglin add for hd shot 20201013 end
                mSettingChangeRequester.sendSettingChangeRequest();
            }
        });
    }

    /**
     * Initialize setting all values after platform supported values ready.
     *
     * @param platformSupportedValues The values current platform is supported.
     * @param defaultValue            The scene mode default value.
     */
    public void initializeValue(List<String> platformSupportedValues, String defaultValue) {
        if (platformSupportedValues.size() > 0) {
            LogHelper.d(TAG, "[initializeValue], platformSupportedValues:" + platformSupportedValues.size()+" defaultValue ="+defaultValue);
            setEntryValues(platformSupportedValues);
            setSupportedEntryValues(platformSupportedValues);
            setSupportedPlatformValues(platformSupportedValues);
            setValue(mDataStore.getValue(getKey(), defaultValue, getStoreScope()));
        }
    }


    /**
     * Get AIS override value.
     *
     * @return The override value.
     */
    public String getOverrideValue() {
        return mOverrideValue;
    }

    @Override
    public synchronized void onModeClosed(String modeKey) {
        super.onModeClosed(modeKey);
        Relation relation = AISRestriction.getRestrictionGroup().getRelation(KEY_AIS, true);
        mSettingController.postRestriction(relation);
    }

    /**
     * Get current camera id.
     * @return The current camera id.
     */
    protected int getCameraId() {
        int cameraId = Integer.parseInt(mSettingController.getCameraId());
        return cameraId;
    }

    //bv wuyonglin add for hd shot 20201013 start
    private IAppUi.RestoreSettingListener mRestoreSettingListener = new IAppUi.RestoreSettingListener() {
        @Override
        public void restoreSettingtoValue() {
            LogHelper.i(TAG, "restoreSettingtoValue");
            setValue("off");
            mDataStore.setValue(getKey(), "off", getStoreScope(), false);
            mSettingChangeRequester.sendSettingChangeRequest();
        }
    };

    public String getCurrentMode(){
        return mAppUi.getCurrentMode();
    }
    //bv wuyonglin add for hd shot 20201013 end
}
