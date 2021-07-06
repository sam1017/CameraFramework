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
package com.mediatek.camera.ui.preview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.transition.TransitionManager;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import com.mediatek.camera.Config;
import com.mediatek.camera.R;

import com.mediatek.camera.common.IAppUiListener.ISurfaceStatusListener;
import com.mediatek.camera.common.app.IApp;

import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
//add by huangfei for notch screen layout adjust start
import com.mediatek.camera.common.utils.CameraUtil;
import com.mediatek.camera.CameraActivity;
//add by huangfei for notch screen layout adjust end
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
//add by huangfei for preview layout relayout start
import android.view.Gravity;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import com.mediatek.camera.common.widget.PreviewFrameLayout;
import android.widget.ImageView;
import android.view.animation.Animation;
//add by huangfei for preview layout relayout end

//*/ hct.huangfei, 20201028. add gridlines.
import com.mediatek.camera.feature.setting.gridlines.GridlinesMonitor;
//*/
//bv wuyonglin add for adjust all icon position 20200612 start
import android.view.WindowManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.content.Context;
//bv wuyonglin add for adjust all icon position 20200612 end
//bv wuyonglin add for change mode and change preview size add blur 20200730 start
import com.mediatek.camera.common.utils.UtilBlurBitmap;
//bv wuyonglin add for change mode and change preview size add blur 20200730 end
//bv wuyonglin add for first time open facebeauty will happend black 20200925 start
import android.os.Handler;
import android.content.SharedPreferences;
//bv wuyonglin add for first time open facebeauty will happend black 20200925 end

/**
 * Camera preview controller for TextureView.
 */

public class TextureViewController implements IController {
    private static final LogUtil.Tag TAG =
            new LogUtil.Tag(TextureViewController.class.getSimpleName());

    private IApp mApp;
    private int mPreviewWidth = 0;
    private int mPreviewHeight = 0;
    private double mPreviewAspectRatio = 0.0d;
    private View.OnLayoutChangeListener mOnLayoutChangeListener;
    private View.OnTouchListener mOnTouchListener;
    private SurfaceChangeCallback mSurfaceChangeCallback;
    private ViewGroup mPreviewRoot;
    private ViewGroup mPreviewContainer;
    private ViewGroup mLastPreviewContainer;
    private PreviewTextureView mTextureView;
    private BlockingQueue<View> mFrameLayoutQueue =
            new LinkedBlockingQueue<View>();

    private boolean isAnimationplaying = false;
    private long mShowAnimationStartTime;
    private boolean mIsSurfaceCreated = false;
    //add by huangfei for notch screen layout adjust start
    private CameraActivity mCameraActivity;
    //add by huangfei for notch screen layout adjust end

    //add by huangfei for mode switch animation start
    private PreviewFrameLayout mPreviewFrameLayout;
    private ImageView mAnimationCover;
    /* add by bv liangchangwei 20200918 for animation change previewsuface size ++*/
    private ViewGroup mAnimationCoverMasktop;
    private ViewGroup mAnimationCoverMaskbottom;
    /* add by bv liangchangwei 20200918 for animation change previewsuface size --*/
    private ViewGroup mAnimationCoverRoot;
    private Bitmap mCoverBitmap;
    private double mOldRatio = 0.0d;
    //add by huangfei for mode switch animation end

    //*/ hct.huangfei, 20201028. add gridlines
    PreviewTextureViewCover mCover;
    private GridlinesMonitor mMonitor;
    private boolean mGridlineValue = false;
    //*/
    private int navigationBarAdjust;
    //add by huangfei for gridlines end	
    //bv wuyonglin add for adjust all icon position 20200612 start
    private int mScreenHeight = 0;
    private int mScreenWidth = 0;
    private float mAnimationscaleY = 1.0f;
    private int mQuickSwitcherHeight = 0;
    private int mQuickSwitcherModifyHeight = 124;
    //bv wuyonglin add for adjust all icon position 20200612 end
    //bv wuyonglin add for adjust all icon position 20200709 start
    private int m1to1TopMargin = 0;
    private int previewFramebottomAdjust;
    private int previewFramebottomAdjustFull;
    //bv wuyonglin add for adjust all icon position 20200709 end
    //bv wuyonglin add for first time open facebeauty will happend black 20200925 start
    private Handler mHandler = new Handler();
    private SharedPreferences mFirstRunSharedPreferences;
    //bv wuyonglin add for first time open facebeauty will happend black 20200925 end

