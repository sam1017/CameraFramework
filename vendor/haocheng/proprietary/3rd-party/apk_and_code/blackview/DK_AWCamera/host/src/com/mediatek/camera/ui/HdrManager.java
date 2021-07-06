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

package com.mediatek.camera.ui;

import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import com.mediatek.camera.R;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil.Tag;
import com.mediatek.camera.common.utils.CameraUtil;
import android.widget.ImageButton;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import android.content.Intent;
import android.content.ComponentName;
import android.content.Context;
import android.content.ActivityNotFoundException;

/**
 * A manager for {@link QuickSwitcher}.
 */
public class HdrManager extends AbstractViewManager {
    private static final Tag TAG = new Tag(
        HdrManager.class.getSimpleName());

    private RelativeLayout mQRCodeScanLayout;
    private HdrViewCtr mHdrViewCtr;
    private final OnOrientationChangeListenerImpl mOrientationChangeListener;

    private static final String HDR_DEFAULT_VALUE = "off";
    private static final String HDR_ON_VALUE = "on";
    private static final String HDR_AUTO_VALUE = "auto";
    private static final String HDR_KEY = "key_hdr";
    private String mValue;
    private HdrSwitchListener mListener;
        
    /**
     * constructor of QuickSwitcherManager.
     * @param app The {@link IApp} implementer.
     * @param parentView the root view of ui.
     */
    public HdrManager(IApp app, ViewGroup parentView) {
        super(app, parentView);
        mOrientationChangeListener = new OnOrientationChangeListenerImpl();
    }

    @Override
    protected View getView() {
        if (mHdrViewCtr == null) {
            mHdrViewCtr = new HdrViewCtr(mApp, this);
            mHdrViewCtr.addQuickSwitchIcon();
            mHdrViewCtr.showQuickSwitchIcon(true);
        }
        return mHdrViewCtr.getView();
    }

    @Override
    public void setEnabled(boolean enabled) {
    }

    @Override
    public void onCreate() {
        mApp.registerOnOrientationChangeListener(mOrientationChangeListener);
    }

    @Override
    public void onResume() {
        if (mHdrViewCtr == null) {
            mHdrViewCtr = new HdrViewCtr(mApp, this);
            mHdrViewCtr.addQuickSwitchIcon();
            mHdrViewCtr.showQuickSwitchIcon(true);
        }
    }

    @Override
    public void onPause() {
        if (mHdrViewCtr == null) {
            mHdrViewCtr.closeHdrChoiceView();
            mHdrViewCtr.removeQuickSwitchIcon();
        }
    }

    @Override
    public void onDestroy() {
        mApp.unregisterOnOrientationChangeListener(mOrientationChangeListener);
    }


    /**
     * Show quick switcher option view, mode picker and quick switch will disappear.
     * @param optionView the option view, it should not attach to any parent view.
     */
    public void showHdrView() {
        if (mHdrViewCtr != null) {
            mHdrViewCtr.addQuickSwitchIcon();
            mHdrViewCtr.showQuickSwitchIcon(true);
        }
    }

    /**
     * Hide quick switcher without animation.
     */
    public void hideHdrView() {
        if (mHdrViewCtr != null) {
            mHdrViewCtr.removeQuickSwitchIcon();
        }
    }

    /**
     * Implementer of OnOrientationChangeListener.
     */
    private class OnOrientationChangeListenerImpl implements IApp.OnOrientationChangeListener {

        @Override
        public void onOrientationChanged(int orientation) {
            if (mQRCodeScanLayout != null ) {
                CameraUtil.rotateRotateLayoutChildView(mApp.getActivity(), mQRCodeScanLayout,
                        orientation, true);
            }
        }
    }

    public String getValue(){
        if(mValue==null){
            return HDR_DEFAULT_VALUE;
        }
        return mValue;
    }

    public void undateHdrValue(String value,boolean clicked){
        mValue = value;
        if(clicked){
            mListener.onHdrValueChanged(value);
        }
        
    }

    public interface HdrSwitchListener {
        void onHdrValueChanged(String value);
    }
    public void setHdrSwitchListener(HdrSwitchListener listener){
        mListener = listener;
    }
    public void exitHdrMode(){
        if (mHdrViewCtr != null) {
            mHdrViewCtr.exitHdrMode();
        }
    }
}
