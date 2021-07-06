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
package com.mediatek.camera.feature.setting.picturesize;

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
//bv wuyonglin add for adapte picture size not support 18:9 20200610 start
import java.util.ArrayList;
import android.widget.RelativeLayout;
//bv wuyonglin add for adapte picture size not support 18:9 20200610 end

/**
 * This class manages the looks of the pictureSize and pictureSize mode choice view.
 */
public class PictureSizeViewController {
    private static final LogUtil.Tag TAG =
            new LogUtil.Tag(PictureSizeViewController.class.getSimpleName());

    private static final int PictureSize_ENTRY_LIST_SWITCH_SIZE = 2;
    private static final int PictureSize_ENTRY_LIST_INDEX_0 = 0;
    private static final int PictureSize_ENTRY_LIST_INDEX_1 = 1;
    private static final int PICTURESIZE_PRIORITY = 40;
    private static final int PICTURESIZE_SHUTTER_PRIORITY = 60;

    private static final int PICTURESIZE_VIEW_INIT = 0;
    private static final int PICTURESIZE_VIEW_ADD_QUICK_SWITCH = 1;
    private static final int PICTURESIZE_VIEW_REMOVE_QUICK_SWITCH = 2;
    private static final int PICTURESIZE_VIEW_HIDE_CHOICE_VIEW = 3;
    private static final int PICTURESIZE_VIEW_UPDATE_QUICK_SWITCH_ICON = 4;
	
    private ImageView mPictureSizeEntryView;
    private ImageView mPictureSize1Icon;
    private ImageView mPictureSize2Icon;
    private ImageView mPictureSize3Icon;
    private ImageView mPictureSize4Icon;
    private View mPictureSizeChoiceView;
    private View mOptionLayout;
    private final PictureSize mPictureSize;
    private final IApp mApp;
    private MainHandler mMainHandler;

    /**
     * Constructor of PictureSize view.
     * @param pictureSize PictureSize instance.
     * @param app   The application app level controller.
     */
    public PictureSizeViewController(PictureSize pictureSize, IApp app) {
        mPictureSize = pictureSize;
        mApp = app;
        mMainHandler = new MainHandler(app.getActivity().getMainLooper());
        mMainHandler.sendEmptyMessage(PICTURESIZE_VIEW_INIT);
    }

    /**
     * add pictureSize switch to quick switch.
     */
    public void addQuickSwitchIcon() {

        mMainHandler.sendEmptyMessage(PICTURESIZE_VIEW_ADD_QUICK_SWITCH);
    }

    /**
     * remove qiuck switch icon.
     */
    public void removeQuickSwitchIcon() {

        mMainHandler.sendEmptyMessage(PICTURESIZE_VIEW_REMOVE_QUICK_SWITCH);
    }

    /**
     * for overrides value, for set visibility.
     * @param isShow true means show.
     */
    public void showQuickSwitchIcon(boolean isShow) {
        mMainHandler.obtainMessage(PICTURESIZE_VIEW_UPDATE_QUICK_SWITCH_ICON, isShow).sendToTarget();
    }

