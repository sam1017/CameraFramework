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

package com.mediatek.camera.feature.setting;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;

import com.mediatek.camera.R;
import com.mediatek.camera.common.IAppUi;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.widget.RotateStrokeTextView;
import com.mediatek.camera.common.utils.CameraUtil;
//bv wuyonglin add for screen 1440px adjust all icon position 20200709 start
import android.view.WindowManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.content.Context;
import android.widget.RelativeLayout;
//bv wuyonglin add for screen 1440px adjust all icon position 20200709 end

/**
 * Default implement of indicator view.
 */
public class ContinuousShotView {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(
            ContinuousShotView.class.getSimpleName());
    public static final int INIT_CONTINUOUS_SHOT_VIEW = 1000;
    public static final int UNINIT_CONTINUOUS_SHOT_VIEW = 1001;
    public static final int SHOW_CONTINUOUS_SHOT_VIEW = 1002;
    public static final int HIDE_CONTINUOUS_SHOT_VIEW = 1003;
    public static final int SET_TEXT_VIEW_ORIENTATION = 1004;
    private boolean mIsSupported = true;
    private IApp mApp;
    private ViewGroup mRootViewGroup;
    private ViewGroup mContinuousShotRoot;
    private RotateStrokeTextView mRotateStrokeTextView;
    private Handler mIndicatorViewHandler;
    private ContinuousShotIndicatorState mIndicatorState;
    //add by huangfei for continuous shot nums start
    private static final int DELAY_SECONDS = 500;
    //add by huangfei for continuous shot nums end
    private  OnOrientationChangeListenerImpl mOrientationChangeListener;
    //bv wuyonglin add for screen 1440px adjust all icon position 20200709 start
    private int mScreenHeight;
    //bv wuyonglin add for screen 1440px adjust all icon position 20200709 end

    /**
     * view state.
     */
    private enum ContinuousShotIndicatorState {
        INIT, SHOW, HIDE, UNINT
    }

    /**
     * init view layout.
     * @param app the Iapp handler.
     */
    public void initIndicatorView(IApp app) {
        if (!mIsSupported) {
            return;
        }
        mApp = app;
        mIndicatorViewHandler = new MainHandler(app.getActivity().getMainLooper());
        mIndicatorState = ContinuousShotIndicatorState.INIT;
        mIndicatorViewHandler.sendEmptyMessage(INIT_CONTINUOUS_SHOT_VIEW);
                mOrientationChangeListener = new OnOrientationChangeListenerImpl();
                mApp.registerOnOrientationChangeListener(mOrientationChangeListener);
        //bv wuyonglin add for screen 1440px adjust all icon position 20200709 start
        WindowManager wm = (WindowManager) mApp.getActivity().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        display.getRealMetrics(dm);
        mScreenHeight = dm.heightPixels;
        //bv wuyonglin add for screen 1440px adjust all icon position 20200709 end
    }

    /**
     * set view indicatorlayout.
     * @param orientation g-sensor orientation.
     */
    public void setIndicatorViewOrientation(int orientation) {
        if (!mIsSupported) {
            return;
        }
        if (mIndicatorViewHandler != null) {
            LogHelper.d(TAG, "setIndicatorViewOrientation(), orientation = " + orientation);
            mIndicatorViewHandler.obtainMessage(SET_TEXT_VIEW_ORIENTATION,
                    orientation).sendToTarget();
        }

    }

    /**
     * disable indicator, not show.
     */
    public void disableIndicator() {
        mIsSupported = false;
    }

    /**
     * show indicator with the image number.
     * @param num the image number.
     */
    public void showIndicatorView(int num) {
        if (mIndicatorViewHandler != null && mIsSupported) {
            LogHelper.d(TAG, "showIndicatorView(), num = " + num);
            mIndicatorViewHandler.obtainMessage(SHOW_CONTINUOUS_SHOT_VIEW, num).sendToTarget();
            mIndicatorState = ContinuousShotIndicatorState.SHOW;
        }
    }

    /**
     * hide the indicator view.
     */
    public void hideIndicatorView() {
        if (mIndicatorViewHandler != null
                && mIsSupported
                && mIndicatorState == ContinuousShotIndicatorState.SHOW) {
            mIndicatorViewHandler.sendEmptyMessage(HIDE_CONTINUOUS_SHOT_VIEW);
            mIndicatorState = ContinuousShotIndicatorState.HIDE;
        }
    }

    /**
     * release the indicator view handler.
     */
    public void unInitIndicatorView() {
        if (mIndicatorViewHandler != null && mIsSupported) {
            mIndicatorViewHandler.sendEmptyMessage(UNINIT_CONTINUOUS_SHOT_VIEW);
            mIndicatorState = ContinuousShotIndicatorState.UNINT;
            mApp.unregisterOnOrientationChangeListener(mOrientationChangeListener);
        }
    }

    /**
     * clear all the message.
     */
    public void clearIndicatorAllMessage() {
        if (mIndicatorViewHandler != null && mIsSupported) {
            mIndicatorViewHandler.removeMessages(INIT_CONTINUOUS_SHOT_VIEW);
            mIndicatorViewHandler.removeMessages(SHOW_CONTINUOUS_SHOT_VIEW);
            mIndicatorViewHandler.removeMessages(HIDE_CONTINUOUS_SHOT_VIEW);
            mIndicatorViewHandler.removeMessages(UNINIT_CONTINUOUS_SHOT_VIEW);
            mIndicatorViewHandler.removeMessages(SET_TEXT_VIEW_ORIENTATION);
        }
    }

