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

package com.mediatek.camera.feature.setting.fps60;

import com.mediatek.camera.common.ICameraContext;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.relation.StatusMonitor;
import com.mediatek.camera.common.setting.ISettingManager.SettingController;
import com.mediatek.camera.common.setting.SettingBase;
import android.media.CamcorderProfile;
/**
 * This class is for EIS feature interacted with others.
 */

public class Fps60 extends SettingBase {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(Fps60.class.getSimpleName());
    private static final String KEY_NOISE_FPS60 = "key_fps60";
    private static final String KEY_HDR10 = "key_hdr10";
    private static final String KEY_EIS = "key_eis";
    private static final String Fps60_OFF = "off";
    private static final String Fps60_ON = "on";
    private boolean mIsSupported = false;
    private ISettingChangeRequester mSettingChangeRequester;
    private Fps60SettingView mSettingView;
    private SettingController mSettingController;
    private String mFpsSetValue;
    private static final String KEY_HDR10_STATUS = "key_hdr10_status";
    private static final String KEY_EIS_STATUS = "key_eis_status";
    private static final String KEY_VIDEO_QUALITY_STATUS = "key_video_quality_status";

    @Override
    public void init(IApp app, ICameraContext cameraContext, SettingController settingController) {
        super.init(app, cameraContext, settingController);
        mSettingController = settingController;
        mFpsSetValue = mDataStore.getValue(getKey(), Fps60_OFF, getStoreScope());
        setValue(mFpsSetValue);
        mSettingView = new Fps60SettingView();
        mSettingView.setFps60ViewListener(mFps60ViewListener);
        mStatusMonitor.registerValueChangedListener(KEY_HDR10_STATUS,mStatusChangeListener);
        mStatusMonitor.registerValueChangedListener(KEY_EIS_STATUS,mStatusChangeListener);
        mStatusMonitor.registerValueChangedListener(KEY_VIDEO_QUALITY_STATUS,mStatusChangeListener);

    }

    @Override
    public void unInit() {
        mStatusMonitor.unregisterValueChangedListener(KEY_HDR10_STATUS,mStatusChangeListener);
        mStatusMonitor.unregisterValueChangedListener(KEY_EIS_STATUS,mStatusChangeListener);
        mStatusMonitor.unregisterValueChangedListener(KEY_VIDEO_QUALITY_STATUS,mStatusChangeListener);
    }

    @Override
    public void addViewEntry() {
        //bv wuyonglin delete for 60fps 20201124 start
        //mAppUi.addSettingView(mSettingView);
        //bv wuyonglin delete for 60fps 20201124 end
    }

    @Override
    public void removeViewEntry() {
        //bv wuyonglin delete for 60fps 20201124 start
        //mAppUi.removeSettingView(mSettingView);
        //bv wuyonglin delete for 60fps 20201124 end
    }

    @Override
    public void refreshViewEntry() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mSettingView != null) {
                    mSettingView.setChecked(Fps60_ON.equals(getValue()));
                    mSettingView.setEnabled(getEntryValues().size() > 1);
                }
            }
        });
    }

    @Override
    public void postRestrictionAfterInitialized() {

    }

    @Override
    public SettingType getSettingType() {
        return SettingType.VIDEO;
    }

    @Override
    public String getKey() {
        return KEY_NOISE_FPS60;
    }

    @Override
    public ICaptureRequestConfigure getCaptureRequestConfigure() {
        Fps60CaptureRequestConfig captureRequestConfig;
        if (mSettingChangeRequester == null) {
            captureRequestConfig = new Fps60CaptureRequestConfig(this, mSettingDevice2Requester,
                    mActivity.getApplicationContext());
            mSettingChangeRequester = captureRequestConfig;
        }
        return (Fps60CaptureRequestConfig) mSettingChangeRequester;
    }

    public SettingController getSettingController() {
        return mSettingController;
    }

    private Fps60SettingView.OnFps60ViewListener mFps60ViewListener
            = new Fps60SettingView.OnFps60ViewListener() {
        @Override
        public void onItemViewClick(boolean isOn) {
            LogHelper.d(TAG, "[onItemViewClick], isOn:" + isOn);
            String value = isOn ? Fps60_ON : Fps60_OFF;
            mFpsSetValue = value;
            setValue(value);
            mDataStore.setValue(getKey(), value, getStoreScope(), false);
            mSettingController.postRestriction(Fps60Restriction.getFps60Restriction()
                    .getRelation(mFpsSetValue,true));
            mSettingController.refreshViewEntry();
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mSettingChangeRequester.sendSettingChangeRequest();
                }
            });
        }

        @Override
        public boolean onCachedValue() {
            return Fps60_ON.equals(
                    mDataStore.getValue(getKey(), Fps60_OFF, getStoreScope()));
        }
    };

    private boolean isSupportQuality() {
        boolean is4kQuality;
        int currentQuality = Integer.parseInt(
                mSettingController.queryValue("key_video_quality"));
        is4kQuality = CamcorderProfile.QUALITY_2160P == currentQuality;
        LogHelper.d(TAG, "[is4kQuality]," + is4kQuality);
        return is4kQuality;
    }

    /**
     * update set value.
     *
     * @param value the default value
     */
    public void updateValue(String value) {
        setValue(mDataStore.getValue(getKey(), value, getStoreScope()));
    }

    /**
     * Get current camera id.
     * @return The current camera id.
     */
    protected int getCameraId() {
        int cameraId = Integer.parseInt(mSettingController.getCameraId());
        return cameraId;
    }

    private StatusMonitor.StatusChangeListener mStatusChangeListener = new StatusMonitor.StatusChangeListener() {
        @Override
        public void onStatusChanged(String key, String value) {
            LogHelper.d(TAG, "[onStatusChanged], = " + key);
            if(key.equals(KEY_HDR10_STATUS)||key.equals(KEY_EIS_STATUS)
                ||key.equals(KEY_VIDEO_QUALITY_STATUS)){
                mSettingController.postRestriction(Fps60Restriction.getFps60Restriction()
                        .getRelation(mFpsSetValue,true));
                mSettingController.refreshViewEntry();
            }
        }
    };

    public String getCurrentMode(){
        return mAppUi.getCurrentMode();
    }
}
