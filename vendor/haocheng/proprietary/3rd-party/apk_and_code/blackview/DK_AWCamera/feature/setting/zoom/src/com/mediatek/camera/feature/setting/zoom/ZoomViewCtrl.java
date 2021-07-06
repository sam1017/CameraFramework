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
package com.mediatek.camera.feature.setting.zoom;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mediatek.camera.R;
import com.mediatek.camera.common.IAppUi;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.utils.CameraUtil;
import com.mediatek.camera.common.widget.RotateLayout;
import com.mediatek.camera.common.widget.RotateStrokeTextView;
//bv wuyonglin add for screen 1440px adjust all icon position 20200709 start
import android.view.WindowManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.content.Context;
import android.widget.RelativeLayout;
//bv wuyonglin add for screen 1440px adjust all icon position 20200709 end

/**
 * The zoom view.
 */
public class ZoomViewCtrl {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(ZoomViewCtrl.class.getSimpleName());
    private ViewGroup mRootViewGroup;
    private ViewGroup mZoomView;
    private IApp mApp;
    private RotateStrokeTextView mTextView;
    private MainHandler mMainHandler;
    //private IAppUi.HintInfo mZoomIndicatorHint;
    //Gesture and View Control
    private static final int ZOOM_TEXT_MARGION_VERTICAL_REVERSE = 120;
    private static final int ZOOM_TEXT_MARGION_VERTICAL = 40;
    private static final int ZOOM_TEXT_MARGION_VERTICAL_TABLET = 100;
    private static final int ZOOM_TEXT_MARGION_HORIZON = 2;
    private static final int ZOOM_VIEW_HIDE_DELAY_TIME = 3000;
    private static final int ZOOM_VIEW_SHOW = 0;
    private static final int ZOOM_VIEW_RESET = 1;
    private static final int ZOOM_VIEW_INIT = 2;
    private static final int ZOOM_VIEW_UNINIT = 3;
    private static final int ZOOM_VIEW_ORIENTATION_CHANGED = 4;
    private static final int ZOOM_VIEW_HIDE = 5;
    //bv wuyonglin add for adjust BrokehMode view 20200227 start
    private static final int ZOOM_VIEW_MARGIN_BOTTOM_CHANGED = 6;
    private static final int ZOOM_VIEW_MARGIN_BOTTOM = 265;
    //bv wuyonglin add for adjust BrokehMode view 20200227 end
    //bv wuyonglin add for screen 1440px adjust all icon position 20200709 start
    private int mCameraZoomViewMarginTop;
    private int mScreenHeight;
    //bv wuyonglin add for screen 1440px adjust all icon position 20200709 end

