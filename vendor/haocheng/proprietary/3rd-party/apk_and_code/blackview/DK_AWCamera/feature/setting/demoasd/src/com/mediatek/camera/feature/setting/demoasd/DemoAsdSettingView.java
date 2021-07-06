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

package com.mediatek.camera.feature.setting.demoasd;

import android.preference.PreferenceFragment;

import com.mediatek.camera.R;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.preference.Preference;
import com.mediatek.camera.common.preference.SwitchPreference;
import com.mediatek.camera.common.setting.ICameraSettingView;

/**
 * This class is for tpi async feature setting view.
 */

public class DemoAsdSettingView implements ICameraSettingView {
	private static final LogUtil.Tag TAG = new LogUtil.Tag(DemoAsdSettingView.class.getSimpleName());
	private SwitchPreference mTpiSwitchPreference;
	private boolean mEnabled;
	private boolean mChecked;
	private String mKey;
	private OnTpiAsyncClickListener mOnTpiAsyncClickListener;
	public static final String VALUE_ON = "on";
	public static final String VALUE_OFF = "off";

	/**
	 * Listener to listen tpi async is clicked.
	 */
	public interface OnTpiAsyncClickListener {
		/**
		 * Callback when tpi async item is clicked by user.
		 *
		 * @param checked True means tpi async is opened, false means zsd is closed.
		 */
		void onTpiAsyncClicked(boolean checked);
	}

	public DemoAsdSettingView(String  key) {
		mKey = key;
	}

	@Override
	public void loadView(PreferenceFragment fragment) {
		fragment.addPreferencesFromResource(R.xml.demo_asd_preference);
		mTpiSwitchPreference = (SwitchPreference) fragment
				.findPreference(mKey);
		mTpiSwitchPreference.setRootPreference(fragment.getPreferenceScreen());
		mTpiSwitchPreference.setId(R.id.demo_asd_setting);
		mTpiSwitchPreference.setContentDescription(fragment.getActivity().getResources()
				.getString(R.string.demo_asd_content_description));
		mTpiSwitchPreference.setOnPreferenceChangeListener(
				new Preference.OnPreferenceChangeListener() {

					@Override
					public boolean onPreferenceChange(android.preference.Preference preference, Object newValue) {
						boolean checked = (Boolean) newValue;
						mChecked = checked;
						mOnTpiAsyncClickListener.onTpiAsyncClicked(checked);
						return true;
					}
				});
		mTpiSwitchPreference.setEnabled(mEnabled);
	}

	@Override
	public void refreshView() {
		if (mTpiSwitchPreference != null) {
			LogHelper.d(TAG, "[refreshView]");
			mTpiSwitchPreference.setChecked(mChecked);
			mTpiSwitchPreference.setEnabled(mEnabled);
		}
	}

	@Override
	public void unloadView() {

	}

	@Override
	public boolean isEnabled() {
		return mEnabled;
	}

	@Override
	public void setEnabled(boolean enabled) {
		mEnabled = enabled;
	}

	/**
	 * Set listener to listen the changed tpi async value.
	 * @param listener The instance of {@link OnTpiAsyncClickListener}.
	 */
	public void setOnTpiAsyncClickListener(OnTpiAsyncClickListener listener) {
		mOnTpiAsyncClickListener = listener;
	}

	/**
	 * Set the default selected value.
	 * @param checked The default selected value.
	 */
	public void setChecked(boolean checked) {
		mChecked = checked;
	}

}
