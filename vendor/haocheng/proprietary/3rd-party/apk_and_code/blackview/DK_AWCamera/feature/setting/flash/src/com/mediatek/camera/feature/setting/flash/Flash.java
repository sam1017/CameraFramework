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
import android.content.Intent;
import android.provider.MediaStore;
import android.view.View;

import com.mediatek.camera.common.ICameraContext;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.mode.ICameraMode;
import com.mediatek.camera.common.relation.Relation;
import com.mediatek.camera.common.relation.StatusMonitor;
import com.mediatek.camera.common.setting.ISettingManager;
import com.mediatek.camera.common.setting.SettingBase;

import java.util.List;

import javax.annotation.Nonnull;

//add by huangfei for flash default start
import com.mediatek.camera.R;
//add by huangfei for flash default end
//bv wuyonglin add for restore settings flash not change to default value 20200929 start
import com.mediatek.camera.common.IAppUi;
import com.mediatek.camera.Config;
//bv wuyonglin add for restore settings flash not change to default value 20200929 end

/**
 * Class used to handle flash feature flow.
 */
public class Flash extends SettingBase {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(Flash.class.getSimpleName());

    public static final String FLASH_AUTO_VALUE = "auto";
    public static final String FLASH_OFF_VALUE = "off";
    public static final String FLASH_ON_VALUE = "on";
    public static final String FLASH_TORCH_VALUE = "torch";
    private static final String FLASH_DEFAULT_VALUE = "off";
    private static final String FLASH_KEY = "key_flash";
    private static final String KEY_CSHOT = "key_continuous_shot";
    private static final String VALUE_CSHOT_START = "start";
    private static final String VALUE_CSHOT_STOP = "stop";
    private ICameraMode.ModeType mModeType;
    private String mCurrentMode = "com.mediatek.camera.common.mode.photo.PhotoMode";
    private String mSdofMode = "com.mediatek.camera.feature.mode.vsdof.photo.SdofPhotoMode";
    private String mSdofVideoMode = "com.mediatek.camera.feature.mode.vsdof.video.SdofVideoMode";
    private String mLongExposureMode =
            "com.mediatek.camera.feature.mode.longexposure.LongExposureMode";
    private String mPanoramaMode =
            "com.mediatek.camera.feature.mode.panorama.PanoramaMode";
    private String mHdrMode =
            "com.mediatek.camera.feature.mode.hdr.HdrMode";
    //add by huangfei for flash supoort start
    private String mMonoMode = "com.mediatek.camera.feature.mode.mono.MonoMode";
    private String mProMode ="com.mediatek.camera.feature.mode.pro.ProMode";
    private String mPiBokeh ="com.mediatek.camera.feature.mode.pibokeh.PiBokehMode";
    //add by huangfei for flash supoort end
    //bv wuyonglin add for flash always bright after open hdr flash still always bright 20200104 start
    //private ICaptureRequestConfigure mFlashRequestConfigure;
    private FlashRequestConfigure mFlashRequestConfigure;
    //bv wuyonglin add for flash always bright after open hdr flash still always bright 20200104 end
    private FlashViewController mFlashViewController;
    private ISettingChangeRequester mSettingChangeRequester;

    private static final String VIDEO_STATUS_KEY = "key_video_status";
    private static final String VIDEO_STATUS_RECORDING = "recording";
    private static final String VIDEO_STATUS_PREVIEW = "preview";
    // [Add for CCT tool] Receive keycode and set flash on/set flash off @{
    private IApp.KeyEventListener mKeyEventListener;
    // @}
    private boolean isInit = false;

    //bv wuyonglin add for restore settings flash not change to default value 20200929 start
    private String mDefaultValue = "";
    //bv wuyonglin add for restore settings flash not change to default value 20200929 end

