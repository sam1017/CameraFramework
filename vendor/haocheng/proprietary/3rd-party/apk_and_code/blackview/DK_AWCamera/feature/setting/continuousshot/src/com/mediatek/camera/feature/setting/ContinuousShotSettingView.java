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
package com.mediatek.camera.feature.setting;

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
//bv wuyonglin add for setting ui 20200923 start
import com.mediatek.camera.common.widget.BottomListDialog;
import com.mediatek.camera.common.widget.OnClickPositionListener;
//bv wuyonglin add for setting ui 20200923 end
//bv wuyonglin add for hd shot 20201013 start
import com.mediatek.camera.common.relation.DataStore;
import com.mediatek.camera.CameraActivity;
//bv wuyonglin add for hd shot 20201013 end

/**
 * This class is for continuous shot feature setting view.
 */

public class ContinuousShotSettingView  implements ICameraSettingView {
    private static final LogUtil.Tag TAG =
            new LogUtil.Tag(ContinuousShotSettingView.class.getSimpleName());

    private String mSelectedValue;
    private List<String> mEntryValues = new ArrayList<>();
    private IContinuousShotViewListener.OnValueChangeListener mOnValueChangeListener; 
    private Preference mContinuousShotPreference;
    //bv wuyonglin add for setting ui 20200923 start
    //private ContinuousShotSelector mContinuousShotSelector;
    private List<String> mTitleList = new ArrayList<>();
    private BottomListDialog.Builder builder;
    private BottomListDialog mBottomListDialog;
    //bv wuyonglin add for setting ui 20200923 end
    private Activity mContext;
    private boolean mEnabled;
    //bv wuyonglin add for hd shot 20201013 start
    private ContinuousShotBase mContinuousShotBase;

    public ContinuousShotSettingView(ContinuousShotBase continuousShotBase) {
        mContinuousShotBase = continuousShotBase;
    }
    //bv wuyonglin add for hd shot 20201013 end

