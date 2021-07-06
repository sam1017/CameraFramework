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
package com.mediatek.camera.feature.setting.gridlines;

import com.mediatek.camera.Config;
import com.mediatek.camera.common.relation.Relation;
import com.mediatek.camera.common.IAppUiListener;
import com.mediatek.camera.common.ICameraContext;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.mode.ICameraMode;
import com.mediatek.camera.common.setting.ISettingManager;
import com.mediatek.camera.common.setting.SettingBase;

import java.util.ArrayList;
import java.util.List;
//bv wuyonglin add for setting ui 20200923 start
import com.mediatek.camera.common.IAppUi;
//bv wuyonglin add for setting ui 20200923 end

/**
 * This class is for gridlines feature interacted with others.
 */

public class Gridlines extends SettingBase {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(Gridlines.class.getSimpleName());

    private static final String MODE_DEVICE_STATE_UNKNOWN = "unknown";
    private static final String MODE_DEVICE_STATE_PREVIEWING = "previewing";
    private static final String KEY_GRIDLINES = "key_gridlines";
    private static final String GRIDLINES_OFF = "0";
    private static final String GRIDLINES_ON = "1";
    private String mModeDeviceState = MODE_DEVICE_STATE_UNKNOWN;
    private GridlinesSettingView mSettingView;;
    private List<String> mSupportValues = new ArrayList<String>();
    private GridlinesMonitor mMonitor;

    @Override
    public void init(IApp app,
                     ICameraContext cameraContext,
                     ISettingManager.SettingController settingController) {
        super.init(app, cameraContext, settingController);
        mSettingView = new GridlinesSettingView();
        mSettingView.setGridlinesViewListener(mGridlinesViewListener);
        mMonitor = app.getAppUi().getMonitor();
        updateSupportedValues();
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
    public ICaptureRequestConfigure getCaptureRequestConfigure() {
        return null;
    }

    @Override
    public void addViewEntry() {
        mAppUi.addSettingView(mSettingView);
        LogHelper.d(TAG, "[addViewEntry] getValue() :" + getValue());
    }

    @Override
    public void removeViewEntry() {
        mAppUi.removeSettingView(mSettingView);
        LogHelper.d(TAG, "[removeViewEntry]");
    }

    @Override
    public void refreshViewEntry() {
        int size = getEntryValues().size();
        if (mSettingView != null) {
            mSettingView.setChecked(GRIDLINES_ON.equals(getValue()));
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
        return KEY_GRIDLINES;
    }

    private GridlinesSettingView.OnGridlinesViewListener mGridlinesViewListener
            = new GridlinesSettingView.OnGridlinesViewListener() {
        @Override
        public void onItemViewClick(boolean isOn) {
            LogHelper.d(TAG, "[onItemViewClick], isOn:" + isOn);
            String value = isOn ? GRIDLINES_ON : GRIDLINES_OFF;
            setValue(value);
            mMonitor.getGridlinesChangeListener().onVauleChange(isOn);
            mDataStore.setValue(getKey(), value, mDataStore.getGlobalScope(), false);
        }

        @Override
        public boolean onCachedValue() {
            String value = mDataStore.getValue(getKey(), GRIDLINES_OFF, mDataStore.getGlobalScope());
            return  GRIDLINES_ON.equals(value);
        }
    };



    /**
     * update set value.
     *
     * @param value the default value
     */
    public void updateValue(String value) {
        String mVaule = mDataStore.getValue(getKey(), value, getStoreScope());
        setValue(mVaule);
    }

    private void updateSupportedValues() {
        List<String> supported = new ArrayList<>();
        supported.add(GRIDLINES_ON);
        supported.add(GRIDLINES_OFF);
        setSupportedPlatformValues(supported);
        setSupportedEntryValues(supported);
        setEntryValues(supported);
        String value = mDataStore.getValue(getKey(), GRIDLINES_OFF, mDataStore.getGlobalScope());
        if(GRIDLINES_ON.equals(value)){
            mMonitor.getGridlinesChangeListener().onVauleChange(true);
        }
        setValue(value);
        mDataStore.setValue(getKey(), value, mDataStore.getGlobalScope(), false);
    }

    //bv wuyonglin add for setting ui 20200923 start
    private IAppUi.RestoreSettingListener mRestoreSettingListener = new IAppUi.RestoreSettingListener() {
        @Override
        public void restoreSettingtoValue() {
            setValue(GRIDLINES_OFF);
            mMonitor.getGridlinesChangeListener().onVauleChange(false);
            mDataStore.setValue(getKey(), GRIDLINES_OFF, mDataStore.getGlobalScope(), false);
        }
    };
    //bv wuyonglin add for setting ui 20200923 end
}
