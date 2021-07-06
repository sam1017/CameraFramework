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
package com.mediatek.camera.feature.mode.aiworksfacebeauty.view;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.mediatek.camera.R;
import com.mediatek.camera.common.IAppUi;
import com.mediatek.camera.common.ICameraContext;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.relation.DataStore;
import com.mediatek.camera.common.utils.CameraUtil;
import com.mediatek.camera.common.widget.RotateLayout;
import com.mediatek.camera.ui.CHSeekBar;

import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.SeekBar;

import com.mediatek.camera.feature.mode.aiworksfacebeauty.view.CRotateTextView;

import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.graphics.drawable.Drawable;

import com.mediatek.camera.CameraActivity;
//bv wuyonglin add for adjust screen height 1560px top bar position 20200628 start
import android.view.WindowManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.content.Context;
//bv wuyonglin add for adjust screen height 1560px top bar position 20200628 end
import android.widget.HorizontalScrollView;

/**
 * The sdof view manager.
 */
public class BeautyViewCtrl implements OnClickListener {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(BeautyViewCtrl.class.getSimpleName());
    // Stereo Photo warning message
    //Gesture and View Control
    private static final int SHOW_INFO_LENGTH_LONG = 5 * 1000;
    private static final int GYBEAUTY_VIEW_INIT_AND_SHOW = 1;
    private static final int GYBEAUTY_VIEW_UNINIT = 2;
    private ViewGroup mRootViewGroup;
    private IApp mApp;
    private MainHandler mMainHandler;
    private ViewChangeListener mViewChangeListener;
    private int seekbarmax = 150;
    private Handler mHandler;
    private View mView;
    private View mFaceBeautyOneKeyLayout;
    private View mFaceBeautyCustomLayout;
    private View mFaceBeautyOnekeyBtn;
    private View mFaceBeautyCustomBtn;
    private TextView mFaceBeautySwitchBtn;
    private CHSeekBar mOneKeyCHSeekBar;
    private CHSeekBar mCustomCHSeekBar;
    private final static float DEFALUT_FACEBEAUTY_VALUE = 0.5f;
    private final static float DEFALUT_SMOOTH_VALUE = 0.5f;
    private final static float DEFALUT_WHITEN_VALUE = 0.5f;
    private final static float DEFALUT_THIN_VALUE = 0.5f;
    private final static float DEFALUT_BIGEYE_VALUE = 0.5f;
    private final static float DEFALUT_BRIGHTEYE_VALUE = 0.5f;
    private final static float DEFALUT_BIGNOSE_VALUE = 0.5f;
    private float mFaceBeautyValue = DEFALUT_FACEBEAUTY_VALUE;
    private float mSmoothValue = DEFALUT_SMOOTH_VALUE;
    private float mWhilenValue = DEFALUT_WHITEN_VALUE;
    private float mThinValue = DEFALUT_THIN_VALUE;
    private float mBigEyeValue = DEFALUT_BIGEYE_VALUE;
    private float mBrightEyeValue = DEFALUT_BRIGHTEYE_VALUE;
    private float mBigNoseValue = DEFALUT_BIGNOSE_VALUE;
    private static final int NUMBER_FACE_BEAUTY_ICON = 6;


    private final static int CUSTOM_TYPE_FACE_THIN = 0;
    private final static int CUSTOM_TYPE_FACE_BIGEYE = 1;
    private final static int CUSTOM_TYPE_FACE_BRIGHTEYE = 2;
    private final static int CUSTOM_TYPE_FACE_BIGNOSE = 3;
    private final static int CUSTOM_TYPE_FACE_SMOOTH = 4;
    private final static int CUSTOM_TYPE_FACE_WHITEN = 5;

    private int mCustomBeautyType = -1;// when value 10  is  Onekey Beauty other is  CUSTOM_TYPE
    private int mBeautyType = 1;//0 is cuctombeauty 1 is onekey beauty
    private CRotateTextView[] mFaceBeautyImageViews = new CRotateTextView[NUMBER_FACE_BEAUTY_ICON];

