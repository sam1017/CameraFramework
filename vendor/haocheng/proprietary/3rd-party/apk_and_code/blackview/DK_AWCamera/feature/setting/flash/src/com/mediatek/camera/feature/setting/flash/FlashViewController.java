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
package com.mediatek.camera.feature.setting.flash;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.KeyEvent;
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

/**
 * This class manages the looks of the flash and flash mode choice view.
 */
public class FlashViewController {
    private static final LogUtil.Tag TAG =
            new LogUtil.Tag(FlashViewController.class.getSimpleName());

    private static final int FLASH_ENTRY_LIST_SWITCH_SIZE = 2;
    private static final int FLASH_ENTRY_LIST_INDEX_0 = 0;
    private static final int FLASH_ENTRY_LIST_INDEX_1 = 1;
    private static final int FLASH_PRIORITY = 30;
    private static final int FLASH_SHUTTER_PRIORITY = 70;

    private static final String FLASH_AUTO_VALUE = "auto";
    private static final String FLASH_OFF_VALUE = "off";
    private static final String FLASH_ON_VALUE = "on";
    private static final String FLASH_TORCH_VALUE = "torch";

    private static final int FLASH_VIEW_INIT = 0;
    private static final int FLASH_VIEW_ADD_QUICK_SWITCH = 1;
    private static final int FLASH_VIEW_REMOVE_QUICK_SWITCH = 2;
    private static final int FLASH_VIEW_HIDE_CHOICE_VIEW = 3;
    private static final int FLASH_VIEW_UPDATE_QUICK_SWITCH_ICON = 4;

	//add by huangfei for lowpower tips start
    private static final int FLASH_VIEW_LOW_BATTERY_STATUS = 5;
	//add by huangfei for lowpower tips end
    private ImageView mFlashEntryView;
    private ImageView mFlashIndicatorView;
    private ImageView mFlashOffIcon;
    private ImageView mFlashAutoIcon;
    private ImageView mFlashOnIcon;
    private ImageView mFlashTorchIcon;
    private View mFlashChoiceView;
    private View mOptionLayout;
    private final Flash mFlash;
    private final IApp mApp;
    private MainHandler mMainHandler;

	//add by huangfei for lowpower tips start
	private int mBatteryLimit = 0;
	private boolean mFrontTips = false;
    private CameraActivity mCameraActivity;
   private static final int SHOW_INFO_LENGTH_LONG = 5 * 1000;
    private IAppUi.HintInfo mGuideHint;
    private IAppUi.HintInfo mFlashHint;
    private BatteryMonitor mBatteryMonitor = new BatteryMonitor();
    private boolean mIsBatteryReg = false;
    private boolean mIsLowBatteryStatus = false;
	//add by huangfei for lowpower tips end

    private String mFlashOpenIndicator = "";
    private String mFlashAlwaysBrightIndicator = "";
    /**
     * Constructor of flash view.
     * @param flash Flash instance.
     * @param app   The application app level controller.
     */
    public FlashViewController(Flash flash, IApp app) {
        mFlash = flash;
        mApp = app;
        mMainHandler = new MainHandler(app.getActivity().getMainLooper());
        mMainHandler.sendEmptyMessage(FLASH_VIEW_INIT);
		//add by huangfei for lowpower tips start
        mGuideHint = new IAppUi.HintInfo();
        int id = mApp.getActivity().getResources().getIdentifier("hint_text_background",
                "drawable", mApp.getActivity().getPackageName());
        mGuideHint.mBackground = mApp.getActivity().getDrawable(id);
        mGuideHint.mType = IAppUi.HintType.TYPE_AUTO_HIDE;
        mGuideHint.mDelayTime = SHOW_INFO_LENGTH_LONG;
        mGuideHint.mHintText = mApp.getActivity().getString(R.string.flash_low_battery_warning);
		mBatteryLimit = mApp.getActivity().getResources().getInteger(R.integer.warning_low_power_value);
		mFrontTips = mApp.getActivity().getResources().getBoolean(R.bool.config_show_low_power_tip_of_front);
        mCameraActivity = (CameraActivity )mApp.getActivity();
		//add by huangfei for lowpower tips end
        mFlashOpenIndicator = mApp.getActivity().getString(R.string.flash_open_warning);
        mFlashAlwaysBrightIndicator = mApp.getActivity().getString(R.string.flash_always_bright_warning);
        mFlashHint = new IAppUi.HintInfo();
        mFlashHint.mBackground = mApp.getActivity().getDrawable(R.drawable.focus_hint_background);
        mFlashHint.mType = IAppUi.HintType.TYPE_ALWAYS_TOP;
                LogHelper.i(TAG, "FlashViewController FLASH_VIEW_INIT mFlashHint ="+mFlashHint);
    }