    @Override
    public void init(IApp app,
                     ICameraContext cameraContext,
                     ISettingManager.SettingController settingController) {
        super.init(app, cameraContext, settingController);
        String value = null;
	if (getCameraId() != 1) {
        value = mDataStore.getValue(FLASH_KEY, FLASH_DEFAULT_VALUE, "_preferences_0");
	} else {
        value = mDataStore.getValue(FLASH_KEY, FLASH_DEFAULT_VALUE, getStoreScope());
	}
	isInit = true;
        setValue(value);
        if (mFlashViewController == null) {
            mFlashViewController = new FlashViewController(this, app);
        }
        mStatusMonitor.registerValueChangedListener(VIDEO_STATUS_KEY, mStatusChangeListener);
        mStatusMonitor.registerValueChangedListener(KEY_CSHOT, mStatusChangeListener);
        // [Add for CCT tool] Receive keycode and enable/disable flash @{
        mKeyEventListener = mFlashViewController.getKeyEventListener();
        mApp.registerKeyEventListener(mKeyEventListener, IApp.DEFAULT_PRIORITY);
        // @}
        //bv wuyonglin add for restore settings flash not change to default value 20200929 start
        if (getCameraId() != 1) {
            mAppUi.setRestoreSettingListener(mRestoreSettingListener);
        }
        //bv wuyonglin add for restore settings flash not change to default value 20200929 end
    }

    @Override
    public void unInit() {
        mFlashViewController.hideFlashScreenHint();
        mStatusMonitor.unregisterValueChangedListener(VIDEO_STATUS_KEY,
                mStatusChangeListener);
        mStatusMonitor.unregisterValueChangedListener(KEY_CSHOT, mStatusChangeListener);
	isInit = false;
        // [Add for CCT tool] Receive keycode and enable/disable flash @{
        if (mKeyEventListener != null) {
            mApp.unRegisterKeyEventListener(mKeyEventListener);
        }
        // @}
        //bv wuyonglin add for restore settings flash not change to default value 20200929 start
        if (getCameraId() != 1) {
            mAppUi.removeRestoreSettingListener(mRestoreSettingListener);
        }
        //bv wuyonglin add for restore settings flash not change to default value 20200929 end
    }

    @Override
    public void addViewEntry() {
	if (getEntryValues().size() > 1) {	//bv wuyonglin add for quickswitcher icon margin 20191226
        mFlashViewController.addQuickSwitchIcon();
        mFlashViewController.showQuickSwitchIcon(getEntryValues().size() > 1);
	//bv wuyonglin add for some quickSwitch icon posistion change quickly 20200113 start
	} else {
        mFlashViewController.removeQuickSwitchIcon();
	}
	//bv wuyonglin add for some quickSwitch icon posistion change quickly 20200113 end
    }

    @Override
    public void removeViewEntry() {
        mFlashViewController.removeQuickSwitchIcon();
    }

    @Override
    public void refreshViewEntry() {
        int num = getEntryValues().size();
        if (num <= 1) {
            mFlashViewController.showQuickSwitchIcon(false);
        } else {
            //add by huangfei for lowpower tips start
            mFlashViewController.regBatteryBroadcastReceiver();
			//add by huangfei for lowpower tips end
            //modify by huangfei flash icon blinks start
            //mFlashViewController.showQuickSwitchIcon(true);
            if (isFlashSupportedInCurrentMode()) {
                mFlashViewController.showQuickSwitchIcon(true);
            }
            //modify by huangfei flash icon blinks end
        }
    }

    @Override
    public void onModeOpened(String modeKey, ICameraMode.ModeType modeType) {
        mCurrentMode = modeKey;
        mModeType = modeType;
        if (ICameraMode.ModeType.VIDEO == mModeType) {
            setValue("off");
        }
    }

    @Override
    public void onModeClosed(String modeKey) {
        //add by huangfei for lowpower tips start
        mFlashViewController.hideLowBatteryWarning();
		//add by huangfei for lowpower tips end
        mFlashViewController.hideFlashChoiceView();
        if (mFlashRequestConfigure != null) {
            ((FlashRequestConfigure) mFlashRequestConfigure).changeFlashToTorchByAeState(false);
        }
        super.onModeClosed(modeKey);
		//add by huangfei for lowpower tips start
        mFlashViewController.unregBatteryBroadcastReceiver();
		//add by huangfei for lowpower tips end
    }

    @Override
    public SettingType getSettingType() {
        return SettingType.PHOTO_AND_VIDEO;
    }

    @Override
    public String getKey() {
        return FLASH_KEY;
    }

    @Override
    public ICaptureRequestConfigure getCaptureRequestConfigure() {
        if (mFlashRequestConfigure == null) {
            mFlashRequestConfigure = new FlashRequestConfigure(mActivity.getApplicationContext(),
                    this, mSettingDevice2Requester);//HCT.ouyang
        }
        mSettingChangeRequester = mFlashRequestConfigure;
        return mFlashRequestConfigure;
    }