    private static final int[] FACE_BEAUTY_ICONS_NORMAL = new int[NUMBER_FACE_BEAUTY_ICON];
    private static final int[] FACE_BEAUTY_ICONS_HIGHTLIGHT = new int[NUMBER_FACE_BEAUTY_ICON];

    static {
        FACE_BEAUTY_ICONS_NORMAL[CUSTOM_TYPE_FACE_THIN] = R.drawable.aiworks_ic_facebeauty_thin_normal;
        FACE_BEAUTY_ICONS_NORMAL[CUSTOM_TYPE_FACE_BIGEYE] = R.drawable.aiworks_ic_facebeauty_big_eye_normal;
        FACE_BEAUTY_ICONS_NORMAL[CUSTOM_TYPE_FACE_BRIGHTEYE] = R.drawable.aiworks_ic_facebeauty_bright_eye_normal;
        FACE_BEAUTY_ICONS_NORMAL[CUSTOM_TYPE_FACE_BIGNOSE] = R.drawable.aiworks_ic_facebeauty_big_nose_normal;
        FACE_BEAUTY_ICONS_NORMAL[CUSTOM_TYPE_FACE_SMOOTH] = R.drawable.aiworks_ic_facebeauty_buffing_normal;
        FACE_BEAUTY_ICONS_NORMAL[CUSTOM_TYPE_FACE_WHITEN] = R.drawable.aiworks_ic_facebeauty_whitening_normal;
    }

    static {
        FACE_BEAUTY_ICONS_HIGHTLIGHT[CUSTOM_TYPE_FACE_THIN] = R.drawable.aiworks_ic_facebeauty_thin_pressed;
        FACE_BEAUTY_ICONS_HIGHTLIGHT[CUSTOM_TYPE_FACE_BIGEYE] = R.drawable.aiworks_ic_facebeauty_big_eye_pressed;
        FACE_BEAUTY_ICONS_HIGHTLIGHT[CUSTOM_TYPE_FACE_BRIGHTEYE] = R.drawable.aiworks_ic_facebeauty_bright_eye_pressed;
        FACE_BEAUTY_ICONS_HIGHTLIGHT[CUSTOM_TYPE_FACE_BIGNOSE] = R.drawable.aiworks_ic_facebeauty_big_nose_pressed;
        FACE_BEAUTY_ICONS_HIGHTLIGHT[CUSTOM_TYPE_FACE_SMOOTH] = R.drawable.aiworks_ic_facebeauty_buffing_pressed;
        FACE_BEAUTY_ICONS_HIGHTLIGHT[CUSTOM_TYPE_FACE_WHITEN] = R.drawable.aiworks_ic_facebeauty_whitening_pressed;
    }

    private int mCustomProgress = 0;

    private IAppUi.HintInfo mGuideHint;
    private CameraActivity mCameraActvity;
    private View mBeautyofLayout;
    private SeekBar mBeautySeekbarLayout;
    private RelativeLayout mControl;
    ICameraContext mICameraContext;
    private DataStore mDataStore;
    private int mCameraId;
    private View mFaceBeautyExpandBtn;
    //add by huangfei for disconnect camear start
    private int mProgress;
    private HorizontalScrollView mHorizontalScrollView;

    //add by huangfei for disconnect camear end
    public BeautyViewCtrl(ICameraContext context) {
        mICameraContext = context;
        mDataStore = mICameraContext.getDataStore();
    }

    /**
     * Init the view.
     *
     * @param app the activity.
     */

    public void init(IApp app) {
        LogHelper.i(TAG, "[init] + mCustomBeautyType =" + mCustomBeautyType);
        mApp = app;
        mMainHandler = new MainHandler(app.getActivity().getMainLooper());
        //mMainHandler.sendEmptyMessage(GYBEAUTY_VIEW_INIT_AND_SHOW);
        mCameraActvity = (CameraActivity) app.getActivity();
    }

    /**
     * To destroy the zoom view.
     */
    public void unInit() {
        mMainHandler.sendEmptyMessage(GYBEAUTY_VIEW_UNINIT);
    }

