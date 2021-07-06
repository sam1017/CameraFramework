/*
 *
 *  *   Copyright Statement:
 *  *
 *  *     This software/firmware and related documentation ("MediaTek Software") are
 *  *     protected under relevant copyright laws. The information contained herein is
 *  *     confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 *  *     the prior written permission of MediaTek inc. and/or its licensors, any
 *  *     reproduction, modification, use or disclosure of MediaTek Software, and
 *  *     information contained herein, in whole or in part, shall be strictly
 *  *     prohibited.
 *  *
 *  *     MediaTek Inc. (C) 2016. All rights reserved.
 *  *
 *  *     BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 *  *    THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 *  *     RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 *  *     ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 *  *     WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 *  *     WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 *  *     NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 *  *     RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 *  *     INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 *  *     TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 *  *     RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 *  *     OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 *  *     SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 *  *     RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 *  *     STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 *  *     ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 *  *     RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 *  *     MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 *  *     CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *  *
 *  *     The following software/firmware and/or related documentation ("MediaTek
 *  *     Software") have been modified by MediaTek Inc. All revisions are subject to
 *  *     any receiver's applicable license agreements with MediaTek Inc.
 *
 */

package com.mediatek.camera.feature.mode.hctbokeh;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.provider.MediaStore;

import com.mediatek.camera.Config;
import com.mediatek.camera.R;
import com.mediatek.camera.common.IAppUi.ModeItem;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.device.CameraDeviceManagerFactory.CameraApi;
import com.mediatek.camera.common.loader.FeatureEntryBase;
import com.mediatek.camera.common.mode.ICameraMode;
import com.mediatek.camera.common.utils.CameraUtil;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Photo mode entry.
 */

public class HctBokehEntry extends FeatureEntryBase {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(HctBokehEntry.class.getSimpleName());
    private static final String MODE_ITEM_TYPE = "Picture";

    private static final int MODE_ITEM_PRIORITY = 110;

    //add by huangfeifor front bokeh start
    private Activity mActivity;
    //add by huangfeifor front bokeh end

    /**
     * create an entry.
     *
     * @param context   current activity.
     * @param resources current resources.
     */
    public HctBokehEntry(Context context, Resources resources) {
        super(context, resources);
    }

    @Override
    public boolean isSupport(CameraApi currentCameraApi, Activity activity) {
        boolean support = false;
        if (!isThirdPartyIntent(activity)&&Config.isHctBokehSupported()) {
            support = true;
        }
        LogHelper.i(TAG, "[isSupport] : " + support);

        //add by huangfeifor front bokeh start
        mActivity = activity;
        //add by huangfeifor front bokeh end

        return support;
    }

    /**
     * Get the advance feature name.
     *
     * @return Feature name.
     */
    @Override
    public String getFeatureEntryName() {
        return HctBokehEntry.class.getName();
    }

    /**
     * Get feature name, the type is define as a Class.
     * For excample,
     * if the feature is a mode ,it will implement the ICameraMode, so the
     * feature typle will  be the ICameraMode.class.
     * if it is a setting, the type will be the ICameraSetting.class.
     *
     * @return Feature type.
     */
    @Override
    public Class getType() {
        return ICameraMode.class;
    }

    /**
     * Create the feature instance.
     *
     * @return Return The ICameraMode or ICameraSetting implementer instance.
     */
    @Override
    public Object createInstance() {
        return new HctBokehMode();
    }

    /**
     * Get mode item.
     *
     * @return the mode item info.
     */
    @Override
    public ModeItem getModeItem() {
        ModeItem modeItem = new ModeItem();
        modeItem.mType = "Picture";
        modeItem.mPriority = MODE_ITEM_PRIORITY; 
        modeItem.mClassName = getFeatureEntryName();

        //modify by huangfeifor front bokeh start
        //modeItem.mSupportedCameraIds = new String[]{"0"};
        if(!Config.isFrontBokehSupport(mActivity)){
            modeItem.mSupportedCameraIds = new String[]{"0"};
        } else {
            modeItem.mSupportedCameraIds = new String[]{"0", "1"};
        }

        //modify by huangfeifor front bokeh end

        modeItem.mMode = "HctBokeh";
        modeItem.mTitle = mContext.getResources().getString(R.string.accessibility_switch_to_hctbokeh);
        modeItem.mModeName = mContext.getString(R.string.accessibility_switch_to_hctbokeh);
        return modeItem;
    }
}
