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

package com.mediatek.camera.feature.mode.aicombo.view;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import com.mediatek.camera.R;
import com.mediatek.camera.common.IAppUi;
import com.mediatek.camera.common.ICameraContext;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.relation.DataStore;
import com.mediatek.camera.feature.mode.aicombo.photo.AIComboPhotoMode;
import com.mediatek.camera.feature.mode.aicombo.view.utils.Util;
import com.mediatek.camera.feature.mode.aicombo.view.widget.RotateImageView;
import com.mediatek.camera.feature.mode.aicombo.view.widget.VerticalSeekBar;
import com.mediatek.campostalgo.FeatureParam;
import com.mediatek.campostalgo.FeaturePipeConfig;

import java.util.ArrayList;


import static com.mediatek.camera.feature.mode.aicombo.photo.device.AIComboPhotoDeviceController.POSTALGO_PARAMS_JPEG_ORIENTATION_KEY;

import static com.mediatek.camera.feature.mode.aicombo.photo.device.AIComboPhotoDeviceController.MTK_POSTALGO_AI_COMBO_BIGEYE;
import static com.mediatek.camera.feature.mode.aicombo.photo.device.AIComboPhotoDeviceController.MTK_POSTALGO_AI_COMBO_SMALLCHEEK;
import static com.mediatek.camera.feature.mode.aicombo.photo.device.AIComboPhotoDeviceController.MTK_POSTALGO_AI_COMBO_SMOOTH;
import static com.mediatek.camera.feature.mode.aicombo.photo.device.AIComboPhotoDeviceController.MTK_POSTALGO_AI_COMBO_WHITE;


public class AIBeautyView implements View.OnClickListener {
    private static final LogUtil.Tag TAG
                             = new LogUtil.Tag(AIBeautyView.class.getSimpleName());
    private View mRootView;
    private View mFaceView;
    private ViewGroup mParentViewGroup;

    private int mCameraId = 0;
    private ICameraContext mICameraContext;

    private IApp mApp;

    private String mEffectsKey = null;
    private String mEffectsValue = null;

    private AIBeautyInfo mAIBeautyInfo;

    private int mSupportedDuration = 10;
    private int mSupportedMaxValue = 0;
    private int mCurrentViewIndex = 0;

    private ArrayList<Integer> mAIBeautyPropertiesValue = new ArrayList<Integer>();

    private LinearLayout mBgLinearLayout;
    private VerticalSeekBar mAdjustmentValueIndicator;
    private RotateImageView[] mAIBeautyImageViews = new RotateImageView[NUMBER_AI_BEAUTY_ICON];


    private static final int AI_BEAUTY_WHITENESS = 0;
    private static final int AI_BEAUTY_BRIGHT_EYES = 1;
    private static final int AI_BEAUTY_SLIM_FACE = 2;
    private static final int AI_BEAUTY_SKINTONE = 3;
    private static final int AI_BEAUTY_MODIFY_ICON = 4;
    private static final int AI_BEAUTY_ICON = 5;

    private static final int SUPPORTED_FB_PROPERTIES_MAX_NUMBER = 4;

    // use for hander the effects item hide
    private static final int DISAPPEAR_VFB_UI_TIME = 5000;
    private static final int DISAPPEAR_VFB_UI = 0;

    // Decoupling: 4 will be replaced by parameters
    private int SUPPORTED_FB_EFFECTS_NUMBER = 4;

    // 7 means all the number of icons in the preview
    private static final int NUMBER_AI_BEAUTY_ICON = 6;

    // Because current face.length is 0 always callback ,if not always callback
    // ,will not use this
    private boolean mIsTimeOutMechanismRunning = false;

    /**
     * when AIBeautyMode send MSG show the FB icon but if current is in
     * setting change, AIBeautyMode will receive a parameters Ready MSG so
     * will notify view show.but this case is in the setting,not need show the
     * view if the msg:ON_CAMERA_PARAMETERS_READY split to
     * ON_CAMERA_PARAMETERS_READY and ON_CAMERA_PARAMETERS_CHANGE and the change
     * MSG used for setting change,so can not use mIsShowSetting
     */
    private boolean mIsShowSetting = false;
    private boolean mIsInPictureTakenProgress = false;

