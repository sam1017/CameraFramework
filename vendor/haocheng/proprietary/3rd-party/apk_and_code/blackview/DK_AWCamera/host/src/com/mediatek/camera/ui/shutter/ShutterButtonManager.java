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
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.mediatek.camera.R;
import com.mediatek.camera.common.IAppUi;
import com.mediatek.camera.common.IAppUiListener.OnShutterButtonListener;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.utils.PriorityConcurrentSkipListMap;
import com.mediatek.camera.ui.AbstractViewManager;


import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.ArrayList;
import com.mediatek.camera.CameraActivity;
import com.mediatek.camera.Config;
import android.widget.TextView;
import android.view.View;
import android.app.Activity;
import android.content.Intent;
import android.provider.MediaStore;
//bv wuyonglin add for adjust all icon position 20200612 start
import android.view.WindowManager;
import android.util.DisplayMetrics;
import android.view.Display;
//bv wuyonglin add for adjust all icon position 20200612 end
import com.mediatek.camera.ui.modepicker.ModePickerFrameLayout;
import android.widget.ImageView;
import com.mediatek.camera.common.utils.CameraUtil;
import android.util.TypedValue;

/**
 * A manager for {@link ShutterButton}.
 */
public class ShutterButtonManager extends AbstractViewManager implements
                                                    ShutterRootLayout.OnShutterChangeListener {
    /**
     * Shutter type change listener.
     */
    public interface OnShutterChangeListener {
        /**
         * When current valid shutter changed, invoke the listener to notify.
         * @param newShutterName The new valid shutter name.
         */
        void onShutterTypeChanged(String newModeName,String newModeType);
    }

    //*/ hct.huangfei, 20201206.hdr view int topbar.
    private static final int HDR_ITEM_PRIORITY = 85;
    //*/
    //add by huangfei for watermark start
    public interface WaterMarkCaptureListener {
        void onWaterMarkCaptureStart();
    }
    //add by huangfei for watermark end

    private final static int SHUTTER_GESTURE_PRIORITY = 20;
    private static final LogUtil.Tag TAG = new LogUtil.Tag(
                              ShutterButtonManager.class.getSimpleName());
    private ShutterButton.OnShutterButtonListener mShutterButtonListener;

    private PriorityConcurrentSkipListMap<String, OnShutterButtonListener> mShutterButtonListeners
            = new PriorityConcurrentSkipListMap<>(true);

    private static final int FINGERPRINT_PRIORITY = -2;
    /**
     * Used to store a shutter information.
     */
    private static class ShutterItem {
        public Drawable mShutterDrawable;
        public String mShutterType;
        public String mShutterName;
        //public ShutterTitleView mShutterTitleView;

        public String mShutterMode;
        public String mShutterModeName;
    }
    private ConcurrentSkipListMap<Integer, ShutterItem> mShutterButtons =
            new ConcurrentSkipListMap<>();

    private ShutterRootLayout mShutterLayout;
    private LayoutInflater mInflater;
    private OnShutterChangeListener mListener;
    private boolean isCanSwitchMode = true;
    private String mLastType = null;


    //add by huangfei for watermark start
    private WaterMarkCaptureListener mCaptureListener;
    private long mCurrentTime = 0;
    private long mTime = 0;
    //add by huangfei for watermark end
    private ShutterView mShutterView;
    private int mChangIndex = 0;
    private boolean mModeChangeStart = false;
    private boolean mShutterEnable =true;
    private List<ModeHolder> mModeHolders;
    private int mCurrentInx = 1;
    private View mContainer;
    private CHorizontalScrollView mScroller;
    private CameraActivity mCameraActivity;
    private TextView mModeTextView;
    private static final String PHOTO_MODE = "com.mediatek.camera.common.mode.photo.PhotoModeEntry";
    private boolean showMoreMode = false;
    private int photoIndex = 0;
    String photoModeName,photoModeType;
    int[] modePriority;
    private boolean mExitMoreModeEnable = true;
    //bv wuyonglin add for adjust all icon position 20200612 start
    private int mScreenHeight = 0;
    //bv wuyonglin add for adjust all icon position 20200612 end
    private ModePickerFrameLayout mFragment;
    private ViewGroup mShutterRoot;
    private boolean mShowMoreModeView = false;

    /**
     * constructor of ShutterButtonManager.
     * @param app The {@link IApp} implementer.
     * @param parentView the root view of ui.
     */
    public ShutterButtonManager(IApp app, ViewGroup parentView) {
        super(app, parentView);
        mShutterButtonListener = new ShutterButtonListenerImpl();
        mInflater = (LayoutInflater) app.getActivity()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mCameraActivity = (CameraActivity)app.getActivity();
        //bv wuyonglin add for adjust all icon position 20200612 start
        WindowManager wm = (WindowManager) mApp.getActivity().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        display.getRealMetrics(dm);
        mScreenHeight = dm.heightPixels;
        //bv wuyonglin add for adjust all icon position 20200612 end
        mFragment = (ModePickerFrameLayout) mApp.getActivity()
                    .findViewById(R.id.more_mode_framelayout);
        mShutterRoot = (ViewGroup) mApp.getActivity()
                    .findViewById(R.id.shutter_root_full_bg);
    }

    @Override
    protected View getView() {
        mShutterLayout = (ShutterRootLayout) mApp.getActivity().findViewById(R.id.shutter_root);
        mShutterLayout.setOnShutterChangedListener(this);
        mApp.getAppUi().registerGestureListener(mShutterLayout.getGestureListener(),
                SHUTTER_GESTURE_PRIORITY);
		mShutterView = (ShutterView) mApp.getActivity().findViewById(R.id.shutter_view_root);
        // [Add for CCT tool] Receive keycode and switch photo/video mode @{
        mApp.registerKeyEventListener(mShutterLayout.getKeyEventListener(), IApp.DEFAULT_PRIORITY);
        // @}
        return mShutterLayout;
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if(mScroller!=null){
            if(showMoreMode){
                if(visibility!=View.VISIBLE){
                    mScroller.setVisibility(visibility);
                }
            }else{
		//bv wuyonglin add for adjust custom FaceMode view position 20200303 start
                ImageView beautyExpand = (ImageView) mApp.getAppUi().getModeRootView().findViewById(R.id.aiworks_beauty_expand_button);
                /* modify by liangchangwei for fixbug 2627 -- 20201103  start */
                if(mApp.getAppUi().getZoomSliderUICtrl().isShowAll()){
                    LogHelper.i(TAG,"isShowAll setVisibility = View.GONE");
                    mScroller.setVisibility(View.GONE);
                }else if (!mApp.getAppUi().getIsCustomBeautyViewShow() || beautyExpand != null && beautyExpand.getVisibility() == View.VISIBLE) {
                    mScroller.setVisibility(visibility);
		        }
                /* modify by liangchangwei for fixbug 2627 -- 20201103  end */
		//bv wuyonglin add for adjust custom FaceMode view position 20200303 end
            }
        }
        if(mShutterView != null)
            mShutterView.setVisibility(visibility);
    }

    //bv wuyonglin add for adjust custom FaceMode view position 20200303 start
    public void setShutterRootLayoutVisibility(int visibility) {
        //add by huangfei for shutter title change start
        if(mScroller!=null){

            //modify by huangfei for more mode start
            //mScroller.setVisibility(visibility);
            if(showMoreMode){
                if(visibility!=View.VISIBLE){
                    mScroller.setVisibility(visibility);
                }
            }else{
                if (!mApp.getAppUi().getIsSelfTimerTextViewShow()) {
                    mScroller.setVisibility(visibility);
		        }
            }
            //modify by huangfei for more mode end
        }
        //add by huangfei for shutter title change end
    }
    //bv wuyonglin add for adjust custom FaceMode view position 20200303 end

    public void setOnShutterChangedListener(OnShutterChangeListener listener) {
        mListener = listener;
    }

    //add by huangfei for watermark start
    public void setWaterMarkCaptureListener(WaterMarkCaptureListener listener) {
        android.util.Log.i("gotowc","setWaterMarkCaptureListener ");
        mCaptureListener = listener;
    }
    //add by huangfei for watermark end

    @Override
    public void onShutterChangedStart(String newModeName,String newModeType,int index) {
        if (mListener != null) {
            mModeChangeStart = true;
            if(newModeType!=null){                
		        mShowMoreModeView = false;
                mApp.getAppUi().applyAllUIVisibility(View.VISIBLE);
                mFragment.setVisibility(View.GONE);
                mShutterRoot.setBackgroundColor(mApp.getActivity().getResources().getColor(android.R.color.transparent));
		        if (mApp.getAppUi().getModeRootView().findViewById(R.id.beauty_rotate_layout) != null) {
                    mApp.getAppUi().getModeRootView().findViewById(R.id.beauty_rotate_layout).setVisibility(View.VISIBLE);
		        }
                mListener.onShutterTypeChanged(newModeName,newModeType);
                mApp.getAppUi().getModePickerManager().updateModeName(newModeName,newModeType,index);
            }else{
		        if (mApp.getAppUi().getModeRootView().findViewById(R.id.beauty_rotate_layout) != null) {
                    mApp.getAppUi().getModeRootView().findViewById(R.id.beauty_rotate_layout).setVisibility(View.GONE);
		        }
		        mShowMoreModeView = true;
                mApp.getAppUi().getModePickerManager().showMoreModeView();
                if (mShutterLayout != null) {
                    //mShutterLayout.enableOnScroll(enabled);
		        }
                mApp.getAppUi().applyAllUIVisibility(View.INVISIBLE);
                mApp.getAppUi().setUIVisibility(IAppUi.SHUTTER_ROOTLAYOUT, View.VISIBLE);
                mShutterRoot.setBackgroundColor(mApp.getActivity().getResources().getColor(R.color.more_transparent_background));
            }
            if(mScroller!=null){
                mScroller.setSelectIndex(index);
            }
            mCurrentInx = index ;
        }
        mShutterLayout.setModeIndex(mCurrentInx);
    }

    @Override
    public void onShutterChangedEnd() {
        mModeChangeStart = false;
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (mShutterLayout != null) {
            mShutterLayout.setEnabled(enabled);
	    if ("AiworksFaceBeauty".equals(mCameraActivity.getAppUi().getCurrentMode())) {
                if (isCanSwitchMode) {
                mShutterLayout.enableOnScroll(enabled);
                }
	    } else {
                mShutterLayout.enableOnScroll(enabled);
	    }
            int count = mShutterLayout.getChildCount();
            for (int i = 0; i < count; i++) {
                View view = mShutterLayout.getChildAt(i);
                view.setEnabled(enabled);
            }
        }
        if(mShutterView != null)
            mShutterView.setEnabled(enabled);
        if ("AiworksFaceBeauty".equals(mCameraActivity.getAppUi().getCurrentMode())) {
            if (isCanSwitchMode) {
                if(mScroller!=null){
                    mScroller.setEnabled(enabled);
                }
	        }
	    } else {
            if(mScroller!=null){
                mScroller.setEnabled(enabled);
            }
	    }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mShutterLayout != null) {
            mShutterLayout.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mShutterLayout != null) {
            mShutterLayout.onPause();
        }
    }

    /**
     * Set Shutter text can be clicked or not.
     * @param enabled True shutter text can be clicked.
     *                False shutter text can not be clicked.
     */
    public void setTextEnabled(boolean enabled) {
        if (mShutterLayout != null) {
            int count = mShutterLayout.getChildCount();
            for (int i = 0; i < count; i++) {
                View view = mShutterLayout.getChildAt(i);
                //((ShutterTitleView) view).setEnabled(enabled);
            }
            LogHelper.i(TAG, "setTextEnabled enabled="+enabled+" count ="+count);
        }
    }

    /**
     * Register shutterButton listener.
     * @param listener
     *            the listener set to shutterButtonManager.
     * @param priority The listener priority.
     */
    public void registerOnShutterButtonListener(OnShutterButtonListener listener, int priority) {
        if (listener == null) {
            LogHelper.e(TAG, "registerOnShutterButtonListener error [why null]");
        }
        mShutterButtonListeners.put(mShutterButtonListeners.getPriorityKey(priority, listener),
                listener);
    }

    /**
     * Unregister shutter button listener.
     *
     * @param listener The listener to be unregistered.
     */
    public void unregisterOnShutterButtonListener(OnShutterButtonListener listener) {
        if (listener == null) {
            LogHelper.e(TAG, "unregisterOnShutterButtonListener error [why null]");
        }
        if (mShutterButtonListeners.containsValue(listener)) {
            mShutterButtonListeners.remove(mShutterButtonListeners.findKey(listener));
        }
    }

    /**
     * Register shutter button UI.
     * @param drawable The shutter button icon drawable.
     * @param type The shutter type, such as "Picture" or "Photo", the type will be shown
     *             above the shutter icon as a text.
     * @param priority The shutter ui priority, the smaller the value, the higher the priority.
     *                 the high priority icon will be located on the left.
     */
    public void registerShutterButton(IAppUi.ModeItem modeItem, int priority) {
        if (mShutterButtons.containsKey(priority)) {
            return;
        }
        ShutterItem item = new ShutterItem();
        String type = modeItem.mType;
        item.mShutterType = type;
        if ("Video".equals(type)) {
            item.mShutterDrawable = mApp.getActivity().getResources()
                    .getDrawable(
                            R.drawable.ic_shutter_video);
        } else {
            item.mShutterDrawable = mApp.getActivity().getResources()
                    .getDrawable(
                            R.drawable.ic_shutter_photo);
        }
        String title = modeItem.mTitle;
        if (title == null) {
            if ("Video".equals(type)) {
                item.mShutterName =
                        (String) mApp.getActivity().getResources().getText(R.string.shutter_type_video);
            } else {
                item.mShutterName =
                        (String) mApp.getActivity().getResources().getText(R.string.shutter_type_photo);
            }
        } else {
            item.mShutterName = title;
        }
        item.mShutterMode = modeItem.mMode;
        item.mShutterModeName = modeItem.mModeName;

        modePriority = Config.getMoreModeList(mCameraActivity);
        boolean addItem = true;
        if(modePriority!=null&&modePriority.length>0){
            for(int i = 0;i< modePriority.length;i++){
                if(modeItem.mPriority==modePriority[i]){
                    addItem = false;
                    return;
                }     
            }
        }

        //*/ hct.huangfei, 20201206.hdr view int topbar.
        if(Config.isHdrInTopBarSupprot(mCameraActivity) && modeItem.mPriority == HDR_ITEM_PRIORITY){
            addItem = false;
            return;
        }
        //*/
        mShutterButtons.put(priority, item);
    }

    public void clearShutterButton() {
        if(mShutterLayout != null) {
            mShutterLayout.removeAllViews();
        }
    }
	public void clearShutterButtonItems() {
		if(mShutterButtons != null) {
            mShutterButtons.clear();
        }
	}
    /**
     * Register shutter done, it will trigger to refresh the ui.
     */
    public void registerDone() {

        ShutterItem shutter;
        ShutterItem prevShutter = null;

        if (mShutterLayout.getChildCount() != 0) {
            return;
        }

        mShutterLayout.removeAllViews();
        int index = 0;
        //modify by huangfei for shutter title change start
        //when registerDone, add the shutter view to root layout.
        //for (Integer key: mShutterButtons.keySet()) {
        //    shutter = mShutterButtons.get(key);
            //inflate the view
            /*shutterTitleView = (ShutterTitleView) mInflater.inflate(R.layout.shutter_item_title, mShutterLayout, false);
            shutterTitleView.setType(shutter.mShutterType);
            shutterTitleView.setName(shutter.mShutterName);
            shutterTitleView.setId(generateViewId());
            shutterTitleView.setOnShutterTextClickedListener(mShutterLayout);
            shutterTitleView.setTag(index);
            shutterTitleView.setMode(shutter.mShutterMode);
            shutterTitleView.setModeName(shutter.mShutterModeName);
            mShutterLayout.addView(shutterTitleView);
            shutter.mShutterTitleView = shutterTitleView;
            RelativeLayout.LayoutParams params =
                    (RelativeLayout.LayoutParams) shutterTitleView.getLayoutParams();
            if (prevShutter == null) {
                params.addRule(RelativeLayout.CENTER_HORIZONTAL);
            } else {
                params.addRule(RelativeLayout.RIGHT_OF, prevShutter.mShutterTitleView.getId());
                params.leftMargin = mApp.getActivity().getResources().getDimensionPixelOffset(R.dimen.camera_shutter_bar_title_gap);
                //params.leftMargin = shutterTitleView.
                //add by huangfei for display abnormal when diplay size change in settings start
                if(index==mShutterButtons.size()-1){
                    params.rightMargin = mApp.getActivity().getResources().getDimensionPixelOffset(R.dimen.camera_shutter_bar_title_right_margin);
                }
                //add by huangfei for display abnormal when diplay size change in settings end
            }
            prevShutter = shutter;
            index++;
        }*/
        View view = mCameraActivity.inflate(R.layout.century_mode_picker);
        mContainer = view.findViewById(R.id.century_mode_picker_scroller_layout);
        mScroller = ((CHorizontalScrollView) view.findViewById(R.id.century_mode_scroller));
        mModeTextView = (TextView) view.findViewById(R.id.more_mode_title);
        mModeTextView.setOnClickListener(mExitModeClickListener);
        //bv wuyonglin add for adjust all icon position 20200612 start
	if (mScreenHeight == 1560) {
        RelativeLayout.LayoutParams params1 =
                 (RelativeLayout.LayoutParams) mScroller.getLayoutParams();
        params1.setMargins(params1.leftMargin, params1.topMargin,
                params1.rightMargin, mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.camera_shutter_title_1560px));
        mScroller.setLayoutParams(params1);
        RelativeLayout.LayoutParams params2 =
                 (RelativeLayout.LayoutParams) mModeTextView.getLayoutParams();
        params2.setMargins(params2.leftMargin, params2.topMargin,
                params2.rightMargin, mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.camera_shutter_title_1560px));
        mModeTextView.setLayoutParams(params2);
        mModeTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.hct_shutter_title_size_1560px));
        //bv wuyonglin add for screen 1440px adjust all icon position 20200709 start
	} else if (mScreenHeight == 1440) {
        RelativeLayout.LayoutParams params1 =
                 (RelativeLayout.LayoutParams) mScroller.getLayoutParams();
        params1.setMargins(params1.leftMargin, params1.topMargin,
                params1.rightMargin, mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.camera_shutter_title_1440px));
        mScroller.setLayoutParams(params1);
        RelativeLayout.LayoutParams params2 =
                 (RelativeLayout.LayoutParams) mModeTextView.getLayoutParams();
        params2.setMargins(params2.leftMargin, params2.topMargin,
                params2.rightMargin, mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.more_mode_title_1440px));
        mModeTextView.setLayoutParams(params2);
        //bv wuyonglin add for screen 1440px adjust all icon position 20200709 end
	} else if (mScreenHeight == 2300) {
        RelativeLayout.LayoutParams params1 =
                 (RelativeLayout.LayoutParams) mScroller.getLayoutParams();
        params1.setMargins(params1.leftMargin, params1.topMargin,
                params1.rightMargin, mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.camera_shutter_title_2300px));
        mScroller.setLayoutParams(params1);
        RelativeLayout.LayoutParams params2 =
                 (RelativeLayout.LayoutParams) mModeTextView.getLayoutParams();
        params2.setMargins(params2.leftMargin, params2.topMargin,
                params2.rightMargin, mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.more_mode_title_2300px));
        mModeTextView.setLayoutParams(params2);
        mModeTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.hct_shutter_title_size_1560px));
        } else if (mScreenHeight == 2400) {
        RelativeLayout.LayoutParams params1 =
                 (RelativeLayout.LayoutParams) mScroller.getLayoutParams();
        params1.setMargins(params1.leftMargin, params1.topMargin,
                params1.rightMargin, mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.camera_shutter_title_2400px));
        mScroller.setLayoutParams(params1);
        RelativeLayout.LayoutParams params2 =
                 (RelativeLayout.LayoutParams) mModeTextView.getLayoutParams();
        params2.setMargins(params2.leftMargin, params2.topMargin,
                params2.rightMargin, mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.more_mode_title_2400px));
        mModeTextView.setLayoutParams(params2);
        mModeTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.hct_shutter_title_size_1560px));
	}
        //bv wuyonglin add for adjust all icon position 20200612 end
        mScroller.setOnItemClickListener(mOnItemClickListener);
        mModeHolders = new ArrayList<ModeHolder>();
        for (Integer key: mShutterButtons.keySet()) {
            shutter = mShutterButtons.get(key);
            mModeHolders.add(new ModeHolder(index, shutter.mShutterName,shutter.mShutterType,shutter.mShutterModeName));

                    //inflate the view
            String photoName = mCameraActivity.getResources().getString(R.string.shutter_type_photo);
            if(photoName.equals(shutter.mShutterName)){
                photoIndex = index;
                photoModeName = shutter.mShutterModeName;

                photoModeType = shutter.mShutterType; 
            }
            prevShutter = shutter;
            index++;
        }
        if(modePriority!=null && modePriority.length>0 && !isThirdPartyIntent() && !mApp.getAppUi().getCameraId().equals("2")
		 && CameraUtil.getUnderWaterSupport(mApp.getActivity()) != 1){//bv wuyonglin add for tripe mode should hide more mode 20200731
            String moreModeName = mCameraActivity.getResources().getString(R.string.more_mode_name);
            mModeHolders.add(new ModeHolder(index+1, moreModeName,null,null));
        }
        CHorizontalScrollLayoutAdapter adapter = new CHorizontalScrollLayoutAdapter(mApp.getActivity(), mModeHolders,
                R.layout.century_mode_item);
        if (mScroller != null) {
            mScroller.setAdapter(adapter);
            if (mCurrentInx > (mModeHolders.size() - 1)){
                mCurrentInx = mModeHolders.size() - 1;
            }
            mScroller.setSelectIndex(mCurrentInx);
        }        
        mShutterView.setOnShutterButtonListener(mShutterButtonListener);
    }

    /**
     * Invoke the onShutterButtonCLicked listener to start a capture event.
     * @param currentPriority Trigger module shutter button listener priority,
     *                        the trigger event will be pass to the modules which shutter listener
     *                        priority is lower than currentPriority value.
     *                        The zero value will pass the click event to all listeners.
     */
    public void triggerShutterButtonClicked(final int currentPriority) {
        // shutter button may be trigger manually by mode or setting,
        // here should judge whether is enabled, if not enabled ignore this trigger.
        
        //*/ hct.huangfei, 20201027. getCsState
        if(mCameraActivity.getAppUi().getCsState()){
            LogHelper.e(TAG, "triggerShutterButtonClicked is on continuousshot");
            return;
        }
        //*/
        if (CameraUtil.isVideo_HDR_changing) {
            LogHelper.i(TAG, "triggerShutterButtonClicked video hdr changing return");
            return;
        }

        //bv wuyonglin add for bug6074 20210511 start
        if("Photo".equals(mCameraActivity.getAppUi().getCurrentMode()) || "Mono".equals(mCameraActivity.getAppUi().getCurrentMode())
                        || "AiworksFaceBeauty".equals(mCameraActivity.getAppUi().getCurrentMode()) || "Night".equals(mCameraActivity.getAppUi().getCurrentMode())
                        || "Pro".equals(mCameraActivity.getAppUi().getCurrentMode()) || "AiWorksBokeh".equals(mCameraActivity.getAppUi().getCurrentMode())
			|| "AiWorksBokehColor".equals(mCameraActivity.getAppUi().getCurrentMode())){
            LogHelper.i(TAG, "triggerShutterButtonClicked to setCaptureStatus currentPriority ="+currentPriority);
            mCameraActivity.getAppUi().setCaptureStatus(true);
            mCameraActivity.getAppUi().hideQuickSwitcherOption();
        }
        //bv wuyonglin add for bug6074 20210511 end

        //add by huangfei for watermark start
        if(Config.isWatermarkThumbnailClickLimited(mCameraActivity) && mCameraActivity.getAppUi().isWaterMarkOn()){ 
            mCaptureListener.onWaterMarkCaptureStart();
        }
        //add by huangfei for watermark end

        if (isEnabled() || currentPriority == FINGERPRINT_PRIORITY) {
            Iterator iterator = mShutterButtonListeners.entrySet().iterator();
            OnShutterButtonListener listener;
            while (iterator.hasNext()) {
                Map.Entry map = (Map.Entry) iterator.next();
                listener = (OnShutterButtonListener) map.getValue();
                int priority = mShutterButtonListeners.getPriorityByKey(
                        (String) map.getKey());
                if (priority > currentPriority
                        && listener != null
                        && listener.onShutterButtonClick()) {
                    return;
                }
            }
        }
    }
	 //add by Jerry
	 public void setChangIndex(int index){
	 	mChangIndex = index;
	 }
    /**
     * Invoke the triggerShutterButtonLongPressed listener to start a capture event.
     * @param currentPriority Trigger module shutter button listener priority,
     *                        the trigger event will be pass to the modules which shutter listener
     *                        priority is lower than currentPriority value.
     *                        The zero value will pass the click event to all listeners.
     */
    public void triggerShutterButtonLongPressed(final int currentPriority) {
        // shutter button may be trigger manually by mode or setting,
        // here should judge whether is enabled, if not enabled ignore this trigger.
        if (isEnabled()) {
            Iterator iterator = mShutterButtonListeners.entrySet().iterator();
            OnShutterButtonListener listener;
            while (iterator.hasNext()) {
                Map.Entry map = (Map.Entry) iterator.next();
                listener = (OnShutterButtonListener) map.getValue();
                int priority = mShutterButtonListeners.getPriorityByKey(
                        (String) map.getKey());
                if (priority > currentPriority
                        && listener != null
                        && listener.onShutterButtonLongPressed()) {
                    return;
                }
            }
        }
    }

    /**
     * Update current mode support shutter types.
     * @param currentType Current mode type.
     * @param types Support type list.
     */
    public void updateModeSupportType() {

        //add by huangfei for more mode start
        if(modePriority!=null && modePriority.length>0&&mScroller.getVisibility()==View.GONE && !mApp.getAppUi().getIsCustomBeautyViewShow()){
            mChangIndex = -1;
                }
        //add by huangfei for more mode end

        mShutterLayout.updateCurrentShutterIndex(mChangIndex);
        }

    //add by huangfei for defalut mode start
    public void updateModeSupportType(int index) {
        if(mChangIndex==index){
            return;
        } else {
            mChangIndex = index;
        }
        mShutterLayout.updateCurrentShutterIndex(mChangIndex);

    }

    /**
     * Update current mode shutter info.
     * @param type Current mode's shutter type.
     * @param drawable Current mode's shutter drawable.
     */
    public void updateCurrentModeShutter(String type, Drawable drawable) {
        if(drawable == null && (type == null || type.equals(mLastType))){
            return;
        }
        if(drawable != null){
            Drawable localDrawable = null;
            localDrawable = mApp.getActivity().getDrawable(
                    R.drawable.ic_slow_motion_shutter);
            mShutterView.setDrawable(localDrawable);
        } else {
            Drawable localDrawable = null;
            /* modify by liangchangwei 20201021 for new API changed begin */
            if("Video".equals(type)) {
                localDrawable = mApp.getActivity().getDrawable(
                                R.drawable.ic_shutter_video_normal);
            } else {
                localDrawable = mApp.getActivity().getDrawable(
                                R.drawable.ic_shutter_photo_normal);
            }
            /* modify by liangchangwei for new API changed end */
            mShutterView.setDrawable(localDrawable);
            if(localDrawable instanceof AnimatedVectorDrawable){
                LogHelper.i(TAG,"AnimatedVectorDrawable start" );
                ((AnimatedVectorDrawable)mShutterView.getDrawable()).start();
            }
            //mLastType = type;
        }
        mLastType = type;

    }

    /**
     * Get shutter root view.
     * @return the shutter root view.
     */
    public View getShutterRootView() {
        return mShutterLayout;
    }

    /**
     * Implementer of {@link OnShutterButtonListener}, receiver the UI event and notify the event
     * by the priority.
     */
    private class ShutterButtonListenerImpl implements ShutterButton.OnShutterButtonListener {

        @Override
        public void onShutterButtonFocused(boolean pressed) {
            Iterator iterator = mShutterButtonListeners.entrySet().iterator();
            OnShutterButtonListener listener;
            while (iterator.hasNext()) {
                Map.Entry map = (Map.Entry) iterator.next();
                listener = (OnShutterButtonListener) map.getValue();
                if (listener != null && listener.onShutterButtonFocus(pressed)) {
                    return;
                }
            }
        }

        @Override
        public void onShutterButtonClicked() {

        //*/ hct.huangfei, 20201027. getCsState
        if(mCameraActivity.getAppUi().getCsState()){
            LogHelper.e(TAG, "onShutterButtonClicked is on continuousshot");
            return;
        }
        //*/
            //add by huangfei for watermark start
            if(Config.isWatermarkThumbnailClickLimited(mCameraActivity) && mCameraActivity.getAppUi().isWaterMarkOn()){ 
                mCaptureListener.onWaterMarkCaptureStart();
            }
            //add by huangfei for watermark end

            //add by huangfei for continuousshot abnormal start
            if("Photo".equals(mCameraActivity.getAppUi().getCurrentMode())){
                mCameraActivity.getAppUi().setCaptureStatus(true);
            }
            //add by huangfei for continuousshot abnormal end

            Iterator iterator = mShutterButtonListeners.entrySet().iterator();
            OnShutterButtonListener listener;
            while (iterator.hasNext()) {
                Map.Entry map = (Map.Entry) iterator.next();
                listener = (OnShutterButtonListener) map.getValue();
                if (listener != null && listener.onShutterButtonClick()) {
                    return;
                }
            }
        }

        @Override
        public void onShutterButtonLongPressed() {

            //add by huangfei for continuousshot abnormal start
            if(mCameraActivity.getAppUi().getCaptureStatus()){
                LogHelper.e(TAG, "onShutterButtonLongPressed is on captureing");
                return;
            }
            //add by huangfei for continuousshot abnormal end   

            Iterator iterator = mShutterButtonListeners.entrySet().iterator();
            OnShutterButtonListener listener;
            while (iterator.hasNext()) {
                Map.Entry map = (Map.Entry) iterator.next();
                listener = (OnShutterButtonListener) map.getValue();
                if (listener != null && listener.onShutterButtonLongPressed()) {
                    return;
                }
            }
        }
    }
    private static final AtomicInteger sNextGenerateId = new AtomicInteger(1);

    private static int generateViewId() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            for (; ; ) {
                final int result = sNextGenerateId.get();
                //aapt-generated IDs have the high byte nonzero; clamp to the range under that.
                int newValue = result + 1;
                if (newValue > 0x00FFFFFF) {
                    newValue = 1; //Roll over to 1, not 0.
                }
                if (sNextGenerateId.compareAndSet(result, newValue)) {
                    return result;
                }
            }
        } else {
            return View.generateViewId();
        }
    }
    public boolean getModeChangeStatus(){
        return  mModeChangeStart;
    }
    //add by huangfei for shutter button enable start
    public void setShutterButtonEnable(boolean enable){
        mShutterEnable = enable;
        if(enable){
            setEnabled(enable);
        }
    }
    //add by huangfei for shutter button enable end
    public static class ModeHolder {
        private int index;
        private String mShutterName;
        private String mShutterType;
        private String mShutterModeName;
        ModeHolder(int inx, String shutterName,String shutterType,String shutterModeName) {
            this.index = inx;
            this.mShutterName = shutterName;
            this.mShutterType = shutterType;            
			this.mShutterModeName = shutterModeName;
        }
        public String getShutterType() {
            return mShutterType;
        }
        public String getShutterName() {
            return mShutterName;
        }
        public String getShutterModeName() {
            return mShutterModeName;
        }
        public int getInx() {
            return index;
        }
    }
    CHorizontalScrollView.OnItemClickListener mOnItemClickListener = new CHorizontalScrollView.OnItemClickListener() {
        @Override
        public void onItemClick(int index) {
            if(mCameraActivity.getAppUi().getCsState()){
                LogHelper.e(TAG, "CHorizontalScrollView is not available");
                return;
            }
            //bv wuyonglin add for bug3751 20200202 start
            if(CameraUtil.isVideo_HDR_changing){
                LogHelper.i(TAG, "[onItemClick], don't do mode change for when isVideo_HDR_changing = true " );
                return;
            }
            //bv wuyonglin add for bug3751 20200202 end
            if (mScroller != null&&mCurrentInx!=index && mScroller.getSwitchEnable()) {
                ModeHolder modeHolder = mModeHolders.get(index);
                onShutterChangedStart(modeHolder.getShutterModeName(),modeHolder.getShutterType(),index);
                mShutterLayout.setModeIndex(index);
            }
        }
    };
    CHorizontalScrollView.OnFlingListener mOnFlingListener = new CHorizontalScrollView.OnFlingListener() {
        @Override
        public void doFling(int direction) {
        }
    };
    public List<ModeHolder> getModeItems(){
        return mModeHolders;
    }
    public CHorizontalScrollView getCHorizontalScrollView(){
        return mScroller;
    } 
    public void doShutterButtonFocused(){
        mShutterButtonListener.onShutterButtonFocused(false);
    }
    public void onModeSelectByMore(boolean show,String modeName,String modetype,int index){
        mCameraActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(show){
                    showMoreMode = true;
                    mShowMoreModeView = false;
                    mApp.getAppUi().applyAllUIVisibility(View.VISIBLE);
                    mFragment.setVisibility(View.GONE);
                    mShutterRoot.setBackgroundColor(mApp.getActivity().getResources().getColor(android.R.color.transparent));
                    mModeTextView.setVisibility(View.VISIBLE);
                    mModeTextView.setText(modeName);
                    mScroller.setVisibility(View.GONE);
                }else{
                    mModeTextView.setVisibility(View.GONE);
                    mScroller.setVisibility(View.VISIBLE);
                    onShutterChangedStart(modeName,modetype,index);
                    mShutterLayout.setModeIndex(index);                    
                    mApp.getAppUi().getModePickerManager().resetCurrentPriority();
                    showMoreMode = false;

                    //*/ hct.huangfei, 20201206.hdr view int topbar.
                    if(Config.isHdrInTopBarSupprot(mCameraActivity) && mApp.getAppUi().getHdrManager()!= null){
                        mApp.getAppUi().getHdrManager().exitHdrMode();
                    }
                    //*/
                }
            }
        });
    }
    private View.OnClickListener mExitModeClickListener = new View.OnClickListener() {
        public void onClick(View view) {
            if(!mExitMoreModeEnable){
                return;
            }
            onModeSelectByMore(false,photoModeName,photoModeType,photoIndex);
        }
    };
    private boolean isThirdPartyIntent() {
        Activity activity = mApp.getActivity();
        Intent intent = activity.getIntent();
        String action = intent.getAction();
        boolean value = MediaStore.ACTION_IMAGE_CAPTURE.equals(action) ||
                //bv wuyonglin add for cts verifier 20210513 start
                MediaStore.ACTION_IMAGE_CAPTURE_SECURE.equals(action) ||
                //bv wuyonglin add for cts verifier 20210513 end
                MediaStore.ACTION_VIDEO_CAPTURE.equals(action);
        return value;
    }
    public void showMoreModeText(boolean visible){
        if(mModeTextView==null){
            return;
        }
        mApp.getActivity().runOnUiThread(new Runnable() {
            public void run() {
        if(showMoreMode&&visible){
            mModeTextView.setVisibility(View.VISIBLE);
        }else{
            mModeTextView.setVisibility(View.GONE);
        }
	    }
	});
    }
    public void moreModeTextEnabled(boolean enabled){
        mExitMoreModeEnable = enabled;
    }
    //*/ hct.huangfei, 20201206.hdr view int topbar.
    public void exitHdrMode(){
        onModeSelectByMore(false,photoModeName,photoModeType,photoIndex);
    }
    //*/

    //bv liangchangwei add for shutter progressbar
    public void showCircleProgressBar() {
        mShutterView.showCircleProgressBar();
        //mApp.getAppUi().CameraSwitheronConfigUIEnabled(false);
        setEnabled(false);
        isCanSwitchMode = false;
    }

    public void hideCircleProgressBar() {
        mShutterView.hideCircleProgressBar();
        //mApp.getAppUi().CameraSwitheronConfigUIEnabled(true);
        isCanSwitchMode = true;
        setEnabled(true);
    }

    public void snapToNextShutter() {
        mShutterLayout.snapToNextShutter();
    }

    public boolean getIsShowMoreModeView() {
        return mShowMoreModeView;
    }

    public boolean getIsInMoreModeView() {
        return showMoreMode;
    }

    public void goToPhotoMode() {
        onModeSelectByMore(false,photoModeName,photoModeType,photoIndex);
    }

    public boolean getIsCanSwitchMode() {
        return isCanSwitchMode;
    }
}
