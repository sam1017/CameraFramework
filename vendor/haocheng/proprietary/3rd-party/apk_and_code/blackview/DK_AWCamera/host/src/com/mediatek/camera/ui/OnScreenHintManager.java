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
package com.mediatek.camera.ui;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mediatek.camera.R;
import com.mediatek.camera.common.IAppUi;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.utils.CameraUtil;

import java.util.LinkedList;
//bv wuyonglin add for screen 1440px adjust all icon position 20200709 start
import android.view.WindowManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.content.Context;
import android.widget.FrameLayout;
//bv wuyonglin add for screen 1440px adjust all icon position 20200709 end

/**
 * Screen hint manager, used for prompt the user for some information.
 */
class OnScreenHintManager {
    private static final LogUtil.Tag TAG =
            new LogUtil.Tag(OnScreenHintManager.class.getSimpleName());
    private static final int HIDE_HINT= 0;
    private ViewGroup mHintRoot;
    private final IApp mApp;
    private TextView mTopAlwaysHint;
    private TextView mAutoHideHint;
    private TextView mBottomAlwaysHint;
    private Stack<IAppUi.HintInfo> mTopStack;
    private Stack<IAppUi.HintInfo> mBottomStack;
    private IAppUi.HintInfo mCurrentTopInfo;
    private IAppUi.HintInfo mCurrentBottomInfo;
    private IAppUi.HintInfo mCurrentAutoHideInfo;
    private MainHandler mMainHandler;
    private final OnOrientationChangeListenerImpl mOrientationChangeListener;
    //add by huangfei for bokeh tips start
    private TextView mTopManualHint;
    private boolean mShow = false;
    //add by huangfei for bokeh tips end
    //bv wuyonglin add for screen 1440px adjust all icon position 20200709 start
    private int mScreenHeight;
    //bv wuyonglin add for screen 1440px adjust all icon position 20200709 end