    /**
     * this tag is used for judge whether in camera preview for example:camera
     * -> Gallery->play video,now when play the video,camera will execute
     * onPause(),and when the finished play,camera will onResume,so this time
     * AIBeautyMode will receive onCameraOpen and onCameraParameters Ready
     * MSG,so will notify AIBeautyView ,but this view will show the VFB UI,so
     * in this case[not in Camera preview] not need show the UI if
     * AIBeautyView not show the UI,so this not use
     */
    private boolean mIsInCameraPreview = true;

    private Handler mHandler;
    private Handler mModeHandler;
    protected DataStore mDataStore;
    private String mModeType = "Picture";

    public static final String KEY_AI_BEAUTY_WHITENESS = "pref_aibeauty_whiteness_key";
    public static final String KEY_AI_BEAUTY_BRIGHT_EYES = "pref_aibeauty_bright_eyes_key";
    public static final String KEY_AI_BEAUTY_SLIM_FACE = "pref_aibeauty_slim_face_key";
    public static final String KEY_AI_BEAUTY_SKINTONE = "pref_aibeauty_smooth_key";


    public static final String KEY_CAMERA_FACE_BEAUTY_MULTI_MODE_KEY =
            "pref_face_beauty_multi_mode_key";

    private static final int[] AI_BEAUTY_ICONS_NORMAL = new int[NUMBER_AI_BEAUTY_ICON];
    private static final int[] AI_BEAUTY_ICONS_HIGHTLIGHT = new int[NUMBER_AI_BEAUTY_ICON];


    static {
        AI_BEAUTY_ICONS_NORMAL[AI_BEAUTY_WHITENESS] = R.drawable.ic_dy_whiteness;
        AI_BEAUTY_ICONS_NORMAL[AI_BEAUTY_BRIGHT_EYES] = R.drawable.ic_dy_bright_eyes;
        AI_BEAUTY_ICONS_NORMAL[AI_BEAUTY_SLIM_FACE] = R.drawable.ic_dy_slimfacelevel;
        AI_BEAUTY_ICONS_NORMAL[AI_BEAUTY_SKINTONE] = R.drawable.ic_dy_skintone;
        AI_BEAUTY_ICONS_NORMAL[AI_BEAUTY_MODIFY_ICON] = R.drawable.ic_fb_3_hide_on;
        AI_BEAUTY_ICONS_NORMAL[AI_BEAUTY_ICON] = R.drawable.ic_mode_facebeauty_normal;
    }

    static {
        AI_BEAUTY_ICONS_HIGHTLIGHT[AI_BEAUTY_WHITENESS]
                    = R.drawable.ic_dy_whiteness_highlight;
        AI_BEAUTY_ICONS_HIGHTLIGHT[AI_BEAUTY_BRIGHT_EYES]
                    = R.drawable.ic_dy_bright_eyes_highlight;
        AI_BEAUTY_ICONS_HIGHTLIGHT[AI_BEAUTY_SLIM_FACE]
                    = R.drawable.ic_dy_slimfacelevel_highlight;
        AI_BEAUTY_ICONS_HIGHTLIGHT[AI_BEAUTY_SKINTONE] = R.drawable.ic_dy_skintone_highlight;
        AI_BEAUTY_ICONS_HIGHTLIGHT[AI_BEAUTY_MODIFY_ICON]
                    = R.drawable.ic_fb_3_hide_off_highlight;
        AI_BEAUTY_ICONS_HIGHTLIGHT[AI_BEAUTY_ICON] = R.drawable.ic_mode_facebeauty_focus;
    }

    public static final int VALUE_AI_BEAUTY_NO_EFFECTS = -1;

    private FeatureParam param;