    /**
     * add flash switch to quick switch.
     */
    public void addQuickSwitchIcon() {

        mMainHandler.sendEmptyMessage(FLASH_VIEW_ADD_QUICK_SWITCH);
    }

    /**
     * remove qiuck switch icon.
     */
    public void removeQuickSwitchIcon() {

        mMainHandler.sendEmptyMessage(FLASH_VIEW_REMOVE_QUICK_SWITCH);
    }

    /**
     * for overrides value, for set visibility.
     * @param isShow true means show.
     */
    public void showQuickSwitchIcon(boolean isShow) {
        mMainHandler.obtainMessage(FLASH_VIEW_UPDATE_QUICK_SWITCH_ICON, isShow).sendToTarget();
    }

    public void hideFlashScreenHint() {
                LogHelper.i(TAG, "hideFlashScreenHint mFlashHint ="+mFlashHint);
        mApp.getAppUi().hideScreenHint(mFlashHint);
    }

    /**
     * close option menu.
     */
    public void hideFlashChoiceView() {
        mMainHandler.sendEmptyMessage(FLASH_VIEW_HIDE_CHOICE_VIEW);
    }
	//add by huangfei for lowpower tips start
    public void showLowBatteryWarning() {
        if (mGuideHint != null) {
            mApp.getAppUi().showScreenHint(mGuideHint);
        }
    }

    public void hideLowBatteryWarning() {
        if (mGuideHint != null) {
            mApp.getAppUi().hideScreenHint(mGuideHint);
        }
    }

