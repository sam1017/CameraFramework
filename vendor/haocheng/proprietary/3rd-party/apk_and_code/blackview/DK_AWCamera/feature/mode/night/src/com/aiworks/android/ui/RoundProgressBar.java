package com.aiworks.android.ui;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;

import com.mediatek.camera.R;

import java.lang.ref.WeakReference;

/**
 * 仿iphone带进度的进度条，线程安全的View，可直接在线程中更新进度
 *
 * @author xiaanming
 */
public class RoundProgressBar extends View {
    /**
     * 画笔对象的引用
     */
    private Paint paint;

    /**
     * 圆环的颜色
     */
    private int roundColor;

    /**
     * 圆环进度的颜色
     */
    private int roundProgressColor;

    /**
     * 中间进度百分比的字符串的颜色
     */
    private int textColor;

    /**
     * 中间进度百分比的字符串的字体
     */
    private float textSize;

    /**
     * 圆环的宽度
     */
    private float roundWidth;

    /**
     * 最大进度
     */
    private int max;

    /**
     * 当前进度
     */
    private int progress;
    /**
     * 是否显示中间的进度
     */
    private boolean textIsDisplayable;

    /**
     * 进度的风格，实心或者空心
     */
    private int style;

    protected boolean mReverse;

    public static final int STROKE = 0;
    public static final int FILL = 1;

    public RoundProgressBar(Context context) {
        this(context, null);
    }

    public RoundProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundProgressBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray mTypedArray = context.obtainStyledAttributes(attrs,
                R.styleable.RoundProgressBar);

        //获取自定义属性和默认值
        roundColor = mTypedArray.getColor(R.styleable.RoundProgressBar_roundColor, Color.RED);
        roundProgressColor = mTypedArray.getColor(R.styleable.RoundProgressBar_roundProgressColor, Color.GREEN);
        textColor = mTypedArray.getColor(R.styleable.RoundProgressBar_progressbarTextColor, Color.GREEN);
        textSize = mTypedArray.getDimension(R.styleable.RoundProgressBar_progressbarTextSize, 15);
        roundWidth = mTypedArray.getDimension(R.styleable.RoundProgressBar_roundWidth, 5);
        max = mTypedArray.getInteger(R.styleable.RoundProgressBar_progressbarMax, 100);
        textIsDisplayable = mTypedArray.getBoolean(R.styleable.RoundProgressBar_textIsDisplayable, true);
        style = mTypedArray.getInt(R.styleable.RoundProgressBar_style, 0);

        android.util.Log.i("RoundProgressBar","roundWidth = " + roundWidth + " style = " + style);
        paint = new Paint();
        //paint.setStyle(Paint.Style.FILL_AND_STROKE);


        mTypedArray.recycle();
    }


    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int Wcenter = getWidth() / 2; //获取圆心的x坐标
        int Hcenter = getHeight() /2;
        int radius = (int) (Wcenter - roundWidth / 2); //圆环的半径
        paint.setStrokeWidth(roundWidth);
        paint.setAntiAlias(true);
        paint.setColor(roundProgressColor);  //设置进度的颜色
        /**
         * 画圆弧
         */
        RectF oval = new RectF(Wcenter - radius, Hcenter - radius, Wcenter
                + radius, Hcenter + radius);  //用于定义的圆弧的形状和大小的界限

        android.util.Log.i("RoundProgressBar","onDraw Wcenter = " + Wcenter + " radius = " + radius + " Hcenter = " + Hcenter);

        switch (style){
            case FILL:
                paint.setStyle(Paint.Style.FILL);
                if (!mReverse) {
                    canvas.drawArc(oval, 0, 360 * progress / max, true, paint);  //根据进度画圆弧
                } else {
                    canvas.drawArc(oval, 360 - 360 * progress / max, 360 * progress / max, true, paint);  //根据进度画圆弧
                }
                break;
            case STROKE:
                paint.setStyle(Paint.Style.STROKE);
                canvas.drawArc(oval, 0, 360 * progress / max, false, paint); // 根据进度画圆弧
                break;
            default:
                break;
        }
    }


    public synchronized int getMax() {
        return max;
    }

    /**
     * 设置进度的最大值
     *
     * @param max
     */
    public synchronized void setMax(int max) {
        if (max < 0) {
            throw new IllegalArgumentException("max not less than 0");
        }
        this.max = max;
    }

    /**
     * 获取进度.需要同步
     *
     * @return
     */
    public synchronized int getProgress() {
        return progress;
    }

    public synchronized void setProgress(int progress) {
        if (progress < 0) {
            throw new IllegalArgumentException("progress not less than 0");
        }
        if (progress > max) {
            progress = max;
        }
        if (progress <= max) {
            this.progress = progress;
            postInvalidate();
        }

    }

    public int getCricleColor() {
        return roundColor;
    }

    public void setCricleColor(int cricleColor) {
        this.roundColor = cricleColor;
    }

    public int getCricleProgressColor() {
        return roundProgressColor;
    }

    public void setCricleProgressColor(int cricleProgressColor) {
        this.roundProgressColor = cricleProgressColor;
    }

    @Override
    public void setVisibility(int Visibility) {

        if (Visibility == View.VISIBLE) {
            mCricleProgressHand.sendEmptyMessage(1);
        } else {
            mCricleProgressHand.removeMessages(1);
            progress = 0;
        }

        super.setVisibility(Visibility);
    }

    private Handler mCricleProgressHand = new CricleProgressHand(this);

    private static class CricleProgressHand extends Handler {
        WeakReference<RoundProgressBar> mRef;

        CricleProgressHand(RoundProgressBar bar) {
            mRef = new WeakReference<RoundProgressBar>(bar);
        }

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            RoundProgressBar mOwer = mRef.get();
            if (mOwer == null) {
                //CoolLog.w(TAG, "ModeContextHandler reference get null");
                return;
            }
            if (msg.what == 1) {
                if (mOwer.progress >= 100) {
                    mOwer.mReverse = true;
                } else if (mOwer.progress <= 0) {
                    mOwer.mReverse = false;
                }
                if (!mOwer.mReverse) {
                    mOwer.progress += 5;
                } else {
                    mOwer.progress -= 5;
                }

                mOwer.mCricleProgressHand.sendEmptyMessageDelayed(1, 100);
                mOwer.postInvalidate();//invalidate();
            }
        }
    }


    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public float getTextSize() {
        return textSize;
    }

    public void setTextSize(float textSize) {
        this.textSize = textSize;
    }

    public float getRoundWidth() {
        return roundWidth;
    }

    public void setRoundWidth(float roundWidth) {
        this.roundWidth = roundWidth;
    }


}