    @Override
    public void loadView(PreferenceFragment fragment) {
        fragment.addPreferencesFromResource(R.xml.continuous_shot_preference);
        mContext = fragment.getActivity();
        //bv wuyonglin delete for setting ui 20200923 start
        /*if (mContinuousShotSelector == null) {
            mContinuousShotSelector = new ContinuousShotSelector();
            mContinuousShotSelector.setOnItemClickListener(mOnItemClickListener);
        }*/
        //bv wuyonglin delete for setting ui 20200923 end
        mContinuousShotPreference = (Preference) fragment
                .findPreference(IContinuousShotViewListener.KEY_CONTINUOUS_SHOT_NUM);
        mContinuousShotPreference.setRootPreference(fragment.getPreferenceScreen());
        mContinuousShotPreference.setId(R.id.continuous_shot_setting);
        mContinuousShotPreference.setContentDescription(fragment.getActivity().getResources()
                .getString(R.string.continuous_shot_content_description));
        mContinuousShotPreference.setSummary(getSummary());
        mContinuousShotPreference.setOnPreferenceClickListener(
                new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(android.preference.Preference preference) {
                //bv wuyonglin add for setting ui 20200923 start
                /*mContinuousShotSelector.setValue(mSelectedValue);
                mContinuousShotSelector.setEntryValues(mEntryValues);

                FragmentTransaction transaction = mContext.getFragmentManager()
                        .beginTransaction();
                transaction.addToBackStack(null);
                transaction.replace(R.id.setting_container,
                        mContinuousShotSelector, "continuous_shot_selector").commit();*/
		if (mBottomListDialog != null && mBottomListDialog.isShowing()) {
                    return true;
		}
                showBottomListDialog();
                //bv wuyonglin add for setting ui 20200923 end
                return true;
            }
        });
        //bv wuyonglin add for hd shot 20201013 start
        DataStore mDataStore = ((CameraActivity) fragment.getActivity()).getCameraContext().getDataStore();
        String mHdrValue = mDataStore.getValue("key_hdr", "off", mDataStore.getGlobalScope());
        String mAisStatus = mDataStore.getValue("key_ais", "off", mDataStore.getCameraScope(mContinuousShotBase.getCameraId()));
        String mFlashStatus = mDataStore.getValue("key_flash", mContext.getString(R.string.flash_default_value), "_preferences_0");
        LogHelper.d(TAG, "[loadView], mFlashStatus ="+mFlashStatus+" mHdrValue ="+mHdrValue+" mAisStatus ="+mAisStatus+" mEnabled ="+mEnabled+" mContinuousShotBase.getCurrentMode() ="+mContinuousShotBase.getCurrentMode());
        if (mContinuousShotBase.getCurrentMode() != null && mContinuousShotBase.getCurrentMode().equals("Photo") && 1 != mContinuousShotBase.getCameraId()) {
            LogHelper.d(TAG, "[loadView], setDisableView mEnabled ="+mEnabled);
            mContinuousShotPreference.setDisableView(true);
        }
        //bv wuyonglin add for hd shot 20201013 end
        mContinuousShotPreference.setEnabled(mEnabled);
        //bv wuyonglin add for setting ui 20200923 start
        filterValuesOnShown();
        //bv wuyonglin add for setting ui 20200923 end
    }

    @Override
    public void refreshView() {
        if (mContinuousShotPreference != null) {
            LogHelper.d(TAG, "[refreshView]");
            mContinuousShotPreference.setSummary(getSummary());
            mContinuousShotPreference.setEnabled(mEnabled);
        }
    }

    @Override
    public void unloadView() {
        //bv wuyonglin add for setting ui 20200923 start
        LogHelper.d(TAG, "[unloadView]");
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
     * Set listener to listen the changed continuous shot value.
     * @param listener The instance of {@link IContinuousShotViewListener.OnValueChangeListener}.
     */
    public void setOnValueChangeListener(IContinuousShotViewListener.OnValueChangeListener listener) {
        mOnValueChangeListener = listener;
    }

    /**
     * Set the default selected value.
     * @param value The default selected value.
     */
    public void setValue(String value) {
        mSelectedValue = value;
        //bv wuyonglin delete for setting ui 20200923 start
        //if(mContinuousShotSelector!=null){
        //    mContinuousShotSelector.setValue(value);
        //}
        //bv wuyonglin delete for setting ui 20200923 end

    }


    public String getValue(){
        return mSelectedValue;
    }

    /**
     * Set the continuous shot supported.
     * @param entryValues The continuous shot supported.
     */
    public void setEntryValues(List<String> entryValues) {
        mEntryValues = entryValues;
    }

    private IContinuousShotViewListener.OnItemClickListener mOnItemClickListener
            = new IContinuousShotViewListener.OnItemClickListener() {
        @Override
        public void onItemClick(String value) {
            mSelectedValue = value;
            if (mOnValueChangeListener != null) {
                mOnValueChangeListener.onValueChanged(value);
            }
        }
    };

    private String getSummary() {
        if (IContinuousShotViewListener.CONTINUOUS_SHOT_NUM10.equals(mSelectedValue)) {
            return mContext.getString(R.string.continuous_shot_num_10);   
        } else{
            return mContext.getString(R.string.continuous_shot_num_20);
        }
    }

    //bv wuyonglin add for setting ui 20200923 start
    public void showBottomListDialog() {
        if (mEntryValues.size() == 0) {
            return;
        }

        builder = new BottomListDialog.Builder(mContext, mContext.getResources().getString(R.string.continuous_shot_content_description), mSelectedValue, mTitleList, null);
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
        mBottomListDialog = builder.show();
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
        if (value.equals(IContinuousShotViewListener.CONTINUOUS_SHOT_NUM10)) {
            return mContext.getString(R.string.continuous_shot_num_10);
        } else {
            return mContext.getString(R.string.continuous_shot_num_20);
        }
    }
    //bv wuyonglin add for setting ui 20200923 end
}
