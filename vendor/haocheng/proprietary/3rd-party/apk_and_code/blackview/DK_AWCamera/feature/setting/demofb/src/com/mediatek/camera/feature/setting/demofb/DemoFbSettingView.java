/*
 *    Copyright Statement:
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
 *     ON_RECORD_ONLY AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
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

package com.mediatek.camera.feature.setting.demofb;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.preference.PreferenceFragment;

import com.mediatek.camera.R;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.preference.Preference;
import com.mediatek.camera.common.setting.ICameraSettingView;
import com.mediatek.camera.feature.setting.demofb.DemoFbSettingViewListener.OnItemClickListener;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is for tpi fb feature setting view.
 */

public class DemoFbSettingView implements ICameraSettingView {
	private static final LogUtil.Tag TAG = new LogUtil.Tag(DemoFbSettingView.class.getSimpleName());
	private Activity mContext;
	private DemoFbSelector mTpiFbSelector;
	private Preference mTpiSelectorPreference;
	private String mSelectedValue;
	private boolean mEnabled;
	private List<String> mEntryValues = new ArrayList<>();
	private DemoFbSettingViewListener.OnValueChangeListener mOnValueChangeListener;

	@Override
	public void loadView(PreferenceFragment fragment) {
		fragment.addPreferencesFromResource(R.xml.demo_fb_preference);
		mContext = fragment.getActivity();

		if (mTpiFbSelector == null) {
			mTpiFbSelector = new DemoFbSelector();
			mTpiFbSelector.setOnItemClickListener(mOnItemClickListener);
		}

		mTpiSelectorPreference = (Preference) fragment
				.findPreference(DemoFbSettingViewListener.KEY_DEMO_FB);
		mTpiSelectorPreference.setRootPreference(fragment.getPreferenceScreen());
		mTpiSelectorPreference.setId(R.id.demo_fb_setting);
		mTpiSelectorPreference.setContentDescription(fragment.getActivity().getResources()
				.getString(R.string.demo_fb_content_description));
		mTpiSelectorPreference.setSummary(getSummary());
		mTpiSelectorPreference.setOnPreferenceClickListener(
				new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(android.preference.Preference preference) {
						mTpiFbSelector.setValue(mSelectedValue);
						mTpiFbSelector.setEntryValues(mEntryValues);

						FragmentTransaction transaction = mContext.getFragmentManager()
								.beginTransaction();
						transaction.addToBackStack(null);
						transaction.replace(R.id.setting_container,
								mTpiFbSelector, "tpi_fb_selector").commit();
						return true;
					}
				});
		mTpiSelectorPreference.setEnabled(mEnabled);
	}

	@Override
	public void refreshView() {
		if (mTpiSelectorPreference != null) {
			LogHelper.d(TAG, "[refreshView]");
			mTpiSelectorPreference.setSummary(getSummary());
			mTpiSelectorPreference.setEnabled(mEnabled);
		}
	}

	@Override
	public void unloadView() {

	}

	@Override
	public boolean isEnabled() {
		return mEnabled;
	}

	private String getSummary() {
		if (DemoFbSettingViewListener.ON.equals(mSelectedValue)) {
			return mContext.getString(R.string.demo_fb_on);
		}
		if (DemoFbSettingViewListener.OFF.equals(mSelectedValue)) {
			return mContext.getString(R.string.demo_fb_off);
		}
		return mContext.getString(R.string.demo_fb_on_and_chang_roi);

	}

	@Override
	public void setEnabled(boolean enabled) {
		mEnabled = enabled;
	}

	/**
	 * Set listener to listen the changed tpi fb value.
	 * @param listener The instance of {@link DemoFbSettingViewListener.OnValueChangeListener}.
	 */
	public void setOnValueChangeListener(DemoFbSettingViewListener.OnValueChangeListener listener) {
		mOnValueChangeListener = listener;
	}

	/**
	 * Set the default selected value.
	 * @param value The default selected value.
	 */
	public void setValue(String value) {
		mSelectedValue = value;
	}

	private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(String value) {
			mSelectedValue = value;
			if (mOnValueChangeListener != null) {
				mOnValueChangeListener.onValueChanged(value);
			}
		}
	};

	/**
	 * Set the tpi fb supported.
	 * @param entryValues The tpi fb supported.
	 */
	public void setEntryValues(List<String> entryValues) {
		mEntryValues = entryValues;
	}
}
