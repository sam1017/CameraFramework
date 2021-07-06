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

package com.mediatek.camera.feature.setting.demoeis;

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

/**
 * This class is for tpi eis feature setting view.
 */

public class DemoEisSettingView implements ICameraSettingView {
	private static final LogUtil.Tag TAG = new LogUtil.Tag(DemoEisSettingView.class.getSimpleName());
	private Activity mContext;
	private DemoEisSelector mDemoEisSelector;
	private Preference mTpiSelectorPreference;
	private String mSelectedValue;
	private boolean mEnabled;
	private List<String> mEntryValues = new ArrayList<>();
	private DemoEisSettingViewListener.OnValueChangeListener mOnValueChangeListener;

	@Override
	public void loadView(PreferenceFragment fragment) {
		fragment.addPreferencesFromResource(R.xml.demo_eis_preference);
		mContext = fragment.getActivity();

		if (mDemoEisSelector == null) {
			mDemoEisSelector = new DemoEisSelector();
			mDemoEisSelector.setOnItemClickListener(mOnItemClickListener);
		}

		mTpiSelectorPreference = (Preference) fragment
				.findPreference(DemoEisSettingViewListener.KEY_DEMO_EIS);
		mTpiSelectorPreference.setRootPreference(fragment.getPreferenceScreen());
		mTpiSelectorPreference.setId(R.id.demo_eis_setting);
		mTpiSelectorPreference.setContentDescription(fragment.getActivity().getResources()
				.getString(R.string.demo_eis_content_description));
		mTpiSelectorPreference.setSummary(getSummary());
		mTpiSelectorPreference.setOnPreferenceClickListener(
				new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(android.preference.Preference preference) {
						mDemoEisSelector.setValue(mSelectedValue);
						mDemoEisSelector.setEntryValues(mEntryValues);

						FragmentTransaction transaction = mContext.getFragmentManager()
								.beginTransaction();
						transaction.addToBackStack(null);
						transaction.replace(R.id.setting_container,
								mDemoEisSelector, "tpi_eis_selector").commit();
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
		if (DemoEisSettingViewListener.ON_RECORD_ONLY.equals(mSelectedValue)) {
			return mContext.getString(R.string.demo_eis_on);
		}
		if (DemoEisSettingViewListener.OFF.equals(mSelectedValue)) {
			return mContext.getString(R.string.demo_eis_off);
		}
		return mContext.getString(R.string.demo_eis_on_and_chang_roi);

	}

	@Override
	public void setEnabled(boolean enabled) {
		mEnabled = enabled;
	}

	/**
	 * Set listener to listen the changed tpi eis value.
	 * @param listener The instance of {@link DemoEisSettingViewListener.OnValueChangeListener}.
	 */
	public void setOnValueChangeListener(DemoEisSettingViewListener.OnValueChangeListener listener) {
		mOnValueChangeListener = listener;
	}

	/**
	 * Set the default selected value.
	 * @param value The default selected value.
	 */
	public void setValue(String value) {
		mSelectedValue = value;
	}

	private DemoEisSettingViewListener.OnItemClickListener mOnItemClickListener = new DemoEisSettingViewListener.OnItemClickListener() {
		@Override
		public void onItemClick(String value) {
			mSelectedValue = value;
			if (mOnValueChangeListener != null) {
				mOnValueChangeListener.onValueChanged(value);
			}
		}
	};

	/**
	 * Set the tpi eis supported.
	 * @param entryValues The tpi eis supported.
	 */
	public void setEntryValues(List<String> entryValues) {
		mEntryValues = entryValues;
	}
}
