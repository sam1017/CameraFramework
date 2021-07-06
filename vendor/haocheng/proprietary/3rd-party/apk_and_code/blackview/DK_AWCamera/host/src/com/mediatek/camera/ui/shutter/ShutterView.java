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
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.aiworks.android.ui.RoundProgressBar;

import com.mediatek.camera.CameraActivity;
import com.mediatek.camera.R;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.utils.CameraUtil;

/**
 * A shutter button view, it has a text view and image view.
 */
class ShutterView extends RelativeLayout {

    /**
     * Shutter text clicked listener.
     */
        /**
         * Shutter text clicked callback.
         * @param index the index of the shutter position.
         */

    private ShutterButton mShutter;
	//bv liangchangwei add for shutter prpgressbar
    private ProgressBar mCircleProgressBar;
    private boolean isDown;
    /**
     * Constructor that is called when inflating a face view from XML.
     * @param context The Context the view is running in.
     * @param attrs The attributes of the XML tag that is inflating the view.
     * @see #View(Context, AttributeSet)
     */
    public ShutterView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }



    public void setDrawable(Drawable drawable) {
        mShutter.setImageDrawable(drawable);
    }

    public Drawable getDrawable() {
        return mShutter.getDrawable();
    }

    public void setOnShutterButtonListener(ShutterButton.OnShutterButtonListener listener) {
        mShutter.setOnShutterButtonListener(listener);
    }

	//bv liangchangwei add for shutter prpgressbar
    public void showCircleProgressBar() {
        Log.i("ShutterView", "showCircleProgressBar");
        if (mCircleProgressBar.getVisibility() == View.GONE) {
            mCircleProgressBar.bringToFront();
            mCircleProgressBar.setVisibility(View.VISIBLE);
/*            if (((CameraActivity)getContext()).isVideoIntent()) {
                mShutter.setImageResource(R.drawable.ic_shutter_video);
            } else {
                mShutter.setImageResource(R.drawable.ic_shutter_photo);
            }*/
            setEnabled(false);
        }
    }

    public void hideCircleProgressBar() {
        Log.i("ShutterView", "hideCircleProgressBar");
        if (mCircleProgressBar.getVisibility() == View.VISIBLE) {
            mCircleProgressBar.setVisibility(View.GONE);
            mShutter.clearAnimation();
/*            if (((CameraActivity)getContext()).isVideoIntent()) {
                mShutter.setImageResource(R.drawable.ic_shutter_video);
            } else {
                mShutter.setImageResource(R.drawable.ic_shutter_photo);
            }*/
            //mShutter.setImageResource(R.drawable.btn_camera_bottom_bar_shutter_button_middle);
            //mShutter.setShutterMode(com.aiworks.android.ui.ShutterButton.ShutterMode.PHOTO);
            setEnabled(true);
        }
    }
    @Override


    protected void onFinishInflate() {
        super.onFinishInflate();

        mShutter = (ShutterButton) findViewById(R.id.shutter_button);
	    //bv liangchangwei add for shutter prpgressbar
        mCircleProgressBar = (ProgressBar)findViewById(R.id.bottom_progressbar);

        //bv wuyonglin modify for cts verifier 20210513 start
        if(CameraUtil.is_videoHdr_Force && ((CameraActivity)getContext()).isVideoIntent()){
            mShutter.setImageResource(R.drawable.ic_shutter_video);
        } else {
            //mShutter.setImageResource(R.drawable.ic_shutter_photo);
            mShutter.setImageResource(R.drawable.ic_shutter_photo_resume);
        }
        //bv wuyonglin modify for cts verifier 20210513 end

        mShutter.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch(motionEvent.getAction()){
                    case MotionEvent.ACTION_UP:
                        if(isDown){
                            isDown = false;
                            if(CameraUtil.is_videoHdr_Force){
                                Log.i("ShutterView", "is_videoHdr_Force");
                            }else if ("Video".equals(((CameraActivity)getContext()).getAppUi().getCurrentMode()) || "SlowMotion".equals(((CameraActivity)getContext()).getAppUi().getCurrentMode())) {
                                Log.i("ShutterView", "mShutter.isVideoIntent");
                            } else {
                                mShutter.setImageResource(R.drawable.ic_shutter_photo_resume);
                                if(mShutter.getDrawable() instanceof AnimatedVectorDrawable){
                                    ((AnimatedVectorDrawable)mShutter.getDrawable()).start();
                                }
                            }
                        }
                        break;
                    case MotionEvent.ACTION_DOWN:
                        if(!isDown){
                            isDown = true;
                            if(CameraUtil.is_videoHdr_Force){
                                Log.i("ShutterView", "is_videoHdr_Force");
                            }else if ("Video".equals(((CameraActivity)getContext()).getAppUi().getCurrentMode()) || "SlowMotion".equals(((CameraActivity)getContext()).getAppUi().getCurrentMode())) {
                                Log.i("ShutterView", "ACTION_DOWN mShutter.isVideoIntent");
                            } else {
                                mShutter.setImageResource(R.drawable.ic_shutter_photo_pressed);
                                if(mShutter.getDrawable() instanceof AnimatedVectorDrawable){
                                    ((AnimatedVectorDrawable)mShutter.getDrawable()).start();
                                }
                            }
                        }
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (mShutter != null) {
            mShutter.setEnabled(enabled);
            mShutter.setClickable(enabled);
        }
    }






}
