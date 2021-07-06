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
 *     MediaTek Inc. (C) 2019. All rights reserved.
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
package com.mediatek.camera.feature.mode.hdr;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.provider.MediaStore;

import com.mediatek.camera.common.IAppUi;
import com.mediatek.camera.common.IAppUi.ModeItem;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.device.CameraDeviceManagerFactory;
import com.mediatek.camera.common.loader.FeatureEntryBase;
import com.mediatek.camera.common.mode.CameraApiHelper;
import com.mediatek.camera.common.mode.ICameraMode;
import com.mediatek.camera.R;
import com.mediatek.camera.common.utils.CameraUtil;
import com.mediatek.camera.portability.SystemProperties;


/**
 * Demo mode entry
 */

public class HdrModeEntry extends FeatureEntryBase {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(HdrModeEntry.class
            .getSimpleName());
    private static final int MTK_CAMERA_APP_VERSION_FOUR = 4;
    private static final int MTK_CAMERA_APP_VERSION_FIVE = 5;
    private static final int MTK_CAMERA_APP_VERSION_SIX = 6;

    /**
     * create an entry.
     *
     * @param context   current activity.
     * @param resources current resources.
     */
    public HdrModeEntry(Context context, Resources resources) {
        super(context, resources);
    }

    @Override
    public boolean isSupport(CameraDeviceManagerFactory.CameraApi currentCameraApi,
                             Activity activity) {
        //*/ hct.huangfei, 20201202.mode disable.
        if (isThirdPartyIntent(activity) || isSecureCameraIntent(activity)){
            return false;
        }
        //*/
        Intent intent = activity.getIntent();
        String action = intent.getAction();
        boolean support = !MediaStore.ACTION_IMAGE_CAPTURE.equals(action)
                && (CameraUtil.getAppVersionLevel() == MTK_CAMERA_APP_VERSION_FOUR
                || CameraUtil.getAppVersionLevel() == MTK_CAMERA_APP_VERSION_FIVE
                || CameraUtil.getAppVersionLevel() == MTK_CAMERA_APP_VERSION_SIX);
        //bv wuyonglin add for add hdr support 20200604 start
        support = false;
        //bv wuyonglin add for add hdr support 20200604 end
        LogHelper.i(TAG, "[isSupport] : " + support);
        return support;
    }

    @Override
    public String getFeatureEntryName() {
        return HdrModeEntry.class.getName();
    }

    @Override
    public Class getType() {
        return ICameraMode.class;
    }

    @Override
    public Object createInstance() {
        return new HdrMode();
    }

    @Override
    public IAppUi.ModeItem getModeItem() {
        ModeItem modeItem = new ModeItem();
        modeItem.mType = "Picture";
        modeItem.mPriority = 85;
        modeItem.mClassName = getFeatureEntryName();
        modeItem.mModeSelectedIcon = mResources.getDrawable(R.drawable.ic_hdr_mode_selected);
        modeItem.mModeUnselectedIcon = mResources.getDrawable(R.drawable.ic_hdr_mode_unselected);
        //*/ hct.huangfei, 20201021. add  mode.
        modeItem.mTitle = mContext.getResources().getString(R.string.hdr_mode_title);
        modeItem.mMode = "Hdr";		
        //*/	
        modeItem.mModeName = mResources.getString(mResources.getIdentifier(
                "hdr_mode_title", "string", mContext.getPackageName()));
        modeItem.mSupportedCameraIds = new String[]{"0"};
        return modeItem;
    }


}
