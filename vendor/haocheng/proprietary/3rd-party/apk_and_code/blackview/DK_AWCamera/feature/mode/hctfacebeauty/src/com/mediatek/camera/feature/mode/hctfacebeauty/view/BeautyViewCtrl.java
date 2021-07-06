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
package com.mediatek.camera.feature.mode.hctfacebeauty.view;

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
import com.mediatek.camera.feature.mode.hctfacebeauty.view.CRotateTextView;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.graphics.drawable.Drawable;
import com.mediatek.camera.CameraActivity;

/**
 * The sdof view manager.
 */
public class BeautyViewCtrl implements OnClickListener {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(BeautyViewCtrl.class.getSimpleName());
    // Stereo Photo warning message
    //Gesture and View Control
    private static final int SHOW_INFO_LENGTH_LONG = 5 * 1000;
    private static final int GYBEAUTY_VIEW_INIT_AND_SHOW = 1;
    private static final int GYBEAUTY_VIEW_UNINIT = 2;
    private ViewGroup mRootViewGroup;
    private IApp mApp;
    private MainHandler mMainHandler;
    private ViewChangeListener mViewChangeListener;
    private int seekbarmax = 150;
    private Handler mHandler;
    private View mView;
	private View mFaceBeautyOneKeyLayout;
	private View mFaceBeautyCustomLayout;
	private View mFaceBeautyOnekeyBtn;
	private View mFaceBeautyCustomBtn;
	private TextView mFaceBeautySwitchBtn;
	private CHSeekBar mOneKeyCHSeekBar;
    private CHSeekBar mCustomCHSeekBar;
    private final static int DEFALUT_FACEBEAUTY_VALUE = 5;
	private final static int DEFALUT_SMOOTH_VALUE = 5;
	private final static int DEFALUT_RUDDY_VALUE = 5;
	private final static int DEFALUT_SLENDER_VALUE = 5;
    private final static int DEFALUT_LIGHTEN_VALUE = 5;
    private int mFaceBeautyValue = DEFALUT_FACEBEAUTY_VALUE;
	private int mSmoothValue = DEFALUT_SMOOTH_VALUE;
	private int mRuddyValue = DEFALUT_RUDDY_VALUE ;
	private int mSlenderValue = DEFALUT_SLENDER_VALUE;
    private int mLightenValue = DEFALUT_LIGHTEN_VALUE;
    private static final int NUMBER_FACE_BEAUTY_ICON = 4;

    private final static int CUSTOM_TYPE_FACE_SMOOTH = 0;
	private final static int CUSTOM_TYPE_FACE_RUDDY = 1;
	private final static int CUSTOM_TYPE_FACE_SLENDER = 2;
	private final static int CUSTOM_TYPE_FACE_LIGHTEN = 3;

    private int mCustomBeautyType = -1;// when value 10  is  Onekey Beauty other is  CUSTOM_TYPE
    private int mBeautyType = 1;//0 is cuctombeauty 1 is onekey beauty
	private CRotateTextView[] mFaceBeautyImageViews = new CRotateTextView[NUMBER_FACE_BEAUTY_ICON];

    private static final int[] FACE_BEAUTY_ICONS_NORMAL = new int[NUMBER_FACE_BEAUTY_ICON];
    private static final int[] FACE_BEAUTY_ICONS_HIGHTLIGHT = new int[NUMBER_FACE_BEAUTY_ICON];
    static {
        FACE_BEAUTY_ICONS_NORMAL[CUSTOM_TYPE_FACE_SMOOTH] = R.drawable.century_ic_facebeauty_buffing_normal;
        FACE_BEAUTY_ICONS_NORMAL[CUSTOM_TYPE_FACE_LIGHTEN] = R.drawable.century_ic_facebeauty_whitening_normal;
        FACE_BEAUTY_ICONS_NORMAL[CUSTOM_TYPE_FACE_RUDDY] = R.drawable.century_ic_facebeauty_ruddy_normal;
        FACE_BEAUTY_ICONS_NORMAL[CUSTOM_TYPE_FACE_SLENDER] = R.drawable.century_ic_facebeauty_thin_normal;
    }

