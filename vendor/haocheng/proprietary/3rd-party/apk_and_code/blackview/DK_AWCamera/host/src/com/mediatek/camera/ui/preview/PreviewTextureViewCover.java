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
import android.view.View;
import android.view.ViewGroup;
import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.Display;
import android.view.TextureView;
import android.view.WindowManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Paint;
import android.graphics.Color;

/**
 * A {@link TextureView} that can be adjusted to a specified aspect ratio.
 */

public class PreviewTextureViewCover extends View {

    private static final double ASPECT_TOLERANCE = 0.03;
    private double mAspectRatio = 0.0;
    private double mAspectRatio_18_9 = 2.0; //modify 18:9 preview size, wangsenhao, 2019.05.29
    int previewWidth;
    int previewHeight;
    private Paint paint;

    /**
     * Creates a new AutoFitTextureView.
     *
     * @param context The context to associate this view with.
     */
    public PreviewTextureViewCover(Context context) {
        this(context, null);
        
    }

    /**
     * Creates a new AutoFitTextureView.
     *
     * @param context The context to associate this view with.
     * @param attrs The attributes of the XML tag that is inflating the view.
     */
    public PreviewTextureViewCover(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Creates a new AutoFitTextureView.
     *
     * @param context The context to associate this view with.
     * @param attrs The attributes of the XML tag that is inflating the view.
     * @param defStyle An attribute in the current theme that contains a
     *        reference to a style resource that supplies default values for
     *        the view. Can be 0 to not look for defaults.
     */
    public PreviewTextureViewCover(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Sets the aspect ratio for this view. The size of the view will be measured based on the ratio
     * calculated from the parameters. Note that the actual sizes of parameters don't matter, that
     * is, calling setAspectRatio(2, 3) and setAspectRatio(4, 6) make the same result.
     * @
     * @param aspectRatio Current preview ratio.
     */
    public void setAspectRatio(double aspectRatio) {
        if (mAspectRatio != aspectRatio) {
            mAspectRatio = aspectRatio;
            requestLayout();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        paint=new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(1.5f);
        paint.setAlpha(230);
        paint.setStyle(Paint.Style.STROKE);
        paint.setShadowLayer(1, 0, 0, 0x88000000);

        //add by huangfei for disable gridlines when mAspectRatio is 0.0 start
        if(mAspectRatio== 0.0){
            return;
        }
        //add by huangfei for disable gridlines when mAspectRatio is 0.0 end

        //绘制网格
        int allSpellWidth=0;
        int allSpellHeight=0;
        int partCount=3;
        float spellWidth=(float) previewWidth/(float) partCount;
        float spellHeight=(float) previewHeight/(float) partCount;
        for(int i=0;i<=partCount;i++){           
            float vX= i==0 ? 1.5f : (i==partCount ? allSpellWidth-1.5f : allSpellWidth);
            if(i!=0&&i!=partCount){
                canvas.drawLine(0, allSpellHeight, previewHeight,allSpellHeight, paint);//横向
                canvas.drawLine(vX, 0, vX, previewHeight, paint);//纵向
            } 
            allSpellWidth+=spellWidth;
            allSpellHeight+=spellHeight;
        }
 
        /*if(downRectF!=null){
            Paint downRectFPaint=new Paint();
            downRectFPaint.setColor(Color.RED);
            downRectFPaint.setAntiAlias(true);
            downRectFPaint.setStyle(Paint.Style.FILL);
            canvas.drawRect(downRectF, downRectFPaint);
        }*/
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        previewWidth = MeasureSpec.getSize(widthMeasureSpec);
        previewHeight = MeasureSpec.getSize(heightMeasureSpec);
        boolean widthLonger = previewWidth > previewHeight;
        int longSide = (widthLonger ? previewWidth : previewHeight);
        int shortSide = (widthLonger ? previewHeight : previewWidth);
        if (mAspectRatio > 0) {
            double fullScreenRatio = findFullscreenRatio(getContext());
            if (Math.abs((mAspectRatio - fullScreenRatio)) <= ASPECT_TOLERANCE) {
                // full screen preview case
                if (longSide < shortSide * mAspectRatio) {
                    longSide = Math.round((float) (shortSide * mAspectRatio) / 2) * 2;
                } else {
                    shortSide = Math.round((float) (longSide / mAspectRatio) / 2) * 2;
                }
            } else {
                // standard (4:3) preview case
                if (longSide > shortSide * mAspectRatio) {
                    longSide = Math.round((float) (shortSide * mAspectRatio) / 2) * 2;
                } else {
                    //start, modify 18:9 preview size, wangsenhao, 2019.05.29
                    if(Math.abs((mAspectRatio - mAspectRatio_18_9)) <= ASPECT_TOLERANCE){
                        longSide = Math.round((float) (shortSide * mAspectRatio) / 2) * 2;
                    } else {
                        shortSide = Math.round((float) (longSide / mAspectRatio) / 2) * 2;
                    }
                    //end, modify 18:9 preview size, wangsenhao, 2019.05.29
                }
            }
        }
        if (widthLonger) {
            previewWidth = longSide;
            previewHeight = shortSide;
        } else {
            previewWidth = shortSide;
            previewHeight = longSide;
        }
        setMeasuredDimension(previewWidth, previewHeight);
    }

    private static double findFullscreenRatio(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point point = new Point();
        display.getRealSize(point);

        double fullscreen;
        if (point.x > point.y) {
            fullscreen = (double) point.x / point.y;
        } else {
            fullscreen = (double) point.y / point.x;
        }
        return fullscreen;
    }
}

