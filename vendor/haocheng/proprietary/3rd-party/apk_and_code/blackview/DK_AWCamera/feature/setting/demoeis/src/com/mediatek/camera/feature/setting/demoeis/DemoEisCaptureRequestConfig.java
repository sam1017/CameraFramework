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

import android.content.Context;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.view.Surface;

import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.loader.DeviceDescription;
import com.mediatek.camera.common.mode.CameraApiHelper;
import com.mediatek.camera.common.setting.ICameraSetting;
import com.mediatek.camera.common.setting.ISettingManager.SettingDevice2Requester;

import java.util.ArrayList;
import java.util.List;

public class DemoEisCaptureRequestConfig implements ICameraSetting.ICaptureRequestConfigure{

	private static final LogUtil.Tag TAG = new LogUtil.Tag(DemoEisCaptureRequestConfig.class.getSimpleName());

	private final DemoEis mDemoEis;
	private final SettingDevice2Requester mDevice2Requester;
	private Context mContext;
	private CaptureRequest.Key<int[]> mTpiEisKey;
	private List<String> platformSupportedValues = new ArrayList<>();


	/**
	 * White balance capture quest configure constructor.
	 *
	 * @param demoEis The instance of {@link DemoEis}.
	 * @param device2Requester The implementer of {@link SettingDevice2Requester}.
	 */
	public DemoEisCaptureRequestConfig(DemoEis demoEis, SettingDevice2Requester device2Requester, Context context) {
		mDemoEis = demoEis;
		mDevice2Requester = device2Requester;
		mContext = context;
	}

	@Override
	public void sendSettingChangeRequest() {
		mDevice2Requester.createAndChangeRepeatingRequest();
	}


	@Override
	public void setCameraCharacteristics(CameraCharacteristics characteristics) {
		platformSupportedValues.clear();
		platformSupportedValues.add(DemoEisSettingViewListener.OFF);

		DeviceDescription deviceDescription = CameraApiHelper.getDeviceSpec(mContext)
				.getDeviceDescriptionMap().get(String.valueOf(mDemoEis.getCameraId()));
		if (deviceDescription != null) {
			mTpiEisKey = deviceDescription.getKeyTpiEis();
			LogHelper.d(TAG, "[setCameraCharacteristics] mTpiEisKey = "+ mTpiEisKey);
		}
		if (mDemoEis.isCurrentModeSupported() && mTpiEisKey != null){
			platformSupportedValues.add(DemoEisSettingViewListener.ON_RECORD_ONLY);
			platformSupportedValues.add(DemoEisSettingViewListener.RECORD_AND_PREVIEW);
		}
		mDemoEis.initializeValue(platformSupportedValues, DemoEisSettingViewListener.OFF);

	}

	@Override
	public void configCaptureRequest(CaptureRequest.Builder captureBuilder) {
		LogHelper.d(TAG, "[configCaptureRequest] isCurrentModeSupported = "
				+ mDemoEis.isCurrentModeSupported()
				+ " mTpiEisKey = " + mTpiEisKey
				+ " SessionValue = " + mDemoEis.getSessionValue()[0]);
		
		if (!mDemoEis.isCurrentModeSupported()){
			return;
		}

		if (mTpiEisKey != null){
			captureBuilder.set(mTpiEisKey, mDemoEis.getSessionValue());
		}

	}

	@Override
	public void configSessionSurface(List<Surface> surfaces) {

	}

	@Override
	public Surface configRawSurface() {
		return null;
	}

	@Override
	public CameraCaptureSession.CaptureCallback getRepeatingCaptureCallback() {
		return mPreviewCallback;
	}

	private CameraCaptureSession.CaptureCallback mPreviewCallback
			= new CameraCaptureSession.CaptureCallback() {

	};

}
