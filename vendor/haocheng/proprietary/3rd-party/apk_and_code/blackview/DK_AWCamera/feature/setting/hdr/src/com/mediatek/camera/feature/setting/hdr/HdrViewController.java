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
package com.mediatek.camera.feature.setting.hdr;

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
import com.mediatek.camera.common.utils.CameraUtil;
import com.mediatek.camera.common.widget.RotateImageView;
//bv wuyonglin add for add hdr hint warn 20200102 start
import com.mediatek.camera.common.IAppUi;
//bv wuyonglin add for add hdr hint warn 20200102 end

/**
 * HdrViewController used to control hdr ui and send view click item
 * to hdr.
 */

public class HdrViewController {
    private static final LogUtil.Tag TAG =
            new LogUtil.Tag(HdrViewController.class.getSimpleName());

    private static final int HDR_ENTRY_LIST_SWITCH_SIZE = 2;
    private static final int HDR_ENTRY_LIST_INDEX_0 = 0;
    private static final int HDR_ENTRY_LIST_INDEX_1 = 1;
    //bv wuyonglin modify for add hdr quickswitch 20191231 start
    //private static final int HDR_PRIORITY = 10;
    private static final int HDR_PRIORITY = 35;
    //bv wuyonglin modify for add hdr quickswitch 20191231 end
    private static final int HDR_SHUTTER_PRIORITY = 60;
    private static final String HDR_AUTO_VALUE = "auto";
    private static final String HDR_OFF_VALUE = "off";
    private static final String HDR_ON_VALUE = "on";

    private static final int HDR_VIEW_INIT = 0;
    private static final int HDR_VIEW_ADD_QUICK_SWITCH = 1;
    private static final int HDR_VIEW_REMOVE_QUICK_SWITCH = 2;
    private static final int HDR_VIEW_UPDATE_INDICATOR = 3;
    private static final int HDR_VIEW_CLOSE_CHOICE_VIEW = 4;
    private static final int HDR_VIEW_UPDATE_QUICK_SWITCH_ICON = 5;

    private ImageView mHdrEntryView;
    private ImageView mHdrIndicatorView;
    private ImageView mHdrAutoIcon;
    private ImageView mHdrOffIcon;
    private ImageView mHdrOnIcon;
    private View mHdrChoiceView;
    private View mChoiceViewLayout;
    private MainHandler mMainHandler;
    private final IApp mApp;
    private final Hdr mHdr;
    //bv wuyonglin add for add hdr hint warn 20200102 start
    private String mHdrOpenIndicator = "";
    private IAppUi.HintInfo mHdrHint;
    //bv wuyonglin add for add hdr hint warn not disappear 20200103 start
    private boolean mHdrHintShow = false;
    //bv wuyonglin add for add hdr hint warn not disappear 20200103 end
    //bv wuyonglin add for add hdr hint warn  20200102 end
    private boolean mNeedRestartSession =false;

    /**
     * The constructor.
     * @param app IApp.
     * @param hdr Hdr.
     */
    public HdrViewController(IApp app, Hdr hdr) {
        mApp = app;
        mHdr = hdr;
        mMainHandler = new MainHandler(app.getActivity().getMainLooper());
        mMainHandler.sendEmptyMessage(HDR_VIEW_INIT);
        //bv wuyonglin add for add hdr hint warn 20200102 start
        mHdrOpenIndicator = mApp.getActivity().getString(R.string.hdr_open_warning);
        mHdrHint = new IAppUi.HintInfo();
        mHdrHint.mBackground = mApp.getActivity().getDrawable(R.drawable.focus_hint_background);
        mHdrHint.mType = IAppUi.HintType.TYPE_ALWAYS_TOP;
        mHdrHint.mHintText = mHdrOpenIndicator;
        //bv wuyonglin add for add hdr hint warn 20200102 end
    }

    /**
     * add flash switch to quick switch.
     */
    public void addQuickSwitchIcon() {

        mMainHandler.sendEmptyMessage(HDR_VIEW_ADD_QUICK_SWITCH);
    }

    /**
     * remove quick switch icon.
     */
    public void removeQuickSwitchIcon() {

        mMainHandler.sendEmptyMessage(HDR_VIEW_REMOVE_QUICK_SWITCH);
    }

