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
package com.mediatek.camera.feature.mode.pro.view;

import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.util.Log;

import com.mediatek.camera.Config;
import com.mediatek.camera.R;
import com.mediatek.camera.common.IAppUi;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.utils.CameraUtil;
import com.mediatek.camera.common.widget.RotateLayout;

/**
 * The sdof view manager.
 */
public class ProViewCtrl implements View.OnClickListener, SeekArcFrameLayout.OnHintChangeListener {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(ProViewCtrl.class.getSimpleName());
    private static final int PRO_VIEW_INIT_AND_SHOW = 0;
    private static final int PRO_VIEW_UNINIT = 1;
    private static final int PRO_VIEW_REFRESH = 2;
    private ViewGroup mRootViewGroup;
    private IApp mApp;
    private RelativeLayout mMainLinearLayout;
    private LinearLayout mToolBarLinearLayout;
    private TextView mMFTextView;
    private TextView mShutterTextView;
    private TextView mWBTextView;
    private TextView mISOTextView;
    private TextView mEXPTextView;
    private MainHandler mMainHandler;

    private SeekArcFrameLayout mSeekArcFrameLayout;

    private LinearLayout mToolHint;
    private TextView mToolTitle;
    private TextView mToolValue;
    private int mMarginTop = 0;
    private int mHightGrap = 0;
    private OnProgressChangeListener mOnProgressChangeListener;

    //add by huangfei for MF start
    private int mDistance;
    //add by huangfei for MF end    

    public void init(IApp app) {
        mApp = app;
        mMarginTop = app.getActivity().getResources().getDimensionPixelOffset(R.dimen.seekarc_hint_marginTop);
        mHightGrap = app.getActivity().getResources().getDimensionPixelOffset(R.dimen.seekarc_hint_marginTop_grap);
        mMainHandler = new MainHandler(app.getActivity().getMainLooper());
        mMainHandler.sendEmptyMessage(PRO_VIEW_INIT_AND_SHOW);
    }