    /**
     * The constructor of panorama view.
     *
     * @param app      the instance of IApp.
     * @param cameraId the camera id.
     */
    public AIBeautyView(@NonNull IApp app, int cameraId, String modeType, ICameraContext mICameraContext
            , Handler mModeHandler) {
        mApp = app;
        mCameraId = cameraId;
        mModeType = modeType;
        mParentViewGroup = app.getAppUi().getModeRootView();
        mHandler = new IndicatorHandler(mApp.getActivity().getMainLooper());
        this.mModeHandler = mModeHandler;
        this.mICameraContext = mICameraContext;
        mDataStore = mICameraContext.getDataStore();
    }

    /**
     * init AIBeauty view.
     */
    public void init() {
        getView();
    }

    /**
     * show AIBeauty view.
     */
    public void show() {
        if (mRootView == null) {
            mRootView = getView();
        }
        mRootView.setVisibility(View.VISIBLE);
        mBgLinearLayout.setVisibility(View.VISIBLE);
        mApp.getAppUi().applyAllUIVisibility(View.VISIBLE);
        mApp.getAppUi().setUIVisibility(IAppUi.SCREEN_HINT, View.VISIBLE);
        if (!mIsShowSetting && mIsInCameraPreview) {
            intoAIBeautyMode();
        }
    }

    /**
     * hide AIBeauty view.
     */
    public void hide() {
        if (mRootView == null) {
            return;
        }
        mRootView.setVisibility(View.GONE);
        mApp.getAppUi().applyAllUIVisibility(View.VISIBLE);
    }

    /**
     *  will be called when app call release() to unload views from view
     *  hierarchy.
     */
    public void unInit() {
        if (mParentViewGroup != null) {
            mParentViewGroup.removeView(mRootView);
            mRootView = null;
            mParentViewGroup = null;
        }
    }

    /**
     * reset AIBeauty view.
     */
    public void reset() {
        if (mRootView == null) {
            return;
        }
    }


    public boolean update(int type, Object... args) {
        if (AIComboPhotoMode.INFO_FACE_DETECTED != type
                && AIComboPhotoMode.ORIENTATION_CHANGED != type) {
            LogHelper.i(TAG, "[update] type = " + type);
        }
        boolean value = false;
        switch (type) {

            case AIComboPhotoMode.ON_CAMERA_CLOSED:
                // when back to camera, the auto back to photoMode not need
                removeBackToNormalMsg();
                break;

            case AIComboPhotoMode.ON_CAMERA_PARAMETERS_READY:
                prepareAIBeauty();
                break;

            case AIComboPhotoMode.ORIENTATION_CHANGED:
                Util.setOrientation(mRootView, (Integer) args[0], true);
                if (mAIBeautyInfo != null) {
                    mAIBeautyInfo.onOrientationChanged((Integer) args[0]);
                }
                break;

            case AIComboPhotoMode.ON_FULL_SCREEN_CHANGED:
                mIsInCameraPreview = (Boolean) args[0];
                if (mIsInCameraPreview) {
                    show();
                } else {
                    // because when effect is showing, we have hide the ALLViews,so
                    // need show the views
                    // otherwise back to Camera,you will found all the UI is hide
                    if (isEffectsShowing()) {
                        mApp.getAppUi().applyAllUIVisibility(View.VISIBLE);
                    }
                    hide();
                }
                break;

            case AIComboPhotoMode.ON_BACK_PRESSED:
                if (isEffectsShowing()) {
                    onModifyIconClick();
                    value = true;
                } else {
                    // when back to camera, the auto back to photoMode not need
                    removeBackToNormalMsg();
                }
                break;

            case AIComboPhotoMode.HIDE_EFFECTS_ITEM:
                if (isEffectsShowing()) {
                    onModifyIconClick();
                }
                break;

            case AIComboPhotoMode.ON_SETTING_BUTTON_CLICK:
                mIsShowSetting = (Boolean) args[0];

                if (mIsShowSetting) {
                    hide();
                } else {
                    show();
                }
                break;

            case AIComboPhotoMode.ON_LEAVE_FACE_BEAUTY_MODE:
                hide();
                unInit();
                break;

            case AIComboPhotoMode.REMVOE_BACK_TO_NORMAL:
                // this case also need reset the automatic back to VFB mode
                removeBackToNormalMsg();
                break;

            case AIComboPhotoMode.ON_SELFTIMER_CAPTUEING:
                if ((Boolean) args[0]) {
                    hide();
                    removeBackToNormalMsg();
                } else {
                    if (!mIsInPictureTakenProgress) {
                        show();
                    }
                }
                break;

            case AIComboPhotoMode.IN_PICTURE_TAKEN_PROGRESS:
                mIsInPictureTakenProgress = (Boolean) args[0];
                if (mIsInPictureTakenProgress) {
                    hide();
                    removeBackToNormalMsg();
                } else {
                    show();
                }
                break;

            default:
                break;
        }

        return value;
    }

