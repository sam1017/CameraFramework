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
package com.mediatek.camera.feature.setting.storagepath;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;

import com.mediatek.camera.R;
import com.mediatek.camera.common.ICameraContext;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.setting.ISettingManager;
import com.mediatek.camera.common.setting.SettingBase;
import com.mediatek.camera.common.storage.IStorageService;
import com.mediatek.camera.common.storage.SDCardFileUtils;
import android.net.Uri;
import android.provider.DocumentsContract;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
//bv wuyonglin add for setting ui 20200923 start
import com.mediatek.camera.common.IAppUi;
//bv wuyonglin add for setting ui 20200923 end

/**
 * This class is for storage path feature interacted with others.
 */

public class StoragePath extends SettingBase {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(StoragePath.class.getSimpleName());
    private StoragePathSettingView mStoragePathSettingView;
    private List<String> mSupportValues = new ArrayList<>();
    private SharedPreferences mStoragePathSharedPreferences;
    private static final String STORAGE_PATH_VALUE = "storage_path_value";
    //bv wuyonglin add for setting ui 20200923 start
    private String defaultValue = "";
    //bv wuyonglin add for setting ui 20200923 end

    @Override
    public void init(IApp app,
                     ICameraContext cameraContext,
                     ISettingManager.SettingController settingController) {
        super.init(app, cameraContext, settingController);
        mStoragePathSettingView = new StoragePathSettingView();
        SDCardFileUtils.setSDCardChangeListener(mSDCardChangeListener);
        mCameraContext.getStorageService().registerStorageStateListener(mStorageStateListener);
        initSettingValue(app);
        //bv wuyonglin add for setting ui 20200923 start
        defaultValue = app.getActivity().getResources().getString(R.string.storage_path_default_value);
        mAppUi.setRestoreSettingListener(mRestoreSettingListener);
        //bv wuyonglin add for setting ui 20200923 end
    }

    @Override
    public void unInit() {
        mCameraContext.getStorageService().unRegisterStorageStateListener(mStorageStateListener);
        //bv wuyonglin add for setting ui 20200923 start
        mAppUi.removeRestoreSettingListener(mRestoreSettingListener);
        //bv wuyonglin add for setting ui 20200923 end
    }

    @Override
    public void addViewEntry() {
        mStoragePathSettingView.setOnValueChangeListener(mValueChangeListener);
        mAppUi.addSettingView(mStoragePathSettingView);
        LogHelper.d(TAG, "[addViewEntry] getValue() :" + getValue());
    }

    @Override
    public void removeViewEntry() {
        mAppUi.removeSettingView(mStoragePathSettingView);
        LogHelper.d(TAG, "[removeViewEntry]");
    }

    @Override
    public void refreshViewEntry() {
        int size = getEntryValues().size();
        if (mStoragePathSettingView != null) {
            mStoragePathSettingView.setEntryValues(getEntryValues());
            mStoragePathSettingView.setValue(getValue());
            mStoragePathSettingView.setEnabled(size > 1);
        }
    }

    @Override
    public SettingType getSettingType() {
        return SettingType.PHOTO_AND_VIDEO;
    }

    @Override
    public String getKey() {
        return IStoragePathViewListener.KEY_STORAGE_PATH;
    }

    @Override
    public ICaptureRequestConfigure getCaptureRequestConfigure() {
        return null;
    }

    @Override
    public void postRestrictionAfterInitialized() {

    }

    private IStoragePathViewListener.OnValueChangeListener mValueChangeListener
            = new IStoragePathViewListener.OnValueChangeListener() {
        @Override
        public void onValueChanged(String value) {
            if (!value.equals(getValue())) {
                mAppUi.notifyUpdateThumbnail();
            }
            setValue(value);
            mStoragePathSharedPreferences.edit().putString(STORAGE_PATH_VALUE, value).commit();
            //bv wuyonglin add for bug6080 20210521 start
            mCameraContext.getStorageService().updateStorageHint();
            //bv wuyonglin add for bug6080 20210521 end
        }
    };

    private IStorageService.IStorageStateListener mStorageStateListener = new IStorageService.IStorageStateListener() {
        @Override
        public void onStateChanged(int storageState, Intent intent) {
            if (Intent.ACTION_MEDIA_EJECT.equals(intent.getAction())) {
                LogHelper.i(TAG, "[onStateChanged] storage out service Intent.ACTION_MEDIA_EJECT");
                setValue(IStoragePathViewListener.INTERNAL_STORAGE);
                mStoragePathSharedPreferences.edit().putString(STORAGE_PATH_VALUE, IStoragePathViewListener.INTERNAL_STORAGE).commit();
            }
        }
    };

    private SDCardFileUtils.SDCardChangeListener mSDCardChangeListener
            = new SDCardFileUtils.SDCardChangeListener() {
        @Override
        public void onSDCardChanged(String file) {
            setValue(IStoragePathViewListener.EXTERNAL_STORAGE);
            mStoragePathSharedPreferences.edit().putString(STORAGE_PATH_VALUE, IStoragePathViewListener.EXTERNAL_STORAGE).commit();
        }
    };

    private void initSettingValue(IApp app) {
        mSupportValues.add(IStoragePathViewListener.INTERNAL_STORAGE);
        mSupportValues.add(IStoragePathViewListener.EXTERNAL_STORAGE);
        setSupportedPlatformValues(mSupportValues);
        setSupportedEntryValues(mSupportValues);
        setEntryValues(mSupportValues);

        mStoragePathSharedPreferences = app.getActivity().getSharedPreferences("storage_path", Context.MODE_PRIVATE);
        //bv wuyonglin add for setting ui 20200923 start
        //String defaultValue = app.getActivity().getResources().getString(R.string.storage_path_default_value);
        //bv wuyonglin add for setting ui 20200923 end
        String value = mStoragePathSharedPreferences.getString(STORAGE_PATH_VALUE, defaultValue);
        setValue(value);
        mStoragePathSharedPreferences.edit().putString(STORAGE_PATH_VALUE, value).commit();
    }

    //bv wuyonglin add for setting ui 20200923 start
    private IAppUi.RestoreSettingListener mRestoreSettingListener = new IAppUi.RestoreSettingListener() {
        @Override
        public void restoreSettingtoValue() {
            LogHelper.d(TAG, "restoreSettingtoValue defaultValue ="+defaultValue);
            setValue(defaultValue);
            mAppUi.notifyUpdateThumbnail();
            mStoragePathSharedPreferences.edit().putString(STORAGE_PATH_VALUE, defaultValue).commit();
            //bv wuyonglin add for bug6080 20210521 start
            mCameraContext.getStorageService().updateStorageHint();
            //bv wuyonglin add for bug6080 20210521 end
        }
    };
    //bv wuyonglin add for setting ui 20200923 end
}