    @Override
    public void overrideValues(@Nonnull String headerKey, String currentValue,
                               List<String> supportValues) {
        LogHelper.d(TAG, "[overrideValues] headerKey = " + headerKey
                + " ,currentValue = " + currentValue + ",supportValues = " + supportValues+" getEntryValues().size() ="+getEntryValues().size());
        if (headerKey.equals("key_scene_mode") && mSettingController.queryValue("key_scene_mode")
                .equals("hdr")) {
            return;
        }
        String lastValue = getValue();
        if (headerKey.equals("key_hdr") && currentValue != null && (currentValue != lastValue) && ICameraMode.ModeType.VIDEO != getCurrentModeType()) {
            onFlashValueChanged(currentValue);
            //bv wuyonglin add for flash always bright after open hdr flash still always bright 20200104 start
            LogHelper.d(TAG, "[overrideValues], setHdrOpenFlag: lastValue ="+lastValue);
            if (FLASH_TORCH_VALUE.equals(lastValue)) {
                mFlashRequestConfigure.setHdrOpenFlag();
            }
            //bv wuyonglin add for flash always bright after open hdr flash still always bright 20200104 end
        }

        if (!headerKey.equals("key_hdr")) {
            super.overrideValues(headerKey, currentValue, supportValues);
            if (!lastValue.equals(getValue())) {
                Relation relation = FlashRestriction.getFlashRestriction()
                        .getRelation(getValue(), true);
                mSettingController.postRestriction(relation);
            }
            //*/ hct.huangfei, 20201203.flash icon blinks.
            /*mActivity.runOnUiThread(new Runnable() {
                public void run() {
                    if (supportValues != null) {
                        mFlashViewController.showQuickSwitchIcon(supportValues.size() > 1);
                    } else {
                        if (isFlashSupportedInCurrentMode()) {
                            mFlashViewController.showQuickSwitchIcon(getEntryValues().size() > 1);
                        }
                    }
                }
            });*/
            //*/
        }
    }

    @Override
    public void postRestrictionAfterInitialized() {
        Relation relation = FlashRestriction.getFlashRestriction().getRelation(getValue(), false);
        if (relation != null) {
            mSettingController.postRestriction(relation);
        }
    }

    /**
     * Get current mode type.
     *
     * @return mModeType current mode type.
     */
    public ICameraMode.ModeType getCurrentModeType() {
        return mModeType;
    }