    static {
        FACE_BEAUTY_ICONS_HIGHTLIGHT[CUSTOM_TYPE_FACE_SMOOTH] = R.drawable.century_ic_facebeauty_buffing_pressed;
        FACE_BEAUTY_ICONS_HIGHTLIGHT[CUSTOM_TYPE_FACE_LIGHTEN] = R.drawable.century_ic_facebeauty_whitening_pressed;
        FACE_BEAUTY_ICONS_HIGHTLIGHT[CUSTOM_TYPE_FACE_RUDDY] = R.drawable.century_ic_facebeauty_ruddy_pressed;
        FACE_BEAUTY_ICONS_HIGHTLIGHT[CUSTOM_TYPE_FACE_SLENDER] = R.drawable.century_ic_facebeauty_thin_pressed;
    }

    private int mCustomProgress = 0;

    private IAppUi.HintInfo mGuideHint;
    private CameraActivity mCameraActvity;
    private View mBeautyofLayout;
    private SeekBar mBeautySeekbarLayout;
    private RelativeLayout mControl;
    ICameraContext mICameraContext;
    private DataStore mDataStore;
    //add by huangfei for disconnect camear start
    private int mProgress;
    //add by huangfei for disconnect camear end

    private boolean mOnlyShowCustomSeekbar = false;

    public BeautyViewCtrl( ICameraContext context){
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
        mMainHandler.sendEmptyMessage(GYBEAUTY_VIEW_INIT_AND_SHOW);
        mCameraActvity = (CameraActivity)app.getActivity();
        mOnlyShowCustomSeekbar = mCameraActvity.getResources().getBoolean(R.bool.congfig_only_show_custom_beautyseekbar);
    }