    /**
     * when phone orientation changed, the zoom view will be updated.
     *onClick
     * @param orientation the orientation of g-sensor.
     */
    public void onOrientationChanged(int orientation) {
        if (mMainHandler != null) {
            //mMainHandler.obtainMessage(SDOF_VIEW_ORIENTATION_CHANGED, orientation).sendToTarget();
        }
    }

    /**
     * Set dof bar view change listener.
     *
     * @param listener the view change listener.
     */
    public void setViewChangeListener(ViewChangeListener listener) {
        mViewChangeListener = listener;
    }


    /**
     * This listener used for update info with mode.
     */
    public interface ViewChangeListener {
        /**
         * This method used for notify mode gybeauty level.
         *
         * @param progress Beauty  level
         */
        public void onBeautyLevelChanged(float level, int beautyType);

        /**
         * This method used for notify beauty mode change.
         *
         * @param progress Beauty  level
         */
        public void onBeautyModeChanged(int beautyType);

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
                case GYBEAUTY_VIEW_INIT_AND_SHOW:
                    initView((int) msg.obj);
                    break;
                case GYBEAUTY_VIEW_UNINIT:
                    unInitView();
                    break;
                default:
                    break;
            }
        }
    }

    private void initView(int cameraId) {
        mCameraId = cameraId;
        LogHelper.i(TAG, "[initView] mFaceBeautyValue =" + mFaceBeautyValue + " mBeautyType =" + mBeautyType + " mSmoothValue =" + mSmoothValue + " mCameraId =" + mCameraId);
        mRootViewGroup = mApp.getAppUi().getModeRootView();
        mBeautyofLayout = (RelativeLayout) mApp.getActivity().getLayoutInflater()
                .inflate(R.layout.aiworks_century_facebeauty,
                        mRootViewGroup, false).findViewById(R.id.beauty_rotate_layout);
        mFaceBeautyOneKeyLayout = mBeautyofLayout.findViewById(R.id.century_facebeauty_onekey);
        mFaceBeautyCustomLayout = mBeautyofLayout.findViewById(R.id.century_facebeauty_custom);
        mFaceBeautyCustomBtn = mBeautyofLayout.findViewById(R.id.aiworks_facebeauty_custom_btn);
        mFaceBeautyOnekeyBtn = mBeautyofLayout.findViewById(R.id.aiworks_facebeauty_onekey_btn);
        //bv wuyonglin add for bug2376 20200929 start
        mFaceBeautyCustomBtn.setForceDarkAllowed(false);
        mFaceBeautyOnekeyBtn.setForceDarkAllowed(false);
        //bv wuyonglin add for bug2376 20200929 end
        mFaceBeautySwitchBtn = (TextView) mBeautyofLayout.findViewById(R.id.century_facebeauty_switch_btn);
        mFaceBeautyImageViews[CUSTOM_TYPE_FACE_SMOOTH] = (CRotateTextView) mBeautyofLayout.findViewById(R.id.aiworks_facebeauty_buffing_btn);
        mFaceBeautyImageViews[CUSTOM_TYPE_FACE_WHITEN] = (CRotateTextView) mBeautyofLayout.findViewById(R.id.aiworks_facebeauty_whitening_btn);
        mFaceBeautyImageViews[CUSTOM_TYPE_FACE_THIN] = (CRotateTextView) mBeautyofLayout.findViewById(R.id.aiworks_facebeauty_thin_btn);
        mFaceBeautyImageViews[CUSTOM_TYPE_FACE_BIGEYE] = (CRotateTextView) mBeautyofLayout.findViewById(R.id.aiworks_facebeauty_big_eye_btn);
        mFaceBeautyImageViews[CUSTOM_TYPE_FACE_BRIGHTEYE] = (CRotateTextView) mBeautyofLayout.findViewById(R.id.aiworks_facebeauty_bright_eye_btn);
        mFaceBeautyImageViews[CUSTOM_TYPE_FACE_BIGNOSE] = (CRotateTextView) mBeautyofLayout.findViewById(R.id.aiworks_facebeauty_big_nose_btn);
        mFaceBeautyExpandBtn = mBeautyofLayout.findViewById(R.id.aiworks_beauty_expand_button);
        //bv wuyonglin add for bug2376 20200929 start
        mFaceBeautyExpandBtn.setForceDarkAllowed(false);
        //bv wuyonglin add for bug2376 20200929 end

        mHorizontalScrollView = (HorizontalScrollView) mFaceBeautyCustomLayout.findViewById(R.id.horizontal_scroll_view);
        mOneKeyCHSeekBar = (CHSeekBar) mBeautyofLayout.findViewById(R.id.century_facebeauty_onekey_seekbar);
        mCustomCHSeekBar = (CHSeekBar) mBeautyofLayout.findViewById(R.id.century_facebeauty_custom_seekbar);
        //bv wuyonglin add for adjust screen height 1560px top bar position 20200723 start
        WindowManager wm = (WindowManager) mApp.getActivity().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        display.getRealMetrics(dm);
        if (dm.heightPixels == 1560) {
            RelativeLayout.LayoutParams mControlLayoutParams = (RelativeLayout.LayoutParams) mFaceBeautyOneKeyLayout.getLayoutParams();
            mControlLayoutParams.bottomMargin = mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.hct_shutter_layout_height_1560px);
            mFaceBeautyOneKeyLayout.setLayoutParams(mControlLayoutParams);
        } else if (dm.heightPixels == 1440) {
            RelativeLayout.LayoutParams mControlLayoutParams = (RelativeLayout.LayoutParams) mFaceBeautyOneKeyLayout.getLayoutParams();
            mControlLayoutParams.bottomMargin = mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.hct_shutter_layout_height_1440px);
            mFaceBeautyOneKeyLayout.setLayoutParams(mControlLayoutParams);
        } else if (dm.heightPixels == 2300) {
            RelativeLayout.LayoutParams mControlLayoutParams = (RelativeLayout.LayoutParams) mFaceBeautyOneKeyLayout.getLayoutParams();
            mControlLayoutParams.bottomMargin = mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.hct_shutter_layout_height_2300px);
            mFaceBeautyOneKeyLayout.setLayoutParams(mControlLayoutParams);
            RelativeLayout.LayoutParams mControlCustomLayoutParams = (RelativeLayout.LayoutParams) mFaceBeautyCustomLayout.getLayoutParams();
            mControlCustomLayoutParams.bottomMargin = mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.century_facebeauty_custom_margin_bottom_2300px);
            mFaceBeautyCustomLayout.setLayoutParams(mControlCustomLayoutParams);
            LinearLayout.LayoutParams mHorizontalScrollViewLayoutParams = (LinearLayout.LayoutParams) mHorizontalScrollView.getLayoutParams();
            mHorizontalScrollViewLayoutParams.topMargin = mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.aiworks_horizontal_scroll_view_margin_top_2300px);
            mHorizontalScrollView.setLayoutParams(mHorizontalScrollViewLayoutParams);
            RelativeLayout.LayoutParams mCustomBtnLayoutParams = (RelativeLayout.LayoutParams) mFaceBeautyCustomBtn.getLayoutParams();
            mCustomBtnLayoutParams.bottomMargin = mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.aiworks_facebeauty_custom_margin_bottom_2300px);
            mFaceBeautyCustomBtn.setLayoutParams(mCustomBtnLayoutParams);
            RelativeLayout.LayoutParams mOnekeyBtnLayoutParams = (RelativeLayout.LayoutParams) mFaceBeautyOnekeyBtn.getLayoutParams();
            mOnekeyBtnLayoutParams.bottomMargin = mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.aiworks_facebeauty_custom_margin_bottom_2300px);
            mFaceBeautyOnekeyBtn.setLayoutParams(mOnekeyBtnLayoutParams);
            RelativeLayout.LayoutParams mExpandBtnLayoutParams = (RelativeLayout.LayoutParams) mFaceBeautyExpandBtn.getLayoutParams();
            mExpandBtnLayoutParams.bottomMargin = mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.aiworks_facebeauty_custom_margin_bottom_2300px);
            mFaceBeautyExpandBtn.setLayoutParams(mExpandBtnLayoutParams);
        } else if (dm.heightPixels == 2400) {
            RelativeLayout.LayoutParams mControlLayoutParams = (RelativeLayout.LayoutParams) mFaceBeautyOneKeyLayout.getLayoutParams();
            mControlLayoutParams.bottomMargin = mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.hct_shutter_layout_height_2400px);
            mFaceBeautyOneKeyLayout.setLayoutParams(mControlLayoutParams);
            RelativeLayout.LayoutParams mControlCustomLayoutParams = (RelativeLayout.LayoutParams) mFaceBeautyCustomLayout.getLayoutParams();
            mControlCustomLayoutParams.bottomMargin = mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.century_facebeauty_custom_margin_bottom_2400px);
            mFaceBeautyCustomLayout.setLayoutParams(mControlCustomLayoutParams);
            LinearLayout.LayoutParams mHorizontalScrollViewLayoutParams = (LinearLayout.LayoutParams) mHorizontalScrollView.getLayoutParams();
            mHorizontalScrollViewLayoutParams.topMargin = mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.aiworks_horizontal_scroll_view_margin_top_2400px);
            mHorizontalScrollView.setLayoutParams(mHorizontalScrollViewLayoutParams);
            RelativeLayout.LayoutParams mCustomBtnLayoutParams = (RelativeLayout.LayoutParams) mFaceBeautyCustomBtn.getLayoutParams();
            mCustomBtnLayoutParams.bottomMargin = mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.aiworks_facebeauty_custom_margin_bottom_2400px);
            mFaceBeautyCustomBtn.setLayoutParams(mCustomBtnLayoutParams);
            RelativeLayout.LayoutParams mOnekeyBtnLayoutParams = (RelativeLayout.LayoutParams) mFaceBeautyOnekeyBtn.getLayoutParams();
            mOnekeyBtnLayoutParams.bottomMargin = mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.aiworks_facebeauty_custom_margin_bottom_2400px);
            mFaceBeautyOnekeyBtn.setLayoutParams(mOnekeyBtnLayoutParams);
            RelativeLayout.LayoutParams mExpandBtnLayoutParams = (RelativeLayout.LayoutParams) mFaceBeautyExpandBtn.getLayoutParams();
            mExpandBtnLayoutParams.bottomMargin = mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.aiworks_facebeauty_custom_margin_bottom_2400px);
            mFaceBeautyExpandBtn.setLayoutParams(mExpandBtnLayoutParams);
        }
        //bv wuyonglin add for adjust screen height 1560px top bar position 20200723 end
        mRootViewGroup.addView(mBeautyofLayout);
        applyListeners();
        showViews();
        if (mBeautyType == 0) {
            mApp.getAppUi().setUIVisibility(IAppUi.SHUTTER_ROOTLAYOUT, View.GONE);
            initCustomSeekBar(CUSTOM_TYPE_FACE_SMOOTH);
        }
    }

    private void unInitView() {
        LogHelper.i(TAG, "[unInitView] ");
        reset();
        mRootViewGroup.removeView(mBeautyofLayout);
        mBeautySeekbarLayout = null;
        mBeautyofLayout = null;
    }

    private void reset() {

    }


    private int dpToPixel(int dp) {
        float scale = mApp.getActivity().getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public void onClick(View view) {
        if (view == mFaceBeautyOnekeyBtn) {
            mFaceBeautyOneKeyLayout.setVisibility(View.VISIBLE);
            mFaceBeautyCustomLayout.setVisibility(View.GONE);
            mFaceBeautySwitchBtn.setSelected(false);
            mFaceBeautySwitchBtn.setText(R.string.century_facebeauty_onekey);
            mFaceBeautyOnekeyBtn.setVisibility(View.GONE);
            mFaceBeautyCustomBtn.setVisibility(View.VISIBLE);
            mFaceBeautyValue = Float.parseFloat(mDataStore.getValue("aiworks_beauty_all_level", DEFALUT_FACEBEAUTY_VALUE + "", mDataStore.getCameraScope(mCameraId)));
            LogHelper.i(TAG, "[onClick] mFaceBeautyOnekeyBtn mFaceBeautyValue =" + mFaceBeautyValue);
            mOneKeyCHSeekBar.setProgress((int) (mFaceBeautyValue * mOneKeyCHSeekBar.getMax()));
            //bv wuyonglin add for adjust custom FaceMode view position 20200303 start
            mCustomBeautyType = -1;
            mApp.getAppUi().setUIVisibility(IAppUi.SHUTTER_ROOTLAYOUT, View.VISIBLE);
            mApp.getAppUi().setIsCustomBeautyViewShow(false);
            mBeautyType = 1;
            //bv wuyonglin add for adjust custom FaceMode view position 20200303 end
            mViewChangeListener.onBeautyModeChanged(mCustomBeautyType);
        } else if (view == mFaceBeautyCustomBtn) {
            mFaceBeautyOneKeyLayout.setVisibility(View.GONE);
            mFaceBeautyCustomLayout.setVisibility(View.VISIBLE);
            mFaceBeautySwitchBtn.setSelected(false);
            mFaceBeautySwitchBtn.setText(R.string.century_facebeauty_custom);

            mFaceBeautyOnekeyBtn.setVisibility(View.VISIBLE);
            mFaceBeautyCustomBtn.setVisibility(View.GONE);
            //bv wuyonglin modify for adjust custom FaceMode view position 20200303 start
            mApp.getAppUi().setUIVisibility(IAppUi.SHUTTER_ROOTLAYOUT, View.GONE);
            LogHelper.i(TAG, "[onClick] mFaceBeautyCustomBtn mCustomBeautyType =" + mCustomBeautyType);
            if (mCustomBeautyType != -1) {
                initCustomSeekBar(mCustomBeautyType);
            } else {
                initCustomSeekBar(CUSTOM_TYPE_FACE_SMOOTH);
            }
            mApp.getAppUi().setIsCustomBeautyViewShow(true);
            mBeautyType = 0;
            //bv wuyonglin modify for adjust custom FaceMode view position 20200303 end
            mViewChangeListener.onBeautyModeChanged(mCustomBeautyType);
        } else if (view == mFaceBeautySwitchBtn) {
            if (mBeautyType == 0) {
                mFaceBeautyOneKeyLayout.setVisibility(View.GONE);
                mFaceBeautyCustomLayout.setVisibility(View.VISIBLE);
                //mFaceBeautySwitchBtn.setSelected(false);
                mFaceBeautySwitchBtn.setText(R.string.century_facebeauty_custom);

                mFaceBeautyOnekeyBtn.setVisibility(View.GONE);
                mFaceBeautyCustomBtn.setVisibility(View.GONE);
                mBeautyType = 1;
            } else {
                mFaceBeautyOneKeyLayout.setVisibility(View.VISIBLE);
                mFaceBeautyCustomLayout.setVisibility(View.GONE);
                //mFaceBeautySwitchBtn.setSelected(false);
                mFaceBeautySwitchBtn.setText(R.string.century_facebeauty_onekey);
                mFaceBeautyOnekeyBtn.setVisibility(View.GONE);
                mFaceBeautyCustomBtn.setVisibility(View.GONE);
                //bv wuyonglin delete for adjust custom FaceMode view position 20200303 start
                mCustomBeautyType = -1;
                //bv wuyonglin delete for adjust custom FaceMode view position 20200303 end
                mBeautyType = 0;
                mOneKeyCHSeekBar.setProgress((int) (mFaceBeautyValue * mOneKeyCHSeekBar.getMax()));
            }
        } else if (view == mFaceBeautyExpandBtn) {
            LogHelper.i(TAG, "[onClick] mFaceBeautyExpandBtn mBeautyType =" + mBeautyType);
            mFaceBeautyExpandBtn.setVisibility(View.GONE);
            if (mBeautyType == 0) {
                mFaceBeautyOneKeyLayout.setVisibility(View.GONE);
                mFaceBeautyCustomLayout.setVisibility(View.VISIBLE);
                mFaceBeautySwitchBtn.setSelected(false);
                mFaceBeautySwitchBtn.setText(R.string.century_facebeauty_custom);

                mFaceBeautyOnekeyBtn.setVisibility(View.VISIBLE);
                mFaceBeautyCustomBtn.setVisibility(View.GONE);
                //bv wuyonglin modify for adjust custom FaceMode view position 20200303 start
                mApp.getAppUi().setUIVisibility(IAppUi.SHUTTER_ROOTLAYOUT, View.GONE);
                LogHelper.i(TAG, "[onClick] mFaceBeautyCustomBtn mCustomBeautyType =" + mCustomBeautyType);
                if (mCustomBeautyType != -1) {
                    initCustomSeekBar(mCustomBeautyType);
                } else {
                    initCustomSeekBar(CUSTOM_TYPE_FACE_SMOOTH);
                }
                mApp.getAppUi().setIsCustomBeautyViewShow(true);
                //bv wuyonglin modify for adjust custom FaceMode view position 20200303 end
            } else {
                mFaceBeautyOneKeyLayout.setVisibility(View.VISIBLE);
                mFaceBeautyCustomLayout.setVisibility(View.GONE);
                mFaceBeautySwitchBtn.setSelected(false);
                mFaceBeautySwitchBtn.setText(R.string.century_facebeauty_onekey);
                mFaceBeautyOnekeyBtn.setVisibility(View.GONE);
                mFaceBeautyCustomBtn.setVisibility(View.VISIBLE);
                //bv wuyonglin add for adjust custom FaceMode view position 20200303 start
                mApp.getAppUi().setUIVisibility(IAppUi.SHUTTER_ROOTLAYOUT, View.VISIBLE);
                mApp.getAppUi().setIsCustomBeautyViewShow(false);
                //bv wuyonglin add for adjust custom FaceMode view position 20200303 end
            }
        } else {
            for (int i = 0; i < NUMBER_FACE_BEAUTY_ICON; i++) {
                if (mFaceBeautyImageViews[i] == view) {
                    initCustomSeekBar(i);
                    break;
                }
            }
        }
        //add by huanfei for adjust layout start
        //reLayoutSeekbar();
        //add by huanfei for adjust layout end
    }

    private void showViews() {
        if (mBeautyType == 0) {
            mFaceBeautyCustomBtn.setVisibility(View.GONE);
            mFaceBeautyOnekeyBtn.setVisibility(View.VISIBLE);
            mFaceBeautyOneKeyLayout.setVisibility(View.GONE);
            mFaceBeautyCustomLayout.setVisibility(View.VISIBLE);
            mFaceBeautySwitchBtn.setVisibility(View.GONE);
            mApp.getAppUi().setIsCustomBeautyViewShow(true);
        } else {
            //bv wuyonglin delete for adjust custom FaceMode view position 20200303 start
            mCustomBeautyType = -1;
            //bv wuyonglin delete for adjust custom FaceMode view position 20200303 end
            mFaceBeautyCustomBtn.setVisibility(View.VISIBLE);
            mFaceBeautyOnekeyBtn.setVisibility(View.GONE);
            mFaceBeautyCustomLayout.setVisibility(View.GONE);
            mFaceBeautySwitchBtn.setVisibility(View.GONE);
            mFaceBeautyOneKeyLayout.setVisibility(View.VISIBLE);
            mApp.getAppUi().setIsCustomBeautyViewShow(false);
        }
    }

    private void resetFaceBeautyParamters() {
        mFaceBeautyValue = DEFALUT_FACEBEAUTY_VALUE;
        mSmoothValue = DEFALUT_SMOOTH_VALUE;
        mWhilenValue = DEFALUT_WHITEN_VALUE;
        mThinValue = DEFALUT_THIN_VALUE;
        mBigEyeValue = DEFALUT_BIGEYE_VALUE;
        mBrightEyeValue = DEFALUT_BRIGHTEYE_VALUE;
        mBigNoseValue = DEFALUT_BIGNOSE_VALUE;
    }

    private void initCustomSeekBar(int type) {
        mCustomBeautyType = type;
        mCustomCHSeekBar.setVisibility(View.VISIBLE);
        for (int i = 0; i < NUMBER_FACE_BEAUTY_ICON; i++) {
            if (type == i) {
                Drawable drawable = mCameraActvity.getResources().getDrawable(FACE_BEAUTY_ICONS_HIGHTLIGHT[i]);
                drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                mFaceBeautyImageViews[i].setCompoundDrawables(null, drawable, null, null);
                mFaceBeautyImageViews[i].setSelected(true);
            } else {
                Drawable drawable = mCameraActvity.getResources().getDrawable(FACE_BEAUTY_ICONS_NORMAL[i]);
                drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                mFaceBeautyImageViews[i].setCompoundDrawables(null, drawable, null, null);
                mFaceBeautyImageViews[i].setSelected(false);
            }
        }
        float mBeautylevel = Float.parseFloat(mDataStore.getValue("aiworks_beauty_level_" + type + "",
                DEFALUT_SMOOTH_VALUE + "", mDataStore.getCameraScope(mCameraId)));
        LogHelper.i(TAG, "[initCustomSeekBar] mBeautylevel : " + mBeautylevel + " type =" + type);
        mCustomCHSeekBar.setProgress((int) (mBeautylevel * mCustomCHSeekBar.getMax()));
    }

    private void applyListeners() {

        mFaceBeautyOnekeyBtn.setOnClickListener(this);
        mFaceBeautyCustomBtn.setOnClickListener(this);
        mFaceBeautySwitchBtn.setOnClickListener(this);
        mFaceBeautyExpandBtn.setOnClickListener(this);
        for (int i = 0; i < NUMBER_FACE_BEAUTY_ICON; i++) {
            if (null != mFaceBeautyImageViews[i]) {
                mFaceBeautyImageViews[i].setOnClickListener(this);
            }
        }
        mOneKeyCHSeekBar.setMax(10);
        mOneKeyCHSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mOneKeyCHSeekBar.setSeekBarText("" + progress);
                mFaceBeautyValue = (progress) * 1.0f / seekBar.getMax();
                LogHelper.i(TAG, "mOneKeyCHSeekBar [onProgressChanged] progress : " + progress + " mFaceBeautyValue =" + mFaceBeautyValue + " mCustomBeautyType =" + mCustomBeautyType);
                mViewChangeListener.onBeautyLevelChanged(mFaceBeautyValue, mCustomBeautyType);
            }
        });
        LogHelper.i(TAG, "[applyListeners] mFaceBeautyValue : " + mFaceBeautyValue + " mCustomBeautyType =" + mCustomBeautyType);

        if (mCustomBeautyType == -1) {
            mFaceBeautyValue = Float.parseFloat(mDataStore.getValue("aiworks_beauty_all_level", DEFALUT_FACEBEAUTY_VALUE + "", mDataStore.getCameraScope(mCameraId)));
            mOneKeyCHSeekBar.setProgress((int) (mFaceBeautyValue * mOneKeyCHSeekBar.getMax()));
        }

        mCustomCHSeekBar.setMax(10);
        mCustomCHSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mCustomCHSeekBar.setSeekBarText("" + progress);
                LogHelper.i(TAG, "[mCustomCHSeekBar] final mSmoothValue : " + mSmoothValue + " progress =" + progress);
                mViewChangeListener.onBeautyLevelChanged(progress * 1.0f / seekBar.getMax(), mCustomBeautyType);
                mCustomProgress = progress;
            }
        });
    }

    /**
     * Used to show current beauty level.
     *
     * @param msg the beauty level.
     */
    public void showView(int cameraId) {
        mMainHandler.obtainMessage(GYBEAUTY_VIEW_INIT_AND_SHOW, cameraId).sendToTarget();
    }

    public View getFaceBeautyView() {
        return mBeautyofLayout;
    }
}