    class BatteryMonitor extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
			 if("1".equals(mApp.getAppUi().getCameraId())&&!mFrontTips){
                    return;
                }
            if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
                int level = intent.getIntExtra("level", 0);
                LogHelper.i(TAG, "mBatteryLimit==="+mBatteryLimit);
				if(level < mBatteryLimit) {			
                    mIsLowBatteryStatus = true;
                    hideLowBatteryWarning();
                    showLowBatteryWarning();
                    mMainHandler.obtainMessage(FLASH_VIEW_LOW_BATTERY_STATUS).sendToTarget();
                } else {
                    mIsLowBatteryStatus = false;
                }
            }
        }
    }

    public void regBatteryBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        if (!mIsBatteryReg) {
            mApp.getActivity().registerReceiver(mBatteryMonitor, intentFilter);
            mIsBatteryReg = true;
        }
    }

    public void unregBatteryBroadcastReceiver() {
        if (mIsBatteryReg) {
            mApp.getActivity().unregisterReceiver(mBatteryMonitor);
            mIsBatteryReg = false;
        }
    }
	//add by huangfei for lowpower tips end
    // [Add for CCT tool] Receive keycode and enable/disable ZSD @{
    protected IApp.KeyEventListener getKeyEventListener() {
        return new IApp.KeyEventListener() {
            @Override
            public boolean onKeyDown(int keyCode, KeyEvent event) {
                if ((keyCode != CameraUtil.KEYCODE_SET_FLASH_ON
                        && keyCode != CameraUtil.KEYCODE_SET_FLASH_OFF)
                        || !CameraUtil.isSpecialKeyCodeEnabled()) {
                    return false;
                }
                return true;
            }

            @Override
            public boolean onKeyUp(int keyCode, KeyEvent event) {
                if (!CameraUtil.isSpecialKeyCodeEnabled()) {
                    return false;
                }
                if (keyCode != CameraUtil.KEYCODE_SET_FLASH_ON
                        && keyCode != CameraUtil.KEYCODE_SET_FLASH_OFF) {
                    return false;
                }
                if (mFlashEntryView == null) {
                    LogHelper.e(TAG, "[onKeyUp] mFlashEntryView is null");
                    return false;
                }

                if (keyCode == CameraUtil.KEYCODE_SET_FLASH_ON) {
                    LogHelper.i(TAG, "[onKeyUp] update flash on");
                    updateFlashEntryView(FLASH_ON_VALUE);
                    mFlash.onFlashValueChanged(FLASH_ON_VALUE);
                } else if (keyCode == CameraUtil.KEYCODE_SET_FLASH_OFF) {
                    LogHelper.i(TAG, "[onKeyUp] update flash off");
                    updateFlashEntryView(FLASH_OFF_VALUE);
                    mFlash.onFlashValueChanged(FLASH_OFF_VALUE);
                }
                return true;
            }
        };
    }
    // @}

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
                case FLASH_VIEW_INIT:
                    mFlashEntryView = initFlashEntryView();
                    break;

                case FLASH_VIEW_ADD_QUICK_SWITCH:
                    mApp.getAppUi().addToQuickSwitcher(mFlashEntryView, FLASH_PRIORITY);
                    updateFlashEntryView(mFlash.getValue());
                    mApp.getAppUi().registerOnShutterButtonListener(mShutterListener,
                            FLASH_SHUTTER_PRIORITY);
                    break;

                case FLASH_VIEW_REMOVE_QUICK_SWITCH:
		    mApp.getAppUi().hideScreenHint(mFlashHint);
                    mApp.getAppUi().removeFromQuickSwitcher(mFlashEntryView);
                    //updateFlashIndicator(false);
                    mApp.getAppUi().unregisterOnShutterButtonListener(mShutterListener);
                    break;

                case FLASH_VIEW_UPDATE_QUICK_SWITCH_ICON:
                    if ((boolean) msg.obj) {
                        mFlashEntryView.setVisibility(View.VISIBLE);
                        updateFlashEntryView(mFlash.getValue());
                    } else {
			//bv wuyonglin add for panorama mode flash hint should hide 20200104 start
			mApp.getAppUi().hideScreenHint(mFlashHint);
			//bv wuyonglin add for panorama mode flash hint should hide 20200104 end
                        mFlashEntryView.setVisibility(View.GONE);
                    }
                    break;

                case FLASH_VIEW_HIDE_CHOICE_VIEW:
                    //modify by huangfei for flashChoiceView did not hide when mode closed start
                    //if (mFlashChoiceView != null && mFlashChoiceView.isShown()) {
                    if (mFlashChoiceView != null) {
                    //modify by huangfei for flashChoiceView did not hide when mode closed end
                        mApp.getAppUi().hideQuickSwitcherOption();
                        updateFlashEntryView(mFlash.getValue());
                        // Flash indicator no need to show now,would be enable later
                        // updateFlashIndicator(mFlash.getValue());
                    }
                    break;
				//add by huangfei for lowpower tips start
				case FLASH_VIEW_LOW_BATTERY_STATUS:
                    mFlash.onFlashValueChanged(FLASH_OFF_VALUE);
                    break;
				//add by huangfei for lowpower tips end	
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
    public void updateFlashEntryView(final String value) {
        LogHelper.d(TAG, "[updateFlashView] currentValue = " + mFlash.getValue());
        if (FLASH_ON_VALUE.equals(value)) {
            mFlashEntryView.setImageResource(R.drawable.century_ic_flash_on_pressed);
            mFlashEntryView.setContentDescription(mApp.getActivity().getResources().getString(
                    R.string.accessibility_flash_on));
            mApp.getAppUi().hideScreenHint(mFlashHint);
            mFlashHint.mHintText = mFlashOpenIndicator;
	    mApp.getAppUi().showScreenHint(mFlashHint);
        } else if (FLASH_AUTO_VALUE.equals(value)) {
            mFlashEntryView.setImageResource(R.drawable.century_ic_flash_auto_normal);
            mFlashEntryView.setContentDescription(mApp.getActivity().getResources().getString(
                    R.string.accessibility_flash_auto));
	    mApp.getAppUi().hideScreenHint(mFlashHint);
        } else if (FLASH_TORCH_VALUE.equals(value)) {
            mFlashEntryView.setImageResource(R.drawable.century_ic_flash_torch_pressed);
            mFlashEntryView.setContentDescription(mApp.getActivity().getResources().getString(
                    R.string.accessibility_flash_on));
            mApp.getAppUi().hideScreenHint(mFlashHint);
            mFlashHint.mHintText = mFlashAlwaysBrightIndicator;
            mApp.getAppUi().showScreenHint(mFlashHint);
        } else {
            mFlashEntryView.setImageResource(R.drawable.century_ic_flash_off_normal);
            mFlashEntryView.setContentDescription(mApp.getActivity().getResources().getString(
                    R.string.accessibility_flash_off));
	    mApp.getAppUi().hideScreenHint(mFlashHint);
        }
        // Flash indicator no need to show now,would be enable later
        // updateFlashIndicator(value);
    }

    /**
     * Initialize the flash view which will add to quick switcher.
     * @return the view add to quick switcher
     */
    private ImageView initFlashEntryView() {
        Activity activity = mApp.getActivity();
        RotateImageView view = (RotateImageView) activity.getLayoutInflater().inflate(
                R.layout.flash_icon, null);
        view.setOnClickListener(mFlashEntryListener);
        mFlashIndicatorView = (RotateImageView) activity.getLayoutInflater().inflate(
                R.layout.flash_indicator, null);
        return view;
    }

    /**
     * This listener used to monitor the flash quick switch icon click item.
     */
    private final View.OnClickListener mFlashEntryListener = new View.OnClickListener() {
        public void onClick(View view) {
            if (mFlash.getEntryValues().size() <= 1) {
                return;
            }
            if(CameraUtil.isVideo_HDR_changing) {
                LogHelper.i(TAG,"mFlashEntryListener isVideo_HDR_changing = true return");
                return;
            }
                //add by huangfei for lowpower tips start
            if(mIsLowBatteryStatus){
            	hideLowBatteryWarning();
                showLowBatteryWarning();
                return;
            }
            if (mFlash.getEntryValues().size() > FLASH_ENTRY_LIST_SWITCH_SIZE) {
                initializeFlashChoiceView();
                updateChoiceView();
                mApp.getAppUi().showQuickSwitcherOption(mOptionLayout);
            } else {
                String value = mFlash.getEntryValues().get(FLASH_ENTRY_LIST_INDEX_0);
                if (value.equals(mFlash.getValue())) {
                    value = mFlash.getEntryValues().get(FLASH_ENTRY_LIST_INDEX_1);
                }
                updateFlashEntryView(value);
                // Flash indicator no need to show now,would be enable later
                // updateFlashIndicator(value);
                mFlash.onFlashValueChanged(value);
            }
        }
    };

    private View.OnClickListener mFlashChoiceViewListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String value = "";
            if (mFlashAutoIcon == view) {
                value = FLASH_AUTO_VALUE;
            } else if (mFlashOnIcon == view) {
                value = FLASH_ON_VALUE;
            } else if (mFlashTorchIcon == view) {
                value = FLASH_TORCH_VALUE;
            } else {
                value = FLASH_OFF_VALUE;
            }
            mApp.getAppUi().hideQuickSwitcherOption();
            updateFlashEntryView(value);
            // Flash indicator no need to show now,would be enable later
            // updateFlashIndicator(value);
            mFlash.onFlashValueChanged(value);
        }

    };

    private void updateFlashIndicator(final boolean value) {
        if (value) {
            mApp.getAppUi().addToIndicatorView(mFlashIndicatorView, FLASH_PRIORITY);
        } else {
            mApp.getAppUi().removeFromIndicatorView(mFlashIndicatorView);
        }
    }

    /**
     * This function used to high light the current choice for.
     * flash if flash choice view is show.
     */
    private void updateChoiceView() {
        if (FLASH_ON_VALUE.equals(mFlash.getValue())) {
            mFlashOnIcon.setImageResource(R.drawable.century_ic_flash_on_pressed);
            mFlashOffIcon.setImageResource(R.drawable.century_ic_flash_off_normal);
            mFlashAutoIcon.setImageResource(R.drawable.century_ic_flash_auto_normal);
            mFlashTorchIcon.setImageResource(R.drawable.century_ic_flash_torch_normal);
        } else if (FLASH_OFF_VALUE.equals(mFlash.getValue())) {
            mFlashOnIcon.setImageResource(R.drawable.century_ic_flash_on_normal);
            mFlashOffIcon.setImageResource(R.drawable.century_ic_flash_off_pressed);
            mFlashAutoIcon.setImageResource(R.drawable.century_ic_flash_auto_normal);
            mFlashTorchIcon.setImageResource(R.drawable.century_ic_flash_torch_normal);
        } else if (FLASH_TORCH_VALUE.equals(mFlash.getValue())) {
            mFlashOnIcon.setImageResource(R.drawable.century_ic_flash_on_normal);
            mFlashOffIcon.setImageResource(R.drawable.century_ic_flash_off_normal);
            mFlashAutoIcon.setImageResource(R.drawable.century_ic_flash_auto_normal);
            mFlashTorchIcon.setImageResource(R.drawable.century_ic_flash_torch_pressed);
        } else {
            mFlashOnIcon.setImageResource(R.drawable.century_ic_flash_on_normal);
            mFlashOffIcon.setImageResource(R.drawable.century_ic_flash_off_normal);
            mFlashAutoIcon.setImageResource(R.drawable.century_ic_flash_auto_pressed);
            mFlashTorchIcon.setImageResource(R.drawable.century_ic_flash_torch_normal);
        }
    }

    private void initializeFlashChoiceView() {
        if (mFlashChoiceView == null || mOptionLayout == null) {
            ViewGroup viewGroup =  mApp.getAppUi().getModeRootView();
            mOptionLayout = mApp.getActivity().getLayoutInflater().inflate(
                    R.layout.flash_option, viewGroup, false);
            mFlashChoiceView = mOptionLayout.findViewById(R.id.flash_choice);
            mFlashOnIcon = (ImageView) mOptionLayout.findViewById(R.id.flash_on);
            mFlashOffIcon = (ImageView) mOptionLayout.findViewById(R.id.flash_off);
            mFlashAutoIcon = (ImageView) mOptionLayout.findViewById(R.id.flash_auto);
            mFlashTorchIcon = (ImageView) mOptionLayout.findViewById(R.id.bv_flash_torch);
            mFlashOffIcon.setOnClickListener(mFlashChoiceViewListener);
            mFlashOnIcon.setOnClickListener(mFlashChoiceViewListener);
            mFlashAutoIcon.setOnClickListener(mFlashChoiceViewListener);
            mFlashTorchIcon.setOnClickListener(mFlashChoiceViewListener);
        }
        if (mFlash.getCurrentModeType() == ICameraMode.ModeType.VIDEO) {
            mFlashOnIcon.setVisibility(View.INVISIBLE);
	    mFlashAutoIcon.setVisibility(View.INVISIBLE);
        } else {
            mFlashOnIcon.setVisibility(View.VISIBLE);
	    mFlashAutoIcon.setVisibility(View.VISIBLE);
	}
    }

    private final IAppUiListener.OnShutterButtonListener mShutterListener =
            new IAppUiListener.OnShutterButtonListener() {

                @Override
                public boolean onShutterButtonFocus(boolean pressed) {
                    if (pressed) {
                        hideFlashChoiceView();
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