    /**
     * will be called if app want to show current view which hasn't been
     * created.
     *
     * @return
     */
    private View getView() {
        View viewLayout
              = mApp.getActivity().getLayoutInflater().inflate(R.layout.aibeauty_indicator,
                                                               mParentViewGroup, true);
        mRootView = viewLayout.findViewById(R.id.aibeauty_indicator);
        initializeViewManager();
        return mRootView;
    }

    private void initializeViewManager() {
        mBgLinearLayout = (LinearLayout) mRootView.findViewById(R.id.effcts_bg);
        mAIBeautyImageViews[AI_BEAUTY_ICON] = (RotateImageView) mRootView
                .findViewById(R.id.aibeauty_icon);
        mAIBeautyImageViews[AI_BEAUTY_MODIFY_ICON] = (RotateImageView) mRootView
               .findViewById(R.id.aibeauty_modify);

        mAIBeautyImageViews[AI_BEAUTY_WHITENESS] = (RotateImageView) mRootView
                .findViewById(R.id.aibeauty_whiteness);
        mAIBeautyImageViews[AI_BEAUTY_BRIGHT_EYES] = (RotateImageView) mRootView
                .findViewById(R.id.aibeauty_bright_eyes);
        mAIBeautyImageViews[AI_BEAUTY_SLIM_FACE] = (RotateImageView) mRootView
                .findViewById(R.id.aibeauty_slim_face);
        mAIBeautyImageViews[AI_BEAUTY_SKINTONE] = (RotateImageView) mRootView
                .findViewById(R.id.aibeauty_smooth);

        mAdjustmentValueIndicator = mRootView
                .findViewById(R.id.aibeauty_changevalue);
        mAdjustmentValueIndicator.setThumbSizePx(50, 50);
        mAdjustmentValueIndicator.setOrientation(0);
        mAdjustmentValueIndicator.setUnSelectColor(ContextCompat.getColor(mApp.getActivity(),
                R.color.thumb_unSelected));
        mAdjustmentValueIndicator.setSelectColor(ContextCompat.getColor(mApp.getActivity(),
                R.color.thumb_selected));
        applyListeners();
        mAIBeautyInfo = new AIBeautyInfo(mApp.getActivity(), mApp);
    }

