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
package com.mediatek.camera.ui.modepicker;


import android.app.Fragment;
import android.os.Bundle;
import javax.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.mediatek.camera.R;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.utils.CameraUtil;

import java.util.List;
import android.widget.FrameLayout;
import android.content.Context;
import android.util.AttributeSet;
import android.content.ContextWrapper;
import android.app.Activity;
import androidx.annotation.NonNull;
import android.graphics.Rect;
import com.mediatek.camera.CameraActivity;
//bv wuyonglin add for screen 2300px adjust all icon position 20201026 start
import android.view.WindowManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.content.Context;
import android.widget.RelativeLayout;
//bv wuyonglin add for screen 2300px adjust all icon position 20201026 end

/**
 *  Mode picker fragment, it will show the current support mode list.
 */

public class ModePickerFrameLayout extends FrameLayout {
    /**
     * Listener to listen mode fragment's state.
     */
    public interface StateListener {
        /**
         * Callback when setting fragment is created.
         */
        void onCreate();

        /**
         * Callback when setting fragment is resumed.
         */
        void onResume();

        /**
         * Callback when setting fragment is paused.
         */
        void onPause();

        /**
         * Callback when setting fragment is destroyed.
         */
        void onDestroy();
    }

    /**
     * Mode selected listener.
     */
    public interface OnModeSelectedListener {
        /**
         * When a mode is selected,notify the event.
         * @param modeInfo @param modeInfo Selected mode info.
         * @return Is the mode change success.
         */
        boolean onModeSelected(ModePickerManager.ModeInfo modeInfo);
    }
    private static final LogUtil.Tag TAG =
            new LogUtil.Tag(ModePickerFrameLayout.class.getSimpleName());
    /* add  by liangchangwei for Detai Fragment View begin -- */
    private ImageView mDetailView;
    private View.OnClickListener mDetailClickedListener;
    /* add  by liangchangwei for Detai Fragment View end-- */
    private RecyclerView mRecyclerView;
    private ModeItemAdapter mAdapter;
    private StateListener mStateListener;
    private View.OnClickListener mSettingClickedListener;
    private OnModeSelectedListener mModeSelectedListener;
    private List<ModePickerManager.ModeInfo> mModeList;
    private String mCurrentModeName;
    private int mOrientation;
    private int mSettingVisibility;
    private boolean mIsClickEnabled = true;
    private Context mContext;
    private OnFlingListener mOnFlingListener;
    private float mDownX = -1f;
    private float mMoveX = -1f;
    private int dis = 0;
    //bv wuyonglin add for screen 2300px adjust all icon position 20201026 start
    private int mScreenHeight;
    //bv wuyonglin add for screen 2300px adjust all icon position 20201026 end

