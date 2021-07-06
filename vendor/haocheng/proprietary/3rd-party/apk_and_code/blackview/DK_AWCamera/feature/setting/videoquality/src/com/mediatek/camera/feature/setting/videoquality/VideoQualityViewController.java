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
package com.mediatek.camera.feature.setting.videoquality;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.mediatek.camera.R;
import com.mediatek.camera.common.IAppUiListener;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.mode.ICameraMode;
import com.mediatek.camera.common.utils.CameraUtil;
import com.mediatek.camera.common.widget.RotateImageView;

//add by huangfei for lowpower tips start
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.Toast;
import com.mediatek.camera.common.IAppUi;
import com.mediatek.camera.CameraActivity;
import com.mediatek.camera.Config;
//add by huangfei for lowpower tips end
import java.util.List;
//bv wuyonglin add for adapte video quality not support 1080p 20200604 start
import android.media.CamcorderProfile;
//bv wuyonglin add for adapte video quality not support 1080p 20200604 end

/**
 * This class manages the looks of the VideoQuality and VideoQuality mode choice view.
 */
public class VideoQualityViewController {
    private static final LogUtil.Tag TAG =
            new LogUtil.Tag(VideoQualityViewController.class.getSimpleName());

    private static final int VIDEOQUALITY_ENTRY_LIST_SWITCH_SIZE = 2;
    private static final int VIDEOQUALITY_ENTRY_LIST_INDEX_0 = 0;
    private static final int VIDEOQUALITY_ENTRY_LIST_INDEX_1 = 1;
    private static final int VIDEOQUALITY_PRIORITY = 45;
    private static final int VIDEOQUALITY_SHUTTER_PRIORITY = 60;

    private static final int VIDEOQUALITY_VIEW_INIT = 0;
    private static final int VIDEOQUALITY_VIEW_ADD_QUICK_SWITCH = 1;
    private static final int VIDEOQUALITY_VIEW_REMOVE_QUICK_SWITCH = 2;
    private static final int VIDEOQUALITY_VIEW_HIDE_CHOICE_VIEW = 3;
    private static final int VIDEOQUALITY_VIEW_UPDATE_QUICK_SWITCH_ICON = 4;
	
    private ImageView mVideoQualityEntryView;
    private ImageView mVideoQuality1Icon;
    private ImageView mVideoQuality2Icon;
    private ImageView mVideoQuality3Icon;
    private ImageView mVideoQuality4Icon;
    private View mVideoQualityChoiceView;
    private View mOptionLayout;
    private final VideoQuality mVideoQuality;
    private final IApp mApp;
    private MainHandler mMainHandler;
    private boolean mAddQuickSwitchIcon = false;
    private boolean isSupportVideoQuality_4K = false;
    private boolean isSupportVideoQuality_2K = false;

    /**
     * Constructor of videoquality view.
     * @param videoquality VideoQuality instance.
     * @param app   The application app level controller.
     */
    public VideoQualityViewController(VideoQuality videoquality, IApp app) {
        mVideoQuality = videoquality;
        mApp = app;
        mMainHandler = new MainHandler(app.getActivity().getMainLooper());
        //mMainHandler.sendEmptyMessage(VIDEOQUALITY_VIEW_INIT);
    }

    /**
     * init videoquality switch to quick switch.
     */
    public void initQuickSwitchIcon() {

        mMainHandler.sendEmptyMessage(VIDEOQUALITY_VIEW_INIT);
    }

    /**
     * add videoquality switch to quick switch.
     */
    public void addQuickSwitchIcon() {

        mMainHandler.sendEmptyMessage(VIDEOQUALITY_VIEW_ADD_QUICK_SWITCH);
    }

    /**
     * remove qiuck switch icon.
     */
    public void removeQuickSwitchIcon() {
            LogHelper.d(TAG, "removeQuickSwitchIcon mAddQuickSwitchIcon ="+mAddQuickSwitchIcon);
	if (mAddQuickSwitchIcon) {
        mMainHandler.sendEmptyMessage(VIDEOQUALITY_VIEW_REMOVE_QUICK_SWITCH);
	}
    }

    /**
     * for overrides value, for set visibility.
     * @param isShow true means show.
     */
    public void showQuickSwitchIcon(boolean isShow) {
        mMainHandler.obtainMessage(VIDEOQUALITY_VIEW_UPDATE_QUICK_SWITCH_ICON, isShow).sendToTarget();
    }

