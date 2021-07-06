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

package com.mediatek.camera.feature.setting.watermark;

import android.annotation.TargetApi;

import android.content.Context; 
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.os.Build;
import android.view.Surface;

import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.setting.ICameraSetting;
import com.mediatek.camera.common.setting.ISettingManager.SettingDevice2Requester;
import com.mediatek.camera.R; 
import com.mediatek.camera.Config;
import java.util.ArrayList;
import java.util.List;


/**
 * This is for EIS capture flow in camera API2.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class WaterMarkCaptureRequestConfig implements ICameraSetting.ICaptureRequestConfigure {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(
        WaterMarkCaptureRequestConfig.class.getSimpleName());
    private static final String WATERMARK_OFF = "off";
    private static final String WATERMARK_ON = "on";
    private boolean mIsSupported = false;
    private SettingDevice2Requester mDevice2Requester;
    private WaterMark mWaterMark;
    private Context mContext;
    CaptureRequest.Key<int[]> mWaterMarkValue =  new CaptureRequest.Key<int[]>("com.mediatek.control.capture.hctHalWatermakr", int[].class);
    private CameraCharacteristics mCameraCharacteristics;
    private static final int[] HAL_WATERMARK_VALUE = new int[]{0,0,0,0,0,0} ;

    /**
     * EIS capture request configure constructor.
     * @param watermark The instance of {@link watermark}.
     * @param device2Requester The implementer of {@link SettingDevice2Requester}.
     */
    public WaterMarkCaptureRequestConfig(WaterMark watermark, SettingDevice2Requester device2Requester, 
            Context context) {
        mWaterMark = watermark;
        mDevice2Requester = device2Requester;
        mContext = context;
    }

    @Override
    public void setCameraCharacteristics(CameraCharacteristics characteristics) {
        mCameraCharacteristics = characteristics;
        updateSupportedValues();
    }

    @Override
    public void configCaptureRequest(CaptureRequest.Builder captureBuilder) {

        if (captureBuilder == null||mCameraCharacteristics==null) {
            LogHelper.d(TAG, "[configCaptureRequest] captureBuilder or mCharacteristics is null");
            return;
        }

        //*/ hct.huangfei, 20201103. modify watermark.
        String pictureSize = mWaterMark.getPictureSize();
        if(pictureSize == null){
            LogHelper.d(TAG, "[configCaptureRequest] pictureSize is null");
            return;
        }
        //*/

        if(Config.hctHalWaterMarkSupport() && !mWaterMark.isAiWorksBokehMode()){    //bv wuyonglin modify for AiWorksBokeh water logo 20200827
            if(WATERMARK_ON.equals(mWaterMark.getValue())){
                int cameraRotation = mWaterMark.getJpegRotationFromDeviceSpec();
                int currentCameraId = Integer.parseInt(mWaterMark.getCameraId());

                //*/ hct.huangfei, 20201103. modify watermark.
                //String[] pictureSizes = mWaterMark.getPictureSize().split("x");
                String[] pictureSizes = pictureSize.split("x");
                //*/

                int mCaptureWidth = Integer.parseInt(pictureSizes[0]);
                int mCaptureHeight = Integer.parseInt(pictureSizes[1]);
                int mirror = mWaterMark.getMirrorValue();
                HAL_WATERMARK_VALUE[0] = 1;
                HAL_WATERMARK_VALUE[1] = cameraRotation;
                HAL_WATERMARK_VALUE[2] = currentCameraId;
                HAL_WATERMARK_VALUE[3] = mCaptureWidth;
                HAL_WATERMARK_VALUE[4] = mCaptureHeight;
                HAL_WATERMARK_VALUE[5] = mirror;
            }else{
                HAL_WATERMARK_VALUE[0] = 0;
            }
            captureBuilder.set(mWaterMarkValue, HAL_WATERMARK_VALUE);
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
        return null;
    }

    @Override
    public void sendSettingChangeRequest() {
        mDevice2Requester.createAndChangeRepeatingRequest();
    }

    private void updateSupportedValues() {
        mWaterMark.updateSupportedValues();
    }
}