	//modify by huangfei for preview layout relayout start
    //public TextureViewController(IApp app) {
	public TextureViewController(IApp app,PreviewFrameLayout previewFrameLayout) {
		mPreviewFrameLayout = previewFrameLayout;
	//modify by huangfei for preview layout relayout end
        mApp = app;
        //add by huangfei for notch screen layout adjust start
        mCameraActivity = (CameraActivity) mApp.getActivity() ;
        //add by huangfei for notch screen layout adjust end
        mPreviewRoot = (ViewGroup) mApp.getActivity().findViewById(R.id.preview_frame_root);

        //*/ hct.huangfei, 20201028. add gridlines
        if(Config.isGridlinesSupport(mCameraActivity)){
            mCover = (PreviewTextureViewCover) mCameraActivity.findViewById(R.id.preview_grid_cover);
            mMonitor = app.getAppUi().getMonitor();
            mMonitor.setGridlinesChangeListener(mChangeListener);
        }
        navigationBarAdjust = mApp.getActivity().getResources().getDimensionPixelOffset(R.dimen.navigationbar_hide_bottom_margin);	//bv wuyonglin modify for adjust 16:9 preview position 20200416
        //*/
        //bv wuyonglin add for adjust all icon position 20200612 start
        WindowManager wm = (WindowManager) mApp.getActivity().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        display.getRealMetrics(dm);
        mScreenHeight = dm.heightPixels;
        mScreenWidth = dm.widthPixels;
        //bv wuyonglin add for screen 1440px adjust all icon position 20200709 start
        previewFramebottomAdjustFull = mApp.getActivity().getResources().getDimensionPixelOffset(R.dimen.camera_preview_margin_bottom_for_full);
        previewFramebottomAdjust = mApp.getActivity().getResources().getDimensionPixelOffset(R.dimen.preview_frame_bottom_margin_adjust);
        if (mScreenHeight == 1440) {
            mQuickSwitcherHeight = mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.quick_switcher_bottom_position_1440px);
            m1to1TopMargin = mApp.getActivity().getResources().getDimensionPixelOffset(R.dimen.top_margin_1_1_1440px);
        } else if (mScreenHeight == 2300) {
            mQuickSwitcherHeight = mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.quick_switcher_bottom_position_2300px);
            m1to1TopMargin = mApp.getActivity().getResources().getDimensionPixelOffset(R.dimen.top_margin_1_1_2300px);
        } else if (mScreenHeight == 2400) {
            mQuickSwitcherHeight = mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.quick_switcher_bottom_position_2400px);
            m1to1TopMargin = mApp.getActivity().getResources().getDimensionPixelOffset(R.dimen.top_margin_1_1_2400px);
	    previewFramebottomAdjustFull = 0;
        } else {
            mQuickSwitcherHeight = mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.quick_switcher_bottom_position);
            m1to1TopMargin = mApp.getActivity().getResources().getDimensionPixelOffset(R.dimen.top_margin_1_1);
        }
        //bv wuyonglin add for screen 1440px adjust all icon position 20200709 end
        //bv wuyonglin add for adjust all icon position 20200612 end
        //bv wuyonglin add for first time open facebeauty will happend black 20200925 start
        mFirstRunSharedPreferences = mApp.getActivity().getSharedPreferences("camera_first_open", Context.MODE_PRIVATE);
        //bv wuyonglin add for first time open facebeauty will happend black 20200925 end
    }

    @Override
    public void updatePreviewSize(int width, int height, ISurfaceStatusListener listener) {
        //add by huangfei for mode switch animation start
        double ratio = (double) Math.max(width, height)/ Math.min(width, height);
        if(ratio != mOldRatio){ 
            //initAnimation();		//bv wuyonglin delete for change mode and change preview size add blur 20200730
            mOldRatio = ratio;
        }
        //add by huangfei for mode switch animation end
        //bv wuyonglin add for change mode and change preview size add blur 20200730 start
        initAnimation();
        //bv wuyonglin add for change mode and change preview size add blur 20200730 end

        LogHelper.i(TAG, "updatePreviewSize: new size (" + width + " , " + height + " )"
                + " current size (" + mPreviewWidth + " , " + mPreviewHeight + " )" + "," +
                "mIsSurfaceCreated = " + mIsSurfaceCreated +
                " listener = " + listener);
        if (mPreviewWidth == width && mPreviewHeight == height && !"AiworksFaceBeauty".equals(mApp.getAppUi().getCurrentMode())
		&& !"AiWorksBokeh".equals(mApp.getAppUi().getCurrentMode()) && !"AiWorksBokehColor".equals(mApp.getAppUi().getCurrentMode()) &&!"Video".equals(mApp.getAppUi().getCurrentMode())) {
            //If preview size is same, just call back surface available.
            ISurfaceStatusListener l = mSurfaceChangeCallback.getBindStatusListener();
            if (listener != null && listener != l) {
                mTextureView.setSurfaceTextureListener(null);
                mSurfaceChangeCallback = new SurfaceChangeCallback(listener);
                mTextureView.setSurfaceTextureListener(mSurfaceChangeCallback);
            }
            if (mIsSurfaceCreated) {
                if (listener != null && mTextureView.isAvailable()) {
                    mTextureView.getSurfaceTexture()
                            .setDefaultBufferSize(mPreviewWidth, mPreviewHeight);
                    listener.surfaceAvailable(mTextureView.getSurfaceTexture(),
                            mPreviewWidth, mPreviewHeight);
                }
            }
            return;
        }



        if (ratio == mPreviewAspectRatio && !"AiworksFaceBeauty".equals(mApp.getAppUi().getCurrentMode())
		&& !"AiWorksBokeh".equals(mApp.getAppUi().getCurrentMode()) && !"AiWorksBokehColor".equals(mApp.getAppUi().getCurrentMode()) &&!"Video".equals(mApp.getAppUi().getCurrentMode())) {
            mPreviewWidth = width;
            mPreviewHeight = height;
            if (mTextureView.isAvailable()) {
                mTextureView.getSurfaceTexture().setDefaultBufferSize(mPreviewWidth,
                        mPreviewHeight);
            }

            if (listener != null) {
                listener.surfaceAvailable(mTextureView.getSurfaceTexture(),
                        mPreviewWidth, mPreviewHeight);
                ISurfaceStatusListener l = mSurfaceChangeCallback.getBindStatusListener();
                if (listener != l) {
                    mTextureView.setSurfaceTextureListener(null);
                    mSurfaceChangeCallback = new SurfaceChangeCallback(listener);
                    mTextureView.setSurfaceTextureListener(mSurfaceChangeCallback);
                }
            }
            return;
        }

        if (mPreviewAspectRatio != 0) {
            mLastPreviewContainer = mPreviewContainer;
            mTextureView = null;
        }
        mPreviewWidth = width;
        mPreviewHeight = height;
        mPreviewAspectRatio = (double) Math.max(width, height)
                / Math.min(width, height);

        /* add by bv liangchangwei 20200918 for animation change previewsuface size ++*/
        if(mAnimationCoverRoot != null && mAnimationCoverRoot.getVisibility() == View.VISIBLE && !CameraUtil.is_videoHdr_Force){
            mCameraActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    startAnimation(mPreviewAspectRatio);
                }
            });
        }
        /* add by bv liangchangwei 20200918 for animation change previewsuface size --*/
        if (mTextureView == null) {
            attachTextureView(listener);
        } else {
            ISurfaceStatusListener l = mSurfaceChangeCallback.getBindStatusListener();
            if (listener != null && listener != l) {
                mTextureView.setSurfaceTextureListener(null);
                mSurfaceChangeCallback = new SurfaceChangeCallback(listener);
                mTextureView.setSurfaceTextureListener(mSurfaceChangeCallback);
                listener.surfaceAvailable(mTextureView.getSurfaceTexture(),
                        mPreviewWidth, mPreviewHeight);
            }
        }
        mTextureView.setAspectRatio(mPreviewAspectRatio);
		//add by huangfei for preview layout relayout start
		relayoutPreview(mPreviewAspectRatio);
        //add by huangfei for preview layout relayout end
    }
	//add by huangfei for preview layout relayout start
	private void relayoutPreview(double previewAspectRatio) {
        LogHelper.i(TAG,"relayoutPreview:");
        String aspect = String.valueOf(previewAspectRatio);
        boolean longScreen = CameraUtil.isLongScreen(mApp.getActivity());
        LogHelper.i(TAG,"relayoutPreview: aspect = " + aspect +" longScreen = " + longScreen);
        FrameLayout.LayoutParams previewRootFrameParams = (FrameLayout.LayoutParams) mPreviewRoot.getLayoutParams();
        FrameLayout.LayoutParams previewFrameParams = (FrameLayout.LayoutParams) mPreviewFrameLayout.getLayoutParams();
        if (aspect.startsWith("1.7")) {
            /*if (CameraUtil.isHasNavigationBar(mApp.getActivity())) {
                previewRootFrameParams.bottomMargin =  navigationBarAdjust;
            } else {
                previewRootFrameParams.bottomMargin = 0;
            }
            previewRootFrameParams.gravity = Gravity.CENTER;
            mPreviewRoot.setLayoutParams(previewRootFrameParams);

            if (CameraUtil.isHasNavigationBar(mApp.getActivity())) {
                previewFrameParams.bottomMargin = navigationBarAdjust;
            } else {
                previewFrameParams.bottomMargin = 0;
            }
            previewFrameParams.gravity =  Gravity.CENTER;
            mPreviewFrameLayout.setLayoutParams(previewFrameParams);*/
            int bottomMargin;
            LogHelper.i(TAG,"relayoutPreview: mQuickSwitcherHeight = " + mQuickSwitcherHeight +" previewFramebottomAdjust = " + previewFramebottomAdjust);
            if (mScreenHeight == 1440) {
            bottomMargin = mScreenHeight - mPreviewWidth - mQuickSwitcherHeight + previewFramebottomAdjust;
            } else {
            bottomMargin = mScreenHeight - mPreviewWidth - mQuickSwitcherHeight;
            }
            previewRootFrameParams.bottomMargin = bottomMargin;
            previewFrameParams.bottomMargin = bottomMargin;
            //bv wuyonglin add for adjust preview Frame position 20200312 end
            previewRootFrameParams.gravity = longScreen ? Gravity.CENTER | Gravity.BOTTOM : Gravity.CENTER;
            mPreviewRoot.setLayoutParams(previewRootFrameParams);

            //bv wuyonglin delete for adjust preview Frame position 20200312 start
            /*if (CameraUtil.isHasNavigationBar(mApp.getActivity())) {
                previewFrameParams.bottomMargin = longScreen ? CameraUtil.getNavigationBarHeight(mApp.getActivity()) : navigationBarAdjust;
            } else {
                previewFrameParams.bottomMargin = 0;
            }*/
            //bv wuyonglin delete for adjust preview Frame position 20200312 end
            previewFrameParams.gravity = longScreen ? Gravity.CENTER | Gravity.BOTTOM : Gravity.CENTER;
            mPreviewFrameLayout.setLayoutParams(previewFrameParams);
        //bv wuyonglin add for adjust preview Frame position 20200312 start
        } else if (aspect.startsWith("2.0")) {
            previewRootFrameParams.bottomMargin = previewFramebottomAdjustFull;
            //previewRootFrameParams.bottomMargin = mScreenHeight - mPreviewWidth - CameraUtil.getRealStatusBarHeight(mCameraActivity);
            previewRootFrameParams.gravity = longScreen ? Gravity.CENTER | Gravity.BOTTOM : Gravity.CENTER;
            mPreviewRoot.setLayoutParams(previewRootFrameParams);

            previewFrameParams.bottomMargin = previewFramebottomAdjustFull;
            //previewFrameParams.bottomMargin = mScreenHeight - mPreviewWidth - CameraUtil.getRealStatusBarHeight(mCameraActivity);
            previewFrameParams.gravity = longScreen ? Gravity.CENTER | Gravity.BOTTOM : Gravity.CENTER;
            LogHelper.i(TAG,"relayoutPreview: aspect = " + aspect +" previewRootFrameParams.bottomMargin = " + previewRootFrameParams.bottomMargin);
            mPreviewFrameLayout.setLayoutParams(previewFrameParams);
        //bv wuyonglin add for adjust preview Frame position 20200312 end
        //add by huangfei for  picture size ratio 1:1 start
        }else if(aspect.startsWith("1.0")){
            //int bottomMargin = mApp.getActivity().getResources().getDimensionPixelOffset(R.dimen.bottom_margin_1_1);
            int bottomMargin = mScreenHeight - mScreenWidth - m1to1TopMargin;
            previewRootFrameParams.bottomMargin = bottomMargin;
            previewRootFrameParams.gravity = Gravity.CENTER | Gravity.BOTTOM;
            mPreviewRoot.setLayoutParams(previewRootFrameParams);
            previewFrameParams.bottomMargin = bottomMargin;
            previewFrameParams.gravity = Gravity.CENTER | Gravity.BOTTOM;
            LogHelper.i(TAG,"relayoutPreview: aspect = " + aspect +" previewRootFrameParams.bottomMargin = " + previewRootFrameParams.bottomMargin+" m1to1TopMargin ="+m1to1TopMargin+" mPreviewWidth ="+mPreviewWidth);
            mPreviewFrameLayout.setLayoutParams(previewFrameParams);
        } else {
            //int bottomMargin = mApp.getActivity().getResources().getDimensionPixelOffset(R.dimen.camera_shutter_bar_height);
            int bottomMargin = mScreenHeight - (int)(mScreenWidth * mPreviewAspectRatio) - mQuickSwitcherHeight;
            previewRootFrameParams.bottomMargin = bottomMargin;
            previewRootFrameParams.gravity = Gravity.CENTER | Gravity.BOTTOM;
            mPreviewRoot.setLayoutParams(previewRootFrameParams);

            previewFrameParams.bottomMargin = bottomMargin;
            previewFrameParams.gravity = Gravity.CENTER | Gravity.BOTTOM;
            LogHelper.i(TAG,"relayoutPreview: aspect = " + aspect +" previewRootFrameParams.bottomMargin = " + previewRootFrameParams.bottomMargin+" mPreviewWidth ="+mPreviewWidth+" mQuickSwitcherHeight ="+mQuickSwitcherHeight);
            mPreviewFrameLayout.setLayoutParams(previewFrameParams);
        }
    }
	//add by huangfei for preview layout relayout end

    //*/ hct.huangfei, 20201028. add gridlines
    private void relayoutPreviewCover(double previewAspectRatio) {
        String aspect = String.valueOf(previewAspectRatio);
        boolean longScreen = Config.isLongScreenSupport(mApp.getActivity());
        if (mCover != null ) {
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mCover.getLayoutParams();
            if (aspect.startsWith("1.7")) {
                /*if (CameraUtil.isHasNavigationBar(mApp.getActivity())){
                    lp.bottomMargin =  navigationBarAdjust;
                }
                lp.gravity = Gravity.CENTER;*/
                if (mScreenHeight == 1440) {
                    lp.bottomMargin = mScreenHeight - mPreviewWidth - mQuickSwitcherHeight + previewFramebottomAdjust;
                } else {
                    lp.bottomMargin = mScreenHeight - mPreviewWidth - mQuickSwitcherHeight;
                }
                lp.gravity = longScreen ? Gravity.CENTER | Gravity.BOTTOM : Gravity.CENTER;
                //bv wuyonglin add for adjust preview Frame position 20200312 start
            }else if(aspect.startsWith("2.0")) {
                lp.bottomMargin = previewFramebottomAdjustFull;
                //lp.bottomMargin = mScreenHeight - mPreviewWidth - CameraUtil.getRealStatusBarHeight(mCameraActivity);
                lp.gravity = longScreen ? Gravity.CENTER | Gravity.BOTTOM : Gravity.CENTER;
		    //bv wuyonglin add for adjust preview Frame position 20200312 end
            }else if(aspect.startsWith("1.0")){
                //int bottomMargin = mApp.getActivity().getResources().getDimensionPixelOffset(R.dimen.bottom_margin_1_1);
                int bottomMargin = mScreenHeight - mScreenWidth - m1to1TopMargin;
                lp.bottomMargin = bottomMargin;
                lp.gravity = Gravity.CENTER | Gravity.BOTTOM;
            } else {
                //int bottomMargin = mApp.getActivity().getResources().getDimensionPixelOffset(R.dimen.camera_shutter_bar_height);
                int bottomMargin = mScreenHeight - (int)(mScreenWidth * mPreviewAspectRatio) - mQuickSwitcherHeight;
                lp.bottomMargin = bottomMargin;
                lp.gravity = Gravity.CENTER | Gravity.BOTTOM;
            }
            mCover.setLayoutParams(lp);
        }

    }

    private GridlinesMonitor.GridlinesChangeListener mChangeListener
            = new GridlinesMonitor.GridlinesChangeListener() {
        @Override
        public void onVauleChange(boolean isOn) {
            mGridlineValue = isOn;
            if(isShowGridlineMode()){
                showGridlines(isOn);
            }
        }

    };
    public void showGridlines(boolean show){
        if(mCover==null){
            return;
        }
        mCameraActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(show){
                    mCover.setVisibility(View.VISIBLE);
                }else{
                    mCover.setVisibility(View.GONE);
                }
            }
        });
    }
    public boolean isShowGridlineMode(){
        String mMode = mApp.getAppUi().getCurrentMode();
        if("HctBokeh".equals(mMode)||"Pro".equals(mMode)/*||"Video".equals(mMode)*/){
            return false;
        }else{
            return true;
        }
    }
    //*/
    @Override
    public View getView() {
        return mTextureView;
    }

    @Override
    public void removeTopSurface() {

        //*/ hct.huangfei, 20201028. add gridlines.
        /*if(isShowGridlineMode()&&mGridlineValue){
            showGridlines(true);
        }else{
            showGridlines(false);
        }*/
        //*/
        int queueSize = mFrameLayoutQueue.size();
        LogHelper.d(TAG, "removeTopSurface size = " + queueSize);
        for (int i = 0; i < queueSize; i++) {
            View view = mFrameLayoutQueue.poll();
            if (view != null) {
                view.setVisibility(View.GONE);
                mPreviewRoot.removeView(view);
            }
        }
        //add by huangfei for mode switch animation start
        //bv wuyonglin add for first time open facebeauty will happend black 20200925 start
        if ("AiworksFaceBeauty".equals(mApp.getAppUi().getCurrentMode()) && mFirstRunSharedPreferences.getBoolean("camera_first_open_aiworksfacebeauty", true)) {
            mFirstRunSharedPreferences.edit().putBoolean("camera_first_open_aiworksfacebeauty", false).commit();
            LogHelper.i(TAG, "removeTopSurface to postDelayed");
            mHandler.postDelayed(new AnimationCoverEndAction(), 400);
        /* add by liangchangwei for HDR Video start--*/
        } else if("Video".equals(mApp.getAppUi().getCurrentMode())&&CameraUtil.isVideo_HDR_on) {
            LogHelper.i(TAG, "removeTopSurface to Video && HDR is ON");
            mHandler.postDelayed(new AnimationCoverEndAction(), 500);
        } else{
        /* add by liangchangwei for HDR Video end--*/
            //showAnimation(false);
            LogHelper.i(TAG, "removeTopSurface to isAnimationplaying = " + isAnimationplaying);
            if(!isAnimationplaying){
                showAnimation(false);
            }else{
                mHandler.postDelayed(new AnimationCoverEndAction(), 400);
                LogHelper.i(TAG, "removeTopSurface to postDelayed 400");
            }
        }
        //bv wuyonglin add for first time open facebeauty will happend black 20200925 end
        //add by huangfei for mode switch animation end
        mLastPreviewContainer = null;

        //*/ hct.huangfei, 20201028. add gridlines.
        if(mCover!=null&&isShowGridlineMode()){
            mCover.setAspectRatio(mPreviewAspectRatio);
            relayoutPreviewCover(mPreviewAspectRatio);
        }
        //*/
    }

    @Override
    public void setOnLayoutChangeListener(View.OnLayoutChangeListener layoutChangeListener) {
        mOnLayoutChangeListener = layoutChangeListener;
    }

    @Override
    public void setOnTouchListener(View.OnTouchListener onTouchListener) {
        mOnTouchListener = onTouchListener;
    }

    @Override
    public void clearPreviewStatusListener(ISurfaceStatusListener listener) {

    }

    @Override
    public void setEnabled(boolean enabled) {
        if (mTextureView != null) {
            mTextureView.setEnabled(enabled);
        }
    }

    @Override
    public void onPause() {

    }

    @Override
    public Bitmap getPreviewBitmap(int downSample) {
        if (mTextureView == null) {
            return null;
        }
        RectF textureArea = getTextureArea();
        int width = (int) textureArea.width() / downSample;
        int height = (int) textureArea.height() / downSample;
        Bitmap preview = mTextureView.getBitmap(width, height);
        if(preview != null){
            Bitmap n = Bitmap.createBitmap(
                    preview, 0, 0, width, height, mTextureView.getTransform(null), true);
            return n;
        }else{
            return null;
        }
    }

    private void attachTextureView(ISurfaceStatusListener listener) {
        ViewGroup container = (ViewGroup) mApp.getActivity().getLayoutInflater().inflate(
                R.layout.textureview_layout, null);
        PreviewTextureView textureView =
                (PreviewTextureView) container.findViewById(R.id.preview_surface);

        if (mLastPreviewContainer != null) {
            TextureView texture =
                    (TextureView) mLastPreviewContainer.findViewById(R.id.preview_surface);
            texture.removeOnLayoutChangeListener(mOnLayoutChangeListener);
            texture.setSurfaceTextureListener(null);
            textureView.setOnTouchListener(null);
            if (mSurfaceChangeCallback != null) {
                mSurfaceChangeCallback.onSurfaceTextureDestroyed(texture.getSurfaceTexture());
            }
            mLastPreviewContainer.bringToFront();
            if (!mFrameLayoutQueue.contains(mLastPreviewContainer)) {
                mFrameLayoutQueue.add(mLastPreviewContainer);
                mLastPreviewContainer.setVisibility(View.GONE);//HCT:ouyang request layout preview
            }
        }

        mSurfaceChangeCallback = new SurfaceChangeCallback(listener);
        textureView.setSurfaceTextureListener(mSurfaceChangeCallback);
        textureView.addOnLayoutChangeListener(mOnLayoutChangeListener);
        textureView.setOnTouchListener(mOnTouchListener);
        mPreviewRoot.addView(container, 0);
        mTextureView = textureView;
        mPreviewContainer = container;
    }

    private RectF getTextureArea() {
        Matrix matrix = new Matrix();
        RectF area = new RectF(0, 0, mTextureView.getWidth(), mTextureView.getHeight());
        mTextureView.getTransform(matrix).mapRect(area);
        return area;
    }

    /**
     * Surface change call back receiver, it bind a status change listener.
     * When surface status change, use the listener to notify the change.
     */
    private class SurfaceChangeCallback implements TextureView.SurfaceTextureListener {
        private ISurfaceStatusListener mListener;

         SurfaceChangeCallback(ISurfaceStatusListener listener) {
            mListener = listener;
        }

         ISurfaceStatusListener getBindStatusListener() {
            return mListener;
        }

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            mIsSurfaceCreated = true;
            surface.setDefaultBufferSize(mPreviewWidth, mPreviewHeight);
            if (mListener != null) {
                mListener.surfaceChanged(surface, mPreviewWidth, mPreviewHeight);
            }
            LogHelper.i(TAG, "onSurfaceTextureAvailable surface  = " + surface +
             " width " + width + " height " + height+" mPreviewWidth ="+mPreviewWidth+" mPreviewHeight ="+mPreviewHeight);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            if (mListener != null &&"AiworksFaceBeauty".equals(mApp.getAppUi().getCurrentMode())) {    //bv wuyonglin mofidy for bug3529 20210126
                mListener.surfaceChanged(surface, width, height);
            }
            LogHelper.i(TAG, "onSurfaceTextureSizeChanged surface  = " + surface +
                    " width " + width + " height " + height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            mIsSurfaceCreated = false;
            if (mListener != null) {
                mListener.surfaceDestroyed(surface,  mPreviewWidth, mPreviewHeight);
            }
            LogHelper.i(TAG, "onSurfaceTextureDestroyed surface  = " + surface);
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    }
	//add by huangfei for previewmanager hide start
	public void hide(){
		mPreviewRoot.setVisibility(View.INVISIBLE);
	}
	public void show(){
		mPreviewRoot.setVisibility(View.VISIBLE);
	}
    //add by huangfei for previewmanager hide end
    //add by huangfei for mode switch animation start
    private synchronized void initAnimation() {
        if ( null != mAnimationCoverRoot && mAnimationCover.isShown()) {
            return;
        }
        if(mAnimationCoverRoot==null){
            mAnimationCoverRoot = (ViewGroup) mApp.getActivity().findViewById(R.id.animation_coverview_root);
        }
        if (mAnimationCover == null){
            mAnimationCover = (ImageView) mApp.getActivity().findViewById(R.id.animation_coverview);
        }
        //bv wuyonglin add for change mode and change preview size add blur 20200730 start
        //mCoverBitmap = getPreviewBitmap(1);
        mCoverBitmap = UtilBlurBitmap.blurBitmap(mApp.getActivity(), getPreviewBitmap(1), 25.0f);
        //bv wuyonglin add for change mode and change preview size add blur 20200730 end
        if (mCoverBitmap == null) {
            LogHelper.e(TAG, "[showPreviewCover]","animation cover is null");
            return;
        }
        if (mAnimationCover != null ) {
            mAnimationCover.setScaleType(ImageView.ScaleType.FIT_CENTER);
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mAnimationCover.getLayoutParams();
            lp.width = mCoverBitmap.getWidth();
            lp.height = mCoverBitmap.getHeight();
            lp.gravity = Gravity.CENTER;
            boolean longScreen = CameraUtil.isLongScreen(mApp.getActivity());
            double mPreviewAspectRatio = (double) Math.max(mCoverBitmap.getWidth(), mCoverBitmap.getHeight())
                / Math.min(mCoverBitmap.getWidth(), mCoverBitmap.getHeight());
            String aspect = String.valueOf(mPreviewAspectRatio);
            LogHelper.i(TAG,"initAnimation aspect = " + aspect + " longScreen = " + longScreen);
            //bv wuyonglin add for adjust preview Frame position 20200312 start
            if (aspect.startsWith("1.7")) {
                /*if (!CameraUtil.isEdgeToEdgeEnabled(mApp.getActivity())) {
                    //bv wuyonglin add for adjust preview Frame position 20200312 end
                        lp.bottomMargin = !longScreen ? CameraUtil.getNavigationBarHeight(mApp.getActivity()) : navigationBarAdjust;
                    //bv wuyonglin add for adjust preview Frame position 20200312 start
                    } else {
                        lp.bottomMargin = mApp.getActivity().getResources().getDimensionPixelOffset(R.dimen.navigationbar_hide_bottom_margin);
                }*/
                if (mScreenHeight == 1440) {
                    lp.bottomMargin = mScreenHeight - mPreviewWidth - mQuickSwitcherHeight + previewFramebottomAdjust;
                } else {
                    lp.bottomMargin = mScreenHeight - mPreviewWidth - mQuickSwitcherHeight;
                }
                //bv wuyonglin add for adjust preview Frame position 20200312 end
                lp.gravity = longScreen ? Gravity.CENTER | Gravity.BOTTOM : Gravity.CENTER;
                //bv wuyonglin add for adjust preview Frame position 20200312 start
            }else if(aspect.startsWith("2.0")) {
                lp.bottomMargin = previewFramebottomAdjustFull;
                //lp.bottomMargin = mScreenHeight - mPreviewWidth - CameraUtil.getRealStatusBarHeight(mCameraActivity);
                lp.gravity = longScreen ? Gravity.CENTER | Gravity.BOTTOM : Gravity.CENTER;
                //bv wuyonglin add for adjust preview Frame position 20200312 end
            }else if(aspect.startsWith("1.0")){
                //int bottomMargin = mApp.getActivity().getResources().getDimensionPixelOffset(R.dimen.bottom_margin_1_1);
                int bottomMargin = mScreenHeight - mScreenWidth - m1to1TopMargin;
                lp.bottomMargin = bottomMargin;
                lp.gravity = Gravity.CENTER | Gravity.BOTTOM;   
            } else {
                //int bottomMargin = mApp.getActivity().getResources().getDimensionPixelOffset(R.dimen.camera_shutter_bar_height);
                int bottomMargin = mScreenHeight - (int)(mScreenWidth * mPreviewAspectRatio) - mQuickSwitcherHeight;
                lp.bottomMargin = bottomMargin;
                lp.gravity = Gravity.CENTER | Gravity.BOTTOM;
            }
            LogHelper.i(TAG,"initAnimation lp.bottomMargin = " + lp.bottomMargin + " lp.height = " + lp.height + " lp.topMargin = " + lp.topMargin);
            mAnimationCover.setLayoutParams(lp);
        }
        if (mCoverBitmap != null && !mCoverBitmap.isRecycled()){
            mAnimationCover.setImageBitmap(mCoverBitmap);
            showAnimation(true);
	    }     
        LogHelper.i(TAG, "[showAnimation]---");
    }

    /* add by bv liangchangwei 20200918 for animation change previewsuface size ++*/
    private void startAnimation(double previewAspectRatio){
        if (mCoverBitmap == null) {
            LogHelper.i(TAG, "[startAnimation]  return mCoverBitmap ="+mCoverBitmap);
            return;
        }
        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        String aspect = String.valueOf(previewAspectRatio);

        isAnimationplaying = true;
        if (mAnimationCover != null ) {
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mAnimationCover.getLayoutParams();
            int bottomMargin = lp.bottomMargin;
            int height = lp.height;
            if(height > mScreenHeight){
                height = mScreenHeight;
            }
            startBounds.top = mScreenHeight - bottomMargin - height;
            startBounds.bottom = mScreenHeight - bottomMargin;
            startBounds.left = 0;
            startBounds.right = mScreenWidth;
            LogHelper.i(TAG,"startAnimation startBounds = " + startBounds);
        }
        int previewheight = Math.max(mPreviewWidth,mPreviewHeight);
        LogHelper.i(TAG,"startAnimation previewheight = " + previewheight);
        int finalbottomMargin = 0;
        if(aspect.startsWith("2.0")){
            finalbottomMargin = 0;

            finalBounds.top = mScreenHeight - mPreviewWidth;
            finalBounds.bottom = mScreenHeight - previewFramebottomAdjustFull;
            finalBounds.left = 0;
            finalBounds.right = mScreenWidth;
            LogHelper.i(TAG,"startAnimation previewAspectRatio = " + previewAspectRatio + " finalBounds = " + finalBounds+" mScreenHeight ="+mScreenHeight+" mPreviewWidth ="+mPreviewWidth);

        }else if (aspect.startsWith("1.7")) {
            if (mScreenHeight == 1440) {
                finalbottomMargin = mScreenHeight - mPreviewWidth - mQuickSwitcherHeight + previewFramebottomAdjust;
            } else {
                finalbottomMargin = mScreenHeight - mPreviewWidth - mQuickSwitcherHeight;
            }

            finalBounds.top = mScreenHeight - finalbottomMargin - previewheight;
            finalBounds.bottom = mScreenHeight - finalbottomMargin;
            finalBounds.left = 0;
            finalBounds.right = mScreenWidth;
            LogHelper.i(TAG,"startAnimation previewAspectRatio = " + previewAspectRatio + " finalBounds = " + finalBounds);

        }else if(aspect.startsWith("1.0")){
            //int bottomMargin = mApp.getActivity().getResources().getDimensionPixelOffset(R.dimen.bottom_margin_1_1);
            finalbottomMargin = mScreenHeight - mScreenWidth - m1to1TopMargin;
        }else if(aspect.startsWith("1.3")){
            finalbottomMargin = mScreenHeight - (int)(mScreenWidth * mPreviewAspectRatio) - mQuickSwitcherHeight;
        }else{
            finalbottomMargin = 0;
        }
        if(aspect.startsWith("1.0")){
        finalBounds.top = mScreenHeight - finalbottomMargin - mScreenWidth;
        } else {
        if (mScreenHeight == 2400 && aspect.startsWith("2.0")) {
        finalBounds.top = mScreenHeight - finalbottomMargin - previewheight + mApp.getActivity().getResources().getDimensionPixelOffset(R.dimen.camera_preview_margin_bottom_for_full);
        } else {
        finalBounds.top = mScreenHeight - finalbottomMargin - previewheight;
        }
        }
        if(!aspect.startsWith("2.0")){
        finalBounds.bottom = mScreenHeight - finalbottomMargin;
        }
        finalBounds.left = 0;
        finalBounds.right = mScreenWidth;
        LogHelper.i(TAG,"startAnimation previewAspectRatio = " + previewAspectRatio + " finalBounds = " + finalBounds);

        int baseHeight = mCoverBitmap.getHeight()/2;
        int finalHeight = finalBounds.bottom - (startBounds.top + startBounds.height()/2);
        if((startBounds.top + startBounds.height()/2 - finalBounds.top) > finalHeight){
            finalHeight = (startBounds.top + startBounds.height()/2 - finalBounds.top);
        }
        float scaleY = 1.0f;
        if(baseHeight != 0){
            scaleY = (float)(Math.round((finalHeight*100)/baseHeight))/100;
            LogHelper.i(TAG,"(finalHeight*10)/baseHeight = " + (finalHeight*100)/baseHeight);
            LogHelper.i(TAG,"scaleY = " + scaleY);

        }
        LogHelper.i(TAG,"startAnimation baseHeight = " + baseHeight + " finalHeight = " + finalHeight + " scaleY = " + scaleY);

        if(scaleY > 1.0){
            mAnimationscaleY = scaleY;
            ObjectAnimator objectAnimatorscaleY = ObjectAnimator.ofFloat(mAnimationCover, "scaleY", 1.0f, mAnimationscaleY);
            ObjectAnimator objectAnimatorscaleX = ObjectAnimator.ofFloat(mAnimationCover, "scaleX", 1.0f, mAnimationscaleY);

            AnimatorSet set = new AnimatorSet();
            LogHelper.i(TAG,"startAnimation startBounds.top = " + startBounds.top + " finalBounds.top = " + finalBounds.top);
            LogHelper.i(TAG,"startAnimation startBounds.bottom = " + startBounds.bottom + " finalBounds.bottom = " + finalBounds.bottom);

            set.setDuration(0);
            set.setInterpolator(new DecelerateInterpolator());
            set.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    LogHelper.i(TAG,"onAnimationEnd");
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    LogHelper.i(TAG,"onAnimationCancel");
                }
            });
            set.play(objectAnimatorscaleY).with(objectAnimatorscaleX);
            set.start();
            LogHelper.i(TAG,"startAnimation set.start");
        }

        if(mAnimationCoverMasktop == null){
            mAnimationCoverMasktop = (ViewGroup) mApp.getActivity().findViewById(R.id.animation_coverview_masktop);
        }
        ViewGroup.LayoutParams topparams = mAnimationCoverMasktop.getLayoutParams();
        topparams.width = mScreenWidth;
        topparams.height = Math.max(finalBounds.top, startBounds.top);
        LogHelper.i(TAG,"mAnimationCoverMasktop width = " + topparams.width + " height = " + topparams.height);
        mAnimationCoverMasktop.setLayoutParams(topparams);

        float topstartY = (float)(startBounds.top - topparams.height);
        float topendY = (float)(finalBounds.top - topparams.height);
        LogHelper.i(TAG,"mAnimationCoverMasktop topstartY = " + topstartY + " topendY = " + topendY);
        ObjectAnimator top = ObjectAnimator.ofFloat(mAnimationCoverMasktop, "translationY", topstartY, topendY);
        AnimatorSet animatortopSet = new AnimatorSet();
        animatortopSet.setDuration(400);
        animatortopSet.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
                // TODO Auto-generated method stub
                mAnimationCoverMasktop.setVisibility(View.VISIBLE);
                LogHelper.i(TAG,"mAnimationCoverMasktop onAnimationStart");
                showGridlines(false);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                // TODO Auto-generated method stub
                mAnimationCoverMasktop.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                // TODO Auto-generated method stub

            }
        });
        animatortopSet.playTogether(top);
        animatortopSet.start();

        if(mAnimationCoverMaskbottom == null){
            mAnimationCoverMaskbottom = (ViewGroup) mApp.getActivity().findViewById(R.id.animation_coverview_maskbottom);
        }
        ViewGroup.LayoutParams bottomparams = mAnimationCoverMaskbottom.getLayoutParams();
        bottomparams.width = mScreenWidth;
        bottomparams.height = mScreenHeight - Math.min(finalBounds.bottom, startBounds.bottom);
        LogHelper.i(TAG,"mAnimationCoverMaskbottom width = " + bottomparams.width + " height = " + bottomparams.height);
        mAnimationCoverMaskbottom.setLayoutParams(bottomparams);

        float bottomstartY = (float)( startBounds.bottom );
        float bottomendY = (float)(finalBounds.bottom );
        LogHelper.i(TAG,"mAnimationCoverMaskbottom bottomstartY = " + bottomstartY + " bottomendY = " + bottomendY);
        ObjectAnimator bottom = ObjectAnimator.ofFloat(mAnimationCoverMaskbottom, "translationY", bottomstartY, bottomendY);
        AnimatorSet animatorbottomSet = new AnimatorSet();
        animatorbottomSet.setDuration(400);
        animatorbottomSet.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
                // TODO Auto-generated method stub
                mAnimationCoverMaskbottom.setVisibility(View.VISIBLE);
                LogHelper.i(TAG,"mAnimationCoverMaskbottom onAnimationStart");
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                // TODO Auto-generated method stub
                mAnimationCoverMaskbottom.setVisibility(View.VISIBLE);
                LogHelper.i(TAG,"mAnimationCoverMaskbottom onAnimationEnd");
                isAnimationplaying = false;
            }
            @Override
            public void onAnimationCancel(Animator animation) {
                // TODO Auto-generated method stub

            }
        });
        animatorbottomSet.playTogether(bottom);
        animatorbottomSet.start();
    }
    /* add by bv liangchangwei 20200918 for animation change previewsuface size --*/

    public void showAnimation(boolean show){
        mCameraActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(mAnimationCoverRoot==null){
                    return;
                }
                if(show){
                    mAnimationCoverRoot.setVisibility(View.VISIBLE);
                }else{
                    mAnimationCoverRoot.setVisibility(View.GONE);
                    if(mAnimationscaleY > 1.0f){
                        ObjectAnimator objectAnimatorscaleY = ObjectAnimator.ofFloat(mAnimationCover, "scaleY", mAnimationscaleY, 1.0f);
                        ObjectAnimator objectAnimatorscaleX = ObjectAnimator.ofFloat(mAnimationCover, "scaleX", mAnimationscaleY,1.0f);

                        AnimatorSet set = new AnimatorSet();

                        set.setDuration(0);
                        set.setInterpolator(new DecelerateInterpolator());
                        set.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                LogHelper.i(TAG,"resize onAnimationEnd");
                                mAnimationscaleY = 1.0f;
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {
                                LogHelper.i(TAG,"resize onAnimationCancel");
                            }
                        });
                        set.play(objectAnimatorscaleY).with(objectAnimatorscaleX);
                        set.start();
                        LogHelper.i(TAG,"resize  mAnimationCover set.start");
                    }

                    //add by huangfei for gridlines start
                    if(isShowGridlineMode()&&mGridlineValue){
                        showGridlines(true);
                        LogHelper.i(TAG,"removeTopSurface showGridlines true");
                    }else{
                        showGridlines(false);
                        LogHelper.i(TAG,"removeTopSurface showGridlines false");
                    }
                    //add by huangfei for gridlines end

                    if (mCoverBitmap != null && !mCoverBitmap.isRecycled()){
                        LogHelper.i(TAG, "[isRecycled]---");
                        mCoverBitmap.recycle();
                        mCoverBitmap = null;
                    }

                    if(CameraUtil.isVideo_HDR_changing){
                        CameraUtil.isVideo_HDR_changing = false;
                    }
                }
                    
            }
        });
    }
    //add by huangfei for mode switch animation end

    //bv wuyonglin add for first time open facebeauty will happend black 20200925 start
    private class AnimationCoverEndAction implements Runnable {
        @Override
        public void run() {
            showAnimation(false);
        }
    }
    //bv wuyonglin add for first time open facebeauty will happend black 20200925 end
}
