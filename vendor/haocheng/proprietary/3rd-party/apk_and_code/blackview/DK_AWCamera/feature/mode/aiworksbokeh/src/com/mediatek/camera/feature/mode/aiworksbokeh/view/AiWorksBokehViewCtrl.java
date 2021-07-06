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
package com.mediatek.camera.feature.mode.aiworksbokeh.view;

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
import android.view.Gravity;
import android.widget.FrameLayout;
import android.view.WindowManager;
import android.view.Display;
import android.graphics.Point;
import android.content.Context;
//bv wuyonglin add for adjust screen height 1560px top bar position 20200628 start
import android.view.WindowManager;
import android.util.DisplayMetrics;
import android.view.Display;
//bv wuyonglin add for adjust screen height 1560px top bar position 20200628 end


/**
 * The sdof view manager.
 */
public class AiWorksBokehViewCtrl {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(AiWorksBokehViewCtrl.class.getSimpleName());
    // Stereo Photo warning message
    //Gesture and View Control
    private static final int SHOW_INFO_LENGTH_LONG = 5 * 1000;
    private static final int BOKEH_VIEW_SHOW = 1;
    private static final int BOKEH_VIEW_UNINIT = 2;
    private static final int SDOF_TEXT_VIEW_SHOW = 3;
    private static final int SDOF_TEXT_VIEW_HIDE = 4;
    private ViewGroup mRootViewGroup;
    private IApp mApp;
    private MainHandler mMainHandler;
    private ViewChangeListener mViewChangeListener;
    private int seekbarmax = 8;

    //add by huangfei for default Bokeh value start
    private int mDefaultBokehValue = 0;
    //add by huangfei for default Bokeh value end

    private IAppUi.HintInfo mGuideHint;

    private boolean isScreen_18_9 = false;//add by Jerry for 18:9
    private View mBokehofLayout;
    private CHSeekBar mBokehSeekbarLayout;
    private RelativeLayout mControl;
    ICameraContext mICameraContext;
    private DataStore mDataStore;
    private int mProgress;
    private AiWorksCameraAperture mCameraAperture;
    private int radius = 6;
    private int radiusMax = 10;
    private int radiusMin = 0;
    private int panelHeight = 0;
    private int panelWidth = 0;
    private boolean convert;
    private float radiusTime = 0;
    private int bokeh_x = -1;
    private int bokeh_y = -1;
    //private TextView mTextView;

    public AiWorksBokehViewCtrl( ICameraContext context){
        mICameraContext = context;
        mDataStore = mICameraContext.getDataStore();
    }
    /**
     * Init the view.
     * @param app the activity.
     */

    public void init(IApp app) {
        mApp = app;
        mMainHandler = new MainHandler(app.getActivity().getMainLooper());
        seekbarmax = app.getActivity().getResources().getInteger(R.integer.aiworks_bokeh_seekbar_max);
        mDefaultBokehValue = app.getActivity().getResources().getInteger(R.integer.aiworks_bokeh_level_default);
        WindowManager wm = (WindowManager) app.getActivity().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point point = new Point();
        display.getRealSize(point);
        panelHeight = Math.max(point.x, point.y);
        panelWidth = Math.min(point.x, point.y);
    }