    /**
     * Constructor of OnScreenHintManager.
     *
     * @param app        The {@link IApp} implementer.
     * @param parentView the root view of ui.
     */
     OnScreenHintManager(IApp app, ViewGroup parentView) {
        mApp = app;
        mHintRoot = (ViewGroup)parentView.findViewById(R.id.screen_hint_root);
        mOrientationChangeListener = new OnOrientationChangeListenerImpl();
        mApp.registerOnOrientationChangeListener(mOrientationChangeListener);
        mTopAlwaysHint = (TextView) mHintRoot.findViewById(R.id.top_always_hint);
        mAutoHideHint = (TextView) mHintRoot.findViewById(R.id.auto_hide_hint);
        mBottomAlwaysHint = (TextView) mHintRoot.findViewById(R.id.bottom_always_hint);
        //add by huangfei for bokeh tips start
        mTopManualHint = (TextView) mHintRoot.findViewById(R.id.top_manual_hint);
        //add by huangfei for bokeh tips start
        mTopStack = new Stack<>();
        mBottomStack = new Stack<>();
        mMainHandler = new MainHandler(app.getActivity().getMainLooper());
        //bv wuyonglin add for screen 1440px adjust all icon position 20200709 start
        WindowManager wm = (WindowManager) mApp.getActivity().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        display.getRealMetrics(dm);
        mScreenHeight = dm.heightPixels;
        if (dm.heightPixels == 1560) {
            FrameLayout.LayoutParams mHintRootLayoutParams = (FrameLayout.LayoutParams) mHintRoot.getLayoutParams();
            mHintRootLayoutParams.topMargin = mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.camera_preview_margin_top_1560px);
            mHintRoot.setLayoutParams(mHintRootLayoutParams);
	} else if (dm.heightPixels == 1440) {
            FrameLayout.LayoutParams mHintRootLayoutParams = (FrameLayout.LayoutParams) mHintRoot.getLayoutParams();
            mHintRootLayoutParams.topMargin = mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.camera_preview_margin_top_1440px);
            mHintRoot.setLayoutParams(mHintRootLayoutParams);
	} else if (dm.heightPixels == 2300) {
            FrameLayout.LayoutParams mHintRootLayoutParams = (FrameLayout.LayoutParams) mHintRoot.getLayoutParams();
            mHintRootLayoutParams.topMargin = mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.camera_preview_margin_top_2300px);
            mHintRoot.setLayoutParams(mHintRootLayoutParams);
	} else if (dm.heightPixels == 2400) {
            FrameLayout.LayoutParams mHintRootLayoutParams = (FrameLayout.LayoutParams) mHintRoot.getLayoutParams();
            mHintRootLayoutParams.topMargin = mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.camera_preview_margin_top_2400px);
            mHintRoot.setLayoutParams(mHintRootLayoutParams);
	}
        //bv wuyonglin add for screen 1440px adjust all icon position 20200709 end
    }

    void showScreenHint(IAppUi.HintInfo info) {
        if(info == null) {
            LogHelper.e(TAG,"showScreenHint info is null!");
            return;
        }
        LogHelper.d(TAG,"showScreenHint type = " + info.mType + " string = " + info.mHintText);
        if(info.mHintText == null) {
            return;
        }
        switch(info.mType) {
            case TYPE_ALWAYS_TOP:
                if(mCurrentTopInfo != null) {
                    mTopStack.push(mCurrentTopInfo);
                }
                mCurrentTopInfo = info;

                mTopAlwaysHint.setText(mCurrentTopInfo.mHintText);
                mTopAlwaysHint.setBackground(mCurrentTopInfo.mBackground);
                mTopAlwaysHint.setVisibility(View.VISIBLE);
                break;
            case TYPE_AUTO_HIDE:
                mAutoHideHint.setText(info.mHintText);
                mAutoHideHint.setBackground(info.mBackground);
                mBottomAlwaysHint.setVisibility(View.GONE);
                mAutoHideHint.setVisibility(View.VISIBLE);
                mCurrentAutoHideInfo = info;
                mMainHandler.removeMessages(HIDE_HINT);
                mMainHandler.sendEmptyMessageDelayed(HIDE_HINT, info.mDelayTime);
                break;
            case TYPE_ALWAYS_BOTTOM:
                if(mCurrentBottomInfo != null) {
                    mBottomStack.push(mCurrentBottomInfo);
                }
                mCurrentBottomInfo = info;

                mBottomAlwaysHint.setText(mCurrentBottomInfo.mHintText);
                mBottomAlwaysHint.setBackground(mCurrentBottomInfo.mBackground);
                if(mAutoHideHint.getVisibility() != View.VISIBLE) {
                    mBottomAlwaysHint.setVisibility(View.VISIBLE);
                }
                break;
            //add by huangfei for bokeh tips start
            case TYPE_MANUAL_HIDE:
                if(mShow){
                    break;
                }
                if(mCurrentTopInfo != null) {
                    mTopStack.push(mCurrentTopInfo);
                }
                mCurrentTopInfo = info;
                //mOrientationChangeListener.onOrientationChanged(0);
                mTopManualHint.setText(mCurrentTopInfo.mHintText);
                mTopManualHint.setBackground(mCurrentTopInfo.mBackground);
                mTopManualHint.setVisibility(View.VISIBLE);
                mShow =true;
                break;
            //add by huangfei for bokeh tips end
            default:
                break;
        }
    }

     void hideScreenHint(IAppUi.HintInfo info){
         if(info == null) {
             LogHelper.e(TAG,"hideScreenHint info is null!");
             return;
         }
         LogHelper.d(TAG,"hideScreenHint type = " + info.mType + " string = " + info.mHintText);
        switch(info.mType) {
            case TYPE_ALWAYS_TOP:
                if (info == mCurrentTopInfo) {
                    if (mTopStack.empty()) {
                        mCurrentTopInfo = null;
                        mTopAlwaysHint.setVisibility(View.GONE);
                    } else {
                        mCurrentTopInfo = mTopStack.pop();
                        mTopAlwaysHint.setText(mCurrentTopInfo.mHintText);
                        mTopAlwaysHint.setBackground(mCurrentTopInfo.mBackground);
                        mTopAlwaysHint.setVisibility(View.VISIBLE);
                    }
                } else {
                    if(!mTopStack.empty()) {
                        mTopStack.remove(info);
                    }
                }
                break;
            case TYPE_AUTO_HIDE:
                if(info == mCurrentAutoHideInfo) {
                    mMainHandler.removeMessages(HIDE_HINT);
                    mAutoHideHint.setVisibility(View.GONE);
                    if (mCurrentBottomInfo != null) {
                        mBottomAlwaysHint.setVisibility(View.VISIBLE);
                    }
                }
                break;
            case TYPE_ALWAYS_BOTTOM:
                if(info == mCurrentBottomInfo) {
                    if (mBottomStack.empty()) {
                        mCurrentBottomInfo = null;
                        mBottomAlwaysHint.setVisibility(View.GONE);
                    } else {
                        mCurrentBottomInfo = mBottomStack.pop();
                        mBottomAlwaysHint.setText(mCurrentBottomInfo.mHintText);
                        mBottomAlwaysHint.setBackground(mCurrentBottomInfo.mBackground);
                        mBottomAlwaysHint.setVisibility(View.VISIBLE);
                    }
                } else {
                    if(!mBottomStack.empty()) {
                        mBottomStack.remove(info);
                    }
                }
                break;
            //add by huangfei for bokeh tips start
            case TYPE_MANUAL_HIDE:
                if (info == mCurrentTopInfo) {
                    mCurrentTopInfo = null;
                    mTopManualHint.setVisibility(View.GONE);    
                }else{
                    if(!mTopStack.empty()) {
                        mTopStack.remove(info);
                    }
                }
                mShow =false;
                break;
            //add by huangfei for bokeh tips end
            default:
                break;
        }
    }

    void setVisibility(int visibility) {
        if (mHintRoot != null) {
            mHintRoot.setVisibility(visibility);
        }
    }

    private class MainHandler extends Handler {
        public MainHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HIDE_HINT:
                    mAutoHideHint.setVisibility(View.GONE);
                    if (mCurrentBottomInfo != null) {
                        mBottomAlwaysHint.setVisibility(View.VISIBLE);
                    }
                    break;
                default:
                    break;

            }
        }
    }

    /**
     * Implementer of OnOrientationChangeListener.
     */
    private class OnOrientationChangeListenerImpl implements IApp.OnOrientationChangeListener {

        private static final int TEXT_MARGIN_VERTICAL_REVERSE = 180;
        private static final int TEXT_MARGIN_VERTICAL = 50;
        private static final int TEXT_MARGIN_HORIZON = 10;

        @Override
        public void onOrientationChanged(int orientation) {
            //add by huangfei for bokeh tips start
            if(true){
                return;
            }
            //add by huangfei for bokeh tips end
            View view = mHintRoot.findViewById(R.id.hint_root);

            int display = CameraUtil.getDisplayRotation(mApp.getActivity());
            int ori = orientation;
            if (display == 270) {
                ori = (ori + 180) % 360;
            }
            if (ori == 180) {
                return;
            }
            switch (ori) {
                case 0:
                    view.setPadding(dpToPixel(11),dpToPixel(TEXT_MARGIN_VERTICAL),dpToPixel(11),0);

                    break;
                case 180:
                    view.setPadding(dpToPixel(11),dpToPixel(TEXT_MARGIN_VERTICAL_REVERSE),
                            dpToPixel(11),0);
                    break;
                case 90:
                case 270:
                    view.setPadding(dpToPixel(TEXT_MARGIN_VERTICAL_REVERSE),
                            dpToPixel(TEXT_MARGIN_HORIZON),
                            dpToPixel(TEXT_MARGIN_VERTICAL_REVERSE),
                            0);
                    break;
                default:
                    break;
            }

            if (mHintRoot != null) {
                CameraUtil.rotateRotateLayoutChildView(mApp.getActivity(), mHintRoot,
                        orientation, true);
            }
        }
        private int dpToPixel(int dp) {
            float scale = mApp.getActivity().getResources().getDisplayMetrics().density;
            return (int) (dp * scale + 0.5f);
        }
    }

    /**
     * Stack for recording the always hint.
     * @param <T> The hint structure.
     */
    private class Stack<T> {
        private LinkedList<T> storage = new LinkedList<>();

        public void push(T v) {
            storage.addFirst(v);
        }

        public T peek() {
          return storage.getFirst();
        }

        public T pop() {
            return storage.removeFirst();
        }

        public boolean empty() {
            return storage.isEmpty();
        }

        public boolean remove(T v) {
            return storage.remove(v);
        }

        public String toString() {
            return storage.toString();
        }
    }
}