    /**
     * Called when flash value changed.
     *
     * @param value The new value.
     */
    public void onFlashValueChanged(String value) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (!value.equals(getValue())) {
                    LogHelper.i(TAG, "[onFlashValueChanged] value = " + value+" mModeType ="+mModeType);
                    setValue(value);
                    mSettingController.postRestriction(
                            FlashRestriction.getFlashRestriction().getRelation(value, true));
                    //bv wuyonglin add for bug1766 20200801 start
                    //if (ICameraMode.ModeType.VIDEO != mModeType) {
                            mSettingController.refreshViewEntry();
                    //}
                    //bv wuyonglin add for bug1766 20200801 end
                    //mSettingChangeRequester.sendSettingChangeRequest();
                    if (!value.equals(FLASH_TORCH_VALUE) && !(value.equals(FLASH_OFF_VALUE) && ICameraMode.ModeType.VIDEO == mModeType)) {
			    if (getCameraId() != 1) {
                                mDataStore.setValue(FLASH_KEY, value, "_preferences_0", false, true);
			    } else {
                                mDataStore.setValue(FLASH_KEY, value, getStoreScope(), false, true);
			    }
                    }
                    //bv wuyonglin add for hd shot 20201013 start
                    if (Config.isAisSupport(mActivity.getApplicationContext())) {
                    if (ICameraMode.ModeType.VIDEO != mModeType) {
                        mSettingDevice2Requester.requestRestartSession();
                    }
                    }
                    mSettingChangeRequester.sendSettingChangeRequest();
                    //bv wuyonglin add for hd shot 20201013 end
                }
            }
        });
    }

    protected boolean isThirdPartyIntent() {
        Activity activity = mApp.getActivity();
        Intent intent = activity.getIntent();
        String action = intent.getAction();
        boolean value = MediaStore.ACTION_IMAGE_CAPTURE.equals(action) ||
                MediaStore.ACTION_VIDEO_CAPTURE.equals(action);
        return value;
    }

    private StatusMonitor.StatusChangeListener mStatusChangeListener = new StatusMonitor
            .StatusChangeListener() {

        @Override
        public void onStatusChanged(String key, String value) {
            LogHelper.d(TAG, "[onStatusChanged] + key " + key + "," +
                    "value " + value);
            switch (key) {
                case VIDEO_STATUS_KEY:
                    //only API2 need to check whether ae state flash required
                    if (mFlashRequestConfigure == null) {
                        return;
                    }
                    if (VIDEO_STATUS_RECORDING.equals(value)) {
                        ((FlashRequestConfigure) mFlashRequestConfigure)
                                .changeFlashToTorchByAeState(true);
                    } else if (VIDEO_STATUS_PREVIEW.equals(value)) {
                        ((FlashRequestConfigure) mFlashRequestConfigure)
                                .changeFlashToTorchByAeState(false);
                    }
                    break;
                case KEY_CSHOT:
                    //only API2 need to check whether ae state flash required
                    if (mFlashRequestConfigure == null) {
                        return;
                    }
                    if (VALUE_CSHOT_START.equals(value)) {
                        ((FlashRequestConfigure) mFlashRequestConfigure)
                                .changeFlashToTorchByAeState(true);
                    } else if (VALUE_CSHOT_STOP.equals(value)) {
                        ((FlashRequestConfigure) mFlashRequestConfigure)
                                .changeFlashToTorchByAeState(false);
                    }
                    break;
                default:
                    break;
            }
            LogHelper.d(TAG, "[onStatusChanged] -");
        }
    };

    private boolean isFlashSupportedInCurrentMode() {
        return !mCurrentMode.equals(mLongExposureMode) && !mCurrentMode.equals(mSdofMode)
                &&!mCurrentMode.equals(mPanoramaMode)&&!mCurrentMode.equals(mHdrMode)
				&&!mCurrentMode.equals(mMonoMode)&&!mCurrentMode.equals(mProMode) && !mCurrentMode.equals(mSdofVideoMode);
    }

    //add by huangfei for flash default start
    public void initializeValue(List<String> platformSupportedValues,
                                String defaultValue) {
        LogHelper.d(TAG, "[initializeValue], platformSupportedValues:" + platformSupportedValues
                + ", defaultValue:" + defaultValue);
        //bv wuyonglin add for restore settings flash not change to default value 20200929 start
        mDefaultValue = defaultValue;
        //bv wuyonglin add for restore settings flash not change to default value 20200929 end
        if (ICameraMode.ModeType.VIDEO != mModeType) {
        String value = FLASH_DEFAULT_VALUE;
        if (platformSupportedValues != null && platformSupportedValues.size() > 1) {
            if (getCameraId() != 1) {
            value = mDataStore.getValue(getKey(), defaultValue, "_preferences_0");
            } else {
            value = mDataStore.getValue(getKey(), defaultValue, getStoreScope());
            }
        }
        setValue(value);
	}
    }		
	//add by huangfei for flash default end

    //bv wuyonglin add for restore settings flash not change to default value 20200929 start

    protected int getCameraId() {
        int cameraId = Integer.parseInt(mSettingController.getCameraId());
        return cameraId;
    }

    private IAppUi.RestoreSettingListener mRestoreSettingListener = new IAppUi.RestoreSettingListener() {
        @Override
        public void restoreSettingtoValue() {
            LogHelper.i(TAG, "restoreSettingtoValue mDefaultValue ="+mDefaultValue);
            setValue(mDefaultValue);
            //bv wuyonglin add for hd shot 20201013 start
            if (ICameraMode.ModeType.VIDEO != mModeType) {
                    if (getCameraId() != 1) {
                        mDataStore.setValue(FLASH_KEY, mDefaultValue, "_preferences_0", false, true);
                    } else {
                        mDataStore.setValue(FLASH_KEY, mDefaultValue, getStoreScope(), false, true);
                    }
            }
            //bv wuyonglin add for hd shot 20201013 end
            mSettingController.postRestriction(
		    FlashRestriction.getFlashRestriction().getRelation(mDefaultValue, true));
            if (ICameraMode.ModeType.VIDEO != mModeType) {
                    mSettingController.refreshViewEntry();
            }
            mSettingChangeRequester.sendSettingChangeRequest();
        }
    };
    //bv wuyonglin add for restore settings flash not change to default value 20200929 end
}
