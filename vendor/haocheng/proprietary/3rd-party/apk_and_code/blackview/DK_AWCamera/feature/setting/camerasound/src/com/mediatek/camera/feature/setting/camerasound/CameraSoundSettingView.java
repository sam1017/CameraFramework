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

package com.mediatek.camera.feature.setting.camerasound;

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
import com.mediatek.camera.common.preference.SwitchPreference;
import com.mediatek.camera.common.setting.ICameraSettingView;
import android.content.SharedPreferences;
import android.content.Context;
import android.util.Log;
/**
 * CameraSound setting view.
 */

public class CameraSoundSettingView implements ICameraSettingView {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(CameraSoundSettingView.class.getSimpleName());
    private static final String KEY_CAMERASOUND = "key_camera_sound";
    private boolean isSoundOn = false;
    private OnCameraSoundViewListener mViewListener;
    private SwitchPreference mPref;
    private boolean mChecked;
    private boolean mEnabled;
    private SharedPreferences mCameraSoundSharedPreferences;

    /**
     * Listener with Location view.
     */
    interface OnCameraSoundViewListener {
        void onItemViewClick(boolean isOn);
    }

    @Override
    public void loadView(final PreferenceFragment fragment) {
        fragment.addPreferencesFromResource(R.xml.camera_sound_preference);
        mCameraSoundSharedPreferences = fragment.getActivity().getSharedPreferences("camera_sound", Context.MODE_PRIVATE);
        mPref = (SwitchPreference) fragment.findPreference(KEY_CAMERASOUND);
        mPref.setRootPreference(fragment.getPreferenceScreen());
        mPref.setId(R.id.camerasound_setting);
        mPref.setContentDescription(fragment.getActivity().getResources()
                .getString(R.string.camers_sound_content_description));
        mPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object object) {
                boolean value = (Boolean) object;
                mChecked = value;
                mViewListener.onItemViewClick(value);
                return true;
            }
        });
        //start, wangsenhao, default camera sound value, 2019.06.03
        isSoundOn = fragment.getActivity().getResources().getBoolean(R.bool.config_default_camerasound_on);
        mPref.setChecked(mCameraSoundSharedPreferences.getBoolean("camera_sound_value",isSoundOn));
        //end, wangsenhao, default camera sound value, 2019.06.03
        mPref.setEnabled(mEnabled);
    }

    @Override
    public void refreshView() {
        if (mPref != null) {
            //start, wangsenhao, default camera sound value, 2019.06.03
            mPref.setChecked(mCameraSoundSharedPreferences.getBoolean("camera_sound_value",isSoundOn));
            //end, wangsenhao, default camera sound value, 2019.06.03
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
     * This is to set CameraSound view update listener.
     *
     * @param viewListener the CameraSound view listener.
     */
    public void setCameraSoundViewListener(OnCameraSoundViewListener viewListener) {
        mViewListener = viewListener;
    }

    public void setChecked(boolean checked) {
        mChecked = checked;
        refreshView();
    }
}