    /**
     * close option menu.
     */
    public void hideVideoQualityChoiceView() {
        mMainHandler.sendEmptyMessage(VIDEOQUALITY_VIEW_HIDE_CHOICE_VIEW);
    }

    /**
     * Handler let some task execute in main thread.
     */
    private class MainHandler extends Handler {
        public MainHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            LogHelper.d(TAG, "view handleMessage: " + msg.what);
            switch (msg.what) {
                case VIDEOQUALITY_VIEW_INIT:
                    mVideoQualityEntryView = initVideoQualityEntryView();
                    break;

                case VIDEOQUALITY_VIEW_ADD_QUICK_SWITCH:
                    mApp.getAppUi().addToQuickSwitcher(mVideoQualityEntryView, VIDEOQUALITY_PRIORITY);
		            mAddQuickSwitchIcon = true;
                    updateVideoQualityEntryView(mVideoQuality.getValue());
                    mApp.getAppUi().registerOnShutterButtonListener(mShutterListener,
                            VIDEOQUALITY_SHUTTER_PRIORITY);
                    break;

                case VIDEOQUALITY_VIEW_REMOVE_QUICK_SWITCH:
                    mApp.getAppUi().removeFromQuickSwitcher(mVideoQualityEntryView);
		            mAddQuickSwitchIcon = false;
                    mApp.getAppUi().unregisterOnShutterButtonListener(mShutterListener);
                    break;

                case VIDEOQUALITY_VIEW_UPDATE_QUICK_SWITCH_ICON:
                    if (mVideoQualityEntryView != null) {
                    if ((boolean) msg.obj) {
                        mVideoQualityEntryView.setVisibility(View.VISIBLE);
                        updateVideoQualityEntryView(mVideoQuality.getValue());
                    } else {
                        mVideoQualityEntryView.setVisibility(View.GONE);
                    }
                    }
                    break;

                case VIDEOQUALITY_VIEW_HIDE_CHOICE_VIEW:
                    if (mVideoQualityChoiceView != null && mVideoQualityChoiceView.isShown()) {
                        mApp.getAppUi().hideQuickSwitcherOption();
                        updateVideoQualityEntryView(mVideoQuality.getValue());
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Update ui by the value.
     * @param value the value to change.
     *
     */
    public void updateVideoQualityEntryView(final String value) {
        LogHelper.d(TAG, "[updateVideoQualityView] currentValue = " + mVideoQuality.getValue()+" value ="+value);
        //bv wuyonglin modify for adapte video quality not support 1080p 20200604 start
        if (mVideoQuality.getValue().equals(Integer.toString(CamcorderProfile.QUALITY_1080P))) {
            mVideoQualityEntryView.setImageResource(R.drawable.ic_1080p_pressed);
            mVideoQualityEntryView.setContentDescription(mApp.getActivity().getResources().getString(
                    R.string.accessibility_flash_on));
        } else if (mVideoQuality.getValue().equals(Integer.toString(CamcorderProfile.QUALITY_720P))) {
            mVideoQualityEntryView.setImageResource(R.drawable.ic_720p_pressed);
            mVideoQualityEntryView.setContentDescription(mApp.getActivity().getResources().getString(
                    R.string.accessibility_flash_auto));
        } else if (mVideoQuality.getValue().equals(Integer.toString(CamcorderProfile.QUALITY_480P))) {
            mVideoQualityEntryView.setImageResource(R.drawable.century_ic_videoquality1_pressed);
            mVideoQualityEntryView.setContentDescription(mApp.getActivity().getResources().getString(
                    R.string.accessibility_flash_off));
        } else if(mVideoQuality.getValue().equals(Integer.toString(CamcorderProfile.QUALITY_2160P))){
            mVideoQualityEntryView.setImageResource(R.drawable.ic_4k_pressed);
            mVideoQualityEntryView.setContentDescription(mApp.getActivity().getResources().getString(
                    R.string.accessibility_flash_off));
        } else if(mVideoQuality.getValue().equals(Integer.toString(CamcorderProfile.QUALITY_QHD))){
            mVideoQualityEntryView.setImageResource(R.drawable.ic_2k_pressed);
            mVideoQualityEntryView.setContentDescription(mApp.getActivity().getResources().getString(
                    R.string.accessibility_flash_off));
        } else if(mVideoQuality.getValue().equals("60")){
            mVideoQualityEntryView.setImageResource(R.drawable.ic_1080p60fps_pressed);
            mVideoQualityEntryView.setContentDescription(mApp.getActivity().getResources().getString(
                    R.string.accessibility_flash_off));
        }

        //bv wuyonglin modify for adapte video quality not support 1080p 20200604 end
    }

    /**
     * Initialize the videoquality view which will add to quick switcher.
     * @return the view add to quick switcher
     */
    private ImageView initVideoQualityEntryView() {
        Activity activity = mApp.getActivity();
        RotateImageView view = (RotateImageView) activity.getLayoutInflater().inflate(
                R.layout.bv_videoquality_icon, null);
        view.setOnClickListener(mVideoQualityEntryListener);
        isSupportVideoQuality_4K = mVideoQuality.getEntryValues().contains(Integer.toString(CamcorderProfile.QUALITY_2160P));
        isSupportVideoQuality_2K = mVideoQuality.getEntryValues().contains(Integer.toString(CamcorderProfile.QUALITY_QHD));
        LogHelper.i(TAG,"initVideoQualityEntryView isSupportVideoQuality_4K = " + isSupportVideoQuality_4K + " isSupportVideoQuality_2K = " + isSupportVideoQuality_2K);
        return view;
    }

    /**
     * This listener used to monitor the videoquality quick switch icon click item.
     */
    private final View.OnClickListener mVideoQualityEntryListener = new View.OnClickListener() {
        public void onClick(View view) {
            if (mVideoQuality.getEntryValues().size() <= 1) {
                return;
            }
            LogHelper.d(TAG, "[mVideoQualityEntryListener] mVideoQuality.getEntryValues().size() = " + mVideoQuality.getEntryValues().size());
            if(CameraUtil.isVideo_HDR_changing){
                LogHelper.i(TAG,"mVideoQualityEntryListener isVideo_HDR_changing = true return");
                return;
            }
            initializeVideoQualityChoiceView();
            updateChoiceView();
            mApp.getAppUi().showQuickSwitcherOption(mOptionLayout);
        }
    };

    private View.OnClickListener mVideoQualityChoiceViewListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String value = "";
            //bv wuyonglin modify for adapte video quality not support 1080p 20200604 start
            if(isSupportVideoQuality_4K){
                if (mVideoQuality3Icon == view) {
                    value = Integer.toString(CamcorderProfile.QUALITY_2160P);
                } else if (mVideoQuality2Icon == view) {
                    value = Integer.toString(CamcorderProfile.QUALITY_1080P);
                } else if (mVideoQuality1Icon == view) {
                    value = Integer.toString(CamcorderProfile.QUALITY_720P);
                } else if (mVideoQuality4Icon == view) {
                    value = "60";
                }

            }else if(isSupportVideoQuality_2K){
                if (mVideoQuality3Icon == view) {
                    value = Integer.toString(CamcorderProfile.QUALITY_QHD);
                } else if (mVideoQuality2Icon == view) {
                    value = Integer.toString(CamcorderProfile.QUALITY_1080P);
                } else if (mVideoQuality1Icon == view) {
                    value = Integer.toString(CamcorderProfile.QUALITY_720P);
                }
            }else{
                if (mVideoQuality3Icon == view) {
                    value = Integer.toString(CamcorderProfile.QUALITY_1080P);
                } else if (mVideoQuality2Icon == view) {
                    value = Integer.toString(CamcorderProfile.QUALITY_720P);
                } else if (mVideoQuality1Icon == view) {
                    value = Integer.toString(CamcorderProfile.QUALITY_480P);
                }
            }
            //bv wuyonglin modify for adapte video quality not support 1080p 20200604 end
            mApp.getAppUi().hideQuickSwitcherOption();
            updateVideoQualityEntryView(value);
            mVideoQuality.onVideoQualityValueChanged(value);
        }

    };