    /**
     * Init the view.
     * @param app the activity.
     */
    public void init(IApp app) {
        LogHelper.d(TAG, "[init]");
        mApp = app;
        mMainHandler = new MainHandler(app.getActivity().getMainLooper());
        mMainHandler.sendEmptyMessage(ZOOM_VIEW_INIT);
        //bv wuyonglin add for screen 1440px adjust all icon position 20200709 start
        WindowManager wm = (WindowManager) mApp.getActivity().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        display.getRealMetrics(dm);
        mScreenHeight = dm.heightPixels;
        if (mScreenHeight == 1560) {
            mCameraZoomViewMarginTop = mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.camera_zoom_view_margin_top_1560px);
        } else if (mScreenHeight == 1440) {
            mCameraZoomViewMarginTop = mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.camera_zoom_view_margin_top_1440px);
        } else if (mScreenHeight == 2300) {
            mCameraZoomViewMarginTop = mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.camera_zoom_view_margin_top_2300px);
        } else {
            mCameraZoomViewMarginTop = mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.camera_zoom_view_margin_top);
        }
        //bv wuyonglin add for screen 1440px adjust all icon position 20200709 end
    }

    /**
     * To destroy the zoom view.
     */
    public void unInit() {
        mMainHandler.sendEmptyMessage(ZOOM_VIEW_RESET);
        mMainHandler.sendEmptyMessage(ZOOM_VIEW_UNINIT);
    }

    /**
     * Used to show ratio test when zooming.
     * @param msg the ratio test.
     */
    public void showView(String msg) {
        mMainHandler.obtainMessage(ZOOM_VIEW_SHOW, msg).sendToTarget();
    }

    /**
     * Used to hide ratio test when zooming.
     */
    public void hideView() {
        mMainHandler.sendEmptyMessage(ZOOM_VIEW_HIDE);
    }

    /**
     * reset camera view after zoom done.
     */
    public void resetView() {
        mMainHandler.sendEmptyMessageDelayed(ZOOM_VIEW_RESET, ZOOM_VIEW_HIDE_DELAY_TIME);
    }

    /**
     * when phone orientation changed, the zoom view will be updated.
     * @param orientation the orientation of g-sensor.
     */
    public void onOrientationChanged(int orientation) {
        if (mMainHandler == null) {
            return;
        }
        //mMainHandler.obtainMessage(ZOOM_VIEW_ORIENTATION_CHANGED, orientation).sendToTarget();
    }

    //bv wuyonglin add for adjust BrokehMode view 20200227 start
    public void onMarginBottomChanged(boolean isSpecialMode) {
        mMainHandler.obtainMessage(ZOOM_VIEW_MARGIN_BOTTOM_CHANGED, isSpecialMode).sendToTarget();
    }
    //bv wuyonglin add for adjust BrokehMode view 20200227 end

    /**
     * clear the invalid view such as indicator view.
     */
    public void clearInvalidView() {
        mMainHandler.removeMessages(ZOOM_VIEW_RESET);
        mMainHandler.removeMessages(ZOOM_VIEW_SHOW);
        mApp.getAppUi().setUIVisibility(IAppUi.INDICATOR, View.INVISIBLE);
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
                case ZOOM_VIEW_SHOW:
                    show((String) msg.obj);
                    break;
                case ZOOM_VIEW_HIDE:
                    hide();
                    break;
                case ZOOM_VIEW_RESET:
                    reset();
                    break;
                case ZOOM_VIEW_INIT:
                    initView();
                    break;
                case ZOOM_VIEW_UNINIT:
                    unInitView();
                    break;
                case ZOOM_VIEW_ORIENTATION_CHANGED:
                    updateOrientation((Integer) msg.obj);
                    break;
                //bv wuyonglin add for adjust BrokehMode view 20200227 start
                case ZOOM_VIEW_MARGIN_BOTTOM_CHANGED:
                    updateMarginBottom((boolean) msg.obj);
                    break;
                //bv wuyonglin add for adjust BrokehMode view 20200227 end
                default:
                    break;
            }
        }
    }

    private void show(String msg) {
        if (mZoomView == null) {
            return;
        }
        //mZoomIndicatorHint.mHintText = msg;
        //mApp.getAppUi().showScreenHint(mZoomIndicatorHint);
        if (mTextView != null) {
            LogHelper.d(TAG, "[show] msg = " + msg);
            mTextView.setText(msg);
            //mTextView.setVisibility(View.VISIBLE);
            mTextView.setVisibility(View.GONE);
        }
    }

    private void hide() {
        if (mZoomView == null) {
            return;
        }
        //mApp.getAppUi().hideScreenHint(mZoomIndicatorHint);
    }

    private void reset() {
        if (mZoomView == null) {
            return;
        }
        //mApp.getAppUi().hideScreenHint(mZoomIndicatorHint);
        //mApp.getAppUi().setUIVisibility(IAppUi.INDICATOR, View.VISIBLE);
    }

    private void initView() {
        mRootViewGroup = mApp.getAppUi().getModeRootView();
        mZoomView = (ViewGroup) mApp.getActivity().getLayoutInflater()
                .inflate(R.layout.bv_zoom_view,
                mRootViewGroup, true).findViewById(R.id.zoom_layout);
        mTextView = (RotateStrokeTextView) mZoomView.findViewById(R.id.zoom_ratio);
        mTextView.setVisibility(View.GONE);

        //bv wuyonglin add for screen 1440px adjust all icon position 20200709 start
	if (mScreenHeight == 1560) {
            RelativeLayout.LayoutParams mTextViewLayoutParams = (RelativeLayout.LayoutParams) mTextView.getLayoutParams();
            mTextViewLayoutParams.topMargin = mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.camera_zoom_view_margin_top_1560px);
            mTextView.setLayoutParams(mTextViewLayoutParams);
	} else if (mScreenHeight == 1440) {
            RelativeLayout.LayoutParams mTextViewLayoutParams = (RelativeLayout.LayoutParams) mTextView.getLayoutParams();
            mTextViewLayoutParams.topMargin = mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.camera_zoom_view_margin_top_1440px);
            mTextView.setLayoutParams(mTextViewLayoutParams);
	} else if (mScreenHeight == 2300) {
            RelativeLayout.LayoutParams mTextViewLayoutParams = (RelativeLayout.LayoutParams) mTextView.getLayoutParams();
            mTextViewLayoutParams.topMargin = mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.camera_zoom_view_margin_top_2300px);
            mTextView.setLayoutParams(mTextViewLayoutParams);
	}
        //bv wuyonglin add for screen 1440px adjust all icon position 20200709 end
        //mRootViewGroup.addView(mZoomView);

        //mZoomIndicatorHint = new IAppUi.HintInfo();
        //mZoomIndicatorHint.mType = IAppUi.HintType.TYPE_ALWAYS_BOTTOM;
        //mZoomIndicatorHint.mDelayTime = ZOOM_VIEW_HIDE_DELAY_TIME;
    }

    private void unInitView() {
        mRootViewGroup.removeView(mZoomView);
        mZoomView = null;
    }

    private void updateOrientation(int orientation) {
        RelativeLayout.LayoutParams params =
                (RelativeLayout.LayoutParams) mTextView.getLayoutParams();
        switch (orientation) {
            case 0:
                if (CameraUtil.isTablet()) {
                    params.setMargins(params.leftMargin, dpToPixel(ZOOM_TEXT_MARGION_VERTICAL_TABLET),
                            params.rightMargin, params.bottomMargin);
                } else {
                    params.setMargins(params.leftMargin, dpToPixel(ZOOM_TEXT_MARGION_VERTICAL),
                            params.rightMargin, params.bottomMargin);
                }
                break;
            case 180:
                params.setMargins(params.leftMargin, dpToPixel(ZOOM_TEXT_MARGION_VERTICAL_REVERSE),
                        params.rightMargin, params.bottomMargin);
                break;
            case 90:
            case 270:
                params.setMargins(params.leftMargin, dpToPixel(ZOOM_TEXT_MARGION_HORIZON),
                        params.rightMargin, params.bottomMargin);
                break;
            default:
                break;
        }
        mTextView.setLayoutParams(params);
        CameraUtil.rotateRotateLayoutChildView(mApp.getActivity(), mZoomView, orientation, true);
    }

    //bv wuyonglin add for adjust BrokehMode view 20200227 start
    private void updateMarginBottom(boolean isSpecialMode) {
	if (isSpecialMode) {
        RelativeLayout.LayoutParams params =
                (RelativeLayout.LayoutParams) mTextView.getLayoutParams();
        //bv liangchangwei add for modify video and photomode show zoom
        int topMargin = mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.camera_zoom_view_new_margin_top);
        LogHelper.i(TAG,"updateMarginBottom topMargin = " + topMargin + " params.bottomMargin = " + params.bottomMargin + " modify to: " + dpToPixel(ZOOM_VIEW_MARGIN_BOTTOM));
        params.setMargins(params.leftMargin, topMargin,
                params.rightMargin, params.bottomMargin);
        mTextView.setLayoutParams(params);
	} else {
        RelativeLayout.LayoutParams params =
                (RelativeLayout.LayoutParams) mTextView.getLayoutParams();
	//bv wuyonglin modify for adjust all icon position 20200612 start
        params.setMargins(params.leftMargin, mCameraZoomViewMarginTop,	//bv wuyonglin modify for screen 1440px adjust all icon position 20200709
                params.rightMargin, params.bottomMargin);
	//bv wuyonglin modify for adjust all icon position 20200612 end
        mTextView.setLayoutParams(params);
	}
    }
    //bv wuyonglin add for adjust BrokehMode view 20200227 end

    private int dpToPixel(int dp) {
        float scale = mApp.getActivity().getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public RotateStrokeTextView getZoomView() {
        return mTextView;
    }
}