    //bv wuyonglin add for add hdr hint warn 20200102 start
    public void hideHdrScreenHint(boolean isShow) {
        //bv wuyonglin modify for add hdr hint warn not disappear 20200103 start
        if (!(isShow && mHdrHintShow)) {
            mMainHandler.obtainMessage(HDR_VIEW_UPDATE_INDICATOR, isShow).sendToTarget();
        }
        //bv wuyonglin modify for add hdr hint warn not disappear 20200103 end
    }
    //bv wuyonglin add for add hdr hint warn 20200102 start

    /**
     * for overrides value, for set visibility.
     * @param isShow true means show.
     */
    public void showQuickSwitchIcon(boolean isShow) {
        mMainHandler.obtainMessage(HDR_VIEW_UPDATE_QUICK_SWITCH_ICON, isShow).sendToTarget();
    }

    /**
     * close hdr option menu.
     */
    public void closeHdrChoiceView() {
        mMainHandler.sendEmptyMessage(HDR_VIEW_CLOSE_CHOICE_VIEW);
    }

    /**
     * show hdr indicator.
     * @param isShow true means show.
     */
    public void showHdrIndicator(boolean isShow) {
        mMainHandler.obtainMessage(HDR_VIEW_UPDATE_INDICATOR, isShow).sendToTarget();
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
            switch (msg.what) {
                case HDR_VIEW_INIT:
                    mHdrEntryView = initHdrEntryView();
                    break;

                case HDR_VIEW_ADD_QUICK_SWITCH:
                    mApp.getAppUi().addToQuickSwitcher(mHdrEntryView, HDR_PRIORITY);
                    updateHdrViewState(mHdr.getValue());
                    mApp.getAppUi().registerOnShutterButtonListener(
                            mShutterListener, HDR_SHUTTER_PRIORITY);
                    break;

                case HDR_VIEW_REMOVE_QUICK_SWITCH:
                    mApp.getAppUi().removeFromQuickSwitcher(mHdrEntryView);
                    updateHdrIndicator(false);
                    mApp.getAppUi().unregisterOnShutterButtonListener(mShutterListener);
                    break;

                case HDR_VIEW_UPDATE_QUICK_SWITCH_ICON:
                    if ((boolean) msg.obj) {
                        mHdrEntryView.setVisibility(View.VISIBLE);
                        updateHdrViewState(mHdr.getValue());
                    } else {
                        mHdrEntryView.setVisibility(View.GONE);
                        updateHdrIndicator(false);
                    }
                    break;

                case HDR_VIEW_UPDATE_INDICATOR:
                    //bv wuyonglin add for add hdr hint warn 20200102 start
                    //updateHdrIndicator((boolean) msg.obj);
                    if ((boolean) msg.obj) {
                        mApp.getAppUi().showScreenHint(mHdrHint);
			//bv wuyonglin add for add hdr hint warn not disappear 20200103 start
			mHdrHintShow = true;
			//bv wuyonglin add for add hdr hint warn not disappear 20200103 end
                    } else {
                        mApp.getAppUi().hideScreenHint(mHdrHint);
			//bv wuyonglin add for add hdr hint warn not disappear 20200103 start
			mHdrHintShow = false;
			//bv wuyonglin add for add hdr hint warn not disappear 20200103 end
                    }
                    //bv wuyonglin add for add hdr hint warn 20200102 end
                    break;

                case HDR_VIEW_CLOSE_CHOICE_VIEW:
                    onChoiceViewClosed();
                    break;

                default:
                    break;
            }
        }
    }

    /**
     * Initialize the hdr view which will add to quick switcher.
     * @return the view add to quick switcher
     */
    private ImageView initHdrEntryView() {
        Activity activity = mApp.getActivity();
        RotateImageView view = (RotateImageView) activity.getLayoutInflater().inflate(
                R.layout.hdr_icon, null);
        CameraUtil.isVideo_HDR_changing = false;
        view.setOnClickListener(mHdrEntryListener);
        mHdrIndicatorView = (RotateImageView) activity.getLayoutInflater().inflate(
                R.layout.hdr_indicator, null);
        return view;
    }

    private void onChoiceViewClosed() {
        if (mHdrChoiceView != null && mHdrChoiceView.isShown()) {
            mApp.getAppUi().hideQuickSwitcherOption();
            updateHdrViewState(mHdr.getValue());
        }
    }

    /**
     * This listener used to monitor the hdr quick switch icon click item.
     */
    private final View.OnClickListener mHdrEntryListener = new View.OnClickListener() {
        public void onClick(View view) {
            if (mHdr.getEntryValues().size() <= 1) {
                return;
            }
            if (mHdr.isVideoRecording()) {
                LogHelper.i(TAG, "onClick, is video recording return");
                return;
            }
            if (mHdr.getEntryValues().size() > HDR_ENTRY_LIST_SWITCH_SIZE) {
                initializeHdrChoiceView();
                updateChoiceView();
                mApp.getAppUi().showQuickSwitcherOption(mChoiceViewLayout);
            } else {
                //no choice view
                //bv wuyonglin add for bug3677 20210204 start
                LogHelper.i(TAG, "onClick, mApp.getAppUi().getVideoStateIsPreview() ="+mApp.getAppUi().getVideoStateIsPreview());
                if (!mApp.getAppUi().getVideoStateIsPreview()) {
                    LogHelper.i(TAG, "onClick, getVideoStateIsPreview is video preview return");
                    return;
                }
                //bv wuyonglin add for bug3677 20210204 end
                if(!CameraUtil.isVideo_HDR_changing){
                    String value = mHdr.getEntryValues().get(HDR_ENTRY_LIST_INDEX_0);
                    CameraUtil.isVideo_HDR_changing = true;
                    if (value.equals(mHdr.getValue())) {
                        value = mHdr.getEntryValues().get(HDR_ENTRY_LIST_INDEX_1);
                    }
                    mNeedRestartSession = true;
                    updateHdrViewState(value);
                    mHdr.onHdrValueChanged(value);
                }
            }
        }
    };

    private final View.OnClickListener mHdrChoiceViewListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String value = "";
            if (mHdrAutoIcon == view) {
                value = HDR_AUTO_VALUE;
            } else if (mHdrOnIcon == view) {
                value = HDR_ON_VALUE;
            } else {
                value = HDR_OFF_VALUE;
            }
            mApp.getAppUi().hideQuickSwitcherOption();
            updateHdrViewState(value);
            mHdr.onHdrValueChanged(value);
        }
    };

    /**
     * Initialize hdr sub items view and add listener for them.
     * Hdr auto choice will not be initialize if auto hdr does not supported.
     */
    private void initializeHdrChoiceView() {
        if (mHdrChoiceView == null || mChoiceViewLayout == null) {
            ViewGroup viewGroup = mApp.getAppUi().getModeRootView();
            mChoiceViewLayout = mApp.getActivity().getLayoutInflater().inflate(
                    R.layout.hdr_option, viewGroup, false);
            mHdrChoiceView = mChoiceViewLayout.findViewById(R.id.hdr_choice);
            mHdrOnIcon = (ImageView) mChoiceViewLayout.findViewById(R.id.hdr_on);
            mHdrOffIcon = (ImageView) mChoiceViewLayout.findViewById(R.id.hdr_off);
            mHdrAutoIcon = (ImageView) mChoiceViewLayout.findViewById(R.id.hdr_auto);
            mHdrOffIcon.setOnClickListener(mHdrChoiceViewListener);
            mHdrOnIcon.setOnClickListener(mHdrChoiceViewListener);
            mHdrAutoIcon.setOnClickListener(mHdrChoiceViewListener);
            //bv wuyonglin add for add hdr quickswitch 20191231 start
            //bv liangchangwei add for aiworks hdr
            if(!CameraUtil.MTKCAM_AIWORKS_HDR_SUPPORT){
                mHdrAutoIcon.setVisibility(View.INVISIBLE);
            }
            //bv wuyonglin add for add hdr quickswitch 20191231 end
        }
    }

    /**
     * This function used to high light the current choice for.
     * hdr if hdr choice view is show.
     */
    private void updateChoiceView() {
        if (HDR_ON_VALUE.equals(mHdr.getValue())) {
            //bv wuyonglin modify for add hdr quickswitch 20191231 start
            /*mHdrOnIcon.setImageResource(R.drawable.ic_hdr_on_selected);
            mHdrOffIcon.setImageResource(R.drawable.ic_hdr_off);
            mHdrAutoIcon.setImageResource(R.drawable.ic_hdr_auto);*/
            mHdrOnIcon.setImageResource(R.drawable.bv_ic_hdr_on_selected);
            mHdrOffIcon.setImageResource(R.drawable.bv_ic_hdr_off);
            mHdrAutoIcon.setImageResource(R.drawable.bv_ic_hdr_auto);
            //bv wuyonglin modify for add hdr quickswitch 20191231 end
        } else if (HDR_OFF_VALUE.equals(mHdr.getValue())) {
            //bv wuyonglin modify for add hdr quickswitch 20191231 start
            /*mHdrOnIcon.setImageResource(R.drawable.ic_hdr_on);
            mHdrOffIcon.setImageResource(R.drawable.ic_hdr_off_selected);
            mHdrAutoIcon.setImageResource(R.drawable.ic_hdr_auto);*/
            mHdrOnIcon.setImageResource(R.drawable.bv_ic_hdr_on);
            mHdrOffIcon.setImageResource(R.drawable.bv_ic_hdr_off_selected);
            mHdrAutoIcon.setImageResource(R.drawable.bv_ic_hdr_auto);
            //bv wuyonglin modify for add hdr quickswitch 20191231 end
        } else {
            //bv wuyonglin modify for add hdr quickswitch 20191231 start
            /*mHdrOnIcon.setImageResource(R.drawable.ic_hdr_on);
            mHdrOffIcon.setImageResource(R.drawable.ic_hdr_off);
            mHdrAutoIcon.setImageResource(R.drawable.ic_hdr_auto_selected);*/
            mHdrOnIcon.setImageResource(R.drawable.bv_ic_hdr_on);
            mHdrOffIcon.setImageResource(R.drawable.bv_ic_hdr_off);
            mHdrAutoIcon.setImageResource(R.drawable.bv_ic_hdr_auto_selected);
            //bv wuyonglin modify for add hdr quickswitch 20191231 end
        }
    }

    private void updateHdrViewState(String value) {
        updateHdrEntryView(value);
        updateHdrIndicator(HDR_ON_VALUE.equals(value));
    }

    /**
     * Update hdr entry view by current hdr value.
     */
    private void updateHdrEntryView(String value) {
        LogHelper.d(TAG, "updateHdrEntryView, value: " + value);
        if (HDR_ON_VALUE.equals(value)) {
            //bv wuyonglin modify for add hdr quickswitch 20191231 start
            //mHdrEntryView.setImageResource(R.drawable.ic_hdr_on_selected);
            mHdrEntryView.setImageResource(R.drawable.bv_ic_hdr_on_selected);
            //bv wuyonglin modify for add hdr quickswitch 20191231 end
            mHdrEntryView.setContentDescription(mApp.getActivity().getResources().getString(
                    R.string.accessibility_hdr_on));
        } else if (HDR_AUTO_VALUE.equals(value)) {
            mHdrEntryView.setImageResource(R.drawable.bv_ic_hdr_auto);
            mHdrEntryView.setContentDescription(mApp.getActivity().getResources().getString(
                    R.string.accessibility_hdr_auto));
        } else {
            //bv wuyonglin modify for add hdr quickswitch 20191231 start
            //mHdrEntryView.setImageResource(R.drawable.ic_hdr_off);
            mHdrEntryView.setImageResource(R.drawable.bv_ic_hdr_off);
            //bv wuyonglin modify for add hdr quickswitch 20191231 end
            mHdrEntryView.setContentDescription(mApp.getActivity().getResources().getString(
                    R.string.accessibility_hdr_off));
        }
    }

    private void updateHdrIndicator(boolean show) {
        //bv wuyonglin detele for add hdr hint warn 20200102 start
        /*if (show) {
            mApp.getAppUi().addToIndicatorView(mHdrIndicatorView, HDR_PRIORITY);
        } else {
            mApp.getAppUi().removeFromIndicatorView(mHdrIndicatorView);
        }*/
        //bv wuyonglin detele for add hdr hint warn 20200102 end
    }

    /* add by bv liangchangwei for fixbug 5674 -- */
    public boolean getmNeedRestartSession(){
        return mNeedRestartSession;
    }

    public void setmNeedRestartSession(boolean enable){
        mNeedRestartSession = enable;
    }
    /* add by bv liangchangwei for fixbug 5674 -- */

    private final IAppUiListener.OnShutterButtonListener mShutterListener =
            new IAppUiListener.OnShutterButtonListener() {

                @Override
                public boolean onShutterButtonFocus(boolean pressed) {
                    if (pressed) {
                        onChoiceViewClosed();
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