    private void initView() {
        mRootViewGroup = mApp.getAppUi().getModeRootView();
        mMainLinearLayout = (RelativeLayout) mApp.getActivity().getLayoutInflater()
                .inflate(R.layout.pro_view,
                        mRootViewGroup, false).findViewById(R.id.pro_main_layout);

        mMainLinearLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    if (mSeekArcFrameLayout != null && mSeekArcFrameLayout.getVisibility() == View.VISIBLE) {
                        mSeekArcFrameLayout.setVisibility(View.GONE);
                        mToolBarLinearLayout.setVisibility(View.VISIBLE);
                        if(Config.isMFSupport(mApp.getActivity())){
                            mOnProgressChangeListener.onShowSeekArcFrameLayout(false);
                            /*bv modify by liangchangwei for fixbug 3422 --*/
                            return false;
                            /*bv modify by liangchangwei for fixbug 3422 --*/
                        }
                    }
                }
                return false;
            }
        });

        mToolBarLinearLayout = (LinearLayout) mMainLinearLayout.findViewById(R.id.pro_tool_bar);
        mToolBarLinearLayout.setOnClickListener(this);

        //add by huangfei for mf start
        if(Config.isMFSupport(mApp.getActivity())){
            mMFTextView = (TextView) mMainLinearLayout.findViewById(R.id.pro_tool_mf);
            mMFTextView.setVisibility(View.VISIBLE);
        }        
        //add by huangfei for mf end

        //add by huangfei for shutter start
        if(Config.isShutterSupport(mApp.getActivity())){
            mShutterTextView = (TextView) mMainLinearLayout.findViewById(R.id.pro_tool_shutter);
            mShutterTextView.setVisibility(View.VISIBLE);
        }
        //add by huangfei for shutter end

        mWBTextView = (TextView) mMainLinearLayout.findViewById(R.id.pro_tool_wb);
        mISOTextView = (TextView) mMainLinearLayout.findViewById(R.id.pro_tool_iso);
        mEXPTextView = (TextView) mMainLinearLayout.findViewById(R.id.pro_tool_exp);
        mEXPTextView.setText(String.valueOf(SeekArcFrameLayout.mEXPValue[SeekArcFrameLayout.mEXPValue.length / 2]));

        mSeekArcFrameLayout = (SeekArcFrameLayout) mMainLinearLayout.findViewById(R.id.pro_seek_bar);
        mSeekArcFrameLayout.setOnHintChangeListener(this);

        mToolHint = (LinearLayout) mMainLinearLayout.findViewById(R.id.pro_tool_hint);
        mToolTitle = (TextView) mMainLinearLayout.findViewById(R.id.pro_tool_title);
        mToolValue = (TextView) mMainLinearLayout.findViewById(R.id.pro_tool_value);

        mRootViewGroup.addView(mMainLinearLayout);
    }

    public void unInit() {

        if(mSeekArcFrameLayout!=null){
            mSeekArcFrameLayout.destroy();
        }

        mMainHandler.sendEmptyMessage(PRO_VIEW_UNINIT);
    }

    private void unInitView() {
        mRootViewGroup.removeView(mMainLinearLayout);
        mMainLinearLayout = null;
        if(mMainHandler != null) {
            mMainHandler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    public void onClick(View view) {
        if (view == mToolBarLinearLayout) {
            if (mSeekArcFrameLayout != null && mSeekArcFrameLayout.getVisibility() == View.GONE) {

                //add by huangfei for MF start
                mSeekArcFrameLayout.setFoucsDistance(mDistance);
                if(Config.isMFSupport(mApp.getActivity())){
                    mOnProgressChangeListener.onShowSeekArcFrameLayout(true);
                }
                //add by huangfei for MF end

                mSeekArcFrameLayout.setVisibility(View.VISIBLE);
                mToolBarLinearLayout.setVisibility(View.GONE);

                //add by huangfei for MF start
                if(Config.isMFSupport(mApp.getActivity())){
                    mSeekArcFrameLayout.initMF();
                }
                //add by huangfei for MF end
            }
        }
    }

    @Override
    public void onProgressChanged(SeekArc seekArc, String progress, String hint) {
        if (mOnProgressChangeListener != null) {
            int id = seekArc.getId();
            mToolValue.setText(hint);
            if (id == SeekArcFrameLayout.Style.AWB.constId) {
                mOnProgressChangeListener.onWbProgressChanged(progress);
                mWBTextView.setText(hint);
            } else if (id == SeekArcFrameLayout.Style.ISO.constId) {
                mOnProgressChangeListener.onISOProgressChanged(progress);
                mISOTextView.setText(hint);
            } else if (id == SeekArcFrameLayout.Style.MF.constId) {
                int step = Integer.valueOf(progress);
                mOnProgressChangeListener.onMfProgressChanged(String.valueOf(step));
                mMFTextView.setText(hint);
            } else if (id == SeekArcFrameLayout.Style.EXP.constId) {
                mOnProgressChangeListener.onEXPProgressChanged(progress);
                mEXPTextView.setText(hint);
            } else if (id == SeekArcFrameLayout.Style.SHUTTER.constId) {
                mOnProgressChangeListener.onShutterrogressChanged(progress);
                mShutterTextView.setText(hint);
            }
        }
    }

    @Override
    public void onStartTrackingTouch(String title, String value, int index) {
        mToolHint.setVisibility(View.VISIBLE);
        mToolTitle.setText(title);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mToolHint.getLayoutParams();
        layoutParams.topMargin = mMarginTop + index * mHightGrap;
        mToolHint.setLayoutParams(layoutParams);
    }

    @Override
    public void onStopTrackingTouch() {
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mToolHint.getLayoutParams();
        layoutParams.topMargin = 0;
        mToolHint.setLayoutParams(layoutParams);
        mToolHint.setVisibility(View.GONE);
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
                case PRO_VIEW_INIT_AND_SHOW:
                    initView();
                    break;
                case PRO_VIEW_UNINIT:
                    unInitView();
                    break;
                case PRO_VIEW_REFRESH:
                    mSeekArcFrameLayout.setVisibility(View.GONE);
                    mToolBarLinearLayout.setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }
        }
    }

    public void setProgressChangeListener(OnProgressChangeListener listener) {
        mOnProgressChangeListener = listener;
    }
    public void refreshSeekArc(){
        if(mSeekArcFrameLayout!=null&&mSeekArcFrameLayout.getVisibility() == View.VISIBLE){
            mMainHandler.sendEmptyMessage(PRO_VIEW_REFRESH);
        }
    }

    public static abstract interface OnProgressChangeListener {
        public abstract void onWbProgressChanged(String progress);

        public abstract void onISOProgressChanged(String progress);

        public abstract void onMfProgressChanged(String progress);

        public abstract void onEXPProgressChanged(String progress);

        //add by huangfei for shutter start
        public abstract void onShutterrogressChanged(String progress);

        public abstract void onShowSeekArcFrameLayout(boolean show);
        //add by huangfei for shutter end
    }

    //add by huangfei for MF start
    public void setFoucsDistance(int distance) {
        mDistance = distance;
    }

    public void resetMF(){

        //modify by huangfei for null point exception start
        //mSeekArcFrameLayout.resetMF();
        if(mSeekArcFrameLayout!=null && Config.isMFSupport(mApp.getActivity())){
            mSeekArcFrameLayout.resetMF();
            mApp.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mMFTextView.setText(mApp.getActivity().getResources().getString(R.string.pro_mode_auto));
                }
            });
        }
        //modify by huangfei for null point exception end
    }
    //add by huangfei for MF end

    //bv wuyonglin add for after iso and shutter speed change not auto exp should unavailable 20200117 start
    public SeekArcFrameLayout getSeekArcFrameLayout() {
        return mSeekArcFrameLayout;
    }
    //bv wuyonglin add for after iso and shutter speed change not auto exp should unavailable 20200117 end
}
