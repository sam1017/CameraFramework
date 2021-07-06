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
package com.mediatek.camera.ui.shutter;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.RelativeLayout;
import android.widget.Scroller;

import com.mediatek.camera.R;
import com.mediatek.camera.common.IAppUiListener;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.utils.CameraUtil;
import com.mediatek.camera.CameraActivity;
import java.util.ArrayList;
import java.util.List;

/**
 * Shutter button root layout, control the shutter ui layout and scroll animation.
 */
 public class ShutterRootLayout extends RelativeLayout implements ShutterTitleView.OnShutterTextClicked {
    /**
     * Shutter type change listener.
     */
    public interface OnShutterChangeListener {
        /**
         * When current valid shutter changed, invoke the listener to notify.
         * @param newShutterName The new valid shutter name.
         */
        void onShutterChangedStart(String newModeName,String newModeType,int index);

        /**
         * When shutter change animation finish, invoke the listener to notify.
         */
        void onShutterChangedEnd();
    }
    private static final LogUtil.Tag TAG = new LogUtil.Tag(
                                        ShutterRootLayout.class.getSimpleName());
    private static final int ANIM_DURATION_MS = 1000;
    private Scroller mScroller;

    private static final int MINI_SCROLL_LENGTH = 50;	//bv wuyonglin modify for all area fling to change mode

    private int mCurrentIndex = 0;
    private int mScrollDistance = 0;
    private boolean mDirection = false; //flase left true ringht

    private OnShutterChangeListener mListener;
    private Context mContext; //add by Jerry
    private int mChangIndex = 0;
    private Handler mHandler = new Handler();
    ViewTreeObserver vto = null;
    private boolean mIsFirstInit = true;
    private boolean mResumed = false;
    private CameraActivity mCameraActivity;
    private long mScrollTime =0;
    private boolean mEnable = false;
	//bv wuyonglin add for all area fling to change mode start
    private CHorizontalScrollView mCHorizontalScrollView;
    private int items;
	//bv wuyonglin add for all area fling to change mode end

    public void setOnShutterChangedListener(OnShutterChangeListener listener) {
        mListener = listener;
    }

    public IAppUiListener.OnGestureListener getGestureListener() {
        return new GestureListenerImpl();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }
    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
            if (mScroller.isFinished() && mListener != null) {
                mListener.onShutterChangedEnd();
            }
        }
        for(int i = 0; i < getChildCount();i++) {
            ShutterTitleView shutter = (ShutterTitleView) getChildAt(i);
            if(i == mCurrentIndex){
                shutter.setTextColor(R.color.shutter_title_select_color);
            } else {
                shutter.setTextColor(R.color.shutter_title_normal_color);
            }
        }
    }

    public ShutterRootLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mScroller = new Scroller(context, new DecelerateInterpolator());
        mContext = context;//add by Jerry
        mCameraActivity = (CameraActivity)context;
        vto = getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if(mIsFirstInit) {
                    mHandler.removeCallbacks(mNoModeDelayed);
                    mHandler.postDelayed(mNoModeDelayed, 200);//add by Jerry this must delay
                }
            }
        });
    }


    @Override
    public void onShutterTextClicked(int index) {
        LogHelper.d(TAG, "onShutterTextClicked index = " + index);
        if (mScroller.isFinished() && isEnabled() && mResumed) {
            snapTOShutter(index, ANIM_DURATION_MS);
        }
    }

    public void updateCurrentShutterIndex(int shutterIndex) {
        snapTOShutterNoMode(0, 0);
        mChangIndex = shutterIndex;
        mHandler.removeCallbacks(mDelayed);
        mHandler.postDelayed(mDelayed,100);//add by Jerry this must delay
    }

    public void onResume() {
        //bv wuyonglin add for all area fling to change mode start
        mCHorizontalScrollView = mCameraActivity.getShutterButtonManager().getCHorizontalScrollView();
        items = mCameraActivity.getShutterButtonManager().getModeItems().size();
        if (mCHorizontalScrollView != null) {
	        mCHorizontalScrollView.setOnFlingListener(mOnFlingListener);
	}
        //bv wuyonglin delete for all area fling to change mode end
        mResumed = true;
    }

    public void onPause() {
        mResumed = false;
    }

    Runnable mDelayed = new Runnable() {
    @Override
        public void run() {
            snapTOShutter(mChangIndex, ANIM_DURATION_MS);
        }
    };
    Runnable mNoModeDelayed = new Runnable() {
        @Override
        public void run() {
            mIsFirstInit = false;
            snapTOShutterNoMode(mCurrentIndex, ANIM_DURATION_MS);
    }
    };

    private void doShutterAnimation(int whichShutter, int animationDuration) {
        long now = System.currentTimeMillis();
        if (Math.abs(now - mScrollTime) <= 500) {
            return;
        }
        mScrollTime = now;
        int step = Math.abs(whichShutter - mCurrentIndex); //add by Jerry

        //modify by huangfei for shutter title change start
        if (whichShutter > mCameraActivity.getShutterButtonManager().getModeItems().size()-1) {
            return;
        }

        int dx = 0;
        if (whichShutter == 0) {
            dx = -getScrollX();
        } else {
            //delete by huangfei for shutter title change start
            /*if(mDirection) {
                if(step > 1) {
                    for (int i = 0; i < step; i++) {
                        dx += (getChildAt(mCurrentIndex - i).getMeasuredWidth() + getChildAt(mCurrentIndex - i - 1).getMeasuredWidth()) / 2
                                + mContext.getResources().getDimensionPixelOffset(R.dimen.camera_shutter_bar_title_gap);
                    }
                } else {
                    dx = (getChildAt(mCurrentIndex).getMeasuredWidth() + getChildAt(mCurrentIndex -1).getMeasuredWidth() + 1) / 2
                            + mContext.getResources().getDimensionPixelOffset(R.dimen.camera_shutter_bar_title_gap);
                }
            } else {
                if(step > 1) {
                    for (int i = 0; i < step; i++) {
                        dx += (getChildAt(mCurrentIndex + i).getMeasuredWidth() + getChildAt(mCurrentIndex + i + 1).getMeasuredWidth()) / 2
                                + mContext.getResources().getDimensionPixelOffset(R.dimen.camera_shutter_bar_title_gap);
                    }
                } else {
                    dx = (getChildAt(mCurrentIndex).getMeasuredWidth() + getChildAt(mCurrentIndex + 1).getMeasuredWidth() + 1) / 2
                            + mContext.getResources().getDimensionPixelOffset(R.dimen.camera_shutter_bar_title_gap);
                }
            }*/
            //delete by huangfei for shutter title change end
        }
        if(mDirection && whichShutter != 0) {
            dx = -dx;
        }
        /*mScroller.startScroll(getScrollX(), 0, dx, 0, animationDuration);
        mScrollDistance = Math.abs(dx);
        invalidate();*/
        mCurrentIndex = whichShutter;
    }

    private void snapTOShutter(int whichShutter, int animationDuration) {
        //if (whichShutter == mCurrentIndex) {
        if (whichShutter == mCurrentIndex && whichShutter != 0) {
            LogHelper.i(TAG,"whichShutter = " + whichShutter + ", and return");
            return;
        }
        //bv wuyonglin add for bug3751 20200202 start
        if(CameraUtil.isVideo_HDR_changing){
            LogHelper.i(TAG, "[snapTOShutter], don't do mode change for when isVideo_HDR_changing = true " );
            return;
        }
        //bv wuyonglin add for bug3751 20200202 end
        if(whichShutter > mCurrentIndex) {
            mDirection = false;
        } else {
            mDirection = true;
        }
        doShutterAnimation(whichShutter, animationDuration);
        //if (mListener != null) {
        if (mListener != null && whichShutter>=0) {
            /*ShutterTitleView shutter = (ShutterTitleView) getChildAt(mCurrentIndex);
            mListener.onShutterChangedStart(shutter.getModeName(),shutter.getType(),mCurrentIndex);*/
            mCurrentIndex = whichShutter;
            List<ShutterButtonManager.ModeHolder>  modeHolders = mCameraActivity.getShutterButtonManager().getModeItems();
            if(modeHolders.size()==1){
                mCurrentIndex = 0;
            }
            ShutterButtonManager.ModeHolder modeHolder = modeHolders.get(mCurrentIndex);
            mListener.onShutterChangedStart(modeHolder.getShutterModeName(),modeHolder.getShutterType(),mCurrentIndex);
        }
    }
    private void snapTOShutterNoMode(int whichShutter, int animationDuration) {
        if (whichShutter == mCurrentIndex) {
            return;
        }
        if(whichShutter > mCurrentIndex) {
            mDirection = false;
        } else {
            mDirection = true;
        }
        doShutterAnimation(whichShutter, animationDuration);
    }

    /**
     * Gesture listener implementer.
     */
    private class GestureListenerImpl implements IAppUiListener.OnGestureListener {

        private float mTransitionX;
        private float mTransitionY;
        private boolean mIsScale;

        @Override
        public boolean onDown(MotionEvent event) {
            mTransitionX = 0;
            mTransitionY = 0;
            return false;
        }

        @Override
        public boolean onUp(MotionEvent event) {
            mTransitionX = 0;
            mTransitionY = 0;
            return false;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float dx, float dy) {
            if(mCameraActivity.getAppUi().getCsState()){
                LogHelper.e(TAG, "onScroll for mode switch is not available");
                return false;
            }
            if(!mEnable){
                return false;
            }
            if (e2.getPointerCount() > 1) {
                return false;
            }
		    //bv wuyonglin delete for all area fling to change mode start
            /*int items = mCameraActivity.getShutterButtonManager().getModeItems().size();
            CHorizontalScrollView mCHorizontalScrollView = mCameraActivity.getShutterButtonManager().getCHorizontalScrollView();*/
		    //bv wuyonglin delete for all area fling to change mode end
            //bv wuyonglin add for switch to front camera slide to left happened IndexOutOfBoundsException 20200108 start
            items = mCameraActivity.getShutterButtonManager().getModeItems().size();
            //bv wuyonglin add for switch to front camera slide to left happened IndexOutOfBoundsException 20200108 end
            if(mCHorizontalScrollView.getVisibility() != VISIBLE){
                return false;
            }
            if (items < 2) {
                return false;
            }
            if (mIsScale) {
                return false;
            }
            if (mScroller.isFinished() && isEnabled() && mResumed) {
                mTransitionX += dx;
                mTransitionY += dy;

                Configuration config = getResources().getConfiguration();
                if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    if (Math.abs(mTransitionX) > MINI_SCROLL_LENGTH
                            && Math.abs(mTransitionY) < Math.abs(mTransitionX)) {
                        if (mTransitionX > 0 && mCurrentIndex < (items - 1)) {
                            /*if (getVisibility() != VISIBLE) {
                                return false;
                            }
                            if (getChildAt(mCurrentIndex + 1).getVisibility() != VISIBLE) {
                                return false;
                            }*/
                            snapTOShutter(mCurrentIndex + 1, ANIM_DURATION_MS);
                        } else if (mTransitionX < 0 && mCurrentIndex > 0) {
                            /*if (getVisibility() != VISIBLE) {
                                return false;
                            }
                            if (getChildAt(mCurrentIndex - 1).getVisibility() != VISIBLE) {
                                return false;
                            }*/
                            snapTOShutter(mCurrentIndex - 1, ANIM_DURATION_MS);
                        }
                        mEnable = false;
                        return true;
                    }
                } else if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    if (Math.abs(mTransitionY) > MINI_SCROLL_LENGTH
                            && Math.abs(mTransitionX) < Math.abs(mTransitionY)) {
                        if (mTransitionY < 0 && mCurrentIndex < (getChildCount() - 1)) {
                            if (getChildAt(mCurrentIndex + 1).getVisibility() != VISIBLE) {
                                return false;
                            }
                            snapTOShutter(mCurrentIndex + 1, ANIM_DURATION_MS);
                        } else if (mTransitionY > 0 && mCurrentIndex > 0) {
                            if (getChildAt(mCurrentIndex - 1).getVisibility() != VISIBLE) {
                                return false;
                            }
                            snapTOShutter(mCurrentIndex - 1, ANIM_DURATION_MS);
                        }
                    }
                    mEnable = false;
                }
                return false;
            } else {
                return true;
            }
        }

        @Override
        public boolean onSingleTapUp(float x, float y) {
            return false;
        }

        @Override
        public boolean onSingleTapConfirmed(float x, float y) {
            return false;
        }

        @Override
        public boolean onDoubleTap(float x, float y) {
            return false;
        }

        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
            return false;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
            mIsScale = true;
            return false;
        }

        @Override
        public boolean onScaleEnd(ScaleGestureDetector scaleGestureDetector) {
            mIsScale = false;
            return false;
        }

        @Override
        public boolean onLongPress(float x, float y) {
            return false;
        }
    }

    // [Add for CCT tool] Receive keycode and switch photo/video mode @{
    public IApp.KeyEventListener getKeyEventListener() {
        return new KeyEventListenerImpl();
    }

    private class KeyEventListenerImpl implements IApp.KeyEventListener {

        @Override
        public boolean onKeyDown(int keyCode, KeyEvent event) {
            if ((keyCode != CameraUtil.KEYCODE_SWITCH_TO_PHOTO
                    && keyCode != CameraUtil.KEYCODE_SWITCH_TO_VIDEO)
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
            if ((keyCode != CameraUtil.KEYCODE_SWITCH_TO_PHOTO
                    && keyCode != CameraUtil.KEYCODE_SWITCH_TO_VIDEO)) {
                return false;
            }
            if (getChildCount() < 2) {
                LogHelper.w(TAG, "onKeyUp no need to slide betwwen photo mode and video mode," +
                        "one mode olny");
                return false;
            }
            if (keyCode == CameraUtil.KEYCODE_SWITCH_TO_PHOTO
                    && getChildCount() == 2
                    && getChildAt(0).getVisibility() == View.VISIBLE
                    && getChildAt(1).getVisibility() == View.VISIBLE) {
                onShutterTextClicked(0);
            } else if (keyCode == CameraUtil.KEYCODE_SWITCH_TO_VIDEO
                    && getChildCount() == 2
                    && getChildAt(0).getVisibility() == View.VISIBLE
                    && getChildAt(1).getVisibility() == View.VISIBLE) {
                onShutterTextClicked(1);
            }
            return true;
        }
    }
    // @}
    public void setModeIndex(int index){
        mCurrentIndex = index;
    }
    public void enableOnScroll(boolean enable){
        mEnable = enable;
    }
    //bv wuyonglin add for all area fling to change mode start
    CHorizontalScrollView.OnFlingListener mOnFlingListener = new CHorizontalScrollView.OnFlingListener() {
        @Override
        public void doFling(int direction) {
		//bv wuyonglin add for take picture at CHorizontalScrollView quick slide second to change mode happened can not connect camera 20200306 start
		if (mEnable) {
		//bv wuyonglin add for take picture at CHorizontalScrollView quick slide second to change mode happened can not connect camera 20200306 end
		if (direction == 1) {
		    if (mCurrentIndex > 0) {
			snapTOShutter(mCurrentIndex - 1, ANIM_DURATION_MS);
		    }
		} else {
		    if (mCurrentIndex < (items - 1)) {
			snapTOShutter(mCurrentIndex + 1, ANIM_DURATION_MS);
		    }
		}
		//bv wuyonglin add for take picture at CHorizontalScrollView quick slide second to change mode happened can not connect camera 20200306 start
		}
		//bv wuyonglin add for take picture at CHorizontalScrollView quick slide second to change mode happened can not connect camera 20200306 end
        }
    };
    //bv wuyonglin add for all area fling to change mode end
 
    public void snapToNextShutter() {
        snapTOShutter(mCurrentIndex - 1, ANIM_DURATION_MS);
        mEnable = true;
    };
}
