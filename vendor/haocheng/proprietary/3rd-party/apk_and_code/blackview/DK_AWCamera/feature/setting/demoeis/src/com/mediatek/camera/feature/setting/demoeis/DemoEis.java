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

import com.mediatek.camera.common.ICameraContext;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.mode.ICameraMode;
import com.mediatek.camera.common.mode.photo.PhotoMode;
import com.mediatek.camera.common.mode.video.VideoMode;
import com.mediatek.camera.common.relation.Relation;
import com.mediatek.camera.common.setting.ISettingManager.SettingController;
import com.mediatek.camera.common.setting.SettingBase;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

public class DemoEis extends SettingBase{

	private static final LogUtil.Tag TAG = new LogUtil.Tag(DemoEis.class.getSimpleName());

	private DemoEisSettingView mSettingView;
	private ISettingChangeRequester mSettingChangeRequester;
	private int[] mSessionValue = new int[]{0};
	private String mCurrentModeKey;

	@Override
	public void init(IApp app, ICameraContext cameraContext, SettingController settingController) {
		super.init(app,cameraContext,settingController);
		if (mSettingView == null) {
			mSettingView = new DemoEisSettingView();
			mSettingView.setOnValueChangeListener(mSettingViewItemChangeListener);
		}
	}

	@Override
	public void unInit() {

	}

	@Override
	public void addViewEntry() {
		LogHelper.d(TAG, "[addViewEntry] entryValue:" + getEntryValues());
		mAppUi.addSettingView(mSettingView);
	}

	@Override
	public void onModeOpened(String modeKey, ICameraMode.ModeType modeType) {
		super.onModeOpened(modeKey, modeType);
		mCurrentModeKey = modeKey;
		LogHelper.d(TAG, "onModeOpened modeKey " + modeKey);
		if (!isCurrentModeSupported()) {
			List<String> supportValues = new ArrayList<>();
			supportValues.add(DemoEisSettingViewListener.OFF);
			overrideValues(modeKey, DemoEisSettingViewListener.OFF, supportValues);
		}
	}

	private DemoEisSettingViewListener.OnValueChangeListener mSettingViewItemChangeListener =
			new DemoEisSettingViewListener.OnValueChangeListener() {
				@Override
				public void onValueChanged(String value) {
					LogHelper.d(TAG, "[onValueChanged], value:" + value);
					if (!getValue().equals(value)) {
						setValue(value);

						mDataStore.setValue(getKey(), value, getStoreScope(), false);

						Relation relation = DemoEisRestriction.getRestriction().getRelation(value, true);
						mSettingController.postRestriction(relation);
						mSettingController.refreshViewEntry();
						mAppUi.refreshSettingView();

						mSettingChangeRequester.sendSettingChangeRequest();
					}
				}
			};

	@Override
	public void removeViewEntry() {
		mAppUi.removeSettingView(mSettingView);
	}

	@Override
	public void refreshViewEntry() {
		if (mSettingView != null) {
			mSettingView.setEntryValues(getEntryValues());
			mSettingView.setValue(getValue());
			mSettingView.setEnabled(getEntryValues().size() > 1);
		}
	}

	@Override
	public void overrideValues(@Nonnull String headerKey, String currentValue, List<String> supportValues) {
		super.overrideValues(headerKey, currentValue, supportValues);
		setEntryValues(getEntryValues());
		refreshViewEntry();
	}

	@Override
	public void postRestrictionAfterInitialized() {
		Relation relation = DemoEisRestriction.getRestriction().getRelation(getValue(), true);
		mSettingController.postRestriction(relation);
	}

	@Override
	public SettingType getSettingType() {
		return SettingType.PHOTO_AND_VIDEO;
	}

	@Override
	public String getKey() {
		return DemoEisSettingViewListener.KEY_DEMO_EIS;
	}

	@Override
	public ICaptureRequestConfigure getCaptureRequestConfigure() {
		if (mSettingChangeRequester == null) {
			DemoEisCaptureRequestConfig captureRequestConfig =
					new DemoEisCaptureRequestConfig(this, mSettingDevice2Requester,mActivity);
			mSettingChangeRequester = captureRequestConfig;
		}
		return (DemoEisCaptureRequestConfig) mSettingChangeRequester;
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

	public boolean isCurrentModeSupported() {
		if (PhotoMode.class.getName().equals(mCurrentModeKey)){
			return true;
		}

		if (VideoMode.class.getName().equals(mCurrentModeKey)){
			return true;
		}
		return false;
	}


	public int[] getSessionValue(){
		if (DemoEisSettingViewListener.OFF.equals(getValue())){
			mSessionValue = new int[]{0};
		}else if (DemoEisSettingViewListener.ON_RECORD_ONLY.equals(getValue())){
			mSessionValue = new int[]{1};
		}else if (DemoEisSettingViewListener.RECORD_AND_PREVIEW.equals(getValue())){
			mSessionValue = new int[]{2};
		}
		return mSessionValue;
	}
}
