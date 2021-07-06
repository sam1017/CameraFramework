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
package com.mediatek.camera.feature.mode.hctbokeh.view;

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


/**
 * The sdof view manager.
 */
public class BokehViewCtrl implements CameraAperture.OnProgressChangedListener {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(BokehViewCtrl.class.getSimpleName());
    // Stereo Photo warning message
    //Gesture and View Control
    private static final int SHOW_INFO_LENGTH_LONG = 5 * 1000;
    private static final int BOKEH_VIEW_INIT_AND_SHOW = 1;
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
    private RotateLayout mBokehofLayout;
    private SeekBar mBokehSeekbarLayout;
    private RelativeLayout mControl;
    ICameraContext mICameraContext;
    private DataStore mDataStore;
    private int mProgress;
    private CameraAperture mCameraAperture;
    private int radius = 6;
    private int radiusMax = 9;
    private int radiusMin = 0;
    private int panelHeight = 0;
    private int panelWidth = 0;
    private boolean convert;
    private float radiusTime = 0;
    private int bokeh_x = -1;
    private int bokeh_y = -1;
    private TextView mTextView;

    public BokehViewCtrl( ICameraContext context){
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
        mMainHandler.sendEmptyMessage(BOKEH_VIEW_INIT_AND_SHOW);
        seekbarmax = app.getActivity().getResources().getInteger(R.integer.bokeh_seekbar_max);
        mDefaultBokehValue = app.getActivity().getResources().getInteger(R.integer.bokeh_level_default);
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

    public void showApertureView(final int x1, final int y1,int width,int height) {
        LogHelper.e(TAG, "[showApertureView] x:" + x1 + " y:" + y1 );  
        mApp.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int pointX = x1,pointY = y1;
                if (pointX == -1 && pointY == -1) {
                    pointX = width / 2;
                    pointX = height / 2;
                }

                //if (GYConfig.BOKEH_FOCUS_MOVING_SHOW_APRTURE) {
                //    onSingleTapUp(x, y);
                //}    
                if (mCameraAperture != null) {
                    int x1= pointX,y1 =pointY;
                    mCameraAperture.showView();
                    FrameLayout.LayoutParams ps = (FrameLayout.LayoutParams) mCameraAperture.getLayoutParams();
                    int previewHeight = panelHeight;
                    int previewWidth = panelWidth;
                    int seekbarW = mCameraAperture.getWidth() - mCameraAperture.getApertureViewWidth();
                    convert = ((int) pointX + mCameraAperture.mApertureViewWidth)  >= previewWidth ? true : false;
                    int left = 0;
                    int top = 0;
                    int aperture_w = mCameraAperture.getWidth();
                    int aperture_h = mCameraAperture.getHeight();
                    if(convert){
                        left =clamp((int) x1 - seekbarW, 0, previewWidth);
                    }else{
                        left = CameraUtil.clamp(x1 - mCameraAperture.getApertureCenterX(), 0 - mCameraAperture.getApertureCenterX(), previewWidth - aperture_w + (int) mCameraAperture.getApertureCenterDisX());
                    }
                    top = CameraUtil.clamp(y1 - mCameraAperture.getApertureCenterY(), (0 - mCameraAperture.getApertureCenterY()), previewHeight - aperture_h);
                    ps.setMargins(left, top, 0, 0);
                    LogHelper.e(TAG, "[showApertureView] left " + left + " top " + top + " previewWidth " + previewWidth + " previewHeight " + previewHeight);
                    mCameraAperture.requestLayout();
                    LogHelper.e(TAG, "[showApertureView] aperture_w " + aperture_w + " aperture_h " + aperture_h);
                    mCameraAperture.convertSeekbarLeft(convert);
                    bokeh_x = left + aperture_w / 2;
                    bokeh_y = top + aperture_h / 2;
                    mViewChangeListener.onBokehParameterChanged(mProgress,bokeh_x,bokeh_y,radius);
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

    @Override
    public void onProgressChanged(int arg1) {
        radius = radiusMin + arg1;
        mViewChangeListener.onBokehParameterChanged(mProgress,bokeh_x,bokeh_y,radius);
    }



    /**
     * To destroy the zoom view.
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
            switch (msg.what) {
                case SDOF_TEXT_VIEW_SHOW:
                    mTextView.setVisibility(View.VISIBLE);
                break;
                case SDOF_TEXT_VIEW_HIDE:
                    mTextView.setVisibility(View.INVISIBLE);
                break;
                case BOKEH_VIEW_INIT_AND_SHOW:
                    initView();
                    break;
                case BOKEH_VIEW_UNINIT:
                    unInitView();
                    break;
                default:
                    break;
            }
        }
    }

    private void initView() {
        mRootViewGroup = mApp.getAppUi().getModeRootView();
        mBokehofLayout = (RotateLayout) mApp.getActivity().getLayoutInflater()
                .inflate(R.layout.bokeh_view,
                mRootViewGroup, false).findViewById(R.id.bokeh_rotate_layout);
        mCameraAperture = (CameraAperture) mApp.getActivity().findViewById(R.id.camera_aperture);
        if (mCameraAperture != null) {
            mCameraAperture.setOnProgressChangedListener(this);
            mCameraAperture.setBokehValue(radiusMax, radius);
            int len = CameraAperture.dip2px(mApp.getActivity(), (float) 62.75);
            if (len > 0) {
                mCameraAperture.setApertureViewWidth(len);
            }
            mApp.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mCameraAperture.requestLayout();
                }
            });
        }
        mControl = (RelativeLayout)mBokehofLayout.findViewById(R.id.bokeh_bottom_controls);
        mControl.setVisibility(View.VISIBLE);
        mBokehSeekbarLayout = (SeekBar)mBokehofLayout.findViewById(R.id.bokeh_seekbar);
        mTextView = (TextView) mBokehofLayout.findViewById(R.id.bokeh_text_view);
        mBokehSeekbarLayout.setMax(seekbarmax);
        mProgress = Integer.parseInt(mDataStore.getValue("bokeh_progress","-1",mDataStore.getGlobalScope()));
        if(mProgress==-1){
            mProgress = mDefaultBokehValue;
        }
        mBokehSeekbarLayout.setProgress(mProgress);
        mBokehSeekbarLayout.setOnSeekBarChangeListener(mChangeListener);
        mRootViewGroup.addView(mBokehofLayout);
        mViewChangeListener.onBokehParameterChanged(mProgress,bokeh_x,bokeh_y,radius);
    }

    private void unInitView() {
        reset();
        mRootViewGroup.removeView(mBokehofLayout);

        //add by huangfei for disable CameraAperture when unInitView start
        mCameraAperture.setVisibility(View.INVISIBLE);
        //add by huangfei for disable CameraAperture when unInitView end
        
        mBokehofLayout = null;
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
            //delete by huangfei for disconnect camear start
            /*if(b){
                mViewChangeListener.onBokehLevelChanged(1,progress);
            }*/
            //delete by huangfei for disconnect camear end
            mTextView.setText(progress+"");
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            mMainHandler.sendEmptyMessage(SDOF_TEXT_VIEW_SHOW);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            //add by huangfei for disconnect camear start
            mProgress= mBokehSeekbarLayout.getProgress();
            mViewChangeListener.onBokehParameterChanged(mProgress,bokeh_x,bokeh_y,radius);
            mMainHandler.sendEmptyMessage(SDOF_TEXT_VIEW_HIDE);
            //add by huangfei for disconnect camear end
        }
    };
    public void setBokehProgress(int progress ){
        if(progress==-1){
            mBokehSeekbarLayout.setProgress(mDefaultBokehValue);
        }else {
            mBokehSeekbarLayout.setProgress(progress);
        }

    }
}