    /**
     * close option menu.
     */
    public void hidePictureSizeChoiceView() {
        mMainHandler.sendEmptyMessage(PICTURESIZE_VIEW_HIDE_CHOICE_VIEW);
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
                case PICTURESIZE_VIEW_INIT:
                    mPictureSizeEntryView = initPictureSizeEntryView();
                    break;

                case PICTURESIZE_VIEW_ADD_QUICK_SWITCH:
                    mApp.getAppUi().addToQuickSwitcher(mPictureSizeEntryView, PICTURESIZE_PRIORITY);
                    //bv wuyonglin add for from PanoramaMode to MonoMode happened NullPointerException 20200225 start
                    if (mPictureSize.getValue() != null) {
                    updatePictureSizeEntryView(PictureSizeHelper.getStandardAspectRatio(mPictureSize.getValue()));
                    }
                    //bv wuyonglin add for from PanoramaMode to MonoMode happened NullPointerException 20200225 end
                    mApp.getAppUi().registerOnShutterButtonListener(mShutterListener,
                            PICTURESIZE_SHUTTER_PRIORITY);
                    break;

                case PICTURESIZE_VIEW_REMOVE_QUICK_SWITCH:
                    mApp.getAppUi().removeFromQuickSwitcher(mPictureSizeEntryView);
                    mApp.getAppUi().unregisterOnShutterButtonListener(mShutterListener);
                    break;

                case PICTURESIZE_VIEW_UPDATE_QUICK_SWITCH_ICON:
                    if ((boolean) msg.obj) {
                        mPictureSizeEntryView.setVisibility(View.VISIBLE);
                    //bv wuyonglin add for from PanoramaMode to MonoMode happened NullPointerException 20200225 start
                    if (mPictureSize.getValue() != null) {
                        updatePictureSizeEntryView(PictureSizeHelper.getStandardAspectRatio(mPictureSize.getValue()));
                    }
                    //bv wuyonglin add for from PanoramaMode to MonoMode happened NullPointerException 20200225 end
                    } else {
                        mPictureSizeEntryView.setVisibility(View.GONE);
                    }
                    break;

                case PICTURESIZE_VIEW_HIDE_CHOICE_VIEW:
                    if (mPictureSizeChoiceView != null && mPictureSizeChoiceView.isShown()) {
                        mApp.getAppUi().hideQuickSwitcherOption();
                        updatePictureSizeEntryView(PictureSizeHelper.getStandardAspectRatio(mPictureSize.getValue()));
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
    public void updatePictureSizeEntryView(final double value) {
        LogHelper.d(TAG, "[updatePictureSizeView] currentValue = " + mPictureSize.getValue()+" value ="+value);
        if (PictureSizeHelper.RATIO_18_9 == value) {
            //bv wuyonglin modify for picture size icon not show pressed status color 20200115 start
            mPictureSizeEntryView.setImageResource(R.drawable.century_ic_size3_normal);
            //bv wuyonglin modify for picture size icon not show pressed status color 20200115 end
        } else if (PictureSizeHelper.RATIO_1_1 == value) {
            //bv wuyonglin modify for picture size icon not show pressed status color 20200115 start
            mPictureSizeEntryView.setImageResource(R.drawable.century_ic_size1_normal);
            //bv wuyonglin modify for picture size icon not show pressed status color 20200115 end
        } else if (PictureSizeHelper.RATIO_16_9 == value) {
            mPictureSizeEntryView.setImageResource(R.drawable.bv_picturesize_16_9);
        } else {
            //bv wuyonglin modify for picture size icon not show pressed status color 20200115 start
            mPictureSizeEntryView.setImageResource(R.drawable.century_ic_size2_normal);
            //bv wuyonglin modify for picture size icon not show pressed status color 20200115 end
        }
    }

    /**
     * Initialize the pictureSize view which will add to quick switcher.
     * @return the view add to quick switcher
     */
    private ImageView initPictureSizeEntryView() {
        Activity activity = mApp.getActivity();
        RotateImageView view = (RotateImageView) activity.getLayoutInflater().inflate(
                R.layout.bv_picturesize_icon, null);
        view.setOnClickListener(mPictureSizeEntryListener);
        return view;
    }

    /**
     * This listener used to monitor the PictureSize quick switch icon click item.
     */
    private final View.OnClickListener mPictureSizeEntryListener = new View.OnClickListener() {
        public void onClick(View view) {
            if (mPictureSize.getEntryValues().size() <= 1) {
                return;
            }
            LogHelper.d(TAG, "[mPictureSizeEntryListener] mPictureSize.getEntryValues().size() = " + mPictureSize.getEntryValues().size());
            initializePictureSizeChoiceView();
            updateChoiceView();
            mApp.getAppUi().showQuickSwitcherOption(mOptionLayout);
        }
    };

    private View.OnClickListener mPictureSizeChoiceViewListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            double value = 1d / 1;
            String stringValue = "";
            if (mPictureSize3Icon == view) {
                value = PictureSizeHelper.RATIO_18_9;
            } else if (mPictureSize2Icon == view) {
                value = PictureSizeHelper.RATIO_4_3;
            } else if (mPictureSize4Icon == view) {
                value = PictureSizeHelper.RATIO_16_9;
            } else {
                value = PictureSizeHelper.RATIO_1_1;
            }
            mApp.getAppUi().hideQuickSwitcherOption();
            updatePictureSizeEntryView(value);
            List<String> entryValues = mPictureSize.getEntryValues();
            for (String value1 : entryValues) {
                if (PictureSizeHelper.getStandardAspectRatio(value1) == value) {
                    stringValue = value1;
                    break;
                }
            }
            mPictureSize.onPictureSizeValueChanged(stringValue);
        }

    };