    /**
     * To destroy the zoom view.
     */
    public void unInit() {
        mMainHandler.sendEmptyMessage(GYBEAUTY_VIEW_UNINIT);
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
         * This method used for notify mode gybeauty level.
         * @param progress Beauty  level
         */
        public void onBeautyLevelChanged(int smooth,int lighten,int ruddy,int slender);

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
                case GYBEAUTY_VIEW_INIT_AND_SHOW:
                    initView();
                    break;
                case GYBEAUTY_VIEW_UNINIT:
                    unInitView();
                    break;
                default:
                    break;
            }
        }
    }

    private void initView() {
        mRootViewGroup = mApp.getAppUi().getModeRootView();
        mBeautyofLayout = (RelativeLayout) mApp.getActivity().getLayoutInflater()
                .inflate(R.layout.century_facebeauty,
                mRootViewGroup, false).findViewById(R.id.beauty_rotate_layout);
		mFaceBeautyOneKeyLayout = mBeautyofLayout.findViewById(R.id.century_facebeauty_onekey);
		mFaceBeautyCustomLayout = mBeautyofLayout.findViewById(R.id.century_facebeauty_custom);
		mFaceBeautyCustomBtn = mBeautyofLayout.findViewById(R.id.century_facebeauty_custom_btn);
		mFaceBeautyOnekeyBtn = mBeautyofLayout.findViewById(R.id.century_facebeauty_onekey_btn);
		mFaceBeautySwitchBtn = (TextView)mBeautyofLayout.findViewById(R.id.century_facebeauty_switch_btn);
		mFaceBeautyImageViews[CUSTOM_TYPE_FACE_SMOOTH] = (CRotateTextView)mBeautyofLayout.findViewById(R.id.century_facebeauty_buffing_btn);
	 	mFaceBeautyImageViews[CUSTOM_TYPE_FACE_LIGHTEN] = (CRotateTextView)mBeautyofLayout.findViewById(R.id.century_facebeauty_whitening_btn);
		mFaceBeautyImageViews[CUSTOM_TYPE_FACE_RUDDY] = (CRotateTextView)mBeautyofLayout.findViewById(R.id.century_facebeauty_ruddy_btn);
		mFaceBeautyImageViews[CUSTOM_TYPE_FACE_SLENDER] = (CRotateTextView)mBeautyofLayout.findViewById(R.id.century_facebeauty_thin_btn);

		
		mOneKeyCHSeekBar = (CHSeekBar)mBeautyofLayout.findViewById(R.id.century_facebeauty_onekey_seekbar);	
		mCustomCHSeekBar = (CHSeekBar)mBeautyofLayout.findViewById(R.id.century_facebeauty_custom_seekbar);
        mRootViewGroup.addView(mBeautyofLayout);
        if(mBeautyType==0){
			for (int i = NUMBER_FACE_BEAUTY_ICON-1; i >=0 ; i--) {
				initCustomSeekBar(i);
			}
        }
        applyListeners();
        if(!mOnlyShowCustomSeekbar){
            showViews();
        }else{
            showCustomLayoutView();
        }
    }

    private void unInitView() {
        reset();
        mRootViewGroup.removeView(mBeautyofLayout);
        mBeautySeekbarLayout = null;
        mBeautyofLayout = null;
    }

    private void reset() {

    }  

  
    private int dpToPixel(int dp) {
        float scale = mApp.getActivity().getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
 
    public void onClick(View view) {
		if (view == mFaceBeautyOnekeyBtn){
			mFaceBeautyOneKeyLayout.setVisibility(View.VISIBLE);
			mFaceBeautyCustomLayout.setVisibility(View.GONE);
			mFaceBeautySwitchBtn.setSelected(false);
			mFaceBeautySwitchBtn.setText(R.string.century_facebeauty_onekey);			
			mFaceBeautyOnekeyBtn.setVisibility(View.GONE);
			mFaceBeautyCustomBtn.setVisibility(View.VISIBLE);
			mCustomBeautyType  = 10;
			
		}else if (view == mFaceBeautyCustomBtn){
			mFaceBeautyOneKeyLayout.setVisibility(View.GONE);
			mFaceBeautyCustomLayout.setVisibility(View.VISIBLE);
			mFaceBeautySwitchBtn.setSelected(false);
			mFaceBeautySwitchBtn.setText(R.string.century_facebeauty_custom);
			
			mFaceBeautyOnekeyBtn.setVisibility(View.VISIBLE);
			mFaceBeautyCustomBtn.setVisibility(View.GONE);
			
		}else if (view == mFaceBeautySwitchBtn){
			if(mBeautyType==0){
                  mFaceBeautyOneKeyLayout.setVisibility(View.GONE);
                  mFaceBeautyCustomLayout.setVisibility(View.VISIBLE);
                  //mFaceBeautySwitchBtn.setSelected(false);
                  mFaceBeautySwitchBtn.setText(R.string.century_facebeauty_custom);
                  
                  mFaceBeautyOnekeyBtn.setVisibility(View.GONE);
                  mFaceBeautyCustomBtn.setVisibility(View.GONE);
				  mBeautyType=1;
			}else{
				mFaceBeautyOneKeyLayout.setVisibility(View.VISIBLE);
				mFaceBeautyCustomLayout.setVisibility(View.GONE);
				//mFaceBeautySwitchBtn.setSelected(false);
				mFaceBeautySwitchBtn.setText(R.string.century_facebeauty_onekey);			
				mFaceBeautyOnekeyBtn.setVisibility(View.GONE);
				mFaceBeautyCustomBtn.setVisibility(View.GONE);
				mCustomBeautyType  = 10;
				mBeautyType=0;
                mOneKeyCHSeekBar.setProgress(mFaceBeautyValue);
			}
		}else{
			for (int i = 0; i < NUMBER_FACE_BEAUTY_ICON; i++) {
				if (mFaceBeautyImageViews[i] == view) {
					initCustomSeekBar(i);
					break;
				}
			}
		}
        //add by huanfei for adjust layout start
        //reLayoutSeekbar();
        //add by huanfei for adjust layout end
    }
    private void showViews(){
		mFaceBeautyCustomBtn.setVisibility(View.VISIBLE);
		mFaceBeautyOnekeyBtn.setVisibility(View.GONE);
		if (mBeautyType==0){
			mFaceBeautyOneKeyLayout.setVisibility(View.GONE);
			mFaceBeautyCustomLayout.setVisibility(View.VISIBLE);
			mFaceBeautySwitchBtn.setVisibility(View.GONE);
		}else{
			mCustomBeautyType  = 10;
			mFaceBeautyCustomLayout.setVisibility(View.GONE);
			mFaceBeautySwitchBtn.setVisibility(View.GONE);
			mFaceBeautyOneKeyLayout.setVisibility(View.VISIBLE);
		}
	}
    
    private void showCustomLayoutView(){
        mFaceBeautyCustomLayout.setVisibility(View.VISIBLE);
    }

	private void resetFaceBeautyParamters(){
		mFaceBeautyValue = DEFALUT_FACEBEAUTY_VALUE;
		mSmoothValue = DEFALUT_SMOOTH_VALUE;
		mRuddyValue = DEFALUT_RUDDY_VALUE;
		mSlenderValue = DEFALUT_SLENDER_VALUE;
    }
	private void initCustomSeekBar(int type){
        mCustomBeautyType = type;
		mCustomCHSeekBar.setVisibility(View.VISIBLE);
        for (int i = 0; i < NUMBER_FACE_BEAUTY_ICON; i++) {
            if (type == i) {
				Drawable drawable = mCameraActvity.getResources().getDrawable(FACE_BEAUTY_ICONS_HIGHTLIGHT[i]);  
			    drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
		    	mFaceBeautyImageViews[i].setCompoundDrawables(null, drawable, null, null);
			    mFaceBeautyImageViews[i].setSelected(true);
            } else {
			 	Drawable drawable = mCameraActvity.getResources().getDrawable(FACE_BEAUTY_ICONS_NORMAL[i]);  
		 		drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());  
			 	mFaceBeautyImageViews[i].setCompoundDrawables(null, drawable, null, null);
	      	    mFaceBeautyImageViews[i].setSelected(false);
            }
        }
		if (type == CUSTOM_TYPE_FACE_SMOOTH){      
			 mCustomCHSeekBar.setProgress(mSmoothValue);
		}else if (type == CUSTOM_TYPE_FACE_RUDDY){
		     mCustomCHSeekBar.setProgress(mRuddyValue);
		}else if (type == CUSTOM_TYPE_FACE_SLENDER){ 
		     mCustomCHSeekBar.setProgress(mSlenderValue);
		}else if (type == CUSTOM_TYPE_FACE_LIGHTEN){
		     mCustomCHSeekBar.setProgress(mLightenValue);
        }
        mViewChangeListener.onBeautyLevelChanged( mSmoothValue,mLightenValue, mRuddyValue, mSlenderValue);
	}
    private void applyListeners() {

		mFaceBeautyOnekeyBtn.setOnClickListener(this);
		mFaceBeautyCustomBtn.setOnClickListener(this);
		mFaceBeautySwitchBtn.setOnClickListener(this);
		for (int i = 0; i < NUMBER_FACE_BEAUTY_ICON; i++) {
            if (null != mFaceBeautyImageViews[i]) {
                mFaceBeautyImageViews[i].setOnClickListener(this);
            }
        }
		mOneKeyCHSeekBar.setMax(9);
		mOneKeyCHSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
		{
			@Override
			public void onStopTrackingTouch(SeekBar seekBar)
			{
                mViewChangeListener.onBeautyLevelChanged(mFaceBeautyValue, mFaceBeautyValue, 0, 0);
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar)
			{
                //
			}
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser)
			{
                mOneKeyCHSeekBar.setSeekBarText("" + progress);
                mFaceBeautyValue = progress;
			}
		});
        mOneKeyCHSeekBar.setProgress((int)(mFaceBeautyValue));
        mViewChangeListener.onBeautyLevelChanged(mFaceBeautyValue, mFaceBeautyValue, 0, 0);
		mCustomCHSeekBar.setMax(9);
		mCustomCHSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
		{
			@Override
			public void onStopTrackingTouch(SeekBar seekBar)
			{
                mViewChangeListener.onBeautyLevelChanged( mSmoothValue,mLightenValue, mRuddyValue, mSlenderValue);
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar)
			{
				
			}
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser)
			{
				mCustomCHSeekBar.setSeekBarText("" + progress);
				if (mCustomBeautyType == CUSTOM_TYPE_FACE_SMOOTH) {
                    mSmoothValue = progress;
				} else if (mCustomBeautyType == CUSTOM_TYPE_FACE_LIGHTEN) {
                    mLightenValue = progress;
				} else if (mCustomBeautyType == CUSTOM_TYPE_FACE_RUDDY) {
                    mRuddyValue = progress;
				} else if (mCustomBeautyType == CUSTOM_TYPE_FACE_SLENDER) {
                    mSlenderValue = progress;
                }
                mCustomProgress = progress;
			}
        });
        mCustomCHSeekBar.setProgress(mCustomProgress);

    }
}