    @Override
    public void onClick(View view) {
        // First:get the click view index,because need show the effects name
        for (int i = 0; i < NUMBER_AI_BEAUTY_ICON; i++) {
            if (mAIBeautyImageViews[i] == view) {
                mCurrentViewIndex = i;
                break;
            }
        }

        // Second:highlight the effect's image Resource which is clicked
        // also need set the correct effect value
        for (int i = 0; i < SUPPORTED_FB_EFFECTS_NUMBER; i++) {
            if (mCurrentViewIndex == i) {
                mAIBeautyImageViews[i]
                        .setImageResource(AI_BEAUTY_ICONS_HIGHTLIGHT[i]);
                // set the effects value
                int progerss = mAIBeautyPropertiesValue.get(i);
                setProgressValue(progerss);
            } else {
                mAIBeautyImageViews[i]
                        .setImageResource(AI_BEAUTY_ICONS_NORMAL[i]);
            }
        }

        switch (mCurrentViewIndex) {
            case AI_BEAUTY_WHITENESS:
                mEffectsKey = KEY_AI_BEAUTY_WHITENESS;
                break;

            case AI_BEAUTY_BRIGHT_EYES:
                mEffectsKey = KEY_AI_BEAUTY_BRIGHT_EYES;
                break;

            case AI_BEAUTY_SLIM_FACE:
                mEffectsKey = KEY_AI_BEAUTY_SLIM_FACE;
                break;

            case AI_BEAUTY_SKINTONE:
                mEffectsKey = KEY_AI_BEAUTY_SKINTONE;
                break;

            case AI_BEAUTY_MODIFY_ICON:
                onModifyIconClick();
                break;

            case AI_BEAUTY_ICON:
                onAIBeautyIconClick();
                break;

            default:
                LogHelper.i(TAG, "[onClick]click is not the aibeauty imageviews,need check");
                break;
        }
        // current not show the toast of the view
        showEffectsToast(view, mCurrentViewIndex);
        //
        mModeHandler.post(new Runnable() {
            @Override
            public void run() {
                onEffectsIconClick();
            }
        });

    }

    private void applyListeners() {
        for (int i = 0; i < NUMBER_AI_BEAUTY_ICON; i++) {
            if (null != mAIBeautyImageViews[i]) {
                mAIBeautyImageViews[i].setOnClickListener(this);
            }
        }
        if (mAdjustmentValueIndicator != null) {
            mAdjustmentValueIndicator
                    .setOnSlideChangeListener(mVerticalSeekBarSlideChangeListener);
        }
    }

    // when click the effects modify icon will run follow
    private void onModifyIconClick() {
        if (isEffectsShowing()) {
            // if current is showing and click the modify icon,need hide the
            // common views ,such as ModePicker/thumbnail/picker/settings item
            //mICameraAppUi.setViewState(ViewState.VIEW_STATE_NORMAL);
            mApp.getAppUi().applyAllUIVisibility(View.VISIBLE);
            hideEffectsIconAndSeekBar();
        } else {
            if (mBgLinearLayout != null) {
                mBgLinearLayout.setBackgroundResource(R.drawable.bg_icon);
            }
            showAIBeautyEffects();
            // initialize the parameters
            mEffectsKey = KEY_AI_BEAUTY_WHITENESS;
            // show default string
            showEffectsToast(mAIBeautyImageViews[AI_BEAUTY_WHITENESS],
                    AI_BEAUTY_WHITENESS);
            // need set current values
            setProgressValue(mAIBeautyPropertiesValue
                    .get(AI_BEAUTY_WHITENESS));
        }
    }

    private boolean isEffectsShowing() {
        boolean isEffectsShowing = View.VISIBLE == mAIBeautyImageViews[AI_BEAUTY_WHITENESS]
                .getVisibility();
        LogHelper.d(TAG, "isEffectsShowing = " + isEffectsShowing);

        return isEffectsShowing;
    }

    // when click the effects icon will run follow
    private void onEffectsIconClick() {
        if (mCurrentViewIndex < SUPPORTED_FB_EFFECTS_NUMBER) {
            configMetaParams(getmEffectsKey(mCurrentViewIndex),
                    mAIBeautyPropertiesValue.get(mCurrentViewIndex));
        }
    }

    private void hideEffectsIconAndSeekBar() {
        hideEffectsItems();
        hideSeekBar();
        if (mAIBeautyInfo != null) {
            mAIBeautyInfo.cancel();
        }
        if (mBgLinearLayout != null) {
            mBgLinearLayout.setBackgroundDrawable(null);
        }

        // change the image resource
        mAIBeautyImageViews[AI_BEAUTY_MODIFY_ICON]
                .setImageResource(AI_BEAUTY_ICONS_NORMAL[AI_BEAUTY_MODIFY_ICON]);
    }

