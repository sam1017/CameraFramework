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

import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import com.mediatek.camera.R;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil.Tag;
import com.mediatek.camera.common.utils.CameraUtil;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import com.mediatek.camera.feature.setting.visualsearch.VisualSearchSettingViewController;
//bv wuyonglin add for adjust screen height 1560px top bar position 20200628 start
import android.view.WindowManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.content.Context;
import android.widget.RelativeLayout;
import android.widget.FrameLayout;
//bv wuyonglin add for adjust screen height 1560px top bar position 20200628 end

/**
 * A manager for {@link QuickSwitcher}.
 */
public class QuickSwitcherManager extends AbstractViewManager {

    /**
     * Mode change listener, when a mode is selected, notify the new mode.
     */
    public interface OnModeChangedListener {
        /**
         * Notify the new mode info.
         * @param modeName The selected mode item.
         */
        void onModeChanged(String modeName);
    }

    private static final Tag TAG = new Tag(
            QuickSwitcherManager.class.getSimpleName());
    private static final int MARGIN_IN_DP = 61;        //bv wuyonglin modify for quickswitcher icon margin 20191226
    //bv wuyonglin add for adjust hdr and video quality quickswitcher icon margin 20200104 start
    private static final int VIDEO_QUALITY_MARGIN_IN_DP = 7;
    private static final int HDR_MARGIN_IN_DP = 5;
    //bv wuyonglin add for adjust hdr and video quality quickswitcher icon margin 20200104 end
    private static final int ITEM_LIMIT = 4;
    private LinearLayout mQuickSwitcherLayout;
    private ConcurrentSkipListMap<Integer, View> mQuickItems = new ConcurrentSkipListMap<>();
    private final OnOrientationChangeListenerImpl mOrientationChangeListener;
    private View mTopBar;
    private ViewGroup mOptionRoot;
    private OnModeChangedListener mOnModeChangedListener;
    private IApp mIApp;
    private VisualSearchSettingViewController mVisualSearchSettingViewController;
    private int mScreenWidth;        //bv wuyonglin add for quickswitcher icon margin 20191226
    /**
     * constructor of QuickSwitcherManager.
     * @param app The {@link IApp} implementer.
     * @param parentView the root view of ui.
     */
    public QuickSwitcherManager(IApp app, ViewGroup parentView) {
        super(app, parentView);
        mIApp = app;
        mTopBar = app.getActivity().findViewById(R.id.top_bar);
        mOptionRoot = (ViewGroup) mApp.getActivity().findViewById(R.id.quick_switcher_option);
        mOrientationChangeListener = new OnOrientationChangeListenerImpl();
        mVisualSearchSettingViewController= new VisualSearchSettingViewController(mIApp);
        mScreenWidth = mApp.getActivity().getResources().getDisplayMetrics().widthPixels;        //bv wuyonglin add for quickswitcher icon margin 20191226
        //bv wuyonglin add for adjust screen height 1560px top bar position 20200628 start
        WindowManager wm = (WindowManager) mApp.getActivity().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        display.getRealMetrics(dm);
        if (dm.heightPixels == 1560) {
            RelativeLayout.LayoutParams topBarLayoutParams = (RelativeLayout.LayoutParams) mTopBar.getLayoutParams();
            topBarLayoutParams.topMargin = mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.top_bar_margin_top_1560px);
            mTopBar.setLayoutParams(topBarLayoutParams);
            FrameLayout.LayoutParams optionRootLayoutParams = (FrameLayout.LayoutParams) mOptionRoot.getLayoutParams();
            optionRootLayoutParams.topMargin = mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.top_bar_margin_top_1560px);
            mOptionRoot.setLayoutParams(optionRootLayoutParams);
            //bv wuyonglin add for screen 1440px adjust all icon position 20200709 start
        } else if (dm.heightPixels == 1440) {
            RelativeLayout.LayoutParams topBarLayoutParams = (RelativeLayout.LayoutParams) mTopBar.getLayoutParams();
            topBarLayoutParams.topMargin = mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.top_bar_margin_top_1440px);
            mTopBar.setLayoutParams(topBarLayoutParams);
            FrameLayout.LayoutParams optionRootLayoutParams = (FrameLayout.LayoutParams) mOptionRoot.getLayoutParams();
            optionRootLayoutParams.topMargin = mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.top_bar_margin_top_1440px);
            mOptionRoot.setLayoutParams(optionRootLayoutParams);
            //bv wuyonglin add for screen 1440px adjust all icon position 20200709 end
        } else if (dm.heightPixels == 2300) {
            RelativeLayout.LayoutParams topBarLayoutParams = (RelativeLayout.LayoutParams) mTopBar.getLayoutParams();
            topBarLayoutParams.topMargin = mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.top_bar_margin_top_2300px);
            mTopBar.setLayoutParams(topBarLayoutParams);
            FrameLayout.LayoutParams optionRootLayoutParams = (FrameLayout.LayoutParams) mOptionRoot.getLayoutParams();
            optionRootLayoutParams.topMargin = mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.top_bar_margin_top_2300px);
            mOptionRoot.setLayoutParams(optionRootLayoutParams);
        }
        //bv wuyonglin add for adjust screen height 1560px top bar position 20200628 end
    }

    @Override
    protected View getView() {
        mQuickSwitcherLayout = (LinearLayout) mParentView.findViewById(R.id.quick_switcher);
        updateQuickItems();
        return mQuickSwitcherLayout;
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (mQuickSwitcherLayout != null) {
            int count = mQuickSwitcherLayout.getChildCount();
            for (int i = 0; i < count; i++) {
                View view = mQuickSwitcherLayout.getChildAt(i);
                view.setEnabled(enabled);
                view.setClickable(enabled);
            }
        }
    }

    /**
     * Set mode change listener that used to notify mode change event.
     * @param listener The listener instance.
     */
    public void setModeChangeListener(OnModeChangedListener listener) {
        mOnModeChangedListener = listener;
    }

    /**
     * add view to quick switcher with specified priority.
     * @param view The view register to quick switcher.
     * @param priority The priority that the registered view sort order.
     */
    public void addToQuickSwitcher(View view, int priority) {
        LogHelper.d(TAG, "[registerToQuickSwitcher] priority = " + priority);
        if (mQuickItems.size() > ITEM_LIMIT) {
            LogHelper.w(TAG, "already reach to limit number : " + ITEM_LIMIT);
            return;
        }
        if (!mQuickItems.containsValue(view)) {
            mQuickItems.put(priority, view);
        }
    }

    public void isChangedMode(boolean isChanged,int flag){
        LogHelper.i(TAG, "isChanged : " + isChanged);
        if (isChanged) {
            if (mVisualSearchSettingViewController == null) {
                LogHelper.i(TAG, "mVisualSearchSettingViewController is null : ");
                mVisualSearchSettingViewController= new VisualSearchSettingViewController(mIApp);
            }
            mVisualSearchSettingViewController.setOnModeChangedListener(new OnModeSelectedListenerImpl(),flag);
        }
    }

    /**
     * remove view from quick switcher.
     * @param view The view removed from quick switcher.
     */
    public void removeFromQuickSwitcher(View view) {
        LogHelper.d(TAG, "[removeFromQuickSwitcher]");
        if (mQuickItems.containsValue(view)) {
            Iterator iterator = mQuickItems.entrySet().iterator();
            int priority;
            while (iterator.hasNext()) {
                Map.Entry map = (Map.Entry) iterator.next();
                View v = (View) map.getValue();
                if (v == view) {
                    priority = (Integer) map.getKey();
                    LogHelper.d(TAG, "[removeFromQuickSwitcher] priority = " + priority);
                    mQuickItems.remove(priority, v);
                }
            }
        }
    }
    /**
     * Register quick switcher icon view, layout position will be decided by the priority.
     */
    public void registerQuickIconDone() {
        updateQuickItems();
    }

    /**
     * Show quick switcher option view, mode picker and quick switch will disappear.
     * @param optionView the option view, it should not attach to any parent view.
     */
    public void showQuickSwitcherOption(View optionView) {
        if (mOptionRoot.getChildCount() != 0) {
            LogHelper.e(TAG, "[showQuickSwitcherOption] Already has options to be shown!");
            return;
        }
        //bv wuyonglin delete for remove quick switcher option view anim top in 20200109 start
        //Animation inAnim = AnimationUtils.loadAnimation(mApp.getActivity(), R.anim.anim_top_in);
        //bv wuyonglin delete for remove quick switcher option view anim top in 20200109 end
        mOptionRoot.addView(optionView);
        int orientation = mApp.getGSensorOrientation();
        CameraUtil.rotateRotateLayoutChildView(mApp.getActivity(), mOptionRoot, orientation, true);
        mOptionRoot.setVisibility(View.VISIBLE);
        mOptionRoot.setClickable(true);
        //bv wuyonglin delete for remove quick switcher option view anim top in 20200109 start
        //mOptionRoot.startAnimation(inAnim);
        //bv wuyonglin delete for remove quick switcher option view anim top in 20200109 end
        mTopBar.setVisibility(View.GONE);
        mApp.registerOnOrientationChangeListener(mOrientationChangeListener);
    }

    /**
     * Hide quick switcher option view, it will remove from the option parent view.
     */
    public void hideQuickSwitcherOption() {
        //bv wuyonglin delete for remove quick switcher option view anim top out 20200109 start
        /*Animation outAnim = AnimationUtils.loadAnimation(mApp.getActivity(), R.anim.anim_top_out);
        outAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mOptionRoot.setVisibility(View.GONE);
                mOptionRoot.setClickable(false);
                mOptionRoot.removeAllViews();
                mTopBar.setVisibility(View.VISIBLE);
                mApp.unregisterOnOrientationChangeListener(mOrientationChangeListener);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        mOptionRoot.startAnimation(outAnim);
        outAnim.setFillAfter(true);*/
        //bv wuyonglin delete for remove quick switcher option view anim top out 20200109 end
        //bv wuyonglin add for remove quick switcher option view anim top out 20200109 start
        //bv wuyonglin add for optimize touch blank area quick switcher option can hide 2020025 start
        if (mOptionRoot.isShown()) {
        //bv wuyonglin add for optimize touch blank area quick switcher option can hide 2020025 end
        mOptionRoot.setVisibility(View.GONE);
        mOptionRoot.setClickable(false);
        mOptionRoot.removeAllViews();
        mTopBar.setVisibility(View.VISIBLE);
        mApp.unregisterOnOrientationChangeListener(mOrientationChangeListener);
        //bv wuyonglin add for optimize touch blank area quick switcher option can hide 2020025 start
        }
        //bv wuyonglin add for optimize touch blank area quick switcher option can hide 2020025 end
        //bv wuyonglin add for remove quick switcher option view anim top out 20200109 end
    }

    /**
     * Hide quick switcher without animation.
     */
    public void hideQuickSwitcherImmediately() {
        mOptionRoot.setVisibility(View.GONE);
        mOptionRoot.removeAllViews();
        mTopBar.setVisibility(View.VISIBLE);
        mApp.unregisterOnOrientationChangeListener(mOrientationChangeListener);
    }

    private void updateQuickItems() {
        float density = mApp.getActivity().getResources().getDisplayMetrics().density;
        int marginInPix = (int) (MARGIN_IN_DP * density);
        //bv wuyonglin add for adjust hdr and video quality quickswitcher icon margin 20200104 start
        int marginInPixVideoQuality = (int) (VIDEO_QUALITY_MARGIN_IN_DP * density);
        int marginInPixHdr = (int) (HDR_MARGIN_IN_DP * density);
        //bv wuyonglin add for adjust hdr and video quality quickswitcher icon margin 20200104 end
        //bv wuyonglin add for quickswitcher icon margin 20191226 start
        int mQuickSwitcherIconWidth = mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.quick_switcher_icon_width);
        if (mQuickItems.entrySet().size() > 0) {
            marginInPix = (int) (mScreenWidth - mQuickSwitcherIconWidth * (mQuickItems.entrySet().size() + 1)) / mQuickItems.entrySet().size();
        }
        //bv wuyonglin add for quickswitcher icon margin 20191226 end
        if (mQuickSwitcherLayout != null && mQuickSwitcherLayout.getChildCount() != 0) {
            mQuickSwitcherLayout.removeAllViews();
        }
       //bv wuyonglin add for hdr icon not show first 20200102 start
       //bv wuyonglin detele for switcher front camera not show video quality 20200103 start
	/*if (mQuickItems.entrySet().size() <= 1) {
	    return;
	}*/
       //bv wuyonglin detele for switcher front camera not show video quality 20200103 end
       //bv wuyonglin add for hdr icon not show first 20200102 end
        if (mQuickSwitcherLayout != null) {
            Iterator iterator = mQuickItems.entrySet().iterator();
	    int i = 1;
	    //bv wuyonglin add for adjust hdr and video quality quickswitcher icon margin 20200104 start
	    int priority = 1;
	    //bv wuyonglin add for adjust hdr and video quality quickswitcher icon margin 20200104 start
            while (iterator.hasNext()) {
                Map.Entry map = (Map.Entry) iterator.next();
                View view = (View) map.getValue();
		//bv wuyonglin add for adjust hdr and video quality quickswitcher icon margin 20200104 start
                priority = (Integer) map.getKey();
		//bv wuyonglin add for adjust hdr and video quality quickswitcher icon margin 20200104 end
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
		if (i == 1) {
		   //bv wuyonglin add for adjust hdr and video quality quickswitcher icon margin 20200104 start
		   if (priority == 35) {
                        params.setMargins(marginInPixHdr, 0, 0, 0);
		   } else if (priority == 45) {
                        params.setMargins(marginInPixVideoQuality, 0, 0, 0);
		   } else {
		   //bv wuyonglin add for adjust hdr and video quality quickswitcher icon margin 20200104 end
                params.setMargins(0, 0, 0, 0);        //bv wuyonglin modify for quickswitcher icon margin 20191226
		   }
                } else {
                params.setMargins(marginInPix, 0, 0, 0);
		}
		i++;
                view.setLayoutParams(params);
                mQuickSwitcherLayout.addView(view);
            }
            updateViewOrientation();
        }
    }
    /**
     * Implementer of OnOrientationChangeListener.
     */
    private class OnOrientationChangeListenerImpl implements IApp.OnOrientationChangeListener {

        @Override
        public void onOrientationChanged(int orientation) {
            if (mOptionRoot != null && mOptionRoot.getChildCount() != 0) {
                CameraUtil.rotateRotateLayoutChildView(mApp.getActivity(), mOptionRoot,
                        orientation, true);
            }
        }
    }

    /**
     * The OnModeSelectedListener implement.
     */
    private class OnModeSelectedListenerImpl implements VisualSearchSettingViewController.OnAIModeChangedListener {

        @Override
        public void onModeChanged(String modeName) {
            LogHelper.d(TAG, "[OnModeSelectedListenerImpl] modeName = "+modeName);
            mOnModeChangedListener.onModeChanged(modeName);
        }
    }

}