    public ModePickerFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
	mContext = context;
	android.util.Log.d("geek","ModePickerFrameLayout mContext ="+mContext);
        //bv wuyonglin add for screen 2300px adjust all icon position 20201026 start
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        display.getRealMetrics(dm);
        mScreenHeight = dm.heightPixels;
        //bv wuyonglin add for screen 2300px adjust all icon position 20201026 end
    }

    public interface OnFlingListener {
	public void doFling();
    }

    public void setOnFlingListener(OnFlingListener listener){
	mOnFlingListener = listener;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        //int compensateOri = CameraUtil.calculateRotateLayoutCompensate(
        //        getContext());
        //CameraUtil.rotateViewOrientation(view, compensateOri, false);                         
		android.util.Log.d("geek","ModePickerFrameLayout onFinishInflate");
        mRecyclerView = (RecyclerView) findViewById(R.id.mode_list);
        mAdapter = new ModeItemAdapter(getContext(), new OnViewItemClickListenerImpl());
        mAdapter.updateCurrentModeName(mCurrentModeName);
        mAdapter.setModeList(mModeList);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new HorizontalItemDecoration(3,mContext));
        mRecyclerView.addOnChildAttachStateChangeListener(
                new RecyclerView.OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(View view) {
                if (mRecyclerView != null) {
                    CameraUtil.rotateRotateLayoutChildView(findActivity(mContext), mRecyclerView,
                            mOrientation, false);
                }
            }

            @Override
            public void onChildViewDetachedFromWindow(View view) {

            }
        });
        mRecyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                android.util.Log.d("geek","mRecyclerView onInterceptTouchEvent e="+e);
                switch (e.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        mDownX = e.getX();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        break;
                    case MotionEvent.ACTION_UP:
                        mMoveX = e.getX();
                        dis = (int) (mMoveX - mDownX);
                        android.util.Log.d("geek","mRecyclerView onInterceptTouchEvent mDownX="+mDownX+" mMoveX ="+mMoveX+" dis ="+dis);
                        if (dis > 50) {
                        mOnFlingListener.doFling();
                        }
                        break;
                    default:
                        break;
                }
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {
		        android.util.Log.d("geek","mRecyclerView onTouchEvent e="+e);
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
		        android.util.Log.d("geek","onRequestDisallowInterceptTouchEvent disallowIntercept="+disallowIntercept);
            }
        });

        /* add  by liangchangwei for Detai Fragment View begin -- */
        mDetailView = (ImageView)findViewById(R.id.detail);
        mDetailView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                LogHelper.i(TAG,"mDetailView onClick");
                if(mDetailClickedListener != null){
                    mDetailClickedListener.onClick(view);
                }
            }
        });
        /* add  by liangchangwei for Detai Fragment View end -- */

        RelativeLayout mMainLinearLayout = (RelativeLayout) findViewById(R.id.mode_fragment_root_rl);
		//bv wuyonglin add for screen 2300px adjust all icon position 20201026 start
		if (mScreenHeight == 2300) {
	            ViewGroup.LayoutParams params = mMainLinearLayout.getLayoutParams();
	            params.height = mContext.getResources().getDimensionPixelSize(R.dimen.mode_fragment_root_rl_height_2300px);
	            mMainLinearLayout.setLayoutParams(params);
		} else if (mScreenHeight == 2400) {
	            ViewGroup.LayoutParams params = mMainLinearLayout.getLayoutParams();
	            params.height = mContext.getResources().getDimensionPixelSize(R.dimen.mode_fragment_root_rl_height_2400px);
	            mMainLinearLayout.setLayoutParams(params);
		}
		//bv wuyonglin add for screen 2300px adjust all icon position 20201026 end
        mMainLinearLayout.setClickable(true);
        mMainLinearLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                android.util.Log.d("geek","mMainLinearLayout onTouch event ="+event+" v ="+v);
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        mDownX = event.getX();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        break;
                    case MotionEvent.ACTION_UP:
                        mMoveX = event.getX();
                        dis = (int) (mMoveX - mDownX);
                        android.util.Log.d("geek","mMainLinearLayout  mDownX="+mDownX+" mMoveX ="+mMoveX+" dis ="+dis);
                        if (dis > 50) {
                        mOnFlingListener.doFling();
                        }
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
    }

    /*public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.mode_fragment, container, false);

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIsClickEnabled = true;
        if (mStateListener != null) {
            mStateListener.onCreate();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mStateListener != null) {
            mStateListener.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mStateListener != null) {
            mStateListener.onPause();
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mAdapter = null;
        if (mStateListener != null) {
            mStateListener.onDestroy();
        }
    }*/

    /*@Override
    public void onOrientationChanged(int orientation) {
        mOrientation = orientation;
	android.util.Log.d("geek","onOrientationChanged mOrientation ="+mOrientation+" getContext() ="+getContext()+" mRecyclerView ="+mRecyclerView);
        if (mRecyclerView != null && getContext() != null) {
            CameraUtil.rotateRotateLayoutChildView(findActivity(mContext), mRecyclerView,
                    orientation, true);
        }
    }*/

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ((CameraActivity)findActivity(mContext)).registerOnOrientationChangeListener(mOrientationListener);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ((CameraActivity)findActivity(mContext)).unregisterOnOrientationChangeListener(mOrientationListener);
    }

    private IApp.OnOrientationChangeListener mOrientationListener =
            new IApp.OnOrientationChangeListener() {
                @Override
                public void onOrientationChanged(int orientation) {
                    mOrientation = orientation;
                    if (mRecyclerView != null && getContext() != null) {
                        CameraUtil.rotateRotateLayoutChildView(findActivity(mContext), mRecyclerView,orientation, true);
                    }
                }
            };

    /**
     * Refresh current mode list to be shown in the list.
     * @param modeList Current mode list.
     */
    public void refreshModeList(List<ModePickerManager.ModeInfo> modeList) {
        mModeList = modeList;
        if (mAdapter != null) {
            mAdapter.setModeList(modeList);
        }
    }

    /**
     *  Set fragment state change listener.
     * @param listener State listener.
     */
    public void setStateListener(StateListener listener) {
        mStateListener = listener;
    }

    /**
     * Update current running mode name.
     * @param modeName Current mode name string.
     */

    public void updateCurrentModeName(String modeName) {
        mCurrentModeName  = modeName;
    }

    public void setSettingClickedListener(View.OnClickListener listener) {
        mSettingClickedListener = listener;
    }

    /* add  by liangchangwei for Detai Fragment View begin -- */
    public void setDetailClickedListener(View.OnClickListener listener){
        mDetailClickedListener = listener;
    }
    /* add  by liangchangwei for Detai Fragment View end -- */

    public void setModeSelectedListener(OnModeSelectedListener listener) {
        mModeSelectedListener = listener;
    }

    /**
     * Set the setting icon visible or not.
     *
     * @param visible True means setting icon is visible, otherwise it is invisible.
     */
    public void setSettingIconVisible(boolean visible) {
        mSettingVisibility = visible ? View.VISIBLE : View.GONE;
    }

    /**
     * set the ui whether chickable.
     * @param enabled state.
     */
    public void setEnabled(boolean enabled) {
        mIsClickEnabled = enabled;
    }
    /**
     * OnViewItemClickListener implement.
     */
    private class OnViewItemClickListenerImpl implements ModeItemAdapter.OnViewItemClickListener {

        @Override
        public boolean onItemCLicked(ModePickerManager.ModeInfo modeInfo) {
            //ModePickerFragment.this.getContext().getFragmentManager().popBackStack();
	        android.util.Log.d("geek","onItemCLicked modeInfo ="+modeInfo+" mIsClickEnabled ="+mIsClickEnabled);
            if (!mIsClickEnabled) {
                return false;
            }
            if (!modeInfo.mName.equals("") && mModeSelectedListener.onModeSelected(modeInfo)) {
                return true;
            } else {
                return false;
            }
        }
    }

    public Activity findActivity(Context context) {
        if (context instanceof Activity) {
            return (Activity) context;
        }
        if (context instanceof ContextWrapper) {
            ContextWrapper wrapper = (ContextWrapper) context;
            return findActivity(wrapper.getBaseContext());
        } else {
            return null;
        }
    }

/**
 * 定义垂直方向的距离
 */
public class HorizontalItemDecoration extends RecyclerView.ItemDecoration {
    private int space;//定义2个Item之间的距离

    public HorizontalItemDecoration(int space, Context mContext) {
        this.space = dip2px(space,mContext);
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        int totalCount = parent.getAdapter().getItemCount();
        outRect.top = space;
    }

    public int dip2px(float dpValue,Context context) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale);
    }
}
}