    private void showAIBeautyEffects() {
        // default first effects is wrinkle Remove effects
        mAIBeautyImageViews[AI_BEAUTY_WHITENESS]
                .setImageResource(AI_BEAUTY_ICONS_HIGHTLIGHT[AI_BEAUTY_WHITENESS]);
        mAIBeautyImageViews[AI_BEAUTY_WHITENESS]
                .setVisibility(View.VISIBLE);

        mAIBeautyImageViews[AI_BEAUTY_MODIFY_ICON]
                .setImageResource(AI_BEAUTY_ICONS_HIGHTLIGHT[AI_BEAUTY_MODIFY_ICON]);

        // also need to show the background
        if (mBgLinearLayout != null) {
            mBgLinearLayout.setVisibility(View.VISIBLE);
        }

        // show the left of imageviews
        for (int i = 1; i < SUPPORTED_FB_EFFECTS_NUMBER; i++) {
            mAIBeautyImageViews[i]
                    .setImageResource(AI_BEAUTY_ICONS_NORMAL[i]);
            mAIBeautyImageViews[i].setVisibility(View.VISIBLE);
        }
        // when set the face mode to Mulit-face ->close camera ->reopen camera
        // ->go to FB mdoe
        // will found the effects UI is error,so need set not supported effects
        // view gone. //[this need check whether nead TODO]
        for (int i = SUPPORTED_FB_EFFECTS_NUMBER;
             i < SUPPORTED_FB_PROPERTIES_MAX_NUMBER; i++) {
            mAIBeautyImageViews[i].setVisibility(View.GONE);
        }
        // also need to show SeekBar
        if (mAdjustmentValueIndicator != null) {
            mAdjustmentValueIndicator.setMaxProgress(mSupportedDuration);
            mAdjustmentValueIndicator.setVisibility(View.VISIBLE);
        }
    }

    private void showEffectsToast(View view, int index) {
        if (index >= 0 && index < SUPPORTED_FB_EFFECTS_NUMBER) {
            if (view.getContentDescription() != null) {
                mAIBeautyInfo.setText(view.getContentDescription());
                mAIBeautyInfo.cancel();
                mAIBeautyInfo.setTargetId(index, SUPPORTED_FB_EFFECTS_NUMBER + 2);
                mAIBeautyInfo.showToast();
            }
        }
    }

    private void hideToast() {
        if (mAIBeautyInfo != null) {
            mAIBeautyInfo.hideToast();
        }
    }

    private void setProgressValue(int value) {
        mAdjustmentValueIndicator.setProgress(convertToParamertersValue(value));
    }

    private int convertToParamertersValue(int value) {
        return value;
    }

    private void onAIBeautyIconClick() {
        if (!isModifyIconShowing()) {
            intoAIBeautyMode();
        } else {
            leaveAIBeautyMode();
        }
    }

    private void intoAIBeautyMode() {
        mAIBeautyImageViews[AI_BEAUTY_MODIFY_ICON]
                .setImageResource(AI_BEAUTY_ICONS_NORMAL[AI_BEAUTY_MODIFY_ICON]);
        updateModifyIconStatus(true);
        mAIBeautyImageViews[AI_BEAUTY_ICON]
                .setImageResource(AI_BEAUTY_ICONS_HIGHTLIGHT[AI_BEAUTY_ICON]);
        mAIBeautyImageViews[AI_BEAUTY_ICON].setVisibility(View.VISIBLE);
        hideEffectsItems();
        hideSeekBar();
    }

    private void leaveAIBeautyMode() {
        // when isAIBeautyModifyIconShowing = true,means the icon is
        // showing ,need hide the
        // face beauty effects and modify values Seekbar
        updateModifyIconStatus(false);
        mAIBeautyImageViews[AI_BEAUTY_ICON]
                .setImageResource(AI_BEAUTY_ICONS_NORMAL[AI_BEAUTY_ICON]);
        mAIBeautyImageViews[AI_BEAUTY_ICON].setVisibility(View.VISIBLE);
        hideEffectsIconAndSeekBar();
    }