    /**
     * clear the given message.
     * @param msg which msg to be cleared.
     */
    public void clearIndicatorMessage(int msg) {
        if (mIndicatorViewHandler != null && mIsSupported) {
            mIndicatorViewHandler.removeMessages(msg);
        }
    }

    /**
     * Handler let some task execute in main thread.
     */
    private class MainHandler extends Handler {
        MainHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            LogHelper.d(TAG, "[handleMessage]msg.what = " + msg.what + ", msg.obj = " + msg.obj);
            switch (msg.what) {
                case INIT_CONTINUOUS_SHOT_VIEW:
                    init(mApp.getActivity(), mApp.getAppUi());
                    break;
                case UNINIT_CONTINUOUS_SHOT_VIEW:
                    unInit();
                    break;
                case SHOW_CONTINUOUS_SHOT_VIEW:
                    show(String.valueOf(msg.obj));
                    break;
                case HIDE_CONTINUOUS_SHOT_VIEW:
                    hide();
                    break;
                case SET_TEXT_VIEW_ORIENTATION:
                    String value = String.valueOf(msg.obj);
                    int orientation = Integer.valueOf(value);
                    LogHelper.d(TAG, "[handleMessage], orientation = " + orientation);
                    setOrientation(orientation);
                    break;
                default:
                    break;
            }
        }
    }

    private void init(Activity mActivity, IAppUi mAppUi) {
        mRootViewGroup = mAppUi.getModeRootView();
        View viewLayout = mActivity.getLayoutInflater().inflate(R.layout.continuous_shot_view,
                mRootViewGroup, true);
        mContinuousShotRoot = (ViewGroup) viewLayout.findViewById(R.id.continuous_root);
        mRotateStrokeTextView = (RotateStrokeTextView) viewLayout.findViewById(R.id.shot_num);
        LogHelper.d(TAG, "[init] mRotateStrokeTextView = " + mRotateStrokeTextView);
        //bv wuyonglin add for screen 1440px adjust all icon position 20200709 start
	if (mScreenHeight == 1560) {
            RelativeLayout.LayoutParams mTextViewLayoutParams = (RelativeLayout.LayoutParams) mRotateStrokeTextView.getLayoutParams();
            mTextViewLayoutParams.topMargin = mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.camera_zoom_view_margin_top_1560px);
            mRotateStrokeTextView.setLayoutParams(mTextViewLayoutParams);
	} else if (mScreenHeight == 1440) {
            RelativeLayout.LayoutParams mTextViewLayoutParams = (RelativeLayout.LayoutParams) mRotateStrokeTextView.getLayoutParams();
            mTextViewLayoutParams.topMargin = mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.camera_zoom_view_margin_top_1440px);
            mRotateStrokeTextView.setLayoutParams(mTextViewLayoutParams);
	} else if (mScreenHeight == 2300) {
            RelativeLayout.LayoutParams mTextViewLayoutParams = (RelativeLayout.LayoutParams) mRotateStrokeTextView.getLayoutParams();
            mTextViewLayoutParams.topMargin = mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.camera_zoom_view_margin_top_2300px);
            mRotateStrokeTextView.setLayoutParams(mTextViewLayoutParams);
	} else if (mScreenHeight == 2400) {
            RelativeLayout.LayoutParams mTextViewLayoutParams = (RelativeLayout.LayoutParams) mRotateStrokeTextView.getLayoutParams();
            mTextViewLayoutParams.topMargin = mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.camera_zoom_view_margin_top_2400px);
            mRotateStrokeTextView.setLayoutParams(mTextViewLayoutParams);
	}
        //bv wuyonglin add for screen 1440px adjust all icon position 20200709 end
        hide();
    }

    private void show(String msg) {
        if (mRotateStrokeTextView != null) {
            LogHelper.d(TAG, "[show] msg = " + msg);
            //bv wuyonglin add for continuous shot view show at same time with zoom view 20200604 start
            mApp.getAppUi().applyZoomViewVisibilityImmediately(View.INVISIBLE);
            //bv wuyonglin add for continuous shot view show at same time with zoom view 20200604 end
            mRotateStrokeTextView.setText(msg);
            mRotateStrokeTextView.setVisibility(View.VISIBLE);
        }
    }

    private void hide() {
        if (mRotateStrokeTextView != null) {
            mRotateStrokeTextView.setVisibility(View.INVISIBLE);
        }
        //bv wuyonglin add for continuous shot view show at same time with zoom view 20200604 start
        mApp.getAppUi().applyZoomViewVisibilityImmediately(View.VISIBLE);
        //bv wuyonglin add for continuous shot view show at same time with zoom view 20200604 end
    }

    private void setOrientation(int orientation) {
        if (mRotateStrokeTextView != null) {
            mRotateStrokeTextView.setOrientation(orientation, false);
        }
    }

    private void unInit() {
        if (mRootViewGroup != null) {
            mRotateStrokeTextView.setVisibility(View.GONE);
            mRootViewGroup.removeView(mRotateStrokeTextView);
            mRootViewGroup.removeView(mContinuousShotRoot);
            mRotateStrokeTextView = null;
            mContinuousShotRoot = null;
            mRootViewGroup = null;
        }
    }

    private class OnOrientationChangeListenerImpl implements IApp.OnOrientationChangeListener {

        @Override
        public void onOrientationChanged(int orientation) {
            if (mRotateStrokeTextView != null) {
                CameraUtil.rotateRotateLayoutChildView(mApp.getActivity(), mRotateStrokeTextView,
                        orientation, true);
            }
        }
    }
}