    public void hideAperture() {
        LogHelper.d(TAG, "[hideAperture]... ");
        mApp.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mCameraAperture != null) {
                    mCameraAperture.hideView();
                }
            }
        });
    }

    public void showAperture() {
        LogHelper.d(TAG, "[showAperture]... ");
        mApp.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mCameraAperture != null) {
                    mCameraAperture.showView();
                }
            }
        });
    }

    private int clamp(int x, int min, int max) {
        if (x > max)
            return max;
        if (x < min)
            return min;
        return x;
    }

    /**
     * To destroy the bokeh view.
     */
    public void unInit() {
        mMainHandler.sendEmptyMessage(BOKEH_VIEW_UNINIT);
    }

    /**
     * when phone orientation changed, the zoom view will be updated.
     * @param orientation the orientation of g-sensor.
     */
    public void onOrientationChanged(int orientation) {
        if (mMainHandler != null) {
            //mMainHandler.obtainMessage(SDOF_VIEW_ORIENTATION_CHANGED, orientation).sendToTarget();
        }
    }

    /**
     * Set dof bar view change listener.
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
         * This method used for notify mode Bokeh level.
         * @param progress Bokeh  level
         */
        public void onBokehParameterChanged(int level,int x,int y,int radius);

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
 LogHelper.e(TAG, "[handleMessage] msg.what =" + msg.what);
            switch (msg.what) {
                /*case SDOF_TEXT_VIEW_SHOW:
                    mTextView.setVisibility(View.VISIBLE);
                break;
                case SDOF_TEXT_VIEW_HIDE:
                    mTextView.setVisibility(View.INVISIBLE);
                break;*/
                case BOKEH_VIEW_SHOW:
                    initView((int) msg.obj);
                    break;
                case BOKEH_VIEW_UNINIT:
                    unInitView();
                    break;
                default:
                    break;
            }
        }
    }

    private void initView(int level) {
        mRootViewGroup = mApp.getAppUi().getModeRootView();
        mBokehofLayout = (RelativeLayout) mApp.getActivity().getLayoutInflater()
                .inflate(R.layout.aiworks_bokeh_view,
                mRootViewGroup, false).findViewById(R.id.bokeh_rotate_layout);
        /*mCameraAperture = (AiWorksCameraAperture) mApp.getActivity().findViewById(R.id.aiworks_camera_aperture);
        if (mCameraAperture != null) {
            mCameraAperture.setOnProgressChangedListener(this);
            mCameraAperture.setBokehValue(radiusMax, radius);
            int len = AiWorksCameraAperture.dip2px(mApp.getActivity(), (float) 62.75);
            if (len > 0) {
                mCameraAperture.setApertureViewWidth(len);
            }
            mApp.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mCameraAperture.requestLayout();
                }
            });
        }*/
        mControl = (RelativeLayout)mBokehofLayout.findViewById(R.id.bokeh_bottom_controls);
        mControl.setVisibility(View.VISIBLE);
        //bv wuyonglin add for adjust screen height 1560px top bar position 20200628 start
        WindowManager wm = (WindowManager) mApp.getActivity().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        display.getRealMetrics(dm);
        if (dm.heightPixels == 1560) {
        RelativeLayout.LayoutParams mControlLayoutParams = (RelativeLayout.LayoutParams) mControl.getLayoutParams();
        mControlLayoutParams.bottomMargin = mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.hct_shutter_layout_height_1560px);
        mControl.setLayoutParams(mControlLayoutParams);
        //bv wuyonglin add for screen 1440px adjust all icon position 20200709 start
        } else if (dm.heightPixels == 1440) {
            RelativeLayout.LayoutParams mControlLayoutParams = (RelativeLayout.LayoutParams) mControl.getLayoutParams();
            mControlLayoutParams.bottomMargin = mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.hct_shutter_layout_height_1440px);
            mControl.setLayoutParams(mControlLayoutParams);
        //bv wuyonglin add for screen 1440px adjust all icon position 20200709 end
        } else if (dm.heightPixels == 2300) {
            RelativeLayout.LayoutParams mControlLayoutParams = (RelativeLayout.LayoutParams) mControl.getLayoutParams();
            mControlLayoutParams.bottomMargin = mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.hct_shutter_layout_height_2300px);
            mControl.setLayoutParams(mControlLayoutParams);
        } else if (dm.heightPixels == 2400) {
            RelativeLayout.LayoutParams mControlLayoutParams = (RelativeLayout.LayoutParams) mControl.getLayoutParams();
            mControlLayoutParams.bottomMargin = mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.hct_shutter_layout_height_2400px);
            mControl.setLayoutParams(mControlLayoutParams);
        }
        //bv wuyonglin add for adjust screen height 1560px top bar position 20200628 end

        mBokehSeekbarLayout = (CHSeekBar)mBokehofLayout.findViewById(R.id.bokeh_seekbar);
        //mTextView = (TextView) mBokehofLayout.findViewById(R.id.bokeh_text_view);
        mBokehSeekbarLayout.setMax(seekbarmax);
        mProgress = seekbarmax - level;
        LogHelper.d(TAG, "[initView]- setProgress mProgress ="+mProgress);
        mBokehSeekbarLayout.setOnSeekBarChangeListener(mChangeListener);
        mBokehSeekbarLayout.setProgress(mProgress);
        mRootViewGroup.addView(mBokehofLayout);
    }

    private void unInitView() {
        reset();
        mRootViewGroup.removeView(mBokehofLayout);
        mBokehSeekbarLayout = null;
    }

    private void reset() {

    }  

  
    private int dpToPixel(int dp) {
        float scale = mApp.getActivity().getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    private SeekBar.OnSeekBarChangeListener mChangeListener
            = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
            LogHelper.e(TAG, "[onProgressChanged] progress =" + progress);
            //mTextView.setText(progress+"");
            mBokehSeekbarLayout.setSeekBarText("" + progress);
            mViewChangeListener.onBokehParameterChanged(seekbarmax - progress,bokeh_x,bokeh_y,radius);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            //mMainHandler.sendEmptyMessage(SDOF_TEXT_VIEW_SHOW);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            //add by huangfei for disconnect camear start
            //mMainHandler.sendEmptyMessage(SDOF_TEXT_VIEW_HIDE);
            //add by huangfei for disconnect camear end
        }
    };
    public void setBokehProgress(int progress ){
        LogHelper.d(TAG, "[setBokehProgress]- progress ="+progress);
        if(progress==-1){
            mBokehSeekbarLayout.setProgress(mDefaultBokehValue);
        }else {
            mBokehSeekbarLayout.setProgress(progress);
        }

    }

    /**
     * Used to show current bokeh level.
     * @param msg the bokeh level.
     */
    public void showView(int msg) {
        mMainHandler.obtainMessage(BOKEH_VIEW_SHOW, msg).sendToTarget();
    }

    public View getBokehView() {
        return mBokehofLayout;
    }
}