    private void showFaceBeautyIcon() {
        if (null != mAIBeautyImageViews && !isFBIconShowing()) {
            int resValue = AI_BEAUTY_ICONS_NORMAL[AI_BEAUTY_ICON];
            // when modify icon is showing , this time not only show the FB icon
            // also need show the modify icon
            if (isModifyIconShowing()) { // Need Check
                updateModifyIconStatus(true);
            }
            mAIBeautyImageViews[AI_BEAUTY_ICON].setImageResource(resValue);
            mAIBeautyImageViews[AI_BEAUTY_ICON].setVisibility(View.VISIBLE);
        }
    }

    private boolean isFBIconShowing() {
        boolean isFBIconShowing = View.VISIBLE == mAIBeautyImageViews[AI_BEAUTY_ICON]
                .getVisibility();

        return isFBIconShowing;
    }

    private boolean isModifyIconShowing() {
        boolean isModifyIconShowing = View.VISIBLE == mAIBeautyImageViews[AI_BEAUTY_MODIFY_ICON]
                .getVisibility();

        return isModifyIconShowing;
    }

    private void hideFaceBeautyIcon() {
        if (null != mAIBeautyImageViews) {
            mAIBeautyImageViews[AI_BEAUTY_ICON]
                    .setVisibility(View.INVISIBLE);
        }
    }

    private void updateModifyIconStatus(boolean visible) {
        if (!visible && mAIBeautyInfo != null) {
            mAIBeautyInfo.cancel();
        }
        if (mBgLinearLayout != null) {
            mBgLinearLayout.setBackgroundDrawable(null);
            mBgLinearLayout.setVisibility(View.VISIBLE);
        }
        mAIBeautyImageViews[AI_BEAUTY_MODIFY_ICON]
                .setImageResource(AI_BEAUTY_ICONS_NORMAL[AI_BEAUTY_MODIFY_ICON]);
        mAIBeautyImageViews[AI_BEAUTY_MODIFY_ICON]
                .setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }

    private void hideEffectsItems() {
        for (int i = 0; i < SUPPORTED_FB_EFFECTS_NUMBER; i++) {
            mAIBeautyImageViews[i].setVisibility(View.GONE);
        }
    }

    private void hideSeekBar() {
        if (mAdjustmentValueIndicator != null) {
            mAdjustmentValueIndicator.setVisibility(View.GONE);
       }
    }


