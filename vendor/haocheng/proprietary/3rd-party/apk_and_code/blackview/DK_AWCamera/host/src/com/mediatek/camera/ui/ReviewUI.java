/*
 *   Copyright Statement:
 *
 *     This software/firmware and related documentation ("MediaTek Software") are
 *     protected under relevant copyright laws. The information contained herein is
 *     confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 *     the prior written permission of MediaTek inc. and/or its licensors, any
 *     reproduction, modification, use or disclosure of MediaTek Software, and
 *     information contained herein, in whole or in part, shall be strictly
 *     prohibited.
 *
 *     MediaTek Inc. (C) 2016. All rights reserved.
 *
 *     BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 *    THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 *     RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 *     ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 *     WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 *     WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 *     NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 *     RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 *     INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 *     TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 *     RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 *     OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 *     SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 *     RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 *     STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 *     ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 *     RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 *     MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 *     CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 *     The following software/firmware and/or related documentation ("MediaTek
 *     Software") have been modified by MediaTek Inc. All revisions are subject to
 *     any receiver's applicable license agreements with MediaTek Inc.
 */
package com.mediatek.camera.ui;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.mediatek.camera.R;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil.Tag;
import com.mediatek.camera.common.mode.IReviewUI;
import com.mediatek.camera.common.utils.CameraUtil;
//bv wuyonglin add for screen 2300px adjust all icon position 20201026 start
import android.view.WindowManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.content.Context;
//bv wuyonglin add for screen 2300px adjust all icon position 20201026 end
import android.view.DisplayInfo;

/**
 * ReviewUI used to show review after 3th use camera capture or recording.
 */

public class ReviewUI implements IReviewUI {
    private static final Tag TAG = new Tag(ReviewUI.class.getSimpleName());
    private static final int ORIENTATION_270 = 270;
    private static final int ORIENTATION_180 = 180;
    private static final int ORIENTATION_90 = 90;
    private static final int ORIENTATION_0 = 0;
    //add description for the cts case MediaStoreUiTest#testImageCapture
    private static final String SAVE_DONE_DESCRIPTION = "Done";
    private int mOrientation;
    private View mReviewRootView = null;
    private ViewGroup mParentViewGroup;
    private ImageView mReviewImage;
    private ImageView mRetakeButton;
    private ImageView mSaveButton;
    private ImageView mPlayButton;
    private Activity mActivity;
    private IApp mApp;
    private Bitmap mBitmap;
    private ReviewSpec mReviewSpec;
    //bv wuyonglin add for adjust third app open camera ui 20200930 start
    private ImageView mCancelButton;
    //bv wuyonglin add for adjust third app open camera ui 20200930 end
    //bv wuyonglin add for screen 2300px adjust all icon position 20201026 start
    private int mScreenHeight;
    private int mMarginBottomHeight;
    //bv wuyonglin add for screen 2300px adjust all icon position 20201026 end

