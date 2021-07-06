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

package com.mediatek.camera.feature.setting.volumekey;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.preference.PreferenceFragment;

import android.preference.PreferenceGroup;
import com.mediatek.camera.R;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.preference.Preference;
import com.mediatek.camera.common.setting.ICameraSettingView;

import java.util.ArrayList;
import java.util.List;
/**
 * VolumeKey setting view.
 */

public class VolumeKeySettingView implements ICameraSettingView {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(VolumeKeySettingView.class.getSimpleName());
    private PreferenceFragment mFragment;
    private String mSelectedValue;
    private List<String> mEntryValues = new ArrayList<>();
    private static final String KEY_VOLUME_KEY = "key_volume_key";
    private IVolumeKeyViewListener.OnValueChangeListener mOnValueChangeListener;
    private Preference mPref;
    private VolumeKeySelector mVolumeKeySelector;
    private boolean mChecked;
    private boolean mEnabled;
    private Activity mContext;
    
    /**
     * Listener with VolumeKey view.
     */
    @Override
    public void loadView(final PreferenceFragment fragment) {
        fragment.addPreferencesFromResource(R.xml.volume_key_preference);
        mFragment = fragment;
        mContext = fragment.getActivity();

        if (mVolumeKeySelector == null) {
            mVolumeKeySelector = new VolumeKeySelector();
            mVolumeKeySelector.setOnItemClickListener(mOnItemClickListener);
        }

        mPref = (Preference) fragment
                .findPreference(IVolumeKeyViewListener.KEY_VOLUME_KEY);
        mPref.setRootPreference(fragment.getPreferenceScreen());
        mPref.setDisableView(true);
        mPref.setId(R.id.volumekey_setting);
        mPref.setContentDescription(fragment.getActivity().getResources()
                .getString(R.string.volume_key_content_description));
        mPref.setSummary(getSummary());
        mPref.setOnPreferenceClickListener(
                new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(android.preference.Preference preference) {
                mVolumeKeySelector.setValue(mSelectedValue);
                mVolumeKeySelector.setEntryValues(mEntryValues);

                FragmentTransaction transaction = mContext.getFragmentManager()
                        .beginTransaction();
                transaction.addToBackStack(null);
                transaction.replace(R.id.setting_container,
                        mVolumeKeySelector, "volume_key_selector").commit();
                return true;
            }
        });
       mPref.setEnabled(mEnabled);

    }

    @Override
    public void refreshView() {
        if (mPref != null) {
            LogHelper.d(TAG, "[refreshView]");
           
          mPref.setSummary(getSummary());
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
     * This is to set VolumeKey view update listener.
     *
     * @param viewListener the VolumeKey view listener.
     */
    public void setOnValueChangeListener(IVolumeKeyViewListener.OnValueChangeListener listener) {
        mOnValueChangeListener = listener;
    }

    /**
     * Set the default selected value.
     * @param value The default selected value.
     */
    public void setValue(String value) {
        mSelectedValue = value;
    }
    /**
     * Set the storage path supported.
     * @param entryValues The storage path supported.
     */
    public void setEntryValues(List<String> entryValues) {
        mEntryValues = entryValues;
    }

    private IVolumeKeyViewListener.OnItemClickListener mOnItemClickListener
            = new IVolumeKeyViewListener.OnItemClickListener() {
        @Override
        public void onItemClick(String value) {
            mSelectedValue = value;
            if (mOnValueChangeListener != null) {
                mOnValueChangeListener.onValueChanged(value);
            }
        }
    };

    private String getSummary() {
        if (IVolumeKeyViewListener.VOLUME_KEY_VOLUME.equals(mSelectedValue)) {
            return mContext.getString(R.string.volume_key_volume);
        } else if(IVolumeKeyViewListener.VOLUME_KEY_ZOOM.equals(mSelectedValue)) {
            return mContext.getString(R.string.volume_key_zoom);
        } else {
            return mContext.getString(R.string.volume_key_capture);
        }
    }
}