    /**
     * This function used to high light the current choice for.
     * videoquality if videoquality choice view is show.
     */
    private void updateChoiceView() {
        //bv wuyonglin modify for adapte video quality not support 1080p 20200604 start
        if(isSupportVideoQuality_4K && mVideoQuality.isFPS60Support1080P()){
            if (mVideoQuality.getValue().equals(Integer.toString(CamcorderProfile.QUALITY_2160P))) {
                mVideoQuality3Icon.setImageResource(R.drawable.ic_4k_pressed);
                mVideoQuality2Icon.setImageResource(R.drawable.ic_1080p_narmal);
                mVideoQuality1Icon.setImageResource(R.drawable.ic_720p_normal);
                mVideoQuality4Icon.setImageResource(R.drawable.ic_1080p60fps_namal);
            } else if (mVideoQuality.getValue().equals(Integer.toString(CamcorderProfile.QUALITY_1080P))) {
                mVideoQuality3Icon.setImageResource(R.drawable.ic_4k_normal);
                mVideoQuality2Icon.setImageResource(R.drawable.ic_1080p_pressed);
                mVideoQuality1Icon.setImageResource(R.drawable.ic_720p_normal);
                mVideoQuality4Icon.setImageResource(R.drawable.ic_1080p60fps_namal);
            } else if (mVideoQuality.getValue().equals(Integer.toString(CamcorderProfile.QUALITY_720P))) {
                mVideoQuality3Icon.setImageResource(R.drawable.ic_4k_normal);
                mVideoQuality2Icon.setImageResource(R.drawable.ic_1080p_narmal);
                mVideoQuality1Icon.setImageResource(R.drawable.ic_720p_pressed);
                mVideoQuality4Icon.setImageResource(R.drawable.ic_1080p60fps_namal);
            } else if (mVideoQuality.getValue().equals("60")) {
                mVideoQuality3Icon.setImageResource(R.drawable.ic_4k_normal);
                mVideoQuality2Icon.setImageResource(R.drawable.ic_1080p_narmal);
                mVideoQuality1Icon.setImageResource(R.drawable.ic_720p_normal);
                mVideoQuality4Icon.setImageResource(R.drawable.ic_1080p60fps_pressed);
            }
        } else if(isSupportVideoQuality_4K){
            if (mVideoQuality.getValue().equals(Integer.toString(CamcorderProfile.QUALITY_2160P))) {
                mVideoQuality3Icon.setImageResource(R.drawable.ic_4k_pressed);
                mVideoQuality2Icon.setImageResource(R.drawable.ic_1080p_narmal);
                mVideoQuality1Icon.setImageResource(R.drawable.ic_720p_normal);
            } else if (mVideoQuality.getValue().equals(Integer.toString(CamcorderProfile.QUALITY_1080P))) {
                mVideoQuality3Icon.setImageResource(R.drawable.ic_4k_normal);
                mVideoQuality2Icon.setImageResource(R.drawable.ic_1080p_pressed);
                mVideoQuality1Icon.setImageResource(R.drawable.ic_720p_normal);
            } else if (mVideoQuality.getValue().equals(Integer.toString(CamcorderProfile.QUALITY_720P))) {
                mVideoQuality3Icon.setImageResource(R.drawable.ic_4k_normal);
                mVideoQuality2Icon.setImageResource(R.drawable.ic_1080p_narmal);
                mVideoQuality1Icon.setImageResource(R.drawable.ic_720p_pressed);
            }
        }else if(isSupportVideoQuality_2K){
            LogHelper.i(TAG,"updateChoiceView isSupportVideoQuality_2K = " + isSupportVideoQuality_2K);
            if (mVideoQuality.getValue().equals(Integer.toString(CamcorderProfile.QUALITY_QHD))) {
                LogHelper.i(TAG,"11 updateChoiceView isSupportVideoQuality_2K = " + isSupportVideoQuality_2K);
                mVideoQuality3Icon.setImageResource(R.drawable.ic_2k_pressed);
                mVideoQuality2Icon.setImageResource(R.drawable.ic_1080p_narmal);
                mVideoQuality1Icon.setImageResource(R.drawable.ic_720p_normal);
            } else if (mVideoQuality.getValue().equals(Integer.toString(CamcorderProfile.QUALITY_1080P))) {
                LogHelper.i(TAG,"6 updateChoiceView isSupportVideoQuality_2K = " + isSupportVideoQuality_2K);
                mVideoQuality3Icon.setImageResource(R.drawable.ic_2k_normal);
                mVideoQuality2Icon.setImageResource(R.drawable.ic_1080p_pressed);
                mVideoQuality1Icon.setImageResource(R.drawable.ic_720p_normal);
            } else if (mVideoQuality.getValue().equals(Integer.toString(CamcorderProfile.QUALITY_720P))) {
                LogHelper.i(TAG,"5 updateChoiceView isSupportVideoQuality_2K = " + isSupportVideoQuality_2K);
                mVideoQuality3Icon.setImageResource(R.drawable.ic_2k_normal);
                mVideoQuality2Icon.setImageResource(R.drawable.ic_1080p_narmal);
                mVideoQuality1Icon.setImageResource(R.drawable.ic_720p_pressed);
            }
        }else {
            if (mVideoQuality.getValue().equals(Integer.toString(CamcorderProfile.QUALITY_1080P))) {
                mVideoQuality3Icon.setImageResource(R.drawable.century_ic_videoquality3_pressed);
                mVideoQuality2Icon.setImageResource(R.drawable.century_ic_videoquality2_normal);
                mVideoQuality1Icon.setImageResource(R.drawable.century_ic_videoquality1_normal);
            } else if (mVideoQuality.getValue().equals(Integer.toString(CamcorderProfile.QUALITY_720P))) {
                mVideoQuality3Icon.setImageResource(R.drawable.century_ic_videoquality3_normal);
                mVideoQuality2Icon.setImageResource(R.drawable.century_ic_videoquality2_pressed);
                mVideoQuality1Icon.setImageResource(R.drawable.century_ic_videoquality1_normal);
            } else if (mVideoQuality.getValue().equals(Integer.toString(CamcorderProfile.QUALITY_480P))) {
                mVideoQuality3Icon.setImageResource(R.drawable.century_ic_videoquality3_normal);
                mVideoQuality2Icon.setImageResource(R.drawable.century_ic_videoquality2_normal);
                mVideoQuality1Icon.setImageResource(R.drawable.century_ic_videoquality1_pressed);
            }
        }
        //bv wuyonglin modify for adapte video quality not support 1080p 20200604 end
    }