    /**
     * This function used to high light the current choice for.
     * PictureSize if PictureSize choice view is show.
     */
    private void updateChoiceView() {
        if (PictureSizeHelper.RATIO_18_9 == PictureSizeHelper.getStandardAspectRatio(mPictureSize.getValue())) {
            mPictureSize3Icon.setImageResource(R.drawable.century_ic_size3_pressed);
            mPictureSize2Icon.setImageResource(R.drawable.century_ic_size2_normal);
            mPictureSize1Icon.setImageResource(R.drawable.century_ic_size1_normal);
            mPictureSize4Icon.setImageResource(R.drawable.bv_picturesize_16_9);
        } else if (PictureSizeHelper.RATIO_4_3 == PictureSizeHelper.getStandardAspectRatio(mPictureSize.getValue())) {
            mPictureSize3Icon.setImageResource(R.drawable.century_ic_size3_normal);
            mPictureSize2Icon.setImageResource(R.drawable.century_ic_size2_pressed);
            mPictureSize1Icon.setImageResource(R.drawable.century_ic_size1_normal);
            mPictureSize4Icon.setImageResource(R.drawable.bv_picturesize_16_9);
        } else if (PictureSizeHelper.RATIO_16_9 == PictureSizeHelper.getStandardAspectRatio(mPictureSize.getValue())) {
            mPictureSize3Icon.setImageResource(R.drawable.century_ic_size3_normal);
            mPictureSize2Icon.setImageResource(R.drawable.century_ic_size2_normal);
            mPictureSize1Icon.setImageResource(R.drawable.century_ic_size1_normal);
            mPictureSize4Icon.setImageResource(R.drawable.bv_picturesize_16_9_pressed);
        } else {
            mPictureSize3Icon.setImageResource(R.drawable.century_ic_size3_normal);
            mPictureSize2Icon.setImageResource(R.drawable.century_ic_size2_normal);
            mPictureSize1Icon.setImageResource(R.drawable.century_ic_size1_pressed);
            mPictureSize4Icon.setImageResource(R.drawable.bv_picturesize_16_9);
        }
    }

    private void initializePictureSizeChoiceView() {
        if (mPictureSizeChoiceView == null || mOptionLayout == null) {
            ViewGroup viewGroup =  mApp.getAppUi().getModeRootView();
            mOptionLayout = mApp.getActivity().getLayoutInflater().inflate(
                    R.layout.bv_picturesize_option, viewGroup, false);
            mPictureSizeChoiceView = mOptionLayout.findViewById(R.id.bv_picturesize_choice);
            mPictureSize1Icon = (ImageView) mOptionLayout.findViewById(R.id.bv_picturesize1);
            mPictureSize2Icon = (ImageView) mOptionLayout.findViewById(R.id.bv_picturesize2);
            mPictureSize3Icon = (ImageView) mOptionLayout.findViewById(R.id.bv_picturesize3);
            mPictureSize1Icon.setOnClickListener(mPictureSizeChoiceViewListener);
            mPictureSize2Icon.setOnClickListener(mPictureSizeChoiceViewListener);
            mPictureSize3Icon.setOnClickListener(mPictureSizeChoiceViewListener);
            mPictureSize4Icon = (ImageView) mOptionLayout.findViewById(R.id.bv_picturesize4);
            mPictureSize4Icon.setOnClickListener(mPictureSizeChoiceViewListener);
            //bv wuyonglin add for adapte picture size not support 18:9 20200610 start
            List<String> entryValues = mPictureSize.getEntryValues();
            List<Double> entryValuesDouble = new ArrayList<>();
            for (String value1 : entryValues) {
                entryValuesDouble.add(PictureSizeHelper.getStandardAspectRatio(value1));
            }
            if (!entryValuesDouble.contains(PictureSizeHelper.RATIO_18_9)) {
                mPictureSize3Icon.setVisibility(View.GONE);
                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mPictureSize2Icon.getLayoutParams();
                int marginStart = (mApp.getActivity().getResources().getDisplayMetrics().widthPixels - entryValuesDouble.size() * mApp.getActivity().getResources()
			.getDimensionPixelSize(R.dimen.quick_switcher_icon_width)) / (entryValuesDouble.size() - 1);
            LogHelper.d(TAG, "[initializePictureSizeChoiceView] marginStart = " + marginStart);
		//lp.setMargins(mApp.getActivity().getResources().getDisplayMetrics().widthPixels - 2 * mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.quick_switcher_icon_width) ,0, 0, 0);
                lp.setMarginStart(marginStart);
                mPictureSize2Icon.setLayoutParams(lp);
                RelativeLayout.LayoutParams lp4 = (RelativeLayout.LayoutParams) mPictureSize4Icon.getLayoutParams();
                lp4.setMarginStart(marginStart);
                mPictureSize4Icon.setLayoutParams(lp4);
            }
            if (!entryValuesDouble.contains(PictureSizeHelper.RATIO_4_3)) {
                mPictureSize2Icon.setVisibility(View.GONE);
            }
            if (!entryValuesDouble.contains(PictureSizeHelper.RATIO_1_1)) {
                mPictureSize1Icon.setVisibility(View.GONE);
            }
            if (!entryValuesDouble.contains(PictureSizeHelper.RATIO_16_9)) {
                mPictureSize4Icon.setVisibility(View.GONE);
            }
            //bv wuyonglin add for adapte picture size not support 18:9 20200610 end
        }
    }

    private final IAppUiListener.OnShutterButtonListener mShutterListener =
            new IAppUiListener.OnShutterButtonListener() {

                @Override
                public boolean onShutterButtonFocus(boolean pressed) {
                    if (pressed) {
                        hidePictureSizeChoiceView();
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
}
