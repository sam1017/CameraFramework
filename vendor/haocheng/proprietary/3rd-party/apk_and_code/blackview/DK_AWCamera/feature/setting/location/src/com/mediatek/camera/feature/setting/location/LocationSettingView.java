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

package com.mediatek.camera.feature.setting.location;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.provider.Settings;
import android.widget.Toast;

import com.mediatek.camera.R;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.permission.PermissionManager;
import com.mediatek.camera.common.preference.SwitchPreference;
import com.mediatek.camera.common.setting.ICameraSettingView;
//add by huangfei for location abnormal start
import com.mediatek.camera.common.ICameraContext;
import com.mediatek.camera.common.setting.ISettingManager.SettingController;
import com.mediatek.camera.common.relation.DataStore;
//add by huangfei for location abnormal end

/**
 * EIS setting view.
 */

public class LocationSettingView implements ICameraSettingView {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(LocationSettingView.class.getSimpleName());
    private static final String KEY_LOCATION = "key_location";
    private static final String KEY_LOCATION_PERMISSION = "key_location_permission";
    private OnLocationViewListener mViewListener;
    private SwitchPreference mPref;
    private boolean mChecked;
    private boolean mEnabled;
    private PermissionManager mPermissionManager;
    //add by huangfei for location abnormal start
    private String mLocationEnable;
    //add by huangfei for location abnormal end

    /**
     * Listener with Location view.
     */
    interface OnLocationViewListener {
        void onItemViewClick(boolean isOn);

        boolean onCachedValue();
    }

    @Override
    public void loadView(final PreferenceFragment fragment) {
        mPermissionManager = new PermissionManager(fragment.getActivity());
        fragment.addPreferencesFromResource(R.xml.location_preference);
        mPref = (SwitchPreference) fragment.findPreference(KEY_LOCATION);
        mPref.setRootPreference(fragment.getPreferenceScreen());
        mPref.setId(R.id.location_setting);
        mPref.setContentDescription(fragment.getActivity().getResources()
                .getString(R.string.location_content_description));
        mPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object object) {
                boolean value = (Boolean) object;
                if (value) {
                    if (!mPermissionManager.checkCameraLocationPermissions()) {
                        mPermissionManager.requestCameraLocationPermissions();
                    } else {
                        mChecked = value;
                        mViewListener.onItemViewClick(value);
                    }
                } else {
                    mChecked = value;
                    mViewListener.onItemViewClick(value);
                }
                return true;
            }
        });
        mPref.setChecked(mViewListener.onCachedValue());
        mPref.setEnabled(mEnabled);
    }

    private void goToAppSetting(Activity activity) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
        intent.setData(uri);
        activity.startActivity(intent);
    }


    @Override
    public void refreshView() {
        if (mPref != null) {
            mPref.setChecked(mChecked);
            mPref.setEnabled(mEnabled);
        }
    }

    @Override
    public void unloadView() {
        LogHelper.i(TAG, "[unloadView]");
    }

    @Override
    public void setEnabled(boolean enabled) {
        mEnabled = enabled;
    }

    @Override
    public boolean isEnabled() {
        return mEnabled;
    }

    /**
     * This is to set Location view update listener.
     *
     * @param viewListener the EIS view listener.
     */
    public void setLocationViewListener(OnLocationViewListener viewListener) {
        mViewListener = viewListener;
    }

    /**
     * Set Location reduction state.
     *
     * @param checked True means Eis is opened, false means Eis is closed.
     */
    public void setChecked(boolean checked) {
        mChecked = checked;
        refreshView();
    }
    //add by huangfei for location abnormal start
    public void refreshViewAfterSetPermission(PermissionManager permissionManager,ICameraContext cameraContext){
        DataStore mDataStore = cameraContext.getDataStore();
        if(permissionManager!=null){
            if (permissionManager.checkCameraLocationPermissions()) {
                mLocationEnable = mDataStore.getValue(KEY_LOCATION_PERMISSION, "off", mDataStore.getGlobalScope());
                if("off".equals(mLocationEnable)){
                    mViewListener.onItemViewClick(true);
                    mDataStore.setValue(KEY_LOCATION_PERMISSION, "on", mDataStore.getGlobalScope(), false);
                }            
               
            }else{
                mDataStore.setValue(KEY_LOCATION_PERMISSION, "off", mDataStore.getGlobalScope(), false);
            }
        }
    }
    //add by huangfei for location abnormal end
}
