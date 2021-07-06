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

package com.mediatek.camera.feature.setting.mirror;

import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.mediatek.camera.R;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.preference.SwitchPreference;
import com.mediatek.camera.common.setting.ICameraSettingView;

/**
 * EIS setting view.
 */

public class MirrorSettingView implements ICameraSettingView {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(MirrorSettingView.class.getSimpleName());
    private static final String KEY_MIRROR = "key_camera_mirror";
    private OnMirrorViewListener mViewListener;
    private SwitchPreference mPref;
    private boolean mChecked;
    private boolean mEnabled;
    //bv wuyonglin add for setting ui 20200923 start
    private int mCameraId;
    //bv wuyonglin add for setting ui 20200923 end

    /**
     * Listener with Mirror view.
     */
    interface OnMirrorViewListener {
        void onItemViewClick(boolean isOn);

        boolean onCachedValue();
    }

    //bv wuyonglin add for setting ui 20200923 start
    public MirrorSettingView(int cameraId) {
        mCameraId = cameraId;
    }
    //bv wuyonglin add for setting ui 20200923 end

    @Override
    public void loadView(final PreferenceFragment fragment) {
        fragment.addPreferencesFromResource(R.xml.camera_mirror_preference);
        mPref = (SwitchPreference) fragment.findPreference(KEY_MIRROR);
        mPref.setRootPreference(fragment.getPreferenceScreen());
        mPref.setId(R.id.mirror_setting);
        mPref.setContentDescription(fragment.getActivity().getResources()
                .getString(R.string.camers_mirror_content_description));
        //bv wuyonglin add for setting ui 20200923 start
        mPref.setSummary(fragment.getActivity().getResources().getString(R.string.mirror_summary));
        //bv wuyonglin add for setting ui 20200923 end
        mPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object object) {
                boolean value = (Boolean) object;
                mChecked = value;
                mViewListener.onItemViewClick(value);
                return true;
            }
        });

        //bv wuyonglin add for setting ui 20200923 start
        if (mCameraId != 1) {
            mPref.setDisableView(true);
        }
        //bv wuyonglin add for setting ui 20200923 start
        mPref.setChecked(mViewListener.onCachedValue());
        mPref.setEnabled(mEnabled);
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
     * This is to set Mirror view update listener.
     *
     * @param viewListener the Mirror view listener.
     */
    public void setMirrorViewListener(OnMirrorViewListener viewListener) {
        mViewListener = viewListener;
    }

    /**
     * Set Mirror reduction state.
     *
     * @param checked True means Mirror is opened, false means Mirror is closed.
     */
    public void setChecked(boolean checked) {
        mChecked = checked;
        refreshView();
    }
}
