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

import android.app.Activity;
import android.app.FragmentTransaction;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;

import com.mediatek.camera.R;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.preference.Preference;
import com.mediatek.camera.common.setting.ICameraSettingView;
import com.mediatek.camera.common.storage.SDCardFileUtils;
import com.mediatek.camera.common.utils.CameraUtil;

import java.util.ArrayList;
import java.util.List;
//bv wuyonglin add for setting ui 20200923 start
import com.mediatek.camera.common.widget.BottomListDialog;
import com.mediatek.camera.common.widget.OnClickPositionListener;
//bv wuyonglin add for setting ui 20200923 end

/**
 * This class is for storage path feature setting view.
 */

public class StoragePathSettingView  implements ICameraSettingView {
    private static final LogUtil.Tag TAG =
            new LogUtil.Tag(StoragePathSettingView.class.getSimpleName());

    private PreferenceFragment mFragment;
    private String mSelectedValue;
    private List<String> mEntryValues = new ArrayList<>();
    private IStoragePathViewListener.OnValueChangeListener mOnValueChangeListener;
    private Preference mStoragePathPreference;
    //bv wuyonglin add for setting ui 20200923 start
    //private StoragePathSelector mStoragePathSelector;
    private List<String> mTitleList = new ArrayList<>();
    private BottomListDialog.Builder builder;
    private BottomListDialog mBottomListDialog;
    //bv wuyonglin add for setting ui 20200923 end
    private Activity mContext;
    private boolean mEnabled;

    @Override
    public void loadView(PreferenceFragment fragment) {
        fragment.addPreferencesFromResource(R.xml.storage_path_preference);
        mFragment = fragment;
        mContext = fragment.getActivity();

        //bv wuyonglin delete for setting ui 20200923 start
        /*if (mStoragePathSelector == null) {
            mStoragePathSelector = new StoragePathSelector();
            mStoragePathSelector.setOnItemClickListener(mOnItemClickListener);
        }*/
        //bv wuyonglin delete for setting ui 20200923 end

        mStoragePathPreference = (Preference) fragment
                .findPreference(IStoragePathViewListener.KEY_STORAGE_PATH);
        mStoragePathPreference.setRootPreference(fragment.getPreferenceScreen());
        mStoragePathPreference.setDisableView(true);
        mStoragePathPreference.setId(R.id.storage_path_setting);
        mStoragePathPreference.setContentDescription(fragment.getActivity().getResources()
                .getString(R.string.storage_path_content_description));
        mStoragePathPreference.setSummary(getSummary());
        mStoragePathPreference.setOnPreferenceClickListener(
                new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(android.preference.Preference preference) {
                //bv wuyonglin add for setting ui 20200923 start
                /*mStoragePathSelector.setValue(mSelectedValue);
                mStoragePathSelector.setEntryValues(mEntryValues);

                FragmentTransaction transaction = mContext.getFragmentManager()
                        .beginTransaction();
                transaction.addToBackStack(null);
                transaction.replace(R.id.setting_container,
                        mStoragePathSelector, "storage_path_selector").commit();*/
		if (mBottomListDialog != null && mBottomListDialog.isShowing()) {
                    return true;
		}
                showBottomListDialog();
                //bv wuyonglin add for setting ui 20200923 end
                return true;
            }
        });
        mStoragePathPreference.setEnabled(mEnabled);
        //bv wuyonglin add for setting ui 20200923 start
        filterValuesOnShown();
        //bv wuyonglin add for setting ui 20200923 end
    }

    @Override
    public void refreshView() {
        if (mStoragePathPreference != null) {
            LogHelper.d(TAG, "[refreshView]");
            if (SDCardFileUtils.getSDCardVolumePath() == null){
                //mFragment.getPreferenceScreen().removePreference(mStoragePathPreference);
                mStoragePathPreference.setSummary(mContext.getString(R.string.no_sdcard));
                mStoragePathPreference.setEnabled(false);
            }else{
                mStoragePathPreference.setSummary(getSummary());
                mStoragePathPreference.setEnabled(mEnabled);
            }
        }
    }

    @Override
    public void unloadView() {
        //bv wuyonglin add for setting ui 20200923 start
        if (builder != null) {
            builder.hide();
        }
        //bv wuyonglin add for setting ui 20200923 end
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
     * Set listener to listen the changed self timer value.
     * @param listener The instance of {@link IStoragePathViewListener.OnValueChangeListener}.
     */
    public void setOnValueChangeListener(IStoragePathViewListener.OnValueChangeListener listener) {
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

    private IStoragePathViewListener.OnItemClickListener mOnItemClickListener
            = new IStoragePathViewListener.OnItemClickListener() {
        @Override
        public void onItemClick(String value) {
            mSelectedValue = value;
            if (mOnValueChangeListener != null) {
                mOnValueChangeListener.onValueChanged(value);
            }
        }
    };

    private String getSummary() {
        if (IStoragePathViewListener.EXTERNAL_STORAGE.equals(mSelectedValue)) {
            return mContext.getString(R.string.storage_path_external);
        } else {
            return mContext.getString(R.string.storage_path_internal);
        }
    }

    //bv wuyonglin add for setting ui 20200923 start
    public void showBottomListDialog() {
        if (mEntryValues.size() == 0) {
            return;
        }

        builder = new BottomListDialog.Builder(mContext, mContext.getResources().getString(R.string.storage_path_title), mSelectedValue, mTitleList, null);
        for (int i = 0; i < mEntryValues.size(); i++) {
            builder.addMenuItem(new BottomListDialog.BottomListMenuItem(mEntryValues.get(i), new OnClickPositionListener() {
                @Override
                public void onClickPosition(int position) {
                    mSelectedValue = mEntryValues.get(position);
                    if (mOnValueChangeListener != null) {
                        mOnValueChangeListener.onValueChanged(mSelectedValue);
                        refreshView();
                    }
                }
            }));
        }
        builder.show();
    }

    private void filterValuesOnShown() {
        List<String> tempValues = new ArrayList<>(mEntryValues);
        mTitleList.clear();
        for (int i = 0; i < tempValues.size(); i++) {
            String value = tempValues.get(i);
            String title = getTitlePattern(value);
            if (title != null) {
                mTitleList.add(title);
            }
        }
    }

    private String getTitlePattern(String value) {
        if (value.equals(IStoragePathViewListener.EXTERNAL_STORAGE)) {
            return mContext.getResources().getString(R.string.storage_path_external);
        }  else {
            return mContext.getResources().getString(R.string.storage_path_internal);
        }
    }
    //bv wuyonglin add for setting ui 20200923 end
}
