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
package com.mediatek.camera.feature.setting.picturesize;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.preference.PreferenceFragment;

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

/**
 * Picture size setting view.
 */
public class PictureSizeSettingView implements ICameraSettingView,
        PictureSizeSelector.OnItemClickListener {
    private static final LogUtil.Tag TAG =
            new LogUtil.Tag(PictureSizeSettingView.class.getSimpleName());

    private Activity mActivity;
    private Preference mPref;
    private OnValueChangeListener mListener;
    private String mKey;
    private String mSelectedValue;
    private List<String> mEntryValues = new ArrayList<>();
    private String mSummary;
    //bv wuyonglin add for setting ui 20200923 start
    //private PictureSizeSelector mSizeSelector;
    private List<String> mTitleList = new ArrayList<>();
    private List<String> mSummaryList = new ArrayList<>();
    private BottomListDialog.Builder builder;
    private BottomListDialog mBottomListDialog;
    //bv wuyonglin add for setting ui 20200923 end

    private boolean mEnabled;

    /**
     * Listener to listen picture size value changed.
     */
    public interface OnValueChangeListener {
        /**
         * Callback when picture size value changed.
         *
         * @param value The changed picture size, such as "1920x1080".
         */
        void onValueChanged(String value);
    }

    /**
     * Picture size setting view constructor.
     *
     * @param key The key of picture size.
     */
    public PictureSizeSettingView(String key) {
        mKey = key;
    }

    @Override
    public void loadView(PreferenceFragment fragment) {
        LogHelper.d(TAG, "[loadView]");
        mActivity = fragment.getActivity();

        //bv wuyonglin add for setting ui 20200923 start
        prepareValuesOnShown();
        //bv wuyonglin add for setting ui 20200923 end

        //bv wuyonglin delete for setting ui 20200923 start
        /*if (mSizeSelector == null) {
            mSizeSelector = new PictureSizeSelector();
            mSizeSelector.setOnItemClickListener(this);
        }*/
        //bv wuyonglin delete for setting ui 20200923 end

        fragment.addPreferencesFromResource(R.xml.picturesize_preference);
        mPref = (Preference) fragment.findPreference(mKey);
        mPref.setRootPreference(fragment.getPreferenceScreen());
        mPref.setId(R.id.picture_size_setting);
        mPref.setContentDescription(mActivity.getResources()
                .getString(R.string.picture_size_content_description));
        mPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(android.preference.Preference preference) {
                //bv wuyonglin add for setting ui 20200923 start
                /*mSizeSelector.setValue(mSelectedValue);
                mSizeSelector.setEntryValues(mEntryValues);

                FragmentTransaction transaction = mActivity.getFragmentManager()
                        .beginTransaction();
                transaction.addToBackStack(null);
                transaction.replace(R.id.setting_container,
                        mSizeSelector, "picture_size_selector").commit();*/
		if (mBottomListDialog != null && mBottomListDialog.isShowing()) {
                    return true;
		}
                showBottomListDialog();
                //bv wuyonglin add for setting ui 20200923 end
                return true;
            }

        });
        mPref.setEnabled(mEnabled);
        if (mSelectedValue != null) {
            mSummary = PictureSizeHelper.getPixelsAndRatio(mSelectedValue);
        }
    }

    @Override
    public void refreshView() {
        if (mPref != null) {
            LogHelper.d(TAG, "[refreshView]");
            mPref.setSummary(mSummary);
            mPref.setEnabled(mEnabled);
        }
    }

    @Override
    public void unloadView() {
        LogHelper.d(TAG, "[unloadView]");
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
     * Set listener to listen the changed picture size value.
     *
     * @param listener The instance of {@link OnValueChangeListener}.
     */
    public void setOnValueChangeListener(OnValueChangeListener listener) {
        mListener = listener;
    }

    /**
     * Set the default selected value.
     *
     * @param value The default selected value.
     */
    public void setValue(String value) {
        mSelectedValue = value;
    }

    /**
     * Set the picture sizes supported.
     *
     * @param entryValues The picture sizes supported.
     */
    public void setEntryValues(List<String> entryValues) {
        mEntryValues = entryValues;
        prepareValuesOnShown();
    }

    @Override
    public void onItemClick(String value) {
        mSelectedValue = value;
        mSummary = PictureSizeHelper.getPixelsAndRatio(value);
        if (mListener != null) {
            mListener.onValueChanged(value);
        }
    }

    //bv wuyonglin add for setting ui 20200923 start
    public void showBottomListDialog() {
        if (mEntryValues.size() == 0) {
            return;
        }
        LogHelper.i(TAG, "[onPreferenceClick] to showBottomListDialog mTitleList ="+mTitleList+" mEntryValues ="+mEntryValues);
        builder = new BottomListDialog.Builder(mActivity, mActivity.getResources().getString(R.string.picture_size_content_description), mSelectedValue, mTitleList, mSummaryList);
        for (int i = 0; i < mEntryValues.size(); i++) {
            builder.addMenuItem(new BottomListDialog.BottomListMenuItem(mEntryValues.get(i), new OnClickPositionListener() {
                @Override
                public void onClickPosition(int position) {
                    mSelectedValue = mEntryValues.get(position);
                    mSummary = PictureSizeHelper.getPixelsAndRatio(mSelectedValue);
                    if (mListener != null) {
                        mListener.onValueChanged(mSelectedValue);
                        refreshView();
                    }
                }
            }));
        }
        mBottomListDialog = builder.show();
    }

    private void prepareValuesOnShown() {
        List<String> tempValues = new ArrayList<>(mEntryValues);
        mTitleList.clear();
        mSummaryList.clear();
        for (int i = 0; i < tempValues.size(); i++) {
            String value = tempValues.get(i);
            String title = PictureSizeHelper.getPixelsAndRatio(value);
            if (title != null) {
                mTitleList.add(title);
                mSummaryList.add(value.replace("x", " x "));
            }
        }
    }
    //bv wuyonglin add for setting ui 20200923 end
}