    private void initializeVideoQualityChoiceView() {
        if (mVideoQualityChoiceView == null || mOptionLayout == null) {
            ViewGroup viewGroup =  mApp.getAppUi().getModeRootView();
            //bv wuyonglin add for adapte video quality not support 1080p 20200604 start
            if (!mVideoQuality.getEntryValues().contains(Integer.toString(CamcorderProfile.QUALITY_1080P))) {
                mOptionLayout = mApp.getActivity().getLayoutInflater().inflate(
                    R.layout.bv_videoquality_option_no_1080p, viewGroup, false);
                LogHelper.i(TAG,"Layout bv_videoquality_option_no_1080p");
            } else if(isSupportVideoQuality_4K && mVideoQuality.isFPS60Support1080P()){
                mOptionLayout = mApp.getActivity().getLayoutInflater().inflate(
                        R.layout.bv_videoquality_option_with_4k_60fps, viewGroup, false);
                LogHelper.i(TAG,"Layout bv_videoquality_option_with_4k_60fps");
            } else if(isSupportVideoQuality_4K){
                mOptionLayout = mApp.getActivity().getLayoutInflater().inflate(
                        R.layout.bv_videoquality_option_with_4k, viewGroup, false);
                LogHelper.i(TAG,"Layout bv_videoquality_option_with_4K");
            } else if(isSupportVideoQuality_2K){
                mOptionLayout = mApp.getActivity().getLayoutInflater().inflate(
                        R.layout.bv_videoquality_option_with_2k, viewGroup, false);
                LogHelper.i(TAG,"Layout bv_videoquality_option_with_2K");
            } else if (!mVideoQuality.getEntryValues().contains(Integer.toString(CamcorderProfile.QUALITY_480P))) {
                mOptionLayout = mApp.getActivity().getLayoutInflater().inflate(
                    R.layout.bv_videoquality_option_no_480p, viewGroup, false);
            }else {
                mOptionLayout = mApp.getActivity().getLayoutInflater().inflate(
                    R.layout.bv_videoquality_option, viewGroup, false);
            }
            //bv wuyonglin add for adapte video quality not support 1080p 20200604 end
            mVideoQualityChoiceView = mOptionLayout.findViewById(R.id.bv_videoquality_choice);
            mVideoQuality1Icon = (ImageView) mOptionLayout.findViewById(R.id.bv_videoquality1);
            mVideoQuality2Icon = (ImageView) mOptionLayout.findViewById(R.id.bv_videoquality2);
            mVideoQuality3Icon = (ImageView) mOptionLayout.findViewById(R.id.bv_videoquality3);
            if (isSupportVideoQuality_4K && mVideoQuality.isFPS60Support1080P()) {
                mVideoQuality4Icon = (ImageView) mOptionLayout.findViewById(R.id.bv_videoquality4);
                mVideoQuality4Icon.setOnClickListener(mVideoQualityChoiceViewListener);
            }
            mVideoQuality1Icon.setOnClickListener(mVideoQualityChoiceViewListener);
            mVideoQuality2Icon.setOnClickListener(mVideoQualityChoiceViewListener);
            mVideoQuality3Icon.setOnClickListener(mVideoQualityChoiceViewListener);
            //bv wuyonglin add for adapte video quality not support 1080p 20200604 start
            if(isSupportVideoQuality_4K){
                if (!mVideoQuality.getEntryValues().contains(Integer.toString(CamcorderProfile.QUALITY_2160P))) {
                    mVideoQuality3Icon.setVisibility(View.GONE);
                }
                if (!mVideoQuality.getEntryValues().contains(Integer.toString(CamcorderProfile.QUALITY_1080P))) {
                    mVideoQuality2Icon.setVisibility(View.GONE);
                }
                if (!mVideoQuality.getEntryValues().contains(Integer.toString(CamcorderProfile.QUALITY_720P))) {
                    mVideoQuality1Icon.setVisibility(View.GONE);
                }

            }else if(isSupportVideoQuality_2K){
                if (!mVideoQuality.getEntryValues().contains(Integer.toString(CamcorderProfile.QUALITY_QHD))) {
                    LogHelper.i(TAG,"11 initializeVideoQualityChoiceView mVideoQuality3Icon  View.GONE");
                    mVideoQuality3Icon.setVisibility(View.GONE);
                }
                if (!mVideoQuality.getEntryValues().contains(Integer.toString(CamcorderProfile.QUALITY_1080P))) {
                    LogHelper.i(TAG,"6 initializeVideoQualityChoiceView mVideoQuality3Icon  View.GONE");
                    mVideoQuality2Icon.setVisibility(View.GONE);
                }
                if (!mVideoQuality.getEntryValues().contains(Integer.toString(CamcorderProfile.QUALITY_720P))) {
                    LogHelper.i(TAG,"5 initializeVideoQualityChoiceView mVideoQuality3Icon  View.GONE");
                    mVideoQuality1Icon.setVisibility(View.GONE);
                }
            }else{
                if (!mVideoQuality.getEntryValues().contains(Integer.toString(CamcorderProfile.QUALITY_1080P))) {
                    mVideoQuality3Icon.setVisibility(View.GONE);
                }
                if (!mVideoQuality.getEntryValues().contains(Integer.toString(CamcorderProfile.QUALITY_720P))) {
                    mVideoQuality2Icon.setVisibility(View.GONE);
                }
                if (!mVideoQuality.getEntryValues().contains(Integer.toString(CamcorderProfile.QUALITY_480P))) {
                    mVideoQuality1Icon.setVisibility(View.GONE);
                }
            }
            //bv wuyonglin add for adapte video quality not support 1080p 20200604 end
        }
    }

    private final IAppUiListener.OnShutterButtonListener mShutterListener =
            new IAppUiListener.OnShutterButtonListener() {

                @Override
                public boolean onShutterButtonFocus(boolean pressed) {
                    if (pressed) {
                        hideVideoQualityChoiceView();
                    }
                    return false;
                }

                @Override
                public boolean onShutterButtonClick() {
                    return false;
                }

                @Override
                public boolean onShutterButtonLongPressed() {
                    return false;
                }
            };

    //bv wuyonglin add for startVideoRecord not show videoquality quickSwitch 20191225 start
    public ImageView getVideoQualityEntryView() {
	return mVideoQualityEntryView;
    }
    //bv wuyonglin add for startVideoRecord not show videoquality quickSwitch 20191225 end
}