    /**
     * ReviewUI used for show review after 3th use camera.
     * can override it and show it self ui.
     * @param app The IApp instance.
     * @param parentViewGroup camera root view
     */
    public ReviewUI(IApp app, ViewGroup parentViewGroup) {
        mApp = app;
        mActivity = mApp.getActivity();
        mParentViewGroup = parentViewGroup;
        //bv wuyonglin add for screen 2300px adjust all icon position 20201026 start
        WindowManager wm = (WindowManager) mApp.getActivity().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        display.getRealMetrics(dm);
        mScreenHeight = dm.heightPixels;
        Boolean isNotchSupport = false;

        DisplayInfo mInfo = new DisplayInfo();
        mApp.getActivity().getDisplay().getDisplayInfo(mInfo);
        isNotchSupport = false;
        if (mInfo.displayCutout != null){
            isNotchSupport = !mInfo.displayCutout.isEmpty();
        }

        LogHelper.i(TAG,"isNotchSupport = " + isNotchSupport);
        if (CameraUtil.isEdgeToEdgeEnabled(mApp.getActivity())) {
            if (mScreenHeight == 2300) {
                if(isNotchSupport){
                    mMarginBottomHeight = mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.review_ui_margin_bottom_2300px);
                }else{
                    mMarginBottomHeight = mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.review_ui_margin_bottom_2300px)
                            + mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.navigationbar_hide_bottom_margin);
                }
                LogHelper.i(TAG,"isEdgeToEdgeEnabled mMarginBottomHeight = " + mMarginBottomHeight);

            } else if (mScreenHeight == 2400) {
                mMarginBottomHeight = mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.review_ui_margin_bottom_2400px);
            }
        } else {
            if (mScreenHeight == 2300) {
                mMarginBottomHeight = mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.gesture_review_ui_margin_bottom_2300px);
            } else if (mScreenHeight == 2400) {
                mMarginBottomHeight = mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.gesture_review_ui_margin_bottom_2400px);
            }
            LogHelper.i(TAG," mMarginBottomHeight = " + mMarginBottomHeight);
        }
        //bv wuyonglin add for screen 2300px adjust all icon position 20201026 end
    }

    @Override
    public void initReviewUI(ReviewSpec spec) {
        mReviewSpec = spec;
    }

    @Override
    public void showReviewUI(Bitmap bitmap) {
        mBitmap = bitmap;
        LogHelper.d(TAG, "[showReviewUI]");
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                View viewLayout = mActivity.getLayoutInflater().inflate(R.layout.review_layout,
                        mParentViewGroup, true);
                if (CameraUtil.isHasNavigationBar(mApp.getActivity())) {
                    //get navigation bar height.
                    int navigationBarHeight = CameraUtil.getNavigationBarHeight(mApp.getActivity());
                    //set root view bottom margin to let the UI above the navigation bar.
                    RelativeLayout btnRoot =
                            (RelativeLayout) viewLayout.findViewById(R.id.review_btn_root);
                    FrameLayout.LayoutParams params =
                            (FrameLayout.LayoutParams) btnRoot.getLayoutParams();
                    if (CameraUtil.isTablet()) {
                        int displayRotation = CameraUtil.getDisplayRotation(mApp.getActivity());
                        LogHelper.d(TAG, " showReviewUI displayRotation  " + displayRotation);
                        if (displayRotation == 90 || displayRotation == 270) {
                            params.leftMargin += navigationBarHeight;
                            btnRoot.setLayoutParams(params);
                        } else {
                            params.bottomMargin += navigationBarHeight;
                            btnRoot.setLayoutParams(params);
                        }
                    } else {
                        params.bottomMargin += navigationBarHeight;
                        btnRoot.setLayoutParams(params);
                    }
                }


                mReviewRootView = viewLayout.findViewById(R.id.review_layout);
                int orientation = mApp.getGSensorOrientation();
                CameraUtil.rotateRotateLayoutChildView(mActivity, mReviewRootView,
                        orientation, false);
                mReviewRootView.setVisibility(View.VISIBLE);
                if (mReviewSpec.playListener != null) {
                    mPlayButton = (ImageView) mReviewRootView.findViewById(R.id.btn_play);
                    mPlayButton.setOnClickListener(mReviewSpec.playListener);
                    mPlayButton.setVisibility(View.VISIBLE);
                }
                if (mReviewSpec.saveListener != null) {
                    mSaveButton = (ImageView) mReviewRootView.findViewById(R.id.btn_save);
                    mSaveButton.setOnClickListener(mReviewSpec.saveListener);
                    mSaveButton.setContentDescription(SAVE_DONE_DESCRIPTION);
                    mSaveButton.setVisibility(View.VISIBLE);
                }
                if (mReviewSpec.retakeListener != null) {
                    mRetakeButton = (ImageView) mReviewRootView.findViewById(R.id.btn_retake);
                    mRetakeButton.setOnClickListener(mReviewSpec.retakeListener);
                    mReviewRootView.setVisibility(View.VISIBLE);
                }
                if (mBitmap != null) {
                    mReviewImage = (ImageView) mReviewRootView.findViewById(R.id.review_image);
                    mReviewImage.setImageBitmap(mBitmap);
                    mReviewImage.setVisibility(View.VISIBLE);
                }
                //bv wuyonglin add for adjust third app open camera ui 20200930 start
                mCancelButton = (ImageView) mReviewRootView.findViewById(R.id.btn_cancel);
                mCancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mActivity.finish();
                    }
                });
                mCancelButton.setVisibility(View.VISIBLE);
                //bv wuyonglin add for adjust third app open camera ui 20200930 end
                //bv wuyonglin add for screen 2300px adjust all icon position 20201026 start
                if (mScreenHeight == 2300 || mScreenHeight == 2400) {
                    RelativeLayout.LayoutParams mSaveButtonLayoutParams = (RelativeLayout.LayoutParams) mSaveButton.getLayoutParams();
                    mSaveButtonLayoutParams.bottomMargin = mMarginBottomHeight;
                    mSaveButton.setLayoutParams(mSaveButtonLayoutParams);
                    RelativeLayout.LayoutParams mRetakeButtonLayoutParams = (RelativeLayout.LayoutParams) mRetakeButton.getLayoutParams();
                    mRetakeButtonLayoutParams.bottomMargin = mMarginBottomHeight;
                    mRetakeButton.setLayoutParams(mRetakeButtonLayoutParams);
                    RelativeLayout.LayoutParams mCancelButtonLayoutParams = (RelativeLayout.LayoutParams) mCancelButton.getLayoutParams();
                    mCancelButtonLayoutParams.bottomMargin = mMarginBottomHeight;
                    mCancelButton.setLayoutParams(mCancelButtonLayoutParams);
                }
                //bv wuyonglin add for screen 2300px adjust all icon position 20201026 end
            }
        });

    }

    @Override
    public void hideReviewUI() {
        LogHelper.d(TAG, "[hideReviewUI]");
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mReviewRootView != null) {
                    mReviewRootView.setVisibility(View.INVISIBLE);
                }
                if (mPlayButton != null) {
                    mPlayButton.setVisibility(View.INVISIBLE);
                }
                if (mRetakeButton != null) {
                    mRetakeButton.setVisibility(View.INVISIBLE);
                }
                if (mSaveButton != null) {
                    mSaveButton.setVisibility(View.INVISIBLE);
                }
                //bv wuyonglin add for adjust third app open camera ui 20200930 start
                if (mCancelButton != null) {
                    mCancelButton.setVisibility(View.INVISIBLE);
                }
                //bv wuyonglin add for adjust third app open camera ui 20200930 end
                if (mBitmap != null) {
                    mBitmap.recycle();
                    mBitmap = null;
                }
                mParentViewGroup.removeView(mReviewRootView);
                mReviewRootView = null;
            }
        });
    }

    /**
     * update orientation to review ui,maybe use to rotate.
     * @param orientation the orientation want to set,the value only supported
     *                    0,90,180,270 and other value will ignore.
     */
    @Override
    public void updateOrientation(int orientation) {
        LogHelper.d(TAG, "[updateOrientation] orientation = " + orientation);
        switch (orientation) {
            case ORIENTATION_0:
            case ORIENTATION_90:
            case ORIENTATION_180:
            case ORIENTATION_270:
                mOrientation = orientation;
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        CameraUtil.rotateRotateLayoutChildView(mActivity, mReviewRootView,
                                mOrientation, true);
                    }
                });
                break;
            default:
                LogHelper.e(TAG, "error orientation = " + orientation);
                break;
        }
    }
}