    /*
     *follow is
     *the seekbar's :min  ~ max
     *
     *but UI
     *is:max  ~min
     */
    private SeekBar.OnSeekBarChangeListener mHorientiaonlSeekBarLisenter
                = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            updateEffectsChache(Integer.valueOf(mEffectsValue));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            mAdjustmentValueIndicator.setProgress(progress);
            mCurrentViewIndex = mCurrentViewIndex % SUPPORTED_FB_EFFECTS_NUMBER;
            setEffectsValueParameters(progress);
        }
    };

    private void updateEffectsChache(int value) {
        if (mEffectsKey != null && mCurrentViewIndex >= 0
                && mCurrentViewIndex < SUPPORTED_FB_EFFECTS_NUMBER
                && value != mAIBeautyPropertiesValue.get(mCurrentViewIndex)) {
            mAIBeautyPropertiesValue.set(mCurrentViewIndex, value);
        }
    }

    private void setEffectsValueParameters(int progress) {
        mEffectsValue = Integer.toString(convertToParamertersValue(progress));

        mModeHandler.post(new Runnable() {
            @Override
            public void run() {
                // set the value to parameters to devices
                configMetaParams(getmEffectsKey(mCurrentViewIndex), progress);
            }
        });
        mDataStore.setValue(getmEffectsKey(mCurrentViewIndex),
                Integer.toString(progress),
                mDataStore.getGlobalScope(), false);
    }

    private void prepareAIBeauty() {
        // first need clear the effects value;
        mAIBeautyPropertiesValue.clear();
        for (int i = 0; i < SUPPORTED_FB_EFFECTS_NUMBER; i++) {
            int value = Integer.parseInt(mDataStore.getValue(getmEffectsKey(i),
                    "0", mDataStore.getGlobalScope()));
            mAIBeautyPropertiesValue.add(value);
        }
        // get the supported max effects
        mSupportedMaxValue = 10;
        // set the effects duration: Max - Min
        mSupportedDuration = mSupportedMaxValue - 0;
    }

    // 5s timeout mechanism
    // This is used to the 5s timeout mechanism Read
    protected class IndicatorHandler extends Handler {
        public IndicatorHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            if (!mIsTimeOutMechanismRunning) {
                LogHelper.i(TAG, "Time out mechanism not running ,so return ");
                return;
            }
            switch (msg.what) {
                case DISAPPEAR_VFB_UI:
                    hide();
                    mApp.getAppUi().applyAllUIVisibility(View.VISIBLE);
                    break;

                default:
                    break;
            }
        }
    }

    private void removeBackToNormalMsg() {
        if (mHandler != null) {
            mHandler.removeMessages(DISAPPEAR_VFB_UI);
            mIsTimeOutMechanismRunning = false;
        }
    }

    private void configMetaParams(String type, int value) {
        param = new FeatureParam();
        param.appendInt(type, value);
        mICameraContext.getCamPostAlgo().configParams(FeaturePipeConfig.INDEX_VIDEO, param);
        mICameraContext.getCamPostAlgo().configParams(FeaturePipeConfig.INDEX_CAPTURE, param);
    }

    private void configBeautyAllMetaParams(int beautyAllValue) {
        param = new FeatureParam();
        param.appendInt(MTK_POSTALGO_AI_COMBO_WHITE, VALUE_AI_BEAUTY_NO_EFFECTS);
        param.appendInt(MTK_POSTALGO_AI_COMBO_BIGEYE, VALUE_AI_BEAUTY_NO_EFFECTS);
        param.appendInt(MTK_POSTALGO_AI_COMBO_SMALLCHEEK, VALUE_AI_BEAUTY_NO_EFFECTS);
        param.appendInt(MTK_POSTALGO_AI_COMBO_SMOOTH, VALUE_AI_BEAUTY_NO_EFFECTS);
        mICameraContext.getCamPostAlgo().configParams(FeaturePipeConfig.INDEX_VIDEO, param);
        mICameraContext.getCamPostAlgo().configParams(FeaturePipeConfig.INDEX_CAPTURE, param);
    }

    private String getmEffectsKey(int index) {
        switch (index) {
            case AI_BEAUTY_WHITENESS:
                return MTK_POSTALGO_AI_COMBO_WHITE;
            case AI_BEAUTY_BRIGHT_EYES:
                return MTK_POSTALGO_AI_COMBO_BIGEYE;
            case AI_BEAUTY_SLIM_FACE:
                return MTK_POSTALGO_AI_COMBO_SMALLCHEEK;
            case AI_BEAUTY_SKINTONE:
                return MTK_POSTALGO_AI_COMBO_SMOOTH;
            default:
                return MTK_POSTALGO_AI_COMBO_WHITE;
        }
    }

    private VerticalSeekBar.SlideChangeListener mVerticalSeekBarSlideChangeListener
            = new VerticalSeekBar.SlideChangeListener() {
        @Override
        public void onStart(VerticalSeekBar slideView, int progress) {

        }

        @Override
        public void onProgress(VerticalSeekBar slideView, int progress) {
            mAdjustmentValueIndicator.setProgress(progress);
            mCurrentViewIndex = mCurrentViewIndex % SUPPORTED_FB_EFFECTS_NUMBER;
            setEffectsValueParameters(progress);
        }

        @Override
        public void onStop(VerticalSeekBar slideView, int progress) {
            updateEffectsChache(Integer.valueOf(mEffectsValue));
        }
    };

}
