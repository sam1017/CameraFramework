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

import com.mediatek.camera.common.ICameraContext;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.mode.ICameraMode;
import com.mediatek.camera.common.mode.photo.PhotoMode;
import com.mediatek.camera.common.mode.video.VideoMode;
import com.mediatek.camera.common.relation.Relation;
import com.mediatek.camera.common.relation.StatusMonitor;
import com.mediatek.camera.common.setting.ISettingManager.SettingController;
import com.mediatek.camera.common.setting.SettingBase;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

public class DemoAsd extends SettingBase{

	private static final LogUtil.Tag TAG = new LogUtil.Tag(DemoAsd.class.getSimpleName());

	private DemoAsdSettingView mSettingView;
	private ISettingChangeRequester mSettingChangeRequester;
	private int[] mSessionValue = new int[]{0};
	private StatusMonitor.StatusResponder mTPIAsyncStateStatusResponder;
	private String mCurrentModeKey;

	@Override
	public void init(IApp app, ICameraContext cameraContext, SettingController settingController) {
		super.init(app,cameraContext,settingController);
		mTPIAsyncStateStatusResponder = mStatusMonitor.getStatusResponder(getKey());
		if (mSettingView == null) {
			mSettingView = new DemoAsdSettingView(getKey());
			mSettingView.setOnTpiAsyncClickListener(mSettingViewItemChangeListener);
		}
	}

	@Override
	public void unInit() {

	}

	@Override
	public void onModeOpened(String modeKey, ICameraMode.ModeType modeType) {
		super.onModeOpened(modeKey, modeType);
		mCurrentModeKey = modeKey;
		LogHelper.d(TAG, "onModeOpened modeKey " + modeKey);
		if (!isCurrentModeSupported()){
			List<String> supportValues = new ArrayList<>();
			supportValues.add(DemoAsdSettingView.VALUE_OFF);
			overrideValues(modeKey, DemoAsdSettingView.VALUE_OFF, supportValues);
		}

	}

	@Override
	public synchronized void onModeClosed(String modeKey) {
		super.onModeClosed(modeKey);
		mCurrentModeKey = null;
	}

	@Override
	public void addViewEntry() {
		LogHelper.d(TAG, "[addViewEntry] entryValue:" + getEntryValues());
		mAppUi.addSettingView(mSettingView);
	}

	private DemoAsdSettingView.OnTpiAsyncClickListener mSettingViewItemChangeListener =
			new DemoAsdSettingView.OnTpiAsyncClickListener() {
				@Override
				public void onTpiAsyncClicked(boolean checked) {
					String value = checked ? DemoAsdSettingView.VALUE_ON : DemoAsdSettingView.VALUE_OFF;
					LogHelper.d(TAG, "[onZsdClicked], value:" + value);
					setValue(value);
					mDataStore.setValue(getKey(), value, getStoreScope(), false);

					Relation relation = DemoAsdRestriction.getRestriction().getRelation(getValue(),true);
					mSettingController.postRestriction(relation);
					mSettingController.refreshViewEntry();
					mAppUi.refreshSettingView();

					mTPIAsyncStateStatusResponder.statusChanged(getKey(), value);
						mHandler.post(new Runnable() {
							@Override
							public void run() {
								mSettingChangeRequester.sendSettingChangeRequest();
							}
						});
				}
			};

	@Override
	public void removeViewEntry() {
		mAppUi.removeSettingView(mSettingView);
	}

	@Override
	public void refreshViewEntry() {
		mSettingView.setChecked(DemoAsdSettingView.VALUE_ON.equals(getValue()));
		mSettingView.setEnabled(getEntryValues().size()>1);
	}

	@Override
	public void postRestrictionAfterInitialized() {
		Relation relation = DemoAsdRestriction.getRestriction().getRelation(getValue(),true);
		mSettingController.postRestriction(relation);
	}

	public int[] getSessionValue() {
		if (DemoAsdSettingView.VALUE_ON.equalsIgnoreCase(getValue())) {
			mSessionValue = new int[]{1};
		} else {
			mSessionValue = new int[]{0};
		}
		return mSessionValue;
	}

	@Override
	public SettingType getSettingType() {
		return SettingType.PHOTO_AND_VIDEO;
	}

	@Override
	public String getKey() {
		return "key_demo_asd";
	}

	@Override
	public ICaptureRequestConfigure getCaptureRequestConfigure() {
		if (mSettingChangeRequester == null) {
			DemoAsdCaptureRequestConfig captureRequestConfig =
					new DemoAsdCaptureRequestConfig(this, mSettingDevice2Requester,mActivity);
			mSettingChangeRequester = captureRequestConfig;
		}
		return (DemoAsdCaptureRequestConfig) mSettingChangeRequester;
	}

	/**
	 * Get current camera id.
	 * @return The current camera id.
	 */
	protected int getCameraId() {
		int cameraId = Integer.parseInt(mSettingController.getCameraId());
		return cameraId;
	}

	/**
	 * Initialize zsd values when platform supported values is ready.
	 *
	 * @param platformSupportedValues The platform supported values.
	 * @param defaultValue The platform default values
	 */
	public void initializeValue(List<String> platformSupportedValues,
	                            String defaultValue) {
		LogHelper.d(TAG, "[initializeValue], platformSupportedValues:" + platformSupportedValues
				+ ", defaultValue:" + defaultValue);
		if (platformSupportedValues != null && platformSupportedValues.size() > 1) {
			setSupportedPlatformValues(platformSupportedValues);
			setSupportedEntryValues(platformSupportedValues);
			setEntryValues(platformSupportedValues);
			String value = mDataStore.getValue(getKey(), defaultValue, getStoreScope());
			setValue(value);
		}
	}

	@Override
	public void overrideValues(@Nonnull String headerKey, String currentValue, List<String> supportValues) {
		super.overrideValues(headerKey, currentValue, supportValues);
		setEntryValues(supportValues);
		refreshViewEntry();
	}


	public boolean isCurrentModeSupported() {
		if (PhotoMode.class.getName().equals(mCurrentModeKey)){
			return true;
		}

		if (VideoMode.class.getName().equals(mCurrentModeKey)){
			return true;
		}
		return false;
	}
}
